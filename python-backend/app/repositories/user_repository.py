from __future__ import annotations

import logging

import aiomysql

from app.models.user import User

log = logging.getLogger(__name__)


async def find_by_username(pool: aiomysql.Pool, username: str) -> User | None:
    async with pool.acquire() as conn:
        async with conn.cursor(aiomysql.DictCursor) as cur:
            await cur.execute(
                "SELECT id, username, password, token, created_at FROM `user` WHERE username = %s",
                (username,),
            )
            row = await cur.fetchone()
            if row is None:
                return None
            return User(
                id=row["id"],
                username=row["username"],
                password=row["password"],
                token=row["token"],
                created_at=row["created_at"],
            )


async def find_by_token(pool: aiomysql.Pool, token: str) -> User | None:
    async with pool.acquire() as conn:
        async with conn.cursor(aiomysql.DictCursor) as cur:
            await cur.execute(
                "SELECT id, username, password, token, created_at FROM `user` WHERE token = %s",
                (token,),
            )
            row = await cur.fetchone()
            if row is None:
                return None
            return User(
                id=row["id"],
                username=row["username"],
                password=row["password"],
                token=row["token"],
                created_at=row["created_at"],
            )


async def save_user(pool: aiomysql.Pool, username: str, hashed_password: str) -> None:
    async with pool.acquire() as conn:
        await conn.commit()
        async with conn.cursor() as cur:
            await cur.execute(
                "INSERT INTO `user` (username, password, created_at) VALUES (%s, %s, NOW())",
                (username, hashed_password),
            )


async def update_token(pool: aiomysql.Pool, user_id: int, token: str) -> None:
    async with pool.acquire() as conn:
        await conn.commit()
        async with conn.cursor() as cur:
            await cur.execute(
                "UPDATE `user` SET token = %s WHERE id = %s",
                (token, user_id),
            )
