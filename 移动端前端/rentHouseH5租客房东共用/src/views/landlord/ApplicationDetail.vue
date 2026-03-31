<template>
  <div class="application-detail">
    <!-- 导航栏 -->
    <van-nav-bar
      title="直接入住申请详情"
      left-arrow
      @click-left="handleGoBack"
      fixed
      placeholder
    />
    
    <PageContainer :loading="loading" :show-empty="showEmpty" :show-header="false" empty-type="error">
    <!-- 申请状态卡片 -->
    <div class="application-detail-status" v-if="applicationDetail">
      <div class="status-header">
        <ReviewStatusBadge :status="applicationDetail.status" size="large" />
        <div class="status-time">
          <van-icon name="clock-o" size="14" color="#969799" />
          <span>{{ formatDate(applicationDetail.createTime || applicationDetail.applicationDate) }}</span>
        </div>
      </div>
      
      <div class="status-info" v-if="applicationDetail.status !== 1">
        <div class="processed-info">
          <span class="processed-label">处理时间：</span>
          <span class="processed-time">{{ formatDate(applicationDetail.processedAt) }}</span>
        </div>
        
        <div class="processor-info" v-if="applicationDetail.processorName">
          <span class="processor-label">处理人：</span>
          <span class="processor-name">{{ applicationDetail.processorName }}</span>
        </div>
      </div>
    </div>

    <!-- 租客信息 -->
    <div class="application-detail-user" v-if="applicationDetail">
      <div class="section-title">
        <van-icon name="contact" size="16" color="#1989fa" />
        <span>租客信息</span>
      </div>
      
      <div class="user-info">
        <div class="user-item">
          <span class="user-label">姓名</span>
          <span class="user-value">{{ applicationDetail.name || applicationDetail.userName || '未提供' }}</span>
        </div>
        
        <div class="user-item">
          <span class="user-label">联系电话</span>
          <div class="user-phone">
            <span class="user-value">{{ applicationDetail.phone || applicationDetail.userPhone || '未提供' }}</span>
            <van-button 
              v-if="applicationDetail.phone || applicationDetail.userPhone"
              type="primary" 
              size="mini"
              @click="handleCall"
            >
              拨打
            </van-button>
          </div>
        </div>
        
        <div class="user-item" v-if="applicationDetail.identificationNumber">
          <span class="user-label">身份证号</span>
          <span class="user-value">{{ formatIdNumber(applicationDetail.identificationNumber) }}</span>
        </div>
        
        <div class="user-item" v-if="applicationDetail.email">
          <span class="user-label">邮箱地址</span>
          <span class="user-value">{{ applicationDetail.email }}</span>
        </div>
        
        <div class="user-item" v-if="applicationDetail.emergencyContact">
          <span class="user-label">紧急联系人</span>
          <span class="user-value">{{ applicationDetail.emergencyContact }}</span>
        </div>
        
        <div class="user-item" v-if="applicationDetail.emergencyPhone">
          <span class="user-label">紧急联系电话</span>
          <div class="user-phone">
            <span class="user-value">{{ applicationDetail.emergencyPhone }}</span>
            <van-button 
              type="default" 
              size="mini"
              @click="handleCallEmergency"
            >
              拨打
            </van-button>
          </div>
        </div>
        
        <div class="user-item">
          <span class="user-label">申请时间</span>
          <span class="user-value">{{ formatDate(applicationDetail.createTime || applicationDetail.applicationDate) }}</span>
        </div>
      </div>
    </div>

    <!-- 房源信息 -->
    <div class="application-detail-room" v-if="applicationDetail">
      <div class="section-title">
        <van-icon name="home-o" size="16" color="#07c160" />
        <span>房源信息</span>
      </div>
      
      <div class="room-card" @click="handleViewRoom">
        <div class="room-info">
          <h4 class="room-name">{{ applicationDetail.apartmentName }} - {{ applicationDetail.roomNumber }}</h4>
          <div class="room-rent">
            <span class="rent-amount">¥{{ applicationDetail.rent }}</span>
            <span class="rent-unit">/月</span>
          </div>
        </div>
        
        <van-icon name="arrow" size="16" color="#c8c9cc" />
      </div>
    </div>

    <!-- 租赁信息 -->
    <div class="application-detail-lease" v-if="applicationDetail">
      <div class="section-title">
        <van-icon name="calendar-o" size="16" color="#ff976a" />
        <span>租赁信息</span>
      </div>
      
      <div class="lease-info">
        <div class="lease-item">
          <span class="lease-label">期望入住时间</span>
          <span class="lease-value">{{ formatDate(applicationDetail.leaseStartDate || applicationDetail.moveInDate) }}</span>
        </div>
        
        <div class="lease-item">
          <span class="lease-label">租期</span>
          <span class="lease-value">{{ applicationDetail.leaseTermName || (applicationDetail.leaseTerm + '个月') }}</span>
        </div>
        
        <div class="lease-item">
          <span class="lease-label">月租金</span>
          <span class="lease-value lease-value--price">¥{{ applicationDetail.rent }}</span>
        </div>
      </div>
    </div>

    <!-- 备注信息 -->
    <div class="application-detail-notes" v-if="applicationDetail?.additionalInfo">
      <div class="section-title">
        <van-icon name="notes-o" size="16" color="#646566" />
        <span>备注信息</span>
      </div>
      
      <div class="notes-content">
        {{ applicationDetail.additionalInfo }}
      </div>
    </div>

    <!-- 处理记录 -->
    <div class="application-detail-history" v-if="applicationDetail && applicationDetail.status !== 1">
      <div class="section-title">
        <van-icon name="clock-o" size="16" color="#969799" />
        <span>处理记录</span>
      </div>
      
      <div class="history-timeline">
        <div class="timeline-item">
          <div class="timeline-dot" :class="getStatusClass(applicationDetail.status)"></div>
          <div class="timeline-content">
            <div class="timeline-status">
              <ReviewStatusBadge :status="applicationDetail.status" size="small" />
              <span class="timeline-time">{{ formatDate(applicationDetail.processedAt) }}</span>
            </div>
            
            <div class="timeline-desc">
              <span v-if="applicationDetail.status === 2">申请已通过，租约生成成功</span>
              <span v-else-if="applicationDetail.status === 3">申请被拒绝</span>
            </div>
            
            <div class="timeline-processor" v-if="applicationDetail.processorName">
              处理人：{{ applicationDetail.processorName }}
            </div>
            
            <div class="timeline-reason" v-if="applicationDetail.status === 3 && applicationDetail.rejectionReason">
              <div class="reason-label">拒绝原因：</div>
              <div class="reason-text">{{ applicationDetail.rejectionReason }}</div>
            </div>
          </div>
        </div>
        
        <div class="timeline-item">
          <div class="timeline-dot timeline-dot--default"></div>
          <div class="timeline-content">
            <div class="timeline-status">
              <van-tag type="default" size="small">已提交</van-tag>
              <span class="timeline-time">{{ formatDate(applicationDetail.applicationDate) }}</span>
            </div>
            <div class="timeline-desc">租客提交申请</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="application-detail-actions" v-if="applicationDetail?.status === 1">
      <van-button 
        type="danger" 
        size="large"
        plain
        @click="handleReject"
        :loading="rejecting"
        class="action-btn"
      >
        拒绝申请
      </van-button>
      
      <van-button 
        type="success" 
        size="large"
        @click="handleApprove"
        :loading="approving"
        class="action-btn"
      >
        通过申请
      </van-button>
    </div>

    <!-- 已处理状态的操作 -->
    <div class="application-detail-info" v-else-if="applicationDetail">
      <van-button 
        type="default" 
        size="large"
        @click="handleViewRoom"
        class="info-btn"
      >
        查看房源
      </van-button>
      
      <van-button 
        v-if="applicationDetail.status === 2"
        type="primary" 
        size="large"
        @click="handleViewLease"
        class="info-btn"
      >
        查看租约
      </van-button>
    </div>

    <!-- 联系租客 -->
    <div class="application-detail-contact" v-if="applicationDetail?.phone || applicationDetail?.userPhone">
      <van-cell-group>
        <van-cell 
          title="联系租客" 
          :value="applicationDetail.phone || applicationDetail.userPhone"
          is-link
          @click="handleCall"
        >
          <template #icon>
            <van-icon name="phone-o" size="16" color="#07c160" />
          </template>
        </van-cell>
        
        <van-cell 
          title="发送短信" 
          value="发送通知"
          is-link
          @click="handleSendSMS"
        >
          <template #icon>
            <van-icon name="chat-o" size="16" color="#1989fa" />
          </template>
        </van-cell>
      </van-cell-group>
    </div>
  </PageContainer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showConfirmDialog, showDialog } from 'vant';
