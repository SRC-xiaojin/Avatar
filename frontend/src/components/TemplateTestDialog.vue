<template>
  <DraggableDialog
    v-model="visible"
    title="测试模板"
    width="700px"
    @close="handleClose"
  >
    <div class="test-container">
      <div class="test-info" v-if="template">
        <el-card shadow="never" header="模板信息">
          <el-descriptions :column="2" size="small">
            <el-descriptions-item label="模板名称">{{ template.templateName }}</el-descriptions-item>
            <el-descriptions-item label="模板编码">{{ template.templateCode }}</el-descriptions-item>
            <el-descriptions-item label="执行类">{{ template.executorClass }}</el-descriptions-item>
            <el-descriptions-item label="执行方法">{{ template.executorMethod }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>

      <div class="test-form">
        <el-card shadow="never" header="测试输入">
          <el-form label-width="100px">
            <el-form-item label="输入数据">
              <MonacoEditor
                v-model="inputData"
                language="json"
                theme="vs"
                height="200px"
                width="100%"
                title="测试输入数据"
                placeholder="请输入JSON格式的测试数据"
                :readonly="false"
                :show-toolbar="true"
                :show-status="true"
                :options="{}"
              />
              <div class="input-hint">
                <span class="hint-text">请输入符合该模板输入数据结构的JSON数据</span>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
      
      <div class="test-result" v-if="testResult">
        <el-card shadow="never" header="执行结果">
          <div class="result-content">
            <div class="result-status">
              <el-tag :type="testResult.success ? 'success' : 'danger'" size="large">
                {{ testResult.success ? '执行成功' : '执行失败' }}
              </el-tag>
              <div class="result-meta">
                <span class="execution-time" v-if="testResult.executionTimeMs !== undefined">
                  <el-icon><Timer /></el-icon>
                  耗时: {{ testResult.executionTimeMs }}ms
                </span>
                <span class="execution-timestamp" v-if="testResult.timestamp">
                  <el-icon><Clock /></el-icon>
                  执行时间: {{ formatTimestamp(testResult.timestamp) }}
                </span>
              </div>
            </div>
            
            <div class="result-data">
              <el-tabs>
                <el-tab-pane label="完整响应" v-if="testResult.success">
                  <div class="result-section">
                    <MonacoEditor
                      :model-value="JSON.stringify(testResult, null, 2)"
                      language="json"
                      theme="vs"
                      height="300px"
                      width="100%"
                      title="执行结果"
                      placeholder=""
                      :readonly="true"
                      :show-toolbar="false"
                      :show-status="true"
                      :options="{}"
                    />
                  </div>
                </el-tab-pane>
                
                <el-tab-pane label="错误信息" v-if="!testResult.success || testResult.errorMessage">
                  <div class="error-content">
                    <el-alert
                      :title="testResult.errorMessage || '执行失败'"
                      type="error"
                      :closable="false"
                      show-icon
                    />
                    <div v-if="testResult.errorCode" class="error-details">
                      <h4>错误代码: {{ testResult.errorCode }}</h4>
                    </div>
                  </div>
                </el-tab-pane>
                
                <el-tab-pane label="执行日志" v-if="testResult.executionLog">
                  <pre class="logs">{{ getExecutionLogs() }}</pre>
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="runTest" :loading="testing">
        <el-icon><VideoPlay /></el-icon>
        执行测试
      </el-button>
    </template>
  </DraggableDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay, Timer, Clock } from '@element-plus/icons-vue'
import DraggableDialog from './DraggableDialog.vue'
import MonacoEditor from './MonacoEditor.vue'
import { templateApi } from '@/api/templates.ts'
import type { OperatorTemplate, ExecutorResult } from '@/types/api'

