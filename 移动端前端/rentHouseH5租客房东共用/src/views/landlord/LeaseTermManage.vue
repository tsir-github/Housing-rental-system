<template>
  <PageContainer :loading="loading" :show-empty="showEmpty" empty-type="no-data">
    <!-- 页面头部 -->
    <template #header>
      <div class="lease-term-manage-header">
        <div class="lease-term-manage-header__title">
          <h2>租期管理</h2>
          <p>管理房源租期选项</p>
        </div>
        
        <div class="lease-term-manage-header__actions">
          <van-button 
            type="primary" 
            size="small" 
            icon="plus"
            @click="handleAddLeaseTerm"
          >
            添加租期
          </van-button>
        </div>
      </div>
    </template>

    <!-- 租期列表 -->
    <div class="lease-term-manage-content">
      <van-pull-refresh v-model="refreshing" @refresh="handleRefresh">
        <div class="lease-term-list">
          <div
            v-for="leaseTerm in leaseTermList"
            :key="leaseTerm.id"
            class="lease-term-item"
          >
            <div class="lease-term-item__info">
              <div class="lease-term-main">
                <h4 class="lease-term-name">{{ leaseTerm.name }}</h4>
                <div class="lease-term-details">
                  <span class="lease-term-months">{{ leaseTerm.monthCount }}个月</span>
                  <span class="lease-term-unit">{{ leaseTerm.unit }}</span>
                </div>
              </div>
              
              <div class="lease-term-description">
                <van-icon name="info-o" size="12" color="#969799" />
                <span>{{ leaseTerm.description || '暂无描述' }}</span>
              </div>
            </div>
            
            <div class="lease-term-item__actions">
              <van-button 
                v-if="canEditLeaseTerm(leaseTerm)"
                type="default" 
                size="mini"
                @click="handleEditLeaseTerm(leaseTerm)"
              >
                编辑
              </van-button>
              
              <van-button 
                v-if="canDeleteLeaseTerm(leaseTerm)"
                type="danger" 
                size="mini"
                @click="handleDeleteLeaseTerm(leaseTerm)"
              >
                删除
              </van-button>
            </div>
          </div>
        </div>
        
        <EmptyState
          v-if="showEmpty"
          type="no-data"
          title="暂无租期选项"
          description="点击右上角按钮添加租期选项"
          @action="handleAddLeaseTerm"
        />
      </van-pull-refresh>
    </div>

    <!-- 租期表单弹窗 -->
    <van-popup 
      v-model:show="showLeaseTermForm" 
      position="bottom" 
      :style="{ height: '70%' }"
      round
    >
      <div class="form-popup">
        <div class="form-popup__header">
          <h3>{{ leaseTermFormMode === 'add' ? '添加租期' : '编辑租期' }}</h3>
          <van-icon name="cross" size="18" @click="closeLeaseTermForm" />
        </div>
        
        <div class="form-popup__content">
          <van-form @submit="handleSubmitLeaseTerm">
            <van-field
              v-model="leaseTermForm.name"
              name="name"
              label="租期名称"
              placeholder="请输入租期名称"
              :rules="[{ required: true, message: '请输入租期名称' }]"
            />
            
            <van-field
              v-model.number="leaseTermForm.monthCount"
              name="monthCount"
              label="租期月数"
              placeholder="请输入租期月数"
              type="number"
              :rules="[
                { required: true, message: '请输入租期月数' },
                { pattern: /^[1-9]\d*$/, message: '请输入正整数' }
              ]"
            />
            
            <van-field
              v-model="leaseTermForm.unit"
              name="unit"
              label="租期单位"
              placeholder="请输入租期单位"
              :rules="[{ required: true, message: '请输入租期单位' }]"
            />
            
            <!-- 常用租期快速选择 -->
            <van-field name="quickTerm" label="常用租期">
              <template #input>
                <div class="quick-term-selector">
                  <van-tag
                    v-for="term in commonTerms"
                    :key="term.name"
                    :type="isSelectedTerm(term) ? 'primary' : 'default'"
                    size="small"
                    @click="selectTerm(term)"
                  >
                    {{ term.name }}
                  </van-tag>
                </div>
              </template>
            </van-field>
            
            <van-field
              v-model="leaseTermForm.description"
              name="description"
              label="租期描述"
              placeholder="请输入租期描述（可选）"
              type="textarea"
              rows="3"
            />
            
            <!-- 租期说明 -->
            <div class="lease-term-description-info">
              <van-icon name="info-o" size="14" color="#1989fa" />
              <div class="description-text">
                <p>租期选项用于房源发布时设置租赁期限，常见的租期包括：</p>
                <ul>
                  <li>短期租赁：1-3个月，适合临时住宿</li>
                  <li>中期租赁：6-12个月，适合一般租客</li>
                  <li>长期租赁：12个月以上，适合稳定租客</li>
                </ul>
              </div>
            </div>
            
            <div class="form-actions">
              <van-button 
                type="default" 
                size="large" 
                @click="closeLeaseTermForm"
              >
                取消
              </van-button>
              
              <van-button 
                type="primary" 
                size="large" 
                native-type="submit"
                :loading="submitting"
              >
                {{ leaseTermFormMode === 'add' ? '添加' : '保存' }}
              </van-button>
            </div>
          </van-form>
        </div>
      </div>
    </van-popup>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { showToast, showConfirmDialog } from 'vant';
