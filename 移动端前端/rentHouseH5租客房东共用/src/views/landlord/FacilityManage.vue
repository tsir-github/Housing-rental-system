<template>
  <div class="facility-manage">
    <!-- 导航栏 -->
    <van-nav-bar title="配套管理" left-arrow @click-left="handleGoBack">
      <template #right>
        <van-button 
          type="primary" 
          size="small" 
          icon="plus"
          @click="handleAddFacility"
        >
          添加配套
        </van-button>
      </template>
    </van-nav-bar>

    <!-- 页面内容 -->
    <div class="facility-manage-content">
      <!-- 筛选标签 -->
      <div class="facility-manage-filters">
        <van-tabs v-model:active="activeTab" @change="handleTabChange">
          <van-tab title="全部" name="all" />
          <van-tab title="房间配套" name="1" />
          <van-tab title="公寓配套" name="2" />
        </van-tabs>
      </div>

      <!-- 加载状态 -->
      <van-loading v-if="loading" vertical>加载中...</van-loading>

      <!-- 配套设施列表 -->
      <div v-else class="facility-list-container">
        <van-pull-refresh v-model="refreshing" @refresh="handleRefresh">
          <div v-if="filteredFacilities.length > 0" class="facility-list">
            <div
              v-for="facility in filteredFacilities"
              :key="facility.id"
              class="facility-item"
            >
              <div class="facility-item__icon">
                <van-icon 
                  :name="facility.icon || 'shop-o'" 
                  size="24" 
                  :color="getFacilityColor(facility.type)"
                />
              </div>
              
              <div class="facility-item__info">
                <div class="facility-name-wrapper">
                  <h4 class="facility-name">{{ facility.name }}</h4>
                  <van-tag 
                    v-if="facility.creatorType === 2" 
                    type="primary" 
                    size="small"
                    class="custom-tag"
                  >
                    自定义
                  </van-tag>
                </div>
                <div class="facility-meta">
                  <span class="facility-type">{{ getFacilityTypeText(facility.type) }}</span>
                  <span class="facility-creator">{{ getCreatorTypeText(facility.creatorType) }}</span>
                </div>
              </div>
              
              <div class="facility-item__actions">
                <van-button 
                  v-if="canEditFacility(facility)"
                  type="default" 
                  size="mini"
                  @click="handleEditFacility(facility)"
                >
                  编辑
                </van-button>
                
                <van-button 
                  v-if="canDeleteFacility(facility)"
                  type="danger" 
                  size="mini"
                  @click="handleDeleteFacility(facility)"
                >
                  删除
                </van-button>
                
                <van-tag 
                  v-if="!canEditFacility(facility) && !canDeleteFacility(facility)"
                  type="default" 
                  size="small"
                >
                  系统配套
                </van-tag>
              </div>
            </div>
          </div>
          
          <EmptyState
            v-else
            type="no-data"
            title="暂无配套设施"
            description="点击右上角按钮添加配套设施"
            @action="handleAddFacility"
          />
        </van-pull-refresh>
      </div>
    </div>

    <!-- 配套设施表单弹窗 -->
    <van-popup 
      v-model:show="showFacilityForm" 
      position="bottom" 
      :style="{ height: '70%' }"
      round
    >
      <div class="form-popup">
        <div class="form-popup__header">
          <h3>{{ facilityFormMode === 'add' ? '添加配套设施' : '编辑配套设施' }}</h3>
          <van-icon name="cross" size="18" @click="closeFacilityForm" />
        </div>
        
        <div class="form-popup__content">
          <van-form @submit="handleSubmitFacility">
            <van-field
              v-model="facilityForm.name"
              name="name"
              label="配套名称"
              placeholder="请输入配套设施名称"
              :rules="[{ required: true, message: '请输入配套设施名称' }]"
            />
            
            <van-field name="type" label="配套类型">
              <template #input>
                <van-radio-group v-model="facilityForm.type" direction="horizontal">
                  <van-radio :name="1">房间配套</van-radio>
                  <van-radio :name="2">公寓配套</van-radio>
                </van-radio-group>
              </template>
            </van-field>
            
            <van-field name="icon" label="图标选择">
              <template #input>
                <div class="icon-selector">
                  <div class="selected-icon">
                    <van-icon 
                      :name="facilityForm.icon || 'shop-o'" 
                      size="24" 
                      :color="getFacilityColor(facilityForm.type)"
                    />
                    <span>{{ facilityForm.icon || '默认图标' }}</span>
                  </div>
                  
                  <van-button 
                    type="primary" 
                    size="small"
                    @click="showIconPicker = true"
                  >
                    选择图标
                  </van-button>
                </div>
              </template>
            </van-field>
            
            <div class="form-actions">
              <van-button 
                type="default" 
                size="large" 
                @click="closeFacilityForm"
              >
                取消
              </van-button>
              
              <van-button 
                type="primary" 
                size="large" 
                native-type="submit"
                :loading="submitting"
              >
                {{ facilityFormMode === 'add' ? '添加' : '保存' }}
              </van-button>
            </div>
          </van-form>
        </div>
      </div>
    </van-popup>   
 <!-- 图标选择器弹窗 -->
    <van-popup 
      v-model:show="showIconPicker" 
      position="bottom" 
      :style="{ height: '60%' }"
      round
    >
      <div class="icon-picker">
        <div class="icon-picker__header">
          <h3>选择图标</h3>
          <van-icon name="cross" size="18" @click="showIconPicker = false" />
        </div>
        
        <div class="icon-picker__content">
          <div class="icon-grid">
            <div
              v-for="iconName in iconList"
              :key="iconName"
              class="icon-item"
              :class="{ active: facilityForm.icon === iconName }"
              @click="selectIcon(iconName)"
            >
              <van-icon :name="iconName" size="24" />
              <span class="icon-name">{{ iconName }}</span>
            </div>
          </div>
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
  getFacilities,
  createFacility,
  updateFacility,
  deleteFacility
} from '@/api/landlord/management';
import type { FacilityInfo } from '@/api/landlord/management';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';
import { useUserStore } from '@/store/modules/user';

