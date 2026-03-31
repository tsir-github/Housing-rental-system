<template>
  <PageContainer :loading="loading" :show-empty="showEmpty" :empty-type="emptyType">
    <!-- 页面头部 -->
    <template #header>
      <div class="room-list-header">
        <div class="room-list-header__stats">
          <StatCard
            :value="statistics?.totalRooms || 0"
            label="房源总数"
            icon="home-o"
            icon-color="#1989fa"
            unit="间"
            size="small"
          />
          
          <StatCard
            :value="statistics?.published || 0"
            label="已发布"
            icon="eye-o"
            icon-color="#07c160"
            unit="间"
            size="small"
          />
          
          <StatCard
            :value="statistics?.avgComplianceScore || 0"
            label="平均评分"
            icon="star"
            icon-color="#ffd21e"
            unit="分"
            :formatter="formatScore"
            size="small"
          />
        </div>
        
        <div class="room-list-header__actions">
          <van-button 
            type="primary" 
            size="small" 
            icon="plus"
            @click="handleCreateRoom"
          >
            发布房源
          </van-button>
        </div>
      </div>
    </template>

    <!-- 搜索和筛选 -->
    <div class="room-list-filters">
      <van-search
        v-model="searchKeyword"
        placeholder="搜索房源编号或公寓名称"
        @search="handleSearch"
        @clear="handleClearSearch"
      />
      
      <!-- 筛选标签 - 统一样式 -->
      <div class="filter-tabs">
        <van-tabs v-model:active="activeTab" @change="onTabChange">
          <van-tab title="全部" name="all" />
          <van-tab title="已发布" name="published" />
          <van-tab title="未发布" name="unpublished" />
          <van-tab title="待审核" name="pending" />
          <van-tab title="审核拒绝" name="rejected" />
        </van-tabs>
      </div>
      
      <!-- 高级筛选 -->
      <div class="room-list-filters__advanced">
        <van-button 
          type="default" 
          size="small" 
          icon="filter-o"
          @click="showFilterPopup = true"
        >
          筛选
        </van-button>
        
        <van-button 
          type="default" 
          size="small" 
          icon="sort"
          @click="showSortPopup = true"
        >
          排序
        </van-button>
      </div>
    </div>

    <!-- 房源列表 -->
    <div class="room-list-content">
      <van-pull-refresh v-model="refreshing" @refresh="handleRefresh">
        <van-list
          v-model:loading="listLoading"
          :finished="finished"
          finished-text="没有更多了"
          @load="handleLoadMore"
        >
          <RoomCard
            v-for="room in roomList"
            :key="room.id"
            :room="room"
            :show-actions="true"
            @click="handleViewRoom"
            @edit="handleEditRoom"
            @delete="handleDeleteRoom"
            @publish="handlePublishRoom"
            @unpublish="handleUnpublishRoom"
            @resubmit="handleResubmitRoom"
          />
          
          <EmptyState
            v-if="showEmpty"
            :type="emptyStateType"
            @action="handleEmptyAction"
          />
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 浮动操作按钮 -->
    <van-floating-bubble
      axis="xy"
      icon="plus"
      @click="handleCreateRoom"
    />

    <!-- 筛选弹窗 -->
    <van-popup v-model:show="showFilterPopup" position="bottom" :style="{ height: '60%' }">
      <div class="filter-popup">
        <div class="filter-popup__header">
          <van-button type="default" size="small" @click="handleResetFilter">重置</van-button>
          <h3>筛选条件</h3>
          <van-button type="primary" size="small" @click="handleApplyFilter">确定</van-button>
        </div>
        
        <div class="filter-popup__content">
          <!-- 公寓筛选 -->
          <div class="filter-section">
            <div class="filter-section__title">所属公寓</div>
            <van-radio-group v-model="filterData.apartmentId">
              <van-radio name="">全部公寓</van-radio>
              <van-radio
                v-for="apartment in apartmentOptions"
                :key="apartment.id"
                :name="apartment.id"
              >
                {{ apartment.name }}
              </van-radio>
            </van-radio-group>
          </div>
          
          <!-- 租金范围 -->
          <div class="filter-section">
            <div class="filter-section__title">租金范围</div>
            <div class="filter-section__range">
              <van-field
                v-model="filterData.minRent"
                type="number"
                placeholder="最低租金"
                label="￥"
              />
              <span class="range-separator">-</span>
              <van-field
                v-model="filterData.maxRent"
                type="number"
                placeholder="最高租金"
                label="￥"
              />
            </div>
          </div>
          
          <!-- 审核状态 -->
          <div class="filter-section">
            <div class="filter-section__title">审核状态</div>
            <van-radio-group v-model="filterData.reviewStatus">
              <van-radio name="">全部状态</van-radio>
              <van-radio name="1">待审核</van-radio>
              <van-radio name="2">审核通过</van-radio>
              <van-radio name="3">审核拒绝</van-radio>
            </van-radio-group>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 排序弹窗 -->
    <van-popup v-model:show="showSortPopup" position="bottom">
      <van-picker
        :columns="sortOptions"
        @confirm="handleSortConfirm"
        @cancel="showSortPopup = false"
      />
    </van-popup>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onActivated, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { useLandlordStore } from '@/store/modules/landlord';
