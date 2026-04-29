import { ref } from 'vue';
const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, } = await import('vue');
let __VLS_typeProps;
const props = defineProps();
const emit = defineEmits();
const isEditing = ref(false);
const editValues = ref({});
const getButtonClass = (actionId) => {
    switch (actionId) {
        case 'confirm':
            return 'btn-primary';
        case 'cancel':
            return 'btn-secondary';
        case 'edit':
            return 'btn-edit';
        default:
            return '';
    }
};
const saveEdit = () => {
    const updatedCard = {
        ...props.card,
        cardInfo: props.card.cardInfo.map(item => ({
            ...item,
            value: editValues.value[item.key] || item.value
        }))
    };
    console.log('[CardRenderer] 保存编辑:', updatedCard);
    emit('update', updatedCard);
    isEditing.value = false;
};
const cancelEdit = () => {
    editValues.value = {};
    isEditing.value = false;
};
const handleAction = (btn) => {
    switch (btn.actionId) {
        case 'edit':
            editValues.value = {};
            props.card.cardInfo.forEach(item => {
                editValues.value[item.key] = item.value;
            });
            isEditing.value = true;
            break;
        case 'confirm':
            console.log('[CardRenderer] 发送请求到:', btn.apiEndpoint);
            console.log('[CardRenderer] 请求数据:', props.card);
            break;
        case 'cancel':
            console.log('[CardRenderer] 移除卡片:', props.card.cardId);
            emit('remove', props.card.cardId);
            break;
        default:
            console.log('[CardRenderer] 未知操作:', btn.actionId);
    }
};
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
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("card-container") }, });
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("card-header") }, });
    __VLS_elementAsFunction(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
    (__VLS_ctx.card.displayTitle);
    // @ts-ignore
    [card,];
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("card-body") }, });
    for (const [item] of __VLS_getVForSourceType((__VLS_ctx.card.cardInfo))) {
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ key: ((item.key)), ...{ class: ("card-row") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({ ...{ class: ("card-label") }, });
        (item.label);
        // @ts-ignore
        [card,];
        if (!__VLS_ctx.isEditing) {
            __VLS_elementAsFunction(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({ ...{ class: ("card-value") }, });
            (item.value);
            // @ts-ignore
            [isEditing,];
        }
        else {
            __VLS_elementAsFunction(__VLS_intrinsicElements.input)({ value: ((__VLS_ctx.editValues[item.key])), ...{ class: ("card-input") }, type: ("text"), });
            // @ts-ignore
            [editValues,];
        }
    }
    if (__VLS_ctx.isEditing) {
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("card-footer") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({ ...{ onClick: (__VLS_ctx.saveEdit) }, ...{ class: ("card-btn btn-primary") }, });
        // @ts-ignore
        [isEditing, saveEdit,];
        __VLS_elementAsFunction(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({ ...{ onClick: (__VLS_ctx.cancelEdit) }, ...{ class: ("card-btn btn-secondary") }, });
        // @ts-ignore
        [cancelEdit,];
    }
    else {
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("card-footer") }, });
        for (const [btn] of __VLS_getVForSourceType((__VLS_ctx.card.buttons))) {
            __VLS_elementAsFunction(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({ ...{ onClick: (...[$event]) => {
                        if (!(!((__VLS_ctx.isEditing))))
                            return;
                        __VLS_ctx.handleAction(btn);
                        // @ts-ignore
                        [card, handleAction,];
                    } }, key: ((btn.actionId)), ...{ class: ((__VLS_ctx.getButtonClass(btn.actionId))) }, ...{ class: ("card-btn") }, });
            __VLS_styleScopedClasses = (getButtonClass(btn.actionId));
            (btn.label);
            // @ts-ignore
            [getButtonClass,];
        }
    }
    if (typeof __VLS_styleScopedClasses === 'object' && !Array.isArray(__VLS_styleScopedClasses)) {
        __VLS_styleScopedClasses['card-container'];
        __VLS_styleScopedClasses['card-header'];
        __VLS_styleScopedClasses['card-body'];
        __VLS_styleScopedClasses['card-row'];
        __VLS_styleScopedClasses['card-label'];
        __VLS_styleScopedClasses['card-value'];
        __VLS_styleScopedClasses['card-input'];
        __VLS_styleScopedClasses['card-footer'];
        __VLS_styleScopedClasses['card-btn'];
        __VLS_styleScopedClasses['btn-primary'];
        __VLS_styleScopedClasses['card-btn'];
        __VLS_styleScopedClasses['btn-secondary'];
        __VLS_styleScopedClasses['card-footer'];
        __VLS_styleScopedClasses['card-btn'];
    }
    var __VLS_slots;
    return __VLS_slots;
    const __VLS_componentsOption = {};
    let __VLS_name;
    let __VLS_defineComponent;
    const __VLS_internalComponent = __VLS_defineComponent({
        setup() {
            return {
                isEditing: isEditing,
                editValues: editValues,
                getButtonClass: getButtonClass,
                saveEdit: saveEdit,
                cancelEdit: cancelEdit,
                handleAction: handleAction,
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
