<template>
  <div class="expiring-leases">
    <!-- 导航栏 -->
    <van-nav-bar 
      title="即将到期租约" 
      left-arrow 
      @click-left="handleGoBack"
      fixed
      placeholder
    />

    <!-- 页面内容 -->
    <div class="content">
      <!-- 统计信息 -->
      <div class="stats-card">
        <div class="stat-item">
          <div class="stat-number">{{ expiringLeases.length }}</div>
          <div class="stat-label">即将到期</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ urgentCount }}</div>
          <div class="stat-label">7天内到期</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ warningCount }}</div>
          <div class="stat-label">30天内到期</div>
        </div>
      </div>

      <!-- 租约列表 -->
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <div class="lease-list">
          <div 
            v-for="lease in expiringLeases" 
            :key="lease.id"
            class="lease-card"
            :class="getUrgencyClass(lease.remainingDays)"
            @click="goToDetail(lease.id)"
          >
            <!-- 紧急程度标识 -->
            <div class="urgency-badge">
              <van-tag 
                :color="getUrgencyColor(lease.remainingDays)"
                plain
                size="medium"
              >
                {{ getUrgencyText(lease.remainingDays) }}
              </van-tag>
            </div>

            <!-- 租约信息 -->
            <div class="lease-info">
              <div class="lease-title">
                {{ lease.apartmentInfo.name }} - {{ lease.roomInfo.roomNumber }}
              </div>
              <div class="lease-tenant">
                <van-icon name="contact" size="14" />
                {{ lease.name }} - {{ lease.phone }}
              </div>
              <div class="lease-time">
                <div class="time-info">
                  <span class="time-label">到期时间：</span>
                  <span class="time-value">{{ formatDate(lease.leaseEndDate) }}</span>
                </div>
                <div class="remaining-days">
                  剩余 <span class="days">{{ lease.remainingDays }}</span> 天
                </div>
              </div>
              <div class="lease-rent">
                租金：¥{{ lease.rent }}/月 | 押金：¥{{ lease.deposit }}
              </div>
            </div>

            <!-- 快速操作 -->
            <div class="quick-actions">
              <van-button 
                size="small" 
                type="primary" 
                @click.stop="sendReminder(lease.id)"
              >
                发送提醒
              </van-button>
              <van-button 
                size="small" 
                plain 
                @click.stop="initiateRenewal(lease.id)"
              >
                申请续约
              </van-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <van-empty 
          v-if="!loading && expiringLeases.length === 0" 
          description="暂无即将到期的租约"
          image="search"
        />
      </van-pull-refresh>
    </div>

    <!-- 加载状态 -->
    <van-loading v-if="loading" class="loading-overlay" vertical>
      加载中...
    </van-loading>

    <!-- 批量操作栏 -->
    <div v-if="expiringLeases.length > 0" class="batch-actions">
      <van-button 
        type="primary" 
        size="large" 
        @click="batchSendReminders"
        :loading="batchLoading"
      >
        批量发送提醒
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { NavigationHelper } from '@/utils/navigationHelper';
import {
  getExpiringLeases,
  sendExpirationReminder,
  updateLeaseStatus,
  LeaseStatus,
  type LandlordLeaseVo
} from '@/api/landlord/lease';

const router = useRouter();

// 响应式数据
const loading = ref(false);
const refreshing = ref(false);
const batchLoading = ref(false);
const expiringLeases = ref<LandlordLeaseVo[]>([]);

// 计算属性
const urgentCount = computed(() => {
  return expiringLeases.value.filter(lease => lease.remainingDays <= 7).length;
});

const warningCount = computed(() => {
  return expiringLeases.value.filter(lease => lease.remainingDays <= 30).length;
});

