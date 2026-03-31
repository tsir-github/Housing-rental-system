<template>
  <el-dialog
    v-model="visible"
    title="撤销权限"
    width="600px"
    :before-close="handleClose"
  >
    <div v-if="landlord" class="revoke-permission">
      <!-- 房东信息 -->
      <el-card class="landlord-info-card" shadow="never">
        <div class="landlord-info">
          <el-avatar 
            :src="landlord.avatarUrl" 
            :size="40"
          >
            {{ landlord.nickname?.charAt(0) || '房' }}
          </el-avatar>
          <div class="info">
            <div class="name">{{ landlord.nickname || '未设置昵称' }}</div>
            <div class="phone">{{ landlord.phone }}</div>
          </div>
          <el-tag :type="getStatusTagType(landlord.status)">
            {{ getStatusText(landlord.status) }}
          </el-tag>
        </div>
      </el-card>

      <!-- 权限撤销表单 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="permission-form"
      >
        <el-form-item label="当前权限" prop="selectedPermissions">
          <div v-loading="permissionLoading" class="permission-selection">
            <div v-if="currentPermissions.length === 0" class="empty-permissions">
              <el-empty description="该房东暂无权限" />
            </div>
            <div v-else class="permission-checkboxes">
              <el-checkbox-group v-model="formData.selectedPermissions">
                <div class="permission-categories">
                  <div 
                    v-for="category in permissionCategories" 
                    :key="category.name"
                    class="category-section"
                    v-show="getCategoryPermissions(category).length > 0"
                  >
                    <div class="category-header">
                      <el-checkbox 
                        :indeterminate="getCategoryIndeterminate(category)"
                        :model-value="getCategoryChecked(category)"
                        @change="handleCategoryChange(category, $event)"
                      >
                        {{ category.label }}
                      </el-checkbox>
                    </div>
                    <div class="category-permissions">
                      <el-checkbox
                        v-for="permission in getCategoryPermissions(category)"
                        :key="permission.value"
                        :label="permission.value"
                      >
                        {{ permission.label }}
                      </el-checkbox>
                    </div>
                  </div>
                </div>
              </el-checkbox-group>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="撤销原因" prop="reason">
          <el-input
            v-model="formData.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入撤销权限的原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <!-- 警告提示 -->
      <el-alert
        v-if="formData.selectedPermissions.length > 0"
        title="撤销权限警告"
        type="warning"
        :description="`您即将撤销 ${formData.selectedPermissions.length} 个权限，此操作可能影响房东的正常使用，请谨慎操作。`"
        show-icon
        :closable="false"
        class="warning-alert"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="danger" 
          :loading="loading"
          @click="handleSubmit"
        >
          确认撤销
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { landlordPermissionApi } from '@/api/landlordManagement'
import type { 
  LandlordPermissionVo, 
  RevokePermissionsParams 
} from '@/api/landlordManagement/types'
import { LandlordStatus } from '@/enums/constEnums'

// 权限分类定义
interface PermissionItem {
  value: string
  label: string
}

interface PermissionCategory {
  name: string
  label: string
  permissions: PermissionItem[]
}

// Props
const props = defineProps<{
  visible: boolean
  landlord: LandlordPermissionVo | null
}>()

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 响应式数据
const loading = ref(false)
const permissionLoading = ref(false)
const formRef = ref<FormInstance>()
const currentPermissions = ref<string[]>([])

// 表单数据
const formData = reactive<{
  selectedPermissions: string[]
  reason: string
}>({
  selectedPermissions: [],
  reason: ''
})

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 权限分类
const permissionCategories: PermissionCategory[] = [
  {
    name: 'basic',
    label: '基础权限',
    permissions: [
      { value: 'manage_rooms', label: '房间管理' },
      { value: 'manage_apartments', label: '公寓管理' },
      { value: 'manage_appointments', label: '预约管理' },
      { value: 'create_lease', label: '创建租约' }
    ]
  },
  {
    name: 'property',
    label: '房源管理',
    permissions: [
      { value: 'manage_fees', label: '杂费管理' },
      { value: 'manage_facilities', label: '配套管理' },
      { value: 'manage_attributes', label: '属性管理' },
      { value: 'manage_lease_terms', label: '租期管理' },
      { value: 'manage_payment_types', label: '支付方式管理' },
      { value: 'manage_labels', label: '标签管理' },
      { value: 'manage_files', label: '文件管理' },
      { value: 'manage_regions', label: '地区管理' }
    ]
  },
  {
    name: 'application',
    label: '申请处理',
    permissions: [
      { value: 'view_applications', label: '查看申请' },
      { value: 'process_applications', label: '处理申请' }
    ]
  },
  {
    name: 'tenant',
    label: '租客管理',
    permissions: [
      { value: 'manage_tenants', label: '管理租客' }
    ]
  },
  {
    name: 'statistics',
    label: '数据统计',
    permissions: [
      { value: 'view_statistics', label: '查看统计' }
    ]
  },
  {
    name: 'finance',
    label: '财务管理',
    permissions: [
      { value: 'financial_management', label: '财务管理' }
    ]
  },
  {
    name: 'review',
    label: '审核管理',
    permissions: [
      { value: 'view_reviews', label: '查看审核状态' },
      { value: 'manage_reviews', label: '审核管理' }
    ]
  }
]

