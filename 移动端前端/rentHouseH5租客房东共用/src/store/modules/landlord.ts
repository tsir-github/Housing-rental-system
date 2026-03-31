import { defineStore } from "pinia";
import type {
  LandlordDashboardVo,
  LandlordStatistics,
  ApartmentItemVo,
  RoomItemVo,
  DirectMoveInApplicationDetailVO,
  AppointmentItemVo,
  RoomReviewStatus,
  PageResult
} from "@/api/landlord";
import {
  getDashboardData,
  getStatistics,
  getQuickActions,
  getMyApartments,
  getMyRooms,
  getPendingApplications,
  getLandlordAppointments,
  getReviewStatusList
} from "@/api/landlord";

/**
 * 房东状态接口
 */
export interface LandlordState {
  // 仪表板数据
  dashboardData: LandlordDashboardVo | null;
  statistics: LandlordStatistics | null;
  quickActions: any[] | null;
  
  // 公寓数据
  apartments: ApartmentItemVo[];
  apartmentTotal: number;
  apartmentLoading: boolean;
  
  // 房源数据
  rooms: RoomItemVo[];
  roomTotal: number;
  roomLoading: boolean;
  
  // 申请数据
  applications: DirectMoveInApplicationDetailVO[];
  applicationLoading: boolean;
  
  // 预约数据
  appointments: AppointmentItemVo[];
  appointmentLoading: boolean;
  
  // 审核数据
  reviews: RoomReviewStatus[];
  reviewTotal: number;
  reviewLoading: boolean;
  
  // 缓存控制
  lastUpdateTime: number;
  cacheExpireTime: number; // 缓存过期时间（毫秒）
}

