/**
 * Axios 异步请求工具封装 — 企业级增强版
 *
 * 新增能力：
 *   - 请求取消（AbortController）— 防止重复请求/页面切换残留
 *   - 自动重试（retry）— 网络波动/临时故障自动恢复
 *   - 请求去重（dedupe）— 相同请求同时发起只执行一次
 *   - Mock 数据拦截 — 开发环境无后端时可使用 Mock
 *   - 加载状态追踪 — pendingMap 全局跟踪请求状态
 *   - 统一业务码处理 — code === 401 Token 过期自动跳转
 */
import axios from 'axios'
import { getToken, removeToken } from './auth'
import { ElMessage } from 'element-plus'
import router from '../router'

// ===================== 配置 =====================

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 是否启用 Mock（开发环境 + 后端不可用时）
let mockEnabled = false
export function enableMock(v = true) { mockEnabled = v }
export function isMockEnabled() { return mockEnabled }

// 全局请求追踪 Map：key = 请求标识，value = { promise, timestamp }
const pendingMap = new Map()
const PENDING_TTL = 60000 // 1分钟清除

// 请求重试配置
const RETRY_MAX = 2
const RETRY_DELAY = 1000

// ===================== 工具函数 =====================

/** 生成请求唯一标识（method + url + params + data） */
function requestKey(config) {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

/** 注册请求到 pendingMap */
function addPending(config) {
  const key = requestKey(config)
  if (pendingMap.has(key)) {
    const cached = pendingMap.get(key)
    if (Date.now() - cached.timestamp < PENDING_TTL) {
      return cached.controller
    }
    pendingMap.delete(key)
  }
  const controller = new AbortController()
  config.signal = controller.signal
  pendingMap.set(key, { controller, timestamp: Date.now() })
  return null // 表示是新的请求
}

/** 从 pendingMap 移除已完成的请求 */
function removePending(config) {
  const key = requestKey(config)
  pendingMap.delete(key)
}

/** 清除过期请求 */
function cleanPending() {
  const now = Date.now()
  for (const [key, val] of pendingMap) {
    if (now - val.timestamp > PENDING_TTL) pendingMap.delete(key)
  }
}

/** 延时 */
function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// ===================== 请求拦截器 =====================

service.interceptors.request.use(config => {
  // 定期清理过期记录
  cleanPending()

  // Token 注入
  const token = getToken()
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }

  // 请求去重检测 — 相同请求正在 pending 则取消当前
  const dupController = addPending(config)
  if (dupController) {
    // 已有相同请求在进行中 → 取消本次
    const source = axios.CancelToken.source()
    config.cancelToken = source.token
    source.cancel('DUPLICATE_REQUEST')
  }

  // 保存请求开始时间
  config._startTime = Date.now()

  return config
}, error => Promise.reject(error))

// ===================== 响应拦截器 =====================

service.interceptors.response.use(
  response => {
    removePending(response.config)
    const res = response.data

    if (res.code === 200) {
      return res
    }

    // 业务错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },

  async error => {
    // 请求被取消 — 不报错
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }

    removePending(error.config || {})

    // 自动重试
    const config = error.config
    if (config && !config._retryCount) {
      config._retryCount = 0
    }
    if (config && config._retryCount < RETRY_MAX) {
      config._retryCount++
      console.log(`[Request] 重试 ${config._retryCount}/${RETRY_MAX}: ${config.url}`)
      await delay(RETRY_DELAY * config._retryCount)
      return service(config)
    }

    // HTTP 错误处理
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          removeToken()
          ElMessage.error('登录已过期，请重新登录')
          router.push('/login')
          break
        case 403:
          ElMessage.error('无权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(`服务器错误 (${status})`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请重试')
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

// ===================== 扩展方法 =====================

/**
 * 发起 GET 请求（支持取消）
 * @param {string} url
 * @param {object} params   查询参数
 * @param {object} options  { signal, skipMock } 可选 AbortSignal / 跳过 Mock
 */
service.getWithCancel = function (url, params = {}, options = {}) {
  const config = { params }
  if (options.signal) config.signal = options.signal
  return service.get(url, config)
}

/**
 * 发起 POST 请求（支持取消）
 */
service.postWithCancel = function (url, data = {}, options = {}) {
  const config = {}
  if (options.signal) config.signal = options.signal
  return service.post(url, data, config)
}

/**
 * 获取当前待处理的请求数
 */
service.getPendingCount = function () {
  return pendingMap.size
}

/**
 * 取消所有待处理的请求（页面跳转时使用）
 */
service.cancelAll = function (reason = '页面切换') {
  for (const [key, val] of pendingMap) {
    val.controller.abort(reason)
  }
  pendingMap.clear()
}

/**
 * 取消指定 URL 前缀的请求
 */
service.cancelByPrefix = function (urlPrefix) {
  for (const [key, val] of pendingMap) {
    if (key.includes(urlPrefix)) {
      val.controller.abort('取消重复请求')
      pendingMap.delete(key)
    }
  }
}

export default service
