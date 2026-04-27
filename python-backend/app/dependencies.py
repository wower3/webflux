from __future__ import annotations

import aiomysql

from fastapi import Depends, HTTPException, Request
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.models.user import User
from app.services import auth_service

security = HTTPBearer(auto_error=False)


async def get_db_pool(request: Request) -> aiomysql.Pool:
    return request.app.state.db_pool


async def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(security),
    pool: aiomysql.Pool = Depends(get_db_pool),
) -> User:
    if credentials is None:
        raise HTTPException(status_code=401, detail="未提供认证token")

    token = credentials.credentials
    user = await auth_service.validate_token(pool, token)
    if user is None:
        raise HTTPException(status_code=401, detail="无效的认证token")

    return user
