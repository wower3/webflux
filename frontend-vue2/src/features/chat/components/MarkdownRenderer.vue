<template>
  <div class="markdown-renderer">
    <div v-if="isStreaming" class="raw-text">{{ content }}</div>
    <div v-else v-html="renderedHtml"></div>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

var escapeHtml = function (s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
}

var md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value
      } catch (e) {
        // fall through
      }
    }
    return escapeHtml(str) || ''
  }
})

var defaultRender = md.renderer.rules.link_open || function (tokens, idx, options, _env, self) {
  return self.renderToken(tokens, idx, options)
}
md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultRender(tokens, idx, options, env, self)
}

export default {
  name: 'MarkdownRenderer',
  props: {
    content: { type: String, required: true },
    isStreaming: { type: Boolean, default: false }
  },
  computed: {
    renderedHtml: function () {
      if (this.isStreaming || !this.content) return ''
      return md.render(this.content)
    }
  }
}
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

.markdown-renderer ::v-deep p {
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-renderer ::v-deep strong {
  font-weight: 600;
}

.markdown-renderer ::v-deep code {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

.markdown-renderer ::v-deep pre {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-renderer ::v-deep pre code {
  background: transparent;
  padding: 0;
}
</style>
