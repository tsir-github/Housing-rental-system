import type { Router } from 'vue-router';
import { UserType } from '@/api/common/user/types';
import { UserTypeUtils } from '@/utils/userType';

/**
 * 路由跳转辅助工具
 */
export class RouteHelper {
  /**
   * 根据用户类型跳转到对应的首页
   */
  static navigateToHome(router: Router, userType?: UserType) {
    const targetUserType = userType || UserTypeUtils.getCurrentUserType();
    if (!targetUserType) {
      router.push('/login');
      return;
    }

    const homePath = UserTypeUtils.getDefaultHomePath(targetUserType);
    router.push(homePath);
  }

  /**
   * 检查路由权限并跳转
   */
  static checkRoutePermission(router: Router, to: any): boolean {
    const currentUserType = UserTypeUtils.getCurrentUserType();
    
    // 如果没有登录，跳转到登录页
    if (!currentUserType) {
      router.push({
        path: '/login',
        query: { redirect: to.fullPath }
      });
      return false;
    }

    // 检查路由是否需要特定用户类型
    const requiredUserType = to.meta?.userType;
    if (requiredUserType && requiredUserType !== currentUserType) {
      // 用户类型不匹配，跳转到对应首页
      this.navigateToHome(router, currentUserType);
      return false;
    }

    // 检查路由是否需要特定权限
    const requiredPermissions = to.meta?.permissions;
    if (requiredPermissions && Array.isArray(requiredPermissions)) {
      const hasPermission = requiredPermissions.some(permission => 
        UserTypeUtils.hasPermission(permission)
      );
      
      if (!hasPermission) {
        // 权限不足，跳转到首页
        this.navigateToHome(router, currentUserType);
        return false;
      }
    }

    return true;
  }

  /**
   * 获取面包屑导航
   */
  static getBreadcrumbs(route: any): Array<{ name: string; path?: string }> {
    const breadcrumbs: Array<{ name: string; path?: string }> = [];
    
    // 根据用户类型添加首页面包屑
    const currentUserType = UserTypeUtils.getCurrentUserType();
    if (currentUserType) {
      const homeTitle = currentUserType === UserType.TENANT ? '租客首页' : '房东控制台';
      const homePath = UserTypeUtils.getDefaultHomePath(currentUserType);
      breadcrumbs.push({ name: homeTitle, path: homePath });
    }

    // 添加当前页面面包屑
    if (route.meta?.title) {
      breadcrumbs.push({ name: route.meta.title });
    }

    return breadcrumbs;
  }

  /**
   * 生成带权限检查的路由配置
   */
  static createProtectedRoute(config: {
    path: string;
    name: string;
    component: any;
    meta?: {
      title?: string;
      userType?: UserType;
      permissions?: string[];
      requiresAuth?: boolean;
    };
  }) {
    return {
      ...config,
      meta: {
        requiresAuth: true,
        ...config.meta
      },
      beforeEnter: (to: any, from: any, next: any) => {
        // 这里可以添加路由级别的权限检查
        // 实际的权限检查会在全局路由守卫中进行
        next();
      }
    };
  }
}

/**
 * 路由元信息类型定义
 */
export interface RouteMeta {
  title?: string;
  userType?: UserType;
  permissions?: string[];
  requiresAuth?: boolean;
  keepAlive?: boolean;
  showTabbar?: boolean;
}