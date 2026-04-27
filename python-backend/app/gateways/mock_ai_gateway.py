from __future__ import annotations

import asyncio
import json
import logging

from app.gateways.ai_gateway import AiGateway

log = logging.getLogger(__name__)


class MockAiGateway(AiGateway):
    async def stream(self, message: str) -> AsyncGenerator[str, None]:
        log.info("[Mock AI] message=%s", message)
        await asyncio.sleep(0.3)

        chunks = [
            '好的，我正在为您分析数据...\n\n',
            '以下是本月的**销售趋势分析**：\n\n',
            '{"type":"chart","chartId":"chart_sales","subtype":"line",',
            '"title":"月度销售趋势","data":{',
            '"1月":320,"2月":450,"3月":380,',
            '"4月":510,"5月":620,"6月":580',
            '}}',
            '\n\n从趋势图来看，**5月**达到销售峰值（620），整体呈上升趋势。\n\n',
            '{"type":"chart","chartId":"chart_dept","subtype":"bar",',
            '"title":"各部门业绩对比","data":{',
            '"销售部":850,"技术部":620,',
            '"市场部":580,"人事部":200',
            '}}',
            '\n\n**销售部**业绩最为突出，达到了 850。如果您需要进一步的数据分析，请随时告诉我！',
        ]

        for chunk in chunks:
            await asyncio.sleep(0.1)
            event = json.dumps({"type": "content", "data": chunk}, ensure_ascii=False, separators=(",", ":"))
            yield event

        await asyncio.sleep(0.1)
        end_event = json.dumps({"type": "end", "data": None}, ensure_ascii=False, separators=(",", ":"))
        yield end_event
