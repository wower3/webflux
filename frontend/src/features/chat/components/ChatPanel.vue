<template>
  <div class="chat-panel">
    <!-- 未登录：显示登录表单 -->
    <template v-if="!isLoggedIn">
      <div class="panel-header">
        <span class="panel-title">AI 数据分析助手</span>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>
      <div class="login-container">
        <div class="login-card">
          <h3>{{ isRegister ? '创建账号' : '登录' }}</h3>
          <p class="subtitle">{{ isRegister ? '注册后即可使用' : '请登录以继续' }}</p>
          <div class="form-group">
            <label>用户名</label>
            <input v-model="username" type="text" :placeholder="isRegister ? '请输入用户名' : ''" @keyup.enter="handleAuth" />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="password" type="password" placeholder="请输入密码" @keyup.enter="handleAuth" />
          </div>
          <button class="auth-btn" @click="handleAuth" :disabled="loading || !username || !password">
            {{ loading ? '请求中...' : (isRegister ? '注册' : '登录') }}
          </button>
          <p class="switch-mode" @click="isRegister = !isRegister">
            {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
          </p>
          <p v-if="error" class="error-msg">{{ error }}</p>
        </div>
      </div>
    </template>

    <!-- 已登录：聊天界面 -->
    <template v-else>
      <div class="panel-header">
        <button class="icon-btn" @click="showSidebar = !showSidebar" title="会话列表">
          <i class="fa fa-bars"></i>
        </button>
        <span class="panel-title">AI 数据分析助手</span>
        <span class="username">{{ usernameDisplay }}</span>
        <button class="icon-btn logout-btn" @click="logout" title="退出">
          <i class="fa fa-sign-out"></i>
        </button>
        <button class="icon-btn close-btn" @click="$emit('close')" title="关闭">
          <i class="fa fa-times"></i>
        </button>
      </div>
      <div class="panel-body">
        <!-- 侧栏（点击切换） -->
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

        <!-- 聊天区 -->
        <div class="chat-area">
          <ChatView ref="chatViewRef" :conversation-id="currentConversationId" @send-from-guide="handleSendFromGuide" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ChatView from './ChatView.vue'
import type { Message, MessageDTO, ConversationItem } from '../types'

const emit = defineEmits<{
  close: []
}>()

const isLoggedIn = ref(!!localStorage.getItem('chat_token'))
const token = () => localStorage.getItem('chat_token') || ''
const usernameDisplay = ref(localStorage.getItem('chat_username') || '')

// 登录表单
const isRegister = ref(false)
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

// 会话管理
const showSidebar = ref(false)
const conversations = ref<ConversationItem[]>([])
const currentConversationId = ref<string | null>(null)
const chatViewRef = ref<InstanceType<typeof ChatView>>()

const handleAuth = async () => {
  if (loading.value || !username.value || !password.value) return
  loading.value = true
  error.value = ''

  try {
    const endpoint = isRegister.value ? '/api/auth/register' : '/api/auth/login'
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    const data = await res.json()

    if (res.ok) {
      localStorage.setItem('chat_token', data.token)
      localStorage.setItem('chat_username', data.username)
      isLoggedIn.value = true
      usernameDisplay.value = data.username
      await initChat()
    } else {
      error.value = data.message || '操作失败'
    }
  } catch (e) {
    error.value = '网络错误'
  } finally {
    loading.value = false
  }
}

const logout = () => {
  localStorage.removeItem('chat_token')
  localStorage.removeItem('chat_username')
  isLoggedIn.value = false
  usernameDisplay.value = ''
  conversations.value = []
  currentConversationId.value = null
  showSidebar.value = false
}

const initChat = async () => {
  await fetchConversations()
  if (conversations.value.length > 0) {
    currentConversationId.value = conversations.value[0].conversationId
    selectConversation(conversations.value[0].conversationId)
  } else {
    await createConversation()
  }
}

const fetchConversations = async () => {
  try {
    const res = await fetch('/api/conversations', {
      headers: { 'Authorization': `Bearer ${token()}` }
    })
    const data = await res.json()
    conversations.value = data.conversations || []
  } catch {
    // silently fail — will retry on next user action
  }
}

const createConversation = async () => {
  try {
    const res = await fetch('/api/conversation', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token()}`
      }
    })
    const data = await res.json()
    if (res.ok) {
      await fetchConversations()
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
    const res = await fetch(`/api/conversation/${conversationId}/messages`, {
      headers: { 'Authorization': `Bearer ${token()}` }
    })
    const data = await res.json()
    const msgs: Message[] = data.map((m: MessageDTO) => ({
      id: m.requestId,
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

// 面板打开时，如果已登录则初始化
onMounted(async () => {
  if (isLoggedIn.value) {
    await initChat()
  }
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

.logout-btn:hover {
  background: #fef2f2;
  color: #ef4444;
}

.username {
  font-size: 13px;
  color: #9ca3af;
}

/* 登录 */
.login-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
}

.login-card {
  background: #fff;
  padding: 36px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  width: 340px;
}

.login-card h3 {
  margin: 0 0 4px;
  font-size: 20px;
  color: #1f2937;
}

.subtitle {
  color: #9ca3af;
  margin: 0 0 28px;
  font-size: 13px;
}

.form-group {
  margin-bottom: 16px;
  text-align: left;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.form-group input {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-group input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12);
}

.auth-btn {
  width: 100%;
  padding: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

.auth-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.auth-btn:not(:disabled):hover {
  opacity: 0.9;
}

.switch-mode {
  margin-top: 16px;
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
  text-align: center;
}

.switch-mode:hover {
  text-decoration: underline;
}

.error-msg {
  margin-top: 12px;
  color: #ef4444;
  font-size: 13px;
  text-align: center;
}

/* 主体 */
.panel-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 侧栏 */
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
