import { ref } from 'vue'
import type { Ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { categoryApi } from '@/api/categories.ts'
import { templateApi } from '@/api/templates.ts'
import { workflowApi, connectionApi, nodeApi } from '@/api/workflows.ts'
import type { 
  UIOperatorCategory, 
  UIOperatorTemplate, 
  CanvasNode, 
  Connection, 
  ConnectionType,
  ConnectionPointType,
  Size,
  UseWorkflowDesignerReturn
} from '../types'
import type { 
  OperatorCategory as ApiOperatorCategory, 
  OperatorTemplate as ApiOperatorTemplate,
  Workflow,
  WorkflowNode,
  WorkflowConnection
} from '@/types/api'

export function useWorkflowDesigner(): UseWorkflowDesignerReturn {
  // 路由相关
  const route = useRoute()

  // 响应式数据
  const operatorCategories: Ref<UIOperatorCategory[]> = ref([])
  const loading: Ref<boolean> = ref(false)
  const collapsedCategories: Ref<Set<number>> = ref(new Set())

  // 画布相关
  const canvasNodes: Ref<CanvasNode[]> = ref([])
  const selectedNode: Ref<CanvasNode | null> = ref(null)
  const nextNodeId: Ref<number> = ref(1)
  const canvasSize: Ref<Size> = ref({ width: 2000, height: 1200 })

  // 连线相关
  const connections: Ref<Connection[]> = ref([])
  const selectedConnection: Ref<Connection | null> = ref(null)
  const hoveredConnection: Ref<Connection | null> = ref(null)
  const nextConnectionId: Ref<number> = ref(1)

  // 工作流相关
  const currentWorkflowId: Ref<number | null> = ref(null)

  // 算子分类管理
  const toggleCategory = (categoryId: number): void => {
    if (collapsedCategories.value.has(categoryId)) {
      collapsedCategories.value.delete(categoryId)
    } else {
      collapsedCategories.value.add(categoryId)
    }
  }

  const isCategoryCollapsed = (categoryId: number): boolean => {
    return collapsedCategories.value.has(categoryId)
  }

  // 显示算子详情
  const showOperatorDetails = (operator: UIOperatorTemplate): void => {
    console.log('显示算子详情:', operator)
    // 这里可以触发详情对话框显示
  }

  // 获取默认配置
  const getDefaultConfig = (type: string): Record<string, any> => {
    const defaultConfigs: Record<string, Record<string, any>> = {
      'TRANSFORM': {
        fieldMapping: '{"sourceField": "targetField"}',
        typeConversion: '{"field": "targetType"}',
        enableValidation: true,
        batchSize: 100
      },
      'FILTER': {
        condition: 'value > 0',
        enableLogging: false,
        strictMode: true
      },
      'AGGREGATION': {
        groupBy: 'category',
        aggregateFunction: 'SUM',
        targetField: 'total'
      },
      'CONDITION': {
        condition: 'status == "active"',
        trueAction: 'continue',
        falseAction: 'skip'
      }
    }
    
    return defaultConfigs[type] || {}
  }

  // 转换API数据格式
  const convertApiDataToLocal = (categories: ApiOperatorCategory[], templates: ApiOperatorTemplate[]): UIOperatorCategory[] => {
    console.log('🔄 开始转换API数据格式...')
    console.log('📋 原始分类数据:', categories)
    console.log('🔧 原始模板数据:', templates)
    
    const result = categories.map(category => {
      const categoryTemplates = templates.filter(template => template.categoryId === category.id)
      
      const convertedCategory = {
        id: category.id || 0,
        title: category.categoryName,
        type: category.categoryCode,
        operators: categoryTemplates.map(template => ({
          id: template.id || 0,
          name: template.templateName,
          type: template.templateCode as any,
          description: template.description || '',
          icon: getIconByType(template.templateCode),
          templateId: template.id || 0,
          categoryId: template.categoryId,
          config: template.configSchema ? JSON.parse(template.configSchema) : getDefaultConfig(template.templateCode)
        }))
      }
      
      console.log(`📂 转换分类 "${category.categoryName}" (${category.categoryCode}):`, {
        原始分类: category,
        转换后分类: convertedCategory,
        分类模板数量: categoryTemplates.length
      })
      
      return convertedCategory
    })
    
    console.log('✅ API数据转换完成:', result)
    return result
  }

  // 根据算子类型获取图标名称
  const getIconByType = (type: string): string => {
    const iconMap: Record<string, string> = {
      // 新的5类算子库
      'DATA_PROCESS': 'DataBoard',
      'CONTROL': 'Switch', 
      'SERVICE_CALL': 'PhoneFilled',
      'DATABASE': 'Coin',
      'FUNCTION': 'Tools',
      
      // 兼容旧的类型
      'TRANSFORM': 'DataBoard',
      'FILTER': 'Connection',
      'AGGREGATION': 'Coin',
      'CONDITION': 'Tools',
      'CUSTOM': 'Operation',
      'INPUT': 'Download',
      'OUTPUT': 'Upload'
    }
    return iconMap[type] || 'Operation'
  }

  // 加载算子分类
  const loadOperatorCategories = async (): Promise<void> => {
    loading.value = true
    try {
      console.log('开始加载算子分类和模板...')
      
      // 尝试从API获取数据
      const [categoriesResponse, templatesResponse] = await Promise.all([
        categoryApi.getCategories(),
        templateApi.getTemplates()
      ])
      
      if (categoriesResponse.success && templatesResponse.success) {
        const categories: ApiOperatorCategory[] = categoriesResponse.data || []
        const templates: ApiOperatorTemplate[] = templatesResponse.data || []
        
        if (categories.length > 0 && templates.length > 0) {
          // 转换API数据格式
          const categorizedOperators = convertApiDataToLocal(categories, templates)
          
          operatorCategories.value = categorizedOperators
          
          console.log('算子分类加载成功(API):', {
            分类数量: categorizedOperators.length,
            总模板数量: templates.length,
            详细分类: categorizedOperators.map((cat: UIOperatorCategory) => ({
              分类名: cat.title,
              模板数量: cat.operators.length
            }))
          })
          
          ElMessage.success(`成功加载 ${categorizedOperators.length} 个分类，${templates.length} 个算子模板`)
          return
        }
      }
      
      // API数据为空或失败，使用模拟数据
      throw new Error('API返回数据为空')
      
    } catch (error) {
      console.warn('API返回数据为空', error)
      
      ElMessage.warning(`加载算子分类失败`)
    } finally {
      loading.value = false
    }
  }

  // 加载工作流数据
  const loadWorkflow = async (workflowId: number): Promise<void> => {
    if (!workflowId) return
    
    loading.value = true
    currentWorkflowId.value = workflowId
    
    try {
      console.log('🚀 开始加载工作流数据:', workflowId)
      console.log('📡 开始并行API调用...')
      
      // 并行加载工作流、节点和连线数据
      const [workflowResponse, nodesResponse, connectionsResponse] = await Promise.all([
        workflowApi.getWorkflowById(workflowId),
        nodeApi.getNodesByWorkflow(workflowId),
        connectionApi.getConnectionsByWorkflow(workflowId)
      ])
      
      console.log('📊 API响应结果:', {
        工作流响应: {
          成功: workflowResponse.success,
          数据: workflowResponse.data,
          消息: workflowResponse.message
        },
        节点响应: {
          成功: nodesResponse.success,
          数据长度: nodesResponse.data?.length || 0,
          数据: nodesResponse.data,
          消息: nodesResponse.message
        },
        连线响应: {
          成功: connectionsResponse.success,
          数据长度: connectionsResponse.data?.length || 0,
          数据: connectionsResponse.data,
          消息: connectionsResponse.message
        }
      })
      
      if (!workflowResponse.success) {
        throw new Error(workflowResponse.message || '工作流加载失败')
      }
      
      if (!nodesResponse.success) {
        throw new Error(nodesResponse.message || '节点数据加载失败')
      }
      
      if (!connectionsResponse.success) {
        throw new Error(connectionsResponse.message || '连线数据加载失败')
      }
      
      const workflow: Workflow = workflowResponse.data!
      const nodes: WorkflowNode[] = nodesResponse.data || []
      const workflowConnections: WorkflowConnection[] = connectionsResponse.data || []
      
      console.log('✅ 工作流数据加载成功:', {
        工作流名称: workflow.workflowName,
        工作流描述: workflow.description,
        节点数量: nodes.length,
        连线数量: workflowConnections.length,
        原始节点数据: nodes,
        原始连线数据: workflowConnections
      })
      
      // 转换节点数据为画布节点
      console.log('🔄 开始转换节点数据...')
      const maxNodeId = Math.max(0, ...nodes.map(n => n.id || 0))
      nextNodeId.value = maxNodeId + 1
      
      const oldCanvasNodes = [...canvasNodes.value]
      canvasNodes.value = nodes.map(node => {
        const canvasNode = {
          id: node.id || 0,
          type: 'CUSTOM' as any, // 暂时使用默认类型，后续可以通过templateId查询具体类型
          name: node.nodeName || '未命名节点',
          description: '', // WorkflowNode没有description字段，后续可以通过templateId查询
          icon: getIconByType('CUSTOM'), // 暂时使用默认图标，后续可以通过templateId查询
          templateId: node.templateId || 0,
          categoryId: 0, // WorkflowNode没有categoryId字段，后续可以通过templateId查询
          x: node.positionX || 100,
          y: node.positionY || 100,
          config: node.nodeConfig ? JSON.parse(node.nodeConfig) : getDefaultConfig('CUSTOM'),
          dbId: node.id
        }
        
        console.log('🎨 转换节点:', {
          原始节点: node,
          转换后节点: canvasNode
        })
        
        return canvasNode
      })
      
      console.log('📍 画布节点转换完成:', {
        转换前节点数量: oldCanvasNodes.length,
        转换后节点数量: canvasNodes.value.length,
        nextNodeId: nextNodeId.value
      })
      
      // 转换连线数据为画布连线
      console.log('🔗 开始转换连线数据...')
      const maxConnectionId = Math.max(0, ...workflowConnections.map(c => c.id || 0))
      nextConnectionId.value = maxConnectionId + 1
      
      const oldConnections = [...connections.value]
      connections.value = workflowConnections.map(conn => {
        const connection = {
          id: conn.id || 0,
          sourceNodeId: conn.sourceNodeId,
          targetNodeId: conn.targetNodeId,
          type: (conn.connectionType || 'data') as ConnectionType
        }
        
        console.log('🔗 转换连线:', {
          原始连线: conn,
          转换后连线: connection
        })
        
        return connection
      })
      
      console.log('🔗 画布连线转换完成:', {
        转换前连线数量: oldConnections.length,
        转换后连线数量: connections.value.length,
        nextConnectionId: nextConnectionId.value
      })
      
      console.log('🎯 工作流渲染完成:', {
        画布节点数量: canvasNodes.value.length,
        画布连线数量: connections.value.length,
        节点详情: canvasNodes.value.map(n => ({
          ID: n.id,
          名称: n.name,
          位置: { x: n.x, y: n.y },
          数据库ID: n.dbId
        })),
        连线详情: connections.value.map(c => ({
          ID: c.id,
          源节点: c.sourceNodeId,
          目标节点: c.targetNodeId,
          类型: c.type
        }))
      })
      
      // 强制触发响应式更新
      console.log('🔄 强制触发响应式更新...')
      const tempNodes = [...canvasNodes.value]
      const tempConnections = [...connections.value]
      canvasNodes.value = []
      connections.value = []
      
      // 使用nextTick确保DOM更新
      await new Promise(resolve => setTimeout(resolve, 100))
      
      canvasNodes.value = tempNodes
      connections.value = tempConnections
      
      console.log('✅ 响应式更新完成')
      
      ElMessage.success(`工作流 "${workflow.workflowName}" 加载成功，包含 ${nodes.length} 个节点和 ${workflowConnections.length} 条连线`)
      
    } catch (error) {
      console.error('❌ 加载工作流失败:', error)
      console.error('❌ 错误堆栈:', error instanceof Error ? error.stack : '无堆栈信息')
      ElMessage.error(`加载工作流失败: ${error instanceof Error ? error.message : '未知错误'}`)
      
      // 清空当前工作流ID
      currentWorkflowId.value = null
    } finally {
      loading.value = false
      console.log('🏁 工作流加载流程结束')
    }
  }

  // 检查路由参数并自动加载工作流
  const checkAndLoadWorkflow = async (): Promise<void> => {
    console.log('🔍 开始检查路由参数...')
    console.log('📍 当前路由信息:', {
      路径: route.path,
      参数: route.params,
      查询: route.query,
      完整路由: route
    })
    
    const workflowId = route.params.workflowId || route.query.workflowId
    console.log('🆔 获取到的工作流ID:', workflowId)
    
    if (workflowId) {
      const id = parseInt(workflowId as string)
      console.log('🔢 转换后的ID:', id, '是否有效:', !isNaN(id) && id > 0)
      
      if (!isNaN(id) && id > 0) {
        console.log('✅ 检测到有效的工作流ID参数:', id)
        try {
          await loadWorkflow(id)
          console.log('✅ 工作流加载完成')
        } catch (error) {
          console.error('❌ 工作流加载失败:', error)
        }
      } else {
        console.warn('⚠️ 工作流ID无效:', workflowId)
      }
    } else {
      console.log('ℹ️ 没有检测到工作流ID参数，跳过加载')
    }
  }

  // 寻找最近的连接点
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

  // 连线验证
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
        showConnectionError(
          '无法创建连接',
          '不能将节点连接到自己。请选择不同的目标节点。'
        )
      }
      return false
    }
    
    // 验证规则2: 输出端口 -> 输入端口
    if (sourceType !== 'output' || targetType !== 'input') {
      if (showWarning) {
        showConnectionError(
          '连接方向错误',
          '连线必须从输出端口（右侧）连接到输入端口（左侧）。'
        )
      }
      return false
    }
    
    // 验证规则3: 检查是否已存在相同连接
    const existingConnection = connections.value.find(conn => 
      conn.sourceNodeId === sourceNodeId && conn.targetNodeId === targetNodeId
    )
    
    if (existingConnection) {
      if (showWarning) {
        showConnectionError(
          '连接已存在',
          '这两个节点之间已经存在连接。如需修改，请先删除现有连接。'
        )
      }
      return false
    }
    
    return true
  }

  // 显示连接错误
  const showConnectionError = (message: string, details?: string): void => {
    const fullMessage = details ? `${message}\n\n详细信息：${details}` : message
    
    ElMessage({
      type: 'error',
      message: fullMessage,
      duration: 5000,
      showClose: true,
      customClass: 'connection-error-message'
    })
    
    console.warn('连线验证失败:', {
      错误信息: message,
      详细信息: details,
      当前连线数量: connections.value.length,
      当前节点数量: canvasNodes.value.length
    })
  }

  // 选择连接
  const selectConnection = (connection: Connection): void => {
    selectedConnection.value = connection
    selectedNode.value = null
    console.log('选中连线:', {
      连线ID: connection.id,
      源节点: canvasNodes.value.find(n => n.id === connection.sourceNodeId)?.name,
      目标节点: canvasNodes.value.find(n => n.id === connection.targetNodeId)?.name
    })
  }

  // 删除连接
  const deleteConnection = (connection: Connection): void => {
    const index = connections.value.findIndex(conn => conn.id === connection.id)
    if (index !== -1) {
      const sourceNode = canvasNodes.value.find(n => n.id === connection.sourceNodeId)
      const targetNode = canvasNodes.value.find(n => n.id === connection.targetNodeId)
      
      connections.value.splice(index, 1)
      
      if (selectedConnection.value?.id === connection.id) {
        selectedConnection.value = null
      }
      
      console.log('删除连线:', {
        连线ID: connection.id,
        源节点: sourceNode?.name,
        目标节点: targetNode?.name,
        剩余连线数量: connections.value.length
      })
      
      ElMessage.success(`已删除连线: ${sourceNode?.name} → ${targetNode?.name}`)
    }
  }

  // 连线悬停事件
  const onConnectionHover = (connection: Connection, event: Event): void => {
    hoveredConnection.value = connection
  }

  const onConnectionLeave = (): void => {
    hoveredConnection.value = null
  }

  // 删除选中的连接
  const deleteSelectedConnection = (): void => {
    if (selectedConnection.value) {
      deleteConnection(selectedConnection.value)
    }
  }

  // 连接类型变化
  const onConnectionTypeChange = (type: ConnectionType): void => {
    if (selectedConnection.value) {
      selectedConnection.value.type = type
      console.log('连接类型已更改:', {
        连线ID: selectedConnection.value.id,
        新类型: type
      })
    }
  }

  // 节点属性变化
  const onNodePropertyChange = (property: string, value: any): void => {
    if (selectedNode.value) {
      ;(selectedNode.value as any)[property] = value
      console.log('节点属性已更改:', {
        节点ID: selectedNode.value.id,
        属性名: property,
        新值: value
      })
    }
  }

  // 节点配置变化
  const onNodeConfigChange = (key: string, value: any): void => {
    if (selectedNode.value) {
      selectedNode.value.config[key] = value
      console.log('节点配置已更改:', {
        节点ID: selectedNode.value.id,
        配置键: key,
        新值: value
      })
    }
  }

  return {
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
    loadWorkflow,
    checkAndLoadWorkflow,
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
  }
} 