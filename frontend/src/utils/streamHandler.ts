// 流式响应处理工具

import type { StreamEvent } from '@/types'

// 调试开关
const DEBUG = true

/**
 * 解析SSE数据行
 */
export function parseSSELine(line: string): StreamEvent | null {
  if (!line.startsWith('data: ')) return null

  const dataStr = line.slice(6).trim()
  if (!dataStr || dataStr === '[DONE]') return null

  try {
    return JSON.parse(dataStr) as StreamEvent
  } catch (e) {
    console.error('Failed to parse SSE data:', e)
    return null
  }
}

/**
 * 处理流式响应
 */
export async function* processStream(response: Response): AsyncGenerator<StreamEvent> {
  const reader = response.body?.getReader()
  if (!reader) throw new Error('No reader available')

  const decoder = new TextDecoder()
  let buffer = ''
  let chunkCount = 0

  if (DEBUG) console.log('[StreamHandler] Starting stream processing...')

  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      if (DEBUG) console.log('[StreamHandler] Stream done, total chunks:', chunkCount)
      break
    }

    chunkCount++
    const text = decoder.decode(value, { stream: true })
    if (DEBUG) console.log(`[StreamHandler] Chunk #${chunkCount}:`, text.length, 'bytes')

    buffer += text
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      if (line.trim()) {
        if (DEBUG) console.log('[StreamHandler] Line:', line.substring(0, 50))
        const event = parseSSELine(line.trim())
        if (event) {
          if (DEBUG) console.log('[StreamHandler] Event:', event.type)
          yield event
        }
      }
    }
  }
}
