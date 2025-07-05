<template>
  <div class="operator-designer">
    <div class="designer-header">
      <div class="header-left">
        <h3>算子编排设计器</h3>
        <div v-if="currentWorkflowId" class="workflow-info">
          <el-tag type="success" size="small">
            <el-icon><DocumentChecked /></el-icon>
            工作流ID: {{ currentWorkflowId }}
          </el-tag>
        </div>
      </div>
      <div class="header-right">
        <el-button 
          type="primary" 
          @click="saveWorkflow"
          :loading="isSaving"
          :disabled="canvasNodes.length === 0"
        >
          <el-icon v-if="!isSaving"><Document /></el-icon>
          {{ isSaving ? '保存中...' : '保存编排' }}
        </el-button>
        <el-button type="success" @click="executeWorkflow">
          <el-icon><VideoPlay /></el-icon>
          执行编排
        </el-button>
        <el-button 
          type="warning" 
          @click="clearCanvas"
          :disabled="canvasNodes.length === 0 && connections.length === 0"
        >
          <el-icon><Delete /></el-icon>
          清空画布
        </el-button>
      </div>
    </div>

    <div class="designer-content">
      <!-- 左侧算子面板 -->
      <div class="operator-panel">
        <div class="panel-header">
          <h4>算子库</h4>
        </div>
        <div class="panel-content">
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-state">
            <el-icon class="is-loading"><Loading /></el-icon>
            <p>正在加载算子模板...</p>
          </div>
          
          <!-- 空状态 -->
          <div v-else-if="operatorCategories.length === 0" class="empty-state">
            <el-icon><Box /></el-icon>
            <p>暂无可用的算子模板</p>
          </div>
          
          <!-- 算子分类列表 -->
          <div v-else class="operator-category" v-for="category in operatorCategories" :key="category.id">
            <h5 
              class="category-title"
              @click="toggleCategory(category.id)"
              :class="{ 'collapsed': isCategoryCollapsed(category.id) }"
            >
              <el-icon class="collapse-icon">
                <ArrowDown v-if="!isCategoryCollapsed(category.id)" />
                <ArrowRight v-else />
              </el-icon>
              <el-icon><component :is="iconMap[category.type] || 'Operation'" /></el-icon>
              {{ category.title }}
              <el-tag size="small" type="info">{{ category.operators.length }}</el-tag>
            </h5>
            
            <transition name="collapse">
              <div 
                v-show="!isCategoryCollapsed(category.id)" 
                class="operator-list"
              >
                <div 
                  class="operator-item" 
                  v-for="operator in category.operators" 
                  :key="operator.id"
                  draggable="true"
                  @dragstart="onOperatorDragStart($event, operator)"
                  @dblclick="showOperatorDetails(operator)"
                  :title="operator.description"
                >
                  <el-icon>
                    <component :is="operator.icon" />
                  </el-icon>
                  <div class="operator-info">
                    <span class="operator-name">{{ operator.name }}</span>
                    <span class="operator-desc">{{ operator.description }}</span>
                  </div>
                  <div class="operator-actions">
                    <el-button 
                      size="small" 
                      type="primary" 
                      link
                      @click.stop="showOperatorDetails(operator)"
                      title="查看详情"
                    >
                      <el-icon><InfoFilled /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>

      <!-- 中央设计画布 -->
      <div class="design-canvas">
        <div 
          class="canvas-container"
          ref="canvasRef"
          @drop="onCanvasDrop"
          @dragover="onCanvasDragOver"
          @mouseup="onCanvasMouseUp"
          @mousemove="onCanvasMouseMove"
          @click="onCanvasClick"
        >
          <!-- SVG连线层 -->
          <svg 
            class="connections-layer" 
            :width="canvasSize.width" 
            :height="canvasSize.height"
          >
            <!-- 定义箭头标记 -->
            <defs>
              <!-- 箭头阴影滤镜 -->
              <filter id="arrow-shadow" x="-50%" y="-50%" width="200%" height="200%">
                <feDropShadow dx="1" dy="1" stdDeviation="1" flood-color="#000000" flood-opacity="0.2"/>
              </filter>
              
              <filter id="arrow-shadow-hover" x="-50%" y="-50%" width="200%" height="200%">
                <feDropShadow dx="2" dy="2" stdDeviation="2" flood-color="#000000" flood-opacity="0.3"/>
              </filter>
              
              <filter id="arrow-shadow-selected" x="-50%" y="-50%" width="200%" height="200%">
                <feDropShadow dx="2" dy="2" stdDeviation="3" flood-color="#000000" flood-opacity="0.4"/>
              </filter>
              
              <!-- 数据流动指示器 -->
              <marker
                id="flow-indicator"
                markerWidth="8"
                markerHeight="8"
                refX="4"
                refY="4"
                orient="auto"
                markerUnits="strokeWidth"
              >
                <circle
                  cx="4"
                  cy="4"
                  r="2"
                  fill="#409eff"
                  opacity="0.8"
                >
                  <animate
                    attributeName="r"
                    values="2;3;2"
                    dur="1.5s"
                    repeatCount="indefinite"
                  />
                  <animate
                    attributeName="opacity"
                    values="0.8;1;0.8"
                    dur="1.5s"
                    repeatCount="indefinite"
                  />
                </circle>
              </marker>
              <!-- 普通连接线箭头 -->
              <marker
                id="arrowhead"
                markerWidth="20"
                markerHeight="20"
                refX="18"
                refY="10"
                orient="auto"
                markerUnits="userSpaceOnUse"
                viewBox="0 0 20 20"
              >
                <path
                  d="M 2 2 L 18 10 L 2 18 L 6 10 Z"
                  fill="#409eff"
                  stroke="#ffffff"
                  stroke-width="1.5"
                  filter="url(#arrow-shadow)"
                />
              </marker>
              
              <!-- 悬停状态箭头 -->
              <marker
                id="arrowhead-hover"
                markerWidth="24"
                markerHeight="24"
                refX="22"
                refY="12"
                orient="auto"
                markerUnits="userSpaceOnUse"
                viewBox="0 0 24 24"
              >
                <path
                  d="M 2 2 L 22 12 L 2 22 L 7 12 Z"
                  fill="#1890ff"
                  stroke="#ffffff"
                  stroke-width="2"
                  filter="url(#arrow-shadow-hover)"
                />
              </marker>
              
              <!-- 选中状态箭头 -->
              <marker
                id="arrowhead-selected"
                markerWidth="28"
                markerHeight="28"
                refX="26"
                refY="14"
                orient="auto"
                markerUnits="userSpaceOnUse"
                viewBox="0 0 28 28"
              >
                <path
                  d="M 2 2 L 26 14 L 2 26 L 8 14 Z"
                  fill="#f56c6c"
                  stroke="#ffffff"
                  stroke-width="2.5"
                  filter="url(#arrow-shadow-selected)"
                />
              </marker>
              
              <!-- 临时连接线箭头 -->
              <marker
                id="arrowhead-temp"
                markerWidth="22"
                markerHeight="22"
                refX="20"
                refY="11"
                orient="auto"
                markerUnits="userSpaceOnUse"
                viewBox="0 0 22 22"
              >
                <path
                  d="M 2 2 L 20 11 L 2 20 L 7 11 Z"
                  fill="#909399"
                  stroke="#ffffff"
                  stroke-width="1.5"
                  opacity="0.9"
                  stroke-dasharray="3,3"
                />
              </marker>
            </defs>
            
            <!-- 已建立的连接线 -->
            <g v-for="(connection, index) in connections" :key="`${connection.sourceNodeId}-${connection.targetNodeId}`">
              <!-- 主连接线 -->
              <path
                :d="getConnectionPath(connection)"
                class="connection-line"
                @click="selectConnection(connection)"
                @mouseenter="onConnectionHover(connection, $event)"
                @mouseleave="onConnectionLeave()"
                :class="{ 
                  'selected': selectedConnection?.id === connection.id,
                  'hovered': hoveredConnection?.id === connection.id
                }"
                :marker-end="getConnectionMarker(connection)"
                :stroke-width="getConnectionStrokeWidth(connection)"
                :stroke="getConnectionColor(connection)"
                fill="none"
              />
              
              <!-- 主要数据流动粒子 -->
              <circle
                r="3"
                :fill="hoveredConnection?.id === connection.id ? '#1890ff' : '#409eff'"
                opacity="0.9"
                class="flow-particle primary-flow"
              >
                <animateMotion
                  :dur="hoveredConnection?.id === connection.id ? '1.5s' : '2.5s'"
                  repeatCount="indefinite"
                  begin="0s"
                >
                  <mpath :href="`#connection-path-${index}`"/>
                </animateMotion>
                <animate
                  attributeName="r"
                  :values="hoveredConnection?.id === connection.id ? '3;5;3' : '2;4;2'"
                  :dur="hoveredConnection?.id === connection.id ? '1.5s' : '2.5s'"
                  repeatCount="indefinite"
                />
                <animate
                  attributeName="opacity"
                  values="0.4;1;0.4"
                  :dur="hoveredConnection?.id === connection.id ? '1.5s' : '2.5s'"
                  repeatCount="indefinite"
                />
              </circle>
              
              <!-- 次要流动粒子 -->
              <circle
                r="2"
                :fill="hoveredConnection?.id === connection.id ? '#52c41a' : '#66d9ef'"
                opacity="0.7"
                class="flow-particle secondary-flow"
              >
                <animateMotion
                  :dur="hoveredConnection?.id === connection.id ? '2s' : '3s'"
                  repeatCount="indefinite"
                  begin="0.8s"
                >
                  <mpath :href="`#connection-path-${index}`"/>
                </animateMotion>
                <animate
                  attributeName="opacity"
                  values="0.2;0.8;0.2"
                  :dur="hoveredConnection?.id === connection.id ? '2s' : '3s'"
                  repeatCount="indefinite"
                />
              </circle>
              
              <!-- 流向指示器（在连线中点） -->
              <g class="flow-indicator" v-if="hoveredConnection?.id === connection.id || selectedConnection?.id === connection.id">
                <circle
                  :cx="getConnectionMidpoint(connection).x"
                  :cy="getConnectionMidpoint(connection).y"
                  r="8"
                  fill="rgba(24, 144, 255, 0.1)"
                  stroke="#1890ff"
                  stroke-width="2"
                  class="indicator-background"
                >
                  <animate
                    attributeName="r"
                    values="6;10;6"
                    dur="2s"
                    repeatCount="indefinite"
                  />
                  <animate
                    attributeName="stroke-opacity"
                    values="0.3;1;0.3"
                    dur="2s"
                    repeatCount="indefinite"
                  />
                </circle>
                
                <!-- 箭头指示 -->
                <path
                  :d="getDirectionArrow(connection)"
                  fill="#1890ff"
                  stroke="white"
                  stroke-width="1"
                  class="direction-arrow"
                >
                  <animateTransform
                    attributeName="transform"
                    type="scale"
                    values="0.8;1.2;0.8"
                    dur="2s"
                    repeatCount="indefinite"
                    :transform-origin="`${getConnectionMidpoint(connection).x} ${getConnectionMidpoint(connection).y}`"
                  />
                </path>
              </g>
              
              <!-- 数据流量指示器 -->
              <text
                v-if="selectedConnection?.id === connection.id"
                :x="getConnectionMidpoint(connection).x"
                :y="getConnectionMidpoint(connection).y - 15"
                text-anchor="middle"
                class="flow-label"
                fill="#1890ff"
                font-size="12"
                font-weight="bold"
              >
                数据流
                <animate
                  attributeName="opacity"
                  values="0.5;1;0.5"
                  dur="1.5s"
                  repeatCount="indefinite"
                />
              </text>
              
              <!-- 额外的末端箭头指示器（确保箭头可见） -->
              <g class="endpoint-arrow" v-if="hoveredConnection?.id === connection.id || selectedConnection?.id === connection.id">
                <circle
                  :cx="getConnectionEndpoint(connection).x"
                  :cy="getConnectionEndpoint(connection).y"
                  r="12"
                  :fill="selectedConnection?.id === connection.id ? 'rgba(245, 108, 108, 0.1)' : 'rgba(24, 144, 255, 0.1)'"
                  :stroke="selectedConnection?.id === connection.id ? '#f56c6c' : '#1890ff'"
                  stroke-width="2"
                  stroke-dasharray="4,4"
                  opacity="0.7"
                >
                  <animate
                    attributeName="r"
                    values="10;14;10"
                    dur="2s"
                    repeatCount="indefinite"
                  />
                  <animate
                    attributeName="stroke-dashoffset"
                    values="0;8"
                    dur="1s"
                    repeatCount="indefinite"
                  />
                </circle>
                
                <!-- 文字指示 -->
                <text
                  :x="getConnectionEndpoint(connection).x"
                  :y="getConnectionEndpoint(connection).y + 25"
                  text-anchor="middle"
                  font-size="10"
                  :fill="selectedConnection?.id === connection.id ? '#f56c6c' : '#1890ff'"
                  font-weight="bold"
                  class="endpoint-label"
                >
                  终点
                </text>
              </g>
              
              <!-- 隐藏的路径用于动画引用 -->
              <path
                :id="`connection-path-${index}`"
                :d="getConnectionPath(connection)"
                fill="none"
                stroke="none"
                style="display: none;"
              />
            </g>
            
            <!-- 临时连接线（拖拽中） -->
            <path
              v-if="tempConnection.isDrawing"
              :d="getTempConnectionPath()"
              class="temp-connection-line"
              marker-end="url(#arrowhead-temp)"
            />
          </svg>

          <!-- 算子节点 -->
          <div
            v-for="node in canvasNodes"
            :key="node.id"
            class="canvas-node"
            :style="{
              left: node.x + 'px',
              top: node.y + 'px'
            }"
            @click="selectNode(node)"
            @mousedown="startNodeDrag($event, node)"
            :class="{ 
              'selected': selectedNode?.id === node.id,
              'dragging': draggingNode?.id === node.id
            }"
          >
            <!-- 输入连接点 -->
            <div 
              class="connection-point input-point"
              :data-node-id="node.id"
              :data-type="'input'"
              @mousedown.stop="startConnection($event, node, 'input')"
              title="输入端口"
            >
              <div class="point-inner"></div>
            </div>
            
            <div class="node-header" @mousedown.stop="startNodeDrag($event, node)">
              <el-icon>
                <component :is="node.icon" />
              </el-icon>
              <span class="node-title">{{ node.name }}</span>
              <el-button 
                type="danger" 
                size="small" 
                circle 
                @click.stop="removeNode(node.id)"
                @mousedown.stop
                class="node-delete"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <div class="node-content">
              <div class="node-description">{{ node.description }}</div>
            </div>
            
            <!-- 输出连接点 -->
            <div 
              class="connection-point output-point"
              :data-node-id="node.id"
              :data-type="'output'"
              @mousedown.stop="startConnection($event, node, 'output')"
              title="输出端口"
            >
              <div class="point-inner"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="property-panel">
        <div class="panel-header">
          <h4>属性配置</h4>
        </div>
        <div class="panel-content">
          <div v-if="selectedNode" class="node-properties">
            <el-form :model="selectedNode" label-width="80px">
              <el-form-item label="名称">
                <el-input v-model="selectedNode.name" />
              </el-form-item>
              <el-form-item label="描述">
                <el-input 
                  v-model="selectedNode.description" 
                  type="textarea" 
                  :rows="3"
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
                  />
                </el-form-item>
                <el-form-item label="类型转换">
                  <el-input 
                    v-model="selectedNode.config.typeConversion"
                    type="textarea"
                    placeholder='{"field": "targetType"}'
                    :rows="3"
                  />
                </el-form-item>
              </div>

              <!-- 条件算子配置 -->
              <div v-if="selectedNode.type === 'CONDITION'">
                <el-form-item label="条件表达式">
                  <el-input 
                    v-model="selectedNode.config.condition"
                    placeholder="例: age > 18 && status == 'active'"
                  />
                </el-form-item>
              </div>
            </el-form>
          </div>
          <div v-else class="no-selection">
            <p>请选择一个算子节点来编辑属性</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 执行结果对话框 -->
    <el-dialog
      v-model="showExecutionResult"
      title="执行结果"
      width="60%"
    >
      <el-tabs v-model="activeResultTab">
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
        <el-tab-pane label="输出数据" name="output">
          <el-input
            v-model="executionOutput"
            type="textarea"
            :rows="10"
            readonly
          />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 算子详情对话框 -->
    <TemplateDetailDialog 
      v-model="showOperatorDetailDialog" 
      :template="operatorDetails" 
    />

    <!-- 连接线工具提示 -->
    <el-tooltip
      ref="connectionTooltip"
      :visible="showConnectionTooltip"
      :virtual-ref="tooltipTarget"
      virtual-triggering
      effect="dark"
      placement="top"
      popper-class="connection-tooltip"
    >
      <template #content>
        <div v-if="hoveredConnection" class="tooltip-content">
          <div class="tooltip-title">连接详情</div>
          <div class="tooltip-item">
            <span class="label">源节点:</span>
            <span class="value">{{ getNodeName(hoveredConnection.sourceNodeId) }}</span>
          </div>
          <div class="tooltip-item">
            <span class="label">目标节点:</span>
            <span class="value">{{ getNodeName(hoveredConnection.targetNodeId) }}</span>
          </div>
          <div class="tooltip-item">
            <span class="label">连接状态:</span>
            <span class="value status-active">活跃</span>
          </div>
          <div class="tooltip-item">
            <span class="label">数据流向:</span>
            <span class="value flow-direction">{{ getNodeName(hoveredConnection.sourceNodeId) }} → {{ getNodeName(hoveredConnection.targetNodeId) }}</span>
          </div>
        </div>
      </template>
    </el-tooltip>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  InfoFilled, 
  ArrowDown, 
  ArrowRight, 
  Loading, 
  Box, 
  Document, 
  VideoPlay, 
  Close,
  DataBoard,
  Switch,
  Connection,
  Coin,
  Tools,
  Operation,
  DocumentChecked,
  Delete
} from '@element-plus/icons-vue'
import { categoryApi } from '@/api/categories'
import { templateApi } from '@/api/templates'
import { workflowApi, connectionApi } from '@/api/workflows'
import { nodeApi } from '@/api/nodes'
import TemplateDetailDialog from '@/components/TemplateDetailDialog.vue'

