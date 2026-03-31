<template>
  <PageContainer :loading="loading" :show-empty="showEmpty" empty-type="error">
    <!-- 公寓图片轮播 -->
    <div class="apartment-detail-images" v-if="apartmentDetail?.graphVoList?.length">
      <van-swipe :autoplay="3000" indicator-color="white">
        <van-swipe-item
          v-for="image in apartmentDetail.graphVoList"
          :key="image.id"
        >
          <van-image
            :src="image.url"
            fit="cover"
            width="100%"
            height="200px"
          />
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 基本信息 -->
    <div class="apartment-detail-info">
      <div class="apartment-detail-info__header">
        <h1 class="apartment-detail-info__name">{{ apartmentDetail?.name }}</h1>
        
        <div class="apartment-detail-info__status">
          <van-tag 
            :type="apartmentDetail?.isRelease === 1 ? 'success' : 'default'"
            size="medium"
            round
          >
            {{ apartmentDetail?.isRelease === 1 ? '已发布' : '未发布' }}
          </van-tag>
        </div>
      </div>
      
      <div class="apartment-detail-info__location">
        <van-icon name="location-o" size="16" color="#969799" />
        <span>{{ locationText }}</span>
      </div>
      
      <div class="apartment-detail-info__address">
        <van-icon name="home-o" size="16" color="#969799" />
        <span>{{ apartmentDetail?.addressDetail }}</span>
      </div>
      
      <div class="apartment-detail-info__contact">
        <van-icon name="phone-o" size="16" color="#969799" />
        <span>{{ apartmentDetail?.phone }}</span>
        <van-button 
          type="primary" 
          size="mini" 
          @click="handleCall"
          class="contact-btn"
        >
          拨打
        </van-button>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="apartment-detail-stats">
      <div class="apartment-detail-stats__title">房源统计</div>
      
      <div class="apartment-detail-stats__grid">
        <StatCard
          :value="roomStats.total"
          label="房源总数"
          icon="home-o"
          icon-color="#1989fa"
          unit="间"
          size="small"
          clickable
          @click="handleViewRooms"
        />
        
        <StatCard
          :value="roomStats.available"
          label="可租房源"
          icon="eye-o"
          icon-color="#07c160"
          unit="间"
          size="small"
          clickable
          @click="handleViewAvailableRooms"
        />
        
        <StatCard
          :value="roomStats.occupied"
          label="已出租"
          icon="user-o"
          icon-color="#ff976a"
          unit="间"
          size="small"
        />
      </div>
    </div>

    <!-- 公寓介绍 -->
    <div class="apartment-detail-description">
      <div class="apartment-detail-description__title">公寓介绍</div>
      <div class="apartment-detail-description__content">
        {{ apartmentDetail?.introduction }}
      </div>
    </div>

    <!-- 配套设施 -->
    <div class="apartment-detail-facilities" v-if="apartmentDetail?.facilityInfoList?.length">
      <div class="apartment-detail-facilities__title">配套设施</div>
      
      <div class="apartment-detail-facilities__grid">
        <div
          v-for="facility in apartmentDetail.facilityInfoList"
          :key="facility.id"
          class="facility-item"
        >
          <van-icon :name="facility.icon" size="20" color="#1989fa" />
          <span>{{ facility.name }}</span>
        </div>
      </div>
    </div>

    <!-- 公寓标签 -->
    <div class="apartment-detail-labels" v-if="apartmentDetail?.labelInfoList?.length">
      <div class="apartment-detail-labels__title">公寓标签</div>
      
      <div class="apartment-detail-labels__list">
        <van-tag
          v-for="label in apartmentDetail.labelInfoList"
          :key="label.id"
          type="primary"
          plain
          size="medium"
          class="label-tag"
        >
          {{ label.name }}
        </van-tag>
      </div>
    </div>

    <!-- 杂费信息 -->
    <div class="apartment-detail-fees" v-if="apartmentDetail?.feeValueVoList?.length">
      <div class="apartment-detail-fees__title">杂费信息</div>
      
      <div class="apartment-detail-fees__list">
        <div
          v-for="fee in apartmentDetail.feeValueVoList"
          :key="fee.id"
          class="fee-item"
        >
          <span class="fee-name">{{ fee.feeKeyName }}</span>
          <span class="fee-value">{{ fee.name }}{{ fee.unit }}</span>
        </div>
      </div>
    </div>

    <!-- 房源列表 -->
    <div class="apartment-detail-rooms" v-if="apartmentDetail?.roomList?.length">
      <div class="apartment-detail-rooms__header">
        <div class="apartment-detail-rooms__title">房源列表</div>
        <van-button 
          type="primary" 
          size="mini" 
          @click="handleAddRoom"
        >
          添加房源
        </van-button>
      </div>
      
      <div class="apartment-detail-rooms__list">
        <div
          v-for="room in apartmentDetail.roomList"
          :key="room.id"
          class="room-item"
          @click="handleViewRoom(room)"
        >
          <div class="room-item__info">
            <div class="room-item__number">{{ room.roomNumber }}</div>
            <div class="room-item__rent">¥{{ room.rent }}/月</div>
          </div>
          
          <div class="room-item__status">
            <ReviewStatusBadge :status="room.reviewStatus" size="small" />
            
            <van-tag 
              :type="room.isRelease === 1 ? 'success' : 'default'"
              size="small"
            >
              {{ room.isRelease === 1 ? '已发布' : '未发布' }}
            </van-tag>
          </div>
          
          <van-icon name="arrow" size="16" color="#c8c9cc" />
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="apartment-detail-actions">
      <van-button 
        type="default" 
        size="large"
        @click="handleEdit"
        class="action-btn"
      >
        编辑公寓
      </van-button>
      
      <van-button 
        v-if="apartmentDetail?.isRelease === 0"
        type="primary" 
        size="large"
        @click="handlePublish"
        :loading="publishing"
        class="action-btn"
      >
        发布公寓
      </van-button>
      
      <van-button 
        v-if="apartmentDetail?.isRelease === 1"
        type="warning" 
        size="large"
        @click="handleUnpublish"
        :loading="publishing"
        class="action-btn"
      >
        下架公寓
      </van-button>
    </div>

    <!-- 危险操作 -->
    <div class="apartment-detail-danger">
      <van-button 
        type="danger" 
        size="large"
        plain
        @click="handleDelete"
        :loading="deleting"
      >
        删除公寓
      </van-button>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { useUserStore } from '@/store/modules/user';
