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
        meta: { title: '工作台' }
      },
      {
        path: 'teams',
        name: 'Teams',
        component: () => import('@/views/Teams.vue'),
        meta: { title: '招募墙' }
      },
      {
        path: 'teams/:teamId',
        name: 'TeamDetail',
        component: () => import('@/views/TeamDetail.vue'),
        meta: { title: '团队详情' }
      },
      {
        path: 'projects/:projectId',
        name: 'ProjectDetail',
        component: () => import('@/views/ProjectDetail.vue'),
        meta: { title: '项目看板' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/Notifications.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理后台', role: 'admin' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 —— 未登录跳转到登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  if (!to.meta.noAuth && !token) {
    next('/login')
  } else if (to.meta.noAuth && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
