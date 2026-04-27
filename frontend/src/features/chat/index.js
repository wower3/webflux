// 组件
import ChatWidget from './components/ChatWidget.vue';
import ChatPanel from './components/ChatPanel.vue';
import ChatView from './components/ChatView.vue';
import ChatMessage from './components/ChatMessage.vue';
import ChatInput from './components/ChatInput.vue';
import GuidedPrompts from './components/GuidedPrompts.vue';
import MarkdownRenderer from './components/MarkdownRenderer.vue';
import ChartRenderer from './components/ChartRenderer.vue';
import CardRenderer from './components/CardRenderer.vue';
export { ChatWidget, ChatPanel, ChatView, ChatMessage, ChatInput, GuidedPrompts, MarkdownRenderer, ChartRenderer, CardRenderer };
// Composable
export { useChat } from './composables/useChat';
// API
export { sendChatStream } from './api/chat';
// Utils
export { toEChartsOption } from './utils/chartParser';
/**
 * Vue Plugin - 一键注册所有聊天组件
 */
export const ChatPlugin = {
    install(app) {
        app.component('ChatWidget', ChatWidget);
        app.component('ChatPanel', ChatPanel);
        app.component('ChatView', ChatView);
        app.component('ChatMessage', ChatMessage);
        app.component('ChatInput', ChatInput);
        app.component('GuidedPrompts', GuidedPrompts);
        app.component('MarkdownRenderer', MarkdownRenderer);
        app.component('ChartRenderer', ChartRenderer);
        app.component('CardRenderer', CardRenderer);
    }
};
