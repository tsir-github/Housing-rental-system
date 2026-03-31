<template>
  <div class="appointment-page">
    <!-- 导航栏 -->
    <van-nav-bar 
      title="预约管理" 
      left-arrow 
      @click-left="handleGoBack"
      fixed
      placeholder
    />

    <!-- 页面头部统计 -->
    <div class="appointment-header" v-if="!loading">
      <div class="appointment-header__stats">
        <StatCard
          :value="statistics?.totalAppointments || 0"
          label="总预约数"
          icon="calendar-o"
          icon-color="#1989fa"
          unit="条"
          size="small"
        />
        
        <StatCard
          :value="statistics?.waitingAppointments || statistics?.pendingAppointments || 0"
          label="待看房"
          icon="clock-o"
          icon-color="#ff976a"
          unit="条"
          size="small"
          clickable
          @click="handleFilterWaiting"
        />
        
        <StatCard
          :value="statistics?.viewedAppointments || 0"
          label="已看房"
          icon="fire-o"
          icon-color="#07c160"
          unit="条"
          size="small"
          clickable
          @click="handleFilterViewed"
        />
      </div>
      
      <div class="appointment-header__calendar">
        <van-calendar
          v-model:show="showCalendar"
          :min-date="minDate"
          :max-date="maxDate"
          @confirm="handleDateSelect"
        />
        
        <van-button 
          type="primary" 
          size="small" 
          icon="calendar-o"
          @click="showCalendar = true"
        >
          选择日期
        </van-button>
      </div>
    </div>

    <!-- 筛选标签 - 统一样式 -->
    <div class="filter-tabs">
      <van-tabs v-model:active="activeTab" @change="onTabChange">
        <van-tab title="全部" name="all" />
        <van-tab title="待看房" name="waiting" />
        <van-tab title="已取消" name="cancelled" />
        <van-tab title="已看房" name="viewed" />
      </van-tabs>
      
      <!-- 日期筛选显示 -->
      <div class="date-filter" v-if="selectedDate">
        <van-tag type="primary" closeable @close="handleClearDate">
          {{ formatSelectedDate(selectedDate) }}
        </van-tag>
      </div>
    </div>

    <!-- 预约列表 -->
    <div class="appointment-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <van-loading vertical>加载中...</van-loading>
      </div>
      
      <!-- 列表内容 -->
      <van-pull-refresh v-else v-model="refreshing" @refresh="handleRefresh">
        <van-list
          v-model:loading="listLoading"
          :finished="finished"
          finished-text="没有更多了"
          @load="handleLoadMore"
        >
          <div
            v-for="appointment in appointmentList"
            :key="appointment.id"
            class="appointment-item"
            @click="handleViewAppointment(appointment)"
          >
            <div class="appointment-item__header">
              <div class="appointment-item__user">
                <van-icon name="contact" size="16" color="#1989fa" />
                <span class="user-name">{{ appointment.name || '匿名用户' }}</span>
              </div>
              
              <div class="appointment-item__status">
                <van-tag 
                  :type="getStatusType(appointment.appointmentStatus)"
                  size="small"
                  round
                >
                  {{ getStatusText(appointment.appointmentStatus) }}
                </van-tag>
              </div>
            </div>
            
            <div class="appointment-item__content">
              <div class="appointment-item__room">
                <h4 class="room-name">{{ appointment.apartmentName }} - {{ appointment.roomNumber }}</h4>
              </div>
              
              <div class="appointment-item__time">
                <van-icon name="clock-o" size="14" color="#ff976a" />
                <span class="time-text">{{ formatAppointmentTime(appointment.appointmentTime) }}</span>
                <span class="time-status" :class="getTimeStatusClass(appointment.appointmentTime)">
                  {{ getTimeStatus(appointment.appointmentTime) }}
                </span>
              </div>
              
              <div class="appointment-item__guide" v-if="appointment.guideName">
                <van-icon name="user-o" size="14" color="#07c160" />
                <span>带看人：{{ appointment.guideName }}</span>
              </div>
              
              <div class="appointment-item__contact" v-if="appointment.phone">
                <van-icon name="phone-o" size="14" color="#969799" />
                <span>{{ appointment.phone }}</span>
                <van-button 
                  type="primary" 
                  size="mini"
                  @click.stop="handleCall(appointment)"
                >
                  拨打
                </van-button>
              </div>
              
              <div class="appointment-item__notes" v-if="appointment.additionalInfo">
                <van-icon name="notes-o" size="14" color="#969799" />
                <span>{{ appointment.additionalInfo }}</span>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="appointment-item__actions" v-if="appointment.appointmentStatus === 1">
              <van-button 
                type="danger" 
                size="mini"
                plain
                @click.stop="handleReject(appointment)"
              >
                拒绝
              </van-button>
              
              <van-button 
                type="primary" 
                size="mini"
                @click.stop="handleConfirm(appointment)"
              >
                确认
              </van-button>
            </div>
            
            <!-- 时间冲突提示 -->
            <div class="appointment-item__conflict" v-if="hasTimeConflict(appointment)">
              <van-icon name="warning-o" size="14" color="#ee0a24" />
              <span>时间冲突</span>
            </div>
            
            <van-icon name="arrow" size="16" color="#c8c9cc" class="appointment-item__arrow" />
          </div>
          
          <!-- 空状态 -->
          <EmptyState
            v-if="showEmpty"
            :type="emptyStateType"
            @action="handleEmptyAction"
          />
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 批量操作 -->
    <div class="appointment-batch" v-if="selectedAppointments.length > 0">
      <div class="batch-info">
        <span>已选择 {{ selectedAppointments.length }} 条预约</span>
        <van-button type="default" size="mini" @click="handleClearSelection">
          取消选择
        </van-button>
      </div>
      
      <div class="batch-actions">
        <van-button 
          type="success" 
          size="small"
          @click="handleBatchConfirm"
          :loading="batchProcessing"
        >
          批量确认
        </van-button>
        
        <van-button 
          type="danger" 
          size="small"
          @click="handleBatchReject"
          :loading="batchProcessing"
        >
          批量拒绝
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { NavigationHelper } from '@/utils/navigationHelper';
import { useLandlordStore } from '@/store/modules/landlord';
import { useUserStore } from '@/store/modules/user';
import {
  getLandlordAppointments,
  getAppointmentStatistics,
  processAppointment,
  confirmAppointmentAndCreateLease
} from '@/api/landlord';
import type { AppointmentItemVo, AppointmentStatistics } from '@/api/landlord';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import StatCard from '@/components/landlord/StatCard/StatCard.vue';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';

