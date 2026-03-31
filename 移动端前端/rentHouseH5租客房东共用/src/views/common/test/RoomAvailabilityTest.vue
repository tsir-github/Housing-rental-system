<template>
  <div class="test-page">
    <van-nav-bar title="房源可用性测试" left-arrow @click-left="$router.go(-1)" />
    
    <div class="main-container">
      <van-cell-group title="功能测试">
        <van-cell title="测试房源ID" :value="testRoomId" />
        <van-field
          v-model="testRoomId"
          label="房源ID"
          placeholder="请输入房源ID"
          type="number"
        />
      </van-cell-group>

      <div class="button-group">
        <van-button 
          type="primary" 
          block 
          @click="testDirectMoveIn"
          :loading="directMoveInLoading"
        >
          测试直接入住检查
        </van-button>
        
        <van-button 
          type="default" 
          block 
          @click="testAppointment"
          :loading="appointmentLoading"
        >
          测试预约看房检查
        </van-button>
      </div>

      <van-cell-group title="测试结果">
        <van-cell 
          v-if="lastResult"
          :title="lastResult.type"
          :value="lastResult.available ? '可用' : '不可用'"
          :label="lastResult.message"
        />
      </van-cell-group>

      <van-cell-group title="模拟不同状态">
        <van-button 
          v-for="status in mockStatuses"
          :key="status.code"
          :type="status.buttonType"
          size="small"
          @click="showMockDialog(status)"
          class="mock-button"
        >
          {{ status.name }}
        </van-button>
      </van-cell-group>

      <van-cell-group title="使用说明">
        <van-cell>
          <div class="instructions">
            <p>1. 输入房源ID进行真实测试</p>
            <p>2. 点击"模拟不同状态"按钮查看各种提示效果</p>
            <p>3. 在房源详情页面点击按钮时会自动进行检查</p>
            <p>4. 房源列表中会显示实时可用状态</p>
          </div>
        </van-cell>
      </van-cell-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { showToast } from 'vant';
import { 
  checkDirectMoveInAvailability, 
  checkAppointmentAvailability,
  showAvailabilityDialog 
} from '@/utils/roomAvailabilityHelper';
import type { RoomAvailabilityVo } from '@/api/tenant/directMoveIn';

// 测试房源ID
const testRoomId = ref('1');

// 加载状态
const directMoveInLoading = ref(false);
const appointmentLoading = ref(false);

// 最后一次测试结果
const lastResult = ref<{
  type: string;
  available: boolean;
  message: string;
} | null>(null);

// 模拟状态数据
const mockStatuses = [
  {
    code: 'AVAILABLE',
    name: '房源可用',
    buttonType: 'success',
    data: {
      available: true,
      statusCode: 'AVAILABLE',
      message: '房源可预约',
      reason: '房源当前空闲，可以预约看房或直接入住'
    }
  },
  {
    code: 'OCCUPIED',
    name: '房源已占用',
    buttonType: 'danger',
    data: {
      available: false,
      statusCode: 'OCCUPIED',
      message: '房源已出租',
      reason: '此房源已有租客入住，暂时无法预约'
    }
  },
  {
    code: 'NOT_RELEASED',
    name: '房源未发布',
    buttonType: 'default',
    data: {
      available: false,
      statusCode: 'NOT_RELEASED',
      message: '房源暂未开放',
      reason: '房东暂时未开放此房源，请关注其他房源'
    }
  },
  {
    code: 'NOT_APPROVED',
    name: '房源审核中',
    buttonType: 'warning',
    data: {
      available: false,
      statusCode: 'NOT_APPROVED',
      message: '房源审核中',
      reason: '房源正在审核中，暂时无法预约'
    }
  }
];

// 测试直接入住
const testDirectMoveIn = async () => {
  if (!testRoomId.value) {
    showToast('请输入房源ID');
    return;
  }

  directMoveInLoading.value = true;
  try {
    const isAvailable = await checkDirectMoveInAvailability(Number(testRoomId.value));
    lastResult.value = {
      type: '直接入住检查',
      available: isAvailable,
      message: isAvailable ? '可以申请直接入住' : '不能申请直接入住'
    };
    
    if (isAvailable) {
      showToast('房源可用，可以申请直接入住');
    }
  } catch (error) {
    showToast('测试失败，请检查房源ID');
  } finally {
    directMoveInLoading.value = false;
  }
};

// 测试预约看房
const testAppointment = async () => {
  if (!testRoomId.value) {
    showToast('请输入房源ID');
    return;
  }

  appointmentLoading.value = true;
  try {
    const isAvailable = await checkAppointmentAvailability(Number(testRoomId.value));
    lastResult.value = {
      type: '预约看房检查',
      available: isAvailable,
      message: isAvailable ? '可以预约看房' : '不能预约看房'
    };
    
    if (isAvailable) {
      showToast('房源可用，可以预约看房');
    }
  } catch (error) {
    showToast('测试失败，请检查房源ID');
  } finally {
    appointmentLoading.value = false;
  }
};

// 显示模拟对话框
const showMockDialog = (status: any) => {
  showAvailabilityDialog(status.data as RoomAvailabilityVo);
};
</script>

<style scoped lang="less">
.test-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.button-group {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mock-button {
  margin: 4px;
  flex: 1;
}

.instructions {
  font-size: 14px;
  line-height: 1.6;
  color: #646566;
  
  p {
    margin: 8px 0;
  }
}

.van-cell-group {
  margin: 16px 0;
}
</style>