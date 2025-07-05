<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :before-close="handleClose"
    :class="['draggable-dialog', { 'is-dragging': isDragging }]"
    append-to-body
    destroy-on-close
  >
    <template #header="{ titleId, titleClass }">
      <div
        :id="titleId"
        :class="titleClass"
        class="draggable-header"
        @mousedown="handleMouseDown"
      >
        <span class="dialog-title">{{ title }}</span>
        <div class="header-actions">
          <slot name="header-actions"></slot>
        </div>
      </div>
    </template>

    <div class="dialog-content">
      <slot></slot>
    </div>

    <template #footer v-if="$slots.footer">
      <slot name="footer"></slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'

interface Props {
  modelValue: boolean
  title: string
  width: string
  beforeClose?: ((done: () => void) => void) | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  title: '对话框',
  width: '500px',
  beforeClose: null
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const isDragging = ref(false)
let startX = 0
let startY = 0
let dialogElement: HTMLElement | null = null

const handleClose = (done: () => void) => {
  if (props.beforeClose) {
    props.beforeClose(done)
  } else {
    done()
    emit('close')
  }
}

const handleMouseDown = (e: MouseEvent) => {
  // 只有点击标题栏时才启用拖拽
  if ((e.target as Element)?.closest('.header-actions')) return

  isDragging.value = true
  startX = e.clientX
  startY = e.clientY

  // 获取对话框元素
  nextTick(() => {
    const dialogWrapper = document.querySelector('.draggable-dialog .el-dialog') as HTMLElement
    if (dialogWrapper) {
      dialogElement = dialogWrapper
      
      // 设置初始transform如果没有的话
      if (!dialogElement.style.transform) {
        dialogElement.style.transform = 'translate(0px, 0px)'
      }
      
      // 添加拖拽样式
      dialogElement.style.cursor = 'move'
      dialogElement.style.userSelect = 'none'
      
      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
    }
  })

  e.preventDefault()
}

const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging.value || !dialogElement) return

  const deltaX = e.clientX - startX
  const deltaY = e.clientY - startY

  // 获取当前的transform值
  const currentTransform = dialogElement.style.transform
  const match = currentTransform.match(/translate\((-?\d+)px,\s*(-?\d+)px\)/)
  
  let currentX = 0
  let currentY = 0
  if (match) {
    currentX = parseInt(match[1])
    currentY = parseInt(match[2])
  }

  const newX = currentX + deltaX
  const newY = currentY + deltaY

  // 限制拖拽范围，防止拖出屏幕
  const maxX = (window.innerWidth - dialogElement.offsetWidth) / 2
  const maxY = (window.innerHeight - dialogElement.offsetHeight) / 2
  const minX = -maxX
  const minY = -maxY

  const constrainedX = Math.max(minX, Math.min(maxX, newX))
  const constrainedY = Math.max(minY, Math.min(maxY, newY))

  dialogElement.style.transform = `translate(${constrainedX}px, ${constrainedY}px)`

  startX = e.clientX
  startY = e.clientY
}

const handleMouseUp = () => {
  isDragging.value = false
  
  if (dialogElement) {
    dialogElement.style.cursor = ''
    dialogElement.style.userSelect = ''
  }

  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
}
</script>

<style scoped>
.draggable-dialog .draggable-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: move;
  user-select: none;
  padding: 0;
  margin: 0;
}

.draggable-dialog .dialog-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.draggable-dialog .header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.draggable-dialog.is-dragging .el-dialog {
  transition: none !important;
}

.draggable-dialog .dialog-content {
  max-height: 70vh;
  overflow-y: auto;
}

/* 美化滚动条 */
.dialog-content::-webkit-scrollbar {
  width: 6px;
}

.dialog-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 