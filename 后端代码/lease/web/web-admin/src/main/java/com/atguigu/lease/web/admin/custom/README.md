# Web-Admin Custom 模块文档

## 模块概述
Custom 模块是 web-admin（后台管理系统）的自定义配置模块，包含了系统的核心配置、拦截器、转换器等组件，负责系统的基础架构和通用功能。

## 目录结构

### 1. config 包 - 配置类
#### **Knife4jConfiguration.java** - API 文档配置
**功能说明：**
- 配置 Knife4j（Swagger UI 增强版）API 文档生成
- 定义 OpenAPI 基本信息（标题、版本、描述）
- 按功能模块分组管理 API 接口

**主要配置：**
- **系统信息管理组** - `/admin/system/**` 路径下的接口
- **后台登录管理组** - `/admin/login/**` 和 `/admin/info` 接口
- **公寓信息管理组** - 公寓、房间、标签、设施等相关接口
- **租赁信息管理组** - 预约、协议等租赁相关接口
- **平台用户管理组** - `/admin/user/**` 用户管理接口

**使用场景：**
- 开发阶段的 API 调试和测试
- 接口文档的自动生成和展示
- 前后端联调时的接口规范参考

#### **WebMvcConfiguration.java** - Spring MVC 配置
**功能说明：**
- 实现 WebMvcConfigurer 接口，自定义 Spring MVC 配置
- 注册自定义转换器和拦截器
- 配置系统的请求处理流程

**主要配置：**
- **转换器注册** - 注册字符串到枚举的转换器工厂
- **拦截器注册** - 配置身份认证拦截器
  - 拦截路径：`/admin/**`（所有后台管理接口）
  - 排除路径：`/admin/login/**`（登录相关接口不需要认证）

**作用：**
- 自动处理请求参数中的枚举类型转换
- 统一处理用户身份认证和授权

### 2. converter 包 - 类型转换器
#### **StringToBaseEnumConverterFactory.java** - 枚举转换器工厂
**功能说明：**
- 实现 ConverterFactory 接口，提供字符串到枚举的转换功能
- 支持所有继承 BaseEnum 接口的枚举类型
- 根据枚举的 code 值进行匹配转换

**转换逻辑：**
1. 接收字符串类型的 code 参数
2. 遍历目标枚举类型的所有常量
3. 通过 `getCode()` 方法匹配对应的枚举值
4. 找到匹配项则返回对应枚举，否则抛出异常

**使用场景：**
- HTTP 请求参数中的枚举类型自动转换
- 前端传递的数字代码自动映射为后端枚举对象
- 简化控制器中的参数处理逻辑

**示例：**
```java
// 前端传递: status=1
// 自动转换为: BaseStatus.ENABLE
```

### 3. interceptor 包 - 拦截器
#### **AuthenticationInterceptor.java** - 身份认证拦截器
**功能说明：**
- 实现 HandlerInterceptor 接口，处理用户身份认证
- 在请求处理前验证用户登录状态
- 在请求完成后清理线程上下文

**执行流程：**

**preHandle（请求前处理）：**
1. 从请求头中获取 `access-token`
2. 使用 JwtUtil 解析 token 获取用户信息
3. 提取 userId 和 username
4. 创建 LoginUser 对象并存储到 ThreadLocal
5. 返回 true 表示放行请求

**afterCompletion（请求完成后处理）：**
1. 清理 ThreadLocal 中的用户信息
2. 防止内存泄漏

**异常处理：**
- token 为空或无效时，JwtUtil 会抛出 LeaseException
- 由全局异常处理器统一处理并返回错误响应

**安全机制：**
- 所有后台管理接口都需要通过身份认证
- 登录接口被排除在外，允许匿名访问
- 使用 JWT token 进行无状态认证

## 模块间协作关系

### 1. 配置加载顺序
```
WebMvcConfiguration → 注册转换器和拦截器
↓
StringToBaseEnumConverterFactory → 处理枚举转换
↓
AuthenticationInterceptor → 处理身份认证
↓
Knife4jConfiguration → 生成API文档
```

### 2. 请求处理流程
```
HTTP请求 → AuthenticationInterceptor（身份验证）
↓
StringToBaseEnumConverterFactory（参数转换）
↓
Controller（业务处理）
↓
AuthenticationInterceptor.afterCompletion（清理上下文）
```

## 依赖关系
- **依赖 common 模块**：使用 JwtUtil、LoginUser、LoginUserHolder 等
- **依赖 model 模块**：使用 BaseEnum 接口
- **被 controller 层使用**：提供认证和转换支持

## 配置要点

### 1. JWT Token 配置
- Token 通过 HTTP 请求头 `access-token` 传递
- Token 解析失败会抛出相应异常
- 用户信息存储在 ThreadLocal 中，确保线程安全

### 2. 枚举转换配置
- 支持所有实现 BaseEnum 接口的枚举类型
- 转换基于枚举的 code 值进行匹配
- 转换失败会抛出 IllegalArgumentException

### 3. API 文档配置
- 按业务模块分组展示接口
- 支持在线测试和调试
- 自动生成接口参数和响应格式说明

## 使用示例

### 1. 获取当前登录用户
```java
@RestController
public class SomeController {
    
    @GetMapping("/test")
    public Result test() {
        // 通过拦截器设置的用户信息
        LoginUser currentUser = LoginUserHolder.getLoginUser();
        return Result.ok(currentUser);
    }
}
```

### 2. 枚举参数自动转换
```java
@GetMapping("/list")
public Result list(@RequestParam BaseStatus status) {
    // 前端传递 status=1，自动转换为 BaseStatus.ENABLE
    return Result.ok();
}
```

## 注意事项
1. **线程安全**：LoginUserHolder 使用 ThreadLocal，确保多线程环境下的用户信息隔离
2. **内存管理**：拦截器在请求完成后会清理 ThreadLocal，防止内存泄漏
3. **异常处理**：认证失败和转换失败都有相应的异常处理机制
4. **性能考虑**：拦截器会对所有后台接口生效，JWT 解析有一定性能开销
5. **安全性**：确保 JWT 密钥的安全性，定期更新 token 过期时间