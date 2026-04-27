<template>
  <div class="guided-prompts">
    <div class="category-list">
      <div
        v-for="(cat, i) in CATEGORIES"
        :key="i"
        class="category-card"
        :class="{ active: activeIndex === i }"
        @click.stop="toggle(i)"
      >
        <i :class="cat.icon"></i>
        <span>{{ cat.name }}</span>

        <Transition name="expand">
          <div v-if="activeIndex === i" class="example-popover" @click.stop>
            <div v-for="(ex, j) in cat.examples" :key="j" class="example-item">
              <span class="example-text">{{ ex }}</span>
              <button
                class="copy-btn"
                :class="{ copied: copiedIndex === `${i}-${j}` }"
                @click="copy(ex, `${i}-${j}`)"
              >
                <i :class="copiedIndex === `${i}-${j}` ? 'fa fa-check' : 'fa fa-copy'"></i>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const activeIndex = ref<number | null>(null)
const copiedIndex = ref<string | null>(null)

const toggle = (i: number) => {
  activeIndex.value = activeIndex.value === i ? null : i
}

const close = () => {
  activeIndex.value = null
}

onMounted(() => {
  document.addEventListener('click', close)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', close)
})

const copy = async (text: string, key: string) => {
  try {
    await navigator.clipboard.writeText(text)
  } catch {
    const ta = document.createElement('textarea')
    ta.value = text
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
  }
  copiedIndex.value = key
  setTimeout(() => { copiedIndex.value = null }, 1500)
}

const CATEGORIES = [
  {
    name: '查询统计数据',
    icon: 'fa fa-table',
    examples: [
      '本月各区域的销售总额分别是多少？',
      '对比本季度与上季度的订单量变化',
      '统计各产品类别的退货率排名',
    ]
  },
  {
    name: '根据统计数据作图',
    icon: 'fa fa-chart-line',
    examples: [
      '画一张近12个月的销售额趋势折线图',
      '用柱状图展示各部门本季度业绩对比',
      '生成一个各区域占比的饼图',
    ]
  },
  {
    name: '查询投诉详情',
    icon: 'fa fa-clipboard-list',
    examples: [
      '查询最近7天的投诉记录及处理状态',
      '投诉编号C20240415003的处理进度如何？',
      '按投诉类型统计本月投诉数量分布',
    ]
  }
] as const
</script>

<style scoped>
.guided-prompts {
  padding: 0 20px 8px;
  border-top: 1px solid #f3f4f6;
}

.category-list {
  display: flex;
  gap: 8px;
}

.category-card {
  flex: 1;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #374151;
  transition: all 0.15s;
  position: relative;
  white-space: nowrap;
}

.category-card:hover {
  border-color: #667eea;
  background: #f0f0ff;
}

.category-card.active {
  border-color: #667eea;
  background: #eef2ff;
}

.category-card > i:first-child {
  color: #667eea;
  font-size: 13px;
}

.example-popover {
  position: absolute;
  left: 0;
  bottom: 100%;
  margin-bottom: 4px;
  min-width: 320px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.1);
  padding: 6px;
  z-index: 10;
}

.example-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  transition: background 0.12s;
}

.example-item:hover {
  background: #f3f4f6;
}

.example-text {
  flex: 1;
  font-size: 13px;
  color: #374151;
  line-height: 1.5;
  white-space: normal;
}

.copy-btn {
  width: 26px;
  height: 26px;
  border: 1px solid #d1d5db;
  border-radius: 5px;
  background: #fff;
  color: #9ca3af;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  flex-shrink: 0;
  transition: all 0.15s;
}

.copy-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.copy-btn.copied {
  border-color: #10b981;
  color: #10b981;
  background: #ecfdf5;
}

.expand-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
  transform-origin: bottom left;
}
.expand-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
  transform-origin: bottom left;
}
.expand-enter-from, .expand-leave-to {
  opacity: 0;
  transform: scaleY(0.9);
}
</style>
