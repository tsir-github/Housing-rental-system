<template>
  <div class="landlord-permission-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>房东权限管理</h2>
      <p>管理房东账户权限，支持权限分配、撤销、账户启用/禁用等操作</p>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="手机号">
          <el-input 
            v-model="queryParams.phone" 
            placeholder="请输入手机号" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="昵称">
          <el-input 
            v-model="queryParams.nickname" 
            placeholder="请输入昵称" 
            clearable 
            style="width: 200px"
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
          <el-button type="primary" @click="handleRefreshPermissions">
            <el-icon><Refresh /></el-icon>
            刷新权限
          </el-button>
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
        <el-table-column prop="landlordId" label="房东ID" width="80" />
        
        <el-table-column label="房东信息" min-width="200">
          <template #default="{ row }">
            <div class="landlord-info">
              <el-avatar 
                :src="row.avatarUrl" 
                :size="40"
                class="avatar"
              >
                {{ row.nickname?.charAt(0) || '房' }}
              </el-avatar>
              <div class="info">
                <div class="nickname">{{ row.nickname || '未设置昵称' }}</div>
                <div class="phone">{{ row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="账户状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="权限信息" min-width="200">
          <template #default="{ row }">
            <div class="permission-info">
              <div class="permission-count">
                权限数量: <span class="count">{{ row.permissionCount }}</span>
              </div>
              <div class="has-permission">
                <el-tag :type="row.hasLandlordPermission ? 'success' : 'danger'" size="small">
                  {{ row.hasLandlordPermission ? '有房东权限' : '无房东权限' }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleViewPermissions(row)"
            >
              查看权限
            </el-button>
            
            <el-button 
              type="success" 
              size="small" 
              @click="handleAssignPermissions(row)"
            >
              分配权限
            </el-button>
            
            <el-button 
              type="warning" 
              size="small" 
              @click="handleRevokePermissions(row)"
            >
              撤销权限
            </el-button>
            
            <el-dropdown @command="(command) => handleMoreActions(command, row)">
              <el-button type="info" size="small">
                更多
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="reset">重置权限</el-dropdown-item>
                  <el-dropdown-item 
                    :command="row.status === LandlordStatus.NORMAL ? 'disable' : 'enable'"
                  >
                    {{ row.status === LandlordStatus.NORMAL ? '禁用账户' : '启用账户' }}
                  </el-dropdown-item>
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

    <!-- 权限详情对话框 -->
    <PermissionDetailDialog 
      v-model:visible="permissionDetailVisible"
      :landlord="currentLandlord"
    />

    <!-- 分配权限对话框 -->
    <AssignPermissionDialog
      v-model:visible="assignPermissionVisible"
      :landlord="currentLandlord"
      @success="handleOperationSuccess"
    />

    <!-- 撤销权限对话框 -->
    <RevokePermissionDialog
      v-model:visible="revokePermissionVisible"
      :landlord="currentLandlord"
      @success="handleOperationSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
import { landlordPermissionApi } from '@/api/landlordManagement'
import type { 
  LandlordPermissionVo, 
  LandlordListParams
} from '@/api/landlordManagement/types'
import { LandlordStatus } from '@/enums/constEnums'
import PermissionDetailDialog from './components/PermissionDetailDialog.vue'
import AssignPermissionDialog from './components/AssignPermissionDialog.vue'
import RevokePermissionDialog from './components/RevokePermissionDialog.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<LandlordPermissionVo[]>([])
const total = ref(0)
const permissionDetailVisible = ref(false)
const assignPermissionVisible = ref(false)
const revokePermissionVisible = ref(false)
const currentLandlord = ref<LandlordPermissionVo | null>(null)

// 查询参数
const queryParams = reactive<LandlordListParams>({
  current: 1,
  size: 10,
  phone: '',
  nickname: ''
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await landlordPermissionApi.getLandlordList(queryParams)
    tableData.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('获取房东列表失败')
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
    phone: '',
    nickname: ''
  })
  fetchData()
}

// 刷新
const handleRefresh = () => {
  fetchData()
}

// 刷新权限
const handleRefreshPermissions = async () => {
  try {
    await fetchData()
    ElMessage.success('权限信息已刷新')
  } catch (error) {
    ElMessage.error('刷新权限信息失败')
  }
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

// 查看权限
const handleViewPermissions = (row: LandlordPermissionVo) => {
  currentLandlord.value = row
  permissionDetailVisible.value = true
}

// 分配权限
const handleAssignPermissions = (row: LandlordPermissionVo) => {
  currentLandlord.value = row
  assignPermissionVisible.value = true
}

// 撤销权限
const handleRevokePermissions = (row: LandlordPermissionVo) => {
  currentLandlord.value = row
  revokePermissionVisible.value = true
}

// 更多操作
const handleMoreActions = async (command: string, row: LandlordPermissionVo) => {
  currentLandlord.value = row
  
  switch (command) {
    case 'reset':
      await handleResetPermissions(row)
      break
    case 'disable':
      await handleDisableLandlord(row)
      break
    case 'enable':
      await handleEnableLandlord(row)
      break
  }
}

// 重置权限
const handleResetPermissions = async (row: LandlordPermissionVo) => {
  try {
    await ElMessageBox.confirm(
      `确认重置房东 "${row.nickname}" 的所有权限吗？此操作不可撤销。`,
      '重置权限',
      { type: 'warning' }
    )
    
    await landlordPermissionApi.resetLandlordPermissions(row.landlordId)
    ElMessage.success('权限重置成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('权限重置失败')
    }
  }
}

// 禁用房东
const handleDisableLandlord = async (row: LandlordPermissionVo) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `请输入禁用房东 "${row.nickname}" 的原因`,
      '禁用账户',
      {
        inputType: 'textarea',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '请输入禁用原因'
          }
          return true
        }
      }
    )
    
    await landlordPermissionApi.disableLandlord(row.landlordId, { reason })
    ElMessage.success('账户禁用成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('账户禁用失败')
    }
  }
}

// 启用房东
const handleEnableLandlord = async (row: LandlordPermissionVo) => {
  try {
    await ElMessageBox.confirm(
      `确认启用房东 "${row.nickname}" 的账户吗？`,
      '启用账户',
      { type: 'warning' }
    )
    
    await landlordPermissionApi.enableLandlord(row.landlordId)
    ElMessage.success('账户启用成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('账户启用失败')
    }
  }
}

// 操作成功回调
const handleOperationSuccess = () => {
  fetchData()
}

// 获取状态标签类型
const getStatusTagType = (status: LandlordStatus) => {
  switch (status) {
    case LandlordStatus.NORMAL:
      return 'success'
    case LandlordStatus.SUSPENDED:
      return 'warning'
    case LandlordStatus.BANNED:
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: LandlordStatus) => {
  switch (status) {
    case LandlordStatus.NORMAL:
      return '正常'
    case LandlordStatus.SUSPENDED:
      return '暂停'
    case LandlordStatus.BANNED:
      return '封禁'
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
.landlord-permission-management {
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
        display: flex;
        gap: 8px;
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

  .landlord-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .avatar {
      flex-shrink: 0;
    }
    
    .info {
      flex: 1;
      
      .nickname {
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .phone {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .permission-info {
    .permission-count {
      margin-bottom: 4px;
      
      .count {
        font-weight: 600;
        color: #409eff;
      }
    }
  }
}
</style>