// 路由相关
const route = useRoute()

// 响应式数据
const operatorCategories = ref([])
const loading = ref(false)
const collapsedCategories = ref(new Set()) // 折叠状态管理

// 画布相关
const canvasRef = ref(null)
const canvasNodes = ref([])
const selectedNode = ref(null)
const nextNodeId = ref(1)

// 连线相关
const connections = ref([])
const selectedConnection = ref(null)
const hoveredConnection = ref(null)
const nextConnectionId = ref(1)
const canvasSize = ref({ width: 2000, height: 1200 })

// 临时连线状态
const tempConnection = ref({
  isDrawing: false,
  sourceNode: null,
  sourceType: null,
  startX: 0,
  startY: 0,
  currentX: 0,
  currentY: 0
})

// 节点拖拽状态
const draggingNode = ref(null)
const nodeDragState = ref({
  isDragging: false,
  startX: 0,
  startY: 0,
  nodeStartX: 0,
  nodeStartY: 0
})

// 执行结果
const showExecutionResult = ref(false)
const activeResultTab = ref('logs')
const executionLogs = ref([])
const executionOutput = ref('')

// 算子详情弹窗
const showOperatorDetailDialog = ref(false)
const operatorDetails = ref(null)

// 工具提示相关状态
const showConnectionTooltip = ref(false)
const tooltipTarget = ref(null)
const connectionTooltip = ref(null)

