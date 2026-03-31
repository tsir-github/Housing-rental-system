import { LandlordStatus } from '@/enums/constEnums'

/**
 * 房东权限信息
 */
export interface LandlordPermissionVo {
  landlordId: number
  phone: string
  nickname: string
  avatarUrl?: string
  status: LandlordStatus
  permissions: string[]
  permissionCount: number
  createTime: string
  updateTime: string
  hasLandlordPermission: boolean
}

/**
 * 房东列表查询参数
 */
export interface LandlordListParams {
  current?: number
  size?: number
  phone?: string
  nickname?: string
}

/**
 * 房东列表返回结果
 */
export interface LandlordListResult {
  records: LandlordPermissionVo[]
  total: number
  current: number
  size: number
}

/**
 * 分配权限参数
 */
export interface AssignPermissionsParams {
  landlordId: number
  permissions: string[]
  reason?: string
}

/**
 * 撤销权限参数
 */
export interface RevokePermissionsParams {
  landlordId: number
  permissions: string[]
  reason: string
}

/**
 * 禁用房东参数
 */
export interface DisableLandlordParams {
  reason: string
}