import http from "@/utils/http";

// 预约看房相关接口

/**
 * 房源可用性检查结果
 */
export interface RoomAvailabilityVo {
  available: boolean;
  statusCode: string;
  message: string;
  reason: string;
}

/**
 * 检查房源预约可用性（含友好提示）
 */
export const checkRoomAvailabilityForAppointment = (roomId: number): Promise<{ data: RoomAvailabilityVo }> => {
  return http.get(`/app/appointment/checkRoomAvailability/${roomId}`);
};

/**
 * 创建预约
 */
export const createAppointment = (data: any) => {
  return http.post("/app/appointment/create", data);
};

/**
 * 保存或更新预约
 */
export const saveOrUpdateAppointment = (data: any) => {
  return http.post("/app/appointment/saveOrUpdate", data);
};

/**
 * 获取我的预约列表
 */
export const getMyAppointments = () => {
  return http.get("/app/appointment/listItem");
};

/**
 * 获取预约详情
 */
export const getAppointmentDetail = (appointmentId: number) => {
  return http.get(`/app/appointment/getDetailById?id=${appointmentId}`);
};