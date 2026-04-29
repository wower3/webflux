const getToken = () => localStorage.getItem('chat_token') || '';
function createSSEConnection(url, body, callbacks) {
    let ended = false;
    let abortController = null;
    const close = () => {
        ended = true;
        abortController?.abort();
    };
    abortController = new AbortController();
    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`
        },
        body: JSON.stringify(body),
        signal: abortController.signal
    }).then(async (response) => {
        if (!response.ok) {
            callbacks.onError(new Error(`HTTP ${response.status}`));
            return;
        }
        if (!response.body) {
            callbacks.onError(new Error('No response body'));
            return;
        }
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        while (!ended) {
            const { done, value } = await reader.read();
            if (done)
                break;
            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop() || '';
            for (const line of lines) {
                if (!line.startsWith('data:'))
                    continue;
                const dataStr = line.slice(5).trim();
                if (!dataStr)
                    continue;
                try {
                    const event = JSON.parse(dataStr);
                    if (event.type === 'end') {
                        ended = true;
                        callbacks.onEnd();
                    }
                    else if (event.type === 'final_output') {
                        callbacks.onFinalOutput(String(event.data ?? ''));
                    }
                    else {
                        callbacks.onContent(String(event.data ?? ''));
                    }
                }
                catch {
                    callbacks.onContent(dataStr);
                }
            }
        }
        if (!ended) {
            callbacks.onEnd();
        }
    }).catch((err) => {
        if (!ended) {
            ended = true;
            callbacks.onError(err instanceof Error ? err : new Error(String(err)));
        }
    });
    return close;
}
/**
 * 发送 AI 流式聊天请求
 */
export function sendChatStream(message, callbacks, conversationId) {
    const body = { message };
    if (conversationId) {
        body.conversationId = conversationId;
    }
    return createSSEConnection('/api/chat/stream', body, callbacks);
}
