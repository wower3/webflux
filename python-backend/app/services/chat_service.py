from __future__ import annotations

import asyncio
import json
import logging

import aiomysql

from app.gateways.ai_gateway import AiGateway
from app.gateways.mock_ai_gateway import MockAiGateway
from app.repositories import conversation_repository, message_repository
from app.utils.id_generator import new_id

log = logging.getLogger(__name__)

_gateway: AiGateway = MockAiGateway()


def _format_sse(data: str) -> str:
    return f"data: {data}\n\n"


def _assemble_context(context: list, user_message: str) -> str:
    if not context:
        return user_message

    lines = ["历史对话:"]
    for msg in context:
        label = "用户" if msg.role == "user" else "AI"
        lines.append(f"{label}: {msg.content}")
    lines.append(f"用户输入: {user_message}")
    return "\n".join(lines)


async def generate_chat_events(
    pool: aiomysql.Pool,
    user_id: int,
    message: str,
    conversation_id: str | None,
) -> str:
    content_parts: list[str] = []

    try:
        if not conversation_id:
            conversation_id = new_id()
            await conversation_repository.save(pool, conversation_id, user_id)
            log.info("新建会话: %s", conversation_id)

        request_id = new_id()
        await message_repository.save_message(pool, request_id, conversation_id, "user", message)

        context = await message_repository.find_context_messages(pool, conversation_id, 2)
        full_message = _assemble_context(context, message)

        async for event_json in _gateway.stream(full_message):
            event = json.loads(event_json)
            if event.get("type") == "content" and isinstance(event.get("data"), str):
                content_parts.append(event["data"])

            if event.get("type") == "end":
                assistant_content = "".join(content_parts)
                if assistant_content.strip():
                    await message_repository.save_message(
                        pool, request_id, conversation_id, "assistant", assistant_content
                    )
                    log.info("AI回复已持久化: requestId=%s, length=%d", request_id, len(assistant_content))

            yield _format_sse(event_json)

    except Exception:
        log.exception("SSE 流式生成异常")
        error_event = json.dumps({"type": "end", "data": None}, ensure_ascii=False, separators=(",", ":"))
        yield _format_sse(error_event)
