<template>
  <div class="label-manage">
    <!-- 导航栏 -->
    <van-nav-bar title="标签管理" left-arrow @click-left="handleGoBack">
      <template #right>
        <van-button 
          type="primary" 
          size="small" 
          icon="plus"
          @click="handleAddLabel"
        >
          添加标签
        </van-button>
      </template>
    </van-nav-bar>

    <!-- 页面内容 -->
    <div class="label-manage-content">
      <!-- 加载状态 -->
      <van-loading v-if="loading" vertical>加载中...</van-loading>

      <!-- 标签管理内容 -->
      <div v-else class="label-list-container">
        <!-- 筛选标签 -->
        <div class="label-manage-filters">
          <van-tabs v-model:active="activeTab" @change="handleTabChange">
            <van-tab title="全部" name="all" />
            <van-tab title="房间标签" name="1" />
            <van-tab title="公寓标签" name="2" />
          </van-tabs>
        </div>

        <van-pull-refresh v-model="refreshing" @refresh="handleRefresh">
          <div v-if="filteredLabels.length > 0" class="label-list">
            <div
              v-for="label in filteredLabels"
              :key="label.id"
              class="label-item"
            >
              <div class="label-item__content">
                <div class="label-item__info">
                  <h4 class="label-name">{{ label.name }}</h4>
                  <span class="label-type">{{ getLabelTypeText(label.type) }}</span>
                </div>
                
                <div class="label-item__preview">
                  <van-tag 
                    :type="getLabelTagType(label.type)" 
                    size="medium"
                  >
                    {{ label.name }}
                  </van-tag>
                </div>
              </div>
              
              <div class="label-item__actions">
                <van-button 
                  type="default" 
                  size="mini"
                  @click="handleEditLabel(label)"
                >
                  编辑
                </van-button>
                
                <van-button 
                  type="danger" 
                  size="mini"
                  @click="handleDeleteLabel(label)"
                >
                  删除
                </van-button>
              </div>
            </div>
          </div>
          
          <EmptyState
            v-else
            type="no-data"
            title="暂无标签"
            description="点击右上角按钮添加标签"
            @action="handleAddLabel"
          />
        </van-pull-refresh>
      </div>
    </div>

    <!-- 标签表单弹窗 -->
    <van-popup 
      v-model:show="showLabelForm" 
      position="bottom" 
      :style="{ height: '60%' }"
      round
    >
      <div class="form-popup">
        <div class="form-popup__header">
          <h3>{{ labelFormMode === 'add' ? '添加标签' : '编辑标签' }}</h3>
          <van-icon name="cross" size="18" @click="closeLabelForm" />
        </div>
        
        <div class="form-popup__content">
          <van-form @submit="handleSubmitLabel">
            <van-field
              v-model="labelForm.name"
              name="name"
              label="标签名称"
              placeholder="请输入标签名称"
              :rules="[{ required: true, message: '请输入标签名称' }]"
            />
            
            <van-field name="type" label="标签类型">
              <template #input>
                <van-radio-group v-model="labelForm.type" direction="horizontal">
                  <van-radio :name="1">房间标签</van-radio>
                  <van-radio :name="2">公寓标签</van-radio>
                </van-radio-group>
              </template>
            </van-field>
            
            <!-- 标签预览 -->
            <van-field name="preview" label="标签预览">
              <template #input>
                <div class="label-preview">
                  <van-tag 
                    :type="getLabelTagType(labelForm.type)" 
                    size="medium"
                  >
                    {{ labelForm.name || '标签预览' }}
                  </van-tag>
                </div>
              </template>
            </van-field>
            
            <!-- 标签说明 -->
            <div class="label-description">
              <van-icon name="info-o" size="14" color="#1989fa" />
              <div class="description-text">
                <p>标签用于为房源添加特色描述，帮助租客快速了解房源特点：</p>
                <ul>
                  <li>房间标签：如"朝南"、"独立卫浴"、"阳台"等</li>
                  <li>公寓标签：如"地铁沿线"、"商圈核心"、"学区房"等</li>
                </ul>
              </div>
            </div>
            
            <div class="form-actions">
              <van-button 
                type="default" 
                size="large" 
                @click="closeLabelForm"
              >
                取消
              </van-button>
              
              <van-button 
                type="primary" 
                size="large" 
                native-type="submit"
                :loading="submitting"
              >
                {{ labelFormMode === 'add' ? '添加' : '保存' }}
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
import { useUserStore } from '@/store/modules/user';
import {
  getLabels,
  createLabel,
  updateLabel,
  deleteLabel
} from '@/api/landlord/management';
import type { LabelInfo } from '@/api/landlord/management';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';

