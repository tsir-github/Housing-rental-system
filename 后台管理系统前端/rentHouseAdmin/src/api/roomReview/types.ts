import { ReviewStatus } from '@/enums/constEnums'

/**
 * 房源审核查询参数
 */
export interface RoomReviewQueryVo {
  status?: ReviewStatus
  landlordId?: number
  roomId?: number
  apartmentId?: number
  reviewerId?: number
  startDate?: string
  endDate?: string
}

/**
 * 房源审核信息
 */
export interface RoomReviewVo {
  id: number
  roomId: number
  apartmentId: number
  landlordId: number
  reviewerId?: number
  status: ReviewStatus
  reviewNotes?: string
  rejectionReason?: string
  complianceScore: number
  photoQualityScore: number
  descriptionAccuracyScore: number
  facilityCompletenessScore: number
  locationAccuracyScore: number
  overallScore: number
  createTime: string
  updateTime: string
  reviewTime?: string

  // 关联信息（前端展示用）
  roomNumber?: string
  apartmentName?: string
  landlordName?: string
  landlordPhone?: string
  reviewerName?: string
  statusName?: string
}

/**
 * 房源审核列表查询参数
 */
export interface RoomReviewListParams {
  current?: number
  size?: number
  status?: ReviewStatus
  landlordId?: number
  roomId?: number
  apartmentId?: number
  reviewerId?: number
  startDate?: string
  endDate?: string
}

/**
 * 房源审核列表返回结果
 */
export interface RoomReviewListResult {
  records: RoomReviewVo[]
  total: number
  current: number
  size: number
}

/**
 * 房源审核提交参数
 */
export interface RoomReviewSubmitVo {
  id?: number                              // 审核ID（更新时需要）
  roomId: number                          // 房间ID（必填）
  apartmentId: number                     // 公寓ID（必填）
  landlordId?: number                     // 房东ID
  reviewerId: number                      // 审核员ID（必填）
  complianceScore: number                 // 符合率评分（0-10分，必填）
  photoQualityScore?: number              // 照片质量评分（0-10分）
  descriptionAccuracyScore?: number       // 描述准确性评分（0-10分）
  facilityCompletenessScore?: number      // 设施完整性评分（0-10分）
  locationAccuracyScore?: number          // 位置准确性评分（0-10分）
  status: ReviewStatus                    // 审核状态（必填）
  overallRating?: number                  // 综合评级：1-优秀，2-良好，3-一般，4-较差
  reviewComments?: string                 // 审核意见
  rejectionReasons?: string[]             // 拒绝原因列表
}

/**
 * 房源评分信息
 */
export interface RoomScoreInfo {
  complianceScore: number
  photoQualityScore: number
  descriptionAccuracyScore: number
  facilityCompletenessScore: number
  locationAccuracyScore: number
  overallScore: number
}

/**
 * 智能审核结果
 */
export interface PropertyReviewResult {
  roomId: number
  overallScore: number
  recommendation: 'APPROVE' | 'REJECT' | 'MANUAL_REVIEW'
  reasons: string[]
  scores: RoomScoreInfo
}