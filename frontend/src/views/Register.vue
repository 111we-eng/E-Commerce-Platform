<template>
  <div class="register-page">
    <div class="register-card">
      <h2>📝 用户注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（至少6位）"
            show-password />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item prop="address">
          <el-input v-model="form.address" placeholder="收货地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%"
            @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>
      <div class="tip">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

const router = useRouter()
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  username: '', password: '', phone: '', email: '', address: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authApi.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex; justify-content: center; align-items: center;
  min-height: 100vh; background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}
.register-card {
  width: 440px; background: #fff; border-radius: 12px;
  padding: 40px; box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.register-card h2 { text-align: center; margin-bottom: 25px; }
.tip { text-align: center; color: #909399; font-size: 13px; }
.tip a { color: #409eff; text-decoration: none; }
</style>
