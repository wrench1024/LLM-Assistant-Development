package com.uni.research.module.writing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新提示词模板请求
 */
@Data
public class PromptTemplateUpdateRequest {
    
    @NotNull(message = "模板ID不能为空")
    private Long id;
    
    private String name;
    private String promptContent;
    private String description;
    private Integer sortOrder;
    private Boolean enabled;
}
