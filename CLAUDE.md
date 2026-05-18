# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Chat-Chart 智能对话图表系统

> **项目状态**: 开发完成
> **技术栈**: Java Spring Boot + Vue3/Vue2 + ECharts + MySQL

---

## 项目简介

一个前后端分离的智能对话系统，支持：
- Markdown格式文本渲染
- 流式响应（SSE打字机效果）
- 结构化图表数据自动解析
- ECharts图表渲染（折线图、柱状图、饼图）
- 卡片交互（确认/取消按钮）

---

## 快速启动

### Java后端 (Spring Boot 2.3.2 + Java 8)
```bash
cd java-backend
mvn spring-boot:run
# 端口: 8080, 需要MySQL运行在3306
```

### 前端 Vue3 (Vite)
```bash
cd frontend
npm install
npm run dev
# 端口: 5173
```

### 前端 Vue2 (Vue CLI)
```bash
cd frontend-vue2
npm install
npm run dev
# 端口: 8081
```

### 依赖服务
- MySQL: `localhost:3306/chat_chart`，用户 `root`，密码 `123456`
- AI服务: `http://localhost:9999/chatabc/chat`，超时 90s
- Mock AI服务: `mock-ai-service/` 目录下

---

## 项目结构

```
webflux/
├── java-backend/                    # Spring Boot后端（COLA架构）
│   └── src/main/java/com/chat/chart/
│       ├── adapter/                 # 适配器层
│       │   ├── config/              #   CorsConfig, AsyncConfig
│       │   └── web/                 #   ChatController, ConversationController
│       ├── app/                     # 应用层
│       │   └── service/             #   ChatAppService, ConversationAppService
│       ├── client/                  # 客户端层
│       │   ├── api/                 #   服务接口定义
│       │   └── dto/                 #   DTO: ChatRequest, ConversationDTO, MessageDTO, StreamEvent
│       ├── domain/                  # 领域层
│       │   ├── gateway/             #   网关接口 + AI流式回调
│       │   ├── model/               #   领域模型: ChatMessage, Conversation
│       │   └── util/                #   IdGenerator
│       └── infrastructure/          # 基础设施层
│           ├── config/              #   MybatisPlusConfig, AiServiceProperties
│           ├── dataobject/          #   DO: ChatMessageDO, ConversationDO
│           ├── gateway/             #   网关实现: AiChatGatewayImpl(OkHttp SSE), ConversationGatewayImpl, MessageGatewayImpl
│           ├── mapper/              #   MyBatis Mapper接口
│           └── gateway/model/       #   AI请求模型
│   └── src/main/resources/
│       ├── application.yml
│       └── mapper/                  #   MyBatis XML Mapper
│           ├── ConversationMapper.xml
│           └── ChatMessageMapper.xml
│
├── frontend/                        # Vue3前端 (Vite + TypeScript)
│   └── src/features/chat/
│       ├── api/                     #   SSE客户端
│       ├── components/              #   ChatPanel, ChatView, ChatMessage, ChatInput, CardRenderer, ChartRenderer, MarkdownRenderer
│       ├── composables/             #   useChat状态管理
│       ├── types/                   #   TypeScript类型
│       └── utils/                   #   chartParser, streamJsonParser, streamHandler
│
├── frontend-vue2/                   # Vue2前端 (Vue CLI, Options API)
│   └── src/features/chat/           #   同Vue3结构，JS而非TS
│
└── mock-ai-service/                 # AI服务Mock
```

---

## 后端架构：COLA分层

| 层 | 职责 | 关键技术 |
|----|------|----------|
| adapter | HTTP接口、CORS、异常处理 | Spring MVC |
| app | 业务编排、DTO转换 | - |
| client | 接口定义、DTO | - |
| domain | 领域模型、网关接口 | 纯POJO |
| infrastructure | 数据库、外部API调用 | MyBatis-Plus + OkHttp |

### 数据库（MyBatis-Plus XML Mapper风格）
- DO类: `@TableName` + `@TableField(fill=FieldFill.INSERT)` 自动填充 createdAt
- Mapper接口: `@Mapper` + `@Param`，不使用 BaseMapper 自动CRUD
- XML Mapper: `resultMap` + `sql片段` + `<where>` + `<if>` + `<foreach>`（对齐 xuanjiao2 项目风格）
- 自动填充: `MybatisPlusConfig` 实现 `MetaObjectHandler`，INSERT时自动填充 createdAt
- 分页插件: `PaginationInnerInterceptor`（MySQL）

### AI流式调用
- `AiChatGatewayImpl` 使用 OkHttp 调用外部AI接口
- SSE流式响应逐行读取，通过 `AiStreamCallback` 回调
- 异步执行: `@Async("chatExecutor")` 线程池

---

## 核心架构：跨SSE事件的JSON重组

### 问题背景
后端SSE流式响应中，图表JSON可能被拆分到多个事件中发送。

### 解决方案
前端使用 `StreamJsonParser` (utils/streamJsonParser) 处理JSON碎片：
1. **缓冲区管理**: 累积接收到的文本片段
2. **括号追踪**: 通过 `{}` 深度判断JSON完整性
3. **渐进解析**: 完整JSON提取后，剩余文本继续显示

### 数据流路径
```
后端 AiChatGatewayImpl (OkHttp SSE)
  ↓ 异步回调
ChatController → SSE事件流
  ↓ EventSource / fetch
前端 ChatPanel → ChatView → ChatMessage
  ↓ 解析
MarkdownRenderer + ChartRenderer + CardRenderer
```

---

## 关键数据结构

### 后端SSE流式响应格式
```
data: {"type":"content","data":"文本内容"}
data: {"type":"chart","data":{"chartId":"xxx","type":"chart","subtype":"line","title":"xxx","data":{...}}}
data: {"type":"card","data":{...}}
data: {"type":"end","data":null}
```

### 前端消息结构
```typescript
interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string           // Markdown文本（已移除JSON）
  embeds?: EmbedData[]      // 通用嵌入数据（chart/card）
  timestamp: number
  isStreaming?: boolean
}
```

---

## API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/conversations` | GET | 查询用户会话列表 |
| `/api/conversation` | POST | 创建新会话 |
| `/api/conversation/{id}/messages` | GET | 查询会话消息 |
| `/api/chat/stream` | POST | SSE流式聊天 |
| `/api/chat/stream/{conversationId}` | GET | SSE流式聊天（GET方式） |

---

## 依赖版本

### Java后端 (pom.xml)
- Spring Boot 2.3.2.RELEASE + Java 8
- mybatis-plus-boot-starter 3.5.3.1
- okhttp 4.11.0
- mysql-connector-java 8.0.21

### Vue3前端 (package.json)
- vue@3.4.31
- element-plus@2.7.6
- echarts@5.4.3
- vite@4.5.13

### Vue2前端 (package.json)
- vue@2.6.12
- element-ui@2.10.1
- echarts@5.4.3
- @vue/cli-service@4.5.19

---

## 开发规范

### Java代码
- COLA分层架构：adapter/app/client/domain/infrastructure
- 构造器注入，不使用 @Autowired 字段注入
- DO→Domain转换在 GatewayImpl 中完成
- MyBatis XML Mapper 手写SQL，不使用 BaseMapper 自动CRUD

### 前端代码
- Vue3: Composition API + `<script setup>` + TypeScript
- Vue2: Options API + JavaScript
- userId 默认返回 `'1'`，无 localStorage 逻辑
- 卡片确认后按钮永久禁用（组件内 local state）
