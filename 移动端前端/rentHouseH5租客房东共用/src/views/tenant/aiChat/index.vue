<template>
  <div class="ai-chat-container">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="AI助手"
      left-arrow
      @click-left="onClickLeft"
      fixed
      placeholder
    >
      <template #right>
        <van-icon name="delete-o" @click="showClearDialog" />
      </template>
    </van-nav-bar>

    <!-- 消息列表 -->
    <div class="message-list" ref="messageListRef" @scroll="onScroll">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh" pull-distance="80">
        <template #pulling="{ distance }">
          <div class="pull-hint">下拉加载历史记录 {{ distance }}px</div>
        </template>
        <template #loosing>
          <div class="pull-hint">释放加载历史记录</div>
        </template>
        <template #loading>
          <div class="pull-hint">
            <van-loading type="spinner" size="20" />
            <span>加载中...</span>
          </div>
        </template>
        
        <!-- 没有更多历史记录提示 -->
        <div v-if="!aiChatStore.hasMoreHistory && messages.length > 0" class="no-more-hint">
          没有更多历史记录了
        </div>
        
        <!-- 消息项 -->
        <div
          v-for="message in messages"
          :key="message.id"
          :class="['message-item', message.type.toLowerCase()]"
        >
          <!-- 用户消息 -->
          <div v-if="message.type === 'USER'" class="user-message">
            <div class="message-content">
              {{ message.content }}
            </div>
            <div class="message-avatar">
              <van-icon name="user-o" />
            </div>
          </div>

          <!-- AI消息 -->
          <div v-else class="ai-message">
            <div class="message-avatar">
              <van-icon name="service-o" />
            </div>
            <div class="message-content-wrapper">
              <!-- 加载中 -->
              <div v-if="message.loading" class="message-loading">
                <van-loading type="spinner" size="20" />
                <span>AI正在思考中...</span>
              </div>
              
              <!-- AI回复内容 -->
              <div v-else class="message-content">
                {{ message.content }}
              </div>

              <!-- 房源推荐卡片 -->
              <div
                v-if="message.recommendations && message.recommendations.length > 0"
                class="recommendations"
              >
                <div class="recommendations-title">
                  <van-icon name="home-o" />
                  <span>为您推荐以下房源</span>
                </div>
                <div class="recommendation-list">
                  <RoomRecommendationCard
                    v-for="room in message.recommendations"
                    :key="room.roomId"
                    :room="room"
                    @click="goToRoomDetail"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 时间戳 -->
          <div class="message-time">
            {{ formatTime(message.timestamp) }}
          </div>
        </div>
      </van-pull-refresh>
    </div>

    <!-- 输入框 -->
    <div class="input-bar">
      <van-field
        v-model="inputMessage"
        placeholder="请输入您的租房需求..."
        :disabled="sending"
        @keyup.enter="sendMessage"
      >
        <template #button>
          <van-button
            size="small"
            type="primary"
            :loading="sending"
            @click="sendMessage"
          >
            发送
          </van-button>
        </template>
      </van-field>
    </div>

    <!-- 清空对话确认弹窗 -->
    <van-dialog
      v-model:show="showClear"
      title="清空对话"
      message="确定要清空所有对话记录吗？此操作不可恢复。"
      show-cancel-button
      @confirm="clearHistory"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import { useAiChatStore } from '@/store/modules/aiChat'
import { RoomRecommendationCard } from '@/components/tenant'
import {
  sendMessage as sendMessageApi,
  getConversationHistory,
  clearConversationHistory
} from '@/api/tenant/ai'

const router = useRouter()
const aiChatStore = useAiChatStore()

// 响应式数据
const inputMessage = ref('')
const sending = ref(false)
const refreshing = ref(false)
const showClear = ref(false)
const messageListRef = ref<HTMLElement>()

// 计算属性
const messages = computed(() => aiChatStore.allMessages)

// 生命周期
onMounted(async () => {
  console.log('🔍 AI聊天页面已挂载')
  
  // 重置状态
  refreshing.value = false
  
  // 清空store中的消息
  aiChatStore.clearMessages()
  
  // 立即加载第一页（最新的消息）
  try {
    console.log('🔍 加载最新的聊天记录')
    const response = await getConversationHistory(1, 20)
    const pageData = response.data || {}
    const records = pageData.records || []
    
    if (records.length > 0) {
      // 加载最新的消息（isInitialLoad = true，消息会添加到末尾）
      aiChatStore.loadHistoryMessages(records, true)
      aiChatStore.setCurrentPage(1)
      aiChatStore.setHasMore(records.length === 20)
      console.log('🔍 已加载最新的', records.length, '条消息')
      
      // 不滚动，直接显示最新消息（因为消息已经在底部了）
      // 使用 nextTick 确保 DOM 已更新，然后立即滚动到底部（不使用动画）
      await nextTick()
      if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight
      }
    } else {
      // 如果没有历史记录，显示欢迎消息
      aiChatStore.addWelcomeMessage()
      aiChatStore.setHasMore(false)
    }
  } catch (error: any) {
    console.error('🔍 加载最新消息失败:', error)
    // 加载失败时显示欢迎消息
    aiChatStore.addWelcomeMessage()
    aiChatStore.setHasMore(false)
  }
  
  console.log('🔍 页面初始化完成')
})

