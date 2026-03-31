<template>
  <div class="publisher-badge" :class="badgeClass">
    <span class="badge-text">{{ badgeText }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { PublisherType } from '@/api/tenant/search/types';

interface Props {
  publisherType?: PublisherType | string;
  size?: 'small' | 'medium' | 'large';
}

const props = withDefaults(defineProps<Props>(), {
  publisherType: 'OFFICIAL',
  size: 'small'
});

// 计算徽章文本
const badgeText = computed(() => {
  // 处理数字类型的发布者类型
  if (props.publisherType === 1 || props.publisherType === '1') {
    return '官方';
  }
  if (props.publisherType === 2 || props.publisherType === '2') {
    return '房东';
  }
  
  // 处理字符串类型的发布者类型
  switch (props.publisherType) {
    case 'OFFICIAL':
      return '官方';
    case 'LANDLORD':
      return '房东';
    default:
      return '官方';
  }
});

// 计算徽章样式类
const badgeClass = computed(() => {
  // 判断是否为房东类型
  const isLandlord = props.publisherType === 2 || 
                    props.publisherType === '2' || 
                    props.publisherType === 'LANDLORD';
  
  const typeClass = isLandlord ? 'landlord' : 'official';
  const sizeClass = `size-${props.size}`;
  return `${typeClass} ${sizeClass}`;
});
</script>

<style scoped lang="less">
.publisher-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-weight: 500;
  white-space: nowrap;
  
  .badge-text {
    line-height: 1;
  }
  
  // 官方发布样式
  &.official {
    background-color: #e8f4fd;
    color: #1890ff;
    border: 1px solid #91d5ff;
  }
  
  // 房东发布样式
  &.landlord {
    background-color: #f6ffed;
    color: #52c41a;
    border: 1px solid #b7eb8f;
  }
  
  // 尺寸样式
  &.size-small {
    padding: 2px 6px;
    font-size: 10px;
    min-width: 28px;
    height: 16px;
  }
  
  &.size-medium {
    padding: 4px 8px;
    font-size: 12px;
    min-width: 32px;
    height: 20px;
  }
  
  &.size-large {
    padding: 6px 12px;
    font-size: 14px;
    min-width: 40px;
    height: 24px;
  }
}
</style>