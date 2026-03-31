<template>
  <div class="application-card" @click="handleClick">
    <div class="application-card__header">
      <div class="application-card__user">
        <van-icon name="contact" size="16" color="#1989fa" />
        <span class="application-card__username">{{ application.name || '匿名用户' }}</span>
      </div>
      
      <div class="application-card__status">
        <ReviewStatusBadge :status="application.status" size="small" />
      </div>
    </div>
    
    <div class="application-card__content">
      <div class="application-card__room">
        <div class="application-card__room-info">
          <h4 class="application-card__room-name">
            {{ application.apartmentName }} - {{ application.roomNumber }}
          </h4>
          <div class="application-card__rent">
            <span class="application-card__price">¥{{ application.rent }}</span>
            <span class="application-card__unit">/月</span>
          </div>
        </div>
      </div>
      
      <div class="application-card__details">
        <div class="application-card__detail-item">
          <van-icon name="calendar-o" size="14" color="#969799" />
          <span>申请时间：{{ formatDate(application.createTime) }}</span>
        </div>
        
        <div class="application-card__detail-item">
          <van-icon name="home-o" size="14" color="#969799" />
          <span>入住时间：{{ formatDate(application.leaseStartDate) }}</span>
        </div>
        
        <div class="application-card__detail-item">
          <van-icon name="clock-o" size="14" color="#969799" />
          <span>租期：{{ application.leaseTermName || '未知' }}</span>
        </div>
        
        <div class="application-card__detail-item" v-if="application.phone">
          <van-icon name="phone-o" size="14" color="#969799" />
          <span>联系电话：{{ application.phone }}</span>
        </div>
      </div>
      
      <div class="application-card__additional" v-if="application.additionalInfo">
        <div class="application-card__additional-label">备注信息：</div>
        <div class="application-card__additional-text">{{ application.additionalInfo }}</div>
      </div>
    </div>
    
    <div class="application-card__footer" v-if="showActions && application.status === 1">
      <van-button 
        size="small" 
        type="danger" 
        plain
        @click.stop="handleReject"
        :loading="processing"
      >
        拒绝
      </van-button>
      
      <van-button 
        size="small" 
        type="primary"
        @click.stop="handleApprove"
        :loading="processing"
      >
        通过
      </van-button>
    </div>
    
    <div class="application-card__processed" v-if="application.status !== 1 && application.processedAt">
      <div class="application-card__processed-info">
        <span class="application-card__processed-time">
          处理时间：{{ formatDate(application.processedAt) }}
        </span>
        <span class="application-card__processor" v-if="application.processorName">
          处理人：{{ application.processorName }}
        </span>
      </div>
      
      <div class="application-card__rejection-reason" v-if="application.status === 3 && application.rejectionReason">
        <div class="application-card__rejection-label">拒绝原因：</div>
        <div class="application-card__rejection-text">{{ application.rejectionReason }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { DirectMoveInApplicationDetailVO } from '@/api/landlord';
import ReviewStatusBadge from '../ReviewStatusBadge/ReviewStatusBadge.vue';

interface Props {
  // 申请数据
  application: DirectMoveInApplicationDetailVO;
  // 是否显示操作按钮
  showActions?: boolean;
}

interface Emits {
  (e: 'click', application: DirectMoveInApplicationDetailVO): void;
  (e: 'approve', application: DirectMoveInApplicationDetailVO): void;
  (e: 'reject', application: DirectMoveInApplicationDetailVO): void;
}

const props = withDefaults(defineProps<Props>(), {
  showActions: true
});

const emit = defineEmits<Emits>();

const processing = ref(false);

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

// 点击卡片
const handleClick = () => {
  emit('click', props.application);
};

// 通过申请
const handleApprove = async () => {
  processing.value = true;
  try {
    emit('approve', props.application);
  } finally {
    processing.value = false;
  }
};

// 拒绝申请
const handleReject = async () => {
  processing.value = true;
  try {
    emit('reject', props.application);
  } finally {
    processing.value = false;
  }
};
</script>

<style scoped lang="less">
.application-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  
  &__user {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  
  &__username {
    font-size: 14px;
    font-weight: 500;
    color: #323233;
  }
  
  &__content {
    margin-bottom: 12px;
  }
  
  &__room {
    margin-bottom: 12px;
  }
  
  &__room-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  
  &__room-name {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin: 0;
    flex: 1;
    min-width: 0;
    margin-right: 12px;
  }
  
  &__rent {
    flex-shrink: 0;
  }
  
  &__price {
    font-size: 18px;
    font-weight: 600;
    color: #ee0a24;
  }
  
  &__unit {
    font-size: 14px;
    color: #969799;
  }
  
  &__details {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  &__detail-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #646566;
  }
  
  &__additional {
    margin-top: 12px;
    padding: 12px;
    background: #f7f8fa;
    border-radius: 6px;
  }
  
  &__additional-label {
    font-size: 13px;
    color: #646566;
    margin-bottom: 4px;
  }
  
  &__additional-text {
    font-size: 14px;
    color: #323233;
    line-height: 1.4;
  }
  
  &__footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 12px;
    border-top: 1px solid #ebedf0;
  }
  
  &__processed {
    padding-top: 12px;
    border-top: 1px solid #ebedf0;
  }
  
  &__processed-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }
  
  &__processed-time,
  &__processor {
    font-size: 12px;
    color: #969799;
  }
  
  &__rejection-reason {
    margin-top: 8px;
  }
  
  &__rejection-label {
    font-size: 13px;
    color: #646566;
    margin-bottom: 4px;
  }
  
  &__rejection-text {
    font-size: 14px;
    color: #ee0a24;
    line-height: 1.4;
    padding: 8px;
    background: #fef2f2;
    border-radius: 4px;
    border-left: 3px solid #ee0a24;
  }
}

// 响应式设计
@media (max-width: 375px) {
  .application-card {
    padding: 12px;
    
    &__room-info {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
    
    &__room-name {
      margin-right: 0;
      font-size: 15px;
    }
    
    &__price {
      font-size: 16px;
    }
    
    &__processed-info {
      flex-direction: column;
      align-items: flex-start;
      gap: 4px;
    }
  }
}
</style>