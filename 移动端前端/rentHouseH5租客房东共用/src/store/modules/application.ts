import { defineStore } from "pinia";
import type {
  DirectMoveInApplicationDetailVO,
  ApplicationStatistics
} from "@/api/landlord";
import {
  getPendingApplications,
  getApplicationHistory,
  getApplicationDetail,
  processApplication,
  approveApplication,
  rejectApplication,
  checkRoomAvailability,
  getApplicationStatistics
} from "@/api/landlord";

/**
 * 申请管理状态接口
 */
export interface ApplicationState {
  // 待处理申请
  pendingApplications: DirectMoveInApplicationDetailVO[];
  pendingLoading: boolean;
  
  // 申请历史
  applicationHistory: DirectMoveInApplicationDetailVO[];
  historyLoading: boolean;
  
  // 当前申请详情
  currentApplication: DirectMoveInApplicationDetailVO | null;
  detailLoading: boolean;
  
  // 统计信息
  statistics: ApplicationStatistics | null;
  
  // 操作状态
  processing: boolean;
  
  // 房东ID
  landlordId: number;
}

export const useApplicationStore = defineStore({
  id: "app-application",
  state: (): ApplicationState => ({
    pendingApplications: [],
    pendingLoading: false,
    
    applicationHistory: [],
    historyLoading: false,
    
    currentApplication: null,
    detailLoading: false,
    
    statistics: null,
    processing: false,
    landlordId: 0
  }),
  
  getters: {
    // 待处理申请数量
    pendingCount: (state) => {
      return state.pendingApplications.length;
    },
    
    // 今日申请数量
    todayApplications: (state) => {
      const today = new Date().toDateString();
      return state.pendingApplications.filter(app => 
        new Date(app.applicationDate).toDateString() === today
      ).length;
    },
    
    // 按状态分组的申请
    applicationsByStatus: (state) => {
      const all = [...state.pendingApplications, ...state.applicationHistory];
      return {
        pending: all.filter(app => app.status === 1),
        approved: all.filter(app => app.status === 2),
        rejected: all.filter(app => app.status === 3)
      };
    }
  },
  
  actions: {
    // 设置房东ID
    setLandlordId(landlordId: number) {
      this.landlordId = landlordId;
    },
    
    // 获取待处理申请
    async fetchPendingApplications(refresh = false) {
      if (!refresh && this.pendingApplications.length > 0) {
        return this.pendingApplications;
      }
      
      this.pendingLoading = true;
      try {
        const { data } = await getPendingApplications(this.landlordId);
        this.pendingApplications = data;
        return data;
      } catch (error) {
        console.error('获取待处理申请失败:', error);
        throw error;
      } finally {
        this.pendingLoading = false;
      }
    },
    
    // 获取申请历史
    async fetchApplicationHistory(refresh = false) {
      if (!refresh && this.applicationHistory.length > 0) {
        return this.applicationHistory;
      }
      
      this.historyLoading = true;
      try {
        const { data } = await getApplicationHistory(this.landlordId);
        this.applicationHistory = data;
        return data;
      } catch (error) {
        console.error('获取申请历史失败:', error);
        throw error;
      } finally {
        this.historyLoading = false;
      }
    },
    
    // 获取申请详情
    async fetchApplicationDetail(applicationId: number) {
      this.detailLoading = true;
      try {
        const { data } = await getApplicationDetail(applicationId);
        this.currentApplication = data;
        return data;
      } catch (error) {
        console.error('获取申请详情失败:', error);
        throw error;
      } finally {
        this.detailLoading = false;
      }
    },
    
    // 处理申请
    async processApplication(params: {
      id: number;
      approved: boolean;
      rejectionReason?: string;
    }) {
      this.processing = true;
      try {
        await processApplication({
          ...params,
          landlordId: this.landlordId
        });
        
        // 更新本地状态
        await this.updateApplicationStatus(params.id, params.approved ? 2 : 3);
        
        // 刷新列表
        await Promise.all([
          this.fetchPendingApplications(true),
          this.fetchApplicationHistory(true)
        ]);
        
      } catch (error) {
        console.error('处理申请失败:', error);
        throw error;
      } finally {
        this.processing = false;
      }
    },
    
    // 批准申请
    async approveApplication(applicationId: number) {
      this.processing = true;
      try {
        const { data } = await approveApplication(applicationId, this.landlordId);
        
        // 更新本地状态
        await this.updateApplicationStatus(applicationId, 2);
        
        // 刷新列表
        await Promise.all([
          this.fetchPendingApplications(true),
          this.fetchApplicationHistory(true)
        ]);
        
        return data; // 返回租约ID
      } catch (error) {
        console.error('批准申请失败:', error);
        throw error;
      } finally {
        this.processing = false;
      }
    },
    
    // 拒绝申请
    async rejectApplication(params: {
      id: number;
      rejectionReason: string;
    }) {
      this.processing = true;
      try {
        await rejectApplication({
          ...params,
          landlordId: this.landlordId
        });
        
        // 更新本地状态
        await this.updateApplicationStatus(params.id, 3);
        
        // 刷新列表
        await Promise.all([
          this.fetchPendingApplications(true),
          this.fetchApplicationHistory(true)
        ]);
        
      } catch (error) {
        console.error('拒绝申请失败:', error);
        throw error;
      } finally {
        this.processing = false;
      }
    },
    
    // 检查房间可用性
    async checkRoomAvailability(roomId: number) {
      try {
        const { data } = await checkRoomAvailability(roomId);
        return data;
      } catch (error) {
        console.error('检查房间可用性失败:', error);
        throw error;
      }
    },
    
    // 获取统计信息
    async fetchStatistics() {
      try {
        const { data } = await getApplicationStatistics(this.landlordId);
        this.statistics = data;
        return data;
      } catch (error) {
        console.error('获取申请统计失败:', error);
        throw error;
      }
    },
    
    // 更新申请状态（本地）
    async updateApplicationStatus(applicationId: number, status: number) {
      // 更新待处理列表
      const pendingIndex = this.pendingApplications.findIndex(app => app.id === applicationId);
      if (pendingIndex !== -1) {
        this.pendingApplications[pendingIndex].status = status;
        
        // 如果不再是待处理状态，移动到历史列表
        if (status !== 1) {
          const application = this.pendingApplications.splice(pendingIndex, 1)[0];
          this.applicationHistory.unshift(application);
        }
      }
      
      // 更新历史列表
      const historyIndex = this.applicationHistory.findIndex(app => app.id === applicationId);
      if (historyIndex !== -1) {
        this.applicationHistory[historyIndex].status = status;
      }
      
      // 更新当前详情
      if (this.currentApplication && this.currentApplication.id === applicationId) {
        this.currentApplication.status = status;
      }
    },
    
    // 刷新所有数据
    async refreshAllData() {
      await Promise.all([
        this.fetchPendingApplications(true),
        this.fetchApplicationHistory(true),
        this.fetchStatistics()
      ]);
    },
    
    // 清空当前申请详情
    clearCurrentApplication() {
      this.currentApplication = null;
    },
    
    // 重置状态
    resetState() {
      this.pendingApplications = [];
      this.applicationHistory = [];
      this.currentApplication = null;
      this.statistics = null;
      this.landlordId = 0;
    }
  },
  
  // 持久化存储
  persist: {
    key: 'application-store',
    storage: sessionStorage,
    // 只持久化房东ID
    paths: ['landlordId']
  }
});