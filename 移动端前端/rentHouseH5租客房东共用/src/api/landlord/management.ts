import http from "@/utils/http";

/**
 * 属性键信息
 */
export interface AttrKey {
  id?: number;
  name: string;
}

/**
 * 属性值信息
 */
export interface AttrValue {
  id?: number;
  name: string;
  attrKeyId: number;
}

/**
 * 设施信息
 */
export interface FacilityInfo {
  id?: number;
  type: number;
  name: string;
  icon?: string;
  creatorType?: number; // 创建者类型：0-系统，1-官方，2-房东
  creatorId?: number;   // 创建者ID
}

/**
 * 杂费键信息
 */
export interface FeeKey {
  id?: number;
  name: string;
  unit?: string;
  creatorType?: number; // 创建者类型：0-系统，1-官方，2-房东
  creatorId?: number;   // 创建者ID
}

/**
 * 杂费值信息
 */
export interface FeeValue {
  id?: number;
  name: string;
  unit: string;
  feeKeyId: number;
  creatorType?: number; // 创建者类型：0-系统，1-官方，2-房东
  creatorId?: number;   // 创建者ID
}

/**
 * 标签信息
 */
export interface LabelInfo {
  id?: number;
  type: number;
  name: string;
}

/**
 * 支付方式
 */
export interface PaymentType {
  id?: number;
  name: string;
  payMonthCount?: number;
  additionalInfo?: string;
  creatorType?: number;
  creatorId?: number;
  // 兼容数据库字段名
  pay_month_count?: number;
  additional_info?: string;
  creator_type?: number;
  creator_id?: number;
}

/**
 * 租期信息
 */
export interface LeaseTerm {
  id?: number;
  name: string;
  monthCount: number;
  unit: string;
  description?: string;
}

/**
 * 属性键信息（包含属性值列表）
 */
export interface AttrKeyVo extends AttrKey {
  attrValueList?: AttrValue[];
}

// ==================== 属性管理 ====================

/**
 * 获取属性键列表（包含属性值）
 */
export function getAttrKeys() {
  return http.get<AttrKeyVo[]>("/app/landlord/attr/list");
}

/**
 * 创建属性键
 */
export function createAttrKey(landlordId: number, attrKey: AttrKey) {
  return http.post<number>("/app/landlord/attr/key/saveOrUpdate", attrKey, {
    params: { landlordId }
  });
}

/**
 * 更新属性键
 */
export function updateAttrKey(landlordId: number, attrKey: AttrKey) {
  return http.post<void>("/app/landlord/attr/key/saveOrUpdate", attrKey, {
    params: { landlordId }
  });
}

/**
 * 删除属性键
 */
export function deleteAttrKey(landlordId: number, attrKeyId: number) {
  return http.delete<void>(`/app/landlord/attr/key/deleteById?attrKeyId=${attrKeyId}&landlordId=${landlordId}`);
}

/**
 * 创建属性值
 */
export function createAttrValue(landlordId: number, attrValue: AttrValue) {
  return http.post<number>("/app/landlord/attr/value/saveOrUpdate", attrValue, {
    params: { landlordId }
  });
}

/**
 * 更新属性值
 */
export function updateAttrValue(landlordId: number, attrValue: AttrValue) {
  return http.post<void>("/app/landlord/attr/value/saveOrUpdate", attrValue, {
    params: { landlordId }
  });
}

/**
 * 删除属性值
 */
export function deleteAttrValue(landlordId: number, id: number) {
  return http.delete<void>(`/app/landlord/attr/value/deleteById?id=${id}&landlordId=${landlordId}`);
}

// ==================== 配套设施管理 ====================

/**
 * 获取配套设施列表
 */
export function getFacilities(landlordId?: number, type?: number) {
  const params: any = {};
  if (type !== undefined) params.type = type;
  if (landlordId !== undefined) params.landlordId = landlordId;

  return http.get<FacilityInfo[]>(`/app/landlord/facility/list?${new URLSearchParams(params).toString()}`);
}

/**
 * 获取房东可用的配套设施列表（官方+房东发布，用于编辑选择）
 */
export function getAvailableFacilitiesForEdit(landlordId: number) {
  return http.get<FacilityInfo[]>(`/app/landlord/apartment/facilities?landlordId=${landlordId}`);
}

/**
 * 创建配套设施
 */
export function createFacility(landlordId: number, facility: FacilityInfo) {
  return http.post<number>("/app/landlord/facility/saveOrUpdate", facility, {
    params: { landlordId }
  });
}

/**
 * 更新配套设施
 */
export function updateFacility(landlordId: number, facility: FacilityInfo) {
  return http.post<void>("/app/landlord/facility/saveOrUpdate", facility, {
    params: { landlordId }
  });
}

/**
 * 删除配套设施
 */
export function deleteFacility(landlordId: number, id: number) {
  return http.delete<void>(`/app/landlord/facility/deleteById?id=${id}&landlordId=${landlordId}`);
}

/**
 * 杂费键信息（包含杂费值列表）
 */
export interface FeeKeyVo extends FeeKey {
  feeValueList?: FeeValue[];
}

