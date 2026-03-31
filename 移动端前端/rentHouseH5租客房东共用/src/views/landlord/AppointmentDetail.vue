<template>
  <div class="appointment-detail">
    <!-- 导航栏 -->
    <van-nav-bar
      title="预约详情"
      left-arrow
      @click-left="handleGoBack"
      fixed
      placeholder
    />
    
    <PageContainer :loading="loading" :show-empty="showEmpty" :show-header="false" empty-type="error">
    <!-- 预约状态卡片 -->
    <div class="appointment-detail-status" v-if="appointmentDetail">
      <div class="status-header">
        <van-tag 
          :type="getStatusType(appointmentDetail.appointmentStatus)"
          size="large"
        >
          {{ getStatusText(appointmentDetail.appointmentStatus) }}
        </van-tag>
        
        <div class="status-time">
          <van-icon name="clock-o" size="14" color="#969799" />
          <span>{{ formatDateTime(appointmentDetail.appointmentTime) }}</span>
        </div>
      </div>
      
      <div class="status-countdown" v-if="appointmentDetail.appointmentStatus === 2 && isUpcoming">
        <van-count-down 
          :time="countdownTime"
          format="DD天HH时mm分ss秒"
        >
          <template #default="timeData">
            <div class="countdown-display">
              <van-icon name="clock-o" size="16" color="#1989fa" />
              <span>距离预约时间还有：</span>
              <span class="countdown-time">
                {{ timeData.days }}天{{ timeData.hours }}时{{ timeData.minutes }}分
              </span>
            </div>
          </template>
        </van-count-down>
      </div>
    </div>

    <!-- 租客信息 -->
    <div class="appointment-detail-user" v-if="appointmentDetail">
      <div class="section-title">
        <van-icon name="contact" size="16" color="#1989fa" />
        <span>租客信息</span>
      </div>
      
      <div class="user-card">
        <div class="user-avatar" v-if="appointmentDetail.userInfo?.avatarUrl">
          <van-image
            :src="appointmentDetail.userInfo.avatarUrl"
            width="50"
            height="50"
            round
            fit="cover"
          />
        </div>
        <div class="user-info">
          <div class="user-item">
            <span class="user-label">姓名</span>
            <span class="user-value">{{ appointmentDetail.name || '未提供' }}</span>
          </div>
          
          <div class="user-item">
            <span class="user-label">联系电话</span>
            <div class="user-phone">
              <span class="user-value">{{ appointmentDetail.phone || '未提供' }}</span>
              <van-button 
                v-if="appointmentDetail.phone"
                type="primary" 
                size="mini"
                @click="handleCall"
              >
                拨打
              </van-button>
            </div>
          </div>
          
          <div class="user-item" v-if="appointmentDetail.userInfo?.nickname">
            <span class="user-label">昵称</span>
            <span class="user-value">{{ appointmentDetail.userInfo.nickname }}</span>
          </div>
          
          <div class="user-item" v-if="appointmentDetail.userInfo?.status">
            <span class="user-label">用户状态</span>
            <van-tag :type="appointmentDetail.userInfo.status === 1 ? 'success' : 'danger'" size="small">
              {{ appointmentDetail.userInfo.status === 1 ? '正常' : '禁用' }}
            </van-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 预约信息 -->
    <div class="appointment-detail-time" v-if="appointmentDetail">
      <div class="section-title">
        <van-icon name="clock-o" size="16" color="#ff976a" />
        <span>预约信息</span>
      </div>
      
      <div class="time-card">
        <div class="time-info">
          <div class="time-item">
            <span class="time-label">预约时间</span>
            <span class="time-value">{{ formatDateTime(appointmentDetail.appointmentTime) }}</span>
          </div>
          
          <div class="time-item">
            <span class="time-label">创建时间</span>
            <span class="time-value">{{ formatDateTime(appointmentDetail.createTime) }}</span>
          </div>
          
          <div class="time-item" v-if="appointmentDetail.updateTime && appointmentDetail.updateTime !== appointmentDetail.createTime">
            <span class="time-label">更新时间</span>
            <span class="time-value">{{ formatDateTime(appointmentDetail.updateTime) }}</span>
          </div>
          
          <div class="time-status-info">
            <span class="status-label">状态</span>
            <van-tag 
              :type="getStatusType(appointmentDetail.appointmentStatus)"
              size="medium"
            >
              {{ getStatusText(appointmentDetail.appointmentStatus) }}
            </van-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 预约备注 -->
    <div class="appointment-detail-note" v-if="appointmentDetail?.additionalInfo">
      <div class="section-title">
        <van-icon name="notes-o" size="16" color="#ff976a" />
        <span>预约备注</span>
      </div>
      
      <div class="note-content">
        {{ appointmentDetail.additionalInfo }}
      </div>
    </div>

    <!-- 关联信息（可展开） -->
    <div class="appointment-detail-related" v-if="appointmentDetail">
      <van-collapse v-model="activeCollapse">
        <van-collapse-item title="查看房源信息" name="room-detail">
          <template #icon>
            <van-icon name="home-o" size="16" color="#07c160" />
          </template>
          
          <!-- 房间详细信息 -->
          <div class="room-detail-section" v-if="appointmentDetail.roomInfo">
            <div class="subsection-title">
              <van-icon name="home-o" size="14" color="#07c160" />
              <span>房间信息</span>
            </div>
            
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">房间号</span>
                <span class="detail-value">{{ appointmentDetail.roomInfo.roomNumber || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">租金</span>
                <span class="detail-value rent-price">{{ appointmentDetail.roomInfo.rent || '-' }}元/月</span>
              </div>
              <div class="detail-item" v-if="appointmentDetail.roomInfo.area">
                <span class="detail-label">面积</span>
                <span class="detail-value">{{ appointmentDetail.roomInfo.area }}㎡</span>
              </div>
              <div class="detail-item" v-if="appointmentDetail.roomInfo.roomType">
                <span class="detail-label">房型</span>
                <span class="detail-value">{{ appointmentDetail.roomInfo.roomType }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">发布状态</span>
                <van-tag :type="appointmentDetail.roomInfo.isRelease === 1 ? 'success' : 'warning'" size="small">
                  {{ appointmentDetail.roomInfo.isRelease === 1 ? '已发布' : '未发布' }}
                </van-tag>
              </div>
            </div>
          </div>

          <!-- 公寓详细信息 -->
          <div class="apartment-detail-section" v-if="appointmentDetail.apartmentItemVo">
            <div class="subsection-title">
              <van-icon name="building-o" size="14" color="#1989fa" />
              <span>公寓信息</span>
            </div>
            
            <div class="apartment-info">
              <div class="apartment-header">
                <h4 class="apartment-name">{{ appointmentDetail.apartmentItemVo.name }}</h4>
                <div class="apartment-address">
                  <van-icon name="location-o" size="12" color="#969799" />
                  <span>{{ getFullAddress() }}</span>
                </div>
              </div>
              
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="detail-label">建筑面积</span>
                  <span class="detail-value">{{ appointmentDetail.apartmentItemVo.totalArea || '-' }}㎡</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">房间总数</span>
                  <span class="detail-value">{{ appointmentDetail.apartmentItemVo.roomCount || '-' }}间</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">可用房间</span>
                  <span class="detail-value">{{ appointmentDetail.apartmentItemVo.availableRoomCount || '-' }}间</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">最低租金</span>
                  <span class="detail-value rent-price">{{ appointmentDetail.apartmentItemVo.minRent || '-' }}元/月</span>
                </div>
              </div>

              <!-- 公寓图片 -->
              <div class="apartment-images" v-if="appointmentDetail.apartmentItemVo.graphVoList?.length">
                <div class="subsection-title">
                  <van-icon name="photo-o" size="14" color="#ff976a" />
                  <span>公寓图片</span>
                </div>
                <van-swipe :autoplay="3000" indicator-color="white">
                  <van-swipe-item v-for="(image, index) in appointmentDetail.apartmentItemVo.graphVoList" :key="index">
                    <van-image
                      :src="image.url"
                      width="100%"
                      height="200"
                      fit="cover"
                      @click="previewImages(index)"
                    />
                  </van-swipe-item>
                </van-swipe>
              </div>

              <!-- 公寓配套设施 -->
              <div class="apartment-facilities" v-if="appointmentDetail.apartmentItemVo.facilityInfoList?.length">
                <div class="subsection-title">
                  <van-icon name="service-o" size="14" color="#07c160" />
                  <span>配套设施</span>
                </div>
                <div class="facility-tags">
                  <van-tag 
                    v-for="facility in appointmentDetail.apartmentItemVo.facilityInfoList" 
                    :key="facility.id"
                    type="primary"
                    size="small"
                    class="facility-tag"
                  >
                    {{ facility.name }}
                  </van-tag>
                </div>
              </div>

              <!-- 公寓标签 -->
              <div class="apartment-labels" v-if="appointmentDetail.apartmentItemVo.labelInfoList?.length">
                <div class="subsection-title">
                  <van-icon name="label-o" size="14" color="#ff976a" />
                  <span>公寓标签</span>
                </div>
                <div class="label-tags">
                  <van-tag 
                    v-for="label in appointmentDetail.apartmentItemVo.labelInfoList" 
                    :key="label.id"
                    type="success"
                    size="small"
                    class="label-tag"
                  >
                    {{ label.name }}
                  </van-tag>
                </div>
              </div>
            </div>
          </div>
        </van-collapse-item>
      </van-collapse>
    </div>

    <!-- 操作按钮 -->
    <div class="appointment-detail-actions" v-if="appointmentDetail?.appointmentStatus === 1">
      <van-button 
        type="danger" 
        size="large"
        plain
        @click="handleReject"
        :loading="rejecting"
        class="action-btn"
      >
        拒绝预约
      </van-button>
      
      <van-button 
        type="success" 
        size="large"
        @click="handleConfirm"
        :loading="confirming"
        class="action-btn"
      >
        确认预约
      </van-button>
    </div>

    <!-- 已确认状态的操作 -->
    <div class="appointment-detail-confirmed" v-else-if="appointmentDetail?.appointmentStatus === 2">
      <van-button 
        type="primary" 
        size="large"
        @click="handleComplete"
        :loading="completing"
        class="action-btn"
      >
        标记完成
      </van-button>
      
      <van-button 
        type="default" 
        size="large"
        @click="handleReschedule"
        class="action-btn"
      >
        重新安排
      </van-button>
    </div>

    <!-- 联系租客 -->
    <div class="appointment-detail-contact" v-if="appointmentDetail?.phone">
      <van-cell-group>
        <van-cell 
          title="联系租客" 
          :value="appointmentDetail.phone"
          is-link
          @click="handleCall"
        >
          <template #icon>
            <van-icon name="phone-o" size="16" color="#07c160" />
          </template>
        </van-cell>
      </van-cell-group>
    </div>
  </PageContainer>
  </div>
</template><script
 setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { useUserStore } from '@/store/modules/user';
import {
  getAppointmentDetail,
  getLandlordAppointmentDetail,
  processAppointment,
  confirmAppointmentAndCreateLease
} from '@/api/landlord';
import type { AppointmentDetailVo } from '@/api/landlord';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import { NavigationHelper } from '@/utils/navigationHelper';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const confirming = ref(false);
const rejecting = ref(false);
const completing = ref(false);
const appointmentDetail = ref<AppointmentDetailVo | null>(null);
const activeCollapse = ref<string[]>([]); // 控制折叠面板展开状态

// 计算属性
const showEmpty = computed(() => !loading.value && !appointmentDetail.value);

// 是否即将到来的预约
const isUpcoming = computed(() => {
  if (!appointmentDetail.value) return false;
  const appointmentTime = new Date(appointmentDetail.value.appointmentTime).getTime();
  const now = Date.now();
  return appointmentTime > now;
});

// 倒计时时间
const countdownTime = computed(() => {
  if (!appointmentDetail.value || !isUpcoming.value) return 0;
  const appointmentTime = new Date(appointmentDetail.value.appointmentTime).getTime();
  const now = Date.now();
  return Math.max(0, appointmentTime - now);
});

// 格式化函数
const formatDateTime = (dateString?: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    weekday: 'short'
  });
};

const getStatusType = (status: number) => {
  switch (status) {
    case 1: return 'warning'; // 待确认
    case 2: return 'primary'; // 已确认
    case 3: return 'success'; // 已完成
    case 4: return 'default'; // 已取消
    default: return 'default';
  }
};

const getStatusText = (status: number) => {
  switch (status) {
    case 1: return '待确认';
    case 2: return '已确认';
    case 3: return '已完成';
    case 4: return '已取消';
    default: return '未知';
  }
};

const getGuideTypeText = (guideType: number) => {
  switch (guideType) {
    case 1: return '房东带看';
    case 2: return '管理员带看';
    default: return '未知';
  }
};

// 房源信息相关方法
const getRoomAddress = () => {
  if (appointmentDetail.value?.roomInfo?.address) {
    return appointmentDetail.value.roomInfo.address;
  }
  if (appointmentDetail.value?.apartmentInfo?.addressDetail) {
    return appointmentDetail.value.apartmentInfo.addressDetail;
  }
  return '地址信息暂无';
};

const formatRent = () => {
  if (appointmentDetail.value?.roomInfo?.rent) {
    return `${appointmentDetail.value.roomInfo.rent}元/月`;
  }
  return '租金待定';
};

const getRoomStatusType = () => {
  if (!appointmentDetail.value?.roomInfo) return 'default';
  
  const isRelease = appointmentDetail.value.roomInfo.isRelease;
  if (isRelease === 1) return 'success';
  if (isRelease === 0) return 'warning';
  return 'default';
};

const getRoomStatusText = () => {
  if (!appointmentDetail.value?.roomInfo) return '状态未知';
  
  const isRelease = appointmentDetail.value.roomInfo.isRelease;
  if (isRelease === 1) return '已发布';
  if (isRelease === 0) return '未发布';
  return '状态未知';
};

const getReviewStatusType = (reviewStatus: number) => {
  switch (reviewStatus) {
    case 1: return 'warning';  // 待审核
    case 2: return 'success';  // 审核通过
    case 3: return 'danger';   // 审核拒绝
    default: return 'default';
  }
};

const getReviewStatusText = (reviewStatus: number) => {
  switch (reviewStatus) {
    case 1: return '待审核';
    case 2: return '审核通过';
    case 3: return '审核拒绝';
    default: return '未知';
  }
};

const handleRefreshRoomInfo = () => {
  // 重新加载预约详情以获取最新的房源信息
  loadAppointmentDetail();
};

// 获取完整地址
const getFullAddress = () => {
  if (!appointmentDetail.value?.apartmentItemVo) return '地址信息暂无';
  
  const apartment = appointmentDetail.value.apartmentItemVo;
  const addressParts = [
    apartment.provinceName,
    apartment.cityName,
    apartment.districtName,
    apartment.addressDetail
  ].filter(Boolean);
  
  return addressParts.join('') || '地址信息暂无';
};

// 预览图片
const previewImages = (startIndex: number) => {
  if (!appointmentDetail.value?.apartmentItemVo?.graphVoList) return;
  
  const images = appointmentDetail.value.apartmentItemVo.graphVoList.map(item => item.url);
  // 这里可以使用 Vant 的 ImagePreview 组件
  // ImagePreview({ images, startPosition: startIndex });
};// 加载预约详情

const loadAppointmentDetail = async () => {
  try {
    loading.value = true;
    const appointmentId = Number(route.params.id);
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    // 使用房东专用的预约详情接口
    const { data } = await getLandlordAppointmentDetail(appointmentId, landlordId);
    appointmentDetail.value = data;
    
  } catch (error) {
    console.error('加载预约详情失败:', error);
    showToast('加载预约详情失败');
  } finally {
    loading.value = false;
  }
};

// 事件处理
const handleCall = () => {
  if (appointmentDetail.value?.phone) {
    window.location.href = `tel:${appointmentDetail.value.phone}`;
  }
};

const handleViewRoom = () => {
  if (appointmentDetail.value?.roomId) {
    router.push(`/landlord/rooms/${appointmentDetail.value.roomId}`);
  }
};

const handleConfirm = async () => {
  try {
    await showConfirmDialog({
      title: '确认预约',
      message: `确定要确认${appointmentDetail.value?.name}的看房预约吗？确认后是否要创建租约？`,
      confirmButtonText: '确认并创建租约',
      cancelButtonText: '仅确认预约'
    });
    
    confirming.value = true;
    
    if (!appointmentDetail.value) {
      showToast('操作失败');
      return;
    }
    
    // 用户选择确认并创建租约
    await processAppointment({
      appointmentId: appointmentDetail.value.id,
      status: 2, // 已确认
      remarks: '预约已确认，准备创建租约',
      landlordId: userStore.userInfo?.id || 1
    });
    
    // 更新预约状态
    appointmentDetail.value.appointmentStatus = 2;
    
    showToast('预约已确认');
    
    // 跳转到租约创建页面，并传递预约信息
    router.push({
      name: 'LandlordLeaseForm',
      params: { id: 'new' },
      query: {
        source: 'appointment',
        appointmentId: appointmentDetail.value.id,
        name: appointmentDetail.value.name,
        phone: appointmentDetail.value.phone,
        apartmentId: appointmentDetail.value.apartmentId,
        roomId: appointmentDetail.value.roomId
      }
    });
    
  } catch (error: any) {
    if (error === 'cancel') {
      // 用户选择仅确认预约
      try {
        if (!appointmentDetail.value) {
          showToast('操作失败');
          return;
        }
        
        await processAppointment({
          appointmentId: appointmentDetail.value.id,
          status: 2, // 已确认
          remarks: '预约已确认',
          landlordId: userStore.userInfo?.id || 1
        });
        
        // 更新预约状态
        appointmentDetail.value.appointmentStatus = 2;
        
        showToast('预约已确认');
        
      } catch (confirmError) {
        console.error('确认预约失败:', confirmError);
        showToast('确认预约失败');
      }
    } else {
      console.error('确认预约失败:', error);
      showToast('操作失败');
    }
  } finally {
    confirming.value = false;
  }
};

const handleReject = async () => {
  try {
    await showConfirmDialog({
      title: '拒绝预约',
      message: `确定要拒绝${appointmentDetail.value?.name}的看房预约吗？`,
      confirmButtonText: '拒绝',
      cancelButtonText: '取消'
    });
    
    rejecting.value = true;
    
    if (!appointmentDetail.value) {
      showToast('操作失败');
      return;
    }
    
    await processAppointment({
      appointmentId: appointmentDetail.value.id,
      status: 4, // 已取消
      remarks: '预约被拒绝',
      landlordId: userStore.userInfo?.id || 1
    });
    
    // 更新预约状态
    appointmentDetail.value.appointmentStatus = 4;
    
    showToast('预约已拒绝');
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('拒绝预约失败:', error);
      showToast('操作失败');
    }
  } finally {
    rejecting.value = false;
  }
};

