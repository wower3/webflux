/**
 * 解析嵌入数据（图表、卡片）
 * 从文本中提取 JSON 格式的嵌入数据
 * 前端自动生成 ID，不依赖后端提供的 chartId/cardId
 */
import type { EmbedData } from '@/types'

export interface ParseResult {
  cleanContent: string
  embeds: EmbedData[]
}

/**
 * 生成唯一 ID
 * 格式: {prefix}_{timestamp}_{random}
 */
const generateId = (prefix: string) =>
  `${prefix}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

export function parseEmbeds(content: string): ParseResult {
  const embeds: EmbedData[] = []
  const embedStart = '{"type":"'
  let resultContent = content

  let startPos = resultContent.indexOf(embedStart)

  while (startPos !== -1) {
    let braceDepth = 0
    let bracketDepth = 0
    let pos = startPos

    while (pos < resultContent.length) {
      const char = resultContent[pos]
      if (char === '{') braceDepth++
      else if (char === '}') braceDepth--
      else if (char === '[') bracketDepth++
      else if (char === ']') bracketDepth--

      // 当括号和方括号都回到0时，表示完整的JSON
      if (braceDepth === 0 && bracketDepth === 0 && pos > startPos) {
        const jsonStr = resultContent.slice(startPos, pos + 1)

        try {
          const data = JSON.parse(jsonStr)

          // 解析图表（自动生成 ID，丢弃 chartId 等干扰字段）
          if (data.type === 'chart' && data.subtype) {
            const id = generateId('chart')

            embeds.push({
              id: id,
              type: 'chart',
              data: {
                subtype: data.subtype,
                title: data.title || '',
                chartData: data.data || {}
              }
            })

            const placeholder = `[CHART:${id}]`
            resultContent = resultContent.substring(0, startPos) + placeholder + resultContent.substring(pos + 1)
            pos = startPos + placeholder.length - 1
          }

          // 解析卡片（自动生成 ID，丢弃 cardId 等干扰字段）
          else if (data.type === 'card' && data.cardName) {
            const id = generateId('card')

            embeds.push({
              id: id,
              type: 'card',
              data: data
            })

            const placeholder = `[CARD:${id}]`
            resultContent = resultContent.substring(0, startPos) + placeholder + resultContent.substring(pos + 1)
            pos = startPos + placeholder.length - 1
          }
        } catch (e) {
          // JSON 不完整，跳过
        }
        break
      }
      pos++
    }

    startPos = resultContent.indexOf(embedStart, pos + 1)
  }

  return { cleanContent: resultContent, embeds }
}
