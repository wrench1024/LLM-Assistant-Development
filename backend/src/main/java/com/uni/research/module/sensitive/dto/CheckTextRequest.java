package com.uni.research.module.sensitive.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 文本检测请求 DTO
 */
@Data
public class CheckTextRequest {
    
    /**
     * 待检测的文本内容
     */
    @NotBlank(message = "文本内容不能为空")
    private String text;
}
