<template>
  <DraggableDialog
    v-model="visible"
    title="模板详情"
    width="900px"
    @close="handleClose"
  >
    <div v-if="template" class="template-detail">
      <!-- 基本信息 -->
      <el-card class="detail-section" header="基本信息" shadow="never">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="模板编码">{{ template.templateCode }}</el-descriptions-item>
          <el-descriptions-item label="模板名称">{{ template.templateName }}</el-descriptions-item>
          <el-descriptions-item label="所属类别">{{ template.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="template.status ? 'success' : 'danger'">
              {{ template.status ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ template.description }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 执行信息 -->
      <el-card class="detail-section" header="执行信息" shadow="never">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="执行器类">{{ template.executorClass }}</el-descriptions-item>
          <el-descriptions-item label="执行方法">{{ template.executorMethod }}</el-descriptions-item>
          <el-descriptions-item label="是否异步">
            <el-tag :type="template.isAsync ? 'warning' : 'info'">
              {{ template.isAsync ? '异步' : '同步' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="超时时间">{{ template.timeoutSeconds }}秒</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 数据结构定义 -->
      <el-card class="detail-section" header="数据结构定义" shadow="never">
        <el-tabs>
          <el-tab-pane label="输入数据结构">
            <div v-if="template.inputSchema">
              <div class="schema-header">
                <h6>输入数据格式说明</h6>
                <p>定义算子接收的输入数据结构和字段类型</p>
              </div>
              <el-input
                :model-value="formatTemplateSchema(template.inputSchema)"
                type="textarea"
                :rows="8"
                readonly
                class="schema-display"
              />
            </div>
            <el-empty v-else description="暂无输入数据结构定义" />
          </el-tab-pane>
          
          <el-tab-pane label="输出数据结构">
            <div v-if="template.outputSchema">
              <div class="schema-header">
                <h6>输出数据格式说明</h6>
                <p>定义算子返回的输出数据结构和字段类型</p>
              </div>
              <el-input
                :model-value="formatTemplateSchema(template.outputSchema)"
                type="textarea"
                :rows="8"
                readonly
                class="schema-display"
              />
            </div>
            <el-empty v-else description="暂无输出数据结构定义" />
          </el-tab-pane>

          <el-tab-pane label="配置信息" v-if="template.configSchema">
            <div class="schema-header">
              <h6>配置参数说明</h6>
              <p>定义算子的配置参数和选项</p>
            </div>
            <el-input
              :model-value="formatTemplateSchema(template.configSchema)"
              type="textarea"
              :rows="8"
              readonly
              class="schema-display"
            />
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <!-- 参数定义 -->
      <el-card class="detail-section" header="参数定义" shadow="never" v-if="template.params && Object.keys(template.params).length > 0">
        <div class="params-content">
          <div v-for="(paramList, paramKey) in template.params" :key="paramKey" class="param-category">
            <h4 class="category-title">{{ paramKey }}</h4>
            <el-table :data="Array.isArray(paramList) ? paramList : [paramList]" stripe>
              <el-table-column prop="paramName" label="参数名称" width="150" />
              <el-table-column prop="paramKey" label="参数键" width="150" />
              <el-table-column prop="paramType" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="getParamTypeColor(row.paramType)">
                    {{ row.paramType }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="paramCategory" label="类别" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="getCategoryColor(row.paramCategory)">
                    {{ getCategoryLabel(row.paramCategory) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="isRequired" label="必填" width="80">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.isRequired ? 'danger' : 'info'">
                    {{ row.isRequired ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="defaultValue" label="默认值" width="200">
                <template #default="{ row }">
                  <div class="default-value-cell">
                    <span class="value-preview">{{ getValuePreview(row.defaultValue) }}</span>
                    <el-button 
                      v-if="row.defaultValue" 
                      type="primary" 
                      size="small" 
                      plain
                      @click="viewDefaultValue(row)"
                      class="view-btn"
                    >
                      <el-icon><View /></el-icon>
                      查看
                    </el-button>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="validationRules" label="验证规则" width="150" />
            </el-table>
          </div>
        </div>
      </el-card>
    </div>
  </DraggableDialog>

  <!-- Monaco Editor 全屏查看器 -->
  <el-dialog
    v-model="monacoVisible"
    :title="`默认值查看器 - ${currentViewingParam?.paramName || '未知参数'} (只读模式)`"
    width="95%"
    top="3vh"
    :before-close="closeMonacoViewer"
    destroy-on-close
    class="monaco-viewer-dialog"
  >
    <div v-if="currentViewingParam" class="monaco-viewer-content">
      <div class="param-info">
        <el-descriptions :column="4" size="small" border>
          <el-descriptions-item label="参数名称">{{ currentViewingParam.paramName }}</el-descriptions-item>
          <el-descriptions-item label="参数键">{{ currentViewingParam.paramKey }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag size="small" :type="getParamTypeColor(currentViewingParam.paramType)">
              {{ currentViewingParam.paramType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="类别">
            <el-tag size="small" :type="getCategoryColor(currentViewingParam.paramCategory)">
              {{ getCategoryLabel(currentViewingParam.paramCategory) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="monaco-container">
        <MonacoEditor
          :model-value="currentViewingParam.defaultValue || ''"
          :title="`${currentViewingParam.paramName} - 默认值 (只读)`"
          :language="getEditorLanguage(currentViewingParam.paramType)"
          height="60vh"
          readonly
          :show-toolbar="true"
          :show-status="true"
          class="viewer-editor"
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="closeMonacoViewer">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { View } from '@element-plus/icons-vue'
import DraggableDialog from './DraggableDialog.vue'
import MonacoEditor from './MonacoEditor.vue'
import type { OperatorTemplate, TemplateParam } from '@/types/api'

interface Props {
  modelValue: boolean
  template?: OperatorTemplate | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  template: null
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const handleClose = () => {
  visible.value = false
}

// Monaco Editor 全屏查看相关状态
const monacoVisible = ref(false)
const currentViewingParam = ref<TemplateParam | null>(null)

// 获取默认值预览文本
const getValuePreview = (value: any): string => {
  if (!value) return '无'
  
  const str = String(value)
  if (str.length <= 30) {
    return str
  }
  return str.substring(0, 30) + '...'
}

// 查看默认值
const viewDefaultValue = (param: TemplateParam) => {
  currentViewingParam.value = param
  console.log('currentViewingParam', currentViewingParam.value)
  monacoVisible.value = true
}

// 关闭Monaco Editor
const closeMonacoViewer = () => {
  monacoVisible.value = false
  currentViewingParam.value = null
}

// 根据参数类型获取编辑器语言
const getEditorLanguage = (paramType: string): string => {
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

// 格式化模板数据结构
const formatTemplateSchema = (jsonString: any): string => {
  try {
    if (typeof jsonString === 'string') {
      return JSON.stringify(JSON.parse(jsonString), null, 2)
    } else {
      return JSON.stringify(jsonString, null, 2)
    }
  } catch (error) {
    return jsonString || ''
  }
}

// 获取参数类型对应的颜色
const getParamTypeColor = (paramType?: string): string => {
  const colorMap: Record<string, string> = {
    'string': 'primary',
    'number': 'success',
    'integer': 'success',
    'boolean': 'warning',
    'object': 'info',
    'array': 'danger',
    'date': 'warning',
    'datetime': 'warning'
  }
  return colorMap[paramType?.toLowerCase() || ''] || ''
}

// 获取参数类别对应的颜色
const getCategoryColor = (category?: string): string => {
  const colorMap: Record<string, string> = {
    'input': 'primary',
    'output': 'success',
    'config': 'warning'
  }
  return colorMap[category?.toLowerCase() || ''] || 'info'
}

// 获取参数类别的中文标签
const getCategoryLabel = (category?: string): string => {
  const labelMap: Record<string, string> = {
    'input': '输入',
    'output': '输出',
    'config': '配置'
  }
  return labelMap[category?.toLowerCase() || ''] || category || ''
}
</script>

<style scoped>
.template-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.schema-header {
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.schema-header h6 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.schema-header p {
  margin: 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.schema-display .el-textarea__inner {
  background-color: #f8f9fa;
  border: 1px solid #e4e7ed;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
}

/* 美化滚动条 */
.template-detail::-webkit-scrollbar {
  width: 6px;
}

.template-detail::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.template-detail::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.template-detail::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 参数展示样式 */
.params-content {
  max-height: 400px;
  overflow-y: auto;
}

.param-category {
  margin-bottom: 20px;
}

.param-category:last-child {
  margin-bottom: 0;
}

.category-title {
  margin: 0 0 12px 0;
  padding: 8px 12px;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  border-radius: 4px;
}

.params-content::-webkit-scrollbar {
  width: 6px;
}

.params-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.params-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.params-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 默认值单元格样式 */
.default-value-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.value-preview {
  flex: 1;
  font-size: 12px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: #f8f9fa;
  padding: 2px 6px;
  border-radius: 3px;
  border: 1px solid #e4e7ed;
}

.view-btn {
  flex-shrink: 0;
  padding: 4px 8px;
  height: auto;
  line-height: 1.2;
}

.view-btn .el-icon {
  margin-right: 2px;
}

/* Monaco 查看器对话框样式 */
:deep(.monaco-viewer-dialog) {
  .el-dialog__body {
    padding: 12px 20px;
  }
  
  .el-dialog__header {
    padding: 16px 20px 12px;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .el-dialog__title {
    font-weight: 600;
    font-size: 16px;
    color: #303133;
  }
  
  .el-dialog__footer {
    padding: 12px 20px 16px;
    border-top: 1px solid #e4e7ed;
    text-align: right;
  }
}

.monaco-viewer-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.param-info {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.monaco-container {
  flex: 1;
  min-height: 0;
}

.viewer-editor {
  height: 100%;
}

.viewer-editor .monaco-editor-container {
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.viewer-editor .editor-toolbar {
  background: linear-gradient(135deg, #f0f2f5 0%, #e8eaed 100%);
  border-bottom: 1px solid #d1d5db;
}

.viewer-editor .editor-title {
  color: #374151;
  font-weight: 600;
}

/* 响应式Monaco查看器 */
@media (max-width: 768px) {
  .default-value-cell {
    flex-direction: column;
    align-items: stretch;
    gap: 4px;
  }
  
  .value-preview {
    text-align: center;
    margin-bottom: 4px;
  }
  
  .view-btn {
    align-self: stretch;
    justify-content: center;
  }
  
  :deep(.monaco-viewer-dialog) {
    .el-dialog {
      width: 98% !important;
      margin: 1vh auto;
    }
  }
  
  .param-info :deep(.el-descriptions) {
    .el-descriptions__table {
      .el-descriptions__cell {
        padding: 6px 8px;
      }
    }
  }
}
</style> 