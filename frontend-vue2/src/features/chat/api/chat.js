import { getToken } from '@/utils/auth'

function createSSEConnection(url, body, callbacks) {
  var ended = false
  var abortController = new AbortController()

  var close = function () {
    ended = true
    abortController.abort()
  }

  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': getToken()
    },
    body: JSON.stringify(body),
    signal: abortController.signal
  }).then(function (response) {
    if (!response.ok) {
      callbacks.onError(new Error('HTTP ' + response.status))
      return
    }

    if (!response.body) {
      callbacks.onError(new Error('No response body'))
      return
    }

    var reader = response.body.getReader()
    var decoder = new TextDecoder()
    var buffer = ''

    function processChunk() {
      return reader.read().then(function (result) {
        if (ended || result.done) return

        buffer += decoder.decode(result.value, { stream: true })

        var lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (var i = 0; i < lines.length; i++) {
          var line = lines[i]
          if (line.indexOf('data:') !== 0) continue

          var dataStr = line.slice(5).trim()
          if (!dataStr) continue

          try {
            var event = JSON.parse(dataStr)
            if (event.type === 'end') {
              ended = true
              callbacks.onEnd(event.data)
            } else if (event.type === 'final_output') {
              callbacks.onFinalOutput(String(event.data != null ? event.data : ''))
            } else {
              callbacks.onContent(String(event.data != null ? event.data : ''))
            }
          } catch (e) {
            callbacks.onContent(dataStr)
          }
        }

        if (!ended) {
          return processChunk()
        }
      }).catch(function () {
        // handled below
      })
    }

    processChunk().catch(function () {
      if (!ended) {
        ended = true
        callbacks.onEnd()
      }
    })
  }).catch(function (err) {
    if (!ended) {
      ended = true
      callbacks.onError(err instanceof Error ? err : new Error(String(err)))
    }
  })

  return close
}

export function sendChatStream(message, callbacks, conversationId) {
  var body = { message: message }
  if (conversationId) {
    body.conversationId = conversationId
  }
  return createSSEConnection('/chatbot/chat/stream', body, callbacks)
}
