<template>
  <div class="room-availability-checker">
    <!-- 这个组件主要提供逻辑，不渲染UI -->
  </div>
</template>

<script setup lang="ts">
import { showDialog, showToast } from 'vant';
import { checkRoomAvailability } from '@/api/tenant/directMoveIn';
import { checkRoomAvailabilityForAppointment } from '@/api/tenant/appointment';
import type { RoomAvailabilityVo } from '@/api/tenant/directMoveIn';

interface Props {
  roomId: number;
  checkType: 'directMoveIn' | 'appointment';
}

interface Emits {
  (e: 'success'): void;
  (e: 'failed', availability: RoomAvailabilityVo): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

// 显示友好提示对话框
const showAvailabilityDialog = (availability: RoomAvailabilityVo): Promise<void> => {
  const { message, reason, statusCode } = availability;
  
  return showDialog({
    title: message,
    message: reason,
    confirmButtonText: '我知道了',
    confirmButtonColor: '#1989fa',
    className: 'availability-dialog'
  });
};

// 显示继续操作确认对话框
const showContinueDialog = (type: 'appointment' | 'directMoveIn'): Promise<void> => {
  const actionText = type === 'appointment' ? '预约看房' : '直接入住';
  
  return showDialog({
    title: `仍要${actionText}吗？`,
    message: `虽然房源当前不可用，但您仍可以${actionText}了解详情，或关注房源状态变化。`,
    confirmButtonText: `继续${actionText}`,
    cancelButtonText: '暂不操作',
    confirmButtonColor: '#1989fa'
  });
};

// 检查房源可用性
const checkAvailability = async (): Promise<boolean> => {
  try {
    let availability: RoomAvailabilityVo;
    
    if (props.checkType === 'directMoveIn') {
      const { data } = await checkRoomAvailability(props.roomId);
      availability = data;
    } else {
      const { data } = await checkRoomAvailabilityForAppointment(props.roomId);
      availability = data;
    }
    
    if (!availability.available) {
      // 显示友好提示
      await showAvailabilityDialog(availability);
      
      // 对于预约看房，某些状态下仍然可以继续
      if (props.checkType === 'appointment' && availability.statusCode === 'OCCUPIED') {
        try {
          await showContinueDialog('appointment');
          emit('success');
          return true;
        } catch {
          // 用户取消操作
          emit('failed', availability);
          return false;
        }
      }
      
      // 对于其他不可用状态，不允许继续
      emit('failed', availability);
      return false;
    }
    
    // 房源可用
    emit('success');
    return true;
    
  } catch (error) {
    console.error('检查房源可用性失败:', error);
    showToast('检查房源状态失败，请重试');
    return false;
  }
};

// 暴露方法给父组件
defineExpose({
  checkAvailability
});
</script>

<style scoped lang="less">
.room-availability-checker {
  display: none;
}

// 友好提示对话框样式
:deep(.availability-dialog) {
  .van-dialog__header {
    padding: 20px 20px 10px;
    font-size: 18px;
    font-weight: 600;
  }
  
  .van-dialog__content {
    padding: 10px 20px 20px;
    font-size: 14px;
    line-height: 1.6;
    color: #646566;
  }
  
  .van-dialog__confirm {
    border-radius: 6px;
    font-weight: 500;
  }
}
</style>