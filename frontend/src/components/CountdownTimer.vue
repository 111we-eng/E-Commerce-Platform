<template>
  <!--
    促销倒计时子组件 — CountdownTimer
    ================================
    接收父组件传入的促销结束时间，实时渲染倒计时

    Props 说明（父→子传值）：
      endTime : 促销结束时间（String 或 Number）
                - String: '2026-06-20T23:59:59' 或 '2026-06-20 23:59:59'
                - Number: 毫秒时间戳
      title   : 倒计时标题（可选，例如 "距离秒杀结束还有"）

    使用示例：
      <CountdownTimer endTime="2026-06-20T23:59:59" title="距离秒杀结束还有" />

    核心实现：
      1. 接收 endTime prop → 计算剩余秒数
      2. setInterval 每秒更新一次
      3. 倒计时归零 → 触发 @timeup 事件通知父组件
      4. onUnmounted 清理定时器，防止内存泄漏
  -->
  <div class="countdown-timer" v-if="remaining.total > 0">
    <span class="countdown-title" v-if="title">{{ title }}</span>
    <div class="countdown-digits">
      <!-- 天 -->
      <span class="digit-block" v-if="remaining.days > 0">
        <span class="digit">{{ pad(remaining.days) }}</span>
        <span class="label">天</span>
      </span>
      <span class="divider" v-if="remaining.days > 0">:</span>
      <!-- 时 -->
      <span class="digit-block">
        <span class="digit">{{ pad(remaining.hours) }}</span>
        <span class="label">时</span>
      </span>
      <span class="divider">:</span>
      <!-- 分 -->
      <span class="digit-block">
        <span class="digit">{{ pad(remaining.minutes) }}</span>
        <span class="label">分</span>
      </span>
      <span class="divider">:</span>
      <!-- 秒 -->
      <span class="digit-block seconds">
        <span class="digit">{{ pad(remaining.seconds) }}</span>
        <span class="label">秒</span>
      </span>
    </div>
  </div>
  <!-- 已过期 -->
  <div class="countdown-expired" v-else-if="expired">
    <span>⏰ 活动已结束</span>
  </div>
</template>

<script setup>
/**
 * 促销倒计时子组件
 *
 * 父→子传值流程：
 *   1. ProductDetail.vue (父组件) 通过 :endTime="product.promotion.endTime" 绑定
 *   2. 子组件通过 defineProps() 声明接收
 *   3. watch 监听 endTime 变化并启动定时器
 *   4. 倒计时归零时 emit('timeup') 通知父组件
 */
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  /** 促销截止时间 */
  endTime: {
    type: [String, Number],
    required: true,
    default: ''
  },
  /** 倒计时标题 */
  title: {
    type: String,
    default: '距活动结束'
  }
})

// 向父组件发送事件
const emit = defineEmits(['timeup'])

// 倒计时剩余
const remaining = reactive({
  total: 0,    // 总剩余秒数
  days: 0,
  hours: 0,
  minutes: 0,
  seconds: 0
})

const expired = ref(false)
let timer = null

/**
 * 解析截止时间 → 毫秒时间戳
 */
function parseEndTime(val) {
  if (typeof val === 'number') return val
  if (!val) return 0
  // 兼容多种日期格式
  const str = String(val).replace(' ', 'T')
  const ts = new Date(str).getTime()
  return isNaN(ts) ? 0 : ts
}

/**
 * 计算剩余时间
 */
function calcRemaining() {
  const endTs = parseEndTime(props.endTime)
  if (!endTs) return

  const now = Date.now()
  const diff = Math.max(0, Math.floor((endTs - now) / 1000))

  remaining.total   = diff
  remaining.days    = Math.floor(diff / 86400)
  remaining.hours   = Math.floor((diff % 86400) / 3600)
  remaining.minutes = Math.floor((diff % 3600) / 60)
  remaining.seconds = diff % 60

  // 倒计时归零
  if (diff <= 0) {
    expired.value = true
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    emit('timeup')
  }
}

/** 补零 */
function pad(n) {
  return String(n).padStart(2, '0')
}

// ===== 生命周期 =====

onMounted(() => {
  calcRemaining()
  // 每秒更新
  timer = setInterval(calcRemaining, 1000)
})

onUnmounted(() => {
  // 清理定时器 — 防止内存泄漏
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})

// 监听 endTime 变化（例如父组件动态更新促销时间）
watch(() => props.endTime, () => {
  expired.value = false
  calcRemaining()
  if (!timer) {
    timer = setInterval(calcRemaining, 1000)
  }
})
</script>

<style scoped>
.countdown-timer {
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  color: #fff; padding: 12px 20px; border-radius: 10px;
  margin-bottom: 10px; box-shadow: 0 4px 16px rgba(0,0,0,0.25);
}

.countdown-title {
  font-size: 13px; color: #ffa940; font-weight: 500;
}

.countdown-digits {
  display: flex; align-items: center; gap: 2px;
}

.digit-block {
  display: flex; flex-direction: column; align-items: center;
}

.digit {
  background: #ff4d4f; color: #fff; padding: 4px 8px;
  border-radius: 6px; font-size: 18px; font-weight: bold;
  font-family: 'Courier New', monospace; min-width: 32px;
  text-align: center; box-shadow: 0 2px 6px rgba(255,77,79,0.5);
}

.seconds .digit {
  animation: secondPulse 1s infinite;
}
@keyframes secondPulse {
  0%, 100% { background: #ff4d4f; }
  50%      { background: #ff7875; }
}

.label {
  font-size: 11px; color: #a0a0b0; margin-top: 2px;
}

.divider {
  font-size: 18px; color: #ff4d4f; font-weight: bold;
  margin: 0 2px;
}

.countdown-expired {
  background: #f5f5f5; color: #999; padding: 12px 20px;
  border-radius: 8px; text-align: center; margin-bottom: 10px;
}
</style>
