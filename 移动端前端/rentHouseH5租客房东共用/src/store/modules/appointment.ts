import { defineStore } from "pinia";
import type {
  AppointmentItemVo,
  AppointmentDetailVo,
  AppointmentStatistics
} from "@/api/landlord";
import {
  getLandlordAppointments,
  getAppointmentsByGuide,
  getAppointmentsByRoom,
  getAppointmentDetail,
  processAppointment,
  createAppointment,
  updateAppointment,
  getAppointmentStatistics
} from "@/api/landlord";

/**
 * 预约管理状态接口
 */
export interface AppointmentState {
  // 预约列表
  appointments: AppointmentItemVo[];
  loading: boolean;
  
  // 当前预约详情
  currentAppointment: AppointmentDetailVo | null;
  detailLoading: boolean;
  
  // 统计信息
  statistics: AppointmentStatistics | null;
  
  // 操作状态
  processing: boolean;
  
  // 房东ID
  landlordId: number;
  
  // 筛选条件
  filters: {
    status?: number;
    dateRange?: [string, string];
    roomId?: number;
  };
}

export const useAppointmentStore = defineStore({
  id: "app-appointment",
  state: (): AppointmentState => ({
    appointments: [],
    loading: false,
    
    currentAppointment: null,
    detailLoading: false,
    
    statistics: null,
    processing: false,
    landlordId: 0,
    
    filters: {}
  }),
  
  getters: {
    // 待处理预约
    pendingAppointments: (state) => {
      return state.appointments.filter(apt => apt.appointmentStatus === 1);
    },
    
    // 已确认预约
    confirmedAppointments: (state) => {
      return state.appointments.filter(apt => apt.appointmentStatus === 2);
    },
    
    // 已完成预约
    completedAppointments: (state) => {
      return state.appointments.filter(apt => apt.appointmentStatus === 3);
    },
    
    // 已取消预约
    cancelledAppointments: (state) => {
      return state.appointments.filter(apt => apt.appointmentStatus === 4);
    },
    
    // 今日预约
    todayAppointments: (state) => {
      const today = new Date().toDateString();
      return state.appointments.filter(apt => 
        new Date(apt.appointmentTime).toDateString() === today
      );
    },
    
    // 本周预约
    thisWeekAppointments: (state) => {
      const now = new Date();
      const startOfWeek = new Date(now.setDate(now.getDate() - now.getDay()));
      const endOfWeek = new Date(now.setDate(now.getDate() - now.getDay() + 6));
      
      return state.appointments.filter(apt => {
        const aptDate = new Date(apt.appointmentTime);
        return aptDate >= startOfWeek && aptDate <= endOfWeek;
      });
    },
    
    // 按状态分组的预约
    appointmentsByStatus: (state) => {
      return {
        pending: state.pendingAppointments,
        confirmed: state.confirmedAppointments,
        completed: state.completedAppointments,
        cancelled: state.cancelledAppointments
      };
    },
    
    // 筛选后的预约
    filteredAppointments: (state) => {
      let filtered = [...state.appointments];
      
      // 按状态筛选
      if (state.filters.status !== undefined) {
        filtered = filtered.filter(apt => apt.appointmentStatus === state.filters.status);
      }
      
      // 按房间筛选
      if (state.filters.roomId) {
        filtered = filtered.filter(apt => apt.roomId === state.filters.roomId);
      }
      
      // 按日期范围筛选
      if (state.filters.dateRange) {
        const [startDate, endDate] = state.filters.dateRange;
        filtered = filtered.filter(apt => {
          const aptDate = apt.appointmentTime;
          return aptDate >= startDate && aptDate <= endDate;
        });
      }
      
      return filtered;
    }
  },
  
  actions: {
    // 设置房东ID
    setLandlordId(landlordId: number) {
      this.landlordId = landlordId;
    },
    
    // 设置筛选条件
    setFilters(filters: Partial<typeof this.filters>) {
      this.filters = { ...this.filters, ...filters };
    },
    
    // 清空筛选条件
    clearFilters() {
      this.filters = {};
    },
    
    // 获取房东预约列表
    async fetchAppointments(refresh = false) {
      if (!refresh && this.appointments.length > 0) {
        return this.appointments;
      }
      
      this.loading = true;
      try {
        const { data } = await getLandlordAppointments(this.landlordId);
        this.appointments = data;
        return data;
      } catch (error) {
        console.error('获取预约列表失败:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // 根据带看人获取预约
    async fetchAppointmentsByGuide(guideType: number, guideId: number) {
      this.loading = true;
      try {
        const { data } = await getAppointmentsByGuide(guideType, guideId);
        this.appointments = data;
        return data;
      } catch (error) {
        console.error('获取带看人预约失败:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // 根据房间获取预约
    async fetchAppointmentsByRoom(roomId: number) {
      this.loading = true;
      try {
        const { data } = await getAppointmentsByRoom(roomId);
        return data;
      } catch (error) {
        console.error('获取房间预约失败:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // 获取预约详情
    async fetchAppointmentDetail(appointmentId: number) {
      this.detailLoading = true;
      try {
        const { data } = await getAppointmentDetail(appointmentId);
        this.currentAppointment = data;
        return data;
      } catch (error) {
        console.error('获取预约详情失败:', error);
        throw error;
      } finally {
        this.detailLoading = false;
      }
    },
    
    // 处理预约
    async processAppointment(params: {
      appointmentId: number;
      status: number;
      remarks?: string;
    }) {
      this.processing = true;
      try {
        await processAppointment(params);
        
        // 更新本地状态
        this.updateAppointmentStatus(params.appointmentId, params.status);
        
        // 刷新列表
        await this.fetchAppointments(true);
        
      } catch (error) {
        console.error('处理预约失败:', error);
        throw error;
      } finally {
        this.processing = false;
      }
    },
    
    // 确认预约
    async confirmAppointment(appointmentId: number, remarks?: string) {
      await this.processAppointment({
        appointmentId,
        status: 2, // 已确认
        remarks
      });
    },
    
    // 拒绝预约
    async rejectAppointment(appointmentId: number, remarks?: string) {
      await this.processAppointment({
        appointmentId,
        status: 4, // 已取消
        remarks
      });
    },
    
    // 完成预约
    async completeAppointment(appointmentId: number, remarks?: string) {
      await this.processAppointment({
        appointmentId,
        status: 3, // 已完成
        remarks
      });
    },
    
    // 创建预约
    async createAppointment(appointmentData: {
      roomId: number;
      appointmentTime: string;
      additionalInfo?: string;
      guideType?: number;
      guideId?: number;
    }) {
      this.processing = true;
      try {
        const { data } = await createAppointment(appointmentData);
        
        // 刷新列表
        await this.fetchAppointments(true);
        
        return data;
      } catch (error) {
        console.error('创建预约失败:', error);
        throw error;
      } finally {
        this.processing = false;
      }
    },
    
    // 更新预约
    async updateAppointment(appointmentData: {
      id: number;
      appointmentTime?: string;
      additionalInfo?: string;
      appointmentStatus?: number;
    }) {
      this.processing = true;
      try {
        await updateAppointment(appointmentData);
        
        // 更新本地状态
        const index = this.appointments.findIndex(apt => apt.id === appointmentData.id);
        if (index !== -1) {
          Object.assign(this.appointments[index], appointmentData);
        }
        
        // 更新当前详情
        if (this.currentAppointment && this.currentAppointment.id === appointmentData.id) {
          Object.assign(this.currentAppointment, appointmentData);
        }
        
      } catch (error) {
        console.error('更新预约失败:', error);
        throw error;
      } finally {
        this.processing = false;
      }
    },
    
    // 获取统计信息
    async fetchStatistics() {
      try {
        const { data } = await getAppointmentStatistics(this.landlordId);
        this.statistics = data;
        return data;
      } catch (error) {
        console.error('获取预约统计失败:', error);
        throw error;
      }
    },
    
    // 更新预约状态（本地）
    updateAppointmentStatus(appointmentId: number, status: number) {
      const index = this.appointments.findIndex(apt => apt.id === appointmentId);
      if (index !== -1) {
        this.appointments[index].appointmentStatus = status;
      }
      
      // 更新当前详情
      if (this.currentAppointment && this.currentAppointment.id === appointmentId) {
        this.currentAppointment.appointmentStatus = status;
      }
    },
    
    // 批量处理预约
    async batchProcessAppointments(appointmentIds: number[], status: number, remarks?: string) {
      this.processing = true;
      try {
        const promises = appointmentIds.map(id => 
          this.processAppointment({ appointmentId: id, status, remarks })
        );
        
        await Promise.all(promises);
        
        // 刷新列表
        await this.fetchAppointments(true);
        
      } catch (error) {
        console.error('批量处理预约失败:', error);
        throw error;
      } finally {
        this.processing = false;
      }
    },
    
    // 刷新所有数据
    async refreshAllData() {
      await Promise.all([
        this.fetchAppointments(true),
        this.fetchStatistics()
      ]);
    },
    
    // 清空当前预约详情
    clearCurrentAppointment() {
      this.currentAppointment = null;
    },
    
    // 重置状态
    resetState() {
      this.appointments = [];
      this.currentAppointment = null;
      this.statistics = null;
      this.landlordId = 0;
      this.filters = {};
    }
  },
  
  // 持久化存储
  persist: {
    key: 'appointment-store',
    storage: sessionStorage,
    // 只持久化房东ID和筛选条件
    paths: ['landlordId', 'filters']
  }
});