/**
 * 发送消息
 */
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message) {
    showToast('请输入消息')
    return
  }

  if (message.length > 1000) {
    showToast('消息长度不能超过1000字符')
    return
  }

  // 添加用户消息
  aiChatStore.addUserMessage(message)
  inputMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  await nextTick()
  scrollToBottom()

  // 添加加载中的AI消息
  const loadingId = aiChatStore.addLoadingMessage()
  sending.value = true

  try {
    // 调用API
    const response = await sendMessageApi({ message })
    
    // 移除加载消息,添加AI回复
    aiChatStore.removeLoadingMessage(loadingId)
    aiChatStore.addAiMessage(response.data.aiReply, response.data.recommendations)
    aiChatStore.setConversationId(response.data.conversationId)

    // 滚动到底部
    await nextTick()
    await nextTick()
    scrollToBottom()

  } catch (error: any) {
    console.error('发送消息失败:', error)
    aiChatStore.removeLoadingMessage(loadingId)
    showFailToast(error.message || '发送失败,请稍后重试')
  } finally {
    sending.value = false
  }
}

/**
 * 下拉刷新 - 加载历史记录
 */
const onRefresh = async () => {
  // 如果已经没有更多历史记录，直接返回
  if (!aiChatStore.hasMoreHistory) {
    refreshing.value = false
    showToast('没有更多历史记录了')
    return
  }

  try {
    console.log('🔍 开始加载历史记录')
    
    // 记录当前滚动位置和第一条消息的ID
    const scrollElement = messageListRef.value as HTMLElement
    const oldScrollHeight = scrollElement.scrollHeight
    const oldScrollTop = scrollElement.scrollTop
    const firstMessageId = messages.value[0]?.id
    
    const nextPage = aiChatStore.currentPage + 1
    console.log('🔍 加载第', nextPage, '页历史记录')
    
    const response = await getConversationHistory(nextPage, 20)
    console.log('🔍 获取历史记录成功:', response)
    
    const pageData = response.data || {}
    const records = pageData.records || []
    console.log('🔍 解析后的记录数:', records.length)
    
    if (records.length > 0) {
      // 加载历史记录（isInitialLoad = false，消息会添加到开头）
      aiChatStore.loadHistoryMessages(records, false)
      aiChatStore.setCurrentPage(nextPage)
      console.log('🔍 已加载', records.length, '条记录')
      
      // 恢复滚动位置（保持在原来的消息位置）
      await nextTick()
      const newScrollHeight = scrollElement.scrollHeight
      const scrollDiff = newScrollHeight - oldScrollHeight
      scrollElement.scrollTop = oldScrollTop + scrollDiff
      console.log('🔍 已恢复滚动位置，scrollDiff:', scrollDiff)
    }
    
    // 判断是否还有更多
    if (records.length < 20) {
      aiChatStore.setHasMore(false)
      console.log('🔍 没有更多历史记录了')
    } else {
      aiChatStore.setHasMore(true)
    }
  } catch (error: any) {
    console.error('🔍 加载历史记录失败:', error)
    showFailToast('加载失败: ' + (error.message || '未知错误'))
  } finally {
    refreshing.value = false
  }
}

/**
 * 滚动事件 - 用于检测是否需要加载历史记录
 */
const onScroll = (event: Event) => {
  const element = event.target as HTMLElement
  // 当滚动到顶部附近时，可以提示用户下拉加载历史记录
  if (element.scrollTop < 50 && aiChatStore.hasMoreHistory && !refreshing.value) {
    console.log('🔍 接近顶部，可以下拉加载历史记录')
  }
}

/**
 * 显示清空对话确认弹窗
 */
const showClearDialog = () => {
  showClear.value = true
}

/**
 * 清空对话历史
 */
const clearHistory = async () => {
  try {
    await clearConversationHistory()
    aiChatStore.clearMessages()
    aiChatStore.addWelcomeMessage()
    showToast('对话记录已清空')
  } catch (error: any) {
    console.error('清空失败:', error)
    showFailToast('清空失败')
  }
}

/**
 * 跳转到房源详情
 */
const goToRoomDetail = (roomId: number) => {
  router.push(`/tenant/roomDetail/${roomId}`)
}

/**
 * 返回上一页
 */
const onClickLeft = () => {
  router.back()
}

/**
 * 滚动到底部
 */
const scrollToBottom = () => {
  if (messageListRef.value) {
    // 使用 nextTick 确保 DOM 已更新
    nextTick(() => {
      const element = messageListRef.value as HTMLElement
      // 使用 scrollTo 方法，更平滑且可靠
      element.scrollTo({
        top: element.scrollHeight,
        behavior: 'smooth'
      })
      console.log('🔍 已滚动到底部，scrollHeight:', element.scrollHeight, 'scrollTop:', element.scrollTop)
    })
  }
}

