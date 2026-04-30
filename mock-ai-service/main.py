import json
import asyncio
from pathlib import Path

from fastapi import FastAPI, Request
from fastapi.responses import StreamingResponse

app = FastAPI()

SCENARIOS_DIR = Path(__file__).parent / "scenarios"
CONFIG_PATH = Path(__file__).parent / "content.json"
DEFAULT_DELAY_MS = 50


def load_content() -> dict:
    return json.loads(CONFIG_PATH.read_text(encoding="utf-8"))


@app.post("/chatabc/chat")
async def mock_chat(request: Request):
    config = load_content()
    content = config.get("content", "")
    delay_ms = config.get("delay_ms", DEFAULT_DELAY_MS)

    async def generate():
        yield 'event: chat_started\ndata: {"session_id": "mock-001"}\n\n'

        for char in content:
            chunk_data = json.dumps({"content": char}, ensure_ascii=False)
            yield f"event: chunk\ndata: {chunk_data}\n\n"
            await asyncio.sleep(delay_ms / 1000)

        msg_data = json.dumps({
            "additional_kwargs": {
                "node_id": "node-1",
                "node_title": "mock-output",
                "node_output": {"output": content}
            }
        }, ensure_ascii=False)
        yield f"event: message\ndata: {msg_data}\n\n"

        yield "event: done\ndata: {}\n\n"

    return StreamingResponse(generate(), media_type="text/event-stream",
                             headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"})


@app.post("/chatabc/init")
async def mock_init(request: Request):
    return {"session_id": "mock-001", "status": "ok"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=9999)
