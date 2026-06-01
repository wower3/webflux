<template>
  <div class="guided-prompts">
    <div class="history-hint">
      <i class="fa fa-info-circle"></i>
      <span>当前对话保留最近5轮历史上下文</span>
    </div>
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

        <transition name="expand">
          <div v-if="activeIndex === i" class="example-popover" @click.stop>
            <div v-for="(ex, j) in cat.examples" :key="j" class="example-item">
              <span class="example-text">{{ ex }}</span>
              <button
                class="copy-btn"
                :class="{ copied: copiedIndex === (i + '-' + j) }"
                @click="copy(ex, i + '-' + j)"
              >
                <i :class="copiedIndex === (i + '-' + j) ? 'fa fa-check' : 'fa fa-copy'"></i>
              </button>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>
var CATEGORIES = [
  {
    name: '查询统计数据',
    icon: 'fa fa-table',
    examples: [
      '查询2025年1月至3月XX机构XX渠道的分类数据（可选是否进行环比）',
      '查询2025年第一季度XX分类XX渠道的机构统计数据（可选是否进行环比）',
      '查询2025年5月XX机构的渠道统计数据（可选是否进行环比）',
    ]
  },
  {
    name: '根据统计数据作图',
    icon: 'fa fa-chart-line',
    examples: [
      '（这里填写你查询到的统计数据），根据上述统计数据作图',
    ]
  },
  {
    name: '查询投诉详情',
    icon: 'fa fa-clipboard-list',
    examples: [
      '请查询XX（手机号或投诉人姓名）的投诉详情',
    ]
  }
]

export default {
  name: 'GuidedPrompts',
  data: function () {
    return {
      activeIndex: null,
      copiedIndex: null,
      CATEGORIES: CATEGORIES
    }
  },
  mounted: function () {
    document.addEventListener('click', this.close)
  },
  beforeDestroy: function () {
    document.removeEventListener('click', this.close)
  },
  methods: {
    toggle: function (i) {
      this.activeIndex = this.activeIndex === i ? null : i
    },
    close: function () {
      this.activeIndex = null
    },
    copy: function (text, key) {
      var self = this
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).catch(function () {
          self.fallbackCopy(text)
        })
      } else {
        self.fallbackCopy(text)
      }
      self.copiedIndex = key
      setTimeout(function () { self.copiedIndex = null }, 1500)
    },
    fallbackCopy: function (text) {
      var ta = document.createElement('textarea')
      ta.value = text
      document.body.appendChild(ta)
      ta.select()
      document.execCommand('copy')
      document.body.removeChild(ta)
    }
  }
}
</script>

<style scoped>
.guided-prompts {
  padding: 0 20px 8px;
  border-top: 1px solid #f3f4f6;
}

.history-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
  padding: 6px 0 2px;
}

.history-hint i {
  font-size: 12px;
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
.expand-enter, .expand-leave-to {
  opacity: 0;
  transform: scaleY(0.9);
}
</style>
