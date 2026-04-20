# Chat Chart API - Java WebFlux后端

> **技术栈**: Spring Boot 2.7.18 + Spring WebFlux + Java 8

## 快速启动

### 1. 编译项目
```bash
cd java-backend
mvn clean install
```

### 2. 运行项目
```bash
mvn spring-boot:run
```

或直接运行jar：
```bash
java -jar target/chart-api-1.0.0.jar
```

### 3. 访问
- API根路径: http://localhost:8080/
- 健康检查: http://localhost:8080/health
- 流式聊天: http://localhost:8080/api/chat/stream?message=hello
- 测试模式: http://localhost:8080/api/chat/stream?message=hello&test=true

## API端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 根路径，返回版本信息 |
| `/health` | GET | 健康检查 |
| `/api/chat/stream` | GET/POST | 流式聊天（SSE） |
| `/api/chat/echo/stream` | GET/POST | 回显模式（测试） |
| `/api/chat` | POST | 非流式聊天 |

## SSE事件格式

```
data: {"type":"content","data":"文本内容"}
data: {"type":"chart","data":{...}}
data: {"type":"end","data":null}
```

## 项目结构

```
java-backend/
├── pom.xml                                    # Maven配置
├── src/main/
│   ├── java/com/chat/chart/
│   │   ├── ChatChartApplication.java         # 启动类
│   │   ├── config/
│   │   │   ├── CorsConfig.java               # CORS配置
│   │   │   └── RouterConfig.java             # 路由配置
│   │   ├── controller/
│   │   │   └── ChatController.java           # 聊天API控制器
│   │   ├── model/
│   │   │   ├── ChatRequest.java              # 请求模型
│   │   │   ├── StreamEvent.java              # SSE事件模型
│   │   │   ├── ChartData.java                # 图表数据
│   │   │   └── CardData.java                 # 卡片数据
│   │   ├── service/
│   │   │   └── ChatService.java              # 流式响应服务
│   │   └── util/
│   │       └── SymbolConverter.java          # 中文符号转换
│   └── resources/
│       ├── application.yml                    # 应用配置
│       └── test_content.txt                  # 测试内容文件
```

## 与Python版本的功能对比

| 功能 | Python FastAPI | Java WebFlux | 状态 |
|------|----------------|--------------|------|
| SSE流式响应 | ✓ | ✓ | 完全兼容 |
| CORS配置 | ✓ | ✓ | 完全兼容 |
| 中文符号转换 | ✓ | ✓ | 完全兼容 |
| 测试文件模式 | ✓ | ✓ | 完全兼容 |
| 回显模式 | ✓ | ✓ | 完全兼容 |
| 图表JSON拆分 | ✓ | ✓ | 完全兼容 |
| AI服务预留 | - | ✓ | 新增 |

## AI服务集成（预留）

`ChatService.callAiService()` 方法已预留AI服务集成接口，后续可集成：
- OpenAI (GPT-4)
- Anthropic (Claude)
- 文心一言
- 通义千问
- 等

## 端口配置

默认端口: **8080** (Python版本为8000)

如需修改，编辑 `src/main/resources/application.yml`:
```yaml
server:
  port: 8080  # 修改为其他端口
```