// 工作流保存相关状态
const currentWorkflowId = ref(null)
const isSaving = ref(false)

// 图标映射
const iconMap = {
  'DATA_PROCESSING': 'DataBoard',
  'CONTROL': 'Switch',
  'SERVICE_CALL': 'Connection',
  'DATABASE': 'Coin',
  'FUNCTION': 'Tools'
}

// 加载工作流数据
const loadWorkflowData = async (workflowId) => {
  try {
    ElMessage.info('正在加载工作流数据...')
    
    // 并行获取工作流基本信息、节点和连线
    const [workflowResponse, nodesResponse, connectionsResponse] = await Promise.all([
      workflowApi.getWorkflowById(workflowId),
      nodeApi.getNodesByWorkflow(workflowId),
      connectionApi.getConnectionsByWorkflow(workflowId)
    ])
    
    if (!workflowResponse.success) {
      ElMessage.error('获取工作流信息失败')
      return
    }
    
    // 设置当前工作流ID
    currentWorkflowId.value = workflowId
    
    // 处理节点数据
    if (nodesResponse.success && nodesResponse.data && nodesResponse.data.length > 0) {
      // 获取所有需要的模板信息
      const templateIds = [...new Set(nodesResponse.data.map(node => node.templateId))]
      const templatesMap = new Map()
      
      // 并行获取所有模板信息
      const templatePromises = templateIds.map(async (templateId) => {
        try {
          const templateResponse = await templateApi.getTemplateById(templateId)
          if (templateResponse.success) {
            templatesMap.set(templateId, templateResponse.data)
          }
        } catch (error) {
          console.warn(`获取模板 ${templateId} 信息失败:`, error)
        }
      })
      
      await Promise.all(templatePromises)
      
      // 转换节点数据格式
      const convertedNodes = nodesResponse.data.map(node => {
        const template = templatesMap.get(node.templateId)
        let maxNodeId = Math.max(nextNodeId.value, node.id || 0)
        nextNodeId.value = Math.max(nextNodeId.value, maxNodeId + 1)
        
        return {
          id: node.id || nextNodeId.value++,
          type: template?.templateCode || 'UNKNOWN',
          name: node.nodeName || template?.templateName || '未知节点',
          description: template?.description || '无描述',
          icon: getIconForTemplate(template),
          templateId: node.templateId,
          categoryId: template?.categoryId,
          x: node.positionX || 100,
          y: node.positionY || 100,
          width: node.width || 200,
          height: node.height || 120,
          config: parseNodeConfig(node.nodeConfig) || getDefaultConfig(template),
          originalNodeData: node // 保存原始节点数据，用于后续保存
        }
      })
      
      canvasNodes.value = convertedNodes
      ElMessage.success(`已加载 ${convertedNodes.length} 个节点`)
    }
    
    // 处理连线数据
    if (connectionsResponse.success && connectionsResponse.data && connectionsResponse.data.length > 0) {
      const convertedConnections = connectionsResponse.data.map(conn => {
        let maxConnId = Math.max(nextConnectionId.value, conn.id || 0)
        nextConnectionId.value = Math.max(nextConnectionId.value, maxConnId + 1)
        
        return {
          id: conn.id || nextConnectionId.value++,
          sourceNodeId: conn.sourceNodeId,
          targetNodeId: conn.targetNodeId,
          sourceType: 'output', // 默认输出连接
          targetType: 'input',  // 默认输入连接
          connectionType: conn.connectionType || 'default',
          originalConnectionData: conn // 保存原始连线数据
        }
      })
      
      connections.value = convertedConnections
      ElMessage.success(`已加载 ${convertedConnections.length} 条连线`)
    }
    
    ElMessage.success(`工作流 "${workflowResponse.data.workflowName}" 加载完成！`)
    
  } catch (error) {
    console.error('加载工作流数据失败:', error)
    ElMessage.error('加载工作流数据失败: ' + error.message)
  }
}

