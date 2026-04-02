# 尚庭公寓移动端 H5 应用

<p align="center">
  <img src="https://img.shields.io/badge/Vue-3.3.4-brightgreen.svg"/>
  <img src="https://img.shields.io/badge/TypeScript-4.7.4-blue.svg"/>
  <img src="https://img.shields.io/badge/Vite-4.3.9-646cff.svg"/>
  <img src="https://img.shields.io/badge/Vant-4.6.0-07c160.svg"/>
  <img src="https://img.shields.io/badge/Pinia-2.1.4-ffd859.svg"/>
</p>

## 📖 项目简介

尚庭公寓移动端 H5 应用是一个基于 **Vue 3 + TypeScript + Vite + Vant** 构建的现代化移动端租房平台。系统采用**租客和房东双端合一**的架构设计，通过统一的代码库为两类用户提供差异化的功能体验。

> 本项目为毕业设计作品，展示了完整的移动端应用开发流程和最佳实践。

## ✨ 核心功能

### 👤 租客端功能

#### 🏠 房源浏览与搜索
- **智能搜索**：支持地区、价格、房型等多维度筛选
- **地图找房**：高德地图集成，直观展示房源位置
- **房源详情**：详细的房源信息、图片展示、配套设施
- **浏览历史**：自动记录浏览过的房源

#### 🤖 AI 智能助手
- **智能对话**：基于 DeepSeek AI 的智能房源推荐
- **关键词提取**：自动理解用户需求，精准推荐
- **房源卡片**：对话中直接展示推荐房源，一键跳转

#### 📅 预约与申请
- **看房预约**：在线预约看房时间，实时状态跟踪
- **直接入住申请**：快速提交入住申请，在线签约
- **房源可用性检查**：智能检测房源状态，避免无效操作
- **申请管理**：查看申请历史和处理进度

#### 📝 租约管理
- **我的租约**：查看当前租约详情和历史租约
- **租约续签**：在线提交续约申请
- **租金缴纳**：租金支付记录和提醒

### 🏢 房东端功能

#### 📊 数据仪表板
- **经营概览**：公寓数量、房源统计、出租率
- **收益分析**：月度收入、成交数据、趋势图表
- **待办提醒**：待处理申请、预约、即将到期租约

#### 🏘️ 公寓与房源管理
- **公寓管理**：创建、编辑、删除公寓信息
- **房源发布**：发布房源、上传图片、设置租金
- **房源审核**：查看审核状态、审核历史、打回原因
- **发布控制**：一键上架/下架房源

#### 📋 租赁业务管理
- **看房预约**：查看预约列表、确认/取消预约
- **入住申请**：处理租客申请、在线签约
- **租约管理**：创建租约、续约处理、租约统计
- **即将到期提醒**：自动提醒即将到期的租约

#### ⚙️ 基础数据管理
- **配套设施**：管理房源配套设施选项
- **房源标签**：管理房源特色标签
- **房间属性**：管理房间属性（朝向、楼层等）
- **杂费管理**：管理水电费、物业费等杂费项目
- **租期管理**：管理可选租期选项
- **支付方式**：管理支付方式选项

## 🛠️ 技术栈

### 核心技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.4 | 渐进式 JavaScript 框架 |
| TypeScript | 4.7.4 | JavaScript 的超集，提供类型安全 |
| Vite | 4.3.9 | 下一代前端构建工具 |
| Vant | 4.6.0 | 轻量、可靠的移动端组件库 |
| Pinia | 2.1.4 | Vue 3 官方推荐的状态管理库 |
| Vue Router | 4.2.2 | Vue.js 官方路由管理器 |
| Axios | 1.4.0 | 基于 Promise 的 HTTP 客户端 |

### 特色功能库
- **@amap/amap-jsapi-loader** - 高德地图集成，支持地图找房
- **ECharts 6.0.0** - 数据可视化图表库，房东数据分析
- **Tailwind CSS 3.3.2** - 原子化 CSS 框架，快速构建界面
- **Day.js** - 轻量级日期处理库

### 工程化工具
- **ESLint + Prettier** - 代码规范和格式化
- **Husky + Commitlint** - Git hooks 和提交信息规范
- **PostCSS + Autoprefixer** - CSS 处理和兼容性
- **vw 视口适配** - 移动端屏幕适配方案

## 📁 项目结构