const handleComplete = async () => {
  try {
    await showConfirmDialog({
      title: '标记完成',
      message: '确定要标记这次看房为已完成吗？',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    });
    
    completing.value = true;
    
    if (!appointmentDetail.value) {
      showToast('操作失败');
      return;
    }
    
    await processAppointment({
      appointmentId: appointmentDetail.value.id,
      status: 3, // 已完成
      remarks: '看房已完成',
      landlordId: userStore.userInfo?.id || 1
    });
    
    // 更新预约状态
    appointmentDetail.value.appointmentStatus = 3;
    
    showToast('已标记为完成');
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('标记完成失败:', error);
      showToast('操作失败');
    }
  } finally {
    completing.value = false;
  }
};

const handleReschedule = () => {
  // TODO: 实现重新安排功能
  showToast('重新安排功能开发中');
};

const handleGoBack = () => {
  console.log('🧭 预约详情页面返回按钮被点击');
  NavigationHelper.smartGoBack(router);
};

// 生命周期
onMounted(() => {
  loadAppointmentDetail();
});
</script>

<style scoped lang="less">
.appointment-detail-status {
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
  
  .status-countdown {
    padding-top: 12px;
    border-top: 1px solid #ebedf0;
  }
  
  .countdown-display {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: #1989fa;
    
    .countdown-time {
      font-weight: 600;
    }
  }
}

