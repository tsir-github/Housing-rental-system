<template>
  <van-nav-bar
    v-if="showNavBar"
    :title="title"
    left-arrow
    @click-left="onClickLeft"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { NavigationHelper } from '@/utils/navigationHelper';

const route = useRoute();
const router = useRouter();

// 是否显示导航栏
const showNavBar = computed(() => {
  // 登录页面不显示导航栏
  if (route.path === '/login') return false;
  
  // 根据路由meta配置决定是否显示
  return route.meta?.isShowNavBar !== false;
});

// 页面标题
const title = computed(() => {
  return route.meta?.title || '公寓找房';
});

// 返回按钮点击事件
const onClickLeft = () => {
  console.log('🧭 全局NavBar返回按钮被点击');
  // 使用智能返回逻辑，避免返回到登录页面
  NavigationHelper.globalSmartGoBack(router);
};
</script>