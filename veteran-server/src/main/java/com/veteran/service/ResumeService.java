package com.veteran.service;

import com.veteran.dto.ResumeEditDTO;
import com.veteran.vo.PortraitVO;
import java.util.Map;

public interface ResumeService {

    /**
     * 生成简历 PDF，返回文件路径
     */
    String generatePdf(Long studentId, String templateCode);

    /**
     * 获取简历 HTML 预览
     */
    String getPreviewHtml(Long studentId, String templateCode);

    /**
     * 根据编辑数据生成 PDF，返回文件路径
     */
    String generatePdfWithEdit(Long studentId, ResumeEditDTO editData, String templateCode);

    /**
     * 根据编辑数据获取 HTML 预览
     */
    String getPreviewHtmlWithEdit(Long studentId, ResumeEditDTO editData, String templateCode);

    /**
     * 获取可用模板列表
     */
    java.util.List<Map<String, Object>> getTemplates();

    /**
     * 计算数据哈希（用于缓存校验）
     */
    String computeContentHash(PortraitVO portrait);

    /**
     * 获取简历编辑初始数据（从学生画像回填表单）
     */
    Map<String, Object> getEditInitData(Long studentId);

    /**
     * 获取学生最近简历生成记录
     * @param limit 最多返回条数，<=0 表示不限制（最多 20）
     */
    java.util.List<Map<String, Object>> getRecentRecords(Long studentId, int limit);
}
