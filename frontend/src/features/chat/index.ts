import type { App } from 'vue'

// 组件
export { default as ChatWidget } from './components/ChatWidget.vue'
export { default as ChatPanel } from './components/ChatPanel.vue'
export { default as ChatView } from './components/ChatView.vue'
export { default as ChatMessage } from './components/ChatMessage.vue'
export { default as ChatInput } from './components/ChatInput.vue'
export { default as GuidedPrompts } from './components/GuidedPrompts.vue'
export { default as MarkdownRenderer } from './components/MarkdownRenderer.vue'
export { default as ChartRenderer } from './components/ChartRenderer.vue'
export { default as CardRenderer } from './components/CardRenderer.vue'

// Composable
export { useChat } from './composables/useChat'

// API
export { sendChatStream, type StreamCallbacks } from './api/chat'

// Utils
export { toEChartsOption } from './utils/chartParser'

// Types
export type {
  ChartData,
  CardData,
  CardInfoItem,
  CardButton,
  EmbedData,
  Message,
  StreamEvent
} from './types'

/**
 * Vue Plugin - 一键注册所有聊天组件
 */
export const ChatPlugin = {
  install(app: App) {
    app.component('ChatWidget', ChatWidget)
    app.component('ChatPanel', ChatPanel)
    app.component('ChatView', ChatView)
    app.component('ChatMessage', ChatMessage)
    app.component('ChatInput', ChatInput)
    app.component('GuidedPrompts', GuidedPrompts)
    app.component('MarkdownRenderer', MarkdownRenderer)
    app.component('ChartRenderer', ChartRenderer)
    app.component('CardRenderer', CardRenderer)
  }
}
