package com.veteran.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 学生画像 VO
 */
@Data
public class PortraitVO {

    /** 基本信息 */
    private BasicInfo basic;

    /** 积分总览 */
    private PointsInfo points;

    /** 服役经历 */
    private MilitaryInfo military;

    /** 荣誉列表 */
    private List<HonorInfo> honors;

    /** 技能证书列表 */
    private List<CertInfo> certs;

    /** 社会服务记录 */
    private List<ServiceInfo> services;

    /** 学业表现 */
    private List<AcademicInfo> academic;

    /** 服务意愿 */
    private WillingnessInfo willingness;

    /** 就业意向 */
    private IntentionInfo intention;

    @Data
    public static class BasicInfo {
        private Long id;
        private String studentNo;
        private String name;
        private String gender;
        private String ethnicity;
        private String birthDate;
        private String nativePlace;
        private String politicalStatus;
        private String college;
        private String major;
        private String grade;
        private Integer status;
        private String retireDate;
    }

    @Data
    public static class PointsInfo {
        private Integer total;
        private Integer honorPoints;
        private Integer certPoints;
        private Integer servicePoints;
        private Integer academicPoints;
        private String level;
    }

    @Data
    public static class MilitaryInfo {
        private String branchName;
        private String startDate;
        private String endDate;
        private String leaderPostName;
        private String position;
        private String serviceYears;
    }

    @Data
    public static class HonorInfo {
        private String name;
        private String categoryName;
        private String awardDate;
        private Integer points;
    }

    @Data
    public static class CertInfo {
        private String name;
        private String certNo;
        private String obtainDate;
        private String validUntil;
        private Integer points;
    }

    @Data
    public static class ServiceInfo {
        private String title;
        private String content;
        private String serviceDate;
        private Double hours;
        private Integer points;
    }

    @Data
    public static class AcademicInfo {
        private String semester;
        private Double gpa;
        private String scholarship;
        private Integer points;
    }

    @Data
    public static class WillingnessInfo {
        private List<String> postTypes;
        private String availableTime;
    }

    @Data
    public static class IntentionInfo {
        private List<String> jobTypes;
        private String expectedSalary;
        private String expectedCity;
    }
}
