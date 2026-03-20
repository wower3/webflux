"""
Pydantic数据模型定义
"""
from pydantic import BaseModel
from typing import Optional


class ChatRequest(BaseModel):
    """聊天请求模型"""
    message: str
    session_id: Optional[str] = None


class ChartData(BaseModel):
    """图表数据模型"""
    chartId: str
    type: str
    subtype: str
    title: str
    data: dict


class StreamEvent(BaseModel):
    """流式事件模型"""
    type: str  # content | chart | end
    data: Optional[dict | str] = None
