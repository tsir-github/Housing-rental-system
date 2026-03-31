<template>
  <div class="review-page">
    <!-- 页面头部统计 -->
    <div class="review-header" v-if="!loading">
      <div class="review-header__stats">
        <StatCard
          :value="statistics?.totalRooms || 0"
          label="房源总数"
          icon="home-o"
          icon-color="#1989fa"
          unit="间"
          size="small"
        />
        
        <StatCard
          :value="statistics?.pendingReview || 0"
          label="待审核"
          icon="clock-o"
          icon-color="#ff976a"
          unit="间"
          size="small"
          clickable
          @click="handleFilterPending"
        />
        
        <StatCard
          :value="statistics?.approved || 0"
          label="已通过"
          icon="passed"
          icon-color="#07c160"
          unit="间"
          size="small"
          clickable
          @click="handleFilterApproved"
        />
      </div>
      
      <div class="review-header__rate">
        <div class="approval-rate">
          <span class="rate-label">审核通过率</span>
          <span class="rate-value">{{ formatApprovalRate(statistics?.approvalRate) }}</span>
        </div>
        
        <div class="avg-score">
          <span class="score-label">平均合规性评分</span>
          <span class="score-value" :class="getScoreClass(statistics?.avgComplianceScore)">
            {{ formatComplianceScore(statistics?.avgComplianceScore) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 筛选标签 - 始终显示 -->
    <div class="review-filters">
      <van-tabs v-model:active="activeTab" @change="onTabChange">
        <van-tab title="全部" name="all" />
        <van-tab title="待审核" name="pending" />
        <van-tab title="审核通过" name="approved" />
        <van-tab title="审核拒绝" name="rejected" />
      </van-tabs>
    </div>

    <!-- 房源审核列表 -->
    <div class="review-content">
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
          <div
            v-for="room in roomList"
            :key="room.roomId"
            class="review-item"
            @click="handleViewDetail(room)"
          >
            <div class="review-item__header">
              <div class="review-item__room">
                <h4 class="room-number">房间 {{ room.roomNumber }}</h4>
                <div class="room-rent">¥{{ room.rent }}/月</div>
                <!-- 新增：显示公寓信息 -->
                <div class="apartment-info" v-if="room.apartmentName">
                  <van-icon name="location-o" size="12" color="#969799" />
                  <span class="apartment-name">{{ room.apartmentName }}</span>
                </div>
              </div>
              
              <div class="review-item__status">
                <van-tag 
                  :type="getStatusConfig(room.reviewStatus).type"
                  size="small"
                  round
                >
                  <van-icon 
                    :name="getStatusConfig(room.reviewStatus).icon" 
                    size="12"
                  />
                  {{ getStatusConfig(room.reviewStatus).text }}
                </van-tag>
              </div>
            </div>
            
            <div class="review-item__content">
              <div class="review-item__score" v-if="room.complianceScore">
                <span class="score-label">合规性评分：</span>
                <span class="score-value" :class="getScoreClass(room.complianceScore)">
                  {{ formatComplianceScore(room.complianceScore) }}
                </span>
                <span class="score-level" :style="{ color: getScoreLevel(room.complianceScore).color }">
                  ({{ getScoreLevel(room.complianceScore).level }})
                </span>
              </div>
              
              <div class="review-item__time" v-if="room.lastReviewedAt">
                <van-icon name="clock-o" size="12" color="#969799" />
                <span>最后审核：{{ formatReviewTime(room.lastReviewedAt) }}</span>
              </div>
              
              <div class="review-item__suggestion" v-if="room.improvementSuggestion">
                <van-icon name="info-o" size="12" color="#1989fa" />
                <span class="suggestion-text">{{ room.improvementSuggestion }}</span>
              </div>
              
              <div class="review-item__release" v-if="room.reviewStatus === 2">
                <van-tag 
                  :type="room.isRelease === 1 ? 'success' : 'default'"
                  size="mini"
                >
                  {{ room.isRelease === 1 ? '已发布' : '未发布' }}
                </van-tag>
                <span class="release-tip" v-if="room.isRelease === 0">
                  审核通过，可快捷发布
                </span>
              </div>
            </div>
            
            <van-icon name="arrow" size="16" color="#c8c9cc" class="review-item__arrow" />
          </div>
          
          <!-- 空状态 -->
          <EmptyState
            v-if="showEmpty"
            :type="emptyStateType"
            @action="handleEmptyAction"
          />
        </van-list>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { useUserStore } from '@/store/modules/user';

// 恢复API导入
import { 
  getReviewStatusList,
  getReviewStatistics,
  getReviewStatusConfig,
  formatComplianceScore,
  getScoreLevel
} from '@/api/landlord/review';
import type { ReviewStatusItem, ReviewStatistics } from '@/api/landlord/review';

import StatCard from '@/components/landlord/StatCard/StatCard.vue';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';

const router = useRouter();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const refreshing = ref(false);
const listLoading = ref(false);
const finished = ref(false);
const activeTab = ref('all');
const roomList = ref<ReviewStatusItem[]>([]);
const statistics = ref<ReviewStatistics | null>(null);
const currentPage = ref(1);
const pageSize = ref(10);

// 计算属性
const showEmpty = computed(() => 
  !loading.value && !listLoading.value && (!roomList.value || roomList.value.length === 0)
);

const emptyType = computed(() => {
  return 'no-reviews';
});

const emptyStateType = computed(() => {
  return 'no-reviews';
});

// 状态配置
const getStatusConfig = (status: number) => {
  return getReviewStatusConfig(status);
};

// 格式化函数
const formatApprovalRate = (rate: number | undefined) => {
  if (rate === undefined || rate === null) return '0%';
  return `${rate.toFixed(1)}%`;
};

const formatReviewTime = (timeString: string) => {
  const date = new Date(timeString);
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getScoreClass = (score: number | null | undefined) => {
  if (score === null || score === undefined) return '';
  
  if (score >= 9.0) return 'score-excellent';
  if (score >= 7.5) return 'score-good';
  if (score >= 6.0) return 'score-fair';
  return 'score-poor';
};

// 加载房源审核列表
const loadReviewList = async (isRefresh = false) => {
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
    
    console.log('🔍 开始加载审核列表，房东ID:', landlordId);
    
    // 调用真正的后端接口
    const reviewStatus = getReviewStatusFilter();
    const response = await getReviewStatusList({
      landlordId,
      current: currentPage.value,
      size: pageSize.value,
      reviewStatus
    });
    
    console.log('✅ 审核列表API响应:', response);
    console.log('📋 返回的记录数:', response?.data?.records?.length || 0);
    
    if (isRefresh) {
      roomList.value = response?.data?.records || [];
    } else {
      roomList.value.push(...(response?.data?.records || []));
    }
    
    console.log('📊 当前房源列表长度:', roomList.value.length);
    
    // 检查是否还有更多数据
    finished.value = currentPage.value >= (response?.data?.pages || 1);
    
  } catch (error) {
    console.error('❌ 加载审核列表失败:', error);
    showToast('加载审核列表失败');
    // 确保在错误情况下也初始化数组
    if (isRefresh) {
      roomList.value = [];
    }
  }
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      console.log('❌ 统计数据加载失败：房东ID不存在');
      return;
    }
    
    console.log('🔍 开始加载统计数据，房东ID:', landlordId);
    
    // 调用真正的后端接口
    const response = await getReviewStatistics(landlordId);
    console.log('✅ 统计数据API响应:', response);
    
    statistics.value = response?.data || response;
    console.log('📊 统计数据设置完成:', statistics.value);
  } catch (error) {
    console.error('❌ 加载统计数据失败:', error);
    // 如果统计接口失败，使用默认值
    statistics.value = {
      totalRooms: 0,
      pendingReview: 0,
      approved: 0,
      rejected: 0,
      avgComplianceScore: 0,
      approvalRate: 0
    };
  }
};

