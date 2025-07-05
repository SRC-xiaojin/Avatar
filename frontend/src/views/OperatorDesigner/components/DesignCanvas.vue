<template>
  <div class="design-canvas">
    <div 
      class="canvas-container"
      ref="canvasRef"
      @drop="$emit('canvas-drop', $event)"
      @dragover="$emit('canvas-drag-over', $event)"
      @mouseup="$emit('canvas-mouse-up', $event)"
      @mousemove="$emit('canvas-mouse-move', $event)"
      @click="$emit('canvas-click', $event)"
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
            :d="props.getConnectionPath(connection)"
            class="connection-line"
            @click="$emit('select-connection', connection)"
            @mouseenter="$emit('connection-hover', connection, $event)"
            @mouseleave="$emit('connection-leave')"
            :class="{ 
              'selected': selectedConnection?.id === connection.id,
              'hovered': hoveredConnection?.id === connection.id
            }"
            :marker-end="props.getConnectionMarker(connection)"
            :stroke-width="props.getConnectionStrokeWidth(connection)"
            :stroke="props.getConnectionColor(connection)"
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
          
          <!-- 隐藏的路径用于动画引用 -->
          <path
            :id="`connection-path-${index}`"
            :d="props.getConnectionPath(connection)"
            fill="none"
            stroke="none"
            style="display: none;"
          />
        </g>
        
        <!-- 临时连接线（拖拽中） -->
        <path
          v-if="tempConnection.isDrawing"
          :d="props.getTempConnectionPath()"
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
        @click="$emit('select-node', node)"
        @mousedown="$emit('start-node-drag', $event, node)"
        :class="{ 
          'selected': selectedNode?.id === node.id,
          'dragging': draggingNode?.id === node.id
        }"
      >
        <!-- 输入连接点 -->
        <div 
          class="connection-point input-point"
          :class="{
            'drawing-connection': tempConnection.isDrawing,
            'can-connect': tempConnection.isDrawing && tempConnection.sourceNode && props.canCreateConnection(tempConnection.sourceNode.id, node.id, tempConnection.sourceType, 'input', false),
            'cannot-connect': tempConnection.isDrawing && tempConnection.sourceNode && !props.canCreateConnection(tempConnection.sourceNode.id, node.id, tempConnection.sourceType, 'input', false)
          }"
          :data-node-id="node.id"
          :data-type="'input'"
          @mousedown.stop="$emit('start-connection', $event, node, 'input')"
          @mouseenter="$emit('connection-point-enter', $event, node, 'input')"
          @mouseleave="$emit('connection-point-leave', $event, node, 'input')"
          title="输入端口"
        >
          <div class="point-inner"></div>
          <div class="point-hitarea" :data-node-id="node.id" :data-type="'input'"></div>
        </div>
        
        <div class="node-header" @mousedown.stop="$emit('start-node-drag', $event, node)">
          <el-icon>
            <component :is="node.icon" />
          </el-icon>
          <span class="node-title">{{ node.name }}</span>
          <el-button 
            type="danger" 
            size="small" 
            circle 
            @click.stop="$emit('remove-node', node.id)"
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
          :class="{
            'drawing-connection': tempConnection.isDrawing,
            'can-connect': tempConnection.isDrawing && tempConnection.sourceNode && props.canCreateConnection(tempConnection.sourceNode.id, node.id, tempConnection.sourceType, 'output', false),
            'cannot-connect': tempConnection.isDrawing && tempConnection.sourceNode && !props.canCreateConnection(tempConnection.sourceNode.id, node.id, tempConnection.sourceType, 'output', false)
          }"
          :data-node-id="node.id"
          :data-type="'output'"
          @mousedown.stop="$emit('start-connection', $event, node, 'output')"
          @mouseenter="$emit('connection-point-enter', $event, node, 'output')"
          @mouseleave="$emit('connection-point-leave', $event, node, 'output')"
          title="输出端口"
        >
          <div class="point-inner"></div>
          <div class="point-hitarea" :data-node-id="node.id" :data-type="'output'"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Close } from '@element-plus/icons-vue'

