import http from "@/utils/http";

/**
 * 直接入住申请详情
 */
export interface DirectMoveInApplicationDetailVO {
  id: number;
  userId: number;
  roomId: number;
  apartmentId?: number;
  landlordId?: number;
  name?: string;
  phone?: string;
  identificationNumber?: string;
  email?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
  leaseStartDate: string;
  leaseEndDate: string;
  leaseTermId: number;
  rent?: number;
  deposit?: number;
  paymentTypeId: number;
  status: number;
  processorId?: number;
  processorType?: number;
  rejectionReason?: string;
  processedAt?: string;
  additionalInfo?: string;
  createTime: string;
  
  // 关联信息
  roomNumber?: string;
  apartmentName?: string;
  leaseTermName?: string;
  paymentTypeName?: string;
  
  // 兼容旧字段名
  userName?: string;
  userPhone?: string;
  applicationDate?: string;
  moveInDate?: string;
  leaseTerm?: number;
  statusDesc?: string;
  processorName?: string;
  updateTime?: string;
}

/**
 * 申请统计信息
 */
export interface ApplicationStatistics {
  totalApplications: number;
  pendingApplications: number;
  approvedApplications: number;
  rejectedApplications: number;
  approvalRate: number;
  todayApplications: number;
  monthApplications: number; // 修改字段名以匹配后端
}

/**
 * 房东查看待处理申请列表
 */
export function getPendingApplications(landlordId: number) {
  return http.get<DirectMoveInApplicationDetailVO[]>(`/app/landlord/applications/pending?landlordId=${landlordId}`);
}

/**
 * 房东查看申请历史
 */
export function getApplicationHistory(landlordId: number) {
  return http.get<DirectMoveInApplicationDetailVO[]>(`/app/landlord/applications/history?landlordId=${landlordId}`);
}

/**
 * 房东查看申请详情
 */
export function getApplicationDetail(applicationId: number) {
  return http.get<DirectMoveInApplicationDetailVO>(`/app/landlord/applications/detail/${applicationId}`);
}

/**
 * 房东处理申请（审核通过/拒绝）
 */
export function processApplication(params: {
  id: number;
  landlordId: number;
  approved: boolean;
  rejectionReason?: string;
}) {
  return http.post<void>(`/app/landlord/applications/process/${params.id}`, undefined, {
    params: {
      landlordId: params.landlordId,
      approved: params.approved,
      rejectionReason: params.rejectionReason
    }
  });
}

/**
 * 房东确认申请并生成租约
 */
export function approveApplication(id: number, landlordId: number) {
  return http.post<number>(`/app/landlord/applications/approve/${id}`, undefined, {
    params: { landlordId }
  });
}

/**
 * 房东拒绝申请
 */
export function rejectApplication(params: {
  id: number;
  landlordId: number;
  rejectionReason: string;
}) {
  return http.post<void>(`/app/landlord/applications/reject/${params.id}`, undefined, {
    params: {
      landlordId: params.landlordId,
      rejectionReason: params.rejectionReason
    }
  });
}

/**
 * 房东检查房间可用性
 */
export function checkRoomAvailability(roomId: number) {
  return http.get<boolean>(`/app/landlord/applications/room/${roomId}/availability`);
}

/**
 * 房东获取申请统计
 */
export function getApplicationStatistics(landlordId: number) {
  return http.get<ApplicationStatistics>(`/app/landlord/applications/statistics?landlordId=${landlordId}`);
}

/**
 * 房东获取申请对应的租约ID
 */
export function getApplicationLeaseId(applicationId: number) {
  return http.get<number>(`/app/landlord/applications/${applicationId}/lease`);
}