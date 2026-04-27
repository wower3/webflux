from typing import Literal

from pydantic import BaseModel, ConfigDict, Field


class ChatRequest(BaseModel):
    model_config = ConfigDict(frozen=True, populate_by_name=True)
    message: str
    conversationId: str | None = None


class StreamEvent(BaseModel):
    model_config = ConfigDict(frozen=True)
    type: Literal["content", "chart", "card", "end"]
    data: str | dict | None = None
