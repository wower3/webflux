/**
 * ECharts 图表配置转换器
 * 将内部图表数据格式转换为 ECharts 配置
 */
import type { ChartData } from '@/types'
import * as echarts from 'echarts'

export function parseChartOption(chart: ChartData): echarts.EChartsOption {
  const { type, title, data } = chart

  // 提取数据
  const xAxisData = Object.keys(data)
  const seriesData = Object.values(data)

  // 基础配置
  const baseOption: echarts.EChartsOption = {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      bottom: 10
    }
  }

  // 根据图表类型生成配置
  switch (type) {
    case 'line':
      return {
        ...baseOption,
        xAxis: {
          type: 'category',
          data: xAxisData
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: title,
          type: 'line',
          data: seriesData,
          smooth: true
        }]
      }

    case 'bar':
      return {
        ...baseOption,
        xAxis: {
          type: 'category',
          data: xAxisData
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: title,
          type: 'bar',
          data: seriesData
        }]
      }

    case 'pie':
      return {
        ...baseOption,
        tooltip: {
          trigger: 'item'
        },
        series: [{
          name: title,
          type: 'pie',
          radius: '70%',
          data: xAxisData.map((key, index) => ({
            name: key,
            value: seriesData[index]
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

    case 'scatter':
      return {
        ...baseOption,
        xAxis: {
          type: 'category',
          data: xAxisData
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: title,
          type: 'scatter',
          data: seriesData
        }]
      }

    default:
      return baseOption
  }
}
