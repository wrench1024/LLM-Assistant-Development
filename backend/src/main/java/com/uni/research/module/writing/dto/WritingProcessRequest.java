package com.uni.research.module.writing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 写作辅助处理请求（新版）
 */
@Data
public class WritingProcessRequest {
    
    /**
     * 待处理的文本内容
     */
    @NotBlank(message = "文本内容不能为空")
    private String text;
    
    /**
     * 文体类型：academic_paper, literature_review, research_report
     */
    @NotBlank(message = "文体类型不能为空")
    private String genre;
    
    /**
     * 动作类型：polish, expand, continue, fix_grammar, translate, summarize
     */
    @NotBlank(message = "动作类型不能为空")
    private String action;
    
    /**
     * 可选的上下文信息
     */
    private String context;
    
    /**
     * 可选的自定义提示词模板ID（如果不指定则使用系统默认）
     */
    private Long templateId;
}