// 定义props
const props = defineProps({
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
  hoveredConnection: {
    type: Object,
    default: null
  },
  draggingNode: {
    type: Object,
    default: null
  },
  tempConnection: {
    type: Object,
    default: () => ({
      isDrawing: false,
      sourceNode: null,
      sourceType: null,
      startX: 0,
      startY: 0,
      currentX: 0,
      currentY: 0
    })
  },
  canvasSize: {
    type: Object,
    default: () => ({ width: 2000, height: 1200 })
  },
  // 函数类型的props
  getConnectionPath: {
    type: Function,
    required: true
  },
  getConnectionMarker: {
    type: Function,
    required: true
  },
  getConnectionStrokeWidth: {
    type: Function,
    required: true
  },
  getConnectionColor: {
    type: Function,
    required: true
  },
  getTempConnectionPath: {
    type: Function,
    required: true
  },
  canCreateConnection: {
    type: Function,
    required: true
  }
})

// 定义emits
defineEmits([
  'canvas-drop',
  'canvas-drag-over',
  'canvas-mouse-up',
  'canvas-mouse-move',
  'canvas-click',
  'select-node',
  'start-node-drag',
  'remove-node',
  'start-connection',
  'connection-point-enter',
  'connection-point-leave',
  'select-connection',
  'connection-hover',
  'connection-leave'
])

// 画布引用
const canvasRef = ref(null)

// 暴露给父组件  
defineExpose({
  canvasElement: canvasRef
})
</script>

<style scoped>
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

/* 连接点样式 - 改进版 */
.connection-point {
  position: absolute;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #409eff;
  border: 3px solid white;
  cursor: crosshair;
  transition: all 0.3s ease;
  z-index: 3;
  opacity: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.canvas-node:hover .connection-point,
.connection-point.drawing-connection {
  opacity: 1;
}

.connection-point:hover {
  background: #1890ff;
  transform: scale(1.3);
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.6);
  border-color: #ffffff;
  border-width: 4px;
}

.connection-point.input-point {
  left: -10px;
  top: 50%;
  transform: translateY(-50%);
}

.connection-point.output-point {
  right: -10px;
  top: 50%;
  transform: translateY(-50%);
}

.connection-point.output-point:hover {
  transform: translateY(-50%) scale(1.3);
}

.connection-point.input-point:hover {
  transform: translateY(-50%) scale(1.3);
}

/* 连接点内部圆点 */
.point-inner {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.2s ease;
  z-index: 4;
}

.connection-point:hover .point-inner {
  width: 10px;
  height: 10px;
  background: #ffffff;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
}

/* 连接点感应区域 */
.point-hitarea {
  position: absolute;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 5;
  background: transparent;
  cursor: crosshair;
}

/* 连接点状态反馈 */
.connection-point.can-connect {
  background: #52c41a;
  border-color: #ffffff;
  box-shadow: 0 0 20px rgba(82, 196, 26, 0.8);
  animation: pulse-connect 1s ease-in-out infinite;
}

.connection-point.cannot-connect {
  background: #f56c6c;
  border-color: #ffffff;
  box-shadow: 0 0 20px rgba(245, 108, 108, 0.8);
  animation: pulse-warning 1s ease-in-out infinite;
}

@keyframes pulse-connect {
  0%, 100% {
    transform: translateY(-50%) scale(1.3);
    box-shadow: 0 0 20px rgba(82, 196, 26, 0.8);
  }
  50% {
    transform: translateY(-50%) scale(1.5);
    box-shadow: 0 0 30px rgba(82, 196, 26, 1);
  }
}

@keyframes pulse-warning {
  0%, 100% {
    transform: translateY(-50%) scale(1.3);
    box-shadow: 0 0 20px rgba(245, 108, 108, 0.8);
  }
  50% {
    transform: translateY(-50%) scale(1.5);
    box-shadow: 0 0 30px rgba(245, 108, 108, 1);
  }
}

/* 正在连线时的连接点状态 */
.connection-point.drawing-connection {
  opacity: 1;
  transform: scale(1.1);
  box-shadow: 0 0 15px rgba(64, 158, 255, 0.5);
  animation: drawing-pulse 1.5s ease-in-out infinite;
}

.connection-point.drawing-connection.input-point {
  transform: translateY(-50%) scale(1.1);
}

.connection-point.drawing-connection.output-point {
  transform: translateY(-50%) scale(1.1);
}

/* 连线时的脉动效果 */
@keyframes drawing-pulse {
  0%, 100% {
    box-shadow: 0 0 15px rgba(64, 158, 255, 0.3);
  }
  50% {
    box-shadow: 0 0 25px rgba(64, 158, 255, 0.7);
  }
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
</style> 