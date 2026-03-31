<template>
  <div class="facility-selector">
    <div class="section-header">
      <h3 class="section-title">配套设施</h3>
      <span class="facility-count">已选择 {{ selectedFacilities.length }} 项</span>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" color="#1989fa">加载中...</van-loading>
    </div>
    
    <!-- 设施网格 -->
    <div v-else class="facility-grid">
      <div 
        v-for="facility in allFacilities" 
        :key="facility.id"
        :class="[
          'facility-item',
          { 'selected': isSelected(facility.id) },
          getCreatorTypeClass(facility.creatorType)
        ]"
        @click="toggleFacility(facility.id)"
      >
        <div class="facility-content">
          <van-icon :name="facility.icon || 'setting-o'" class="facility-icon" />
          <span class="facility-name">{{ facility.name }}</span>
          <van-icon 
            v-if="isSelected(facility.id)" 
            name="success" 
            class="check-icon" 
          />
        </div>
        <div class="creator-badge" :class="getCreatorBadgeClass(facility.creatorType)">
          {{ getCreatorTypeText(facility.creatorType) }}
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-if="!loading && allFacilities.length === 0" class="empty-state">
      <van-empty description="暂无可用的配套设施" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { showToast } from 'vant';
import { getAvailableFacilitiesForEdit } from '@/api/landlord/management';
import type { FacilityInfo } from '@/types/landlord';

// Props定义
interface Props {
  modelValue: number[];
  selectedFacilities?: FacilityInfo[];
  landlordId: number;
}

// Emits定义
interface Emits {
  (e: 'update:modelValue', value: number[]): void;
}

const props = withDefaults(defineProps<Props>(), {
  selectedFacilities: () => []
});

const emit = defineEmits<Emits>();

// 响应式数据
const loading = ref(false);
const allFacilities = ref<FacilityInfo[]>([]);
const selectedFacilities = ref<number[]>([...props.modelValue]);

// 计算属性
const isSelected = (facilityId: number) => {
  return selectedFacilities.value.includes(facilityId);
};

const getCreatorTypeClass = (creatorType: number) => {
  if (creatorType === 1) {
    return 'official-facility'; // 官方 - 蓝色
  } else if (creatorType === 2) {
    return 'landlord-facility'; // 房东 - 绿色
  }
  return 'system-facility'; // 系统 - 默认
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
const loadFacilities = async () => {
  try {
    loading.value = true;
    console.log('加载配套设施列表，房东ID:', props.landlordId);
    
    const response = await getAvailableFacilitiesForEdit(props.landlordId);
    allFacilities.value = response.data || [];
    
    console.log('配套设施加载成功:', allFacilities.value.length, '项');
  } catch (error) {
    console.error('加载配套设施失败:', error);
    showToast('加载配套设施失败，请重试');
  } finally {
    loading.value = false;
  }
};

const toggleFacility = (facilityId: number) => {
  const index = selectedFacilities.value.indexOf(facilityId);
  
  if (index > -1) {
    // 取消选择
    selectedFacilities.value.splice(index, 1);
  } else {
    // 添加选择
    selectedFacilities.value.push(facilityId);
  }
  
  // 发出更新事件
  emit('update:modelValue', [...selectedFacilities.value]);
  
  console.log('配套设施选择更新:', selectedFacilities.value);
};

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  selectedFacilities.value = [...newValue];
}, { deep: true });

watch(() => props.landlordId, (newLandlordId) => {
  if (newLandlordId) {
    loadFacilities();
  }
});

// 生命周期
onMounted(() => {
  if (props.landlordId) {
    loadFacilities();
  }
});
</script>

<style scoped>
.facility-selector {
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

.facility-count {
  font-size: 12px;
  color: #969799;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 120px;
}

.facility-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.facility-item {
  position: relative;
  background: #f7f8fa;
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 16px 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 80px;
}

.facility-item:active {
  transform: scale(0.98);
}

.facility-item.selected {
  background: #f0f9ff;
  border-color: #1989fa;
}

.facility-item.official-facility {
  border-color: #1989fa; /* 蓝色边框 - 官方 */
}

.facility-item.official-facility.selected {
  background: #e6f4ff;
  border-color: #1989fa;
}

.facility-item.landlord-facility {
  border-color: #07c160; /* 绿色边框 - 房东 */
}

.facility-item.landlord-facility.selected {
  background: #f0f9ff;
  border-color: #07c160;
}

.facility-item.system-facility {
  border-color: #dcdee0; /* 灰色边框 - 系统 */
}

.facility-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
}

.facility-icon {
  font-size: 24px;
  color: #646566;
  margin-bottom: 8px;
}

.facility-item.selected .facility-icon {
  color: #1989fa;
}

.facility-name {
  font-size: 14px;
  color: #323233;
  line-height: 1.4;
  word-break: break-all;
}

.check-icon {
  position: absolute;
  top: -8px;
  right: -8px;
  font-size: 16px;
  color: #1989fa;
  background: white;
  border-radius: 50%;
  padding: 2px;
}

.creator-badge {
  position: absolute;
  top: 4px;
  left: 4px;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  color: white;
  font-weight: 500;
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