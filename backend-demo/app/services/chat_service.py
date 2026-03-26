# -*- coding: utf-8 -*-
"""
聊天服务 - 非流式版本
"""
import asyncio
import random
import json
import re
from pathlib import Path


# 中文符号 → 英文符号映射
SYMBOL_MAP = {
    "：": ":",
    "，": ",",
    "【": "[",
    "】": "]",
    "｛": "{",
    "｝": "}",
    "《": "<",
    "》": ">",
    "；": ";",
    "（": "(",
    "）": ")",
    "！": "!",
    "？": "?",
    "\u201c": '"',  # 左双引号
    "\u201d": '"',  # 右双引号
    "\u2018": "'",  # 左单引号
    "\u2019": "'",  # 右单引号
}


def normalize_json_input(text: str) -> str:
    """
    中文符号转英文 + JSON 格式标准化
    """
    # 中文符号转英文
    for cn, en in SYMBOL_MAP.items():
        text = text.replace(cn, en)

    # 标准化 JSON 格式
    text = re.sub(r'("[^"]+")\s*:\s*', r'\1:', text)
    text = re.sub(r'\{\s*', '{', text)
    text = re.sub(r'\s*\}', '}', text)
    text = re.sub(r'\[\s*', '[', text)
    text = re.sub(r'\s*\]', ']', text)
    text = re.sub(r',\s+', ',', text)

    return text


async def process_message(message: str) -> dict:
    """
    处理用户消息，返回处理后的内容

    Args:
        message: 用户输入的消息

    Returns:
        dict: 包含原始消息和处理后消息的字典

    模拟 AI 思考时间：随机延迟 1-10 秒
    """
    # ⭐ 随机延迟 1-10 秒，模拟 AI 思考
    delay = random.uniform(1, 10)
    await asyncio.sleep(delay)

    if not message:
        return {
            "original": "",
            "content": "请输入消息内容"
        }

    trimmed = message.strip()
    normalized = normalize_json_input(trimmed)

    return {
        "original": message,
        "content": normalized
    }


def read_test_file(filename: str = "test_content.txt") -> dict:
    """
    读取测试文件内容

    Args:
        filename: 测试文件名（相对于 services 目录）

    Returns:
        dict: 包含文件内容或错误信息
    """
    file_path = Path(__file__).parent / filename

    if not file_path.exists():
        return {
            "content": f"[错误] 测试文件不存在: {filename}",
            "file_exists": False
        }

    content = file_path.read_text(encoding='utf-8')
    normalized = normalize_json_input(content)

    return {
        "content": normalized,
        "file_exists": True,
        "file_path": str(file_path),
        "original_length": len(content),
        "normalized_length": len(normalized)
    }


async def generate_chat_response(message: str) -> dict:
    """
    生成聊天响应（模拟）

    Args:
        message: 用户消息

    Returns:
        dict: 响应内容

    模拟 AI 思考时间：随机延迟 1-10 秒
    """
    # ⭐ 随机延迟 1-10 秒，模拟 AI 思考
    delay = random.uniform(1, 10)
    await asyncio.sleep(delay)

    # 简单的响应逻辑
    if "卡片" in message or "card" in message.lower():
        response = """好的，我为您生成了一个可编辑的卡片：

{"type":"card","cardId":"card_user_001","cardName":"UserInfoCard","displayTitle":"用户信息","cardInfo":[{"key":"name","label":"姓名","value":"张三"},{"key":"age","label":"年龄","value":"25"},{"key":"email","label":"邮箱","value":"zhangsan@example.com"},{"key":"phone","label":"电话","value":"13800138000"}],"buttons":[{"actionId":"edit","label":"编辑"},{"actionId":"confirm","label":"确认","apiEndpoint":"/api/user/confirm"},{"actionId":"cancel","label":"取消"}]}

您可以点击"编辑"按钮修改卡片内容，修改后点击"确认"保存，或点击"取消"删除卡片。"""
    elif "图表" in message or "chart" in message.lower():
        response = """好的，我已经根据您提供的数据为您生成了对应的图表分析。

首先是**数量随时间变化**的折线图：

{"type":"chart","chartId":"chart_time","subtype":"line","title":"数量随时间变化","data":{"时间1":224,"时间2":268,"时间3":307,"时间4":221}}

从上图中可以看出，数量在"时间3"达到了峰值（307），而在"时间4"有明显的回落。

接下来是**本周各分类数量**的柱状图对比：

{"type":"chart","chartId":"chart_category","subtype":"bar","title":"本周各分类数量","data":{"分类1":127,"分类2":555,"分类3":238,"分类4":700,"分类5":450}}

通过对比可知，**分类4**的数据表现最为突出，达到了 700；**分类2**紧随其后（555）；**分类5**表现中等（450）；而分类1的数据量相对最少。

如果您还有其他数据需要分析或调整图表格式，请随时告诉我！"""
    else:
        response = f"""收到您的消息：{message}

已为您处理完成。消息长度：{len(message)} 字符。

如有其他需求，请继续输入。"""

    return {
        "content": response,
        "message_length": len(response)
    }
