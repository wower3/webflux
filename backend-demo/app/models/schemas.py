"""
Pydantic数据模型定义
"""
from pydantic import BaseModel
from typing import Optional


class ChatRequest(BaseModel):
    """聊天请求模型"""
    message: str
    session_id: Optional[str] = None


class ApiResponse(BaseModel):
    """通用API响应模型"""
    code: int = 0
    message: str = "success"
    data: Optional[dict] = None


class ChatResponse(BaseModel):
    """聊天响应数据"""
    content: str
    original: str
