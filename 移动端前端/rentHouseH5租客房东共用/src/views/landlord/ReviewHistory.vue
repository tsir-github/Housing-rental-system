<template>
  <PageContainer :loading="loading" :show-empty="showEmpty" empty-type="no-data">
    <!-- 页面头部 -->
    <template #header>
      <div class="review-history-header">
        <h2 class="header-title">审核历史</h2>
        <p class="header-subtitle" v-if="roomInfo">
          {{ roomInfo.apartmentName }} - {{ roomInfo.roomNumber }}
        </p>
      </div>
    </template>

    <!-- 审核历史时间线 -->
    <div class="review-history-timeline">
      <div
        v-for="(review, index) in reviewHistory"
        :key="review.id"
        class="timeline-item"
        :class="{ 'timeline-item--latest': index === 0 }"
      >
        <div class="timeline-dot" :class="getTimelineDotClass(review, index)"></div>
        
        <div class="timeline-content">
          <div class="timeline-header">
            <div class="timeline-status">
              <ReviewStatusBadge :status="getReviewStatus(review)" size="small" />
              <span class="timeline-time">{{ formatDate(review.reviewedAt) }}</span>
            </div>
            
            <div class="timeline-reviewer" v-if="review.reviewerName">
              <van-icon name="contact" size="12" color="#969799" />
              <span>{{ review.reviewerName }}</span>
            </div>
          </div>
          
          <!-- 审核评分 -->
          <div class="timeline-score" v-if="review.overallRating">
            <div class="score-overall">
              <span class="score-label">综合评级：</span>
              <span class="score-text" :style="{ color: getOverallRatingLevel(review.overallRating).color }">
                {{ getOverallRatingText(review.overallRating) }}
              </span>
            </div>
            
            <div class="score-details">
              <div class="score-detail-item">
                <span>图片质量：</span>
                <span class="score-value">{{ review.photoQualityScore?.toFixed(1) || '-' }}</span>
              </div>
              <div class="score-detail-item">
                <span>描述准确性：</span>
                <span class="score-value">{{ review.descriptionAccuracyScore?.toFixed(1) || '-' }}</span>
              </div>
              <div class="score-detail-item">
                <span>设施完整性：</span>
                <span class="score-value">{{ review.facilityCompletenessScore?.toFixed(1) || '-' }}</span>
              </div>
              <div class="score-detail-item">
                <span>位置准确性：</span>
                <span class="score-value">{{ review.locationAccuracyScore?.toFixed(1) || '-' }}</span>
              </div>
            </div>
          </div>
          
          <!-- 审核意见 -->
          <div class="timeline-comments" v-if="review.reviewComments">
            <div class="comments-label">审核意见：</div>
            <div class="comments-text">{{ review.reviewComments }}</div>
          </div>
          
          <!-- 改进建议 -->
          <div class="timeline-suggestion" v-if="review.improvementSuggestion">
            <div class="suggestion-label">
              <van-icon name="warning-o" size="14" color="#ff976a" />
              改进建议：
            </div>
            <div class="suggestion-text">{{ review.improvementSuggestion }}</div>
          </div>
          
          <!-- 操作记录 -->
          <div class="timeline-actions" v-if="index === 0 && getReviewStatus(review) === 3">
            <van-button 
              type="primary" 
              size="mini"
              @click="handleResubmit"
              :loading="resubmitting"
            >
              重新提交
            </van-button>
            
            <van-button 
              type="default" 
              size="mini"
              @click="handleEditRoom"
            >
              编辑房源
            </van-button>
          </div>
        </div>
      </div>
      
      <!-- 初始提交记录 -->
      <div class="timeline-item timeline-item--initial">
        <div class="timeline-dot timeline-dot--initial"></div>
        
        <div class="timeline-content">
          <div class="timeline-header">
            <div class="timeline-status">
              <van-tag type="default" size="small">已提交</van-tag>
              <span class="timeline-time">{{ formatDate(roomInfo?.createTime) }}</span>
            </div>
          </div>
          
          <div class="timeline-desc">
            房源信息首次提交，等待审核
          </div>
        </div>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="review-history-stats" v-if="reviewHistory.length > 0">
      <div class="stats-title">审核统计</div>
      
      <div class="stats-grid">
        <div class="stats-item">
          <div class="stats-value">{{ reviewHistory.length }}</div>
          <div class="stats-label">审核次数</div>
        </div>
        
        <div class="stats-item">
          <div class="stats-value">{{ getAverageScore() }}</div>
          <div class="stats-label">平均评分</div>
        </div>
        
        <div class="stats-item">
          <div class="stats-value">{{ getReviewDays() }}</div>
          <div class="stats-label">审核天数</div>
        </div>
      </div>
    </div>

    <!-- 帮助提示 -->
    <div class="review-history-tips">
      <van-notice-bar
        left-icon="info-o"
        text="审核历史记录了房源的所有审核过程，您可以根据历史建议持续优化房源质量。"
        color="#1989fa"
        background="#e6f7ff"
      />
    </div>

    <!-- 空状态 -->
    <EmptyState
      v-if="showEmpty"
      type="no-data"
      title="暂无审核记录"
      description="该房源还没有审核记录"
      @action="handleGoBack"
    >
      <template #action>
        <van-button type="primary" size="small">返回</van-button>
      </template>
    </EmptyState>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { useUserStore } from '@/store/modules/user';
