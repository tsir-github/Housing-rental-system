<template>
  <div class="fee-manage">
    <!-- 导航栏 -->
    <van-nav-bar title="杂费管理" left-arrow @click-left="handleGoBack">
      <template #right>
        <van-button 
          type="primary" 
          size="small" 
          icon="plus"
          @click="handleAddFeeKey"
        >
          添加杂费项
        </van-button>
      </template>
    </van-nav-bar>

    <!-- 页面内容 -->
    <div class="fee-manage-content">
      <!-- 加载状态 -->
      <van-loading v-if="loading" vertical>加载中...</van-loading>

      <!-- 杂费项目列表 -->
      <div v-else class="fee-list-container">
        <van-pull-refresh v-model="refreshing" @refresh="handleRefresh">
          <div v-if="feeKeyList.length > 0" class="fee-key-list">
            <div
              v-for="feeKey in feeKeyList"
              :key="feeKey.id"
              class="fee-key-item"
            >
              <div class="fee-key-item__header">
                <div class="fee-key-info">
                  <div class="fee-key-name-wrapper">
                    <h4 class="fee-key-name">{{ feeKey.name }}</h4>
                    <van-tag 
                      v-if="feeKey.creatorType === 2" 
                      type="primary" 
                      size="small"
                      class="custom-tag"
                    >
                      自定义
                    </van-tag>
                    <van-tag 
                      v-else-if="feeKey.creatorType === 1" 
                      type="default" 
                      size="small"
                      class="official-tag"
                    >
                      官方
                    </van-tag>
                  </div>
                  <div class="fee-key-meta">
                    <span class="fee-key-creator">{{ getCreatorTypeText(feeKey.creatorType) }}</span>
                  </div>
                </div>
                
                <div class="fee-key-actions">
                  <van-button 
                    type="default" 
                    size="mini"
                    @click="handleAddFeeValue(feeKey)"
                  >
                    添加费用
                  </van-button>
                  
                  <van-button 
                    v-if="canEditFeeKey(feeKey)"
                    type="default" 
                    size="mini"
                    @click="handleEditFeeKey(feeKey)"
                  >
                    编辑
                  </van-button>
                  
                  <van-button 
                    v-if="canDeleteFeeKey(feeKey)"
                    type="danger" 
                    size="mini"
                    @click="handleDeleteFeeKey(feeKey)"
                  >
                    删除
                  </van-button>
                </div>
              </div>
              
              <!-- 杂费值列表 -->
              <div v-if="feeKey.feeValueList && feeKey.feeValueList.length > 0" class="fee-value-list">
                <div
                  v-for="feeValue in feeKey.feeValueList"
                  :key="feeValue.id"
                  class="fee-value-item"
                >
                  <div class="fee-value-info">
                    <span class="fee-value-name">{{ feeValue.name }}</span>
                    <span class="fee-value-unit">{{ feeValue.unit }}</span>
                    <van-tag 
                      v-if="feeValue.creatorType === 2" 
                      type="primary" 
                      size="mini"
                    >
                      自定义
                    </van-tag>
                  </div>
                  <div class="fee-value-actions">
                    <van-button 
                      v-if="canEditFeeValue(feeValue)"
                      type="default" 
                      size="mini"
                      @click="handleEditFeeValue(feeValue, feeKey)"
                    >
                      编辑
                    </van-button>
                    <van-button 
                      v-if="canDeleteFeeValue(feeValue)"
                      type="danger" 
                      size="mini"
                      @click="handleDeleteFeeValue(feeValue)"
                    >
                      删除
                    </van-button>
                  </div>
                </div>
              </div>
              
              <!-- 杂费项目说明 -->
              <div class="fee-key-description">
                <van-icon name="info-o" size="14" color="#969799" />
                <span>此杂费项目可用于房源发布时设置具体费用</span>
              </div>
            </div>
          </div>
          
          <EmptyState
            v-else
            type="no-data"
            title="暂无杂费项目"
            description="点击右上角按钮添加杂费项目"
            @action="handleAddFeeKey"
          />
        </van-pull-refresh>
      </div>
    </div>

    <!-- 杂费项目表单弹窗 -->
    <van-popup 
      v-model:show="showFeeKeyForm" 
      position="bottom" 
      :style="{ height: '60%' }"
      round
    >
      <div class="form-popup">
        <div class="form-popup__header">
          <h3>{{ feeKeyFormMode === 'add' ? '添加杂费项目' : '编辑杂费项目' }}</h3>
          <van-icon name="cross" size="18" @click="closeFeeKeyForm" />
        </div>
        
        <div class="form-popup__content">
          <van-form @submit="handleSubmitFeeKey">
            <van-field
              v-model="feeKeyForm.name"
              name="name"
              label="杂费名称"
              placeholder="请输入杂费项目名称"
              :rules="[{ required: true, message: '请输入杂费项目名称' }]"
            />
            
            <!-- 杂费说明 -->
            <div class="fee-description">
              <van-icon name="info-o" size="14" color="#1989fa" />
              <div class="description-text">
                <p>杂费项目用于房源发布时设置具体的费用金额，常见的杂费项目包括：</p>
                <ul>
                  <li>水费、电费、燃气费等按用量计费的项目</li>
                  <li>物业费、停车费等按月计费的项目</li>
                  <li>网络费、有线电视费等固定费用项目</li>
                </ul>
              </div>
            </div>
            
            <div class="form-actions">
              <van-button 
                type="default" 
                size="large" 
                @click="closeFeeKeyForm"
              >
                取消
              </van-button>
              
              <van-button 
                type="primary" 
                size="large" 
                native-type="submit"
                :loading="submitting"
              >
                {{ feeKeyFormMode === 'add' ? '添加' : '保存' }}
              </van-button>
            </div>
          </van-form>
        </div>
      </div>
    </van-popup>

    <!-- 杂费值表单弹窗 -->
    <van-popup 
      v-model:show="showFeeValueForm" 
      position="bottom" 
      :style="{ height: '60%' }"
      round
    >
      <div class="form-popup">
        <div class="form-popup__header">
          <h3>{{ feeValueFormMode === 'add' ? '添加费用' : '编辑费用' }}</h3>
          <van-icon name="cross" size="18" @click="closeFeeValueForm" />
        </div>
        
        <div class="form-popup__content">
          <van-form @submit="handleSubmitFeeValue">
            <van-field
              v-model="feeValueForm.name"
              name="name"
              label="费用金额"
              placeholder="请输入费用金额"
              :rules="[{ required: true, message: '请输入费用金额' }]"
            />
            
            <van-field
              v-model="feeValueForm.unit"
              name="unit"
              label="计费单位"
              placeholder="请输入计费单位"
              :rules="[{ required: true, message: '请输入计费单位' }]"
            />
            
            <!-- 常用单位快速选择 -->
            <van-field name="quickUnit" label="常用单位">
              <template #input>
                <div class="quick-unit-selector">
                  <van-tag
                    v-for="unit in commonUnits"
                    :key="unit"
                    :type="feeValueForm.unit === unit ? 'primary' : 'default'"
                    size="small"
                    @click="selectUnit(unit)"
                  >
                    {{ unit }}
                  </van-tag>
                </div>
              </template>
            </van-field>
            
            <div class="form-actions">
              <van-button 
                type="default" 
                size="large" 
                @click="closeFeeValueForm"
              >
                取消
              </van-button>
              
              <van-button 
                type="primary" 
                size="large" 
                native-type="submit"
                :loading="submitting"
              >
                {{ feeValueFormMode === 'add' ? '添加' : '保存' }}
              </van-button>
            </div>
          </van-form>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import {
  getFeeKeys,
  createFeeKey,
  updateFeeKey,
  deleteFeeKey,
  createFeeValue,
  updateFeeValue,
  deleteFeeValue
} from '@/api/landlord/management';
import type { FeeKey, FeeKeyVo, FeeValue } from '@/api/landlord/management';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';
import { useUserStore } from '@/store/modules/user';

