<template>
  <div>
    <!-- ===== 顶部操作栏 ===== -->
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <!-- 搜索 -->
        <el-input v-model="searchKeyword" placeholder="搜索商品名称或描述..." clearable
          size="large" style="flex: 1; max-width: 400px;"
          @clear="handleSearchClear" @keyup.enter="handleSearch">
          <template #append>
            <el-button @click="handleSearch" icon="Search" :loading="loading" />
          </template>
        </el-input>

        <!-- 分类筛选 -->
        <el-select v-model="selectedCategory" placeholder="全部分类" clearable
          size="large" style="width: 160px;" @change="handleCategoryChange">
          <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
        </el-select>

        <!-- Mock 状态指示 -->
        <el-tooltip :content="'当前数据来源: ' + (useMock ? 'Mock 模拟数据' : '后端 API')" placement="top">
          <el-tag :type="useMock ? 'warning' : 'success'" effect="plain" size="large">
            {{ useMock ? '📦 Mock数据' : '🖥️ 后端API' }}
          </el-tag>
        </el-tooltip>

        <!-- 请求状态 -->
        <span class="request-info" v-if="requestTime > 0">
          ⚡ 上次请求: {{ requestTime }}ms /
          共 <b>{{ totalProducts }}</b> 件商品 /
          第 <b>{{ currentPage }}</b> 页
        </span>
      </div>
    </el-card>

    <!-- ===== 商品列表 ===== -->
    <div class="product-grid" v-loading="loading" element-loading-text="正在加载商品...">
      <el-empty v-if="!loading && products.length === 0" description="没有找到匹配的商品">
        <el-button type="primary" @click="resetFilters">重置筛选</el-button>
      </el-empty>

      <template v-else>
        <div v-for="p in products" :key="p.id" class="product-card"
          @click="$router.push('/product/' + p.id)">
          <div class="product-image">
            <img :src="p.image || 'https://via.placeholder.com/300?text=' + p.name"
              :alt="p.name" loading="lazy"
              @error="e => e.target.src = 'https://via.placeholder.com/300?text=' + p.name" />
            <span class="category-tag">{{ p.category }}</span>
            <!-- 价格覆盖层 -->
            <div class="price-overlay">
              <span class="price-text">¥{{ formatPrice(p.price) }}</span>
            </div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ p.name }}</h3>
            <p class="product-desc">
              {{ p.description ? p.description.substring(0, 55) + '...' : '暂无描述' }}
            </p>
            <div class="product-meta">
              <span class="stock">
                <i class="el-icon-box"></i> 库存 {{ p.stock }}
              </span>
              <el-tag size="small" :type="p.stock > 50 ? 'success' : p.stock > 10 ? 'warning' : 'danger'">
                {{ p.stock > 50 ? '充足' : p.stock > 10 ? '紧张' : '紧缺' }}
              </el-tag>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- ===== 分页组件（每次翻页发送独立请求）===== -->
    <div class="pagination-wrapper" v-if="totalPages > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 12, 16, 20]"
        :total="totalProducts"
        layout="total, sizes, prev, pager, next, jumper"
        background
        :disabled="loading"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- ===== Mock 开关（开发调试用）===== -->
    <div class="mock-toggle">
      <el-popover placement="top" :width="360" trigger="click">
        <template #reference>
          <el-button size="small" circle>⚙️</el-button>
        </template>
        <div>
          <p><b>调试面板</b></p>
          <el-switch v-model="useMock" active-text="Mock数据" inactive-text="后端API"
            @change="toggleMock" size="small" />
          <p style="font-size:12px;color:#999;margin-top:8px;">
            Mock 模式模拟真实异步请求（200-600ms延迟），每页独立返回数据
          </p>
          <p style="font-size:12px;color:#999;">
            待处理请求: {{ pendingCount }}
          </p>
        </div>
      </el-popover>
    </div>
  </div>
</template>

<script setup>
/**
 * 商品列表页 — 完整异步分页 + 分类筛选
 *
 * 核心特性：
 *   1. 每次翻页/筛选发起独立异步请求
 *   2. 请求去重 — 快速翻页时自动取消前一个请求
 *   3. 加载状态 — el-pagination 在 loading 时禁用
 *   4. Mock 支持 — 无后端时自动使用本地 Mock 数据
 *   5. 响应时间追踪 — 记录每次请求耗时
 */
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi, enableMock as apiEnableMock, isMockEnabled } from '../api/product'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()

// ===== 状态 =====
const loading        = ref(false)
const products       = ref([])
const totalProducts  = ref(0)
const currentPage    = ref(1)
const pageSize       = ref(8)
const totalPages     = ref(0)
const selectedCategory = ref('')
const searchKeyword  = ref('')
const categories     = ref(['电子产品', '运动鞋服', '生活家电'])
const useMock        = ref(isMockEnabled())
const requestTime    = ref(0)
const pendingCount   = ref(0)

// 当前请求的 AbortController — 用于取消废弃的请求
let activeController = null

// ===== 核心方法：异步获取商品分页数据 =====

