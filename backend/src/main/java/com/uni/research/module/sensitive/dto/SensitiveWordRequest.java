package com.uni.research.module.sensitive.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 敏感词请求 DTO
 */
@Data
public class SensitiveWordRequest {
    
    /**
     * 敏感词内容
     */
    @NotBlank(message = "敏感词内容不能为空")
    private String word;
    
    /**
     * 敏感词类型：1-政治敏感 2-色情暴力 3-广告营销 4-其他
     */
    @NotNull(message = "敏感词类型不能为空")
    private Integer type;
    
    /**
     * 是否启用：0-禁用 1-启用
     */
    private Integer enabled = 1;
}