// 根据模板获取图标
const getIconForTemplate = (template) => {
  if (!template) return 'Operation'
  
  // 根据类别代码获取图标
  const categoryCode = template.categoryCode || 'FUNCTION'
  return iconMap[categoryCode] || 'Operation'
}

// 解析节点配置
const parseNodeConfig = (nodeConfig) => {
  if (!nodeConfig) return null
  
  try {
    return typeof nodeConfig === 'string' ? JSON.parse(nodeConfig) : nodeConfig
  } catch (error) {
    console.warn('解析节点配置失败:', error)
    return null
  }
}

// 初始化函数
const initializeDesigner = async () => {
  // 首先加载算子数据
  await loadOperatorData()
  
  // 检查URL参数中是否有workflowId
  const workflowId = route.query.workflowId
  if (workflowId) {
    await loadWorkflowData(parseInt(workflowId))
  }
}

// 折叠/展开类别
const toggleCategory = (categoryId) => {
  if (collapsedCategories.value.has(categoryId)) {
    collapsedCategories.value.delete(categoryId)
  } else {
    collapsedCategories.value.add(categoryId)
  }
}

// 检查类别是否折叠
const isCategoryCollapsed = (categoryId) => {
  return collapsedCategories.value.has(categoryId)
}

// 显示算子详情
const showOperatorDetails = async (operator) => {
  try {
    showOperatorDetailDialog.value = true
    operatorDetails.value = null

    // 并行获取模板信息和参数信息
    const [templateResponse, paramsResponse] = await Promise.all([
      templateApi.getTemplateById(operator.templateId),
      templateApi.getTemplateDetails(operator.templateId)
    ])
    
    if (templateResponse.success) {
      // 合并模板信息和参数信息
      operatorDetails.value = {
        ...templateResponse.data,
        params: paramsResponse.success ? paramsResponse.data.params : {}
      }
    } else {
      ElMessage.error('获取算子详情失败')
      showOperatorDetailDialog.value = false
    }
  } catch (error) {
    console.error('获取算子详情失败:', error)
    ElMessage.error('获取算子详情失败')
    showOperatorDetailDialog.value = false
  }
}

// 加载算子类别和模板
const loadOperatorData = async () => {
  try {
    loading.value = true
    
    // 并行加载类别和模板
    const [categoriesResponse, templatesResponse] = await Promise.all([
      categoryApi.getCategories(),
      templateApi.getTemplates()
    ])
    
    if (categoriesResponse.success && templatesResponse.success) {
      const categories = categoriesResponse.data.filter(cat => cat.status)
      const templates = templatesResponse.data.filter(template => template.status)
      
      // 组织数据结构
      operatorCategories.value = categories.map(category => ({
        id: category.id,
        type: category.categoryCode,
        title: category.categoryName,
        description: category.description,
        operators: templates
          .filter(template => template.categoryId === category.id)
          .map(template => ({
            id: template.id,
            type: template.templateCode,
            name: template.templateName,
            icon: iconMap[category.categoryCode] || 'Operation',
            description: template.description,
            templateId: template.id,
            categoryId: template.categoryId,
            executorClass: template.executorClass,
            executorMethod: template.executorMethod,
            configSchema: template.configSchema
          }))
      })).filter(category => category.operators.length > 0)
      
    } else {
      ElMessage.error('加载算子数据失败')
    }
  } catch (error) {
    console.error('加载算子数据失败:', error)
    ElMessage.error('加载算子数据失败')
  } finally {
    loading.value = false
  }
}

// 拖拽处理
const onOperatorDragStart = (event, operator) => {
  event.dataTransfer.setData('operator', JSON.stringify(operator))
}

const onCanvasDragOver = (event) => {
  event.preventDefault()
}

const onCanvasDrop = (event) => {
  event.preventDefault()
  const operatorData = JSON.parse(event.dataTransfer.getData('operator'))
  const rect = canvasRef.value.getBoundingClientRect()
  
  const newNode = {
    id: nextNodeId.value++,
    type: operatorData.type,
    name: operatorData.name,
    description: operatorData.description,
    icon: operatorData.icon,
    templateId: operatorData.templateId,
    categoryId: operatorData.categoryId,
    x: event.clientX - rect.left - 100,
    y: event.clientY - rect.top - 50,
    config: getDefaultConfig(operatorData)
  }
  
  canvasNodes.value.push(newNode)
  ElMessage.success(`已添加${operatorData.name}`)
}

// 获取默认配置
const getDefaultConfig = (operatorData) => {
  try {
    if (operatorData.configSchema) {
      const schema = JSON.parse(operatorData.configSchema)
      // 根据配置模式生成默认值
      const defaultConfig = {}
      if (schema.properties) {
        Object.keys(schema.properties).forEach(key => {
          const prop = schema.properties[key]
          switch (prop.type) {
            case 'string':
              defaultConfig[key] = prop.default || ''
              break
            case 'number':
              defaultConfig[key] = prop.default || 0
              break
            case 'boolean':
              defaultConfig[key] = prop.default || false
              break
            case 'object':
              defaultConfig[key] = prop.default || {}
              break
            case 'array':
              defaultConfig[key] = prop.default || []
              break
            default:
              defaultConfig[key] = prop.default || ''
          }
        })
      }
      return defaultConfig
    }
  } catch (error) {
    console.error('解析配置模式失败:', error)
  }
  
  // 回退到简单的默认配置
  return {}
}

// 节点操作
const selectNode = (node) => {
  selectedNode.value = node
  selectedConnection.value = null
}

// 画布点击事件
const onCanvasClick = (event) => {
  // 如果点击的是画布空白区域，清除所有选中状态
  if (event.target === canvasRef.value || event.target.classList.contains('canvas-container')) {
    selectedNode.value = null
    selectedConnection.value = null
  }
}

const removeNode = (nodeId) => {
  const index = canvasNodes.value.findIndex(n => n.id === nodeId)
  if (index !== -1) {
    canvasNodes.value.splice(index, 1)
    if (selectedNode.value?.id === nodeId) {
      selectedNode.value = null
    }
    // 删除相关连线
    connections.value = connections.value.filter(
      conn => conn.sourceNodeId !== nodeId && conn.targetNodeId !== nodeId
    )
  }
}

