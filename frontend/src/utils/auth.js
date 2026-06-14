/**
 * Token 管理工具
 *
 * Token 生命周期：
 * - 登录成功 → setToken(token) 存入 localStorage
 * - 每次请求 → getToken() 由 Axios 拦截器读取并添加到 Authorization 头
 * - 登出/过期 → removeToken() 清除并跳转登录页
 * - 路由守卫 → isAuthenticated() 判断是否有 token
 */

const TOKEN_KEY = 'ecommerce_token'
const USER_KEY = 'ecommerce_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function isAuthenticated() {
  return !!getToken()
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function getUser() {
  const data = localStorage.getItem(USER_KEY)
  return data ? JSON.parse(data) : null
}
