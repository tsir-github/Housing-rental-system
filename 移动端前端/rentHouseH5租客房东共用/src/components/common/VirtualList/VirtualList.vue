<template>
  <div 
    ref="containerRef"
    class="virtual-list"
    :style="{ height: containerHeight }"
    @scroll="handleScroll"
  >
    <!-- 虚拟滚动容器 -->
    <div 
      class="virtual-list__phantom"
      :style="{ height: `${totalHeight}px` }"
    ></div>
    
    <!-- 可见项容器 -->
    <div 
      class="virtual-list__content"
      :style="{ transform: `translateY(${offsetY}px)` }"
    >
      <div
        v-for="item in visibleItems"
        :key="getItemKey(item.data, item.index)"
        class="virtual-list__item"
        :style="{ height: `${itemHeight}px` }"
      >
        <slot 
          :item="item.data" 
          :index="item.index"
          :isVisible="item.isVisible"
        >
          <div class="virtual-list__default-item">
            {{ item.data }}
          </div>
        </slot>
      </div>
    </div>
    
    <!-- 加载更多 -->
    <div 
      v-if="hasMore && visibleItems.length > 0"
      class="virtual-list__loading"
      :style="{ transform: `translateY(${totalHeight}px)` }"
    >
      <slot name="loading">
        <div class="virtual-list__loading-default">
          <van-loading size="20px">加载中...</van-loading>
        </div>
      </slot>
    </div>
    
    <!-- 空状态 -->
    <div 
      v-if="items.length === 0 && !loading"
      class="virtual-list__empty"
    >
      <slot name="empty">
        <van-empty description="暂无数据" />
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';

interface Props {
  items: any[];
  itemHeight: number;
  containerHeight?: string;
  bufferSize?: number;
  hasMore?: boolean;
  loading?: boolean;
  keyField?: string;
  estimatedItemHeight?: number;
  overscan?: number;
}

interface VisibleItem {
  data: any;
  index: number;
  isVisible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  containerHeight: '400px',
  bufferSize: 5,
  hasMore: false,
  loading: false,
  keyField: 'id',
  estimatedItemHeight: 50,
  overscan: 3
});

const emit = defineEmits<{
  loadMore: [];
  scroll: [{ scrollTop: number; scrollLeft: number }];
  itemVisible: [{ item: any; index: number; isVisible: boolean }];
}>();

// 响应式数据
const containerRef = ref<HTMLElement>();
const scrollTop = ref(0);
const containerClientHeight = ref(0);
const isScrolling = ref(false);
const scrollTimeout = ref<number>();

// 计算属性
const totalHeight = computed(() => {
  return props.items.length * props.itemHeight;
});

const visibleRange = computed(() => {
  const containerHeight = containerClientHeight.value || 400;
  const start = Math.floor(scrollTop.value / props.itemHeight);
  const visibleCount = Math.ceil(containerHeight / props.itemHeight);
  
  // 添加缓冲区
  const startWithBuffer = Math.max(0, start - props.bufferSize);
  const endWithBuffer = Math.min(
    props.items.length - 1,
    start + visibleCount + props.bufferSize
  );
  
  return {
    start: startWithBuffer,
    end: endWithBuffer,
    visibleStart: start,
    visibleEnd: start + visibleCount
  };
});

const visibleItems = computed((): VisibleItem[] => {
  const { start, end, visibleStart, visibleEnd } = visibleRange.value;
  const items: VisibleItem[] = [];
  
  for (let i = start; i <= end; i++) {
    if (props.items[i]) {
      items.push({
        data: props.items[i],
        index: i,
        isVisible: i >= visibleStart && i <= visibleEnd
      });
    }
  }
  
  return items;
});

const offsetY = computed(() => {
  return visibleRange.value.start * props.itemHeight;
});

// 方法
const getItemKey = (item: any, index: number): string | number => {
  if (props.keyField && item[props.keyField] !== undefined) {
    return item[props.keyField];
  }
  return index;
};

