-- =============================================================================
-- 提示词模板表
-- =============================================================================

CREATE TABLE IF NOT EXISTS writing_prompt_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    genre VARCHAR(50) NOT NULL COMMENT '文体类型：academic_paper, literature_review, research_report',
    action VARCHAR(50) NOT NULL COMMENT '动作类型：polish, expand, continue, fix_grammar, translate, summarize',
    prompt_content TEXT NOT NULL COMMENT '提示词内容（支持变量占位符）',
    is_builtin TINYINT(1) DEFAULT 0 COMMENT '是否为系统内置模板：0-否，1-是',
    user_id BIGINT COMMENT '所属用户ID（系统模板为NULL）',
    description VARCHAR(500) COMMENT '模板描述',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_genre_action (genre, action),
    INDEX idx_user_id (user_id),
    INDEX idx_builtin (is_builtin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词模板表';
