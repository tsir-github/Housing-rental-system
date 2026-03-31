import http from "@/utils/http";

/**
 * 房源信息接口
 */
export interface RoomInfo {
  id?: number;
  roomNumber: string;
  rent: number;
  apartmentId: number;
  isRelease: number;
  reviewStatus: number;
  complianceScore?: number;
  lastReviewedAt?: string;
  publisherType: number;
  publisherId: number;
  createTime?: string;
  updateTime?: string;
}

/**
 * 房源列表项
 */
export interface RoomItemVo {
  id: number;
  roomNumber: string;
  rent: number;
  apartmentId: number;
  apartmentName: string;
  publisherType?: number;
  publisherId?: number;
  isRelease: number;
  reviewStatus: number;
  complianceScore?: number;
  lastReviewedAt?: string;
  createTime: string;
  updateTime: string;
  graphVoList: GraphVo[];
}

/**
 * 房源详情
 */
export interface RoomDetailVo extends RoomInfo {
  apartmentInfo: ApartmentInfo;
  graphVoList: GraphVo[];
  attrValueVoList: AttrValueVo[];
  facilityInfoList: FacilityInfo[];
  labelInfoList: LabelInfo[];
  paymentTypeList: PaymentType[];
  leaseTermList: LeaseTerm[];
  feeValueVoList: FeeValueVo[];
}

/**
 * 房源提交数据
 */
export interface RoomSubmitVo extends RoomInfo {
  attrValueIds: number[];
  attrValueVoList: AttrValueVo[];
  facilityInfoIds: number[];
  labelInfoIds: number[];
  paymentTypeIds: number[];
  leaseTermIds: number[];
  feeValueVoList: FeeValueVo[];
  graphVoList: GraphVo[];
}

/**
 * 房源查询参数
 */
export interface RoomQueryVo {
  apartmentId?: number;
  provinceId?: number;
  cityId?: number;
  districtId?: number;
  minRent?: number;
  maxRent?: number;
  paymentTypeId?: number;
  publisherType?: number;
  publisherId?: number;
  isRelease?: number;
  reviewStatus?: number;
}

/**
 * 公寓信息
 */
export interface ApartmentInfo {
  id: number;
  name: string;
  introduction: string;
  districtName: string;
  cityName: string;
  provinceName: string;
  addressDetail: string;
  latitude: string;
  longitude: string;
  phone: string;
}

/**
 * 图片信息
 */
export interface GraphVo {
  id?: number;
  name: string;
  itemType: number;
  itemId: number;
  url: string;
}

/**
 * 属性值信息
 */
export interface AttrValueVo {
  id?: number;
  attrKeyId: number;
  attrKeyName: string;
  attrValueId: number;
  attrValueName: string;
}

/**
 * 属性键值对信息
 */
export interface AttrKeyVo {
  id: number;
  name: string;
  publisherType: number;
  publisherId?: number;
  attrValueList: AttrValue[];
  createTime?: string;
  updateTime?: string;
}

/**
 * 属性值
 */
export interface AttrValue {
  id: number;
  attrKeyId: number;
  name: string;
  createTime?: string;
  updateTime?: string;
}

/**
 * 设施信息
 */
export interface FacilityInfo {
  id: number;
  type: number;
  name: string;
  icon: string;
}

/**
 * 标签信息
 */
export interface LabelInfo {
  id: number;
  type: number;
  name: string;
}

/**
 * 支付方式
 */
export interface PaymentType {
  id: number;
  name: string;
  payMonthCount: number;
  additionalInfo: string;
}

/**
 * 租期
 */
export interface LeaseTerm {
  id: number;
  monthCount: number;
  unit: string;
}

/**
 * 自定义属性请求
 */
export interface CustomAttributeRequest {
  attrKeyName: string;
  attrValueName: string;
  landlordId: number;
  roomId?: number;
}

/**
 * 费用信息
 */
export interface FeeValueVo {
  id?: number;
  feeKeyId: number;
  feeKeyName: string;
  unit: string;
  value: string;
}

/**
 * 分页响应
 */
export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * 房东发布房源
 */
export function publishRoom(roomData: RoomSubmitVo, landlordId: number) {
  return http.post<void>("/app/landlord/room/publish", roomData, {
    params: { landlordId }
  });
}

/**
 * 房东查看自己发布的房源列表
 */
