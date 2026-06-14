<template>
  <div>
    <h2 style="margin-bottom:20px;">📝 确认订单</h2>
    <div class="checkout-content" v-loading="loading">
      <!-- 订单商品 -->
      <el-card header="订单商品" style="margin-bottom:20px;">
        <el-table :data="items" border stripe>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="price" label="单价">
            <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column label="小计">
            <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
        <div class="amount">
          应付金额：<span class="amount-value">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
      </el-card>

      <!-- 收货信息 -->
      <el-card header="收货信息" style="margin-bottom:20px;">
        <el-form :model="form" label-width="100px">
          <el-form-item label="收货地址" required>
            <el-input v-model="form.address" placeholder="请输入收货地址" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2"
              placeholder="选填：给卖家的备注" />
          </el-form-item>
        </el-form>
      </el-card>

      <div style="text-align:right;">
        <el-button size="large" @click="$router.back()">返回</el-button>
        <el-button type="primary" size="large" :loading="submitting"
          @click="submitOrder">提交订单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi, cartApi } from '../api'
import { getUser } from '../utils/auth'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const items = ref([])
const form = reactive({ address: '', remark: '' })

const totalAmount = computed(() => {
  return items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

onMounted(() => {
  // 从路由 state 获取购物车选中商品
  if (history.state && history.state.items) {
    items.value = history.state.items
  } else {
    ElMessage.warning('请先选择商品')
    router.push('/cart')
  }
  // 自动填充默认地址
  const user = getUser()
  if (user && user.address) {
    form.address = ''
  }
})

const submitOrder = async () => {
  if (!form.address.trim()) {
    ElMessage.warning('请填写收货地址')
    return
  }
  submitting.value = true
  try {
    const orderItems = items.value.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }))
    await orderApi.create({
      address: form.address,
      remark: form.remark,
      items: orderItems
    })
    // 清空购物车
    await cartApi.clear()
    ElMessage.success('下单成功！')
    router.push('/orders')
  } catch (e) {
    // 错误由拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.amount { text-align: right; padding: 15px 0 0; font-size: 16px; }
.amount-value { color: #f56c6c; font-size: 28px; font-weight: bold; }
</style>
