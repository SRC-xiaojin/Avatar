# OperatorDesigner 工作流设计器

## 简介
OperatorDesigner 是一个基于 Vue 3 和 TypeScript 的可视化工作流设计器，提供拖拽式操作界面，支持算子节点管理、连线操作、属性配置等功能。

## 项目结构
```
OperatorDesigner/
├── components/           # 组件目录
│   ├── OperatorPanel.vue      # 左侧算子面板
│   ├── DesignCanvas.vue       # 中央设计画布
│   ├── PropertyPanel.vue      # 右侧属性面板
│   ├── DebugPanel.vue         # 调试面板
│   └── HelpPanel.vue          # 帮助面板
├── composables/         # 组合式函数目录 (TypeScript)
│   ├── useWorkflowDesigner.ts # 工作流设计器主逻辑
│   ├── useConnectionManager.ts # 连线管理逻辑
│   └── useCanvasOperations.ts # 画布操作逻辑
├── types/               # TypeScript类型定义
│   └── index.ts         # 类型定义文件
├── index.vue            # 主入口组件
└── README.md           # 说明文档
```

## 技术栈
- **Vue 3** - 前端框架
- **TypeScript** - 类型系统
- **Composition API** - 组合式API
- **Element Plus** - UI组件库
- **SVG** - 矢量图形绘制
- **Pinia** - 状态管理

## 功能特点

### 1. 算子面板 (OperatorPanel.vue)
- 分类展示算子模板
- 支持拖拽操作
- 分类折叠/展开
- 算子搜索功能

### 2. 设计画布 (DesignCanvas.vue)
- 节点拖拽放置
- SVG连线绘制
- 连接点管理
- 画布缩放/拖拽
- 节点选择/删除

### 3. 属性面板 (PropertyPanel.vue)
- 节点属性编辑
- 连线属性配置
- 动态表单生成
- 配置参数管理

### 4. 调试面板 (DebugPanel.vue)
- 连线状态监控
- 实时调试信息
- 性能监控
- 操作日志记录

### 5. 帮助面板 (HelpPanel.vue)
- 操作指南
- 常见问题解答
- 快捷键说明
- 连线技巧

## TypeScript类型系统

### 核心类型定义
- **CanvasNode** - 画布节点类型
- **Connection** - 连线类型
- **UIOperatorTemplate** - UI算子模板类型
- **UIOperatorCategory** - UI算子分类类型
- **TempConnection** - 临时连线状态
- **ExecutionLog** - 执行日志类型

### API类型集成
项目直接使用 `@/types/api` 中的类型定义，确保类型一致性：
- **Workflow** - 工作流数据类型
- **WorkflowNode** - 工作流节点类型
- **WorkflowConnection** - 工作流连线类型
- **OperatorTemplate** - API算子模板类型
- **OperatorCategory** - API算子分类类型

### UI组件专用类型
为了避免与API类型命名冲突，UI组件使用专用的类型定义：
- **UIOperatorTemplate** - UI算子模板类型（针对UI组件优化）
- **UIOperatorCategory** - UI算子分类类型（针对UI组件优化）

这两种类型的区别：
- **API类型**：用于与后端API交互，字段名称与API保持一致
- **UI类型**：用于UI组件内部，字段名称针对前端显示优化

### Composables类型返回值
- **UseWorkflowDesignerReturn** - 工作流设计器返回类型
- **UseConnectionManagerReturn** - 连线管理返回类型
- **UseCanvasOperationsReturn** - 画布操作返回类型

## 组合式函数 (Composables)

### useWorkflowDesigner.ts
**主要功能：**
- 算子分类管理
- 节点和连线状态管理
- 工作流加载和保存
- 连线验证和错误处理

**返回值：**
```typescript
{
  // 响应式数据
  operatorCategories: Ref<UIOperatorCategory[]>
  canvasNodes: Ref<CanvasNode[]>
  connections: Ref<Connection[]>
  selectedNode: Ref<CanvasNode | null>
  
  // 方法
  loadOperatorCategories: () => Promise<void>
  canCreateConnection: (sourceNodeId, targetNodeId, sourceType, targetType) => boolean
  selectConnection: (connection: Connection) => void
  deleteConnection: (connection: Connection) => void
}
```

### useConnectionManager.ts
**主要功能：**
- 连线创建和管理
- 临时连线状态处理
- 连接点交互逻辑
- 连线路径计算

**返回值：**
```typescript
{
  tempConnection: Ref<TempConnection>
  hoveredConnectionPoint: Ref<{ node: CanvasNode; type: ConnectionPointType } | null>
  
  startConnection: (event: MouseEvent, node: CanvasNode, type: ConnectionPointType) => void
  endConnection: (event: MouseEvent, canvasElement: HTMLElement) => void
  getConnectionPath: (connection: Connection) => string
}
```

### useCanvasOperations.ts
**主要功能：**
- 节点拖拽管理
- 画布事件处理
- 工作流执行逻辑
- 数据保存和清空

**返回值：**
```typescript
{
  // 状态
  draggingNode: Ref<CanvasNode | null>
  isSaving: Ref<boolean>
  
  // 方法
  onOperatorDragStart: (event: DragEvent, operator: UIOperatorTemplate) => void
  onCanvasDrop: (event: DragEvent, canvasRef: Ref<HTMLElement | null>) => void
  saveWorkflow: () => Promise<void>
  executeWorkflow: (logs, output, showResult) => Promise<void>
}
```

