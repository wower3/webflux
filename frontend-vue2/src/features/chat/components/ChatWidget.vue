<template>
  <div>
    <!-- 浮窗按钮 -->
    <div class="chat-fab" @click="open">
      <i class="fa fa-comments"></i>
    </div>

    <!-- 面板遮罩 -->
    <transition name="fade">
      <div v-if="isOpen" class="chat-overlay" @click.self="close">
        <transition name="slide">
          <ChatPanel v-if="isOpen" @close="close" />
        </transition>
      </div>
    </transition>
  </div>
</template>

<script>
import ChatPanel from './ChatPanel.vue'

export default {
  name: 'ChatWidget',
  components: { ChatPanel },
  data: function () {
    return {
      isOpen: false
    }
  },
  methods: {
    open: function () {
      this.isOpen = true
    },
    close: function () {
      this.isOpen = false
    }
  }
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
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 9999;
  display: flex;
  justify-content: flex-end;
}

.fade-enter-active { transition: opacity 0.25s ease; }
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter, .fade-leave-to { opacity: 0; }

.slide-enter-active { transition: transform 0.3s ease; }
.slide-leave-active { transition: transform 0.2s ease; }
.slide-enter, .slide-leave-to { transform: translateX(100%); }
</style>
