# 房东移动端接口规范文档

## 1. 通用规范

### 1.1 基础信息
- **Base URL**: `/app/landlord`
- **认证方式**: JWT Token
- **请求头**: 
  ```
  Authorization: Bearer <token>
  Content-Type: application/json
  ```

### 1.2 统一响应格式
```typescript
interface ApiResponse<T> {
  code: number;        // 200=成功, 其他=失败
  message: string;     // 提示信息
  data: T;            // 实际数据
}
```

### 1.3 分页格式
```typescript
interface PageResult<T> {
  records: T[];        // 当前页数据
  total: number;       // 总记录数
  size: number;        // 每页大小
  current: number;     // 当前页码
  pages: number;       // 总页数
}
```

### 1.4 枚举值定义
```typescript
// 发布状态
enum ReleaseStatus {
  NOT_RELEASED = 0,    // 未发布
  RELEASED = 1         // 已发布
}

// 审核状态
enum ReviewStatus {
  PENDING = 1,         // 待审核
  APPROVED = 2,        // 审核通过
  REJECTED = 3         // 审核拒绝
}

// 发布者类型
enum PublisherType {
  LANDLORD = 1,        // 房东
  AGENT = 2           // 中介
}
```

### 1.5 错误码定义
```typescript
enum ErrorCode {
  SUCCESS = 200,              // 成功
  PARAM_ERROR = 400,          // 参数错误
  UNAUTHORIZED = 401,         // 未授权/token过期
  FORBIDDEN = 403,            // 权限不足
  NOT_FOUND = 404,            // 资源不存在
  SERVER_ERROR = 500          // 服务器错误
}
```

---

## 2. 公寓管理接口

### 2.1 分页查询公寓列表
```
GET /apartment/pageItem
```

**请求参数** (Query Parameters):
```typescript
interface ApartmentListParams {
  current: number;           // 必需，当前页码，从1开始
  size: number;             // 必需，每页大小，建议10-50
  landlordId: number;       // 必需，房东ID
  
  // 以下为ApartmentQueryVo的属性，直接作为Query参数
  name?: string;            // 可选，公寓名称模糊搜索
  provinceId?: number;      // 可选，省份ID
  cityId?: number;          // 可选，城市ID
  districtId?: number;      // 可选，区县ID
  isRelease?: 0 | 1;        // 可选，发布状态
}
```

**响应数据**:
```typescript
interface ApartmentItemVo {
  id: number;
  name: string;
  introduction: string;
  districtName: string;
  cityName: string;
  provinceName: string;
  addressDetail: string;
  phone: string;
  isRelease: 0 | 1;         // 发布状态
  roomCount: number;        // 房源数量
  availableRoomCount: number; // 可用房源数量
  createTime: string;       // 创建时间
  updateTime?: string;      // 更新时间
}

type Response = ApiResponse<PageResult<ApartmentItemVo>>;
```

**前端调用示例**:
```typescript
const url = `/app/landlord/apartment/pageItem?current=1&size=10&landlordId=8&isRelease=1`;
const response = await http.get<PageResult<ApartmentItemVo>>(url);
```

### 2.2 获取公寓详情
```
GET /apartment/getDetailById
```

**请求参数** (Query Parameters):
```typescript
interface ApartmentDetailParams {
  id: number;              // 必需，公寓ID
  landlordId: number;      // 必需，房东ID
}
```

**响应数据**:
```typescript
interface ApartmentDetailVo extends ApartmentItemVo {
  facilityInfoList: FacilityInfo[];
  labelInfoList: LabelInfo[];
  feeValueVoList: FeeValueVo[];
  graphVoList: GraphVo[];
  roomList: RoomInfo[];
}

type Response = ApiResponse<ApartmentDetailVo>;
```

### 2.3 创建/更新公寓
```
POST /apartment/saveOrUpdate
```

