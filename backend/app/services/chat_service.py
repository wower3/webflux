# -*- coding: utf-8 -*-
"""
聊天服务 - 流式响应，图表JSON可能被拆分到多个SSE事件
"""
import asyncio
import json
import os
from typing import AsyncGenerator
from pathlib import Path


def format_sse_text(content: str) -> str:
    """格式化SSE文本事件"""
    return f"data: {json.dumps({'type': 'content', 'data': content}, ensure_ascii=False)}\n\n"


async def stream_text_by_length(
    content: str,
    chunk_size: int = 15,
    delay: float = 0.2
) -> AsyncGenerator[str, None]:
    """
    按固定长度拆分文本并模拟流式发送

    Args:
        content: 要发送的完整内容
        chunk_size: 每个片段的字符数（默认15）
        delay: 每个片段之间的延迟（秒）

    Yields:
        SSE格式的数据片段
    """
    for i in range(0, len(content), chunk_size):
        chunk = content[i:i + chunk_size]
        yield format_sse_text(chunk)
        await asyncio.sleep(delay)


async def generate_stream_from_file(
    filename: str = "test_content.txt",
    chunk_size: int = 15,
    delay: float = 0.2
) -> AsyncGenerator[str, None]:
    """
    从文件读取内容并按流式发送

    Args:
        filename: 测试文件名（相对于 services 目录）
        chunk_size: 每个片段的字符数
        delay: 每个片段之间的延迟

    Yields:
        SSE格式的数据片段
    """
    file_path = Path(__file__).parent / filename

    if not file_path.exists():
        yield format_sse_text(f"[错误] 测试文件不存在: {filename}")
        yield f"data: {json.dumps({'type': 'end', 'data': None}, ensure_ascii=False)}\n\n"
        return

    content = file_path.read_text(encoding='utf-8')

    async for chunk in stream_text_by_length(content, chunk_size, delay):
        yield chunk

    yield f"data: {json.dumps({'type': 'end', 'data': None}, ensure_ascii=False)}\n\n"


async def generate_chat_response(message: str) -> AsyncGenerator[str, None]:
    """
    生成流式聊天响应
    图表JSON会被拆分到多个SSE事件中发送，模拟真实场景

    Args:
        message: 用户消息

    Yields:
        SSE格式的响应数据
    """
    await asyncio.sleep(0.5)

    # === 开场文本 ===
    yield format_sse_text("好的，我已经根据您提供的数据为您生成了对应的图表分析。")
    await asyncio.sleep(0.25)

    yield format_sse_text("\n\n")
    await asyncio.sleep(0.25)

    yield format_sse_text("首先是**数量随时间变化**的折线图：\n\n")
    await asyncio.sleep(0.25)

    # === 折线图JSON（拆分发送） ===
    # 拆分1: JSON开头部分
    yield format_sse_text('{"type":"chart","chartId":"chart_time","subtype":"line",')
    await asyncio.sleep(0.25)

    # 拆分2: title和数据开始
    yield format_sse_text('"title":"数量随时间变化","data":{')
    await asyncio.sleep(0.25)

    # 拆分3: 数据部分1
    yield format_sse_text('"时间1":224,"时间2":268,')
    await asyncio.sleep(0.25)

    # 拆分4: 数据部分2
    yield format_sse_text('"时间3":307,"时间4":221')
    await asyncio.sleep(0.25)

    # 拆分5: JSON结束
    yield format_sse_text('}}')
    await asyncio.sleep(0.5)

    # === 中间文本 ===
    yield format_sse_text('\n\n从上图中可以看出，数量在"时间3"达到了峰值（307），而在"时间4"有明显的回落。')
    await asyncio.sleep(0.25)

    yield format_sse_text("\n\n")
    await asyncio.sleep(0.25)

    yield format_sse_text("接下来是**本周各分类数量**的柱状图对比：\n\n")
    await asyncio.sleep(0.25)

    # === 柱状图JSON（拆分发送，包含负值测试） ===
    # 拆分1
    yield format_sse_text('{"type":"chart","chartId":"chart_category","subtype":"bar",')
    await asyncio.sleep(0.25)

    # 拆分2
    yield format_sse_text('"title":"本周各分类数量","data":{')
    await asyncio.sleep(0.25)

    # 拆分3
    yield format_sse_text('"分类1":127,"分类2":555,')
    await asyncio.sleep(0.25)

    # 拆分4
    yield format_sse_text('"分类3":238,"分类4":700,')
    await asyncio.sleep(0.25)

    # 拆分5
    yield format_sse_text('"分类5":450')
    await asyncio.sleep(0.25)

    # 拆分6
    yield format_sse_text('}}')
    await asyncio.sleep(0.5)

    # === 结束文本 ===
    yield format_sse_text("\n\n通过对比可知，**分类4**的数据表现最为突出，达到了 700；**分类2**紧随其后（555）；**分类5**表现中等（450）；而分类1的数据量相对最少。")
    await asyncio.sleep(0.25)

    yield format_sse_text("\n\n如果您还有其他数据需要分析或调整图表格式，请随时告诉我！")
    await asyncio.sleep(0.25)

    # === 结束标志 ===
    yield f"data: {json.dumps({'type': 'end', 'data': None}, ensure_ascii=False)}\n\n"
