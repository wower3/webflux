import { ref, watch, nextTick, triggerRef, type Ref } from 'vue'
import { sendChatStream } from '../api/chat'
import type { Message, EmbedData, CardData } from '../types'

export function useChat(conversationId: Ref<string | null>) {
  const messages = ref<Message[]>([])
  const isLoading = ref(false)
  let currentStreamCloser: (() => void) | null = null
  let messageIdCounter = 0

  const messagesRef = ref<HTMLElement>()

  const scrollToBottom = () => {
    nextTick(() => {
      messagesRef.value?.scrollTo({
        top: messagesRef.value.scrollHeight,
        behavior: 'smooth'
      })
    })
  }

  function parseEmbeds(content: string): { cleanContent: string, embeds: EmbedData[] } {
    const embeds: EmbedData[] = []
    const processedIds = new Set<string>()
    const embedStart = '{"type":"'
    let startPos = content.indexOf(embedStart)

    while (startPos !== -1) {
      let braceDepth = 0
      let bracketDepth = 0
      let pos = startPos

      while (pos < content.length) {
        if (content[pos] === '{') braceDepth++
        else if (content[pos] === '}') braceDepth--
        else if (content[pos] === '[') bracketDepth++
        else if (content[pos] === ']') bracketDepth--

        if (braceDepth === 0 && bracketDepth === 0 && pos > startPos) {
          const jsonStr = content.slice(startPos, pos + 1)

          try {
            const data = JSON.parse(jsonStr)

            if (data.type === 'chart' && data.subtype && !processedIds.has(data.chartId)) {
              processedIds.add(data.chartId)
              embeds.push({
                id: data.chartId,
                type: 'chart',
                data: {
                  subtype: data.subtype,
                  title: data.title || '',
                  chartData: data.data || {}
                }
              })
              const placeholder = `[CHART:${data.chartId}]`
              content = content.substring(0, startPos) + placeholder + content.substring(pos + 1)
              pos = startPos + placeholder.length - 1
            } else if (data.type === 'card' && data.cardId && !processedIds.has(data.cardId)) {
              processedIds.add(data.cardId)
              embeds.push({
                id: data.cardId,
                type: 'card',
                data: data
              })
              const placeholder = `[CARD:${data.cardId}]`
              content = content.substring(0, startPos) + placeholder + content.substring(pos + 1)
              pos = startPos + placeholder.length - 1
            }
          } catch (e) {
            // JSON 不完整，等待更多数据
          }
          break
        }
        pos++
      }

      startPos = content.indexOf(embedStart, pos + 1)
    }

    return { cleanContent: content, embeds }
  }

  const handleRemoveCard = (cardId: string) => {
    const assistantMsg = messages.value.filter(m => m.role === 'assistant').pop()
    if (!assistantMsg || !assistantMsg.embeds) return

    const index = assistantMsg.embeds.findIndex(e => e.id === cardId)
    if (index !== -1) {
      assistantMsg.embeds.splice(index, 1)
      assistantMsg.content = assistantMsg.content.replace(`[CARD:${cardId}]`, '')
      triggerRef(messages)
    }
  }

  const handleUpdateCard = (card: CardData) => {
    const assistantMsg = messages.value.filter(m => m.role === 'assistant').pop()
    if (!assistantMsg || !assistantMsg.embeds) return

    const index = assistantMsg.embeds.findIndex(e => e.id === card.cardId)
    if (index !== -1) {
      assistantMsg.embeds[index].data = card
      triggerRef(messages)
    }
  }

  const loadMessages = (msgs: Message[]) => {
    if (currentStreamCloser) {
      currentStreamCloser()
      currentStreamCloser = null
    }
    messages.value = msgs
    isLoading.value = false
    messageIdCounter = msgs.length
    nextTick(scrollToBottom)
  }

  const clearMessages = () => {
    if (currentStreamCloser) {
      currentStreamCloser()
      currentStreamCloser = null
    }
    messages.value = []
    isLoading.value = false
    messageIdCounter = 0
  }

  const handleSend = async (userMessage: string) => {
    if (isLoading.value) return

    if (currentStreamCloser) {
      currentStreamCloser()
      currentStreamCloser = null
    }

    const userMsg: Message = {
      id: `msg_${messageIdCounter++}`,
      role: 'user',
      content: userMessage,
      timestamp: Date.now()
    }
    messages.value.push(userMsg)
    triggerRef(messages)

    const assistantMsg: Message = {
      id: `msg_${messageIdCounter++}`,
      role: 'assistant',
      content: '',
      embeds: [],
      timestamp: Date.now(),
      isStreaming: true
    }
    messages.value.push(assistantMsg)
    triggerRef(messages)
    isLoading.value = true

    let rawContent = ''
    const collectedEmbeds = new Set<string>()

    const contentCallback = (content: string) => {
      rawContent += content
      const { cleanContent, embeds } = parseEmbeds(rawContent)
      assistantMsg.content = cleanContent

      for (const embed of embeds) {
        if (!collectedEmbeds.has(embed.id)) {
          collectedEmbeds.add(embed.id)
          if (!assistantMsg.embeds) assistantMsg.embeds = []
          assistantMsg.embeds.push(embed)
        }
      }
      triggerRef(messages)
    }

    const cardCallback = (card: Record<string, unknown>) => {
      const cardId = String(card.cardId ?? '')
      if (!collectedEmbeds.has(cardId)) {
        collectedEmbeds.add(cardId)
        if (!assistantMsg.embeds) assistantMsg.embeds = []
        assistantMsg.embeds.push({
          id: cardId,
          type: 'card' as const,
          data: card as unknown as CardData
        })
      }
      triggerRef(messages)
    }

    const endCallback = () => {
      assistantMsg.isStreaming = false
      isLoading.value = false
      currentStreamCloser = null
      triggerRef(messages)
    }

    const errorCallback = (_error: Error) => {
      assistantMsg.content += '\n[请求失败，请重试]'
      assistantMsg.isStreaming = false
      isLoading.value = false
      currentStreamCloser = null
      triggerRef(messages)
    }

    currentStreamCloser = sendChatStream(
      userMessage,
      {
        onContent: contentCallback,
        onChart: () => {},
        onCard: cardCallback,
        onEnd: endCallback,
        onError: errorCallback
      },
      conversationId.value || undefined
    )
  }

  watch(messages, scrollToBottom, { deep: true })

  return {
    messages,
    isLoading,
    messagesRef,
    handleSend,
    handleRemoveCard,
    handleUpdateCard,
    loadMessages,
    clearMessages
  }
}