**请求参数** (Body + Query):
```typescript
// Query参数
interface SaveApartmentQuery {
  landlordId: number;      // 必需，房东ID
}

// Body参数
interface ApartmentSubmitVo {
  id?: number;             // 可选，更新时必需
  name: string;            // 必需，公寓名称
  introduction: string;    // 必需，公寓介绍
  districtId: number;      // 必需，区县ID
  cityId: number;          // 必需，城市ID
  provinceId: number;      // 必需，省份ID
  addressDetail: string;   // 必需，详细地址
  latitude: string;        // 必需，纬度
  longitude: string;       // 必需，经度
  phone: string;           // 必需，联系电话
  facilityInfoIds: number[]; // 可选，配套设施ID列表
  labelIds: number[];      // 可选，标签ID列表
  feeValueVoList: FeeValueVo[]; // 可选，费用信息
  graphVoList: GraphVo[];  // 可选，图片信息
}
```

**前端调用示例**:
```typescript
const response = await http.post<void>(
  '/app/landlord/apartment/saveOrUpdate',
  apartmentData,
  { params: { landlordId: 8 } }
);
```

### 2.4 删除公寓
```
DELETE /apartment/removeById
```

**请求参数** (Query Parameters):
```typescript
interface DeleteApartmentParams {
  id: number;              // 必需，公寓ID
  landlordId: number;      // 必需，房东ID
}
```

### 2.5 修改发布状态
```
POST /apartment/updateReleaseStatusById
```

**请求参数** (Query Parameters):
```typescript
interface UpdateReleaseStatusParams {
  id: number;              // 必需，公寓ID
  status: 0 | 1;          // 必需，发布状态
  landlordId: number;      // 必需，房东ID
}
```

### 2.6 获取公寓统计
```
GET /apartment/statistics
```

**请求参数** (Query Parameters):
```typescript
interface ApartmentStatsParams {
  landlordId: number;      // 必需，房东ID
}
```

**响应数据**:
```typescript
interface ApartmentStatistics {
  totalApartments: number;     // 总公寓数
  publishedApartments: number; // 已发布公寓数
  unpublishedApartments: number; // 未发布公寓数
  totalRooms: number;         // 总房源数
}

type Response = ApiResponse<ApartmentStatistics>;
```

---

## 3. 房源管理接口

### 3.1 查询房东房源列表
```
GET /room/my-rooms
```

**请求参数** (Query Parameters):
```typescript
interface RoomListParams {
  current: number;         // 必需，当前页码
  size: number;           // 必需，每页大小
  landlordId: number;     // 必需，房东ID
  
  // RoomQueryVo属性
  apartmentId?: number;   // 可选，公寓ID
  reviewStatus?: 1 | 2 | 3; // 可选，审核状态
  isRelease?: 0 | 1;      // 可选，发布状态
  minRent?: number;       // 可选，最低租金
  maxRent?: number;       // 可选，最高租金
}
```

**响应数据**:
```typescript
interface RoomItemVo {
  id: number;
  roomNumber: string;
  rent: number;
  apartmentId: number;
  apartmentName: string;
  isRelease: 0 | 1;
  reviewStatus: 1 | 2 | 3;
  complianceScore?: number;
  createTime: string;
  updateTime?: string;
  lastReviewedAt?: string;
  graphVoList?: GraphVo[];
}

type Response = ApiResponse<PageResult<RoomItemVo>>;
```

### 3.2 发布房源
```
POST /room/publish
```

**请求参数** (Body + Query):
```typescript
// Query参数
interface PublishRoomQuery {
  landlordId: number;      // 必需，房东ID
}

// Body参数
interface RoomSubmitVo {
  id?: number;             // 可选，更新时必需
  roomNumber: string;      // 必需，房间号
  rent: number;           // 必需，租金
  apartmentId: number;    // 必需，所属公寓ID
  // ... 其他房源信息
}
```

### 3.3 房源统计
```
GET /room/statistics
```

**请求参数** (Query Parameters):
```typescript
interface RoomStatsParams {
  landlordId: number;      // 必需，房东ID
}
```

**响应数据**:
```typescript
interface RoomStatistics {
  totalRooms: number;      // 总房源数
  pendingReview: number;   // 待审核数
  approved: number;        // 审核通过数
  rejected: number;        // 审核拒绝数
  published: number;       // 已发布数
  avgComplianceScore: number; // 平均符合率评分
  approvalRate: number;    // 审核通过率
}

type Response = ApiResponse<RoomStatistics>;
```

---

## 4. 仪表板接口

