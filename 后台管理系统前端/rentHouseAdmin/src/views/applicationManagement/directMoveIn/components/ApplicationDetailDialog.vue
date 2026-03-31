<template>
  <el-dialog
    v-model="dialogVisible"
    title="申请详情"
    width="800px"
    :before-close="handleClose"
  >
    <div v-if="application" class="application-detail">
      <!-- 基本信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-tag 
              :type="getStatusTagType(application.status)"
              size="large"
            >
              {{ getStatusText(application.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>申请ID：</label>
              <span>{{ application.id }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>申请人：</label>
              <span>{{ application.name }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>手机号：</label>
              <span>{{ application.phone }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>身份证号：</label>
              <span>{{ application.identificationNumber }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>邮箱：</label>
              <span>{{ application.email || '未填写' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>申请时间：</label>
              <span>{{ application.createTime }}</span>
            </div>
          </el-col>
        </el-row>

        <!-- 紧急联系人信息 -->
        <el-divider content-position="left">紧急联系人</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <label>联系人：</label>
              <span>{{ application.emergencyContact || '未填写' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>联系电话：</label>
              <span>{{ application.emergencyPhone || '未填写' }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 房源信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span>房源信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>公寓名称：</label>
              <span>{{ application.apartmentName }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>房间号：</label>
              <span>{{ application.roomNumber }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>房源类型：</label>
              <el-tag 
                :type="application.publisherType === 1 ? 'primary' : 'success'"
                size="small"
              >
                {{ application.publisherTypeName }}
              </el-tag>
            </div>
          </el-col>
        </el-row>

        <!-- 房东信息（仅房东房源显示） -->
        <template v-if="application.landlordName">
          <el-divider content-position="left">房东信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>房东姓名：</label>
                <span>{{ application.landlordName }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>房东电话：</label>
                <span>{{ application.landlordPhone }}</span>
              </div>
            </el-col>
          </el-row>
        </template>
      </el-card>

      <!-- 租约信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span>租约信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>租期开始：</label>
              <span>{{ application.leaseStartDate }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>租期结束：</label>
              <span>{{ application.leaseEndDate }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>租期类型：</label>
              <span>{{ application.leaseTermName }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>月租金：</label>
              <span class="price">¥{{ application.rent }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>押金：</label>
              <span class="price">¥{{ application.deposit }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>支付方式：</label>
              <span>{{ application.paymentTypeName }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 处理信息 -->
      <el-card v-if="application.processorName || application.rejectionReason" class="info-card" shadow="never">
        <template #header>
          <span>处理信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8" v-if="application.processorName">
            <div class="info-item">
              <label>处理人：</label>
              <span>{{ application.processorName }}</span>
            </div>
          </el-col>
          <el-col :span="8" v-if="application.processorType">
            <div class="info-item">
              <label>处理人类型：</label>
              <el-tag 
                :type="application.processorType === 1 ? 'primary' : 'success'"
                size="small"
              >
                {{ application.processorType === 1 ? '官方人员' : '房东' }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="8" v-if="application.processedAt">
            <div class="info-item">
              <label>处理时间：</label>
              <span>{{ application.processedAt }}</span>
            </div>
          </el-col>
        </el-row>

        <!-- 拒绝原因 -->
        <div v-if="application.rejectionReason" class="rejection-reason">
          <el-divider content-position="left">拒绝原因</el-divider>
          <div class="reason-content">
            {{ application.rejectionReason }}
          </div>
        </div>
      </el-card>

      <!-- 备注信息 -->
      <el-card v-if="application.additionalInfo" class="info-card" shadow="never">
        <template #header>
          <span>备注信息</span>
        </template>
        <div class="additional-info">
          {{ application.additionalInfo }}
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button 
          v-if="application && application.status === 1"
          type="success" 
          @click="handleApprove"
        >
          通过申请
        </el-button>
        <el-button 
          v-if="application && application.status === 1"
          type="danger" 
          @click="handleReject"
        >
          拒绝申请
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DirectMoveInApplication } from '@/api/applicationManagement/types'
import { DirectMoveInStatus } from '@/enums/constEnums'

interface Props {
  visible: boolean
  application: DirectMoveInApplication | null
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'approve', application: DirectMoveInApplication): void
  (e: 'reject', application: DirectMoveInApplication): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const handleClose = () => {
  dialogVisible.value = false
}

const handleApprove = () => {
  if (props.application) {
    emit('approve', props.application)
    handleClose()
  }
}

const handleReject = () => {
  if (props.application) {
    emit('reject', props.application)
    handleClose()
  }
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
</script>

<style scoped lang="scss">
.application-detail {
  .info-card {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;
    }
  }

  .info-item {
    margin-bottom: 12px;
    display: flex;
    align-items: center;

    label {
      font-weight: 500;
      color: #606266;
      min-width: 80px;
      margin-right: 8px;
    }

    span {
      color: #303133;
    }
  }

  .price {
    color: #f56c6c;
    font-weight: 600;
  }

  .rejection-reason {
    .reason-content {
      background-color: #fef0f0;
      border: 1px solid #fbc4c4;
      border-radius: 4px;
      padding: 12px;
      color: #f56c6c;
      line-height: 1.5;
    }
  }

  .additional-info {
    background-color: #f4f4f5;
    border-radius: 4px;
    padding: 12px;
    color: #606266;
    line-height: 1.5;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>