<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="500px"
    :before-close="handleClose"
  >
    <div v-if="application" class="process-form">
      <!-- 申请基本信息 -->
      <div class="application-info">
        <h4>申请信息</h4>
        <div class="info-row">
          <span class="label">申请人：</span>
          <span>{{ application.name }}</span>
        </div>
        <div class="info-row">
          <span class="label">房间：</span>
          <span>{{ application.apartmentName }} - {{ application.roomNumber }}</span>
        </div>
        <div class="info-row">
          <span class="label">租金：</span>
          <span class="price">¥{{ application.rent }}/月</span>
        </div>
        <div class="info-row">
          <span class="label">租期：</span>
          <span>{{ application.leaseStartDate }} 至 {{ application.leaseEndDate }}</span>
        </div>
      </div>

      <!-- 处理表单 -->
      <el-form 
        ref="formRef" 
        :model="formData" 
        :rules="formRules" 
        label-width="100px"
        class="process-form-content"
      >
        <el-form-item label="处理人ID" prop="processorId">
          <el-input 
            v-model.number="formData.processorId" 
            placeholder="请输入处理人ID"
            type="number"
          />
          <div class="form-tip">当前登录用户的ID</div>
        </el-form-item>

        <el-form-item label="处理人类型" prop="processorType">
          <el-select v-model="formData.processorType" placeholder="请选择处理人类型">
            <el-option label="官方人员" :value="1" />
            <el-option label="房东" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item 
          v-if="action === 'reject'" 
          label="拒绝原因" 
          prop="rejectionReason"
        >
          <el-input
            v-model="formData.rejectionReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <!-- 操作提示 -->
      <div class="action-tip">
        <el-alert
          :title="actionTipTitle"
          :description="actionTipDescription"
          :type="action === 'approve' ? 'success' : 'warning'"
          show-icon
          :closable="false"
        />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          :type="action === 'approve' ? 'success' : 'danger'"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ action === 'approve' ? '确认通过' : '确认拒绝' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { directMoveInApplicationApi } from '@/api/applicationManagement'
import type { DirectMoveInApplication } from '@/api/applicationManagement/types'

interface Props {
  visible: boolean
  application: DirectMoveInApplication | null
  action: 'approve' | 'reject'
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive({
  processorId: 1, // 默认值，实际应该从当前登录用户获取
  processorType: 1, // 默认官方人员
  rejectionReason: ''
})

const formRules: FormRules = {
  processorId: [
    { required: true, message: '请输入处理人ID', trigger: 'blur' },
    { type: 'number', message: '处理人ID必须是数字', trigger: 'blur' }
  ],
  processorType: [
    { required: true, message: '请选择处理人类型', trigger: 'change' }
  ],
  rejectionReason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' },
    { min: 5, max: 500, message: '拒绝原因长度在 5 到 500 个字符', trigger: 'blur' }
  ]
}

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const dialogTitle = computed(() => {
  return props.action === 'approve' ? '通过申请' : '拒绝申请'
})

const actionTipTitle = computed(() => {
  return props.action === 'approve' ? '通过申请' : '拒绝申请'
})

const actionTipDescription = computed(() => {
  if (props.action === 'approve') {
    return '通过申请后，系统将自动生成租约合同，状态为"待签约"。请确认所有信息无误后操作。'
  } else {
    return '拒绝申请后，该申请将无法再次处理。请填写详细的拒绝原因。'
  }
})

// 监听对话框打开，重置表单
watch(() => props.visible, (visible) => {
  if (visible) {
    resetForm()
  }
})

const resetForm = () => {
  formData.processorId = 1 // 实际应该从当前登录用户获取
  formData.processorType = 1
  formData.rejectionReason = ''
  formRef.value?.clearValidate()
}

const handleClose = () => {
  dialogVisible.value = false
}

const handleSubmit = async () => {
  if (!props.application) return

  try {
    await formRef.value?.validate()
    
    loading.value = true

    if (props.action === 'approve') {
      // 通过申请
      await directMoveInApplicationApi.processApplication(props.application.id, {
        processorId: formData.processorId,
        processorType: formData.processorType,
        approved: true
      })
      ElMessage.success('申请已通过，租约已自动生成')
    } else {
      // 拒绝申请
      await directMoveInApplicationApi.rejectApplication(props.application.id, {
        processorId: formData.processorId,
        processorType: formData.processorType,
        rejectionReason: formData.rejectionReason
      })
      ElMessage.success('申请已拒绝')
    }

    emit('success')
    handleClose()
  } catch (error) {
    console.error('处理申请失败:', error)
    ElMessage.error('处理申请失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.process-form {
  .application-info {
    background-color: #f8f9fa;
    border-radius: 6px;
    padding: 16px;
    margin-bottom: 20px;

    h4 {
      margin: 0 0 12px 0;
      color: #303133;
      font-size: 14px;
      font-weight: 600;
    }

    .info-row {
      display: flex;
      align-items: center;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        font-weight: 500;
        color: #606266;
        min-width: 60px;
        margin-right: 8px;
      }

      .price {
        color: #f56c6c;
        font-weight: 600;
      }
    }
  }

  .process-form-content {
    margin-bottom: 20px;

    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }

  .action-tip {
    margin-bottom: 20px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>