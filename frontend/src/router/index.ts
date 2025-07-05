import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '工作台' }
  },
  {
    path: '/operators',
    name: 'Operators',
    component: () => import('@/views/OperatorManagement.vue'),
    meta: { title: '算子管理' }
  },
  {
    path: '/workflows',
    name: 'Workflows',
    component: () => import('@/views/WorkflowManagement.vue'),
    meta: { title: '编排管理' }
  },
  {
    path: '/designer',
    name: 'Designer',
    component: () => import('@/views/OperatorDesigner/index.vue'),
    meta: { title: '编排设计器' }
  },
  {
    path: '/jolt-test',
    name: 'JoltTest',
    component: () => import('@/views/JoltTest.vue'),
    meta: { title: 'JOLT测试' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 算子编排系统`
  }
  next()
})

export default router 