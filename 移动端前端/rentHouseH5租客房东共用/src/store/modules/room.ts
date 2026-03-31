import { defineStore } from "pinia";
import type {
  RoomItemVo,
  RoomDetailVo,
  RoomSubmitVo,
  RoomQueryVo,
  PageResult
} from "@/api/landlord";
import {
  getMyRooms,
  getRoomDetail,
  publishRoom,
  updateRoom,
  deleteRoom,
  updateRoomReleaseStatus,
  resubmitForReview,
  getRoomStatistics
} from "@/api/landlord";

/**
 * 房源管理状态接口
 */
export interface RoomState {
  // 房源列表
  rooms: RoomItemVo[];
  total: number;
  loading: boolean;
  
  // 当前房源详情
  currentRoom: RoomDetailVo | null;
  detailLoading: boolean;
  
  // 查询条件
  queryParams: {
    current: number;
    size: number;
    landlordId: number;
    queryVo?: RoomQueryVo;
  };
  
  // 统计信息
  statistics: {
    totalRooms: number;
    pendingReview: number;
    approved: number;
    rejected: number;
    published: number;
    avgComplianceScore: number;
    approvalRate: number;
  } | null;
  
  // 操作状态
  submitting: boolean;
}

export const useRoomStore = defineStore({
  id: "app-room",
  state: (): RoomState => ({
    rooms: [],
    total: 0,
    loading: false,
    
    currentRoom: null,
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
    // 待审核房源
    pendingRooms: (state) => {
      return state.rooms.filter(room => room.reviewStatus === 1);
    },
    
    // 已通过审核房源
    approvedRooms: (state) => {
      return state.rooms.filter(room => room.reviewStatus === 2);
    },
    
    // 被拒绝房源
    rejectedRooms: (state) => {
      return state.rooms.filter(room => room.reviewStatus === 3);
    },
    
    // 已发布房源
    publishedRooms: (state) => {
      return state.rooms.filter(room => room.isRelease === 1);
    },
    
    // 是否有更多数据
    hasMore: (state) => {
      return state.rooms.length < state.total;
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
    
    // 获取房源列表
    async fetchRooms(refresh = false) {
      if (refresh) {
        this.queryParams.current = 1;
        this.rooms = [];
      }
      
      this.loading = true;
      try {
        const { data } = await getMyRooms(this.queryParams);
        
        if (refresh) {
          this.rooms = data.records;
        } else {
          this.rooms.push(...data.records);
        }
        
        this.total = data.total;
        this.queryParams.current = data.current;
        
        return data;
      } catch (error) {
        console.error('获取房源列表失败:', error);
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // 加载更多房源
    async loadMoreRooms() {
      if (!this.hasMore || this.loading) return;
      
      this.queryParams.current += 1;
      await this.fetchRooms(false);
    },
    
    // 获取房源详情
    async fetchRoomDetail(roomId: number) {
      this.detailLoading = true;
      try {
        const { data } = await getRoomDetail(roomId, this.queryParams.landlordId);
        this.currentRoom = data;
        return data;
      } catch (error) {
        console.error('获取房源详情失败:', error);
        throw error;
      } finally {
        this.detailLoading = false;
      }
    },
    
    // 发布房源
    async publishRoom(roomData: RoomSubmitVo) {
      this.submitting = true;
      try {
        await publishRoom(roomData, this.queryParams.landlordId);
        
        // 刷新列表
        await this.fetchRooms(true);
        
      } catch (error) {
        console.error('发布房源失败:', error);
        throw error;
      } finally {
        this.submitting = false;
      }
    },
    
    // 更新房源
    async updateRoom(roomData: RoomSubmitVo) {
      this.submitting = true;
      try {
        await updateRoom(roomData, this.queryParams.landlordId);
        
        // 更新列表中的数据
        if (roomData.id) {
          const index = this.rooms.findIndex(room => room.id === roomData.id);
          if (index !== -1) {
            Object.assign(this.rooms[index], roomData);
          }
        }
        
        // 更新当前详情
        if (this.currentRoom && this.currentRoom.id === roomData.id) {
          Object.assign(this.currentRoom, roomData);
        }
        
      } catch (error) {
        console.error('更新房源失败:', error);
        throw error;
      } finally {
        this.submitting = false;
      }
    },
    
    // 删除房源
    async deleteRoom(roomId: number) {
      this.submitting = true;
      try {
        await deleteRoom(roomId, this.queryParams.landlordId);
        
        // 从列表中移除
        const index = this.rooms.findIndex(room => room.id === roomId);
        if (index !== -1) {
          this.rooms.splice(index, 1);
          this.total -= 1;
        }
        
        // 清空当前详情
        if (this.currentRoom && this.currentRoom.id === roomId) {
          this.currentRoom = null;
        }
        
      } catch (error) {
        console.error('删除房源失败:', error);
        throw error;
      } finally {
        this.submitting = false;
      }
    },
    
    // 更新发布状态
    async updateReleaseStatus(roomId: number, status: number) {
      this.submitting = true;
      try {
        await updateRoomReleaseStatus(roomId, status, this.queryParams.landlordId);
        
        // 更新列表中的状态
        const index = this.rooms.findIndex(room => room.id === roomId);
        if (index !== -1) {
          this.rooms[index].isRelease = status;
        }
        
        // 更新当前详情
        if (this.currentRoom && this.currentRoom.id === roomId) {
          this.currentRoom.isRelease = status;
        }
        
      } catch (error) {
        console.error('更新发布状态失败:', error);
        throw error;
      } finally {
        this.submitting = false;
      }
    },
    
    // 重新提交审核
    async resubmitForReview(roomId: number) {
      this.submitting = true;
      try {
        await resubmitForReview(roomId, this.queryParams.landlordId);
        
        // 更新列表中的审核状态
        const index = this.rooms.findIndex(room => room.id === roomId);
        if (index !== -1) {
          this.rooms[index].reviewStatus = 1; // 待审核
          this.rooms[index].isRelease = 0; // 未发布
        }
        
        // 更新当前详情
        if (this.currentRoom && this.currentRoom.id === roomId) {
          this.currentRoom.reviewStatus = 1;
          this.currentRoom.isRelease = 0;
        }
        
      } catch (error) {
        console.error('重新提交审核失败:', error);
        throw error;
      } finally {
        this.submitting = false;
      }
    },
    
    // 获取统计信息
    async fetchStatistics() {
      try {
        const { data } = await getRoomStatistics(this.queryParams.landlordId);
        this.statistics = data;
        return data;
      } catch (error) {
        console.error('获取房源统计失败:', error);
        throw error;
      }
    },
    
    // 搜索房源
    async searchRooms(queryVo: RoomQueryVo) {
      this.setQueryParams({ queryVo, current: 1 });
      await this.fetchRooms(true);
    },
    
    // 按审核状态筛选
    async filterByReviewStatus(reviewStatus?: number) {
      const queryVo = { ...this.queryParams.queryVo, reviewStatus };
      await this.searchRooms(queryVo);
    },
    
    // 按发布状态筛选
    async filterByReleaseStatus(isRelease?: number) {
      const queryVo = { ...this.queryParams.queryVo, isRelease };
      await this.searchRooms(queryVo);
    },
    
    // 清空当前房源详情
    clearCurrentRoom() {
      this.currentRoom = null;
    },
    
    // 重置状态
    resetState() {
      this.rooms = [];
      this.total = 0;
      this.currentRoom = null;
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
    key: 'room-store',
    storage: sessionStorage,
    // 只持久化查询参数
    paths: ['queryParams']
  }
});