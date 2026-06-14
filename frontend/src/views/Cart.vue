<template>
  <div>
    <h2 style="margin-bottom:20px;">🛒 购物车</h2>
    <div v-loading="loading">
      <el-empty v-if="!loading && cartItems.length === 0" description="购物车空空如也">
        <el-button type="primary" @click="$router.push('/home')">去逛逛</el-button>
      </el-empty>

      <div v-else>
        <el-table :data="cartItems" border stripe @selection-change="handleSelectionChange"
          empty-text="购物车为空">
          <el-table-column type="selection" width="50" />
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div class="cart-product">
                <img :src="row.productImage" style="width:60px;height:60px;object-fit:cover;border-radius:4px;"
                  @error="e => e.target.src = 'https://via.placeholder.com/60'" />
                <span>{{ row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="productPrice" label="单价" width="120">
            <template #default="{ row }">¥{{ row.productPrice ? row.productPrice.toFixed(2) : '-' }}</template>
          </el-table-column>
          <el-table-column label="数量" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" :max="99" size="small"
                @change="val => handleQtyChange(row.id, val)" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ (row.productPrice * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="handleRemove(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="cart-footer">
          <div class="total">
            合计：<span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
            （已选 {{ selectedIds.length }} 件）
          </div>
          <el-button type="primary" size="large" @click="goCheckout"
            :disabled="selectedIds.length === 0">去结算</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cartApi } from '../api'

const router = useRouter()
const loading = ref(false)
const cartItems = ref([])
const selectedIds = ref([])

const totalAmount = computed(() => {
  return cartItems.value
    .filter(item => selectedIds.value.includes(item.id))
    .reduce((sum, item) => sum + (item.productPrice || 0) * item.quantity, 0)
})

const fetchCart = async () => {
  loading.value = true
  try {
    const res = await cartApi.list()
    cartItems.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleQtyChange = async (id, qty) => {
  try {
    await cartApi.updateQuantity(id, qty)
  } catch (e) {
    fetchCart() // 失败则重新加载
  }
}

const handleRemove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此商品吗？', '提示', { type: 'warning' })
    await cartApi.remove(id)
    ElMessage.success('已删除')
    fetchCart()
  } catch (e) {}
}

const goCheckout = () => {
  const items = cartItems.value
    .filter(item => selectedIds.value.includes(item.id))
    .map(item => ({
      productId: item.productId,
      productName: item.productName,
      quantity: item.quantity,
      price: item.productPrice
    }))
  router.push({
    path: '/checkout',
    state: { items }
  })
}

onMounted(() => fetchCart())
</script>

<style scoped>
.cart-product { display: flex; align-items: center; gap: 12px; }
.cart-footer {
  display: flex; justify-content: flex-end; align-items: center; gap: 20px;
  margin-top: 20px; background: #fff; padding: 20px; border-radius: 8px;
}
.total { font-size: 16px; }
.total-price { color: #f56c6c; font-size: 24px; font-weight: bold; }
</style>