```
rentHouseH5租客房东共用/
├── src/
│   ├── api/                        # API 接口层
│   │   ├── common/                 # 通用接口（用户、上传等）
│   │   ├── tenant/                 # 租客专用接口
│   │   │   ├── search/            # 搜索相关
│   │   │   ├── appointment.ts     # 预约看房
│   │   │   └── directMoveIn.ts    # 直接入住
│   │   └── landlord/              # 房东专用接口
│   │       ├── apartment.ts       # 公寓管理
│   │       ├── room.ts            # 房源管理
│   │       ├── lease.ts           # 租约管理
│   │       └── dashboard.ts       # 数据仪表板
│   ├── assets/                     # 静态资源
│   ├── components/                 # 组件层
│   │   ├── common/                # 通用组件
│   │   │   ├── NavBar/           # 导航栏
│   │   │   ├── Tabbar/           # 底部导航
│   │   │   ├── RoomCard/         # 房源卡片
│   │   │   └── SearchBar/        # 搜索栏
│   │   ├── tenant/               # 租客专用组件
│   │   │   └── RoomRecommendationCard/  # AI推荐卡片
│   │   └── landlord/             # 房东专用组件
│   │       ├── ApartmentCard/    # 公寓卡片
│   │       ├── StatCard/         # 统计卡片
│   │       └── LeaseCard/        # 租约卡片
│   ├── views/                      # 页面层
│   │   ├── common/                # 通用页面
│   │   │   ├── login/            # 登录页
│   │   │   └── home/             # 首页（角色选择）
│   │   ├── tenant/               # 租客页面
│   │   │   ├── home/             # 租客首页
│   │   │   ├── search/           # 搜索页面
│   │   │   ├── roomDetail/       # 房源详情
│   │   │   ├── aiChat/           # AI 助手
│   │   │   ├── appointment/      # 预约看房
│   │   │   ├── directMoveIn/     # 直接入住
│   │   │   ├── myAppointment/    # 我的预约
│   │   │   ├── myDirectMoveIn/   # 我的申请
│   │   │   ├── agreement/        # 我的租约
│   │   │   └── userCenter/       # 个人中心
│   │   └── landlord/             # 房东页面
│   │       ├── Dashboard.vue     # 数据仪表板
│   │       ├── ApartmentList.vue # 公寓列表
│   │       ├── ApartmentForm.vue # 公寓表单
│   │       ├── RoomList.vue      # 房源列表
│   │       ├── RoomForm.vue      # 房源表单
│   │       ├── ReviewStatus.vue  # 审核状态
│   │       ├── AppointmentList.vue    # 预约列表
│   │       ├── ApplicationList.vue    # 申请列表
│   │       ├── LeaseManagement.vue    # 租约管理
│   │       └── [各种基础数据管理页面]
│   ├── store/                      # Pinia 状态管理
│   │   └── modules/
│   │       ├── user.ts           # 用户状态
│   │       ├── apartment.ts      # 公寓状态
│   │       ├── room.ts           # 房源状态
│   │       ├── aiChat.ts         # AI 对话状态
│   │       └── landlordLease.ts  # 房东租约状态
│   ├── router/                     # 路由配置
│   │   ├── index.ts              # 路由入口
│   │   ├── routes.ts             # 主路由
│   │   ├── tabBarRoutes.ts       # 底部导航路由
│   │   ├── landlordRoutes.ts     # 房东路由
│   │   └── guards/               # 路由守卫
│   ├── utils/                      # 工具函数
│   │   ├── http/                 # HTTP 请求封装
│   │   ├── authHandler.ts        # 认证处理
│   │   ├── userHelper.ts         # 用户工具
│   │   └── roomAvailabilityHelper.ts  # 房源可用性检查
│   ├── styles/                     # 全局样式
│   ├── config/                     # 配置文件
│   └── main.ts                     # 应用入口
├── .env.development                # 开发环境配置
├── .env.production                 # 生产环境配置
├── vite.config.ts                  # Vite 配置
├── tailwind.config.js              # Tailwind 配置
├── postcss.config.js               # PostCSS 配置
└── package.json                    # 项目依赖
```

## 🚀 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0 或 pnpm >= 7.0.0（推荐）

### 安装依赖

```bash
# 使用 npm
npm install

# 或使用 pnpm（推荐）
pnpm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5173

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

### 代码质量检查

```bash
# ESLint 检查并修复
npm run lint