.appointment-detail-user,
.appointment-detail-room,
.appointment-detail-time,
.appointment-detail-note,
.appointment-detail-guide {
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

.user-card,
.guide-card {
  .user-info,
  .guide-info {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .user-item,
  .guide-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .user-label,
    .guide-label {
      font-size: 14px;
      color: #646566;
      min-width: 80px;
    }
    
    .user-value,
    .guide-value {
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
}

.room-card {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebedf0;
  
  &:hover {
    background: #f2f3f5;
    border-color: #1989fa;
    box-shadow: 0 2px 8px rgba(25, 137, 250, 0.1);
  }
  
  .room-info {
    flex: 1;
    margin-right: 12px;
  }
  
  .room-name {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin: 0 0 6px 0;
    
    .room-number {
      font-size: 14px;
      color: #969799;
      margin-left: 4px;
    }
  }
  
  .room-address {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #969799;
    margin-bottom: 8px;
  }
  
  .room-details {
    .detail-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;
      
      .detail-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #646566;
        background: #ebedf0;
        padding: 4px 8px;
        border-radius: 4px;
        
        &.rent-price {
          background: #fff7ed;
          color: #ff976a;
          font-weight: 500;
        }
      }
    }
    
    .room-status {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: 4px;
    }
  }
  
  .room-action {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    
    .action-text {
      font-size: 10px;
      color: #969799;
    }
  }
}

.room-empty {
  padding: 20px;
  text-align: center;
}

.time-card {
  .time-info {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .time-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .time-label {
      font-size: 14px;
      color: #646566;
      min-width: 80px;
    }
    
    .time-value {
      font-size: 14px;
      color: #323233;
      font-weight: 500;
    }
  }
  
  .time-status-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 8px;
    border-top: 1px solid #ebedf0;
    
    .status-label {
      font-size: 14px;
      color: #646566;
      min-width: 80px;
    }
  }
}

