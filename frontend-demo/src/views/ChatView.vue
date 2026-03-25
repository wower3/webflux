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
        @update-card="handleUpdateCard"
      />
      <div v-if="messages.length === 0" class="empty-state">
        <i class="fa fa-comments"></i>
        <p>开始对话吧！</p>
      </div>
    </div>
    <ChatInput @send="handleSend" v-model:loading="isLoading" />
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import ChatMessage from '@/components/ChatMessage.vue'
import ChatInput from '@/components/ChatInput.vue'
import { sendChat, sendEcho, sendTest } from '@/api/chat'
import { parseEmbeds } from '@/utils/parseEmbeds'
import type { Message, EmbedData, CardData } from '@/types'

const messages = ref<Message[]>([])
const isLoading = ref(false)
const messagesRef = ref<HTMLElement>()

let messageIdCounter = 0

const scrollToBottom = () => {
  nextTick(() => {
    messagesRef.value?.scrollTo({
      top: messagesRef.value.scrollHeight,
      behavior: 'smooth'
    })
  })
}

const handleRemoveCard = (cardId: string) => {
  // 找到最后一条助手消息
  const assistantMsg = [...messages.value].reverse().find(m => m.role === 'assistant')
  if (!assistantMsg || !assistantMsg.embeds) return

  // 从 embeds 中移除该卡片
  const index = assistantMsg.embeds.findIndex(e => e.id === cardId)
  if (index !== -1) {
    assistantMsg.embeds.splice(index, 1)
    // 从 content 中移除占位符
    assistantMsg.content = assistantMsg.content.replace(`[CARD:${cardId}]`, '')
  }
}

const handleUpdateCard = (updatedCard: CardData) => {
  // 找到最后一条助手消息
  const assistantMsg = [...messages.value].reverse().find(m => m.role === 'assistant')
  if (!assistantMsg || !assistantMsg.embeds) return

  // 更新 embeds 中的卡片数据
  const embed = assistantMsg.embeds.find(e => e.id === updatedCard.cardId)
  if (embed && embed.type === 'card') {
    embed.data = updatedCard
  }
}

const handleSend = async (userMessage: string, testMode: boolean = false, echoMode: boolean = false) => {
  if (isLoading.value) return

  // 添加用户消息
  const userMsg: Message = {
    id: `msg_${messageIdCounter++}`,
    role: 'user',
    content: userMessage,
    charts: [],
    timestamp: Date.now()
  }
  messages.value.push(userMsg)
  scrollToBottom()

  isLoading.value = true

  try {
    let content = ''
    let embeds: EmbedData[] = []

    // 根据模式调用不同的 API
    if (echoMode) {
      // 回显模式
      const result = await sendEcho(userMessage)
      content = result.data.content
    } else if (testMode) {
      // 测试文件模式
      const result = await sendTest()
      content = result.data.content
    } else {
      // 普通聊天模式
      const result = await sendChat({ message: userMessage })
      content = result.data.content
    }

    // 解析嵌入数据
    const parseResult = parseEmbeds(content)
    content = parseResult.cleanContent
    embeds = parseResult.embeds

    // 添加助手消息
    const assistantMsg: Message = {
      id: `msg_${messageIdCounter++}`,
      role: 'assistant',
      content,
      charts: [],
      embeds,
      timestamp: Date.now(),
      isStreaming: false
    }
    messages.value.push(assistantMsg)
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    const errorMsg: Message = {
      id: `msg_${messageIdCounter++}`,
      role: 'assistant',
      content: `[错误: ${error instanceof Error ? error.message : '未知错误'}]`,
      charts: [],
      timestamp: Date.now(),
      isStreaming: false
    }
    messages.value.push(errorMsg)
    scrollToBottom()
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f9fafb;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9ca3af;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 18px;
  margin: 0;
}
</style>
