<template>
  <div class="markdown-renderer" v-html="renderedHtml"></div>
</template>

<script setup lang="ts">
import { marked } from 'marked'
import { computed } from 'vue'

const props = defineProps<{
  content: string
}>()

// 配置marked
marked.setOptions({
  breaks: true,
  gfm: true
})

const renderedHtml = computed(() => {
  if (!props.content) return ''
  return marked(props.content)
})
</script>

<style scoped>
.markdown-renderer {
  line-height: 1.6;
  font-size: 15px;
  word-wrap: break-word;
}

.markdown-renderer :deep(p) {
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-renderer :deep(strong) {
  font-weight: 600;
}

.markdown-renderer :deep(code) {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

.markdown-renderer :deep(pre) {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-renderer :deep(pre code) {
  background: transparent;
  padding: 0;
}
</style>
