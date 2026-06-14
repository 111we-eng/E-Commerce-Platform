<template>
  <div>
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索商品..." clearable size="large"
        @clear="fetchProducts" @keyup.enter="handleSearch">
        <template #append>
          <el-button @click="handleSearch" icon="Search" />
        </template>
      </el-input>
    </div>

    <!-- 分类筛选 -->
    <div class="category-bar">
      <el-radio-group v-model="category" @change="filterByCategory" size="default">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="电子产品">电子产品</el-radio-button>
        <el-radio-button label="运动鞋服">运动鞋服</el-radio-button>
        <el-radio-button label="生活家电">生活家电</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 商品列表 -->
    <div class="product-grid" v-loading="loading">
      <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
      <div v-for="p in products" :key="p.id" class="product-card" @click="$router.push('/product/' + p.id)">
        <div class="product-image">
          <img :src="p.image || 'https://via.placeholder.com/300'" :alt="p.name"
            @error="e => e.target.src = 'https://via.placeholder.com/300?text=' + p.name" />
          <span class="category-tag">{{ p.category }}</span>
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ p.name }}</h3>
          <p class="product-desc">{{ p.description ? p.description.substring(0, 60) + '...' : '' }}</p>
          <div class="product-bottom">
            <span class="price">¥{{ p.price ? p.price.toFixed(2) : '0.00' }}</span>
            <span class="stock">库存 {{ p.stock }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '../api'

const loading = ref(false)
const keyword = ref('')
const category = ref('')
const products = ref([])

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await productApi.list()
    products.value = res.data
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    fetchProducts()
    return
  }
  loading.value = true
  try {
    const res = await productApi.search(keyword.value)
    products.value = res.data
  } finally {
    loading.value = false
  }
}

const filterByCategory = async (val) => {
  loading.value = true
  try {
    if (val) {
      const res = await productApi.byCategory(val)
      products.value = res.data
    } else {
      const res = await productApi.list()
      products.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchProducts())
</script>

<style scoped>
.search-bar { margin-bottom: 20px; }
.category-bar { margin-bottom: 20px; }
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
@media (max-width: 992px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .product-grid { grid-template-columns: 1fr; } }

.product-card {
  background: #fff; border-radius: 8px; overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08); cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.product-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.15); }

.product-image {
  position: relative; height: 200px; overflow: hidden; background: #f5f5f5;
  display: flex; align-items: center; justify-content: center;
}
.product-image img { width: 100%; height: 100%; object-fit: cover; }
.category-tag {
  position: absolute; top: 8px; right: 8px;
  background: #409eff; color: #fff; padding: 2px 8px; border-radius: 4px; font-size: 12px;
}
.product-info { padding: 16px; }
.product-name { font-size: 15px; font-weight: 600; margin-bottom: 8px; color: #303133; }
.product-desc { font-size: 12px; color: #909399; margin-bottom: 12px; height: 18px; overflow: hidden; }
.product-bottom { display: flex; justify-content: space-between; align-items: center; }
.price { font-size: 20px; font-weight: bold; color: #f56c6c; }
.stock { font-size: 12px; color: #909399; }
</style>
