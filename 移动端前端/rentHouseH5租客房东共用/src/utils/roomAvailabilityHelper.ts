import { showDialog, showToast } from 'vant';
import { checkRoomAvailability } from '@/api/tenant/directMoveIn';
import { checkRoomAvailabilityForAppointment } from '@/api/tenant/appointment';
import type { RoomAvailabilityVo } from '@/api/tenant/directMoveIn';

/**
 * 显示房源可用性友好提示对话框
 */
export const showAvailabilityDialog = (availability: RoomAvailabilityVo): Promise<void> => {
  const { message, reason } = availability;
  
  return showDialog({
    title: message,
    message: reason,
    confirmButtonText: '我知道了',
    confirmButtonColor: '#1989fa',
    className: 'availability-dialog'
  });
};

/**
 * 显示继续操作确认对话框
 */
export const showContinueDialog = (actionType: 'appointment' | 'directMoveIn'): Promise<void> => {
  const actionText = actionType === 'appointment' ? '预约看房' : '直接入住';
  
  return showDialog({
    title: `仍要${actionText}吗？`,
    message: `虽然房源当前不可用，但您仍可以${actionType === 'appointment' ? '预约看房了解详情' : '关注房源状态变化'}。`,
    confirmButtonText: `继续${actionText}`,
    cancelButtonText: '暂不操作',
    confirmButtonColor: '#1989fa'
  });
};

/**
 * 检查直接入住可用性
 */
export const checkDirectMoveInAvailability = async (roomId: number): Promise<boolean> => {
  try {
    const { data: availability } = await checkRoomAvailability(roomId);
    
    if (!availability.available) {
      await showAvailabilityDialog(availability);
      return false;
    }
    
    return true;
  } catch (error) {
    console.error('检查房源可用性失败:', error);
    showToast('检查房源状态失败，请重试');
    return false;
  }
};

/**
 * 检查预约看房可用性
 */
export const checkAppointmentAvailability = async (roomId: number): Promise<boolean> => {
  try {
    const { data: availability } = await checkRoomAvailabilityForAppointment(roomId);
    
    if (!availability.available) {
      await showAvailabilityDialog(availability);
      
      // 对于已被占用的房源，询问是否仍要预约
      if (availability.statusCode === 'OCCUPIED') {
        try {
          await showContinueDialog('appointment');
          return true;
        } catch {
          return false;
        }
      }
      
      // 对于其他不可用状态，不允许预约
      return false;
    }
    
    return true;
  } catch (error) {
    console.error('检查房源预约可用性失败:', error);
    showToast('检查房源状态失败，请重试');
    return false;
  }
};

/**
 * 获取状态对应的图标和颜色
 */
export const getStatusIconInfo = (statusCode: string) => {
  const iconMap: Record<string, { icon: string; color: string }> = {
    'OCCUPIED': { icon: 'lock', color: '#ee0a24' },
    'NOT_RELEASED': { icon: 'eye-o', color: '#646566' },
    'NOT_APPROVED': { icon: 'clock-o', color: '#1989fa' },
    'NOT_FOUND': { icon: 'warning-o', color: '#ff976a' },
    'ERROR': { icon: 'warning-o', color: '#ff976a' }
  };
  
  return iconMap[statusCode] || { icon: 'warning-o', color: '#ff976a' };
};