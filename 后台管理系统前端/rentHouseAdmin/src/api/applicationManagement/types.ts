import { DirectMoveInStatus, ProcessorType, PublisherType } from '@/enums/constEnums'

/**
 * 直接入住申请实体
 */
export interface DirectMoveInApplication {
  id: number
  userId: number
  roomId: number
  apartmentId: number
  landlordId?: number
  name: string
  phone: string
  identificationNumber: string
  email?: string
  emergencyContact?: string
  emergencyPhone?: string
  leaseStartDate: string
  leaseEndDate: string
  leaseTermId: number
  rent: number
  deposit: number
  paymentTypeId: number
  status: DirectMoveInStatus
  processorId?: number
  processorType?: ProcessorType
  rejectionReason?: string
  processedAt?: string
  additionalInfo?: string
  createTime: string
  updateTime: string
  
  // 关联信息（前端展示用）
  roomNumber?: string
  apartmentName?: string
  leaseTermName?: string
  paymentTypeName?: string
  landlordName?: string
  landlordPhone?: string
  processorName?: string
  publisherType?: PublisherType
  publisherTypeName?: string
}

/**
 * 申请列表查询参数
 */
export interface DirectMoveInApplicationListParams {
  current?: number
  size?: number
  userId?: number
  status?: DirectMoveInStatus
  processorId?: number
  processorType?: ProcessorType
  publisherType?: PublisherType
}

/**
 * 申请列表返回结果
 */
export interface DirectMoveInApplicationListResult {
  records: DirectMoveInApplication[]
  total: number
  current: number
  size: number
}

/**
 * 处理申请参数
 */
export interface ProcessApplicationParams {
  processorId: number
  processorType: ProcessorType
  approved: boolean
  rejectionReason?: string
}