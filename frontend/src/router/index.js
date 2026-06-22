import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台', auth: true }
      },
      {
        path: 'teams',
        name: 'Teams',
        component: () => import('@/views/Teams.vue'),
        meta: { title: '招募墙', public: true }
      },
      {
        path: 'teams/:teamId',
        name: 'TeamDetail',
        component: () => import('@/views/TeamDetail.vue'),
        meta: { title: '团队详情', public: true }
      },
      {
        path: 'projects/:projectId',
        name: 'ProjectDetail',
        component: () => import('@/views/ProjectDetail.vue'),
        meta: { title: '项目看板', auth: true }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/Notifications.vue'),
        meta: { title: '消息中心', auth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', auth: true }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理后台', auth: true, role: 'admin' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  const isDemo = localStorage.getItem('demoMode') === 'true'
  // Demo 模式下放行所有页面
  if (isDemo && token) {
    if (to.meta.noAuth) { return next('/dashboard') }
    return next()
  }
  // 不需要鉴权的页面：login / register
  if (to.meta.noAuth) {
    if (token) return next('/dashboard')
    return next()
  }
  // 公开页面：任何人可访问
  if (to.meta.public) {
    return next()
  }
  // 需要鉴权的页面
  if (!token) return next('/login')
  next()
})

export default router
