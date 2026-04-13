package com.uni.research.module.sensitive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文本检测响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckTextResponse {
    
    /**
     * 是否包含敏感词
     */
    private Boolean hasSensitive;
    
    /**
     * 检测到的敏感词列表
     */
    private List<SensitiveWordMatch> matches;
    
    /**
     * 敏感词匹配信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensitiveWordMatch {
        /**
         * 敏感词内容
         */
        private String word;
        
        /**
         * 敏感词类型
         */
        private Integer type;
        
        /**
         * 在文本中的起始位置
         */
        private Integer startIndex;
        
        /**
         * 在文本中的结束位置
         */
        private Integer endIndex;
    }
}
