<template>
  <DraggableDialog
    v-model="visible"
    :title="editingTemplate ? '编辑模板' : '新建模板'"
    width="900px"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属类别" prop="categoryId">
            <el-select 
              v-model="form.categoryId" 
              placeholder="请选择类别" 
              style="width: 100%"
              :disabled="!!editingTemplate"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
            <div v-if="editingTemplate" class="field-hint">
              <span class="hint-text">编辑模式下不允许修改所属类别</span>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模板编码" prop="templateCode">
            <el-input 
              v-model="form.templateCode" 
              placeholder="请输入模板编码"
              :disabled="!!editingTemplate"
            />
            <div v-if="editingTemplate" class="field-hint">
              <span class="hint-text">编辑模式下不允许修改模板编码</span>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="模板名称" prop="templateName">
            <el-input v-model="form.templateName" placeholder="请输入模板名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="执行类">
            <el-input v-model="form.executorClass" placeholder="请输入执行类名" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="执行方法">
        <el-input v-model="form.executorMethod" placeholder="请输入执行方法" />
      </el-form-item>
      
      <el-form-item label="描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入模板描述"
        />
      </el-form-item>
      
      <el-form-item label="输入数据结构">
        <el-input
          v-model="form.inputSchema"
          type="textarea"
          :rows="5"
          placeholder="请输入JSON格式的输入数据结构定义"
          style="font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;"
        />
        <div class="schema-hint">
          <span class="hint-text">定义算子接收的输入数据结构和字段类型</span>
        </div>
      </el-form-item>
      
      <el-form-item label="输出数据结构">
        <el-input
          v-model="form.outputSchema"
          type="textarea"
          :rows="5"
          placeholder="请输入JSON格式的输出数据结构定义"
          style="font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;"
        />
        <div class="schema-hint">
          <span class="hint-text">定义算子返回的输出数据结构和字段类型</span>
        </div>
      </el-form-item>
      
      <el-form-item label="配置信息">
        <el-input
          v-model="form.configSchema"
          type="textarea"
          :rows="4"
          placeholder="请输入JSON格式的配置信息"
          style="font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;"
        />
        <div class="schema-hint">
          <span class="hint-text">定义算子的配置参数和选项</span>
        </div>
      </el-form-item>

      <!-- 参数定义部分 -->
      <el-form-item label="参数定义">
        <el-card class="params-card" shadow="never">
          <template #header>
            <div class="params-header">
              <span>参数列表</span>
              <el-button type="primary" size="small" @click="addParam">
                <el-icon><Plus /></el-icon>
                添加参数
              </el-button>
            </div>
          </template>

          <div v-if="templateParams.length === 0" class="no-params">
            <el-empty description="暂无参数定义">
              <el-button type="primary" size="small" @click="addParam">
                <el-icon><Plus /></el-icon>
                添加参数
              </el-button>
            </el-empty>
          </div>
          
          <div v-else class="param-list">
            <div 
              v-for="(param, index) in templateParams" 
              :key="param.tempId || param.id"
              class="param-item"
            >
              <el-card shadow="never" class="param-card">
                <template #header>
                  <div class="param-card-header">
                    <span>参数 {{ index + 1 }}</span>
                    <el-button 
                      type="danger" 
                      size="small" 
                      text
                      @click="removeParam(index)"
                    >
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </template>
                
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-form-item label="参数名称" label-width="80px">
                      <el-input v-model="param.paramName" placeholder="参数名称" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="参数键" label-width="80px">
                      <el-input v-model="param.paramKey" placeholder="参数键（唯一）" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="参数类别" label-width="80px">
                      <el-input v-model="param.paramCategory" placeholder="可选分类标签" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-form-item label="类型" label-width="80px">
                      <el-select v-model="param.paramType" placeholder="类型">
                        <el-option label="字符串" value="string" />
                        <el-option label="数字" value="number" />
                        <el-option label="布尔" value="boolean" />
                        <el-option label="对象" value="object" />
                        <el-option label="数组" value="array" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="必填" label-width="80px">
                      <el-switch v-model="param.isRequired" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="验证规则" label-width="80px">
                      <el-input v-model="param.validationRules" placeholder="验证规则" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="24">
                    <el-form-item label="默认值" label-width="80px">
                      <MonacoEditor
                        v-model="param.defaultValue"
                        :title="`${param.paramName || '参数'} - 默认值`"
                        :language="getEditorLanguage(param.paramType)"
                        height="150px"
                        width="100%"
                        :placeholder="`请输入${param.paramType || 'string'}类型的默认值`"
                        :show-toolbar="true"
                        :show-status="false"
                        class="param-default-value-editor"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-card>
            </div>
          </div>
        </el-card>
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
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import DraggableDialog from './DraggableDialog.vue'
import MonacoEditor from './MonacoEditor.vue'
import { templateApi } from '@/api/templates.ts'
import { paramApi } from '@/api/params.ts'
import type { OperatorTemplate, OperatorCategory, TemplateParam } from '@/types/api'

