package com.uni.research.module.sensitive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词实体
 */
@Data
@TableName("sensitive_word")
public class SensitiveWord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 敏感词内容
     */
    private String word;
    
    /**
     * 敏感词类型：1-政治敏感 2-色情暴力 3-广告营销 4-其他
     */
    private Integer type;
    
    /**
     * 是否启用：0-禁用 1-启用
     */
    private Integer enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
