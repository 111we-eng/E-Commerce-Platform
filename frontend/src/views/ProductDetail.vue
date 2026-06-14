<template>
  <div class="detail" v-loading="loading">
    <el-page-header @back="$router.push('/home')" content="商品详情" style="margin-bottom:20px;" />

    <div class="detail-card" v-if="product">
      <div class="detail-image">
        <img :src="product.image || 'https://via.placeholder.com/500'" :alt="product.name"
          @error="e => e.target.src = 'https://via.placeholder.com/500?text=' + product.name" />
      </div>
      <div class="detail-info">
        <h1>{{ product.name }}</h1>
        <div class="meta">
          <el-tag>{{ product.category }}</el-tag>
          <span class="stock-text">库存：{{ product.stock }}</span>
        </div>
        <p class="desc">{{ product.description }}</p>
        <div class="price-section">
          <span class="price-label">价格</span>
          <span class="price-value">¥{{ product.price ? product.price.toFixed(2) : '0.00' }}</span>
        </div>
        <div class="quantity-row">
          <span>数量：</span>
          <el-input-number v-model="quantity" :min="1" :max="product.stock" />
        </div>
        <div class="actions">
          <el-button type="primary" size="large" @click="addToCart"
            :disabled="!isLogin">加入购物车</el-button>
          <el-button type="success" size="large" @click="buyNow"
            :disabled="!isLogin">立即购买</el-button>
        </div>
        <p v-if="!isLogin" style="color:#f56c6c;margin-top:10px;">
          请先<router-link to="/login">登录</router-link>后操作
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi, cartApi } from '../api'
import { isAuthenticated } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const isLogin = computed(() => isAuthenticated())

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await productApi.detail(route.params.id)
    product.value = res.data
    document.title = res.data.name + ' - 电商平台'
  } finally {
    loading.value = false
  }
}

const addToCart = async () => {
  try {
    await cartApi.add(product.value.id, quantity.value)
    ElMessage.success('已添加到购物车')
  } catch (e) {}
}

const buyNow = async () => {
  try {
    await cartApi.add(product.value.id, quantity.value)
    router.push('/cart')
  } catch (e) {}
}

onMounted(() => fetchDetail())
</script>

<style scoped>
.detail-card { display: flex; gap: 30px; background: #fff; border-radius: 8px; padding: 30px; }
@media (max-width: 768px) { .detail-card { flex-direction: column; } }
.detail-image { flex: 1; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 8px; }
.detail-image img { max-width: 100%; max-height: 400px; object-fit: contain; }
.detail-info { flex: 1; }
.detail-info h1 { font-size: 24px; margin-bottom: 15px; }
.meta { display: flex; align-items: center; gap: 12px; margin-bottom: 15px; }
.stock-text { color: #909399; font-size: 13px; }
.desc { color: #606266; line-height: 1.8; margin-bottom: 20px; }
.price-section { background: #fef0f0; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
.price-label { color: #909399; font-size: 14px; }
.price-value { color: #f56c6c; font-size: 32px; font-weight: bold; margin-left: 10px; }
.quantity-row { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.actions { display: flex; gap: 15px; }
</style>
