package com.veteran.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.veteran.dto.DictItemDTO;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private static final String DICT_CACHE_PREFIX = "veteran:dict:";

    private final DictBranchMapper dictBranchMapper;
    private final DictHonorMapper dictHonorMapper;
    private final DictCertMapper dictCertMapper;
    private final DictPostTypeMapper dictPostTypeMapper;
    private final DictJobTypeMapper dictJobTypeMapper;
    private final DictHonorCategoryMapper dictHonorCategoryMapper;
    private final DictLeaderPostMapper dictLeaderPostMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> listAll(Class<T> clazz) {
        String cacheKey = DICT_CACHE_PREFIX + clazz.getSimpleName();

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return (List<T>) cached;
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过缓存读取: {}", e.getMessage());
        }

        List<T> result = loadFromDb(clazz);

        try {
            redisTemplate.opsForValue().set(cacheKey, result, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过缓存写入: {}", e.getMessage());
        }

        return result;
    }

    @Override
    public void refreshCache() {
        try {
            redisTemplate.delete(DICT_CACHE_PREFIX + DictBranch.class.getSimpleName());
            redisTemplate.delete(DICT_CACHE_PREFIX + DictHonor.class.getSimpleName());
            redisTemplate.delete(DICT_CACHE_PREFIX + DictCert.class.getSimpleName());
            redisTemplate.delete(DICT_CACHE_PREFIX + DictPostType.class.getSimpleName());
            redisTemplate.delete(DICT_CACHE_PREFIX + DictJobType.class.getSimpleName());
            redisTemplate.delete(DICT_CACHE_PREFIX + DictHonorCategory.class.getSimpleName());
            redisTemplate.delete(DICT_CACHE_PREFIX + DictLeaderPost.class.getSimpleName());
            log.info("字典缓存已刷新");
        } catch (Exception e) {
            log.warn("Redis 不可用，无法刷新缓存: {}", e.getMessage());
        }
    }

    // ========== 字典 CRUD ==========

    @Override
    @SuppressWarnings("unchecked")
    public <T> void addDictItem(Class<T> clazz, BaseMapper<T> mapper, DictItemDTO dto) {
        try {
            T entity = clazz.getDeclaredConstructor().newInstance();
            setDictFields(entity, dto);
            mapper.insert(entity);
            clearCache(clazz);
            log.info("新增字典条目: type={}, code={}", clazz.getSimpleName(), dto.getCode());
        } catch (Exception e) {
            throw new RuntimeException("新增字典条目失败: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void updateDictItem(Class<T> clazz, BaseMapper<T> mapper, Long id, DictItemDTO dto) {
        try {
            T entity = clazz.getDeclaredConstructor().newInstance();
            setDictId(entity, id);
            setDictFields(entity, dto);
            mapper.updateById(entity);
            clearCache(clazz);
            log.info("更新字典条目: type={}, id={}", clazz.getSimpleName(), id);
        } catch (Exception e) {
            throw new RuntimeException("更新字典条目失败: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void deleteDictItem(Class<T> clazz, BaseMapper<T> mapper, Long id) {
        mapper.deleteById(id);
        clearCache(clazz);
        log.info("删除字典条目: type={}, id={}", clazz.getSimpleName(), id);
    }

    private <T> void clearCache(Class<T> clazz) {
        try {
            redisTemplate.delete(DICT_CACHE_PREFIX + clazz.getSimpleName());
        } catch (Exception e) {
            log.warn("清除字典缓存失败: {}", e.getMessage());
        }
    }

    private void setDictId(Object entity, Long id) throws Exception {
        entity.getClass().getMethod("setId", Long.class).invoke(entity, id);
    }

    private void setDictFields(Object entity, DictItemDTO dto) throws Exception {
        Class<?> clazz = entity.getClass();
        try { clazz.getMethod("setCode", String.class).invoke(entity, dto.getCode()); } catch (NoSuchMethodException ignored) {}
        try { clazz.getMethod("setName", String.class).invoke(entity, dto.getName()); } catch (NoSuchMethodException ignored) {}
        try { clazz.getMethod("setSortOrder", Integer.class).invoke(entity, dto.getSortOrder()); } catch (NoSuchMethodException ignored) {}
        try { clazz.getMethod("setDefaultPoints", Integer.class).invoke(entity, dto.getSortOrder()); } catch (NoSuchMethodException ignored) {}
        try { clazz.getMethod("setValidityMonths", Integer.class).invoke(entity, dto.getSortOrder()); } catch (NoSuchMethodException ignored) {}
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> loadFromDb(Class<T> clazz) {
        if (clazz == DictBranch.class) {
            return (List<T>) dictBranchMapper.selectList(null);
        } else if (clazz == DictHonor.class) {
            return (List<T>) dictHonorMapper.selectList(null);
        } else if (clazz == DictCert.class) {
            return (List<T>) dictCertMapper.selectList(null);
        } else if (clazz == DictPostType.class) {
            return (List<T>) dictPostTypeMapper.selectList(null);
        } else if (clazz == DictJobType.class) {
            return (List<T>) dictJobTypeMapper.selectList(null);
        } else if (clazz == DictHonorCategory.class) {
            return (List<T>) dictHonorCategoryMapper.selectList(null);
        } else if (clazz == DictLeaderPost.class) {
            return (List<T>) dictLeaderPostMapper.selectList(null);
        }
        throw new IllegalArgumentException("Unknown dict type: " + clazz.getName());
    }

    // ========== dictType 字符串到实体类的映射 ==========

    public Class<?> getEntityClass(String dictType) {
        switch (dictType) {
            case "branch": return DictBranch.class;
            case "honor": return DictHonor.class;
            case "cert": return DictCert.class;
            case "post-type": return DictPostType.class;
            case "job-type": return DictJobType.class;
            case "honor-category": return DictHonorCategory.class;
            case "leader-post": return DictLeaderPost.class;
            default: throw new IllegalArgumentException("未知字典类型: " + dictType);
        }
    }

    @SuppressWarnings("rawtypes")
    public BaseMapper getMapper(String dictType) {
        switch (dictType) {
            case "branch": return dictBranchMapper;
            case "honor": return dictHonorMapper;
            case "cert": return dictCertMapper;
            case "post-type": return dictPostTypeMapper;
            case "job-type": return dictJobTypeMapper;
            case "honor-category": return dictHonorCategoryMapper;
            case "leader-post": return dictLeaderPostMapper;
            default: throw new IllegalArgumentException("未知字典类型: " + dictType);
        }
    }
}
