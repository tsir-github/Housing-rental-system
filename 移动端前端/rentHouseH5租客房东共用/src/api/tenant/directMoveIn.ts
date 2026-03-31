import http from "@/utils/http";

// 直接入住申请相关接口

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
 * 提交直接入住申请
 */
export const submitDirectMoveInApplication = (data: any) => {
  return http.post("/app/direct-move-in/apply", data);
};

/**
 * 获取我的申请列表
 */
export const getMyApplications = (userId: number) => {
  return http.get(`/app/direct-move-in/my-applications?userId=${userId}`);
};

/**
 * 获取申请详情
 */
export const getApplicationDetail = (applicationId: number) => {
  return http.get(`/app/direct-move-in/detail/${applicationId}`);
};

/**
 * 验证房源可用性（简单版本）
 * @deprecated 建议使用 checkRoomAvailability 获取详细信息
 */
export const validateRoomAvailability = (roomId: number) => {
  return http.get(`/app/direct-move-in/validate-room/${roomId}`);
};

/**
 * 检查房源可用性详情（含友好提示）
 */
export const checkRoomAvailability = (roomId: number): Promise<{ data: RoomAvailabilityVo }> => {
  return http.get(`/app/direct-move-in/check-room/${roomId}`);
};

/**
 * 取消直接入住申请
 */
export const cancelDirectMoveInApplication = (applicationId: number) => {
  return http.post(`/app/direct-move-in/cancel/${applicationId}`);
};