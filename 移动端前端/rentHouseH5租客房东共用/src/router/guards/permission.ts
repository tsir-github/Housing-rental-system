import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router';
import { UserTypeUtils, PermissionUtils } from '@/utils/userType';
import { showToast, showDialog } from 'vant';

/**
 * 权限检查路由守卫
 */
export function createPermissionGuard() {
  return async (
    to: RouteLocationNormalized,
    _from: RouteLocationNormalized,
    next: NavigationGuardNext
  ) => {
    // 检查路由是否需要权限验证
    if (!to.meta?.requiresAuth) {
      next();
      return;
    }

    // 检查用户是否已登录
    const currentUserType = UserTypeUtils.getCurrentUserType();
    if (!currentUserType) {
      showToast('请先登录');
      next('/login');
      return;
    }

    // 检查用户类型是否匹配
    if (to.meta.userType && to.meta.userType !== currentUserType) {
      showToast('用户类型不匹配');
      next(UserTypeUtils.getDefaultHomePath(currentUserType));
      return;
    }

    // 检查页面权限
    if (to.meta.permissions && Array.isArray(to.meta.permissions)) {
      const hasPermission = to.meta.permissions.some((permission: string) =>
        UserTypeUtils.hasPermission(permission)
      );

      if (!hasPermission) {
        // 权限不足，显示提示并跳转
        await showPermissionDeniedDialog(to.meta.permissions as string[]);
        next(UserTypeUtils.getDefaultHomePath(currentUserType));
        return;
      }
    }

    // 房东专用页面检查
    if (to.path.startsWith('/landlord') && !UserTypeUtils.isLandlord(currentUserType)) {
      showToast('此页面仅限房东访问');
      next('/');
      return;
    }

    // 租客专用页面检查
    if (to.path.startsWith('/tenant') && !UserTypeUtils.isTenant(currentUserType)) {
      showToast('此页面仅限租客访问');
      next('/');
      return;
    }

    next();
  };
}

/**
 * 显示权限不足对话框
 */
async function showPermissionDeniedDialog(requiredPermissions: string[]) {
  const permissionNames = requiredPermissions.map(permission => {
    // 简化权限名称显示
    const parts = permission.split(':');
    return parts.length > 1 ? parts[1] : permission;
  }).join('、');

  try {
    await showDialog({
      title: '权限不足',
      message: `访问此页面需要以下权限：${permissionNames}。请联系管理员申请相关权限。`,
      confirmButtonText: '我知道了',
      showCancelButton: false
    });
  } catch (error) {
    // 用户关闭对话框
  }
}

/**
 * 检查功能权限的工具函数
 */
export class RoutePermissionChecker {
  /**
   * 检查是否可以访问公寓管理
   */
  static canAccessApartmentManagement(): boolean {
    const currentUserType = UserTypeUtils.getCurrentUserType();
    return !!(currentUserType && UserTypeUtils.isLandlord(currentUserType) &&
      PermissionUtils.canManageApartment());
  }

  /**
   * 检查是否可以访问房源管理
   */
  static canAccessRoomManagement(): boolean {
    const currentUserType = UserTypeUtils.getCurrentUserType();
    return !!(currentUserType && UserTypeUtils.isLandlord(currentUserType) &&
      PermissionUtils.canManageRoom());
  }

  /**
   * 检查是否可以访问申请管理
   */
  static canAccessApplicationManagement(): boolean {
    const currentUserType = UserTypeUtils.getCurrentUserType();
    return !!(currentUserType && UserTypeUtils.isLandlord(currentUserType) &&
      UserTypeUtils.hasPermission(PermissionUtils.LANDLORD_PERMISSIONS.APPLICATION_VIEW));
  }

  /**
   * 检查是否可以访问预约管理
   */
  static canAccessAppointmentManagement(): boolean {
    const currentUserType = UserTypeUtils.getCurrentUserType();
    return !!(currentUserType && UserTypeUtils.isLandlord(currentUserType) &&
      UserTypeUtils.hasPermission(PermissionUtils.LANDLORD_PERMISSIONS.APPOINTMENT_VIEW));
  }

  /**
   * 检查是否可以访问审核管理
   */
  static canAccessReviewManagement(): boolean {
    const currentUserType = UserTypeUtils.getCurrentUserType();
    return !!(currentUserType && UserTypeUtils.isLandlord(currentUserType) &&
      UserTypeUtils.hasPermission(PermissionUtils.LANDLORD_PERMISSIONS.REVIEW_VIEW));
  }

  /**
   * 检查是否可以访问其他管理功能
   */
  static canAccessOtherManagement(): boolean {
    const currentUserType = UserTypeUtils.getCurrentUserType();
    return !!(currentUserType && UserTypeUtils.isLandlord(currentUserType) && (
      UserTypeUtils.hasPermission(PermissionUtils.LANDLORD_PERMISSIONS.ATTR_MANAGE) ||
      UserTypeUtils.hasPermission(PermissionUtils.LANDLORD_PERMISSIONS.FACILITY_MANAGE) ||
      UserTypeUtils.hasPermission(PermissionUtils.LANDLORD_PERMISSIONS.FEE_MANAGE)
    ));
  }
}