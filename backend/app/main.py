"""
FastAPI Chat Application Main Entry
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.chat import router as chat_router

app = FastAPI(
    title="Chat Chart API",
    description="智能对话图表系统API",
    version="1.0.0"
)

# 配置CORS - 支持所有localhost端口，方便开发
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:*",
        "http://127.0.0.1:*",
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:5175",
        "http://localhost:5176",
        "http://localhost:5177",
        "http://localhost:5178",
        "http://localhost:5179",
        "http://localhost:3000",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:5179",
        # 也可以使用通配符（生产环境慎用）
        "*"
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=["*"],
)

# 注册路由
app.include_router(chat_router, prefix="/api", tags=["chat"])


@app.get("/")
async def root():
    return {"message": "Chat Chart API is running", "version": "1.0.0"}


@app.get("/health")
async def health():
    return {"status": "healthy"}
