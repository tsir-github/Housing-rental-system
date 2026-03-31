<template>
  <el-dialog
    v-model="visible"
    title="智能审核结果"
    width="700px"
    :before-close="handleClose"
  >
    <div v-if="result" class="comprehensive-review">
      <!-- 审核结果概览 -->
      <el-card class="result-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>智能审核结果</span>
            <el-tag 
              :type="getRecommendationTagType(result.recommendation)"
              size="large"
            >
              {{ getRecommendationText(result.recommendation) }}
            </el-tag>
          </div>
        </template>
        
        <div class="result-overview">
          <div class="overall-score">
            <div class="score-label">综合评分</div>
            <el-progress 
              :percentage="result.overallScore" 
              :color="getScoreColor(result.overallScore)"
              :stroke-width="16"
              text-inside
            />
            <div class="score-text">{{ result.overallScore }}分</div>
          </div>
          
          <div class="recommendation-desc">
            <div class="desc-title">建议说明：</div>
            <div class="desc-content">
              {{ getRecommendationDescription(result.recommendation) }}
            </div>
          </div>
        </div>
      </el-card>

      <!-- 详细评分 -->
      <el-card class="result-card" shadow="never">
        <template #header>
          <span>详细评分</span>
        </template>
        
        <div class="detailed-scores">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="score-item">
                <div class="score-header">
                  <el-icon><House /></el-icon>
                  <span>合规性评分</span>
                </div>
                <el-progress 
                  :percentage="result.scores.complianceScore" 
                  :color="getScoreColor(result.scores.complianceScore)"
                  :stroke-width="8"
                />
                <div class="score-value">{{ result.scores.complianceScore }}分</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="score-item">
                <div class="score-header">
                  <el-icon><Camera /></el-icon>
                  <span>照片质量</span>
                </div>
                <el-progress 
                  :percentage="result.scores.photoQualityScore" 
                  :color="getScoreColor(result.scores.photoQualityScore)"
                  :stroke-width="8"
                />
                <div class="score-value">{{ result.scores.photoQualityScore }}分</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="score-item">
                <div class="score-header">
                  <el-icon><Document /></el-icon>
                  <span>描述准确性</span>
                </div>
                <el-progress 
                  :percentage="result.scores.descriptionAccuracyScore" 
                  :color="getScoreColor(result.scores.descriptionAccuracyScore)"
                  :stroke-width="8"
                />
                <div class="score-value">{{ result.scores.descriptionAccuracyScore }}分</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="score-item">
                <div class="score-header">
                  <el-icon><Tools /></el-icon>
                  <span>设施完整性</span>
                </div>
                <el-progress 
                  :percentage="result.scores.facilityCompletenessScore" 
                  :color="getScoreColor(result.scores.facilityCompletenessScore)"
                  :stroke-width="8"
                />
                <div class="score-value">{{ result.scores.facilityCompletenessScore }}分</div>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="score-item">
                <div class="score-header">
                  <el-icon><Location /></el-icon>
                  <span>位置准确性</span>
                </div>
                <el-progress 
                  :percentage="result.scores.locationAccuracyScore" 
                  :color="getScoreColor(result.scores.locationAccuracyScore)"
                  :stroke-width="8"
                />
                <div class="score-value">{{ result.scores.locationAccuracyScore }}分</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 审核原因 -->
      <el-card class="result-card" shadow="never">
        <template #header>
          <span>审核原因</span>
        </template>
        
        <div class="reasons-list">
          <div 
            v-for="(reason, index) in result.reasons" 
            :key="index"
            class="reason-item"
          >
            <el-icon class="reason-icon"><InfoFilled /></el-icon>
            <span>{{ reason }}</span>
          </div>
        </div>
      </el-card>

      <!-- 操作建议 -->
      <el-card class="result-card" shadow="never">
        <template #header>
          <span>操作建议</span>
        </template>
        
        <div class="suggestions">
          <el-alert
            :title="getSuggestionTitle(result.recommendation)"
            :type="getSuggestionType(result.recommendation)"
            :description="getSuggestionDescription(result.recommendation)"
            show-icon
            :closable="false"
          />
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button 
          v-if="result && result.recommendation === 'APPROVE'"
          type="success" 
          @click="handleConfirm('approve')"
        >
          确认通过
        </el-button>
        <el-button 
          v-if="result && result.recommendation === 'REJECT'"
          type="danger" 
          @click="handleConfirm('reject')"
        >
          确认拒绝
        </el-button>
        <el-button 
          v-if="result && result.recommendation === 'MANUAL_REVIEW'"
          type="primary" 
          @click="handleConfirm('manual')"
        >
          转人工审核
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { House, Camera, Document, Tools, Location, InfoFilled } from '@element-plus/icons-vue'
import type { PropertyReviewResult } from '@/api/roomReview/types'