import { useUserStore } from '@/store/modules/user';
import {
  getApplicationDetail,
  approveApplication,
  rejectApplication,
  getApplicationLeaseId
} from '@/api/landlord';
import type { DirectMoveInApplicationDetailVO } from '@/api/landlord';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import ReviewStatusBadge from '@/components/landlord/ReviewStatusBadge/ReviewStatusBadge.vue';
import { NavigationHelper } from '@/utils/navigationHelper';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const approving = ref(false);
const rejecting = ref(false);
const applicationDetail = ref<DirectMoveInApplicationDetailVO | null>(null);

// 计算属性
const showEmpty = computed(() => !loading.value && !applicationDetail.value);

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

// 格式化身份证号（隐藏中间部分）
const formatIdNumber = (idNumber: string) => {
  if (!idNumber || idNumber.length < 8) return idNumber;
  // 显示前4位和后4位，中间用*号替代
  const start = idNumber.substring(0, 4);
  const end = idNumber.substring(idNumber.length - 4);
  const middle = '*'.repeat(idNumber.length - 8);
  return `${start}${middle}${end}`;
};

// 获取状态样式类
const getStatusClass = (status: number) => {
  switch (status) {
    case 2:
      return 'timeline-dot--success';
    case 3:
      return 'timeline-dot--danger';
    default:
      return 'timeline-dot--warning';
  }
};

