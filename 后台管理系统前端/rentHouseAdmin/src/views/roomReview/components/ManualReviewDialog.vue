<template>
  <el-dialog
    v-model="visible"
    title="房源审核评分"
    width="700px"
    :before-close="handleClose"
  >
    <div v-if="review" class="smart-review">
      <!-- 房源信息概览 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span>房源信息</span>
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
              <label>房间号：</label>
              <span>{{ review.roomInfo?.roomNumber || '未知' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>房东：</label>
              <span>{{ review.landlordName || '官方房源' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>租金：</label>
              <span class="rent">{{ formatRent(review.roomInfo?.rent) }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 评分表单 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="review-form"
      >
        <!-- 各项评分 -->
        <el-card class="score-card" shadow="never">
          <template #header>
            <span>评分详情</span>
          </template>
          
          <el-form-item label="合规性评分" prop="complianceScore">
            <div class="score-input">
              <el-slider 
                v-model="formData.complianceScore" 
                :min="0" 
                :max="10" 
                :step="0.5"
                :marks="{0: '0', 2: '2', 4: '4', 6: '6', 8: '8', 10: '10'}"
                style="flex: 1; margin-right: 12px;"
              />
              <span class="score-unit">{{ formData.complianceScore }}分</span>
            </div>
          </el-form-item>

          <el-form-item label="照片质量" prop="photoQualityScore">
            <div class="score-input">
              <el-slider 
                v-model="formData.photoQualityScore" 
                :min="0" 
                :max="10" 
                :step="0.5"
                :marks="{0: '0', 2: '2', 4: '4', 6: '6', 8: '8', 10: '10'}"
                style="flex: 1; margin-right: 12px;"
              />
              <span class="score-unit">{{ formData.photoQualityScore }}分</span>
            </div>
          </el-form-item>

          <el-form-item label="描述准确性" prop="descriptionAccuracyScore">
            <div class="score-input">
              <el-slider 
                v-model="formData.descriptionAccuracyScore" 
                :min="0" 
                :max="10" 
                :step="0.5"
                :marks="{0: '0', 2: '2', 4: '4', 6: '6', 8: '8', 10: '10'}"
                style="flex: 1; margin-right: 12px;"
              />
              <span class="score-unit">{{ formData.descriptionAccuracyScore }}分</span>
            </div>
          </el-form-item>

          <el-form-item label="设施完整性" prop="facilityCompletenessScore">
            <div class="score-input">
              <el-slider 
                v-model="formData.facilityCompletenessScore" 
                :min="0" 
                :max="10" 
                :step="0.5"
                :marks="{0: '0', 2: '2', 4: '4', 6: '6', 8: '8', 10: '10'}"
                style="flex: 1; margin-right: 12px;"
              />
              <span class="score-unit">{{ formData.facilityCompletenessScore }}分</span>
            </div>
          </el-form-item>

          <el-form-item label="位置准确性" prop="locationAccuracyScore">
            <div class="score-input">
              <el-slider 
                v-model="formData.locationAccuracyScore" 
                :min="0" 
                :max="10" 
                :step="0.5"
                :marks="{0: '0', 2: '2', 4: '4', 6: '6', 8: '8', 10: '10'}"
                style="flex: 1; margin-right: 12px;"
              />
              <span class="score-unit">{{ formData.locationAccuracyScore }}分</span>
            </div>
          </el-form-item>
        </el-card>

        <!-- 综合评分和智能建议 -->
        <el-card class="recommendation-card" shadow="never">
          <template #header>
            <span>综合评分与建议</span>
          </template>
          
          <div class="overall-score-section">
            <div class="score-display">
              <div class="score-label">综合评分</div>
              <div class="score-value">{{ calculatedScore.toFixed(1) }}分</div>
              <el-progress 
                :percentage="(calculatedScore / 10) * 100" 
                :color="getScoreColor(calculatedScore)"
                :stroke-width="8"
                style="margin-top: 8px;"
              />
            </div>
            
            <div class="recommendation" :class="recommendation.type">
              <div class="recommendation-header">
                <el-icon class="recommendation-icon">
                  <component :is="recommendation.icon" />
                </el-icon>
                <span class="recommendation-title">{{ recommendation.title }}</span>
              </div>
              <div class="recommendation-message">{{ recommendation.message }}</div>
            </div>
          </div>
        </el-card>

        <!-- 审核决策 -->
        <el-form-item label="最终决策" prop="status">
          <el-radio-group v-model="formData.status" class="decision-group">
            <el-radio 
              :label="ReviewStatus.APPROVED" 
              :class="{ 'recommended': recommendation.suggestedStatus === ReviewStatus.APPROVED }"
            >
              <el-icon><Check /></el-icon>
              审核通过
            </el-radio>
            <el-radio 
              :label="ReviewStatus.REJECTED"
              :class="{ 'recommended': recommendation.suggestedStatus === ReviewStatus.REJECTED }"
            >
              <el-icon><Close /></el-icon>
              审核拒绝
            </el-radio>
          </el-radio-group>
          
          <!-- 冲突警告 -->
          <div v-if="hasConflict" class="conflict-warning">
            <el-alert
              title="决策与评分不匹配"
              :description="conflictMessage"
              type="warning"
              show-icon
              :closable="false"
            />
          </div>
        </el-form-item>

        <!-- 审核意见 -->
        <el-form-item label="审核意见" prop="reviewComments">
          <el-input
            v-model="formData.reviewComments"
            type="textarea"
            :rows="3"
            :placeholder="hasConflict ? '请说明决策理由（必填）' : '请输入审核意见（可选）'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 拒绝原因 -->
        <el-form-item 
          v-if="formData.status === ReviewStatus.REJECTED" 
          label="拒绝原因" 
          prop="rejectionReasons"
        >
          <el-checkbox-group v-model="formData.rejectionReasons" class="rejection-reasons">
            <el-checkbox label="照片质量不清晰">照片质量不清晰</el-checkbox>
            <el-checkbox label="房间描述不准确">房间描述不准确</el-checkbox>
            <el-checkbox label="设施信息不完整">设施信息不完整</el-checkbox>
            <el-checkbox label="位置信息有误">位置信息有误</el-checkbox>
            <el-checkbox label="价格不合理">价格不合理</el-checkbox>
            <el-checkbox label="其他问题">其他问题</el-checkbox>
          </el-checkbox-group>
          
          <el-input
            v-if="formData.rejectionReasons.includes('其他问题')"
            v-model="formData.customRejectionReason"
            placeholder="请详细说明其他问题"
            style="margin-top: 12px;"
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          :loading="loading"
          @click="handleSubmit"
        >
          提交审核
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Check, Close, SuccessFilled, WarningFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { roomReviewApi } from '@/api/roomReview'
import type { RoomReviewVo, RoomReviewSubmitVo } from '@/api/roomReview/types'
import { ReviewStatus } from '@/enums/constEnums'

// Props
const props = defineProps<{
  visible: boolean
  review: RoomReviewVo | null
  action: 'approve' | 'reject'
}>()

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 响应式数据
const loading = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  status: ReviewStatus.APPROVED,
  complianceScore: 8.0,
  photoQualityScore: 8.0,
  descriptionAccuracyScore: 8.0,
  facilityCompletenessScore: 8.0,
  locationAccuracyScore: 8.0,
  reviewComments: '',
  rejectionReasons: [] as string[],
  customRejectionReason: ''
})

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 实时计算综合评分（加权平均）
const calculatedScore = computed(() => {
  const weights = {
    complianceScore: 0.15,       // 合规性 15%
    photoQualityScore: 0.15,     // 照片质量 15%
    descriptionAccuracyScore: 0.4, // 描述准确性 40%
    facilityCompletenessScore: 0.15, // 设施完整性 15%
    locationAccuracyScore: 0.15  // 位置准确性 15%
  }
  
  const score = (
    formData.complianceScore * weights.complianceScore +
    formData.photoQualityScore * weights.photoQualityScore +
    formData.descriptionAccuracyScore * weights.descriptionAccuracyScore +
    formData.facilityCompletenessScore * weights.facilityCompletenessScore +
    formData.locationAccuracyScore * weights.locationAccuracyScore
  )
  
  // 修复浮点数精度问题，保留1位小数
  return Math.round(score * 10) / 10
})

// 智能建议
const recommendation = computed(() => {
  const score = calculatedScore.value
  
  if (score >= 7.0) {
    return {
      type: 'success',
      icon: SuccessFilled,
      title: '建议通过',
      message: `综合评分 ${score.toFixed(1)} 分，房源质量良好，建议审核通过`,
      suggestedStatus: ReviewStatus.APPROVED
    }
  } else if (score >= 4.0) {
    return {
      type: 'warning',
      icon: WarningFilled,
      title: '建议有条件通过',
      message: `综合评分 ${score.toFixed(1)} 分，房源质量一般，建议要求房东改进后通过`,
      suggestedStatus: ReviewStatus.APPROVED
    }
  } else {
    return {
      type: 'danger',
      icon: CircleCloseFilled,
      title: '建议拒绝',
      message: `综合评分 ${score.toFixed(1)} 分，房源质量不达标，建议审核拒绝`,
      suggestedStatus: ReviewStatus.REJECTED
    }
  }
})

// 检测决策冲突
const hasConflict = computed(() => {
  return formData.status !== recommendation.value.suggestedStatus
})

// 冲突提示信息
const conflictMessage = computed(() => {
  if (!hasConflict.value) return ''
  
  const score = calculatedScore.value.toFixed(1)
  if (formData.status === ReviewStatus.APPROVED && recommendation.value.suggestedStatus === ReviewStatus.REJECTED) {
    return `评分仅 ${score} 分但选择通过，请说明理由`
  } else if (formData.status === ReviewStatus.REJECTED && recommendation.value.suggestedStatus === ReviewStatus.APPROVED) {
    return `评分达 ${score} 分但选择拒绝，请说明理由`
  }
  return '决策与系统建议不符，请说明理由'
})

// 转换为数据库枚举值
const convertToOverallRating = (calculatedScore: number): number => {
  if (calculatedScore >= 8.0) return 1  // 优秀
  if (calculatedScore >= 6.0) return 2  // 良好  
  if (calculatedScore >= 4.0) return 3  // 一般
  return 4  // 较差
}

// 表单验证规则
const formRules: FormRules = {
  status: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  reviewComments: [
    {
      validator: (rule, value, callback) => {
        if (hasConflict.value && (!value || value.trim() === '')) {
          callback(new Error('决策与评分不匹配时必须说明理由'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  rejectionReasons: [
    {
      validator: (rule, value, callback) => {
        if (formData.status === ReviewStatus.REJECTED && (!value || value.length === 0)) {
          callback(new Error('审核拒绝时必须选择拒绝原因'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 监听对话框显示状态
watch(() => props.visible, (visible) => {
  if (visible && props.review) {
    initFormData()
  }
})

// 初始化表单数据
const initFormData = () => {
  if (!props.review) return
  
  // 根据用户点击的按钮设置初始状态
  formData.status = props.action === 'approve' ? ReviewStatus.APPROVED : ReviewStatus.REJECTED
  
  // 设置预设评分（根据按钮意图）
  if (props.action === 'approve') {
    // 点击通过按钮，预设较高分数
    formData.complianceScore = 8.0
    formData.photoQualityScore = 7.5
    formData.descriptionAccuracyScore = 8.5
    formData.facilityCompletenessScore = 7.0
    formData.locationAccuracyScore = 8.0
  } else {
    // 点击拒绝按钮，预设较低分数
    formData.complianceScore = 3.0
    formData.photoQualityScore = 3.5
    formData.descriptionAccuracyScore = 4.0
    formData.facilityCompletenessScore = 3.0
    formData.locationAccuracyScore = 3.5
  }
  
  // 重置其他字段
  formData.reviewComments = ''
  formData.rejectionReasons = []
  formData.customRejectionReason = ''
  
  // 清除验证
  formRef.value?.clearValidate()
}

// 格式化租金
const formatRent = (rent: number | undefined) => {
  if (!rent) return '未知'
  return `¥${rent.toLocaleString()}/月`
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 提交审核
const handleSubmit = async () => {
  if (!props.review) return
  
  try {
    // 表单验证
    await formRef.value?.validate()
    
    // 如果有冲突，再次确认
    if (hasConflict.value) {
      await ElMessageBox.confirm(
        `您的决策与系统建议不符：${conflictMessage.value}。确认要继续提交吗？`,
        '确认决策',
        {
          type: 'warning',
          confirmButtonText: '确认提交',
          cancelButtonText: '重新考虑'
        }
      )
    }
    
    loading.value = true
    
    // 处理拒绝原因
    let rejectionReasons: string[] = []
    if (formData.status === ReviewStatus.REJECTED) {
      rejectionReasons = [...formData.rejectionReasons]
      if (formData.rejectionReasons.includes('其他问题') && formData.customRejectionReason) {
        rejectionReasons = rejectionReasons.filter(r => r !== '其他问题')
        rejectionReasons.push(formData.customRejectionReason)
      }
    }
    
    // 准备请求参数
    const params: RoomReviewSubmitVo = {
      roomId: props.review.roomId,
      apartmentId: props.review.apartmentId || props.review.roomInfo?.apartmentId || 0,
      landlordId: props.review.landlordId,
      reviewerId: 1, // 这里应该从用户信息获取
      complianceScore: formData.complianceScore,
      photoQualityScore: formData.photoQualityScore,
      descriptionAccuracyScore: formData.descriptionAccuracyScore,
      facilityCompletenessScore: formData.facilityCompletenessScore,
      locationAccuracyScore: formData.locationAccuracyScore,
      status: formData.status,
      overallRating: convertToOverallRating(calculatedScore.value),
      reviewComments: formData.reviewComments || undefined,
      rejectionReasons: rejectionReasons.length > 0 ? rejectionReasons : undefined
    }
    
    console.log('提交审核参数:', params) // 调试日志
    
    // 调用API
    await roomReviewApi.submitReview(params)
    
    ElMessage.success('审核提交成功')
    
    // 触发成功事件
    emit('success')
    
    // 关闭对话框
    handleClose()
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('提交审核失败:', error)
      ElMessage.error(error.message || '提交审核失败')
    }
  } finally {
    loading.value = false
  }
}

// 获取评分颜色
const getScoreColor = (score: number) => {
  if (score >= 7.0) return '#67c23a'  // 绿色
  if (score >= 4.0) return '#e6a23c'  // 橙色
  return '#f56c6c'  // 红色
}
</script>

<style scoped lang="scss">
.smart-review {
  .info-card {
    margin-bottom: 20px;
  }
  
  .info-item {
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    
    label {
      min-width: 80px;
      color: #606266;
      font-weight: 500;
      margin-right: 8px;
    }
    
    span {
      color: #303133;
      flex: 1;
      
      &.rent {
        color: #e6a23c;
        font-weight: 600;
      }
    }
  }
}

.review-form {
  .score-card {
    margin-bottom: 20px;
    
    .score-input {
      display: flex;
      align-items: center;
      width: 100%;
      
      .el-slider {
        flex: 1;
        margin-right: 16px;
        min-width: 300px; /* 确保滑动条有足够的宽度 */
      }
      
      .score-unit {
        color: #606266;
        font-weight: 500;
        min-width: 40px;
        text-align: right;
        font-size: 14px;
      }
    }
  }
  
  .recommendation-card {
    margin-bottom: 20px;
    
    .overall-score-section {
      display: flex;
      gap: 24px;
      align-items: flex-start;
      
      .score-display {
        flex: 1;
        text-align: center;
        
        .score-label {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 8px;
        }
        
        .score-value {
          font-size: 24px;
          font-weight: 700;
          color: #409eff;
          margin-bottom: 12px;
        }
      }
      
      .recommendation {
        flex: 1;
        padding: 16px;
        border-radius: 8px;
        border: 2px solid;
        
        &.success {
          background-color: #f0f9ff;
          border-color: #10b981;
          
          .recommendation-icon {
            color: #10b981;
          }
          
          .recommendation-title {
            color: #059669;
          }
        }
        
        &.warning {
          background-color: #fffbeb;
          border-color: #f59e0b;
          
          .recommendation-icon {
            color: #f59e0b;
          }
          
          .recommendation-title {
            color: #d97706;
          }
        }
        
        &.danger {
          background-color: #fef2f2;
          border-color: #ef4444;
          
          .recommendation-icon {
            color: #ef4444;
          }
          
          .recommendation-title {
            color: #dc2626;
          }
        }
        
        .recommendation-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          
          .recommendation-icon {
            font-size: 20px;
          }
          
          .recommendation-title {
            font-size: 16px;
            font-weight: 600;
          }
        }
        
        .recommendation-message {
          color: #6b7280;
          font-size: 14px;
          line-height: 1.5;
        }
      }
    }
  }
  
  .decision-group {
    display: flex;
    gap: 16px;
    
    :deep(.el-radio) {
      margin-right: 0;
      padding: 12px 16px;
      border: 2px solid #e5e7eb;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        border-color: #409eff;
      }
      
      &.is-checked {
        border-color: #409eff;
        background-color: #f0f9ff;
      }
      
      &.recommended {
        border-color: #10b981;
        background-color: #f0f9ff;
        
        &.is-checked {
          border-color: #10b981;
          background-color: #ecfdf5;
        }
      }
      
      .el-radio__label {
        display: flex;
        align-items: center;
        gap: 6px;
        font-weight: 500;
      }
    }
  }
  
  .conflict-warning {
    margin-top: 12px;
  }
  
  .rejection-reasons {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    
    :deep(.el-checkbox) {
      margin-right: 0;
      
      .el-checkbox__label {
        font-size: 14px;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

// 响应式设计
@media (max-width: 768px) {
  .smart-review {
    .review-form {
      .recommendation-card {
        .overall-score-section {
          flex-direction: column;
          gap: 16px;
        }
      }
      
      .decision-group {
        flex-direction: column;
        gap: 8px;
      }
      
      .rejection-reasons {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style>