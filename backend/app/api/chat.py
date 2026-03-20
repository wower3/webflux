"""
聊天API路由
"""
from fastapi import APIRouter, Query
from fastapi.responses import StreamingResponse
from app.models.schemas import ChatRequest
from app.services.chat_service import generate_chat_response, generate_stream_from_file

router = APIRouter()


@router.get("/chat/stream")
async def chat_stream_get(
    message: str = Query(...),
    test: bool = Query(False, description="是否使用测试文件模式")
):
    """
    流式聊天接口（GET，用于EventSource）

    参数:
        message: 用户消息
        test: 是否使用测试文件模式（从文件读取并拆分发送）

    返回SSE格式的流式响应
    """
    if test:
        return StreamingResponse(
            generate_stream_from_file("test_content.txt", chunk_size=15, delay=0.15),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "X-Accel-Buffering": "no",
            }
        )

    return StreamingResponse(
        generate_chat_response(message),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        }
    )


@router.post("/chat/stream")
async def chat_stream_post(request: ChatRequest):
    """
    流式聊天接口（POST，用于fetch）

    返回SSE格式的流式响应
    """
    return StreamingResponse(
        generate_chat_response(request.message),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        }
    )


@router.post("/chat")
async def chat(request: ChatRequest):
    """
    非流式聊天接口（用于测试）
    """
    return {
        "message": f"收到消息: {request.message}",
        "session_id": request.session_id or "default"
    }
