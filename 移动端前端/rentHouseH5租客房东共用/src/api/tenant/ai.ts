/**
 * 租客AI助手API接口
 */
import http from '@/utils/http'
import type { PageRes } from '@/api/common/types'

/**
 * 聊天请求参数
 */
export interface ChatRequest {
  message: string
}

/**
 * 关键词提取结果
 */
export interface KeywordExtractionResult {
  districtIds: number[]
  roomTypeAttrValueIds: number[]
  priceRange: {
    min: number
    max: number
  }
  facilityIds: number[]
  moveInDate: string | null
  leaseDuration: number | null
  rawText: string
  confidence: number
}

/**
 * 房源推荐
 */
export interface RoomRecommendation {
  roomId: number
  roomNumber: string
  rent: number
  reviewStatus: string
  apartmentId: number
  apartmentName: string
  location: string
  addressDetail: string
  latitude: string
  longitude: string
  roomAttributes: string[]
  facilities: string[]
  complianceScore: number
  overallRating: number
  photoQualityScore: number
  descriptionAccuracyScore: number
  facilityCompletenessScore: number
  locationAccuracyScore: number
  matchScore: number
  detailPageUrl: string
  imageUrls?: string[]
}

/**
 * 聊天响应
 */
export interface ChatResponse {
  conversationId: string
  userMessage: string
  aiReply: string
  keywords: KeywordExtractionResult
  recommendations: RoomRecommendation[]
  responseTime: number
  tokensUsed: number
}

/**
 * AI对话记录
 */
export interface AiConversation {
  id: number
  userId: number
  messageType: 'USER' | 'AI'
  message: string
  aiModel: string
  tokensUsed: number
  responseTime: number
  keywords: string | null
  recommendedRooms: string | null
  createTime: string
}

/**
 * 对话统计信息
 */
export interface ConversationStatistics {
  totalMessages: number
  userMessages: number
  aiMessages: number
  totalTokensUsed: number
  averageResponseTime: number
  totalRecommendations: number
  recommendationClickRate: number
}

/**
 * 发送消息给AI
 */
export function sendMessage(data: ChatRequest) {
  return http.post<ChatResponse>('/api/tenant/ai/chat', data)
}

/**
 * 获取对话历史
 */
export function getConversationHistory(page: number = 1, pageSize: number = 20) {
  return http.get<PageRes<AiConversation>>('/api/tenant/ai/history', { page, pageSize })
}

/**
 * 获取指定时间范围内的对话历史
 */
export function getConversationHistoryByDateRange(
  startDate?: string,
  endDate?: string,
  page: number = 1,
  pageSize: number = 20
) {
  return http.get<PageRes<AiConversation>>('/api/tenant/ai/history/range', { 
    startDate, 
    endDate, 
    page, 
    pageSize 
  })
}

/**
 * 清空对话记录
 */
export function clearConversationHistory() {
  return http.delete<string>('/api/tenant/ai/clear')
}

/**
 * 获取对话统计信息
 */
export function getConversationStatistics(startDate?: string, endDate?: string) {
  return http.get<ConversationStatistics>('/api/tenant/ai/statistics', { 
    startDate, 
    endDate 
  })
}
