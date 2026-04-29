import { ref } from 'vue';
import ChatPanel from './ChatPanel.vue';
const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, } = await import('vue');
const isOpen = ref(false);
const open = () => {
    isOpen.value = true;
};
const close = () => {
    isOpen.value = false;
};
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
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ onClick: (__VLS_ctx.open) }, ...{ class: ("chat-fab") }, });
    __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ("fa fa-comments") }, });
    // @ts-ignore
    [open,];
    // @ts-ignore
    const __VLS_0 = {}
        .Transition;
    ({}.Transition);
    ({}.Transition);
    __VLS_components.Transition;
    __VLS_components.Transition;
    // @ts-ignore
    [Transition, Transition,];
    // @ts-ignore
    const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({ name: ("fade"), }));
    const __VLS_2 = __VLS_1({ name: ("fade"), }, ...__VLS_functionalComponentArgsRest(__VLS_1));
    ({}({ name: ("fade"), }));
    const __VLS_5 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(__VLS_0, __VLS_2));
    if (__VLS_ctx.isOpen) {
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ onClick: (__VLS_ctx.close) }, ...{ class: ("chat-overlay") }, });
        // @ts-ignore
        const __VLS_6 = {}
            .Transition;
        ({}.Transition);
        ({}.Transition);
        __VLS_components.Transition;
        __VLS_components.Transition;
        // @ts-ignore
        [Transition, Transition,];
        // @ts-ignore
        const __VLS_7 = __VLS_asFunctionalComponent(__VLS_6, new __VLS_6({ name: ("slide"), }));
        const __VLS_8 = __VLS_7({ name: ("slide"), }, ...__VLS_functionalComponentArgsRest(__VLS_7));
        ({}({ name: ("slide"), }));
        const __VLS_11 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(__VLS_6, __VLS_8));
        if (__VLS_ctx.isOpen) {
            // @ts-ignore
            [ChatPanel,];
            // @ts-ignore
            const __VLS_12 = __VLS_asFunctionalComponent(ChatPanel, new ChatPanel({ ...{ 'onClose': {} }, }));
            const __VLS_13 = __VLS_12({ ...{ 'onClose': {} }, }, ...__VLS_functionalComponentArgsRest(__VLS_12));
            ({}({ ...{ 'onClose': {} }, }));
            let __VLS_17;
            const __VLS_18 = {
                onClose: (__VLS_ctx.close)
            };
            const __VLS_16 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(ChatPanel, __VLS_13));
            let __VLS_14;
            let __VLS_15;
            // @ts-ignore
            [isOpen, isOpen, close, close,];
        }
        __VLS_nonNullable(__VLS_11.slots).default;
    }
    __VLS_nonNullable(__VLS_5.slots).default;
    if (typeof __VLS_styleScopedClasses === 'object' && !Array.isArray(__VLS_styleScopedClasses)) {
        __VLS_styleScopedClasses['chat-fab'];
        __VLS_styleScopedClasses['fa'];
        __VLS_styleScopedClasses['fa-comments'];
        __VLS_styleScopedClasses['chat-overlay'];
    }
    var __VLS_slots;
    return __VLS_slots;
    const __VLS_componentsOption = {};
    let __VLS_name;
    let __VLS_defineComponent;
    const __VLS_internalComponent = __VLS_defineComponent({
        setup() {
            return {
                ChatPanel: ChatPanel,
                isOpen: isOpen,
                open: open,
                close: close,
            };
        },
    });
}
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
;
