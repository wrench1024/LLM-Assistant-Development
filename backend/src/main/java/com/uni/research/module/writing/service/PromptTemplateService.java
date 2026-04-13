package com.uni.research.module.writing.service;

import com.uni.research.module.writing.dto.PromptTemplateCreateRequest;
import com.uni.research.module.writing.dto.PromptTemplateDTO;
import com.uni.research.module.writing.dto.PromptTemplateUpdateRequest;
import com.uni.research.module.writing.entity.PromptTemplate;

import java.util.List;

/**
 * 提示词模板服务接口
 */
public interface PromptTemplateService {
    
    /**
     * 初始化系统内置模板
     */
    void initBuiltinTemplates();
    
    /**
     * 获取指定文体和动作的模板（优先用户自定义，其次系统内置）
     */
    PromptTemplate getTemplate(Long userId, String genre, String action, Long templateId);
    
    /**
     * 查询用户的所有模板（包括系统内置）
     */
    List<PromptTemplateDTO> listTemplates(Long userId, String genre, String action);
    
    /**
     * 创建用户自定义模板
     */
    PromptTemplateDTO createTemplate(Long userId, PromptTemplateCreateRequest request);
    
    /**
     * 更新用户自定义模板
     */
    PromptTemplateDTO updateTemplate(Long userId, PromptTemplateUpdateRequest request);
    
    /**
     * 删除用户自定义模板
     */
    void deleteTemplate(Long userId, Long templateId);
    
    /**
     * 获取模板详情
     */
    PromptTemplateDTO getTemplateDetail(Long userId, Long templateId);
}
