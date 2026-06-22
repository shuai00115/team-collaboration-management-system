import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMyInfo } from '@/api/user'
import { getUnreadCount } from '@/api/notification'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const unreadCount = ref(0)
  const token = ref(localStorage.getItem('accessToken') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')

  async function fetchUserInfo() {
    try {
      const res = await getMyInfo()
      userInfo.value = res.data
    } catch {
      userInfo.value = null
    }
  }

  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.data?.unreadCount ?? 0
    } catch {
      unreadCount.value = 0
    }
  }

  function setToken(t) {
    token.value = t
    localStorage.setItem('accessToken', t)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('accessToken')
  }

  return { userInfo, unreadCount, token, isLoggedIn, isAdmin, fetchUserInfo, fetchUnreadCount, setToken, logout }
})
