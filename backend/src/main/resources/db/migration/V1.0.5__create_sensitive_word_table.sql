-- 创建敏感词表
CREATE TABLE IF NOT EXISTS `sensitive_word` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `word` VARCHAR(100) NOT NULL COMMENT '敏感词内容',
    `type` TINYINT NOT NULL DEFAULT 4 COMMENT '敏感词类型：1-政治敏感 2-色情暴力 3-广告营销 4-其他',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`, `deleted`),
    KEY `idx_type` (`type`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- 插入示例敏感词
INSERT INTO `sensitive_word` (`word`, `type`, `enabled`) VALUES
('测试敏感词', 4, 1),
('违禁词', 1, 1),
('广告', 3, 1);
