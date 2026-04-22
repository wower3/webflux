<template>
  <!-- 浮窗按钮 -->
  <div class="chat-fab" @click="open">
    <i class="fa fa-comments"></i>
  </div>

  <!-- 面板遮罩 -->
  <Transition name="fade">
    <div v-if="isOpen" class="chat-overlay" @click.self="close">
      <Transition name="slide">
        <ChatPanel v-if="isOpen" @close="close" />
      </Transition>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ChatPanel from './ChatPanel.vue'

const isOpen = ref(false)

const open = () => {
  isOpen.value = true
}

const close = () => {
  isOpen.value = false
}
</script>

<style scoped>
.chat-fab {
  position: fixed;
  right: 28px;
  bottom: 28px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  z-index: 9998;
  transition: transform 0.2s, box-shadow 0.2s;
}

.chat-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.chat-fab i {
  font-size: 22px;
}

.chat-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 9999;
  display: flex;
  justify-content: flex-end;
}

/* 遮罩淡入 */
.fade-enter-active { transition: opacity 0.25s ease; }
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 面板滑入 */
.slide-enter-active { transition: transform 0.3s ease; }
.slide-leave-active { transition: transform 0.2s ease; }
.slide-enter-from, .slide-leave-to { transform: translateX(100%); }
</style>