import {
  getReviewHistory,
  getRoomDetail,
  resubmitForReview
} from '@/api/landlord';
import { getOverallRatingText, getOverallRatingLevel } from '@/api/landlord/review';
import type { RoomReview } from '@/api/landlord';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import ReviewStatusBadge from '@/components/landlord/ReviewStatusBadge/ReviewStatusBadge.vue';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const resubmitting = ref(false);
const reviewHistory = ref<RoomReview[]>([]);
const roomInfo = ref<any>(null);

// 计算属性
const showEmpty = computed(() => 
  !loading.value && reviewHistory.value.length === 0
);

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 获取审核状态（基于评分判断）
const getReviewStatus = (review: RoomReview) => {
  if (review.overallRating >= 4) {
    return 2; // 审核通过
  } else if (review.overallRating >= 1) {
    return 3; // 审核拒绝
  }
  return 1; // 待审核
};

// 获取时间线点的样式类
const getTimelineDotClass = (review: RoomReview, index: number) => {
  const status = getReviewStatus(review);
  const classes = ['timeline-dot'];
  
  if (index === 0) {
    classes.push('timeline-dot--latest');
  }
  
  if (status === 2) {
    classes.push('timeline-dot--approved');
  } else if (status === 3) {
    classes.push('timeline-dot--rejected');
  } else {
    classes.push('timeline-dot--pending');
  }
  
  return classes;
};

// 计算平均评分
const getAverageScore = () => {
  if (reviewHistory.value.length === 0) return '-';
  
  const totalScore = reviewHistory.value.reduce((sum, review) => {
    return sum + (review.overallRating || 0);
  }, 0);
  
  return (totalScore / reviewHistory.value.length).toFixed(1);
};

// 计算审核天数
const getReviewDays = () => {
  if (reviewHistory.value.length === 0 || !roomInfo.value?.createTime) return '-';
  
  const createDate = new Date(roomInfo.value.createTime);
  const latestReviewDate = new Date(reviewHistory.value[0].reviewedAt);
  const diffTime = Math.abs(latestReviewDate.getTime() - createDate.getTime());
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  return diffDays;
};

// 加载审核历史
const loadReviewHistory = async () => {
  try {
    loading.value = true;
    const roomId = Number(route.params.id);
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    // 并行加载审核历史和房源信息
    const [historyRes, roomRes] = await Promise.all([
      getReviewHistory(roomId, landlordId),
      getRoomDetail(roomId, landlordId)
    ]);
    
    reviewHistory.value = historyRes.data;
    roomInfo.value = roomRes.data;
    
  } catch (error) {
    console.error('加载审核历史失败:', error);
    showToast('加载审核历史失败');
  } finally {
    loading.value = false;
  }
};

// 事件处理
const handleResubmit = async () => {
  try {
    await showConfirmDialog({
      title: '确认重新提交',
      message: '确定要重新提交审核吗？请确保已根据审核建议进行了修改。'
    });
    
    resubmitting.value = true;
    const roomId = Number(route.params.id);
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await resubmitForReview(roomId, landlordId);
    showToast('重新提交成功');
    
    // 刷新页面数据
    await loadReviewHistory();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('重新提交失败:', error);
      showToast('重新提交失败');
    }
  } finally {
    resubmitting.value = false;
  }
};

