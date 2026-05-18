# 后端接口测试

> 后端地址: `http://localhost:8080`

## 健康检查

```bash
curl http://localhost:8080/health
```

```json
{"status":"healthy"}
```

## 根路径

```bash
curl http://localhost:8080/
```

```json
{"message":"Chat Chart API is running","version":"1.0.0"}
```

## 创建会话

```bash
curl -X POST "http://localhost:8080/api/conversation?userId=1"
```

```json
{
  "conversationId": "1778119695474-ya6nxe",
  "createdAt": "2026-05-07T10:08:15.482",
  "messageCount": 0,
  "active": true
}
```

## 查询会话列表

```bash
curl "http://localhost:8080/api/conversations?userId=1"
```

```json
{
  "conversations": [
    {
      "conversationId": "1778119609263-wf0rhd",
      "createdAt": "2026-05-07T09:11:50",
      "messageCount": 2,
      "active": true
    }
  ]
}
```

## 查询会话消息

```bash
curl "http://localhost:8080/api/conversation/{conversationId}/messages?userId=1"
```

示例：
```bash
curl "http://localhost:8080/api/conversation/1778116310852-he6j17/messages?userId=1"
```

```json
[
  {
    "requestId": "1778116312169-ruq1rz",
    "conversationId": "1778116310852-he6j17",
    "role": "assistant",
    "content": "好的，我正在为您分析数据...",
    "createdAt": "2026-05-07T09:11:52"
  },
  {
    "requestId": "1778116312169-ruq1rz",
    "conversationId": "1778116310852-he6j17",
    "role": "user",
    "content": "3",
    "createdAt": "2026-05-07T09:11:52"
  }
]
```

## SSE流式聊天

```bash
curl -X POST "http://localhost:8080/api/chat/stream?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"message":"你好","conversationId":"test-conv-001"}'
```

响应为 SSE 事件流：
```
data: {"type":"content","data":"你好"}

data: {"type":"end","data":null}
```

## 卡片确认回调

```bash
curl -X POST "http://localhost:8080/api/datahub/old-stat/complaint" \
  -H "Content-Type: application/json" \
  -d '{"cardId":"card_detail","actionId":"confirm","department":"全部","metric":"销售额"}'
```

```
/datahub/old-stat/complaint/2238730974744936450?id=2238730974744936450
```
