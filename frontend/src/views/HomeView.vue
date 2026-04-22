<template>
  <div class="home-page">
    <!-- 顶部栏 -->
    <header class="header">
      <div class="header-left">
        <i class="fa fa-cube"></i>
        <span class="header-title">AI 数据分析助手</span>
      </div>
      <div class="header-right">
        <span class="username">{{ username }}</span>
        <button class="logout-btn" @click="logout">退出</button>
      </div>
    </header>

    <!-- 主体 -->
    <div class="main-area">
      <!-- 左侧会话列表 -->
      <aside class="sidebar">
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
              <span>{{ conv.title || '新对话' }}</span>
            </div>
            <div class="conv-time">{{ formatTime(conv.createdAt) }}</div>
          </div>
          <div v-if="conversations.length === 0" class="empty-hint">暂无对话</div>
        </div>
      </aside>

      <!-- 右侧聊天区 -->
      <div class="chat-area">
        <ChatSidebar ref="chatSidebarRef" :conversation-id="currentConversationId" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatSidebar } from '@/features/chat'
import type { Message } from '@/features/chat'

const router = useRouter()
const username = ref(localStorage.getItem('chat_username') || '')
const conversations = ref<any[]>([])
const currentConversationId = ref<string | null>(null)
const chatSidebarRef = ref<InstanceType<typeof ChatSidebar>>()

const token = () => localStorage.getItem('chat_token') || ''

const fetchConversations = async () => {
  try {
    const res = await fetch('/api/conversations', {
      headers: { 'Authorization': `Bearer ${token()}` }
    })
    const data = await res.json()
    conversations.value = data.conversations || []
  } catch (e) {
    console.error('获取会话列表失败', e)
  }
}

const fetchMessages = async (conversationId: string) => {
  try {
    const res = await fetch(`/api/conversation/${conversationId}/messages`, {
      headers: { 'Authorization': `Bearer ${token()}` }
    })
    const data = await res.json()
    const msgs: Message[] = data.map((m: any) => ({
      id: m.requestId,
      role: m.role,
      content: m.content,
      timestamp: new Date(m.createdAt).getTime()
    }))
    chatSidebarRef.value?.loadMessages(msgs)
  } catch (e) {
    console.error('获取消息失败', e)
  }
}

const createConversation = async () => {
  try {
    const res = await fetch('/api/conversation', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token()}`
      }
    })
    const data = await res.json()
    if (res.ok) {
      await fetchConversations()
      currentConversationId.value = data.conversationId
      chatSidebarRef.value?.clearMessages()
    }
  } catch (e) {
    console.error('创建会话失败', e)
  }
}

const selectConversation = async (conversationId: string) => {
  currentConversationId.value = conversationId
  await fetchMessages(conversationId)
}

const logout = () => {
  localStorage.removeItem('chat_token')
  localStorage.removeItem('chat_username')
  router.push('/login')
}

const formatTime = (dateStr: string) => {
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
  await fetchConversations()
  if (conversations.value.length > 0) {
    const latest = conversations.value[0]
    currentConversationId.value = latest.conversationId
    await fetchMessages(latest.conversationId)
  }
})
</script>

<style scoped>
.home-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #1f2937;
}

.header-left i {
  color: #667eea;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-size: 14px;
  color: #6b7280;
}

.logout-btn {
  padding: 4px 14px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  color: #6b7280;
  font-size: 13px;
  cursor: pointer;
}

.logout-btn:hover {
  border-color: #ef4444;
  color: #ef4444;
}

.main-area {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.new-chat-btn {
  margin: 16px;
  padding: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.new-chat-btn:hover {
  opacity: 0.9;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 16px;
}

.conv-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 4px;
}

.conv-item:hover {
  background: #f3f4f6;
}

.conv-item.active {
  background: #eef2ff;
}

.conv-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-title i {
  color: #9ca3af;
  font-size: 12px;
  flex-shrink: 0;
}

.conv-time {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.empty-hint {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  padding: 40px 0;
}

.chat-area {
  flex: 1;
  overflow: hidden;
}
</style>
