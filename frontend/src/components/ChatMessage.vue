<template>
  <div class="message-row">
    <div class="message-content" :class="{ 'user-message': role === 'user', 'ai-message': role === 'assistant' }">
      <div class="avatar" v-if="role === 'assistant'">
        <i class="fa fa-cube"></i>
      </div>
      <div class="message-bubble">
        <template v-for="(segment, index) in contentSegments" :key="segment.type === 'chart' ? segment.data?.id || `chart-${index}` : `text-${index}`">
          <MarkdownRenderer v-if="segment.type === 'text'" :content="segment.content || ''" />
          <ChartRenderer v-else-if="segment.type === 'chart' && segment.data" :chart="segment.data" />
        </template>
        <span v-if="isStreaming" class="cursor">|</span>
      </div>
      <div class="avatar" v-if="role === 'user'">
        <i class="fa fa-user"></i>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownRenderer from './MarkdownRenderer.vue'
import ChartRenderer from './ChartRenderer.vue'
import type { EmbedData, ChartData } from '@/types'

const props = defineProps<{
  role: 'user' | 'assistant'
  content: string
  charts?: any[]
  embeds?: EmbedData[]
  isStreaming?: boolean
}>()

interface ContentSegment {
  type: 'text' | 'chart'
  content?: string
  data?: ChartData
}

const PLACEHOLDER_PATTERN = /\[([A-Z]+):([^\]]+)\]/g

const contentSegments = computed((): ContentSegment[] => {
  if (props.role === 'user') {
    return [{ type: 'text', content: props.content }]
  }

  const segments: ContentSegment[] = []
  const embeds = props.embeds || []

  let lastIndex = 0
  let match: RegExpExecArray | null

  while ((match = PLACEHOLDER_PATTERN.exec(props.content)) !== null) {
    const fullMatch = match[0]
    const id = match[2]

    if (match.index > lastIndex) {
      segments.push({
        type: 'text',
        content: props.content.slice(lastIndex, match.index)
      })
    }

    const embed = embeds.find((e: EmbedData) => e.id === id)
    if (embed && embed.type === 'chart') {
      segments.push({
        type: 'chart',
        data: {
          id: embed.id,
          type: embed.data.subtype,
          title: embed.data.title,
          data: embed.data.chartData
        }
      })
    }

    lastIndex = match.index + fullMatch.length
  }

  if (lastIndex < props.content.length) {
    segments.push({
      type: 'text',
      content: props.content.slice(lastIndex)
    })
  }

  return segments.length > 0 ? segments : [{ type: 'text', content: props.content }]
})
</script>

<style scoped>
@import url("https://cdn.bootcdn.net/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css");

.message-row {
  padding: 16px 0;
}

.message-content {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 0 16px;
}

/* 用户消息 - 右对齐 */
.user-message {
  flex-direction: row-reverse;
}

/* AI 消息 - 左对齐 */
.ai-message {
  flex-direction: row;
}

/* 头像 */
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  flex-shrink: 0;
}

.user-message .avatar {
  background: linear-gradient(135deg, #10a37f 0%, #0d8a6a 100%);
}

.ai-message .avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 气泡框 */
.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.6;
  word-wrap: break-word;
  overflow-x: auto;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, #10a37f 0%, #0d8a6a 100%);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-message .message-bubble {
  background: #f3f4f6;
  color: #1f2937;
  border-bottom-left-radius: 4px;
}

/* 用户消息中的链接颜色 */
.user-message .message-bubble :deep(a) {
  color: #fff;
  text-decoration: underline;
}

/* AI 消息中的 Markdown 样式 */
.ai-message .message-bubble :deep(h1),
.ai-message .message-bubble :deep(h2),
.ai-message .message-bubble :deep(h3) {
  margin-top: 0;
  margin-bottom: 8px;
  color: #1f2937;
}

.ai-message .message-bubble :deep(p) {
  margin-bottom: 8px;
}

.ai-message .message-bubble :deep(code) {
  background: #e5e7eb;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
}

.ai-message .message-bubble :deep(pre) {
  background: #1f2937;
  color: #f3f4f6;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
}

.cursor {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
</style>
