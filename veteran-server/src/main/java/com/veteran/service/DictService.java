package com.veteran.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.veteran.dto.DictItemDTO;

import java.util.List;

public interface DictService {

    <T> List<T> listAll(Class<T> clazz);

    void refreshCache();

    <T> void addDictItem(Class<T> clazz, BaseMapper<T> mapper, DictItemDTO dto);

    <T> void updateDictItem(Class<T> clazz, BaseMapper<T> mapper, Long id, DictItemDTO dto);

    <T> void deleteDictItem(Class<T> clazz, BaseMapper<T> mapper, Long id);
}