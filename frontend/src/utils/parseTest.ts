// 测试图表解析
const testContent = `好的，这是折线图：
{"type":"chart","chartId":"chart_time","subtype":"line","title":"数量随时间变化","data":{"时间1":224,"时间2":268,"时间3":307,"时间4":221}}
这是柱状图：
{"type":"chart","chartId":"chart_category","subtype":"bar","title":"本周各分类数量","data":{"分类1":127,"分类2":555,"分类3":238,"分类4":700,"分类5":450}}
完成！`

// 查找所有 {"type":"chart" 的位置
const chartStart = '{"type":"chart"'
let startPos = testContent.indexOf(chartStart)

console.log('=== 测试解析 ===')
console.log('Content:', testContent.substring(0, 100) + '...')

let chartCount = 0

while (startPos !== -1) {
  console.log(`\n找到图表 #${++chartCount} 位置:`, startPos)

  // 从起始位置开始，找到匹配的闭合花括号
  let braceDepth = 0
  let pos = startPos

  while (pos < testContent.length) {
    if (testContent[pos] === '{') braceDepth++
    else if (testContent[pos] === '}') braceDepth--

    if (braceDepth === 0 && pos > startPos) {
      const jsonStr = testContent.slice(startPos, pos + 1)
      console.log(`  提取JSON:`, jsonStr.substring(0, 60) + '...')

      try {
        const data = JSON.parse(jsonStr)
        console.log(`  解析成功: type=${data.type}, subtype=${data.subtype}, chartId=${data.chartId}`)
      } catch (e) {
        console.log(`  解析失败:`, e.message)
      }
      break
    }
    pos++
  }

  startPos = testContent.indexOf(chartStart, pos + 1)
}

console.log('\n=== 测试完成 ===')
