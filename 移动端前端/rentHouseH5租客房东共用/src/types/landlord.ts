/**
 * 房东相关类型定义
 * 基于后端接口规范生成
 */

// 基础响应类型
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 枚举类型
export enum ReleaseStatus {
  NOT_RELEASED = 0,    // 未发布
  RELEASED = 1         // 已发布
}

export enum ReviewStatus {
  PENDING = 1,         // 待审核
  APPROVED = 2,        // 审核通过
  REJECTED = 3         // 审核拒绝
}

export enum PublisherType {
  LANDLORD = 1,        // 房东
  AGENT = 2           // 中介
}

// 公寓相关类型
export interface ApartmentItemVo {
  id: number;
  name: string;
  introduction: string;
  districtName: string;
  cityName: string;
  provinceName: string;
  addressDetail: string;
  phone: string;
  isRelease: ReleaseStatus;
  roomCount: number;
  availableRoomCount: number;
  createTime: string;
  updateTime?: string;
  graphVoList?: GraphVo[];  // 公寓图片列表
  labelInfoList?: LabelInfo[];  // 公寓标签列表
  facilityInfoList?: FacilityInfo[];  // 公寓配套列表
  minRent?: number;  // 公寓最低租金
  feeValueVoList?: FeeValueVo[];  // 公寓杂费列表
}

export interface ApartmentQueryVo {
  name?: string;
  landlordId?: number;
  provinceId?: number;
  cityId?: number;
  districtId?: number;
  isRelease?: ReleaseStatus;
}

export interface ApartmentDetailVo extends ApartmentItemVo {
  facilityInfoList: FacilityInfo[];
  labelInfoList: LabelInfo[];
  feeValueVoList: FeeValueVo[];
  graphVoList: GraphVo[];
  roomList: RoomInfo[];
}

export interface ApartmentSubmitVo {
  id?: number;
  name: string;
  introduction: string;
  districtId: number;
  cityId: number;
  provinceId: number;
  addressDetail: string;
  latitude: string;
  longitude: string;
  phone: string;
  isRelease?: ReleaseStatus;  // 添加发布状态字段
  facilityInfoIds?: number[];
  labelIds?: number[];
  feeValueVoList?: FeeValueVo[];
  graphVoList?: GraphVo[];
}

// 房源相关类型
export interface RoomItemVo {
  id: number;
  roomNumber: string;
  rent: number;
  apartmentId: number;
  apartmentName: string;
  isRelease: ReleaseStatus;
  reviewStatus: ReviewStatus;
  complianceScore?: number;
  createTime: string;
  updateTime?: string;
  lastReviewedAt?: string;
  graphVoList?: GraphVo[];
}

export interface RoomQueryVo {
  apartmentId?: number;
  reviewStatus?: ReviewStatus;
  isRelease?: ReleaseStatus;
  minRent?: number;
  maxRent?: number;
  publisherType?: PublisherType;
  publisherId?: number;
}

export interface RoomSubmitVo {
  id?: number;
  roomNumber: string;
  rent: number;
  apartmentId: number;
  publisherType?: PublisherType;
  publisherId?: number;
  isRelease?: ReleaseStatus;
  reviewStatus?: ReviewStatus;
  complianceScore?: number;
  // 扩展字段
  graphVoList?: GraphVo[];
  attrValueIds?: number[];
  facilityInfoIds?: number[];
  labelInfoIds?: number[];
  paymentTypeIds?: number[];
  leaseTermIds?: number[];
}

// 统计数据类型
export interface ApartmentStatistics {
  totalApartments: number;
  publishedApartments: number;
  unpublishedApartments: number;
  totalRooms: number;
}

export interface RoomStatistics {
  totalRooms: number;
  pendingReview: number;
  approved: number;
  rejected: number;
  published: number;
  avgComplianceScore: number;
  approvalRate: number;
}

// 仪表板类型
export interface LandlordDashboardVo {
  landlordId: number;
  apartmentCount: number;
  roomCount: number;
  publishedRoomCount: number;
  availableRoomCount: number;
  occupiedRoomCount: number;
  pendingApplicationCount: number;
  pendingAppointmentCount: number;
  todayAppointmentCount: number;
  occupancyRate: number;
  monthlyNewRooms: number;
  monthlyDeals: number;
  monthlyIncome: number;
  landlordLevel: string;
  creditScore: number;
  lastUpdateTime: string;
}

export interface QuickAction {
  key: string;
  title: string;
  icon: string;
  path: string;
}

export interface QuickActionsResponse {
  actions: QuickAction[];
}

// 辅助类型
export interface FacilityInfo {
  id: number;
  type: number;
  name: string;
  icon: string;
}

export interface LabelInfo {
  id: number;
  type: number;
  name: string;
}

export interface FeeValueVo {
  id: number;
  feeKeyId: number;
  feeKeyName: string;
  name: string;        // 杂费值名称（如：300、60）
  unit: string;        // 单位（如：元/月）
  value?: string;      // 兼容字段，实际使用name字段
}

export interface FeeKeyInfo {
  id: number;
  name: string;
  unit?: string;
  creatorType?: number; // 创建者类型：0-系统，1-官方，2-房东
  creatorId?: number;   // 创建者ID
}

export interface FeeKeyVo extends FeeKeyInfo {
  feeValueList?: FeeValueVo[];
}

export interface GraphVo {
  id: number;
  name: string;
  itemType: number;
  itemId: number;
  url: string;
}

export interface RoomInfo {
  id: number;
  roomNumber: string;
  rent: number;
  isRelease: ReleaseStatus;
  reviewStatus: ReviewStatus;
}