### 4.1 获取仪表板数据
```
GET /dashboard/data
```

**请求参数**: 无 (从token中获取landlordId)

**响应数据**:
```typescript
interface LandlordDashboardVo {
  landlordId: number;
  apartmentCount: number;
  roomCount: number;
  publishedRoomCount: number;
  availableRoomCount: number;
  occupiedRoomCount: number;
  pendingApplicationCount: number;
  pendingAppointmentCount: number;
  todayAppointmentCount: number;
  occupancyRate: number;
  monthlyNewRooms: number;
  monthlyDeals: number;
  monthlyIncome: number;
  landlordLevel: string;
  creditScore: number;
  lastUpdateTime: string;
}

type Response = ApiResponse<LandlordDashboardVo>;
```

### 4.2 获取快速操作菜单
```
GET /dashboard/quick-actions
```

**响应数据**:
```typescript
interface QuickAction {
  key: string;
  title: string;
  icon: string;
  path: string;
}

interface QuickActionsResponse {
  actions: QuickAction[];
}

type Response = ApiResponse<QuickActionsResponse>;
```

### 4.3 获取权限列表
```
GET /dashboard/permissions
```

**响应数据**:
```typescript
type Response = ApiResponse<string[]>; // 权限代码列表
```

### 4.4 检查功能权限
```
GET /dashboard/check-permission
```

**请求参数** (Query Parameters):
```typescript
interface CheckPermissionParams {
  permission: string;      // 必需，权限代码
}
```

**响应数据**:
```typescript
type Response = ApiResponse<boolean>; // 是否有权限
```

---

## 5. 前端TypeScript类型定义

### 5.1 创建类型定义文件
```typescript
// src/types/landlord.ts

// 基础响应类型
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 枚举类型
export enum ReleaseStatus {
  NOT_RELEASED = 0,
  RELEASED = 1
}

export enum ReviewStatus {
  PENDING = 1,
  APPROVED = 2,
  REJECTED = 3
}

// 公寓相关类型
export interface ApartmentItemVo {
  id: number;
  name: string;
  introduction: string;
  districtName: string;
  cityName: string;
  provinceName: string;
  addressDetail: string;
  phone: string;
  isRelease: ReleaseStatus;
  roomCount: number;
  availableRoomCount: number;
  createTime: string;
  updateTime?: string;
}

export interface ApartmentQueryVo {
  name?: string;
  landlordId?: number;
  provinceId?: number;
  cityId?: number;
  districtId?: number;
  isRelease?: ReleaseStatus;
}

// 房源相关类型
export interface RoomItemVo {
  id: number;
  roomNumber: string;
  rent: number;
  apartmentId: number;
  apartmentName: string;
  isRelease: ReleaseStatus;
  reviewStatus: ReviewStatus;
  complianceScore?: number;
  createTime: string;
  updateTime?: string;
  lastReviewedAt?: string;
  graphVoList?: GraphVo[];
}

// 统计数据类型
export interface ApartmentStatistics {
  totalApartments: number;
  publishedApartments: number;
  unpublishedApartments: number;
  totalRooms: number;
}

export interface RoomStatistics {
  totalRooms: number;
  pendingReview: number;
  approved: number;
  rejected: number;
  published: number;
  avgComplianceScore: number;
  approvalRate: number;
}

// 仪表板类型
export interface LandlordDashboardVo {
  landlordId: number;
  apartmentCount: number;
  roomCount: number;
  publishedRoomCount: number;
  availableRoomCount: number;
  occupiedRoomCount: number;
  pendingApplicationCount: number;
  pendingAppointmentCount: number;
  todayAppointmentCount: number;
  occupancyRate: number;
  monthlyNewRooms: number;
  monthlyDeals: number;
  monthlyIncome: number;
  landlordLevel: string;
  creditScore: number;
  lastUpdateTime: string;
}
```

