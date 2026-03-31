<template>
  <el-dialog
    v-model="visible"
    title="审核详情"
    width="900px"
    :before-close="handleClose"
  >
    <div v-if="review" class="review-detail">
      <!-- 基本信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>审核基本信息</span>
            <el-tag 
              :type="getStatusTagType(review.status)"
              size="large"
            >
              {{ getStatusText(review.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>审核ID：</label>
              <span>{{ review.id || '无' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>房间ID：</label>
              <span>{{ review.roomId }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>房东ID：</label>
              <span>{{ review.landlordId || '无' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>审核员：</label>
              <span>{{ review.reviewerName || '未分配' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>创建时间：</label>
              <span>{{ formatDateTime(review.createTime) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>审核时间：</label>
              <span>{{ formatDateTime(review.reviewedAt) || '未审核' }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 公寓信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span>公寓信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <label>公寓名称：</label>
              <span>{{ review.apartmentInfo?.name || '未知' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>联系电话：</label>
              <span>{{ review.apartmentInfo?.phone || '未知' }}</span>
            </div>
          </el-col>
          <el-col :span="24">
            <div class="info-item">
              <label>公寓地址：</label>
              <span>{{ getFullAddress(review.apartmentInfo) }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>经纬度：</label>
              <span>{{ getCoordinates(review.apartmentInfo) }}</span>
            </div>
          </el-col>
          <el-col v-if="review.apartmentInfo?.introduction" :span="24">
            <div class="info-item">
              <label>公寓介绍：</label>
              <div class="introduction-content">{{ review.apartmentInfo.introduction }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 房间信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span>房间信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>房间号：</label>
              <span>{{ review.roomInfo?.roomNumber || '未知' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>租金：</label>
              <span class="rent-price">{{ formatRent(review.roomInfo?.rent) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>发布类型：</label>
              <span>{{ getPublisherTypeText(review.roomInfo?.publisherType) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>发布状态：</label>
              <el-tag :type="review.roomInfo?.isRelease === 1 ? 'success' : 'warning'" size="small">
                {{ review.roomInfo?.isRelease === 1 ? '已发布' : '未发布' }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>审核状态：</label>
              <el-tag :type="getReviewStatusTagType(review.roomInfo?.reviewStatus)" size="small">
                {{ getReviewStatusText(review.roomInfo?.reviewStatus) }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>合规评分：</label>
              <span>{{ review.roomInfo?.complianceScore || '-' }}分</span>
            </div>
          </el-col>
          <el-col v-if="review.landlordName && review.roomInfo?.publisherType === 2" :span="12">
            <div class="info-item">
              <label>房东姓名：</label>
              <span>{{ review.landlordName }}（房东房源发布到官方公寓）</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 评分信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span>审核评分详情</span>
        </template>
        
        <div class="score-section">
          <div class="overall-score">
            <div class="score-label">综合评分</div>
            <div v-if="review.overallRating !== null && review.overallRating !== undefined">
              <div class="rating-badge" :class="getRatingClass(review.overallRating)">
                {{ getRatingText(review.overallRating) }}
              </div>
              <div class="score-value">等级：{{ review.overallRating }}</div>
            </div>
            <div v-else class="score-value text-gray">待评分</div>
          </div>
          
          <el-row :gutter="20" class="detailed-scores">
            <el-col :span="12">
              <div class="score-item">
                <label>合规性评分：</label>
                <el-progress 
                  :percentage="review.complianceScore || 0" 
                  :stroke-width="6"
                  :show-text="false"
                />
                <span class="score">{{ review.complianceScore || '-' }}分</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="score-item">
                <label>照片质量：</label>
                <el-progress 
                  :percentage="review.photoQualityScore || 0" 
                  :stroke-width="6"
                  :show-text="false"
                />
                <span class="score">{{ review.photoQualityScore || '-' }}分</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="score-item">
                <label>描述准确性：</label>
                <el-progress 
                  :percentage="review.descriptionAccuracyScore || 0" 
                  :stroke-width="6"
                  :show-text="false"
                />
                <span class="score">{{ review.descriptionAccuracyScore || '-' }}分</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="score-item">
                <label>设施完整性：</label>
                <el-progress 
                  :percentage="review.facilityCompletenessScore || 0" 
                  :stroke-width="6"
                  :show-text="false"
                />
                <span class="score">{{ review.facilityCompletenessScore || '-' }}分</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="score-item">
                <label>位置准确性：</label>
                <el-progress 
                  :percentage="review.locationAccuracyScore || 0" 
                  :stroke-width="6"
                  :show-text="false"
                />
                <span class="score">{{ review.locationAccuracyScore || '-' }}分</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 审核意见 -->
      <el-card v-if="review.reviewComments || review.rejectionReasons" class="info-card" shadow="never">
        <template #header>
          <span>审核意见</span>
        </template>
        
        <div v-if="review.reviewComments" class="review-notes">
          <label>审核备注：</label>
          <div class="notes-content">{{ review.reviewComments }}</div>
        </div>
        
        <div v-if="review.rejectionReasons" class="rejection-reason">
          <label>拒绝原因：</label>
          <div class="reason-content">{{ review.rejectionReasons }}</div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RoomReviewVo } from '@/api/roomReview/types'
import { ReviewStatus } from '@/enums/constEnums'

// Props
const props = defineProps<{
  visible: boolean
  review: RoomReviewVo | null
}>()

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 获取房东显示名称
const getLandlordDisplayName = (review: any) => {
  // 如果是官方公寓且房东发布，显示发布房东信息
  if (review.roomInfo?.publisherType === 2 && review.landlordName) {
    return `${review.landlordName}（房东发布）`
  }
  
  // 如果是官方发布，显示官方
  if (review.roomInfo?.publisherType === 1) {
    return '官方发布'
  }
  
  // 如果有房东信息，显示房东姓名
  if (review.landlordName) {
    return review.landlordName
  }
  
  // 默认显示官方房源
  return '官方房源'
}

// 获取完整地址
const getFullAddress = (apartmentInfo: any) => {
  if (!apartmentInfo) return '未知地址'
  
  const parts = []
  if (apartmentInfo.provinceName) parts.push(apartmentInfo.provinceName)
  if (apartmentInfo.cityName) parts.push(apartmentInfo.cityName)
  if (apartmentInfo.districtName) parts.push(apartmentInfo.districtName)
  if (apartmentInfo.addressDetail) parts.push(apartmentInfo.addressDetail)
  
  return parts.length > 0 ? parts.join(' ') : '未知地址'
}

// 获取经纬度信息
const getCoordinates = (apartmentInfo: any) => {
  if (!apartmentInfo || !apartmentInfo.latitude || !apartmentInfo.longitude) {
    return '未知'
  }
  return `${apartmentInfo.latitude}, ${apartmentInfo.longitude}`
}

// 格式化租金
const formatRent = (rent: number | undefined) => {
  if (!rent) return '未知'
  return `¥${rent.toLocaleString()}/月`
}

// 获取发布类型文本
const getPublisherTypeText = (publisherType: number | undefined) => {
  switch (publisherType) {
    case 1:
      return '官方发布'
    case 2:
      return '房东发布'
    default:
      return '未知'
  }
}

// 获取审核状态标签类型
const getReviewStatusTagType = (reviewStatus: number | undefined) => {
  switch (reviewStatus) {
    case 1:
      return 'warning' // 待审核
    case 2:
      return 'success' // 审核通过
    case 3:
      return 'danger'  // 审核拒绝
    default:
      return 'info'
  }
}

// 获取审核状态文本
const getReviewStatusText = (reviewStatus: number | undefined) => {
  switch (reviewStatus) {
    case 1:
      return '待审核'
    case 2:
      return '审核通过'
    case 3:
      return '审核拒绝'
    default:
      return '未知'
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string | undefined) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取状态标签类型
const getStatusTagType = (status: ReviewStatus) => {
  switch (status) {
    case ReviewStatus.PENDING:
      return 'warning'
    case ReviewStatus.APPROVED:
      return 'success'
    case ReviewStatus.REJECTED:
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: ReviewStatus) => {
  switch (status) {
    case ReviewStatus.PENDING:
      return '待审核'
    case ReviewStatus.APPROVED:
      return '审核通过'
    case ReviewStatus.REJECTED:
      return '审核拒绝'
    default:
      return '未知'
  }
}

// 获取评级文本
const getRatingText = (rating: number) => {
  switch (rating) {
    case 1:
      return '优秀'
    case 2:
      return '良好'
    case 3:
      return '一般'
    case 4:
      return '较差'
    default:
      return '未知'
  }
}

// 获取评级样式类
const getRatingClass = (rating: number) => {
  switch (rating) {
    case 1:
      return 'rating-excellent'  // 优秀 - 绿色
    case 2:
      return 'rating-good'       // 良好 - 蓝色
    case 3:
      return 'rating-average'    // 一般 - 橙色
    case 4:
      return 'rating-poor'       // 较差 - 红色
    default:
      return 'rating-unknown'
  }
}

// 获取评分颜色
const getScoreColor = (score: number) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped lang="scss">
.review-detail {
  max-height: 600px;
  overflow-y: auto;
}

.info-card {
  margin-bottom: 16px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.info-item {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
  
  label {
    min-width: 100px;
    color: #606266;
    font-weight: 500;
    margin-right: 8px;
    flex-shrink: 0;
  }
  
  span {
    color: #303133;
    flex: 1;
  }
  
  .rent-price {
    color: #e6a23c;
    font-weight: 600;
    font-size: 16px;
  }
}

.introduction-content {
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border-left: 3px solid #409eff;
  line-height: 1.6;
  color: #303133;
  margin-top: 4px;
  font-size: 14px;
}

.text-gray {
  color: #909399;
}

.score-section {
  .overall-score {
    text-align: center;
    margin-bottom: 24px;
    
    .score-label {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 12px;
    }
    
    .rating-badge {
      display: inline-block;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 16px;
      font-weight: 600;
      margin: 8px 0;
      
      &.rating-excellent {
        background-color: #f0f9ff;
        color: #059669;
        border: 1px solid #10b981;
      }
      
      &.rating-good {
        background-color: #eff6ff;
        color: #2563eb;
        border: 1px solid #3b82f6;
      }
      
      &.rating-average {
        background-color: #fffbeb;
        color: #d97706;
        border: 1px solid #f59e0b;
      }
      
      &.rating-poor {
        background-color: #fef2f2;
        color: #dc2626;
        border: 1px solid #ef4444;
      }
      
      &.rating-unknown {
        background-color: #f3f4f6;
        color: #6b7280;
        border: 1px solid #9ca3af;
      }
    }
    
    .score-value {
      font-size: 14px;
      font-weight: 500;
      color: #6b7280;
      margin-top: 4px;
    }
  }
  
  .detailed-scores {
    .score-item {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      
      label {
        min-width: 100px;
        color: #606266;
        font-weight: 500;
        margin-right: 12px;
      }
      
      :deep(.el-progress) {
        flex: 1;
        margin-right: 12px;
      }
      
      .score {
        min-width: 50px;
        color: #303133;
        font-weight: 500;
      }
    }
  }
}

.review-notes,
.rejection-reason {
  margin-bottom: 16px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  label {
    display: block;
    color: #606266;
    font-weight: 500;
    margin-bottom: 8px;
  }
  
  .notes-content,
  .reason-content {
    background-color: #f5f7fa;
    padding: 12px;
    border-radius: 4px;
    border-left: 3px solid #409eff;
    line-height: 1.5;
    color: #303133;
  }
  
  .reason-content {
    border-left-color: #f56c6c;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>