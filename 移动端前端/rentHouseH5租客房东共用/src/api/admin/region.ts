/**
 * 管理端地区信息API - 用于租约管理五级联动
 */
import http from '@/utils/http';

// 地区信息类型定义
export interface AdminProvinceInfo {
    id: number;
    name: string;
}

export interface AdminCityInfo {
    id: number;
    name: string;
    provinceId: number;
}

export interface AdminDistrictInfo {
    id: number;
    name: string;
    cityId: number;
}

/**
 * 获取省份列表 - 房东端接口
 */
export const getAdminProvinceList = () => {
    return http.get<AdminProvinceInfo[]>('/app/landlord/region/province/list');
};

/**
 * 根据省份ID获取城市列表 - 房东端接口
 */
export const getAdminCityListByProvinceId = (provinceId: number) => {
    return http.get<AdminCityInfo[]>(`/app/landlord/region/city/listByProvinceId?id=${provinceId}`);
};

/**
 * 根据城市ID获取区县列表 - 房东端接口
 */
export const getAdminDistrictListByCityId = (cityId: number) => {
    return http.get<AdminDistrictInfo[]>(`/app/landlord/region/district/listByCityId?id=${cityId}`);
};