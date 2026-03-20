<template>
  <div class="message-row" :class="roleClass">
    <div class="message-content">
      <div class="avatar">
        <i v-if="role === 'user'" class="fa fa-user"></i>
        <i v-else class="fa fa-cube"></i>
      </div>
      <div class="message-body">
        <template v-for="(segment, index) in contentSegments" :key="segment.type === 'chart' ? segment.data?.id || `chart-${index}` : `text-${index}`">
          <MarkdownRenderer v-if="segment.type === 'text'" :content="segment.content || ''" />
          <ChartRenderer v-else-if="segment.type === 'chart' && segment.data" :chart="segment.data" />
        </template>
        <span v-if="isStreaming" class="cursor">|</span>
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

// 修复 CSS 类名映射：'assistant' -> 'ai'
const roleClass = computed(() => props.role === 'assistant' ? 'ai' : props.role)

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
  padding: 24px 0;
  border-bottom: 1px solid #e5e7eb;
}

.message-row.user {
  background-color: #ffffff;
}

.message-row.ai {
  background-color: #f9f9f9;
}

.message-content {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  gap: 20px;
  padding: 0 20px;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  flex-shrink: 0;
}

.user .avatar {
  background-color: #10a37f;
}

.ai .avatar {
  background-color: #667eea;
}

.message-body {
  flex: 1;
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
