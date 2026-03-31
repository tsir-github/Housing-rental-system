import http from "@/utils/http";

/**
 * 房源审核状态项
 */
export interface ReviewStatusItem {
  roomId: number;
  roomNumber: string;
  rent: number;
  apartmentId: number;
  apartmentName: string;        // 新增：公寓名称
  apartmentAddress: string;     // 新增：公寓地址
  apartmentFullAddress: string; // 新增：公寓完整地址
  reviewStatus: number;
  reviewStatusDesc: string;
  complianceScore: number;
  lastReviewedAt: string;
  isRelease: number;
  createTime: string;
  updateTime: string;
  improvementSuggestion: string;
  // 审核详情信息
  reviewId?: number;
  reviewerName?: string;
  reviewComments?: string;
  overallRating?: number;       // 修改：改为Integer类型对应的number
  reviewedAt?: string;
  photoQualityScore?: number;
  descriptionAccuracyScore?: number;
  facilityCompletenessScore?: number;
  locationAccuracyScore?: number;
}

/**
 * 房源审核详情
 */
export interface ReviewDetail {
  roomInfo?: {
    id: number;
    roomNumber: string;
    rent: number;
    isRelease: number;
  };
  apartmentInfo?: {
    id: number;
    name: string;
    provinceName: string;
    cityName: string;
    districtName: string;
    addressDetail: string;
  };
  reviewStatus: number;
  reviewStatusDesc: string;
  complianceScore?: number;
  lastReviewedAt?: string;
  latestReview?: RoomReview;
  overallRating?: number;
  reviewComments?: string;
  photoQualityScore?: number;
  descriptionAccuracyScore?: number;
  facilityCompletenessScore?: number;
  locationAccuracyScore?: number;
  reviewedAt?: string;
  reviewerName?: string;
  improvementSuggestion?: string;
  reviewHistory?: RoomReview[];
}

/**
 * 房源审核记录
 */
export interface RoomReview {
  id: number;
  roomId: number;
  reviewerId: number;
  reviewerName?: string;
  status: number;
  overallRating?: number;
  photoQualityScore?: number;
  descriptionAccuracyScore?: number;
  facilityCompletenessScore?: number;
  locationAccuracyScore?: number;
  reviewComments?: string;
  improvementSuggestion?: string;
  reviewedAt: string;
  createTime: string;
  updateTime: string;
}

/**
 * 审核统计信息
 */
export interface ReviewStatistics {
  totalRooms: number;
  pendingReview: number;
  approved: number;
  rejected: number;
  avgComplianceScore: number;
  approvalRate: number;
}

/**
 * 分页查询参数
 */
export interface ReviewStatusParams {
  landlordId: number;
  current?: number;
  size?: number;
  reviewStatus?: number;
}

/**
 * 获取房源审核状态列表
 */
export function getReviewStatusList(params: ReviewStatusParams) {
  return http.get<{
    records: ReviewStatusItem[];
    total: number;
    size: number;
    current: number;
    pages: number;
  }>("/app/landlord/review/status", params);
}

/**
 * 获取审核统计信息
 */
export function getReviewStatistics(landlordId: number) {
  return http.get<ReviewStatistics>("/app/landlord/review/review-statistics", {
    landlordId
  });
}

/**
 * 获取房源审核详情
 */
export function getReviewDetail(roomId: number, landlordId: number) {
  return http.get<ReviewDetail>(`/app/landlord/review/detail/${roomId}`, {
    landlordId
  });
}

/**
 * 获取房源审核历史
 */
export function getReviewHistory(roomId: number, landlordId: number) {
  return http.get<RoomReview[]>(`/app/landlord/review/review-history/${roomId}`, {
    landlordId
  });
}

/**
 * 审核状态枚举
 */
export enum ReviewStatus {
  PENDING = 1,    // 待审核
  APPROVED = 2,   // 审核通过
  REJECTED = 3    // 审核拒绝
}

/**
 * 获取审核状态配置
 */
export function getReviewStatusConfig(status: number) {
  const configs = {
    1: { type: 'warning', text: '待审核', color: '#ff976a', icon: 'clock-o' },
    2: { type: 'success', text: '审核通过', color: '#07c160', icon: 'passed' },
    3: { type: 'danger', text: '审核拒绝', color: '#ee0a24', icon: 'close' }
  };
  
  return configs[status as keyof typeof configs] || {
    type: 'default',
    text: '未知状态',
    color: '#969799',
    icon: 'question-o'
  };
}

/**
 * 格式化合规性评分
 */
export function formatComplianceScore(score: number | null | undefined): string {
  if (score === null || score === undefined) {
    return '暂无评分';
  }
  return `${score.toFixed(1)}分`;
}

/**
 * 获取综合评级文字
 */
export function getOverallRatingText(rating: number | null | undefined): string {
  if (rating === null || rating === undefined) {
    return '暂无评级';
  }
  
  const ratingMap: Record<number, string> = {
    1: '优秀',
    2: '良好', 
    3: '一般',
    4: '较差'
  };
  
  return ratingMap[rating] || '未知评级';
}

/**
 * 获取综合评级等级
 */
export function getOverallRatingLevel(rating: number | null | undefined): {
  level: string;
  color: string;
} {
  if (rating === null || rating === undefined) {
    return { level: '暂无评级', color: '#969799' };
  }
  
  const levelMap: Record<number, { level: string; color: string }> = {
    1: { level: '优秀', color: '#07c160' },
    2: { level: '良好', color: '#1989fa' },
    3: { level: '一般', color: '#ff976a' },
    4: { level: '较差', color: '#ee0a24' }
  };
  
  return levelMap[rating] || { level: '未知评级', color: '#969799' };
}

/**
 * 获取合规性评分等级
 */
export function getScoreLevel(score: number | null | undefined): {
  level: string;
  color: string;
} {
  if (score === null || score === undefined) {
    return { level: '暂无评分', color: '#969799' };
  }
  
  if (score >= 9.0) {
    return { level: '优秀', color: '#07c160' };
  } else if (score >= 7.5) {
    return { level: '良好', color: '#1989fa' };
  } else if (score >= 6.0) {
    return { level: '一般', color: '#ff976a' };
  } else {
    return { level: '较差', color: '#ee0a24' };
  }
}