import { useUserStore } from '@/store/modules/user';
import { 
  getMyRooms, 
  getRoomStatistics,
  deleteRoom,
  updateRoomReleaseStatus,
  resubmitForReview,
  getMyApartments
} from '@/api/landlord';
import type { RoomItemVo, RoomQueryVo, ApartmentItemVo } from '@/api/landlord';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import StatCard from '@/components/landlord/StatCard/StatCard.vue';
import RoomCard from '@/components/landlord/RoomCard/RoomCard.vue';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';

const router = useRouter();
const route = useRoute();
const landlordStore = useLandlordStore();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const refreshing = ref(false);
const listLoading = ref(false);
const finished = ref(true); // 初始设为 true，防止自动触发加载更多
const searchKeyword = ref('');
const activeTab = ref('all');
const currentPage = ref(1);
const pageSize = ref(10);
const roomList = ref<RoomItemVo[]>([]);
const statistics = ref<any>(null);
const apartmentOptions = ref<ApartmentItemVo[]>([]);

// 防止重复加载的标志
const isInitialized = ref(false);
const isLoadingData = ref(false); // 防止并发加载
const globalLock = ref(false); // 全局锁，防止任何形式的重复调用

// 筛选和排序
const showFilterPopup = ref(false);
const showSortPopup = ref(false);
const filterData = ref({
  apartmentId: '',
  minRent: '',
  maxRent: '',
  reviewStatus: ''
});

const sortOptions = ref([
  { text: '创建时间（最新）', value: 'createTime_desc' },
  { text: '创建时间（最早）', value: 'createTime_asc' },
  { text: '租金（从低到高）', value: 'rent_asc' },
  { text: '租金（从高到低）', value: 'rent_desc' },
  { text: '审核评分（从高到低）', value: 'score_desc' }
]);

const currentSort = ref('createTime_desc');

// 计算属性
const showEmpty = computed(() => 
  !loading.value && !listLoading.value && roomList.value.length === 0
);

const emptyType = computed(() => {
  if (searchKeyword.value) {
    return 'no-search';
  }
  return 'no-rooms';
});

const emptyStateType = computed(() => {
  if (searchKeyword.value) {
    return 'no-search';
  }
  return 'no-rooms';
});

// 查询参数
const queryParams = computed((): RoomQueryVo => {
  const params: RoomQueryVo = {};
  
  // 标签页筛选
  if (activeTab.value === 'published') {
    params.isRelease = 1;
  } else if (activeTab.value === 'unpublished') {
    params.isRelease = 0;
  } else if (activeTab.value === 'pending') {
    params.reviewStatus = 1;
  } else if (activeTab.value === 'rejected') {
    params.reviewStatus = 3;
  }
  
  // 高级筛选
  if (filterData.value.minRent) {
    params.minRent = Number(filterData.value.minRent);
  }
  if (filterData.value.maxRent) {
    params.maxRent = Number(filterData.value.maxRent);
  }
  if (filterData.value.reviewStatus) {
    params.reviewStatus = Number(filterData.value.reviewStatus);
  }
  
  // URL参数（从公寓页面跳转过来）
  if (route.query.apartmentId) {
    params.apartmentId = Number(route.query.apartmentId);
  }
  
  return params;
});

// 格式化函数
const formatScore = (value: number | string) => {
  const num = typeof value === 'string' ? parseFloat(value) : value;
  return num.toFixed(1);
};

// 加载公寓选项
const loadApartmentOptions = async () => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) return;
    
    const { data } = await getMyApartments({
      current: 1,
      size: 100, // 获取所有公寓
      landlordId
    });
    
    apartmentOptions.value = data.records;
  } catch (error) {
    console.error('加载公寓选项失败:', error);
  }
};

