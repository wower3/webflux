// 类型定义

export interface ChartData {
  id: string
  type: 'line' | 'bar' | 'pie' | 'scatter'
  title: string
  data: Record<string, number>
}

export interface EmbedData {
  id: string
  type: 'chart' | 'table' | 'card' | 'image'
  data: any
}

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  charts: ChartData[]      // 旧字段，保留兼容
  embeds?: EmbedData[]      // 新字段，通用嵌入数据
  timestamp: number
  isStreaming?: boolean
}

export interface StreamEvent {
  type: 'content' | 'chart' | 'end'
  data?: any
}

export interface ChatRequest {
  message: string
  session_id?: string
}
