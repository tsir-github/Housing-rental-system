/**
 * AI聊天状态管理
 */
import { defineStore } from 'pinia'
import type { AiConversation, RoomRecommendation } from '@/api/tenant/ai'

/**
 * 消息项
 */
export interface MessageItem {
  id: string
  type: 'USER' | 'AI'
  content: string
  timestamp: string
  recommendations?: RoomRecommendation[]
  loading?: boolean
}

interface AiChatState {
  messages: MessageItem[]
  loading: boolean
  currentRecommendations: RoomRecommendation[]
  conversationId: string | null
  hasMore: boolean
  currentPage: number
}

export const useAiChatStore = defineStore('aiChat', {
  state: (): AiChatState => ({
    messages: [],
    loading: false,
    currentRecommendations: [],
    conversationId: null,
    hasMore: true,
    currentPage: 0  // 从0开始，第一次加载时会变成1
  }),

  getters: {
    /**
     * 获取所有消息
     */
    allMessages: (state) => state.messages,

    /**
     * 是否正在加载
     */
    isLoading: (state) => state.loading,

    /**
     * 当前推荐房源
     */
    recommendations: (state) => state.currentRecommendations,

    /**
     * 是否有更多历史记录
     */
    hasMoreHistory: (state) => state.hasMore
  },

  actions: {
    /**
     * 添加用户消息
     */
    addUserMessage(content: string) {
      const message: MessageItem = {
        id: `user-${Date.now()}`,
        type: 'USER',
        content,
        timestamp: new Date().toISOString()
      }
      this.messages.push(message)
    },

    /**
     * 添加AI消息
     */
    addAiMessage(content: string, recommendations?: RoomRecommendation[]) {
      const message: MessageItem = {
        id: `ai-${Date.now()}`,
        type: 'AI',
        content,
        timestamp: new Date().toISOString(),
        recommendations
      }
      this.messages.push(message)
      
      if (recommendations && recommendations.length > 0) {
        this.currentRecommendations = recommendations
      }
    },

    /**
     * 添加加载中的AI消息
     */
    addLoadingMessage() {
      const message: MessageItem = {
        id: `loading-${Date.now()}`,
        type: 'AI',
        content: '',
        timestamp: new Date().toISOString(),
        loading: true
      }
      this.messages.push(message)
      return message.id
    },

    /**
     * 更新加载中的消息
     */
    updateLoadingMessage(id: string, content: string, recommendations?: RoomRecommendation[]) {
      const index = this.messages.findIndex(m => m.id === id)
      if (index !== -1) {
        this.messages[index] = {
          ...this.messages[index],
          content,
          loading: false,
          recommendations
        }
      }
    },

    /**
     * 移除加载中的消息
     */
    removeLoadingMessage(id: string) {
      const index = this.messages.findIndex(m => m.id === id)
      if (index !== -1) {
        this.messages.splice(index, 1)
      }
    },

    /**
     * 设置加载状态
     */
    setLoading(loading: boolean) {
      this.loading = loading
    },

    /**
     * 设置对话ID
     */
    setConversationId(id: string) {
      this.conversationId = id
    },

    /**
     * 从历史记录加载消息
     * @param conversations 历史对话记录
     * @param isInitialLoad 是否是初始加载（第一次进入页面）
     */
    loadHistoryMessages(conversations: AiConversation[], isInitialLoad = false) {
      const historyMessages: MessageItem[] = conversations.map(conv => {
        // 解析推荐房源
        let recommendations: RoomRecommendation[] | undefined = undefined
        if (conv.recommendedRooms) {
          try {
            const parsed = JSON.parse(conv.recommendedRooms)
            // 检查解析后的数据是否是有效的房源推荐数组
            if (Array.isArray(parsed) && parsed.length > 0) {
              // 检查第一个元素是否是对象（而不是数字）
              if (typeof parsed[0] === 'object' && parsed[0] !== null && 'roomId' in parsed[0]) {
                recommendations = parsed
              } else {
                // 如果是数字数组或其他格式，忽略
                console.warn('🔍 recommendedRooms 格式不正确，已忽略:', parsed)
                recommendations = undefined
              }
            }
          } catch (error) {
            console.error('🔍 解析 recommendedRooms 失败:', error)
            recommendations = undefined
          }
        }
        
        return {
          id: `history-${conv.id}`,
          type: conv.messageType,
          content: conv.message,
          timestamp: conv.createTime,
          recommendations
        }
      })
      
      // 反转历史消息顺序（后端返回的是倒序，需要反转成正序）
      const reversedMessages = historyMessages.reverse()
      
      if (isInitialLoad) {
        // 初始加载：将消息添加到末尾（最新消息在底部）
        this.messages = [...this.messages, ...reversedMessages]
      } else {
        // 下拉加载历史记录：将消息添加到开头（旧消息在上）
        this.messages = [...reversedMessages, ...this.messages]
      }
    },

    /**
     * 设置是否有更多历史记录
     */
    setHasMore(hasMore: boolean) {
      this.hasMore = hasMore
    },

    /**
     * 设置当前页码
     */
    setCurrentPage(page: number) {
      this.currentPage = page
    },

    /**
     * 清空所有消息
     */
    clearMessages() {
      this.messages = []
      this.currentRecommendations = []
      this.conversationId = null
      this.hasMore = true
      this.currentPage = 0  // 重置为0，下次加载会从第1页开始
      this.loading = false
    },

    /**
     * 添加欢迎消息
     */
    addWelcomeMessage() {
      const welcomeMessage: MessageItem = {
        id: 'welcome',
        type: 'AI',
        content: '您好!我是您的专属租房AI助手,可以帮您找房、答疑解惑。请告诉我您的租房需求吧!',
        timestamp: new Date().toISOString()
      }
      this.messages.push(welcomeMessage)
    }
  },

  persist: {
    key: 'ai-chat',
    storage: localStorage,
    paths: ['messages', 'conversationId']
  }
})
