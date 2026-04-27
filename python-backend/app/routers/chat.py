from __future__ import annotations

import aiomysql
from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse

from app.dependencies import get_current_user, get_db_pool
from app.models.user import User
from app.schemas.chat import ChatRequest
from app.services import chat_service

router = APIRouter(tags=["聊天"])


@router.get("/")
async def root():
    return {"name": "Chat Chart API", "version": "1.0.0"}


@router.get("/health")
async def health():
    return {"status": "healthy"}


@router.post("/api/chat/stream")
async def chat_stream(
    request: ChatRequest,
    user: User = Depends(get_current_user),
    pool: aiomysql.Pool = Depends(get_db_pool),
) -> StreamingResponse:
    return StreamingResponse(
        chat_service.generate_chat_events(pool, user.id, request.message, request.conversationId),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )
