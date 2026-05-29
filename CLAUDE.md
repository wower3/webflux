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
- 消息采纳/未采纳评价
- 日维度统计（用户量、交易量、采纳率）

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
- AI服务: `http://localhost:9999`，两个接口：
  - 初始化会话: `/chatabc/init_session`
  - 聊天: `/chatabc/chat`
- 超时: 90s
- Mock AI服务: `mock-ai-service/` 目录下

---

## 项目结构

```
webflux/
├── java-backend/                    # Spring Boot后端（COLA架构）
│   └── src/main/java/com/chat/chart/
│       ├── adapter/                 # 适配器层
│       │   ├── config/              #   CorsConfig
│       │   └── web/                 #   ChatController, ConversationController, GlobalExceptionHandler
│       ├── app/                     # 应用层
│       │   └── service/             #   ChatAppService, ConversationAppService, StatService
│       ├── client/                  # 客户端层
│       │   ├── api/                 #   服务接口定义
│       │   └── dto/                 #   DTO: ChatRequest, ConversationDTO, StreamEvent, AdoptionRequest
│       ├── domain/                  # 领域层
│       │   ├── gateway/             #   网关接口 + AiStreamCallback
│       │   ├── model/               #   领域模型: ChatMessage, Conversation, AdoptionStatus
│       │   └── util/                #   IdGenerator
│       └── infrastructure/          # 基础设施层
│           ├── config/              #   MybatisPlusConfig, AiServiceProperties, AiRateLimiter
│           ├── dataobject/          #   DO: ChatMessageDO, ConversationDO
│           ├── gateway/             #   网关实现: AiChatGatewayImpl(OkHttp SSE), ConversationGatewayImpl, MessageGatewayImpl
│           ├── mapper/              #   MyBatis Mapper: ChatMessageMapper, ConversationMapper, LlmParameterMapper, StatMapper
│           └── gateway/model/       #   AI请求模型: BaseAgentRequest, ChatReqData, InitSessionData
│   └── src/main/resources/
│       ├── application.yml
│       └── mapper/                  #   MyBatis XML Mapper
│           ├── ConversationMapper.xml
│           ├── ChatMessageMapper.xml
│           └── LlmParameterMapper.xml
│
├── stat-service/                    # 统计服务（待集成到java-backend）
│   ├── StatService.java             #   日维度统计：用户量、交易量、采纳率
│   ├── StatMapper.java              #   统计Mapper接口
│   └── StatMapper.xml               #   统计SQL
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
- **必传参数不加 `<if>` 条件**：如 `LlmParameterMapper` 中 `llmKey` 是必传的，直接用 `WHERE llm_key = #{llmKey}`，不加 `<if test="llmKey != null">` 防御，避免空值时生成错误SQL
- 自动填充: `MybatisPlusConfig` 实现 `MetaObjectHandler`，INSERT时自动填充 createdAt
- 分页插件: `PaginationInnerInterceptor`（MySQL）

### AI流式调用（两步流程）
1. **init_session**: 先调用 `/chatabc/init_session` 获取 `session_id`
2. **chat**: 再调用 `/chatabc/chat` 发送聊天请求，请求体中携带 `session_id`
- 两个请求通过 `Cookie: sessionId=xxx` 头实现会话亲和性（确保路由到同一Pod）
- `AiChatGatewayImpl` 使用 OkHttp SSE流式响应逐行读取，通过 `AiStreamCallback` 回调
- 异步执行: `chatExecutor` 线程池（`ChatAppService` 构造器注入 `@Qualifier("chatExecutor") Executor`）

### 限流机制（AiRateLimiter）
- 基于数据库 `llm_parameter` 表的固定窗口限流器，每分钟最多15次请求
- CAS（Compare-And-Swap）乐观锁保证多Pod间计数一致
- `markBlocked()` 熔断：外部AI服务返回限流错误时标记当前分钟 blocked=true，阻止所有Pod后续请求
- 初次投产无数据时自动插入初始行（`insertIfAbsent`），不会因缺数据导致服务不可用

### 统计服务（stat-service）
- `StatService` 提供日维度统计：用户量、交易量、采纳率
- 时间范围：`baseDate-2日23:00 ~ baseDate-1日23:00`
- 文件暂存在 `stat-service/` 目录，包名已对齐 `java-backend`

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
AiChatGatewayImpl.chatStream()
  ↓ 1. initSession(cookieValue) → 获取session_id
  ↓ 2. buildChatRequest(sessionId, message) → 构建请求体
  ↓ 3. executeChatRequest(jsonBody, cookieValue) → OkHttp SSE
  ↓ 异步回调 ChatStreamCallback
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
data: {"type":"content","data":"文本内容"}          # 增量内容
data: {"type":"final_output","data":"完整输出"}      # 最终结构化输出（覆盖之前内容）
data: {"type":"end","data":{"requestId":"xxx"}}      # 流结束，携带requestId
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
| `/api/chat/adoption` | POST | 更新消息采纳状态 `{requestId, adoptionStatus}` |

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
- font-awesome@4.6.3（npm包引入，webpack自动打包字体文件）

---

## 测试规范

### 后端测试
修改Java后端代码后，**必须**通过实际启动服务验证：
1. 若8080端口被占用，先 kill 占用进程
2. `mvn spring-boot:run` 启动服务
3. 确认日志输出 `Started ChatChartApplication` 无 ERROR
4. 验证完成后 kill 掉服务
5. **不要仅依赖 `mvn test` 单元测试作为验证通过的标准**，单元测试可能不加载Spring容器

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
- Font Awesome图标通过 npm 包引入（`import 'font-awesome/css/font-awesome.min.css'`），不依赖 public 目录下的字体文件
- userId 默认返回 `'1'`，无 localStorage 逻辑
- 卡片确认后按钮永久禁用（组件内 local state）
