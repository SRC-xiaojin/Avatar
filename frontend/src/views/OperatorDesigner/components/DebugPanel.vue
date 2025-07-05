<template>
  <!-- 连线调试面板 -->
  <div v-if="show" class="debug-panel">
    <div class="debug-header">
      <h4>连线调试面板</h4>
      <el-button size="small" @click="$emit('toggle-debug-panel')">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
    <div class="debug-content">
      <div class="debug-section">
        <h5>连线状态</h5>
        <div class="debug-item">
          <span class="label">正在连线:</span>
          <span class="value">{{ tempConnection.isDrawing ? '是' : '否' }}</span>
        </div>
        <div v-if="tempConnection.isDrawing" class="debug-item">
          <span class="label">源节点:</span>
          <span class="value">{{ tempConnection.sourceNode?.name }}</span>
        </div>
        <div v-if="tempConnection.isDrawing" class="debug-item">
          <span class="label">连接类型:</span>
          <span class="value">{{ tempConnection.sourceType }}</span>
        </div>
      </div>
      
      <div class="debug-section">
        <h5>连接统计</h5>
        <div class="debug-item">
          <span class="label">节点数量:</span>
          <span class="value">{{ canvasNodes.length }}</span>
        </div>
        <div class="debug-item">
          <span class="label">连线数量:</span>
          <span class="value">{{ connections.length }}</span>
        </div>
        <div class="debug-item">
          <span class="label">选中节点:</span>
          <span class="value">{{ selectedNode?.name || '无' }}</span>
        </div>
        <div class="debug-item">
          <span class="label">选中连线:</span>
          <span class="value">{{ selectedConnection ? `${getNodeName(selectedConnection.sourceNodeId)} → ${getNodeName(selectedConnection.targetNodeId)}` : '无' }}</span>
        </div>
      </div>
      
      <div class="debug-section">
        <h5>连线列表</h5>
        <div class="connections-list">
          <div 
            v-for="conn in connections" 
            :key="conn.id"
            class="connection-item"
            :class="{ active: selectedConnection?.id === conn.id }"
            @click="$emit('select-connection', conn)"
          >
            <span class="connection-text">
              {{ getNodeName(conn.sourceNodeId) }} → {{ getNodeName(conn.targetNodeId) }}
            </span>
            <el-button 
              size="small" 
              type="danger" 
              link 
              @click.stop="$emit('delete-connection', conn)"
            >
              删除
            </el-button>
          </div>
          <div v-if="connections.length === 0" class="no-connections">
            暂无连线
          </div>
        </div>
      </div>

      <!-- 性能监控 -->
      <div class="debug-section">
        <h5>性能监控</h5>
        <div class="debug-item">
          <span class="label">渲染节点:</span>
          <span class="value">{{ canvasNodes.length }}</span>
        </div>
        <div class="debug-item">
          <span class="label">渲染连线:</span>
          <span class="value">{{ connections.length }}</span>
        </div>
        <div class="debug-item">
          <span class="label">SVG元素:</span>
          <span class="value">{{ connections.length * 3 + (tempConnection.isDrawing ? 1 : 0) }}</span>
        </div>
      </div>

      <!-- 操作日志（最近10条） -->
      <div class="debug-section" v-if="operationLogs.length > 0">
        <h5>操作日志</h5>
        <div class="operation-logs">
          <div 
            v-for="(log, index) in operationLogs.slice(-10)" 
            :key="index"
            class="log-item"
            :class="log.type"
          >
            <span class="log-time">{{ formatTime(log.timestamp) }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue'

// 定义props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  tempConnection: {
    type: Object,
    default: () => ({
      isDrawing: false,
      sourceNode: null,
      sourceType: null
    })
  },
  canvasNodes: {
    type: Array,
    default: () => []
  },
  connections: {
    type: Array,
    default: () => []
  },
  selectedNode: {
    type: Object,
    default: null
  },
  selectedConnection: {
    type: Object,
    default: null
  },
  operationLogs: {
    type: Array,
    default: () => []
  }
})

// 定义emits
const emit = defineEmits([
  'toggle-debug-panel',
  'select-connection',
  'delete-connection'
])

// 获取节点名称
const getNodeName = (nodeId) => {
  const node = props.canvasNodes.find(n => n.id === nodeId)
  return node ? node.name : '未知节点'
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { 
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
</script>

<style scoped>
/* 调试面板样式 */
.debug-panel {
  position: fixed;
  top: 80px;
  right: 20px;
  width: 350px;
  max-height: 600px;
  background: white;
  border: 1px solid #e6e6e6;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.debug-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px 12px 0 0;
}

.debug-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.debug-content {
  padding: 16px;
  max-height: 520px;
  overflow-y: auto;
}

.debug-section {
  margin-bottom: 20px;
}

.debug-section:last-child {
  margin-bottom: 0;
}

.debug-section h5 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}

.debug-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.debug-item:last-child {
  margin-bottom: 0;
}

.debug-item .label {
  color: #909399;
  font-weight: 500;
}

.debug-item .value {
  color: #303133;
  font-weight: 600;
  text-align: right;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.connections-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #f8f9fa;
}

.connection-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
  cursor: pointer;
  transition: background-color 0.2s;
}

.connection-item:last-child {
  border-bottom: none;
}

.connection-item:hover {
  background: #e6f7ff;
}

.connection-item.active {
  background: #1890ff;
  color: white;
}

.connection-item.active .connection-text {
  color: white;
}

.connection-text {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

.no-connections {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.operation-logs {
  max-height: 150px;
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #f8f9fa;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 11px;
}

.log-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 6px 12px;
  border-bottom: 1px solid #e4e7ed;
}

.log-item:last-child {
  border-bottom: none;
}

.log-item.success {
  background: rgba(82, 196, 26, 0.1);
  border-left: 3px solid #52c41a;
}

.log-item.warning {
  background: rgba(250, 173, 20, 0.1);
  border-left: 3px solid #faad14;
}

.log-item.error {
  background: rgba(245, 108, 108, 0.1);
  border-left: 3px solid #f56c6c;
}

.log-item.info {
  background: rgba(64, 158, 255, 0.1);
  border-left: 3px solid #409eff;
}

.log-time {
  color: #909399;
  white-space: nowrap;
  font-size: 10px;
  min-width: 50px;
}

.log-message {
  flex: 1;
  color: #303133;
  line-height: 1.4;
  word-break: break-word;
}

/* 滚动条样式 */
.debug-content::-webkit-scrollbar,
.connections-list::-webkit-scrollbar,
.operation-logs::-webkit-scrollbar {
  width: 6px;
}

.debug-content::-webkit-scrollbar-track,
.connections-list::-webkit-scrollbar-track,
.operation-logs::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.debug-content::-webkit-scrollbar-thumb,
.connections-list::-webkit-scrollbar-thumb,
.operation-logs::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.debug-content::-webkit-scrollbar-thumb:hover,
.connections-list::-webkit-scrollbar-thumb:hover,
.operation-logs::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .debug-panel {
    width: 90%;
    right: 5%;
    top: 70px;
    max-height: 70vh;
  }
}
</style> 