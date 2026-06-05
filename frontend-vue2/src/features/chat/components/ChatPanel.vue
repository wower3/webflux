<template>
  <div class="chat-panel" :style="{ width: panelWidth + '%' }">
    <div class="drag-handle" @mousedown="startDrag"></div>
    <div class="panel-header">
      <button class="icon-btn" @click="showSidebar = !showSidebar" title="会话列表">
        <i class="fa fa-bars"></i>
      </button>
      <i class="fa fa-cube header-icon"></i>
      <span class="panel-title">AI 数据分析助手</span>
      <button class="icon-btn close-btn" @click="$emit('close')" title="关闭">
        <i class="fa fa-times"></i>
      </button>
    </div>
    <div class="panel-body">
      <transition name="sidebar">
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
      </transition>

      <div class="chat-area">
        <ChatView ref="chatViewRef" :conversation-id="currentConversationId" />
      </div>
    </div>
  </div>
</template>

<script>
import ChatView from './ChatView.vue'
import { getToken } from '@/utils/auth'

export default {
  name: 'ChatPanel',
  components: { ChatView },
  data: function () {
    return {
      showSidebar: false,
      conversations: [],
      currentConversationId: null,
      panelWidth: 80,
      isDragging: false
    }
  },
  mounted: function () {
    this.panelWidth = this.getDefaultWidth()
    this.initChat()
  },
  beforeDestroy: function () {
    this.cleanDragListeners()
  },
  methods: {
    getDefaultWidth: function () {
      var physicalWidth = screen.width * window.devicePixelRatio
      if (physicalWidth >= 3840) return 30
      if (physicalWidth >= 2560) return 40
      return 50
    },
    startDrag: function (e) {
      e.preventDefault()
      this.isDragging = true
      document.body.style.cursor = 'col-resize'
      document.body.style.userSelect = 'none'
      this._onMouseMove = this.onDragMove.bind(this)
      this._onMouseUp = this.onDragEnd.bind(this)
      document.addEventListener('mousemove', this._onMouseMove)
      document.addEventListener('mouseup', this._onMouseUp)
    },
    onDragMove: function (e) {
      var newWidth = ((window.innerWidth - e.clientX) / window.innerWidth) * 100
      if (newWidth < 30) newWidth = 30
      if (newWidth > 80) newWidth = 80
      this.panelWidth = newWidth
    },
    onDragEnd: function () {
      this.isDragging = false
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
      this.cleanDragListeners()
    },
    cleanDragListeners: function () {
      if (this._onMouseMove) {
        document.removeEventListener('mousemove', this._onMouseMove)
        this._onMouseMove = null
      }
      if (this._onMouseUp) {
        document.removeEventListener('mouseup', this._onMouseUp)
        this._onMouseUp = null
      }
    },
    getUserId: function () {
      return '1'
    },
    initChat: function () {
      var self = this
      self.fetchConversations().then(function () {
        self.createConversation()
      })
    },
    fetchConversations: function () {
      var self = this
      return fetch('/chatbot/conversations?userId=' + self.getUserId(), {
        headers: { 'Authorization': getToken() }
      }).then(function (res) {
        return res.json()
      }).then(function (data) {
        self.conversations = data.conversations || []
      }).catch(function () {})
    },
    createConversation: function () {
      var self = this
      fetch('/chatbot/conversation?userId=' + self.getUserId(), {
        method: 'POST',
        headers: { 'Authorization': getToken() }
      }).then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data }
        })
      }).then(function (result) {
        if (result.ok) {
          self.conversations.unshift({
            conversationId: result.data.conversationId,
            createdAt: result.data.createdAt,
            messageCount: 0,
            active: true
          })
          self.currentConversationId = result.data.conversationId
          self.$refs.chatViewRef.clearMessages()
          self.showSidebar = false
        }
      }).catch(function () {
        // silently fail
      })
    },
    selectConversation: function (conversationId) {
      var self = this
      self.currentConversationId = conversationId
      fetch('/chatbot/conversation/' + conversationId + '/messages?userId=' + self.getUserId(), {
        headers: { 'Authorization': getToken() }
      }).then(function (res) {
        return res.json()
      }).then(function (data) {
        var msgs = data.map(function (m) {
          return {
            id: m.requestId + '_' + m.role,
            role: m.role,
            content: m.content,
            timestamp: new Date(m.createdAt).getTime()
          }
        })
        self.$refs.chatViewRef.loadMessages(msgs)
        self.showSidebar = false
      }).catch(function () {
        // silently fail
      })
    },
    formatConvName: function (dateStr) {
      if (!dateStr) return '新对话'
      var d = new Date(dateStr)
      var pad = function (n) { return String(n).padStart(2, '0') }
      return pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
    },
    formatRelativeTime: function (dateStr) {
      if (!dateStr) return ''
      var d = new Date(dateStr)
      var now = new Date()
      var diffMs = now.getTime() - d.getTime()
      var diffMin = Math.floor(diffMs / 60000)
      if (diffMin < 1) return '刚刚'
      if (diffMin < 60) return diffMin + '分钟前'
      var diffHour = Math.floor(diffMin / 60)
      if (diffHour < 24) return diffHour + '小时前'
      return (d.getMonth() + 1) + '/' + d.getDate()
    }
  }
}
</script>

<style scoped>
.chat-panel {
  height: 80%;
  background: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.15);
  position: fixed;
  right: 20px;
  bottom: 50px;
  border-radius: 16px;
  overflow: hidden;
}

.drag-handle {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 12px;
  cursor: col-resize;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.drag-handle::after {
  content: '';
  width: 4px;
  height: 32px;
  background-image: radial-gradient(circle, #b0b8c8 1.5px, transparent 1.5px);
  background-size: 4px 10px;
  opacity: 0.6;
  transition: opacity 0.15s, background-image 0.15s;
}

.drag-handle:hover::after,
.drag-handle:active::after {
  background-image: radial-gradient(circle, #667eea 1.5px, transparent 1.5px);
  opacity: 1;
}

.panel-header {
  height: 52px;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  flex-shrink: 0;
  border-bottom: 1px solid #e5e7eb;
}

.header-icon {
  font-size: 18px;
  color: #667eea;
}

.panel-title {
  flex: 1;
  font-weight: 600;
  font-size: 16px;
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

.chat-area {
  flex: 1;
  overflow: hidden;
}

.sidebar-enter-active { transition: width 0.2s ease, opacity 0.2s ease; }
.sidebar-leave-active { transition: width 0.15s ease, opacity 0.15s ease; }
.sidebar-enter, .sidebar-leave-to { width: 0; opacity: 0; overflow: hidden; }
</style>