// Props
const props = defineProps<{
  visible: boolean
  result: PropertyReviewResult | null
}>()

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'confirm': [action: 'approve' | 'reject' | 'manual']
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

// 确认操作
const handleConfirm = (action: 'approve' | 'reject' | 'manual') => {
  emit('confirm', action)
  handleClose()
}

// 获取建议标签类型
const getRecommendationTagType = (recommendation: string) => {
  switch (recommendation) {
    case 'APPROVE':
      return 'success'
    case 'REJECT':
      return 'danger'
    case 'MANUAL_REVIEW':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取建议文本
const getRecommendationText = (recommendation: string) => {
  switch (recommendation) {
    case 'APPROVE':
      return '建议通过'
    case 'REJECT':
      return '建议拒绝'
    case 'MANUAL_REVIEW':
      return '需人工审核'
    default:
      return '未知'
  }
}

// 获取建议描述
const getRecommendationDescription = (recommendation: string) => {
  switch (recommendation) {
    case 'APPROVE':
      return '房源信息完整准确，各项评分均达到标准，建议直接通过审核。'
    case 'REJECT':
      return '房源信息存在明显问题，多项评分不达标，建议拒绝审核。'
    case 'MANUAL_REVIEW':
      return '房源信息存在一些问题，需要人工进一步审核确认。'
    default:
      return '智能审核结果未知，请人工处理。'
  }
}

// 获取建议标题
const getSuggestionTitle = (recommendation: string) => {
  switch (recommendation) {
    case 'APPROVE':
      return '建议通过审核'
    case 'REJECT':
      return '建议拒绝审核'
    case 'MANUAL_REVIEW':
      return '建议人工审核'
    default:
      return '需要处理'
  }
}

// 获取建议类型
const getSuggestionType = (recommendation: string) => {
  switch (recommendation) {
    case 'APPROVE':
      return 'success'
    case 'REJECT':
      return 'error'
    case 'MANUAL_REVIEW':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取建议详细描述
const getSuggestionDescription = (recommendation: string) => {
  switch (recommendation) {
    case 'APPROVE':
      return '系统分析认为该房源符合平台标准，可以直接通过审核并上架。'
    case 'REJECT':
      return '系统分析发现该房源存在多项不符合标准的问题，建议拒绝审核并要求房东整改。'
    case 'MANUAL_REVIEW':
      return '系统分析发现该房源存在一些需要人工判断的问题，建议转交给审核员进行详细审核。'
    default:
      return '请根据具体情况进行处理。'
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
.comprehensive-review {
  .result-card {
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
  
  .result-overview {
    .overall-score {
      text-align: center;
      margin-bottom: 24px;
      
      .score-label {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 12px;
      }
      
      .score-text {
        font-size: 20px;
        font-weight: 600;
        color: #409eff;
        margin-top: 12px;
      }
    }
    
    .recommendation-desc {
      .desc-title {
        font-weight: 600;
        color: #303133;
        margin-bottom: 8px;
      }
      
      .desc-content {
        color: #606266;
        line-height: 1.6;
        background-color: #f5f7fa;
        padding: 12px;
        border-radius: 4px;
        border-left: 3px solid #409eff;
      }
    }
  }
  
  .detailed-scores {
    .score-item {
      margin-bottom: 20px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .score-header {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        color: #606266;
        font-weight: 500;
        
        .el-icon {
          margin-right: 8px;
          color: #409eff;
        }
      }
      
      .score-value {
        text-align: center;
        font-weight: 600;
        color: #303133;
        margin-top: 8px;
      }
    }
  }
  
  .reasons-list {
    .reason-item {
      display: flex;
      align-items: flex-start;
      margin-bottom: 12px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .reason-icon {
        color: #409eff;
        margin-right: 8px;
        margin-top: 2px;
        flex-shrink: 0;
      }
      
      span {
        color: #606266;
        line-height: 1.5;
      }
    }
  }
  
  .suggestions {
    :deep(.el-alert) {
      .el-alert__description {
        line-height: 1.6;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>