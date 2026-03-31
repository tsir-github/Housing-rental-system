<template>
  <div class="empty-state" :class="stateClass">
    <!-- 图片/图标 -->
    <div class="empty-state__image">
      <slot name="image">
        <img v-if="image" :src="image" :alt="description" class="empty-image" />
        <van-icon v-else-if="icon" :name="icon" :size="iconSize" :color="iconColor" />
        <van-empty v-else :image="emptyType" :image-size="imageSize" />
      </slot>
    </div>
    
    <!-- 标题 -->
    <div v-if="title" class="empty-state__title">
      {{ title }}
    </div>
    
    <!-- 描述 -->
    <div v-if="description" class="empty-state__description">
      {{ description }}
    </div>
    
    <!-- 操作按钮 -->
    <div v-if="$slots.action || showAction" class="empty-state__action">
      <slot name="action">
        <van-button 
          v-if="actionText"
          :type="actionType"
          :size="actionSize"
          :round="actionRound"
          @click="handleAction"
        >
          {{ actionText }}
        </van-button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

// 预定义的空状态配置
const EMPTY_CONFIGS = {
  // 数据相关
  'no-data': {
    title: '暂无数据',
    description: '当前没有任何数据',
    emptyType: 'default'
  },
  'no-search': {
    title: '无搜索结果',
    description: '试试其他关键词吧',
    emptyType: 'search'
  },
  'no-network': {
    title: '网络异常',
    description: '请检查网络连接后重试',
    emptyType: 'network',
    actionText: '重新加载'
  },
  
  // 房东业务相关
  'no-apartments': {
    title: '还没有公寓',
    description: '创建您的第一个公寓吧',
    emptyType: 'default',
    actionText: '创建公寓'
  },
  'no-rooms': {
    title: '还没有房源',
    description: '发布您的第一个房源吧',
    emptyType: 'default',
    actionText: '发布房源'
  },
  'no-applications': {
    title: '暂无申请',
    description: '还没有租客申请您的房源',
    emptyType: 'default'
  },
  'no-appointments': {
    title: '暂无预约',
    description: '还没有租客预约看房',
    emptyType: 'default'
  },
  'no-reviews': {
    title: '暂无审核记录',
    description: '您的房源还没有审核记录',
    emptyType: 'default'
  },
  
  // 权限相关
  'no-permission': {
    title: '权限不足',
    description: '您没有权限访问此内容',
    emptyType: 'error'
  },
  
  // 错误状态
  'error': {
    title: '出错了',
    description: '页面加载失败，请稍后重试',
    emptyType: 'error',
    actionText: '重新加载'
  },
  '404': {
    title: '页面不存在',
    description: '您访问的页面不存在',
    emptyType: 'error',
    actionText: '返回首页'
  },
  
  // 功能状态
  'coming-soon': {
    title: '敬请期待',
    description: '功能正在开发中，敬请期待',
    emptyType: 'default'
  },
  'maintenance': {
    title: '系统维护中',
    description: '系统正在维护，请稍后再试',
    emptyType: 'default'
  }
} as const;

interface Props {
  type?: keyof typeof EMPTY_CONFIGS;
  title?: string;
  description?: string;
  image?: string;
  icon?: string;
  iconSize?: string;
  iconColor?: string;
  emptyType?: 'default' | 'error' | 'network' | 'search';
  imageSize?: string;
  actionText?: string;
  actionType?: 'primary' | 'success' | 'warning' | 'danger' | 'default';
  actionSize?: 'mini' | 'small' | 'normal' | 'large';
  actionRound?: boolean;
  showAction?: boolean;
  size?: 'small' | 'medium' | 'large';
}

interface Emits {
  (e: 'action'): void;
}

const props = withDefaults(defineProps<Props>(), {
  iconSize: '64px',
  iconColor: '#c8c9cc',
  emptyType: 'default',
  imageSize: '160px',
  actionType: 'primary',
  actionSize: 'normal',
  actionRound: true,
  showAction: false,
  size: 'medium'
});

const emit = defineEmits<Emits>();

// 获取配置
const config = computed(() => {
  if (props.type && EMPTY_CONFIGS[props.type]) {
    return EMPTY_CONFIGS[props.type];
  }
  return {};
});

// 计算属性
const title = computed(() => props.title || config.value.title);
const description = computed(() => props.description || config.value.description);
const emptyType = computed(() => props.emptyType || config.value.emptyType || 'default');
const actionText = computed(() => props.actionText || config.value.actionText);
const showAction = computed(() => props.showAction || !!actionText.value);

// 样式类
const stateClass = computed(() => ({
  [`empty-state--${props.size}`]: props.size
}));

// 处理操作
const handleAction = () => {
  emit('action');
};
</script>

<style scoped lang="less">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  
  &--small {
    padding: 20px 16px;
  }
  
  &--large {
    padding: 60px 24px;
  }
}

.empty-state__image {
  margin-bottom: 16px;
  
  .empty-state--small & {
    margin-bottom: 12px;
  }
  
  .empty-state--large & {
    margin-bottom: 24px;
  }
}

.empty-image {
  max-width: 100%;
  height: auto;
  display: block;
}

.empty-state__title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  margin-bottom: 8px;
  
  .empty-state--small & {
    font-size: 14px;
    margin-bottom: 6px;
  }
  
  .empty-state--large & {
    font-size: 18px;
    margin-bottom: 12px;
  }
}

.empty-state__description {
  font-size: 14px;
  color: #999;
  line-height: 1.5;
  margin-bottom: 24px;
  max-width: 300px;
  
  .empty-state--small & {
    font-size: 12px;
    margin-bottom: 16px;
  }
  
  .empty-state--large & {
    font-size: 16px;
    margin-bottom: 32px;
  }
}

.empty-state__action {
  .empty-state--small & {
    :deep(.van-button) {
      font-size: 12px;
      padding: 6px 12px;
    }
  }
  
  .empty-state--large & {
    :deep(.van-button) {
      font-size: 16px;
      padding: 12px 24px;
    }
  }
}
</style>