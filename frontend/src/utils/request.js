/**
 * Axios 封装 — 统一请求/响应拦截
 *
 * 请求拦截器：
 * - 自动从 localStorage 读取 Token
 * - 在 Authorization 头附加 "Bearer <token>"
 *
 * 响应拦截器：
 * - 自动解析 {code, message, data}
 * - code !== 200 → 自动提示错误
 * - code === 401 → Token 过期 → 清除 Token → 跳转登录页
 */
import axios from 'axios'
import { getToken, removeToken } from './auth'
import { ElMessage } from 'element-plus'
import router from '../router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// ===== 请求拦截器 =====
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// ===== 响应拦截器 =====
service.interceptors.response.use(
  response => {
    const res = response.data
    // 直接返回 data 字段（业务数据）
    if (res.code === 200) {
      return res
    }
    // 业务错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  error => {
    // HTTP 错误
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        // Token 过期或无效 → 清除并跳转登录
        removeToken()
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('无权限访问')
      } else {
        ElMessage.error('服务器错误 (' + status + ')')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default service
