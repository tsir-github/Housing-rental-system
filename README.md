#公寓租赁管理系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.0+-brightgreen.svg"/>
  <img src="https://img.shields.io/badge/Vue-3.3.4-brightgreen.svg"/>
  <img src="https://img.shields.io/badge/MySQL-8.0+-blue.svg"/>
  <img src="https://img.shields.io/badge/Redis-7.0+-red.svg"/>
  <img src="https://img.shields.io/badge/License-MIT-green.svg"/>
</p>

## 📖 项目简介

公寓租赁管理系统是一个基于 **Spring Boot + Vue 3** 的全栈房屋租赁管理平台。系统采用前后端分离架构，支持**三种用户角色**（官方管理员、房东、租客），提供完整的房源管理、租约管理、预约看房、在线签约等功能。

> 本项目为毕业设计作品，展示了完整的企业级应用开发流程和最佳实践。

## ✨ 系统架构

### 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                      前端层 (Frontend)                    │
├──────────────────┬──────────────────┬───────────────────┤
│  后台管理系统前端  │   移动端 H5 应用   │                   │
│  Vue 3 + Element │  Vue 3 + Vant    │                   │
│  TypeScript      │  TypeScript      │                   │
│  Vite            │  Vite            │                   │
└──────────────────┴──────────────────┴───────────────────┘
                           ↓ HTTP/HTTPS
┌─────────────────────────────────────────────────────────┐
│                    后端层 (Backend)                       │
├──────────────────┬──────────────────┬───────────────────┤
│  Spring Boot 3.0 │  Spring Security │  MyBatis-Plus     │
│  Spring AI       │  JWT Token       │  Redis Cache      │
│  DeepSeek AI     │  MinIO 文件存储   │  高德地图 API      │
└──────────────────┴──────────────────┴───────────────────┘
                           ↓ JDBC
┌─────────────────────────────────────────────────────────┐
│                    数据层 (Database)                      │
├──────────────────┬──────────────────┬───────────────────┤
│  MySQL 8.0+      │  Redis 7.0+      │  MinIO 对象存储    │
└──────────────────┴──────────────────┴───────────────────┘
```

### 👥 用户角色

| 角色 | 说明 | 访问端 |
|------|------|--------|
| 🔑 官方管理员 | 系统管理员，拥有最高权限 | 后台管理系统 |
| 🏢 房东 | 房源发布者，管理自己的房源和租约 | 移动端 H5 |
| 👤 租客 | 租房用户，浏览房源、预约看房、签约租房 | 移动端 H5 |

## 📂 项目结构

```
Housing-rental-system/
├── 后端代码/                          # Spring Boot 后端项目
│   └── lease/
│       ├── common/                   # 公共模块（工具类、配置等）
│       ├── model/                    # 数据模型（实体类、枚举等）
│       └── web/                      # Web 模块
│           ├── web-admin/           # 后台管理系统接口
│           └── web-app/             # 移动端接口
│
├── 后台管理系统前端/                   # Vue 3 后台管理系统
│   └── rentHouseAdmin/
│       ├── src/
│       │   ├── api/                 # API 接口
│       │   ├── views/               # 页面组件
│       │   ├── components/          # 公共组件
│       │   ├── router/              # 路由配置
│       │   └── store/               # 状态管理
│       └── README.md                # 后台管理系统文档
│
├── 移动端前端/                        # Vue 3 移动端 H5 应用
│   └── rentHouseH5租客房东共用/
│       ├── src/
│       │   ├── api/                 # API 接口
│       │   │   ├── tenant/         # 租客接口
│       │   │   └── landlord/       # 房东接口
│       │   ├── views/               # 页面组件
│       │   │   ├── tenant/         # 租客页面
│       │   │   └── landlord/       # 房东页面
│       │   ├── components/          # 公共组件
│       │   ├── router/              # 路由配置
│       │   └── store/               # 状态管理
│       └── README.md                # 移动端应用文档
│
├── 后台管理系统前端开发进度/           # 后台开发文档
├── 移动端前端开发进度/                # 移动端开发文档
└── README.md                         # 项目总览文档（本文件）
```

## 🚀 核心功能

### 🔐 后台管理系统（官方管理员）

#### 公寓与房源管理
- 公寓信息管理（增删改查）
- 房间信息管理（增删改查）
- 房源审核管理（审核、打回、通过）
- 属性管理（配套设施、标签、房间属性）

#### 租赁业务管理
- 看房预约管理（查看、处理预约）
- 租约管理（创建、查看、修改、续约）
- 直接入住申请管理（审批流程）

#### 用户与权限管理
- 租客信息管理
- 房东权限管理（权限分配、审核）
- 系统用户管理（管理员账号）
- 岗位管理（岗位信息维护）

### 📱 移动端 H5 应用

#### 租客端功能
- **房源浏览**：智能搜索、地图找房、房源详情
- **AI 智能助手**：基于 DeepSeek AI 的智能房源推荐
- **预约看房**：在线预约、状态跟踪
- **直接入住申请**：快速申请、在线签约
- **租约管理**：查看租约、续约申请、租金缴纳
- **个人中心**：个人信息、浏览历史

#### 房东端功能
- **数据仪表板**：经营概览、收益分析、待办提醒
- **公寓管理**：创建、编辑、删除公寓
- **房源管理**：发布房源、审核状态、发布控制
- **租赁管理**：预约处理、申请审批、租约管理
- **基础数据**：配套设施、标签、属性、杂费管理

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.0+ | 核心框架 |
| Spring Security | 6.0+ | 安全框架 |
| MyBatis-Plus | 3.5+ | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存数据库 |
| Spring AI | 1.0+ | AI 集成框架 |
| DeepSeek AI | - | 智能推荐引擎 |
| MinIO | - | 对象存储服务 |
| JWT | - | Token 认证 |
| 高德地图 API | - | 地图服务 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.4 | 渐进式 JavaScript 框架 |
| TypeScript | 4.7+ | JavaScript 的超集 |
| Vite | 4.3.9 | 下一代前端构建工具 |
| Element Plus | 2.3.7 | 后台管理 UI 组件库 |
| Vant | 4.6.0 | 移动端 UI 组件库 |
| Pinia | 2.0+ | 状态管理库 |
| Vue Router | 4.2+ | 路由管理器 |
| Axios | 1.4+ | HTTP 客户端 |
| ECharts | 5.4+ | 数据可视化 |
| Tailwind CSS | 3.3+ | 原子化 CSS 框架 |

## 📦 快速开始

### 环境要求

**后端：**
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- MinIO（可选）

**前端：**
- Node.js 16+
- npm 8+ 或 pnpm 7+

### 后端启动

```bash
# 1. 进入后端目录
cd 后端代码/lease

