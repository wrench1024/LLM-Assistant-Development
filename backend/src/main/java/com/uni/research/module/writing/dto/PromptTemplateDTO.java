package com.uni.research.module.writing.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提示词模板 DTO
 */
@Data
public class PromptTemplateDTO {
    
    private Long id;
    private String name;
    private String genre;
    private String genreName;
    private String action;
    private String actionName;
    private String promptContent;
    private Boolean isBuiltin;
    private Long userId;
    private String description;
    private Integer sortOrder;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
