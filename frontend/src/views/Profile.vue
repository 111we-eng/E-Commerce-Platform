<template>
  <div>
    <h2 style="margin-bottom:20px;">👤 个人中心</h2>
    <el-card v-loading="loading">
      <el-descriptions title="用户信息" :column="1" border>
        <el-descriptions-item label="用户ID">{{ user?.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ user?.phone || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user?.email || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ user?.address || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="user?.role === 'ADMIN' ? 'danger' : 'info'">{{ user?.role }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ user?.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    <div style="margin-top:20px;text-align:right;">
      <el-button type="primary" @click="$router.push('/orders')">查看我的订单</el-button>
      <el-button type="default" @click="$router.push('/cart')">查看购物车</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { authApi } from '../api'

const loading = ref(false)
const user = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    const res = await authApi.getMe()
    user.value = res.data
  } finally {
    loading.value = false
  }
})
</script>
