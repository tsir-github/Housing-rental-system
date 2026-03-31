<template>
  <div class="room-card" @click="handleClick">
    <div class="room-card__image">
      <van-image
        :src="props.room.graphVoList?.[0]?.url"
        fit="cover"
        error-icon="photo-fail"
        loading-icon="photo"
      >
        <template #error>
          <div class="room-card__image-placeholder">
            <van-icon name="photo-fail" size="32" color="#c8c9cc" />
          </div>
        </template>
      </van-image>
      
      <!-- 发布状态标签 -->
      <div class="room-card__release-status" v-if="room.isRelease !== undefined">
        <van-tag 
          :type="room.isRelease === 1 ? 'success' : 'default'"
          size="small"
          round
        >
          {{ room.isRelease === 1 ? '已发布' : '未发布' }}
        </van-tag>
      </div>
    </div>
    
    <div class="room-card__content">
      <div class="room-card__header">
        <h3 class="room-card__title">{{ room.apartmentName }} - {{ room.roomNumber }}</h3>
        <div class="room-card__price">
          <span class="room-card__amount">¥{{ room.rent }}</span>
          <span class="room-card__unit">/月</span>
        </div>
      </div>
      
      <div class="room-card__status">
        <ReviewStatusBadge 
          :status="room.reviewStatus" 
          size="small"
        />
        
        <div class="room-card__score" v-if="room.complianceScore">
          <van-icon name="star" size="12" color="#ffd21e" />
          <span>{{ room.complianceScore }}分</span>
        </div>
      </div>
      
      <div class="room-card__info">
        <div class="room-card__info-item">
          <van-icon name="calendar-o" size="14" color="#969799" />
          <span>发布时间：{{ formatDate(room.createTime) }}</span>
        </div>
        
        <div class="room-card__info-item" v-if="room.lastReviewedAt">
          <van-icon name="completed" size="14" color="#969799" />
          <span>审核时间：{{ formatDate(room.lastReviewedAt) }}</span>
        </div>
        
        <div class="room-card__info-item">
          <van-icon name="edit" size="14" color="#969799" />
          <span>更新时间：{{ formatDate(room.updateTime) }}</span>
        </div>
      </div>
    </div>
    
    <div class="room-card__actions" v-if="showActions">
      <van-button 
        size="small" 
        type="default" 
        plain
        @click.stop="handleEdit"
      >
        编辑
      </van-button>
      
      <van-button 
        v-if="room.reviewStatus === 2 && room.isRelease === 0"
        size="small" 
        type="primary"
        @click.stop="handlePublish"
      >
        发布
      </van-button>
      
      <van-button 
        v-if="room.isRelease === 1"
        size="small" 
        type="warning"
        @click.stop="handleUnpublish"
      >
        下架
      </van-button>
      
      <van-button 
        v-if="room.reviewStatus === 3"
        size="small" 
        type="primary"
        @click.stop="handleResubmit"
      >
        重新提交
      </van-button>
      
      <van-button 
        size="small" 
        type="danger" 
        plain
        @click.stop="confirmDelete"
      >
        删除
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { showConfirmDialog } from 'vant';
import type { RoomItemVo } from '@/api/landlord';
import ReviewStatusBadge from '../ReviewStatusBadge/ReviewStatusBadge.vue';

interface Props {
  // 房源数据
  room: RoomItemVo;
  // 是否显示操作按钮
  showActions?: boolean;
}

interface Emits {
  (e: 'click', room: RoomItemVo): void;
  (e: 'edit', room: RoomItemVo): void;
  (e: 'delete', room: RoomItemVo): void;
  (e: 'publish', room: RoomItemVo): void;
  (e: 'unpublish', room: RoomItemVo): void;
  (e: 'resubmit', room: RoomItemVo): void;
}

const props = withDefaults(defineProps<Props>(), {
  showActions: true
});

const emit = defineEmits<Emits>();

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
};

// 事件处理
const handleClick = () => {
  emit('click', props.room);
};

const handleEdit = () => {
  emit('edit', props.room);
};

const handleDelete = () => {
  emit('delete', props.room);
};

const confirmDelete = async () => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这个房源吗？',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    });
    handleDelete();
  } catch {
    // 用户取消删除
  }
};

const handlePublish = () => {
  emit('publish', props.room);
};

const handleUnpublish = () => {
  emit('unpublish', props.room);
};

const handleResubmit = () => {
  emit('resubmit', props.room);
};
</script>

<style scoped lang="less">
.room-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
  
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
  
  &__image {
    position: relative;
    height: 160px;
    overflow: hidden;
    
    :deep(.van-image) {
      width: 100%;
      height: 100%;
    }
  }
  
  &__image-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: #f7f8fa;
  }
  
  &__release-status {
    position: absolute;
    top: 8px;
    right: 8px;
  }
  
  &__content {
    padding: 16px;
  }
  
  &__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 12px;
  }
  
  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin: 0;
    flex: 1;
    min-width: 0;
    margin-right: 12px;
    line-height: 1.3;
  }
  
  &__price {
    flex-shrink: 0;
    text-align: right;
  }
  
  &__amount {
    font-size: 18px;
    font-weight: 600;
    color: #ee0a24;
  }
  
  &__unit {
    font-size: 14px;
    color: #969799;
  }
  
  &__status {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }
  
  &__score {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #646566;
  }
  
  &__info {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 12px;
  }
  
  &__info-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #969799;
  }
  
  &__actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding-top: 12px;
    border-top: 1px solid #ebedf0;
  }
}

// 响应式设计
@media (max-width: 375px) {
  .room-card {
    &__image {
      height: 140px;
    }
    
    &__content {
      padding: 12px;
    }
    
    &__header {
      flex-direction: column;
      gap: 8px;
    }
    
    &__title {
      margin-right: 0;
      font-size: 15px;
    }
    
    &__price {
      text-align: left;
    }
    
    &__amount {
      font-size: 16px;
    }
    
    &__actions {
      flex-wrap: wrap;
      gap: 6px;
    }
  }
}
</style>