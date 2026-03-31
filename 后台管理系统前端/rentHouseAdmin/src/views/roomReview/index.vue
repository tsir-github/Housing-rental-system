<template>
  <div class="room-review-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>房源审核管理</h2>
      <p>管理房源审核流程，支持手动审核、智能审核、批量审核等功能</p>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.status" placeholder="请选择审核状态" clearable>
            <el-option label="待审核" :value="ReviewStatus.PENDING" />
            <el-option label="审核通过" :value="ReviewStatus.APPROVED" />
            <el-option label="审核拒绝" :value="ReviewStatus.REJECTED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="房东ID">
          <el-input 
            v-model="queryParams.landlordId" 
            placeholder="请输入房东ID" 
            clearable 
            style="width: 150px"
          />
        </el-form-item>
        
        <el-form-item label="房间ID">
          <el-input 
            v-model="queryParams.roomId" 
            placeholder="请输入房间ID" 
            clearable 
            style="width: 150px"
          />
        </el-form-item>
        
        <el-form-item label="审核时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <!-- 表格工具栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="待审核房源" name="pending" />
            <el-tab-pane label="审核历史" name="history" />
          </el-tabs>
        </div>
        <div class="toolbar-right">
          <el-button 
            type="success" 
            @click="handleBatchReview" 
            :disabled="!multipleSelection.length"
          >
            <el-icon><Check /></el-icon>
            批量通过
          </el-button>
          <el-button 
            type="danger" 
            @click="handleBatchReject" 
            :disabled="!multipleSelection.length"
          >
            <el-icon><Close /></el-icon>
            批量拒绝
          </el-button>
          <el-button @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        @selection-change="handleSelectionChange"
        stripe 
        border
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" align="center" />
        
        <el-table-column prop="id" label="审核ID" width="80" />
        
        <el-table-column label="房源信息" min-width="200">
          <template #default="{ row }">
            <div class="room-info">
              <div class="apartment">{{ row.apartmentInfo?.name || '未知公寓' }}</div>
              <div class="room">房间号: {{ row.roomInfo?.roomNumber || row.roomId }}</div>
              <div class="landlord">房东: {{ row.landlordName || `ID:${row.landlordId}` }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="综合评分" width="120" align="center">
          <template #default="{ row }">
            <div v-if="row.overallRating !== null && row.overallRating !== undefined" class="score-info">
              <el-progress 
                :percentage="getOverallScorePercentage(row.overallRating)" 
                :color="getScoreColor(getOverallScorePercentage(row.overallRating))"
                :stroke-width="8"
              />
              <div class="score-text">{{ row.overallRating }}/4分</div>
            </div>
            <div v-else class="score-info">
              <span class="text-gray-400">待评分</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="各项评分" min-width="400">
          <template #default="{ row }">
            <div class="detailed-scores">
              <div class="score-item">
                <span class="label">合规性:</span>
                <el-progress 
                  :percentage="(row.complianceScore || 0) * 10" 
                  :stroke-width="4"
                  :show-text="false"
                />
                <span class="value">{{ row.complianceScore || '-' }}</span>
              </div>
              <div class="score-item">
                <span class="label">照片质量:</span>
                <el-progress 
                  :percentage="(row.photoQualityScore || 0) * 10" 
                  :stroke-width="4"
                  :show-text="false"
                />
                <span class="value">{{ row.photoQualityScore || '-' }}</span>
              </div>
              <div class="score-item">
                <span class="label">描述准确:</span>
                <el-progress 
                  :percentage="(row.descriptionAccuracyScore || 0) * 10" 
                  :stroke-width="4"
                  :show-text="false"
                />
                <span class="value">{{ row.descriptionAccuracyScore || '-' }}</span>
              </div>
              <div class="score-item">
                <span class="label">设施完整:</span>
                <el-progress 
                  :percentage="(row.facilityCompletenessScore || 0) * 10" 
                  :stroke-width="4"
                  :show-text="false"
                />
                <span class="value">{{ row.facilityCompletenessScore || '-' }}</span>
              </div>
              <div class="score-item">
                <span class="label">位置准确:</span>
                <el-progress 
                  :percentage="(row.locationAccuracyScore || 0) * 10" 
                  :stroke-width="4"
                  :show-text="false"
                />
                <span class="value">{{ row.locationAccuracyScore || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="审核信息" min-width="150">
          <template #default="{ row }">
            <div class="review-info">
              <div v-if="row.reviewerName" class="reviewer">
                审核员: {{ row.reviewerName }}
              </div>
              <div class="time">
                {{ row.reviewTime ? formatDateTime(row.reviewTime) : formatDateTime(row.createTime) }}
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleViewDetail(row)"
            >
              查看详情
            </el-button>
            
            <el-button 
              v-if="row.status === ReviewStatus.PENDING"
              type="success" 
              size="small" 
              @click="handleManualReview(row, 'approve')"
            >
              通过
            </el-button>
            
            <el-button 
              v-if="row.status === ReviewStatus.PENDING"
              type="danger" 
              size="small" 
              @click="handleManualReview(row, 'reject')"
            >
              拒绝
            </el-button>
            
            <el-dropdown @command="(command) => handleMoreActions(command, row)">
              <el-button type="info" size="small">
                更多
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="auto">自动审核</el-dropdown-item>
                  <el-dropdown-item command="comprehensive">智能审核</el-dropdown-item>
                  <el-dropdown-item command="score">查看评分</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 审核详情对话框 -->
    <ReviewDetailDialog 
      v-model:visible="detailDialogVisible"
      :review="currentReview"
    />

    <!-- 手动审核对话框 -->
    <ManualReviewDialog
      v-model:visible="manualReviewVisible"
      :review="currentReview"
      :action="reviewAction"
      @success="handleOperationSuccess"
    />

    <!-- 评分详情对话框 -->
    <ScoreDetailDialog
      v-model:visible="scoreDetailVisible"
      :room-id="currentRoomId"
    />

    <!-- 智能审核结果对话框 -->
    <ComprehensiveReviewDialog
      v-model:visible="comprehensiveReviewVisible"
      :result="comprehensiveResult"
      @confirm="handleComprehensiveConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Check, Close, ArrowDown } from '@element-plus/icons-vue'
import { roomReviewApi } from '@/api/roomReview'
import type { 
  RoomReviewVo, 
  RoomReviewListParams
} from '@/api/roomReview/types'
import ReviewDetailDialog from './components/ReviewDetailDialog.vue'
import ManualReviewDialog from './components/ManualReviewDialog.vue'
import ScoreDetailDialog from './components/ScoreDetailDialog.vue'
import ComprehensiveReviewDialog from './components/ComprehensiveReviewDialog.vue'

// 审核状态枚举
enum ReviewStatus {
  PENDING = 1,    // 待审核
  APPROVED = 2,   // 审核通过
  REJECTED = 3    // 审核拒绝
}

// 响应式数据
const loading = ref(false)
const activeTab = ref('pending')
const tableData = ref<RoomReviewVo[]>([])
const total = ref(0)
const multipleSelection = ref<RoomReviewVo[]>([])
const detailDialogVisible = ref(false)
const manualReviewVisible = ref(false)
const scoreDetailVisible = ref(false)
const comprehensiveReviewVisible = ref(false)
const currentReview = ref<RoomReviewVo | null>(null)
const currentRoomId = ref<number | null>(null)
const reviewAction = ref<'approve' | 'reject'>('approve')
const comprehensiveResult = ref<any | null>(null)
const dateRange = ref<[string, string] | null>(null)

// 查询参数
const queryParams = reactive<RoomReviewListParams>({
  current: 1,
  size: 10,
  status: undefined,
  landlordId: undefined,
  roomId: undefined,
  startDate: undefined,
  endDate: undefined
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const api = activeTab.value === 'pending' 
      ? roomReviewApi.getPendingReviews
      : roomReviewApi.getReviewHistory
    
    console.log('正在获取数据，参数:', queryParams) // 调试日志
    
    const { data } = await api(queryParams)
    
    console.log('API返回数据:', data) // 调试日志
    
    tableData.value = data.records || []
    total.value = data.total || 0
    
    // 调试：检查每条记录的数据结构
    tableData.value.forEach((item, index) => {
      console.log(`记录${index + 1}:`, {
        id: item.id,
        roomId: item.roomId,
        apartmentInfo: item.apartmentInfo,
        roomInfo: item.roomInfo,
        overallRating: item.overallRating,
        status: item.status
      })
    })
  } catch (error) {
    ElMessage.error('获取审核数据失败')
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(queryParams, {
    current: 1,
    size: 10,
    status: undefined,
    landlordId: undefined,
    roomId: undefined,
    startDate: undefined,
    endDate: undefined
  })
  dateRange.value = null
  fetchData()
}

// 刷新
const handleRefresh = () => {
  fetchData()
}

// 日期范围变化
const handleDateRangeChange = (dates: [string, string] | null) => {
  if (dates) {
    queryParams.startDate = dates[0]
    queryParams.endDate = dates[1]
  } else {
    queryParams.startDate = undefined
    queryParams.endDate = undefined
  }
}

// 切换标签页
const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
  queryParams.current = 1
  // 待审核页面只显示待审核状态
  if (tabName === 'pending') {
    queryParams.status = ReviewStatus.PENDING
  } else {
    queryParams.status = undefined
  }
  fetchData()
}

// 分页变化
const handleSizeChange = (size: number) => {
  queryParams.size = size
  queryParams.current = 1
  fetchData()
}

const handleCurrentChange = (current: number) => {
  queryParams.current = current
  fetchData()
}

// 多选变化
const handleSelectionChange = (selection: RoomReviewVo[]) => {
  multipleSelection.value = selection
}

// 查看详情
const handleViewDetail = async (row: RoomReviewVo) => {
  try {
    // 如果有审核记录ID，调用详情接口获取完整数据
    if (row.id) {
      console.log('调用审核详情接口，ID:', row.id) // 调试日志
      const { data } = await roomReviewApi.getReviewById(row.id)
      currentReview.value = data
    } else {
      // 待审核状态没有审核记录ID，直接使用列表数据
      console.log('待审核房源，使用列表数据:', row) // 调试日志
      currentReview.value = row
    }
    
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取审核详情失败:', error)
    ElMessage.error('获取审核详情失败')
    // 失败时仍然显示列表数据
    currentReview.value = row
    detailDialogVisible.value = true
  }
}

// 手动审核（统一入口）
const handleManualReview = (row: RoomReviewVo, action: 'approve' | 'reject') => {
  currentReview.value = row
  reviewAction.value = action
  manualReviewVisible.value = true
}

// 统一审核入口（新增）
const handleReview = (row: RoomReviewVo, suggestedAction?: 'approve' | 'reject') => {
  currentReview.value = row
  reviewAction.value = suggestedAction || 'approve'
  manualReviewVisible.value = true
}

// 更多操作
const handleMoreActions = async (command: string, row: RoomReviewVo) => {
  currentReview.value = row
  currentRoomId.value = row.roomId
  
  switch (command) {
    case 'auto':
      await handleAutoReview(row)
      break
    case 'comprehensive':
      await handleComprehensiveReview(row)
      break
    case 'score':
      scoreDetailVisible.value = true
      break
  }
}

// 自动审核
const handleAutoReview = async (row: RoomReviewVo) => {
  try {
    await ElMessageBox.confirm(
      `确认对房源 "${row.apartmentInfo?.name || '未知公寓'} - ${row.roomInfo?.roomNumber || row.roomId}" 执行自动审核吗？`,
      '自动审核',
      { type: 'warning' }
    )
    
    // 这里需要获取当前用户ID，暂时使用固定值
    const reviewerId = 1
    
    await roomReviewApi.autoReviewRoom(row.roomId, reviewerId)
    ElMessage.success('自动审核完成')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('自动审核失败')
    }
  }
}

// 智能综合审核
const handleComprehensiveReview = async (row: RoomReviewVo) => {
  try {
    // 这里需要获取当前用户ID，暂时使用固定值
    const reviewerId = 1
    
    const { data } = await roomReviewApi.comprehensiveReview(row.roomId, reviewerId)
    comprehensiveResult.value = data
    comprehensiveReviewVisible.value = true
  } catch (error) {
    ElMessage.error('智能审核失败')
    console.error(error)
  }
}

// 确认智能审核结果
const handleComprehensiveConfirm = () => {
  fetchData()
}

// 批量审核通过
const handleBatchReview = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要审核的房源')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认批量通过选中的 ${multipleSelection.value.length} 个房源审核吗？`,
      '批量审核',
      { type: 'warning' }
    )
    
    // 暂时使用单个审核API来实现批量功能
    for (const item of multipleSelection.value) {
      const reviewData = {
        roomId: item.roomId,
        complianceScore: 85,
        photoQualityScore: 85,
        descriptionAccuracyScore: 85,
        facilityCompletenessScore: 85,
        locationAccuracyScore: 85,
        overallRating: 4,
        reviewComments: '批量审核通过',
        approved: true
      }
      
      try {
        await roomReviewApi.submitReview(reviewData)
      } catch (error) {
        console.error(`房源 ${item.roomId} 审核失败:`, error)
      }
    }
    
    ElMessage.success('批量审核完成')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('批量审核失败')
    }
  }
}