.note-content {
  font-size: 14px;
  color: #646566;
  line-height: 1.6;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 6px;
}

.appointment-detail-actions,
.appointment-detail-confirmed {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .action-btn {
    flex: 1;
  }
}

.appointment-detail-contact {
  margin-bottom: 16px;
}

// 房源详情展开区域样式
.appointment-detail-related {
  margin-bottom: 16px;
  
  .room-detail-section,
  .apartment-detail-section {
    background: white;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 12px;
  }
  
  .subsection-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebedf0;
  }
  
  .detail-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin-bottom: 16px;
    
    .detail-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .detail-label {
        font-size: 12px;
        color: #969799;
      }
      
      .detail-value {
        font-size: 14px;
        color: #323233;
        font-weight: 500;
        
        &.rent-price {
          color: #ff976a;
          font-weight: 600;
        }
      }
    }
  }
  
  .apartment-info {
    .apartment-header {
      margin-bottom: 16px;
      
      .apartment-name {
        font-size: 18px;
        font-weight: 600;
        color: #323233;
        margin: 0 0 8px 0;
      }
      
      .apartment-address {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #646566;
      }
    }
  }
  
  .apartment-images {
    margin-bottom: 16px;
    
    :deep(.van-swipe) {
      border-radius: 8px;
      overflow: hidden;
    }
  }
  
  .apartment-facilities,
  .apartment-labels {
    margin-bottom: 16px;
    
    .facility-tags,
    .label-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 8px;
      
      .facility-tag,
      .label-tag {
        margin: 0;
      }
    }
  }
}

// 响应式设计
@media (max-width: 375px) {
  .appointment-detail-status {
    .status-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
  }
  
  .room-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    
    .room-info {
      margin-right: 0;
    }
  }
  
  .appointment-detail-actions,
  .appointment-detail-confirmed {
    flex-direction: column;
  }
}
</style>