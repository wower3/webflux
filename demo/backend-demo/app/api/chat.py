"""
聊天API路由 - 非流式版本
"""
from fastapi import APIRouter, Query, HTTPException
from app.models.schemas import ChatRequest, ApiResponse, ChatResponse
from app.services.chat_service import process_message, read_test_file, generate_chat_response

router = APIRouter()


@router.post("/chat", response_model=ApiResponse)
async def chat(request: ChatRequest):
    """
    非流式聊天接口

    接收用户消息，返回处理后的响应
    """
    if not request.message:
        raise HTTPException(status_code=400, detail="消息内容不能为空")

    result = generate_chat_response(request.message)

    return ApiResponse(
        code=0,
        message="success",
        data={
            "content": result["content"],
            "message_length": result["message_length"],
            "session_id": request.session_id or "default"
        }
    )


@router.get("/chat/test", response_model=ApiResponse)
async def chat_test(
    file: str = Query("test_content.txt", description="测试文件名")
):
    """
    读取测试文件内容并返回

    参数:
        file: 测试文件名（相对于 services 目录）
    """
    result = read_test_file(file)

    if not result.get("file_exists"):
        return ApiResponse(
            code=404,
            message="文件不存在",
            data=result
        )

    return ApiResponse(
        code=0,
        message="success",
        data=result
    )


@router.post("/chat/process", response_model=ApiResponse)
async def chat_process(request: ChatRequest):
    """
    消息处理接口（中文符号转英文等）

    直接返回处理后的消息，不生成额外响应
    """
    result = process_message(request.message)

    return ApiResponse(
        code=0,
        message="success",
        data=result
    )


@router.get("/chat/echo", response_model=ApiResponse)
async def chat_echo(
    message: str = Query(..., description="要回显的消息内容")
):
    """
    回显接口（GET）

    将用户输入的消息处理后返回
    """
    result = process_message(message)

    return ApiResponse(
        code=0,
        message="success",
        data=result
    )
