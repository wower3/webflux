// API接口

import type { ChatRequest } from '@/types'

/**
 * 发送流式聊天请求（使用EventSource）
 */
export function sendChatStream(
  message: string,
  onContent: (content: string) => void,
  onChart: (chart: any) => void,
  onEnd: () => void,
  onError: (error: Error) => void,
  options?: { testMode?: boolean }
): () => void {
  console.log('[Chat API] Starting stream, message:', message, 'testMode:', options?.testMode)

  // 直接访问后端，绕过Vite代理
  const baseUrl = 'http://localhost:8000'
  const testParam = options?.testMode ? '&test=true' : ''
  const url = `${baseUrl}/api/chat/stream?message=${encodeURIComponent(message)}${testParam}`

  console.log('[Chat API] URL:', url)

  const eventSource = new EventSource(url)
  let eventCount = 0
  let ended = false

  eventSource.onopen = () => {
    console.log('[Chat API] EventSource opened')
  }

  eventSource.onmessage = (event) => {
    if (ended) return

    eventCount++
    console.log(`[Chat API] Event #${eventCount}:`, event.data.substring(0, 100))

    try {
      const data = JSON.parse(event.data)
      console.log('[Chat API] Parsed:', data.type)

      switch (data.type) {
        case 'content':
          onContent(data.data as string)
          break
        case 'chart':
          onChart(data.data)
          break
        case 'end':
          console.log('[Chat API] End event received, total events:', eventCount)
          ended = true
          eventSource.close()
          onEnd()
          break
      }
    } catch (e) {
      console.error('[Chat API] Parse error:', e)
    }
  }

  eventSource.onerror = (error) => {
    console.error('[Chat API] Error:', error)
    if (!ended) {
      ended = true
      eventSource.close()
      onError(new Error('Connection error'))
    }
  }

  // 返回关闭函数
  return () => {
    ended = true
    eventSource.close()
  }
}

/**
 * 发送普通聊天请求（非流式）
 */
export async function sendChat(request: ChatRequest) {
  const response = await fetch('http://localhost:8000/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  })
  return await response.json()
}