// 响应式数据
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false);
const refreshing = ref(false);
const submitting = ref(false);
const feeKeyList = ref<FeeKeyVo[]>([]);

// 处理返回按钮点击
const handleGoBack = () => {
  // 返回到房东仪表板
  router.push('/landlord/dashboard')
}

// 获取房东ID
const landlordId = computed(() => {
  return userStore.userInfo?.id || 0
})

// 杂费项目表单
const showFeeKeyForm = ref(false);
const feeKeyFormMode = ref<'add' | 'edit'>('add');
const feeKeyForm = ref<FeeKey>({
  name: ''
});
const currentFeeKey = ref<FeeKey | null>(null);

// 杂费值表单
const showFeeValueForm = ref(false);
const feeValueFormMode = ref<'add' | 'edit'>('add');
const feeValueForm = ref<FeeValue>({
  name: '',
  unit: '',
  feeKeyId: 0
});
const currentFeeValue = ref<FeeValue | null>(null);
const currentFeeKeyForValue = ref<FeeKey | null>(null);

// 常用单位
const commonUnits = ref([
  '元/月', '元/度', '元/吨', '元/次', '元/年', 
  '元/平方米', '元/户', '元/车位', '元/天', '元/小时'
]);

// 计算属性
const showEmpty = computed(() => 
  !loading.value && feeKeyList.value.length === 0
);

