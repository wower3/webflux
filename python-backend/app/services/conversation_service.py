from __future__ import annotations

import logging

import aiomysql

from app.models.conversation import Conversation
from app.repositories import conversation_repository, message_repository
from app.schemas.conversation import ConversationDTO, MessageDTO

log = logging.getLogger(__name__)


async def create_conversation(pool: aiomysql.Pool, user_id: int) -> ConversationDTO:
    from app.utils.id_generator import new_id

    conversation_id = new_id()
    await conversation_repository.save(pool, conversation_id, user_id)

    return ConversationDTO(
        conversation_id=conversation_id,
        created_at=_now(),
        message_count=0,
        active=True,
    )


async def list_conversations(pool: aiomysql.Pool, user_id: int) -> list[ConversationDTO]:
    conversations = await conversation_repository.find_all_by_user(pool, user_id)
    result = []
    for conv in conversations:
        count = await message_repository.count_by_conversation(pool, conv.conversation_id)
        result.append(
            ConversationDTO(
                conversation_id=conv.conversation_id,
                created_at=conv.created_at,
                message_count=count,
                active=True,
            )
        )
    return result


async def get_messages(pool: aiomysql.Pool, conversation_id: str) -> list[MessageDTO]:
    messages = await message_repository.find_by_conversation(pool, conversation_id)
    return [
        MessageDTO(
            request_id=msg.request_id,
            conversation_id=msg.conversation_id,
            role=msg.role,
            content=msg.content,
            created_at=msg.created_at,
        )
        for msg in messages
    ]


def _now():
    from datetime import datetime, timezone

    return datetime.now(timezone.utc)
