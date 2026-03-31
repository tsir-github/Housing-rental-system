<template>
  <div class="message-container">
    <van-nav-bar title="消息中心" left-arrow @click-left="goBack" />
    
    <div class="message-list">
      <van-empty
        image="search"
        description="暂无消息"
      />
    </div>
  </div>
</template>

<script setup lang="ts" name="Message">
import { useRouter } from 'vue-router';

const router = useRouter();

const goBack = () => {
  router.back();
};
</script>

<style scoped lang="less">
.message-container {
  min-height: 100vh;
  background: #f8f9fa;
  
  .message-list {
    padding: 20px;
  }
}
</style>