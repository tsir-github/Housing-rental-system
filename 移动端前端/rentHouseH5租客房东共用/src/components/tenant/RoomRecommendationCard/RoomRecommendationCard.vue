<template>
  <div class="room-recommendation-card" @click="handleClick">
    <!-- 房源图片 -->
    <div class="room-image">
      <img
        v-if="room.imageUrls && room.imageUrls.length > 0"
        :src="room.imageUrls[0]"
        alt="房源图片"
      />
      <img
        v-else
        src="@/assets/placeholder-apartment.svg"
        alt="默认图片"
      />
      
      <!-- 匹配度标签 -->
      <div v-if="room.matchScore" class="match-badge">
        匹配度 {{ Math.round(room.matchScore * 100) }}%
      </div>
    </div>

    <!-- 房源信息 -->
    <div class="room-info">
      <!-- 房源名称 + 房间号 -->
      <div class="room-name">{{ room.apartmentName }}-{{ room.roomNumber }}</div>
      
      <!-- 位置信息 -->
      <div class="room-location">
        <van-icon name="location-o" />
        <span>{{ room.location }}</span>
      </div>
      
      <!-- 房源属性 -->
      <div v-if="room.roomAttributes && room.roomAttributes.length > 0" class="room-attributes">
        <van-tag
          v-for="(attr, index) in room.roomAttributes.slice(0, 3)"
          :key="index"
          type="primary"
          plain
          size="small"
        >
          {{ attr }}
        </van-tag>
      </div>
      
      <!-- 底部信息 -->
      <div class="room-footer">
        <!-- 价格 -->
        <div class="room-price">
          <span class="price-value">¥{{ room.rent }}</span>
          <span class="price-unit">/月</span>
        </div>
        
        <!-- 评分 -->
        <div v-if="room.overallRating" class="room-rating">
          <van-rate
            :model-value="room.overallRating"
            :size="14"
            readonly
            allow-half
            color="#ffd21e"
            void-icon="star"
            void-color="#eee"
          />
          <span class="rating-text">{{ room.overallRating.toFixed(1) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { RoomRecommendation } from '@/api/tenant/ai'

interface Props {
  room: RoomRecommendation
}

const props = defineProps<Props>()
const emit = defineEmits<{
  click: [roomId: number]
}>()

/**
 * 处理点击事件
 */
const handleClick = () => {
  emit('click', props.room.roomId)
}
</script>

<style scoped lang="less">
.room-recommendation-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  }
}

.room-image {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  background-color: #f5f5f5;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .match-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    padding: 4px 8px;
    background-color: rgba(25, 137, 250, 0.9);
    color: #fff;
    font-size: 12px;
    border-radius: 4px;
    font-weight: 500;
  }
}

.room-info {
  padding: 12px;
}

.room-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.room-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.room-attributes {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.room-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-price {
  .price-value {
    font-size: 20px;
    font-weight: 600;
    color: #ff6b6b;
  }

  .price-unit {
    font-size: 13px;
    color: #999;
    margin-left: 2px;
  }
}

.room-rating {
  display: flex;
  align-items: center;
  gap: 4px;

  .rating-text {
    font-size: 13px;
    color: #666;
    font-weight: 500;
  }
}
</style>
