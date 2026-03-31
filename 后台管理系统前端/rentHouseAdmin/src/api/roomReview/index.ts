import http from '@/utils/http'
import type {
  RoomReviewListParams,
  RoomReviewListResult,
  RoomReviewVo,
  RoomReviewSubmitVo,
  RoomScoreInfo,
  PropertyReviewResult
} from './types'

/**
 * 房源审核管理API
 */
export const roomReviewApi = {
  /**
   * 分页查询待审核房源列表
   */
  getPendingReviews: (params: RoomReviewListParams) => {
    return http.get<RoomReviewListResult>('/admin/room/review/pending', params)
  },

  /**
   * 分页查询房源审核历史
   */
  getReviewHistory: (params: RoomReviewListParams) => {
    return http.get<RoomReviewListResult>('/admin/room/review/history', params)
  },

  /**
   * 根据ID获取房源审核详情
   */
  getReviewById: (id: number) => {
    return http.get<RoomReviewVo>(`/admin/room/review/${id}`)
  },

  /**
   * 手动审核提交房源审核
   */
  submitReview: (data: RoomReviewSubmitVo) => {
    return http.post<void>('/admin/room/review/submit', data)
  },

  /**
   * 自动审核房源
   */
  autoReviewRoom: (roomId: number, reviewerId: number) => {
    return http.post<void>(`/admin/room/review/auto/${roomId}`, null, {
      params: { reviewerId }
    })
  },

  /**
   * 执行智能综合审核
   */
  comprehensiveReview: (roomId: number, reviewerId: number) => {
    return http.post<PropertyReviewResult>(`/admin/room/review/comprehensive/${roomId}`, null, {
      params: { reviewerId }
    })
  },

  /**
   * 获取房源综合评分
   */
  getRoomScore: (roomId: number) => {
    return http.get<RoomScoreInfo>(`/admin/room/review/score/${roomId}`)
  },

  /**
   * 房东查看房源审核状态
   */
  getLandlordReviews: (landlordId: number, params: RoomReviewListParams) => {
    return http.get<RoomReviewListResult>(`/admin/room/review/landlord/${landlordId}`, params)
  },

  /**
   * 根据房间ID查询审核状态
   */
  getRoomReviewStatus: (roomId: number) => {
    return http.get<RoomReviewVo>(`/admin/room/review/room/${roomId}/status`)
  },

  /**
   * 批量审核房源
   */
  batchReview: (data: RoomReviewSubmitVo[]) => {
    return http.post<void>('/admin/room/review/batch', data)
  }
}