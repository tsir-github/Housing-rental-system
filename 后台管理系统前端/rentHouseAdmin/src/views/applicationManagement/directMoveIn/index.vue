<template>
  <div class="direct-move-in-application">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>直接入住申请管理</h2>
      <p>管理租客的直接入住申请，支持审核通过、拒绝等操作</p>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="申请状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待处理" :value="DirectMoveInStatus.PENDING" />
            <el-option label="已确认" :value="DirectMoveInStatus.APPROVED" />
            <el-option label="已拒绝" :value="DirectMoveInStatus.REJECTED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="处理人类型">
          <el-select v-model="queryParams.processorType" placeholder="请选择处理人类型" clearable>
            <el-option label="官方人员" :value="ProcessorType.OFFICIAL" />
            <el-option label="房东" :value="ProcessorType.LANDLORD" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="房源类型">
          <el-select v-model="queryParams.publisherType" placeholder="请选择房源类型" clearable>
            <el-option label="官方房源" :value="PublisherType.OFFICIAL" />
            <el-option label="房东房源" :value="PublisherType.LANDLORD" />
          </el-select>
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
            <el-tab-pane label="待处理申请" name="pending" />
            <el-tab-pane label="申请历史" name="history" />
          </el-tabs>
        </div>
        <div class="toolbar-right">
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
        stripe 
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="name" label="申请人" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="apartmentName" label="公寓名称" width="150" show-overflow-tooltip />
        <el-table-column prop="publisherTypeName" label="房源类型" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="row.publisherType === 1 ? 'primary' : 'success'"
              size="small"
            >
              {{ row.publisherTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="landlordName" label="房东信息" width="120">
          <template #default="{ row }">
            <div v-if="row.landlordName">
              <div>{{ row.landlordName }}</div>
              <div class="text-gray-500 text-xs">{{ row.landlordPhone }}</div>
            </div>
            <span v-else class="text-gray-400">无房东信息</span>
          </template>
        </el-table-column>
        <el-table-column prop="rent" label="租金(元/月)" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.rent }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="leaseTermName" label="租期类型" width="100" />
        <el-table-column prop="paymentTypeName" label="支付方式" width="100" />
        <el-table-column prop="leaseStartDate" label="租期开始" width="120" />
        <el-table-column prop="leaseEndDate" label="租期结束" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusTagType(row.status)"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="processorName" label="处理人" width="120">
          <template #default="{ row }">
            <div v-if="row.processorName">
              <div>{{ row.processorName }}</div>
              <div class="text-gray-500 text-xs">
                {{ row.processorType === 1 ? '官方人员' : '房东' }}
              </div>
            </div>
            <span v-else class="text-gray-400">未处理</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleViewDetail(row)"
            >
              查看详情
            </el-button>
            <el-button 
              v-if="row.status === DirectMoveInStatus.PENDING"
              type="success" 
              size="small" 
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === DirectMoveInStatus.PENDING"
              type="danger" 
              size="small" 
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
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

    <!-- 申请详情对话框 -->
    <ApplicationDetailDialog 
      v-model:visible="detailDialogVisible"
      :application="currentApplication"
      @approve="handleApproveFromDetail"
      @reject="handleRejectFromDetail"
    />

    <!-- 处理申请对话框 -->
    <ProcessApplicationDialog
      v-model:visible="processDialogVisible"
      :application="currentApplication"
      :action="processAction"
      @success="handleProcessSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { directMoveInApplicationApi } from '@/api/applicationManagement'
import type { 
  DirectMoveInApplication, 
  DirectMoveInApplicationListParams
} from '@/api/applicationManagement/types'
import ApplicationDetailDialog from './components/ApplicationDetailDialog.vue'
import ProcessApplicationDialog from './components/ProcessApplicationDialog.vue'

// 导入枚举
import { 
  DirectMoveInStatus,
  DirectMoveInStatusMap,
  ProcessorType,
  PublisherType
} from '@/enums/constEnums'

// 响应式数据
const loading = ref(false)
const activeTab = ref('pending')
const tableData = ref<DirectMoveInApplication[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentApplication = ref<DirectMoveInApplication | null>(null)
const processAction = ref<'approve' | 'reject'>('approve')

// 查询参数
const queryParams = reactive<DirectMoveInApplicationListParams>({
  current: 1,
  size: 10,
  status: undefined,
  processorType: undefined,
  publisherType: undefined
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const api = activeTab.value === 'pending' 
      ? directMoveInApplicationApi.getPendingApplications
      : directMoveInApplicationApi.getApplicationHistory
    
    const { data } = await api(queryParams)
    tableData.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('获取数据失败')
    console.error(error)
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
    processorType: undefined,
    publisherType: undefined
  })
  fetchData()
}

// 刷新
const handleRefresh = () => {
  fetchData()
}

// 切换标签页
const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
  queryParams.current = 1
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

// 查看详情
const handleViewDetail = (row: DirectMoveInApplication) => {
  currentApplication.value = row
  detailDialogVisible.value = true
}

// 通过申请
const handleApprove = (row: DirectMoveInApplication) => {
  currentApplication.value = row
  processAction.value = 'approve'
  processDialogVisible.value = true
}

// 拒绝申请
const handleReject = (row: DirectMoveInApplication) => {
  currentApplication.value = row
  processAction.value = 'reject'
  processDialogVisible.value = true
}

// 处理成功回调
const handleProcessSuccess = () => {
  fetchData()
}

// 从详情对话框触发的通过申请
const handleApproveFromDetail = (application: DirectMoveInApplication) => {
  currentApplication.value = application
  processAction.value = 'approve'
  processDialogVisible.value = true
}

// 从详情对话框触发的拒绝申请
const handleRejectFromDetail = (application: DirectMoveInApplication) => {
  currentApplication.value = application
  processAction.value = 'reject'
  processDialogVisible.value = true
}

// 获取状态标签类型
const getStatusTagType = (status: DirectMoveInStatus) => {
  switch (status) {
    case DirectMoveInStatus.PENDING:
      return 'warning'
    case DirectMoveInStatus.APPROVED:
      return 'success'
    case DirectMoveInStatus.REJECTED:
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: DirectMoveInStatus) => {
  switch (status) {
    case DirectMoveInStatus.PENDING:
      return '待处理'
    case DirectMoveInStatus.APPROVED:
      return '已确认'
    case DirectMoveInStatus.REJECTED:
      return '已拒绝'
    default:
      return '未知'
  }
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.direct-move-in-application {
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

  .price {
    color: #f56c6c;
    font-weight: 600;
  }
}
</style>