const handleEditRoom = () => {
  const roomId = route.params.id;
  router.push(`/landlord/rooms/edit/${roomId}`);
};

const handleGoBack = () => {
  router.back();
};

// 生命周期
onMounted(() => {
  loadReviewHistory();
});
</script>

<style scoped lang="less">
.review-history-header {
  margin-bottom: 20px;
  
  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #323233;
    margin: 0 0 8px 0;
  }
  
  .header-subtitle {
    font-size: 14px;
    color: #969799;
    margin: 0;
  }
}

.review-history-timeline {
  position: relative;
  margin-bottom: 20px;
  
  &::before {
    content: '';
    position: absolute;
    left: 12px;
    top: 0;
    bottom: 0;
    width: 2px;
    background: #ebedf0;
  }
}

.timeline-item {
  position: relative;
  padding-left: 40px;
  margin-bottom: 24px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &--latest {
    .timeline-content {
      background: #f0f9ff;
      border: 1px solid #bae7ff;
    }
  }
  
  &--initial {
    .timeline-content {
      background: #f7f8fa;
    }
  }
}

.timeline-dot {
  position: absolute;
  left: 0;
  top: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #ebedf0;
  border: 4px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  
  &--latest {
    background: #1989fa;
    box-shadow: 0 0 0 4px rgba(25, 137, 250, 0.2);
  }
  
  &--approved {
    background: #07c160;
  }
  
  &--rejected {
    background: #ee0a24;
  }
  
  &--pending {
    background: #ff976a;
  }
  
  &--initial {
    background: #c8c9cc;
  }
}

.timeline-content {
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.timeline-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.timeline-time {
  font-size: 12px;
  color: #969799;
}

.timeline-reviewer {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #646566;
}

.timeline-score {
  margin-bottom: 12px;
  
  .score-overall {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    
    .score-label {
      font-size: 14px;
      color: #323233;
      font-weight: 500;
    }
    
    .score-text {
      font-size: 14px;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 4px;
      background: rgba(255, 255, 255, 0.8);
    }
    
    .score-number {
      font-size: 14px;
      font-weight: 600;
      color: #ffd21e;
    }
  }
  
  .score-details {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    padding: 8px;
    background: #f7f8fa;
    border-radius: 6px;
  }
  
  .score-detail-item {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #646566;
    
    .score-value {
      font-weight: 600;
      color: #323233;
    }
  }
}

.timeline-comments {
  margin-bottom: 12px;
  
  .comments-label {
    font-size: 13px;
    color: #646566;
    margin-bottom: 4px;
  }
  
  .comments-text {
    font-size: 14px;
    color: #323233;
    line-height: 1.5;
    padding: 8px;
    background: #f7f8fa;
    border-radius: 6px;
  }
}

.timeline-suggestion {
  margin-bottom: 12px;
  
  .suggestion-label {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #ff976a;
    font-weight: 500;
    margin-bottom: 4px;
  }
  
  .suggestion-text {
    font-size: 14px;
    color: #ee0a24;
    line-height: 1.5;
    padding: 8px;
    background: #fef2f2;
    border-radius: 6px;
    border-left: 3px solid #ee0a24;
  }
}

.timeline-actions {
  display: flex;
  gap: 8px;
}

.timeline-desc {
  font-size: 13px;
  color: #646566;
  line-height: 1.4;
}

.review-history-stats {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  
  .stats-title {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 12px;
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
  
  .stats-item {
    text-align: center;
    
    .stats-value {
      font-size: 20px;
      font-weight: 600;
      color: #1989fa;
      margin-bottom: 4px;
    }
    
    .stats-label {
      font-size: 12px;
      color: #969799;
    }
  }
}

.review-history-tips {
  margin-bottom: 16px;
}

// 响应式设计
@media (max-width: 375px) {
  .timeline-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .score-details {
    grid-template-columns: 1fr;
  }
  
  .timeline-actions {
    flex-wrap: wrap;
    gap: 6px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>