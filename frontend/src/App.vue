<script setup lang="ts">
import ChatView from './views/ChatView.vue'
</script>

<template>
  <ChatView />
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
  background-color: #ffffff;
  color: #24292f;
}

#app {
  height: 100%;
}
</style>
