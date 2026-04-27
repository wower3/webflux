import { ref } from 'vue';
const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, } = await import('vue');
const emit = defineEmits();
const inputText = ref('');
const loading = defineModel('loading', { default: false });
const handleSend = () => {
    const message = inputText.value.trim();
    if (!message || loading.value)
        return;
    emit('send', message);
    inputText.value = '';
};
const __VLS_fnComponent = (await import('vue')).defineComponent({
    emits: {},
});
;
let __VLS_functionalComponentProps;
const __VLS_defaults = {};
const __VLS_modelEmitsType = (await import('vue')).defineEmits();
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
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("input-container") }, });
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("input-box") }, });
    // @ts-ignore
    const __VLS_0 = {}
        .ElInput;
    ({}.ElInput);
    __VLS_components.ElInput;
    __VLS_components.elInput;
    // @ts-ignore
    [ElInput,];
    // @ts-ignore
    const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({ ...{ 'onKeydown': {} }, modelValue: ((__VLS_ctx.inputText)), type: ("textarea"), rows: ((3)), placeholder: ("给大模型发送消息..."), disabled: ((__VLS_ctx.loading)), resize: ("none"), }));
    const __VLS_2 = __VLS_1({ ...{ 'onKeydown': {} }, modelValue: ((__VLS_ctx.inputText)), type: ("textarea"), rows: ((3)), placeholder: ("给大模型发送消息..."), disabled: ((__VLS_ctx.loading)), resize: ("none"), }, ...__VLS_functionalComponentArgsRest(__VLS_1));
    ({}({ ...{ 'onKeydown': {} }, modelValue: ((__VLS_ctx.inputText)), type: ("textarea"), rows: ((3)), placeholder: ("给大模型发送消息..."), disabled: ((__VLS_ctx.loading)), resize: ("none"), }));
    let __VLS_6;
    const __VLS_7 = {
        onKeydown: (__VLS_ctx.handleSend)
    };
    const __VLS_5 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(__VLS_0, __VLS_2));
    let __VLS_3;
    let __VLS_4;
    // @ts-ignore
    [inputText, loading, handleSend,];
    // @ts-ignore
    const __VLS_8 = {}
        .ElButton;
    ({}.ElButton);
    ({}.ElButton);
    __VLS_components.ElButton;
    __VLS_components.elButton;
    __VLS_components.ElButton;
    __VLS_components.elButton;
    // @ts-ignore
    [ElButton, ElButton,];
    // @ts-ignore
    const __VLS_9 = __VLS_asFunctionalComponent(__VLS_8, new __VLS_8({ ...{ 'onClick': {} }, type: ("success"), circle: (true), loading: ((__VLS_ctx.loading)), disabled: ((!__VLS_ctx.inputText.trim())), ...{ class: ("send-btn") }, }));
    const __VLS_10 = __VLS_9({ ...{ 'onClick': {} }, type: ("success"), circle: (true), loading: ((__VLS_ctx.loading)), disabled: ((!__VLS_ctx.inputText.trim())), ...{ class: ("send-btn") }, }, ...__VLS_functionalComponentArgsRest(__VLS_9));
    ({}({ ...{ 'onClick': {} }, type: ("success"), circle: (true), loading: ((__VLS_ctx.loading)), disabled: ((!__VLS_ctx.inputText.trim())), ...{ class: ("send-btn") }, }));
    let __VLS_14;
    const __VLS_15 = {
        onClick: (__VLS_ctx.handleSend)
    };
    const __VLS_13 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(__VLS_8, __VLS_10));
    let __VLS_11;
    let __VLS_12;
    __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ("fa fa-paper-plane") }, ...{ style: ({}) }, });
    // @ts-ignore
    [inputText, loading, handleSend,];
    __VLS_nonNullable(__VLS_13.slots).default;
    if (typeof __VLS_styleScopedClasses === 'object' && !Array.isArray(__VLS_styleScopedClasses)) {
        __VLS_styleScopedClasses['input-container'];
        __VLS_styleScopedClasses['input-box'];
        __VLS_styleScopedClasses['send-btn'];
        __VLS_styleScopedClasses['fa'];
        __VLS_styleScopedClasses['fa-paper-plane'];
    }
    var __VLS_slots;
    return __VLS_slots;
    const __VLS_componentsOption = {};
    let __VLS_name;
    let __VLS_defineComponent;
    const __VLS_internalComponent = __VLS_defineComponent({
        setup() {
            return {
                inputText: inputText,
                loading: loading,
                handleSend: handleSend,
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
