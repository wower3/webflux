function hasNegativeValues(data) {
  return data.some(v => v < 0)
}

export function toEChartsOption(chart) {
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
        label: {
          show: true,
          position: 'top',
          formatter: (params) => (params.value != null ? String(params.value) : '') || ''
        },
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
          itemStyle: {
            color: value >= 0 ? '#667eea' : '#f56565'
          }
        })),
        type: 'bar',
        barMaxWidth: 45,
        label: {
          show: true,
          position: (params) => {
            return (params.value != null ? params.value : 0) >= 0 ? 'top' : 'bottom'
          },
          formatter: (params) => (params.value != null ? String(params.value) : '') || ''
        }
      }]
    }
  }

  if (chart.type === 'pie') {
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