const router = useRouter();
const landlordStore = useLandlordStore();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const refreshing = ref(false);
const listLoading = ref(false);
const batchProcessing = ref(false);
const finished = ref(false);
const activeTab = ref('all');
const appointmentList = ref<AppointmentItemVo[]>([]);
const statistics = ref<AppointmentStatistics | null>(null);
const selectedAppointments = ref<number[]>([]);
const showCalendar = ref(false);
const selectedDate = ref<Date | null>(null);

// 日历配置
const minDate = new Date();
const maxDate = new Date(Date.now() + 90 * 24 * 60 * 60 * 1000); // 90天后

// 计算属性
const showEmpty = computed(() => 
  !loading.value && !listLoading.value && appointmentList.value.length === 0
);

const emptyType = computed(() => {
  return 'no-appointments';
});

const emptyStateType = computed(() => {
  return 'no-appointments';
});

// 状态映射
const getStatusType = (status: number) => {
  switch (status) {
    case 1: return 'warning';  // 待看房
    case 2: return 'default';  // 已取消
    case 3: return 'success';  // 已看房
    default: return 'default';
  }
};

const getStatusText = (status: number) => {
  switch (status) {
    case 1: return '待看房';
    case 2: return '已取消';
    case 3: return '已看房';
    default: return '未知';
  }
};

