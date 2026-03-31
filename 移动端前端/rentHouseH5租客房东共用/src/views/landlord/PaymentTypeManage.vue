<template>
  <div class="payment-manage">
    <!-- 导航栏 -->
    <van-nav-bar title="支付方式管理" left-arrow @click-left="handleGoBack">
      <template #right>
        <van-button 
          type="primary" 
          size="small" 
          icon="plus"
          @click="handleAddPaymentType"
        >
          添加支付方式
        </van-button>
      </template>
    </van-nav-bar>

    <!-- 页面内容 -->
    <div class="payment-manage-content">
      <!-- 加载状态 -->
      <van-loading v-if="loading" vertical>加载中...</van-loading>

      <!-- 支付方式管理内容 -->
      <div v-else class="payment-list-container">
        <van-pull-refresh v-model="refreshing" @refresh="handleRefresh">
          <div v-if="paymentTypeList.length > 0" class="payment-type-list">
            <div
              v-for="paymentType in paymentTypeList"
              :key="paymentType.id"
              class="payment-type-item"
            >
              <div class="payment-type-item__content">
                <div class="payment-type-item__icon">
                  <van-icon 
                    :name="paymentType.icon || 'gold-coin-o'" 
                    size="24" 
                    color="#1989fa"
                  />
                </div>
                
                <div class="payment-type-item__info">
                  <div class="payment-type-name-wrapper">
                    <h4 class="payment-type-name">{{ paymentType.name }}</h4>
                    <van-tag 
                      v-if="canEditPaymentType(paymentType)" 
                      type="primary" 
                      size="small"
                      class="custom-tag"
                    >
                      自定义
                    </van-tag>
                    <van-tag 
                      v-else 
                      type="default" 
                      size="small"
                      class="official-tag"
                    >
                      官方
                    </van-tag>
                  </div>
                  <div class="payment-type-meta">
                    <div class="payment-type-details">
                      <span class="payment-month-count">付款月数：{{ paymentType.payMonthCount || paymentType.pay_month_count || '未设置' }}个月</span>
                      <span v-if="paymentType.additionalInfo || paymentType.additional_info" class="payment-description">{{ paymentType.additionalInfo || paymentType.additional_info }}</span>
                    </div>
                    <span class="payment-type-creator">{{ getCreatorTypeText(paymentType.creatorType || paymentType.creator_type) }}</span>
                  </div>
                </div>
              </div>
              
              <div class="payment-type-item__actions">
                <van-button 
                  v-if="canEditPaymentType(paymentType)"
                  type="default" 
                  size="mini"
                  @click="handleEditPaymentType(paymentType)"
                >
                  编辑
                </van-button>
                
                <van-button 
                  v-if="canDeletePaymentType(paymentType)"
                  type="danger" 
                  size="mini"
                  @click="handleDeletePaymentType(paymentType)"
                >
                  删除
                </van-button>
                
                <van-tag 
                  v-if="!canEditPaymentType(paymentType) && !canDeletePaymentType(paymentType)"
                  type="default" 
                  size="small"
                >
                  只读
                </van-tag>
              </div>
            </div>
          </div>
          
          <EmptyState
            v-else
            type="no-data"
            title="暂无支付方式"
            description="点击右上角按钮添加支付方式"
            @action="handleAddPaymentType"
          />
        </van-pull-refresh>
      </div>
    </div>

    <!-- 支付方式表单弹窗 -->
    <van-popup 
      v-model:show="showPaymentTypeForm" 
      position="bottom" 
      :style="{ height: '70%' }"
      round
    >
      <div class="form-popup">
        <div class="form-popup__header">
          <h3>{{ paymentTypeFormMode === 'add' ? '添加支付方式' : '编辑支付方式' }}</h3>
          <van-icon name="cross" size="18" @click="closePaymentTypeForm" />
        </div>
        
        <div class="form-popup__content">
          <van-form @submit="handleSubmitPaymentType">
            <van-field
              v-model="paymentTypeForm.name"
              name="name"
              label="支付方式名称"
              placeholder="请输入支付方式名称"
              :rules="[{ required: true, message: '请输入支付方式名称' }]"
            />
            
            <van-field
              v-model="payMonthCountStr"
              name="payMonthCount"
              label="付款月数"
              placeholder="请输入付款月数"
              type="number"
              :rules="[{ required: true, message: '请输入付款月数' }]"
              @input="handlePayMonthCountInput"
            />
            
            <van-field
              v-model="paymentTypeForm.additionalInfo"
              name="additionalInfo"
              label="支付方式描述"
              placeholder="请输入支付方式描述"
              type="textarea"
              rows="3"
              maxlength="200"
              show-word-limit
            />
            
            <!-- 常用付款月数快速选择 -->
            <van-field name="quickMonthCount" label="常用付款方式">
              <template #input>
                <div class="quick-month-selector">
                  <van-tag
                    v-for="month in commonMonthCounts"
                    :key="month.value"
                    :type="paymentTypeForm.payMonthCount === month.value ? 'primary' : 'default'"
                    size="small"
                    @click="selectMonthCount(month.value)"
                  >
                    {{ month.label }}
                  </van-tag>
                </div>
              </template>
            </van-field>
            
            <!-- 支付方式说明 -->
            <div class="payment-description">
              <van-icon name="info-o" size="14" color="#1989fa" />
              <div class="description-text">
                <p>支付方式用于房源发布时设置租金付款规则，常见的支付方式包括：</p>
                <ul>
                  <li>押一付一：押金1个月，每次付1个月租金</li>
                  <li>押一付三：押金1个月，每次付3个月租金</li>
                  <li>押二付一：押金2个月，每次付1个月租金</li>
                  <li>年付：一次性支付全年租金</li>
                </ul>
              </div>
            </div>
            
            <div class="form-actions">
              <van-button 
                type="default" 
                size="large" 
                @click="closePaymentTypeForm"
              >
                取消
              </van-button>
              
              <van-button 
                type="primary" 
                size="large" 
                native-type="submit"
                :loading="submitting"
              >
                {{ paymentTypeFormMode === 'add' ? '添加' : '保存' }}
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
  getPaymentTypes,
  createPaymentType,
  updatePaymentType,
  deletePaymentType
} from '@/api/landlord/management';
import type { PaymentType } from '@/api/landlord/management';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';
import { useUserStore } from '@/store/modules/user';

