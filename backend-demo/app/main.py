"""
FastAPI Chat Application - Demo (Non-Streaming)
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.chat import router as chat_router

app = FastAPI(
    title="Chat Chart API Demo",
    description="智能对话图表系统API - 非流式版本",
    version="1.0.0"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:*",
        "http://127.0.0.1:*",
        "http://localhost:5173",
        "http://localhost:3000",
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
    return {"message": "Chat Chart API Demo is running", "version": "1.0.0", "mode": "non-streaming"}


@app.get("/health")
async def health():
    return {"status": "healthy"}
