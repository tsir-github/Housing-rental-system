import { computed, onMounted, onUnmounted } from 'vue';
import { 
  useLandlordStore, 
  useApartmentStore, 
  useRoomStore, 
  useApplicationStore, 
  useAppointmentStore 
} from '@/store/modules';
import { useUserStore } from '@/store/modules/user';
import { UserTypeUtils } from '@/utils/userType';

/**
 * 房东数据管理组合函数
 */
export function useLandlord() {
  const userStore = useUserStore();
  const landlordStore = useLandlordStore();
  const apartmentStore = useApartmentStore();
  const roomStore = useRoomStore();
  const applicationStore = useApplicationStore();
  const appointmentStore = useAppointmentStore();

  // 当前房东ID
  const landlordId = computed(() => {
    return userStore.userInfo?.id || 0;
  });

  // 是否为房东
  const isLandlord = computed(() => {
    return UserTypeUtils.isLandlord();
  });

  // 初始化房东数据
  const initLandlordData = async () => {
    if (!isLandlord.value || !landlordId.value) return;

    try {
      // 设置各store的房东ID
      apartmentStore.setLandlordId(landlordId.value);
      roomStore.setLandlordId(landlordId.value);
      applicationStore.setLandlordId(landlordId.value);
      appointmentStore.setLandlordId(landlordId.value);

      // 获取基础数据
      await Promise.all([
        landlordStore.fetchDashboardData(),
        landlordStore.fetchStatistics(),
        landlordStore.fetchQuickActions()
      ]);
    } catch (error) {
      console.error('初始化房东数据失败:', error);
    }
  };

  // 刷新所有数据
  const refreshAllData = async () => {
    if (!isLandlord.value || !landlordId.value) return;

    try {
      await landlordStore.refreshAllData(landlordId.value);
    } catch (error) {
      console.error('刷新房东数据失败:', error);
    }
  };

  // 清空所有数据
  const clearAllData = () => {
    landlordStore.clearAllData();
    apartmentStore.resetState();
    roomStore.resetState();
    applicationStore.resetState();
    appointmentStore.resetState();
  };

  return {
    // 状态
    isLandlord,
    landlordId,
    
    // Store实例
    landlordStore,
    apartmentStore,
    roomStore,
    applicationStore,
    appointmentStore,
    
    // 方法
    initLandlordData,
    refreshAllData,
    clearAllData
  };
}

/**
 * 房东仪表板数据组合函数
 */
export function useLandlordDashboard() {
  const { landlordStore, isLandlord, landlordId } = useLandlord();

  // 仪表板数据
  const dashboardData = computed(() => landlordStore.dashboardData);
  const statistics = computed(() => landlordStore.statistics);
  const quickActions = computed(() => landlordStore.quickActions);
  const pendingCount = computed(() => landlordStore.pendingCount);
  const needRefresh = computed(() => landlordStore.needRefresh);

  // 获取仪表板数据
  const fetchDashboardData = async (force = false) => {
    if (!isLandlord.value) return;
    return await landlordStore.fetchDashboardData(force);
  };

  // 获取统计数据
  const fetchStatistics = async (force = false) => {
    if (!isLandlord.value) return;
    return await landlordStore.fetchStatistics(force);
  };

  // 获取快速操作
  const fetchQuickActions = async (force = false) => {
    if (!isLandlord.value) return;
    return await landlordStore.fetchQuickActions(force);
  };

  return {
    // 数据
    dashboardData,
    statistics,
    quickActions,
    pendingCount,
    needRefresh,
    
    // 方法
    fetchDashboardData,
    fetchStatistics,
    fetchQuickActions
  };
}

/**
 * 房东公寓管理组合函数
 */
export function useLandlordApartments() {
  const { apartmentStore, isLandlord, landlordId } = useLandlord();

  // 公寓数据
  const apartments = computed(() => apartmentStore.apartments);
  const total = computed(() => apartmentStore.total);
  const loading = computed(() => apartmentStore.loading);
  const hasMore = computed(() => apartmentStore.hasMore);
  const currentApartment = computed(() => apartmentStore.currentApartment);

  // 获取公寓列表
  const fetchApartments = async (refresh = false) => {
    if (!isLandlord.value) return;
    return await apartmentStore.fetchApartments(refresh);
  };

  // 加载更多
  const loadMore = async () => {
    if (!isLandlord.value) return;
    return await apartmentStore.loadMoreApartments();
  };

  return {
    // 数据
    apartments,
    total,
    loading,
    hasMore,
    currentApartment,
    
    // 方法
    fetchApartments,
    loadMore,
    fetchApartmentDetail: apartmentStore.fetchApartmentDetail,
    createApartment: apartmentStore.createApartment,
    updateApartment: apartmentStore.updateApartment,
    deleteApartment: apartmentStore.deleteApartment,
    updateReleaseStatus: apartmentStore.updateReleaseStatus
  };
}