// 加载杂费项目列表
const loadFeeKeys = async () => {
  try {
    console.log('🏠 开始加载杂费数据...')
    
    const { data } = await getFeeKeys();
    console.log('🏠 杂费API响应:', data)
    feeKeyList.value = data || [];
    console.log('🏠 杂费列表:', feeKeyList.value)
  } catch (error) {
    console.error('🏠 加载杂费项目列表失败:', error);
    showToast('加载杂费项目列表失败');
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    await loadFeeKeys();
  } catch (error) {
    console.error('初始化数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 事件处理
const handleRefresh = async () => {
  try {
    refreshing.value = true;
    await loadFeeKeys();
    showToast('刷新成功');
  } catch (error) {
    showToast('刷新失败');
  } finally {
    refreshing.value = false;
  }
};

// 杂费项目操作
const handleAddFeeKey = () => {
  feeKeyFormMode.value = 'add';
  feeKeyForm.value = {
    name: ''
  };
  currentFeeKey.value = null;
  showFeeKeyForm.value = true;
};

const handleEditFeeKey = (feeKey: FeeKey) => {
  feeKeyFormMode.value = 'edit';
  feeKeyForm.value = {
    id: feeKey.id,
    name: feeKey.name
  };
  currentFeeKey.value = feeKey;
  showFeeKeyForm.value = true;
};

const handleDeleteFeeKey = async (feeKey: FeeKey) => {
  try {
    await showConfirmDialog({
      title: '删除确认',
      message: `确定要删除杂费项目"${feeKey.name}"吗？删除后相关的房源杂费设置可能受到影响。`,
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
    
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    await deleteFeeKey(landlordId.value, feeKey.id!);
    showToast('删除成功');
    
    // 从列表中移除
    const index = feeKeyList.value.findIndex(item => item.id === feeKey.id);
    if (index !== -1) {
      feeKeyList.value.splice(index, 1);
    }
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除杂费项目失败:', error);
      showToast('删除失败');
    }
  }
};

const handleSubmitFeeKey = async () => {
  try {
    submitting.value = true;
    
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    if (feeKeyFormMode.value === 'add') {
      await createFeeKey(landlordId.value, feeKeyForm.value);
      showToast('添加成功');
    } else {
      await updateFeeKey(landlordId.value, feeKeyForm.value);
      showToast('更新成功');
    }
    
    closeFeeKeyForm();
    await loadFeeKeys(); // 重新加载数据
    
  } catch (error) {
    console.error('提交杂费项目失败:', error);
    showToast('操作失败');
  } finally {
    submitting.value = false;
  }
};

const closeFeeKeyForm = () => {
  showFeeKeyForm.value = false;
  feeKeyForm.value = { name: '' };
  currentFeeKey.value = null;
};

// 杂费值操作
const handleAddFeeValue = (feeKey: FeeKey) => {
  feeValueFormMode.value = 'add';
  feeValueForm.value = {
    name: '',
    unit: '',
    feeKeyId: feeKey.id!
  };
  currentFeeValue.value = null;
  currentFeeKeyForValue.value = feeKey;
  showFeeValueForm.value = true;
};

const handleEditFeeValue = (feeValue: FeeValue, feeKey: FeeKey) => {
  feeValueFormMode.value = 'edit';
  feeValueForm.value = {
    id: feeValue.id,
    name: feeValue.name,
    unit: feeValue.unit,
    feeKeyId: feeValue.feeKeyId
  };
  currentFeeValue.value = feeValue;
  currentFeeKeyForValue.value = feeKey;
  showFeeValueForm.value = true;
};

const handleDeleteFeeValue = async (feeValue: FeeValue) => {
  try {
    await showConfirmDialog({
      title: '删除确认',
      message: `确定要删除费用"${feeValue.name} ${feeValue.unit}"吗？`,
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
    
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    await deleteFeeValue(landlordId.value, feeValue.id!);
    showToast('删除成功');
    
    await loadFeeKeys(); // 重新加载数据
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除杂费值失败:', error);
      showToast('删除失败');
    }
  }
};

const handleSubmitFeeValue = async () => {
  try {
    submitting.value = true;
    
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    if (feeValueFormMode.value === 'add') {
      await createFeeValue(landlordId.value, feeValueForm.value);
      showToast('添加成功');
    } else {
      await updateFeeValue(landlordId.value, feeValueForm.value);
      showToast('更新成功');
    }
    
    closeFeeValueForm();
    await loadFeeKeys(); // 重新加载数据
    
  } catch (error) {
    console.error('提交杂费值失败:', error);
    showToast('操作失败');
  } finally {
    submitting.value = false;
  }
};

const closeFeeValueForm = () => {
  showFeeValueForm.value = false;
  feeValueForm.value = { name: '', unit: '', feeKeyId: 0 };
  currentFeeValue.value = null;
  currentFeeKeyForValue.value = null;
};

// 权限控制函数
const canEditFeeKey = (feeKey: FeeKeyVo) => {
  // 只有房东自定义的杂费才能编辑
  console.log('🔍 权限检查 - canEditFeeKey:', {
    feeKeyName: feeKey.name,
    creatorType: feeKey.creatorType,
    creatorId: feeKey.creatorId,
    currentLandlordId: landlordId.value,
    typeMatch: feeKey.creatorType === 2,
    idMatch: feeKey.creatorId === landlordId.value,
    idStrictMatch: feeKey.creatorId === Number(landlordId.value)
  });
  return feeKey.creatorType === 2 && feeKey.creatorId === Number(landlordId.value);
};

const canDeleteFeeKey = (feeKey: FeeKeyVo) => {
  // 只有房东自定义的杂费才能删除
  return feeKey.creatorType === 2 && feeKey.creatorId === Number(landlordId.value);
};

const canEditFeeValue = (feeValue: FeeValue) => {
  // 只有房东自定义的杂费值才能编辑
  return feeValue.creatorType === 2 && feeValue.creatorId === Number(landlordId.value);
};

const canDeleteFeeValue = (feeValue: FeeValue) => {
  // 只有房东自定义的杂费值才能删除
  return feeValue.creatorType === 2 && feeValue.creatorId === Number(landlordId.value);
};

// 获取创建者类型文本
const getCreatorTypeText = (creatorType?: number) => {
  console.log('🔍 创建者类型:', creatorType, typeof creatorType);
  switch (creatorType) {
    case 0: return '系统默认';
    case 1: return '官方杂费';
    case 2: return '房东自定义';
    default: return `未知来源(${creatorType})`;
  }
};

// 快速选择单位
const selectUnit = (unit: string) => {
  feeValueForm.value.unit = unit;
};

// 生命周期
onMounted(() => {
  initData();
});
</script>

<style scoped lang="less">
.fee-manage {
  min-height: 100vh;
  background: #f5f5f5;
}

.fee-manage-content {
  padding: 16px;
}

.fee-list-container {
  min-height: 200px;
}

.fee-key-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fee-key-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }
}

.fee-key-info {
  flex: 1;
  
  .fee-key-name-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
  }
  
  .fee-key-name {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin: 0;
  }
  
  .custom-tag {
    flex-shrink: 0;
  }
  
  .official-tag {
    flex-shrink: 0;
  }
  
  .fee-key-meta {
    margin-bottom: 4px;
  }
  
  .fee-key-creator {
    font-size: 11px;
    color: #646566;
    opacity: 0.8;
  }
  
  .fee-key-unit {
    font-size: 12px;
    color: #969799;
    background: #f7f8fa;
    padding: 2px 8px;
    border-radius: 4px;
  }
}

.fee-key-actions {
  display: flex;
  gap: 8px;
}

.fee-key-description {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 13px;
  color: #646566;
  line-height: 1.4;
  padding: 8px 12px;
  background: #f7f8fa;
  border-radius: 6px;
}

.fee-value-list {
  margin: 12px 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #1989fa;
}

.fee-value-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #ebedf0;
  
  &:last-child {
    border-bottom: none;
  }
}

.fee-value-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  
  .fee-value-name {
    font-size: 14px;
    font-weight: 500;
    color: #323233;
  }
  
  .fee-value-unit {
    font-size: 12px;
    color: #969799;
    background: #fff;
    padding: 2px 6px;
    border-radius: 4px;
    border: 1px solid #ebedf0;
  }
}

.fee-value-actions {
  display: flex;
  gap: 6px;
}

.form-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #ebedf0;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #323233;
      margin: 0;
    }
    
    .van-icon {
      cursor: pointer;
      color: #969799;
      
      &:hover {
        color: #646566;
      }
    }
  }
  
  &__content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
  }
}

.quick-unit-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  
  .van-tag {
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      opacity: 0.8;
    }
  }
}

.fee-description {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 16px;
  background: #f0f9ff;
  border-radius: 8px;
  margin: 16px 0;
  
  .description-text {
    flex: 1;
    
    p {
      font-size: 13px;
      color: #646566;
      margin: 0 0 8px 0;
      line-height: 1.4;
    }
    
    ul {
      margin: 0;
      padding-left: 16px;
      
      li {
        font-size: 12px;
        color: #969799;
        line-height: 1.4;
        margin-bottom: 4px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  
  .van-button {
    flex: 1;
  }
}

// 响应式设计
@media (max-width: 375px) {
  .fee-key-item {
    &__header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
  }
  
  .fee-key-actions {
    flex-wrap: wrap;
    gap: 6px;
  }
  
  .quick-unit-selector {
    gap: 6px;
  }
}
</style>