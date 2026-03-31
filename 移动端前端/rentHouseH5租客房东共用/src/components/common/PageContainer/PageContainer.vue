<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div v-if="showHeader" class="page-header">
      <van-nav-bar
        :title="title"
        :left-text="leftText"
        :right-text="rightText"
        left-arrow
        @click-left="onClickLeft"
        @click-right="onClickRight"
      />
    </div>
    
    <!-- 自定义头部内容 -->
    <div v-if="$slots.header" class="page-custom-header">
      <slot name="header" />
    </div>
    
    <!-- 页面内容 -->
    <div class="page-content" :class="{ 'with-header': showHeader || $slots.header }">
      <!-- 加载状态 -->
      <div v-if="loading" class="page-loading">
        <van-loading vertical>加载中...</van-loading>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="showEmpty" class="page-empty">
        <van-empty 
          :image="emptyImage" 
          :description="emptyDescription"
        >
          <van-button 
            v-if="emptyActionText" 
            type="primary" 
            size="small"
            @click="$emit('emptyAction')"
          >
            {{ emptyActionText }}
          </van-button>
        </van-empty>
      </div>
      
      <!-- 正常内容 -->
      <div v-else>
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { NavigationHelper } from '@/utils/navigationHelper';

interface Props {
  title?: string;
  leftText?: string;
  rightText?: string;
  showHeader?: boolean;
  loading?: boolean;
  showEmpty?: boolean;
  emptyType?: string;
  emptyImage?: string;
  emptyDescription?: string;
  emptyActionText?: string;
  fallbackPath?: string; // 新增：自定义回退路径
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  leftText: '',
  rightText: '',
  showHeader: true,
  loading: false,
  showEmpty: false,
  emptyType: 'default',
  emptyImage: '',
  emptyDescription: '暂无数据',
  emptyActionText: ''
});

const emit = defineEmits<{
  clickLeft: [];
  clickRight: [];
  emptyAction: [];
}>();

const router = useRouter();

const onClickLeft = () => {
  emit('clickLeft');
  
  console.log('🧭 PageContainer返回按钮被点击');
  
  // 使用智能返回逻辑，支持自定义回退路径
  if (props.fallbackPath) {
    console.log('🧭 使用自定义回退路径:', props.fallbackPath);
    NavigationHelper.smartGoBack(router, props.fallbackPath);
  } else {
    console.log('🧭 使用默认智能返回逻辑');
    NavigationHelper.smartGoBack(router);
  }
};

const onClickRight = () => {
  emit('clickRight');
};
</script>

<style scoped lang="less">
.page-container {
  min-height: 100vh;
  background: #f8f9fa;
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-custom-header {
  background: white;
  padding: 16px;
  border-bottom: 1px solid #ebedf0;
}

.page-content {
  padding: 16px;
  
  &.with-header {
    padding-top: 16px;
  }
}

.page-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.page-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}
</style>