<template>
  <div class="quick-actions">
    <div class="quick-actions__header" v-if="title">
      <h3 class="quick-actions__title">{{ title }}</h3>
      <div class="quick-actions__extra" v-if="$slots.extra">
        <slot name="extra"></slot>
      </div>
    </div>
    
    <div class="quick-actions__grid" :class="`quick-actions__grid--${columns}`">
      <div 
        v-for="action in actions" 
        :key="action.key"
        class="quick-actions__item"
        @click="handleActionClick(action)"
      >
        <div class="quick-actions__icon">
          <van-icon 
            v-if="action.icon" 
            :name="action.icon" 
            :color="action.color || '#1989fa'"
            size="24"
          />
        </div>
        
        <div class="quick-actions__content">
          <div class="quick-actions__label">{{ action.title }}</div>
          <div class="quick-actions__desc" v-if="action.description">
            {{ action.description }}
          </div>
        </div>
        
        <div class="quick-actions__badge" v-if="action.badge">
          <van-badge 
            :content="action.badge.content" 
            :color="action.badge.color"
            :max="action.badge.max"
          />
        </div>
        
        <div class="quick-actions__arrow" v-if="!hideArrow">
          <van-icon name="arrow" color="#c8c9cc" size="16" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

interface QuickAction {
  key: string;
  title: string;
  description?: string;
  icon?: string;
  color?: string;
  path?: string;
  badge?: {
    content: string | number;
    color?: string;
    max?: number;
  };
  onClick?: () => void;
}

interface Props {
  // 操作列表
  actions: QuickAction[];
  // 标题
  title?: string;
  // 列数
  columns?: 1 | 2 | 3 | 4;
  // 隐藏箭头
  hideArrow?: boolean;
}

interface Emits {
  (e: 'action-click', action: QuickAction): void;
}

const props = withDefaults(defineProps<Props>(), {
  columns: 2,
  hideArrow: false
});

const emit = defineEmits<Emits>();
const router = useRouter();

const handleActionClick = (action: QuickAction) => {
  // 触发事件
  emit('action-click', action);
  
  // 执行自定义点击事件
  if (action.onClick) {
    action.onClick();
    return;
  }
  
  // 路由跳转
  if (action.path) {
    router.push(action.path);
  }
};
</script>

<style scoped lang="less">
.quick-actions {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 16px 0;
  }
  
  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin: 0;
  }
  
  &__grid {
    padding: 16px 0;
    
    &--1 {
      .quick-actions__item {
        margin: 0 16px 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
    
    &--2 {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px;
      padding: 16px;
    }
    
    &--3 {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 8px;
      padding: 16px;
    }
    
    &--4 {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 8px;
      padding: 16px;
    }
  }
  
  &__item {
    display: flex;
    align-items: center;
    padding: 12px;
    background: #f7f8fa;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    min-height: 60px;
    
    &:hover {
      background: #f2f3f5;
      transform: translateY(-1px);
    }
    
    &:active {
      transform: translateY(0);
      background: #ebedf0;
    }
  }
  
  &__icon {
    margin-right: 12px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    background: rgba(25, 137, 250, 0.1);
    border-radius: 50%;
  }
  
  &__content {
    flex: 1;
    min-width: 0;
  }
  
  &__label {
    font-size: 14px;
    font-weight: 500;
    color: #323233;
    line-height: 1.2;
    margin-bottom: 2px;
  }
  
  &__desc {
    font-size: 12px;
    color: #969799;
    line-height: 1.2;
  }
  
  &__badge {
    margin-right: 8px;
    flex-shrink: 0;
  }
  
  &__arrow {
    flex-shrink: 0;
  }
}

// 网格布局优化
.quick-actions__grid--2,
.quick-actions__grid--3,
.quick-actions__grid--4 {
  .quick-actions__item {
    flex-direction: column;
    text-align: center;
    padding: 16px 8px;
  }
  
  .quick-actions__icon {
    margin-right: 0;
    margin-bottom: 8px;
  }
  
  .quick-actions__arrow {
    display: none;
  }
  
  .quick-actions__badge {
    position: absolute;
    top: 8px;
    right: 8px;
    margin: 0;
  }
}

// 响应式设计
@media (max-width: 375px) {
  .quick-actions {
    &__grid--3 {
      grid-template-columns: repeat(2, 1fr);
    }
    
    &__grid--4 {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  
  .quick-actions__item {
    padding: 10px 8px;
    min-height: 50px;
  }
  
  .quick-actions__label {
    font-size: 13px;
  }
  
  .quick-actions__desc {
    font-size: 11px;
  }
}
</style>