// 加载申请详情
const loadApplicationDetail = async () => {
  try {
    loading.value = true;
    const applicationId = Number(route.params.id);
    
    const { data } = await getApplicationDetail(applicationId);
    applicationDetail.value = data;
    
  } catch (error) {
    console.error('加载申请详情失败:', error);
    showToast('加载申请详情失败');
  } finally {
    loading.value = false;
  }
};

// 事件处理
const handleCall = () => {
  const phone = applicationDetail.value?.phone || applicationDetail.value?.userPhone;
  if (phone) {
    window.location.href = `tel:${phone}`;
  }
};

const handleCallEmergency = () => {
  const phone = applicationDetail.value?.emergencyPhone;
  if (phone) {
    window.location.href = `tel:${phone}`;
  }
};

const handleSendSMS = () => {
  const phone = applicationDetail.value?.phone || applicationDetail.value?.userPhone;
  if (phone) {
    // TODO: 实现短信发送功能
    showToast('短信功能开发中');
  }
};

const handleViewRoom = () => {
  if (applicationDetail.value?.roomId) {
    router.push(`/landlord/rooms/${applicationDetail.value.roomId}`);
  }
};

const handleGoBack = () => {
  console.log('🧭 申请详情页面返回按钮被点击');
  NavigationHelper.smartGoBack(router);
};

const handleViewLease = async () => {
  if (!applicationDetail.value?.id) {
    showToast('申请信息获取失败');
    return;
  }

  try {
    console.log('🔍 获取申请对应的租约ID - applicationId:', applicationDetail.value.id);
    
    // 调用API获取申请对应的租约ID
    const { data: leaseId } = await getApplicationLeaseId(applicationDetail.value.id);
    
    if (leaseId) {
      console.log('✅ 找到对应租约，跳转到租约详情 - leaseId:', leaseId);
      // 跳转到租约详情页面
      router.push(`/landlord/leases/detail/${leaseId}`);
    } else {
      showToast('该申请尚未生成租约');
    }
  } catch (error) {
    console.error('获取租约信息失败:', error);
    showToast('获取租约信息失败，请重试');
  }
};

const handleApprove = async () => {
  try {
    await showConfirmDialog({
      title: '确认通过申请',
      message: `确定要通过租客"${applicationDetail.value?.name || applicationDetail.value?.userName}"的申请吗？通过后将生成租约。`,
      confirmButtonText: '通过',
      cancelButtonText: '取消'
    });
    
    approving.value = true;
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId || !applicationDetail.value) {
      showToast('操作失败');
      return;
    }
    
    await approveApplication(applicationDetail.value.id, landlordId);
    
    // 更新申请状态
    applicationDetail.value.status = 2;
    applicationDetail.value.processedAt = new Date().toISOString();
    applicationDetail.value.processorName = userStore.userInfo?.name || '房东';
    
    showToast('申请已通过');
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('通过申请失败:', error);
      showToast('操作失败');
    }
  } finally {
    approving.value = false;
  }
};