// 响应式数据
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false);
const refreshing = ref(false);
const submitting = ref(false);
const activeTab = ref('all');
const facilityList = ref<FacilityInfo[]>([]);

// 处理返回按钮点击
const handleGoBack = () => {
  // 返回到房东仪表板
  router.push('/landlord/dashboard')
}

// 获取房东ID
const landlordId = computed(() => {
  return userStore.userInfo?.id || 0
})

// 配套设施表单
const showFacilityForm = ref(false);
const facilityFormMode = ref<'add' | 'edit'>('add');
const facilityForm = ref<FacilityInfo>({
  name: '',
  type: 1,
  icon: 'shop-o'
});
const currentFacility = ref<FacilityInfo | null>(null);

// 图标选择器
const showIconPicker = ref(false);
const iconList = ref([
  'home-o', 'shop-o', 'tv-o', 'phone-o', 'fire-o', 'location-o',
  'wifi', 'desktop-o', 'printer', 'camera-o', 'music-o', 'video-o',
  'car-o', 'bicycle', 'umbrella-circle', 'flower-o', 'gem-o', 'gift-o',
  'medal-o', 'trophy-o', 'diamond-o', 'crown-o', 'star-o', 'like-o',
  'thumb-circle-o', 'good-job-o', 'smile-o', 'user-circle-o', 'friends-o', 'contact'
]);

// 计算属性
const showEmpty = computed(() => 
  !loading.value && filteredFacilities.value.length === 0
);

const filteredFacilities = computed(() => {
  if (activeTab.value === 'all') {
    return facilityList.value;
  }
  
  const type = parseInt(activeTab.value);
  return facilityList.value.filter(facility => facility.type === type);
});

// 权限控制函数
const canEditFacility = (facility: FacilityInfo) => {
  // 只有房东自定义的配套才能编辑
  return facility.creatorType === 2 && facility.creatorId === landlordId.value;
};

const canDeleteFacility = (facility: FacilityInfo) => {
  // 只有房东自定义的配套才能删除
  return facility.creatorType === 2 && facility.creatorId === landlordId.value;
};

// 工具函数
const getFacilityTypeText = (type: number) => {
  switch (type) {
    case 1: return '房间配套';
    case 2: return '公寓配套';
    default: return '未知类型';
  }
};

const getCreatorTypeText = (creatorType?: number) => {
  switch (creatorType) {
    case 0: return '系统默认';
    case 1: return '官方配套';
    case 2: return '房东自定义';
    default: return '未知来源';
  }
};

const getFacilityColor = (type: number) => {
  switch (type) {
    case 1: return '#1989fa'; // 房间配套 - 蓝色
    case 2: return '#07c160'; // 公寓配套 - 绿色
    default: return '#969799';
  }
};