// 批量审核拒绝
const handleBatchReject = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要审核的房源')
    return
  }
  
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `请输入批量拒绝 ${multipleSelection.value.length} 个房源的原因`,
      '批量拒绝',
      {
        inputType: 'textarea',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '请输入拒绝原因'
          }
          return true
        }
      }
    )
    
    // 暂时使用单个审核API来实现批量功能
    for (const item of multipleSelection.value) {
      const reviewData = {
        roomId: item.roomId,
        complianceScore: 30,
        photoQualityScore: 30,
        descriptionAccuracyScore: 30,
        facilityCompletenessScore: 30,
        locationAccuracyScore: 30,
        overallRating: 1,
        reviewComments: reason,
        approved: false,
        rejectionReasons: [reason]
      }
      
      try {
        await roomReviewApi.submitReview(reviewData)
      } catch (error) {
        console.error(`房源 ${item.roomId} 拒绝失败:`, error)
      }
    }
    
    ElMessage.success('批量拒绝完成')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('批量拒绝失败')
    }
  }
}

// 操作成功回调
const handleOperationSuccess = () => {
  fetchData()
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

// 获取评分颜色
const getScoreColor = (score: number) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 获取综合评分百分比（1-4分制转换为百分比）
const getOverallScorePercentage = (rating: number) => {
  // overallRating: 1-优秀(100%), 2-良好(75%), 3-一般(50%), 4-较差(25%)
  switch (rating) {
    case 1: return 100  // 优秀
    case 2: return 75   // 良好
    case 3: return 50   // 一般
    case 4: return 25   // 较差
    default: return 0
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 初始化
onMounted(() => {
  // 默认显示待审核
  queryParams.status = ReviewStatus.PENDING
  fetchData()
})
</script>

<style scoped lang="scss">
.room-review-management {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
    
    h2 {
      margin: 0 0 8px 0;
      color: #303133;
      font-size: 20px;
      font-weight: 600;
    }
    
    p {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .filter-card {
    margin-bottom: 20px;
  }

  .table-card {
    .table-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .toolbar-left {
        flex: 1;
      }
      
      .toolbar-right {
        display: flex;
        gap: 8px;
      }
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }

  .room-info {
    .apartment {
      font-weight: 500;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .room, .landlord {
      font-size: 12px;
      color: #909399;
      margin-bottom: 2px;
    }
  }

  .score-info {
    text-align: center;
    
    .score-text {
      font-size: 12px;
      color: #606266;
      margin-top: 4px;
    }
  }

  .detailed-scores {
    .score-item {
      display: flex;
      align-items: center;
      margin-bottom: 4px;
      font-size: 12px;
      
      .label {
        min-width: 60px;
        color: #606266;
        margin-right: 8px;
      }
      
      :deep(.el-progress) {
        flex: 1;
        margin-right: 8px;
      }
      
      .value {
        min-width: 30px;
        color: #303133;
        font-weight: 500;
      }
    }
  }

  .review-info {
    .reviewer {
      font-size: 12px;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .time {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>