// 时间相关函数
const formatAppointmentTime = (timeString: string) => {
  const date = new Date(timeString);
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const formatSelectedDate = (date: Date) => {
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
};

const getTimeStatus = (timeString: string) => {
  const appointmentTime = new Date(timeString);
  const now = new Date();
  const diffHours = (appointmentTime.getTime() - now.getTime()) / (1000 * 60 * 60);
  
  if (diffHours < 0) {
    return '已过期';
  } else if (diffHours < 2) {
    return '即将开始';
  } else if (diffHours < 24) {
    return '今日';
  } else {
    return '';
  }
};

const getTimeStatusClass = (timeString: string) => {
  const appointmentTime = new Date(timeString);
  const now = new Date();
  const diffHours = (appointmentTime.getTime() - now.getTime()) / (1000 * 60 * 60);
  
  if (diffHours < 0) {
    return 'time-status--expired';
  } else if (diffHours < 2) {
    return 'time-status--urgent';
  } else if (diffHours < 24) {
    return 'time-status--today';
  } else {
    return '';
  }
};

// 时间冲突检查
const hasTimeConflict = (appointment: AppointmentItemVo) => {
  const appointmentTime = new Date(appointment.appointmentTime);
  
  return appointmentList.value.some(other => {
    if (other.id === appointment.id || other.appointmentStatus === 2) {
      return false; // 排除自己和已取消的预约
    }
    
    // 检查是否是同一个房源
    if (other.roomId !== appointment.roomId) {
      return false; // 不同房源不算冲突
    }
    
    const otherTime = new Date(other.appointmentTime);
    const timeDiff = Math.abs(appointmentTime.getTime() - otherTime.getTime());
    
    // 检查是否在1小时内有其他预约（同一房源）
    return timeDiff < 60 * 60 * 1000;
  });
};

// 批量时间冲突检查
const checkBatchTimeConflicts = (appointmentIds: number[]) => {
  const conflicts: { id: number; conflictWith: number[] }[] = [];
  
  appointmentIds.forEach(id => {
    const appointment = appointmentList.value.find(apt => apt.id === id);
    if (!appointment) return;
    
    const conflictWith = appointmentList.value
      .filter(other => 
        other.id !== id && 
        other.roomId === appointment.roomId &&
        other.appointmentStatus !== 2 &&
        appointmentIds.includes(other.id)
      )
      .map(other => {
        const appointmentTime = new Date(appointment.appointmentTime);
        const otherTime = new Date(other.appointmentTime);
        const timeDiff = Math.abs(appointmentTime.getTime() - otherTime.getTime());
        
        return timeDiff < 60 * 60 * 1000 ? other.id : null;
      })
      .filter(Boolean) as number[];
    
    if (conflictWith.length > 0) {
      conflicts.push({ id, conflictWith });
    }
  });
  
  return conflicts;
};

// 加载预约列表
const loadAppointmentList = async (isRefresh = false) => {
  try {
    if (isRefresh) {
      finished.value = false;
    }
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    const { data } = await getLandlordAppointments(landlordId);
    let filteredData = data;
    
    // 根据标签页筛选
    if (activeTab.value === 'waiting') {
      filteredData = data.filter(apt => apt.appointmentStatus === 1);
    } else if (activeTab.value === 'cancelled') {
      filteredData = data.filter(apt => apt.appointmentStatus === 2);
    } else if (activeTab.value === 'viewed') {
      filteredData = data.filter(apt => apt.appointmentStatus === 3);
    }
    
    // 根据选择的日期筛选
    if (selectedDate.value) {
      const selectedDateStr = selectedDate.value.toDateString();
      filteredData = filteredData.filter(apt => {
        const appointmentDate = new Date(apt.appointmentTime);
        return appointmentDate.toDateString() === selectedDateStr;
      });
    }
    
    appointmentList.value = filteredData;
    finished.value = true; // 由于API不支持分页，直接设为完成
    
  } catch (error) {
    console.error('加载预约列表失败:', error);
    showToast('加载预约列表失败');
  }
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      console.log('🔍 统计数据加载失败：landlordId不存在');
      return;
    }
    
    console.log('🔍 开始加载统计数据，landlordId:', landlordId);
    const { data } = await getAppointmentStatistics(landlordId);
    console.log('🔍 统计数据API响应:', data);
    
    statistics.value = data;
    console.log('🔍 统计数据已设置:', statistics.value);
  } catch (error) {
    console.error('🔍 加载统计数据失败:', error);
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    await Promise.all([
      loadAppointmentList(true),
      loadStatistics()
    ]);
  } catch (error) {
    console.error('初始化数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 事件处理
const handleRefresh = async () => {
  try {
    refreshing.value = true;
    await initData();
    showToast('刷新成功');
  } catch (error) {
    showToast('刷新失败');
  } finally {
    refreshing.value = false;
  }
};

const handleLoadMore = async () => {
  if (finished.value) return;
  
  try {
    listLoading.value = true;
    await loadAppointmentList();
  } finally {
    listLoading.value = false;
  }
};

// 标签页切换处理
const onTabChange = (name: string | number) => {
  console.log('🔄 切换标签页:', name)
  
  try {
    // 清除选择并重新加载数据
    selectedAppointments.value = []
    handleRefresh()
  } catch (error) {
    console.error('标签切换失败:', error)
    showToast('切换失败，请重试')
  }
}

const handleFilterWaiting = () => {
  activeTab.value = 'waiting';
};

const handleFilterViewed = () => {
  activeTab.value = 'viewed';
};

const handleFilterToday = () => {
  const today = new Date();
  selectedDate.value = today;
  handleRefresh();
};

const handleDateSelect = (date: Date) => {
  selectedDate.value = date;
  showCalendar.value = false;
  handleRefresh();
};

const handleClearDate = () => {
  selectedDate.value = null;
  handleRefresh();
};

const handleViewAppointment = (appointment: AppointmentItemVo) => {
  router.push(`/landlord/appointments/${appointment.id}`);
};

const handleCall = (appointment: AppointmentItemVo) => {
  if (appointment.phone) {
    window.location.href = `tel:${appointment.phone}`;
  }
};

const handleConfirm = async (appointment: AppointmentItemVo) => {
  try {
    // 先确认预约
    await showConfirmDialog({
      title: '确认预约',
      message: `确定要确认"${appointment.name}"的预约吗？确认后是否要创建租约？`,
      confirmButtonText: '确认并创建租约',
      cancelButtonText: '仅确认预约'
    });
    
    // 用户选择确认并创建租约
    await processAppointment({
      appointmentId: appointment.id,
      status: 3, // 已看房（确认后变为已看房）
      remarks: '预约已确认，准备创建租约',
      landlordId: userStore.userInfo?.id || 1
    });
    
    showToast('预约已确认');
    
    // 更新列表中的状态
    const index = appointmentList.value.findIndex(item => item.id === appointment.id);
    if (index !== -1) {
      appointmentList.value[index].appointmentStatus = 3;
    }
    
    // 刷新统计数据
    await loadStatistics();
    
    // 跳转到租约创建页面，并传递预约信息
    router.push({
      name: 'LandlordLeaseForm',
      params: { id: 'new' },
      query: {
        source: 'appointment',
        appointmentId: appointment.id,
        name: appointment.name,
        phone: appointment.phone,
        apartmentId: appointment.apartmentId,
        roomId: appointment.roomId
      }
    });
    
  } catch (error: any) {
    if (error === 'cancel') {
      // 用户选择仅确认预约
      try {
        await processAppointment({
          appointmentId: appointment.id,
          status: 3, // 已看房
          remarks: '预约已确认',
          landlordId: userStore.userInfo?.id || 1
        });
        
        showToast('预约已确认');
        
        // 更新列表中的状态
        const index = appointmentList.value.findIndex(item => item.id === appointment.id);
        if (index !== -1) {
          appointmentList.value[index].appointmentStatus = 3;
        }
        
        // 刷新统计数据
        await loadStatistics();
        
      } catch (confirmError) {
        console.error('确认预约失败:', confirmError);
        showToast('确认预约失败');
      }
    } else {
      console.error('确认预约失败:', error);
      showToast('操作失败');
    }
  }
};

const handleReject = async (appointment: AppointmentItemVo) => {
  try {
    await showConfirmDialog({
      title: '拒绝预约',
      message: `确定要拒绝"${appointment.name}"的预约吗？`,
      confirmButtonText: '拒绝',
      cancelButtonText: '取消'
    });
    
    await processAppointment({
      appointmentId: appointment.id,
      status: 2, // 已取消（后端状态码2表示已取消）
      remarks: '预约被拒绝',
      landlordId: userStore.userInfo?.id || 1
    });
    
    showToast('预约已拒绝');
    
    // 更新列表中的状态
    const index = appointmentList.value.findIndex(item => item.id === appointment.id);
    if (index !== -1) {
      appointmentList.value[index].appointmentStatus = 2;
    }
    
    // 刷新统计数据
    await loadStatistics();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('拒绝预约失败:', error);
      showToast('操作失败');
    }
  }
};

const handleClearSelection = () => {
  selectedAppointments.value = [];
};

const handleBatchConfirm = async () => {
  try {
    // 检查时间冲突
    const conflicts = checkBatchTimeConflicts(selectedAppointments.value);
    
    let confirmMessage = `确定要确认选中的 ${selectedAppointments.value.length} 条预约吗？`;
    
    if (conflicts.length > 0) {
      confirmMessage += `\n\n注意：检测到 ${conflicts.length} 条预约存在时间冲突，确认后可能影响带看安排。`;
    }
    
    await showConfirmDialog({
      title: '批量确认预约',
      message: confirmMessage,
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    });
    
    batchProcessing.value = true;
    
    // 批量处理预约
    const promises = selectedAppointments.value.map(id => 
      processAppointment({
        appointmentId: id,
        status: 3, // 已看房（确认后变为已看房）
        remarks: '批量确认',
        landlordId: userStore.userInfo?.id || 1
      })
    );
    
    await Promise.all(promises);
    
    if (conflicts.length > 0) {
      showToast('批量操作成功，请注意时间冲突预约的安排');
    } else {
      showToast('批量操作成功');
    }
    
    // 清除选择并刷新数据
    selectedAppointments.value = [];
    await initData();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量确认失败:', error);
      showToast('批量操作失败');
    }
  } finally {
    batchProcessing.value = false;
  }
};

