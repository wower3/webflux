<script setup lang="ts">
import { ref } from 'vue'
import HomeView from './views/HomeView.vue'
import ChatSidebar from './components/ChatSidebar.vue'

const chatSidebarRef = ref<InstanceType<typeof ChatSidebar>>()

const openChat = () => {
  chatSidebarRef.value?.open()
}
</script>

<template>
  <HomeView @open-chat="openChat" />
  <ChatSidebar ref="chatSidebarRef" />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body, html {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f8fafc;
  color: #24292f;
}

#app {
  height: 100%;
}
</style>
