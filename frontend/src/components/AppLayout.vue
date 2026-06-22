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

// 初始拉取用户信息和未读数
userStore.fetchUserInfo()
userStore.fetchUnreadCount()

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
  background: #304156;
  overflow: hidden;
  transition: width 0.3s;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  user-select: none;
}
.logo-short { font-size: 22px; letter-spacing: 2px; }
.app-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}
.header-left { display: flex; align-items: center; }
.collapse-btn { cursor: pointer; color: #666; }
.header-right { display: flex; align-items: center; gap: 16px; }
.user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}
.bell-badge { margin-right: 4px; }
.app-main {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 60px);
}
</style>
