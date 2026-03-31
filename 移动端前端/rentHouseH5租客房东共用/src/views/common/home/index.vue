<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1>欢迎来到公寓租赁系统</h1>
      <p>您的理想居住选择</p>
    </div>
    
    <div class="quick-actions">
      <van-grid :column-num="2" :gutter="10">
        <van-grid-item icon="home-o" text="房源管理" />
        <van-grid-item icon="user-o" text="个人中心" />
        <van-grid-item icon="chat-o" text="消息中心" />
        <van-grid-item icon="setting-o" text="设置" />
      </van-grid>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';

onMounted(() => {
  console.log('首页加载完成');
});
</script>

<style scoped lang="less">
.home-container {
  padding: 20px;
  min-height: 100vh;
  background: #f8f9fa;
  
  .welcome-section {
    text-align: center;
    padding: 40px 0;
    
    h1 {
      font-size: 28px;
      color: #333;
      margin-bottom: 10px;
    }
    
    p {
      font-size: 16px;
      color: #666;
    }
  }
  
  .quick-actions {
    margin-top: 30px;
  }
}
</style>