// ==================== 杂费管理 ====================

/**
 * 获取杂费键和杂费值列表
 */
export function getFeeKeys(landlordId?: number) {
  const params = landlordId ? `?landlordId=${landlordId}` : '';
  return http.get<FeeKeyVo[]>(`/app/landlord/fee/list${params}`);
}

/**
 * 创建杂费键
 */
export function createFeeKey(landlordId: number, feeKey: FeeKey) {
  return http.post<number>("/app/landlord/fee/key/saveOrUpdate", feeKey, {
    params: { landlordId }
  });
}

/**
 * 更新杂费键
 */
export function updateFeeKey(landlordId: number, feeKey: FeeKey) {
  return http.post<void>("/app/landlord/fee/key/saveOrUpdate", feeKey, {
    params: { landlordId }
  });
}

/**
 * 删除杂费键
 */
export function deleteFeeKey(landlordId: number, feeKeyId: number) {
  return http.delete<void>(`/app/landlord/fee/key/deleteById?feeKeyId=${feeKeyId}&landlordId=${landlordId}`);
}

/**
 * 创建杂费值
 */
export function createFeeValue(landlordId: number, feeValue: FeeValue) {
  return http.post<number>("/app/landlord/fee/value/saveOrUpdate", feeValue, {
    params: { landlordId }
  });
}

/**
 * 更新杂费值
 */
export function updateFeeValue(landlordId: number, feeValue: FeeValue) {
  return http.post<void>("/app/landlord/fee/value/saveOrUpdate", feeValue, {
    params: { landlordId }
  });
}

/**
 * 删除杂费值
 */
export function deleteFeeValue(landlordId: number, id: number) {
  return http.delete<void>(`/app/landlord/fee/value/deleteById?id=${id}&landlordId=${landlordId}`);
}

// ==================== 标签管理 ====================

/**
 * 获取标签列表
 */
export function getLabels(type?: number, landlordId?: number) {
  const params: any = {};
  if (type !== undefined) params.type = type;
  if (landlordId !== undefined) params.landlordId = landlordId;

  return http.get<LabelInfo[]>(`/app/landlord/label/list?${new URLSearchParams(params).toString()}`);
}

/**
 * 获取房东可用的标签列表（官方+房东发布，用于编辑选择）
 */
export function getAvailableLabelsForEdit(landlordId: number) {
  return http.get<LabelInfo[]>(`/app/landlord/apartment/labels?landlordId=${landlordId}`);
}

/**
 * 创建标签
 */
export function createLabel(label: LabelInfo, landlordId: number) {
  return http.post<number>("/app/landlord/label/saveOrUpdate", label, {
    params: { landlordId }
  });
}

/**
 * 更新标签
 */
export function updateLabel(label: LabelInfo, landlordId: number) {
  return http.post<void>("/app/landlord/label/saveOrUpdate", label, {
    params: { landlordId }
  });
}

/**
 * 删除标签
 */
export function deleteLabel(id: number, landlordId: number) {
  return http.delete<void>(`/app/landlord/label/deleteById?id=${id}&landlordId=${landlordId}`);
}

// ==================== 支付方式管理 ====================

/**
 * 获取支付方式列表
 */
export function getPaymentTypes(landlordId?: number) {
  const params = landlordId ? `?landlordId=${landlordId}` : '';
  return http.get<PaymentType[]>(`/app/landlord/payment/list${params}`);
}

/**
 * 创建支付方式
 */
export function createPaymentType(landlordId: number, paymentType: PaymentType) {
  return http.post<number>("/app/landlord/payment/saveOrUpdate", paymentType, {
    params: { landlordId }
  });
}

/**
 * 更新支付方式
 */
export function updatePaymentType(landlordId: number, paymentType: PaymentType) {
  return http.post<void>("/app/landlord/payment/saveOrUpdate", paymentType, {
    params: { landlordId }
  });
}

/**
 * 删除支付方式
 */
export function deletePaymentType(landlordId: number, id: number) {
  return http.delete<void>(`/app/landlord/payment/deleteById?id=${id}&landlordId=${landlordId}`);
}

// ==================== 租期管理 ====================

/**
 * 获取租期列表
 */
export function getLeaseTerms(landlordId?: number) {
  const params = landlordId ? `?landlordId=${landlordId}` : '';
  return http.get<LeaseTerm[]>(`/app/landlord/lease-term/list${params}`);
}

/**
 * 创建租期
 */
export function createLeaseTerm(landlordId: number, leaseTerm: LeaseTerm) {
  return http.post<number>("/app/landlord/lease-term/saveOrUpdate", leaseTerm, {
    params: { landlordId }
  });
}

/**
 * 更新租期
 */
export function updateLeaseTerm(landlordId: number, leaseTerm: LeaseTerm) {
  return http.post<void>("/app/landlord/lease-term/saveOrUpdate", leaseTerm, {
    params: { landlordId }
  });
}

/**
 * 删除租期
 */
export function deleteLeaseTerm(landlordId: number, id: number) {
  return http.delete<void>(`/app/landlord/lease-term/deleteById?id=${id}&landlordId=${landlordId}`);
}