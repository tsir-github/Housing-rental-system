<template>
  <div class="apartment-card" @click="handleClick">
    <div class="apartment-card__image">
      <van-image
        :src="props.apartment.graphVoList?.[0]?.url"
        fit="cover"
        error-icon="photo-fail"
        loading-icon="photo"
        @load="handleImageLoad"
        @error="handleImageError"
      >
        <template #error>
          <div class="apartment-card__image-placeholder">
            <van-icon name="photo-fail" size="32" color="#c8c9cc" />
            <div style="font-size: 12px; color: #969799; margin-top: 4px;">图片加载失败</div>
          </div>
        </template>
      </van-image>
      
      <!-- 发布状态标签 -->
      <div class="apartment-card__release-status" v-if="apartment.isRelease !== undefined">
        <van-tag 
          :type="apartment.isRelease === 1 ? 'success' : 'default'"
          size="small"
          round
        >
          {{ apartment.isRelease === 1 ? '已发布' : '未发布' }}
        </van-tag>
      </div>
    </div>
    
    <div class="apartment-card__content">
      <div class="apartment-card__header">
        <h3 class="apartment-card__title">{{ apartment.name }}</h3>
        <div class="apartment-card__price">
          <span class="apartment-card__amount">¥{{ apartment.minRent || '待定' }}</span>
          <span class="apartment-card__unit">/月起</span>
        </div>
      </div>
      
      <div class="apartment-card__location">
        <van-icon name="location-o" size="14" color="#969799" />
        <span>{{ apartment.provinceName }} {{ apartment.cityName }} {{ apartment.districtName }}</span>
      </div>
      
      <div class="apartment-card__stats">
        <div class="apartment-card__stat-item">
          <span class="apartment-card__stat-label">房源数量</span>
          <span class="apartment-card__stat-value">{{ apartment.roomCount || 0 }}间</span>
        </div>
        
        <div class="apartment-card__stat-item">
          <span class="apartment-card__stat-label">可用房源</span>
          <span class="apartment-card__stat-value">{{ apartment.availableRoomCount || 0 }}间</span>
        </div>
        
        <div class="apartment-card__stat-item">
          <span class="apartment-card__stat-label">已出租</span>
          <span class="apartment-card__stat-value">{{ (apartment.roomCount || 0) - (apartment.availableRoomCount || 0) }}间</span>
        </div>
      </div>
      
      <div class="apartment-card__info">
        <div class="apartment-card__info-item">
          <van-icon name="calendar-o" size="14" color="#969799" />
          <span>创建时间：{{ formatDate(apartment.createTime) }}</span>
        </div>
        
        <div class="apartment-card__info-item">
          <van-icon name="edit" size="14" color="#969799" />
          <span>更新时间：{{ formatDate(apartment.updateTime) }}</span>
        </div>
      </div>
      
      <!-- 公寓标签 -->
      <div class="apartment-card__tags" v-if="apartment.labelInfoList && apartment.labelInfoList.length > 0">
        <van-tag 
          v-for="label in apartment.labelInfoList" 
          :key="label.id"
          size="small"
          type="primary"
          plain
          class="apartment-card__tag"
        >
          {{ label.name }}
        </van-tag>
      </div>
    </div>
    
    <div class="apartment-card__actions" v-if="showActions">
      <van-button 
        size="small" 
        type="default" 
        plain
        @click.stop="handleEdit"
      >
        编辑
      </van-button>
      
      <van-button 
        size="small" 
        type="primary"
        @click.stop="handleManageRooms"
      >
        房源管理
      </van-button>
      
      <van-button 
        v-if="apartment.isRelease === 0"
        size="small" 
        type="primary"
        @click.stop="handlePublish"
      >
        发布
      </van-button>
      
      <van-button 
        v-if="apartment.isRelease === 1"
        size="small" 
        type="warning"
        @click.stop="handleUnpublish"
      >
        下架
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
import type { ApartmentItemVo } from '@/api/landlord';

interface Props {
  // 公寓数据
  apartment: ApartmentItemVo;
  // 是否显示操作按钮
  showActions?: boolean;
}

interface Emits {
  (e: 'click', apartment: ApartmentItemVo): void;
  (e: 'edit', apartment: ApartmentItemVo): void;
  (e: 'delete', apartment: ApartmentItemVo): void;
  (e: 'publish', apartment: ApartmentItemVo): void;
  (e: 'unpublish', apartment: ApartmentItemVo): void;
  (e: 'manage-rooms', apartment: ApartmentItemVo): void;
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
  emit('click', props.apartment);
};

const handleEdit = () => {
  emit('edit', props.apartment);
};

const handleDelete = () => {
  emit('delete', props.apartment);
};

const confirmDelete = async () => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这个公寓吗？删除后相关房源也会被删除。',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    });
    handleDelete();
  } catch {
    // 用户取消删除
  }
};

const handlePublish = () => {
  emit('publish', props.apartment);
};

const handleUnpublish = () => {
  emit('unpublish', props.apartment);
};

const handleImageLoad = () => {
  console.log('✅ [ApartmentCard] 图片加载成功:', props.apartment.name, props.apartment.graphVoList?.[0]?.url);
};

const handleImageError = (error: any) => {
  console.error('❌ [ApartmentCard] 图片加载失败:', props.apartment.name, props.apartment.graphVoList?.[0]?.url, error);
};

const handleManageRooms = () => {
  emit('manage-rooms', props.apartment);
};
</script>

<style scoped lang="less">
.apartment-card {
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
  
  &__location {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #646566;
    margin-bottom: 12px;
  }
  
  &__stats {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    padding: 12px;
    background: #f7f8fa;
    border-radius: 6px;
  }
  
  &__stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
  }
  
  &__stat-label {
    font-size: 12px;
    color: #969799;
    margin-bottom: 4px;
  }
  
  &__stat-value {
    font-size: 14px;
    font-weight: 600;
    color: #323233;
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
  
  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 12px;
  }
  
  &__tag {
    margin: 0;
  }
  
  &__actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding-top: 12px;
    border-top: 1px solid #ebedf0;
    flex-wrap: wrap;
  }
}

// 响应式设计
@media (max-width: 375px) {
  .apartment-card {
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
    
    &__stats {
      padding: 8px;
    }
    
    &__stat-label {
      font-size: 11px;
    }
    
    &__stat-value {
      font-size: 13px;
    }
    
    &__actions {
      gap: 6px;
    }
  }
}
</style>