from __future__ import annotations

import logging
import uuid

import aiomysql

from app.exceptions import AppError
from app.models.user import User
from app.repositories import user_repository
from app.utils.hash_util import sha256

log = logging.getLogger(__name__)


async def register(pool: aiomysql.Pool, username: str, password: str) -> User:
    existing = await user_repository.find_by_username(pool, username)
    if existing is not None:
        raise AppError(f"用户名已存在: {username}", status_code=500)

    hashed_password = sha256(password)
    await user_repository.save_user(pool, username, hashed_password)

    user = await user_repository.find_by_username(pool, username)
    if user is None:
        raise RuntimeError("注册后用户查找失败")

    token = uuid.uuid4().hex
    await user_repository.update_token(pool, user.id, token)
    return User(
        id=user.id,
        username=user.username,
        password=user.password,
        token=token,
        created_at=user.created_at,
    )


async def login(pool: aiomysql.Pool, username: str, password: str) -> User:
    user = await user_repository.find_by_username(pool, username)
    if user is None:
        raise AppError("用户名或密码错误", status_code=401)

    if user.password != sha256(password):
        raise AppError("用户名或密码错误", status_code=401)

    token = uuid.uuid4().hex
    await user_repository.update_token(pool, user.id, token)
    return User(
        id=user.id,
        username=user.username,
        password=user.password,
        token=token,
        created_at=user.created_at,
    )


async def validate_token(pool: aiomysql.Pool, token: str) -> User | None:
    return await user_repository.find_by_token(pool, token)
