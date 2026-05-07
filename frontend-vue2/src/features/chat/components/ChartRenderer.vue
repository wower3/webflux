<template>
  <div
    class="chart-renderer"
    :class="{ 'is-resizing': isResizing }"
    :style="{ width: chartWidth + 'px', height: chartHeight + 'px' }"
  >
    <div :id="chartId" class="chart-container"></div>
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

<script>
import * as echarts from 'echarts'
import { toEChartsOption } from '../utils/chartParser'

export default {
  name: 'ChartRenderer',
  props: {
    chart: { type: Object, required: true }
  },
  data: function () {
    return {
      chartId: 'chart-' + this.chart.id,
      chartWidth: 600,
      chartHeight: 350,
      isResizing: false,
      startX: 0,
      startY: 0,
      startWidth: 0,
      startHeight: 0
    }
  },
  created: function () {
    this.chartInstance = null
  },
  mounted: function () {
    this.initChart()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy: function () {
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
    window.removeEventListener('resize', this.handleResize)
    document.removeEventListener('mousemove', this.onResize)
    document.removeEventListener('mouseup', this.stopResize)
  },
  watch: {
    chart: {
      handler: function () {
        this.initChart()
      },
      deep: true
    }
  },
  methods: {
    initChart: function () {
      var container = document.getElementById(this.chartId)
      if (!container) return

      if (this.chartInstance) {
        this.chartInstance.dispose()
      }

      this.chartInstance = echarts.init(container)
      var option = toEChartsOption(this.chart)
      this.chartInstance.setOption(option)
    },
    handleResize: function () {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    },
    startResize: function (e) {
      this.isResizing = true
      this.startX = e.clientX
      this.startY = e.clientY
      this.startWidth = this.chartWidth
      this.startHeight = this.chartHeight

      document.addEventListener('mousemove', this.onResize)
      document.addEventListener('mouseup', this.stopResize)
      e.preventDefault()
      e.stopPropagation()
    },
    onResize: function (e) {
      if (!this.isResizing) return

      var deltaX = e.clientX - this.startX
      var deltaY = e.clientY - this.startY

      var newWidth = Math.max(300, Math.min(1200, this.startWidth + deltaX))
      var newHeight = Math.max(200, Math.min(800, this.startHeight + deltaY))

      this.chartWidth = newWidth
      this.chartHeight = newHeight

      var self = this
      requestAnimationFrame(function () {
        if (self.chartInstance) {
          self.chartInstance.resize()
        }
      })
    },
    stopResize: function () {
      this.isResizing = false
      document.removeEventListener('mousemove', this.onResize)
      document.removeEventListener('mouseup', this.stopResize)
    }
  }
}
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
