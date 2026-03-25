<template>
  <div class="chat-input-container">
    <div class="input-wrapper">
      <textarea
        v-model="inputMessage"
        @keydown="handleKeyDown"
        placeholder="输入消息... (Shift+Enter 换行)"
        rows="1"
        ref="textareaRef"
        class="chat-textarea"
      ></textarea>
      <button @click="handleSend" :disabled="!inputMessage.trim() || loading" class="send-btn">
        <i class="fa fa-paper-plane"></i>
      </button>
    </div>
    <div class="input-options">
      <label class="option-label">
        <input type="checkbox" v-model="testMode" :disabled="loading" />
        测试模式
      </label>
      <label class="option-label">
        <input type="checkbox" v-model="echoMode" :disabled="loading" />
        回显模式
      </label>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'

const inputMessage = ref('')
const testMode = ref(false)
const echoMode = ref(false)
const loading = defineModel<boolean>('loading', { default: false })
const textareaRef = ref<HTMLTextAreaElement>()

const emit = defineEmits<{
  send: [message: string, testMode: boolean, echoMode: boolean]
}>()

const handleSend = () => {
  const message = inputMessage.value.trim()
  if (!message || loading.value) return

  emit('send', message, testMode.value, echoMode.value)
  inputMessage.value = ''
  testMode.value = false
  echoMode.value = false
}

const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 自动调整高度
watch(inputMessage, () => {
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
      textareaRef.value.style.height = textareaRef.value.scrollHeight + 'px'
    }
  })
})
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css");

.chat-input-container {
  border-top: 1px solid #e5e7eb;
  background: white;
  padding: 16px;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-textarea {
  flex: 1;
  border: 1px solid #d1d5db;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 15px;
  font-family: inherit;
  resize: none;
  min-height: 48px;
  max-height: 200px;
  outline: none;
  transition: border-color 0.2s;
}

.chat-textarea:focus {
  border-color: #667eea;
}

.send-btn {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: opacity 0.2s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-options {
  display: flex;
  gap: 20px;
  margin-top: 12px;
}

.option-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
}

.option-label input[type="checkbox"] {
  cursor: pointer;
}

.option-label input[type="checkbox"]:disabled {
  cursor: not-allowed;
}
</style>
