/**
 * 通用房间信息API
 */
import http from '@/utils/http';

// 房间基本信息
export interface RoomItemVo {
  id: number;
  roomNumber: string;
  rent: number;
  apartmentId: number;
  apartmentName: string;
  isRelease: number;
  createTime: string;
  updateTime: string;
}

// 房间信息（用于五级联动）
export interface RoomInfo {
  id: number;
  roomNumber: string;
  rent: number;
  apartmentId: number;
  isRelease: number;
}

// 分页结果
export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * 根据公寓ID分页查询房间列表
 */
export const getRoomsByApartmentId = (
  current: number = 1,
  size: number = 100,
  apartmentId: number
) => {
  return http.get<PageResult<RoomItemVo>>(
    `/app/room/pageItemByApartmentId?current=${current}&size=${size}&id=${apartmentId}`
  );
};

/**
 * 根据公寓ID获取所有房间列表（不分页）
 */
export const getAllRoomsByApartmentId = async (apartmentId: number) => {
  const response = await getRoomsByApartmentId(1, 1000, apartmentId);
  return response.data.records;
};
/*
*
 * 根据公寓ID获取房间列表（用于五级联动） - 使用官方管理接口
 */
export const getRoomsByApartmentIdForSelect = (apartmentId: number) => {
  return http.get<RoomInfo[]>(`/admin/room/listBasicByApartmentId?id=${apartmentId}`);
};