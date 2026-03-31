<template>
  <div class="fee-manager">
    <div class="section-header">
      <h3 class="section-title">杂费信息</h3>
      <span class="fee-count">共 {{ currentFees.length }} 项</span>
    </div>
    
    <!-- 当前杂费列表 -->
    <div v-if="currentFees.length > 0" class="fee-list">
      <div 
        v-for="(fee, index) in currentFees" 
        :key="fee.id || index" 
        class="fee-item"
      >
        <div class="fee-info">
          <div class="fee-name">{{ fee.feeKeyName }}</div>
          <div class="fee-input-container">
            <van-field 
              v-model="fee.name" 
              type="number" 
              :placeholder="`请输入${fee.feeKeyName}`"
              class="fee-input"
              @blur="validateFeeValue(fee, index)"
            />
            <span class="fee-unit">{{ fee.unit }}</span>
          </div>
        </div>
        <van-button 
          type="danger" 
          size="small" 
          icon="delete-o"
          @click="deleteFee(index)"
        >
          删除
        </van-button>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <van-empty description="暂无杂费信息" />
    </div>
    
    <!-- 添加杂费按钮 -->
    <van-button 
      type="primary" 
      block 
      icon="plus"
      @click="showAddFee = true"
      class="add-fee-btn"
    >
      添加杂费
    </van-button>
    
    <!-- 添加杂费弹窗 -->
    <van-popup 
      v-model:show="showAddFee" 
      position="bottom" 
      :style="{ height: '80%' }"
      round
    >
      <div class="add-fee-popup">
        <div class="popup-header">
          <h3>添加杂费</h3>
          <van-icon name="cross" @click="showAddFee = false" />
        </div>
        
        <div class="popup-content">
          <!-- 选择模式切换 -->
          <div class="mode-selector">
            <van-tabs v-model:active="activeMode" @change="handleModeChange">
              <van-tab title="选择杂费" name="select">
                <!-- 可选杂费列表 -->
                <div v-if="loading" class="loading-container">
                  <van-loading type="spinner" color="#1989fa">加载中...</van-loading>
                </div>
                
                <div v-else-if="availableFees.length > 0" class="available-fees">
                  <div 
                    v-for="feeGroup in groupedFees" 
                    :key="feeGroup.feeKeyName"
                    class="fee-group"
                  >
                    <div class="fee-group-header">
                      <span class="fee-group-title">{{ feeGroup.feeKeyName }}</span>
                      <span 
                        :class="['fee-group-badge', getCreatorBadgeClass(feeGroup.creatorType)]"
                      >
                        {{ getCreatorTypeText(feeGroup.creatorType) }}
                      </span>
                    </div>
                    
                    <div class="fee-values">
                      <div 
                        v-for="feeValue in feeGroup.feeValues" 
                        :key="feeValue.id"
                        :class="[
                          'fee-value-item',
                          { 'selected': isSelectedFee(feeValue) },
                          getCreatorTypeClass(feeValue.creatorType)
                        ]"
                        @click="selectFee(feeValue)"
                      >
                        <div class="fee-value-content">
                          <span class="fee-value-name">{{ feeValue.name }}</span>
                          <span class="fee-value-unit">{{ feeValue.unit }}</span>
                        </div>
                        <van-icon 
                          v-if="isSelectedFee(feeValue)" 
                          name="success" 
                          class="check-icon" 
                        />
                      </div>
                    </div>
                  </div>
                </div>
                
                <div v-else class="empty-available">
                  <van-empty description="暂无可选杂费" />
                </div>
              </van-tab>
              
              <van-tab title="自定义杂费" name="custom">
                <!-- 自定义杂费表单 -->
                <div class="custom-fee-form">
                  <div class="form-item">
                    <label class="form-label">杂费类型</label>
                    <van-field
                      v-model="newFee.feeKeyName"
                      placeholder="请输入杂费类型（如：停车费、物业费）"
                      :rules="[{ required: true, message: '请输入杂费类型' }]"
                    />
                  </div>
                  
                  <div class="form-item">
                    <label class="form-label">杂费金额</label>
                    <van-field
                      v-model="newFee.name"
                      type="number"
                      placeholder="请输入金额"
                      :rules="[{ required: true, message: '请输入杂费金额' }]"
                    />
                  </div>
                  
                  <div class="form-item">
                    <label class="form-label">单位</label>
                    <van-field
                      v-model="newFee.unit"
                      placeholder="请输入单位（如：元/月、元/次）"
                      :rules="[{ required: true, message: '请输入单位' }]"
                    />
                  </div>
                </div>
              </van-tab>
            </van-tabs>
          </div>
        </div>
        
        <div class="popup-footer">
          <van-button block @click="showAddFee = false">取消</van-button>
          <van-button 
            type="primary" 
            block 
            @click="activeMode === 'select' ? addSelectedFees() : addCustomFee()"
          >
            {{ activeMode === 'select' ? '确认选择' : '确认添加' }}
          </van-button>
        </div>
      </div>
    </van-popup>
    
    <!-- 删除确认弹窗 -->
    <van-dialog
      v-model:show="showDeleteConfirm"
      title="确认删除"
      message="确定要删除这项杂费吗？"
      show-cancel-button
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { showToast } from 'vant';
import { getFeeKeys, type FeeKeyVo } from '@/api/landlord/management';
import type { FeeValueVo, FeeKeyInfo } from '@/types/landlord';