const fetchProducts = async () => {
  // 1. 取消上一次未完成的请求（防止快速翻页产生竞态）
  if (activeController) {
    activeController.abort('新的请求已发起')
    console.log('[Home] 取消上一个请求')
  }

  // 2. 创建新的 AbortController
  activeController = new AbortController()

  loading.value = true
  const startTime = Date.now()

  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      category: selectedCategory.value || '',
      keyword: searchKeyword.value || ''
    }

    console.log(`[Home] 发起请求 → page=${params.page}, pageSize=${params.pageSize}, category="${params.category}", keyword="${params.keyword}"`)

    const res = await productApi.listPage(params)

    // 解析分页响应
    const data = res.data || {}
    products.value      = data.records  || []
    totalProducts.value = data.total    || 0
    currentPage.value   = data.current  || params.page
    totalPages.value    = data.pages    || 0

    requestTime.value = Date.now() - startTime
    console.log(`[Home] ✅ 请求完成 → ${products.value.length} 条 / 共 ${totalProducts.value} 条 / 耗时 ${requestTime.value}ms`)

  } catch (err) {
    // 请求被取消不算错误
    if (err.name === 'AbortError' || err.name === 'CanceledError' || err.message?.includes('取消')) {
      console.log('[Home] ⚠️ 请求已被取消 (AbortError)')
      return
    }
    ElMessage.error('加载商品失败: ' + (err.message || '未知错误'))
    console.error('[Home] ❌ 请求失败:', err)
  } finally {
    loading.value = false
    activeController = null
    pendingCount.value = request.getPendingCount()
  }
}

// ===== 事件处理（每次触发都发起独立请求）=====

/** 翻页 — 请求新一页数据 */
const handlePageChange = (page) => {
  console.log(`[Home] 翻页 → 第 ${page} 页`)
  currentPage.value = page
  fetchProducts()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/** 修改每页条数 */
const handleSizeChange = (size) => {
  console.log(`[Home] 修改每页条数 → ${size}`)
  pageSize.value = size
  currentPage.value = 1
  fetchProducts()
}

/** 分类筛选 */
const handleCategoryChange = (val) => {
  console.log(`[Home] 分类筛选 → "${val || '全部'}"`)
  selectedCategory.value = val || ''
  currentPage.value = 1
  fetchProducts()
}

/** 搜索 */
const handleSearch = () => {
  console.log(`[Home] 搜索 → "${searchKeyword.value}"`)
  currentPage.value = 1
  fetchProducts()
}

/** 清除搜索 */
const handleSearchClear = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  fetchProducts()
}

/** 重置所有筛选条件 */
const resetFilters = () => {
  searchKeyword.value = ''
  selectedCategory.value = ''
  currentPage.value = 1
  fetchProducts()
}

/** Mock 开关切换 */
const toggleMock = (val) => {
  apiEnableMock(val)
  currentPage.value = 1
  fetchProducts()
  ElMessage.info(val ? '已切换到 Mock 数据模式' : '已切换到后端 API 模式')
}

// ===== 工具函数 =====

const formatPrice = (price) => {
  if (price == null) return '0.00'
  const num = Number(price)
  return num % 1 === 0 ? num.toFixed(0) : num.toFixed(2)
}

// ===== URL 参数同步 =====
// 分类参数写入 URL query，支持刷新保持状态
watch([selectedCategory, currentPage], () => {
  const query = {}
  if (selectedCategory.value) query.category = selectedCategory.value
  if (currentPage.value > 1) query.page = currentPage.value
  router.replace({ query })
}, { flush: 'post' })

// ===== 生命周期 =====

onMounted(async () => {
  // 从 URL 恢复筛选状态
  if (route.query.category) {
    selectedCategory.value = route.query.category
  }
  if (route.query.page) {
    currentPage.value = parseInt(route.query.page)
  }

  // 加载分类列表
  try {
    const res = await productApi.getCategories()
    if (res.data) categories.value = res.data
  } catch (e) {
    // 分类加载失败使用默认值
  }

  // 首次加载（自动判断使用 Mock 还是后端）
  fetchProducts()
})

onUnmounted(() => {
  // 组件销毁时取消所有未完成的请求
  if (activeController) {
    activeController.abort('组件已卸载')
  }
  request.cancelByPrefix('/products')
})
</script>

<style scoped>
/* ===== 工具栏 ===== */
.toolbar-card { margin-bottom: 16px; }
.toolbar {
  display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
}
.request-info { color: #909399; font-size: 13px; white-space: nowrap; }

/* ===== 商品网格 ===== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 300px;
}
@media (max-width: 1200px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px)  { .product-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px)  { .product-grid { grid-template-columns: 1fr; } }

/* ===== 商品卡片 ===== */
.product-card {
  background: #fff; border-radius: 10px; overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06); cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s;
}
.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.12);
}

.product-image {
  position: relative; height: 200px; overflow: hidden; background: #f8f9fa;
  display: flex; align-items: center; justify-content: center;
}
.product-image img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.product-card:hover .product-image img { transform: scale(1.05); }

.category-tag {
  position: absolute; top: 8px; left: 8px;
  background: rgba(64,158,255,0.9); color: #fff; padding: 3px 10px;
  border-radius: 4px; font-size: 12px; font-weight: 500;
}

.price-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  padding: 30px 12px 10px;
}
.price-text {
  color: #fff; font-size: 18px; font-weight: bold;
  text-shadow: 0 1px 3px rgba(0,0,0,0.5);
}

.product-info { padding: 14px 16px; }
.product-name {
  font-size: 14px; font-weight: 600; margin-bottom: 6px;
  color: #303133; line-height: 1.4;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; min-height: 39px;
}
.product-desc {
  font-size: 12px; color: #909399; margin-bottom: 10px;
  line-height: 1.5; height: 18px; overflow: hidden;
}
.product-meta {
  display: flex; justify-content: space-between; align-items: center;
}
.stock { font-size: 12px; color: #909399; }

/* ===== 分页 ===== */
.pagination-wrapper {
  display: flex; justify-content: center; margin-top: 24px;
  background: #fff; padding: 20px; border-radius: 8px;
}

/* ===== Mock 开关 ===== */
.mock-toggle {
  position: fixed; bottom: 24px; right: 24px; z-index: 1000;
}
</style>