import {
  getLeaseTerms,
  createLeaseTerm,
  updateLeaseTerm,
  deleteLeaseTerm
} from '@/api/landlord/management';
import type { LeaseTerm } from '@/api/landlord/management';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';
import { useUserStore } from '@/store/modules/user';

// 响应式数据
const userStore = useUserStore()
const loading = ref(false);
const refreshing = ref(false);
const submitting = ref(false);
const leaseTermList = ref<LeaseTerm[]>([]);

// 获取房东ID
const landlordId = computed(() => {
  return userStore.userInfo?.id || 0
})

// 租期表单
const showLeaseTermForm = ref(false);
const leaseTermFormMode = ref<'add' | 'edit'>('add');
const leaseTermForm = ref<LeaseTerm>({
  name: '',
  monthCount: 1,
  unit: '个月',
  description: ''
});
const currentLeaseTerm = ref<LeaseTerm | null>(null);

// 常用租期
const commonTerms = ref([
  { name: '1个月', monthCount: 1, unit: '个月' },
  { name: '3个月', monthCount: 3, unit: '个月' },
  { name: '6个月', monthCount: 6, unit: '个月' },
  { name: '12个月', monthCount: 12, unit: '个月' },
  { name: '18个月', monthCount: 18, unit: '个月' },
  { name: '24个月', monthCount: 24, unit: '个月' }
]);

// 计算属性
const showEmpty = computed(() => 
  !loading.value && leaseTermList.value.length === 0
);

// 工具函数
const isSelectedTerm = (term: any) => {
  return leaseTermForm.value.name === term.name && 
         leaseTermForm.value.monthCount === term.monthCount &&
         leaseTermForm.value.unit === term.unit;
};

// 权限控制函数
const canEditLeaseTerm = (leaseTerm: LeaseTerm) => {
  // 只有房东自定义的租期才能编辑
  console.log('🔍 租期权限检查 - canEditLeaseTerm:', {
    leaseTermName: leaseTerm.name,
    creatorType: leaseTerm.creatorType || leaseTerm.creator_type,
    creatorId: leaseTerm.creatorId || leaseTerm.creator_id,
    currentLandlordId: landlordId.value,
    typeMatch: (leaseTerm.creatorType || leaseTerm.creator_type) === 2,
    idMatch: (leaseTerm.creatorId || leaseTerm.creator_id) === Number(landlordId.value)
  });
  return (leaseTerm.creatorType || leaseTerm.creator_type) === 2 && 
         (leaseTerm.creatorId || leaseTerm.creator_id) === Number(landlordId.value);
};

const canDeleteLeaseTerm = (leaseTerm: LeaseTerm) => {
  // 只有房东自定义的租期才能删除
  return (leaseTerm.creatorType || leaseTerm.creator_type) === 2 && 
         (leaseTerm.creatorId || leaseTerm.creator_id) === Number(landlordId.value);
};

// 加载租期列表
const loadLeaseTerms = async () => {
  try {
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    const { data } = await getLeaseTerms(landlordId.value);
    leaseTermList.value = data;
  } catch (error) {
    console.error('加载租期列表失败:', error);
    showToast('加载租期列表失败');
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    await loadLeaseTerms();
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
    await loadLeaseTerms();
    showToast('刷新成功');
  } catch (error) {
    showToast('刷新失败');
  } finally {
    refreshing.value = false;
  }
};

