<template>
  <div v-loading="loading">
    <el-page-header @back="$router.push('/orders')" content="订单详情" style="margin-bottom:20px;" />

    <div v-if="order">
      <!-- 订单状态 -->
      <el-card style="margin-bottom:20px;">
        <div class="status-bar">
          <div>
            <span class="label">订单状态：</span>
            <el-tag :type="statusType(order.status)" size="large">{{ statusText(order.status) }}</el-tag>
          </div>
          <div>
            <span class="label">订单编号：</span>{{ order.orderNo }}
          </div>
          <div>
            <span class="label">下单时间：</span>{{ order.createTime }}
          </div>
        </div>
      </el-card>

      <!-- 订单商品 -->
      <el-card header="商品信息" style="margin-bottom:20px;">
        <el-table :data="order.items || []" border stripe>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="price" label="单价" width="120">
            <template #default="{ row }">¥{{ row.price ? row.price.toFixed(2) : '-' }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
        <div class="amount">
          订单总额：<span class="amount-value">¥{{ order.totalAmount ? order.totalAmount.toFixed(2) : '0.00' }}</span>
        </div>
      </el-card>

      <!-- 收货信息 -->
      <el-card header="收货信息" style="margin-bottom:20px;">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="收货地址">{{ order.address || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <div style="text-align:right;">
        <el-button v-if="order.status === 'PENDING'" type="warning" @click="handleCancel">取消订单</el-button>
        <el-button type="default" @click="$router.push('/orders')">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../api'

const route = useRoute()
const loading = ref(false)
const order = ref(null)

const statusType = (s) => {
  const map = { PENDING: 'warning', PAID: 'success', SHIPPED: '', COMPLETED: 'info', CANCELLED: 'danger' }
  return map[s] || 'info'
}
const statusText = (s) => {
  const map = { PENDING: '待支付', PAID: '已支付', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[s] || s
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await orderApi.detail(route.params.id)
    order.value = res.data
  } finally {
    loading.value = false
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '确认', { type: 'warning' })
    await orderApi.cancel(order.value.id)
    ElMessage.success('订单已取消')
    fetchDetail()
  } catch (e) {}
}

onMounted(() => fetchDetail())
</script>

<style scoped>
.status-bar { display: flex; gap: 30px; flex-wrap: wrap; }
.label { color: #909399; }
.amount { text-align: right; padding: 15px 0 0; font-size: 16px; }
.amount-value { color: #f56c6c; font-size: 28px; font-weight: bold; }
</style>
