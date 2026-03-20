<template>
  <div class="chat-sidebar-container">
    <!-- 展开时的侧边栏 -->
    <transition name="slide">
      <div v-show="isOpen" class="chat-sidebar" :style="{ width: sidebarWidth + 'px' }">
        <!-- 拖拽手柄 -->
        <div
          class="resize-handle"
          @mousedown="startResize"
          :class="{ 'is-resizing': isResizing }"
        ></div>
        <div class="sidebar-header">
          <div class="header-title">
            <i class="fa fa-comments"></i>
            <span>AI 数据分析助手</span>
          </div>
          <button class="close-btn" @click="close">
            <i class="fa fa-times"></i>
          </button>
        </div>
        <div class="sidebar-content">
          <ChatView />
        </div>
      </div>
    </transition>

    <!-- 收起时的悬浮按钮 -->
    <transition name="fade">
      <button v-show="!isOpen" class="chat-toggle-btn" @click="open">
        <i class="fa fa-comments"></i>
        <span class="btn-text">AI助手</span>
        <span class="btn-badge">3</span>
      </button>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ChatView from '@/views/ChatView.vue'

const isOpen = ref(false)
const sidebarWidth = ref(420)
const isResizing = ref(false)
let resizeTimer: number | null = null

const minWidth = 320
const maxWidth = 800

const open = () => {
  isOpen.value = true
}

const close = () => {
  isOpen.value = false
}

const triggerChartResize = () => {
  // 使用 requestAnimationFrame 优化性能
  if (resizeTimer !== null) {
    cancelAnimationFrame(resizeTimer)
  }
  resizeTimer = requestAnimationFrame(() => {
    window.dispatchEvent(new Event('resize'))
    resizeTimer = null
  })
}

const startResize = (e: MouseEvent) => {
  isResizing.value = true
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}

const onResize = (e: MouseEvent) => {
  if (!isResizing.value) return
  const newWidth = window.innerWidth - e.clientX
  sidebarWidth.value = Math.max(minWidth, Math.min(maxWidth, newWidth))
  triggerChartResize()
}

const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
  // 最终触发一次确保大小正确
  triggerChartResize()
}

defineExpose({
  open,
  close
})
</script>

<style scoped>
@import url("https://cdn.bootcdn.net/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css");

.chat-sidebar-container {
  position: fixed;
  top: 0;
  right: 0;
  height: 100vh;
  z-index: 100;
  pointer-events: none;
}

/* 侧边栏 */
.chat-sidebar {
  position: absolute;
  top: 0;
  right: 0;
  height: 100vh;
  background: #fff;
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  pointer-events: auto;
}

/* 拖拽手柄 */
.resize-handle {
  position: absolute;
  left: -4px;
  top: 0;
  bottom: 0;
  width: 8px;
  cursor: ew-resize;
  background: transparent;
  transition: background 0.2s;
  z-index: 1;
}

.resize-handle:hover {
  background: rgba(102, 126, 234, 0.3);
}

.resize-handle.is-resizing {
  background: #667eea;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.sidebar-content {
  flex: 1;
  overflow: hidden;
}

/* 悬浮按钮 */
.chat-toggle-btn {
  position: absolute;
  top: 80px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  pointer-events: auto;
  transition: all 0.3s;
}

.chat-toggle-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.chat-toggle-btn i {
  font-size: 18px;
}

.btn-text {
  display: none;
}

@media (min-width: 768px) {
  .btn-text {
    display: inline;
  }
}

.btn-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 20px;
  height: 20px;
  background: #ef4444;
  border-radius: 50%;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
}

/* 动画 */
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 覆盖 ChatView 中的样式，适应侧边栏 */
.sidebar-content :deep(.chat-container) {
  height: 100%;
}

.sidebar-content :deep(.chat-messages) {
  padding: 16px;
}

.sidebar-content :deep(.input-container) {
  padding: 12px 16px;
}
</style>
