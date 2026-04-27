import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts';
import { toEChartsOption } from '../utils/chartParser';
const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, } = await import('vue');
let __VLS_typeProps;
const props = defineProps();
const chartId = `chart-${props.chart.id}`;
let chartInstance = null;
const chartWidth = ref(600);
const chartHeight = ref(350);
const isResizing = ref(false);
let startX = 0;
let startY = 0;
let startWidth = 0;
let startHeight = 0;
const minWidth = 300;
const minHeight = 200;
const maxWidth = 1200;
const maxHeight = 800;
const initChart = () => {
    const container = document.getElementById(chartId);
    if (!container)
        return;
    if (chartInstance) {
        chartInstance.dispose();
    }
    chartInstance = echarts.init(container);
    const option = toEChartsOption(props.chart);
    chartInstance.setOption(option);
};
onMounted(() => {
    initChart();
    window.addEventListener('resize', handleResize);
});
onUnmounted(() => {
    if (chartInstance) {
        chartInstance.dispose();
    }
    window.removeEventListener('resize', handleResize);
    document.removeEventListener('mousemove', onResize);
    document.removeEventListener('mouseup', stopResize);
});
const handleResize = () => {
    if (chartInstance) {
        chartInstance.resize();
    }
};
const startResize = (e) => {
    isResizing.value = true;
    startX = e.clientX;
    startY = e.clientY;
    startWidth = chartWidth.value;
    startHeight = chartHeight.value;
    document.addEventListener('mousemove', onResize);
    document.addEventListener('mouseup', stopResize);
    e.preventDefault();
    e.stopPropagation();
};
const onResize = (e) => {
    if (!isResizing.value)
        return;
    const deltaX = e.clientX - startX;
    const deltaY = e.clientY - startY;
    const newWidth = Math.max(minWidth, Math.min(maxWidth, startWidth + deltaX));
    const newHeight = Math.max(minHeight, Math.min(maxHeight, startHeight + deltaY));
    chartWidth.value = newWidth;
    chartHeight.value = newHeight;
    requestAnimationFrame(() => {
        if (chartInstance) {
            chartInstance.resize();
        }
    });
};
const stopResize = () => {
    isResizing.value = false;
    document.removeEventListener('mousemove', onResize);
    document.removeEventListener('mouseup', stopResize);
};
watch(() => props.chart, () => {
    initChart();
}, { deep: true });
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
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("chart-renderer") }, ...{ class: (({ 'is-resizing': __VLS_ctx.isResizing })) }, ...{ style: (({ width: __VLS_ctx.chartWidth + 'px', height: __VLS_ctx.chartHeight + 'px' })) }, });
    __VLS_styleScopedClasses = ({ 'is-resizing': isResizing });
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ id: ((__VLS_ctx.chartId)), ...{ class: ("chart-container") }, });
    // @ts-ignore
    [isResizing, chartWidth, chartHeight, chartId,];
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ onMousedown: (__VLS_ctx.startResize) }, ...{ class: ("resize-handle") }, ...{ class: (({ 'is-resizing': __VLS_ctx.isResizing })) }, });
    __VLS_styleScopedClasses = ({ 'is-resizing': isResizing });
    __VLS_elementAsFunction(__VLS_intrinsicElements.svg, __VLS_intrinsicElements.svg)({ ...{ class: ("resize-icon") }, viewBox: ("0 0 24 24"), fill: ("none"), stroke: ("currentColor"), "stroke-width": ("2"), });
    __VLS_elementAsFunction(__VLS_intrinsicElements.polyline)({ points: ("15 3 21 3 21 9"), });
    // @ts-ignore
    [isResizing, startResize,];
    __VLS_elementAsFunction(__VLS_intrinsicElements.polyline)({ points: ("9 21 3 21 3 15"), });
    __VLS_elementAsFunction(__VLS_intrinsicElements.line)({ x1: ("21"), y1: ("3"), x2: ("14"), y2: ("10"), });
    __VLS_elementAsFunction(__VLS_intrinsicElements.line)({ x1: ("3"), y1: ("21"), x2: ("10"), y2: ("14"), });
    if (typeof __VLS_styleScopedClasses === 'object' && !Array.isArray(__VLS_styleScopedClasses)) {
        __VLS_styleScopedClasses['chart-renderer'];
        __VLS_styleScopedClasses['chart-container'];
        __VLS_styleScopedClasses['resize-handle'];
        __VLS_styleScopedClasses['resize-icon'];
    }
    var __VLS_slots;
    return __VLS_slots;
    const __VLS_componentsOption = {};
    let __VLS_name;
    let __VLS_defineComponent;
    const __VLS_internalComponent = __VLS_defineComponent({
        setup() {
            return {
                chartId: chartId,
                chartWidth: chartWidth,
                chartHeight: chartHeight,
                isResizing: isResizing,
                startResize: startResize,
            };
        },
        props: {},
    });
}
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
    props: {},
});
;
