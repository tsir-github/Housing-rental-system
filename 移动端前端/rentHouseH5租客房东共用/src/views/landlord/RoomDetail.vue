<template>
  <div class="room-detail">
    <!-- 导航栏 -->
    <van-nav-bar
      title="房源详情"
      left-arrow
      @click-left="handleGoBack"
      fixed
      placeholder
    />
    
    <PageContainer :loading="loading" :show-empty="showEmpty" :show-header="false" empty-type="error">
      <!-- 1. 顶部图片区域 -->
      <div class="image-section">
        <div v-if="roomImages.length > 0" class="image-container">
          <van-swipe :autoplay="3000" indicator-color="white">
            <van-swipe-item v-for="(image, index) in roomImages" :key="index">
              <van-image 
                :src="image.url" 
                :alt="image.name" 
                class="room-image"
                fit="cover"
                width="100%"
                height="250px"
              />
            </van-swipe-item>
          </van-swipe>
        </div>
        <div v-else class="no-image">
          <van-icon name="photo" size="40" color="#ddd" />
          <p>暂无房间图片</p>
        </div>
      </div>

      <!-- 2. 房间核心信息区域 -->
      <div class="room-core-info">
        <div class="room-header">
          <div class="room-title">
            <h1>房间号：{{ roomDetail?.roomNumber }}</h1>
            <div class="room-status">
              <ReviewStatusBadge :status="roomDetail?.reviewStatus || 0" size="small" />
              <van-tag :type="roomDetail?.isRelease === 1 ? 'success' : 'default'" size="small" round>
                {{ roomDetail?.isRelease === 1 ? '已发布' : '未发布' }}
              </van-tag>
            </div>
          </div>
          <div class="room-price">
            <span class="price-amount">¥{{ roomDetail?.rent }}</span>
            <span class="price-unit">/月</span>
          </div>
        </div>

        <div class="room-attrs" v-if="roomDetail?.attrValueVoList?.length">
          <div class="attr-grid">
            <div v-for="attr in roomDetail.attrValueVoList" :key="attr.id" class="attr-item">
              <span class="attr-label">{{ attr.attrKeyName }}：</span>
              <span class="attr-value">{{ attr.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. 房间配套设施 -->
      <div class="room-facilities" v-if="roomDetail?.facilityInfoList?.length">
        <h3 class="section-title">🏠 房间设施</h3>
        <div class="facility-grid">
          <div v-for="facility in roomDetail.facilityInfoList" :key="facility.id" class="facility-item">
            <van-icon :name="facility.icon" size="18" />
            <span>{{ facility.name }}</span>
          </div>
        </div>
      </div>

      <!-- 4. 房间标签 -->
      <div class="room-labels" v-if="displayLabels.length">
        <h3 class="section-title">🏷️ 房间特色</h3>
        <div class="label-list">
          <van-tag v-for="label in displayLabels" :key="label.id" :type="getLabelType(label)" size="medium">
            {{ label.name }}
          </van-tag>
        </div>
      </div>

      <!-- 5. 租赁信息 -->
      <div class="rental-info">
        <!-- 支付方式 -->
        <div class="payment-section" v-if="roomDetail?.paymentTypeList?.length">
          <h3 class="section-title">💳 支付方式</h3>
          <div class="payment-list">
            <div v-for="payment in roomDetail.paymentTypeList" :key="payment.id" class="payment-item">
              <div class="payment-header">
                <span class="payment-name">{{ payment.name }}</span>
                <span class="payment-period">{{ payment.payMonthCount }}个月</span>
              </div>
              <div v-if="payment.additionalInfo" class="payment-desc">{{ payment.additionalInfo }}</div>
            </div>
          </div>
        </div>

        <!-- 可选租期 -->
        <div class="lease-section" v-if="roomDetail?.leaseTermList?.length">
          <h3 class="section-title">📅 可选租期</h3>
          <div class="lease-list">
            <div v-for="term in roomDetail.leaseTermList" :key="term.id" class="lease-item">
              <span class="lease-count">{{ term.monthCount }}</span>
              <span class="lease-unit">{{ term.unit }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 6. 公寓信息区域 -->
      <div class="apartment-info" v-if="roomDetail?.apartmentItemVo">
        <h3 class="section-title">🏢 所属公寓：{{ roomDetail.apartmentItemVo.name }}</h3>
        
        <div class="apartment-basic">
          <div class="apartment-address">
            <van-icon name="location-o" size="16" />
            <span>{{ fullAddress }}</span>
          </div>
          <div class="apartment-phone" v-if="roomDetail.apartmentItemVo.phone">
            <van-icon name="phone-o" size="16" />
            <span>{{ roomDetail.apartmentItemVo.phone }}</span>
          </div>
        </div>

        <!-- 公寓标签 -->
        <div class="apartment-labels" v-if="roomDetail.apartmentItemVo.labelInfoList?.length">
          <h4 class="subsection-title">🏷️ 公寓标签</h4>
          <div class="label-list">
            <van-tag v-for="label in roomDetail.apartmentItemVo.labelInfoList" :key="label.id" type="success" size="medium">
              {{ label.name }}
            </van-tag>
          </div>
        </div>

        <!-- 公寓配套 -->
        <div class="apartment-facilities" v-if="roomDetail.apartmentItemVo.facilityInfoList?.length">
          <h4 class="subsection-title">🛋️ 公寓配套</h4>
          <div class="facility-grid">
            <div v-for="facility in roomDetail.apartmentItemVo.facilityInfoList" :key="facility.id" class="facility-item">
              <van-icon :name="facility.icon" size="16" />
              <span>{{ facility.name }}</span>
            </div>
          </div>
        </div>

        <!-- 公寓杂费 -->
        <div class="apartment-fees" v-if="roomDetail.apartmentItemVo.feeValueVoList?.length">
          <h4 class="subsection-title">💰 公寓杂费</h4>
          <div class="fee-list">
            <div v-for="fee in roomDetail.apartmentItemVo.feeValueVoList" :key="fee.id" class="fee-item">
              <span class="fee-name">{{ fee.feeKeyName }}：</span>
              <span class="fee-value">{{ fee.name }}{{ fee.unit }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 7. 审核信息区域（房东查看时显示） -->
      <div class="review-info" v-if="roomDetail?.reviewDetail">
        <h3 class="section-title">{{ getReviewStatusText() }}</h3>
        
        <div class="review-overview">
          <div class="score-main">
            <div class="compliance-score">
              <span class="score-label">📊 合规性评分：</span>
              <span class="score-value">{{ roomDetail.reviewDetail.complianceScore }}分</span>
            </div>
            <div class="review-time" v-if="roomDetail.reviewDetail.reviewedAt">
              <span class="time-label">⏰ 审核时间：</span>
              <span class="time-value">{{ formatDate(roomDetail.reviewDetail.reviewedAt) }}</span>
            </div>
            <div class="reviewer">
              <span class="reviewer-label">👤 审核员：</span>
              <span class="reviewer-value">{{ roomDetail.reviewDetail.reviewerName || '管理员' }}</span>
            </div>
          </div>
        </div>

        <div class="review-comments" v-if="roomDetail.reviewDetail.reviewComments">
          <h4 class="subsection-title">📝 审核意见</h4>
          <div class="comment-content">{{ roomDetail.reviewDetail.reviewComments }}</div>
        </div>

        <div class="detailed-scores" v-if="hasDetailedScores()">
          <h4 class="subsection-title">📈 详细评分</h4>
          <div class="score-grid">
            <div class="score-item" v-if="roomDetail.reviewDetail.photoQualityScore">
              <span class="score-name">照片质量：</span>
              <span class="score-points">{{ roomDetail.reviewDetail.photoQualityScore }}分</span>
            </div>
            <div class="score-item" v-if="roomDetail.reviewDetail.descriptionAccuracyScore">
              <span class="score-name">描述准确性：</span>
              <span class="score-points">{{ roomDetail.reviewDetail.descriptionAccuracyScore }}分</span>
            </div>
            <div class="score-item" v-if="roomDetail.reviewDetail.facilityCompletenessScore">
              <span class="score-name">设施完整性：</span>
              <span class="score-points">{{ roomDetail.reviewDetail.facilityCompletenessScore }}分</span>
            </div>
            <div class="score-item" v-if="roomDetail.reviewDetail.locationAccuracyScore">
              <span class="score-name">位置准确性：</span>
              <span class="score-points">{{ roomDetail.reviewDetail.locationAccuracyScore }}分</span>
            </div>
          </div>
        </div>

        <!-- 拒绝原因（如果被拒绝） -->
        <div class="rejection-reason" v-if="roomDetail.reviewStatus === 3 && roomDetail.reviewDetail.improvementSuggestion">
          <h4 class="subsection-title">❌ 拒绝原因</h4>
          <div class="rejection-content">{{ roomDetail.reviewDetail.improvementSuggestion }}</div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <van-button type="default" size="large" @click="handleEdit" class="action-btn">
          编辑房源
        </van-button>
        
        <van-button 
          v-if="roomDetail?.reviewStatus === 2 && roomDetail?.isRelease === 0"
          type="primary" 
          size="large"
          @click="handlePublish"
          :loading="publishing"
          class="action-btn"
        >
          发布房源
        </van-button>
        
        <van-button 
          v-if="roomDetail?.isRelease === 1"
          type="warning" 
          size="large"
          @click="handleUnpublish"
          :loading="publishing"
          class="action-btn"
        >
          下架房源
        </van-button>
        
        <van-button 
          v-if="roomDetail?.reviewStatus === 3"
          type="primary" 
          size="large"
          @click="handleResubmit"
          :loading="resubmitting"
          class="action-btn"
        >
          重新提交
        </van-button>
      </div>

      <!-- 危险操作 -->
      <div class="danger-zone">
        <van-button 
          type="danger" 
          size="large"
          plain
          @click="handleDelete"
          :loading="deleting"
        >
          删除房源
        </van-button>
      </div>
    </PageContainer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { useUserStore } from '@/store/modules/user';
import {
  getRoomDetail,
  updateRoomReleaseStatus,
  resubmitForReview,
  deleteRoom
} from '@/api/landlord';
import type { RoomDetailVo } from '@/api/landlord';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import ReviewStatusBadge from '@/components/landlord/ReviewStatusBadge/ReviewStatusBadge.vue';
import { NavigationHelper } from '@/utils/navigationHelper';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const publishing = ref(false);
const resubmitting = ref(false);
const deleting = ref(false);
const roomDetail = ref<RoomDetailVo | null>(null);

// 计算属性
const showEmpty = computed(() => !loading.value && !roomDetail.value);

// 计算房间图片：优先显示房间图片，如果没有则显示公寓图片
const roomImages = computed(() => {
  if (!roomDetail.value) return [];
  
  // 优先使用房间级别的图片
  if (roomDetail.value.graphVoList && roomDetail.value.graphVoList.length > 0) {
    return roomDetail.value.graphVoList;
  }
  
  // 如果房间没有图片，使用公寓级别的图片
  if (roomDetail.value.apartmentItemVo?.graphVoList && roomDetail.value.apartmentItemVo.graphVoList.length > 0) {
    return roomDetail.value.apartmentItemVo.graphVoList;
  }
  
  return [];
});

// 智能显示标签：优先房间标签，如果没有则显示公寓标签
const displayLabels = computed(() => {
  if (!roomDetail.value) return [];
  
  // 如果房间有标签，优先显示房间标签
  if (roomDetail.value.labelInfoList && roomDetail.value.labelInfoList.length > 0) {
    return roomDetail.value.labelInfoList;
  }
  
  // 如果房间没有标签，显示公寓标签
  if (roomDetail.value.apartmentItemVo?.labelInfoList && roomDetail.value.apartmentItemVo.labelInfoList.length > 0) {
    return roomDetail.value.apartmentItemVo.labelInfoList;
  }
  
  return [];
});

// 获取标签类型
const getLabelType = (label: any) => {
  // 如果是房间级别的标签，使用primary类型
  if (roomDetail.value?.labelInfoList?.some(l => l.id === label.id)) {
    return 'primary';
  }
  // 如果是公寓级别的标签，使用success类型
  return 'success';
};

const locationText = computed(() => {
  if (!roomDetail.value?.apartmentItemVo) return '';
  const { provinceName, cityName, districtName, addressDetail } = roomDetail.value.apartmentItemVo;
  return `${provinceName} ${cityName} ${districtName} ${addressDetail}`;
});

const fullAddress = computed(() => {
  if (!roomDetail.value?.apartmentItemVo) return '';
  const { provinceName, cityName, districtName, addressDetail } = roomDetail.value.apartmentItemVo;
  return `${provinceName} ${cityName} ${districtName} ${addressDetail}`;
});

// 获取审核状态文本
const getReviewStatusText = () => {
  if (!roomDetail.value) return '';
  
  switch (roomDetail.value.reviewStatus) {
    case 1:
      return '⏳ 审核状态：待审核';
    case 2:
      return '✅ 审核状态：已通过';
    case 3:
      return '❌ 审核状态：已拒绝';
    default:
      return '📋 审核状态：未知';
  }
};

// 检查是否有详细评分
const hasDetailedScores = () => {
  if (!roomDetail.value?.reviewDetail) return false;
  
  return !!(
    roomDetail.value.reviewDetail.photoQualityScore ||
    roomDetail.value.reviewDetail.descriptionAccuracyScore ||
    roomDetail.value.reviewDetail.facilityCompletenessScore ||
    roomDetail.value.reviewDetail.locationAccuracyScore
  );
};

// 格式化日期
const formatDate = (dateString: string) => {
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

// 加载房源详情
const loadRoomDetail = async () => {
  try {
    loading.value = true;
    const roomId = Number(route.params.id);
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    const { data } = await getRoomDetail(roomId, landlordId);
    roomDetail.value = data;
    
  } catch (error) {
    console.error('加载房源详情失败:', error);
    showToast('加载房源详情失败');
  } finally {
    loading.value = false;
  }
};

// 事件处理
const handleViewApartment = () => {
  if (roomDetail.value?.apartmentItemVo?.id) {
    router.push(`/landlord/apartment/${roomDetail.value.apartmentItemVo.id}`);
  }
};

const handleEdit = () => {
  router.push(`/landlord/rooms/edit/${route.params.id}`);
};

const handlePublish = async () => {
  try {
    await showConfirmDialog({
      title: '确认发布',
      message: '确定要发布这个房源吗？发布后租客可以看到此房源。'
    });
    
    publishing.value = true;
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId || !roomDetail.value) {
      showToast('操作失败');
      return;
    }
    
    await updateRoomReleaseStatus(roomDetail.value.id!, 1, landlordId);
    roomDetail.value.isRelease = 1;
    showToast('发布成功');
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('发布房源失败:', error);
      showToast('发布失败');
    }
  } finally {
    publishing.value = false;
  }
};

const handleUnpublish = async () => {
  try {
    await showConfirmDialog({
      title: '确认下架',
      message: '确定要下架这个房源吗？下架后租客将无法看到此房源。'
    });
    
    publishing.value = true;
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId || !roomDetail.value) {
      showToast('操作失败');
      return;
    }
    
    await updateRoomReleaseStatus(roomDetail.value.id!, 0, landlordId);
    roomDetail.value.isRelease = 0;
    showToast('下架成功');
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('下架房源失败:', error);
      showToast('下架失败');
    }
  } finally {
    publishing.value = false;
  }
};

const handleResubmit = async () => {
  try {
    await showConfirmDialog({
      title: '确认重新提交',
      message: '确定要重新提交审核吗？请确保已根据审核建议进行了修改。'
    });
    
    resubmitting.value = true;
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId || !roomDetail.value) {
      showToast('操作失败');
      return;
    }
    
    await resubmitForReview(roomDetail.value.id!, landlordId);
    roomDetail.value.reviewStatus = 1; // 重新设为待审核状态
    showToast('重新提交成功');
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('重新提交失败:', error);
      showToast('重新提交失败');
    }
  } finally {
    resubmitting.value = false;
  }
};

