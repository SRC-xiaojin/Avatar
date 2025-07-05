<template>
  <div id="app">
    <el-container class="app-container">
      <!-- 头部 -->
      <el-header class="app-header">
        <div class="header-content">
          <h2 class="app-title">
            <el-icon><Setting /></el-icon>
            算子编排系统
          </h2>
          <div class="header-actions">
            <el-button type="primary" @click="$router.push('/designer')">
              <el-icon><Plus /></el-icon>
              新建编排
            </el-button>
          </div>
        </div>
      </el-header>

      <!-- 主体内容 -->
      <el-container>
        <!-- 侧边栏导航 -->
        <el-aside width="200px" class="app-sidebar">
          <el-menu
            :default-active="activeMenu"
            router
            class="sidebar-menu"
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
          >
            <el-menu-item index="/dashboard">
              <el-icon><Monitor /></el-icon>
              <span>工作台</span>
            </el-menu-item>
            <el-menu-item index="/operators">
              <el-icon><Setting /></el-icon>
              <span>算子管理</span>
            </el-menu-item>
            <el-menu-item index="/workflows">
              <el-icon><DocumentCopy /></el-icon>
              <span>编排管理</span>
            </el-menu-item>
            <el-menu-item index="/designer">
              <el-icon><EditPen /></el-icon>
              <span>编排设计器</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主要内容区域 -->
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const activeMenu = computed(() => {
  return route.path
})
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.app-title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-sidebar {
  background-color: #545c64;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.app-main {
  background-color: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
}
</style> 