// 类型定义

export interface ChartData {
  id: string
  type: 'line' | 'bar' | 'pie' | 'scatter'
  title: string
  data: Record<string, number>
}

export interface CardData {
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

export interface EmbedData {
  id: string
  type: 'chart' | 'table' | 'card' | 'image'
  data: any
}

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  embeds?: EmbedData[]
  timestamp: number
  isStreaming?: boolean
}

export interface StreamEvent {
  type: 'content' | 'chart' | 'card' | 'end'
  data?: any
}
