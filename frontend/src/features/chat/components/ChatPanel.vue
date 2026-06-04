<template>
  <div class="chat-panel">
    <div class="panel-header">
      <button class="icon-btn" @click="showSidebar = !showSidebar" title="会话列表">
        <i class="fa fa-bars"></i>
      </button>
      <span class="panel-title">AI 数据分析助手</span>
      <button class="icon-btn close-btn" @click="$emit('close')" title="关闭">
        <i class="fa fa-times"></i>
      </button>
    </div>
    <div class="panel-body">
      <Transition name="sidebar">
        <div v-if="showSidebar" class="panel-sidebar">
          <button class="new-chat-btn" @click="createConversation">
            <i class="fa fa-plus"></i>
            新建对话
          </button>
          <div class="conversation-list">
            <div
              v-for="conv in conversations"
              :key="conv.conversationId"
              class="conv-item"
              :class="{ active: conv.conversationId === currentConversationId }"
              @click="selectConversation(conv.conversationId)"
            >
              <div class="conv-title">
                <i class="fa fa-message"></i>
                <span>{{ formatConvName(conv.createdAt) }}</span>
              </div>
              <div class="conv-time">{{ formatRelativeTime(conv.createdAt) }}</div>
            </div>
            <div v-if="conversations.length === 0" class="empty-hint">暂无对话</div>
          </div>
        </div>
      </Transition>

      <div class="chat-area">
        <ChatView ref="chatViewRef" :conversation-id="currentConversationId" @send-from-guide="handleSendFromGuide" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ChatView from './ChatView.vue'
import type { Message, MessageDTO, ConversationItem } from '../types'

const emit = defineEmits<{
  close: []
}>()

const getUserId = () => '1'

const showSidebar = ref(false)
const conversations = ref<ConversationItem[]>([])
const currentConversationId = ref<string | null>(null)
const chatViewRef = ref<InstanceType<typeof ChatView>>()

const initChat = async () => {
  await fetchConversations()
  await createConversation()
}

const fetchConversations = async () => {
  try {
    const res = await fetch(`/chatbot/conversations?userId=${getUserId()}`)
    const data = await res.json()
    conversations.value = data.conversations || []
  } catch {
    // silently fail
  }
}

const createConversation = async () => {
  try {
    const res = await fetch(`/chatbot/conversation?userId=${getUserId()}`, {
      method: 'POST'
    })
    const data = await res.json()
    if (res.ok) {
      conversations.value.unshift({
        conversationId: data.conversationId,
        createdAt: data.createdAt,
        messageCount: 0,
        active: true
      })
      currentConversationId.value = data.conversationId
      chatViewRef.value?.clearMessages()
      showSidebar.value = false
    }
  } catch {
    // silently fail
  }
}

const selectConversation = async (conversationId: string) => {
  currentConversationId.value = conversationId
  try {
    const res = await fetch(`/chatbot/conversation/${conversationId}/messages?userId=${getUserId()}`)
    const data = await res.json()
    const msgs: Message[] = data.map((m: MessageDTO) => ({
      id: `${m.requestId}_${m.role}`,
      role: m.role,
      content: m.content,
      timestamp: new Date(m.createdAt).getTime()
    }))
    chatViewRef.value?.loadMessages(msgs)
    showSidebar.value = false
  } catch {
    // silently fail
  }
}

const handleSendFromGuide = (text: string) => {
  chatViewRef.value?.sendMessage(text)
}

const formatConvName = (dateStr: string) => {
  if (!dateStr) return '新对话'
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const formatRelativeTime = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - d.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour}小时前`
  return `${d.getMonth() + 1}/${d.getDate()}`
}

onMounted(async () => {
  await initChat()
})
</script>

<style scoped>
.chat-panel {
  width: 80%;
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.15);
}

.panel-header {
  height: 52px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  flex-shrink: 0;
}

.panel-title {
  flex: 1;
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.15s;
}

.icon-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

.close-btn:hover {
  background: #fef2f2;
  color: #ef4444;
}

.panel-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.panel-sidebar {
  width: 240px;
  background: #f9fafb;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.new-chat-btn {
  margin: 12px;
  padding: 9px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.new-chat-btn:hover {
  opacity: 0.9;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 6px 12px;
}

.conv-item {
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 3px;
}

.conv-item:hover {
  background: #eef2ff;
}

.conv-item.active {
  background: #e0e7ff;
}

.conv-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #1f2937;
}

.conv-title i {
  color: #9ca3af;
  font-size: 11px;
  flex-shrink: 0;
}

.conv-time {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 3px;
  padding-left: 19px;
}

.empty-hint {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  padding: 32px 0;
}

/* 聊天区 */
.chat-area {
  flex: 1;
  overflow: hidden;
}

/* 侧栏动画 */
.sidebar-enter-active { transition: width 0.2s ease, opacity 0.2s ease; }
.sidebar-leave-active { transition: width 0.15s ease, opacity 0.15s ease; }
.sidebar-enter-from, .sidebar-leave-to { width: 0; opacity: 0; overflow: hidden; }
</style>
