/**
 * 公寓信息API - 使用官方管理接口
 */
import http from '@/utils/http';

// 公寓基本信息
export interface ApartmentInfo {
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
}

/**
 * 根据区县ID获取公寓列表 - 使用官方管理接口
 */
export const getApartmentsByDistrictId = (districtId: number) => {
  return http.get<ApartmentInfo[]>(`/admin/apartment/listInfoByDistrictId?id=${districtId}`);
};