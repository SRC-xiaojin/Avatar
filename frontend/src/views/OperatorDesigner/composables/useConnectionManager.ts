import { ref, reactive } from 'vue'
import type { Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { 
  CanvasNode, 
  Connection, 
  ConnectionPointType, 
  TempConnection,
  UseConnectionManagerReturn
} from '../types'

export function useConnectionManager(
  canvasNodes: Ref<CanvasNode[]>,
  connections: Ref<Connection[]>,
  selectedConnection: Ref<Connection | null>,
  hoveredConnection: Ref<Connection | null>,
  nextConnectionId: Ref<number>,
  canvasRef: Ref<any>
): UseConnectionManagerReturn {
  // 临时连线状态
  const tempConnection: Ref<TempConnection> = ref({
    isDrawing: false,
    sourceNode: null,
    sourceType: null,
    startX: 0,
    startY: 0,
    currentX: 0,
    currentY: 0
  })

  // 连接点悬停状态
  const hoveredConnectionPoint: Ref<{ node: CanvasNode; type: ConnectionPointType } | null> = ref(null)

  // 开始连线
  const startConnection = (event: MouseEvent, node: CanvasNode, type: ConnectionPointType): void => {
    event.preventDefault()
    const canvasComponent = canvasRef.value as any
    const canvasElement = canvasComponent?.canvasElement
    if (!canvasElement) {
      console.error('无法获取画布元素引用 - startConnection')
      return
    }
    
    const rect = canvasElement.getBoundingClientRect()
    
    console.log('开始连线:', {
      节点ID: node.id,
      节点名称: node.name,
      连接类型: type,
      鼠标位置: { x: event.clientX - rect.left, y: event.clientY - rect.top }
    })
    
    tempConnection.value = {
      isDrawing: true,
      sourceNode: node,
      sourceType: type,
      startX: event.clientX - rect.left,
      startY: event.clientY - rect.top,
      currentX: event.clientX - rect.left,
      currentY: event.clientY - rect.top
    }
  }

  // 更新临时连线
  const updateTempConnection = (event: MouseEvent): void => {
    if (!tempConnection.value.isDrawing) return

    const canvasComponent = canvasRef.value as any
    const canvasElement = canvasComponent?.canvasElement
    if (!canvasElement) {
      console.error('无法获取画布元素引用 - startConnection')
      return
    }
    
    const rect = canvasElement.getBoundingClientRect()
    tempConnection.value.currentX = event.clientX - rect.left
    tempConnection.value.currentY = event.clientY - rect.top
  }

  // 结束连线
  const endConnection = (event: MouseEvent): void => {
    if (!tempConnection.value.isDrawing || !tempConnection.value.sourceNode) return
    
    const canvasComponent = canvasRef.value as any
    const canvasElement = canvasComponent?.canvasElement
    if (!canvasElement) {
      console.error('无法获取画布元素引用 - startConnection')
      return
    }
    const rect = canvasElement.getBoundingClientRect()
    const endX = event.clientX - rect.left
    const endY = event.clientY - rect.top
    
    console.log('尝试结束连线:', {
      结束位置: { x: endX, y: endY },
      源节点: tempConnection.value.sourceNode.name,
      源类型: tempConnection.value.sourceType
    })
    
    // 直接检测目标元素
    let targetElement: Element | null = null
    let targetNode: CanvasNode | null = null
    let targetType: ConnectionPointType | null = null
    
    const elementAtPoint = document.elementFromPoint(event.clientX, event.clientY)
    
    if (elementAtPoint) {
      // 检查是否点击在连接点上
      const connectionPoint = elementAtPoint.closest('.connection-point')
      if (connectionPoint) {
        const nodeIdAttr = connectionPoint.getAttribute('data-node-id')
        const typeAttr = connectionPoint.getAttribute('data-type')
        
        if (nodeIdAttr && typeAttr) {
          const nodeId = parseInt(nodeIdAttr)
          targetNode = canvasNodes.value.find(n => n.id === nodeId) || null
          targetType = typeAttr as ConnectionPointType
          targetElement = connectionPoint
          
          console.log('直接检测到连接点:', {
            目标节点: targetNode?.name,
            目标类型: targetType,
            元素: targetElement.className
          })
        }
      } else {
        // 检查是否点击在连接点的感应区域上
        const hitarea = elementAtPoint.closest('.point-hitarea')
        if (hitarea) {
          const nodeIdAttr = hitarea.getAttribute('data-node-id')
          const typeAttr = hitarea.getAttribute('data-type')
          
          if (nodeIdAttr && typeAttr) {
            const nodeId = parseInt(nodeIdAttr)
            targetNode = canvasNodes.value.find(n => n.id === nodeId) || null
            targetType = typeAttr as ConnectionPointType
            targetElement = hitarea
            
            console.log('检测到感应区域:', {
              目标节点: targetNode?.name,
              目标类型: targetType,
              元素: targetElement.className
            })
          }
        }
      }
    }
    
    // 如果直接检测失败，尝试位置检测
    if (!targetNode || !targetType) {
      console.log('直接检测失败，尝试位置检测...')
      
      const nearestPoint = findNearestConnectionPoint(endX, endY, tempConnection.value.sourceNode.id)
      if (nearestPoint) {
        targetNode = nearestPoint.node
        targetType = nearestPoint.type
        
        console.log('位置检测成功:', {
          目标节点: targetNode.name,
          目标类型: targetType,
          距离: '智能检测'
        })
      }
    }
    
    // 验证和创建连接
    if (targetNode && targetType && tempConnection.value.sourceNode && tempConnection.value.sourceType) {
      console.log('开始连线验证:', {
        源节点: tempConnection.value.sourceNode.name,
        源类型: tempConnection.value.sourceType,
        目标节点: targetNode.name,
        目标类型: targetType
      })
      
      const canCreate = canCreateConnection(
        tempConnection.value.sourceNode.id,
        targetNode.id,
        tempConnection.value.sourceType,
        targetType,
        true
      )
      
      if (canCreate) {
        const newConnection: Connection = {
          id: nextConnectionId.value++,
          sourceNodeId: tempConnection.value.sourceNode.id,
          targetNodeId: targetNode.id,
          type: 'data'
        }
        
        connections.value.push(newConnection)
        
        console.log('连线创建成功:', {
          连线ID: newConnection.id,
          源节点: tempConnection.value.sourceNode.name,
          目标节点: targetNode.name,
          总连线数: connections.value.length
        })
        
        ElMessage.success({
          message: `连线成功: ${tempConnection.value.sourceNode.name} → ${targetNode.name}`,
          duration: 3000,
          showClose: true
        })
      }
    } else {
      console.log('连线失败 - 未找到有效目标:', {
        检测方法: targetElement ? '直接检测' : '位置检测',
        目标节点: targetNode?.name || '无',
        目标类型: targetType || '无',
        结束位置: { x: endX, y: endY }
      })
      
      ElMessage.warning({
        message: '连线失败：未在有效的连接点上释放鼠标。请确保将连线拖拽到目标节点的连接点上。',
        duration: 4000,
        showClose: true
      })
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

  // 连接点悬停事件
  const onConnectionPointEnter = (event: MouseEvent, node: CanvasNode, type: ConnectionPointType): void => {
    hoveredConnectionPoint.value = { node, type }
    
    if (tempConnection.value.isDrawing) {
      console.log('连线中悬停在连接点:', {
        目标节点: node.name,
        目标类型: type,
        可连接: tempConnection.value.sourceNode ? canCreateConnection(
          tempConnection.value.sourceNode.id,
          node.id,
          tempConnection.value.sourceType!,
          type,
          false
        ) : false
      })
    }
  }

  const onConnectionPointLeave = (event: MouseEvent, node: CanvasNode, type: ConnectionPointType): void => {
    hoveredConnectionPoint.value = null
  }

  // 获取连接路径
  const getConnectionPath = (connection: Connection): string => {
    const sourceNode = canvasNodes.value.find(n => n.id === connection.sourceNodeId)
    const targetNode = canvasNodes.value.find(n => n.id === connection.targetNodeId)
    
    if (!sourceNode || !targetNode) return ''
    
    const sourceX = sourceNode.x + 200
    const sourceY = sourceNode.y + 60
    const targetX = targetNode.x
    const targetY = targetNode.y + 60
    
    const deltaX = targetX - sourceX
    const controlOffset = Math.abs(deltaX) * 0.5
    
    const cp1X = sourceX + controlOffset
    const cp1Y = sourceY
    const cp2X = targetX - controlOffset
    const cp2Y = targetY
    
    return `M ${sourceX} ${sourceY} C ${cp1X} ${cp1Y}, ${cp2X} ${cp2Y}, ${targetX} ${targetY}`
  }

  // 获取连接标记
  const getConnectionMarker = (connection: Connection): string => {
    if (selectedConnection.value?.id === connection.id) {
      return 'url(#arrowhead-selected)'
    } else if (hoveredConnection.value?.id === connection.id) {
      return 'url(#arrowhead-hover)'
    } else {
      return 'url(#arrowhead)'
    }
  }

  // 获取连接线宽
  const getConnectionStrokeWidth = (connection: Connection): number => {
    if (selectedConnection.value?.id === connection.id) {
      return 3
    } else if (hoveredConnection.value?.id === connection.id) {
      return 3
    } else {
      return 2
    }
  }

  // 获取连接颜色
  const getConnectionColor = (connection: Connection): string => {
    if (selectedConnection.value?.id === connection.id) {
      return '#f56c6c'
    } else if (hoveredConnection.value?.id === connection.id) {
      return '#1890ff'
    } else {
      return '#409eff'
    }
  }

  // 获取临时连接路径
  const getTempConnectionPath = (): string => {
    if (!tempConnection.value.isDrawing) return ''
    
    const sourceX = tempConnection.value.startX
    const sourceY = tempConnection.value.startY
    const targetX = tempConnection.value.currentX
    const targetY = tempConnection.value.currentY
    
    const deltaX = targetX - sourceX
    const controlOffset = Math.abs(deltaX) * 0.5
    
    const cp1X = sourceX + controlOffset
    const cp1Y = sourceY
    const cp2X = targetX - controlOffset
    const cp2Y = targetY
    
    return `M ${sourceX} ${sourceY} C ${cp1X} ${cp1Y}, ${cp2X} ${cp2Y}, ${targetX} ${targetY}`
  }

  // 寻找最近的连接点 (需要从外部引入)
  const findNearestConnectionPoint = (
    x: number, 
    y: number, 
    excludeNodeId?: number
  ): { node: CanvasNode; type: ConnectionPointType } | null => {
    const maxDistance = 50
    let nearestPoint: { node: CanvasNode; type: ConnectionPointType; distance: number } | null = null
    
    for (const node of canvasNodes.value) {
      if (excludeNodeId && node.id === excludeNodeId) continue
      
      // 计算输入连接点距离
      const inputX = node.x - 10
      const inputY = node.y + 60
      const inputDistance = Math.sqrt(Math.pow(x - inputX, 2) + Math.pow(y - inputY, 2))
      
      // 计算输出连接点距离
      const outputX = node.x + 210
      const outputY = node.y + 60
      const outputDistance = Math.sqrt(Math.pow(x - outputX, 2) + Math.pow(y - outputY, 2))
      
      // 检查输入点
      if (inputDistance <= maxDistance) {
        if (!nearestPoint || inputDistance < nearestPoint.distance) {
          nearestPoint = {
            node,
            type: 'input',
            distance: inputDistance
          }
        }
      }
      
      // 检查输出点
      if (outputDistance <= maxDistance) {
        if (!nearestPoint || outputDistance < nearestPoint.distance) {
          nearestPoint = {
            node,
            type: 'output',
            distance: outputDistance
          }
        }
      }
    }
    
    return nearestPoint ? { node: nearestPoint.node, type: nearestPoint.type } : null
  }

  // 连线验证 (需要从外部引入)
  const canCreateConnection = (
    sourceNodeId: number,
    targetNodeId: number,
    sourceType: ConnectionPointType,
    targetType: ConnectionPointType,
    showWarning: boolean = true
  ): boolean => {
    // 验证规则1: 不能连接自己
    if (sourceNodeId === targetNodeId) {
      if (showWarning) {
        ElMessage.error('不能将节点连接到自己')
      }
      return false
    }
    
    // 验证规则2: 输出端口 -> 输入端口
    if (sourceType !== 'output' || targetType !== 'input') {
      if (showWarning) {
        ElMessage.error('连线必须从输出端口（右侧）连接到输入端口（左侧）')
      }
      return false
    }
    
    // 验证规则3: 检查是否已存在相同连接
    const existingConnection = connections.value.find(conn => 
      conn.sourceNodeId === sourceNodeId && conn.targetNodeId === targetNodeId
    )
    
    if (existingConnection) {
      if (showWarning) {
        ElMessage.error('这两个节点之间已经存在连接')
      }
      return false
    }
    
    return true
  }

  return {
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
  }
} 