# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Chat-Chart 智能对话图表系统

> **项目状态**: 开发完成，待功能测试
> **技术栈**: Vue3 + FastAPI + ECharts

---

## 项目简介

一个前后端分离的智能对话系统，支持：
- Markdown格式文本渲染
- 流式响应（打字机效果）
- 结构化图表数据自动解析
- ECharts图表渲染（折线图、柱状图、饼图）

---

## 快速启动

### 后端 (FastAPI)
```bash
cd backend
python -m venv venv
venv\Scripts\activate  # Windows
# venv/bin/activate   # Linux/Mac
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 前端 (Vue3)
```bash
cd frontend
npm install
npm run dev
```

### 构建/测试
```bash
# 前端构建
cd frontend
npm run build

# 前端预览构建产物
npm run preview

# 类型检查
vue-tsc -b
```

### 访问
- 前端页面: http://localhost:5173
- 后端API: http://localhost:8000
- API文档: http://localhost:8000/docs

---

## 核心架构：跨SSE事件的JSON重组

### 问题背景
后端SSE流式响应中，图表JSON可能被拆分到多个事件中发送。例如：
```
Event 1: {"type":"content","data":"{"type":"chart","chartId":"chart_1",""}
Event 2: {"type":"content","data":"subtype":"line","title":"数据"}
Event 3: {"type":"content","data":"","data":{"a":1}}"}
```

### 解决方案
前端使用 `StreamJsonParser` (src/utils/streamJsonParser.ts) 处理JSON碎片：
1. **缓冲区管理**: 累积接收到的文本片段
2. **括号追踪**: 通过 `{}` 深度判断JSON完整性
3. **渐进解析**: 完整JSON提取后，剩余文本继续显示

### 数据流路径
```
后端 generate_chat_response() (chat_service.py)
  ↓ SSE事件流
前端 sendChatStream() (api/chat.ts) - EventSource
  ↓ 原始内容累积
ChatView.parseEmbeds() - 实时提取嵌入数据
  ↓ 渲染
ChatMessage.vue → MarkdownRenderer.vue + ChartRenderer.vue
```

---

## 关键数据结构

### 后端SSE流式响应格式
```
data: {"type":"content","data":"文本内容"}
data: {"type":"chart","data":{"chartId":"xxx","type":"chart","subtype":"line","title":"xxx","data":{...}}}
data: {"type":"end","data":null}
```

### 前端消息结构 (types/index.ts)
```typescript
interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string           // Markdown文本（已移除JSON）
  charts: ChartData[]       // 旧字段，保留兼容
  embeds?: EmbedData[]      // 新字段，通用嵌入数据
  timestamp: number
  isStreaming?: boolean
}
```

### 图表数据结构
```typescript
interface ChartData {
  chartId: string
  type: "chart"
  subtype: 'line' | 'bar' | 'pie' | 'scatter'
  title: string
  data: Record<string, number>
}
```

---

## 项目结构

```
webflux/
├── backend/                    # FastAPI后端
│   ├── app/
│   │   ├── main.py             # 应用入口，CORS配置
│   │   ├── api/chat.py         # 路由：/api/chat, /api/chat/stream
│   │   ├── models/schemas.py   # Pydantic模型
│   │   └── services/
│   │       └── chat_service.py # 流式响应生成器（模拟数据）
│   └── requirements.txt
│
└── frontend/                   # Vue3前端
    └── src/
        ├── components/
        │   ├── ChatView.vue       # 主视图，消息管理
        │   ├── ChatMessage.vue    # 单条消息渲染
        │   ├── ChatInput.vue      # 输入框
        │   ├── MarkdownRenderer.vue  # Markdown渲染
        │   └── ChartRenderer.vue   # ECharts图表渲染
        ├── api/chat.ts             # SSE客户端 (EventSource)
        ├── types/index.ts          # TypeScript类型定义
        └── utils/
            ├── chartParser.ts       # 图表→ECharts配置转换
            ├── streamJsonParser.ts  # 跨事件JSON重组（核心）
            ├── streamHandler.ts     # SSE流处理
            └── *.test.ts            # 单元测试
```

---

## 开发规范

### Python代码
- 使用类型注解
- Pydantic模型定义在 `models/schemas.py`
- 业务逻辑在 `services/` 目录
- API路由在 `api/` 目录

### TypeScript代码
- 使用Vue 3 Composition API
- 组件使用 `<script setup>` 语法
- 类型定义统一在 `types/index.ts`

### 调试开关
- `streamHandler.ts` 中有 `DEBUG` 常量控制日志输出
- `chat.ts` 中有详细的 console.log 用于调试SSE连接

---

## API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 根路径，返回版本信息 |
| `/health` | GET | 健康检查 |
| `/api/chat` | POST | 普通聊天（测试用） |
| `/api/chat/stream` | GET/POST | 流式聊天（SSE） |
| `/docs` | GET | Swagger API文档 |

### 测试模式
GET `/api/chat/stream?message=hello&test=true` 启用测试文件模式，从 `test_content.txt` 读取内容。

---

## 依赖版本

### 后端 (requirements.txt)
- fastapi==0.109.0
- uvicorn[standard]==0.27.0
- pydantic==2.5.3
- python-multipart==0.0.6

### 前端 (package.json)
- vue@^3.5.30
- element-plus@^2.13.6
- echarts@^6.0.0
- marked@^17.0.4
- typescript@~5.9.3
- vite@^8.0.1
