<template>
  <div class="workflow-management">
    <div class="page-header">
      <div class="header-left">
        <h2>编排管理</h2>
        <p>管理算子编排工作流</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="$router.push('/designer')">
          <el-icon><Plus /></el-icon>
          新建编排
        </el-button>
      </div>
    </div>

    <div class="page-content">
      <!-- 搜索过滤 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索工作流名称"
              @input="handleSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.status"
              placeholder="选择状态"
              @change="handleSearch"
              clearable
            >
              <el-option label="全部" value="" />
              <el-option label="草稿" value="DRAFT" />
              <el-option label="激活" value="ACTIVE" />
              <el-option label="暂停" value="PAUSED" />
              <el-option label="停止" value="STOPPED" />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- 工作流列表 -->
      <div class="workflow-list">
        <el-table :data="filteredWorkflows" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="workflowName" label="工作流名称" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="nodeCount" label="节点数量" width="100" />
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="500" class-name="action-column">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" @click="viewWorkflow(row)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button size="small" type="primary" @click="editWorkflow(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button 
                  size="small" 
                  type="success" 
                  @click="executeWorkflow(row)"
                  :disabled="row.status === 'STOPPED'"
                >
                  <el-icon><VideoPlay /></el-icon>
                  执行
                </el-button>
                <el-button 
                  size="small" 
                  type="info" 
                  @click="testWorkflow(row)"
                  :disabled="row.status === 'STOPPED'"
                >
                  <el-icon><Tools /></el-icon>
                  测试
                </el-button>
                <el-button 
                  size="small" 
                  type="warning" 
                  @click="validateWorkflow(row)"
                >
                  验证
                </el-button>
                <el-button size="small" type="danger" @click="deleteWorkflow(row)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 查看工作流详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="工作流详情" width="900px">
      <div v-if="selectedWorkflow" class="workflow-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="名称" :span="2">
            {{ selectedWorkflow.workflowName }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusColor(selectedWorkflow.status)">
              {{ getStatusLabel(selectedWorkflow.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="节点数量">
            {{ selectedWorkflow.nodeCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ selectedWorkflow.description }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(selectedWorkflow.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(selectedWorkflow.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 工作流节点列表 -->
        <div class="workflow-nodes" v-if="workflowNodes.length > 0">
          <h4>工作流节点</h4>
          <el-table :data="workflowNodes" size="small">
            <el-table-column prop="nodeName" label="节点名称" />
            <el-table-column prop="templateName" label="算子模板" width="150">
              <template #default="{ row }">
                <el-tag size="small">{{ row.templateName || '未知模板' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="nodeType" label="节点类型" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="getNodeTypeColor(row.nodeType)">
                  {{ getNodeTypeLabel(row.nodeType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="positionX" label="位置X" width="80" />
            <el-table-column prop="positionY" label="位置Y" width="80" />
          </el-table>
        </div>

        <!-- 执行历史 -->
        <div class="execution-history" v-if="executionHistory.length > 0">
          <h4>执行历史</h4>
          <el-table :data="executionHistory" size="small" max-height="200">
            <el-table-column prop="executionId" label="执行ID" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="getExecutionStatusColor(row.status)">
                  {{ getExecutionStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="150">
              <template #default="{ row }">
                {{ formatDate(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="150">
              <template #default="{ row }">
                {{ formatDate(row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="durationMs" label="耗时(ms)" width="100" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 执行结果对话框 -->
    <el-dialog v-model="showExecutionDialog" title="执行结果" width="70%">
      <div class="execution-result">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="执行日志" name="logs">
            <div class="execution-logs">
              <div 
                v-for="(log, index) in executionLogs" 
                :key="index"
                class="log-item"
                :class="log.level"
              >
                <span class="log-time">{{ log.timestamp }}</span>
                <span class="log-message">{{ log.message }}</span>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="执行状态" name="status">
            <div class="execution-status">
              <el-steps :active="executionStep" finish-status="success">
                <el-step title="开始执行" />
                <el-step title="节点处理" />
                <el-step title="数据转换" />
                <el-step title="执行完成" />
              </el-steps>
              <div class="execution-info" v-if="currentExecution">
                <el-descriptions :column="2" border style="margin-top: 20px;">
                  <el-descriptions-item label="执行ID">
                    {{ currentExecution.executionId }}
                  </el-descriptions-item>
                  <el-descriptions-item label="状态">
                    <el-tag :type="getExecutionStatusColor(currentExecution.status)">
                      {{ getExecutionStatusLabel(currentExecution.status) }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="开始时间">
                    {{ formatDate(currentExecution.startTime) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="耗时">
                    {{ currentExecution.durationMs }}ms
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="输出数据" name="output">
            <el-input
              v-model="executionOutput"
              type="textarea"
              :rows="12"
              readonly
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 测试工作流对话框 -->
    <el-dialog v-model="showTestDialog" title="测试工作流" width="80%">
      <div class="test-workflow">
        <el-row :gutter="20">
          <el-col :span="12">
            <h4>测试输入数据</h4>
            <MonacoEditor
              v-model="testInputData"
              language="json"
              theme="vs"
              height="400px"
              width="100%"
              title="测试输入"
              placeholder="请输入测试数据..."
              :show-toolbar="true"
              :show-status="true"
              :readonly="false"
              :options="{}"
            />
          </el-col>
          <el-col :span="12">
            <h4>测试结果</h4>
            <MonacoEditor
              v-model="testOutputData"
              language="json"
              theme="vs"
              height="400px"
              width="100%"
              title="测试结果"
              placeholder="测试结果将在这里显示..."
              :show-toolbar="true"
              :show-status="true"
              :readonly="true"
              :options="{}"
            />
          </el-col>
        </el-row>
        
        <div class="test-controls" style="margin-top: 20px;">
          <el-row :gutter="20">
            <el-col :span="24">
              <div class="test-status-info" v-if="testStatus">
                <el-alert
                  :type="testStatus.type"
                  :title="testStatus.title"
                  :description="testStatus.message"
                  show-icon
                  style="margin-bottom: 15px;"
                />
              </div>
              
              <div class="test-progress" v-if="testRunning">
                <el-progress 
                  :percentage="testProgress" 
                  :status="testProgress === 100 ? 'success' : 'warning'"
                  :stroke-width="8"
                  style="margin-bottom: 15px;"
                />
                <p class="test-step-info">{{ testStepInfo }}</p>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showTestDialog = false">关闭</el-button>
        <el-button @click="clearTestData">清空数据</el-button>
        <el-button @click="loadSampleData" type="info">加载示例数据</el-button>
        <el-button 
          type="primary" 
          @click="runWorkflowTest"
          :loading="testRunning"
        >
          {{ testRunning ? '测试中...' : '开始测试' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 验证结果对话框 -->
    <el-dialog v-model="showValidationDialog" title="工作流验证" width="600px">
      <div class="validation-result">
        <el-result
          :icon="validationResult?.valid ? 'success' : 'error'"
          :title="validationResult?.valid ? '验证通过' : '验证失败'"
          :sub-title="validationResult?.message"
        >
          <template #extra>
            <el-button @click="showValidationDialog = false">关闭</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, View, Edit, VideoPlay, Delete, Tools } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { workflowApi } from '@/api/workflows.ts'
import { nodeApi } from '@/api/nodes.ts'
import MonacoEditor from '@/components/MonacoEditor.vue'
import type { Workflow, WorkflowNode } from '@/types/api'

interface WorkflowWithCount extends Workflow {
  nodeCount: number
}

interface ExecutionLog {
  timestamp: string
  level: 'info' | 'success' | 'warning' | 'error'
  message: string
}

interface SearchForm {
  keyword: string
  status: string
}

const router = useRouter()

// 响应式数据
const loading = ref(false)
const workflows = ref<WorkflowWithCount[]>([])
const workflowNodes = ref<WorkflowNode[]>([])
const executionHistory = ref<any[]>([])
const showDetailDialog = ref(false)
const showExecutionDialog = ref(false)
const showValidationDialog = ref(false)
const showTestDialog = ref(false)
const selectedWorkflow = ref<Workflow | null>(null)
const selectedTestWorkflow = ref<Workflow | null>(null)
const activeTab = ref<'logs' | 'output'>('logs')
const executionStep = ref(0)
const executionLogs = ref<ExecutionLog[]>([])
const executionOutput = ref('')
const currentExecution = ref<any>(null)
const validationResult = ref<any>(null)
const testInputData = ref('')
const testOutputData = ref('')
const testRunning = ref(false)
const testProgress = ref(0)
const testStepInfo = ref('')
const testStatus = ref<any>(null)

// 搜索表单
const searchForm = reactive<SearchForm>({
  keyword: '',
  status: ''
})

// 计算属性
const filteredWorkflows = computed(() => {
  return workflows.value.filter(workflow => {
    const matchKeyword = !searchForm.keyword || 
      workflow.workflowName.toLowerCase().includes(searchForm.keyword.toLowerCase())
    const matchStatus = !searchForm.status || workflow.status === searchForm.status
    return matchKeyword && matchStatus
  })
})

// 方法
const getStatusLabel = (status: string): string => {
  const statusMap: Record<string, string> = {
    'DRAFT': '草稿',
    'ACTIVE': '激活',
    'PAUSED': '暂停',
    'STOPPED': '停止'
  }
  return statusMap[status] || status
}

const getStatusColor = (status: string): string => {
  const colorMap: Record<string, string> = {
    'DRAFT': '',
    'ACTIVE': 'success',
    'PAUSED': 'warning',
    'STOPPED': 'danger'
  }
  return colorMap[status] || 'default'
}

const getNodeTypeLabel = (type: string): string => {
  const typeMap: Record<string, string> = {
    'START': '开始',
    'PROCESS': '处理',
    'CONDITION': '条件',
    'END': '结束'
  }
  return typeMap[type] || type
}

const getNodeTypeColor = (type: string): string => {
  const colorMap: Record<string, string> = {
    'START': 'success',
    'PROCESS': 'primary',
    'CONDITION': 'warning',
    'END': 'info'
  }
  return colorMap[type] || 'default'
}

const getExecutionStatusLabel = (status: string): string => {
  const statusMap: Record<string, string> = {
    'PENDING': '等待',
    'RUNNING': '运行中',
    'SUCCESS': '成功',
    'FAILED': '失败',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getExecutionStatusColor = (status: string): string => {
  const colorMap: Record<string, string> = {
    'PENDING': 'info',
    'RUNNING': 'warning',
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'CANCELLED': ''
  }
  return colorMap[status] || 'default'
}

const formatDate = (dateString: string | undefined): string => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

const handleSearch = () => {
  loadWorkflows()
}

const loadWorkflows = async () => {
  try {
    loading.value = true
    const response = await workflowApi.getWorkflows()
    if (response.success) {
      workflows.value = response.data.map(workflow => ({
        ...workflow,
        nodeCount: 0 // 这里可以根据需要加载节点数量
      }))
    } else {
      ElMessage.error(response.message || '加载工作流失败')
    }
  } catch (error) {
    console.error('加载工作流失败:', error as Error)
    ElMessage.error('加载工作流失败')
  } finally {
    loading.value = false
  }
}

const loadWorkflowNodes = async (workflowId: number) => {
  try {
    const response = await nodeApi.getNodesByWorkflow(workflowId)
    if (response.success) {
      workflowNodes.value = response.data
    } else {
      workflowNodes.value = []
    }
  } catch (error) {
    console.error('加载工作流节点失败:', error as Error)
    workflowNodes.value = []
  }
}

const loadExecutionHistory = async (workflowId: number) => {
  try {
    const response = await workflowApi.getWorkflowHistory(workflowId)
    if (response.success) {
      executionHistory.value = response.data
    } else {
      executionHistory.value = []
    }
  } catch (error) {
    console.error('加载执行历史失败:', error as Error)
    executionHistory.value = []
  }
}

const viewWorkflow = async (workflow: Workflow) => {
  selectedWorkflow.value = workflow
  await Promise.all([
    loadWorkflowNodes(workflow.id!),
    loadExecutionHistory(workflow.id!)
  ])
  showDetailDialog.value = true
}

const editWorkflow = (workflow: Workflow) => {
  router.push(`/designer?workflowId=${workflow.id}`)
}

const executeWorkflow = async (workflow: Workflow) => {
  try {
    ElMessage.info('开始执行工作流...')
    
    showExecutionDialog.value = true
    activeTab.value = 'logs'
    executionStep.value = 0
    executionLogs.value = []
    executionOutput.value = ''
    currentExecution.value = null
    
    // 调用后端API执行工作流
    const response = await workflowApi.executeWorkflow(workflow.id!, {})
    
    if (response.success) {
      currentExecution.value = response.data
      
      // 模拟步骤进度
      const steps = [
        { step: 0, message: '工作流开始执行', level: 'info' as const },
        { step: 1, message: '正在处理节点...', level: 'info' as const },
        { step: 2, message: '正在进行数据转换...', level: 'info' as const },
        { step: 3, message: '工作流执行完成', level: 'success' as const }
      ]
      
      for (let i = 0; i < steps.length; i++) {
        await new Promise(resolve => setTimeout(resolve, 800))
        executionStep.value = steps[i].step + 1
        executionLogs.value.push({
          timestamp: new Date().toLocaleTimeString(),
          level: steps[i].level,
          message: steps[i].message
        })
      }
      
      executionOutput.value = JSON.stringify(response.data, null, 2)
      ElMessage.success('工作流执行完成')
    } else {
      ElMessage.error(response.message || '工作流执行失败')
    }
  } catch (error) {
    console.error('执行工作流失败:', error as Error)
    ElMessage.error('执行失败: ' + (error as Error).message)
    
    executionLogs.value.push({
      timestamp: new Date().toLocaleTimeString(),
      level: 'error',
      message: '执行失败: ' + (error as Error).message
    })
  }
}

const validateWorkflow = async (workflow: Workflow) => {
  try {
    const response = await workflowApi.validateWorkflow(workflow.id!)
    if (response.success) {
      validationResult.value = response.data
      showValidationDialog.value = true
    } else {
      ElMessage.error(response.message || '验证失败')
    }
  } catch (error) {
    console.error('验证工作流失败:', error as Error)
    ElMessage.error('验证失败')
  }
}

const testWorkflow = (workflow: Workflow) => {
  selectedTestWorkflow.value = workflow
  testInputData.value = ''
  testOutputData.value = ''
  testStatus.value = null
  testRunning.value = false
  testProgress.value = 0
  testStepInfo.value = ''
  showTestDialog.value = true
}

const clearTestData = () => {
  testInputData.value = ''
  testOutputData.value = ''
  testStatus.value = null
  testProgress.value = 0
  testStepInfo.value = ''
}

const loadSampleData = () => {
  const sampleData = {
    "input": {
      "data": "test data",
      "timestamp": new Date().toISOString(),
      "userId": "user123"
    },
    "config": {
      "timeout": 30000,
      "retries": 3
    }
  }
  testInputData.value = JSON.stringify(sampleData, null, 2)
}

const runWorkflowTest = async () => {
  if (!selectedTestWorkflow.value) return
  
  try {
    testRunning.value = true
    testProgress.value = 0
    testStepInfo.value = '开始测试...'
    testStatus.value = null
    testOutputData.value = ''
    
    // 验证输入数据
    if (!testInputData.value.trim()) {
      testStatus.value = {
        type: 'warning',
        title: '输入数据为空',
        message: '请输入测试数据或点击"加载示例数据"'
      }
      return
    }
    
    let inputData
    try {
      inputData = JSON.parse(testInputData.value)
    } catch (error) {
      testStatus.value = {
        type: 'error',
        title: 'JSON格式错误',
        message: '测试输入数据格式不正确，请检查JSON语法'
      }
      return
    }
    
    // 模拟测试步骤
    const steps = [
      { progress: 20, message: '验证工作流配置...' },
      { progress: 40, message: '加载工作流节点...' },
      { progress: 60, message: '执行数据处理...' },
      { progress: 80, message: '生成测试结果...' },
      { progress: 100, message: '测试完成!' }
    ]
    
    for (const step of steps) {
      await new Promise(resolve => setTimeout(resolve, 800))
      testProgress.value = step.progress
      testStepInfo.value = step.message
    }
    
    // 调用后端API执行测试
    const response = await workflowApi.testWorkflow(selectedTestWorkflow.value.id!, inputData)
    
    if (response.success) {
      testOutputData.value = JSON.stringify(response.data, null, 2)
      testStatus.value = {
        type: 'success',
        title: '测试成功',
        message: '工作流测试执行完成，请查看右侧结果'
      }
    } else {
      testOutputData.value = JSON.stringify({
        error: response.message || '测试失败',
        timestamp: new Date().toISOString()
      }, null, 2)
      testStatus.value = {
        type: 'error',
        title: '测试失败',
        message: response.message || '工作流测试执行失败'
      }
    }
    
  } catch (error) {
    console.error('测试工作流失败:', error as Error)
    testOutputData.value = JSON.stringify({
      error: (error as Error).message,
      timestamp: new Date().toISOString()
    }, null, 2)
    testStatus.value = {
      type: 'error',
      title: '测试异常',
      message: '测试执行过程中发生异常: ' + (error as Error).message
    }
  } finally {
    testRunning.value = false
  }
}

const deleteWorkflow = async (workflow: Workflow) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除工作流"${workflow.workflowName}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await workflowApi.deleteWorkflow(workflow.id!)
    if (response.success) {
      ElMessage.success('删除成功')
      loadWorkflows()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除工作流失败:', error as Error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadWorkflows()
})
</script>

<style scoped>
.workflow-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.header-left h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.header-left p {
  margin: 0;
  color: #909399;
}

.search-bar {
  margin-bottom: 20px;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: nowrap;
  align-items: center;
}

.action-buttons .el-button {
  margin: 0;
  min-width: 64px;
  width: 64px;
  padding: 4px 6px;
  font-size: 12px;
  text-align: center;
  justify-content: center;
}

.action-buttons .el-button .el-icon {
  margin-right: 2px;
}

/* 确保操作列不换行 */
:deep(.action-column .cell) {
  overflow: visible;
  white-space: nowrap;
  padding: 8px 12px;
}

.workflow-detail {
  padding: 0;
}

.workflow-nodes,
.execution-history {
  margin-top: 20px;
}

.workflow-nodes h4,
.execution-history h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.execution-result {
  min-height: 400px;
}

.execution-logs {
  max-height: 300px;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.log-item {
  padding: 4px 0;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  gap: 10px;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

.log-message {
  flex: 1;
}

.log-item.error .log-message {
  color: #f56c6c;
}

.log-item.success .log-message {
  color: #67c23a;
}

.log-item.warning .log-message {
  color: #e6a23c;
}

.execution-status {
  padding: 20px;
}

.execution-info {
  margin-top: 20px;
}

.validation-result {
  text-align: center;
}

/* 测试工作流样式 */
.test-workflow h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.test-controls {
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
}

.test-step-info {
  text-align: center;
  color: #606266;
  margin: 5px 0 0 0;
  font-size: 14px;
}

.test-status-info {
  margin-bottom: 15px;
}

.test-progress {
  text-align: center;
}

/* 确保按钮在测试对话框中正确显示 */
.test-workflow .el-dialog__footer {
  text-align: right;
  padding: 12px 20px;
  border-top: 1px solid #e4e7ed;
}

.test-workflow .el-dialog__footer .el-button {
  margin-left: 10px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  /* 移动端调整按钮样式 */
  .action-buttons {
    flex-direction: column;
    gap: 4px;
    align-items: stretch;
  }
  
  .action-buttons .el-button {
    width: 100%;
    justify-content: center;
    padding: 6px 8px;
  }
}
</style> 