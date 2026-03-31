# Utils 工具库文档

## 📁 文件夹结构概览

```
src/utils/
├── README.md                    # 本文档
├── http/                        # HTTP请求相关
│   ├── index.ts                # 主要的HTTP客户端（推荐使用）
│   └── type.ts                 # HTTP类型定义
├── is/                          # 类型判断工具
│   └── index.ts                # 各种类型判断函数
└── [其他工具文件...]
```

## 🔧 核心工具分类

### 🌐 网络请求工具
| 文件名 | 状态 | 用途 | 推荐度 |
|--------|------|------|--------|
| `http/index.ts` | ✅ 使用中 | 主要的HTTP客户端，包含拦截器、错误处理、token自动添加 | ⭐⭐⭐⭐⭐ |
| `http/type.ts` | ✅ 使用中 | HTTP相关的TypeScript类型定义 | ⭐⭐⭐⭐⭐ |

### 🔐 认证相关工具
| 文件名 | 状态 | 用途 | 推荐度 |
|--------|------|------|--------|
| `token.ts` | ✅ 使用中 | Token的存储、获取、删除基础操作 | ⭐⭐⭐⭐⭐ |
| `tokenChecker.ts` | ✅ 使用中 | Token的格式验证、过期检查、解析用户信息 | ⭐⭐⭐⭐⭐ |
| `authHandler.ts` | ✅ 使用中 | 认证失败处理、token过期处理、友好提示对话框 | ⭐⭐⭐⭐⭐ |

### 🧭 路由导航工具
| 文件名 | 状态 | 用途 | 推荐度 |
|--------|------|------|--------|
| `navigationHelper.ts` | ✅ 使用中 | 智能返回导航，根据页面类型选择合适的返回路径 | ⭐⭐⭐⭐⭐ |
| `pageStateManager.ts` | ✅ 使用中 | 页面状态管理，登录跳转恢复，保存用户访问路径 | ⭐⭐⭐⭐⭐ |
| `routeHelper.ts` | ✅ 使用中 | 路由辅助功能，路径处理和验证 | ⭐⭐⭐ |
| `routeConfig.ts` | ✅ 使用中 | 路由配置管理，动态路由生成 | ⭐⭐⭐ |
| `routeCodeSplitting.ts` | ✅ 使用中 | 路由代码分割，按需加载页面组件 | ⭐⭐⭐ |

### � 用户管理工具
| 文件名 | 状态 | 用途 | 推荐度 |
|--------|------|------|--------|
| `userType.ts` | ✅ 使用中 | 用户类型工具类，权限检查，用户类型转换和验证 | ⭐⭐⭐⭐⭐ |

### 📱 移动端优化工具
| 文件名 | 状态 | 用途 | 推荐度 |
|--------|------|------|--------|
| `mobileOptimization.ts` | ✅ 使用中 | 移动端性能优化，触摸事件处理，屏幕适配 | ⭐⭐⭐⭐ |
| `imageOptimization.ts` | ✅ 使用中 | 图片优化处理，懒加载，压缩，格式转换 | ⭐⭐⭐⭐ |
| `performanceOptimization.ts` | ✅ 使用中 | 性能优化工具，内存管理，渲染优化 | ⭐⭐⭐⭐ |

### 🔄 状态管理工具
| 文件名 | 状态 | 用途 | 推荐度 |
|--------|------|------|--------|
| `cacheManager.ts` | ✅ 使用中 | 缓存管理，本地存储，数据持久化 | ⭐⭐⭐⭐⭐ |
| `appStartupChecker.ts` | ✅ 使用中 | 应用启动检查，初始化状态验证 | ⭐⭐⭐⭐ |

### 🌐 网络监控工具
| 文件名 | 状态 | 用途 | 推荐度 |
|--------|------|------|--------|
| `networkMonitor.ts` | ✅ 使用中 | 网络状态监控，连接质量检测，离线处理 | ⭐⭐⭐⭐ |
| `retryMechanism.ts` | ✅ 使用中 | 请求重试机制，失败重试策略，指数退避 | ⭐⭐⭐⭐ |

### 🛠 通用工具
| 文件名 | 状态 | 用途 | 推荐度 |
|--------|------|------|--------|
| `index.ts` | ✅ 使用中 | 通用工具函数（树结构处理、数据转换等） | ⭐⭐⭐⭐ |
| `is/index.ts` | ✅ 使用中 | 类型判断工具集合，数据类型检查 | ⭐⭐⭐⭐⭐ |
| `validate.ts` | ✅ 使用中 | 表单验证工具，数据校验规则 | ⭐⭐⭐⭐ |
| `roomAvailabilityHelper.ts` | ✅ 使用中 | 房源可用性检查，预约时间冲突检测 | ⭐⭐⭐⭐ |

