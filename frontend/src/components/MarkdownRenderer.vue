<template>
  <div class="markdown-renderer">
    <!-- 流式时显示纯文本，结束时显示解析后的HTML -->
    <div v-if="isStreaming" class="raw-text">{{ content }}</div>
    <div v-else v-html="renderedHtml"></div>
  </div>
</template>

<script setup lang="ts">
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import { computed } from 'vue'

const props = defineProps<{
  content: string
  isStreaming?: boolean
}>()

// 配置 markdown-it
const md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value
      } catch (__) {}
    }
    return ''
  }
})

// 只在非流式状态时解析
const renderedHtml = computed(() => {
  if (props.isStreaming || !props.content) return ''
  return md.render(props.content)
})
</script>

<style scoped>
.markdown-renderer {
  line-height: 1.6;
  font-size: 15px;
  word-wrap: break-word;
}

.raw-text {
  white-space: pre-wrap;
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
