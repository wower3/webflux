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

export interface ChartEmbedPayload {
  subtype: string
  title: string
  chartData: Record<string, number>
}

export type EmbedData =
  | { id: string; type: 'chart'; data: ChartEmbedPayload }
  | { id: string; type: 'table'; data: Record<string, unknown> }
  | { id: string; type: 'card'; data: CardData }
  | { id: string; type: 'image'; data: Record<string, unknown> }

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
  data?: string | Record<string, unknown> | null
}

export interface ConversationItem {
  conversationId: string
  createdAt: string
  messageCount: number
  active: boolean
}

export interface ConversationListResponse {
  conversations: ConversationItem[]
}

export interface MessageDTO {
  requestId: string
  conversationId: string
  role: string
  content: string
  createdAt: string
}
