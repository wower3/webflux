<template>
  <div class="chart-wrapper" ref="chartRef"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { ChartData } from '@/types'
import { toEChartsOption } from '@/utils/chartParser'

const props = defineProps<{
  chart: ChartData
}>()

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

// 使用具名函数以便正确移除监听器
const resizeChart = () => {
  chartInstance?.resize()
}

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  const option = toEChartsOption(props.chart)
  chartInstance.setOption(option)
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  chartInstance?.dispose()
  window.removeEventListener('resize', resizeChart)
})

watch(() => props.chart, () => {
  if (chartInstance) {
    const option = toEChartsOption(props.chart)
    chartInstance.setOption(option, true)
  }
}, { deep: true })
</script>

<style scoped>
.chart-wrapper {
  width: 100%;
  height: 400px;
  margin: 20px 0;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  padding: 10px;
}
</style>
