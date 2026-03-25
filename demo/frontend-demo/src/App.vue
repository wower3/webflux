<template>
  <div id="app">
    <!-- 主页面 -->
    <div class="main-page">
      <header class="header">
        <div class="header-content">
          <div class="logo">
            <i class="fa fa-bar-chart"></i>
            <span>ChatChart Demo</span>
          </div>
        </div>
      </header>

      <main class="main-content">
        <div class="hero">
          <h1 class="hero-title">智能对话图表系统</h1>
          <p class="hero-subtitle">
            通过对话式交互，快速生成和编辑数据图表
          </p>
          <div class="hero-actions">
            <button class="btn-primary" @click="openChat">
              <i class="fa fa-comments"></i>
              开始对话
            </button>
          </div>
        </div>
      </main>
    </div>

    <!-- 聊天侧边栏 -->
    <ChatSidebar ref="chatSidebarRef" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ChatSidebar from './components/ChatSidebar.vue'

const chatSidebarRef = ref<InstanceType<typeof ChatSidebar>>()

const openChat = () => {
  chatSidebarRef.value?.open()
}
</script>

<style>
@import url("https://cdn.bootcdn.net/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css");

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.main-page {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
}

.header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.logo i {
  color: #667eea;
  font-size: 24px;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero {
  text-align: center;
  max-width: 600px;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
  margin-bottom: 16px;
}

.hero-subtitle {
  font-size: 18px;
  color: #6b7280;
  margin-bottom: 32px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  padding: 14px 32px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}
</style>
