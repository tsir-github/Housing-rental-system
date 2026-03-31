<template>
  <el-dialog
    v-model="visible"
    title="房东权限详情"
    width="600px"
    :before-close="handleClose"
  >
    <div v-if="landlord" class="permission-detail">
      <!-- 房东基本信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span>房东基本信息</span>
        </template>
        
        <div class="landlord-basic-info">
          <el-avatar 
            :src="landlord.avatarUrl" 
            :size="60"
            class="avatar"
          >
            {{ landlord.nickname?.charAt(0) || '房' }}
          </el-avatar>
          <div class="info">
            <div class="item">
              <label>房东ID：</label>
              <span>{{ landlord.landlordId }}</span>
            </div>
            <div class="item">
              <label>昵称：</label>
              <span>{{ landlord.nickname || '未设置' }}</span>
            </div>
            <div class="item">
              <label>手机号：</label>
              <span>{{ landlord.phone }}</span>
            </div>
            <div class="item">
              <label>账户状态：</label>
              <el-tag :type="getStatusTagType(landlord.status)">
                {{ getStatusText(landlord.status) }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 权限信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>权限信息</span>
            <el-button type="primary" size="small" @click="handleRefreshPermissions">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>
        
        <div v-loading="permissionLoading" class="permission-content">
          <div class="permission-summary">
            <div class="summary-item">
              <label>权限总数：</label>
              <span class="count">{{ permissions.length }}</span>
            </div>
            <div class="summary-item">
              <label>房东权限：</label>
              <el-tag :type="landlord.hasLandlordPermission ? 'success' : 'danger'">
                {{ landlord.hasLandlordPermission ? '已开通' : '未开通' }}
              </el-tag>
            </div>
          </div>
          
          <el-divider />
          
          <div class="permission-list">
            <div v-if="permissions.length === 0" class="empty-permissions">
              <el-empty description="暂无权限" />
            </div>
            <div v-else class="permissions-grid">
              <el-tag
                v-for="permission in permissions"
                :key="permission"
                class="permission-tag"
                type="primary"
              >
                {{ getPermissionDisplayName(permission) }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 操作记录 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span>操作记录</span>
        </template>
        
        <div class="operation-records">
          <div class="record-item">
            <label>注册时间：</label>
            <span>{{ formatDateTime(landlord.createTime) }}</span>
          </div>
          <div class="record-item">
            <label>最后更新：</label>
            <span>{{ formatDateTime(landlord.updateTime) }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleManagePermissions">
          管理权限
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { landlordPermissionApi } from '@/api/landlordManagement'
import type { LandlordPermissionVo } from '@/api/landlordManagement/types'
import { LandlordStatus } from '@/enums/constEnums'

// Props
const props = defineProps<{
  visible: boolean
  landlord: LandlordPermissionVo | null
}>()

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'manage-permissions': [landlord: LandlordPermissionVo]
}>()

// 响应式数据
const permissionLoading = ref(false)
const permissions = ref<string[]>([])

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 监听对话框显示状态
watch(() => props.visible, (visible) => {
  if (visible && props.landlord) {
    loadPermissions()
  }
})

// 加载权限信息
const loadPermissions = async () => {
  if (!props.landlord) return
  
  permissionLoading.value = true
  try {
    const { data } = await landlordPermissionApi.getLandlordPermissions(props.landlord.landlordId)
    permissions.value = data
  } catch (error) {
    ElMessage.error('获取权限信息失败')
    console.error(error)
  } finally {
    permissionLoading.value = false
  }
}

// 刷新权限
const handleRefreshPermissions = () => {
  loadPermissions()
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 管理权限
const handleManagePermissions = () => {
  if (props.landlord) {
    emit('manage-permissions', props.landlord)
  }
  handleClose()
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

// 获取权限显示名称
const getPermissionDisplayName = (permission: string) => {
  const permissionMap: Record<string, string> = {
    // 基础权限
    'manage_rooms': '房间管理',
    'manage_apartments': '公寓管理',
    'manage_appointments': '预约管理',
    'create_lease': '创建租约',
    
    // 房源管理相关权限
    'manage_fees': '杂费管理',
    'manage_facilities': '配套管理',
    'manage_attributes': '属性管理',
    'manage_lease_terms': '租期管理',
    'manage_payment_types': '支付方式管理',
    'manage_labels': '标签管理',
    'manage_files': '文件管理',
    'manage_regions': '地区管理',
    
    // 高级权限
    'view_applications': '查看申请',
    'process_applications': '处理申请',
    'view_statistics': '查看统计',
    'manage_tenants': '管理租客',
    'financial_management': '财务管理',
    'view_reviews': '查看审核状态',
    'manage_reviews': '审核管理'
  }
  
  return permissionMap[permission] || permission
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.permission-detail {
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

  .landlord-basic-info {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    
    .avatar {
      flex-shrink: 0;
    }
    
    .info {
      flex: 1;
      
      .item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        
        label {
          min-width: 80px;
          color: #606266;
          font-weight: 500;
          margin-right: 8px;
        }
        
        span {
          color: #303133;
        }
      }
    }
  }

  .permission-content {
    .permission-summary {
      display: flex;
      gap: 24px;
      margin-bottom: 16px;
      
      .summary-item {
        display: flex;
        align-items: center;
        
        label {
          color: #606266;
          font-weight: 500;
          margin-right: 8px;
        }
        
        .count {
          font-size: 18px;
          font-weight: 600;
          color: #409eff;
        }
      }
    }
    
    .permission-list {
      .empty-permissions {
        text-align: center;
        padding: 20px;
      }
      
      .permissions-grid {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        .permission-tag {
          margin: 0;
        }
      }
    }
  }

  .operation-records {
    .record-item {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      
      label {
        min-width: 80px;
        color: #606266;
        font-weight: 500;
        margin-right: 8px;
      }
      
      span {
        color: #303133;
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