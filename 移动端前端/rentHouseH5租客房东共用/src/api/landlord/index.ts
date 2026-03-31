/**
 * 房东API接口统一导出
 */

// 仪表板相关API
export * from './dashboard';

// 公寓管理API
export * from './apartment';

// 房源管理API
export * from './room';

// 审核管理API
export * from './review';

// 申请管理API
export * from './application';

// 预约管理API
export * from './appointment';

// 其他管理API（属性、配套、杂费等）
export * from './management';

// 租约管理API
export * from './lease';

// 导出类型定义
export type {
  // 仪表板类型
  LandlordDashboardVo,
  QuickActionItem,
  QuickActionsResponse,
  LandlordStatistics,
  LandlordOverview
} from './dashboard';

export type {
  // 公寓类型
  ApartmentInfo,
  ApartmentItemVo,
  ApartmentDetailVo,
  ApartmentSubmitVo,
  ApartmentQueryVo
} from './apartment';

export type {
  // 房源类型
  RoomInfo,
  RoomItemVo,
  RoomDetailVo,
  RoomSubmitVo,
  RoomQueryVo
} from './room';

export type {
  // 审核类型
  RoomReviewStatus,
  RoomReview,
  ReviewDetailInfo,
  ReviewStatistics
} from './review';

export type {
  // 申请类型
  DirectMoveInApplicationDetailVO,
  ApplicationStatistics
} from './application';

export type {
  // 预约类型
  AppointmentItemVo,
  AppointmentDetailVo,
  AppointmentStatistics
} from './appointment';

export type {
  // 管理类型
  AttrKey,
  AttrValue,
  FacilityInfo,
  FeeKey,
  FeeValue,
  LabelInfo,
  PaymentType,
  LeaseTerm,
  PageResult,
  GraphVo,
  AttrValueVo,
  FeeValueVo
} from './management';

export type {
  // 租约管理类型
  LeaseStatus,
  LeaseSourceType,
  LandlordLeaseQueryVo,
  LandlordLeaseVo,
  LandlordLeaseDetailVo,
  LandlordLeaseStatisticsVo,
  StatusStatVo,
  SourceStatVo,
  LandlordLeaseCreateVo
} from './lease';