/**
 * 房东房源管理组合函数
 */
export function useLandlordRooms() {
  const { roomStore, isLandlord, landlordId } = useLandlord();

  // 房源数据
  const rooms = computed(() => roomStore.rooms);
  const total = computed(() => roomStore.total);
  const loading = computed(() => roomStore.loading);
  const hasMore = computed(() => roomStore.hasMore);
  const currentRoom = computed(() => roomStore.currentRoom);

  // 按状态分组的房源
  const roomsByStatus = computed(() => ({
    pending: roomStore.pendingRooms,
    approved: roomStore.approvedRooms,
    rejected: roomStore.rejectedRooms,
    published: roomStore.publishedRooms
  }));

  // 获取房源列表
  const fetchRooms = async (refresh = false) => {
    if (!isLandlord.value) return;
    return await roomStore.fetchRooms(refresh);
  };

  // 加载更多
  const loadMore = async () => {
    if (!isLandlord.value) return;
    return await roomStore.loadMoreRooms();
  };

  return {
    // 数据
    rooms,
    total,
    loading,
    hasMore,
    currentRoom,
    roomsByStatus,
    
    // 方法
    fetchRooms,
    loadMore,
    fetchRoomDetail: roomStore.fetchRoomDetail,
    publishRoom: roomStore.publishRoom,
    updateRoom: roomStore.updateRoom,
    deleteRoom: roomStore.deleteRoom,
    updateReleaseStatus: roomStore.updateReleaseStatus,
    resubmitForReview: roomStore.resubmitForReview,
    searchRooms: roomStore.searchRooms,
    filterByReviewStatus: roomStore.filterByReviewStatus,
    filterByReleaseStatus: roomStore.filterByReleaseStatus
  };
}

/**
 * 房东申请管理组合函数
 */
export function useLandlordApplications() {
  const { applicationStore, isLandlord, landlordId } = useLandlord();

  // 申请数据
  const pendingApplications = computed(() => applicationStore.pendingApplications);
  const applicationHistory = computed(() => applicationStore.applicationHistory);
  const currentApplication = computed(() => applicationStore.currentApplication);
  const pendingCount = computed(() => applicationStore.pendingCount);
  const statistics = computed(() => applicationStore.statistics);

  // 获取待处理申请
  const fetchPendingApplications = async (refresh = false) => {
    if (!isLandlord.value) return;
    return await applicationStore.fetchPendingApplications(refresh);
  };

  // 获取申请历史
  const fetchApplicationHistory = async (refresh = false) => {
    if (!isLandlord.value) return;
    return await applicationStore.fetchApplicationHistory(refresh);
  };

  return {
    // 数据
    pendingApplications,
    applicationHistory,
    currentApplication,
    pendingCount,
    statistics,
    
    // 方法
    fetchPendingApplications,
    fetchApplicationHistory,
    fetchApplicationDetail: applicationStore.fetchApplicationDetail,
    processApplication: applicationStore.processApplication,
    approveApplication: applicationStore.approveApplication,
    rejectApplication: applicationStore.rejectApplication,
    checkRoomAvailability: applicationStore.checkRoomAvailability
  };
}

/**
 * 房东预约管理组合函数
 */
export function useLandlordAppointments() {
  const { appointmentStore, isLandlord, landlordId } = useLandlord();

  // 预约数据
  const appointments = computed(() => appointmentStore.appointments);
  const currentAppointment = computed(() => appointmentStore.currentAppointment);
  const statistics = computed(() => appointmentStore.statistics);
  const filteredAppointments = computed(() => appointmentStore.filteredAppointments);

  // 按状态分组的预约
  const appointmentsByStatus = computed(() => appointmentStore.appointmentsByStatus);

  // 今日预约
  const todayAppointments = computed(() => appointmentStore.todayAppointments);

  // 获取预约列表
  const fetchAppointments = async (refresh = false) => {
    if (!isLandlord.value) return;
    return await appointmentStore.fetchAppointments(refresh);
  };

  return {
    // 数据
    appointments,
    currentAppointment,
    statistics,
    filteredAppointments,
    appointmentsByStatus,
    todayAppointments,
    
    // 方法
    fetchAppointments,
    fetchAppointmentDetail: appointmentStore.fetchAppointmentDetail,
    processAppointment: appointmentStore.processAppointment,
    confirmAppointment: appointmentStore.confirmAppointment,
    rejectAppointment: appointmentStore.rejectAppointment,
    completeAppointment: appointmentStore.completeAppointment,
    createAppointment: appointmentStore.createAppointment,
    updateAppointment: appointmentStore.updateAppointment,
    setFilters: appointmentStore.setFilters,
    clearFilters: appointmentStore.clearFilters
  };
}