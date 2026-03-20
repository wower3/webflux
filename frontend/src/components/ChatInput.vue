<template>
  <div class="input-container">
    <div class="input-box">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="1"
        placeholder="给大模型发送消息..."
        @keydown.enter.exact="handleSend"
        :disabled="loading"
        resize="none"
      />
      <el-button
        type="success"
        circle
        @click="handleSend"
        :loading="loading"
        :disabled="!inputText.trim()"
        class="send-btn"
      >
        <i class="fa fa-paper-plane" style="color: white;"></i>
      </el-button>
    </div>
    <div class="test-mode-toggle">
      <el-checkbox v-model="testMode" label="测试模式 (从文件读取)" size="small" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits<{
  send: [message: string, testMode: boolean]
}>()

const inputText = ref('')
const loading = defineModel<boolean>('loading', { default: false })
const testMode = ref(false)

const handleSend = () => {
  const message = inputText.value.trim()
  if (!message || loading.value) return

  emit('send', message, testMode.value)
  inputText.value = ''
}
</script>

<style scoped>
@import url("https://cdn.bootcdn.net/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css");

.input-container {
  border-top: 1px solid #e5e7eb;
  background-color: #ffffff;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.input-box {
  max-width: 800px;
  width: 100%;
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 10px 16px;
  background: #fff;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.05);
}

.input-box :deep(.el-textarea) {
  flex: 1;
}

.input-box :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  resize: none;
  padding: 0;
  font-size: 15px;
  font-family: inherit;
}

.send-btn {
  margin-left: 10px;
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  min-height: 32px;
}

.test-mode-toggle {
  max-width: 800px;
  width: 100%;
  margin-top: 8px;
  padding-left: 16px;
  font-size: 12px;
}
</style>
