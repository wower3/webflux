<template>
  <div
    class="chart-renderer"
    :class="{ 'is-resizing': isResizing }"
    :style="{ width: chartWidth + 'px', height: chartHeight + 'px' }"
  >
    <div :id="chartId" class="chart-container"></div>
    <!-- 拖拽手柄 -->
    <div
      class="resize-handle"
      @mousedown="startResize"
      :class="{ 'is-resizing': isResizing }"
    >
      <svg class="resize-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="15 3 21 3 21 9"/>
        <polyline points="9 21 3 21 3 15"/>
        <line x1="21" y1="3" x2="14" y2="10"/>
        <line x1="3" y1="21" x2="10" y2="14"/>
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import type { ChartData } from '../types'
import { toEChartsOption } from '../utils/chartParser'

const props = defineProps<{
  chart: ChartData
}>()

const chartId = `chart-${props.chart.id}`
let chartInstance: echarts.ECharts | null = null

const chartWidth = ref(600)
const chartHeight = ref(350)
const isResizing = ref(false)

let startX = 0
let startY = 0
let startWidth = 0
let startHeight = 0

const minWidth = 300
const minHeight = 200
const maxWidth = 1200
const maxHeight = 800

const initChart = () => {
  const container = document.getElementById(chartId)
  if (!container) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(container)
  const option = toEChartsOption(props.chart)
  chartInstance.setOption(option)
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
})

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

const startResize = (e: MouseEvent) => {
  isResizing.value = true
  startX = e.clientX
  startY = e.clientY
  startWidth = chartWidth.value
  startHeight = chartHeight.value

  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault()
  e.stopPropagation()
}

const onResize = (e: MouseEvent) => {
  if (!isResizing.value) return

  const deltaX = e.clientX - startX
  const deltaY = e.clientY - startY

  const newWidth = Math.max(minWidth, Math.min(maxWidth, startWidth + deltaX))
  const newHeight = Math.max(minHeight, Math.min(maxHeight, startHeight + deltaY))

  chartWidth.value = newWidth
  chartHeight.value = newHeight

  requestAnimationFrame(() => {
    if (chartInstance) {
      chartInstance.resize()
    }
  })
}

const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

watch(() => props.chart, () => {
  initChart()
}, { deep: true })
</script>

<style scoped>
.chart-renderer {
  position: relative;
  margin: 16px auto;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.chart-renderer.is-resizing {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.5);
}

.chart-container {
  width: 100%;
  height: 100%;
}

.resize-handle {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 20px;
  height: 20px;
  cursor: nwse-resize;
  background: linear-gradient(135deg, transparent 50%, rgba(102, 126, 234, 0.5) 50%);
  border-radius: 0 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.resize-handle:hover {
  background: linear-gradient(135deg, transparent 50%, rgba(102, 126, 234, 0.8) 50%);
}

.resize-handle.is-resizing {
  background: linear-gradient(135deg, transparent 50%, #667eea 50%);
}

.resize-icon {
  width: 10px;
  height: 10px;
  color: #fff;
  opacity: 0.8;
}

.resize-handle:hover .resize-icon,
.resize-handle.is-resizing .resize-icon {
  opacity: 1;
}
</style>