export function getMyRooms(params: {
  current: number;
  size: number;
  landlordId: number;
  queryVo?: RoomQueryVo;
}) {
  // 构建查询字符串 - 所有参数都作为Query Parameters
  const queryParams: string[] = [
    `current=${params.current}`,
    `size=${params.size}`,
    `landlordId=${params.landlordId}`
  ];
  
  // 展开queryVo的属性到Query参数
  if (params.queryVo) {
    if (params.queryVo.apartmentId) {
      queryParams.push(`apartmentId=${params.queryVo.apartmentId}`);
    }
    if (params.queryVo.provinceId) {
      queryParams.push(`provinceId=${params.queryVo.provinceId}`);
    }
    if (params.queryVo.cityId) {
      queryParams.push(`cityId=${params.queryVo.cityId}`);
    }
    if (params.queryVo.districtId) {
      queryParams.push(`districtId=${params.queryVo.districtId}`);
    }
    if (params.queryVo.minRent) {
      queryParams.push(`minRent=${params.queryVo.minRent}`);
    }
    if (params.queryVo.maxRent) {
      queryParams.push(`maxRent=${params.queryVo.maxRent}`);
    }
    if (params.queryVo.paymentTypeId) {
      queryParams.push(`paymentTypeId=${params.queryVo.paymentTypeId}`);
    }
    if (params.queryVo.publisherType !== undefined && params.queryVo.publisherType !== null) {
      queryParams.push(`publisherType=${params.queryVo.publisherType}`);
    }
    if (params.queryVo.publisherId) {
      queryParams.push(`publisherId=${params.queryVo.publisherId}`);
    }
    if (params.queryVo.isRelease !== undefined && params.queryVo.isRelease !== null) {
      queryParams.push(`isRelease=${params.queryVo.isRelease}`);
    }
    if (params.queryVo.reviewStatus !== undefined && params.queryVo.reviewStatus !== null) {
      queryParams.push(`reviewStatus=${params.queryVo.reviewStatus}`);
    }
  }
  
  const url = `/app/landlord/room/my-rooms?${queryParams.join('&')}`;
  console.log('房源列表API调用:', url);
  
  return http.get<PageResult<RoomItemVo>>(url);
}

/**
 * 房东根据ID获取房源详细信息
 */
export function getRoomDetail(roomId: number, landlordId: number) {
  return http.get<RoomDetailVo>(`/app/landlord/room/detail/${roomId}?landlordId=${landlordId}`);
}

/**
 * 房东修改房源信息
 */
export function updateRoom(roomData: RoomSubmitVo, landlordId: number) {
  return http.put<void>("/app/landlord/room/update", roomData, {
    params: { landlordId }
  });
}

/**
 * 房东删除房源
 */
export function deleteRoom(roomId: number, landlordId: number) {
  return http.delete<void>(`/app/landlord/room/${roomId}`, {
    params: { landlordId }
  });
}

/**
 * 房东修改房源发布状态
 */
export function updateRoomReleaseStatus(roomId: number, status: number, landlordId: number) {
  return http.post<void>("/app/landlord/room/update-release-status", null, {
    params: {
      roomId,
      status,
      landlordId
    }
  });
}

/**
 * 房东重新提交审核
 */
export function resubmitForReview(roomId: number, landlordId: number) {
  return http.post<void>(`/app/landlord/room/resubmit/${roomId}`, null, {
    params: { landlordId }
  });
}

/**
 * 房东房源统计
 */
export function getRoomStatistics(landlordId: number) {
  const url = `/app/landlord/room/statistics?landlordId=${landlordId}`;
  console.log('房源统计API调用:', url);
  
  return http.get<{
    totalRooms: number;
    pendingReview: number;
    approved: number;
    rejected: number;
    published: number;
    avgComplianceScore: number;
    approvalRate: number;
  }>(url);
}

/**
 * 获取房东可选择的公寓列表
 */
export function getAvailableApartments(landlordId: number) {
  return http.get<ApartmentInfo[]>(`/app/landlord/room/available-apartments?landlordId=${landlordId}`);
}

/**
 * 获取房东可选择的属性列表
 */
export function getAvailableAttributes(landlordId: number) {
  return http.get<AttrKeyVo[]>(`/app/landlord/room/available-attributes?landlordId=${landlordId}`);
}

/**
 * 获取房东可选择的配套设施列表
 */
export function getAvailableFacilities(landlordId: number) {
  return http.get<FacilityInfo[]>(`/app/landlord/room/available-facilities?landlordId=${landlordId}`);
}

/**
 * 获取房东可选择的标签列表
 */
export function getAvailableLabels(landlordId: number) {
  return http.get<LabelInfo[]>(`/app/landlord/room/available-labels?landlordId=${landlordId}`);
}

/**
 * 获取房东可选择的支付方式列表
 */
export function getAvailablePaymentTypes(landlordId: number) {
  return http.get<PaymentType[]>(`/app/landlord/room/available-payment-types?landlordId=${landlordId}`);
}

/**
 * 获取房东可选择的租期列表
 */
export function getAvailableLeaseTerms(landlordId: number) {
  return http.get<LeaseTerm[]>(`/app/landlord/room/available-lease-terms?landlordId=${landlordId}`);
}

/**
 * 清除房源详情缓存
 */
export function clearRoomCache(roomId: number, landlordId: number) {
  return http.post<string>(`/app/landlord/room/clear-cache/${roomId}?landlordId=${landlordId}`);
}

/**
 * 房东创建自定义属性
 */
export function createCustomAttribute(request: CustomAttributeRequest) {
  return http.post<AttrKeyVo>("/app/landlord/room/create-custom-attribute", request);
}

/**
 * 获取公寓的杂费信息
 */
export function getApartmentFees(apartmentId: number, landlordId: number) {
  return http.get<Array<{
    fee_key_id: number;
    fee_key_name: string;
    fee_value_id: number;
    fee_value_name: string;
    fee_unit: string;
  }>>(`/app/landlord/room/apartment-fees/${apartmentId}?landlordId=${landlordId}`);
}