export const useLandlordStore = defineStore({
  id: "app-landlord",
  state: (): LandlordState => ({
    // 仪表板数据
    dashboardData: null,
    statistics: null,
    quickActions: null,
    
    // 公寓数据
    apartments: [],
    apartmentTotal: 0,
    apartmentLoading: false,
    
    // 房源数据
    rooms: [],
    roomTotal: 0,
    roomLoading: false,
    
    // 申请数据
    applications: [],
    applicationLoading: false,
    
    // 预约数据
    appointments: [],
    appointmentLoading: false,
    
    // 审核数据
    reviews: [],
    reviewTotal: 0,
    reviewLoading: false,
    
    // 缓存控制
    lastUpdateTime: 0,
    cacheExpireTime: 5 * 60 * 1000 // 5分钟缓存
  }),
  
  getters: {
    // 是否需要刷新数据
    needRefresh: (state) => {
      const now = Date.now();
      return now - state.lastUpdateTime > state.cacheExpireTime;
    },
    
    // 待处理事项数量
    pendingCount: (state) => {
      if (!state.dashboardData) return 0;
      return state.dashboardData.pendingApplicationCount + state.dashboardData.pendingAppointmentCount;
    },
    
    // 今日预约数量
    todayAppointmentCount: (state) => {
      return state.dashboardData?.todayAppointmentCount || 0;
    },
    
    // 入住率
    occupancyRate: (state) => {
      return state.dashboardData?.occupancyRate || 0;
    },
    
    // 房源统计
    roomStats: (state) => {
      if (!state.dashboardData) return null;
      return {
        total: state.dashboardData.roomCount,
        published: state.dashboardData.publishedRoomCount,
        available: state.dashboardData.availableRoomCount,
        occupied: state.dashboardData.occupiedRoomCount
      };
    },
    
    // 待处理申请
    pendingApplications: (state) => {
      return state.applications.filter(app => app.status === 1); // 假设1为待处理状态
    },
    
    // 待处理预约
    pendingAppointments: (state) => {
      return state.appointments.filter(apt => apt.appointmentStatus === 1); // 假设1为待处理状态
    }
  },
  
  actions: {
    // 设置仪表板数据
    setDashboardData(data: LandlordDashboardVo) {
      this.dashboardData = data;
      this.updateLastUpdateTime();
    },
    
    // 设置统计数据
    setStatistics(data: LandlordStatistics) {
      this.statistics = data;
    },
    
    // 设置快速操作
    setQuickActions(actions: any[]) {
      this.quickActions = actions;
    },
    
    // 更新最后更新时间
    updateLastUpdateTime() {
      this.lastUpdateTime = Date.now();
    },
    
    // 获取仪表板数据
    async fetchDashboardData(force = false) {
      if (!force && !this.needRefresh && this.dashboardData) {
        return this.dashboardData;
      }
      
      try {
        const { data } = await getDashboardData();
        this.setDashboardData(data);
        return data;
      } catch (error) {
        console.error('获取仪表板数据失败:', error);
        throw error;
      }
    },
    
    // 获取统计数据
    async fetchStatistics(force = false) {
      if (!force && !this.needRefresh && this.statistics) {
        return this.statistics;
      }
      
      try {
        const { data } = await getStatistics();
        this.setStatistics(data);
        return data;
      } catch (error) {
        console.error('获取统计数据失败:', error);
        throw error;
      }
    },
    
    // 获取快速操作
    async fetchQuickActions(force = false) {
      if (!force && this.quickActions) {
        return this.quickActions;
      }
      
      try {
        const { data } = await getQuickActions();
        this.setQuickActions(data.actions);
        return data.actions;
      } catch (error) {
        console.error('获取快速操作失败:', error);
        throw error;
      }
    },
    
    // 获取公寓列表
    async fetchApartments(params: {
      current: number;
      size: number;
      landlordId: number;
      queryVo?: any;
    }) {
      this.apartmentLoading = true;
      try {
        const { data } = await getMyApartments(params);
        this.apartments = data.records;
        this.apartmentTotal = data.total;
        return data;
      } catch (error) {
        console.error('获取公寓列表失败:', error);
        throw error;
      } finally {
        this.apartmentLoading = false;
      }
    },
    
    // 获取房源列表
    async fetchRooms(params: {
      current: number;
      size: number;
      landlordId: number;
      queryVo?: any;
    }) {
      this.roomLoading = true;
      try {
        const { data } = await getMyRooms(params);
        this.rooms = data.records;
        this.roomTotal = data.total;
        return data;
      } catch (error) {
        console.error('获取房源列表失败:', error);
        throw error;
      } finally {
        this.roomLoading = false;
      }
    },
    
    // 获取申请列表
    async fetchApplications(landlordId: number) {
      this.applicationLoading = true;
      try {
        const { data } = await getPendingApplications(landlordId);
        this.applications = data;
        return data;
      } catch (error) {
        console.error('获取申请列表失败:', error);
        throw error;
      } finally {
        this.applicationLoading = false;
      }
    },
    
    // 获取预约列表
    async fetchAppointments(landlordId: number) {
      this.appointmentLoading = true;
      try {
        const { data } = await getLandlordAppointments(landlordId);
        this.appointments = data;
        return data;
      } catch (error) {
        console.error('获取预约列表失败:', error);
        throw error;
      } finally {
        this.appointmentLoading = false;
      }
    },
    
    // 获取审核状态列表
    async fetchReviews(params: {
      landlordId: number;
      current?: number;
      size?: number;
      reviewStatus?: number;
    }) {
      this.reviewLoading = true;
      try {
        const { data } = await getReviewStatusList(params);
        this.reviews = data.records;
        this.reviewTotal = data.total;
        return data;
      } catch (error) {
        console.error('获取审核状态失败:', error);
        throw error;
      } finally {
        this.reviewLoading = false;
      }
    },
    
    // 刷新所有数据
    async refreshAllData(landlordId: number) {
      try {
        await Promise.all([
          this.fetchDashboardData(true),
          this.fetchStatistics(true),
          this.fetchQuickActions(true)
        ]);
        
        // 刷新列表数据（使用默认参数）
        await Promise.all([
          this.fetchApartments({ current: 1, size: 10, landlordId }),
          this.fetchRooms({ current: 1, size: 10, landlordId }),
          this.fetchApplications(landlordId),
          this.fetchAppointments(landlordId),
          this.fetchReviews({ landlordId, current: 1, size: 10 })
        ]);
        
        this.updateLastUpdateTime();
      } catch (error) {
        console.error('刷新数据失败:', error);
        throw error;
      }
    },
    
    // 清空所有数据
    clearAllData() {
      this.dashboardData = null;
      this.statistics = null;
      this.quickActions = null;
      this.apartments = [];
      this.apartmentTotal = 0;
      this.rooms = [];
      this.roomTotal = 0;
      this.applications = [];
      this.appointments = [];
      this.reviews = [];
      this.reviewTotal = 0;
      this.lastUpdateTime = 0;
    },
    
    // 添加公寓到列表
    addApartment(apartment: ApartmentItemVo) {
      this.apartments.unshift(apartment);
      this.apartmentTotal += 1;
    },
    
    // 更新公寓信息
    updateApartment(apartmentId: number, updates: Partial<ApartmentItemVo>) {
      const index = this.apartments.findIndex(apt => apt.id === apartmentId);
      if (index !== -1) {
        this.apartments[index] = { ...this.apartments[index], ...updates };
      }
    },
    
    // 删除公寓
    removeApartment(apartmentId: number) {
      const index = this.apartments.findIndex(apt => apt.id === apartmentId);
      if (index !== -1) {
        this.apartments.splice(index, 1);
        this.apartmentTotal -= 1;
      }
    },
    
    // 添加房源到列表
    addRoom(room: RoomItemVo) {
      this.rooms.unshift(room);
      this.roomTotal += 1;
    },
    
    // 更新房源信息
    updateRoom(roomId: number, updates: Partial<RoomItemVo>) {
      const index = this.rooms.findIndex(room => room.id === roomId);
      if (index !== -1) {
        this.rooms[index] = { ...this.rooms[index], ...updates };
      }
    },
    
    // 删除房源
    removeRoom(roomId: number) {
      const index = this.rooms.findIndex(room => room.id === roomId);
      if (index !== -1) {
        this.rooms.splice(index, 1);
        this.roomTotal -= 1;
      }
    },
    
    // 更新申请状态
    updateApplicationStatus(applicationId: number, status: number) {
      const index = this.applications.findIndex(app => app.id === applicationId);
      if (index !== -1) {
        this.applications[index].status = status;
      }
    },
    
    // 更新预约状态
    updateAppointmentStatus(appointmentId: number, status: number) {
      const index = this.appointments.findIndex(apt => apt.id === appointmentId);
      if (index !== -1) {
        this.appointments[index].appointmentStatus = status;
      }
    }
  },
  
  // 持久化存储
  persist: {
    key: 'landlord-store',
    storage: localStorage,
    // 只持久化关键数据，不持久化loading状态
    paths: [
      'dashboardData',
      'statistics',
      'quickActions',
      'lastUpdateTime'
    ]
  }
});