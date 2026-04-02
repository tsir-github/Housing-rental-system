# 🏠 租房管理系统 - 后台管理前端

> 基于 Vue3 + TypeScript + Vite + Element Plus 的现代化后台管理系统

[![Vue](https://img.shields.io/badge/Vue-3.3.4-brightgreen.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-4.9.3-blue.svg)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-4.3.9-646CFF.svg)](https://vitejs.dev/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.3.7-409EFF.svg)](https://element-plus.org/)

---

## 📋 项目简介

租房管理系统后台管理前端，为官方管理人员提供完整的房源管理、用户管理、租约管理等功能。采用最新的前端技术栈，提供流畅的用户体验和强大的功能支持。

### 核心功能模块

- 🏢 **公寓管理** - 公寓信息的增删改查、图片上传、配套设施管理
- 🏠 **房源管理** - 房源信息管理、房源审核、发布状态控制
- 👥 **用户管理** - 租客信息管理、房东权限管理
- 📝 **租约管理** - 租约创建、查看预约、直接入住申请处理
- 🔍 **房源审核** - 房东提交房源的审核、打回、通过流程
- 📊 **数据统计** - 租约统计、房源统计、用户统计

---

## 🚀 快速开始

### 环境要求

- Node.js >= 16.x
- npm >= 8.x 或 pnpm >= 7.x

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问地址：http://localhost:5173

### 构建生产版本

```bash
# 生产环境构建
npm run build

# 测试环境构建
npm run build:test

# 预览构建结果
npm run preview
```

---

## 🛠️ 技术栈

### 核心框架

- **Vue 3.3.4** - 渐进式 JavaScript 框架
- **TypeScript 4.9.3** - JavaScript 的超集，提供类型安全
- **Vite 4.3.9** - 下一代前端构建工具
- **Vue Router 4.2.2** - Vue.js 官方路由管理器
- **Pinia 2.0.30** - Vue 的状态管理库

### UI 组件库

- **Element Plus 2.3.7** - 基于 Vue 3 的组件库
- **@element-plus/icons-vue** - Element Plus 图标库
- **ECharts 5.4.1** - 数据可视化图表库

### 工具库

- **Axios 1.4.0** - HTTP 客户端
- **@amap/amap-jsapi-loader** - 高德地图 API 加载器
- **NProgress** - 页面加载进度条
- **Pinia Plugin Persistedstate** - Pinia 持久化插件

### 开发工具

- **ESLint** - 代码质量检查
- **Prettier** - 代码格式化
- **Stylelint** - 样式代码检查
- **Husky** - Git hooks 工具
- **Commitlint** - Git 提交信息规范检查
- **Lint-staged** - Git 暂存文件检查

---

## 📁 项目结构

```
后台管理系统前端/rentHouseAdmin/
├── .husky/                    # Git hooks 配置
├── .vscode/                   # VSCode 编辑器配置
├── mock/                      # Mock 数据
├── public/                    # 静态资源（不会被打包）
│   └── logo.png              # 项目 Logo
├── src/
│   ├── api/                   # API 接口管理
│   │   ├── apartmentManagement/    # 公寓管理接口
│   │   ├── applicationManagement/  # 申请管理接口
│   │   ├── landlordManagement/     # 房东管理接口
│   │   ├── rentManagement/         # 租约管理接口
│   │   ├── roomReview/             # 房源审核接口
│   │   ├── system/                 # 系统接口
│   │   ├── upload/                 # 文件上传接口
│   │   ├── user/                   # 用户接口
│   │   └── userManagement/         # 用户管理接口
│   ├── assets/                # 静态资源
│   │   ├── fonts/            # 字体文件
│   │   ├── icons/            # SVG 图标
│   │   └── images/           # 图片资源
│   ├── components/            # 全局组件
│   │   ├── DynamicComponentBox/    # 动态组件容器
│   │   ├── elTableForAllData/      # 全数据表格组件
│   │   ├── Grid/                   # 栅格组件
│   │   ├── IconifyIcon/            # 图标组件
│   │   ├── ProTable/               # 高级表格组件
│   │   └── uploadImg/              # 图片上传组件
│   ├── config/                # 全局配置
│   ├── directives/            # 自定义指令
│   ├── enums/                 # 枚举定义
│   ├── hooks/                 # 组合式函数
│   ├── languages/             # 国际化配置
│   ├── layouts/               # 布局组件
│   ├── routers/               # 路由配置
│   ├── stores/                # Pinia 状态管理
│   ├── styles/                # 全局样式
│   ├── typings/               # TypeScript 类型声明
│   ├── utils/                 # 工具函数
│   ├── views/                 # 页面组件
│   │   ├── apartmentManagement/    # 公寓管理页面
│   │   ├── applicationManagement/  # 申请管理页面
│   │   ├── landlordManagement/     # 房东管理页面
│   │   ├── rentManagement/         # 租约管理页面
│   │   ├── roomReview/             # 房源审核页面
│   │   └── userManagement/         # 用户管理页面
│   ├── App.vue               # 根组件
│   ├── env.d.ts              # 环境变量类型声明
│   └── main.ts               # 应用入口
├── .editorconfig             # 编辑器配置
├── .env.development          # 开发环境变量
├── .env.production           # 生产环境变量
├── .env.test                 # 测试环境变量
├── .eslintignore             # ESLint 忽略配置
├── .eslintrc.cjs             # ESLint 配置
├── .gitignore                # Git 忽略配置
├── .prettierignore           # Prettier 忽略配置
├── .prettierrc.json          # Prettier 配置
├── .stylelintignore          # Stylelint 忽略配置
├── .stylelintrc.cjs          # Stylelint 配置
├── commitlint.config.cjs     # Commitlint 配置
├── index.html                # HTML 入口
├── LICENSE                   # 开源协议
├── package.json              # 项目依赖配置
├── package-lock.json         # 依赖版本锁定
├── README.md                 # 项目说明文档
├── tsconfig.json             # TypeScript 配置
└── vite.config.ts            # Vite 配置
```

---

## 🎯 核心特性

### 开发体验

- ⚡️ **Vite 4** - 极速的开发服务器和构建工具
- 🔥 **热模块替换（HMR）** - 开发时即时更新，无需刷新页面
- 📦 **自动导入** - 组件和 API 自动按需导入
- 🎨 **TypeScript** - 完整的类型支持，提升开发效率

### 代码质量

- 🔍 **ESLint + Prettier** - 统一的代码风格和质量检查
- 🎯 **Stylelint** - CSS/LESS/SCSS 代码规范检查
- 🪝 **Husky + Lint-staged** - Git 提交前自动检查和格式化
- 📝 **Commitlint** - 规范化的 Git 提交信息

### 功能特性

- 🔐 **权限管理** - 基于角色的权限控制（RBAC）
- 🌐 **国际化（i18n）** - 支持多语言切换
- 🎨 **主题切换** - 支持亮色/暗色主题
- 📱 **响应式布局** - 适配不同屏幕尺寸
- 🔄 **请求拦截** - 统一的请求和响应处理
- 💾 **状态持久化** - Pinia 状态自动持久化
- 📊 **数据可视化** - 集成 ECharts 图表库
- 🗺️ **地图集成** - 集成高德地图 API

---

## 🔧 开发规范

### 代码提交规范

本项目使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```bash
<type>(<scope>): <subject>

# 示例
feat(user): 添加用户管理功能
fix(api): 修复接口请求错误
docs(readme): 更新项目文档
style(layout): 调整页面布局样式
refactor(utils): 重构工具函数
perf(table): 优化表格渲染性能
test(user): 添加用户模块测试
chore(deps): 更新依赖版本
```

**Type 类型说明：**

- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档更新
- `style`: 代码格式调整（不影响功能）
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具链相关

### 命名规范

- **文件命名**: 使用 kebab-case（短横线命名）
- **组件命名**: 使用 PascalCase（大驼峰命名）
- **变量/函数**: 使用 camelCase（小驼峰命名）
- **常量**: 使用 UPPER_SNAKE_CASE（大写下划线命名）

### 代码风格

项目已配置 ESLint、Prettier、Stylelint，提交代码前会自动格式化。

手动格式化命令：

```bash
# 格式化所有代码
npm run format

# 修复 ESLint 问题
npm run fix

# 修复样式问题
npm run lint:style
```

---

## 🌐 环境配置

### 开发环境

```env
# .env.development
VITE_APP_TITLE=租房管理系统-开发环境
VITE_APP_BASE_API=/api
VITE_SERVE=http://localhost:8080
```

### 生产环境

```env
# .env.production
VITE_APP_TITLE=租房管理系统
VITE_APP_BASE_API=/api
VITE_SERVE=http://your-production-domain.com
```

### 测试环境

```env
# .env.test
VITE_APP_TITLE=租房管理系统-测试环境
VITE_APP_BASE_API=/api
VITE_SERVE=http://your-test-domain.com
```

---

## 📚 API 文档

### 后端接口文档地址

- 开发环境: http://localhost:8080/doc.html
- 测试环境: http://139.198.163.91:8080/doc.html
- 生产环境: http://139.198.127.41:8080/doc.html

### 主要接口模块

- `/admin/login` - 登录认证
- `/admin/apartment` - 公寓管理
- `/admin/room` - 房源管理
- `/admin/user` - 用户管理
- `/admin/lease` - 租约管理
- `/admin/review` - 房源审核
- `/admin/landlord` - 房东管理

---

## 🔐 权限说明

### 用户角色

系统支持三种用户角色：

1. **官方人员（系统管理员）**
   - 拥有最高权限
   - 可以管理所有模块
   - 可以审核房源
   - 可以管理房东权限

2. **房东**
   - 通过移动端管理自己的房源
   - 查看自己的租约和申请
   - 需要官方人员授权才能发布房源

3. **租客**
   - 通过移动端浏览房源
   - 预约看房
   - 提交租房申请

### 权限控制

- 路由级别权限控制
- 按钮级别权限控制
- API 接口权限验证
- Token 认证机制

---

## 🐛 常见问题

### 1. 依赖安装失败

```bash
# 清除缓存后重新安装
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### 2. 端口被占用

修改 `vite.config.ts` 中的端口配置：

```typescript
export default defineConfig({
  server: {
    port: 5174, // 修改为其他端口
  }
})
```

### 3. 跨域问题

在 `vite.config.ts` 中配置代理：

```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

---

## 📝 更新日志

### Version 1.0.0 (2024)

**核心功能**
- ✅ 完整的后台管理系统框架
- ✅ 公寓管理、房源管理、用户管理
- ✅ 租约管理、房源审核功能
- ✅ 房东权限管理系统
- ✅ 直接入住申请处理
- ✅ 数据统计和可视化

**技术特性**
- ✅ Vue 3 + TypeScript + Vite
- ✅ Element Plus UI 组件库
- ✅ Pinia 状态管理
- ✅ Vue Router 4 路由管理
- ✅ Axios 请求封装
- ✅ 代码规范和质量检查工具链

---

## 📄 开源协议

本项目基于 [MIT License](./LICENSE) 开源协议。

---

## 👥 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📞 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 📧 Email: your-email@example.com
- 💬 Issue: [GitHub Issues](https://github.com/your-repo/issues)

---

**⭐ 如果这个项目对你有帮助，请给一个 Star！**
