<template>
  <div class="apartment-list">
    <!-- 导航栏 -->
    <van-nav-bar 
      title="公寓管理" 
      left-arrow 
      @click-left="handleGoBack"
      fixed
      placeholder
    />

    <!-- 页面头部 -->
    <div class="apartment-list-header">
      <div class="apartment-list-header__stats">
        <StatCard
          :value="statistics?.totalApartments || 0"
          label="总公寓数"
          icon="home-o"
          icon-color="#1989fa"
          unit="个"
          size="small"
        />
        
        <StatCard
          :value="statistics?.publishedApartments || 0"
          label="已发布"
          icon="eye-o"
          icon-color="#07c160"
          unit="个"
          size="small"
        />
        
        <StatCard
          :value="statistics?.occupancyRate || 0"
          label="出租率"
          icon="chart-trending-o"
          icon-color="#ff976a"
          unit="%"
          :formatter="formatPercentage"
          size="small"
        />
      </div>
      
      <div class="apartment-list-header__actions">
        <van-button 
          type="primary" 
          size="small" 
          icon="plus"
          @click="handleCreateApartment"
        >
          新建公寓
        </van-button>
      </div>
    </div>

    <!-- 搜索和筛选 - 移到外面，确保始终显示 -->
    <div class="apartment-list-filters">
      <van-search
        v-model="searchKeyword"
        placeholder="搜索公寓名称"
        @search="handleSearch"
        @clear="handleClearSearch"
      />
      
    <!-- 筛选标签 - 使用与审核管理页面相同的结构 -->
    <div class="filter-tabs">
      <van-tabs v-model:active="activeTab" @change="onTabChange">
        <van-tab title="全部" name="all" />
        <van-tab title="已发布" name="published" />
        <van-tab title="未发布" name="unpublished" />
      </van-tabs>
    </div>
    </div>

    <!-- 公寓列表内容 -->
    <div class="apartment-list-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <van-loading vertical>加载中...</van-loading>
      </div>
      
      <!-- 列表内容 -->
      <van-pull-refresh v-else v-model="refreshing" @refresh="handleRefresh">
        <van-list
          v-model:loading="listLoading"
          :finished="finished"
          finished-text="没有更多了"
          @load="handleLoadMore"
        >
          <ApartmentCard
            v-for="apartment in apartmentList"
            :key="apartment.id"
            :apartment="apartment"
            :show-actions="true"
            @click="handleViewApartment"
            @edit="handleEditApartment"
            @delete="handleDeleteApartment"
            @publish="handlePublishApartment"
            @unpublish="handleUnpublishApartment"
            @manage-rooms="handleManageRooms"
          />
        </van-list>
        
        <!-- 空状态 -->
        <EmptyState
          v-if="showEmpty"
          :type="emptyStateType"
          @action="handleEmptyAction"
        />
      </van-pull-refresh>
    </div>

    <!-- 浮动操作按钮 -->
    <van-floating-bubble
      axis="xy"
      icon="plus"
      @click="handleCreateApartment"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { NavigationHelper } from '@/utils/navigationHelper';
import { useLandlordStore } from '@/store/modules/landlord';
import { useUserStore } from '@/store/modules/user';
import { 
  getMyApartments, 
  getApartmentStatistics,
  deleteApartment,
  updateApartmentReleaseStatus
} from '@/api/landlord/apartment';
import type { 
  ApartmentItemVo, 
  ApartmentQueryVo,
  ApartmentStatistics,
  ReleaseStatus
} from '@/types/landlord';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import StatCard from '@/components/landlord/StatCard/StatCard.vue';
import ApartmentCard from '@/components/landlord/ApartmentCard/ApartmentCard.vue';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';

const router = useRouter();
const landlordStore = useLandlordStore();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const refreshing = ref(false);
const listLoading = ref(false);
const finished = ref(false);
const searchKeyword = ref('');
const activeTab = ref('all');
const currentPage = ref(1);
const pageSize = ref(10);
const apartmentList = ref<ApartmentItemVo[]>([]);
const statistics = ref<any>(null);
const isInitialized = ref(false); // 添加初始化标志

// 计算属性
const showEmpty = computed(() => 
  !loading.value && !listLoading.value && apartmentList.value.length === 0
);

const emptyType = computed(() => {
  if (searchKeyword.value) {
    return 'no-search';
  }
  return 'no-apartments';
});