// 加载房源列表
const loadRoomList = async (isRefresh = true) => {
  // 全局锁检查 - 最高优先级
  if (globalLock.value) {
    console.log('🔍 全局锁激活，跳过所有数据加载操作');
    return;
  }
  
  // 防止并发调用
  if (isLoadingData.value) {
    console.log('🔍 loadRoomList 正在执行中，跳过重复调用');
    return;
  }
  
  // 先检查必要条件，避免设置锁后再检查
  const landlordId = userStore.userInfo?.id;
  if (!landlordId) {
    console.log('🔍 用户信息不存在，跳过加载');
    showToast('用户信息获取失败');
    return;
  }
  
  try {
    globalLock.value = true; // 激活全局锁
    isLoadingData.value = true;
    
    if (isRefresh) {
      currentPage.value = 1;
      finished.value = false;
    }
    
    console.log('🔍 [RoomList] 开始加载房源列表:', { 
      isRefresh, 
      currentPage: currentPage.value, 
      currentListLength: roomList.value.length,
      queryParams: queryParams.value
    });
    
    const { data } = await getMyRooms({
      current: currentPage.value,
      size: pageSize.value,
      landlordId,
      queryVo: queryParams.value
    });
    
    console.log('🔍 [RoomList] API返回数据:', { 
      recordsLength: data.records.length,
      isRefresh 
    });
    
    if (isRefresh) {
      roomList.value = data.records;
      console.log('🔍 刷新模式：替换数据，新长度:', roomList.value.length);
    } else {
      const oldLength = roomList.value.length;
      roomList.value.push(...data.records);
      console.log('🔍 追加模式：从', oldLength, '增加到', roomList.value.length);
    }
    
    // 检查是否还有更多数据
    if (data.records.length < pageSize.value) {
      finished.value = true;
    } else {
      currentPage.value++;
    }
    
  } catch (error) {
    console.error('🔍 [RoomList] 加载房源列表失败:', error);
    showToast('加载房源列表失败');
  } finally {
    isLoadingData.value = false;
    globalLock.value = false; // 释放全局锁
    console.log('🔍 释放所有锁');
  }
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) return;
    
    const { data } = await getRoomStatistics(landlordId);
    statistics.value = data;
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

// 初始化数据
const initData = async () => {
  // 防止重复初始化
  if (isInitialized.value) {
    console.log('RoomList已初始化，跳过重复加载');
    return;
  }
  
  try {
    loading.value = true;
    isInitialized.value = true;
    
    await Promise.all([
      loadRoomList(true),
      loadStatistics(),
      loadApartmentOptions()
    ]);
  } catch (error) {
    console.error('初始化数据失败:', error);
    isInitialized.value = false; // 失败时重置标志，允许重试
  } finally {
    loading.value = false;
  }
};

// 事件处理
const handleRefresh = async () => {
  // 如果已经在刷新中，直接返回
  if (refreshing.value || isLoadingData.value) {
    console.log('🔍 handleRefresh 跳过重复调用:', { refreshing: refreshing.value, isLoadingData: isLoadingData.value });
    return;
  }
  
  try {
    refreshing.value = true;
    // 重置分页状态
    currentPage.value = 1;
    finished.value = false;
    
    console.log('🔍 开始刷新数据...');
    
    // 直接调用 loadRoomList 而不是 initData，避免重复调用
    await Promise.all([
      loadRoomList(true),
      loadStatistics(),
      loadApartmentOptions()
    ]);
    
    console.log('🔍 刷新完成，当前房源数量:', roomList.value.length);
    showToast('刷新成功');
  } catch (error) {
    console.error('🔍 刷新失败:', error);
    showToast('刷新失败');
  } finally {
    refreshing.value = false;
  }
};

const handleLoadMore = async () => {
  // 如果正在刷新或已经在加载数据，则跳过
  if (finished.value || refreshing.value || isLoadingData.value) {
    console.log('🔍 handleLoadMore 跳过:', { finished: finished.value, refreshing: refreshing.value, isLoadingData: isLoadingData.value });
    return;
  }
  
  try {
    listLoading.value = true;
    await loadRoomList(false); // 明确传递 false，表示追加数据而不是刷新
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
    roomList.value = []
    
    // 重新加载数据
    loadRoomList(true)
  } catch (error) {
    console.error('标签切换失败:', error)
    // 即使出错，也要确保标签保持可用状态
    showToast('切换失败，请重试')
  }
}

const handleCreateRoom = () => {
  const apartmentId = route.query.apartmentId;
  if (apartmentId) {
    router.push(`/landlord/rooms/create?apartmentId=${apartmentId}`);
  } else {
    router.push('/landlord/rooms/create');
  }
};

const handleViewRoom = (room: RoomItemVo) => {
  router.push(`/landlord/rooms/${room.id}`);
};