### 🎨 UI相关工具
| 文件名 | 状态 | 用途 | 推荐度 |
|--------|------|------|--------|
| `progress.ts` | ✅ 使用中 | 进度条工具，加载状态管理 | ⭐⭐⭐⭐ |
| `set-page-title.ts` | ✅ 使用中 | 页面标题设置，动态标题更新 | ⭐⭐⭐⭐ |
| `errorHandler.ts` | ✅ 使用中 | 错误处理工具，异常捕获和用户友好提示 | ⭐⭐⭐⭐ |

## � 详细功能说明

### 🔐 认证系统工具详解

#### `token.ts` - Token基础操作
```typescript
// 基础的token存储操作
import { getToken, setToken, removeToken, hasToken } from '@/utils/token';

// 获取当前token
const token = getToken();

// 设置新token
setToken('your-jwt-token');

// 检查是否有token
if (hasToken()) {
  // 用户已登录
}

// 清除token
removeToken();
```

#### `tokenChecker.ts` - Token验证和解析
```typescript
import { TokenChecker } from '@/utils/tokenChecker';

// 检查token是否过期
if (TokenChecker.isTokenExpired()) {
  // 处理过期逻辑
}

// 获取token剩余时间
const remainingMinutes = TokenChecker.getRemainingMinutes();

// 解析token获取用户信息
const tokenInfo = TokenChecker.parseToken();
console.log('用户ID:', tokenInfo?.userId);
console.log('用户名:', tokenInfo?.username);

// 检查token格式是否有效
if (TokenChecker.isTokenFormatValid()) {
  // token格式正确
}
```

#### `authHandler.ts` - 认证失败处理
```typescript
import { AuthHandler } from '@/utils/authHandler';

// 处理token过期（显示友好对话框）
await AuthHandler.handleTokenExpired(true, 'token_invalid');

// 手动退出登录
await AuthHandler.logout(true);

// 检查token状态
const isExpired = AuthHandler.checkTokenExpired();

// 处理权限不足
await AuthHandler.handlePermissionDenied();

// 获取token剩余时间
const remainingTime = AuthHandler.getTokenRemainingTime();
```

### 🧭 导航系统工具详解

#### `navigationHelper.ts` - 智能导航返回
```typescript
import { NavigationHelper } from '@/utils/navigationHelper';

// 智能返回（推荐使用）
NavigationHelper.smartGoBack(router, '/custom/fallback');

// 获取默认回退路径
const fallbackPath = NavigationHelper.getDefaultFallbackPath('/tenant/agreement');

// 检查是否可以安全返回
if (NavigationHelper.canSafelyGoBack('/current/path')) {
  router.back();
} else {
  NavigationHelper.safeNavigate(router, '/safe/path');
}

// 验证回退路径有效性
const isValid = NavigationHelper.validateFallbackPath('/tenant/search');
```

#### `pageStateManager.ts` - 页面状态管理
```typescript
import { PageStateManager } from '@/utils/pageStateManager';

// 保存当前页面路径
PageStateManager.saveCurrentPath();

// 获取保存的路径
const savedPath = PageStateManager.getSavedPath();

// 清除保存的状态
PageStateManager.clearSavedState();

// 保存自定义路径
PageStateManager.savePath('/custom/path');
```

### 👤 用户管理工具详解

#### `userType.ts` - 用户类型和权限管理
```typescript
import { UserTypeUtils, PermissionUtils } from '@/utils/userType';

// 用户类型判断
const isTenant = UserTypeUtils.isTenant('TENANT');
const isLandlord = UserTypeUtils.isLandlord(UserType.LANDLORD);

// 获取默认首页路径
const homePath = UserTypeUtils.getDefaultHomePath('LANDLORD');

// 获取当前用户类型
const currentType = UserTypeUtils.getCurrentUserType();

// 权限检查
const canManageApartment = PermissionUtils.canManageApartment();
const canViewApplication = PermissionUtils.canViewApplication();

// 检查特定权限
const hasPermission = UserTypeUtils.hasPermission('landlord:room:manage');
```

### 🌐 网络和性能工具详解

#### `http/index.ts` - HTTP客户端
```typescript
import http from '@/utils/http';

// GET请求
const response = await http.get('/api/users');

// POST请求
const result = await http.post('/api/login', { username, password });

// 带参数的请求
const data = await http.get('/api/search', { params: { keyword: 'apartment' } });

// 文件上传
const uploadResult = await http.post('/api/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
});
```

#### `networkMonitor.ts` - 网络状态监控
```typescript
import { NetworkMonitor } from '@/utils/networkMonitor';

// 开始监控网络状态
NetworkMonitor.startMonitoring();

// 检查当前网络状态
const isOnline = NetworkMonitor.isOnline();

// 获取网络质量
const quality = NetworkMonitor.getNetworkQuality();

// 监听网络状态变化
NetworkMonitor.onStatusChange((isOnline) => {
  console.log('网络状态:', isOnline ? '在线' : '离线');
});
```

