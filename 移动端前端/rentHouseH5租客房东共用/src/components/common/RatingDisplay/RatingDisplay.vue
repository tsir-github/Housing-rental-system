<template>
  <div class="rating-display" v-if="showRating">
    <div class="rating-item" v-if="complianceScore !== undefined && complianceScore !== null">
      <van-icon name="shield-o" size="12" color="#52c41a" />
      <span class="rating-text">合规性 {{ complianceScore }}分</span>
    </div>
    
    <div class="rating-item" v-if="overallRating !== undefined && overallRating !== null">
      <van-icon name="star" size="12" color="#ffd21e" />
      <span class="rating-text">综合评级：{{ getOverallRatingText(overallRating) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  // 合规性评分
  complianceScore?: number | null;
  // 综合评级
  overallRating?: number | null;
  // 显示模式：inline（水平排列）、vertical（垂直排列）
  layout?: 'inline' | 'vertical';
  // 尺寸
  size?: 'small' | 'medium' | 'large';
}

const props = withDefaults(defineProps<Props>(), {
  layout: 'inline',
  size: 'small'
});

// 是否显示评分
const showRating = computed(() => {
  return (props.complianceScore !== undefined && props.complianceScore !== null) ||
         (props.overallRating !== undefined && props.overallRating !== null);
});

// 获取综合评级文字
const getOverallRatingText = (rating: number) => {
  const ratingMap: Record<number, string> = {
    1: '优秀',
    2: '良好', 
    3: '一般',
    4: '较差'
  };
  return ratingMap[rating] || '暂无评级';
};
</script>

<style scoped lang="less">
.rating-display {
  display: flex;
  align-items: center;
  gap: 8px;
  
  &.vertical {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}

.rating-item {
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 2px 6px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 10px;
  white-space: nowrap;
}

.rating-text {
  font-size: 11px;
  color: #646566;
  font-weight: 500;
  line-height: 1;
}

// 尺寸变体
.rating-display {
  &.size-small {
    .rating-item {
      padding: 2px 6px;
    }
    
    .rating-text {
      font-size: 11px;
    }
  }
  
  &.size-medium {
    gap: 10px;
    
    .rating-item {
      padding: 3px 8px;
    }
    
    .rating-text {
      font-size: 12px;
    }
  }
  
  &.size-large {
    gap: 12px;
    
    .rating-item {
      padding: 4px 10px;
    }
    
    .rating-text {
      font-size: 13px;
    }
  }
}
</style>