// 连线相关函数
const startConnection = (event, node, type) => {
  event.preventDefault()
  const rect = canvasRef.value.getBoundingClientRect()
  
  tempConnection.value = {
    isDrawing: true,
    sourceNode: node,
    sourceType: type,
    startX: event.clientX - rect.left,
    startY: event.clientY - rect.top,
    currentX: event.clientX - rect.left,
    currentY: event.clientY - rect.top
  }
  
  // 阻止节点选择
  event.stopPropagation()
}

const onCanvasMouseMove = (event) => {
  if (tempConnection.value.isDrawing) {
    const rect = canvasRef.value.getBoundingClientRect()
    tempConnection.value.currentX = event.clientX - rect.left
    tempConnection.value.currentY = event.clientY - rect.top
  }
  
  // 处理节点拖拽
  if (nodeDragState.value.isDragging && draggingNode.value) {
    const rect = canvasRef.value.getBoundingClientRect()
    const currentX = event.clientX - rect.left
    const currentY = event.clientY - rect.top
    
    const deltaX = currentX - nodeDragState.value.startX
    const deltaY = currentY - nodeDragState.value.startY
    
    draggingNode.value.x = Math.max(0, Math.min(
      canvasSize.value.width - 200, // 200是节点宽度
      nodeDragState.value.nodeStartX + deltaX
    ))
    draggingNode.value.y = Math.max(0, Math.min(
      canvasSize.value.height - 120, // 120是节点高度
      nodeDragState.value.nodeStartY + deltaY
    ))
  }
}

const onCanvasMouseUp = (event) => {
  // 处理连线结束
  if (tempConnection.value.isDrawing) {
    // 检查是否在连接点上释放
    const target = event.target
    const nodeId = target.getAttribute('data-node-id')
    const type = target.getAttribute('data-type')
    
    if (nodeId && type && tempConnection.value.sourceNode) {
      const targetNodeId = parseInt(nodeId)
      const sourceNodeId = tempConnection.value.sourceNode.id
      
      // 验证连接规则
      if (canCreateConnection(sourceNodeId, targetNodeId, tempConnection.value.sourceType, type)) {
        createConnection(sourceNodeId, targetNodeId, tempConnection.value.sourceType, type)
      }
    }
    
    // 重置临时连线状态
    tempConnection.value = {
      isDrawing: false,
      sourceNode: null,
      sourceType: null,
      startX: 0,
      startY: 0,
      currentX: 0,
      currentY: 0
    }
  }
  
  // 处理节点拖拽结束
  if (nodeDragState.value.isDragging) {
    nodeDragState.value.isDragging = false
    draggingNode.value = null
    
    // 移除全局鼠标事件监听
    document.removeEventListener('mousemove', onDocumentMouseMove)
    document.removeEventListener('mouseup', onDocumentMouseUp)
  }
}

const canCreateConnection = (sourceNodeId, targetNodeId, sourceType, targetType) => {
  // 不能连接自己
  if (sourceNodeId === targetNodeId) {
    ElMessage.warning('不能连接节点自身')
    return false
  }
  
  // 输出只能连接输入，输入只能连接输出
  if (sourceType === targetType) {
    ElMessage.warning('只能从输出端口连接到输入端口')
    return false
  }
  
  // 检查是否已存在连接
  const existingConnection = connections.value.find(
    conn => 
      (conn.sourceNodeId === sourceNodeId && conn.targetNodeId === targetNodeId) ||
      (conn.sourceNodeId === targetNodeId && conn.targetNodeId === sourceNodeId)
  )
  
  if (existingConnection) {
    ElMessage.warning('节点之间已存在连接')
    return false
  }
  
  return true
}

const createConnection = (sourceNodeId, targetNodeId, sourceType, targetType) => {
  // 确保连接方向正确（从输出到输入）
  let finalSourceId, finalTargetId
  if (sourceType === 'output') {
    finalSourceId = sourceNodeId
    finalTargetId = targetNodeId
  } else {
    finalSourceId = targetNodeId
    finalTargetId = sourceNodeId
  }
  
  const newConnection = {
    id: nextConnectionId.value++,
    sourceNodeId: finalSourceId,
    targetNodeId: finalTargetId,
    type: 'data'
  }
  
  connections.value.push(newConnection)
  
  const sourceNode = canvasNodes.value.find(n => n.id === finalSourceId)
  const targetNode = canvasNodes.value.find(n => n.id === finalTargetId)
  ElMessage.success(`已连接: ${sourceNode.name} → ${targetNode.name}`)
}

const getNodeCenter = (nodeId) => {
  const node = canvasNodes.value.find(n => n.id === nodeId)
  if (!node) return { x: 0, y: 0 }
  return {
    x: node.x + 100, // 节点宽度的一半
    y: node.y + 60   // 节点高度的一半
  }
}

const getConnectionPath = (connection) => {
  const sourcePos = getNodeCenter(connection.sourceNodeId)
  const targetPos = getNodeCenter(connection.targetNodeId)
  
  // 计算控制点以创建贝塞尔曲线
  const deltaX = targetPos.x - sourcePos.x
  const controlPoint1X = sourcePos.x + deltaX * 0.5
  const controlPoint2X = targetPos.x - deltaX * 0.5
  
  return `M ${sourcePos.x} ${sourcePos.y} C ${controlPoint1X} ${sourcePos.y}, ${controlPoint2X} ${targetPos.y}, ${targetPos.x} ${targetPos.y}`
}

const getTempConnectionPath = () => {
  const { startX, startY, currentX, currentY } = tempConnection.value
  const deltaX = currentX - startX
  const controlPoint1X = startX + deltaX * 0.5
  const controlPoint2X = currentX - deltaX * 0.5
  
  return `M ${startX} ${startY} C ${controlPoint1X} ${startY}, ${controlPoint2X} ${currentY}, ${currentX} ${currentY}`
}

const getConnectionMarker = (connection) => {
  if (selectedConnection.value?.id === connection.id) {
    return 'url(#arrowhead-selected)'
  }
  if (hoveredConnection.value?.id === connection.id) {
    return 'url(#arrowhead-hover)'
  }
  return 'url(#arrowhead)'
}

// 获取连接线中点坐标
const getConnectionMidpoint = (connection) => {
  const sourcePos = getNodeCenter(connection.sourceNodeId)
  const targetPos = getNodeCenter(connection.targetNodeId)
  
  // 计算贝塞尔曲线中点
  const deltaX = targetPos.x - sourcePos.x
  const controlPoint1X = sourcePos.x + deltaX * 0.5
  const controlPoint2X = targetPos.x - deltaX * 0.5
  
  // 贝塞尔曲线中点计算（t=0.5时的点）
  const t = 0.5
  const x = Math.pow(1-t, 3) * sourcePos.x + 
           3 * Math.pow(1-t, 2) * t * controlPoint1X + 
           3 * (1-t) * Math.pow(t, 2) * controlPoint2X + 
           Math.pow(t, 3) * targetPos.x
           
  const y = Math.pow(1-t, 3) * sourcePos.y + 
           3 * Math.pow(1-t, 2) * t * sourcePos.y + 
           3 * (1-t) * Math.pow(t, 2) * targetPos.y + 
           Math.pow(t, 3) * targetPos.y
  
  return { x, y }
}

