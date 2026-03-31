<template>
  <div class="review-detail">
    <!-- 导航栏 -->
    <van-nav-bar
      title="审核详情"
      left-arrow
      @click-left="handleGoBack"
      fixed
      placeholder
    />
    
    <PageContainer :loading="loading" :show-header="false">
      <!-- 房源基本信息 -->
      <div class="review-detail-info">
        <div class="info-header">
          <h2 class="room-title">房间 {{ reviewDetail?.roomInfo?.roomNumber }}</h2>
          <van-tag 
            :type="getStatusConfig(reviewDetail?.reviewStatus).type"
            size="large"
            round
          >
            <van-icon 
              :name="getStatusConfig(reviewDetail?.reviewStatus).icon" 
              size="14"
            />
            {{ getStatusConfig(reviewDetail?.reviewStatus).text }}
          </van-tag>
        </div>
        
        <div class="info-content">
          <div class="info-item">
            <span class="label">租金：</span>
            <span class="value rent">¥{{ reviewDetail?.roomInfo?.rent }}/月</span>
          </div>
          
          <div class="info-item" v-if="reviewDetail?.complianceScore">
            <span class="label">合规性评分：</span>
            <span class="value score" :class="getScoreClass(reviewDetail?.complianceScore)">
              {{ formatComplianceScore(reviewDetail?.complianceScore) }}
            </span>
            <span class="score-level" :style="{ color: getScoreLevel(reviewDetail?.complianceScore).color }">
              ({{ getScoreLevel(reviewDetail?.complianceScore).level }})
            </span>
          </div>
          
          <div class="info-item" v-if="reviewDetail?.lastReviewedAt">
            <span class="label">最后审核时间：</span>
            <span class="value">{{ formatDateTime(reviewDetail?.lastReviewedAt) }}</span>
          </div>
          
          <div class="info-item" v-if="reviewDetail?.reviewerName">
            <span class="label">审核人员：</span>
            <span class="value">{{ reviewDetail?.reviewerName }}</span>
          </div>
        </div>
      </div>

      <!-- 审核评分详情 -->
      <div class="review-detail-scores" v-if="hasScoreDetails">
        <div class="section-title">
          <van-icon name="star-o" size="16" color="#1989fa" />
          <span>详细评分</span>
        </div>
        
        <div class="scores-grid">
          <div class="score-item" v-if="reviewDetail?.overallRating">
            <div class="score-name">综合评级</div>
            <div class="score-value overall">{{ getOverallRatingText(reviewDetail.overallRating) }}</div>
            <van-progress 
              :percentage="25 * (5 - reviewDetail.overallRating)" 
              color="#1989fa"
              track-color="#f2f3f5"
            />
          </div>
          
          <div class="score-item" v-if="reviewDetail?.photoQualityScore">
            <div class="score-name">照片质量</div>
            <div class="score-value">{{ reviewDetail.photoQualityScore }}分</div>
            <van-progress 
              :percentage="(reviewDetail.photoQualityScore / 10) * 100" 
              color="#07c160"
              track-color="#f2f3f5"
            />
          </div>
          
          <div class="score-item" v-if="reviewDetail?.descriptionAccuracyScore">
            <div class="score-name">描述准确性</div>
            <div class="score-value">{{ reviewDetail.descriptionAccuracyScore }}分</div>
            <van-progress 
              :percentage="(reviewDetail.descriptionAccuracyScore / 10) * 100" 
              color="#ff976a"
              track-color="#f2f3f5"
            />
          </div>
          
          <div class="score-item" v-if="reviewDetail?.facilityCompletenessScore">
            <div class="score-name">设施完整性</div>
            <div class="score-value">{{ reviewDetail.facilityCompletenessScore }}分</div>
            <van-progress 
              :percentage="(reviewDetail.facilityCompletenessScore / 10) * 100" 
              color="#ee0a24"
              track-color="#f2f3f5"
            />
          </div>
          
          <div class="score-item" v-if="reviewDetail?.locationAccuracyScore">
            <div class="score-name">位置准确性</div>
            <div class="score-value">{{ reviewDetail.locationAccuracyScore }}分</div>
            <van-progress 
              :percentage="(reviewDetail.locationAccuracyScore / 10) * 100" 
              color="#7232dd"
              track-color="#f2f3f5"
            />
          </div>
        </div>
      </div>

      <!-- 审核意见和建议 -->
      <div class="review-detail-comments" v-if="reviewDetail?.reviewComments || reviewDetail?.improvementSuggestion">
        <div class="section-title">
          <van-icon name="comment-o" size="16" color="#1989fa" />
          <span>审核意见</span>
        </div>
        
        <div class="comments-content">
          <div class="comment-item" v-if="reviewDetail?.reviewComments">
            <div class="comment-label">审核意见：</div>
            <div class="comment-text">{{ reviewDetail.reviewComments }}</div>
          </div>
          
          <div class="comment-item" v-if="reviewDetail?.improvementSuggestion">
            <div class="comment-label">改进建议：</div>
            <div class="comment-text suggestion">{{ reviewDetail.improvementSuggestion }}</div>
          </div>
        </div>
      </div>

      <!-- 审核历史记录 -->
      <div class="review-detail-history" v-if="reviewHistory.length > 0">
        <div class="section-title">
          <van-icon name="clock-o" size="16" color="#1989fa" />
          <span>审核历史</span>
        </div>
        
        <div class="history-list">
          <div 
            v-for="(history, index) in reviewHistory" 
            :key="history.id"
            class="history-item"
          >
            <div class="history-header">
              <div class="history-time">{{ formatDateTime(history.reviewedAt) }}</div>
              <div class="history-reviewer">审核员{{ history.reviewerId }}</div>
            </div>
            
            <div class="history-content">
              <div class="history-rating">
                <span class="rating-label">综合评级：</span>
                <span class="rating-value">{{ getOverallRatingText(history.overallRating) }}</span>
              </div>
              
              <div class="history-comments" v-if="history.reviewComments">
                {{ history.reviewComments }}
              </div>
            </div>
            
            <div class="history-divider" v-if="index < reviewHistory.length - 1"></div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="review-detail-actions" v-if="reviewDetail?.reviewStatus === 2 && reviewDetail?.roomInfo?.isRelease === 0">
        <van-button 
          type="primary" 
          size="large" 
          block
          @click="handleQuickPublish"
        >
          <van-icon name="fire-o" />
          快捷发布房源
        </van-button>
      </div>
    </PageContainer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { useUserStore } from '@/store/modules/user';
