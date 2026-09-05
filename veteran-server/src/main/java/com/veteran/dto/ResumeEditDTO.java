package com.veteran.dto;

import lombok.Data;
import java.util.List;

/**
 * 简历编辑 DTO — 用户在生成简历前可修改的字段
 * 只有非 null 的字段才会覆盖 PortraitVO 中的原始数据
 */
@Data
public class ResumeEditDTO {

    // ===== 基本信息 =====
    private String name;
    private String phone;
    private String email;
    private String avatarUrl;
    private String college;
    private String major;
    private String grade;

    // ===== 个人简介 =====
    private String personalSummary;

    // ===== 求职意向 =====
    private String expectedJob;
    private String expectedCity;
    private String expectedSalary;

    // ===== 技能 =====
    private List<String> skills;

    // ===== 实习经历（用户自填） =====
    private List<InternshipItem> internships;

    // ===== 项目经历（用户自填） =====
    private List<ProjectItem> projects;

    // ===== 自定义模块（可多个，用户灵活扩展） =====
    private List<CustomSection> customSections;

    // ===== 模块开关 =====
    private Boolean showHonors;
    private Boolean showCerts;
    private Boolean showServices;
    private Boolean showAcademic;
    private Boolean showIntention;
    private Boolean showInternships;
    private Boolean showProjects;

    // ===== 模板 =====
    private String templateCode;

    // ===== 自定义数据结构 =====

    /** 实习经历条目 */
    @Data
    public static class InternshipItem {
        /** 实习单位 */
        private String company;
        /** 担任职位 */
        private String position;
        /** 开始时间 */
        private String startDate;
        /** 结束时间 */
        private String endDate;
        /** 工作内容描述 */
        private String description;
    }

    /** 项目经历条目 */
    @Data
    public static class ProjectItem {
        /** 项目名称 */
        private String name;
        /** 担任角色 */
        private String role;
        /** 开始时间 */
        private String startDate;
        /** 结束时间 */
        private String endDate;
        /** 项目描述/职责 */
        private String description;
    }

    /** 自定义模块（标题 + 内容，用户可自由扩展，如"校园经历""特长爱好"等） */
    @Data
    public static class CustomSection {
        /** 模块标题 */
        private String title;
        /** 模块内容（多行文本，支持换行） */
        private String content;
    }
}
