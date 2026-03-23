<template>
  <div class="card-container">
    <div class="card-header">
      <h3>{{ card.displayTitle }}</h3>
    </div>
    <div class="card-body">
      <div v-for="item in card.cardInfo" :key="item.key" class="card-row">
        <label class="card-label">{{ item.label }}:</label>
        <span v-if="!isEditing" class="card-value">{{ item.value }}</span>
        <input
          v-else
          v-model="editValues[item.key]"
          class="card-input"
          type="text"
        />
      </div>
    </div>
    <!-- 编辑模式：显示确认/取消按钮用于保存内容 -->
    <div v-if="isEditing" class="card-footer">
      <button @click="saveEdit" class="card-btn btn-primary">确认</button>
      <button @click="cancelEdit" class="card-btn btn-secondary">取消</button>
    </div>
    <!-- 非编辑模式：显示原始按钮 -->
    <div v-else class="card-footer">
      <button
        v-for="btn in card.buttons"
        :key="btn.actionId"
        @click="handleAction(btn)"
        :class="getButtonClass(btn.actionId)"
        class="card-btn"
      >
        {{ btn.label }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { CardData, CardButton } from '@/types'

const props = defineProps<{
  card: CardData
}>()

const emit = defineEmits<{
  remove: [cardId: string]
}>()

const isEditing = ref(false)
const editValues = ref<Record<string, string>>({})

const getButtonClass = (actionId: string) => {
  switch (actionId) {
    case 'confirm':
      return 'btn-primary'
    case 'cancel':
      return 'btn-secondary'
    case 'edit':
      return 'btn-edit'
    default:
      return ''
  }
}

// 保存编辑内容
const saveEdit = () => {
  const updatedCard = {
    ...props.card,
    cardInfo: props.card.cardInfo.map(item => ({
      ...item,
      value: editValues.value[item.key] || item.value
    }))
  }
  console.log('[CardRenderer] 保存编辑:', updatedCard)
  // TODO: 更新卡片数据
  isEditing.value = false
}

// 取消编辑
const cancelEdit = () => {
  editValues.value = {}
  isEditing.value = false
}

const handleAction = (btn: CardButton) => {
  switch (btn.actionId) {
    case 'edit':
      // 进入编辑模式，初始化编辑值
      editValues.value = {}
      props.card.cardInfo.forEach(item => {
        editValues.value[item.key] = item.value
      })
      isEditing.value = true
      break

    case 'confirm':
      // 确定操作：向后端发送请求
      console.log('[CardRenderer] 发送请求到:', btn.apiEndpoint)
      console.log('[CardRenderer] 请求数据:', props.card)
      // TODO: 发送 API 请求
      break

    case 'cancel':
      // 取消操作：移除整个卡片
      console.log('[CardRenderer] 移除卡片:', props.card.cardId)
      emit('remove', props.card.cardId)
      break

    default:
      console.log('[CardRenderer] 未知操作:', btn.actionId)
  }
}
</script>

<style scoped>
.card-container {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  margin: 12px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 12px 16px;
}

.card-header h3 {
  margin: 0;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
}

.card-body {
  padding: 16px;
}

.card-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.card-row:last-child {
  border-bottom: none;
}

.card-label {
  font-weight: 500;
  color: #374151;
  min-width: 120px;
  font-size: 14px;
}

.card-value {
  color: #6b7280;
  font-size: 14px;
  flex: 1;
}

.card-input {
  flex: 1;
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.card-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.card-footer {
  padding: 12px 16px;
  background: #f9fafb;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.card-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #667eea;
  color: #ffffff;
}

.btn-primary:hover {
  background: #5a67d8;
}

.btn-secondary {
  background: #e5e7eb;
  color: #374151;
}

.btn-secondary:hover {
  background: #d1d5db;
}

.btn-edit {
  background: #10b981;
  color: #ffffff;
}

.btn-edit:hover {
  background: #059669;
}
</style>
