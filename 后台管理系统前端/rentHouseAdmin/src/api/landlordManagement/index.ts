import http from '@/utils/http'
import type {
  LandlordListParams,
  LandlordListResult,
  LandlordPermissionVo,
  AssignPermissionsParams,
  RevokePermissionsParams,
  DisableLandlordParams
} from './types'

/**
 * 房东权限管理API
 */
export const landlordPermissionApi = {
  /**
   * 获取房东列表
   */
  getLandlordList: (params: LandlordListParams) => {
    return http.get<LandlordListResult>('/admin/landlord/list', params)
  },

  /**
   * 查看房东权限详情
   */
  getLandlordPermissions: (landlordId: number) => {
    return http.get<string[]>(`/admin/landlord/${landlordId}/permissions`)
  },

  /**
   * 获取可分配权限列表
   */
  getAvailablePermissions: () => {
    return http.get<string[]>('/admin/landlord/available-permissions')
  },

  /**
   * 为房东分配权限
   */
  assignPermissions: (params: AssignPermissionsParams) => {
    return http.post<void>('/admin/landlord/assign-permissions', params)
  },

  /**
   * 撤销房东权限
   */
  revokePermissions: (params: RevokePermissionsParams) => {
    return http.delete<void>('/admin/landlord/revoke-permissions', params)
  },

  /**
   * 重置房东权限
   */
  resetLandlordPermissions: (landlordId: number) => {
    return http.post<void>(`/admin/landlord/${landlordId}/reset-permissions`)
  },

  /**
   * 禁用房东账户
   */
  disableLandlord: (landlordId: number, params: DisableLandlordParams) => {
    return http.post<void>(`/admin/landlord/${landlordId}/disable`, null, { params })
  },

  /**
   * 启用房东账户
   */
  enableLandlord: (landlordId: number) => {
    return http.post<void>(`/admin/landlord/${landlordId}/enable`)
  }
}