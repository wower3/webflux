import { ref, onMounted, onBeforeUnmount } from 'vue';
const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, } = await import('vue');
const activeIndex = ref(null);
const copiedIndex = ref(null);
const toggle = (i) => {
    activeIndex.value = activeIndex.value === i ? null : i;
};
const close = () => {
    activeIndex.value = null;
};
onMounted(() => {
    document.addEventListener('click', close);
});
onBeforeUnmount(() => {
    document.removeEventListener('click', close);
});
const copy = async (text, key) => {
    try {
        await navigator.clipboard.writeText(text);
    }
    catch {
        const ta = document.createElement('textarea');
        ta.value = text;
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        document.body.removeChild(ta);
    }
    copiedIndex.value = key;
    setTimeout(() => { copiedIndex.value = null; }, 1500);
};
const CATEGORIES = [
    {
        name: '查询统计数据',
        icon: 'fa fa-table',
        examples: [
            '本月各区域的销售总额分别是多少？',
            '对比本季度与上季度的订单量变化',
            '统计各产品类别的退货率排名',
        ]
    },
    {
        name: '根据统计数据作图',
        icon: 'fa fa-chart-line',
        examples: [
            '画一张近12个月的销售额趋势折线图',
            '用柱状图展示各部门本季度业绩对比',
            '生成一个各区域占比的饼图',
        ]
    },
    {
        name: '查询投诉详情',
        icon: 'fa fa-clipboard-list',
        examples: [
            '查询最近7天的投诉记录及处理状态',
            '投诉编号C20240415003的处理进度如何？',
            '按投诉类型统计本月投诉数量分布',
        ]
    }
];
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
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("guided-prompts") }, });
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("category-list") }, });
    for (const [cat, i] of __VLS_getVForSourceType((__VLS_ctx.CATEGORIES))) {
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ onClick: (...[$event]) => {
                    __VLS_ctx.toggle(i);
                    // @ts-ignore
                    [CATEGORIES, toggle,];
                } }, key: ((i)), ...{ class: ("category-card") }, ...{ class: (({ active: __VLS_ctx.activeIndex === i })) }, });
        __VLS_styleScopedClasses = ({ active: activeIndex === i });
        __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ((cat.icon)) }, });
        __VLS_styleScopedClasses = (cat.icon);
        // @ts-ignore
        [activeIndex,];
        __VLS_elementAsFunction(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        (cat.name);
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
        const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({ name: ("expand"), }));
        const __VLS_2 = __VLS_1({ name: ("expand"), }, ...__VLS_functionalComponentArgsRest(__VLS_1));
        ({}({ name: ("expand"), }));
        const __VLS_5 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(__VLS_0, __VLS_2));
        if (__VLS_ctx.activeIndex === i) {
            __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ onClick: () => { } }, ...{ class: ("example-popover") }, });
            for (const [ex, j] of __VLS_getVForSourceType((cat.examples))) {
                __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ key: ((j)), ...{ class: ("example-item") }, });
                __VLS_elementAsFunction(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({ ...{ class: ("example-text") }, });
                (ex);
                // @ts-ignore
                [activeIndex,];
                __VLS_elementAsFunction(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({ ...{ onClick: (...[$event]) => {
                            if (!((__VLS_ctx.activeIndex === i)))
                                return;
                            __VLS_ctx.copy(ex, `${i}-${j}`);
                            // @ts-ignore
                            [copy,];
                        } }, ...{ class: ("copy-btn") }, ...{ class: (({ copied: __VLS_ctx.copiedIndex === `${i}-${j}` })) }, });
                __VLS_styleScopedClasses = ({ copied: copiedIndex === `${i}-${j}` });
                __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ((__VLS_ctx.copiedIndex === `${i}-${j}` ? 'fa fa-check' : 'fa fa-copy')) }, });
                __VLS_styleScopedClasses = (copiedIndex === `${i}-${j}` ? 'fa fa-check' : 'fa fa-copy');
                // @ts-ignore
                [copiedIndex, copiedIndex,];
            }
        }
        __VLS_nonNullable(__VLS_5.slots).default;
    }
    if (typeof __VLS_styleScopedClasses === 'object' && !Array.isArray(__VLS_styleScopedClasses)) {
        __VLS_styleScopedClasses['guided-prompts'];
        __VLS_styleScopedClasses['category-list'];
        __VLS_styleScopedClasses['category-card'];
        __VLS_styleScopedClasses['example-popover'];
        __VLS_styleScopedClasses['example-item'];
        __VLS_styleScopedClasses['example-text'];
        __VLS_styleScopedClasses['copy-btn'];
    }
    var __VLS_slots;
    return __VLS_slots;
    const __VLS_componentsOption = {};
    let __VLS_name;
    let __VLS_defineComponent;
    const __VLS_internalComponent = __VLS_defineComponent({
        setup() {
            return {
                activeIndex: activeIndex,
                copiedIndex: copiedIndex,
                toggle: toggle,
                copy: copy,
                CATEGORIES: CATEGORIES,
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
