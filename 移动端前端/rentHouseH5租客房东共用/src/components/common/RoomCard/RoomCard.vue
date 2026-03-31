<template>
  <van-card @click="goRoomDetail" class="rounded-xl shadow room-card">
    <!--      title-->
    <template #title>
      <slot name="title">
        <div class="flex items-center gap-2">
          <!--      标题-->
          <span class="text-[14px] font-bold">{{
            `${data.apartmentInfo?.name || ""} ${data.roomNumber}房间`
          }}</span>
          <!-- 发布者类型标识 -->
          <PublisherBadge 
            :publisherType="data.publisherType" 
            size="small"
          />
          <!-- 房源状态标识 -->
          <div class="flex items-center gap-1">
            <van-tag 
              v-if="availabilityStatus"
              :type="availabilityStatus.tagType"
              size="small"
              :class="availabilityStatus.className"
            >
              {{ availabilityStatus.text }}
            </van-tag>
          </div>
        </div>
      </slot>
    </template>
    <!--      desc-->
    <template #desc>
      <slot name="desc">
        <div class="space-y-1">
          <div>
            <span class="text-[12px] --van-gray-6">{{
              `${data?.apartmentInfo?.provinceName || ""} ${
                data?.apartmentInfo?.cityName || ""
              } ${data?.apartmentInfo?.districtName || ""}`
            }}</span>
          </div>
          <!-- 评分信息 -->
          <RatingDisplay 
            :complianceScore="data.complianceScore"
            :overallRating="data.overallRating"
            size="small"
          />
          <!-- 可用状态提示 -->
          <div v-if="availabilityStatus && !availabilityStatus.available" class="availability-hint">
            <van-icon :name="availabilityStatus.icon" size="12" :color="availabilityStatus.iconColor" />
            <span class="text-xs text-gray-500 ml-1">{{ availabilityStatus.hint }}</span>
          </div>
        </div>
      </slot>
    </template>
    <!--    price-->
    <template #price>
      <slot name="price">
        <!--      价格-->
        <span class="text-red-500 text-[14px]">￥</span>
        <span class="text-red-500 text-[20px]">{{ data.rent }}</span>
      </slot>
    </template>
    <!--    thumb-->
    <template #thumb>
      <slot name="thumb">
        <div class="relative">
          <van-image
            class="w-full h-full object-cover"
            :src="data.graphVoList?.[0]?.url || '失败'"
          >
            <template v-slot:error>加载失败</template>
            <template v-slot:loading>
              <van-loading type="spinner" size="20" />
            </template>
          </van-image>
          <!-- 不可用状态遮罩 -->
          <div 
            v-if="availabilityStatus && !availabilityStatus.available" 
            class="absolute inset-0 bg-black bg-opacity-30 flex items-center justify-center"
          >
            <van-icon :name="availabilityStatus.icon" size="24" color="white" />
          </div>
        </div>
      </slot>
    </template>
    <!--    tags-->
    <template #tags>
      <slot name="tags">
        <van-tag
          class="last:mr-0 mr-[5px]"
          plain
          v-for="item in data?.labelInfoList"
          :key="item.id"
          type="primary"
          >{{ item.name }}
        </van-tag>
      </slot>
    </template>
  </van-card>
</template>

<script setup lang="ts">
import type { RoomInterface } from "@/api/tenant/search/types";
import type { PropType } from "vue";
import { useRouter } from "vue-router";
import { ref, computed, onMounted } from "vue";
import PublisherBadge from "@/components/common/PublisherBadge/PublisherBadge.vue";
import RatingDisplay from "@/components/common/RatingDisplay/RatingDisplay.vue";
import { checkRoomAvailability } from "@/api/tenant/directMoveIn";
import type { RoomAvailabilityVo } from "@/api/tenant/directMoveIn";

//实际上只需要这些属性
// export interface RoomCardDataProps
//   extends Pick<
//     RoomInterface,
//     "id" | "roomNumber" | "rent" | "graphVoList" | "labelInfoList"
//   > {
//   apartmentInfo: Pick<
//     RoomInterface["apartmentInfo"],
//     "name" | "provinceName" | "cityName" | "districtName"
//   >;
// }