const handleBatchReject = async () => {
  try {
    await showConfirmDialog({
      title: '批量拒绝预约',
      message: `确定要拒绝选中的 ${selectedAppointments.value.length} 条预约吗？`,
      confirmButtonText: '拒绝',
      cancelButtonText: '取消'
    });
    
    batchProcessing.value = true;
    
    // 批量处理预约
    const promises = selectedAppointments.value.map(id => 
      processAppointment({
        appointmentId: id,
        status: 2, // 已取消（后端状态码2表示已取消）
        remarks: '批量拒绝',
        landlordId: userStore.userInfo?.id || 1
      })
    );
    
    await Promise.all(promises);
    showToast('批量操作成功');
    
    // 清除选择并刷新数据
    selectedAppointments.value = [];
    await initData();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败:', error);
      showToast('批量操作失败');
    }
  } finally {
    batchProcessing.value = false;
  }
};

const handleEmptyAction = () => {
  // 空状态时的操作，可以跳转到房源管理
  router.push('/landlord/rooms');
};

// 返回按钮处理
const handleGoBack = () => {
  console.log('🧭 预约管理页面返回按钮被点击');
  NavigationHelper.smartGoBack(router);
};

// 生命周期
onMounted(() => {
  initData();
});
</script>

<style scoped lang="less">
/* 固定头部区域，防止跳动 */
.appointment-list-header-fixed {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #f7f8fa;
  padding-bottom: 8px;
  /* 强制固定高度，防止任何布局变化 */
  min-height: 120px;
  max-height: 120px;
  overflow: hidden;
  /* 防止重排和重绘 */
  contain: layout style;
  will-change: auto;
}

