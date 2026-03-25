// 类型定义

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  charts?: any[]
  embeds?: EmbedData[]
  timestamp: number
  isStreaming?: boolean
}

export interface EmbedData {
  id: string
  type: 'chart' | 'card'
  data: ChartEmbedData | CardData
}

export interface ChartEmbedData {
  subtype: string
  title: string
  chartData: Record<string, number>
}

export interface ChartData {
  id: string
  type: 'line' | 'bar' | 'pie' | 'scatter'
  title: string
  data: Record<string, number>
}

export interface CardData {
  type: string
  cardId: string
  cardName: string
  displayTitle: string
  cardInfo: CardInfoItem[]
  buttons: CardButton[]
}

export interface CardInfoItem {
  key: string
  label: string
  value: string
}

export interface CardButton {
  actionId: string
  label: string
  apiEndpoint?: string
}

export interface ChatRequest {
  message: string
  session_id?: string
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
