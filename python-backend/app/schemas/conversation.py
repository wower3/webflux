from datetime import datetime

from pydantic import BaseModel, ConfigDict


class ConversationDTO(BaseModel):
    model_config = ConfigDict(frozen=True, alias_generator=str, populate_by_name=True)
    conversation_id: str
    created_at: datetime
    message_count: int
    active: bool = True

    def to_response(self) -> dict:
        return {
            "conversationId": self.conversation_id,
            "createdAt": self.created_at.isoformat() if self.created_at else None,
            "messageCount": self.message_count,
            "active": self.active,
        }


class ConversationListResponse(BaseModel):
    conversations: list[dict]


class MessageDTO(BaseModel):
    model_config = ConfigDict(frozen=True)
    request_id: str
    conversation_id: str
    role: str
    content: str
    created_at: datetime

    def to_response(self) -> dict:
        return {
            "requestId": self.request_id,
            "conversationId": self.conversation_id,
            "role": self.role,
            "content": self.content,
            "createdAt": self.created_at.isoformat() if self.created_at else None,
        }