import {
  getApartmentDetail,
  updateApartmentReleaseStatus,
  deleteApartment
} from '@/api/landlord';
import type { ApartmentDetailVo, RoomInfo } from '@/api/landlord';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import StatCard from '@/components/landlord/StatCard/StatCard.vue';
import ReviewStatusBadge from '@/components/landlord/ReviewStatusBadge/ReviewStatusBadge.vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const publishing = ref(false);
const deleting = ref(false);
const apartmentDetail = ref<ApartmentDetailVo | null>(null);

// 计算属性
const showEmpty = computed(() => !loading.value && !apartmentDetail.value);

const locationText = computed(() => {
  if (!apartmentDetail.value) return '';
  const { provinceName, cityName, districtName } = apartmentDetail.value;
  return `${provinceName} ${cityName} ${districtName}`;
});

const roomStats = computed(() => {
  if (!apartmentDetail.value?.roomList) {
    return { total: 0, available: 0, occupied: 0 };
  }
  
  const rooms = apartmentDetail.value.roomList;
  const total = rooms.length;
  const available = rooms.filter(room => room.isRelease === 1).length;
  const occupied = total - available;
  
  return { total, available, occupied };
});

// 加载公寓详情
const loadApartmentDetail = async () => {
  try {
    loading.value = true;
    const apartmentId = Number(route.params.id);
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    const { data } = await getApartmentDetail(apartmentId, landlordId);
    apartmentDetail.value = data;
    
  } catch (error) {
    console.error('加载公寓详情失败:', error);
    showToast('加载公寓详情失败');
  } finally {
    loading.value = false;
  }
};

// 事件处理
const handleCall = () => {
  if (apartmentDetail.value?.phone) {
    window.location.href = `tel:${apartmentDetail.value.phone}`;
  }
};

const handleViewRooms = () => {
  router.push(`/landlord/rooms?apartmentId=${route.params.id}`);
};

const handleViewAvailableRooms = () => {
  router.push(`/landlord/rooms?apartmentId=${route.params.id}&status=available`);
};

