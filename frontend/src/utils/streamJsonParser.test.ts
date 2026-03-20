/**
 * StreamJsonParser 单元测试
 */
import { createStreamJsonParser } from './streamJsonParser'

console.log('=== StreamJsonParser 测试 ===\n')

const parser = createStreamJsonParser()

// 模拟后端流式发送（JSON被拆分）
const chunks = [
  '好的，这是图表：\n\n',
  '{"type":"chart","chartId":"chart_1","subtype":"line",',
  '"title":"测试图表","data":{',
  '"A":100,',
  '"B":200',
  '}}',
  '\n\n完成！'
]

console.log('模拟流式输入...\n')
chunks.forEach((chunk, i) => {
  console.log(`[chunk ${i + 1}] ${JSON.stringify(chunk)}`)

  const result = parser.append(chunk)

  console.log(`  → content: "${result.content}"`)
  console.log(`  → charts: ${result.charts.length}`)
  if (result.charts.length > 0) {
    result.charts.forEach((c: any) => {
      console.log(`     ${c.title} (${c.type}):`, c.data)
    })
  }
  console.log(`  → hasPending: ${result.hasPending}`)
  console.log()
})

console.log('=== 测试完成 ===')
console.log('最终内容:', parser.getContent())
console.log('最终图表:', parser.getCharts())
