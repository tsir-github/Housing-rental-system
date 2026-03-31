/**
 * Store模块统一导出
 */

// 用户相关
export { useUserStore } from './user';
export { useCachedViewStoreHook } from './cachedView';
export { useDarkModeStore } from './darkMode';

// 房东相关
export { useLandlordStore } from './landlord';
export { useApartmentStore } from './apartment';
export { useRoomStore } from './room';
export { useApplicationStore } from './application';
export { useAppointmentStore } from './appointment';

// 导出类型
export type { LandlordState } from './landlord';
export type { ApartmentState } from './apartment';
export type { RoomState } from './room';
export type { ApplicationState } from './application';
export type { AppointmentState } from './appointment';