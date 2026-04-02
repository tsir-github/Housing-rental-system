# 尚庭公寓后台管理系统

<p align="center">
  <img src="https://img.shields.io/badge/Vue-3.3.4-brightgreen.svg"/>
  <img src="https://img.shields.io/badge/TypeScript-4.9.3-blue.svg"/>
  <img src="https://img.shields.io/badge/Vite-4.3.9-646cff.svg"/>
  <img src="https://img.shields.io/badge/Element_Plus-2.3.7-409eff.svg"/>
  <img src="https://img.shields.io/badge/Pinia-2.0.30-ffd859.svg"/>
</p>

## � 项目简介

尚庭公寓后台管理系统是一个基于 **Vue 3 + TypeScript + Vite + Element Plus** 构建的现代化房屋租赁管理平台。系统采用前后端分离架构，专为官方管理人员设计，提供公寓管理、房源审核、租约管理、用户管理等全方位的业务管理功能。

> 本项目为毕业设计作品，展示了完整的企业级前端应用开发流程和最佳实践。

## ✨ 核心功能

### 🏢 公寓与房间管理
- **公寓管理**：公寓信息的增删改查、详情维护、状态控制（发布/下架）
- **房间管理**：房间信息维护、属性配置（面积、租金、付款方式）、图片管理
- **属性管理**：房间属性、配套设施等基础数据维护

### 📋 租赁业务管理
- **看房预约管理**：查看和处理租客的看房预约申请
- **租约管理**：租约的创建、查看、修改、续约，租约状态跟踪
- **直接入住申请**：处理租客的直接入住申请，申请审批流程管理

### 👥 用户与权限管理
- **用户管理**：租客信息管理、用户状态控制、数据查询统计
- **房东权限管理**：房东账号管理、权限分配与控制、操作权限审核
- **房源审核管理**：房东提交的房源审核、审核流程管理、审核历史记录

### ⚙️ 系统管理
- **系统用户管理**：后台管理员账号的增删改查
- **岗位管理**：岗位信息维护和权限配置

## 🛠️ 技术栈

### 核心技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.4 | 渐进式 JavaScript 框架 |
| TypeScript | 4.9.3 | JavaScript 的超集，提供类型安全 |
| Vite | 4.3.9 | 下一代前端构建工具 |
| Element Plus | 2.3.7 | 基于 Vue 3 的组件库 |
| Pinia | 2.0.30 | Vue 3 官方推荐的状态管理库 |
| Vue Router | 4.2.2 | Vue.js 官方路由管理器 |
| Axios | 1.4.0 | 基于 Promise 的 HTTP 客户端 |

### 特色功能库
- **@amap/amap-jsapi-loader** - 高德地图集成，支持地理位置展示
- **ECharts 5.4.1** - 数据可视化图表库
- **@vueuse/core** - Vue Composition API 工具集
- **nprogress** - 页面加载进度条

### 工程化工具
- **ESLint + Prettier** - 代码规范和格式化
- **Stylelint** - CSS 代码检查
- **Husky + lint-staged** - Git hooks 和暂存文件检查
- **Commitlint** - Git 提交信息规范

## 📁 项目结构

```
rentHouseAdmin/
├── src/
│   ├── api/                        # API 接口层
│   │   ├── apartmentManagement/    # 公寓管理接口
│   │   ├── applicationManagement/  # 申请管理接口
│   │   ├── landlordManagement/     # 房东管理接口
│   │   ├── rentManagement/         # 租赁管理接口
│   │   ├── roomReview/             # 房源审核接口
│   │   ├── system/                 # 系统管理接口
│   │   ├── upload/                 # 文件上传接口
│   │   └── userManagement/         # 用户管理接口
│   ├── assets/                     # 静态资源
│   │   ├── fonts/                  # 字体文件
│   │   ├── icons/                  # SVG 图标
│   │   └── images/                 # 图片资源
│   ├── components/                 # 公共组件
│   │   ├── DynamicComponentBox/    # 动态组件容器
│   │   ├── Grid/                   # 栅格布局组件
│   │   ├── ProTable/               # 高级表格组件
│   │   └── uploadImg/              # 图片上传组件
│   ├── config/                     # 全局配置
│   ├── directives/                 # 自定义指令
│   ├── hooks/                      # 组合式函数
│   ├── layouts/                    # 布局组件
│   ├── router/                     # 路由配置
│   │   ├── constantRoutes.ts       # 静态路由定义
│   │   └── index.ts                # 路由入口
│   ├── store/                      # Pinia 状态管理
│   │   └── modules/                # 状态模块
│   ├── styles/                     # 全局样式
│   ├── utils/                      # 工具函数
│   ├── views/                      # 页面组件
│   │   ├── apartmentManagement/    # 公寓管理页面
│   │   ├── applicationManagement/  # 申请管理页面
│   │   ├── landlordManagement/     # 房东管理页面
│   │   ├── rentManagement/         # 租赁管理页面
│   │   ├── roomReview/             # 房源审核页面
│   │   ├── system/                 # 系统管理页面
│   │   ├── userManagement/         # 用户管理页面
│   │   ├── home/                   # 首页
│   │   └── login/                  # 登录页
│   ├── App.vue                     # 根组件
│   └── main.ts                     # 应用入口
├── .env.development                # 开发环境配置
├── .env.production                 # 生产环境配置
├── .eslintrc.cjs                   # ESLint 配置
├── .prettierrc.json                # Prettier 配置
├── .stylelintrc.cjs                # Stylelint 配置
├── commitlint.config.cjs           # Commitlint 配置
├── index.html                      # HTML 入口
├── package.json                    # 项目依赖
├── tsconfig.json                   # TypeScript 配置
└── vite.config.ts                  # Vite 配置
```

