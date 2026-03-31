<template>
  <van-tabbar 
    v-if="showTabbar" 
    v-model="active" 
    @change="onChange"
    fixed
    placeholder
  >
    <van-tabbar-item
      v-for="(item, index) in tabbarItems"
      :key="item.name"
      :icon="item.icon"
      :to="item.path"
    >
      {{ item.title }}
    </van-tabbar-item>
  </van-tabbar>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '@/store/modules/user';

const route = useRoute();
const userStore = useUserStore();
const active = ref(0);

// 是否显示底部导航栏
const showTabbar = computed(() => {
  return route.meta?.showTabbar === true;
});

// 根据用户类型显示不同的底部导航
const tabbarItems = computed(() => {
  if (userStore.isLandlord) {
    // 房东底部导航
    return [
      {
        name: 'dashboard',
        title: '控制台',
        icon: 'home-o',
        path: '/landlord/dashboard'
      }
    ];
  } else {
    // 租客底部导航 - 使用完整的 /tenant/ 前缀路径
    return [
      {
        name: 'search',
        title: '找房',
        icon: 'search',
        path: '/tenant/search'
      },
      {
        name: 'aiChat',
        title: 'AI助手',
        icon: 'service-o',
        path: '/tenant/aiChat'
      },
      {
        name: 'myRoom',
        title: '我的房间',
        icon: 'home-o',
        path: '/tenant/myRoom'
      },
      {
        name: 'message',
        title: '消息',
        icon: 'comment-o',
        path: '/tenant/message'
      },
      {
        name: 'userCenter',
        title: '个人中心',
        icon: 'user-o',
        path: '/tenant/userCenter'
      }
    ];
  }
});

// 监听路由变化，同步活动状态
watch(() => route.path, (newPath) => {
  const currentIndex = tabbarItems.value.findIndex(item => item.path === newPath);
  if (currentIndex !== -1) {
    active.value = currentIndex;
  }
}, { immediate: true });

const onChange = (index: number) => {
  active.value = index;
};
</script>