/**
 * 房东端公寓信息API - 用于租约管理五级联动
 */
import http from '@/utils/http';

// 房东端公寓基本信息
export interface AdminApartmentInfo {
  id: number;
  name: string;
  introduction: string;
  districtId: number;
  districtName: string;
  cityId: number;
  cityName: string;
  provinceId: number;
  provinceName: string;
  addressDetail: string;
  latitude: string;
  longitude: string;
  phone: string;
  isRelease: number;
  landlordId: number;
}

/**
 * 获取房东的公寓列表 - 根据区县ID过滤（使用自动Token认证）
 */
export const getAdminApartmentsByDistrictId = async (districtId: number) => {
  try {
    // 调用新的自动认证接口，后端通过Token自动获取房东ID
    const response = await http.get<AdminApartmentInfo[]>(`/app/landlord/apartment/listMyApartmentsAuto`);
    
    // 在前端过滤指定区县的公寓
    const filteredApartments = response.data.filter(apartment => apartment.districtId === districtId);
    
    return {
      ...response,
      data: filteredApartments
    };
  } catch (error) {
    console.error('获取房东公寓列表失败:', error);
    throw error;
  }
};