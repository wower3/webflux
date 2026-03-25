<template>
  <div class="markdown-renderer" v-html="renderedHtml"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

const props = defineProps<{
  content: string
}>()

// 配置 markdown-it
const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  highlight: (str: string, lang: string): string => {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang }).value}</code></pre>`
      } catch (__) {}
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
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
  word-wrap: break-word;
}

.markdown-renderer :deep(h1),
.markdown-renderer :deep(h2),
.markdown-renderer :deep(h3) {
  margin-top: 0;
  margin-bottom: 8px;
  color: #1f2937;
}

.markdown-renderer :deep(p) {
  margin-bottom: 8px;
}

.markdown-renderer :deep(code) {
  background: #e5e7eb;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
}

.markdown-renderer :deep(pre) {
  background: #1f2937;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
}

.markdown-renderer :deep(pre code) {
  background: transparent;
  padding: 0;
}

.markdown-renderer :deep(a) {
  color: #3b82f6;
  text-decoration: underline;
}

.markdown-renderer :deep(ul),
.markdown-renderer :deep(ol) {
  margin-left: 20px;
  margin-bottom: 8px;
}

.markdown-renderer :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
}

.markdown-renderer :deep(th),
.markdown-renderer :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 8px;
}

.markdown-renderer :deep(th) {
  background: #f3f4f6;
}
</style>
