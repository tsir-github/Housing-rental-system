import http from "@/utils/http";

/**
 * 房东仪表板数据接口
 */
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
  lastUpdateTime: string;
  landlordLevel: string;
  creditScore: number;
}

/**
 * 快速操作菜单项
 */
export interface QuickActionItem {
  key: string;
  title: string;
  icon: string;
  path: string;
}

/**
 * 快速操作菜单响应
 */
export interface QuickActionsResponse {
  actions: QuickActionItem[];
}

/**
 * 房东统计信息
 */
export interface LandlordStatistics {
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
}

/**
 * 房东概览信息
 */
export interface LandlordOverview {
  statistics: LandlordStatistics;
  permissions: string[];
  landlordId: number;
}

/**
 * 获取房东仪表板数据
 */
export function getDashboardData() {
  return http.get<LandlordDashboardVo>("/app/landlord/dashboard/data");
}

/**
 * 获取房东经营统计
 */
export function getStatistics() {
  return http.get<LandlordStatistics>("/app/landlord/dashboard/statistics");
}

/**
 * 获取房东快速操作菜单
 */
export function getQuickActions() {
  return http.get<QuickActionsResponse>("/app/landlord/dashboard/quick-actions");
}

/**
 * 查看房东权限状态
 */
export function getPermissions() {
  return http.get<string[]>("/app/landlord/dashboard/permissions");
}

/**
 * 检查功能权限
 */
export function checkPermission(permission: string) {
  return http.get<boolean>("/app/landlord/dashboard/check-permission", {
    permission
  });
}

/**
 * 检查资源操作权限
 */
export function checkResourcePermission(resourceType: string, resourceId: number) {
  return http.get<boolean>("/app/landlord/dashboard/check-resource-permission", {
    resourceType,
    resourceId
  });
}

/**
 * 房东仪表板概览
 */
export function getOverview() {
  return http.get<LandlordOverview>("/app/landlord/dashboard/overview");
}