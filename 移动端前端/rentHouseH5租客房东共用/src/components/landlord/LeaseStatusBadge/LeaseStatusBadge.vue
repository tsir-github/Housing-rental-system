<template>
  <van-tag 
    :color="statusColor"
    :plain="plain"
    :size="size"
    :class="['lease-status-badge', statusClass]"
  >
    <van-icon 
      v-if="showIcon" 
      :name="statusIcon" 
      size="12"
      class="status-icon"
    />
    {{ statusText }}
  </van-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {
  getLeaseStatusName,
  getLeaseStatusColor,
  LeaseStatus
} from '@/api/landlord/lease';

// Props
interface Props {
  status: LeaseStatus;
  plain?: boolean;
  size?: 'small' | 'medium' | 'large';
  showIcon?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  plain: true,
  size: 'medium',
  showIcon: false
});

// 计算属性
const statusText = computed(() => getLeaseStatusName(props.status));
const statusColor = computed(() => getLeaseStatusColor(props.status));

const statusClass = computed(() => {
  const statusMap = {
    [LeaseStatus.SIGNING]: 'signing',
    [LeaseStatus.SIGNED]: 'signed',
    [LeaseStatus.CANCELED]: 'canceled',
    [LeaseStatus.EXPIRED]: 'expired',
    [LeaseStatus.WITHDRAWING]: 'withdrawing',
    [LeaseStatus.WITHDRAWN]: 'withdrawn',
    [LeaseStatus.RENEWING]: 'renewing'
  };
  return statusMap[props.status] || 'default';
});

const statusIcon = computed(() => {
  const iconMap = {
    [LeaseStatus.SIGNING]: 'clock-o',
    [LeaseStatus.SIGNED]: 'success',
    [LeaseStatus.CANCELED]: 'cross',
    [LeaseStatus.EXPIRED]: 'warning-o',
    [LeaseStatus.WITHDRAWING]: 'clock-o',
    [LeaseStatus.WITHDRAWN]: 'cross',
    [LeaseStatus.RENEWING]: 'clock-o'
  };
  return iconMap[props.status] || 'info-o';
});
</script>

<style scoped>
.lease-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.status-icon {
  flex-shrink: 0;
}

/* 状态特定样式 */
.lease-status-badge.signing {
  animation: pulse 2s infinite;
}

.lease-status-badge.signed {
  font-weight: bold;
}

.lease-status-badge.canceled,
.lease-status-badge.withdrawn {
  opacity: 0.8;
}

.lease-status-badge.expired {
  text-decoration: line-through;
  opacity: 0.7;
}

.lease-status-badge.withdrawing,
.lease-status-badge.renewing {
  animation: pulse 2s infinite;
}

/* 脉冲动画 */
@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
  100% {
    opacity: 1;
  }
}

/* 尺寸调整 */
.lease-status-badge :deep(.van-tag--small) {
  padding: 2px 6px;
  font-size: 10px;
}

.lease-status-badge :deep(.van-tag--medium) {
  padding: 4px 8px;
  font-size: 12px;
}

.lease-status-badge :deep(.van-tag--large) {
  padding: 6px 12px;
  font-size: 14px;
}
</style>