interface AvailabilityStatus {
  available: boolean;
  text: string;
  tagType: 'default' | 'primary' | 'success' | 'warning' | 'danger';
  className: string;
  icon: string;
  iconColor: string;
  hint: string;
}

const router = useRouter();
const props = defineProps({
  // 房间的信息数据
  data: {
    type: Object as PropType<RoomInterface>,
    default: () => ({}),
    readOnly: true
  },
  // 是否显示可用性状态
  showAvailability: {
    type: Boolean,
    default: true
  }
});

// 房源可用性状态
const availability = ref<RoomAvailabilityVo | null>(null);
const isCheckingAvailability = ref(false);

// 计算可用性状态显示
const availabilityStatus = computed<AvailabilityStatus | null>(() => {
  if (!props.showAvailability || !availability.value) {
    return null;
  }

  const { available, statusCode, message, reason } = availability.value;

  if (available) {
    return {
      available: true,
      text: '可租',
      tagType: 'success',
      className: 'available-tag',
      icon: 'success',
      iconColor: '#07c160',
      hint: '房源可预约'
    };
  }

  // 根据不同的不可用状态返回不同的显示
  switch (statusCode) {
    case 'OCCUPIED':
      return {
        available: false,
        text: '已租',
        tagType: 'danger',
        className: 'occupied-tag',
        icon: 'lock',
        iconColor: '#ee0a24',
        hint: '房源已被占用'
      };
    case 'NOT_RELEASED':
      return {
        available: false,
        text: '未发布',
        tagType: 'default',
        className: 'not-released-tag',
        icon: 'eye-o',
        iconColor: '#969799',
        hint: '房东暂未开放'
      };
    case 'NOT_APPROVED':
      return {
        available: false,
        text: '审核中',
        tagType: 'warning',
        className: 'not-approved-tag',
        icon: 'clock-o',
        iconColor: '#ff976a',
        hint: '正在审核中'
      };
    default:
      return {
        available: false,
        text: '不可用',
        tagType: 'default',
        className: 'unavailable-tag',
        icon: 'warning-o',
        iconColor: '#969799',
        hint: message || '暂时不可用'
      };
  }
});

// 检查房源可用性
const checkAvailability = async () => {
  if (!props.data.id || isCheckingAvailability.value) {
    return;
  }

  try {
    isCheckingAvailability.value = true;
    const { data } = await checkRoomAvailability(props.data.id);
    availability.value = data;
  } catch (error) {
    console.error('检查房源可用性失败:', error);
    // 静默失败，不显示错误提示
  } finally {
    isCheckingAvailability.value = false;
  }
};

// 跳转到房间的详情页面
const goRoomDetail = () => {
  router.push({ path: `/tenant/roomDetail/${props.data.id}` });
};

// 组件挂载时检查可用性
onMounted(() => {
  if (props.showAvailability && props.data.id) {
    // 延迟检查，避免同时发起太多请求
    setTimeout(() => {
      checkAvailability();
    }, Math.random() * 1000); // 随机延迟0-1秒
  }
});
</script>

<style scoped>
.room-card {
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.availability-hint {
  display: flex;
  align-items: center;
  margin-top: 4px;
}

/* 可用性标签样式 */
.available-tag {
  background-color: #f0f9ff !important;
  color: #059669 !important;
  border: 1px solid #10b981 !important;
}

.occupied-tag {
  background-color: #fef2f2 !important;
  color: #dc2626 !important;
  border: 1px solid #ef4444 !important;
}

.not-released-tag {
  background-color: #f9fafb !important;
  color: #6b7280 !important;
  border: 1px solid #9ca3af !important;
}

.not-approved-tag {
  background-color: #fffbeb !important;
  color: #d97706 !important;
  border: 1px solid #f59e0b !important;
}

.unavailable-tag {
  background-color: #f3f4f6 !important;
  color: #6b7280 !important;
  border: 1px solid #9ca3af !important;
}

/* 不可用房源的整体样式调整 */
.room-card:has(.occupied-tag),
.room-card:has(.not-released-tag),
.room-card:has(.not-approved-tag),
.room-card:has(.unavailable-tag) {
  opacity: 0.8;
  
  &:hover {
    transform: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}
</style>