const handleReject = async () => {
  try {
    // 显示拒绝原因输入对话框
    const result = await showDialog({
      title: '拒绝申请',
      message: '请输入拒绝原因：',
      showCancelButton: true,
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      beforeClose: (action, done) => {
        if (action === 'confirm') {
          const input = document.querySelector('.van-dialog__message input') as HTMLInputElement;
          const reason = input?.value?.trim();
          if (!reason) {
            showToast('请输入拒绝原因');
            return;
          }
          done();
        } else {
          done();
        }
      }
    });
    
    if (result === 'confirm') {
      const input = document.querySelector('.van-dialog__message input') as HTMLInputElement;
      const rejectionReason = input?.value?.trim() || '不符合要求';
      
      rejecting.value = true;
      const landlordId = userStore.userInfo?.id;
      
      if (!landlordId || !applicationDetail.value) {
        showToast('操作失败');
        return;
      }
      
      await rejectApplication({
        id: applicationDetail.value.id,
        landlordId,
        rejectionReason
      });
      
      // 更新申请状态
      applicationDetail.value.status = 3;
      applicationDetail.value.processedAt = new Date().toISOString();
      applicationDetail.value.processorName = userStore.userInfo?.name || '房东';
      applicationDetail.value.rejectionReason = rejectionReason;
      
      showToast('申请已拒绝');
    }
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('拒绝申请失败:', error);
      showToast('操作失败');
    }
  } finally {
    rejecting.value = false;
  }
};

// 生命周期
onMounted(() => {
  loadApplicationDetail();
});
</script>

<style scoped lang="less">
.application-detail-status {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  
  .status-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }
  
  .status-time {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #646566;
  }
  
  .status-info {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding-top: 12px;
    border-top: 1px solid #ebedf0;
  }
  
  .processed-info,
  .processor-info {
    display: flex;
    align-items: center;
    font-size: 13px;
    
    .processed-label,
    .processor-label {
      color: #969799;
      margin-right: 8px;
    }
    
    .processed-time,
    .processor-name {
      color: #323233;
      font-weight: 500;
    }
  }
}

.application-detail-user,
.application-detail-room,
.application-detail-lease,
.application-detail-notes,
.application-detail-history {
  background: white;
  padding: 16px;
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
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .user-label {
    font-size: 14px;
    color: #646566;
    min-width: 80px;
  }
  
  .user-value {
    font-size: 14px;
    color: #323233;
    font-weight: 500;
  }
  
  .user-phone {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.room-card {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  
  &:hover {
    background: #f2f3f5;
  }
  
  .room-info {
    flex: 1;
    margin-right: 12px;
  }
  
  .room-name {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin: 0 0 4px 0;
  }
  
  .room-rent {
    display: flex;
    align-items: baseline;
    gap: 2px;
    
    .rent-amount {
      font-size: 18px;
      font-weight: 600;
      color: #ee0a24;
    }
    
    .rent-unit {
      font-size: 14px;
      color: #969799;
    }
  }
}

.lease-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lease-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .lease-label {
    font-size: 14px;
    color: #646566;
  }
  
  .lease-value {
    font-size: 14px;
    color: #323233;
    font-weight: 500;
    
    &--price {
      color: #ee0a24;
      font-size: 16px;
      font-weight: 600;
    }
  }
}

.notes-content {
  font-size: 14px;
  color: #646566;
  line-height: 1.6;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 6px;
}

.history-timeline {
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    left: 8px;
    top: 0;
    bottom: 0;
    width: 2px;
    background: #ebedf0;
  }
}

.timeline-item {
  position: relative;
  padding-left: 32px;
  margin-bottom: 16px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.timeline-dot {
  position: absolute;
  left: 0;
  top: 4px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #ebedf0;
  border: 3px solid white;
  
  &--success {
    background: #07c160;
  }
  
  &--danger {
    background: #ee0a24;
  }
  
  &--warning {
    background: #ff976a;
  }
  
  &--default {
    background: #c8c9cc;
  }
}

.timeline-content {
  .timeline-status {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
  }
  
  .timeline-time {
    font-size: 12px;
    color: #969799;
  }
  
  .timeline-desc {
    font-size: 13px;
    color: #646566;
    margin-bottom: 4px;
  }
  
  .timeline-processor {
    font-size: 12px;
    color: #969799;
    margin-bottom: 8px;
  }
  
  .timeline-reason {
    .reason-label {
      font-size: 12px;
      color: #646566;
      margin-bottom: 4px;
    }
    
    .reason-text {
      font-size: 13px;
      color: #ee0a24;
      padding: 8px;
      background: #fef2f2;
      border-radius: 4px;
      border-left: 3px solid #ee0a24;
    }
  }
}

.application-detail-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .action-btn {
    flex: 1;
  }
}

.application-detail-info {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .info-btn {
    flex: 1;
  }
}

.application-detail-contact {
  margin-bottom: 16px;
}

// 响应式设计
@media (max-width: 375px) {
  .application-detail-status {
    .status-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
  }
  
  .room-card {
    .room-name {
      font-size: 15px;
    }
    
    .rent-amount {
      font-size: 16px;
    }
  }
  
  .application-detail-actions,
  .application-detail-info {
    flex-direction: column;
  }
}
</style>