# Common 模块文档

## 模块概述
Common 模块是整个租房系统的公共模块，包含了系统中各个模块共享的工具类、配置类、常量、异常处理等基础组件。

## 目录结构

### 1. constant 包 - 常量定义
- **LandlordPermissions.java** - 房东权限常量定义
  - 定义了房东在系统中的各种权限常量
  - 包含基础权限、高级权限和所有权限的数组定义
  - 用于权限验证和权限管理功能

- **RedisConstant.java** - Redis 缓存常量
  - 定义了 Redis 缓存的 key 前缀和过期时间
  - 包含登录验证码、用户登录状态等缓存配置
  - 统一管理缓存相关的常量，避免硬编码

### 2. exception 包 - 异常处理
- **LeaseException.java** - 自定义业务异常类
  - 继承 RuntimeException，用于处理业务逻辑异常
  - 包含错误码和错误信息
  - 支持通过 ResultCodeEnum 创建异常

- **GlobalExceptionHandler.java** - 全局异常处理器
  - 使用 @ControllerAdvice 注解，全局捕获异常
  - 处理系统异常和自定义业务异常
  - 统一返回格式化的错误响应

### 3. login 包 - 登录相关
- **LoginUser.java** - 登录用户信息实体
  - 存储当前登录用户的基本信息
  - 包含用户ID、用户名、用户类型、状态等字段
  - 支持官方人员、房东、租客三种用户类型

- **LoginUserHolder.java** - 登录用户上下文持有者
  - 使用 ThreadLocal 存储当前线程的登录用户信息
  - 提供设置、获取、清除登录用户的方法
  - 确保线程安全的用户信息传递

### 4. minio 包 - 对象存储配置
- **MinioConfiguration.java** - MinIO 配置类
  - 配置 MinIO 客户端 Bean
  - 支持条件化配置，只有配置了 endpoint 才生效
  - 用于文件上传和存储功能

- **MinioProperties.java** - MinIO 属性配置
  - 定义 MinIO 连接相关的配置属性
  - 包含 endpoint、accessKey、secretKey 等配置

### 5. mybatisplus 包 - MyBatis Plus 配置
- **MybatisPlusConfiguration.java** - MyBatis Plus 配置类
  - 配置分页插件，支持 MySQL 数据库
  - 配置 Mapper 扫描路径
  - 提供 MyBatis Plus 的基础配置

- **MybatisMetaObjectHandler.java** - 字段自动填充处理器
  - 实现 MetaObjectHandler 接口
  - 自动填充 createTime 和 updateTime 字段
  - 简化数据库操作时的时间字段管理

### 6. redis 包 - Redis 配置
- **RedisConfiguration.java** - Redis 配置类
  - 配置 RedisTemplate Bean
  - 设置 key 和 value 的序列化方式
  - 提供 Redis 操作的基础配置

### 7. result 包 - 统一返回结果
- **Result.java** - 统一返回结果类
  - 封装 API 返回的标准格式
  - 包含 code、message、data 三个字段
  - 提供成功和失败的静态方法

- **ResultCodeEnum.java** - 返回结果状态码枚举
  - 定义系统中所有的状态码和对应消息
  - 包含成功、失败、参数错误、服务异常等状态
  - 分类管理不同模块的错误码

### 8. sms 包 - 短信服务配置
- **AliyunSmsConfiguration.java** - 阿里云短信配置类
  - 配置阿里云短信客户端 Bean
  - 支持条件化配置
  - 用于发送验证码和通知短信

- **AliyunSMSProperties.java** - 阿里云短信属性配置
  - 定义阿里云短信服务的配置属性
  - 包含 accessKeyId、accessKeySecret、endpoint 等配置

### 9. utils 包 - 工具类
- **JwtUtil.java** - JWT 工具类
  - 提供 JWT token 的创建和解析功能
  - 包含用户认证和授权相关的方法
  - 处理 token 过期和无效的异常

- **CodeUtil.java** - 验证码工具类
  - 生成指定长度的随机数字验证码
  - 用于短信验证码和图形验证码生成
  - 提供简单的随机码生成功能

## 使用说明

### 1. 异常处理
```java
// 抛出业务异常
throw new LeaseException(ResultCodeEnum.PARAM_ERROR);

// 自定义异常信息
throw new LeaseException(500, "自定义错误信息");
```

### 2. 登录用户信息
```java
// 设置登录用户
LoginUserHolder.setLoginUser(loginUser);

// 获取当前登录用户
LoginUser currentUser = LoginUserHolder.getLoginUser();

// 清除登录用户信息
LoginUserHolder.clear();
```

### 3. 统一返回结果
```java
// 成功返回
return Result.ok(data);

// 失败返回
return Result.fail("操作失败");

// 自定义状态码返回
return Result.build(data, ResultCodeEnum.SUCCESS);
```

### 4. JWT 操作
```java
// 创建 token
String token = JwtUtil.createToken(userId, username);

// 解析 token
Claims claims = JwtUtil.parseToken(token);
```

## 依赖关系
- 该模块被 web-admin 和 web-app 模块依赖
- 提供了系统的基础功能和公共组件
- 包含了第三方服务的配置（MinIO、Redis、阿里云短信等）

## 注意事项
1. 修改常量定义时需要考虑对其他模块的影响
2. 异常处理器会全局生效，修改时需谨慎
3. 配置类支持条件化加载，确保配置文件正确
4. 工具类方法应保持无状态，确保线程安全