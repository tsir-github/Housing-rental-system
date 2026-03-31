<template>
  <van-tag 
    :type="tagType" 
    :color="tagColor"
    :text-color="textColor"
    :size="size"
    :round="round"
    :plain="plain"
  >
    <van-icon 
      v-if="showIcon" 
      :name="iconName" 
      size="12" 
      style="margin-right: 4px;"
    />
    {{ statusText }}
  </van-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  // 审核状态：1-待审核，2-审核通过，3-审核拒绝
  status: number;
  // 尺寸
  size?: 'large' | 'medium' | 'small';
  // 是否圆角
  round?: boolean;
  // 是否朴素模式
  plain?: boolean;
  // 是否显示图标
  showIcon?: boolean;
  // 自定义文本
  customText?: string;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'medium',
  round: true,
  plain: false,
  showIcon: true
});

// 状态配置
const statusConfig = computed(() => {
  const configs = {
    1: {
      text: '待审核',
      type: 'warning' as const,
      color: '#ff976a',
      textColor: '#fff',
      icon: 'clock-o'
    },
    2: {
      text: '审核通过',
      type: 'success' as const,
      color: '#07c160',
      textColor: '#fff',
      icon: 'success'
    },
    3: {
      text: '审核拒绝',
      type: 'danger' as const,
      color: '#ee0a24',
      textColor: '#fff',
      icon: 'close'
    },
    0: {
      text: '未审核',
      type: 'default' as const,
      color: '#c8c9cc',
      textColor: '#fff',
      icon: 'question-o'
    }
  };
  
  return configs[props.status as keyof typeof configs] || configs[0];
});

// 计算属性
const statusText = computed(() => {
  return props.customText || statusConfig.value.text;
});

const tagType = computed(() => {
  return statusConfig.value.type;
});

const tagColor = computed(() => {
  return props.plain ? undefined : statusConfig.value.color;
});

const textColor = computed(() => {
  return props.plain ? statusConfig.value.color : statusConfig.value.textColor;
});

const iconName = computed(() => {
  return statusConfig.value.icon;
});
</script>

<style scoped lang="less">
// 自定义样式可以在这里添加
</style>