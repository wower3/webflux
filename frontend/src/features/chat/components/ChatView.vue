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
        @confirm-card="handleCardConfirm"
      />
    </div>
    <GuidedPrompts />
    <ChatInput @send="handleSend" v-model:loading="isLoading" />
  </div>
</template>

<script setup lang="ts">
import { toRef } from 'vue'
import ChatMessage from './ChatMessage.vue'
import ChatInput from './ChatInput.vue'
import GuidedPrompts from './GuidedPrompts.vue'
import { useChat } from '../composables/useChat'

const props = defineProps<{
  conversationId?: string | null
}>()

const { messages, isLoading, messagesRef, handleSend, handleRemoveCard, handleUpdateCard, handleCardConfirm, loadMessages, clearMessages } = useChat(toRef(() => props.conversationId ?? null))

const sendMessage = (text: string) => {
  handleSend(text)
}

defineExpose({ loadMessages, clearMessages, sendMessage })
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