interface Props {
  modelValue: boolean
  editingTemplate?: OperatorTemplate | null
  categories: OperatorCategory[]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  editingTemplate: null,
  categories: () => []
})

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'saved'): void
}

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const formRef = ref<FormInstance>()
const saving = ref(false)

interface FormData {
  categoryId: number | string
  templateCode: string
  templateName: string
  description: string
  executorClass: string
  executorMethod: string
  configSchema: string
  inputSchema: string
  outputSchema: string
  status: boolean
}

const form = reactive<FormData>({
  categoryId: 0,
  templateCode: '',
  templateName: '',
  description: '',
  executorClass: '',
  executorMethod: '',
  configSchema: '{}',
  inputSchema: '{}',
  outputSchema: '{}',
  status: true
})

// 保存初始值，用于取消时恢复
const initialFormData = ref<Partial<FormData>>({})

// 参数相关变量
const templateParams = ref<(TemplateParam & { tempId?: string })[]>([]) // 参数列表
const initialParams = ref<(TemplateParam & { tempId?: string })[]>([]) // 保存初始参数，用于取消时恢复
let tempParamId = 1 // 临时ID计数器

const rules = {
  categoryId: [
    { required: true, message: '请选择所属类别', trigger: 'change' }
  ],
  templateCode: [
    { required: true, message: '请输入模板编码', trigger: 'blur' }
  ],
  templateName: [
    { required: true, message: '请输入模板名称', trigger: 'blur' }
  ]
}

// 重置为空表单（新建模式）
const resetForm = () => {
  Object.assign(form, {
    categoryId: '',
    templateCode: '',
    templateName: '',
    description: '',
    executorClass: '',
    executorMethod: '',
    configSchema: '{}',
    inputSchema: '{}',
    outputSchema: '{}',
    status: true
  })
  formRef.value?.resetFields()
}

// 恢复到初始值（编辑模式取消时使用）
const restoreInitialValues = () => {
  if (initialFormData.value && Object.keys(initialFormData.value).length > 0) {
    Object.assign(form, initialFormData.value)
    formRef.value?.clearValidate()
  }
  
  // 恢复参数
  if (initialParams.value && initialParams.value.length > 0) {
    templateParams.value = JSON.parse(JSON.stringify(initialParams.value))
  } else {
    templateParams.value = []
  }
}

// 加载模板参数
const loadTemplateParams = async (templateId: number) => {
  try {
    const response = await paramApi.getParamsByTemplateId(templateId)
    if (response.success) {
      const params = response.data || []
      
      templateParams.value = params
      
      // 保存初始参数状态
      initialParams.value = JSON.parse(JSON.stringify(params))
    }
  } catch (error) {
    console.error('加载模板参数失败:', error)
    ElMessage.error('加载模板参数失败')
  }
}

// 添加参数
const addParam = () => {
  const newParam: TemplateParam & { tempId: string } = {
    tempId: `temp_${tempParamId++}`,
    paramName: '',
    paramKey: '',
    paramType: 'string',
    paramCategory: '',
    isRequired: false,
    defaultValue: '',
    validationRules: ''
  }
  
  templateParams.value.push(newParam)
}

// 删除参数
const removeParam = async (index: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个参数吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    templateParams.value.splice(index, 1)
    ElMessage.success('参数已删除')
  } catch (error) {
    // 用户取消删除
  }
}

// 获取所有参数列表
const getAllParams = () => {
  return templateParams.value.filter(param => param.paramName && param.paramKey)
}

// 根据参数类型获取编辑器语言
const getEditorLanguage = (paramType: string) => {
  switch (paramType) {
    case 'object':
    case 'array':
      return 'json'
    case 'string':
      return 'plaintext'
    case 'number':
    case 'boolean':
      return 'javascript'
    default:
      return 'json'
  }
}

