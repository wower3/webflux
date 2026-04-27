from __future__ import annotations

from abc import ABC, abstractmethod
from collections.abc import AsyncGenerator


class AiGateway(ABC):
    @abstractmethod
    async def stream(self, message: str) -> AsyncGenerator[str, None]:
        yield  # pragma: no cover
