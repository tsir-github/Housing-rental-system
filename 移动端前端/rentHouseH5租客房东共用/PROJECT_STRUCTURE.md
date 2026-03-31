# 项目结构说明

## 概述
本项目采用租客和房东功能分离的架构设计，通过目录结构实现代码的清晰分离和复用。

## 目录结构

```
src/
├── api/                    # API接口层
│   ├── common/            # 通用API接口
│   │   ├── user/         # 用户相关API
│   │   ├── template/     # 模板相关API
│   │   └── types.ts      # 通用类型定义
│   ├── tenant/           # 租客专用API
│   │   └── search/       # 搜索相关API
│   └── landlord/         # 房东专用API
│
├── components/            # 组件层
│   ├── common/           # 通用组件
│   │   ├── ApartmentCard/     # 公寓卡片组件
│   │   ├── LoadingButton/     # 加载按钮组件
│   │   ├── NavBar/           # 导航栏组件
│   │   ├── PullDownRefreshContainer/  # 下拉刷新容器
│   │   ├── RoomCard/         # 房源卡片组件
│   │   ├── SearchBar/        # 搜索栏组件
│   │   ├── SvgIcon/          # SVG图标组件
│   │   └── Tabbar/           # 底部导航栏组件
│   ├── tenant/           # 租客专用组件
│   └── landlord/         # 房东专用组件
│
├── views/                # 页面层
│   ├── common/           # 通用页面
│   │   ├── login/        # 登录页面
│   │   └── message/      # 消息页面
│   ├── tenant/           # 租客页面
│   │   ├── agreement/         # 租约管理
│   │   ├── apartmentDetail/   # 公寓详情
│   │   ├── appointment/       # 预约看房
│   │   ├── browsingHistory/   # 浏览历史
│   │   ├── group/            # 分组管理
│   │   ├── myAgreement/      # 我的租约
│   │   ├── myAppointment/    # 我的预约
│   │   ├── myRoom/           # 我的房源
│   │   ├── roomDetail/       # 房源详情
│   │   ├── search/           # 搜索页面
│   │   └── userCenter/       # 个人中心
│   └── landlord/         # 房东页面
│
├── store/                # 状态管理
│   ├── modules/          # 状态模块
│   │   ├── cachedView.ts     # 缓存视图状态
│   │   ├── darkMode.ts       # 暗黑模式状态
│   │   └── user.ts           # 用户状态
│   └── index.ts          # 状态管理入口
│
└── router/               # 路由配置
    ├── index.ts          # 路由入口
    ├── otherRoutes.ts    # 其他路由
    ├── routes.ts         # 主要路由
    └── tabBarRoutes.ts   # 底部导航路由
```

## 路径别名配置

项目配置了以下路径别名，方便开发时引用：

- `@/*` - src目录下的所有文件
- `@common/*` - 通用组件目录
- `@tenant/*` - 租客组件目录  
- `@landlord/*` - 房东组件目录
- `@api/*` - API接口目录
- `@views/*` - 页面视图目录

## 使用示例

```typescript
// 引用通用组件
import NavBar from '@common/NavBar/index.vue'

// 引用租客组件
import TenantComponent from '@tenant/SomeComponent.vue'

// 引用房东组件
import LandlordComponent from '@landlord/SomeComponent.vue'

// 引用API接口
import { getUserInfo } from '@api/common/user'
import { searchRooms } from '@api/tenant/search'
import { getDashboard } from '@api/landlord/dashboard'
```

## 开发规范

1. **代码分离原则**：租客和房东的功能代码严格分离，避免耦合
2. **通用组件复用**：将可复用的组件放在common目录下
3. **API接口分层**：按照用户类型和功能模块组织API接口
4. **路径别名使用**：统一使用配置的路径别名，提高代码可读性
5. **目录命名规范**：使用小驼峰命名法，保持一致性

## 注意事项

- 在开发房东功能时，优先复用common目录下的通用组件
- 新增API接口时，根据功能归属选择合适的目录
- 修改通用组件时，需要考虑对租客和房东功能的影响
- 路由配置需要根据用户类型进行权限控制