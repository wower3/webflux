"""
Pydantic数据模型定义
"""
from pydantic import BaseModel
from typing import Optional, List


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
    type: str  # content | chart | card | end
    data: Optional[dict | str] = None


class CardInfoItem(BaseModel):
    """卡片信息项"""
    key: str
    label: str
    value: str


class CardButton(BaseModel):
    """卡片按钮"""
    actionId: str
    label: str
    apiEndpoint: Optional[str] = None


class CardData(BaseModel):
    """卡片数据模型"""
    type: str = "card"
    cardId: str
    cardName: str
    displayTitle: str
    cardInfo: List[CardInfoItem]
    buttons: List[CardButton]