// 表单验证规则
const formRules: FormRules = {
  selectedPermissions: [
    { 
      required: true, 
      message: '请选择要撤销的权限', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少选择一个权限'))
        } else {
          callback()
        }
      }
    }
  ],
  reason: [
    { required: true, message: '请输入撤销原因', trigger: 'blur' },
    { min: 5, message: '撤销原因至少5个字符', trigger: 'blur' }
  ]
}

// 监听对话框显示状态
watch(() => props.visible, (visible) => {
  if (visible && props.landlord) {
    loadCurrentPermissions()
  } else {
    resetForm()
  }
})

// 加载当前权限
const loadCurrentPermissions = async () => {
  if (!props.landlord) return
  
  permissionLoading.value = true
  try {
    const { data } = await landlordPermissionApi.getLandlordPermissions(props.landlord.landlordId)
    currentPermissions.value = data
  } catch (error) {
    ElMessage.error('获取权限信息失败')
    console.error(error)
  } finally {
    permissionLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.selectedPermissions = []
  formData.reason = ''
  formRef.value?.clearValidate()
}

// 获取分类下的当前权限
const getCategoryPermissions = (category: PermissionCategory) => {
  return category.permissions.filter(p => currentPermissions.value.includes(p.value))
}

// 获取分类选中状态
const getCategoryChecked = (category: PermissionCategory) => {
  const categoryPermissions = getCategoryPermissions(category).map(p => p.value)
  if (categoryPermissions.length === 0) return false
  
  const selectedInCategory = categoryPermissions.filter(p => 
    formData.selectedPermissions.includes(p)
  )
  return selectedInCategory.length === categoryPermissions.length
}

// 获取分类半选状态
const getCategoryIndeterminate = (category: PermissionCategory) => {
  const categoryPermissions = getCategoryPermissions(category).map(p => p.value)
  if (categoryPermissions.length === 0) return false
  
  const selectedInCategory = categoryPermissions.filter(p => 
    formData.selectedPermissions.includes(p)
  )
  return selectedInCategory.length > 0 && selectedInCategory.length < categoryPermissions.length
}

// 处理分类选择变化
const handleCategoryChange = (category: PermissionCategory, checked: boolean) => {
  const categoryPermissions = getCategoryPermissions(category).map(p => p.value)
  
  if (checked) {
    // 选中分类：添加该分类下所有权限
    const newPermissions = categoryPermissions.filter(p => 
      !formData.selectedPermissions.includes(p)
    )
    formData.selectedPermissions.push(...newPermissions)
  } else {
    // 取消选中分类：移除该分类下所有权限
    formData.selectedPermissions = formData.selectedPermissions.filter(p => 
      !categoryPermissions.includes(p)
    )
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 提交撤销
const handleSubmit = async () => {
  if (!props.landlord) return
  
  try {
    // 表单验证
    await formRef.value?.validate()
    
    // 二次确认
    await ElMessageBox.confirm(
      `确认撤销房东 "${props.landlord.nickname}" 的 ${formData.selectedPermissions.length} 个权限吗？此操作不可撤销。`,
      '确认撤销权限',
      {
        type: 'warning',
        confirmButtonText: '确认撤销',
        cancelButtonText: '取消'
      }
    )
    
    loading.value = true
    
    // 准备请求参数
    const params: RevokePermissionsParams = {
      landlordId: props.landlord.landlordId,
      permissions: formData.selectedPermissions,
      reason: formData.reason
    }
    
    // 调用API
    await landlordPermissionApi.revokePermissions(params)
    
    ElMessage.success('权限撤销成功')
    
    // 触发成功事件
    emit('success')
    
    // 关闭对话框
    handleClose()
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('撤销权限失败:', error)
      ElMessage.error(error.message || '撤销权限失败')
    }
  } finally {
    loading.value = false
  }
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
</script>

<style scoped lang="scss">
.revoke-permission {
  .landlord-info-card {
    margin-bottom: 20px;
    
    .landlord-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .info {
        flex: 1;
        
        .name {
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
  }

  .permission-form {
    margin-bottom: 16px;
    
    .permission-selection {
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      padding: 12px;
      max-height: 400px;
      overflow-y: auto;
      
      .empty-permissions {
        text-align: center;
        padding: 20px;
      }
      
      .permission-checkboxes {
        .permission-categories {
          .category-section {
            margin-bottom: 16px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            .category-header {
              margin-bottom: 8px;
              padding-bottom: 8px;
              border-bottom: 1px solid #f0f0f0;
              
              :deep(.el-checkbox) {
                font-weight: 600;
                color: #303133;
              }
            }
            
            .category-permissions {
              padding-left: 20px;
              
              :deep(.el-checkbox) {
                display: block;
                margin-bottom: 8px;
                margin-right: 0;
              }
            }
          }
        }
      }
    }
  }

  .warning-alert {
    margin-bottom: 0;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>