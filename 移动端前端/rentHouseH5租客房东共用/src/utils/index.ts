/**
 * Utils 工具库统一导出
 * 提供项目中常用的工具函数和类
 */

// ==================== 通用工具函数 ====================

/**
 * 递归树结构，修改树结构的属性的key
 * @param data 树结构数据
 * @param key 要修改的key
 * @param value 要修改的key的值
 * @param childrenName 子节点的key名称
 * @param joinPropName 要拼接的key的名称
 */
export function recursionTree(
  data: any[],
  key: string,
  value: string,
  childrenName = "children",
  joinPropName?: string
) {
  data.forEach(item => {
    if (item[value]) {
      item[key] = item[value];
      delete item[value];
    }
    if (joinPropName) {
      item[key] = item[key] + "|" + item[joinPropName];
    }
    if (item[childrenName]) {
      recursionTree(item[childrenName], key, value, childrenName, joinPropName);
    }
  });
}

// ==================== HTTP 相关 ====================
export { default as http } from './http';

// ==================== Token 管理 ====================
export { getToken, setToken, removeToken, hasToken } from './token';
export { TokenChecker } from './tokenChecker';
export { AuthHandler } from './authHandler';

// ==================== 类型判断 ====================
export * from './is';

// ==================== 页面状态管理 ====================
export { PageStateManager } from './pageStateManager';

// ==================== 用户类型 ====================
export * from './userType';

// ==================== 导航辅助 ====================
export { NavigationHelper } from './navigationHelper';

// ==================== 缓存管理 ====================
export { CacheManager } from './cacheManager';

// ==================== 其他工具 ====================
export { default as NProgress } from './progress';
export { default as setPageTitle } from './set-page-title';
