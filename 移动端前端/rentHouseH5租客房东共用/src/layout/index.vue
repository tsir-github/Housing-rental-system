<script setup lang="ts">
import NavBar from "@/components/common/NavBar/index.vue";
import Tabbar from "@/components/common/Tabbar/index.vue";
import { useCachedViewStore } from "@/store/modules/cachedView";
import { computed } from "vue";

const cachedViewStore = useCachedViewStore();
const cachedViews = computed(() => {
  return cachedViewStore.cachedViewList;
});

// 暂时使用固定主题，避免循环依赖
const isDarkMode = false;
</script>

<template>
  <div class="app-wrapper">
    <van-config-provider :theme="isDarkMode ? 'dark' : 'light'">
      <NavBar />
      <router-view v-slot="{ Component, route }">
        <keep-alive :include="cachedViews">
          <component :is="Component" :key="route.fullPath" />
        </keep-alive>
      </router-view>
      <Tabbar />
    </van-config-provider>
  </div>
</template>

<style lang="less" scoped>
@import "@/styles/mixin.less";

.app-wrapper {
  .clearfix();
  position: relative;
  height: 100%;
  width: 100%;
}
</style>
