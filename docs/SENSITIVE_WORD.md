# 敏感词管理功能

## 功能说明

敏感词管理功能提供了完整的敏感词检测和标红显示能力。

### 核心特性

- ✅ 敏感词增删改查
- ✅ 敏感词分类管理（政治敏感、色情暴力、广告营销、其他）
- ✅ 基于 DFA 算法的高效检测
- ✅ 敏感词标红显示
- ✅ 写作助手集成

## 使用方式

### 1. 敏感词管理

访问左侧菜单的"敏感词管理"，可以：
- 添加、编辑、删除敏感词
- 按类型和状态筛选
- 批量删除

### 2. 写作助手中使用

在"写作助手"页面：
1. 输入文本内容
2. 点击工具栏的"🔍 敏感词检测"按钮
3. 查看检测结果，敏感词会自动标红

## 技术实现

### 后端
- DFA 算法实现高效检测（时间复杂度 O(n)）
- Spring Cache 缓存机制
- RESTful API 接口

### 前端
- Vue 3 + TypeScript
- Element Plus UI 组件
- 敏感词标红组件

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/sensitive/words | 添加敏感词 |
| PUT | /api/sensitive/words/{id} | 更新敏感词 |
| DELETE | /api/sensitive/words/{id} | 删除敏感词 |
| GET | /api/sensitive/words | 分页查询敏感词 |
| POST | /api/sensitive/check | 检测文本中的敏感词 |
| POST | /api/sensitive/refresh | 刷新敏感词缓存 |

## 数据库表

```sql
CREATE TABLE `sensitive_word` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `word` VARCHAR(100) NOT NULL,
    `type` TINYINT NOT NULL DEFAULT 4,
    `enabled` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