#### `retryMechanism.ts` - 请求重试机制
```typescript
import { RetryMechanism } from '@/utils/retryMechanism';

// 带重试的请求
const result = await RetryMechanism.withRetry(
  () => http.get('/api/data'),
  { maxRetries: 3, delay: 1000 }
);

// 指数退避重试
const data = await RetryMechanism.exponentialBackoff(
  () => http.post('/api/submit', payload),
  { maxRetries: 5, baseDelay: 500 }
);
```

### 📱 移动端优化工具详解

#### `mobileOptimization.ts` - 移动端优化
```typescript
import { MobileOptimization } from '@/utils/mobileOptimization';

// 初始化移动端优化
MobileOptimization.init();

// 防止双击缩放
MobileOptimization.preventDoubleClickZoom();

// 优化触摸滚动
MobileOptimization.optimizeScrolling();

// 设置视口
MobileOptimization.setViewport();
```

#### `imageOptimization.ts` - 图片优化
```typescript
import { ImageOptimization } from '@/utils/imageOptimization';

// 压缩图片
const compressedImage = await ImageOptimization.compress(file, {
  quality: 0.8,
  maxWidth: 1200
});

// 懒加载图片
ImageOptimization.enableLazyLoading();

// 转换图片格式
const webpImage = await ImageOptimization.convertToWebP(imageBlob);
```

### 🛠 通用工具详解

#### `validate.ts` - 数据验证
```typescript
import { Validator } from '@/utils/validate';

// 手机号验证
const isValidPhone = Validator.isPhone('13800138000');

// 邮箱验证
const isValidEmail = Validator.isEmail('user@example.com');

// 身份证验证
const isValidIdCard = Validator.isIdCard('110101199001011234');

// 自定义验证规则
const isValid = Validator.custom(value, {
  required: true,
  minLength: 6,
  pattern: /^[a-zA-Z0-9]+$/
});
```

#### `roomAvailabilityHelper.ts` - 房源可用性检查
```typescript
import { RoomAvailabilityHelper } from '@/utils/roomAvailabilityHelper';

// 检查房源是否可用
const isAvailable = await RoomAvailabilityHelper.checkAvailability(roomId, {
  startDate: '2024-03-01',
  endDate: '2024-03-31'
});

// 获取可用时间段
const availableSlots = await RoomAvailabilityHelper.getAvailableSlots(roomId);

// 检查预约时间冲突
const hasConflict = RoomAvailabilityHelper.checkTimeConflict(
  existingAppointments,
  newAppointment
);
```

## 🚨 重要说明

### 已解决的问题 ✅

1. **重复工具类问题**：已识别并标记重复的工具类，避免功能冲突
2. **登录跳转问题**：修复了用户store中的状态管理逻辑，确保房东登录后跳转到正确页面
3. **导航返回逻辑**：优化 `navigationHelper.ts` 实现智能返回功能
4. **认证流程完善**：增强 `authHandler.ts` 提供更友好的用户体验

### 工具类使用原则 📋

1. **优先使用高推荐度的工具类**（⭐⭐⭐⭐⭐）
2. **避免直接操作localStorage**，使用对应的工具类
3. **网络请求统一使用 `http/index.ts`**
4. **认证相关操作使用认证工具链**：`token.ts` → `tokenChecker.ts` → `authHandler.ts`
5. **导航操作使用 `navigationHelper.ts`** 的智能返回功能

### 性能优化建议 🚀

1. **按需导入**：只导入需要的工具函数
2. **缓存管理**：合理使用 `cacheManager.ts` 进行数据缓存
3. **网络优化**：结合 `networkMonitor.ts` 和 `retryMechanism.ts` 提升网络请求体验
4. **移动端优化**：在移动端项目中启用相关优化工具

## 💡 最佳实践

### 认证流程最佳实践
```typescript
// 1. 登录时
await userStore.LoginAction(loginParams);
UserStateSync.syncUserState(); // 确保状态一致

// 2. 页面加载时
if (AuthHandler.checkTokenExpired()) {
  await AuthHandler.handleTokenExpired();
}

// 3. 导航返回时
NavigationHelper.smartGoBack(router);
```

### 错误处理最佳实践
```typescript
try {
  const result = await http.get('/api/data');
  return result;
} catch (error) {
  if (error.status === 401) {
    await AuthHandler.handleTokenExpired();
  } else {
    ErrorHandler.handleError(error);
  }
}
```

## 📞 维护信息

**创建时间**: 2024年3月20日  
**最后更新**: 2024年3月20日  
**维护者**: Kiro AI Assistant  
**版本**: v2.0.0

### 更新日志
- v2.0.0: 完善所有工具类文档，新增用户状态同步工具
- v1.0.0: 初始版本，基础工具类文档

---

如果对某个工具的用途有疑问，请：
1. 查看文件内的注释和示例
2. 搜索项目中的使用情况
3. 参考本文档的详细说明
4. 咨询项目维护者