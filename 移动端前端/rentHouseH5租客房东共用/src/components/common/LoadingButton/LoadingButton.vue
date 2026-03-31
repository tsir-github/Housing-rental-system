<template>
  <van-button
    v-bind="$attrs"
    :loading="loading"
    @click="handleClick"
  >
    <slot />
  </van-button>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface Props {
  loadingClick?: () => Promise<void> | void;
}

const props = defineProps<Props>();

const loading = ref(false);

const handleClick = async () => {
  if (props.loadingClick) {
    try {
      loading.value = true;
      await props.loadingClick();
    } catch (error) {
      console.error('按钮点击处理失败:', error);
    } finally {
      loading.value = false;
    }
  }
};
</script>