import { NavigationHelper } from '@/utils/navigationHelper';
import { 
  getReviewDetail,
  getReviewHistory,
  getReviewStatusConfig,
  formatComplianceScore,
  getScoreLevel,
  getOverallRatingText
} from '@/api/landlord/review';
import type { ReviewDetail, RoomReview } from '@/api/landlord/review';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const reviewDetail = ref<ReviewDetail | null>(null);
const reviewHistory = ref<RoomReview[]>([]);

// 计算属性
const hasScoreDetails = computed(() => {
  return reviewDetail.value && (
    reviewDetail.value.overallRating ||
    reviewDetail.value.photoQualityScore ||
    reviewDetail.value.descriptionAccuracyScore ||
    reviewDetail.value.facilityCompletenessScore ||
    reviewDetail.value.locationAccuracyScore
  );
});

// 状态配置
const getStatusConfig = (status: number | undefined) => {
  return getReviewStatusConfig(status || 0);
};

// 格式化函数
const formatDateTime = (timeString: string) => {
  const date = new Date(timeString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
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

// 加载审核详情
const loadReviewDetail = async () => {
  try {
    const roomId = Number(route.params.roomId);
    const landlordId = userStore.userInfo?.id;
    
    if (!roomId || !landlordId) {
      showToast('参数错误');
      router.back();
      return;
    }
    
    const { data } = await getReviewDetail(roomId, landlordId);
    reviewDetail.value = data;
  } catch (error) {
    console.error('加载审核详情失败:', error);
    showToast('加载审核详情失败');
  }
};

// 加载审核历史
const loadReviewHistory = async () => {
  try {
    const roomId = Number(route.params.roomId);
    const landlordId = userStore.userInfo?.id;
    
    if (!roomId || !landlordId) return;
    
    const { data } = await getReviewHistory(roomId, landlordId);
    reviewHistory.value = data;
  } catch (error) {
    console.error('加载审核历史失败:', error);
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    await Promise.all([
      loadReviewDetail(),
      loadReviewHistory()
    ]);
  } catch (error) {
    console.error('初始化数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 快捷发布房源
const handleQuickPublish = async () => {
  try {
    await showConfirmDialog({
      title: '快捷发布房源',
      message: '确定要发布这个房源吗？发布后租客就可以看到并预约了。',
      confirmButtonText: '确认发布',
      cancelButtonText: '取消'
    });
    
    // 这里应该调用发布房源的API
    showToast('发布功能开发中');
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('发布房源失败:', error);
      showToast('发布失败');
    }
  }
};

// 返回处理
const handleGoBack = () => {
  console.log('🧭 审核详情页面返回按钮被点击');
  NavigationHelper.smartGoBack(router);
};

// 生命周期
onMounted(() => {
  initData();
});
</script>

<style scoped lang="less">
.review-detail-info {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  
  .info-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    
    .room-title {
      font-size: 18px;
      font-weight: 600;
      color: #323233;
      margin: 0;
    }
  }
  
  .info-content {
    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .label {
        font-size: 14px;
        color: #969799;
        margin-right: 8px;
        min-width: 80px;
      }
      
      .value {
        font-size: 14px;
        color: #323233;
        font-weight: 500;
        
        &.rent {
          color: #ee0a24;
          font-weight: 600;
        }
        
        &.score {
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
      
      .score-level {
        font-size: 12px;
        font-weight: 500;
        margin-left: 8px;
      }
    }
  }
}

.review-detail-scores {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  
  .scores-grid {
    .score-item {
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .score-name {
        font-size: 14px;
        color: #646566;
        margin-bottom: 8px;
      }
      
      .score-value {
        font-size: 16px;
        font-weight: 600;
        color: #323233;
        margin-bottom: 8px;
        
        &.overall {
          color: #1989fa;
          font-size: 18px;
        }
      }
    }
  }
}

.review-detail-comments {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  
  .comments-content {
    .comment-item {
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .comment-label {
        font-size: 14px;
        color: #646566;
        margin-bottom: 8px;
        font-weight: 500;
      }
      
      .comment-text {
        font-size: 14px;
        color: #323233;
        line-height: 1.6;
        padding: 12px;
        background: #f7f8fa;
        border-radius: 6px;
        
        &.suggestion {
          background: #e6f7ff;
          color: #1989fa;
        }
      }
    }
  }
}

.review-detail-history {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  
  .history-list {
    .history-item {
      position: relative;
      
      .history-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;
        
        .history-time {
          font-size: 14px;
          color: #646566;
        }
        
        .history-reviewer {
          font-size: 12px;
          color: #969799;
        }
      }
      
      .history-content {
        .history-rating {
          margin-bottom: 8px;
          
          .rating-label {
            font-size: 13px;
            color: #969799;
          }
          
          .rating-value {
            font-size: 13px;
            color: #1989fa;
            font-weight: 500;
          }
        }
        
        .history-comments {
          font-size: 13px;
          color: #646566;
          line-height: 1.5;
          padding: 8px 12px;
          background: #f7f8fa;
          border-radius: 4px;
        }
      }
      
      .history-divider {
        height: 1px;
        background: #ebedf0;
        margin: 16px 0;
      }
    }
  }
}

.review-detail-actions {
  padding: 16px;
  background: white;
  border-radius: 8px;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 16px;
}

// 响应式设计
@media (max-width: 375px) {
  .review-detail-info {
    padding: 12px;
    
    .info-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
      
      .room-title {
        font-size: 16px;
      }
    }
    
    .info-content {
      .info-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
        
        .label {
          min-width: auto;
        }
      }
    }
  }
  
  .review-detail-scores,
  .review-detail-comments,
  .review-detail-history {
    padding: 12px;
  }
}
</style>