// 租期操作
const handleAddLeaseTerm = () => {
  leaseTermFormMode.value = 'add';
  leaseTermForm.value = {
    name: '',
    monthCount: 1,
    unit: '个月',
    description: ''
  };
  currentLeaseTerm.value = null;
  showLeaseTermForm.value = true;
};

const handleEditLeaseTerm = (leaseTerm: LeaseTerm) => {
  leaseTermFormMode.value = 'edit';
  leaseTermForm.value = {
    id: leaseTerm.id,
    name: leaseTerm.name,
    monthCount: leaseTerm.monthCount,
    unit: leaseTerm.unit,
    description: leaseTerm.description
  };
  currentLeaseTerm.value = leaseTerm;
  showLeaseTermForm.value = true;
};

const handleDeleteLeaseTerm = async (leaseTerm: LeaseTerm) => {
  try {
    await showConfirmDialog({
      title: '删除确认',
      message: `确定要删除租期"${leaseTerm.name}"吗？删除后相关的房源租期设置可能受到影响。`,
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
    
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    await deleteLeaseTerm(landlordId.value, leaseTerm.id!);
    showToast('删除成功');
    
    // 从列表中移除
    const index = leaseTermList.value.findIndex(item => item.id === leaseTerm.id);
    if (index !== -1) {
      leaseTermList.value.splice(index, 1);
    }
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除租期失败:', error);
      showToast('删除失败');
    }
  }
};

const handleSubmitLeaseTerm = async () => {
  try {
    submitting.value = true;
    
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    if (leaseTermFormMode.value === 'add') {
      const { data: newId } = await createLeaseTerm(landlordId.value, leaseTermForm.value);
      
      // 添加到列表
      const newLeaseTerm: LeaseTerm = {
        ...leaseTermForm.value,
        id: newId
      };
      leaseTermList.value.push(newLeaseTerm);
      
      showToast('添加成功');
    } else {
      await updateLeaseTerm(landlordId.value, leaseTermForm.value);
      
      // 更新列表中的数据
      if (currentLeaseTerm.value) {
        currentLeaseTerm.value.name = leaseTermForm.value.name;
        currentLeaseTerm.value.monthCount = leaseTermForm.value.monthCount;
        currentLeaseTerm.value.unit = leaseTermForm.value.unit;
        currentLeaseTerm.value.description = leaseTermForm.value.description;
      }
      
      showToast('更新成功');
    }
    
    closeLeaseTermForm();
    
  } catch (error) {
    console.error('提交租期失败:', error);
    showToast('操作失败');
  } finally {
    submitting.value = false;
  }
};

const closeLeaseTermForm = () => {
  showLeaseTermForm.value = false;
  leaseTermForm.value = { name: '', monthCount: 1, unit: '个月', description: '' };
  currentLeaseTerm.value = null;
};

// 快速选择租期
const selectTerm = (term: any) => {
  leaseTermForm.value.name = term.name;
  leaseTermForm.value.monthCount = term.monthCount;
  leaseTermForm.value.unit = term.unit;
};

// 生命周期
onMounted(() => {
  initData();
});
</script>

<style scoped lang="less">
.lease-term-manage-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  
  &__title {
    flex: 1;
    
    h2 {
      font-size: 20px;
      font-weight: 600;
      color: #323233;
      margin: 0 0 4px 0;
    }
    
    p {
      font-size: 14px;
      color: #969799;
      margin: 0;
    }
  }
}

.lease-term-manage-content {
  min-height: 200px;
}

.lease-term-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lease-term-item {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 16px;
  
  &__info {
    flex: 1;
    margin-right: 12px;
    
    .lease-term-main {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      
      .lease-term-name {
        font-size: 16px;
        font-weight: 600;
        color: #323233;
        margin: 0;
      }
      
      .lease-term-details {
        display: flex;
        gap: 8px;
        
        .lease-term-months {
          font-size: 12px;
          color: #1989fa;
          background: #e6f7ff;
          padding: 2px 8px;
          border-radius: 4px;
        }
        
        .lease-term-unit {
          font-size: 12px;
          color: #969799;
          background: #f7f8fa;
          padding: 2px 8px;
          border-radius: 4px;
        }
      }
    }
    
    .lease-term-description {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #646566;
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

.quick-term-selector {
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

.lease-term-description-info {
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
  .lease-term-manage-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .lease-term-item {
    &__info {
      .lease-term-main {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }
    }
    
    &__actions {
      flex-direction: column;
      gap: 6px;
    }
  }
  
  .quick-term-selector {
    gap: 6px;
  }
}
</style>