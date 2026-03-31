import http from "@/utils/http";

/**
 * 预约列表项
 */
export interface AppointmentItemVo {
  id: number;
  userId: number;
  userName?: string;
  userPhone?: string;
  roomId: number;
  roomNumber?: string;
  apartmentId?: number;
  apartmentName?: string;
  appointmentTime: string;
  additionalInfo?: string;
  appointmentStatus: number;
  statusDesc?: string;
  guideType: number;
  guideId: number;
  guideName?: string;
  createTime: string;
  updateTime: string;
}

/**
 * 预约详情
 */
export interface AppointmentDetailVo extends AppointmentItemVo {
  roomInfo?: any;
  apartmentInfo?: any;
  userInfo?: any;
  guideInfo?: any;
}

/**
 * 预约统计信息
 */
export interface AppointmentStatistics {
  totalAppointments: number;
  waitingAppointments: number;  // 待看房数量
  viewedAppointments: number;   // 已看房数量
  canceledAppointments: number; // 已取消数量
  confirmationRate: number;     // 确认率
  // 为了兼容前端组件，添加别名字段
  pendingAppointments?: number; // 等同于 waitingAppointments
  todayAppointments?: number;   // 今日预约数（如果后端支持）
}

/**
 * 获取房东预约列表
 */
export function getLandlordAppointments(landlordId: number) {
  return http.get<AppointmentItemVo[]>("/app/landlord/appointment/list", {
    landlordId
  });
}

/**
 * 根据带看人类型和ID查询预约列表
 */
export function getAppointmentsByGuide(guideType: number, guideId: number) {
  return http.get<AppointmentItemVo[]>("/app/appointment/listByGuide", {
    guideType,
    guideId
  });
}

/**
 * 根据房间ID查询预约列表
 */
export function getAppointmentsByRoom(roomId: number) {
  return http.get<AppointmentItemVo[]>("/app/appointment/listByRoom", {
    roomId
  });
}

/**
 * 获取预约详情
 */
export function getAppointmentDetail(id: number) {
  return http.get<AppointmentDetailVo>("/app/appointment/getDetailById", {
    id
  });
}

/**
 * 房东获取预约详情（包含权限验证）
 */
export function getLandlordAppointmentDetail(appointmentId: number, landlordId: number) {
  return http.get<AppointmentDetailVo>("/app/landlord/appointment/detail", {
    appointmentId,
    landlordId
  });
}

/**
 * 处理预约（确认/拒绝）
 */
export function processAppointment(params: {
  appointmentId: number;
  status: number;
  remarks?: string;
  landlordId?: number;
}) {
  return http.post<void>("/app/landlord/appointment/process", undefined, {
    params: {
      appointmentId: params.appointmentId,
      landlordId: params.landlordId || 1, // 从参数获取，如果没有则使用默认值
      status: params.status,
      remarks: params.remarks
    }
  });
}

/**
 * 创建预约（支持带看人选择）
 */
export function createAppointment(appointmentData: {
  roomId: number;
  appointmentTime: string;
  additionalInfo?: string;
  guideType?: number;
  guideId?: number;
}) {
  return http.post<number>("/app/appointment/create", appointmentData);
}

/**
 * 更新预约信息
 */
export function updateAppointment(appointmentData: {
  id: number;
  appointmentTime?: string;
  additionalInfo?: string;
  appointmentStatus?: number;
}) {
  return http.post<void>("/app/appointment/saveOrUpdate", appointmentData);
}

/**
 * 获取预约统计信息
 */
export function getAppointmentStatistics(landlordId: number) {
  return http.get<AppointmentStatistics>("/app/landlord/appointment/statistics", {
    landlordId
  });
}

/**
 * 房东确认预约并获取租约创建信息
 */
export function confirmAppointmentAndCreateLease(appointmentId: number, landlordId: number) {
  return http.post<{
    appointmentId: number;
    name: string;
    phone: string;
    apartmentId: number;
    roomId: number;
    apartmentName: string;
    roomNumber: string;
  }>("/app/landlord/appointment/confirmAndCreateLease", undefined, {
    params: {
      appointmentId,
      landlordId
    }
  });
}