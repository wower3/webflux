// 测试正则表达式

const json = '{"type":"chart","chartId":"chart_category","subtype":"bar","title":"本周各分类数量","data":{"分类1":127,"分类2":555,"分类3":238,"分类4":700,"分类5":450}}'

const chartPattern = /\{"type":"chart","chartId":"([^"]+)","subtype":"(line|bar|pie)","title":"([^"]+)","data":(\{(?:[^{}]|\{[^}]*\})*\})\}/g

let match = chartPattern.exec(json)
console.log('First match:', match)

if (match) {
  console.log('Chart ID:', match[1])
  console.log('Subtype:', match[2])
  console.log('Title:', match[3])
  console.log('Data:', match[4])
} else {
  console.log('No match!')
}

// 测试完整JSON字符串
console.log('\n--- Testing with full content ---')
const fullContent = `好的，这是第一个图表：{"type":"chart","chartId":"chart_time","subtype":"line","title":"数量随时间变化","data":{"时间1":224,"时间2":268,"时间3":307,"时间4":221}}这是第二个图表：{"type":"chart","chartId":"chart_category","subtype":"bar","title":"本周各分类数量","data":{"分类1":127,"分类2":555,"分类3":238,"分类4":700,"分类5":450}}完成！`

match = chartPattern.exec(fullContent)
console.log('First match in full content:', match?.[1])

match = chartPattern.exec(fullContent)
console.log('Second match in full content:', match?.[1])