const emptyStateType = computed(() => {
  if (searchKeyword.value) {
    return 'no-search';
  }
  return 'no-apartments';
});

// 查询参数
const queryParams = computed((): ApartmentQueryVo => {
  const params: ApartmentQueryVo = {};
  
  if (activeTab.value === 'published') {
    params.isRelease = 1;
  } else if (activeTab.value === 'unpublished') {
    params.isRelease = 0;
  }
  
  return params;
});

// 格式化函数
const formatPercentage = (value: number | string) => {
  const num = typeof value === 'string' ? parseFloat(value) : value;
  return num.toFixed(1);
};

// 加载公寓列表
const loadApartmentList = async (isRefresh = false) => {
  try {
    if (isRefresh) {
      currentPage.value = 1;
      finished.value = false;
    }
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    console.log('🔍 [ApartmentList] 开始加载公寓列表:', {
      isRefresh,
      currentPage: currentPage.value,
      landlordId,
      queryParams: queryParams.value
    });
    
    const { data } = await getMyApartments({
      current: currentPage.value,
      size: pageSize.value,
      landlordId,
      queryVo: queryParams.value
    });
    
    console.log('🔍 [ApartmentList] API返回数据:', {
      recordsLength: data.records.length,
      isRefresh
    });
    
    if (isRefresh) {
      apartmentList.value = data.records;
    } else {
      apartmentList.value.push(...data.records);
    }
    
    console.log('🔍 [ApartmentList] 更新后的公寓列表长度:', apartmentList.value.length);
    
    // 检查是否还有更多数据
    if (data.records.length < pageSize.value) {
      finished.value = true;
    } else {
      currentPage.value++;
    }
    
  } catch (error) {
    console.error('🔍 [ApartmentList] 加载公寓列表失败:', error);
    showToast('加载公寓列表失败');
  }
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) return;
    
    const { data } = await getApartmentStatistics(landlordId);
    statistics.value = data;
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    await Promise.all([
      loadApartmentList(true),
      loadStatistics()
    ]);
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
    await initData();
    showToast('刷新成功');
  } catch (error) {
    showToast('刷新失败');
  } finally {
    refreshing.value = false;
  }
};

const handleLoadMore = async () => {
  // 如果还没有初始化完成，不执行加载更多
  if (!isInitialized.value || finished.value) {
    console.log('handleLoadMore blocked: isInitialized=', isInitialized.value, 'finished=', finished.value);
    return;
  }
  
  console.log('handleLoadMore: loading more data...');
  try {
    listLoading.value = true;
    await loadApartmentList();
  } finally {
    listLoading.value = false;
  }
};

const handleSearch = () => {
  // TODO: 实现搜索功能
  showToast('搜索功能开发中');
};

const handleClearSearch = () => {
  searchKeyword.value = '';
  handleRefresh();
};

// 标签页切换处理
const onTabChange = (name: string | number) => {
  console.log('🔄 切换标签页:', name)
  
  try {
    // 重置分页和数据
    currentPage.value = 1
    finished.value = false
    apartmentList.value = []
    
    // 重新加载数据
    loadApartmentList(true)
  } catch (error) {
    console.error('标签切换失败:', error)
    // 即使出错，也要确保标签保持可用状态
    showToast('切换失败，请重试')
  }
}

const handleCreateApartment = () => {
  router.push('/landlord/apartments/form');
};

const handleViewApartment = (apartment: ApartmentItemVo) => {
  router.push(`/landlord/apartments/${apartment.id}`);
};

const handleEditApartment = (apartment: ApartmentItemVo) => {
  router.push(`/landlord/apartments/form/${apartment.id}`);
};

