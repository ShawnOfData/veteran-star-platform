package com.veteran.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veteran.entity.AIAnalysisResult;
import com.veteran.mapper.AIAnalysisResultMapper;
import com.veteran.service.AIService;
import com.veteran.service.ProfileService;
import com.veteran.vo.AIAnalysisVO;
import com.veteran.vo.PortraitVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private static final String CACHE_KEY_PREFIX = "ai:analysis:";
    private static final long CACHE_TTL_HOURS = 24;

    private final ProfileService profileService;
    private final AIAnalysisResultMapper resultMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AIAnalysisVO analyze(Long studentId, boolean forceRefresh) {
        // 1. 查缓存
        String cacheKey = CACHE_KEY_PREFIX + studentId;
        if (!forceRefresh) {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                try {
                    return objectMapper.convertValue(cached, AIAnalysisVO.class);
                } catch (Exception e) {
                    log.warn("缓存反序列化失败，重新生成: {}", e.getMessage());
                }
            }
        }

        // 2. 获取学生画像数据
        PortraitVO portrait = profileService.getPortrait(studentId);
        if (portrait.getBasic() == null) {
            log.warn("学生 {} 无基础数据，跳过 AI 分析", studentId);
            return buildEmptyResult();
        }

        // 3. 构建 Prompt
        String prompt = buildPrompt(portrait);
        String aiResponse = callAI(prompt);
        if (aiResponse == null) {
            return buildFallbackResult(portrait);
        }

        // 4. 解析 AI 响应
        AIAnalysisVO vo = parseAIResponse(aiResponse, portrait);
        if (vo == null) {
            return buildFallbackResult(portrait);
        }

        // 5. 持久化到数据库
        saveToDb(studentId, vo);

        // 6. 缓存
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_TTL_HOURS, TimeUnit.HOURS);

        return vo;
    }

    @Override
    public void clearCache(Long studentId) {
        redisTemplate.delete(CACHE_KEY_PREFIX + studentId);
    }

    // ========== 内部方法 ==========

    private String buildPrompt(PortraitVO p) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位退役军人职业规划专家。请根据以下退役大学生信息，用JSON格式返回能力分析。\n\n");
        sb.append("【基本信息】姓名:").append(p.getBasic().getName())
            .append("，学院:").append(p.getBasic().getCollege())
            .append("，专业:").append(p.getBasic().getMajor());
        if (p.getMilitary() != null) {
            sb.append("\n【服役】").append(p.getMilitary().getBranchName())
                .append("，").append(p.getMilitary().getLeaderPostName())
                .append("，").append(p.getMilitary().getPosition())
                .append("，服役").append(p.getMilitary().getServiceYears());
        }
        if (p.getHonors() != null && !p.getHonors().isEmpty()) {
            sb.append("\n【荣誉】");
            sb.append(p.getHonors().stream().map(h ->
                h.getName() + "(" + h.getPoints() + "分)").collect(Collectors.joining("、")));
        }
        if (p.getCerts() != null && !p.getCerts().isEmpty()) {
            sb.append("\n【证书】");
            sb.append(p.getCerts().stream().map(c -> c.getName()).collect(Collectors.joining("、")));
        }
        if (p.getServices() != null && !p.getServices().isEmpty()) {
            int totalHours = (int) p.getServices().stream().mapToDouble(s -> s.getHours() != null ? s.getHours() : 0).sum();
            sb.append("\n【社会服务】参与").append(p.getServices().size())
                .append("次，累计").append(totalHours).append("小时");
        }
        if (p.getPoints() != null) {
            sb.append("\n【积分】累计").append(p.getPoints().getTotal()).append("分");
        }
        sb.append("\n\n请严格按以下JSON格式返回（不要Json代码块标记，不要任何解释）：\n");
        sb.append("{\n");
        sb.append("  \"abilityTags\": [\"能力1\", \"能力2\", ...],\n");
        sb.append("  \"abilityScores\": {\"能力1\": 85, \"能力2\": 92},\n");
        sb.append("  \"pointLevel\": \"A或B或C或D\",\n");
        sb.append("  \"recommendedJobs\": [\"岗位1\", \"岗位2\"],\n");
        sb.append("  \"overallComment\": \"综合评语2-3句话\"\n");
        sb.append("}");
        return sb.toString();
    }

    private String callAI(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("AI API Key 未配置，使用兜底结果");
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("max_tokens", 1000);

            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v1/chat/completions",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
            );

            Map<String, Object> resp = response.getBody();
            if (resp != null && resp.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("AI API 调用失败: {}", e.getMessage());
        }
        return null;
    }

    private AIAnalysisVO parseAIResponse(String aiResponse, PortraitVO p) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) return null;
        try {
            // 清理可能的 markdown 代码块标记
            String json = aiResponse.trim();
            if (json.startsWith("```json")) json = json.substring(7);
            if (json.startsWith("```")) json = json.substring(3);
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
            json = json.trim();

            return objectMapper.readValue(json, AIAnalysisVO.class);
        } catch (Exception e) {
            log.error("AI响应解析失败: {}", e.getMessage());
            return null;
        }
    }

    private AIAnalysisVO buildFallbackResult(PortraitVO p) {
        AIAnalysisVO vo = new AIAnalysisVO();

        int honors = p.getHonors() != null ? p.getHonors().size() : 0;
        int certs = p.getCerts() != null ? p.getCerts().size() : 0;
        int svcHours = p.getServices() != null ? (int) p.getServices().stream()
                .mapToDouble(s -> s.getHours() != null ? s.getHours() : 0).sum() : 0;
        int pts = p.getPoints() != null ? p.getPoints().getTotal() : 0;

        // 规则引擎兜底
        List<String> tags = new ArrayList<>();
        Map<String, Integer> scores = new LinkedHashMap<>();

        int lead = Math.min(100, honors * 25 + svcHours);
        tags.add("领导力"); scores.put("领导力", lead);
        int exec = Math.min(100, pts / 2 + honors * 15);
        tags.add("执行力"); scores.put("执行力", exec);
        int team = Math.min(100, svcHours * 3);
        tags.add("团队协作"); scores.put("团队协作", team);
        int skill = Math.min(100, certs * 20 + (p.getAcademic() != null ? p.getAcademic().size() * 8 : 0));
        tags.add("专业技能"); scores.put("专业技能", skill);
        int respond = Math.min(100, honors * 20 + svcHours);
        tags.add("应急响应"); scores.put("应急响应", respond);
        int resp = Math.min(100, pts / 2 + svcHours);
        tags.add("责任心"); scores.put("责任心", resp);

        vo.setAbilityTags(tags);
        vo.setAbilityScores(scores);

        int avg = (lead + exec + team + skill + respond + resp) / 6;
        String level;
        if (avg >= 80) level = "A";
        else if (avg >= 60) level = "B";
        else if (avg >= 40) level = "C";
        else level = "D";
        vo.setPointLevel(level);

        List<String> jobs = new ArrayList<>();
        if (lead >= 70) jobs.add("基层管理");
        if (exec >= 60) jobs.add("部队文职");
        if (respond >= 60) jobs.add("应急救援");
        if (skill >= 50) jobs.add("国企技术岗");
        if (lead >= 60) jobs.add("公务员");
        vo.setRecommendedJobs(jobs);

        vo.setOverallComment(
            p.getBasic().getName() + "同学服役期间" +
            (honors > 0 ? "获得" + honors + "项荣誉，" : "") +
            "累计" + pts + "积分，能力综合评定为" + level + "级。" +
            (p.getMilitary() != null ? (p.getMilitary().getServiceYears() + "军旅生涯") : "军旅生涯") +
            "为未来职业发展奠定了坚实基础。"
        );

        return vo;
    }

    private AIAnalysisVO buildEmptyResult() {
        AIAnalysisVO vo = new AIAnalysisVO();
        vo.setAbilityTags(Collections.emptyList());
        vo.setAbilityScores(Collections.emptyMap());
        vo.setPointLevel("D");
        vo.setRecommendedJobs(Collections.emptyList());
        vo.setOverallComment("数据不足，无法生成分析报告");
        return vo;
    }

    private void saveToDb(Long studentId, AIAnalysisVO vo) {
        try {
            LambdaQueryWrapper<AIAnalysisResult> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AIAnalysisResult::getStudentId, studentId);

            AIAnalysisResult entity = resultMapper.selectOne(wrapper);
            if (entity == null) {
                entity = new AIAnalysisResult();
                entity.setStudentId(studentId);
            }
            entity.setAbilityTags(objectMapper.writeValueAsString(vo.getAbilityTags()));
            entity.setAbilityScores(objectMapper.writeValueAsString(vo.getAbilityScores()));
            entity.setPointLevel(vo.getPointLevel());
            entity.setRecommendedJobs(objectMapper.writeValueAsString(vo.getRecommendedJobs()));
            entity.setOverallComment(vo.getOverallComment());

            if (entity.getId() == null) {
                resultMapper.insert(entity);
            } else {
                resultMapper.updateById(entity);
            }
        } catch (Exception e) {
            log.error("AI分析结果存库失败", e);
        }
    }
}