const handleEditRoom = (room: RoomItemVo) => {
  router.push(`/landlord/rooms/edit/${room.id}`);
};

const handleDeleteRoom = async (room: RoomItemVo) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除房源"${room.roomNumber}"吗？`,
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await deleteRoom(room.id, landlordId);
    showToast('删除成功');
    
    // 从列表中移除
    const index = roomList.value.findIndex(item => item.id === room.id);
    if (index !== -1) {
      roomList.value.splice(index, 1);
    }
    
    // 刷新统计数据
    await loadStatistics();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除房源失败:', error);
      showToast('删除失败');
    }
  }
};

const handlePublishRoom = async (room: RoomItemVo) => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await updateRoomReleaseStatus(room.id, 1, landlordId);
    showToast('发布成功');
    
    // 更新列表中的状态
    const index = roomList.value.findIndex(item => item.id === room.id);
    if (index !== -1) {
      roomList.value[index].isRelease = 1;
    }
    
    // 刷新统计数据
    await loadStatistics();
    
  } catch (error) {
    console.error('发布房源失败:', error);
    showToast('发布失败');
  }
};

const handleUnpublishRoom = async (room: RoomItemVo) => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await updateRoomReleaseStatus(room.id, 0, landlordId);
    showToast('下架成功');
    
    // 更新列表中的状态
    const index = roomList.value.findIndex(item => item.id === room.id);
    if (index !== -1) {
      roomList.value[index].isRelease = 0;
    }
    
    // 刷新统计数据
    await loadStatistics();
    
  } catch (error) {
    console.error('下架房源失败:', error);
    showToast('下架失败');
  }
};

const handleResubmitRoom = async (room: RoomItemVo) => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await resubmitForReview(room.id, landlordId);
    showToast('重新提交成功');
    
    // 更新列表中的状态
    const index = roomList.value.findIndex(item => item.id === room.id);
    if (index !== -1) {
      roomList.value[index].reviewStatus = 1; // 重新设为待审核状态
    }
    
  } catch (error) {
    console.error('重新提交失败:', error);
    showToast('重新提交失败');
  }
};

const handleEmptyAction = () => {
  if (emptyStateType.value === 'no-rooms') {
    handleCreateRoom();
  } else if (emptyStateType.value === 'no-search') {
    handleClearSearch();
  }
};

// 筛选和排序
const handleResetFilter = () => {
  filterData.value = {
    apartmentId: '',
    minRent: '',
    maxRent: '',
    reviewStatus: ''
  };
};

const handleApplyFilter = () => {
  showFilterPopup.value = false;
  handleRefresh();
};

const handleSortConfirm = (value: any) => {
  currentSort.value = value.value;
  showSortPopup.value = false;
  // TODO: 实现排序功能
  showToast('排序功能开发中');
};

// 监听器
watch(activeTab, (newTab, oldTab) => {
  // 只有当tab真正发生变化时才刷新（避免初始化时触发）
  if (oldTab !== undefined && newTab !== oldTab && !refreshing.value && !isLoadingData.value) {
    console.log('🔍 标签页切换，触发刷新:', { newTab, oldTab });
    handleRefresh();
  }
});

// 生命周期
onMounted(() => {
  initData();
});

// 处理keep-alive缓存组件激活时的重复加载
onActivated(() => {
  // 当组件从缓存中激活时，不需要重新加载数据
  // 只有在必要时才刷新数据，比如数据可能已过期
  console.log('RoomList组件被激活，跳过重复加载');
});
</script>

<style scoped lang="less">
/* 固定头部区域，防止跳动 */
.room-list-header-fixed {
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

.room-list-header {
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

.room-list-filters {
  margin-bottom: 16px;
  
  &__tabs {
    margin: 12px 0;
    
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
  
  &__advanced {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
  }
}

.room-list-content {
  /* 强制最小高度，防止布局塌陷 */
  min-height: 400px;
  max-height: none;
  padding-bottom: 20px;
  /* 防止内容变化影响整体布局 */
  contain: layout;
  /* 确保内容区域稳定 */
  position: relative;
}

.filter-popup {
  padding: 16px;
  
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  &__content {
    max-height: 400px;
    overflow-y: auto;
  }
}

.filter-section {
  margin-bottom: 24px;
  
  &__title {
    font-size: 14px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 12px;
  }
  
  &__range {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .range-separator {
      color: #969799;
    }
  }
}

// 响应式设计
@media (max-width: 375px) {
  .room-list-header {
    &__stats {
      gap: 6px;
    }
  }
  
  .room-list-filters {
    &__advanced {
      justify-content: center;
    }
  }
}
</style>