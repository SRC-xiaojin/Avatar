<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :before-close="handleClose"
    :class="['draggable-dialog']"
    draggable="true"
    append-to-body
    destroy-on-close
  >
    <!-- 标题栏插槽 -->
    <template #header="{ titleId, titleClass }">
      <div
        :id="titleId"
        :class="titleClass"
        class="draggable-header"
      >
        <span class="dialog-title">{{ title }}</span>
        <div class="header-actions">
          <slot name="header-actions"></slot>
        </div>
      </div>
    </template>

    <!-- 内容插槽 -->
    <div class="dialog-content">
      <slot></slot>
    </div>

    <!-- 底部插槽 -->
    <template #footer v-if="$slots.footer">
      <slot name="footer"></slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

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


const handleClose = (done: () => void) => {
  if (props.beforeClose) {
    props.beforeClose(done)
  } else {
    done()
    emit('close')
  }
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