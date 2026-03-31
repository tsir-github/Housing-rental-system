/**
 * 地区信息相关API
 */
import http from '@/utils/http';

// 地区信息类型定义
export interface ProvinceInfo {
  id: number;
  name: string;
}

export interface CityInfo {
  id: number;
  name: string;
  provinceId: number;
}

export interface DistrictInfo {
  id: number;
  name: string;
  cityId: number;
}

// 地区选择器数据格式
export interface AreaListData {
  province_list: Record<string, string>;
  city_list: Record<string, string>;
  county_list: Record<string, string>;
}

/**
 * 获取省份列表 - 使用公共接口
 */
export const getProvinceList = () => {
  return http.get<ProvinceInfo[]>('/app/region/province/list');
};

/**
 * 根据省份ID获取城市列表 - 使用公共接口
 */
export const getCityListByProvinceId = (provinceId: number) => {
  console.log('调用getCityListByProvinceId，参数:', provinceId);
  return http.get<CityInfo[]>(`/app/region/city/listByProvinceId?id=${provinceId}`);
};

/**
 * 根据城市ID获取区县列表 - 使用公共接口
 */
export const getDistrictListByCityId = (cityId: number) => {
  console.log('调用getDistrictListByCityId，参数:', cityId);
  return http.get<DistrictInfo[]>(`/app/region/district/listByCityId?id=${cityId}`);
};

/**
 * 构建地区选择器数据格式 - 按需加载，不预加载所有数据
 */
export const buildAreaListData = async (): Promise<AreaListData> => {
  try {
    console.log('开始构建地区数据...');
    
    // 只获取省份数据，城市和区县数据按需加载
    const provinceRes = await getProvinceList();
    console.log('省份数据加载成功:', provinceRes.data.length, '个省份');

    const areaListData: AreaListData = {
      province_list: {},
      city_list: {},
      county_list: {}
    };

    // 只构建省份数据
    provinceRes.data.forEach((province: ProvinceInfo) => {
      areaListData.province_list[String(province.id)] = province.name;
    });

    console.log('地区数据构建完成 - 仅加载省份数据，城市和区县数据将按需加载');
    
    return areaListData;
  } catch (error) {
    console.error('构建地区数据失败，使用备用数据:', error);
    // 返回备用数据，确保基本功能可用
    return {
      province_list: {
        '11': '北京市',
        '12': '天津市',
        '22': '吉林省',
        '31': '上海市',
        '44': '广东省'
      },
      city_list: {},
      county_list: {}
    };
  }
};

/**
 * 动态加载城市数据
 */
export const loadCityData = async (provinceId: number, areaList: AreaListData) => {
  try {
    const cityRes = await getCityListByProvinceId(provinceId);
    const provinceName = areaList.province_list[String(provinceId)];
    
    cityRes.data.forEach((city: CityInfo) => {
      // 正确处理直辖市和普通省份的显示
      if (city.name === '市辖区') {
        // 直辖市：显示省份名称（如：北京市）
        areaList.city_list[String(city.id)] = provinceName;
      } else {
        // 普通省份：显示城市名称（如：长春市、吉林市）
        areaList.city_list[String(city.id)] = city.name;
      }
    });
    
    return cityRes.data;
  } catch (error) {
    console.error(`加载省份${provinceId}的城市数据失败:`, error);
    throw error;
  }
};

/**
 * 动态加载区县数据
 */
export const loadDistrictData = async (cityId: number, areaList: AreaListData) => {
  try {
    const districtRes = await getDistrictListByCityId(cityId);
    
    districtRes.data.forEach((district: DistrictInfo) => {
      areaList.county_list[String(district.id)] = district.name;
    });
    
    return districtRes.data;
  } catch (error) {
    console.error(`加载城市${cityId}的区县数据失败:`, error);
    throw error;
  }
};

/**
 * 简化版地区数据构建 - 用于测试和临时使用
 */
export const buildSimpleAreaListData = (): AreaListData => {
  return {
    province_list: {
      '11': '北京市',
      '12': '天津市',
      '22': '吉林省',
      '31': '上海市',
      '44': '广东省'
    },
    city_list: {
      '1101': '北京市',
      '1201': '天津市',
      '2201': '长春市',
      '2202': '吉林市',
      '3101': '上海市',
      '4401': '广州市'
    },
    county_list: {
      '110101': '东城区',
      '110102': '西城区',
      '110105': '朝阳区',
      '110106': '丰台区',
      '220102': '南关区',
      '220103': '宽城区',
      '220104': '朝阳区',
      '220105': '二道区'
    }
  };
};