# 2. 配置数据库
# 修改 web/web-admin/src/main/resources/application.yml
# 修改 web/web-app/src/main/resources/application.yml

# 3. 导入数据库
# 执行 SQL 脚本创建数据库和表

# 4. 启动 Redis
redis-server

# 5. 编译打包
mvn clean package

# 6. 启动后台管理系统接口
java -jar web/web-admin/target/web-admin.jar

# 7. 启动移动端接口
java -jar web/web-app/target/web-app.jar
```

### 前端启动

**后台管理系统：**
```bash
cd 后台管理系统前端/rentHouseAdmin
npm install
npm run dev
# 访问 http://localhost:5173
```

**移动端 H5：**
```bash
cd 移动端前端/rentHouseH5租客房东共用
npm install
npm run dev
# 访问 http://localhost:5173
```

## 🎯 核心亮点

### 🤖 AI 智能推荐
- 集成 DeepSeek AI，提供智能房源推荐
- 自然语言理解，精准匹配用户需求
- 对话式交互，提升用户体验

### 🗺️ 地图找房
- 高德地图集成
- 房源位置标注
- 地图搜索和定位

### 🔐 权限管理
- 基于角色的权限控制（RBAC）
- 三种用户角色分离
- JWT Token 认证机制

### 📊 数据可视化
- ECharts 图表展示
- 房东经营数据分析
- 租约统计和趋势

### 🚀 性能优化
- Redis 缓存策略（延迟双删）
- 前端路由懒加载
- 图片懒加载和压缩
- 虚拟列表优化

### 📱 移动端优化
- vw 视口适配
- 下拉刷新、上拉加载
- 骨架屏加载
- 网络状态监控

## 📚 文档导航

- [后台管理系统前端文档](./后台管理系统前端/rentHouseAdmin/README.md)
- [移动端 H5 应用文档](./移动端前端/rentHouseH5租客房东共用/README.md)
- [后台开发进度文档](./后台管理系统前端开发进度/README.md)
- [移动端开发进度文档](./移动端前端开发进度/)

## 🔗 相关链接

- **GitHub 仓库**：[https://github.com/tsir-github/Housing-rental-system](https://github.com/tsir-github/Housing-rental-system)
- **Spring Boot 文档**：[https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- **Vue 3 官方文档**：[https://cn.vuejs.org/](https://cn.vuejs.org/)
- **Element Plus 文档**：[https://element-plus.org/zh-CN/](https://element-plus.org/zh-CN/)
- **Vant 文档**：[https://vant-ui.github.io/vant/](https://vant-ui.github.io/vant/)

## 📧 联系方式

- **项目性质**：毕业设计项目
- **开发者邮箱**：rublw07524@outlook.com

## 📄 开源协议

本项目仅供学习交流使用。

---

⭐ 如果这个项目对你有帮助，欢迎 Star 支持！