// 加载配套设施列表
const loadFacilities = async () => {
  try {
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    // 不传递landlordId参数，让后端根据登录用户自动获取
    const { data } = await getFacilities();
    facilityList.value = data;
  } catch (error) {
    console.error('加载配套设施列表失败:', error);
    showToast('加载配套设施列表失败');
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    await loadFacilities();
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
    await loadFacilities();
    showToast('刷新成功');
  } catch (error) {
    showToast('刷新失败');
  } finally {
    refreshing.value = false;
  }
};

const handleTabChange = () => {
  // 标签页切换时不需要重新加载数据，只需要重新计算过滤结果
};

// 配套设施操作
const handleAddFacility = () => {
  facilityFormMode.value = 'add';
  facilityForm.value = {
    name: '',
    type: 1,
    icon: 'shop-o'
  };
  currentFacility.value = null;
  showFacilityForm.value = true;
};

const handleEditFacility = (facility: FacilityInfo) => {
  facilityFormMode.value = 'edit';
  facilityForm.value = {
    id: facility.id,
    name: facility.name,
    type: facility.type,
    icon: facility.icon || 'shop-o'
  };
  currentFacility.value = facility;
  showFacilityForm.value = true;
};

const handleDeleteFacility = async (facility: FacilityInfo) => {
  try {
    await showConfirmDialog({
      title: '删除确认',
      message: `确定要删除配套设施"${facility.name}"吗？`,
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
    
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    await deleteFacility(landlordId.value, facility.id!);
    showToast('删除成功');
    
    // 从列表中移除
    const index = facilityList.value.findIndex(item => item.id === facility.id);
    if (index !== -1) {
      facilityList.value.splice(index, 1);
    }
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除配套设施失败:', error);
      showToast('删除失败');
    }
  }
};

const handleSubmitFacility = async () => {
  try {
    submitting.value = true;
    
    if (!landlordId.value) {
      showToast('获取用户信息失败');
      return;
    }
    
    if (facilityFormMode.value === 'add') {
      const { data: newId } = await createFacility(landlordId.value, facilityForm.value);
      
      // 添加到列表
      const newFacility: FacilityInfo = {
        ...facilityForm.value,
        id: newId
      };
      facilityList.value.push(newFacility);
      
      showToast('添加成功');
    } else {
      await updateFacility(landlordId.value, facilityForm.value);
      
      // 更新列表中的数据
      if (currentFacility.value) {
        currentFacility.value.name = facilityForm.value.name;
        currentFacility.value.type = facilityForm.value.type;
        currentFacility.value.icon = facilityForm.value.icon;
      }
      
      showToast('更新成功');
    }
    
    closeFacilityForm();
    
  } catch (error) {
    console.error('提交配套设施失败:', error);
    showToast('操作失败');
  } finally {
    submitting.value = false;
  }
};

const closeFacilityForm = () => {
  showFacilityForm.value = false;
  facilityForm.value = { name: '', type: 1, icon: 'shop-o' };
  currentFacility.value = null;
};

// 图标选择
const selectIcon = (iconName: string) => {
  facilityForm.value.icon = iconName;
  showIconPicker.value = false;
};

// 生命周期
onMounted(() => {
  initData();
});
</script><style scoped lang="less">
.facility-manage {
  min-height: 100vh;
  background: #f5f5f5;
}

.facility-manage-content {
  padding: 16px;
}

.facility-manage-filters {
  margin-bottom: 16px;
  
  :deep(.van-tabs__nav) {
    background: white;
    border-radius: 8px;
    padding: 4px;
  }
  
  :deep(.van-tab) {
    flex: 1;
    text-align: center;
  }
}

.facility-list-container {
  min-height: 200px;
}

.facility-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.facility-item {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 16px;
  
  &__icon {
    margin-right: 12px;
    flex-shrink: 0;
  }
  
  &__info {
    flex: 1;
    margin-right: 12px;
    
    .facility-name-wrapper {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;
      
      .facility-name {
        font-size: 16px;
        font-weight: 600;
        color: #323233;
        margin: 0;
      }
      
      .custom-tag {
        flex-shrink: 0;
      }
    }
    
    .facility-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .facility-type {
        font-size: 12px;
        color: #969799;
        background: #f7f8fa;
        padding: 2px 8px;
        border-radius: 4px;
      }
      
      .facility-creator {
        font-size: 11px;
        color: #646566;
        opacity: 0.8;
      }
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
  .facility-item {
    &__actions {
      flex-direction: column;
      gap: 6px;
    }
  }
  
  .icon-grid {
    grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
    gap: 8px;
  }
}
</style>