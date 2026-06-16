import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getApiBaseUrl } from '@/config/runtime'
const request = axios.create({
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const baseUrl = getApiBaseUrl()
  const token = localStorage.getItem('campus_token') || ''

  if (!baseUrl) {
    return Promise.reject(new Error('未检测到服务端访问地址'))
  }

  config.baseURL = baseUrl

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const silentStatuses = Array.isArray(error.config?.silentStatuses) ? error.config.silentStatuses : []

    if (silentStatuses.includes(status)) {
      return Promise.reject(error)
    }

    if (status === 401) {
      localStorage.removeItem('campus_token')
      localStorage.removeItem('campus_user')
      ElMessage.error('登录已过期，请重新登录')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    } else if (status === 403) {
      ElMessage.error('当前账号无权限执行该操作')
    } else if (String(error.message).includes('timeout')) {
      ElMessage.error('请求超时，请检查局域网连接或服务端状态')
    } else if (String(error.message).includes('Network Error')) {
      ElMessage.error('无法连接服务端，请检查地址、端口或服务端状态')
    } else {
      ElMessage.error(error.message || '系统异常')
    }

    return Promise.reject(error)
  }
)

export default request
