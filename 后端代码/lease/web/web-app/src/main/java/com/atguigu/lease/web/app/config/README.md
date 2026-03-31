# 移动端配置文件结构说明

## 📁 目录结构

```
config/
├── common/          # 共用配置
│   ├── WebMvcConfig.java           # 通用Web配置（身份认证拦截器）
│   ├── ApiDocConfig.java           # API文档配置（Swagger/Knife4j）
│   └── CommonBusinessConfig.java   # 共用业务规则配置
├── landlord/        # 房东专用配置
│   ├── LandlordConfig.java         # 房东拦截器配置
│   └── LandlordBusinessConfig.java # 房东业务规则配置
├── tenant/          # 租客专用配置
│   ├── TenantConfig.java           # 租客拦截器配置
│   └── TenantBusinessConfig.java   # 租客业务规则配置
└── README.md        # 配置说明文档
```

## 🔧 配置分类原则

### 1. **common/** - 共用配置
- 系统级通用配置
- 适用于所有用户类型的功能
- 基础的身份认证、CORS、静态资源、API文档等

### 2. **landlord/** - 房东专用配置
- 房东特有的业务配置
- 房东权限验证拦截器
- 房东相关的业务规则配置

### 3. **tenant/** - 租客专用配置
- 租客特有的业务配置
- 租客权限验证拦截器（如需要）
- 租客相关的业务规则配置

## 🚀 拦截器执行顺序

1. **WebMvcConfig** (order=1) - 通用身份认证
2. **LandlordConfig** (order=2) - 房东权限验证
3. **TenantConfig** (order=3) - 租客权限验证（预留）

## 📋 接口路径规范

- `/app/common/**` - 共用接口（不需要特殊权限）
- `/app/landlord/**` - 房东专用接口
- `/app/tenant/**` - 租客专用接口
- `/app/public/**` - 公开接口（无需登录）

## 🔍 配置文件职责

### WebMvcConfig.java
- JWT Token解析和验证
- 设置登录用户信息
- 基础的访问控制

### ApiDocConfig.java
- Swagger/Knife4j文档配置
- API分组管理（登录认证、租客功能、房东功能、共用功能）
- 接口文档展示配置

### LandlordConfig.java
- 房东身份验证
- 房东权限控制
- 房东业务规则

### TenantConfig.java
- 租客身份验证（如需要）
- 租客权限控制（如需要）
- 租客业务规则

### BusinessConfig类
- 业务常量定义
- 权限配置
- 业务规则限制
- 状态码定义

## 💡 扩展指南

如需添加新的配置：
1. 确定配置的适用范围（共用/房东/租客）
2. 在对应目录下创建配置类
3. 使用合适的order值设置执行顺序
4. 更新此文档说明

## 🔄 迁移说明

### 旧配置文件处理
- `LandlordPermissionConfig.java` → 拆分为 `WebMvcConfig.java` + `LandlordConfig.java`
- `custom/config/WebMvcConfiguration.java` → 合并到 `WebMvcConfig.java`
- `custom/config/Knife4jConfiguration.java` → 重构为 `ApiDocConfig.java`

### 优势
1. **职责清晰**：每个配置文件职责单一
2. **分类明确**：租客/房东/共用严格分离
3. **易于维护**：配置修改不会相互影响
4. **扩展性强**：可以轻松添加新的用户类型配置
5. **避免冲突**：不同用户类型的配置互不干扰