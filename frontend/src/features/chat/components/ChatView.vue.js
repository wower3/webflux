import { toRef } from 'vue';
import ChatMessage from './ChatMessage.vue';
import ChatInput from './ChatInput.vue';
import GuidedPrompts from './GuidedPrompts.vue';
import { useChat } from '../composables/useChat';
const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, } = await import('vue');
let __VLS_typeProps;
const props = defineProps();
const { messages, isLoading, messagesRef, handleSend, handleRemoveCard, handleUpdateCard, loadMessages, clearMessages } = useChat(toRef(() => props.conversationId ?? null));
const sendMessage = (text) => {
    handleSend(text);
};
const __VLS_exposed = { loadMessages, clearMessages, sendMessage };
defineExpose({ loadMessages, clearMessages, sendMessage });
const __VLS_fnComponent = (await import('vue')).defineComponent({});
;
let __VLS_functionalComponentProps;
function __VLS_template() {
    let __VLS_ctx;
    /* Components */
    let __VLS_otherComponents;
    let __VLS_own;
    let __VLS_localComponents;
    let __VLS_components;
    let __VLS_styleScopedClasses;
    // CSS variable injection 
    // CSS variable injection end 
    let __VLS_resolvedLocalAndGlobalComponents;
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("chat-container") }, });
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("chat-messages") }, ref: ("messagesRef"), });
    // @ts-ignore
    (__VLS_ctx.messagesRef);
    for (const [msg] of __VLS_getVForSourceType((__VLS_ctx.messages))) {
        // @ts-ignore
        [ChatMessage,];
        // @ts-ignore
        const __VLS_0 = __VLS_asFunctionalComponent(ChatMessage, new ChatMessage({ ...{ 'onRemoveCard': {} }, ...{ 'onUpdateCard': {} }, key: ((msg.id)), role: ((msg.role)), content: ((msg.content)), embeds: ((msg.embeds)), isStreaming: ((msg.isStreaming)), }));
        const __VLS_1 = __VLS_0({ ...{ 'onRemoveCard': {} }, ...{ 'onUpdateCard': {} }, key: ((msg.id)), role: ((msg.role)), content: ((msg.content)), embeds: ((msg.embeds)), isStreaming: ((msg.isStreaming)), }, ...__VLS_functionalComponentArgsRest(__VLS_0));
        ({}({ ...{ 'onRemoveCard': {} }, ...{ 'onUpdateCard': {} }, key: ((msg.id)), role: ((msg.role)), content: ((msg.content)), embeds: ((msg.embeds)), isStreaming: ((msg.isStreaming)), }));
        let __VLS_5;
        const __VLS_6 = {
            onRemoveCard: (__VLS_ctx.handleRemoveCard)
        };
        const __VLS_7 = {
            onUpdateCard: (__VLS_ctx.handleUpdateCard)
        };
        const __VLS_4 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(ChatMessage, __VLS_1));
        let __VLS_2;
        let __VLS_3;
        // @ts-ignore
        [messagesRef, messages, handleRemoveCard, handleUpdateCard,];
    }
    // @ts-ignore
    [GuidedPrompts,];
    // @ts-ignore
    const __VLS_8 = __VLS_asFunctionalComponent(GuidedPrompts, new GuidedPrompts({}));
    const __VLS_9 = __VLS_8({}, ...__VLS_functionalComponentArgsRest(__VLS_8));
    ({}({}));
    const __VLS_12 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(GuidedPrompts, __VLS_9));
    // @ts-ignore
    [ChatInput,];
    // @ts-ignore
    const __VLS_13 = __VLS_asFunctionalComponent(ChatInput, new ChatInput({ ...{ 'onSend': {} }, loading: ((__VLS_ctx.isLoading)), }));
    const __VLS_14 = __VLS_13({ ...{ 'onSend': {} }, loading: ((__VLS_ctx.isLoading)), }, ...__VLS_functionalComponentArgsRest(__VLS_13));
    ({}({ ...{ 'onSend': {} }, loading: ((__VLS_ctx.isLoading)), }));
    let __VLS_18;
    const __VLS_19 = {
        onSend: (__VLS_ctx.handleSend)
    };
    const __VLS_17 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(ChatInput, __VLS_14));
    let __VLS_15;
    let __VLS_16;
    // @ts-ignore
    [isLoading, handleSend,];
    if (typeof __VLS_styleScopedClasses === 'object' && !Array.isArray(__VLS_styleScopedClasses)) {
        __VLS_styleScopedClasses['chat-container'];
        __VLS_styleScopedClasses['chat-messages'];
    }
    var __VLS_slots;
    return __VLS_slots;
    const __VLS_componentsOption = {};
    let __VLS_name;
    let __VLS_defineComponent;
    const __VLS_internalComponent = __VLS_defineComponent({
        setup() {
            return {
                ChatMessage: ChatMessage,
                ChatInput: ChatInput,
                GuidedPrompts: GuidedPrompts,
                messages: messages,
                isLoading: isLoading,
                messagesRef: messagesRef,
                handleSend: handleSend,
                handleRemoveCard: handleRemoveCard,
                handleUpdateCard: handleUpdateCard,
            };
        },
        props: {},
    });
}
export default (await import('vue')).defineComponent({
    setup() {
        return {
            ...__VLS_exposed,
        };
    },
    props: {},
});
;
