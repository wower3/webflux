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

const escapeHtml = (s: string) => {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
}

const md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value
      } catch {
        // fall through to escaped default
      }
    }
    return escapeHtml(str) || ''
  }
})

// 所有链接在新标签页打开
const defaultRender = md.renderer.rules.link_open || function (tokens: any, idx: any, options: any, _env: any, self: any) {
  return self.renderToken(tokens, idx, options)
}
md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultRender(tokens, idx, options, env, self)
}

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
