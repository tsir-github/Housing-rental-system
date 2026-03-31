# 房源审核管理 API 文档

## 概述

房源审核管理系统提供了完整的房源质量评估和审核功能，支持智能评分算法和人工审核相结合的方式。

## API 接口列表

### 1. 房源审核管理

#### 1.1 获取待审核房源列表

**接口地址：** `GET /admin/room/review/pending`

**请求参数：**
```json
{
  "current": 1,           // 当前页码，默认1
  "size": 10,            // 每页大小，默认10
  "roomId": 123,         // 房间ID（可选）
  "apartmentId": 456,    // 公寓ID（可选）
  "landlordId": 789,     // 房东ID（可选）
  "reviewerId": 101,     // 审核员ID（可选）
  "minComplianceScore": 6.0,  // 最小符合率评分（可选）
  "maxComplianceScore": 10.0  // 最大符合率评分（可选）
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "roomId": 123,
        "apartmentId": 456,
        "landlordId": 789,
        "reviewerId": 101,
        "complianceScore": 8.5,
        "photoQualityScore": 7.8,
        "descriptionAccuracyScore": 9.2,
        "facilityCompletenessScore": 8.0,
        "locationAccuracyScore": 9.5,
        "status": 1,
        "overallRating": 2,
        "reviewComments": "房源质量良好",
        "createTime": "2024-01-05T10:30:00",
        "roomInfo": {
          "id": 123,
          "roomNumber": "A101",
          "rent": 2500.00,
          "apartmentId": 456
        },
        "apartmentInfo": {
          "id": 456,
          "name": "阳光公寓",
          "address": "北京市朝阳区xxx街道"
        },
        "reviewerName": "张审核员",
        "landlordName": "李房东"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

#### 1.2 提交房源审核

**接口地址：** `POST /admin/room/review/submit`

**请求体：**
```json
{
  "id": null,                    // 审核ID（更新时需要）
  "roomId": 123,                 // 房间ID
  "apartmentId": 456,            // 公寓ID
  "landlordId": 789,             // 房东ID
  "reviewerId": 101,             // 审核员ID
  "complianceScore": 8.5,        // 符合率评分（0-10分）
  "photoQualityScore": 7.8,      // 照片质量评分
  "descriptionAccuracyScore": 9.2, // 描述准确性评分
  "facilityCompletenessScore": 8.0, // 设施完整性评分
  "locationAccuracyScore": 9.5,   // 位置准确性评分
  "status": 2,                   // 审核状态：1-待审核，2-审核通过，3-审核拒绝
  "overallRating": 2,            // 综合评级：1-优秀，2-良好，3-一般，4-较差
  "reviewComments": "房源质量良好，符合发布标准",
  "rejectionReasons": []         // 拒绝原因列表（拒绝时填写）
}
```

#### 1.3 自动审核房源

**接口地址：** `POST /admin/room/review/auto/{roomId}`

**路径参数：**
- `roomId`: 房间ID

**请求参数：**
- `reviewerId`: 审核员ID

**响应示例：**
```json
{
  "code": 200,
  "message": "自动审核完成",
  "data": null
}
```

#### 1.4 获取房源评分详情

**接口地址：** `GET /admin/room/review/score/{roomId}`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "complianceScore": 8.5,
    "photoQualityScore": 7.8,
    "descriptionAccuracyScore": 9.2,
    "facilityCompletenessScore": 8.0,
    "locationAccuracyScore": 9.5,
    "overallScore": 8.2
  }
}
```

### 2. 直接入住申请管理

#### 2.1 获取待处理申请列表

**接口地址：** `GET /admin/application/direct-move-in/pending`

**请求参数：**
```json
{
  "current": 1,
  "size": 10,
  "processorId": 101,      // 处理人ID（可选）
  "processorType": 1       // 处理人类型：1-官方，2-房东（可选）
}
```

#### 2.2 处理直接入住申请

**接口地址：** `POST /admin/application/direct-move-in/process/{id}`

**路径参数：**
- `id`: 申请ID

**请求参数：**
- `processorId`: 处理人ID
- `processorType`: 处理人类型
- `approved`: 是否通过（true/false）
- `rejectionReason`: 拒绝原因（拒绝时必填）

### 3. 房东权限管理

#### 3.1 授予权限

**接口地址：** `POST /admin/landlord/permission/grant`

**请求参数：**
- `userId`: 用户ID
- `permissionCode`: 权限代码

#### 3.2 检查权限

**接口地址：** `GET /admin/landlord/permission/check`

**请求参数：**
- `userId`: 用户ID
- `permissionCode`: 权限代码

## 前端页面设计规范

### 1. 房源审核页面

#### 1.1 审核列表页面
- **页面路径：** `/admin/room-review/list`
- **功能：** 展示待审核和已审核的房源列表
- **组件：** 表格、筛选器、分页器

#### 1.2 房源审核详情页面
- **页面路径：** `/admin/room-review/detail/:id`
- **功能：** 展示房源详细信息和评分界面
- **组件：** 房源信息卡片、图片轮播、评分表单

#### 1.3 智能审核页面
- **页面路径：** `/admin/room-review/auto`
- **功能：** 批量自动审核房源
- **组件：** 房源选择器、审核进度条、结果展示

### 2. 直接入住申请页面

#### 2.1 申请列表页面
- **页面路径：** `/admin/application/direct-move-in`
- **功能：** 展示直接入住申请列表
- **组件：** 表格、状态筛选、操作按钮

#### 2.2 申请处理页面
- **页面路径：** `/admin/application/direct-move-in/process/:id`
- **功能：** 处理具体的入住申请
- **组件：** 申请详情、处理表单、合同预览

### 3. 房东权限管理页面

#### 3.1 权限管理列表
- **页面路径：** `/admin/landlord/permission`
- **功能：** 管理房东权限
- **组件：** 用户列表、权限树、操作按钮

## 前端技术栈建议

- **框架：** Vue 3 + TypeScript
- **UI库：** Element Plus / Ant Design Vue
- **状态管理：** Pinia
- **路由：** Vue Router 4
- **HTTP客户端：** Axios
- **构建工具：** Vite

## 样式规范

### 1. 色彩规范
- **主色：** #409EFF（蓝色）
- **成功色：** #67C23A（绿色）
- **警告色：** #E6A23C（橙色）
- **危险色：** #F56C6C（红色）
- **信息色：** #909399（灰色）

### 2. 字体规范
- **主字体：** 14px
- **标题字体：** 16px/18px/20px
- **小字体：** 12px

### 3. 间距规范
- **基础间距：** 8px的倍数（8px, 16px, 24px, 32px）
- **组件间距：** 16px
- **页面边距：** 24px

## 开发注意事项

1. **权限控制：** 所有页面都需要进行权限验证
2. **错误处理：** 统一的错误提示和处理机制
3. **加载状态：** 异步操作需要显示加载状态
4. **响应式设计：** 支持不同屏幕尺寸
5. **数据验证：** 表单提交前进行数据验证
6. **用户体验：** 操作反馈和确认提示