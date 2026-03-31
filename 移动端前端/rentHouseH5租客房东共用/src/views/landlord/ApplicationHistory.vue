<template>
  <div class="application-history">
    <!-- 导航栏 -->
    <van-nav-bar
      title="直接入住申请历史"
      left-arrow
      @click-left="handleGoBack"
      fixed
      placeholder
    />
    
    <div class="history-container">
      <!-- 加载状态 -->
      <van-loading v-if="loading" class="loading-center" vertical>
        <template #icon>
          <van-icon name="spinner" spin />
        </template>
        加载中...
      </van-loading>

      <!-- 空状态 -->
      <van-empty 
        v-else-if="applicationHistory.length === 0" 
        image="search" 
        description="暂无申请历史"
      />

      <!-- 申请历史列表 -->
      <div v-else class="history-list">
        <div 
          v-for="application in applicationHistory" 
          :key="application.id"
          class="history-item"
          @click="handleViewDetail(application.id)"
        >
          <!-- 申请基本信息 -->
          <div class="item-header">
            <div class="applicant-info">
              <span class="name">{{ application.name }}</span>
              <span class="phone">{{ application.phone }}</span>
            </div>
            <div class="status-badge">
              <van-tag 
                :type="getStatusType(application.status)"
                size="medium"
              >
                {{ getStatusText(application.status) }}
              </van-tag>
            </div>
          </div>

          <!-- 房源信息 -->
          <div class="room-info">
            <div class="room-details">
              <span class="apartment-name">{{ application.apartmentName || '公寓名称' }}</span>
              <span class="room-number">{{ application.roomNumber || application.roomId }}号房</span>
            </div>
            <div class="rent-info">
              <span class="rent">¥{{ application.rent }}/月</span>
            </div>
          </div>

          <!-- 租期信息 -->
          <div class="lease-info">
            <div class="lease-period">
              <van-icon name="calendar-o" />
              <span>{{ formatDate(application.leaseStartDate) }} - {{ formatDate(application.leaseEndDate) }}</span>
            </div>
            <div class="deposit">
              <span>押金: ¥{{ application.deposit }}</span>
            </div>
          </div>

          <!-- 处理信息 -->
          <div class="process-info" v-if="application.processedAt">
            <div class="process-time">
              <van-icon name="clock-o" />
              <span>处理时间: {{ formatDateTime(application.processedAt) }}</span>
            </div>
            <div class="rejection-reason" v-if="application.rejectionReason">
              <span class="reason-label">拒绝原因:</span>
              <span class="reason-text">{{ application.rejectionReason }}</span>
            </div>
          </div>

          <!-- 申请时间 -->
          <div class="create-time">
            <span>申请时间: {{ formatDateTime(application.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { getApplicationHistory } from '@/api/landlord/application';
import type { DirectMoveInApplicationDetailVO } from '@/api/landlord/application';
import { NavigationHelper } from '@/utils/navigationHelper';
import { useUserStore } from '@/store/modules/user';

const router = useRouter();
const userStore = useUserStore();

// 状态管理
const loading = ref(false);
const applicationHistory = ref<DirectMoveInApplicationDetailVO[]>([]);

// 加载申请历史
const loadApplicationHistory = async () => {
  try {
    loading.value = true;
    
    // 获取当前房东ID
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    const response = await getApplicationHistory(landlordId);
    applicationHistory.value = response.data || [];
    console.log('申请历史加载成功:', applicationHistory.value);
  } catch (error) {
    console.error('加载申请历史失败:', error);
    showToast('加载失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 获取状态类型
const getStatusType = (status: number) => {
  switch (status) {
    case 1: // PENDING
      return 'warning';
    case 2: // APPROVED
      return 'success';
    case 3: // REJECTED
      return 'danger';
    default:
      return 'default';
  }
};

// 获取状态文本
const getStatusText = (status: number) => {
  switch (status) {
    case 1:
      return '待处理';
    case 2:
      return '已通过';
    case 3:
      return '已拒绝';
    default:
      return '未知状态';
  }
};

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${formatDate(dateString)} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 查看详情
const handleViewDetail = (applicationId: number) => {
  router.push(`/landlord/applications/${applicationId}`);
};

// 返回处理
const handleGoBack = () => {
  NavigationHelper.goBackFromDetail(router, 'application');
};

// 生命周期
onMounted(() => {
  loadApplicationHistory();
});
</script>

<style scoped>
.application-history {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.history-container {
  padding: 16px;
}

.loading-center {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-item:active {
  transform: scale(0.98);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.applicant-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.phone {
  font-size: 14px;
  color: #666;
}

.status-badge {
  flex-shrink: 0;
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.room-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.apartment-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.room-number {
  font-size: 13px;
  color: #666;
}

.rent-info {
  text-align: right;
}

.rent {
  font-size: 16px;
  font-weight: 600;
  color: #ff6b35;
}

.lease-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
  color: #666;
}

.lease-period {
  display: flex;
  align-items: center;
  gap: 4px;
}

.process-info {
  margin-bottom: 12px;
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
}

.process-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.rejection-reason {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reason-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.reason-text {
  font-size: 13px;
  color: #ff4444;
  background: #fff5f5;
  padding: 8px;
  border-radius: 6px;
  border-left: 3px solid #ff4444;
}

.create-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}
</style>