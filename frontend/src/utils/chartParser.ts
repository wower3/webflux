// 图表数据解析工具

import type { ChartData } from '@/types'

/**
 * 检查数据中是否包含负值
 */
function hasNegativeValues(data: number[]): boolean {
  return data.some(v => v < 0)
}

/**
 * 转换图表数据为ECharts配置
 * 支持正负值显示
 */
export function toEChartsOption(chart: ChartData) {
  const xAxisData = Object.keys(chart.data)
  const seriesData = Object.values(chart.data)
  const hasNegative = hasNegativeValues(seriesData)

  const baseOption = {
    title: {
      text: chart.title,
      left: 'center',
      top: 10,
      textStyle: { fontSize: 15, fontWeight: 'normal' }
    },
    tooltip: {
      trigger: chart.type === 'pie' ? 'item' : 'axis',
      axisPointer: chart.type === 'bar' ? { type: 'shadow' } : undefined
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '5%',
      containLabel: true
    }
  }

  if (chart.type === 'line') {
    return {
      ...baseOption,
      xAxis: {
        type: 'category',
        data: xAxisData,
        axisLine: { lineStyle: { color: '#ccc' } },
        axisLabel: { color: '#333' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: '#eee' } },
        axisLine: { show: false },
        axisLabel: { color: '#333' }
      },
      series: [{
        data: seriesData,
        type: 'line',
        smooth: true,
        symbolSize: 8,
        itemStyle: { color: '#10a37f' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16, 163, 127, 0.3)' },
              { offset: 1, color: 'rgba(16, 163, 127, 0.05)' }
            ]
          }
        },
        // 标签位置根据数值正负自动调整
        label: {
          show: true,
          position: hasNegative ? 'top' : 'top',
          formatter: (params: any) => params.value?.toString() || ''
        },
        // 标记线：零线
        markLine: hasNegative ? {
          data: [{ yAxis: 0 }],
          lineStyle: { color: '#999', type: 'solid', width: 1 },
          symbol: 'none'
        } : undefined
      }]
    }
  }

  if (chart.type === 'bar') {
    return {
      ...baseOption,
      xAxis: {
        type: 'category',
        data: xAxisData,
        axisLine: { lineStyle: { color: '#ccc' } },
        axisLabel: { color: '#333' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: '#eee' } },
        axisLine: { show: false },
        axisLabel: { color: '#333' }
      },
      series: [{
        data: seriesData.map((value) => ({
          value,
          // 正值用一种颜色，负值用另一种颜色
          itemStyle: {
            color: value >= 0 ? '#667eea' : '#f56565'
          }
        })),
        type: 'bar',
        barMaxWidth: 45,
        // 标签位置根据正负自动调整
        label: {
          show: true,
          position: (params: any) => {
            return params.value >= 0 ? 'top' : 'bottom'
          },
          formatter: (params: any) => params.value?.toString() || ''
        }
      }]
    }
  }

  if (chart.type === 'pie') {
    // 饼图不适合显示负值，取绝对值
    return {
      ...baseOption,
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: xAxisData.map((name, i) => ({
          name,
          value: Math.abs(seriesData[i])
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }
  }

  return baseOption
}

// 保留旧的extractCharts函数以兼容（虽然现在主要由streamJsonParser处理）
export function extractCharts(text: string): { charts: ChartData[], cleanText: string } {
  const charts: ChartData[] = []
  const chartPattern = /\{"type":"chart"[^}]*"subtype":"(line|bar|pie|scatter)"[^}]*"title":"([^"]+)"[^}]*"data":\{[^}]+\}[^}]*\}/g

  let cleanText = text
  let match: RegExpExecArray | null

  while ((match = chartPattern.exec(text)) !== null) {
    const chartJson = match[0]
    try {
      const chartData = JSON.parse(chartJson)
      charts.push({
        id: chartData.chartId || `chart_${charts.length}`,
        type: chartData.subtype,
        title: chartData.title,
        data: chartData.data
      })
      cleanText = cleanText.replace(chartJson, `[图表: ${chartData.title}]`)
    } catch (e) {
      console.error('Failed to parse chart:', e)
    }
  }

  return { charts, cleanText }
}
