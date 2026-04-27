from dataclasses import dataclass
from datetime import datetime


@dataclass(frozen=True)
class Conversation:
    conversation_id: str
    user_id: int
    created_at: datetime