## 使用方法

### 1. 基本用法
```vue
<template>
  <OperatorDesigner />
</template>

<script setup lang="ts">
import OperatorDesigner from '@/views/OperatorDesigner/index.vue'
</script>
```

### 2. 自定义配置
```vue
<script setup lang="ts">
import { ref } from 'vue'
import type { CanvasNode, Connection, UIOperatorTemplate, UIOperatorCategory } from '@/views/OperatorDesigner/types'
import type { Workflow, WorkflowNode, WorkflowConnection, OperatorTemplate, OperatorCategory } from '@/types/api'

const nodes = ref<CanvasNode[]>([])
const connections = ref<Connection[]>([])
const workflow = ref<Workflow | null>(null)
// UI组件专用类型
const uiOperators = ref<UIOperatorTemplate[]>([])
const uiCategories = ref<UIOperatorCategory[]>([])
// API接口类型
const apiOperators = ref<OperatorTemplate[]>([])
const apiCategories = ref<OperatorCategory[]>([])
</script>
```

## 重构历史

### 原始版本 (单文件)
- 文件：`OperatorDesigner.vue`
- 大小：2000+ 行代码
- 问题：代码耦合度高，维护困难

### 重构版本 (模块化)
- 重构时间：2024年
- 拆分方式：按功能模块拆分
- 文件数量：9个文件
- 总代码量：约120KB

### TypeScript重构
- 重构时间：2024年
- 主要改进：
  - 所有composables从JavaScript转换为TypeScript
  - 添加完整的类型定义系统
  - 提供类型安全的API接口
  - 改善代码智能提示和错误检查
  - 重命名UI组件类型以避免与API类型冲突

## 性能优化

### 1. 连线优化
- 智能连接点检测
- 连线路径算法优化
- 连线状态缓存

### 2. 渲染优化
- 虚拟滚动
- 节点懒加载
- SVG绘制优化

### 3. 内存管理
- 响应式数据优化
- 事件监听器清理
- 定时器管理

## 开发指南

### 1. 添加新算子类型
```typescript
// types/index.ts
export type OperatorType = 'INPUT' | 'OUTPUT' | 'TRANSFORM' | 'FILTER' | 'NEW_TYPE'
```

### 2. 扩展连线类型
```typescript
// types/index.ts
export type ConnectionType = 'data' | 'control' | 'event' | 'new_type'
```

### 3. 添加新的组合式函数
```typescript
// composables/useNewFeature.ts
export function useNewFeature(): UseNewFeatureReturn {
  // 实现逻辑
  return {
    // 导出API
  }
}
```

## 升级说明

### 从单文件版本升级
1. 更新导入路径：
   ```typescript
   // 旧版本
   import OperatorDesigner from '@/views/OperatorDesigner.vue'
   
   // 新版本
   import OperatorDesigner from '@/views/OperatorDesigner/index.vue'
   ```

2. 类型支持：
   ```typescript
   // 导入类型
   import type { CanvasNode, Connection } from '@/views/OperatorDesigner/types'
   ```

### 从JavaScript版本升级
1. 更新导入路径：
   ```typescript
   // 旧版本
   import { useWorkflowDesigner } from '@/views/OperatorDesigner/composables/useWorkflowDesigner.js'
   
   // 新版本
   import { useWorkflowDesigner } from '@/views/OperatorDesigner/composables/useWorkflowDesigner.ts'
   ```

2. 更新类型导入：
   ```typescript
   // 旧版本
   import type { OperatorTemplate, OperatorCategory } from '@/views/OperatorDesigner/types'
   
   // 新版本
   import type { UIOperatorTemplate, UIOperatorCategory } from '@/views/OperatorDesigner/types'
   import type { OperatorTemplate, OperatorCategory } from '@/types/api'
   ```

2. 启用TypeScript类型检查：
   ```typescript
   // 获得完整的类型支持
   const {
     operatorCategories,
     canvasNodes,
     connections,
     loadOperatorCategories
   } = useWorkflowDesigner()
   ```

## 维护指南

### 1. 代码规范
- 使用TypeScript严格模式
- 遵循Vue 3 Composition API最佳实践
- 组件单一职责原则

### 2. 测试策略
- 单元测试覆盖核心逻辑
- 集成测试验证组件交互
- E2E测试保证用户体验

### 3. 文档更新
- 及时更新类型定义
- 维护API文档
- 记录变更历史

## 常见问题

### Q: 如何添加自定义算子？
A: 在算子分类数据中添加新的算子模板，并在`getDefaultConfig`中定义默认配置。

### Q: 如何自定义连线样式？
A: 修改`getConnectionColor`和`getConnectionStrokeWidth`方法。

### Q: 如何扩展属性面板？
A: 在`PropertyPanel.vue`中添加新的表单字段，并在类型定义中更新相应接口。

### Q: TypeScript类型错误如何解决？
A: 检查类型定义文件，确保所有接口和类型都正确导出和导入。

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 发起 Pull Request

## 许可证
MIT License

---

**版本**: 2.0.0 (TypeScript重构版)  
**更新时间**: 2024年  
**维护者**: 开发团队 