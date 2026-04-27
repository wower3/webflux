from __future__ import annotations

import aiomysql
from fastapi import APIRouter, Depends

from app.dependencies import get_db_pool
from app.models.user import User
from app.schemas.auth import LoginRequest, LoginResponse
from app.services import auth_service

router = APIRouter(prefix="/api/auth", tags=["认证"])


@router.post("/register", response_model=LoginResponse)
async def register(
    request: LoginRequest,
    pool: aiomysql.Pool = Depends(get_db_pool),
) -> LoginResponse:
    user = await auth_service.register(pool, request.username, request.password)
    return LoginResponse(token=user.token, username=user.username)


@router.post("/login", response_model=LoginResponse)
async def login(
    request: LoginRequest,
    pool: aiomysql.Pool = Depends(get_db_pool),
) -> LoginResponse:
    user = await auth_service.login(pool, request.username, request.password)
    return LoginResponse(token=user.token, username=user.username)