.filter-tabs {
  background: white;
  padding: 0 16px;
  margin-bottom: 8px;
  /* 强制固定高度，绝对防止跳动 */
  height: 44px;
  min-height: 44px;
  max-height: 44px;
  display: flex;
  align-items: center;
  /* 强制布局稳定性 */
  position: relative;
  z-index: 1;
  overflow: hidden;
  /* 防止内容变化影响布局 */
  contain: layout style;
  /* 优化渲染性能 */
  transform: translateZ(0);
}

.filter-tabs :deep(.van-tabs__nav) {
  padding-bottom: 0;
  min-height: 44px;
  width: 100%; /* 确保导航栏占满宽度 */
}

.filter-tabs :deep(.van-tabs__line) {
  width: 20px;
  background: #1989fa;
}

.filter-tabs :deep(.van-tab) {
  color: #646566;
  font-size: 14px;
  flex: 1; /* 让每个标签平均分配宽度 */
  text-align: center;
  padding: 0 8px; /* 增加内边距 */
  white-space: nowrap; /* 防止文字换行 */
  overflow: visible; /* 确保文字可见 */
}

.filter-tabs :deep(.van-tab--active) {
  color: #1989fa;
  font-weight: 500;
}

.filter-tabs .date-filter {
  padding: 8px 0;
  text-align: center;
}

