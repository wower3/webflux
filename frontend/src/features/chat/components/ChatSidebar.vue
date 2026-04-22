<template>
  <div class="chat-area">
    <ChatView ref="chatViewRef" :conversation-id="conversationId" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ChatView from './ChatView.vue'

defineProps<{
  conversationId?: string | null
}>()

const chatViewRef = ref<InstanceType<typeof ChatView>>()

defineExpose({
  loadMessages: (msgs: any[]) => chatViewRef.value?.loadMessages(msgs),
  clearMessages: () => chatViewRef.value?.clearMessages()
})
</script>

<style scoped>
.chat-area {
  height: 100%;
}
</style>
