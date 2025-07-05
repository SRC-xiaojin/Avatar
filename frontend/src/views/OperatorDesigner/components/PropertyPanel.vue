<template>
  <div class="property-panel">
    <div class="panel-header">
      <h4>属性配置</h4>
    </div>
    <div class="panel-content">
      <div v-if="selectedNode" class="node-properties">
        <el-form :model="selectedNode" label-width="80px">
          <el-form-item label="名称">
            <el-input v-model="selectedNode.name" @change="$emit('node-property-change', 'name', selectedNode.name)" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input 
              v-model="selectedNode.description" 
              type="textarea" 
              :rows="3"
              @change="$emit('node-property-change', 'description', selectedNode.description)"
            />
          </el-form-item>
          
          <!-- 转换算子配置 -->
          <div v-if="selectedNode.type === 'TRANSFORM'">
            <el-form-item label="字段映射">
              <el-input 
                v-model="selectedNode.config.fieldMapping"
                type="textarea"
                placeholder='{"sourceField": "targetField"}'
                :rows="4"
                @change="$emit('node-config-change', 'fieldMapping', selectedNode.config.fieldMapping)"
              />
            </el-form-item>
            <el-form-item label="类型转换">
              <el-input 
                v-model="selectedNode.config.typeConversion"
                type="textarea"
                placeholder='{"field": "targetType"}'
                :rows="3"
                @change="$emit('node-config-change', 'typeConversion', selectedNode.config.typeConversion)"
              />
            </el-form-item>
          </div>

          <!-- 条件算子配置 -->
          <div v-if="selectedNode.type === 'CONDITION'">
            <el-form-item label="条件表达式">
              <el-input 
                v-model="selectedNode.config.condition"
                placeholder="例: age > 18 && status == 'active'"
                @change="$emit('node-config-change', 'condition', selectedNode.config.condition)"
              />
            </el-form-item>
          </div>

          <!-- 通用配置项 -->
          <div v-if="selectedNode.config && Object.keys(selectedNode.config).length > 0">
            <el-divider>配置参数</el-divider>
            <div v-for="(value, key) in selectedNode.config" :key="key" class="config-item">
              <el-form-item :label="formatConfigKey(key)">
                <el-input 
                  v-if="typeof value === 'string'"
                  v-model="selectedNode.config[key]"
                  @change="$emit('node-config-change', key, selectedNode.config[key])"
                />
                <el-input-number 
                  v-else-if="typeof value === 'number'"
                  v-model="selectedNode.config[key]"
                  @change="$emit('node-config-change', key, selectedNode.config[key])"
                />
                <el-switch 
                  v-else-if="typeof value === 'boolean'"
                  v-model="selectedNode.config[key]"
                  @change="$emit('node-config-change', key, selectedNode.config[key])"
                />
                <el-input 
                  v-else
                  v-model="selectedNode.config[key]"
                  type="textarea"
                  :rows="2"
                  @change="$emit('node-config-change', key, selectedNode.config[key])"
                />
              </el-form-item>
            </div>
          </div>

          <!-- 节点统计信息 -->
          <el-divider>节点信息</el-divider>
          <div class="node-stats">
            <div class="stat-item">
              <span class="stat-label">节点ID:</span>
              <span class="stat-value">{{ selectedNode.id }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">模板ID:</span>
              <span class="stat-value">{{ selectedNode.templateId }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">节点类型:</span>
              <span class="stat-value">{{ selectedNode.type }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">位置:</span>
              <span class="stat-value">({{ Math.round(selectedNode.x) }}, {{ Math.round(selectedNode.y) }})</span>
            </div>
          </div>
        </el-form>
      </div>
      
      <!-- 连接线属性 -->
      <div v-else-if="selectedConnection" class="connection-properties">
        <el-form label-width="80px">
          <el-form-item label="连接ID">
            <el-input :value="selectedConnection.id" readonly />
          </el-form-item>
          <el-form-item label="源节点">
            <el-input :value="getNodeName(selectedConnection.sourceNodeId)" readonly />
          </el-form-item>
          <el-form-item label="目标节点">
            <el-input :value="getNodeName(selectedConnection.targetNodeId)" readonly />
          </el-form-item>
          <el-form-item label="连接类型">
            <el-select 
              v-model="selectedConnection.type" 
              placeholder="请选择连接类型"
              @change="$emit('connection-type-change', selectedConnection.type)"
            >
              <el-option label="数据流" value="data" />
              <el-option label="控制流" value="control" />
              <el-option label="事件流" value="event" />
            </el-select>
          </el-form-item>
          
          <el-divider>连接操作</el-divider>
          <el-button 
            type="danger" 
            @click="$emit('delete-selected-connection')"
            icon="Delete"
            style="width: 100%"
          >
            删除连接
          </el-button>
        </el-form>
      </div>
      
      <!-- 空状态 -->
      <div v-else class="no-selection">
        <el-icon class="no-selection-icon"><Edit /></el-icon>
        <p>请选择一个算子节点或连接线来编辑属性</p>
        <div class="selection-tips">
          <h5>操作提示：</h5>
          <ul>
            <li>点击节点可编辑节点属性</li>
            <li>点击连线可编辑连接属性</li>
            <li>双击算子可查看详情</li>
            <li>拖拽算子到画布添加节点</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Delete, Edit } from '@element-plus/icons-vue'

// 定义props
const props = defineProps({
  selectedNode: {
    type: Object,
    default: null
  },
  selectedConnection: {
    type: Object,
    default: null
  },
  canvasNodes: {
    type: Array,
    default: () => []
  }
})

// 定义emits
const emit = defineEmits([
  'node-property-change',
  'node-config-change',
  'connection-type-change',
  'delete-selected-connection'
])

// 获取节点名称
const getNodeName = (nodeId) => {
  const node = props.canvasNodes.find(n => n.id === nodeId)
  return node ? node.name : '未知节点'
}

// 格式化配置键名
const formatConfigKey = (key) => {
  // 将驼峰命名转换为可读的标签
  return key.replace(/([A-Z])/g, ' $1')
    .replace(/^./, str => str.toUpperCase())
    .trim()
}
</script>

<style scoped>
.property-panel {
  width: 320px;
  background: white;
  border-left: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.panel-header {
  height: 50px;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
}

.panel-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
}

.node-properties,
.connection-properties {
  padding: 16px;
}

.config-item {
  margin-bottom: 12px;
}

.node-stats {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 12px;
  margin-top: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.stat-item:last-child {
  margin-bottom: 0;
}

.stat-label {
  color: #909399;
  font-weight: 500;
}

.stat-value {
  color: #303133;
  font-weight: 600;
  text-align: right;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-selection {
  padding: 32px 16px;
  text-align: center;
  color: #909399;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.no-selection-icon {
  font-size: 48px;
  color: #dcdfe6;
  margin-bottom: 16px;
}

.no-selection p {
  margin: 0 0 24px 0;
  font-size: 14px;
  line-height: 1.6;
}

.selection-tips {
  background: #f0f9ff;
  border: 1px solid #bae7ff;
  border-radius: 6px;
  padding: 16px;
  text-align: left;
  width: 100%;
  max-width: 280px;
}

.selection-tips h5 {
  margin: 0 0 12px 0;
  color: #1890ff;
  font-size: 14px;
  font-weight: 600;
}

.selection-tips ul {
  margin: 0;
  padding-left: 16px;
  color: #606266;
}

.selection-tips li {
  margin-bottom: 6px;
  font-size: 13px;
  line-height: 1.4;
}

.selection-tips li:last-child {
  margin-bottom: 0;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  color: #606266;
  font-weight: 500;
  font-size: 13px;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-textarea__inner) {
  border-radius: 6px;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 12px;
}

:deep(.el-divider__text) {
  background-color: white;
  color: #409eff;
  font-weight: 600;
  font-size: 13px;
}

:deep(.el-divider) {
  margin: 20px 0 16px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .property-panel {
    width: 280px;
  }
}

@media (max-width: 768px) {
  .property-panel {
    width: 100%;
    height: 200px;
  }
  
  .no-selection {
    padding: 16px;
  }
  
  .no-selection-icon {
    font-size: 32px;
  }
  
  .selection-tips {
    max-width: 100%;
  }
}
</style> 