// Vue相关类型导入
import type { Ref } from 'vue'

// 基础类型定义
export interface Position {
  x: number
  y: number
}

export interface Size {
  width: number
  height: number
}

export type OperatorType = 'INPUT' | 'OUTPUT' | 'TRANSFORM' | 'FILTER' | 'AGGREGATION' | 'CONDITION' | 'CUSTOM'

// 本地使用的算子类型（与UI组件兼容）
export interface UIOperatorTemplate {
  id: number
  templateId: number
  name: string
  description: string
  icon: string
  type: OperatorType
  categoryId: number
  config?: Record<string, any>
}

export interface UIOperatorCategory {
  id: number
  title: string
  type: string
  operators: UIOperatorTemplate[]
}

// 节点相关类型
export interface CanvasNode {
  id: number
  type: OperatorType
  name: string
  description: string
  icon: string
  templateId: number
  categoryId: number
  x: number
  y: number
  config: Record<string, any>
  dbId?: number
}

// 连接相关类型
export interface Connection {
  id: number
  sourceNodeId: number
  targetNodeId: number
  type?: ConnectionType
}

export type ConnectionType = 'data' | 'control' | 'event' | 'DATA_FLOW'

export type ConnectionPointType = 'input' | 'output'

// 临时连接状态
export interface TempConnection {
  isDrawing: boolean
  sourceNode: CanvasNode | null
  sourceType: ConnectionPointType | null
  startX: number
  startY: number
  currentX: number
  currentY: number
}

// 拖拽状态
export interface NodeDragState {
  isDragging: boolean
  startX: number
  startY: number
  nodeStartX: number
  nodeStartY: number
}



// 执行日志类型
export interface ExecutionLog {
  timestamp: string
  level: 'info' | 'success' | 'warning' | 'error'
  message: string
}

// 操作日志类型
export interface OperationLog {
  timestamp: number
  type: 'info' | 'success' | 'warning' | 'error'
  message: string
}

// 事件相关类型
export interface DragEvent extends Event {
  dataTransfer: DataTransfer | null
  clientX: number
  clientY: number
}

export interface MouseEventWithTarget extends MouseEvent {
  target: HTMLElement
}

// 图标映射类型
export type IconMap = Record<string, any>

// Composables返回类型
export interface UseWorkflowDesignerReturn {
  // 响应式数据
  operatorCategories: Ref<UIOperatorCategory[]>
  loading: Ref<boolean>
  collapsedCategories: Ref<Set<number>>
  canvasNodes: Ref<CanvasNode[]>
  selectedNode: Ref<CanvasNode | null>
  nextNodeId: Ref<number>
  canvasSize: Ref<Size>
  connections: Ref<Connection[]>
  selectedConnection: Ref<Connection | null>
  hoveredConnection: Ref<Connection | null>
  nextConnectionId: Ref<number>
  currentWorkflowId: Ref<number | null>
  
  // 方法
  toggleCategory: (categoryId: number) => void
  isCategoryCollapsed: (categoryId: number) => boolean
  showOperatorDetails: (operator: UIOperatorTemplate) => void
  getDefaultConfig: (type: string) => Record<string, any>
  loadOperatorCategories: () => Promise<void>
  loadWorkflow: (workflowId: number) => Promise<void>
  checkAndLoadWorkflow: () => Promise<void>
  findNearestConnectionPoint: (x: number, y: number, excludeNodeId?: number) => { node: CanvasNode; type: ConnectionPointType } | null
  canCreateConnection: (sourceNodeId: number, targetNodeId: number, sourceType: ConnectionPointType, targetType: ConnectionPointType, showWarning?: boolean) => boolean
  showConnectionError: (message: string, details?: string) => void
  selectConnection: (connection: Connection) => void
  deleteConnection: (connection: Connection) => void
  onConnectionHover: (connection: Connection, event: Event) => void
  onConnectionLeave: () => void
  deleteSelectedConnection: () => void
  onConnectionTypeChange: (type: ConnectionType) => void
  onNodePropertyChange: (property: string, value: any) => void
  onNodeConfigChange: (key: string, value: any) => void
}

export interface UseConnectionManagerReturn {
  tempConnection: Ref<TempConnection>
  hoveredConnectionPoint: Ref<{ node: CanvasNode; type: ConnectionPointType } | null>
  startConnection: (event: MouseEvent, node: CanvasNode, type: ConnectionPointType) => void
  endConnection: (event: MouseEvent) => void
  updateTempConnection: (event: MouseEvent) => void
  onConnectionPointEnter: (event: MouseEvent, node: CanvasNode, type: ConnectionPointType) => void
  onConnectionPointLeave: (event: MouseEvent, node: CanvasNode, type: ConnectionPointType) => void
  getConnectionPath: (connection: Connection) => string
  getConnectionMarker: (connection: Connection) => string
  getConnectionStrokeWidth: (connection: Connection) => number
  getConnectionColor: (connection: Connection) => string
  getTempConnectionPath: () => string
}

export interface UseCanvasOperationsReturn {
  // 状态
  draggingNode: Ref<CanvasNode | null>
  nodeDragState: Ref<NodeDragState>
  isSaving: Ref<boolean>
  
  // 方法
  onOperatorDragStart: (event: DragEvent, operator: UIOperatorTemplate) => void
  onCanvasDragOver: (event: DragEvent) => void
  onCanvasDrop: (event: DragEvent) => void
  selectNode: (node: CanvasNode) => void
  onCanvasClick: (event: MouseEvent) => void
  removeNode: (nodeId: number) => void
  startNodeDrag: (event: MouseEvent, node: CanvasNode) => void
  onDocumentMouseMove: (event: MouseEvent) => void
  onDocumentMouseUp: () => void
  saveWorkflow: () => Promise<void>
  clearCanvas: () => Promise<void>
  executeWorkflow: (executionLogs: Ref<ExecutionLog[]>, executionOutput: Ref<string>, showExecutionResult: Ref<boolean>) => Promise<void>
  executeNode: (node: CanvasNode, executionLogs: Ref<ExecutionLog[]>) => Promise<void>
  getExecutionOrder: () => number[]
} 