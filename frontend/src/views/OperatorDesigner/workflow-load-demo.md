# 工作流加载功能演示

## 功能概述

算子设计器现在支持从流程管理页面直接跳转到编辑页面，并自动加载已有的工作流数据，包括节点和连线，在画布上重新绘制出来。

## 实现原理

### 1. 路由参数传递
```vue
<!-- 流程管理页面 -->
<template>
  <el-button @click="editWorkflow(workflow.id)">编辑</el-button>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const editWorkflow = (workflowId) => {
  // 通过路由参数传递工作流ID
  router.push(`/operator-designer/${workflowId}`)
}
</script>
```

### 2. 自动检测和加载
```typescript
// useWorkflowDesigner.ts
const checkAndLoadWorkflow = async (): Promise<void> => {
  // 检测路由参数
  const workflowId = route.params.id || route.query.id
  if (workflowId) {
    const id = parseInt(workflowId as string)
    if (!isNaN(id) && id > 0) {
      console.log('检测到工作流ID参数:', id)
      await loadWorkflow(id)
    }
  }
}
```

### 3. 数据加载流程
```typescript
const loadWorkflow = async (workflowId: number): Promise<void> => {
  // 1. 并行加载工作流、节点、连线数据
  const [workflowResponse, nodesResponse, connectionsResponse] = await Promise.all([
    workflowApi.getWorkflowById(workflowId),
    nodeApi.getNodesByWorkflow(workflowId),
    connectionApi.getConnectionsByWorkflow(workflowId)
  ])
  
  // 2. 数据验证
  if (!workflowResponse.success) {
    throw new Error(workflowResponse.message || '工作流加载失败')
  }
  
  // 3. 数据转换
  const workflow = workflowResponse.data
  const nodes = nodesResponse.data || []
  const workflowConnections = connectionsResponse.data || []
  
  // 4. 转换为画布数据格式
  canvasNodes.value = nodes.map(node => ({
    id: node.id || 0,
    type: 'CUSTOM' as any,
    name: node.nodeName || '未命名节点',
    description: '',
    icon: getIconByType('CUSTOM'),
    templateId: node.templateId || 0,
    categoryId: 0,
    x: node.positionX || 100,
    y: node.positionY || 100,
    config: node.nodeConfig ? JSON.parse(node.nodeConfig) : getDefaultConfig('CUSTOM'),
    dbId: node.id // 保存数据库ID用于后续保存
  }))
  
  connections.value = workflowConnections.map(conn => ({
    id: conn.id || 0,
    sourceNodeId: conn.sourceNodeId,
    targetNodeId: conn.targetNodeId,
    type: (conn.connectionType || 'data') as ConnectionType
  }))
}
```

### 4. 页面初始化自动调用
```vue
<!-- index.vue -->
<script setup>
// 在页面挂载时自动检查和加载工作流
onMounted(async () => {
  await loadOperatorCategories()
  await checkAndLoadWorkflow() // 自动检查并加载工作流
  document.addEventListener('keydown', onKeyDown)
  addOperationLog('info', '算子设计器启动')
})
</script>
```

## 使用示例

### 1. 从流程管理页面跳转
```vue
<!-- 流程管理页面 -->
<template>
  <div class="workflow-list">
    <div v-for="workflow in workflows" :key="workflow.id" class="workflow-item">
      <h3>{{ workflow.workflowName }}</h3>
      <p>{{ workflow.description }}</p>
      <div class="actions">
        <el-button type="primary" @click="editWorkflow(workflow.id)">
          编辑工作流
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { workflowApi } from '@/api/workflows'

const router = useRouter()
const workflows = ref([])

const loadWorkflows = async () => {
  try {
    const response = await workflowApi.getWorkflows()
    if (response.success) {
      workflows.value = response.data
    }
  } catch (error) {
    console.error('加载工作流列表失败:', error)
  }
}

const editWorkflow = (workflowId) => {
  router.push(`/operator-designer/${workflowId}`)
}

onMounted(() => {
  loadWorkflows()
})
</script>
```

### 2. 支持的URL格式
```
# 路径参数
/operator-designer/123

# 查询参数
/operator-designer?id=123
```

### 3. 数据验证和错误处理
```typescript
// 加载过程中的错误处理
try {
  await loadWorkflow(workflowId)
  ElMessage.success(`工作流 "${workflow.workflowName}" 加载成功`)
} catch (error) {
  console.error('加载工作流失败:', error)
  ElMessage.error(`加载工作流失败: ${error.message}`)
}
```

## 技术特点

### 1. 并行数据加载
- 同时请求工作流、节点、连线数据
- 提高加载速度，减少用户等待时间

### 2. 数据转换层
- 将API数据格式转换为画布可用格式
- 保持数据库ID用于后续保存操作

### 3. 错误处理
- 详细的错误日志记录
- 用户友好的错误提示
- 失败时的降级策略

### 4. 性能优化
- 只在有工作流ID时才加载数据
- 加载状态管理
- 内存优化

## 调试和排错

### 1. 开启调试日志
```javascript
// 在浏览器控制台查看详细日志
console.log('工作流加载详情:', {
  工作流ID: workflowId,
  节点数量: nodes.length,
  连线数量: connections.length,
  画布节点: canvasNodes.value,
  画布连线: connections.value
})
```

### 2. 常见问题
- **路由参数为空**：检查URL是否包含正确的工作流ID
- **API调用失败**：检查网络连接和API服务状态
- **数据格式错误**：检查数据库中的数据格式是否正确
- **节点位置异常**：检查positionX和positionY字段值

### 3. 网络请求调试
在浏览器开发者工具的Network面板中可以看到：
- `/api/workflows/{id}` - 工作流基本信息
- `/api/workflow-nodes/workflow/{id}` - 节点数据
- `/api/workflow-connections/workflow/{id}` - 连线数据

## 扩展功能

### 1. 添加权限检查
```typescript
const checkPermission = async (workflowId: number): Promise<boolean> => {
  // 检查用户是否有权限编辑该工作流
  try {
    const response = await permissionApi.checkWorkflowPermission(workflowId)
    return response.success && response.data.canEdit
  } catch (error) {
    return false
  }
}
```

### 2. 添加版本控制
```typescript
const loadWorkflowVersion = async (workflowId: number, version?: string): Promise<void> => {
  // 加载指定版本的工作流
  const versionParam = version ? `?version=${version}` : ''
  const response = await workflowApi.getWorkflowById(workflowId + versionParam)
  // ... 处理逻辑
}
```

### 3. 添加缓存机制
```typescript
const workflowCache = new Map()

const loadWorkflowWithCache = async (workflowId: number): Promise<void> => {
  const cacheKey = `workflow_${workflowId}`
  if (workflowCache.has(cacheKey)) {
    const cachedData = workflowCache.get(cacheKey)
    // 使用缓存数据
    return
  }
  
  // 从API加载数据
  const data = await loadWorkflow(workflowId)
  workflowCache.set(cacheKey, data)
}
```

## 总结

工作流加载功能为用户提供了无缝的编辑体验，用户可以直接从流程管理页面跳转到编辑页面，已有的工作流数据会自动加载并在画布上重新绘制。这个功能通过路由参数传递工作流ID，并在页面初始化时自动检测和加载相应的数据，大大提高了用户的使用效率。 