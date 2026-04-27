from __future__ import annotations

import aiomysql
from fastapi import APIRouter, Depends

from app.dependencies import get_current_user, get_db_pool
from app.models.user import User
from app.schemas.conversation import ConversationDTO, ConversationListResponse, MessageDTO
from app.services import conversation_service

router = APIRouter(prefix="/api", tags=["会话"])


@router.post("/conversation")
async def create_conversation(
    user: User = Depends(get_current_user),
    pool: aiomysql.Pool = Depends(get_db_pool),
) -> dict:
    dto = await conversation_service.create_conversation(pool, user.id)
    return dto.to_response()


@router.get("/conversations")
async def list_conversations(
    user: User = Depends(get_current_user),
    pool: aiomysql.Pool = Depends(get_db_pool),
) -> dict:
    dtos = await conversation_service.list_conversations(pool, user.id)
    return {"conversations": [dto.to_response() for dto in dtos]}


@router.get("/conversation/{conversation_id}/messages")
async def get_messages(
    conversation_id: str,
    user: User = Depends(get_current_user),
    pool: aiomysql.Pool = Depends(get_db_pool),
) -> list[dict]:
    dtos = await conversation_service.get_messages(pool, conversation_id)
    return [dto.to_response() for dto in dtos]
