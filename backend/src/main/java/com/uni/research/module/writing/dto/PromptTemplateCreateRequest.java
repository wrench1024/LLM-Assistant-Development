package com.uni.research.module.writing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建提示词模板请求
 */
@Data
public class PromptTemplateCreateRequest {
    
    @NotBlank(message = "模板名称不能为空")
    private String name;
    
    @NotBlank(message = "文体类型不能为空")
    private String genre;
    
    @NotBlank(message = "动作类型不能为空")
    private String action;
    
    @NotBlank(message = "提示词内容不能为空")
    private String promptContent;
    
    private String description;
    
    private Integer sortOrder;
    
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;
}
