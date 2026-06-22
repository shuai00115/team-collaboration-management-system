import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

// ---- Demo 模式拦截器 ----
request.interceptors.request.use(async (config) => {
  if (localStorage.getItem('demoMode') === 'true') {
    // 动态导入 mock 模块，仅在 demo 模式下加载
    const { getMockResponse } = await import('@/mock/index.js')
    const mockData = await getMockResponse(config)
    if (mockData !== null) {
      // 直接返回 mock 数据，不发送真实请求
      config.adapter = () => Promise.resolve({
        data: mockData,
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      })
    }
  }
  return config
})

// 请求拦截器 —— 自动附加 JWT Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 —— 统一错误处理
request.interceptors.response.use(
  (response) => {
    const { code, msg } = response.data
    if (code === 200) return response.data

    ElMessage.error(msg || '请求失败')
    if (code === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('demoMode')
      window.location.href = '/login'
    }
    return Promise.reject(new Error(msg || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('demoMode')
      window.location.href = '/login'
    }
    ElMessage.error(error.response?.data?.msg || '网络异常')
    return Promise.reject(error)
  }
)

export default request
