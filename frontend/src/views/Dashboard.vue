<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h2>工作台</h2>
      <p>算子编排系统概览</p>
    </div>

    <div class="dashboard-content">
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="24" color="#409eff"><Setting /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.operators }}</div>
              <div class="stat-label">算子总数</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="24" color="#67c23a"><DocumentCopy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.workflows }}</div>
              <div class="stat-label">工作流数量</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="24" color="#e6a23c"><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.executions }}</div>
              <div class="stat-label">执行次数</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="24" color="#f56c6c"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.errors }}</div>
              <div class="stat-label">错误次数</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 快速操作 -->
      <div class="quick-actions">
        <h3>快速操作</h3>
        <div class="action-grid">
          <el-card class="action-card" @click="$router.push('/designer')">
            <div class="action-content">
              <el-icon size="32"><EditPen /></el-icon>
              <h4>新建编排</h4>
              <p>创建新的算子编排流程</p>
            </div>
          </el-card>

          <el-card class="action-card" @click="$router.push('/operators')">
            <div class="action-content">
              <el-icon size="32"><Setting /></el-icon>
              <h4>管理算子</h4>
              <p>查看和管理算子库</p>
            </div>
          </el-card>

          <el-card class="action-card" @click="$router.push('/workflows')">
            <div class="action-content">
              <el-icon size="32"><DocumentCopy /></el-icon>
              <h4>编排管理</h4>
              <p>管理已有的工作流</p>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 最近活动 -->
      <div class="recent-activities">
        <h3>最近活动</h3>
        <el-card>
          <el-timeline>
            <el-timeline-item
              v-for="activity in recentActivities"
              :key="activity.id"
              :timestamp="activity.timestamp"
              :type="activity.type"
            >
              {{ activity.message }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Stats {
  operators: number
  workflows: number
  executions: number
  errors: number
}

interface Activity {
  id: number
  message: string
  timestamp: string
  type: 'primary' | 'success' | 'warning' | 'danger'
}

// 统计数据
const stats = ref<Stats>({
  operators: 12,
  workflows: 8,
  executions: 156,
  errors: 3
})

// 最近活动
const recentActivities = ref<Activity[]>([
  {
    id: 1,
    message: '创建了新的数据转换算子',
    timestamp: '2024-01-10 14:30:00',
    type: 'primary'
  },
  {
    id: 2,
    message: '执行了工作流：用户数据处理',
    timestamp: '2024-01-10 13:45:00',
    type: 'success'
  },
  {
    id: 3,
    message: '更新了条件算子配置',
    timestamp: '2024-01-10 12:20:00',
    type: 'warning'
  },
  {
    id: 4,
    message: '工作流执行失败：数据验证错误',
    timestamp: '2024-01-10 11:15:00',
    type: 'danger'
  }
])

onMounted(() => {
  // 可以在这里加载真实的统计数据
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 30px;
}

.dashboard-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.dashboard-header p {
  margin: 0;
  color: #909399;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(64, 158, 255, 0.1);
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.quick-actions {
  margin-bottom: 40px;
}

.quick-actions h3 {
  margin: 0 0 20px 0;
  color: #303133;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.action-content {
  text-align: center;
  padding: 20px;
}

.action-content .el-icon {
  margin-bottom: 16px;
  color: #409eff;
}

.action-content h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.action-content p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.recent-activities h3 {
  margin: 0 0 20px 0;
  color: #303133;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style> 