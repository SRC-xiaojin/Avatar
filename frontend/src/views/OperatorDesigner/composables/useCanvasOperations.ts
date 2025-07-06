import { ref, onMounted, onBeforeUnmount } from 'vue'
import type { Ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { workflowApi } from '@/api/workflows.ts'
import type { 
  CanvasNode, 
  Connection, 
  UIOperatorTemplate, 
  DragEvent, 
  NodeDragState, 
  ExecutionLog,
  UseCanvasOperationsReturn,
  Size
} from '../types'
import type {
  WorkflowSaveRequest,
  WorkflowSaveData,
  NodeSaveData,
  ConnectionSaveData
} from '@/types/api'

export function useCanvasOperations(
  canvasNodes: Ref<CanvasNode[]>,
  connections: Ref<Connection[]>,
  selectedNode: Ref<CanvasNode | null>,
  selectedConnection: Ref<Connection | null>,
  nextNodeId: Ref<number>,
  nextConnectionId: Ref<number>,
  currentWorkflowId: Ref<number | null>,
  canvasSize: Ref<Size>,
  canvasRef: Ref<any>,
  getDefaultConfig: (type: string) => any
): UseCanvasOperationsReturn {
  const route = useRoute()
  const router = useRouter()

  // 拖拽状态
  const draggingNode: Ref<CanvasNode | null> = ref(null)
  const nodeDragState: Ref<NodeDragState> = ref({
    isDragging: false,
    startX: 0,
    startY: 0,
    nodeStartX: 0,
    nodeStartY: 0
  })
  
  // 保存状态
  const isSaving: Ref<boolean> = ref(false)

  // 算子拖拽开始
  const onOperatorDragStart = (event: DragEvent, operator: UIOperatorTemplate): void => {
    if (event.dataTransfer) {
      const dragData = JSON.stringify(operator)
      event.dataTransfer.setData('application/json', dragData)
      event.dataTransfer.effectAllowed = 'copy'
      
      console.log('开始拖拽算子:', {
        算子名称: operator.name,
        算子类型: operator.type,
        算子ID: operator.id,
        拖拽数据: dragData
      })
    } else {
      console.error('无法获取 dataTransfer 对象')
    }
  }

  // 画布拖拽悬停
  const onCanvasDragOver = (event: DragEvent): void => {
    event.preventDefault()
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'copy'
    }
  }

  // 画布拖拽放置
  const onCanvasDrop = (event: DragEvent): void => {
    event.preventDefault()
    
    console.log('onCanvasDrop触发:', {
      事件类型: event.type,
      canvasRef: canvasRef.value,
      canvasElement: canvasRef.value?.canvasElement
    })
    
    const canvasComponent = canvasRef.value as any
    const canvasElement = canvasComponent?.canvasElement
    if (!canvasElement) {
      console.error('无法获取画布元素引用:', {
        canvasComponent,
        canvasElement: canvasComponent?.canvasElement
      })
      ElMessage.error('画布引用失败，请刷新页面重试')
      return
    }
    
    try {
      const data = event.dataTransfer?.getData('application/json')
      if (!data) {
        console.error('无法获取拖拽数据')
        return
      }
      
      console.log('拖拽数据:', data)
      
      const operator: UIOperatorTemplate = JSON.parse(data)
      const rect = canvasElement.getBoundingClientRect()
      
      const x = event.clientX - rect.left
      const y = event.clientY - rect.top
      
      console.log('计算位置:', {
        鼠标位置: { x: event.clientX, y: event.clientY },
        画布位置: { left: rect.left, top: rect.top },
        相对位置: { x, y }
      })
      
      // 创建新节点
      const newNode: CanvasNode = {
        id: nextNodeId.value++,
        type: operator.type,
        name: operator.name,
        description: operator.description,
        icon: operator.icon,
        templateId: operator.templateId,
        categoryId: operator.categoryId,
        x: x - 100,
        y: y - 40,
        config: getDefaultConfig ? { ...getDefaultConfig(operator.type), ...operator.config } : { ...operator.config }
      }
      
      console.log('准备创建节点:', newNode)
      
      canvasNodes.value.push(newNode)
      selectedNode.value = newNode
      selectedConnection.value = null
      
      console.log('算子拖拽创建节点成功:', {
        节点ID: newNode.id,
        节点名称: newNode.name,
        位置: { x: newNode.x, y: newNode.y },
        总节点数: canvasNodes.value.length,
        canvasNodes数组: canvasNodes.value
      })
      
      ElMessage.success(`已添加节点: ${newNode.name}`)
      
    } catch (error) {
      console.error('拖拽数据解析失败:', error)
      ElMessage.error('拖拽操作失败')
    }
  }

  // 选择节点
  const selectNode = (node: CanvasNode): void => {
    selectedNode.value = node
    selectedConnection.value = null
    console.log('选中节点:', {
      节点ID: node.id,
      节点名称: node.name,
      节点类型: node.type
    })
  }

  // 画布点击事件
  const onCanvasClick = (event: MouseEvent): void => {
    const target = event.target as HTMLElement
    
    // 检查是否点击在节点上
    if (target.closest('.canvas-node')) {
      return
    }
    
    // 检查是否点击在连线上
    if (target.closest('.connection-line')) {
      return
    }
    
    // 点击空白区域，取消选择
    selectedNode.value = null
    selectedConnection.value = null
    
    console.log('点击空白区域，取消选择')
  }

  // 移除节点
  const removeNode = (nodeId: number): void => {
    const nodeIndex = canvasNodes.value.findIndex(node => node.id === nodeId)
    if (nodeIndex === -1) return
    
    const node = canvasNodes.value[nodeIndex]
    
    // 移除相关连接
    const relatedConnections = connections.value.filter(conn => 
      conn.sourceNodeId === nodeId || conn.targetNodeId === nodeId
    )
    
    relatedConnections.forEach(conn => {
      const connIndex = connections.value.findIndex(c => c.id === conn.id)
      if (connIndex !== -1) {
        connections.value.splice(connIndex, 1)
      }
    })
    
    // 移除节点
    canvasNodes.value.splice(nodeIndex, 1)
    
    // 清除选择
    if (selectedNode.value?.id === nodeId) {
      selectedNode.value = null
    }
    
    console.log('删除节点:', {
      节点ID: nodeId,
      节点名称: node.name,
      删除连接数: relatedConnections.length,
      剩余节点数: canvasNodes.value.length
    })
    
    ElMessage.success(`已删除节点: ${node.name}`)
  }

  // 开始拖拽节点
  const startNodeDrag = (event: MouseEvent, node: CanvasNode): void => {
    draggingNode.value = node
    nodeDragState.value = {
      isDragging: true,
      startX: event.clientX,
      startY: event.clientY,
      nodeStartX: node.x,
      nodeStartY: node.y
    }
    
    console.log('开始拖拽节点:', {
      节点ID: node.id,
      节点名称: node.name,
      开始位置: { x: event.clientX, y: event.clientY }
    })
  }

  // 文档鼠标移动事件
  const onDocumentMouseMove = (event: MouseEvent): void => {
    if (!nodeDragState.value.isDragging || !draggingNode.value) return
    
    const deltaX = event.clientX - nodeDragState.value.startX
    const deltaY = event.clientY - nodeDragState.value.startY
    
    draggingNode.value.x = nodeDragState.value.nodeStartX + deltaX
    draggingNode.value.y = nodeDragState.value.nodeStartY + deltaY
    
    // 限制在画布范围内
    draggingNode.value.x = Math.max(0, Math.min(canvasSize.value.width - 200, draggingNode.value.x))
    draggingNode.value.y = Math.max(0, Math.min(canvasSize.value.height - 120, draggingNode.value.y))
  }

  // 文档鼠标释放事件
  const onDocumentMouseUp = (): void => {
    if (nodeDragState.value.isDragging && draggingNode.value) {
      console.log('节点拖拽结束:', {
        节点ID: draggingNode.value.id,
        节点名称: draggingNode.value.name,
        最终位置: { x: draggingNode.value.x, y: draggingNode.value.y }
      })
    }
    
    draggingNode.value = null
    nodeDragState.value.isDragging = false
  }

  // 保存工作流
  const saveWorkflow = async (): Promise<void> => {
    if (isSaving.value) return
    
    isSaving.value = true
    
    try {
      console.log('开始保存工作流...')
      
      // 1. 构建工作流基本数据
      const workflowData: WorkflowSaveData = {
        id: currentWorkflowId.value || undefined,
        workflowName: `设计器工作流_${Date.now()}`,
        workflowCode: `WORKFLOW_${Date.now()}`,
        description: '通过可视化设计器创建的工作流',
        status: 'DRAFT',
        version: '1.0.0',
        executionMode: 'SYNC',
        maxExecutionTime: 300
      }
      
      // 2. 构建节点数据
      const nodeDataList: NodeSaveData[] = canvasNodes.value.map(node => ({
        nodeCode: `NODE_${node.id}`,
        nodeName: node.name,
        templateId: node.templateId,
        positionX: node.x,
        positionY: node.y,
        width: 100,
        height: 60,
        isEnabled: true,
        executionOrder: 0,
        onError: 'STOP',
        retryCount: 0,
        nodeConfig: JSON.stringify(node.config),
        canvasNodeId: node.id
      }))
      
      // 3. 构建连线数据
      const connectionDataList: ConnectionSaveData[] = connections.value.map(connection => ({
        sourceCanvasNodeId: connection.sourceNodeId,
        targetCanvasNodeId: connection.targetNodeId,
        connectionType: connection.type || 'DATA_FLOW'
      }))
      
      // 4. 构建完整保存请求
      const saveRequest: WorkflowSaveRequest = {
        workflow: workflowData,
        nodes: nodeDataList,
        connections: connectionDataList
      }
      
      console.log('保存请求数据:', {
        工作流: workflowData,
        节点数量: nodeDataList.length,
        连线数量: connectionDataList.length,
        完整请求: saveRequest
      })
      
      // 5. 调用统一保存接口
      const result = await workflowApi.saveCompleteWorkflow(saveRequest)
      
      if (!result.success) {
        throw new Error(result.message || '保存工作流失败')
      }
      
      // 6. 更新当前工作流ID
      if (result.data?.id) {
        currentWorkflowId.value = result.data.id
      }
      
      console.log('工作流保存成功:', {
        工作流ID: result.data?.id,
        工作流名称: result.data?.workflowName,
        节点数量: nodeDataList.length,
        连线数量: connectionDataList.length
      })
      
      ElMessage.success(`工作流保存成功！节点: ${nodeDataList.length}个，连接: ${connectionDataList.length}个`)
      
    } catch (error) {
      console.error('保存工作流失败:', error)
      ElMessage.error('保存工作流失败: ' + (error as Error).message)
    } finally {
      isSaving.value = false
    }
  }

  // 清空画布
  const clearCanvas = async (): Promise<void> => {
    try {
      await ElMessageBox.confirm(
        '确定要清空画布吗？这将删除所有节点和连接，此操作不可恢复。',
        '清空画布',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      canvasNodes.value = []
      connections.value = []
      selectedNode.value = null
      selectedConnection.value = null
      nextNodeId.value = 1
      nextConnectionId.value = 1
      
      console.log('画布已清空')
      ElMessage.success('画布已清空')
      
    } catch (error) {
      console.log('取消清空画布')
    }
  }

  // 执行工作流
  const executeWorkflow = async (
    executionLogs: Ref<ExecutionLog[]>,
    executionOutput: Ref<string>,
    showExecutionResult: Ref<boolean>
  ): Promise<void> => {
    if (canvasNodes.value.length === 0) {
      ElMessage.warning('请先添加节点')
      return
    }
    
    if (connections.value.length === 0 && canvasNodes.value.length > 1) {
      ElMessage.warning('请先建立节点连接')
      return
    }
    
    executionLogs.value = []
    executionOutput.value = ''
    showExecutionResult.value = true
    
    try {
      console.log('开始执行工作流...')
      
      executionLogs.value.push({
        timestamp: new Date().toLocaleTimeString(),
        level: 'info',
        message: '开始执行工作流'
      })
      
      // 获取执行顺序
      const executionOrder = getExecutionOrder()
      
      executionLogs.value.push({
        timestamp: new Date().toLocaleTimeString(),
        level: 'info',
        message: `执行顺序: ${executionOrder.map(id => canvasNodes.value.find(n => n.id === id)?.name).join(' → ')}`
      })
      
      // 按顺序执行节点
      for (const nodeId of executionOrder) {
        const node = canvasNodes.value.find(n => n.id === nodeId)
        if (node) {
          await executeNode(node, executionLogs)
        }
      }
      
      executionLogs.value.push({
        timestamp: new Date().toLocaleTimeString(),
        level: 'success',
        message: '工作流执行完成'
      })
      
      executionOutput.value = '工作流执行完成，所有节点已成功处理。'
      
    } catch (error) {
      executionLogs.value.push({
        timestamp: new Date().toLocaleTimeString(),
        level: 'error',
        message: `执行失败: ${(error as Error).message}`
      })
      
      executionOutput.value = `执行失败: ${(error as Error).message}`
      console.error('工作流执行失败:', error)
    }
  }

  // 执行单个节点
  const executeNode = async (node: CanvasNode, executionLogs: Ref<ExecutionLog[]>): Promise<void> => {
    return new Promise((resolve) => {
      executionLogs.value.push({
        timestamp: new Date().toLocaleTimeString(),
        level: 'info',
        message: `开始执行节点: ${node.name}`
      })
      
      // 模拟异步执行
      setTimeout(() => {
        executionLogs.value.push({
          timestamp: new Date().toLocaleTimeString(),
          level: 'success',
          message: `节点 ${node.name} 执行成功`
        })
        
        resolve()
      }, 1000 + Math.random() * 2000)
    })
  }

  // 获取执行顺序
  const getExecutionOrder = (): number[] => {
    const visited = new Set<number>()
    const order: number[] = []
    
    // 深度优先遍历获取拓扑排序
    const dfs = (nodeId: number): void => {
      if (visited.has(nodeId)) return
      visited.add(nodeId)
      
      // 找到所有依赖的节点
      const dependencies = connections.value
        .filter(conn => conn.targetNodeId === nodeId)
        .map(conn => conn.sourceNodeId)
      
      dependencies.forEach(depId => dfs(depId))
      order.push(nodeId)
    }
    
    // 从所有节点开始遍历
    canvasNodes.value.forEach(node => dfs(node.id))
    
    return order
  }

  // 监听文档事件
  onMounted(() => {
    document.addEventListener('mousemove', onDocumentMouseMove)
    document.addEventListener('mouseup', onDocumentMouseUp)
  })

  onBeforeUnmount(() => {
    document.removeEventListener('mousemove', onDocumentMouseMove)
    document.removeEventListener('mouseup', onDocumentMouseUp)
  })

  return {
    // 状态
    draggingNode,
    nodeDragState,
    isSaving,
    
    // 方法
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
  }
} 