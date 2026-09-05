package com.veteran.service;

import com.veteran.entity.SysConfig;

import java.util.List;

public interface SysConfigService {

    List<SysConfig> listAll();

    void batchUpdate(List<SysConfig> configs);

    String getConfigValue(String key);
}
