import { defineStore } from "pinia";
import type {
  ApartmentItemVo,
  ApartmentDetailVo,
  ApartmentSubmitVo,
  ApartmentQueryVo,
  PageResult
} from "@/api/landlord";
import {
  getMyApartments,
  getApartmentDetail,
  createApartment,
  updateApartment,
  deleteApartment,
  updateApartmentReleaseStatus,
  getApartmentStatistics
} from "@/api/landlord";

/**
 * 公寓管理状态接口
 */
export interface ApartmentState {
  // 公寓列表
  apartments: ApartmentItemVo[];
  total: number;
  loading: boolean;
  
  // 当前公寓详情
  currentApartment: ApartmentDetailVo | null;
  detailLoading: boolean;
  
  // 查询条件
  queryParams: {
    current: number;
    size: number;
    landlordId: number;
    queryVo?: ApartmentQueryVo;
  };
  
  // 统计信息
  statistics: {
    totalApartments: number;
    publishedApartments: number;
    totalRooms: number;
    availableRooms: number;
    occupancyRate: number;
  } | null;
  
  // 操作状态
  submitting: boolean;
}

export const useApartmentStore = defineStore({
  id: "app-apartment",
  state: (): ApartmentState => ({
    apartments: [],
    total: 0,
    loading: false,
    
    currentApartment: null,
    detailLoading: false,
    
    queryParams: {
      current: 1,
      size: 10,
      landlordId: 0
    },
    
    statistics: null,
    submitting: false
  }),
  
  getters: {
    // 已发布的公寓
    publishedApartments: (state) => {
      return state.apartments.filter(apt => apt.isRelease === 1);
    },
    
    // 未发布的公寓
    unpublishedApartments: (state) => {
      return state.apartments.filter(apt => apt.isRelease === 0);
    },
    
    // 是否有更多数据
    hasMore: (state) => {
      return state.apartments.length < state.total;
    },
    
    // 当前页码
    currentPage: (state) => {
      return state.queryParams.current;
    }
  },
  
  actions: {
    // 设置房东ID
    setLandlordId(landlordId: number) {
      this.queryParams.landlordId = landlordId;
    },
    
    // 设置查询参数
    setQueryParams(params: Partial<typeof this.queryParams>) {
      this.queryParams = { ...this.queryParams, ...params };
    },
    
    // 获取公寓列表
    async fetchApartments(refresh = false) {
      if (refresh) {
        this.queryParams.current = 1;
        this.apartments = [];
      }
      
      this.loading = true;
      try {
        const { data } = await getMyApartments(this.queryParams);
        
        if (refresh) {
          this.apartments = data.records;
        } else {
          this.apartments.push(...data.records);
        }
        
        this.total = data.total;
        this.queryParams.current = data.current;
        
        return data;
      } catch (error) {
        console.error('获取公寓列表失败:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // 加载更多公寓
    async loadMoreApartments() {
      if (!this.hasMore || this.loading) return;
      
      this.queryParams.current += 1;
      await this.fetchApartments(false);
    },
    
    // 获取公寓详情
    async fetchApartmentDetail(apartmentId: number) {
      this.detailLoading = true;
      try {
        const { data } = await getApartmentDetail(apartmentId, this.queryParams.landlordId);
        this.currentApartment = data;
        return data;
      } catch (error) {
        console.error('获取公寓详情失败:', error);
        throw error;
      } finally {
        this.detailLoading = false;
      }
    },
    
    // 创建公寓
    async createApartment(apartmentData: ApartmentSubmitVo) {
      this.submitting = true;
      try {
        const { data } = await createApartment(apartmentData, this.queryParams.landlordId);
        
        // 刷新列表
        await this.fetchApartments(true);
        
        return data;
      } catch (error) {
        console.error('创建公寓失败:', error);
        throw error;
      } finally {
        this.submitting = false;
      }
    },
    
    // 更新公寓
    async updateApartment(apartmentData: ApartmentSubmitVo) {
      this.submitting = true;
      try {
        await updateApartment(apartmentData, this.queryParams.landlordId);
        
        // 更新列表中的数据
        if (apartmentData.id) {
          const index = this.apartments.findIndex(apt => apt.id === apartmentData.id);
          if (index !== -1) {
            // 更新基本信息
            Object.assign(this.apartments[index], apartmentData);
          }
        }
        
        // 更新当前详情
        if (this.currentApartment && this.currentApartment.id === apartmentData.id) {
          Object.assign(this.currentApartment, apartmentData);
        }
        
      } catch (error) {
        console.error('更新公寓失败:', error);
        throw error;
      } finally {
        this.submitting = false;
      }
    },
    
    // 删除公寓
    async deleteApartment(apartmentId: number) {
      this.submitting = true;
      try {
        await deleteApartment(apartmentId, this.queryParams.landlordId);
        
        // 从列表中移除
        const index = this.apartments.findIndex(apt => apt.id === apartmentId);
        if (index !== -1) {
          this.apartments.splice(index, 1);
          this.total -= 1;
        }
        
        // 清空当前详情
        if (this.currentApartment && this.currentApartment.id === apartmentId) {
          this.currentApartment = null;
        }
        
      } catch (error) {
        console.error('删除公寓失败:', error);
        throw error;
      } finally {
        this.submitting = false;
      }
    },
    
    // 更新发布状态
    async updateReleaseStatus(apartmentId: number, status: number) {
      this.submitting = true;
      try {
        await updateApartmentReleaseStatus(apartmentId, status, this.queryParams.landlordId);
        
        // 更新列表中的状态
        const index = this.apartments.findIndex(apt => apt.id === apartmentId);
        if (index !== -1) {
          this.apartments[index].isRelease = status;
        }
        
        // 更新当前详情
        if (this.currentApartment && this.currentApartment.id === apartmentId) {
          this.currentApartment.isRelease = status;
        }
        
      } catch (error) {
        console.error('更新发布状态失败:', error);
        throw error;
      } finally {
        this.submitting = false;
      }
    },
    
    // 获取统计信息
    async fetchStatistics() {
      try {
        const { data } = await getApartmentStatistics(this.queryParams.landlordId);
        this.statistics = data;
        return data;
      } catch (error) {
        console.error('获取公寓统计失败:', error);
        throw error;
      }
    },
    
    // 搜索公寓
    async searchApartments(queryVo: ApartmentQueryVo) {
      this.setQueryParams({ queryVo, current: 1 });
      await this.fetchApartments(true);
    },
    
    // 清空当前公寓详情
    clearCurrentApartment() {
      this.currentApartment = null;
    },
    
    // 重置状态
    resetState() {
      this.apartments = [];
      this.total = 0;
      this.currentApartment = null;
      this.statistics = null;
      this.queryParams = {
        current: 1,
        size: 10,
        landlordId: 0
      };
    }
  },
  
  // 持久化存储
  persist: {
    key: 'apartment-store',
    storage: sessionStorage,
    // 只持久化查询参数
    paths: ['queryParams']
  }
});