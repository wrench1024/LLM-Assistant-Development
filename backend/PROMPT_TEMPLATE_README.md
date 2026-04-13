# 提示词模板管理功能

## 功能概述

将原来的"写作模板模块"升级为支持三种核心科研文体的专业写作辅助系统，并内置独立的提示词管理功能。

## 核心功能

### 1. 三种科研文体
- **学术论文** (academic_paper): 用于撰写学术期刊论文、会议论文
- **文献综述** (literature_review): 用于系统性地综述和分析研究文献
- **研究报告** (research_report): 用于撰写课题研究报告、项目总结报告

### 2. 六种写作动作
- 润色优化 (polish)
- 扩写延伸 (expand)
- 续写补充 (continue)
- 语法修正 (fix_grammar)
- 中英互译 (translate)
- 摘要总结 (summarize)

### 3. 提示词模板系统
- 14 个系统内置专业模板
- 支持用户创建自定义模板
- 模板变量占位符：`{text}`, `{context}`
- 智能模板选择（用户自定义 > 系统内置）

## 快速开始

### 1. 数据库初始化
```bash
docker exec -i uni-research-mysql mysql -uroot -proot uni_research_db -e "CREATE TABLE IF NOT EXISTS writing_prompt_template (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) NOT NULL, genre VARCHAR(50) NOT NULL, action VARCHAR(50) NOT NULL, prompt_content TEXT NOT NULL, is_builtin TINYINT(1) DEFAULT 0, user_id BIGINT, description VARCHAR(500), sort_order INT DEFAULT 0, enabled TINYINT(1) DEFAULT 1, deleted TINYINT(1) DEFAULT 0, create_time DATETIME DEFAULT CURRENT_TIMESTAMP, update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, INDEX idx_genre_action (genre, action), INDEX idx_user_id (user_id), INDEX idx_builtin (is_builtin)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
```

### 2. 启动应用
```bash
cd backend
mvn spring-boot:run
```

系统启动时会自动初始化 14 个内置模板。

### 3. 访问 API 文档
http://localhost:8080/api/doc.html

## API 接口

### 模板管理
- `GET /api/writing/templates/genres` - 获取文体类型列表
- `GET /api/writing/templates/actions` - 获取动作类型列表
- `GET /api/writing/templates` - 查询模板列表
- `POST /api/writing/templates` - 创建自定义模板
- `DELETE /api/writing/templates/{id}` - 删除自定义模板

### 写作辅助
- `POST /api/writing/process-with-template` - 使用模板进行写作辅助（新版）
- `POST /api/writing/process` - 写作辅助处理（旧版，保持兼容）

## 前端使用

### 1. 模板管理界面
- 点击"📚 模板管理"按钮
- 查看所有系统内置模板
- 创建/删除自定义模板
- 选择模板进行写作

### 2. 使用模板
1. 在模板管理界面选择模板
2. 输入待处理文本
3. 点击"开始处理"
4. AI 使用选中的模板处理文本

## 测试

### 使用 Swagger UI
1. 访问 http://localhost:8080/api/doc.html
2. 登录获取 token
3. 设置全局 Authorization
4. 测试各个接口

### 使用 HTTP 文件
打开 `API_TEST_EXAMPLES.http`，替换 token 后执行测试

## 文件结构

```
backend/src/main/java/com/uni/research/module/writing/
├── entity/
│   └── PromptTemplate.java                    # 模板实体
├── enums/
│   ├── WritingGenre.java                      # 文体枚举
│   └── PromptAction.java                      # 动作枚举
├── dto/                                       # 数据传输对象
├── mapper/
│   └── PromptTemplateMapper.java              # 数据访问
├── service/
│   ├── PromptTemplateService.java             # 模板服务接口
│   ├── WritingService.java                    # 写作服务接口
│   └── impl/                                  # 服务实现
└── controller/
    ├── PromptTemplateController.java          # 模板管理控制器
    └── WritingController.java                 # 写作控制器
```

## 技术亮点

1. **枚举设计** - 使用枚举管理文体和动作类型
2. **模板引擎** - 简单高效的占位符替换
3. **流式响应** - SSE 实时返回 AI 生成结果
4. **权限控制** - 用户只能管理自己的模板
5. **自动初始化** - 系统启动时自动加载内置模板

## 常见问题

**Q: 模板列表为空？**
A: 调用 `POST /api/writing/templates/init` 手动初始化

**Q: 如何使用自定义模板？**
A: 在请求中指定 `templateId` 参数

**Q: 模板变量如何使用？**
A: 使用 `{text}` 表示用户输入，`{context}` 表示上下文

---

**版本**: v1.1.0  
**更新日期**: 2024-01-01
