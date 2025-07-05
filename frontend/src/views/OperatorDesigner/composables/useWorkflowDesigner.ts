import { ref } from 'vue'
import type { Ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { categoryApi } from '@/api/categories'
import { templateApi } from '@/api/templates'
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
  OperatorTemplate as ApiOperatorTemplate} from '@/types/api'

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
    return categories.map(category => {
      const categoryTemplates = templates.filter(template => template.categoryId === category.id)
      
      return {
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
    })
  }

  // 根据算子类型获取图标名称
  const getIconByType = (type: string): string => {
    const iconMap: Record<string, string> = {
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