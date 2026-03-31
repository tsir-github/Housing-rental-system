<template>
  <div class="not-found-container">
    <van-empty
      image="error"
      description="页面不存在"
    >
      <van-button 
        type="primary" 
        size="small"
        @click="goHome"
      >
        返回首页
      </van-button>
    </van-empty>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

const goHome = () => {
  router.replace('/home');
};
</script>

<style scoped lang="less">
.not-found-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f8f9fa;
}
</style>