// Props定义
interface Props {
  modelValue: FeeValueVo[];
  currentFees?: FeeValueVo[];
  landlordId: number;
}

// Emits定义
interface Emits {
  (e: 'update:modelValue', value: FeeValueVo[]): void;
}

const props = withDefaults(defineProps<Props>(), {
  currentFees: () => []
});

const emit = defineEmits<Emits>();

// 响应式数据
const showAddFee = ref(false);
const showDeleteConfirm = ref(false);
const deleteIndex = ref(-1);
const loading = ref(false);
const activeMode = ref('select'); // 'select' 或 'custom'
const currentFees = ref<FeeValueVo[]>([...props.modelValue]);
const allFeeKeys = ref<FeeKeyVo[]>([]);
const selectedFeeKeys = ref<Set<number>>(new Set());

// 新杂费表单数据
const newFee = ref<Partial<FeeValueVo>>({
  feeKeyName: '',
  name: '',
  unit: '',
  feeKeyId: 0
});

// 计算属性
const availableFees = computed(() => {
  // 过滤掉已经添加的杂费类型
  const usedFeeKeyIds = currentFees.value.map(fee => fee.feeKeyId).filter(id => id);
  return allFeeKeys.value.filter(feeKey => !usedFeeKeyIds.includes(feeKey.id));
});

const groupedFees = computed(() => {
  const groups: { [key: string]: { feeKeyName: string; creatorType: number; feeValues: any[] } } = {};
  
  availableFees.value.forEach(feeKey => {
    if (!groups[feeKey.name]) {
      groups[feeKey.name] = {
        feeKeyName: feeKey.name,
        creatorType: feeKey.creatorType || 0,
        feeValues: []
      };
    }
    
    // 如果有杂费值列表，添加到组中
    if (feeKey.feeValueList && feeKey.feeValueList.length > 0) {
      feeKey.feeValueList.forEach(feeValue => {
        groups[feeKey.name].feeValues.push({
          ...feeValue,
          feeKeyId: feeKey.id,
          feeKeyName: feeKey.name,
          unit: feeValue.unit || feeKey.unit || ''
        });
      });
    } else {
      // 如果没有杂费值列表，创建一个基于杂费键的项目
      groups[feeKey.name].feeValues.push({
        id: feeKey.id,
        feeKeyId: feeKey.id,
        feeKeyName: feeKey.name,
        name: feeKey.name,
        unit: feeKey.unit || '',
        creatorType: feeKey.creatorType
      });
    }
  });
  
  return Object.values(groups);
});

