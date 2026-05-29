import Vue from 'vue'
import { sendChatStream } from '../api/chat'

export function createChatState(getConversationId) {
  var state = Vue.observable({
    messages: [],
    isLoading: false
  })

  var currentStreamCloser = null
  var messageIdCounter = 0
  var messagesRef = null

  function setMessagesRef(el) {
    messagesRef = el
  }

  function scrollToBottom() {
    Vue.nextTick(function () {
      if (messagesRef) {
        messagesRef.scrollTo({
          top: messagesRef.scrollHeight,
          behavior: 'smooth'
        })
      }
    })
  }

  function parseEmbeds(content) {
    var embeds = []
    var processedIds = {}
    var embedPattern = /"type"\s*:\s*"(chart|card)"/g
    var match

    while ((match = embedPattern.exec(content)) !== null) {
      var openBrace = -1
      var depth = 0
      for (var i = match.index; i >= 0; i--) {
        if (content[i] === '}') depth++
        else if (content[i] === '{') { depth--; if (depth < 0) { openBrace = i; break } }
      }
      if (openBrace === -1) continue

      var closeBrace = -1
      depth = 0
      for (var i = openBrace; i < content.length; i++) {
        if (content[i] === '{') depth++
        else if (content[i] === '}') { depth--; if (depth === 0 && i > openBrace) { closeBrace = i; break } }
      }
      if (closeBrace === -1) continue

      var jsonStr = content.slice(openBrace, closeBrace + 1)

      try {
        var data = JSON.parse(jsonStr)

        if (data.type === 'chart' && data.subtype) {
          var chartId = data.chartId || (data.subtype + '_' + (data.title || 'untitled'))
          if (!processedIds[chartId]) {
            processedIds[chartId] = true
            embeds.push({
              id: chartId,
              type: 'chart',
              data: {
                subtype: data.subtype,
                title: data.title || '',
                chartData: data.data || {}
              }
            })
            var placeholder = '[CHART:' + chartId + ']'
            content = content.substring(0, openBrace) + placeholder + content.substring(closeBrace + 1)
            embedPattern.lastIndex = openBrace + placeholder.length
            continue
          }
        } else if (data.type === 'card' && data.cardId && !processedIds[data.cardId]) {
          processedIds[data.cardId] = true
          embeds.push({
            id: data.cardId,
            type: 'card',
            data: data
          })
          var placeholder = '[CARD:' + data.cardId + ']'
          content = content.substring(0, openBrace) + placeholder + content.substring(closeBrace + 1)
          embedPattern.lastIndex = openBrace + placeholder.length
          continue
        }
      } catch (e) {
        // JSON incomplete, wait for more data
      }

      embedPattern.lastIndex = closeBrace + 1
    }

    return { cleanContent: content, embeds: embeds }
  }

  function handleRemoveCard(cardId) {
    var assistantMsg = state.messages.filter(function (m) { return m.role === 'assistant' }).pop()
    if (!assistantMsg || !assistantMsg.embeds) return

    var index = assistantMsg.embeds.findIndex(function (e) { return e.id === cardId })
    if (index !== -1) {
      assistantMsg.embeds.splice(index, 1)
      assistantMsg.content = assistantMsg.content.replace('[CARD:' + cardId + ']', '')
    }
  }

  function handleUpdateCard(card) {
    var assistantMsg = state.messages.filter(function (m) { return m.role === 'assistant' }).pop()
    if (!assistantMsg || !assistantMsg.embeds) return

    var index = assistantMsg.embeds.findIndex(function (e) { return e.id === card.cardId })
    if (index !== -1) {
      assistantMsg.embeds[index].data = card
    }
  }

  function loadMessages(msgs) {
    if (currentStreamCloser) {
      currentStreamCloser()
      currentStreamCloser = null
    }
    state.messages = msgs.map(function (msg) {
      if (msg.role !== 'assistant') return msg
      var result = parseEmbeds(msg.content)
      return Object.assign({}, msg, {
        content: result.cleanContent,
        embeds: result.embeds.length > 0 ? result.embeds : undefined,
        requestId: msg.requestId || null,
        adoptionStatus: msg.adoptionStatus || '2',
        isSuccess: msg.isSuccess || null
      })
    })
    state.isLoading = false
    messageIdCounter = msgs.length
    Vue.nextTick(scrollToBottom)
  }

  function clearMessages() {
    if (currentStreamCloser) {
      currentStreamCloser()
      currentStreamCloser = null
    }
    state.messages = []
    state.isLoading = false
    messageIdCounter = 0
  }

  function handleSend(userMessage) {
    if (state.isLoading) return

    if (currentStreamCloser) {
      currentStreamCloser()
      currentStreamCloser = null
    }

    var userMsg = {
      id: 'msg_' + messageIdCounter++,
      role: 'user',
      content: userMessage,
      timestamp: Date.now()
    }
    state.messages.push(userMsg)

    var assistantMsg = {
      id: 'msg_' + messageIdCounter++,
      role: 'assistant',
      content: '',
      embeds: [],
      timestamp: Date.now(),
      isStreaming: true,
      requestId: null,
      adoptionStatus: '2',
      isSuccess: null
    }
    state.messages.push(assistantMsg)
    state.isLoading = true

    var rawContent = ''
    var collectedEmbeds = {}

    var contentCallback = function (content) {
      rawContent += content
      var result = parseEmbeds(rawContent)
      assistantMsg.content = result.cleanContent

      for (var i = 0; i < result.embeds.length; i++) {
        var embed = result.embeds[i]
        if (!collectedEmbeds[embed.id]) {
          collectedEmbeds[embed.id] = true
          if (!assistantMsg.embeds) assistantMsg.embeds = []
          assistantMsg.embeds.push(embed)
        }
      }
    }

    var finalOutputCallback = function (content) {
      rawContent = content
      collectedEmbeds = {}
      var result = parseEmbeds(rawContent)
      assistantMsg.content = result.cleanContent
      assistantMsg.embeds = result.embeds.length > 0 ? result.embeds : undefined
      for (var i = 0; i < result.embeds.length; i++) {
        collectedEmbeds[result.embeds[i].id] = true
      }
    }

    var endCallback = function (data) {
      assistantMsg.isStreaming = false
      assistantMsg.isSuccess = '1'
      state.isLoading = false
      currentStreamCloser = null
      if (data && data.requestId) {
        assistantMsg.requestId = data.requestId
      }
    }

    var errorCallback = function () {
      assistantMsg.content += '\n[请求失败，请重试]'
      assistantMsg.isStreaming = false
      assistantMsg.isSuccess = '0'
      state.isLoading = false
      currentStreamCloser = null
    }

    currentStreamCloser = sendChatStream(
      userMessage,
      {
        onContent: contentCallback,
        onFinalOutput: finalOutputCallback,
        onEnd: endCallback,
        onError: errorCallback
      },
      getConversationId() || undefined
    )
  }

  function handleCardConfirm(payload) {
    if (state.isLoading || !payload.apiEndpoint) return

    var userMsg = {
      id: 'msg_' + messageIdCounter++,
      role: 'user',
      content: '[确认] ' + payload.displayTitle,
      timestamp: Date.now()
    }
    state.messages.push(userMsg)

    var assistantMsg = {
      id: 'msg_' + messageIdCounter++,
      role: 'assistant',
      content: '',
      embeds: [],
      timestamp: Date.now(),
      isStreaming: true
    }
    state.messages.push(assistantMsg)
    state.isLoading = true

    var body = {}
    for (var i = 0; i < payload.cardInfo.length; i++) {
      var item = payload.cardInfo[i]
      body[item.key] = item.value
    }

    fetch(payload.apiEndpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(body)
    }).then(function (res) {
      return res.text()
    }).then(function (text) {
      var displayContent
      try {
        var data = JSON.parse(text)
        displayContent = '```json\n' + JSON.stringify(data, null, 2) + '\n```'
      } catch (e) {
        displayContent = text
      }
      assistantMsg.content = displayContent
    }).catch(function () {
      assistantMsg.content = '[查询失败，请重试]'
    }).finally(function () {
      assistantMsg.isStreaming = false
      state.isLoading = false
    })
  }

  return {
    state: state,
    setMessagesRef: setMessagesRef,
    scrollToBottom: scrollToBottom,
    handleSend: handleSend,
    handleRemoveCard: handleRemoveCard,
    handleUpdateCard: handleUpdateCard,
    handleCardConfirm: handleCardConfirm,
    loadMessages: loadMessages,
    clearMessages: clearMessages
  }
}
