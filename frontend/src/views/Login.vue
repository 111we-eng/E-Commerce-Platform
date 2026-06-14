<template>
  <div class="login-page">
    <div class="login-card">
      <h2>🔐 用户登录</h2>
      <p class="subtitle">电商购物平台 — JWT Token 认证</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock"
            show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%"
            @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="tip">
        还没有账号？<router-link to="/register">立即注册</router-link>
        <br />
        <span style="color:#999;font-size:12px;">默认管理员：admin / admin123</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { setToken, setUser } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await authApi.login(form)
    // 登录成功 → 存储 Token + 用户信息
    setToken(res.data.token)
    setUser({ userId: res.data.userId, username: res.data.username, role: res.data.role })
    ElMessage.success('登录成功！')

    // 跳转到之前想访问的页面，或首页
    const redirect = route.query.redirect || '/home'
    router.push(redirect)
  } catch (e) {
    // 错误已由 Axios 拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex; justify-content: center; align-items: center;
  min-height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px; background: #fff; border-radius: 12px;
  padding: 40px; box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.login-card h2 { text-align: center; margin-bottom: 5px; color: #303133; }
.subtitle { text-align: center; color: #909399; font-size: 13px; margin-bottom: 30px; }
.tip { text-align: center; color: #909399; font-size: 13px; line-height: 1.8; }
.tip a { color: #409eff; text-decoration: none; }
</style>