// 生成方向箭头路径
const getDirectionArrow = (connection) => {
  const sourcePos = getNodeCenter(connection.sourceNodeId)
  const targetPos = getNodeCenter(connection.targetNodeId)
  const midpoint = getConnectionMidpoint(connection)
  
  // 计算箭头方向（基于连线方向）
  const angle = Math.atan2(targetPos.y - sourcePos.y, targetPos.x - sourcePos.x)
  
  // 箭头大小
  const arrowSize = 6
  
  // 计算箭头的三个点
  const arrowTip = {
    x: midpoint.x + Math.cos(angle) * arrowSize,
    y: midpoint.y + Math.sin(angle) * arrowSize
  }
  
  const arrowBase1 = {
    x: midpoint.x - Math.cos(angle - Math.PI/6) * arrowSize,
    y: midpoint.y - Math.sin(angle - Math.PI/6) * arrowSize
  }
  
  const arrowBase2 = {
    x: midpoint.x - Math.cos(angle + Math.PI/6) * arrowSize,
    y: midpoint.y - Math.sin(angle + Math.PI/6) * arrowSize
  }
  
  return `M ${arrowTip.x} ${arrowTip.y} L ${arrowBase1.x} ${arrowBase1.y} L ${arrowBase2.x} ${arrowBase2.y} Z`
}

const selectConnection = (connection) => {
  // 如果点击的是已选中的连接，则取消选中
  if (selectedConnection.value?.id === connection.id) {
    selectedConnection.value = null
  } else {
    selectedConnection.value = connection
    selectedNode.value = null
  }
}

// 连接线悬停事件
const onConnectionHover = (connection, event) => {
  hoveredConnection.value = connection
  
  // 显示工具提示
  tooltipTarget.value = {
    getBoundingClientRect: () => ({
      x: event.clientX,
      y: event.clientY,
      top: event.clientY,
      left: event.clientX,
      right: event.clientX,
      bottom: event.clientY,
      width: 0,
      height: 0
    })
  }
  showConnectionTooltip.value = true
}

// 连接线离开事件  
const onConnectionLeave = () => {
  hoveredConnection.value = null
  showConnectionTooltip.value = false
  tooltipTarget.value = null
}

// 获取节点名称
const getNodeName = (nodeId) => {
  const node = canvasNodes.value.find(n => n.id === nodeId)
  return node ? node.name : '未知节点'
}

// 获取连接线终点坐标
const getConnectionEndpoint = (connection) => {
  const targetPos = getNodeCenter(connection.targetNodeId)
  // 返回目标节点的左侧连接点位置（输入端口）
  return {
    x: targetPos.x - 100, // 节点宽度的一半
    y: targetPos.y
  }
}

// 获取连接线粗细
const getConnectionStrokeWidth = (connection) => {
  const baseWidth = 2
  const hoverWidth = 3
  const selectedWidth = 4
  
  if (selectedConnection.value?.id === connection.id) {
    return selectedWidth
  }
  if (hoveredConnection.value?.id === connection.id) {
    return hoverWidth
  }
  return baseWidth
}

// 获取连接线颜色
const getConnectionColor = (connection) => {
  if (selectedConnection.value?.id === connection.id) {
    return '#f56c6c' // 红色 - 选中状态
  }
  if (hoveredConnection.value?.id === connection.id) {
    return '#1890ff' // 深蓝色 - 悬停状态
  }
  return '#409eff' // 蓝色 - 普通状态
}

const deleteSelectedConnection = () => {
  if (selectedConnection.value) {
    const index = connections.value.findIndex(c => c.id === selectedConnection.value.id)
    if (index !== -1) {
      connections.value.splice(index, 1)
      selectedConnection.value = null
      ElMessage.success('已删除连接')
    }
  }
}

// 节点拖拽相关函数
const startNodeDrag = (event, node) => {
  // 如果点击的是连接点，不启动节点拖拽
  if (event.target.classList.contains('connection-point') || 
      event.target.classList.contains('point-inner')) {
    return
  }
  
  // 如果点击的是删除按钮，不启动拖拽
  if (event.target.closest('.node-delete')) {
    return
  }
  
  event.preventDefault()
  event.stopPropagation()
  
  const rect = canvasRef.value.getBoundingClientRect()
  
  draggingNode.value = node
  nodeDragState.value = {
    isDragging: true,
    startX: event.clientX - rect.left,
    startY: event.clientY - rect.top,
    nodeStartX: node.x,
    nodeStartY: node.y
  }
  
  // 选中被拖拽的节点
  selectedNode.value = node
  
  // 添加全局鼠标事件监听，确保在画布外也能正常拖拽
  document.addEventListener('mousemove', onDocumentMouseMove)
  document.addEventListener('mouseup', onDocumentMouseUp)
}

// 全局鼠标移动事件处理
const onDocumentMouseMove = (event) => {
  if (nodeDragState.value.isDragging && draggingNode.value) {
    const rect = canvasRef.value.getBoundingClientRect()
    const currentX = event.clientX - rect.left
    const currentY = event.clientY - rect.top
    
    const deltaX = currentX - nodeDragState.value.startX
    const deltaY = currentY - nodeDragState.value.startY
    
    draggingNode.value.x = Math.max(0, Math.min(
      canvasSize.value.width - 200, // 200是节点宽度
      nodeDragState.value.nodeStartX + deltaX
    ))
    draggingNode.value.y = Math.max(0, Math.min(
      canvasSize.value.height - 120, // 120是节点高度
      nodeDragState.value.nodeStartY + deltaY
    ))
  }
}

// 全局鼠标释放事件处理
const onDocumentMouseUp = () => {
  if (nodeDragState.value.isDragging) {
    nodeDragState.value.isDragging = false
    draggingNode.value = null
    
    // 移除全局事件监听
    document.removeEventListener('mousemove', onDocumentMouseMove)
    document.removeEventListener('mouseup', onDocumentMouseUp)
  }
}

// 保存工作流
const saveWorkflow = async () => {
  if (canvasNodes.value.length === 0) {
    ElMessage.warning('请先添加算子节点')
    return
  }
  
  if (isSaving.value) {
    ElMessage.warning('正在保存中，请稍候...')
    return
  }
  
  isSaving.value = true
  
  try {
    // 生成工作流基本信息
    const timestamp = Date.now()
    const workflowData = {
      workflowName: `工作流_${timestamp}`,
      workflowCode: `WF_${timestamp}`,
      description: '通过设计器创建的工作流',
      status: 'DRAFT',
      nodeCount: canvasNodes.value.length
    }
    
    ElMessage.info('正在保存工作流...')
    
    // 第一步：保存工作流基本信息到workflows表
    const workflowResponse = await workflowApi.createWorkflow(workflowData)
    if (!workflowResponse.success) {
      throw new Error('保存工作流失败: ' + workflowResponse.message)
    }
    
    const workflowId = workflowResponse.data.id
    currentWorkflowId.value = workflowId
    
    ElMessage.success(`工作流创建成功，ID: ${workflowId}`)
    
    // 第二步：保存节点到workflow_nodes表
    const nodePromises = canvasNodes.value.map(async (node, index) => {
      const nodeData = {
        workflowId: workflowId,
        nodeCode: `NODE_${workflowId}_${index + 1}`,
        nodeName: node.name,
        templateId: node.templateId,
        positionX: node.x,
        positionY: node.y,
        nodeConfig: JSON.stringify(node.config || {}),
        status: true
      }
      
      const nodeResponse = await nodeApi.createNode(nodeData)
      if (!nodeResponse.success) {
        throw new Error(`保存节点 ${node.name} 失败: ${nodeResponse.message}`)
      }
      
      // 更新本地节点的数据库ID
      node.dbId = nodeResponse.data.id
      return nodeResponse.data
    })
    
    ElMessage.info('正在保存节点...')
    const savedNodes = await Promise.all(nodePromises)
    ElMessage.success(`成功保存 ${savedNodes.length} 个节点`)
    
    // 第三步：保存连接关系到workflow_connections表
    if (connections.value.length > 0) {
      // 创建节点ID映射（canvas ID -> database ID）
      const nodeIdMap = new Map()
      canvasNodes.value.forEach(node => {
        nodeIdMap.set(node.id, node.dbId)
      })
      
      const connectionData = connections.value.map(conn => ({
        workflowId: workflowId,
        sourceNodeId: nodeIdMap.get(conn.sourceNodeId),
        targetNodeId: nodeIdMap.get(conn.targetNodeId),
        connectionType: conn.type || 'DATA_FLOW'
      }))
      
      ElMessage.info('正在保存连接关系...')
      const connectionResponse = await connectionApi.createConnections(connectionData)
      if (!connectionResponse.success) {
        throw new Error('保存连接关系失败: ' + connectionResponse.message)
      }
      
      ElMessage.success(`成功保存 ${connectionData.length} 个连接关系`)
    }
    
    // 保存成功
    ElMessage({
      type: 'success',
      message: `工作流保存完成！
        • 工作流ID: ${workflowId}
        • 节点数量: ${canvasNodes.value.length}
        • 连接数量: ${connections.value.length}`,
      duration: 5000,
      showClose: true
    })
    
    console.log('工作流保存成功:', {
      workflowId,
      workflow: workflowResponse.data,
      nodes: savedNodes,
      connections: connections.value.length
    })
    
  } catch (error) {
    console.error('保存工作流失败:', error)
    ElMessage.error('保存失败: ' + error.message)
    
    // 如果工作流已创建但后续步骤失败，可以考虑回滚
    if (currentWorkflowId.value) {
      ElMessage.warning('工作流创建成功但部分数据保存失败，请检查数据完整性')
    }
  } finally {
    isSaving.value = false
  }
}