/**
 * 格式化时间
 */
const formatTime = (timestamp: string) => {
  if (!timestamp) {
    return ''
  }
  
  // 尝试解析时间戳
  let date: Date
  
  // 如果是数字字符串或纯数字，当作毫秒时间戳处理
  if (/^\d+$/.test(timestamp)) {
    date = new Date(parseInt(timestamp))
  } else {
    // 否则当作ISO字符串处理
    date = new Date(timestamp)
  }
  
  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    console.warn('🔍 无效的时间戳:', timestamp)
    return ''
  }
  
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 1分钟内
  if (diff < 60000 && diff >= 0) {
    return '刚刚'
  }
  
  // 1小时内
  if (diff < 3600000 && diff >= 0) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  
  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  
  // 其他
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped lang="less">
.ai-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f0f9ff 0%, #f5f5f5 100%); /* 渐变背景 */
  position: relative;
}

/* 下拉加载提示 */
.pull-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #666;
  font-size: 14px;
}

/* 没有更多历史记录提示 */
.no-more-hint {
  text-align: center;
  padding: 16px;
  color: #999;
  font-size: 13px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  padding-bottom: 120px;
  -webkit-overflow-scrolling: touch;
  width: 100%;
  box-sizing: border-box;
}

.message-item {
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-in; /* 添加淡入动画 */
  width: 100%;
  box-sizing: border-box;

  &.user {
    display: flex;
    justify-content: flex-end;
  }

  &.ai {
    display: flex;
    justify-content: flex-start;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  justify-content: flex-end;
  width: 100%;

  .message-content {
    max-width: calc(100% - 52px);
    min-width: 0;
    padding: 12px 16px;
    background: linear-gradient(135deg, #07c160 0%, #06ad56 100%); /* 渐变绿色 */
    color: #fff;
    border-radius: 16px 16px 4px 16px; /* 圆角优化 */
    word-wrap: break-word;
    word-break: break-word;
    overflow-wrap: break-word;
    white-space: normal;
    line-height: 1.5;
    box-shadow: 0 2px 8px rgba(7, 193, 96, 0.2); /* 添加阴影 */
  }

  .message-avatar {
    width: 40px;
    height: 40px;
    min-width: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #fff;
    box-shadow: 0 2px 8px rgba(7, 193, 96, 0.3);
  }
}

.ai-message {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;

  .message-avatar {
    width: 40px;
    height: 40px;
    min-width: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #1989fa 0%, #0d6efd 100%); /* 渐变蓝色 */
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #fff;
    box-shadow: 0 2px 8px rgba(25, 137, 250, 0.3);
  }

  .message-content-wrapper {
    flex: 1;
    min-width: 0;
    max-width: calc(100% - 48px);
  }

  .message-loading {
    padding: 12px 16px;
    background-color: #fff;
    border-radius: 16px 16px 16px 4px;
    display: flex;
    align-items: center;
    gap: 8px;
    color: #999;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .message-content {
    padding: 12px 16px;
    background-color: #fff;
    border-radius: 16px 16px 16px 4px; /* 圆角优化 */
    word-wrap: break-word;
    word-break: break-word;
    overflow-wrap: break-word;
    white-space: normal;
    line-height: 1.5;
    color: #333;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); /* 添加阴影 */
    min-width: 0;
  }
}

.recommendations {
  margin-top: 12px;

  .recommendations-title {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    background: linear-gradient(135deg, #e3f2fd 0%, #f0f9ff 100%);
    border-radius: 8px;
    color: #1989fa;
    font-size: 14px;
    font-weight: 500;
  }

  .recommendation-list {
    margin-top: 8px;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
}

.message-time {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.input-bar {
  position: fixed;
  bottom: 50px;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 1) 100%); /* 半透明渐变背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  border-top: 1px solid rgba(25, 137, 250, 0.1);
  padding: 12px 16px; /* 增加内边距 */
  z-index: 999;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.08); /* 增强阴影 */
}

/* 自定义输入框样式 */
.input-bar :deep(.van-field) {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 24px; /* 圆角输入框 */
  border: 2px solid #e3f2fd;
  padding: 4px 12px;
  transition: all 0.3s ease;
  
  &:focus-within {
    border-color: #1989fa; /* 聚焦时边框颜色 */
    box-shadow: 0 0 0 3px rgba(25, 137, 250, 0.1); /* 聚焦时外发光 */
  }
}

.input-bar :deep(.van-field__control) {
  min-height: 40px;
  font-size: 15px;
  color: #333;
  
  &::placeholder {
    color: #999;
  }
}

/* 发送按钮样式优化 */
.input-bar :deep(.van-button--primary) {
  background: linear-gradient(135deg, #1989fa 0%, #0d6efd 100%);
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(25, 137, 250, 0.3);
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
    box-shadow: 0 1px 4px rgba(25, 137, 250, 0.4);
  }
}

/* 禁用状态 */
.input-bar :deep(.van-button--disabled) {
  background: #e0e0e0;
  box-shadow: none;
}
</style>