# 类型检查
npm run type-check
```

## 📝 开发规范

### Git 提交规范

本项目使用 Commitlint 规范 Git 提交信息，遵循 Angular 规范：

```
<type>(<scope>): <subject>
```

**type 类型说明：**

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 bug |
| docs | 文档更新 |
| style | 代码格式调整（不影响代码运行） |
| refactor | 重构 |
| perf | 性能优化 |
| test | 测试相关 |
| chore | 构建过程或辅助工具的变动 |

**提交示例：**

```bash
git commit -m "feat(tenant): 添加 AI 智能助手功能"
git commit -m "fix(landlord): 修复公寓列表加载问题"
git commit -m "docs: 更新 README 文档"
```

### 代码风格

- 使用 2 个空格缩进
- 组件命名使用 PascalCase
- 文件命名使用 camelCase 或 kebab-case
- 优先使用 Composition API
- 使用 TypeScript 类型注解

### 路径别名

项目配置了以下路径别名：

```typescript
@/*        - src 目录
@common/*  - 通用组件目录
@tenant/*  - 租客组件目录
@landlord/* - 房东组件目录
@api/*     - API 接口目录
@views/*   - 页面视图目录
```

## 🌍 环境配置

### 开发环境

```bash
# .env.development
VITE_APP_BASE_URL=
VITE_ENABLE_ERUDA=false
```

### 生产环境

```bash
# .env.production
VITE_APP_BASE_URL=生产环境API地址
VITE_ENABLE_ERUDA=false
```

## 💡 项目特色

### 🎯 双端合一架构
- 租客和房东功能代码分离，避免耦合
- 通用组件复用，提高开发效率
- 统一的状态管理和路由配置

### 🤖 AI 智能推荐
- 集成 DeepSeek AI，提供智能房源推荐
- 自然语言理解，精准匹配用户需求
- 对话式交互，提升用户体验

### 📱 移动端优化
- vw 视口适配，完美适配各种屏幕
- 下拉刷新、上拉加载
- 骨架屏加载、图片懒加载
- 网络状态监控、请求重试机制

### 🗺️ 地图找房
- 高德地图集成
- 房源位置标注
- 地图搜索和定位

### 🎨 现代化 UI
- Vant 4 组件库，轻量美观
- Tailwind CSS 原子化样式
- 深色模式支持
- 流畅的动画效果

### 🔐 权限管理
- 基于角色的权限控制
- 路由级别权限验证
- 按钮级别权限控制
- JWT Token 认证

## 🎨 核心功能亮点

### 房源可用性智能检测

系统实现了房源可用性的智能检测功能，避免用户无效操作：

**功能特点：**
- 点击按钮前自动检查房源状态
- 清晰的中文友好提示
- 详细的不可用原因说明
- 智能的业务逻辑处理

**房源状态说明：**

| 状态 | 说明 | 是否可预约 |
|------|------|-----------|
| 签约待确认 | 已有租客预订，正在签约 | ❌ |
| 已签约 | 已有租客入住 | ❌ |
| 续约待确认 | 现有租客正在续约 | ❌ |
| 已取消 | 租约无效，重新可租 | ✅ |
| 已到期 | 租期结束，可重新出租 | ✅ |
| 退租待确认 | 租客想退租，可接受预约 | ✅ |
| 已退租 | 已搬离，房源空闲 | ✅ |

### AI 智能助手

基于 DeepSeek AI 的智能房源推荐系统：

**核心能力：**
- 自然语言理解用户需求
- 智能提取关键词（地区、价格、房型等）
- 精准匹配房源数据库
- 对话式推荐，支持多轮交互
- 房源卡片展示，一键跳转详情

**技术实现：**
- Spring AI 框架集成
- DeepSeek API 调用
- 关键词提取服务
- 房源推荐算法
- 对话历史管理

## 🔗 相关链接

- **GitHub 仓库**：[https://github.com/tsir-github/Housing-rental-system](https://github.com/tsir-github/Housing-rental-system)
- **Vue 3 官方文档**：[https://cn.vuejs.org/](https://cn.vuejs.org/)
- **Vant 文档**：[https://vant-ui.github.io/vant/](https://vant-ui.github.io/vant/)
- **Vite 官方文档**：[https://cn.vitejs.dev/](https://cn.vitejs.dev/)
- **Tailwind CSS 文档**：[https://tailwindcss.com/](https://tailwindcss.com/)
- **高德地图 API**：[https://lbs.amap.com/](https://lbs.amap.com/)

## 📧 联系方式

- **项目性质**：毕业设计项目
- **开发者邮箱**：rublw07524@outlook.com

## 📄 开源协议

本项目仅供学习交流使用。