## 🚀 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0 或 pnpm >= 7.0.0

### 安装依赖

```bash
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
# ESLint 检查
npm run lint

# ESLint 自动修复
npm run fix

# 代码格式化
npm run format

# Stylelint 检查并修复
npm run lint:style
```

## 📝 开发规范

### Git 提交规范

本项目使用 Commitlint 规范 Git 提交信息，格式如下：

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
| refactor | 重构（既不是新增功能，也不是修复 bug） |
| perf | 性能优化 |
| test | 测试相关 |
| chore | 构建过程或辅助工具的变动 |
| revert | 回退 |
| build | 打包 |

**提交示例：**

```bash
git commit -m "feat(apartment): 添加公寓管理功能"
git commit -m "fix(login): 修复登录页面验证码显示问题"
git commit -m "docs: 更新 README 文档"
```

### 命名规范

- **文件命名**：小驼峰（camelCase）或短横线（kebab-case）
- **组件命名**：大驼峰（PascalCase）
- **变量命名**：小驼峰（camelCase）
- **常量命名**：全大写下划线（UPPER_SNAKE_CASE）

### 代码风格

- 使用 2 个空格缩进
- 使用单引号
- 语句末尾不加分号
- 每行代码长度不超过 100 字符
- 优先使用 ES6+ 语法

## 🌍 环境配置

### 开发环境

```bash
# .env.development
VITE_APP_NODE_ENV='development'
VITE_APP_TITLE='公寓管理后台管理'
VITE_APP_BASE_URL='http://localhost:8080'
```

### 生产环境

```bash
# .env.production
VITE_APP_NODE_ENV='production'
VITE_APP_TITLE='公寓管理后台管理'
VITE_APP_BASE_URL='生产环境API地址'
```

## 💡 项目特色

- ✅ **现代化技术栈**：Vue 3 Composition API + TypeScript，提供更好的类型安全和开发体验
- ✅ **工程化规范**：集成 ESLint、Prettier、Stylelint、Husky 等工具，确保代码质量
- ✅ **组件化开发**：高度组件化的架构设计，提高代码复用性和可维护性
- ✅ **响应式设计**：适配不同屏幕尺寸，提供良好的用户体验
- ✅ **权限管理**：完善的权限控制体系，支持多角色管理
- ✅ **数据可视化**：集成 ECharts，提供丰富的数据展示方式
- ✅ **地图集成**：集成高德地图，支持地理位置展示和选择

## 🔗 相关链接

- **GitHub 仓库**：[https://github.com/tsir-github/Housing-rental-system](https://github.com/tsir-github/Housing-rental-system)
- **Vue 3 官方文档**：[https://cn.vuejs.org/](https://cn.vuejs.org/)
- **Element Plus 文档**：[https://element-plus.org/zh-CN/](https://element-plus.org/zh-CN/)
- **Vite 官方文档**：[https://cn.vitejs.dev/](https://cn.vitejs.dev/)
- **TypeScript 文档**：[https://www.typescriptlang.org/zh/](https://www.typescriptlang.org/zh/)
- **Pinia 文档**：[https://pinia.vuejs.org/zh/](https://pinia.vuejs.org/zh/)

## 📧 联系方式

- **项目性质**：毕业设计项目
- **开发者邮箱**：rublw07524@outlook.com

## 📄 开源协议

本项目仅供学习交流使用。
