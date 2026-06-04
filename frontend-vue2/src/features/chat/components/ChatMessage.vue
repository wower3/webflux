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
        <div v-if="showAdoption" class="adoption-actions">
          <button
            class="adoption-btn"
            :class="{ active: localAdoptionStatus === '1', dimmed: localAdoptionStatus === '0' }"
            :disabled="localAdoptionStatus !== '2'"
            @click="handleAdopt('1')"
            title="采纳"
          >
            <i class="fa fa-thumbs-up"></i>
          </button>
          <button
            class="adoption-btn"
            :class="{ active: localAdoptionStatus === '0', dimmed: localAdoptionStatus === '1' }"
            :disabled="localAdoptionStatus !== '2'"
            @click="handleAdopt('0')"
            title="未采纳"
          >
            <i class="fa fa-thumbs-down"></i>
          </button>
        </div>
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
    requestId: { type: String, default: null },
    adoptionStatus: { type: String, default: '2' },
    isSuccess: { type: String, default: null }
  },
  data: function () {
    return {
      localAdoptionStatus: this.adoptionStatus || '2'
    }
  },
  computed: {
    showAdoption: function () {
      return this.role === 'assistant' && !this.isStreaming && this.requestId && this.isSuccess === '1'
    },
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
    },
    handleAdopt: function (status) {
      if (this.localAdoptionStatus !== '2') return
      var prev = this.localAdoptionStatus
      this.localAdoptionStatus = status
      var self = this
      fetch('/chatbot/chat/adoption', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ requestId: self.requestId, adoptionStatus: status })
      }).catch(function () {
        self.localAdoptionStatus = prev
      })
    }
  }
}
</script>

<style scoped>
.message-row {
  padding: 14px 0;
}

.message-row + .message-row {
  border-top: 1px solid #f3f4f6;
}

.message-content {
  max-width: 100%;
  margin: 0 auto;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 0 24px;
}

.user-message {
  flex-direction: row-reverse;
}

.ai-message {
  flex-direction: row;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 15px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-message .avatar {
  background: linear-gradient(135deg, #10a37f 0%, #0d8a6a 100%);
}

.ai-message .avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.message-bubble {
  max-width: 75%;
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
  box-shadow: 0 1px 4px rgba(16, 163, 127, 0.2);
}

.ai-message .message-bubble {
  background: #f3f4f6;
  color: #1f2937;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
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

.adoption-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e5e7eb;
}

.adoption-btn {
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 4px 10px;
  cursor: pointer;
  font-size: 14px;
  color: #6b7280;
  transition: all 0.2s;
}

.adoption-btn:hover:not(:disabled) {
  border-color: #9ca3af;
  color: #374151;
  background: #f9fafb;
}

.adoption-btn.active {
  color: #fff;
  border-color: transparent;
  cursor: default;
}

.adoption-btn.active[title="采纳"] {
  background: #10b981;
}

.adoption-btn.active[title="未采纳"] {
  background: #ef4444;
}

.adoption-btn.dimmed {
  opacity: 0.3;
  cursor: default;
}

.adoption-btn:disabled {
  cursor: default;
}
</style>
