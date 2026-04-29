import { ref, watch, nextTick, triggerRef } from 'vue';
import { sendChatStream } from '../api/chat';
export function useChat(conversationId) {
    const messages = ref([]);
    const isLoading = ref(false);
    let currentStreamCloser = null;
    let messageIdCounter = 0;
    const messagesRef = ref();
    const scrollToBottom = () => {
        nextTick(() => {
            messagesRef.value?.scrollTo({
                top: messagesRef.value.scrollHeight,
                behavior: 'smooth'
            });
        });
    };
    function parseEmbeds(content) {
        const embeds = [];
        const processedIds = new Set();
        const embedPattern = /"type"\s*:\s*"(chart|card)"/g;
        let match;
        while ((match = embedPattern.exec(content)) !== null) {
            let openBrace = -1;
            let depth = 0;
            for (let i = match.index; i >= 0; i--) {
                if (content[i] === '}')
                    depth++;
                else if (content[i] === '{') {
                    depth--;
                    if (depth < 0) {
                        openBrace = i;
                        break;
                    }
                }
            }
            if (openBrace === -1)
                continue;
            let closeBrace = -1;
            depth = 0;
            for (let i = openBrace; i < content.length; i++) {
                if (content[i] === '{')
                    depth++;
                else if (content[i] === '}') {
                    depth--;
                    if (depth === 0 && i > openBrace) {
                        closeBrace = i;
                        break;
                    }
                }
            }
            if (closeBrace === -1)
                continue;
            const jsonStr = content.slice(openBrace, closeBrace + 1);
            try {
                const data = JSON.parse(jsonStr);
                if (data.type === 'chart' && data.subtype) {
                    const chartId = data.chartId || `${data.subtype}_${data.title || 'untitled'}`;
                    if (!processedIds.has(chartId)) {
                        processedIds.add(chartId);
                        embeds.push({
                            id: chartId,
                            type: 'chart',
                            data: {
                                subtype: data.subtype,
                                title: data.title || '',
                                chartData: data.data || {}
                            }
                        });
                        const placeholder = `[CHART:${chartId}]`;
                        content = content.substring(0, openBrace) + placeholder + content.substring(closeBrace + 1);
                        embedPattern.lastIndex = openBrace + placeholder.length;
                        continue;
                    }
                }
                else if (data.type === 'card' && data.cardId && !processedIds.has(data.cardId)) {
                    processedIds.add(data.cardId);
                    embeds.push({
                        id: data.cardId,
                        type: 'card',
                        data: data
                    });
                    const placeholder = `[CARD:${data.cardId}]`;
                    content = content.substring(0, openBrace) + placeholder + content.substring(closeBrace + 1);
                    embedPattern.lastIndex = openBrace + placeholder.length;
                    continue;
                }
            }
            catch {
                // JSON 不完整，等待更多数据
            }
            embedPattern.lastIndex = closeBrace + 1;
        }
        return { cleanContent: content, embeds };
    }
    const handleRemoveCard = (cardId) => {
        const assistantMsg = messages.value.filter(m => m.role === 'assistant').pop();
        if (!assistantMsg || !assistantMsg.embeds)
            return;
        const index = assistantMsg.embeds.findIndex(e => e.id === cardId);
        if (index !== -1) {
            assistantMsg.embeds.splice(index, 1);
            assistantMsg.content = assistantMsg.content.replace(`[CARD:${cardId}]`, '');
            triggerRef(messages);
        }
    };
    const handleUpdateCard = (card) => {
        const assistantMsg = messages.value.filter(m => m.role === 'assistant').pop();
        if (!assistantMsg || !assistantMsg.embeds)
            return;
        const index = assistantMsg.embeds.findIndex(e => e.id === card.cardId);
        if (index !== -1) {
            assistantMsg.embeds[index].data = card;
            triggerRef(messages);
        }
    };
    const loadMessages = (msgs) => {
        if (currentStreamCloser) {
            currentStreamCloser();
            currentStreamCloser = null;
        }
        messages.value = msgs.map((msg) => {
            if (msg.role !== 'assistant')
                return msg;
            const { cleanContent, embeds } = parseEmbeds(msg.content);
            return { ...msg, content: cleanContent, embeds: embeds.length > 0 ? embeds : undefined };
        });
        isLoading.value = false;
        messageIdCounter = msgs.length;
        nextTick(scrollToBottom);
    };
    const clearMessages = () => {
        if (currentStreamCloser) {
            currentStreamCloser();
            currentStreamCloser = null;
        }
        messages.value = [];
        isLoading.value = false;
        messageIdCounter = 0;
    };
    const handleSend = async (userMessage) => {
        if (isLoading.value)
            return;
        if (currentStreamCloser) {
            currentStreamCloser();
            currentStreamCloser = null;
        }
        const userMsg = {
            id: `msg_${messageIdCounter++}`,
            role: 'user',
            content: userMessage,
            timestamp: Date.now()
        };
        messages.value.push(userMsg);
        triggerRef(messages);
        const assistantMsg = {
            id: `msg_${messageIdCounter++}`,
            role: 'assistant',
            content: '',
            embeds: [],
            timestamp: Date.now(),
            isStreaming: true
        };
        messages.value.push(assistantMsg);
        triggerRef(messages);
        isLoading.value = true;
        let rawContent = '';
        const collectedEmbeds = new Set();
        const contentCallback = (content) => {
            rawContent += content;
            const { cleanContent, embeds } = parseEmbeds(rawContent);
            assistantMsg.content = cleanContent;
            for (const embed of embeds) {
                if (!collectedEmbeds.has(embed.id)) {
                    collectedEmbeds.add(embed.id);
                    if (!assistantMsg.embeds)
                        assistantMsg.embeds = [];
                    assistantMsg.embeds.push(embed);
                }
            }
            triggerRef(messages);
        };
        const finalOutputCallback = (content) => {
            rawContent = content;
            collectedEmbeds.clear();
            const { cleanContent, embeds } = parseEmbeds(rawContent);
            assistantMsg.content = cleanContent;
            assistantMsg.embeds = embeds.length > 0 ? embeds : undefined;
            for (const embed of embeds) {
                collectedEmbeds.add(embed.id);
            }
            triggerRef(messages);
        };
        const endCallback = () => {
            assistantMsg.isStreaming = false;
            isLoading.value = false;
            currentStreamCloser = null;
            triggerRef(messages);
        };
        const errorCallback = (_error) => {
            assistantMsg.content += '\n[请求失败，请重试]';
            assistantMsg.isStreaming = false;
            isLoading.value = false;
            currentStreamCloser = null;
            triggerRef(messages);
        };
        currentStreamCloser = sendChatStream(userMessage, {
            onContent: contentCallback,
            onFinalOutput: finalOutputCallback,
            onEnd: endCallback,
            onError: errorCallback
        }, conversationId.value || undefined);
    };
    watch(messages, scrollToBottom, { deep: true });
    return {
        messages,
        isLoading,
        messagesRef,
        handleSend,
        handleRemoveCard,
        handleUpdateCard,
        loadMessages,
        clearMessages
    };
}
