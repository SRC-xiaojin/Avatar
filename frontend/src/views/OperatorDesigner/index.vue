<template>
  <div class="operator-designer">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button @click="clearCanvas" type="danger">
          <el-icon><Delete /></el-icon>
          清空画布
        </el-button>
        <el-button @click="saveWorkflow" type="primary" :loading="isSaving">
          <el-icon><DocumentAdd /></el-icon>
          保存工作流
        </el-button>
        <el-button @click="executeWorkflow" type="success">
          <el-icon><VideoPlay /></el-icon>
          执行工作流
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button @click="showConnectionHelp = true" type="info">
          <el-icon><QuestionFilled /></el-icon>
          连线帮助
        </el-button>
        <el-button @click="showDebugPanel = !showDebugPanel" type="warning">
          <el-icon><Monitor /></el-icon>
          调试面板
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧算子面板 -->
      <OperatorPanel
        :operator-categories="operatorCategories"
        :loading="loading"
        :icon-map="iconMap"
        @toggle-category="toggleCategory"
        @is-category-collapsed="isCategoryCollapsed"
        @operator-drag-start="onOperatorDragStart"
        @show-operator-details="showOperatorDetails"
      />

      <!-- 中央设计画布 -->
      <DesignCanvas
        ref="designCanvasRef"
        :canvas-nodes="canvasNodes"
        :connections="connections"
        :selected-node="selectedNode"
        :selected-connection="selectedConnection"
        :hovered-connection="hoveredConnection"
        :dragging-node="draggingNode"
        :temp-connection="tempConnection"
        :canvas-size="canvasSize"
        :get-connection-path="getConnectionPath"
        :get-connection-marker="getConnectionMarker"
        :get-connection-stroke-width="getConnectionStrokeWidth"
        :get-connection-color="getConnectionColor"
        :get-temp-connection-path="getTempConnectionPath"
        :can-create-connection="canCreateConnection"
        @canvas-drop="onCanvasDrop"
        @canvas-drag-over="onCanvasDragOver"
        @canvas-mouse-up="onCanvasMouseUp"
        @canvas-mouse-move="onCanvasMouseMove"
        @canvas-click="onCanvasClick"
        @select-node="selectNode"
        @start-node-drag="startNodeDrag"
        @remove-node="removeNode"
        @start-connection="startConnection"
        @connection-point-enter="onConnectionPointEnter"
        @connection-point-leave="onConnectionPointLeave"
        @select-connection="selectConnection"
        @connection-hover="onConnectionHover"
        @connection-leave="onConnectionLeave"
      />

      <!-- 右侧属性面板 -->
      <PropertyPanel
        :selected-node="selectedNode"
        :selected-connection="selectedConnection"
        :canvas-nodes="canvasNodes"
        @node-property-change="onNodePropertyChange"
        @node-config-change="onNodeConfigChange"
        @connection-type-change="onConnectionTypeChange"
        @delete-selected-connection="deleteSelectedConnection"
      />
    </div>

    <!-- 调试面板 -->
    <DebugPanel
      :show="showDebugPanel"
      :temp-connection="tempConnection"
      :canvas-nodes="canvasNodes"
      :connections="connections"
      :selected-node="selectedNode"
      :selected-connection="selectedConnection"
      :operation-logs="operationLogs"
      @toggle-debug-panel="showDebugPanel = !showDebugPanel"
      @select-connection="selectConnection"
      @delete-connection="deleteConnection"
    />

    <!-- 连线帮助面板 -->
    <HelpPanel v-model="showConnectionHelp" />

    <!-- 算子详情对话框 -->
    <el-dialog
      v-model="showOperatorDetail"
      title="算子详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentOperator" class="operator-detail">
        <div class="operator-header">
          <el-icon class="operator-icon">
            <component :is="currentOperator.icon" />
          </el-icon>
          <div class="operator-info">
            <h3>{{ currentOperator.name }}</h3>
            <p>{{ currentOperator.description }}</p>
          </div>
        </div>
        <div class="operator-config">
          <h4>配置项</h4>
          <pre>{{ JSON.stringify(currentOperator.config || {}, null, 2) }}</pre>
        </div>
      </div>
    </el-dialog>

    <!-- 执行结果对话框 -->
    <el-dialog
      v-model="showExecutionResult"
      title="执行结果"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="execution-result">
        <div class="execution-logs">
          <h4>执行日志</h4>
          <div class="log-list">
            <div
              v-for="(log, index) in executionLogs"
              :key="index"
              class="log-item"
              :class="log.level"
            >
              <span class="log-timestamp">{{ log.timestamp }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
        </div>
        <div class="execution-output">
          <h4>执行输出</h4>
          <pre>{{ executionOutput }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { 
  Delete, 
  DocumentAdd, 
  VideoPlay, 
  QuestionFilled, 
  Monitor,
  DataBoard,
  Switch,
  Connection,
  Coin,
  Tools,
  Operation
} from '@element-plus/icons-vue'

// 导入组件
import OperatorPanel from './components/OperatorPanel.vue'
import DesignCanvas from './components/DesignCanvas.vue'
import PropertyPanel from './components/PropertyPanel.vue'
import DebugPanel from './components/DebugPanel.vue'
import HelpPanel from './components/HelpPanel.vue'

// 导入 composables
import { useWorkflowDesigner } from './composables/useWorkflowDesigner'
import { useConnectionManager } from './composables/useConnectionManager'
import { useCanvasOperations } from './composables/useCanvasOperations'

// 画布引用
const designCanvasRef = ref(null)

// 使用 composables
const {
  // 响应式数据
  operatorCategories,
  loading,
  collapsedCategories,
  canvasNodes,
  selectedNode,
  nextNodeId,
  canvasSize,
  connections,
  selectedConnection,
  hoveredConnection,
  nextConnectionId,
  currentWorkflowId,
  
  // 方法
  toggleCategory,
  isCategoryCollapsed,
  showOperatorDetails,
  getDefaultConfig,
  loadOperatorCategories,
  findNearestConnectionPoint,
  canCreateConnection,
  showConnectionError,
  selectConnection,
  deleteConnection,
  onConnectionHover,
  onConnectionLeave,
  deleteSelectedConnection,
  onConnectionTypeChange,
  onNodePropertyChange,
  onNodeConfigChange
} = useWorkflowDesigner()

const {
  tempConnection,
  hoveredConnectionPoint,
  startConnection,
  endConnection,
  updateTempConnection,
  onConnectionPointEnter,
  onConnectionPointLeave,
  getConnectionPath,
  getConnectionMarker,
  getConnectionStrokeWidth,
  getConnectionColor,
  getTempConnectionPath
} = useConnectionManager(canvasNodes, connections, selectedConnection, hoveredConnection, nextConnectionId, designCanvasRef)

const {
  draggingNode,
  nodeDragState,
  isSaving,
  onOperatorDragStart,
  onCanvasDragOver,
  onCanvasDrop,
  selectNode,
  onCanvasClick,
  removeNode,
  startNodeDrag,
  onDocumentMouseMove,
  onDocumentMouseUp,
  saveWorkflow,
  clearCanvas,
  executeWorkflow,
  executeNode,
  getExecutionOrder
} = useCanvasOperations(canvasNodes, connections, selectedNode, selectedConnection, nextNodeId, nextConnectionId, currentWorkflowId, canvasSize, designCanvasRef, getDefaultConfig)

// 界面状态
const showDebugPanel = ref(false)
const showConnectionHelp = ref(false)
const showOperatorDetail = ref(false)
const currentOperator = ref(null)
const showExecutionResult = ref(false)
const executionLogs = ref([])
const executionOutput = ref('')

// 操作日志
const operationLogs = ref([])

// 图标映射
const iconMap = {
  INPUT: DataBoard,
  OUTPUT: DataBoard,
  TRANSFORM: Switch,
  FILTER: Connection,
  AGGREGATION: Coin,
  CONDITION: Tools,
  CUSTOM: Operation
}

// 画布事件处理
const onCanvasMouseUp = (event) => {
  if (tempConnection.value.isDrawing) {
    endConnection(event)
  }
}

const onCanvasMouseMove = (event) => {
  if (tempConnection.value.isDrawing) {
    updateTempConnection(event)
  }
}

// 显示算子详情
const showOperatorDetailsHandler = (operator) => {
  currentOperator.value = operator
  showOperatorDetail.value = true
}

// 执行工作流
const executeWorkflowHandler = async () => {
  await executeWorkflow(executionLogs, executionOutput, showExecutionResult)
}

// 添加操作日志
const addOperationLog = (type, message) => {
  operationLogs.value.push({
    timestamp: Date.now(),
    type,
    message
  })
  
  // 保持日志数量在合理范围内
  if (operationLogs.value.length > 100) {
    operationLogs.value.splice(0, 50)
  }
}

// 键盘事件处理
const onKeyDown = (event) => {
  if (event.key === 'Delete') {
    if (selectedConnection.value) {
      deleteConnection(selectedConnection.value)
    } else if (selectedNode.value) {
      removeNode(selectedNode.value.id)
    }
  } else if (event.key === 'Escape') {
    if (tempConnection.value.isDrawing) {
      tempConnection.value.isDrawing = false
      addOperationLog('info', '取消连线操作')
    }
  } else if (event.ctrlKey && event.key === 's') {
    event.preventDefault()
    saveWorkflow()
  }
}

// 生命周期钩子
onMounted(async () => {
  await loadOperatorCategories()
  document.addEventListener('keydown', onKeyDown)
  addOperationLog('info', '算子设计器启动')
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeyDown)
  document.removeEventListener('mousemove', onDocumentMouseMove)
  document.removeEventListener('mouseup', onDocumentMouseUp)
})
</script>

<style scoped>
.operator-designer {
  height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.toolbar {
  height: 60px;
  background: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.operator-detail {
  padding: 20px;
}

.operator-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.operator-icon {
  font-size: 32px;
  color: #409eff;
  margin-top: 4px;
}

.operator-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.operator-info p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.operator-config {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.operator-config h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.operator-config pre {
  margin: 0;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  overflow-x: auto;
}

.execution-result {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-height: 70vh;
}

.execution-logs,
.execution-output {
  flex: 1;
}

.execution-logs h4,
.execution-output h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.log-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8f9fa;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 12px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-bottom: 1px solid #e4e7ed;
}

.log-item:last-child {
  border-bottom: none;
}

.log-item.info {
  background: rgba(64, 158, 255, 0.1);
}

.log-item.success {
  background: rgba(82, 196, 26, 0.1);
}

.log-item.warning {
  background: rgba(250, 173, 20, 0.1);
}

.log-item.error {
  background: rgba(245, 108, 108, 0.1);
}

.log-timestamp {
  color: #909399;
  white-space: nowrap;
  font-size: 11px;
}

.log-message {
  color: #303133;
  flex: 1;
}

.execution-output pre {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  max-height: 300px;
  overflow: auto;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    height: auto;
    padding: 10px;
    gap: 10px;
  }
  
  .toolbar-left,
  .toolbar-right {
    width: 100%;
    justify-content: center;
  }
  
  .main-content {
    flex-direction: column;
  }
  
  .execution-result {
    max-height: 60vh;
  }
}

/* 滚动条样式 */
.log-list::-webkit-scrollbar,
.execution-output pre::-webkit-scrollbar {
  width: 6px;
}

.log-list::-webkit-scrollbar-track,
.execution-output pre::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.log-list::-webkit-scrollbar-thumb,
.execution-output pre::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.log-list::-webkit-scrollbar-thumb:hover,
.execution-output pre::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 