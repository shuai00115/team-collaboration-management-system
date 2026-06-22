import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
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
      window.location.href = '/login'
    }
    return Promise.reject(new Error(msg || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      window.location.href = '/login'
    }
    ElMessage.error(error.response?.data?.msg || '网络异常')
    return Promise.reject(error)
  }
)

export default request
