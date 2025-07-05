<template>
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
</template>

<script setup>
import { 
  InfoFilled, 
  ArrowDown, 
  ArrowRight, 
  Loading, 
  Box, 
  DataBoard,
  Switch,
  Connection,
  Coin,
  Tools,
  Operation,
  // 新增图标
  SetUp,
  PhoneFilled,
  Histogram,
  Operation as FunctionIcon
} from '@element-plus/icons-vue'

// 定义props
const props = defineProps({
  operatorCategories: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  iconMap: {
    type: Object,
    default: () => ({})
  },
  // 折叠功能相关props
  toggleCategory: {
    type: Function,
    required: true
  },
  isCategoryCollapsed: {
    type: Function,
    required: true
  }
})

// 定义emits
const emit = defineEmits([
  'operator-drag-start',
  'show-operator-details'
])

// 转发方法调用到父组件
const toggleCategory = (categoryId) => {
  console.log('🔄 OperatorPanel - 切换分类折叠状态:', categoryId)
  props.toggleCategory(categoryId)
}

const isCategoryCollapsed = (categoryId) => {
  const collapsed = props.isCategoryCollapsed(categoryId)
  console.log('👁️ OperatorPanel - 检查分类折叠状态:', categoryId, '是否折叠:', collapsed)
  return collapsed
}

const onOperatorDragStart = (event, operator) => {
  emit('operator-drag-start', event, operator)
}

const showOperatorDetails = (operator) => {
  emit('show-operator-details', operator)
}
</script>

<style scoped>
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
  position: relative;
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

/* 响应式设计 */
@media (max-width: 1200px) {
  .operator-panel {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .operator-panel {
    width: 100%;
    height: 200px;
  }
}
</style> 