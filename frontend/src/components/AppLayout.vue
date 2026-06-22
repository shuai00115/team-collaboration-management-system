<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  HomeFilled, User, Search, Bell, Setting, SwitchButton, DataBoard
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

// 仅已登录用户拉取用户信息和未读数
if (localStorage.getItem('accessToken')) {
  userStore.fetchUserInfo()
  userStore.fetchUnreadCount()
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

const menuItems = [
  { path: '/dashboard', title: '工作台', icon: HomeFilled },
  { path: '/teams', title: '招募墙', icon: Search },
  { path: '/notifications', title: '消息中心', icon: Bell },
  { path: '/profile', title: '个人中心', icon: User }
]
</script>

<template>
  <el-container class="app-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="app-aside">
      <div class="logo" @click="router.push('/dashboard')">
        <span v-if="!isCollapse" class="logo-text">团队协作</span>
        <span v-else class="logo-short">TC</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isAdmin" index="/admin">
          <el-icon><DataBoard /></el-icon>
          <span>管理后台</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="22">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
        </div>
        <div class="header-right">
          <el-badge :value="userStore.unreadCount" :hidden="!userStore.unreadCount" class="bell-badge">
            <el-button link @click="router.push('/notifications')">
              <el-icon :size="20"><Bell /></el-icon>
            </el-button>
          </el-badge>
          <el-dropdown trigger="click">
            <span class="user-avatar">
              {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">
                  <el-icon><Setting /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-layout { height: 100vh; }
.app-aside {
  background: linear-gradient(180deg, #1e2a3a 0%, #304156 100%);
  overflow: hidden;
  transition: width 0.3s;
  box-shadow: 2px 0 12px rgba(0,0,0,0.08);
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  user-select: none;
  background: rgba(255,255,255,0.05);
  border-bottom: 1px solid rgba(255,255,255,0.08);
  letter-spacing: 1px;
}
.logo-short { font-size: 22px; letter-spacing: 2px; }
.app-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.header-left { display: flex; align-items: center; }
.collapse-btn {
  cursor: pointer;
  color: #909399;
  transition: color 0.3s;
}
.collapse-btn:hover { color: #409eff; }
.header-right { display: flex; align-items: center; gap: 16px; }
.user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(64,158,255,0.3);
  transition: transform 0.2s, box-shadow 0.2s;
}
.user-avatar:hover { transform: scale(1.08); box-shadow: 0 4px 12px rgba(64,158,255,0.4); }
.bell-badge { margin-right: 4px; }
.app-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 60px);
}
</style>
