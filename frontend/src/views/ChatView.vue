<template>
  <div class="chat-container">
    <div class="chat-messages" ref="messagesRef">
      <ChatMessage
        v-for="msg in messages"
        :key="msg.id"
        :role="msg.role"
        :content="msg.content"
        :embeds="msg.embeds"
        :is-streaming="msg.isStreaming"
        @remove-card="handleRemoveCard"
      />
    </div>
    <ChatInput @send="handleSend" v-model:loading="isLoading" />
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, triggerRef } from 'vue'
import ChatMessage from '@/components/ChatMessage.vue'
import ChatInput from '@/components/ChatInput.vue'
import { sendChatStream } from '@/api/chat'
import type { Message, EmbedData } from '@/types'

const messages = ref<Message[]>([])
const isLoading = ref(false)
const messagesRef = ref<HTMLElement>()
let currentStreamCloser: (() => void) | null = null

triggerRef(messages)

let messageIdCounter = 0

const scrollToBottom = () => {
  nextTick(() => {
    messagesRef.value?.scrollTo({
      top: messagesRef.value.scrollHeight,
      behavior: 'smooth'
    })
  })
}

function parseEmbeds(content: string): { cleanContent: string, embeds: EmbedData[] } {
  const embeds: EmbedData[] = []
  const processedIds = new Set<string>()
  const embedStart = '{"type": "'
  let startPos = content.indexOf(embedStart)

  while (startPos !== -1) {
    let braceDepth = 0
    let pos = startPos

    while (pos < content.length) {
      if (content[pos] === '{') braceDepth++
      else if (content[pos] === '}') braceDepth--

      if (braceDepth === 0 && pos > startPos) {
        const jsonStr = content.slice(startPos, pos + 1)

        try {
          const data = JSON.parse(jsonStr)

          if (data.type === 'chart' && data.subtype && !processedIds.has(data.chartId)) {
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
            content = content.substring(0, startPos) + placeholder + content.substring(pos + 1)
            pos = startPos + placeholder.length - 1
          } else if (data.type === 'card' && data.cardId && !processedIds.has(data.cardId)) {
            processedIds.add(data.cardId)
            embeds.push({
              id: data.cardId,
              type: 'card',
              data: data
            })
            const placeholder = `[CARD:${data.cardId}]`
            content = content.substring(0, startPos) + placeholder + content.substring(pos + 1)
            pos = startPos + placeholder.length - 1
          }
        } catch (e) {
          // JSON 不完整，等待更多数据
        }
        break
      }
      pos++
    }

    startPos = content.indexOf(embedStart, pos + 1)
  }

  return { cleanContent: content, embeds }
}

const handleRemoveCard = (cardId: string) => {
  console.log('[ChatView] 移除卡片:', cardId)

  // 找到包含该卡片的消息（应该是最后一条助手消息）
  const assistantMsg = messages.value.filter(m => m.role === 'assistant').pop()
  if (!assistantMsg || !assistantMsg.embeds) return

  // 从 embeds 中移除该卡片
  const index = assistantMsg.embeds.findIndex(e => e.id === cardId)
  if (index !== -1) {
    assistantMsg.embeds.splice(index, 1)

    // 从 content 中移除占位符
    assistantMsg.content = assistantMsg.content.replace(`[CARD:${cardId}]`, '')

    triggerRef(messages)
    scrollToBottom()
  }
}

const handleSend = async (userMessage: string, testMode: boolean = false, echoMode: boolean = false) => {
  // 防止并发请求
  if (isLoading.value) {
    console.warn('[ChatView] 请求正在进行中，忽略新消息')
    return
  }

  // 清理之前的流
  if (currentStreamCloser) {
    currentStreamCloser()
    currentStreamCloser = null
  }

  const userMsg: Message = {
    id: `msg_${messageIdCounter++}`,
    role: 'user',
    content: userMessage,
    charts: [],
    timestamp: Date.now()
  }
  messages.value.push(userMsg)
  triggerRef(messages)
  scrollToBottom()

  const assistantMsg: Message = {
    id: `msg_${messageIdCounter++}`,
    role: 'assistant',
    content: '',
    charts: [],
    embeds: [],
    timestamp: Date.now(),
    isStreaming: true
  }
  messages.value.push(assistantMsg)
  triggerRef(messages)
  isLoading.value = true

  let rawContent = ''
  const collectedEmbeds = new Set<string>()

  currentStreamCloser = sendChatStream(
    userMessage,
    (content: string) => {
      rawContent += content
      const { cleanContent, embeds } = parseEmbeds(rawContent)
      assistantMsg.content = cleanContent

      for (const embed of embeds) {
        if (!collectedEmbeds.has(embed.id)) {
          collectedEmbeds.add(embed.id)
          if (!assistantMsg.embeds) assistantMsg.embeds = []
          assistantMsg.embeds.push(embed)
        }
      }

      triggerRef(messages)
      scrollToBottom()
    },
    () => {},
    (card: any) => {
      // 处理卡片事件
      if (!collectedEmbeds.has(card.cardId)) {
        collectedEmbeds.add(card.cardId)
        if (!assistantMsg.embeds) assistantMsg.embeds = []
        assistantMsg.embeds.push({
          id: card.cardId,
          type: 'card',
          data: card
        })
      }
      triggerRef(messages)
      scrollToBottom()
    },
    () => {
      assistantMsg.isStreaming = false
      isLoading.value = false
      currentStreamCloser = null
      triggerRef(messages)
      scrollToBottom()
    },
    (error: Error) => {
      assistantMsg.content += `\n[错误: ${error.message}]`
      assistantMsg.isStreaming = false
      isLoading.value = false
      currentStreamCloser = null
      triggerRef(messages)
    },
    { testMode, echoMode }
  )
}
</script>

<style scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}
</style>