.appointment-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.appointment-header {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
  
  &__stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin-bottom: 12px;
  }
  
  &__calendar {
    display: flex;
    justify-content: center;
  }
}

.appointment-filters {
  background: white;
  margin-bottom: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  :deep(.van-tabs__nav) {
    background: transparent;
    padding: 0 16px;
  }
  
  :deep(.van-tab) {
    flex: 1;
    text-align: center;
  }
  
  .date-filter {
    padding: 8px 16px;
    border-top: 1px solid #f0f0f0;
    text-align: center;
  }
}

.appointment-content {
  padding: 0 16px;
  /* 强制最小高度，防止布局塌陷 */
  min-height: 400px;
  max-height: none;
  padding-bottom: 20px;
  /* 防止内容变化影响整体布局 */
  contain: layout;
  /* 确保内容区域稳定 */
  position: relative;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
  color: #999;
}

.appointment-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
    
    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: #323233;
    }
  }
  
  &__content {
    margin-bottom: 12px;
  }
  
  &__room {
    margin-bottom: 8px;
    
    .room-name {
      font-size: 16px;
      font-weight: 600;
      color: #323233;
      margin: 0;
    }
  }
  
  &__time,
  &__guide,
  &__contact,
  &__notes {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 6px;
    font-size: 13px;
    color: #646566;
    
    .time-text {
      font-weight: 500;
      color: #323233;
    }
    
    .time-status {
      font-size: 12px;
      padding: 2px 6px;
      border-radius: 4px;
      
      &--expired {
        background: #fef2f2;
        color: #ee0a24;
      }
      
      &--urgent {
        background: #fff7ed;
        color: #ff976a;
      }
      
      &--today {
        background: #e6f7ff;
        color: #1989fa;
      }
    }
  }
  
  &__contact {
    justify-content: space-between;
  }
  
  &__actions {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
  }
  
  &__conflict {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #ee0a24;
    background: #fef2f2;
    padding: 4px 8px;
    border-radius: 4px;
    margin-bottom: 8px;
  }
  
  &__arrow {
    position: absolute;
    top: 50%;
    right: 16px;
    transform: translateY(-50%);
  }
}

.appointment-batch {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 16px;
  border-top: 1px solid #ebedf0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  
  .batch-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
    color: #646566;
  }
  
  .batch-actions {
    display: flex;
    gap: 12px;
    
    .van-button {
      flex: 1;
    }
  }
}

// 响应式设计
@media (max-width: 375px) {
  .appointment-header {
    &__stats {
      gap: 6px;
    }
  }
  
  .appointment-item {
    padding: 12px;
    
    &__header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
    
    &__actions {
      flex-wrap: wrap;
      gap: 6px;
    }
  }
}
</style>