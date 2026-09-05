package com.veteran.service;

public interface PointsService {

    int getTotalPoints(Long studentId);

    void addPoints(Long studentId, int points, String reasonType, Long relatedId, String sourceTable, String operator, String remark);

    void deductPoints(Long studentId, int points, String reasonType, Long relatedId, String sourceTable, String operator, String remark);

    void refreshPointsCache(Long studentId);
}