const handleScroll = (event: Event) => {
  const target = event.target as HTMLElement;
  scrollTop.value = target.scrollTop;
  
  isScrolling.value = true;
  
  // 清除之前的定时器
  if (scrollTimeout.value) {
    clearTimeout(scrollTimeout.value);
  }
  
  // 设置新的定时器
  scrollTimeout.value = window.setTimeout(() => {
    isScrolling.value = false;
  }, 150);
  
  // 触发滚动事件
  emit('scroll', {
    scrollTop: target.scrollTop,
    scrollLeft: target.scrollLeft
  });
  
  // 检查是否需要加载更多
  checkLoadMore(target);
  
  // 触发可见性变化事件
  emitVisibilityChanges();
};

const checkLoadMore = (target: HTMLElement) => {
  if (!props.hasMore || props.loading) return;
  
  const { scrollTop, scrollHeight, clientHeight } = target;
  const threshold = 100; // 距离底部100px时触发加载
  
  if (scrollTop + clientHeight >= scrollHeight - threshold) {
    emit('loadMore');
  }
};

const emitVisibilityChanges = () => {
  visibleItems.value.forEach(item => {
    emit('itemVisible', {
      item: item.data,
      index: item.index,
      isVisible: item.isVisible
    });
  });
};

const scrollToIndex = (index: number, behavior: ScrollBehavior = 'smooth') => {
  if (!containerRef.value) return;
  
  const targetScrollTop = index * props.itemHeight;
  containerRef.value.scrollTo({
    top: targetScrollTop,
    behavior
  });
};

const scrollToTop = (behavior: ScrollBehavior = 'smooth') => {
  scrollToIndex(0, behavior);
};

const scrollToBottom = (behavior: ScrollBehavior = 'smooth') => {
  if (!containerRef.value) return;
  
  containerRef.value.scrollTo({
    top: totalHeight.value,
    behavior
  });
};

const getVisibleRange = () => {
  return visibleRange.value;
};

const updateContainerHeight = () => {
  if (containerRef.value) {
    containerClientHeight.value = containerRef.value.clientHeight;
  }
};

// 生命周期
onMounted(() => {
  updateContainerHeight();
  
  // 监听容器大小变化
  if (containerRef.value) {
    const resizeObserver = new ResizeObserver(() => {
      updateContainerHeight();
    });
    
    resizeObserver.observe(containerRef.value);
    
    onUnmounted(() => {
      resizeObserver.disconnect();
    });
  }
});

onUnmounted(() => {
  if (scrollTimeout.value) {
    clearTimeout(scrollTimeout.value);
  }
});

// 监听数据变化
watch(
  () => props.items.length,
  () => {
    nextTick(() => {
      updateContainerHeight();
    });
  }
);

// 暴露方法给父组件
defineExpose({
  scrollToIndex,
  scrollToTop,
  scrollToBottom,
  getVisibleRange,
  updateContainerHeight
});
</script>

<style scoped lang="less">
.virtual-list {
  position: relative;
  overflow: auto;
  -webkit-overflow-scrolling: touch;
  
  &__phantom {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    z-index: -1;
  }
  
  &__content {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
  }
  
  &__item {
    box-sizing: border-box;
  }
  
  &__default-item {
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    align-items: center;
  }
  
  &__loading {
    position: absolute;
    left: 0;
    right: 0;
    z-index: 1;
  }
  
  &__loading-default {
    padding: 20px;
    text-align: center;
    color: #969799;
  }
  
  &__empty {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
  }
}

// 滚动条样式优化
.virtual-list::-webkit-scrollbar {
  width: 4px;
}

.virtual-list::-webkit-scrollbar-track {
  background: transparent;
}

.virtual-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 2px;
}

.virtual-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

// 响应式设计
@media (max-width: 375px) {
  .virtual-list {
    &__default-item {
      padding: 10px 12px;
    }
    
    &__loading-default {
      padding: 16px;
    }
  }
}
</style>