interface Props {
  modelValue: boolean
  template?: OperatorTemplate | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

// 扩展ExecutorResult以包含前端需要的额外字段
interface TestResult extends ExecutorResult {
  timestamp?: Date | string | number
}

interface JsonSchema {
  properties?: Record<string, {
    type: string
    example?: any
  }>
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

const testing = ref(false)
const inputData = ref('{\n  "key": "value"\n}')
const testResult = ref<TestResult | null>(null)

// 监听模板变化，重置测试数据
watch(() => props.template, (newTemplate) => {
  if (newTemplate) {
    // 尝试从inputSchema生成示例数据
    try {
      const inputSchema: JsonSchema = JSON.parse(newTemplate.inputSchema || '{}')
      if (inputSchema.properties && Object.keys(inputSchema.properties).length > 0) {
        inputData.value = JSON.stringify(generateSampleData(inputSchema), null, 2)
      }
    } catch (error) {
      // 使用默认数据
      inputData.value = '{\n  "key": "value"\n}'
    }
  }
  testResult.value = null
})

// 根据schema生成示例数据
const generateSampleData = (schema: JsonSchema): Record<string, any> => {
  const sampleData: Record<string, any> = {}
  
  if (schema.properties) {
    Object.keys(schema.properties).forEach(key => {
      const prop = schema.properties![key]
      switch (prop.type) {
        case 'string':
          sampleData[key] = prop.example || 'sample_string'
          break
        case 'number':
          sampleData[key] = prop.example || 123
          break
        case 'boolean':
          sampleData[key] = prop.example !== undefined ? prop.example : true
          break
        case 'array':
          sampleData[key] = prop.example || []
          break
        case 'object':
          sampleData[key] = prop.example || {}
          break
        default:
          sampleData[key] = prop.example || 'sample_value'
      }
    })
  }
  
  return sampleData
}

// 格式化时间戳
const formatTimestamp = (timestamp: Date | string | number | undefined): string => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取执行日志
const getExecutionLogs = (): string => {
  if (testResult.value?.executionLog) {
    return testResult.value.executionLog
  }
  
  return '暂无执行日志'
}

const handleClose = () => {
  visible.value = false
  testResult.value = null
}

const runTest = async () => {
  if (!props.template || !props.template.id) {
    ElMessage.error('没有选择测试模板')
    return
  }

  try {
    // 验证输入数据格式
    const parsedInput = JSON.parse(inputData.value)
    
    testing.value = true
    const response = await templateApi.testTemplate(props.template.id, parsedInput)
    
    if (response.success) {
      // 处理后端返回的ExecutorResult结构
      const executorResult = response.data
      testResult.value = {
        ...executorResult,
        timestamp: new Date()
      }
      
      const timeInfo = executorResult.executionTimeMs ? `，耗时: ${executorResult.executionTimeMs}ms` : ''
      const successMessage = executorResult.success ? '测试执行成功' : '测试执行失败'
      
      if (executorResult.success) {
        ElMessage.success(`${successMessage}${timeInfo}`)
      } else {
        ElMessage.error(`${successMessage}: ${executorResult.errorMessage || '未知错误'}`)
      }
    } else {
      // API调用失败的情况
      testResult.value = {
        status: 'FAILED',
        success: false,
        outputData: {},
        errorMessage: response.message || '测试执行失败',
        timestamp: new Date(),
        executionTimeMs: 0
      }
      ElMessage.error(response.message || '测试执行失败')
    }
  } catch (error) {
    if (error instanceof SyntaxError) {
      ElMessage.error('输入数据格式错误，请检查JSON格式')
      testResult.value = {
        status: 'FAILED',
        success: false,
        outputData: {},
        errorMessage: 'JSON格式错误，请检查输入数据格式',
        timestamp: new Date(),
        executionTimeMs: 0
      }
    } else {
      console.error('测试执行失败:', error)
      testResult.value = {
        status: 'FAILED',
        success: false,
        outputData: {},
        errorMessage: '测试执行异常: ' + (error instanceof Error ? error.message : String(error)),
        timestamp: new Date(),
        executionTimeMs: 0
      }
      ElMessage.error('测试执行异常')
    }
  } finally {
    testing.value = false
  }
}
</script>

<style scoped>
.test-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 70vh;
  overflow-y: auto;
}

.test-info .el-card {
  margin-bottom: 0;
}

.input-hint {
  margin-top: 8px;
  padding: 6px 12px;
  background-color: #f0f9ff;
  border-left: 3px solid #3b82f6;
  border-radius: 4px;
}

.hint-text {
  font-size: 12px;
  color: #1e40af;
  line-height: 1.4;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-status {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.result-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.execution-time,
.execution-timestamp {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.execution-time .el-icon,
.execution-timestamp .el-icon {
  color: #409eff;
}

.result-section {
  margin-bottom: 16px;
}

.result-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.error-details {
  margin-top: 16px;
}

.error-details h4 {
  margin: 0 0 8px 0;
  font-size: 13px;
  font-weight: 600;
  color: #f56c6c;
}

.result-json,
.stack-trace,
.logs {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.error-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stack-trace {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
}

.logs {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  color: #374151;
}

/* 美化滚动条 */
.test-container::-webkit-scrollbar,
.result-json::-webkit-scrollbar,
.stack-trace::-webkit-scrollbar,
.logs::-webkit-scrollbar {
  width: 6px;
}

.test-container::-webkit-scrollbar-track,
.result-json::-webkit-scrollbar-track,
.stack-trace::-webkit-scrollbar-track,
.logs::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.test-container::-webkit-scrollbar-thumb,
.result-json::-webkit-scrollbar-thumb,
.stack-trace::-webkit-scrollbar-thumb,
.logs::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.test-container::-webkit-scrollbar-thumb:hover,
.result-json::-webkit-scrollbar-thumb:hover,
.stack-trace::-webkit-scrollbar-thumb:hover,
.logs::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 