<template>
  <div class="message-row">
    <div class="message-content" :class="{ 'user-message': role === 'user', 'ai-message': role === 'assistant' }">
      <div class="avatar" v-if="role === 'assistant'">
        <i class="fa fa-cube"></i>
      </div>
      <div class="message-bubble">
        <template v-for="(segment, index) in contentSegments">
          <MarkdownRenderer v-if="segment.type === 'text'" :key="'text-' + index" :content="segment.content || ''" :isStreaming="isStreaming" />
          <ChartRenderer v-else-if="segment.type === 'chart' && segment.data" :key="'chart-' + index" :chart="segment.data" />
          <CardRenderer v-else-if="segment.type === 'card' && segment.data" :key="'card-' + index" :card="segment.data" @remove="handleRemoveCard" @update="handleUpdateCard" @confirm="$emit('confirm-card', $event)" />
        </template>
        <span v-if="isStreaming" class="cursor">|</span>
      </div>
      <div class="avatar" v-if="role === 'user'">
        <i class="fa fa-user"></i>
      </div>
    </div>
  </div>
</template>

<script>
import MarkdownRenderer from './MarkdownRenderer.vue'
import ChartRenderer from './ChartRenderer.vue'
import CardRenderer from './CardRenderer.vue'

var PLACEHOLDER_PATTERN = /\[([A-Z]+):([^\]]+)\]/g

export default {
  name: 'ChatMessage',
  components: { MarkdownRenderer, ChartRenderer, CardRenderer },
  props: {
    role: { type: String, required: true },
    content: { type: String, required: true },
    embeds: { type: Array, default: function () { return [] } },
    isStreaming: { type: Boolean, default: false },
  },
  computed: {
    contentSegments: function () {
      var self = this

      if (self.role === 'user') {
        return [{ type: 'text', content: self.content }]
      }

      var segments = []
      var embeds = self.embeds || []
      var lastIndex = 0
      var match

      PLACEHOLDER_PATTERN.lastIndex = 0
      while ((match = PLACEHOLDER_PATTERN.exec(self.content)) !== null) {
        var fullMatch = match[0]
        var id = match[2]

        if (match.index > lastIndex) {
          segments.push({
            type: 'text',
            content: self.content.slice(lastIndex, match.index)
          })
        }

        var embed = null
        for (var i = 0; i < embeds.length; i++) {
          if (embeds[i].id === id) {
            embed = embeds[i]
            break
          }
        }

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
        } else if (embed && embed.type === 'card') {
          segments.push({
            type: 'card',
            data: embed.data
          })
        }

        lastIndex = match.index + fullMatch.length
      }

      if (lastIndex < self.content.length) {
        segments.push({
          type: 'text',
          content: self.content.slice(lastIndex)
        })
      }

      return segments.length > 0 ? segments : [{ type: 'text', content: self.content }]
    }
  },
  methods: {
    handleRemoveCard: function (cardId) {
      this.$emit('remove-card', cardId)
    },
    handleUpdateCard: function (card) {
      this.$emit('update-card', card)
    }
  }
}
</script>

<style scoped>
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

.user-message {
  flex-direction: row-reverse;
}

.ai-message {
  flex-direction: row;
}

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

.user-message .message-bubble ::v-deep a {
  color: #fff;
  text-decoration: underline;
}

.ai-message .message-bubble ::v-deep h1,
.ai-message .message-bubble ::v-deep h2,
.ai-message .message-bubble ::v-deep h3 {
  margin-top: 0;
  margin-bottom: 8px;
  color: #1f2937;
}

.ai-message .message-bubble ::v-deep p {
  margin-bottom: 8px;
}

.ai-message .message-bubble ::v-deep code {
  background: #e5e7eb;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
}

.ai-message .message-bubble ::v-deep pre {
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
