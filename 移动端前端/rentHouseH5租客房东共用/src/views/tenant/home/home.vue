<template>
  <div class="tenant-home">
    <van-nav-bar title="租客首页" />
    <div class="home-content">
      <van-cell-group>
        <van-cell title="欢迎使用租客端" value="功能开发中..." />
      </van-cell-group>
    </div>
  </div>
</template>

<script setup lang="ts">
// 租客首页
</script>

<style scoped lang="less">
.tenant-home {
  height: 100vh;
  background-color: #f7f8fa;
  
  .home-content {
    padding: 20px;
  }
}
</style>