// 获取审核状态筛选值
const getReviewStatusFilter = () => {
  switch (activeTab.value) {
    case 'pending': return 1;
    case 'approved': return 2;
    case 'rejected': return 3;
    default: return undefined;
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    await Promise.all([
      loadReviewList(true),
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
  if (finished.value) return;
  
  try {
    listLoading.value = true;
    currentPage.value++;
    await loadReviewList();
  } finally {
    listLoading.value = false;
  }
};

// 标签页切换处理
const onTabChange = (name: string | number) => {
  console.log('🔄 切换标签页:', name)
  
  try {
    // 重新加载数据
    handleRefresh()
  } catch (error) {
    console.error('标签切换失败:', error)
    showToast('切换失败，请重试')
  }
}

const handleFilterPending = () => {
  activeTab.value = 'pending';
};

const handleFilterApproved = () => {
  activeTab.value = 'approved';
};

const handleViewDetail = (room: ReviewStatusItem) => {
  router.push(`/landlord/reviews/${room.roomId}`);
};

const handleEmptyAction = () => {
  // 空状态时的操作，可以跳转到房源管理
  router.push('/landlord/rooms');
};

// 生命周期
onMounted(() => {
  initData();
});
</script>

<style scoped lang="less">
/* 固定头部区域，防止跳动 */
.review-list-header-fixed {
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

.review-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.review-header {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
  
  &__stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin-bottom: 12px;
  }
  
  &__rate {
    display: flex;
    justify-content: space-around;
    background: #f8f9fa;
    border-radius: 8px;
    padding: 12px;
    
    .approval-rate,
    .avg-score {
      text-align: center;
      
      .rate-label,
      .score-label {
        display: block;
        font-size: 12px;
        color: #969799;
        margin-bottom: 4px;
      }
      
      .rate-value {
        font-size: 18px;
        font-weight: bold;
        color: #1989fa;
      }
      
      .score-value {
        font-size: 16px;
        font-weight: bold;
        
        &.score-excellent {
          color: #07c160;
        }
        
        &.score-good {
          color: #1989fa;
        }
        
        &.score-fair {
          color: #ff976a;
        }
        
        &.score-poor {
          color: #ee0a24;
        }
      }
    }
  }
}

.review-filters {
  background: white;
  margin-bottom: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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
  
  :deep(.van-tabs__nav) {
    background: transparent;
    padding: 0 16px;
    min-height: 44px; /* 确保导航栏有固定高度 */
    width: 100%; /* 确保导航栏占满宽度 */
  }
  
  :deep(.van-tab) {
    color: #646566;
    font-size: 14px;
    flex: 1; /* 让每个标签平均分配宽度 */
    text-align: center;
    padding: 0 8px; /* 增加内边距 */
    white-space: nowrap; /* 防止文字换行 */
    overflow: visible; /* 确保文字可见 */
  }
  
  :deep(.van-tab--active) {
    color: #1989fa;
    font-weight: 500;
  }
  
  :deep(.van-tabs__content) {
    display: none; /* 隐藏内容区域，只显示标签 */
  }
}

.review-content {
  padding: 0 16px;
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

.review-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
  
  &:active {
    transform: translateY(0);
  }
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }
  
  &__room {
    .room-number {
      font-size: 16px;
      font-weight: 600;
      color: #323233;
      margin: 0 0 4px 0;
    }
    
    .room-rent {
      font-size: 14px;
      color: #ee0a24;
      font-weight: 500;
      margin-bottom: 4px;
    }
    
    .apartment-info {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #969799;
      
      .apartment-name {
        max-width: 200px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
  
  &__content {
    margin-bottom: 8px;
  }
  
  &__score,
  &__time,
  &__suggestion,
  &__release {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 6px;
    font-size: 13px;
    color: #646566;
    
    .score-label {
      color: #969799;
    }
    
    .score-value {
      font-weight: 500;
      
      &.score-excellent {
        color: #07c160;
      }
      
      &.score-good {
        color: #1989fa;
      }
      
      &.score-fair {
        color: #ff976a;
      }
      
      &.score-poor {
        color: #ee0a24;
      }
    }
    
    .score-level {
      font-size: 12px;
      font-weight: 500;
    }
    
    .suggestion-text {
      color: #1989fa;
      font-size: 12px;
    }
    
    .release-tip {
      color: #07c160;
      font-size: 12px;
      margin-left: 8px;
    }
  }
  
  &__arrow {
    position: absolute;
    top: 50%;
    right: 16px;
    transform: translateY(-50%);
  }
}

// 响应式设计
@media (max-width: 375px) {
  .review-header {
    &__stats {
      gap: 6px;
    }
    
    &__rate {
      padding: 8px;
      
      .rate-value,
      .score-value {
        font-size: 16px;
      }
    }
  }
  
  .review-item {
    padding: 12px;
    
    &__header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
  }
}
</style>