// 响应式数据
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false);
const refreshing = ref(false);
const submitting = ref(false);
const paymentTypeList = ref<PaymentType[]>([]);

// 处理返回按钮点击
const handleGoBack = () => {
  // 返回到房东仪表板
  router.push('/landlord/dashboard')
}

// 获取房东ID
const landlordId = computed(() => {
  return userStore.userInfo?.id || 0
})

// 支付方式表单
const showPaymentTypeForm = ref(false);
const paymentTypeFormMode = ref<'add' | 'edit'>('add');
const paymentTypeForm = ref<PaymentType>({
  name: '',
  payMonthCount: 1,
  additionalInfo: ''
});
const currentPaymentType = ref<PaymentType | null>(null);

// 常用付款月数
const commonMonthCounts = ref([
  { label: '押一付一', value: 1 },
  { label: '押一付三', value: 3 },
  { label: '押一付六', value: 6 },
  { label: '押一付十二', value: 12 },
  { label: '半年付', value: 6 },
  { label: '年付', value: 12 }
]);

// 图标选择器（暂时保留但不使用）
const showIconPicker = ref(false);

// 付款月数的字符串表示（用于表单绑定）
const payMonthCountStr = computed({
  get: () => paymentTypeForm.value.payMonthCount?.toString() || '1',
  set: (value: string) => {
    paymentTypeForm.value.payMonthCount = parseInt(value) || 1;
  }
});

// 处理付款月数输入
const handlePayMonthCountInput = (value: string) => {
  const numValue = parseInt(value) || 1;
  paymentTypeForm.value.payMonthCount = numValue;
};

// 加载支付方式列表
const loadPaymentTypes = async () => {
  try {
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    const { data } = await getPaymentTypes(landlordId.value);
    paymentTypeList.value = data;
  } catch (error) {
    console.error('加载支付方式列表失败:', error);
    showToast('加载支付方式列表失败');
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    await loadPaymentTypes();
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
    await loadPaymentTypes();
    showToast('刷新成功');
  } catch (error) {
    showToast('刷新失败');
  } finally {
    refreshing.value = false;
  }
};

// 支付方式操作
const handleAddPaymentType = () => {
  paymentTypeFormMode.value = 'add';
  paymentTypeForm.value = {
    name: '',
    payMonthCount: 1,
    additionalInfo: ''
  };
  currentPaymentType.value = null;
  showPaymentTypeForm.value = true;
};

const handleEditPaymentType = (paymentType: PaymentType) => {
  paymentTypeFormMode.value = 'edit';
  paymentTypeForm.value = {
    id: paymentType.id,
    name: paymentType.name,
    payMonthCount: paymentType.payMonthCount || 1,
    additionalInfo: paymentType.additionalInfo || ''
  };
  currentPaymentType.value = paymentType;
  showPaymentTypeForm.value = true;
};

