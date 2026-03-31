# 房源可用性友好提示功能

## 🎯 功能概述

为了提升用户体验，我们实现了房源可用性的友好提示功能。当租客点击"看房预约"或"直接入住"按钮时，系统会立即检查房源状态并给出清晰的中文提示，避免用户填写完整表单后才发现房源不可用。

## ✨ 主要特性

### 1. 即时检查
- 用户点击按钮时立即检查房源可用性
- 避免无效的表单填写操作
- 提供实时的状态反馈

### 2. 友好提示
- 清晰的中文提示信息
- 详细的不可用原因说明
- 美观的对话框界面

### 3. 智能处理
- 直接入住：严格检查，不可用时阻止操作
- 预约看房：灵活处理，某些情况下仍可预约

### 4. 视觉反馈
- 房源列表显示实时可用状态
- 不可用房源添加视觉标识
- 状态标签和图标提示

## 🔧 技术实现

### 后端API接口

#### 直接入住检查
```
GET /app/application/check-room/{roomId}
返回：RoomAvailabilityVo
```

#### 预约看房检查
```
GET /app/appointment/checkRoomAvailability/{roomId}
返回：RoomAvailabilityVo
```

#### 返回数据结构
```typescript
interface RoomAvailabilityVo {
  available: boolean;      // 是否可用
  statusCode: string;      // 状态码
  message: string;         // 友好提示信息
  reason: string;          // 详细原因说明
}
```

### 前端实现

#### 1. API封装
- `src/api/tenant/directMoveIn.ts` - 直接入住相关API
- `src/api/tenant/appointment.ts` - 预约看房相关API

#### 2. 工具函数
- `src/utils/roomAvailabilityHelper.ts` - 房源可用性检查工具

#### 3. 组件优化
- `src/views/tenant/roomDetail/roomDetail.vue` - 房源详情页面
- `src/components/common/RoomCard/RoomCard.vue` - 房源卡片组件

#### 4. 样式文件
- `src/styles/roomAvailability.less` - 友好提示对话框样式

## 📋 房源状态说明

### 🔒 不可用状态（3个）
这些状态下房源被占用，不能预约看房或直接入住：

| 状态 | 说明 | 提示信息 |
|------|------|----------|
| `SIGNING` | 签约待确认 | "房源预订中" - 已有租客预订此房源，正在签约确认中 |
| `SIGNED` | 已签约 | "房源已出租" - 此房源已有租客入住，暂时无法预约 |
| `RENEWING` | 续约待确认 | "房源续约中" - 现有租客正在办理续约手续，暂时无法预约 |

### ✅ 可用状态（4个）
这些状态下房源空闲，可以预约看房或直接入住：

| 状态 | 说明 |
|------|------|
| `CANCELED` | 已取消 - 租约无效，重新可租 |
| `EXPIRED` | 已到期 - 租期结束，可重新出租 |
| `WITHDRAWING` | 退租待确认 - 租客想退租，可接受预约 |
| `WITHDRAWN` | 已退租 - 已搬离，房源空闲 |

### 🚫 其他不可用状态

| 状态码 | 提示信息 | 说明 |
|--------|----------|------|
| `NOT_FOUND` | "房源不存在" | 该房源可能已被删除或不存在 |
| `NOT_RELEASED` | "房源暂未开放" | 房东暂时未开放此房源 |
| `NOT_APPROVED` | "房源审核中" | 房源正在审核中，暂时无法预约 |

## 🎨 用户体验设计

### 1. 直接入住流程
```
用户点击"直接入住" → 检查房源可用性 → 
├─ 可用：跳转申请表单
└─ 不可用：显示友好提示，阻止操作
```

### 2. 预约看房流程
```
用户点击"预约看房" → 检查房源可用性 → 
├─ 可用：跳转预约表单
├─ 已被占用：显示提示 + 询问是否仍要预约
└─ 其他不可用：显示友好提示，阻止操作
```

### 3. 房源列表优化
- 实时显示房源可用状态
- 不可用房源添加视觉标识
- 状态标签颜色区分

## 🧪 测试功能

访问测试页面：`/test/room-availability`

### 测试内容
1. **真实API测试** - 输入房源ID进行实际检查
2. **模拟状态测试** - 查看各种状态的提示效果
3. **交互体验测试** - 验证对话框和按钮交互

### 测试用例
- 可用房源：正常跳转到申请/预约页面
- 已占用房源：显示友好提示
- 未发布房源：显示相应提示
- 审核中房源：显示审核状态提示

## 📱 使用示例

### 在组件中使用
```typescript
import { 
  checkDirectMoveInAvailability, 
  checkAppointmentAvailability 
} from '@/utils/roomAvailabilityHelper';

// 检查直接入住可用性
const handleDirectMoveIn = async (roomId: number) => {
  const isAvailable = await checkDirectMoveInAvailability(roomId);
  if (isAvailable) {
    // 跳转到申请页面
    router.push('/tenant/directMoveIn');
  }
};

// 检查预约看房可用性
const handleAppointment = async (roomId: number) => {
  const isAvailable = await checkAppointmentAvailability(roomId);
  if (isAvailable) {
    // 跳转到预约页面
    router.push('/tenant/appointment');
  }
};
```

### 在房源卡片中显示状态
```vue
<RoomCard 
  :data="roomData" 
  :showAvailability="true" 
/>
```

## 🎉 效果展示

### 友好提示对话框
- 清晰的标题和说明
- 统一的视觉风格
- 易于理解的中文提示

### 房源列表状态
- 绿色"可租"标签：房源可用
- 红色"已租"标签：房源已被占用
- 灰色"未发布"标签：房东未开放
- 橙色"审核中"标签：正在审核

### 交互优化
- 按钮点击即时反馈
- 加载状态显示
- 错误处理机制

## 🔄 后续优化

1. **缓存机制** - 避免重复检查同一房源
2. **批量检查** - 房源列表批量获取状态
3. **实时更新** - WebSocket推送状态变化
4. **性能优化** - 懒加载和防抖处理

---

这个功能大大提升了用户体验，让租客在操作前就能清楚了解房源状态，避免了无效操作和用户困惑。🏠✨