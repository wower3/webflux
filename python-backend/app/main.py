from __future__ import annotations

import logging

import aiomysql
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.exceptions import register_exception_handlers
from app.routers import auth, chat, conversation

logging.basicConfig(
    level=getattr(logging, settings.log_level, logging.INFO),
    format="%(asctime)s [%(threadName)s] %(levelname)-5s %(name)s - %(message)s",
)
logger = logging.getLogger(__name__)


async def lifespan(app: FastAPI):
    logger.info("创建数据库连接池: %s:%s/%s", settings.db_host, settings.db_port, settings.db_name)
    pool = await aiomysql.create_pool(
        host=settings.db_host,
        port=settings.db_port,
        user=settings.db_user,
        password=settings.db_password,
        db=settings.db_name,
        maxsize=settings.db_pool_size,
        autocommit=True,
        charset="utf8mb4",
    )
    app.state.db_pool = pool
    yield
    pool.close()
    await pool.wait_closed()
    logger.info("数据库连接池已关闭")


app = FastAPI(title="Chat Chart API", version="1.0.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    max_age=3600,
)

register_exception_handlers(app)
app.include_router(auth.router)
app.include_router(chat.router)
app.include_router(conversation.router)
