from __future__ import annotations

import logging

import aiomysql

from app.models.chat_message import ChatMessage
from app.models.conversation import Conversation

log = logging.getLogger(__name__)


async def find_latest_by_user(pool: aiomysql.Pool, user_id: int) -> Conversation | None:
    async with pool.acquire() as conn:
        async with conn.cursor(aiomysql.DictCursor) as cur:
            await cur.execute(
                "SELECT conversation_id, user_id, created_at FROM conversation "
                "WHERE user_id = %s ORDER BY created_at DESC LIMIT 1",
                (user_id,),
            )
            row = await cur.fetchone()
            if row is None:
                return None
            return Conversation(
                conversation_id=row["conversation_id"],
                user_id=row["user_id"],
                created_at=row["created_at"],
            )


async def find_all_by_user(pool: aiomysql.Pool, user_id: int) -> list[Conversation]:
    async with pool.acquire() as conn:
        async with conn.cursor(aiomysql.DictCursor) as cur:
            await cur.execute(
                "SELECT conversation_id, user_id, created_at FROM conversation "
                "WHERE user_id = %s ORDER BY created_at DESC",
                (user_id,),
            )
            rows = await cur.fetchall()
            return [
                Conversation(
                    conversation_id=row["conversation_id"],
                    user_id=row["user_id"],
                    created_at=row["created_at"],
                )
                for row in rows
            ]


async def save(pool: aiomysql.Pool, conversation_id: str, user_id: int) -> None:
    async with pool.acquire() as conn:
        await conn.commit()
        async with conn.cursor() as cur:
            await cur.execute(
                "INSERT INTO conversation (conversation_id, user_id, created_at) VALUES (%s, %s, NOW())",
                (conversation_id, user_id),
            )