// 监听编辑数据变化
watch(() => props.editingTemplate, async (newVal) => {
  if (newVal) {
    const formattedData = {
      ...newVal,
      configSchema: JSON.stringify(JSON.parse(newVal.configSchema || '{}'), null, 2),
      inputSchema: JSON.stringify(JSON.parse(newVal.inputSchema || '{}'), null, 2),
      outputSchema: JSON.stringify(JSON.parse(newVal.outputSchema || '{}'), null, 2)
    }
    Object.assign(form, formattedData)
    // 保存初始值
    initialFormData.value = { ...formattedData }
    
    // 加载参数数据
    if (newVal.id) {
      await loadTemplateParams(newVal.id)
    }
  } else {
    resetForm()
    initialFormData.value = {}
    // 清空参数
    templateParams.value = []
    initialParams.value = []
  }
}, { immediate: true })

const handleClose = () => {
  visible.value = false
  // 如果是编辑模式，恢复到初始值；如果是新建模式，清空表单
  if (props.editingTemplate) {
    restoreInitialValues()
  } else {
    resetForm()
  }
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    
    let templateResponse
    if (props.editingTemplate) {
      templateResponse = await templateApi.updateTemplate(props.editingTemplate.id, form)
    } else {
      templateResponse = await templateApi.createTemplate(form)
    }
    
    if (templateResponse.success) {
      const templateId = props.editingTemplate ? props.editingTemplate.id : templateResponse.data.id
      
      // 保存参数数据
      try {
        const allParams = getAllParams()
        if (allParams.length > 0) {
          const paramResponse = await paramApi.batchSaveParams(templateId, allParams)
          if (!paramResponse.success) {
            ElMessage.warning('模板保存成功，但参数保存失败：' + paramResponse.message)
          }
        } else {
          // 如果没有参数，清空该模板的所有参数
          await paramApi.batchSaveParams(templateId, [])
        }
      } catch (paramError) {
        console.error('保存参数失败:', paramError)
        ElMessage.warning('模板保存成功，但参数保存失败')
      }
      
      ElMessage.success(props.editingTemplate ? '更新成功' : '创建成功')
      emit('saved')
      handleClose()
    } else {
      ElMessage.error(templateResponse.message || '保存失败')
    }
  } catch (error) {
    console.error('保存模板失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
/* Schema 提示样式 */
.schema-hint {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-left: 3px solid #409eff;
  border-radius: 4px;
}

.hint-text {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
}

/* 字段禁用提示样式 */
.field-hint {
  margin-top: 6px;
  padding: 6px 10px;
  background-color: #fff7e6;
  border-left: 3px solid #ff9900;
  border-radius: 4px;
}

.field-hint .hint-text {
  font-size: 12px;
  color: #e67e00;
  line-height: 1.4;
}

/* 参数编辑样式 */
.params-card {
  margin-top: 16px;
}

.params-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 无参数状态 */
.no-params {
  padding: 40px 20px;
  text-align: center;
}



/* 参数卡片头部样式 */
.param-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.param-card-header span {
  font-weight: 600;
  color: #495057;
}



.param-list {
  max-height: 400px;
  overflow-y: auto;
}

.param-item {
  margin-bottom: 16px;
}

.param-item:last-child {
  margin-bottom: 0;
}

.param-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.param-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding-top: 28px;
}

/* 参数列表滚动条样式 */
.param-list::-webkit-scrollbar {
  width: 6px;
}

.param-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.param-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.param-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 默认值Monaco编辑器样式 */
.param-default-value-editor {
  margin-top: 4px;
}

.param-default-value-editor .monaco-editor-container {
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s ease;
}

.param-default-value-editor .monaco-editor-container:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.param-default-value-editor .editor-toolbar {
  background: linear-gradient(135deg, #f8f9fa 0%, #f1f3f4 100%);
  border-bottom: 1px solid #e1e5e9;
}

.param-default-value-editor .editor-title {
  color: #495057;
  font-weight: 600;
}

/* 响应式Monaco编辑器 */
@media (max-width: 768px) {
  .param-default-value-editor .monaco-editor-container {
    margin: 8px 0;
  }
  
  .param-default-value-editor .editor-toolbar {
    padding: 6px 8px;
  }
  

  
  /* 移动端参数卡片头部 */
  .param-card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    text-align: center;
  }
}
</style> 