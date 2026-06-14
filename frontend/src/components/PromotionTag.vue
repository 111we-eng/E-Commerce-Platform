<template>
  <!--
    促销标签子组件 — PromotionTag
    ==============================
    接收父组件传入的促销信息，渲染不同样式的促销标签

    Props 说明（父→子传值）：
      type         : 促销类型 → DISCOUNT / FLASH_SALE / NEW_ARRIVAL / GIFT
      text         : 标签文字（可选，不传则根据 type 自动生成）
      discountRate : 折扣率（可选，仅 DISCOUNT 类型显示）
      originalPrice: 原价（可选，计算省了多少钱）

    使用示例：
      <PromotionTag type="FLASH_SALE" text="秒杀" />
      <PromotionTag type="DISCOUNT" :discountRate="0.85" :originalPrice="14999" />
  -->
  <div class="promotion-tag" :class="'tag-' + type" v-if="visible">
    <span class="tag-icon">{{ iconMap[type] || '🎉' }}</span>
    <span class="tag-text">{{ displayText }}</span>
    <!-- 折扣标签显示折扣率和省金额 -->
    <span v-if="type === 'DISCOUNT' && discountRate" class="tag-discount">
      {{ Math.round((1 - discountRate) * 100) }}% OFF
    </span>
    <span v-if="type === 'DISCOUNT' && savedAmount > 0" class="tag-saved">
      省¥{{ savedAmount }}
    </span>
  </div>
</template>

<script setup>
/**
 * 促销标签子组件
 *
 * 父→子传值流程：
 *   1. ProductDetail.vue (父组件) 通过 :type="product.promotion.type" 绑定
 *   2. 子组件通过 defineProps() 声明接收的 prop
 *   3. 模板中直接使用 type / text / discountRate
 */
import { computed } from 'vue'

const props = defineProps({
  /** 促销类型 */
  type: {
    type: String,
    default: 'DISCOUNT',
    validator: (v) => ['DISCOUNT', 'FLASH_SALE', 'NEW_ARRIVAL', 'GIFT', 'NONE'].includes(v)
  },
  /** 自定义标签文字 */
  text: { type: String, default: '' },
  /** 折扣率（0-1之间，例如 0.8 表示 8折） */
  discountRate: { type: Number, default: 0 },
  /** 原价（用于计算省了多少） */
  originalPrice: { type: Number, default: 0 },
  /** 当前售价 */
  currentPrice: { type: Number, default: 0 }
})

// 类型 → 图标映射
const iconMap = {
  DISCOUNT: '🏷️',
  FLASH_SALE: '⚡',
  NEW_ARRIVAL: '🆕',
  GIFT: '🎁'
}

// 类型 → 默认文字映射
const defaultTextMap = {
  DISCOUNT: '限时折扣',
  FLASH_SALE: '限时秒杀',
  NEW_ARRIVAL: '新品上市',
  GIFT: '买赠活动'
}

// 是否显示
const visible = computed(() => props.type !== 'NONE')

// 显示文字（优先使用传入的 text，否则用默认映射）
const displayText = computed(() => props.text || defaultTextMap[props.type] || '促销')

// 省了多少钱
const savedAmount = computed(() => {
  if (props.originalPrice > 0 && props.currentPrice > 0) {
    return Math.round(props.originalPrice - props.currentPrice)
  }
  if (props.originalPrice > 0 && props.discountRate > 0) {
    return Math.round(props.originalPrice * (1 - props.discountRate))
  }
  return 0
})
</script>

<style scoped>
.promotion-tag {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 600;
  margin-bottom: 10px; white-space: nowrap;
  animation: fadeInScale 0.3s ease;
}
@keyframes fadeInScale {
  from { opacity: 0; transform: scale(0.9); }
  to   { opacity: 1; transform: scale(1); }
}

/* DISCOUNT 样式 — 红色渐变 */
.tag-DISCOUNT {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #fff; box-shadow: 0 2px 8px rgba(255,77,79,0.35);
}
.tag-DISCOUNT .tag-discount {
  background: #fff; color: #ff4d4f; padding: 1px 6px;
  border-radius: 10px; font-size: 11px; font-weight: bold;
}
.tag-DISCOUNT .tag-saved {
  color: #ffe58f; font-size: 11px;
}

/* FLASH_SALE 样式 — 橙色渐变+脉冲 */
.tag-FLASH_SALE {
  background: linear-gradient(135deg, #ff7a45, #ffbb96);
  color: #fff; box-shadow: 0 2px 8px rgba(255,122,69,0.35);
  animation: flashPulse 1.5s infinite;
}
@keyframes flashPulse {
  0%, 100% { box-shadow: 0 2px 8px rgba(255,122,69,0.35); }
  50%      { box-shadow: 0 4px 20px rgba(255,122,69,0.6); }
}

/* NEW_ARRIVAL 样式 — 蓝色渐变 */
.tag-NEW_ARRIVAL {
  background: linear-gradient(135deg, #4e6ef2, #91a7ff);
  color: #fff; box-shadow: 0 2px 8px rgba(78,110,242,0.35);
}

/* GIFT 样式 — 紫色渐变 */
.tag-GIFT {
  background: linear-gradient(135deg, #9c27b0, #ce93d8);
  color: #fff; box-shadow: 0 2px 8px rgba(156,39,176,0.35);
}

.tag-icon { font-size: 16px; }
.tag-text { letter-spacing: 0.5px; }
</style>