### 5.2 API调用规范
```typescript
// src/api/landlord/apartment.ts

import { http } from '@/utils/http';
import type { 
  ApiResponse, 
  PageResult, 
  ApartmentItemVo, 
  ApartmentQueryVo,
  ApartmentStatistics 
} from '@/types/landlord';

/**
 * 获取房东公寓列表
 * @param params 查询参数
 */
export function getMyApartments(params: {
  current: number;
  size: number;
  landlordId: number;
  queryVo?: ApartmentQueryVo;
}) {
  // 构建查询字符串
  const queryParams: string[] = [
    `current=${params.current}`,
    `size=${params.size}`,
    `landlordId=${params.landlordId}`
  ];
  
  // 添加可选参数
  if (params.queryVo) {
    if (params.queryVo.name) {
      queryParams.push(`name=${encodeURIComponent(params.queryVo.name)}`);
    }
    if (params.queryVo.isRelease !== undefined) {
      queryParams.push(`isRelease=${params.queryVo.isRelease}`);
    }
    // ... 其他参数
  }
  
  const url = `/app/landlord/apartment/pageItem?${queryParams.join('&')}`;
  return http.get<PageResult<ApartmentItemVo>>(url);
}

/**
 * 获取公寓统计信息
 * @param landlordId 房东ID
 */
export function getApartmentStatistics(landlordId: number) {
  const url = `/app/landlord/apartment/statistics?landlordId=${landlordId}`;
  return http.get<ApartmentStatistics>(url);
}
```

---

## 6. 前端开发注意事项

### 6.1 参数传递规范
1. **GET请求**: 所有参数都通过Query Parameters传递
2. **POST/PUT请求**: 业务数据通过Body传递，身份标识通过Query传递
3. **参数类型**: 所有Query参数都转换为字符串
4. **对象参数**: 如`ApartmentQueryVo`，其属性直接展开为Query参数

### 6.2 错误处理规范
```typescript
// HTTP拦截器统一处理
axios.interceptors.response.use(
  response => {
    const { data } = response;
    if (data.code !== 200) {
      throw new Error(data.message || '请求失败');
    }
    return data;
  },
  error => {
    if (error.response?.status === 401) {
      // token过期，跳转登录
      removeToken();
      router.push('/login');
    }
    throw error;
  }
);
```

### 6.3 组件开发规范
```typescript
// 避免重复请求
const [isInitialized, setIsInitialized] = useState(false);

const handleLoadMore = () => {
  if (!isInitialized || finished) return;
  // 加载更多逻辑
};

useEffect(() => {
  initData().then(() => {
    setIsInitialized(true);
  });
}, []);
```

### 6.4 路由配置规范
```typescript
// 路由路径要与后端接口保持一致的命名风格
const routes = [
  {
    path: '/landlord/apartments',           // 列表页
    component: ApartmentList
  },
  {
    path: '/landlord/apartments/form/:id?', // 创建/编辑页
    component: ApartmentForm
  },
  {
    path: '/landlord/apartments/:id',       // 详情页
    component: ApartmentDetail
  }
];
```

---

## 7. 测试用例

### 7.1 接口测试
```typescript
// 测试公寓列表接口
describe('Apartment API', () => {
  test('should get apartment list', async () => {
    const params = {
      current: 1,
      size: 10,
      landlordId: 8,
      queryVo: { isRelease: 1 }
    };
    
    const response = await getMyApartments(params);
    
    expect(response.code).toBe(200);
    expect(response.data.records).toBeInstanceOf(Array);
    expect(response.data.total).toBeGreaterThanOrEqual(0);
  });
});
```

### 7.2 组件测试
```typescript
// 测试组件重复加载问题
describe('ApartmentList Component', () => {
  test('should not load data multiple times', async () => {
    const mockApi = jest.fn().mockResolvedValue({
      code: 200,
      data: { records: [], total: 0 }
    });
    
    render(<ApartmentList />);
    
    await waitFor(() => {
      expect(mockApi).toHaveBeenCalledTimes(1);
    });
  });
});
```

---

这个接口规范文档涵盖了：
1. **统一的数据格式和错误处理**
2. **详细的参数说明和类型定义**
3. **完整的TypeScript类型系统**
4. **前端调用示例和最佳实践**
5. **测试规范和注意事项**

有了这个规范，前端开发就能：
- 明确知道每个接口的参数格式和返回数据
- 避免参数传递错误
- 统一错误处理逻辑
- 减少调试时间
- 提高代码质量

你可以基于这个规范来修改现有的前端代码，确保与后端接口完全匹配。