// 清空画布
const clearCanvas = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空画布吗？此操作将删除所有节点和连接关系，且无法恢复。',
      '确认清空',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    // 清空画布数据
    canvasNodes.value = []
    connections.value = []
    selectedNode.value = null
    selectedConnection.value = null
    hoveredConnection.value = null
    currentWorkflowId.value = null
    
    // 重置计数器
    nextNodeId.value = 1
    nextConnectionId.value = 1
    
    ElMessage.success('画布已清空')
  } catch (error) {
    // 用户取消操作
    if (error === 'cancel') {
      ElMessage.info('已取消清空操作')
    }
  }
}

// 执行工作流
const executeWorkflow = async () => {
  if (canvasNodes.value.length === 0) {
    ElMessage.warning('请先添加算子节点')
    return
  }
  
  try {
    executionLogs.value = [
      {
        timestamp: new Date().toLocaleTimeString(),
        level: 'info',
        message: '开始执行工作流...'
      },
      {
        timestamp: new Date().toLocaleTimeString(),
        level: 'info',
        message: `共有 ${canvasNodes.value.length} 个算子节点, ${connections.value.length} 个连接`
      }
    ]
    
    // 根据连接关系确定执行顺序
    const executionOrder = getExecutionOrder()
    
    if (executionOrder.length === 0) {
      // 没有连接关系，按添加顺序执行
      for (let i = 0; i < canvasNodes.value.length; i++) {
        const node = canvasNodes.value[i]
        await executeNode(node)
      }
    } else {
      // 按依赖关系执行
      executionLogs.value.push({
        timestamp: new Date().toLocaleTimeString(),
        level: 'info',
        message: '按连接关系确定执行顺序...'
      })
      
      for (const nodeId of executionOrder) {
        const node = canvasNodes.value.find(n => n.id === nodeId)
        if (node) {
          await executeNode(node)
        }
      }
    }
    
    executionLogs.value.push({
      timestamp: new Date().toLocaleTimeString(),
      level: 'success',
      message: '工作流执行完成'
    })
    
    executionOutput.value = JSON.stringify({
      status: 'success',
      executedNodes: canvasNodes.value.length,
      connections: connections.value.length,
      executionOrder: executionOrder.length > 0 ? executionOrder : 'sequential',
      result: 'Execution completed successfully',
      timestamp: new Date().toISOString()
    }, null, 2)
    
    showExecutionResult.value = true
    ElMessage.success('工作流执行完成')
  } catch (error) {
    ElMessage.error('执行失败: ' + error.message)
  }
}

// 执行单个节点
const executeNode = async (node) => {
  await new Promise(resolve => setTimeout(resolve, 500))
  
  executionLogs.value.push({
    timestamp: new Date().toLocaleTimeString(),
    level: 'info',
    message: `正在执行节点: ${node.name}`
  })
}

// 获取执行顺序（拓扑排序）
const getExecutionOrder = () => {
  if (connections.value.length === 0) {
    return []
  }
  
  const visited = new Set()
  const visiting = new Set()
  const order = []
  
  const visit = (nodeId) => {
    if (visiting.has(nodeId)) {
      // 检测到环形依赖
      executionLogs.value.push({
        timestamp: new Date().toLocaleTimeString(),
        level: 'warning',
        message: '检测到环形依赖，将按添加顺序执行'
      })
      return false
    }
    
    if (visited.has(nodeId)) {
      return true
    }
    
    visiting.add(nodeId)
    
    // 访问所有依赖的节点
    const dependencies = connections.value
      .filter(conn => conn.targetNodeId === nodeId)
      .map(conn => conn.sourceNodeId)
    
    for (const depId of dependencies) {
      if (!visit(depId)) {
        return false
      }
    }
    
    visiting.delete(nodeId)
    visited.add(nodeId)
    order.push(nodeId)
    
    return true
  }
  
  // 访问所有节点
  for (const node of canvasNodes.value) {
    if (!visited.has(node.id)) {
      if (!visit(node.id)) {
        return [] // 有环形依赖，返回空数组
      }
    }
  }
  
  return order
}

// 键盘事件处理
const handleKeyDown = (event) => {
  // Delete键删除选中的连接
  if (event.key === 'Delete' && selectedConnection.value) {
    deleteSelectedConnection()
  }
}

// 组件挂载时加载数据
onMounted(() => {
  initializeDesigner()
  
  // 添加键盘事件监听
  document.addEventListener('keydown', handleKeyDown)
})

// 组件卸载时清理事件监听
onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeyDown)
  document.removeEventListener('mousemove', onDocumentMouseMove)
  document.removeEventListener('mouseup', onDocumentMouseUp)
})
</script>

