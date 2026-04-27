from dataclasses import dataclass
from datetime import datetime


@dataclass(frozen=True)
class ChatMessage:
    request_id: str
    conversation_id: str
    role: str
    content: str
    created_at: datetime
