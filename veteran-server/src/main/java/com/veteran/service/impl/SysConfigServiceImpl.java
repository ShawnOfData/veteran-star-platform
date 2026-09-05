package com.veteran.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veteran.entity.SysConfig;
import com.veteran.mapper.SysConfigMapper;
import com.veteran.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private static final String CACHE_KEY = "veteran:sys_config";
    private static final String CACHE_KEY_PREFIX = "veteran:sys_config:";

    private final SysConfigMapper sysConfigMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<SysConfig> listAll() {
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached != null) {
                return (List<SysConfig>) cached;
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过缓存读取: {}", e.getMessage());
        }

        List<SysConfig> list = sysConfigMapper.selectList(null);

        try {
            redisTemplate.opsForValue().set(CACHE_KEY, list, 1, TimeUnit.HOURS);
            for (SysConfig config : list) {
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + config.getConfigKey(),
                        config.getConfigValue(), 1, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过缓存写入: {}", e.getMessage());
        }

        return list;
    }

    @Override
    public void batchUpdate(List<SysConfig> configs) {
        for (SysConfig config : configs) {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysConfig::getConfigKey, config.getConfigKey());
            SysConfig exist = sysConfigMapper.selectOne(wrapper);
            if (exist != null) {
                exist.setConfigValue(config.getConfigValue());
                sysConfigMapper.updateById(exist);
            } else {
                sysConfigMapper.insert(config);
            }
        }
        refreshCache();
    }

    @Override
    public String getConfigValue(String key) {
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + key);
            if (cached != null) {
                return cached.toString();
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过缓存读取: {}", e.getMessage());
        }

        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, key);
        SysConfig config = sysConfigMapper.selectOne(wrapper);
        String value = config != null ? config.getConfigValue() : null;

        if (value != null) {
            try {
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + key, value, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("Redis 不可用，跳过缓存写入: {}", e.getMessage());
            }
        }

        return value;
    }

    private void refreshCache() {
        try {
            redisTemplate.delete(CACHE_KEY);
            // 简单清理：全量刷新时清除列表缓存，下次查询时重建
        } catch (Exception e) {
            log.warn("Redis 不可用，无法刷新缓存: {}", e.getMessage());
        }
    }
}
