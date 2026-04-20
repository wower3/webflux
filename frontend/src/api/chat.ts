// API接口

import type { ChatRequest } from '@/types'

export interface StreamCallbacks {
  onContent: (content: string) => void
  onChart: (chart: any) => void
  onCard: (card: any) => void
  onEnd: () => void
  onError: (error: Error) => void
}

/**
 * 创建 SSE EventSource 连接
 */
function createSSEConnection(
  url: string,
  callbacks: StreamCallbacks,
  logPrefix: string
): () => void {
  console.log(`[${logPrefix}] URL:`, url)

  const eventSource = new EventSource(url)
  let eventCount = 0
  let ended = false

  eventSource.onopen = () => {
    console.log(`[${logPrefix}] EventSource opened`)
  }

  eventSource.onmessage = (event) => {
    if (ended) return

    eventCount++
    console.log(`[${logPrefix}] Event #${eventCount}:`, event.data.substring(0, 100))

    try {
      const data = JSON.parse(event.data)
      console.log(`[${logPrefix}] Parsed:`, data.type)

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
          eventSource.close()
          callbacks.onEnd()
          break
      }
    } catch (e) {
      console.error(`[${logPrefix}] Parse error:`, e)
    }
  }

  eventSource.onerror = (error) => {
    console.error(`[${logPrefix}] Error:`, error)
    if (!ended) {
      ended = true
      eventSource.close()
      callbacks.onError(new Error('Connection error'))
    }
  }

  return () => {
    ended = true
    eventSource.close()
  }
}

/**
 * 发送流式聊天请求（Python后端，使用EventSource）
 */
export function sendChatStream(
  message: string,
  onContent: (content: string) => void,
  onChart: (chart: any) => void,
  onCard: (card: any) => void,
  onEnd: () => void,
  onError: (error: Error) => void,
  options?: { testMode?: boolean; echoMode?: boolean }
): () => void {
  console.log('[Chat API] Starting stream, message:', message, 'testMode:', options?.testMode, 'echoMode:', options?.echoMode)

  const baseUrl = 'http://localhost:8000'
  const isEcho = options?.echoMode
  const endpoint = isEcho ? '/api/chat/echo/stream' : '/api/chat/stream'
  const testParam = options?.testMode ? '&test=true' : ''
  const url = `${baseUrl}${endpoint}?message=${encodeURIComponent(message)}${testParam}`

  return createSSEConnection(url, { onContent, onChart, onCard, onEnd, onError }, 'Chat API')
}

/**
 * 发送 AI 流式聊天请求（Java后端，调用外部AI模型）
 */
export function sendAiChatStream(
  message: string,
  callbacks: StreamCallbacks,
  sessionId = 'default'
): () => void {
  const baseUrl = 'http://localhost:8080'
  const url = `${baseUrl}/api/ai/chat/stream?message=${encodeURIComponent(message)}&session_id=${sessionId}`

  return createSSEConnection(url, callbacks, 'AI Chat API')
}

/**
 * 发送 AI Mock 流式聊天请求（Java后端，返回模拟数据）
 */
export function sendMockAiChatStream(
  message: string,
  callbacks: StreamCallbacks
): () => void {
  const baseUrl = 'http://localhost:8080'
  const url = `${baseUrl}/api/ai/chat/mock/stream?message=${encodeURIComponent(message)}`

  return createSSEConnection(url, callbacks, 'AI Mock API')
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
