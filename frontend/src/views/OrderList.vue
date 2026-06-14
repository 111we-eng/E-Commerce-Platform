<template>
  <div>
    <h2 style="margin-bottom:20px;">📦 我的订单</h2>

    <div v-loading="loading">
      <el-empty v-if="!loading && orders.length === 0" description="暂无订单">
        <el-button type="primary" @click="$router.push('/home')">去购物</el-button>
      </el-empty>

      <div v-else>
        <el-card v-for="order in orders" :key="order.id" class="order-card"
          shadow="hover" @click="$router.push('/order/' + order.id)">
          <div class="order-header">
            <div>
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ order.createTime }}</span>
            </div>
            <el-tag :type="statusType(order.status)" size="small">{{ statusText(order.status) }}</el-tag>
          </div>

          <el-table :data="order.items || []" border size="small" style="margin-top:12px;">
            <el-table-column prop="productName" label="商品" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }">¥{{ row.price ? row.price.toFixed(2) : '-' }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column label="小计" width="100">
              <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
            </el-table-column>
          </el-table>

          <div class="order-footer">
            <span class="total">合计：<b>¥{{ order.totalAmount ? order.totalAmount.toFixed(2) : '0.00' }}</b></span>
            <el-button v-if="order.status === 'PENDING'" type="warning" size="small"
              @click.stop="handleCancel(order)">取消订单</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../api'

const loading = ref(false)
const orders = ref([])

const statusType = (s) => {
  const map = { PENDING: 'warning', PAID: 'success', SHIPPED: '', COMPLETED: 'info', CANCELLED: 'danger' }
  return map[s] || 'info'
}
const statusText = (s) => {
  const map = { PENDING: '待支付', PAID: '已支付', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[s] || s
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.list()
    orders.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '确认', { type: 'warning' })
    await orderApi.cancel(order.id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (e) {}
}

onMounted(() => fetchOrders())
</script>

<style scoped>
.order-card { margin-bottom: 16px; cursor: pointer; }
.order-header { display: flex; justify-content: space-between; align-items: center; }
.order-no { font-weight: bold; margin-right: 15px; }
.order-time { color: #909399; font-size: 13px; }
.order-footer { display: flex; justify-content: flex-end; align-items: center; gap: 15px; margin-top: 12px; }
.total { font-size: 15px; }
.total b { color: #f56c6c; font-size: 18px; }
</style>
