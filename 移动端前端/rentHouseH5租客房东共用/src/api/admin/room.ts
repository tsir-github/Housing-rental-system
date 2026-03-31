/**
 * 房东端房间信息API - 用于租约管理五级联动
 */
import http from '@/utils/http';

// 房东端房间信息（用于五级联动）
export interface AdminRoomInfo {
  id: number;
  roomNumber: string;
  rent: number;
  apartmentId: number;
  isRelease: number;
}

/**
 * 根据公寓ID获取房间列表（用于五级联动）- 使用自动Token认证
 */
export const getAdminRoomsByApartmentId = async (apartmentId: number) => {
  try {
    // 调用新的自动认证接口，后端通过Token自动获取房东ID
    const response = await http.get(`/app/landlord/room/my-rooms-auto?current=1&size=1000`);
    
    // 从分页结果中提取房间数据，并过滤指定公寓的房间
    const rooms = response.data.records || [];
    const filteredRooms = rooms.filter((room: any) => room.apartmentId === apartmentId);
    
    // 转换为需要的格式
    const adminRooms: AdminRoomInfo[] = filteredRooms.map((room: any) => ({
      id: room.id,
      roomNumber: room.roomNumber,
      rent: room.rent,
      apartmentId: room.apartmentId,
      isRelease: room.isRelease
    }));
    
    return {
      ...response,
      data: adminRooms
    };
  } catch (error) {
    console.error('获取房东房间列表失败:', error);
    throw error;
  }
};