// 方法
const loadFeeKeys = async () => {
  try {
    loading.value = true;
    console.log('加载杂费类型列表，房东ID:', props.landlordId);
    
    const response = await getFeeKeys(props.landlordId);
    allFeeKeys.value = response.data || [];
    
    console.log('杂费类型加载成功:', allFeeKeys.value.length, '项');
    console.log('杂费类型详情:', allFeeKeys.value);
  } catch (error) {
    console.error('加载杂费类型失败:', error);
    showToast('加载杂费类型失败，请重试');
  } finally {
    loading.value = false;
  }
};

const getCreatorTypeClass = (creatorType: number) => {
  if (creatorType === 1) {
    return 'official-fee'; // 官方 - 蓝色
  } else if (creatorType === 2) {
    return 'landlord-fee'; // 房东 - 绿色
  }
  return 'system-fee'; // 系统 - 默认
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

const isSelectedFee = (fee: any) => {
  return selectedFeeKeys.value.has(fee.id);
};

const selectFee = (fee: any) => {
  if (selectedFeeKeys.value.has(fee.id)) {
    selectedFeeKeys.value.delete(fee.id);
  } else {
    selectedFeeKeys.value.add(fee.id);
  }
};

const handleModeChange = (name: string | number) => {
  activeMode.value = name as string;
  // 切换模式时清空选择
  selectedFeeKeys.value.clear();
};

const addSelectedFees = () => {
  if (selectedFeeKeys.value.size === 0) {
    showToast('请选择要添加的杂费');
    return;
  }
  
  // 从所有分组的杂费值中找到选中的项目
  const selectedFees: any[] = [];
  groupedFees.value.forEach(group => {
    group.feeValues.forEach(feeValue => {
      if (selectedFeeKeys.value.has(feeValue.id)) {
        selectedFees.push(feeValue);
      }
    });
  });
  
  selectedFees.forEach(selectedFee => {
    const feeToAdd: FeeValueVo = {
      id: Date.now() + Math.random(), // 临时ID，保存时由后端生成
      feeKeyId: selectedFee.feeKeyId,
      feeKeyName: selectedFee.feeKeyName,
      name: selectedFee.name || '', // 保持原有金额值，如果没有则为空
      unit: selectedFee.unit || ''
    };
    
    currentFees.value.push(feeToAdd);
  });
  
  // 重置状态
  selectedFeeKeys.value.clear();
  showAddFee.value = false;
  updateFees();
  
  showToast(`已添加 ${selectedFees.length} 项杂费`);
};

const addCustomFee = () => {
  // 验证表单
  if (!newFee.value.feeKeyName || !newFee.value.name || !newFee.value.unit) {
    showToast('请填写完整的杂费信息');
    return;
  }
  
  const value = parseFloat(newFee.value.name);
  if (isNaN(value) || value <= 0) {
    showToast('请输入有效的杂费金额');
    return;
  }
  
  // 检查是否已存在相同的杂费类型
  const exists = currentFees.value.some(fee => 
    fee.feeKeyName === newFee.value.feeKeyName
  );
  
  if (exists) {
    showToast('该杂费类型已存在');
    return;
  }
  
  // 添加新杂费
  const feeToAdd: FeeValueVo = {
    id: Date.now(), // 临时ID，保存时由后端生成
    feeKeyId: 0, // 自定义杂费，保存时由后端处理
    feeKeyName: newFee.value.feeKeyName!,
    name: newFee.value.name!,
    unit: newFee.value.unit!
  };
  
  currentFees.value.push(feeToAdd);
  
  // 重置表单
  newFee.value = {
    feeKeyName: '',
    name: '',
    unit: '',
    feeKeyId: 0
  };
  
  showAddFee.value = false;
  updateFees();
  
  showToast('杂费添加成功');
};

const validateFeeValue = (fee: FeeValueVo, index: number) => {
  if (!fee.name || fee.name.trim() === '') {
    showToast('请输入杂费金额');
    return;
  }
  
  const value = parseFloat(fee.name);
  if (isNaN(value) || value <= 0) {
    showToast('请输入有效的杂费金额');
    fee.name = '';
    return;
  }
  
  // 更新数据
  updateFees();
};

const deleteFee = (index: number) => {
  deleteIndex.value = index;
  showDeleteConfirm.value = true;
};

const confirmDelete = () => {
  if (deleteIndex.value >= 0 && deleteIndex.value < currentFees.value.length) {
    const deletedFee = currentFees.value[deleteIndex.value];
    currentFees.value.splice(deleteIndex.value, 1);
    updateFees();
    showToast(`已删除${deletedFee.feeKeyName}`);
  }
  
  showDeleteConfirm.value = false;
  deleteIndex.value = -1;
};

const cancelDelete = () => {
  showDeleteConfirm.value = false;
  deleteIndex.value = -1;
};

const updateFees = () => {
  emit('update:modelValue', [...currentFees.value]);
  console.log('杂费信息更新:', currentFees.value);
};

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  currentFees.value = [...newValue];
}, { deep: true });

