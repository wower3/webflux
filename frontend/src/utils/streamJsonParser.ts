/**
 * 流式JSON解析器
 * 用于处理跨SSE事件的图表JSON拼接和解析
 * 支持JSON被任意拆分发送的场景
 */

import type { ChartData } from '@/types'

export interface ParseResult {
  content: string        // 可显示的内容（已移除完整JSON）
  charts: ChartData[]    // 解析出的图表数据
  hasPending: boolean    // 是否有未完成的JSON正在缓存
}

export class StreamJsonParser {
  private buffer = ''           // 接收缓冲区
  private displayText = ''      // 已确认可显示的文本
  private charts: ChartData[] = []  // 解析出的图表
  private braceDepth = 0        // 花括号深度
  private inJson = false        // 是否正在解析JSON
  private jsonStartPos = -1     // JSON起始位置（在buffer中的索引）
  private processedPos = 0      // 已处理的buffer位置

  // 图表JSON开头标识
  private readonly CHART_JSON_START = '{"type":"chart"'

  /**
   * 追加新的文本内容
   */
  append(chunk: string): ParseResult {
    this.buffer += chunk
    this.processBuffer()
    return this.getResult()
  }

  /**
   * 处理缓冲区，查找并提取完整的图表JSON
   */
  private processBuffer(): void {
    let i = this.processedPos  // 从上次处理位置继续

    while (i < this.buffer.length) {
      if (!this.inJson) {
        // 不在JSON模式，查找图表JSON开头
        const startIndex = this.buffer.indexOf(this.CHART_JSON_START, i)
        if (startIndex !== -1) {
          // 找到JSON开头，之前的文本可以显示
          this.displayText += this.buffer.slice(i, startIndex)
          i = startIndex

          this.inJson = true
          this.jsonStartPos = i
          this.braceDepth = 1  // 从第一个{开始计数
          i++  // 跳过{
        } else {
          // 没有JSON，所有剩余文本都可以显示
          // 更新displayText但不清空buffer（因为可能下一chunk包含JSON开头）
          this.displayText += this.buffer.slice(i)
          this.processedPos = this.buffer.length
          return
        }
      } else {
        // 在JSON模式，追踪括号深度
        const char = this.buffer[i]

        if (char === '{') {
          this.braceDepth++
        } else if (char === '}') {
          this.braceDepth--
        }

        i++

        // 括号深度回到0，JSON完整了
        if (this.braceDepth === 0) {
          const jsonStr = this.buffer.slice(this.jsonStartPos, i)

          // 尝试解析JSON
          if (this.tryParseChart(jsonStr)) {
            // 解析成功，继续处理剩余内容
            this.buffer = this.buffer.slice(i)
            this.processedPos = 0
            this.inJson = false
            this.braceDepth = 0
            this.jsonStartPos = -1
            i = 0  // 从新buffer开头继续
          } else {
            // 解析失败，等待更多数据
            return
          }
        }
      }
    }

    // 更新已处理位置
    this.processedPos = i
  }

  /**
   * 尝试解析图表JSON
   */
  private tryParseChart(jsonStr: string): boolean {
    try {
      const data = JSON.parse(jsonStr)

      // 验证是否是图表数据
      if (data.type === 'chart' && data.subtype) {
        this.charts.push({
          id: data.chartId || `chart_${Date.now()}_${this.charts.length}`,
          type: data.subtype,
          title: data.title || '',
          data: data.data || {}
        })
        return true
      }
    } catch (e) {
      // JSON解析失败，可能还没接收完整
    }
    return false
  }

  /**
   * 获取当前解析结果
   */
  getResult(): ParseResult {
    let content = this.displayText

    if (!this.inJson) {
      // 不在JSON模式，剩余buffer都可以显示
      content += this.buffer.slice(this.processedPos)
    } else {
      // 在JSON模式，显示已处理位置之前的内容（JSON之后的部分）
      content += this.buffer.slice(this.processedPos)
    }

    return {
      content,
      charts: [...this.charts],
      hasPending: this.inJson
    }
  }

  /**
   * 重置解析器（用于新的消息）
   */
  reset(): void {
    this.buffer = ''
    this.displayText = ''
    this.charts = []
    this.braceDepth = 0
    this.inJson = false
    this.jsonStartPos = -1
    this.processedPos = 0
  }

  /**
   * 获取当前所有解析出的图表
   */
  getCharts(): ChartData[] {
    return [...this.charts]
  }

  /**
   * 获取当前可显示的内容
   */
  getContent(): string {
    return this.getResult().content
  }

  /**
   * 是否有未完成的JSON正在缓存
   */
  isParsingJson(): boolean {
    return this.inJson
  }
}

// 导出单例模式函数，方便使用
export function createStreamJsonParser(): StreamJsonParser {
  return new StreamJsonParser()
}
