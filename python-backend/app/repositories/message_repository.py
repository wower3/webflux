from __future__ import annotations

import logging

import aiomysql

from app.models.chat_message import ChatMessage

log = logging.getLogger(__name__)


async def save_message(
    pool: aiomysql.Pool,
    request_id: str,
    conversation_id: str,
    role: str,
    content: str,
) -> None:
    async with pool.acquire() as conn:
        await conn.commit()
        async with conn.cursor() as cur:
            await cur.execute(
                "INSERT INTO chat_message (request_id, conversation_id, role, content, created_at) "
                "VALUES (%s, %s, %s, %s, NOW())",
                (request_id, conversation_id, role, content),
            )


async def find_context_messages(
    pool: aiomysql.Pool,
    conversation_id: str,
    max_requests: int,
) -> list[ChatMessage]:
    async with pool.acquire() as conn:
        async with conn.cursor(aiomysql.DictCursor) as cur:
            await cur.execute(
                "SELECT request_id FROM chat_message "
                "WHERE conversation_id = %s "
                "GROUP BY request_id "
                "ORDER BY MAX(created_at) DESC "
                "LIMIT %s",
                (conversation_id, max_requests),
            )
            rows = await cur.fetchall()
            request_ids = [r["request_id"] for r in rows]
            if not request_ids:
                return []

            placeholders = ",".join(["%s"] * len(request_ids))
            await cur.execute(
                f"SELECT request_id, conversation_id, role, content, created_at "
                f"FROM chat_message "
                f"WHERE conversation_id = %s AND request_id IN ({placeholders}) "
                f"ORDER BY created_at ASC",
                (conversation_id, *request_ids),
            )
            rows = await cur.fetchall()
            return [
                ChatMessage(
                    request_id=row["request_id"],
                    conversation_id=row["conversation_id"],
                    role=row["role"],
                    content=row["content"],
                    created_at=row["created_at"],
                )
                for row in rows
            ]


async def count_by_conversation(pool: aiomysql.Pool, conversation_id: str) -> int:
    async with pool.acquire() as conn:
        async with conn.cursor() as cur:
            await cur.execute(
                "SELECT COUNT(*) FROM chat_message WHERE conversation_id = %s",
                (conversation_id,),
            )
            result = await cur.fetchone()
            return result[0] if result else 0


async def find_by_conversation(pool: aiomysql.Pool, conversation_id: str) -> list[ChatMessage]:
    async with pool.acquire() as conn:
        async with conn.cursor(aiomysql.DictCursor) as cur:
            await cur.execute(
                "SELECT request_id, conversation_id, role, content, created_at "
                "FROM chat_message "
                "WHERE conversation_id = %s "
                "ORDER BY created_at ASC",
                (conversation_id,),
            )
            rows = await cur.fetchall()
            return [
                ChatMessage(
                    request_id=row["request_id"],
                    conversation_id=row["conversation_id"],
                    role=row["role"],
                    content=row["content"],
                    created_at=row["created_at"],
                )
                for row in rows
            ]
