// API接口 - Java后端（POST + fetch SSE）

const getToken = (): string => localStorage.getItem('chat_token') || ''

export interface StreamCallbacks {
  onContent: (content: string) => void
  onChart: (chart: any) => void
  onCard: (card: any) => void
  onEnd: () => void
  onError: (error: Error) => void
}

function createSSEConnection(
  url: string,
  body: object,
  callbacks: StreamCallbacks,
  logPrefix: string
): () => void {
  console.log(`[${logPrefix}] POST ${url}`, body)

  let eventCount = 0
  let ended = false
  let abortController: AbortController | null = null

  const close = () => {
    ended = true
    abortController?.abort()
  }

  abortController = new AbortController()

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
      callbacks.onError(new Error(`HTTP ${response.status}`))
      return
    }

    console.log(`[${logPrefix}] Connected`)

    const reader = response.body!.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (!ended) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (!line.startsWith('data:')) continue

        const dataStr = line.slice(5).trim()
        if (!dataStr) continue

        eventCount++
        console.log(`[${logPrefix}] Event #${eventCount}:`, dataStr.substring(0, 100))

        try {
          const data = JSON.parse(dataStr)

          switch (data.type) {
            case 'content':
              callbacks.onContent(data.data as string)
              break
            case 'chart':
              callbacks.onChart(data.data)
              break
            case 'card':
              callbacks.onCard(data.data)
              break
            case 'end':
              console.log(`[${logPrefix}] End event, total:`, eventCount)
              ended = true
              callbacks.onEnd()
              break
          }
        } catch (e) {
          console.error(`[${logPrefix}] Parse error:`, e)
        }
      }
    }
  }).catch((err) => {
    if (!ended) {
      console.error(`[${logPrefix}] Error:`, err)
      ended = true
      callbacks.onError(err instanceof Error ? err : new Error(String(err)))
    }
  })

  return close
}

/**
 * 发送 AI 流式聊天请求
 */
export function sendChatStream(
  message: string,
  callbacks: StreamCallbacks,
  conversationId?: string
): () => void {
  const body: any = { message }
  if (conversationId) {
    body.conversationId = conversationId
  }
  return createSSEConnection('/api/chat/stream', body, callbacks, 'Chat Stream')
}
