# Chat-Chart 智能对话图表系统 开发文档

> **状态**: 已完成
> **完成日期**: 2026-03-20

---

## 一、项目概述

### 1.1 项目目标
构建一个前后端分离的智能对话系统，前端支持Markdown渲染和动态图表解析，后端提供流式API响应。

### 1.2 技术选型
| 类型 | 技术栈 |
|------|--------|
| 前端框架 | Vue 3 + TypeScript |
| 构建工具 | Vite 5.x |
| UI组件库 | Element Plus |
| 图表库 | ECharts 5.x |
| Markdown渲染 | marked.js |
| 后端框架 | FastAPI + uvicorn |
| Python版本 | 3.12 |

---

## 二、项目结构

```
D:\python_project\webflux\
├── frontend/                 # Vue3前端项目
│   ├── src/
│   │   ├── components/      # 组件
│   │   │   ├── ChatMessage.vue      # 单条消息组件 ✓
│   │   │   ├── ChatInput.vue        # 输入框组件 ✓
│   │   │   ├── ChartRenderer.vue    # 图表渲染组件 ✓
│   │   │   └── MarkdownRenderer.vue  # Markdown渲染组件 ✓
│   │   ├── views/
│   │   │   └── ChatView.vue         # 主聊天页面 ✓
│   │   ├── api/
│   │   │   └── chat.ts              # API接口 ✓
│   │   ├── types/
│   │   │   └── index.ts             # 类型定义 ✓
│   │   ├── utils/
│   │   │   ├── chartParser.ts       # 图表数据解析 ✓
│   │   │   └── streamHandler.ts     # 流式响应处理 ✓
│   │   ├── App.vue          ✓
│   │   └── main.ts          ✓
│   ├── package.json
│   ├── vite.config.ts       ✓
│   └── tsconfig.app.json    ✓
│
├── backend/                  # FastAPI后端项目
│   ├── app/
│   │   ├── __init__.py      ✓
│   │   ├── main.py          ✓ FastAPI应用入口
│   │   ├── api/
│   │   │   └── chat.py      ✓ 聊天API路由
│   │   ├── models/
│   │   │   └── schemas.py   ✓ 数据模型
│   │   └── services/
│   │       └── chat_service.py  ✓ 业务逻辑
│   ├── requirements.txt     ✓
│   └── venv/                # Python虚拟环境 ✓
│
├── CLAUDE.md                # 项目说明文档 ✓
└── memory/
    └── chat-chart-dev-plan.md
```

---

## 三、数据结构定义

### 3.1 后端响应格式（SSE流式）

后端以SSE（Server-Sent Events）格式流式返回数据：

```json
// 每个事件包含type和data字段
{
  "type": "content",           // content | chart | end
  "data": "..."
}
```

**content类型**：普通文本内容
```json
{
  "type": "content",
  "data": "好的，我已经为您生成了图表分析。"
}
```

**chart类型**：结构化图表数据
```json
{
  "type": "chart",
  "data": {
    "chartId": "chart_time",
    "type": "chart",
    "subtype": "line",        // line | bar | pie | scatter
    "title": "数量随时间变化",
    "data": {
      "时间1": 224,
      "时间2": 268,
      "时间3": 307,
      "时间4": 221
    }
  }
}
```

**end类型**：结束标志
```json
{
  "type": "end",
  "data": null
}
```

### 3.2 前端消息数据结构

```typescript
interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  charts: ChartData[];
  timestamp: number;
  isStreaming?: boolean;
}

interface ChartData {
  id: string;
  type: 'line' | 'bar' | 'pie' | 'scatter';
  title: string;
  data: Record<string, number>;
}
```

---

## 四、API接口定义

### 4.1 健康检查
```
GET /health
返回: {"status":"healthy"}
```

### 4.2 流式聊天接口
```
POST /api/chat/stream
Content-Type: application/json

请求: {"message": "帮我把数据画成图表"}

响应 (SSE流):
data: {"type":"content","data":"好的，我已经..."}
data: {"type":"chart","data":{...}}
data: {"type":"end","data":null}
```

---

## 五、依赖清单

### 5.1 后端依赖 (requirements.txt)
```
fastapi==0.109.0
uvicorn[standard]==0.27.0
pydantic==2.5.3
python-multipart==0.0.6
```

### 5.2 前端依赖
```json
{
  "dependencies": {
    "vue": "^3.4.15",
    "element-plus": "^2.5.2",
    "echarts": "^5.4.3",
    "marked": "^11.1.1",
    "highlight.js": "^11.9.0",
    "axios": "^1.6.5"
  }
}
```

---

## 六、启动命令

### 6.1 后端启动
```bash
cd backend
python -m venv venv
venv\Scripts\activate  # Windows
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 6.2 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 6.3 访问地址
- 前端: http://localhost:5173
- 后端API: http://localhost:8000
- API文档: http://localhost:8000/docs

---

## 七、已实现功能

### 前端
- [x] Vue3 + TypeScript + Vite 项目搭建
- [x] Element Plus UI组件集成
- [x] Markdown渲染 (marked.js)
- [x] 流式SSE响应处理
- [x] 图表数据解析
- [x] ECharts图表渲染 (line/bar/pie)
- [x] 聊天界面布局

### 后端
- [x] FastAPI项目搭建
- [x] SSE流式响应接口
- [x] CORS跨域配置
- [x] Pydantic数据模型
- [x] 测试数据返回

---

## 八、测试结果

| 测试项 | 结果 |
|--------|------|
| 后端Python语法检查 | ✓ 通过 |
| FastAPI应用导入 | ✓ 通过 |
| 后端服务启动 | ✓ 正常运行 |
| 健康检查接口 | ✓ 返回正确 |
| 流式API格式 | ✓ SSE格式正确 |
| 前端TypeScript编译 | ✓ 通过 |
| 前端构建 | ✓ 成功 |
| 前端开发服务器 | ✓ 正常运行 |