const handleEdit = () => {
  router.push(`/landlord/apartments/form/${route.params.id}`);
};

const handleAddRoom = () => {
  router.push(`/landlord/rooms/form?apartmentId=${route.params.id}`);
};

const handleViewRoom = (room: RoomInfo) => {
  router.push(`/landlord/rooms/${room.id}`);
};

const handlePublish = async () => {
  try {
    await showConfirmDialog({
      title: '确认发布',
      message: '确定要发布这个公寓吗？发布后租客可以看到此公寓。'
    });
    
    publishing.value = true;
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId || !apartmentDetail.value) {
      showToast('操作失败');
      return;
    }
    
    await updateApartmentReleaseStatus(apartmentDetail.value.id!, 1, landlordId);
    apartmentDetail.value.isRelease = 1;
    showToast('发布成功');
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('发布公寓失败:', error);
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
      message: '确定要下架这个公寓吗？下架后租客将无法看到此公寓。'
    });
    
    publishing.value = true;
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId || !apartmentDetail.value) {
      showToast('操作失败');
      return;
    }
    
    await updateApartmentReleaseStatus(apartmentDetail.value.id!, 0, landlordId);
    apartmentDetail.value.isRelease = 0;
    showToast('下架成功');
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('下架公寓失败:', error);
      showToast('下架失败');
    }
  } finally {
    publishing.value = false;
  }
};

const handleDelete = async () => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除公寓"${apartmentDetail.value?.name}"吗？删除后相关房源也会被删除，此操作不可恢复。`,
      confirmButtonText: '删除',
      confirmButtonColor: '#ee0a24'
    });
    
    deleting.value = true;
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId || !apartmentDetail.value) {
      showToast('操作失败');
      return;
    }
    
    await deleteApartment(apartmentDetail.value.id!, landlordId);
    showToast('删除成功');
    router.back();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除公寓失败:', error);
      showToast('删除失败');
    }
  } finally {
    deleting.value = false;
  }
};

// 生命周期
onMounted(() => {
  loadApartmentDetail();
});
</script>

<style scoped lang="less">
.apartment-detail-images {
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.apartment-detail-info {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  
  &__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 12px;
  }
  
  &__name {
    font-size: 20px;
    font-weight: 600;
    color: #323233;
    margin: 0;
    flex: 1;
    margin-right: 12px;
  }
  
  &__location,
  &__address,
  &__contact {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    font-size: 14px;
    color: #646566;
  }
  
  &__contact {
    margin-bottom: 0;
    
    .contact-btn {
      margin-left: auto;
    }
  }
}

.apartment-detail-stats {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  
  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 12px;
  }
  
  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }
}

.apartment-detail-description {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  
  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 12px;
  }
  
  &__content {
    font-size: 14px;
    color: #646566;
    line-height: 1.6;
  }
}

.apartment-detail-facilities {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  
  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 12px;
  }
  
  &__grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

.facility-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  font-size: 14px;
  color: #323233;
}

.apartment-detail-labels {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  
  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 12px;
  }
  
  &__list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.label-tag {
  margin: 0;
}

.apartment-detail-fees {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  
  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 12px;
  }
  
  &__list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.fee-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  
  .fee-name {
    font-size: 14px;
    color: #323233;
  }
  
  .fee-value {
    font-size: 14px;
    font-weight: 600;
    color: #ee0a24;
  }
}

.apartment-detail-rooms {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }
  
  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
  }
  
  &__list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.room-item {
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
  
  &__info {
    flex: 1;
    margin-right: 12px;
  }
  
  &__number {
    font-size: 14px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 4px;
  }
  
  &__rent {
    font-size: 13px;
    color: #ee0a24;
    font-weight: 600;
  }
  
  &__status {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-right: 8px;
  }
}

.apartment-detail-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .action-btn {
    flex: 1;
  }
}

.apartment-detail-danger {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

// 响应式设计
@media (max-width: 375px) {
  .apartment-detail-stats {
    &__grid {
      grid-template-columns: 1fr;
      gap: 8px;
    }
  }
  
  .apartment-detail-facilities {
    &__grid {
      grid-template-columns: 1fr;
    }
  }
  
  .apartment-detail-actions {
    flex-direction: column;
  }
}
</style>