<template>
  <el-dialog
    v-model="visible"
    title="评分详情"
    width="700px"
    :before-close="handleClose"
  >
    <div v-loading="loading" class="score-detail">
      <div v-if="scoreInfo" class="score-content">
        <!-- 综合评分 -->
        <el-card class="score-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>综合评分</span>
              <el-tag 
                :type="getScoreTagType(scoreInfo.overallScore)"
                size="large"
              >
                {{ getScoreLevel(scoreInfo.overallScore) }}
              </el-tag>
            </div>
          </template>
          
          <div class="overall-score">
            <el-progress 
              :percentage="scoreInfo.overallScore" 
              :color="getScoreColor(scoreInfo.overallScore)"
              :stroke-width="20"
              text-inside
            />
            <div class="score-text">{{ scoreInfo.overallScore }}分</div>
          </div>
        </el-card>

        <!-- 详细评分 -->
        <el-card class="score-card" shadow="never">
          <template #header>
            <span>详细评分</span>
          </template>
          
          <div class="detailed-scores">
            <div class="score-item">
              <div class="score-label">
                <el-icon><House /></el-icon>
                <span>合规性评分</span>
              </div>
              <div class="score-progress">
                <el-progress 
                  :percentage="scoreInfo.complianceScore" 
                  :color="getScoreColor(scoreInfo.complianceScore)"
                  :stroke-width="8"
                />
              </div>
              <div class="score-value">{{ scoreInfo.complianceScore }}分</div>
            </div>
            
            <div class="score-item">
              <div class="score-label">
                <el-icon><Camera /></el-icon>
                <span>照片质量</span>
              </div>
              <div class="score-progress">
                <el-progress 
                  :percentage="scoreInfo.photoQualityScore" 
                  :color="getScoreColor(scoreInfo.photoQualityScore)"
                  :stroke-width="8"
                />
              </div>
              <div class="score-value">{{ scoreInfo.photoQualityScore }}分</div>
            </div>
            
            <div class="score-item">
              <div class="score-label">
                <el-icon><Document /></el-icon>
                <span>描述准确性</span>
              </div>
              <div class="score-progress">
                <el-progress 
                  :percentage="scoreInfo.descriptionAccuracyScore" 
                  :color="getScoreColor(scoreInfo.descriptionAccuracyScore)"
                  :stroke-width="8"
                />
              </div>
              <div class="score-value">{{ scoreInfo.descriptionAccuracyScore }}分</div>
            </div>
            
            <div class="score-item">
              <div class="score-label">
                <el-icon><Tools /></el-icon>
                <span>设施完整性</span>
              </div>
              <div class="score-progress">
                <el-progress 
                  :percentage="scoreInfo.facilityCompletenessScore" 
                  :color="getScoreColor(scoreInfo.facilityCompletenessScore)"
                  :stroke-width="8"
                />
              </div>
              <div class="score-value">{{ scoreInfo.facilityCompletenessScore }}分</div>
            </div>
            
            <div class="score-item">
              <div class="score-label">
                <el-icon><Location /></el-icon>
                <span>位置准确性</span>
              </div>
              <div class="score-progress">
                <el-progress 
                  :percentage="scoreInfo.locationAccuracyScore" 
                  :color="getScoreColor(scoreInfo.locationAccuracyScore)"
                  :stroke-width="8"
                />
              </div>
              <div class="score-value">{{ scoreInfo.locationAccuracyScore }}分</div>
            </div>
          </div>
        </el-card>

        <!-- 评分说明 -->
        <el-card class="score-card" shadow="never">
          <template #header>
            <span>评分说明</span>
          </template>
          
          <div class="score-description">
            <div class="desc-item">
              <el-tag type="success">优秀 (80-100分)</el-tag>
              <span>房源信息完整准确，照片清晰，设施齐全，位置描述准确</span>
            </div>
            <div class="desc-item">
              <el-tag type="warning">良好 (60-79分)</el-tag>
              <span>房源信息基本准确，存在少量不足，需要适当改进</span>
            </div>
            <div class="desc-item">
              <el-tag type="danger">需改进 (0-59分)</el-tag>
              <span>房源信息存在明显问题，需要重新整理和完善</span>
            </div>
          </div>
        </el-card>
      </div>
      
      <div v-else class="empty-state">
        <el-empty description="暂无评分信息" />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { House, Camera, Document, Tools, Location, Refresh } from '@element-plus/icons-vue'
import { roomReviewApi } from '@/api/roomReview'
import type { RoomScoreInfo } from '@/api/roomReview/types'

// Props
const props = defineProps<{
  visible: boolean
  roomId: number | null
}>()

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

// 响应式数据
const loading = ref(false)
const scoreInfo = ref<RoomScoreInfo | null>(null)

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 监听对话框显示状态
watch(() => props.visible, (visible) => {
  if (visible && props.roomId) {
    loadScoreInfo()
  }
})

// 加载评分信息
const loadScoreInfo = async () => {
  if (!props.roomId) return
  
  loading.value = true
  try {
    // 这里应该调用获取房源评分的API
    // const { data } = await roomReviewApi.getRoomScore(props.roomId)
    // scoreInfo.value = data
    
    // 临时模拟数据
    scoreInfo.value = {
      complianceScore: 85,
      photoQualityScore: 78,
      descriptionAccuracyScore: 82,
      facilityCompletenessScore: 90,
      locationAccuracyScore: 88,
      overallScore: 85
    }
  } catch (error) {
    ElMessage.error('获取评分信息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 刷新评分信息
const handleRefresh = () => {
  loadScoreInfo()
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 获取评分颜色
const getScoreColor = (score: number) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 获取评分标签类型
const getScoreTagType = (score: number) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

// 获取评分等级
const getScoreLevel = (score: number) => {
  if (score >= 80) return '优秀'
  if (score >= 60) return '良好'
  return '需改进'
}
</script>

<style scoped lang="scss">
.score-detail {
  .score-content {
    .score-card {
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
    
    .overall-score {
      text-align: center;
      
      .score-text {
        font-size: 24px;
        font-weight: 600;
        color: #409eff;
        margin-top: 16px;
      }
    }
    
    .detailed-scores {
      .score-item {
        display: flex;
        align-items: center;
        margin-bottom: 20px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .score-label {
          display: flex;
          align-items: center;
          min-width: 120px;
          color: #606266;
          font-weight: 500;
          
          .el-icon {
            margin-right: 8px;
            color: #409eff;
          }
        }
        
        .score-progress {
          flex: 1;
          margin: 0 16px;
        }
        
        .score-value {
          min-width: 60px;
          text-align: right;
          font-weight: 600;
          color: #303133;
        }
      }
    }
    
    .score-description {
      .desc-item {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .el-tag {
          margin-right: 12px;
          min-width: 100px;
          text-align: center;
        }
        
        span {
          color: #606266;
          line-height: 1.5;
        }
      }
    }
  }
  
  .empty-state {
    text-align: center;
    padding: 40px 0;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>