const handleDeleteApartment = async (apartment: ApartmentItemVo) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除公寓"${apartment.name}"吗？删除后相关房源也会被删除。`,
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await deleteApartment(apartment.id, landlordId);
    showToast('删除成功');
    
    // 从列表中移除
    const index = apartmentList.value.findIndex(item => item.id === apartment.id);
    if (index !== -1) {
      apartmentList.value.splice(index, 1);
    }
    
    // 刷新统计数据
    await loadStatistics();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除公寓失败:', error);
      showToast('删除失败');
    }
  }
};

const handlePublishApartment = async (apartment: ApartmentItemVo) => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await updateApartmentReleaseStatus(apartment.id, 1, landlordId);
    showToast('发布成功');
    
    // 更新列表中的状态
    const index = apartmentList.value.findIndex(item => item.id === apartment.id);
    if (index !== -1) {
      apartmentList.value[index].isRelease = 1;
    }
    
    // 刷新统计数据
    await loadStatistics();
    
  } catch (error) {
    console.error('发布公寓失败:', error);
    showToast('发布失败');
  }
};

const handleUnpublishApartment = async (apartment: ApartmentItemVo) => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await updateApartmentReleaseStatus(apartment.id, 0, landlordId);
    showToast('下架成功');
    
    // 更新列表中的状态
    const index = apartmentList.value.findIndex(item => item.id === apartment.id);
    if (index !== -1) {
      apartmentList.value[index].isRelease = 0;
    }
    
    // 刷新统计数据
    await loadStatistics();
    
  } catch (error) {
    console.error('下架公寓失败:', error);
    showToast('下架失败');
  }
};

const handleManageRooms = (apartment: ApartmentItemVo) => {
  router.push(`/landlord/rooms?apartmentId=${apartment.id}`);
};

const handleEmptyAction = () => {
  if (emptyStateType.value === 'no-apartments') {
    handleCreateApartment();
  } else if (emptyStateType.value === 'no-search') {
    handleClearSearch();
  }
};

// 返回按钮处理
const handleGoBack = () => {
  console.log('🧭 公寓管理页面返回按钮被点击');
  NavigationHelper.smartGoBack(router);
};

// 生命周期
onMounted(async () => {
  console.log('Component mounted, initializing data...');
  await initData();
  // 初始化完成后，允许加载更多
  isInitialized.value = true;
  console.log('Initialization completed, isInitialized set to true');
});
</script>

<style scoped lang="less">
/* 固定头部区域，防止跳动 */
.apartment-list-header-fixed {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #f7f8fa;
  padding-bottom: 8px;
  /* 强制固定高度，防止任何布局变化 */
  min-height: 120px;
  max-height: 120px;
  overflow: hidden;
  /* 防止重排和重绘 */
  contain: layout style;
  will-change: auto;
}

.filter-tabs {
  background: white;
  padding: 0 16px;
  margin-bottom: 8px;
  /* 强制固定高度，绝对防止跳动 */
  height: 44px;
  min-height: 44px;
  max-height: 44px;
  display: flex;
  align-items: center;
  /* 强制布局稳定性 */
  position: relative;
  z-index: 1;
  overflow: hidden;
  /* 防止内容变化影响布局 */
  contain: layout style;
  /* 优化渲染性能 */
  transform: translateZ(0);
}

.filter-tabs :deep(.van-tabs__nav) {
  padding-bottom: 0;
  min-height: 44px;
  width: 100%; /* 确保导航栏占满宽度 */
  position: relative; /* 防止重排 */
}

.filter-tabs :deep(.van-tabs__line) {
  width: 20px;
  background: #1989fa;
  transition: none; /* 禁用动画防止跳动 */
}

.filter-tabs :deep(.van-tab) {
  color: #646566;
  font-size: 14px;
  flex: 1; /* 让每个标签平均分配宽度 */
  text-align: center;
  padding: 0 8px; /* 增加内边距 */
  white-space: nowrap; /* 防止文字换行 */
  overflow: visible; /* 确保文字可见 */
  min-width: 0; /* 防止flex收缩问题 */
  box-sizing: border-box; /* 确保盒模型一致 */
}

.filter-tabs :deep(.van-tab--active) {
  color: #1989fa;
  font-weight: 500;
}

.apartment-list-header {
  margin-bottom: 16px;
  
  &__stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin-bottom: 12px;
  }
  
  &__actions {
    display: flex;
    justify-content: flex-end;
  }
}

.apartment-list-filters {
  margin-bottom: 16px;
}

.apartment-list-content {
  /* 强制最小高度，防止布局塌陷 */
  min-height: 400px;
  max-height: none;
  padding-bottom: 20px;
  /* 防止内容变化影响整体布局 */
  contain: layout;
  /* 确保内容区域稳定 */
  position: relative;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0; /* 增加加载状态的高度 */
  color: #999;
}

// 响应式设计
@media (max-width: 375px) {
  .apartment-list-header {
    &__stats {
      gap: 6px;
    }
  }
}
</style>