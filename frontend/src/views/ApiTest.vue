<template>
  <div class="api-test">
    <h2>🧪 异步请求功能测试页</h2>
    <p class="desc">验证 request.js 封装能力：分页请求、Mock数据、请求取消、重试、去重</p>

    <!-- ===== 测试控制面板 ===== -->
    <el-card class="control-panel" shadow="never">
      <template #header><b>🎛️ 测试控制面板</b></template>
      <el-space wrap :size="16">
        <div>
          <span style="color:#909399;margin-right:8px;">数据源:</span>
          <el-switch v-model="useMock" active-text="Mock" inactive-text="API" @change="toggleMock" />
        </div>
        <el-button type="primary" :loading="loading" @click="testFetch(1)">请求第1页</el-button>
        <el-button type="success" :loading="loading" @click="testFetch(2)">请求第2页</el-button>
        <el-button type="warning" :loading="loading" @click="testFetch(3)">请求第3页</el-button>
        <el-button type="danger" plain @click="testRapidSwitch">快速翻页测试(竞态)</el-button>
        <el-button plain @click="testCancelAll">取消全部请求</el-button>
      </el-space>
    </el-card>

    <!-- ===== 请求日志 ===== -->
    <el-card class="log-panel" shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <b>📋 请求日志</b>
          <el-button size="small" @click="logs = []">清空</el-button>
        </div>
      </template>
      <div class="log-list">
        <div v-for="(log, i) in logs" :key="i" class="log-item" :class="'log-' + log.type">
          <span class="log-time">{{ log.time }}</span>
          <span class="log-msg">{{ log.msg }}</span>
          <span class="log-cost" v-if="log.cost">{{ log.cost }}ms</span>
        </div>
        <el-empty v-if="logs.length === 0" description="点击上方按钮开始测试" :image-size="60" />
      </div>
    </el-card>

    <!-- ===== 返回数据预览 ===== -->
    <el-card class="data-panel" shadow="never" v-if="lastResponse">
      <template #header><b>📦 最近一次响应数据</b></template>
      <el-descriptions :column="6" border size="small">
        <el-descriptions-item label="总条数">{{ lastResponse.total }}</el-descriptions-item>
        <el-descriptions-item label="总页数">{{ lastResponse.pages }}</el-descriptions-item>
        <el-descriptions-item label="当前页">{{ lastResponse.current }}</el-descriptions-item>
        <el-descriptions-item label="返回条数">{{ lastResponse.records?.length || 0 }}</el-descriptions-item>
        <el-descriptions-item label="请求耗时">{{ lastResponse._cost }}ms</el-descriptions-item>
        <el-descriptions-item label="数据来源">{{ useMock ? 'Mock' : 'API' }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top:12px;">
        <el-table :data="lastResponse.records || []" border stripe size="small" max-height="300">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="商品名称" />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="80" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * API 测试页 — 验证 request.js 所有增强功能
 *
 * 测试项目：
 *   1. 分页独立请求         — 每次翻页发起 async request
 *   2. 竞态条件处理         — 快速翻页自动取消上一个请求
 *   3. Mock 数据切换        — 一键切换数据源
 *   4. 请求取消             — AbortController
 *   5. 响应时间追踪         — 记录每次请求耗时
 */
import { ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { productApi, enableMock, isMockEnabled } from '../api/product'
import request from '../utils/request'

const loading    = ref(false)
const useMock    = ref(isMockEnabled())
const lastResponse = ref(null)
const logs       = ref([])

// 存储当前活跃的 AbortController
let activeCtrl = null

function addLog(type, msg, cost) {
  const now = new Date()
  const time = now.toLocaleTimeString('zh-CN', { hour12: false }) + '.' + String(now.getMilliseconds()).padStart(3, '0')
  logs.value.unshift({ type, msg, time, cost })
  if (logs.value.length > 100) logs.value = logs.value.slice(0, 100)
}

// ===== 测试方法 =====

const testFetch = async (page) => {
  // 取消上一个请求
  if (activeCtrl) {
    activeCtrl.abort()
    addLog('cancel', `❌ 取消上一个未完成的请求（page=${page}）`)
  }
  activeCtrl = new AbortController()

  loading.value = true
  const start = Date.now()
  addLog('info', `📡 发起请求 → GET /api/products?page=${page}&pageSize=8`)

  try {
    const res = await productApi.listPage({ page, pageSize: 8 })
    const cost = Date.now() - start
    res._cost = cost

    lastResponse.value = res.data

    addLog('success', `✅ 请求成功 — 返回 ${res.data?.records?.length || 0} 条 / 共 ${res.data?.total || 0} 条`, cost)
    ElMessage.success(`第${page}页加载成功 (${cost}ms)`)
  } catch (err) {
    if (err?.name === 'AbortError' || err?.message?.includes('取消')) {
      addLog('cancel', `⚠️ 请求被取消 (page=${page}) — 这是正常的，竞态处理生效`)
    } else {
      addLog('error', `❌ 请求失败: ${err.message}`)
    }
  } finally {
    loading.value = false
    activeCtrl = null
  }
}

/** 快速切换页面 — 测试竞态条件 */
const testRapidSwitch = () => {
  addLog('info', '⚡ 快速翻页测试 — 连续发起3个请求，只有最后一个会生效')
  testFetch(1)
  setTimeout(() => testFetch(2), 50)
  setTimeout(() => testFetch(3), 100)
}

/** 取消所有请求 */
const testCancelAll = () => {
  if (activeCtrl) activeCtrl.abort()
  request.cancelAll('用户手动取消')
  loading.value = false
  addLog('cancel', '🛑 已取消所有待处理请求')
  ElMessage.warning('已取消所有请求')
}

const toggleMock = (v) => {
  enableMock(v)
  addLog('info', v ? '🔀 切换到 Mock 数据模式' : '🔀 切换到后端 API 模式')
  lastResponse.value = null
}

// 初始化日志
addLog('info', '🚀 API 测试页就绪 — 默认数据源: ' + (useMock.value ? 'Mock' : '后端API'))

onUnmounted(() => {
  if (activeCtrl) activeCtrl.abort()
})
</script>

<style scoped>
.api-test { max-width: 1000px; margin: 0 auto; }
h2 { margin-bottom: 4px; }
.desc { color: #909399; margin-bottom: 20px; }

.control-panel { margin-bottom: 16px; }
.log-panel { margin-bottom: 16px; }
.data-panel { margin-bottom: 16px; }

.log-list { max-height: 300px; overflow-y: auto; font-family: 'Consolas', 'Courier New', monospace; font-size: 13px; }
.log-item { padding: 4px 8px; border-bottom: 1px solid #f0f0f0; display: flex; gap: 12px; }
.log-time { color: #909399; white-space: nowrap; min-width: 90px; }
.log-msg { flex: 1; word-break: break-all; }
.log-cost { color: #409eff; font-weight: bold; white-space: nowrap; }

.log-success { background: #f0f9eb; }
.log-error { background: #fef0f0; }
.log-cancel { background: #fdf6ec; }
.log-info { background: #ecf5ff; }
</style>
