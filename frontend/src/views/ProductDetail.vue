<template>
  <div class="detail" v-loading="loading">
    <el-page-header @back="$router.push('/home')" content="商品详情" style="margin-bottom:20px;" />

    <div class="detail-card" v-if="product">
      <!-- 商品图片 -->
      <div class="detail-image">
        <img :src="product.image || 'https://via.placeholder.com/500'" :alt="product.name"
          @error="e => e.target.src = 'https://via.placeholder.com/500?text=' + product.name" />
      </div>

      <!-- 商品信息 -->
      <div class="detail-info">
        <h1>{{ product.name }}</h1>

        <div class="meta">
          <el-tag>{{ product.category }}</el-tag>
          <span class="stock-text">库存：{{ product.stock }}</span>
        </div>

        <!-- ===== 🆕 促销标签子组件 ===== -->
        <!-- 父→子传值：通过 :type / :text / :discountRate / :originalPrice / :currentPrice 传递 -->
        <PromotionTag
          :type="product.promotion.type"
          :text="product.promotion.text"
          :discountRate="product.promotion.discountRate"
          :originalPrice="product.promotion.originalPrice"
          :currentPrice="product.price"
        />

        <!-- ===== 🆕 促销倒计时子组件 ===== -->
        <!-- 父→子传值：通过 :endTime / :title 传递 -->
        <!-- 监听子组件 @timeup 事件：倒计时归零时自动刷新 -->
        <CountdownTimer
          v-if="product.promotion.endTime"
          :endTime="product.promotion.endTime"
          :title="countdownTitle"
          @timeup="handlePromotionEnd"
        />

        <!-- 商品描述 -->
        <p class="desc">{{ product.description }}</p>

        <!-- 价格区 — 有促销时显示划线原价 -->
        <div class="price-section">
          <span class="price-label">{{ hasPromotion ? '促销价' : '价格' }}</span>
          <span class="price-value" :class="{ 'promo-price': hasPromotion }">
            ¥{{ promoPrice.toFixed(2) }}
          </span>
          <!-- 显示原价（划线） -->
          <span v-if="hasPromotion && product.promotion.originalPrice > 0" class="original-price">
            ¥{{ product.promotion.originalPrice.toFixed(2) }}
          </span>
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
/**
 * 商品详情页 — 父组件
 *
 * 父→子传值：
 *   1. <PromotionTag>    通过 props 传入 type/text/discountRate/originalPrice/currentPrice
 *   2. <CountdownTimer>  通过 props 传入 endTime/title
 *
 * 子→父通信：
 *   1. CountdownTimer @timeup → handlePromotionEnd() 处理倒计时归零
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi } from '../api'
import { cartApi } from '../api'
import { isAuthenticated } from '../utils/auth'
// 🆕 导入子组件
import PromotionTag from '../components/PromotionTag.vue'
import CountdownTimer from '../components/CountdownTimer.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const isLogin = computed(() => isAuthenticated())

// ===== 促销相关计算属性 =====

/** 是否有促销 */
const hasPromotion = computed(() => {
  return product.value?.promotion?.type &&
         product.value.promotion.type !== 'NONE'
})

/** 促销价格（打折后） */
const promoPrice = computed(() => {
  if (!product.value) return 0
  const price = product.value.price || 0
  const rate  = product.value.promotion?.discountRate
  if (rate && rate > 0 && rate < 1) {
    return Math.round(price * rate * 100) / 100
  }
  return price
})

/** 倒计时标题 */
const countdownTitle = computed(() => {
  const type = product.value?.promotion?.type
  const map = {
    DISCOUNT: '距折扣结束',
    FLASH_SALE: '距秒杀结束',
    GIFT: '距活动结束'
  }
  return map[type] || '距活动结束'
})

// ===== 方法 =====

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await productApi.detail(route.params.id)
    product.value = res.data
    document.title = (res.data.name || '商品') + ' - 电商平台'
    console.log('[ProductDetail] 商品加载完成, 促销信息:', product.value?.promotion)
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

/** 促销倒计时归零回调 */
const handlePromotionEnd = () => {
  console.log('[ProductDetail] 促销活动已结束')
  ElMessage.info('该商品的促销活动已结束')
  // 刷新商品信息
  fetchDetail()
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
.meta { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.stock-text { color: #909399; font-size: 13px; }
.desc { color: #606266; line-height: 1.8; margin-bottom: 20px; }
.price-section { background: #fef0f0; padding: 15px; border-radius: 8px; margin-bottom: 20px; display: flex; align-items: baseline; gap: 10px; }
.price-label { color: #909399; font-size: 14px; }
.price-value { color: #f56c6c; font-size: 32px; font-weight: bold; margin-left: 10px; }
.promo-price { color: #ff4d4f; }
.original-price { color: #999; font-size: 16px; text-decoration: line-through; margin-left: 6px; }
.quantity-row { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.actions { display: flex; gap: 15px; }
</style>
