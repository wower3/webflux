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
venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 前端 (Vue3)
```bash
cd frontend
npm install
npm run dev
```

### 访问
- 前端页面: http://localhost:5173
- 后端API: http://localhost:8000
- API文档: http://localhost:8000/docs

---

## 项目结构

```
webflux/
├── backend/          # FastAPI后端
│   ├── app/
│   │   ├── main.py           # 应用入口
│   │   ├── api/chat.py       # 聊天路由
│   │   ├── models/schemas.py # 数据模型
│   │   └── services/chat_service.py
│   └── venv/                 # Python虚拟环境
│
└── frontend/         # Vue3前端
    └── src/
        ├── components/        # Vue组件
        │   ├── ChatMessage.vue
        │   ├── ChatInput.vue
        │   ├── ChartRenderer.vue
        │   └── MarkdownRenderer.vue
        ├── views/ChatView.vue
        ├── api/chat.ts        # API接口
        ├── types/index.ts     # TS类型定义
        └── utils/
            ├── chartParser.ts    # 图表解析
            └── streamHandler.ts  # 流式处理
```

---

## 数据格式

### 后端SSE流式响应
```
data: {"type":"content","data":"文本内容"}
data: {"type":"chart","data":{"chartId":"xxx","type":"chart","subtype":"line","title":"xxx","data":{...}}}
data: {"type":"end","data":null}
```

### 图表数据结构
```json
{
  "chartId": "chart_1",
  "type": "chart",
  "subtype": "line",    // line | bar | pie
  "title": "图表标题",
  "data": {
    "标签1": 数值1,
    "标签2": 数值2
  }
}
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

---

## API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/health` | GET | 健康检查 |
| `/api/chat` | POST | 普通聊天（测试用） |
| `/api/chat/stream` | POST | 流式聊天（SSE） |
| `/docs` | GET | Swagger API文档 |

---

## 依赖版本

### 后端
- fastapi==0.109.0
- uvicorn[standard]==0.27.0
- pydantic==2.5.3

### 前端
- vue@^3.4.15
- element-plus@^2.5.2
- echarts@^5.4.3
- marked@^11.1.1
