<template>
  <div class="host-app">
    <header class="host-header">
      <h1>业务管理系统</h1>
    </header>
    <main class="host-main">
      <p class="host-hint">这是模拟的宿主系统页面，右下角浮窗为嵌入的 AI 助手</p>
    </main>
    <ChatWidget />
  </div>
</template>

<script>
import ChatWidget from '@/features/chat/components/ChatWidget.vue'

export default {
  name: 'App',
  components: { ChatWidget }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body, html {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f0f2f5;
  color: #24292f;
}

#app {
  height: 100%;
}
</style>

<style scoped>
.host-app {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.host-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.host-header h1 {
  font-size: 18px;
  color: #1f2937;
}

.host-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.host-hint {
  color: #9ca3af;
  font-size: 14px;
}
</style>
