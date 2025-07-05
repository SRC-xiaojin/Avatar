<template>
  <DraggableDialog
    v-model="visible"
    :title="editingCategory ? '编辑类别' : '新建类别'"
    width="500px"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="类别编码" prop="categoryCode">
        <el-input v-model="form.categoryCode" placeholder="请输入类别编码" />
      </el-form-item>
      <el-form-item label="类别名称" prop="categoryName">
        <el-input v-model="form.categoryName" placeholder="请输入类别名称" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入类别描述"
        />
      </el-form-item>
      <el-form-item label="图标">
        <el-input v-model="form.icon" placeholder="请输入图标名称" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sortOrder" :min="0" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" active-text="启用" inactive-text="禁用" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
    </template>
  </DraggableDialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, FormInstance } from 'element-plus'
import DraggableDialog from './DraggableDialog.vue'
import { categoryApi } from '@/api/categories.ts'
import type { OperatorCategory } from '@/types/api'

interface Props {
  modelValue: boolean
  editingCategory?: OperatorCategory | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'saved'): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  editingCategory: null
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const formRef = ref<FormInstance>()
const saving = ref(false)

interface CategoryForm {
  categoryCode: string
  categoryName: string
  description: string
  icon: string
  sortOrder: number
  status: boolean
}

const form = reactive<CategoryForm>({
  categoryCode: '',
  categoryName: '',
  description: '',
  icon: '',
  sortOrder: 0,
  status: true
})

const rules = {
  categoryCode: [
    { required: true, message: '请输入类别编码', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请输入类别名称', trigger: 'blur' }
  ]
}

// 先定义 resetForm 函数
const resetForm = () => {
  Object.assign(form, {
    categoryCode: '',
    categoryName: '',
    description: '',
    icon: '',
    sortOrder: 0,
    status: true
  })
  formRef.value?.resetFields()
}

// 监听编辑数据变化
watch(() => props.editingCategory, (newVal) => {
  if (newVal) {
    Object.assign(form, newVal)
  } else {
    resetForm()
  }
}, { immediate: true })

const handleClose = () => {
  visible.value = false
  resetForm()
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    
    let response
    if (props.editingCategory && props.editingCategory.id) {
      response = await categoryApi.updateCategory(props.editingCategory.id, form)
    } else {
      response = await categoryApi.createCategory(form)
    }
    
    if (response.success) {
      ElMessage.success(props.editingCategory ? '更新成功' : '创建成功')
      emit('saved')
      handleClose()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存类别失败:', error as Error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script> 