<template>
  <div class="markdown-renderer" v-html="renderedHtml"></div>
</template>

<script setup lang="ts">
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import { computed } from 'vue'

const props = defineProps<{
  content: string
}>()

// 配置 markdown-it
const md = new MarkdownIt({
  html: false,         // 禁用 HTML 标签
  breaks: true,        // 转换 \n 为 <br>
  linkify: true,       // 自动转换 URL 为链接
  typographer: true,   // 启用引号美化
  highlight: function (str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value
      } catch (__) {}
    }
    return ''
  }
})

const renderedHtml = computed(() => {
  if (!props.content) return ''
  return md.render(props.content)
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