const handleDelete = async () => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除房源"${roomDetail.value?.roomNumber}"吗？此操作不可恢复。`,
      confirmButtonText: '删除',
      confirmButtonColor: '#ee0a24'
    });
    
    deleting.value = true;
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId || !roomDetail.value) {
      showToast('操作失败');
      return;
    }
    
    await deleteRoom(roomDetail.value.id!, landlordId);
    showToast('删除成功');
    router.back();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除房源失败:', error);
      showToast('删除失败');
    }
  } finally {
    deleting.value = false;
  }
};

const handleGoBack = () => {
  console.log('🧭 房源详情页面返回按钮被点击');
  NavigationHelper.smartGoBack(router);
};

// 生命周期
onMounted(() => {
  loadRoomDetail();
});
</script>

<style scoped lang="less">
.room-detail {
  background-color: #f7f8fa;
  min-height: 100vh;
}

// 1. 图片区域样式
.image-section {
  width: 100%;
  height: 250px;
  background-color: #fff;
}

.room-image {
  width: 100%;
  height: 250px;
  object-fit: cover;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 250px;
  background-color: #f5f5f5;
  color: #999;
  
  p {
    margin-top: 8px;
    font-size: 14px;
  }
}

// 2. 房间核心信息区域
.room-core-info {
  background: white;
  padding: 20px;
  margin: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.room-title {
  flex: 1;
  
  h1 {
    font-size: 20px;
    font-weight: 700;
    color: #323233;
    margin: 0 0 8px 0;
    line-height: 1.3;
  }
}

.room-status {
  display: flex;
  gap: 8px;
  align-items: center;
}

.room-price {
  text-align: right;
  
  .price-amount {
    font-size: 24px;
    font-weight: 700;
    color: #ee0a24;
    display: block;
  }
  
  .price-unit {
    font-size: 14px;
    color: #969799;
  }
}

.room-attrs {
  .attr-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .attr-item {
    display: flex;
    align-items: center;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 8px;
    
    .attr-label {
      font-size: 14px;
      color: #646566;
      font-weight: 500;
    }
    
    .attr-value {
      font-size: 14px;
      color: #323233;
      font-weight: 600;
    }
  }
}

// 通用区块样式
.room-facilities,
.room-labels,
.rental-info,
.apartment-info,
.review-info {
  background: white;
  margin: 16px;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

// 标题样式
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #323233;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
}

.subsection-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin: 16px 0 12px 0;
  
  &:first-child {
    margin-top: 0;
  }
}

// 3. 房间设施样式
.facility-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.facility-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 14px;
  color: #323233;
  transition: all 0.3s ease;
  
  &:hover {
    background: #e8f3ff;
    color: #1989fa;
  }
}

// 4. 标签样式
.label-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

// 5. 租赁信息样式
.rental-info {
  .payment-section,
  .lease-section {
    &:not(:last-child) {
      margin-bottom: 24px;
      padding-bottom: 20px;
      border-bottom: 1px solid #ebedf0;
    }
  }
}

.payment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 10px;
  border-left: 4px solid #1989fa;
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.payment-name {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}

.payment-period {
  font-size: 14px;
  color: #1989fa;
  background: #e8f3ff;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.payment-desc {
  font-size: 14px;
  color: #646566;
  line-height: 1.4;
}

.lease-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 12px;
}

.lease-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 12px;
  background: #f8f9fa;
  border-radius: 10px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    border-color: #1989fa;
    background: #e8f3ff;
  }
}

.lease-count {
  font-size: 24px;
  font-weight: 700;
  color: #1989fa;
  line-height: 1;
}

.lease-unit {
  font-size: 14px;
  color: #646566;
  margin-top: 4px;
}

// 6. 公寓信息样式
.apartment-basic {
  margin-bottom: 20px;
  
  .apartment-address,
  .apartment-phone {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    font-size: 14px;
    color: #646566;
  }
}

.fee-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  
  .fee-name {
    font-size: 14px;
    color: #323233;
    font-weight: 500;
  }
  
  .fee-value {
    font-size: 14px;
    font-weight: 600;
    color: #ee0a24;
  }
}

// 7. 审核信息样式
.review-overview {
  margin-bottom: 20px;
}

.score-main {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .compliance-score,
  .review-time,
  .reviewer {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: #f8f9fa;
    border-radius: 8px;
    
    .score-label,
    .time-label,
    .reviewer-label {
      font-size: 14px;
      color: #646566;
      font-weight: 500;
    }
    
    .score-value {
      font-size: 16px;
      font-weight: 700;
      color: #1989fa;
    }
    
    .time-value,
    .reviewer-value {
      font-size: 14px;
      color: #323233;
      font-weight: 600;
    }
  }
}

.review-comments {
  margin-bottom: 20px;
  
  .comment-content {
    padding: 16px;
    background: #f0f9ff;
    border-radius: 8px;
    border-left: 4px solid #1989fa;
    font-size: 14px;
    color: #323233;
    line-height: 1.5;
  }
}

.detailed-scores {
  .score-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .score-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 12px;
    background: #f8f9fa;
    border-radius: 8px;
    text-align: center;
    
    .score-name {
      font-size: 12px;
      color: #646566;
      margin-bottom: 4px;
    }
    
    .score-points {
      font-size: 18px;
      font-weight: 700;
      color: #1989fa;
    }
  }
}

// 拒绝原因样式
.rejection-reason {
  margin-bottom: 20px;
  
  .rejection-content {
    padding: 16px;
    background: #fef2f2;
    border-radius: 8px;
    border-left: 4px solid #ee0a24;
    font-size: 14px;
    color: #ee0a24;
    line-height: 1.5;
  }
}

// 操作按钮样式
.action-buttons {
  display: flex;
  gap: 12px;
  margin: 16px;
  
  .action-btn {
    flex: 1;
    height: 48px;
    border-radius: 8px;
    font-weight: 600;
  }
}

.danger-zone {
  display: flex;
  justify-content: center;
  margin: 16px;
  margin-bottom: 32px;
  
  .van-button {
    height: 44px;
    border-radius: 8px;
    font-weight: 500;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .room-core-info,
  .room-facilities,
  .room-labels,
  .rental-info,
  .apartment-info,
  .review-info {
    margin: 12px;
    padding: 16px;
  }
  
  .room-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .room-price {
    text-align: left;
  }
  
  .attr-grid {
    grid-template-columns: 1fr !important;
  }
  
  .facility-grid {
    grid-template-columns: 1fr !important;
  }
  
  .score-grid {
    grid-template-columns: 1fr !important;
  }
  
  .action-buttons {
    flex-direction: column;
    margin: 12px;
  }
}

@media (max-width: 375px) {
  .lease-list {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .section-title {
    font-size: 16px;
  }
  
  .room-title h1 {
    font-size: 18px;
  }
  
  .room-price .price-amount {
    font-size: 20px;
  }
}
</style>