watch(() => props.landlordId, (newLandlordId) => {
  if (newLandlordId) {
    loadFeeKeys();
  }
});

// 生命周期
onMounted(() => {
  console.log('杂费管理器初始化，当前杂费:', currentFees.value);
  if (props.landlordId) {
    loadFeeKeys();
  }
});
</script>

<style scoped>
.fee-manager {
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

.fee-count {
  font-size: 12px;
  color: #969799;
}

.fee-list {
  margin-bottom: 16px;
}

.fee-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f7f8fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  gap: 12px;
}

.fee-info {
  flex: 1;
}

.fee-name {
  font-size: 14px;
  font-weight: 500;
  color: #323233;
  margin-bottom: 8px;
}

.fee-input-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fee-input {
  flex: 1;
  background: white;
  border-radius: 4px;
}

.fee-unit {
  font-size: 12px;
  color: #969799;
  white-space: nowrap;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 120px;
  margin-bottom: 16px;
}

.add-fee-btn {
  margin-top: 16px;
}

.add-fee-popup {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebedf0;
}

.popup-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #323233;
  margin: 0;
}

.popup-content {
  flex: 1;
  overflow-y: auto;
}

.form-item {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #323233;
  margin-bottom: 8px;
}

.popup-footer {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #ebedf0;
}

/* 杂费选择相关样式 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.available-fees {
  padding: 16px 0;
}

.fee-group {
  margin-bottom: 24px;
}

.fee-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 4px;
}

.fee-group-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}

.fee-group-badge {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 12px;
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

.fee-values {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fee-value-item {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f7f8fa;
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.fee-value-item:active {
  transform: scale(0.98);
}

.fee-value-item.selected {
  background: #e6f4ff;
  border-color: #1989fa;
}

.fee-value-item.official-fee {
  border-left: 4px solid #1989fa;
}

.fee-value-item.landlord-fee {
  border-left: 4px solid #07c160;
}

.fee-value-item.system-fee {
  border-left: 4px solid #969799;
}

.fee-value-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
}

.fee-value-name {
  font-size: 14px;
  font-weight: 500;
  color: #323233;
}

.fee-value-unit {
  font-size: 12px;
  color: #969799;
}

.check-icon {
  color: #1989fa;
  font-size: 16px;
  margin-left: 8px;
}

.empty-available {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.custom-fee-form {
  padding: 16px 0;
}

.mode-selector {
  margin-top: -20px; /* 抵消popup-content的padding */
}

/* 覆盖vant tabs样式 */
:deep(.van-tabs__nav) {
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

:deep(.van-tab) {
  flex: 1;
  color: #646566;
}

:deep(.van-tab--active) {
  color: #1989fa;
  background: white;
  border-radius: 6px;
  margin: 2px;
}

:deep(.van-tabs__line) {
  display: none;
}

:deep(.van-tabs__content) {
  padding: 0;
}
</style>