<style scoped>
.operator-designer {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.designer-header {
  height: 60px;
  background: white;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left h3 {
  margin: 0;
  color: #303133;
}

.workflow-info {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.workflow-info .el-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 6px;
}

.designer-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.operator-panel {
  width: 280px;
  background: white;
  border-right: 1px solid #e6e6e6;
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
  padding: 16px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.loading-state .el-icon,
.empty-state .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.loading-state p,
.empty-state p {
  margin: 0;
  font-size: 14px;
}

.operator-category {
  margin-bottom: 24px;
}

.category-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.2s;
  user-select: none;
}

.category-title:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.category-title.collapsed {
  margin-bottom: 0;
}

.category-title .el-tag {
  margin-left: auto;
}

.collapse-icon {
  font-size: 12px;
  transition: transform 0.2s;
  color: #909399;
}

.category-title:hover .collapse-icon {
  color: #409eff;
}

.operator-list {
  overflow: hidden;
}

.operator-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: grab;
  transition: all 0.2s;
  user-select: none;
}

.operator-item:hover {
  background: #e6f7ff;
  border-color: #1890ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.operator-item:hover .operator-actions {
  opacity: 1;
}

.operator-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.operator-item:active {
  cursor: grabbing;
  transform: translateY(0);
}

.operator-item .el-icon {
  font-size: 18px;
  color: #1890ff;
  margin-top: 2px;
  flex-shrink: 0;
}

.operator-info {
  flex: 1;
  min-width: 0;
}

.operator-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.operator-desc {
  display: block;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 折叠动画 */
.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.3s ease;
  transform-origin: top;
}

.collapse-enter-from,
.collapse-leave-to {
  opacity: 0;
  max-height: 0;
  transform: scaleY(0);
}

.collapse-enter-to,
.collapse-leave-from {
  opacity: 1;
  max-height: 1000px;
  transform: scaleY(1);
}

.design-canvas {
  flex: 1;
  background: #f5f5f5;
  position: relative;
  overflow: hidden;
}

.canvas-container {
  width: 100%;
  height: 100%;
  position: relative;
  background-image: 
    radial-gradient(circle, #ddd 1px, transparent 1px);
  background-size: 20px 20px;
  background-position: 0 0, 10px 10px;
}

/* SVG连线层 */
.connections-layer {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 1;
}

.connections-layer path {
  pointer-events: stroke;
}

/* 连接线样式 */
.connection-line {
  fill: none;
  stroke: #409eff;
  stroke-width: 2px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.connection-line.hovered,
.connection-line:hover {
  stroke: #1890ff;
  stroke-width: 3px;
  filter: drop-shadow(0 2px 4px rgba(24, 144, 255, 0.3));
}

.connection-line.selected {
  stroke: #f56c6c;
  stroke-width: 3px;
  filter: drop-shadow(0 2px 6px rgba(245, 108, 108, 0.4));
}

/* 临时连接线样式 */
.temp-connection-line {
  fill: none;
  stroke: #909399;
  stroke-width: 2px;
  stroke-dasharray: 5,5;
  animation: dash 1s linear infinite;
}

@keyframes dash {
  to {
    stroke-dashoffset: -10px;
  }
}

/* 流动粒子动画效果 */
.flow-particle {
  filter: drop-shadow(0 0 3px currentColor);
  transition: all 0.3s ease;
}

.flow-particle.primary-flow {
  filter: drop-shadow(0 0 6px currentColor) drop-shadow(0 0 12px rgba(64, 158, 255, 0.4));
}

.flow-particle.secondary-flow {
  filter: drop-shadow(0 0 4px currentColor) drop-shadow(0 0 8px rgba(102, 217, 239, 0.3));
}

/* 流向指示器样式 */
.flow-indicator {
  pointer-events: none;
}

.indicator-background {
  filter: drop-shadow(0 0 4px rgba(24, 144, 255, 0.3));
}

.direction-arrow {
  filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.3));
}

/* 流量标签样式 */
.flow-label {
  font-family: 'Helvetica Neue', Arial, sans-serif;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
  pointer-events: none;
}

/* 连接线工具提示样式 */
:deep(.connection-tooltip) {
  max-width: 300px;
  padding: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

:deep(.connection-tooltip .el-tooltip__content) {
  padding: 0;
  background: transparent;
  color: white;
}

.tooltip-content {
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.tooltip-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #ffffff;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding-bottom: 8px;
}

.tooltip-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.tooltip-item:last-child {
  margin-bottom: 0;
}

.tooltip-item .label {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  margin-right: 12px;
}

.tooltip-item .value {
  color: #ffffff;
  font-weight: 600;
  text-align: right;
  flex: 1;
}

.tooltip-item .status-active {
  color: #52c41a;
  display: inline-flex;
  align-items: center;
}

.tooltip-item .status-active::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #52c41a;
  margin-right: 6px;
  animation: pulse-dot 2s infinite;
}

.tooltip-item .flow-direction {
  color: #66d9ef;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 13px;
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

/* 末端箭头指示器样式 */
.endpoint-arrow {
  pointer-events: none;
}

.endpoint-arrow circle {
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.2));
}

.endpoint-label {
  font-family: 'Helvetica Neue', Arial, sans-serif;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
  pointer-events: none;
}

/* 箭头标记器增强效果 */
.connection-line[marker-end] {
  /* 确保箭头正确显示 */
  marker-end: inherit;
}

/* 连接线动画增强 */
.connection-line {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.connection-line:hover {
  filter: drop-shadow(0 0 12px currentColor);
}

.connection-line.selected {
  animation: connection-pulse 2s ease-in-out infinite;
}

@keyframes connection-pulse {
  0%, 100% {
    stroke-width: 4px;
    filter: drop-shadow(0 0 8px currentColor);
  }
  50% {
    stroke-width: 5px;
    filter: drop-shadow(0 0 16px currentColor);
  }
}

/* 连接线与流动效果的增强 */
.connection-line {
  transition: all 0.3s ease;
}

.connection-line.hovered,
.connection-line:hover {
  filter: drop-shadow(0 0 8px currentColor);
}

.connection-line.selected {
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    filter: drop-shadow(0 0 8px #f56c6c);
  }
  50% {
    filter: drop-shadow(0 0 16px #f56c6c);
  }
}

.canvas-node {
  position: absolute;
  width: 200px;
  background: white;
  border: 2px solid #e6e6e6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 2;
}

/* 连接点样式 */
.connection-point {
  position: absolute;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #409eff;
  border: 2px solid white;
  cursor: crosshair;
  transition: all 0.2s ease;
  z-index: 3;
  opacity: 0;
}

.canvas-node:hover .connection-point {
  opacity: 1;
}

.connection-point:hover {
  background: #1890ff;
  transform: scale(1.2);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.4);
}

.connection-point.input-point {
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
}

.connection-point.output-point {
  right: -8px;
  top: 50%;
  transform: translateY(-50%);
}

.connection-point.output-point:hover {
  transform: translateY(-50%) scale(1.2);
}

.connection-point.input-point:hover {
  transform: translateY(-50%) scale(1.2);
}

.point-inner {
  width: 6px;
  height: 6px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.canvas-node:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.2);
}

.canvas-node.selected {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.2);
}

.canvas-node.dragging {
  cursor: grabbing;
  transform: rotate(2deg);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  z-index: 10;
}

.canvas-node:not(.dragging) {
  cursor: grab;
}

.canvas-node.dragging * {
  pointer-events: none;
  user-select: none;
}

.node-header {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
}

.node-header .el-icon {
  font-size: 16px;
  color: #1890ff;
}

.node-title {
  flex: 1;
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.node-delete {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  min-height: 20px;
  padding: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.canvas-node:hover .node-delete {
  opacity: 1;
}

.node-content {
  padding: 12px;
}

.node-description {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.property-panel {
  width: 320px;
  background: white;
  border-left: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.node-properties {
  padding: 16px;
}

.no-selection {
  padding: 32px 16px;
  text-align: center;
  color: #909399;
}

.execution-logs {
  max-height: 300px;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.log-item {
  padding: 4px 0;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  gap: 12px;
  font-size: 13px;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: #909399;
  white-space: nowrap;
  font-size: 12px;
}

.log-message {
  flex: 1;
}

.log-item.success .log-message {
  color: #67c23a;
}

.log-item.error .log-message {
  color: #f56c6c;
}

.log-item.warning .log-message {
  color: #e6a23c;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .operator-panel {
    width: 240px;
  }
  
  .property-panel {
    width: 280px;
  }
}

@media (max-width: 768px) {
  .designer-content {
    flex-direction: column;
  }
  
  .operator-panel,
  .property-panel {
    width: 100%;
    height: 200px;
  }
  
  .design-canvas {
    height: 400px;
  }
}


</style> 