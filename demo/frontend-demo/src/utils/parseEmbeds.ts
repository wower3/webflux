/**
 * 解析嵌入数据（图表、卡片）
 * 从文本中提取 JSON 格式的嵌入数据
 */
import type { EmbedData } from '@/types'

export interface ParseResult {
  cleanContent: string
  embeds: EmbedData[]
}

export function parseEmbeds(content: string): ParseResult {
  const embeds: EmbedData[] = []
  const processedIds = new Set<string>()
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

          // 解析图表
          if (data.type === 'chart' && data.subtype && data.chartId && !processedIds.has(data.chartId)) {
            processedIds.add(data.chartId)
            embeds.push({
              id: data.chartId,
              type: 'chart',
              data: {
                subtype: data.subtype,
                title: data.title || '',
                chartData: data.data || {}
              }
            })
            const placeholder = `[CHART:${data.chartId}]`
            resultContent = resultContent.substring(0, startPos) + placeholder + resultContent.substring(pos + 1)
            pos = startPos + placeholder.length - 1
          }
          // 解析卡片
          else if (data.type === 'card' && data.cardId && !processedIds.has(data.cardId)) {
            processedIds.add(data.cardId)
            embeds.push({
              id: data.cardId,
              type: 'card',
              data: data
            })
            const placeholder = `[CARD:${data.cardId}]`
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
