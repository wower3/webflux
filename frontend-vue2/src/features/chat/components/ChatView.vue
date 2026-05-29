<template>
  <div class="chat-container">
    <div class="chat-messages" ref="messagesRef">
      <ChatMessage
        v-for="msg in chatState.state.messages"
        :key="msg.id"
        :role="msg.role"
        :content="msg.content"
        :embeds="msg.embeds"
        :is-streaming="msg.isStreaming"
        :request-id="msg.requestId"
        :adoption-status="msg.adoptionStatus"
        :is-success="msg.isSuccess"
        @remove-card="handleRemoveCard"
        @update-card="handleUpdateCard"
        @confirm-card="handleCardConfirm"
      />
    </div>
    <GuidedPrompts />
    <ChatInput @send="handleSend" :loading.sync="chatState.state.isLoading" />
  </div>
</template>

<script>
import ChatMessage from './ChatMessage.vue'
import ChatInput from './ChatInput.vue'
import GuidedPrompts from './GuidedPrompts.vue'
import { createChatState } from '../composables/chatState'

export default {
  name: 'ChatView',
  components: { ChatMessage, ChatInput, GuidedPrompts },
  props: {
    conversationId: { type: String, default: null }
  },
  data: function () {
    var chatState = createChatState(() => this.conversationId || null)
    return {
      chatState: chatState
    }
  },
  mounted: function () {
    this.chatState.setMessagesRef(this.$refs.messagesRef)
  },
  watch: {
    'chatState.state.messages': {
      handler: function () {
        this.chatState.scrollToBottom()
      },
      deep: true
    }
  },
  methods: {
    handleSend: function (text) {
      this.chatState.handleSend(text)
    },
    handleRemoveCard: function (cardId) {
      this.chatState.handleRemoveCard(cardId)
    },
    handleUpdateCard: function (card) {
      this.chatState.handleUpdateCard(card)
    },
    handleCardConfirm: function (payload) {
      this.chatState.handleCardConfirm(payload)
    },
    loadMessages: function (msgs) {
      this.chatState.loadMessages(msgs)
    },
    clearMessages: function () {
      this.chatState.clearMessages()
    },
    sendMessage: function (text) {
      this.chatState.handleSend(text)
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}
</style>
