<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo" @click="$router.push('/home')">
        🛒 电商购物平台
      </div>
      <el-menu mode="horizontal" :default-active="activeMenu" router class="nav-menu"
        background-color="#409eff" text-color="#fff" active-text-color="#ffd04b">
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/cart" v-if="isLogin">购物车</el-menu-item>
        <el-menu-item index="/orders" v-if="isLogin">我的订单</el-menu-item>
        <el-menu-item index="/profile" v-if="isLogin">个人中心</el-menu-item>
        <el-menu-item index="/api-test">🧪 API测试</el-menu-item>
      </el-menu>
      <div class="user-area">
        <template v-if="isLogin">
          <span class="username">👤 {{ username }}</span>
          <el-button type="warning" size="small" @click="handleLogout">退出</el-button>
        </template>
        <template v-else>
          <el-button type="success" size="small" @click="$router.push('/login')">登录</el-button>
          <el-button type="info" size="small" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
    <el-footer class="footer">
      © 2026 电商购物平台 — 前后端分离架构
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { isAuthenticated, getUser, removeToken } from '../utils/auth'

const router = useRouter()
const route = useRoute()

const isLogin = computed(() => isAuthenticated())
const username = computed(() => {
  const user = getUser()
  return user ? user.username : ''
})
const activeMenu = computed(() => route.path)

const handleLogout = () => {
  removeToken()
  router.push('/login')
}
</script>

<style scoped>
.layout { min-height: 100vh; background: #f5f7fa; }
.header {
  display: flex; align-items: center; background: #409eff; padding: 0 20px;
  position: sticky; top: 0; z-index: 100;
}
.logo { color: #fff; font-size: 20px; font-weight: bold; cursor: pointer; margin-right: 30px; white-space: nowrap; }
.nav-menu { flex: 1; border-bottom: none !important; }
.nav-menu .el-menu-item { border-bottom: none !important; }
.user-area { display: flex; align-items: center; gap: 10px; }
.username { color: #fff; }
.main-content { max-width: 1200px; margin: 20px auto; width: 100%; padding: 0 20px; }
.footer { text-align: center; color: #999; padding: 20px; }
</style>
