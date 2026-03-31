<template>
  <div class="label-selector">
    <div class="section-header">
      <h3 class="section-title">公寓标签</h3>
      <span class="label-count">已选择 {{ selectedLabels.length }} 项</span>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" color="#1989fa">加载中...</van-loading>
    </div>
    
    <!-- 标签网格 -->
    <div v-else class="label-grid">
      <div 
        v-for="label in allLabels" 
        :key="label.id"
        :class="[
          'label-item',
          { 'selected': isSelected(label.id) },
          getCreatorTypeClass(label.creatorType)
        ]"
        @click="toggleLabel(label.id)"
      >
        <div class="label-content">
          <span class="label-name">{{ label.name }}</span>
          <van-icon 
            v-if="isSelected(label.id)" 
            name="success" 
            class="check-icon" 
          />
        </div>
        <div class="creator-badge" :class="getCreatorBadgeClass(label.creatorType)">
          {{ getCreatorTypeText(label.creatorType) }}
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-if="!loading && allLabels.length === 0" class="empty-state">
      <van-empty description="暂无可用的公寓标签" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { showToast } from 'vant';
import { getAvailableLabelsForEdit } from '@/api/landlord/management';
import type { LabelInfo } from '@/types/landlord';

// Props定义
interface Props {
  modelValue: number[];
  selectedLabels?: LabelInfo[];
  landlordId: number;
}

// Emits定义
interface Emits {
  (e: 'update:modelValue', value: number[]): void;
}

const props = withDefaults(defineProps<Props>(), {
  selectedLabels: () => []
});

const emit = defineEmits<Emits>();

// 响应式数据
const loading = ref(false);
const allLabels = ref<LabelInfo[]>([]);
const selectedLabels = ref<number[]>([...props.modelValue]);

// 计算属性
const isSelected = (labelId: number) => {
  return selectedLabels.value.includes(labelId);
};

const getCreatorTypeClass = (creatorType: number) => {
  if (creatorType === 1) {
    return 'official-label'; // 官方 - 蓝色
  } else if (creatorType === 2) {
    return 'landlord-label'; // 房东 - 绿色
  }
  return 'system-label'; // 系统 - 默认
};

const getCreatorBadgeClass = (creatorType: number) => {
  if (creatorType === 1) {
    return 'official-badge';
  } else if (creatorType === 2) {
    return 'landlord-badge';
  }
  return 'system-badge';
};

const getCreatorTypeText = (creatorType: number) => {
  if (creatorType === 1) {
    return '官方';
  } else if (creatorType === 2) {
    return '我的';
  }
  return '系统';
};

// 方法
const loadLabels = async () => {
  try {
    loading.value = true;
    console.log('加载标签列表，房东ID:', props.landlordId);
    
    const response = await getAvailableLabelsForEdit(props.landlordId);
    allLabels.value = response.data || [];
    
    console.log('标签加载成功:', allLabels.value.length, '项');
  } catch (error) {
    console.error('加载标签失败:', error);
    showToast('加载标签失败，请重试');
  } finally {
    loading.value = false;
  }
};

const toggleLabel = (labelId: number) => {
  const index = selectedLabels.value.indexOf(labelId);
  
  if (index > -1) {
    // 取消选择
    selectedLabels.value.splice(index, 1);
  } else {
    // 添加选择
    selectedLabels.value.push(labelId);
  }
  
  // 发出更新事件
  emit('update:modelValue', [...selectedLabels.value]);
  
  console.log('标签选择更新:', selectedLabels.value);
};

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  selectedLabels.value = [...newValue];
}, { deep: true });

watch(() => props.landlordId, (newLandlordId) => {
  if (newLandlordId) {
    loadLabels();
  }
});

// 生命周期
onMounted(() => {
  if (props.landlordId) {
    loadLabels();
  }
});
</script>

<style scoped>
.label-selector {
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin: 0;
}

.label-count {
  font-size: 12px;
  color: #969799;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 120px;
}

.label-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.label-item {
  position: relative;
  background: #f7f8fa;
  border: 2px solid transparent;
  border-radius: 20px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 36px;
  display: flex;
  align-items: center;
}

.label-item:active {
  transform: scale(0.98);
}

.label-item.selected {
  background: #f0f9ff;
  border-color: #1989fa;
}

.label-item.official-label {
  border-color: #1989fa; /* 蓝色边框 - 官方 */
}

.label-item.official-label.selected {
  background: #e6f4ff;
  border-color: #1989fa;
}

.label-item.landlord-label {
  border-color: #07c160; /* 绿色边框 - 房东 */
}

.label-item.landlord-label.selected {
  background: #f0f9ff;
  border-color: #07c160;
}

.label-item.system-label {
  border-color: #dcdee0; /* 灰色边框 - 系统 */
}

.label-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.label-name {
  font-size: 14px;
  color: #323233;
  line-height: 1.4;
  white-space: nowrap;
}

.label-item.selected .label-name {
  color: #1989fa;
  font-weight: 500;
}

.check-icon {
  font-size: 14px;
  color: #1989fa;
}

.creator-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  min-width: 24px;
  text-align: center;
}

.official-badge {
  background: #1989fa; /* 蓝色 - 官方 */
}

.landlord-badge {
  background: #07c160; /* 绿色 - 房东 */
}

.system-badge {
  background: #969799; /* 灰色 - 系统 */
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 120px;
}
</style>