// 加载即将到期的租约
const loadExpiringLeases = async () => {
  try {
    loading.value = true;
    const response = await getExpiringLeases(30); // 30天内到期
    expiringLeases.value = response.data.sort((a, b) => a.remainingDays - b.remainingDays);
  } catch (error) {
    console.error('加载即将到期租约失败:', error);
    showToast('加载失败，请重试');
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
};

// 下拉刷新
const onRefresh = () => {
  loadExpiringLeases();
};

// 获取紧急程度样式类
const getUrgencyClass = (remainingDays: number): string => {
  if (remainingDays <= 3) return 'urgent';
  if (remainingDays <= 7) return 'warning';
  if (remainingDays <= 15) return 'notice';
  return 'normal';
};

// 获取紧急程度颜色
const getUrgencyColor = (remainingDays: number): string => {
  if (remainingDays <= 3) return '#fa5151';
  if (remainingDays <= 7) return '#ff9500';
  if (remainingDays <= 15) return '#1989fa';
  return '#07c160';
};

// 获取紧急程度文本
const getUrgencyText = (remainingDays: number): string => {
  if (remainingDays <= 3) return '紧急';
  if (remainingDays <= 7) return '警告';
  if (remainingDays <= 15) return '提醒';
  return '正常';
};

// 格式化日期
const formatDate = (dateStr: string): string => {
  return new Date(dateStr).toLocaleDateString();
};

// 发送单个提醒
const sendReminder = async (leaseId: number) => {
  try {
    await sendExpirationReminder(leaseId);
    showToast('提醒发送成功');
  } catch (error) {
    console.error('发送提醒失败:', error);
    showToast('发送提醒失败，请重试');
  }
};

// 申请续约
const initiateRenewal = async (leaseId: number) => {
  try {
    const result = await showConfirmDialog({
      title: '申请续约',
      message: '确定要为此租约申请续约吗？',
    });
    
    if (result === 'confirm') {
      await updateLeaseStatus(leaseId, LeaseStatus.RENEWING);
      showToast('续约申请已提交');
      loadExpiringLeases(); // 重新加载列表
    }
  } catch (error) {
    console.error('申请续约失败:', error);
    showToast('申请续约失败，请重试');
  }
};

// 批量发送提醒
const batchSendReminders = async () => {
  try {
    const result = await showConfirmDialog({
      title: '批量发送提醒',
      message: `确定要向所有${expiringLeases.value.length}个即将到期的租约发送提醒吗？`,
    });
    
    if (result === 'confirm') {
      batchLoading.value = true;
      
      // 并发发送提醒
      const promises = expiringLeases.value.map(lease => 
        sendExpirationReminder(lease.id).catch(error => {
          console.error(`发送提醒失败 (租约ID: ${lease.id}):`, error);
          return null;
        })
      );
      
      const results = await Promise.allSettled(promises);
      const successCount = results.filter(result => result.status === 'fulfilled').length;
      
      showToast(`成功发送${successCount}个提醒`);
    }
  } catch (error) {
    console.error('批量发送提醒失败:', error);
    showToast('批量发送失败，请重试');
  } finally {
    batchLoading.value = false;
  }
};

// 导航到详情页
const goToDetail = (leaseId: number) => {
  router.push({
    name: 'LandlordLeaseDetail',
    params: { id: leaseId }
  });
};

// 返回按钮处理
const handleGoBack = () => {
  console.log('🧭 即将到期租约页面返回按钮被点击');
  NavigationHelper.smartGoBack(router);
};

// 组件挂载时加载数据
onMounted(() => {
  loadExpiringLeases();
});
</script>

<style scoped>
.expiring-leases {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 80px;
}

.content {
  padding: 16px;
}

/* 统计卡片 */
.stats-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #1989fa;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #646566;
}

/* 租约列表 */
.lease-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lease-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s;
  position: relative;
  border-left: 4px solid #ebedf0;
}

.lease-card:active {
  transform: scale(0.98);
}

/* 紧急程度样式 */
.lease-card.urgent {
  border-left-color: #fa5151;
  background: linear-gradient(90deg, #fff5f5 0%, white 10%);
}

.lease-card.warning {
  border-left-color: #ff9500;
  background: linear-gradient(90deg, #fff7e6 0%, white 10%);
}

.lease-card.notice {
  border-left-color: #1989fa;
  background: linear-gradient(90deg, #f0f9ff 0%, white 10%);
}

.lease-card.normal {
  border-left-color: #07c160;
}

.urgency-badge {
  position: absolute;
  top: 12px;
  right: 12px;
}

/* 租约信息 */
.lease-info {
  padding-right: 80px;
  margin-bottom: 12px;
}

.lease-title {
  font-size: 16px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 8px;
}

.lease-tenant {
  font-size: 14px;
  color: #646566;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.lease-time {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.time-info {
  font-size: 14px;
  color: #646566;
}

.time-label {
  color: #969799;
}

.time-value {
  color: #323233;
  font-weight: bold;
}

.remaining-days {
  font-size: 14px;
  color: #646566;
}

.days {
  font-size: 18px;
  font-weight: bold;
  color: #ff9500;
}

.lease-card.urgent .days {
  color: #fa5151;
}

.lease-card.warning .days {
  color: #ff9500;
}

.lease-card.notice .days {
  color: #1989fa;
}

.lease-rent {
  font-size: 12px;
  color: #969799;
}

/* 快速操作 */
.quick-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* 批量操作栏 */
.batch-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 16px;
  border-top: 1px solid #ebedf0;
  z-index: 100;
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000;
}

/* 响应式适配 */
@media (max-width: 375px) {
  .lease-info {
    padding-right: 70px;
  }
  
  .lease-title {
    font-size: 15px;
  }
  
  .quick-actions {
    flex-direction: column;
    gap: 4px;
  }
  
  .quick-actions .van-button {
    font-size: 12px;
  }
}
</style>