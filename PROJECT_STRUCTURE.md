# 项目结构说明

## 目录结构

```
LLM-Research-Assistant/
├── backend/                    # Java Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/uni/research/
│   │       ├── common/        # 公共组件（配置、过滤器、异常处理）
│   │       └── module/        # 业务模块
│   │           ├── auth/      # 用户认证
│   │           ├── chat/      # AI 对话
│   │           ├── doc/       # 文档管理
│   │           ├── note/      # 笔记管理
│   │           ├── analysis/  # 研读分析
│   │           ├── write/     # 写作助手
│   │           └── sensitive/ # 敏感词管理 (New)
│   └── src/main/resources/
│       └── db/migration/      # 数据库迁移脚本
│
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/               # API 接口封装
│   │   ├── components/        # 公共组件
│   │   │   ├── SensitiveTextHighlight.vue  # 敏感词标红组件
│   │   │   └── ...
│   │   ├── views/             # 页面视图
│   │   │   ├── Chat/          # 对话页面
│   │   │   ├── Doc/           # 文档管理
│   │   │   ├── Analyze/       # 研读分析
│   │   │   ├── Write/         # 写作助手
│   │   │   └── SensitiveWord/ # 敏感词管理 (New)
│   │   ├── stores/            # Pinia 状态管理
│   │   └── router/            # 路由配置
│   └── package.json
│
├── scripts/                    # Python AI 服务脚本
├── docs/                       # 项目文档
│   ├── SENSITIVE_WORD.md      # 敏感词功能文档
│   └── ...
│
├── data/                       # Docker 数据持久化目录
│   ├── mysql/                 # MySQL 数据
│   └── pg_data/               # PostgreSQL 向量数据库
│
├── docker-compose.yml         # Docker 编排配置
├── main.py                    # Python AI 服务入口
├── rag_service.py             # RAG 检索服务
└── README.md                  # 项目说明
```

## 核心模块说明

### 后端模块

#### 1. common - 公共组件
- `config/` - 配置类（Security、Redis、MinIO、MyBatis 等）
- `filter/` - JWT 认证过滤器
- `exception/` - 全局异常处理
- `result/` - 统一响应封装

#### 2. module - 业务模块

**auth** - 用户认证
- 用户注册、登录
- JWT Token 管理

**chat** - AI 对话
- 会话管理
- 流式对话
- 消息历史

**doc** - 文档管理
- 文档上传/下载
- MinIO 对象存储
- 文档索引

**note** - 笔记管理
- 笔记 CRUD
- 标签系统

**analysis** - 研读分析
- 文档对比分析
- 引用生成

**write** - 写作助手
- 文本润色、扩写、纠错
- 流式生成

**sensitive** - 敏感词管理 ⭐ New
- 敏感词 CRUD
- DFA 算法检测
- 缓存管理

### 前端模块

#### 1. api - API 接口
- `auth.ts` - 认证接口
- `chat.ts` - 对话接口
- `doc.ts` - 文档接口
- `note.ts` - 笔记接口
- `write.ts` - 写作接口
- `sensitive.ts` - 敏感词接口 ⭐ New

#### 2. components - 公共组件
- `SensitiveTextHighlight.vue` - 敏感词标红组件 ⭐ New
- `NoteEditor.vue` - 笔记编辑器
- `CitationDialog.vue` - 引用对话框
- 等...

#### 3. views - 页面视图
- `Chat/` - AI 对话页面
- `Doc/` - 文档管理页面
- `Analyze/` - 研读分析页面
- `Write/` - 写作助手页面
- `SensitiveWord/` - 敏感词管理页面 ⭐ New

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.x
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.0
- **对象存储**: MinIO
- **ORM**: MyBatis-Plus
- **安全**: Spring Security + JWT
- **文档**: Knife4j (Swagger)

### 前端
- **框架**: Vue 3 + TypeScript
- **UI 组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP**: Axios
- **Markdown**: Marked
- **文档导出**: Docx.js

### AI 服务
- **框架**: FastAPI
- **LLM**: DeepSeek / Gemini
- **向量数据库**: PGVector
- **检索**: LangChain + BM25

## 数据库表

### 核心表
- `user` - 用户表
- `chat_session` - 对话会话表
- `chat_message` - 对话消息表
- `document` - 文档表
- `note` - 笔记表
- `sensitive_word` - 敏感词表 ⭐ New

## 开发规范

### 后端
- 使用 RESTful API 设计
- 统一响应格式 `Result<T>`
- 全局异常处理
- JWT 认证保护

### 前端
- 组件化开发
- TypeScript 类型安全
- Pinia 状态管理
- 响应式设计

## 部署说明

### 开发环境
1. 启动 Docker 服务：`docker-compose up -d`
2. 启动 Python AI 服务：`python main.py`
3. 启动 Java 后端：`mvn spring-boot:run`
4. 启动 Vue 前端：`npm run dev`

### 生产环境
- 前端：`npm run build` 生成静态文件
- 后端：`mvn package` 打包 JAR
- 使用 Nginx 反向代理
- Docker 容器化部署

## 相关文档

- [敏感词功能说明](docs/SENSITIVE_WORD.md)
- [启动指南](STARTUP_GUIDE.md)
- [项目说明](README.md)