const userStore = useUserStore();

// 响应式数据
const router = useRouter()
const loading = ref(false);
const refreshing = ref(false);
const submitting = ref(false);
const activeTab = ref('all');
const labelList = ref<LabelInfo[]>([]);

// 处理返回按钮点击
const handleGoBack = () => {
  // 返回到房东仪表板
  router.push('/landlord/dashboard')
}

// 标签表单
const showLabelForm = ref(false);
const labelFormMode = ref<'add' | 'edit'>('add');
const labelForm = ref<LabelInfo>({
  name: '',
  type: 1
});
const currentLabel = ref<LabelInfo | null>(null);

// 计算属性
const showEmpty = computed(() => 
  !loading.value && filteredLabels.value.length === 0
);

const filteredLabels = computed(() => {
  if (activeTab.value === 'all') {
    return labelList.value;
  }
  
  const type = parseInt(activeTab.value);
  return labelList.value.filter(label => label.type === type);
});

// 工具函数
const getLabelTypeText = (type: number) => {
  switch (type) {
    case 1: return '房间标签';
    case 2: return '公寓标签';
    default: return '未知类型';
  }
};

const getLabelTagType = (type: number) => {
  switch (type) {
    case 1: return 'primary'; // 房间标签 - 蓝色
    case 2: return 'success'; // 公寓标签 - 绿色
    default: return 'default';
  }
};

// 加载标签列表
const loadLabels = async () => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    const { data } = await getLabels(undefined, landlordId);
    labelList.value = data;
  } catch (error) {
    console.error('加载标签列表失败:', error);
    showToast('加载标签列表失败');
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    await loadLabels();
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
    await loadLabels();
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

// 标签操作
const handleAddLabel = () => {
  labelFormMode.value = 'add';
  labelForm.value = {
    name: '',
    type: 1
  };
  currentLabel.value = null;
  showLabelForm.value = true;
};

const handleEditLabel = (label: LabelInfo) => {
  labelFormMode.value = 'edit';
  labelForm.value = {
    id: label.id,
    name: label.name,
    type: label.type
  };
  currentLabel.value = label;
  showLabelForm.value = true;
};

const handleDeleteLabel = async (label: LabelInfo) => {
  try {
    await showConfirmDialog({
      title: '删除确认',
      message: `确定要删除标签"${label.name}"吗？`,
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await deleteLabel(label.id!, landlordId);
    showToast('删除成功');
    
    // 从列表中移除
    const index = labelList.value.findIndex(item => item.id === label.id);
    if (index !== -1) {
      labelList.value.splice(index, 1);
    }
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除标签失败:', error);
      showToast('删除失败');
    }
  }
};

const handleSubmitLabel = async () => {
  try {
    submitting.value = true;
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    if (labelFormMode.value === 'add') {
      const { data: newId } = await createLabel(labelForm.value, landlordId);
      
      // 添加到列表
      const newLabel: LabelInfo = {
        ...labelForm.value,
        id: newId
      };
      labelList.value.push(newLabel);
      
      showToast('添加成功');
    } else {
      await updateLabel(labelForm.value, landlordId);
      
      // 更新列表中的数据
      if (currentLabel.value) {
        currentLabel.value.name = labelForm.value.name;
        currentLabel.value.type = labelForm.value.type;
      }
      
      showToast('更新成功');
    }
    
    closeLabelForm();
    
  } catch (error) {
    console.error('提交标签失败:', error);
    showToast('操作失败');
  } finally {
    submitting.value = false;
  }
};

const closeLabelForm = () => {
  showLabelForm.value = false;
  labelForm.value = { name: '', type: 1 };
  currentLabel.value = null;
};

// 生命周期
onMounted(() => {
  initData();
});
</script>

<style scoped lang="less">
.label-manage {
  min-height: 100vh;
  background: #f5f5f5;
}

.label-manage-content {
  padding: 16px;
}

.label-list-container {
  min-height: 200px;
}

.label-manage-filters {
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

.label-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.label-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  
  &__content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }
  
  &__info {
    flex: 1;
    
    .label-name {
      font-size: 16px;
      font-weight: 600;
      color: #323233;
      margin: 0 0 4px 0;
    }
    
    .label-type {
      font-size: 12px;
      color: #969799;
      background: #f7f8fa;
      padding: 2px 8px;
      border-radius: 4px;
    }
  }
  
  &__preview {
    margin-left: 12px;
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

.label-preview {
  display: flex;
  align-items: center;
}

.label-description {
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
  .label-item {
    &__content {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
    
    &__preview {
      margin-left: 0;
    }
    
    &__actions {
      flex-wrap: wrap;
      gap: 6px;
    }
  }
}
</style>