const handleDeletePaymentType = async (paymentType: PaymentType) => {
  try {
    await showConfirmDialog({
      title: '删除确认',
      message: `确定要删除支付方式"${paymentType.name}"吗？删除后相关的房源支付设置可能受到影响。`,
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
    
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    await deletePaymentType(landlordId.value, paymentType.id!);
    showToast('删除成功');
    
    // 从列表中移除
    const index = paymentTypeList.value.findIndex(item => item.id === paymentType.id);
    if (index !== -1) {
      paymentTypeList.value.splice(index, 1);
    }
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除支付方式失败:', error);
      showToast('删除失败');
    }
  }
};

const handleSubmitPaymentType = async () => {
  try {
    submitting.value = true;
    
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    if (paymentTypeFormMode.value === 'add') {
      await createPaymentType(landlordId.value, paymentTypeForm.value);
      showToast('添加成功');
    } else {
      await updatePaymentType(landlordId.value, paymentTypeForm.value);
      showToast('更新成功');
    }
    
    closePaymentTypeForm();
    await loadPaymentTypes(); // 重新加载数据
    
  } catch (error) {
    console.error('提交支付方式失败:', error);
    showToast('操作失败');
  } finally {
    submitting.value = false;
  }
};

const closePaymentTypeForm = () => {
  showPaymentTypeForm.value = false;
  paymentTypeForm.value = { 
    name: '', 
    payMonthCount: 1, 
    additionalInfo: '' 
  };
  currentPaymentType.value = null;
};

// 快速选择付款月数
const selectMonthCount = (monthCount: number) => {
  paymentTypeForm.value.payMonthCount = monthCount;
};

// 权限控制函数
const canEditPaymentType = (paymentType: PaymentType) => {
  // 只有房东自定义的支付方式才能编辑
  console.log('🔍 支付方式权限检查 - canEditPaymentType:', {
    paymentTypeName: paymentType.name,
    creatorType: paymentType.creatorType || paymentType.creator_type,
    creatorId: paymentType.creatorId || paymentType.creator_id,
    currentLandlordId: landlordId.value,
    typeMatch: (paymentType.creatorType || paymentType.creator_type) === 2,
    idMatch: (paymentType.creatorId || paymentType.creator_id) === Number(landlordId.value)
  });
  return (paymentType.creatorType || paymentType.creator_type) === 2 && 
         (paymentType.creatorId || paymentType.creator_id) === Number(landlordId.value);
};

const canDeletePaymentType = (paymentType: PaymentType) => {
  // 只有房东自定义的支付方式才能删除
  return (paymentType.creatorType || paymentType.creator_type) === 2 && 
         (paymentType.creatorId || paymentType.creator_id) === Number(landlordId.value);
};

// 获取创建者类型文本
const getCreatorTypeText = (creatorType: number) => {
  switch (creatorType) {
    case 0:
      return '系统';
    case 1:
      return '官方';
    case 2:
      return '房东';
    default:
      return '未知';
  }
};

// 图标选择
const selectIcon = (iconName: string) => {
  paymentTypeForm.value.icon = iconName;
  showIconPicker.value = false;
};

// 生命周期
onMounted(() => {
  initData();
});
</script>

<style scoped lang="less">
.payment-manage {
  min-height: 100vh;
  background: #f5f5f5;
}

.payment-manage-content {
  padding: 16px;
}

.payment-list-container {
  min-height: 200px;
}

.payment-type-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-type-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  
  &__content {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
  }
  
  &__icon {
    margin-right: 12px;
    flex-shrink: 0;
  }
  
  &__info {
    flex: 1;
    
    .payment-type-name-wrapper {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;
    }
    
    .payment-type-name {
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
    
    .payment-type-meta {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .payment-type-details {
        display: flex;
        flex-direction: column;
        gap: 4px;
        
        .payment-month-count {
          font-size: 13px;
          color: #1989fa;
          font-weight: 500;
        }
        
        .payment-description {
          font-size: 12px;
          color: #969799;
          line-height: 1.4;
          margin: 0;
          padding: 0;
          background: none;
          border-radius: 0;
        }
      }
      
      .payment-type-creator {
        font-size: 11px;
        color: #c8c9cc;
        align-self: flex-end;
      }
    }
    
    .payment-type-description {
      font-size: 13px;
      color: #969799;
      margin: 0;
      line-height: 1.4;
    }
  }
  
  &__actions {
    display: flex;
    gap: 8px;
  }
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

.icon-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .selected-icon {
    display: flex;
    align-items: center;
    gap: 8px;
    
    span {
      font-size: 14px;
      color: #646566;
    }
  }
}

.payment-description {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 13px;
  color: #646566;
  line-height: 1.4;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 6px;
  margin: 12px 0;
  
  .description-text {
    flex: 1;
    
    p {
      margin: 0 0 8px 0;
      font-weight: 500;
    }
    
    ul {
      margin: 0;
      padding-left: 16px;
      
      li {
        margin: 4px 0;
        line-height: 1.4;
      }
    }
  }
}

.quick-month-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  
  .van-tag {
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      transform: translateY(-1px);
    }
  }
}

.icon-picker {
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

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 12px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background: #f7f8fa;
  }
  
  &.active {
    background: #e6f7ff;
    border: 1px solid #1989fa;
  }
  
  .icon-name {
    font-size: 10px;
    color: #646566;
    text-align: center;
    word-break: break-all;
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
  .payment-type-item {
    &__content {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
    
    &__actions {
      flex-wrap: wrap;
      gap: 6px;
    }
  }
  
  .icon-grid {
    grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
    gap: 8px;
  }
}
</style>