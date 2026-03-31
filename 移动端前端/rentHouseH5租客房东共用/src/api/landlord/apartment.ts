import http from "@/utils/http";
import type {
  ApiResponse,
  PageResult,
  ApartmentItemVo,
  ApartmentDetailVo,
  ApartmentSubmitVo,
  ApartmentQueryVo,
  ApartmentStatistics,
  ReleaseStatus
} from "@/types/landlord";

/**
 * 房东公寓管理API
 * 基于后端接口规范实现
 */

/**
 * 获取房东公寓列表
 * GET /app/landlord/apartment/pageItem
 */
export function getMyApartments(params: {
  current: number;
  size: number;
  landlordId: number;
  queryVo?: ApartmentQueryVo;
}) {
  // 构建查询字符串 - 所有参数都作为Query Parameters
  const queryParams: string[] = [
    `current=${params.current}`,
    `size=${params.size}`,
    `landlordId=${params.landlordId}`
  ];

  // 展开queryVo的属性到Query参数
  if (params.queryVo) {
    if (params.queryVo.name) {
      queryParams.push(`name=${encodeURIComponent(params.queryVo.name)}`);
    }
    if (params.queryVo.provinceId) {
      queryParams.push(`provinceId=${params.queryVo.provinceId}`);
    }
    if (params.queryVo.cityId) {
      queryParams.push(`cityId=${params.queryVo.cityId}`);
    }
    if (params.queryVo.districtId) {
      queryParams.push(`districtId=${params.queryVo.districtId}`);
    }
    if (params.queryVo.isRelease !== undefined && params.queryVo.isRelease !== null) {
      queryParams.push(`isRelease=${params.queryVo.isRelease}`);
    }
  }

  const url = `/app/landlord/apartment/pageItem?${queryParams.join('&')}`;
  console.log('公寓列表API调用:', url);

  return http.get<PageResult<ApartmentItemVo>>(url);
}

/**
 * 根据ID获取公寓详情
 * GET /app/landlord/apartment/getDetailById
 */
export function getApartmentDetail(apartmentId: number, landlordId: number) {
  const url = `/app/landlord/apartment/getDetailById?id=${apartmentId}&landlordId=${landlordId}`;
  console.log('公寓详情API调用:', url);

  return http.get<ApartmentDetailVo>(url);
}

/**
 * 获取公寓编辑详情（用于编辑回显）
 * GET /app/landlord/apartment/getDetailForEdit
 */
export function getApartmentDetailForEdit(apartmentId: number, landlordId: number) {
  const url = `/app/landlord/apartment/getDetailForEdit?id=${apartmentId}&landlordId=${landlordId}`;
  console.log('公寓编辑详情API调用:', url);

  return http.get<ApartmentDetailVo>(url);
}

/**
 * 获取房东可用的配套设施列表（用于编辑选择）
 * GET /app/landlord/apartment/facilities
 */
export function getAvailableFacilities(landlordId: number) {
  const url = `/app/landlord/apartment/facilities?landlordId=${landlordId}`;
  console.log('可用配套设施API调用:', url);

  return http.get<any[]>(url);
}

/**
 * 获取房东可用的标签列表（用于编辑选择）
 * GET /app/landlord/apartment/labels
 */
export function getAvailableLabels(landlordId: number) {
  const url = `/app/landlord/apartment/labels?landlordId=${landlordId}`;
  console.log('可用标签API调用:', url);

  return http.get<any[]>(url);
}

/**
 * 创建公寓
 * POST /app/landlord/apartment/saveOrUpdate
 */
export function createApartment(apartmentData: ApartmentSubmitVo, landlordId: number) {
  const url = `/app/landlord/apartment/saveOrUpdate?landlordId=${landlordId}`;
  console.log('创建公寓API调用:', url, apartmentData);

  return http.post<number>(url, apartmentData);
}

/**
 * 更新公寓信息
 * POST /app/landlord/apartment/saveOrUpdate
 */
export function updateApartment(apartmentData: ApartmentSubmitVo, landlordId: number) {
  const url = `/app/landlord/apartment/saveOrUpdate?landlordId=${landlordId}`;
  console.log('更新公寓API调用:', url, apartmentData);

  return http.post<void>(url, apartmentData);
}

/**
 * 删除公寓
 * DELETE /app/landlord/apartment/removeById
 */
export function deleteApartment(apartmentId: number, landlordId: number) {
  const url = `/app/landlord/apartment/removeById?id=${apartmentId}&landlordId=${landlordId}`;
  console.log('删除公寓API调用:', url);

  return http.delete<void>(url);
}

/**
 * 修改公寓发布状态
 * POST /app/landlord/apartment/updateReleaseStatusById
 */
export function updateApartmentReleaseStatus(
  apartmentId: number,
  status: ReleaseStatus,
  landlordId: number
) {
  const url = `/app/landlord/apartment/updateReleaseStatusById?id=${apartmentId}&status=${status}&landlordId=${landlordId}`;
  console.log('更新发布状态API调用:', url);

  return http.post<void>(url);
}

/**
 * 获取公寓统计信息
 * GET /app/landlord/apartment/statistics
 */
export function getApartmentStatistics(landlordId: number) {
  const url = `/app/landlord/apartment/statistics?landlordId=${landlordId}`;
  console.log('公寓统计API调用:', url);

  return http.get<ApartmentStatistics>(url);
}

/**
 * 获取房东公寓基础信息列表
 * GET /app/landlord/apartment/listMyApartments
 */
export function listMyApartments(landlordId: number) {
  const url = `/app/landlord/apartment/listMyApartments?landlordId=${landlordId}`;
  console.log('公寓基础列表API调用:', url);

  return http.get<ApartmentItemVo[]>(url);
}