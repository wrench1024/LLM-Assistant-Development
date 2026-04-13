package com.uni.research.module.writing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提示词模板实体
 */
@Data
@TableName("writing_prompt_template")
public class PromptTemplate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 文体类型：academic_paper, literature_review, research_report
     */
    private String genre;
    
    /**
     * 动作类型：polish, expand, continue, fix_grammar, translate, summarize
     */
    private String action;
    
    /**
     * 提示词内容（支持变量占位符）
     */
    private String promptContent;
    
    /**
     * 是否为系统内置模板
     */
    private Boolean isBuiltin;
    
    /**
     * 所属用户ID（系统模板为NULL）
     */
    private Long userId;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
