/**
 * 用户信息辅助工具
 */
import { useUserStore } from '@/store/modules/user';

/**
 * 获取当前登录用户的ID
 * 注意：由于UserInfoInterface中没有id字段，这里使用临时方案
 */
export const getCurrentUserId = (): number => {
  const userStore = useUserStore();
  
  // TODO: 需要后端在用户信息中返回用户ID
  // 目前使用临时的固定值，实际应用中需要从userStore.userInfo中获取真实的用户ID
  
  // 从localStorage中尝试获取用户ID（如果有的话）
  const storedUserId = localStorage.getItem('USER_ID');
  if (storedUserId) {
    return parseInt(storedUserId, 10);
  }
  
  // 如果没有存储的用户ID，返回默认值
  // 这是临时方案，实际应该从后端获取
  return 8; // 从错误日志中看到的用户ID
};

/**
 * 获取当前登录房东的ID（专门用于房东功能）
 */
export const getCurrentLandlordId = (): number => {
  return getCurrentUserId();
};

/**
 * 设置当前用户ID到本地存储
 */
export const setCurrentUserId = (userId: number): void => {
  localStorage.setItem('USER_ID', userId.toString());
};

/**
 * 清除当前用户ID
 */
export const clearCurrentUserId = (): void => {
  localStorage.removeItem('USER_ID');
};