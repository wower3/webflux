import { computed } from 'vue';
import MarkdownRenderer from './MarkdownRenderer.vue';
import ChartRenderer from './ChartRenderer.vue';
import CardRenderer from './CardRenderer.vue';
const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, } = await import('vue');
let __VLS_typeProps;
const props = defineProps();
const emit = defineEmits();
const handleRemoveCard = (cardId) => {
    emit('removeCard', cardId);
};
const handleUpdateCard = (card) => {
    emit('updateCard', card);
};
const PLACEHOLDER_PATTERN = /\[([A-Z]+):([^\]]+)\]/g;
const contentSegments = computed(() => {
    if (props.role === 'user') {
        return [{ type: 'text', content: props.content }];
    }
    const segments = [];
    const embeds = props.embeds || [];
    let lastIndex = 0;
    let match;
    while ((match = PLACEHOLDER_PATTERN.exec(props.content)) !== null) {
        const fullMatch = match[0];
        const id = match[2];
        if (match.index > lastIndex) {
            segments.push({
                type: 'text',
                content: props.content.slice(lastIndex, match.index)
            });
        }
        const embed = embeds.find((e) => e.id === id);
        if (embed && embed.type === 'chart') {
            segments.push({
                type: 'chart',
                data: {
                    id: embed.id,
                    type: embed.data.subtype,
                    title: embed.data.title,
                    data: embed.data.chartData
                }
            });
        }
        else if (embed && embed.type === 'card') {
            segments.push({
                type: 'card',
                data: embed.data
            });
        }
        lastIndex = match.index + fullMatch.length;
    }
    if (lastIndex < props.content.length) {
        segments.push({
            type: 'text',
            content: props.content.slice(lastIndex)
        });
    }
    return segments.length > 0 ? segments : [{ type: 'text', content: props.content }];
});
const __VLS_fnComponent = (await import('vue')).defineComponent({
    emits: {},
});
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
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("message-row") }, });
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("message-content") }, ...{ class: (({ 'user-message': __VLS_ctx.role === 'user', 'ai-message': __VLS_ctx.role === 'assistant' })) }, });
    __VLS_styleScopedClasses = ({ 'user-message': role === 'user', 'ai-message': role === 'assistant' });
    if (__VLS_ctx.role === 'assistant') {
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("avatar") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ("fa fa-cube") }, });
        // @ts-ignore
        [role, role, role,];
    }
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("message-bubble") }, });
    for (const [segment, index] of __VLS_getVForSourceType((__VLS_ctx.contentSegments))) {
        (segment.type === 'chart' ? segment.data?.id || `chart-${index}` : segment.type === 'card' ? segment.data?.cardId || `card-${index}` : `text-${index}`);
        if (segment.type === 'text') {
            // @ts-ignore
            [MarkdownRenderer,];
            // @ts-ignore
            const __VLS_0 = __VLS_asFunctionalComponent(MarkdownRenderer, new MarkdownRenderer({ content: ((segment.content || '')), isStreaming: ((__VLS_ctx.isStreaming)), }));
            const __VLS_1 = __VLS_0({ content: ((segment.content || '')), isStreaming: ((__VLS_ctx.isStreaming)), }, ...__VLS_functionalComponentArgsRest(__VLS_0));
            ({}({ content: ((segment.content || '')), isStreaming: ((__VLS_ctx.isStreaming)), }));
            const __VLS_4 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(MarkdownRenderer, __VLS_1));
            // @ts-ignore
            [contentSegments, isStreaming,];
        }
        else if (segment.type === 'chart' && segment.data) {
            // @ts-ignore
            [ChartRenderer,];
            // @ts-ignore
            const __VLS_5 = __VLS_asFunctionalComponent(ChartRenderer, new ChartRenderer({ chart: segment.data, }));
            const __VLS_6 = __VLS_5({ chart: segment.data, }, ...__VLS_functionalComponentArgsRest(__VLS_5));
            ({}({ chart: segment.data, }));
            const __VLS_9 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(ChartRenderer, __VLS_6));
        }
        else if (segment.type === 'card' && segment.data) {
            // @ts-ignore
            [CardRenderer,];
            // @ts-ignore
            const __VLS_10 = __VLS_asFunctionalComponent(CardRenderer, new CardRenderer({ ...{ 'onRemove': {} }, ...{ 'onUpdate': {} }, card: segment.data, }));
            const __VLS_11 = __VLS_10({ ...{ 'onRemove': {} }, ...{ 'onUpdate': {} }, card: segment.data, }, ...__VLS_functionalComponentArgsRest(__VLS_10));
            ({}({ ...{ 'onRemove': {} }, ...{ 'onUpdate': {} }, card: segment.data, }));
            let __VLS_15;
            const __VLS_16 = {
                onRemove: (__VLS_ctx.handleRemoveCard)
            };
            const __VLS_17 = {
                onUpdate: (__VLS_ctx.handleUpdateCard)
            };
            const __VLS_14 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(CardRenderer, __VLS_11));
            let __VLS_12;
            let __VLS_13;
            // @ts-ignore
            [handleRemoveCard, handleUpdateCard,];
        }
    }
    if (__VLS_ctx.isStreaming) {
        __VLS_elementAsFunction(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({ ...{ class: ("cursor") }, });
        // @ts-ignore
        [isStreaming,];
    }
    if (__VLS_ctx.role === 'user') {
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("avatar") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ("fa fa-user") }, });
        // @ts-ignore
        [role,];
    }
    if (typeof __VLS_styleScopedClasses === 'object' && !Array.isArray(__VLS_styleScopedClasses)) {
        __VLS_styleScopedClasses['message-row'];
        __VLS_styleScopedClasses['message-content'];
        __VLS_styleScopedClasses['avatar'];
        __VLS_styleScopedClasses['fa'];
        __VLS_styleScopedClasses['fa-cube'];
        __VLS_styleScopedClasses['message-bubble'];
        __VLS_styleScopedClasses['cursor'];
        __VLS_styleScopedClasses['avatar'];
        __VLS_styleScopedClasses['fa'];
        __VLS_styleScopedClasses['fa-user'];
    }
    var __VLS_slots;
    return __VLS_slots;
    const __VLS_componentsOption = {};
    let __VLS_name;
    let __VLS_defineComponent;
    const __VLS_internalComponent = __VLS_defineComponent({
        setup() {
            return {
                MarkdownRenderer: MarkdownRenderer,
                ChartRenderer: ChartRenderer,
                CardRenderer: CardRenderer,
                handleRemoveCard: handleRemoveCard,
                handleUpdateCard: handleUpdateCard,
                contentSegments: contentSegments,
            };
        },
        props: {},
        emits: {},
    });
}
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
    props: {},
    emits: {},
});
;
