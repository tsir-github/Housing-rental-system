<template>
  <div 
    class="stat-card"
    :class="{ 'clickable': clickable }"
    @click="handleClick"
  >
    <!-- 图标 -->
    <div v-if="icon" class="stat-icon" :style="{ color: iconColor }">
      <van-icon :name="icon" :size="iconSize" />
    </div>

    <!-- 数值和标签 -->
    <div class="stat-content">
      <div class="stat-number" :style="{ color: numberColor }">
        {{ formattedNumber }}
      </div>
      <div class="stat-label">{{ label }}</div>
      <div v-if="subtitle" class="stat-subtitle">{{ subtitle }}</div>
    </div>

    <!-- 趋势指示器 -->
    <div v-if="trend" class="stat-trend" :class="trendClass">
      <van-icon :name="trendIcon" size="12" />
      <span class="trend-text">{{ trendText }}</span>
    </div>

    <!-- 右侧箭头 -->
    <div v-if="clickable" class="stat-arrow">
      <van-icon name="arrow" size="16" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

// Props
interface Props {
  number?: number;
  value?: number;  // 兼容旧的属性名
  label: string;
  subtitle?: string;
  icon?: string;
  iconColor?: string;
  iconSize?: string | number;
  numberColor?: string;
  clickable?: boolean;
  trend?: 'up' | 'down' | 'stable';
  trendValue?: number;
  formatType?: 'default' | 'currency' | 'percentage';
}

const props = withDefaults(defineProps<Props>(), {
  iconColor: '#1989fa',
  iconSize: 24,
  numberColor: '#323233',
  clickable: false,
  formatType: 'default'
});

// Emits
defineEmits<{
  click: [];
}>();

// 计算属性
const formattedNumber = computed(() => {
  // 兼容 number 和 value 两种属性名
  const num = props.number ?? props.value ?? 0;
  
  switch (props.formatType) {
    case 'currency':
      return `¥${num.toLocaleString()}`;
    case 'percentage':
      return `${num}%`;
    default:
      return num.toLocaleString();
  }
});

const trendClass = computed(() => {
  if (!props.trend) return '';
  return `trend-${props.trend}`;
});

const trendIcon = computed(() => {
  const iconMap = {
    up: 'arrow-up',
    down: 'arrow-down',
    stable: 'minus'
  };
  return iconMap[props.trend || 'stable'];
});

const trendText = computed(() => {
  if (!props.trendValue) return '';
  const value = Math.abs(props.trendValue);
  return `${value}%`;
});

// 方法
const handleClick = () => {
  if (props.clickable) {
    // 触发点击事件
  }
};
</script>

<style scoped>
.stat-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card.clickable:active {
  transform: scale(0.98);
}

/* 图标 */
.stat-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(25, 137, 250, 0.1);
  border-radius: 50%;
}

/* 内容 */
.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #646566;
  line-height: 1.2;
}

.stat-subtitle {
  font-size: 12px;
  color: #969799;
  line-height: 1.2;
}

/* 趋势指示器 */
.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 4px;
}

.stat-trend.trend-up {
  color: #07c160;
  background: rgba(7, 193, 96, 0.1);
}

.stat-trend.trend-down {
  color: #fa5151;
  background: rgba(250, 81, 81, 0.1);
}

.stat-trend.trend-stable {
  color: #969799;
  background: rgba(150, 151, 153, 0.1);
}

.trend-text {
  font-size: 11px;
}

/* 箭头 */
.stat-arrow {
  flex-shrink: 0;
  color: #c8c9cc;
}

/* 响应式适配 */
@media (max-width: 375px) {
  .stat-card {
    padding: 12px;
    gap: 8px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .stat-label {
    font-size: 13px;
  }
  
  .stat-subtitle {
    font-size: 11px;
  }
}

/* 紧凑模式 */
.stat-card.compact {
  padding: 12px;
  flex-direction: column;
  text-align: center;
  gap: 8px;
}

.stat-card.compact .stat-icon {
  width: 32px;
  height: 32px;
}

.stat-card.compact .stat-number {
  font-size: 18px;
}

.stat-card.compact .stat-label {
  font-size: 12px;
}

.stat-card.compact .stat-arrow {
  position: absolute;
  top: 8px;
  right: 8px;
}
</style>