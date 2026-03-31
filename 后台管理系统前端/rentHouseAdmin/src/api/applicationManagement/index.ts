import http from '@/utils/http'
import type {
    DirectMoveInApplicationListParams,
    DirectMoveInApplicationListResult,
    DirectMoveInApplication,
    ProcessApplicationParams
} from './types'

/**
 * 直接入住申请管理API
 */
export const directMoveInApplicationApi = {
    /**
     * 获取待处理申请列表（支持分类筛选）
     */
    getPendingApplications: (params: DirectMoveInApplicationListParams) => {
        return http.get<DirectMoveInApplicationListResult>('/admin/application/direct-move-in/pending', params)
    },

    /**
     * 获取申请历史（支持分类筛选）
     */
    getApplicationHistory: (params: DirectMoveInApplicationListParams) => {
        return http.get<DirectMoveInApplicationListResult>('/admin/application/direct-move-in/history', params)
    },

    /**
     * 获取申请详情
     */
    getApplicationDetail: (id: number) => {
        return http.get<DirectMoveInApplication>(`/admin/application/direct-move-in/${id}`)
    },

    /**
     * 处理申请（审核通过/拒绝）
     */
    processApplication: (id: number, params: ProcessApplicationParams) => {
        return http.post<string>(`/admin/application/direct-move-in/process/${id}`, {}, {
            params
        })
    },

    /**
     * 拒绝申请
     */
    rejectApplication: (id: number, params: Omit<ProcessApplicationParams, 'approved'> & { rejectionReason: string }) => {
        return http.post<void>(`/admin/application/direct-move-in/reject/${id}`, {}, {
            params
        })
    },

    /**
     * 检查房间可用性
     */
    checkRoomAvailability: (roomId: number) => {
        return http.get<boolean>(`/admin/application/direct-move-in/room/${roomId}/availability`)
    }
}