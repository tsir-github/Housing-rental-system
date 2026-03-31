import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showDialog } from 'vant';
import { UserTypeUtils, PermissionUtils } from '@/utils/userType';

/**
 * 权限检查组合式函数
 */
export function usePermission() {
  const router = useRouter();

  // 响应式权限状态
  const isLandlord = computed(() => UserTypeUtils.isLandlord());
  const isTenant = computed(() => UserTypeUtils.isTenant());
  const currentUserType = computed(() => UserTypeUtils.getCurrentUserType());

  /**
   * 检查是否有特定权限
   */
  const hasPermission = (permission: string | string[]): boolean => {
    if (Array.isArray(permission)) {
      return permission.some(p => UserTypeUtils.hasPermission(p));
    }
    return UserTypeUtils.hasPermission(permission);
  };

  /**
   * 检查房东权限
   */
  const hasLandlordPermission = (permission: string): boolean => {
    return PermissionUtils.hasLandlordPermission(permission);
  };

  /**
   * 权限检查装饰器函数
   */
  const withPermission = async (
    permission: string | string[],
    action: () => void | Promise<void>,
    options?: {
      showToast?: boolean;
      showDialog?: boolean;
      redirectOnFail?: boolean;
    }
  ) => {
    const opts = {
      showToast: true,
      showDialog: false,
      redirectOnFail: false,
      ...options
    };

    if (!hasPermission(permission)) {
      if (opts.showDialog) {
        await showPermissionDialog(permission);
      } else if (opts.showToast) {
        showToast('权限不足');
      }

      if (opts.redirectOnFail && currentUserType.value) {
        const homePath = UserTypeUtils.getDefaultHomePath(currentUserType.value);
        router.push(homePath);
      }
      return;
    }

    await action();
  };

  /**
   * 显示权限不足对话框
   */
  const showPermissionDialog = async (permission: string | string[]) => {
    const permissions = Array.isArray(permission) ? permission : [permission];
    const permissionNames = permissions.map(p => {
      const parts = p.split(':');
      return parts.length > 1 ? parts[1] : p;
    }).join('、');

    try {
      await showDialog({
        title: '权限不足',
        message: `此操作需要以下权限：${permissionNames}。请联系管理员申请相关权限。`,
        confirmButtonText: '我知道了',
        showCancelButton: false
      });
    } catch (error) {
      // 用户关闭对话框
    }
  };

  /**
   * 房东功能权限检查
   */
  const landlordPermissions = {
    // 公寓管理
    canViewApartment: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.APARTMENT_VIEW)
    ),
    canCreateApartment: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.APARTMENT_CREATE)
    ),
    canEditApartment: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.APARTMENT_EDIT)
    ),
    canDeleteApartment: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.APARTMENT_DELETE)
    ),

    // 房源管理
    canViewRoom: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.ROOM_VIEW)
    ),
    canCreateRoom: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.ROOM_CREATE)
    ),
    canEditRoom: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.ROOM_EDIT)
    ),
    canDeleteRoom: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.ROOM_DELETE)
    ),
    canPublishRoom: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.ROOM_PUBLISH)
    ),

    // 申请管理
    canViewApplication: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.APPLICATION_VIEW)
    ),
    canProcessApplication: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.APPLICATION_PROCESS)
    ),

    // 预约管理
    canViewAppointment: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.APPOINTMENT_VIEW)
    ),
    canProcessAppointment: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.APPOINTMENT_PROCESS)
    ),

    // 审核管理
    canViewReview: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.REVIEW_VIEW)
    ),

    // 其他管理
    canManageAttr: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.ATTR_MANAGE)
    ),
    canManageFacility: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.FACILITY_MANAGE)
    ),
    canManageFee: computed(() => 
      hasLandlordPermission(PermissionUtils.LANDLORD_PERMISSIONS.FEE_MANAGE)
    )
  };

  return {
    // 基础权限状态
    isLandlord,
    isTenant,
    currentUserType,

    // 权限检查函数
    hasPermission,
    hasLandlordPermission,
    withPermission,
    showPermissionDialog,

    // 房东权限
    landlordPermissions
  };
}

/**
 * 权限检查钩子函数
 */
export function usePermissionCheck() {
  const { hasPermission, withPermission, showPermissionDialog } = usePermission();

  /**
   * 创建权限检查函数
   */
  const createPermissionChecker = (permission: string | string[]) => {
    return {
      check: () => hasPermission(permission),
      execute: (action: () => void | Promise<void>, options?: any) => 
        withPermission(permission, action, options),
      showDialog: () => showPermissionDialog(permission)
    };
  };

  return {
    hasPermission,
    withPermission,
    showPermissionDialog,
    createPermissionChecker
  };
}

/**
 * 页面权限检查钩子
 */
export function usePagePermission(requiredPermissions?: string | string[]) {
  const { hasPermission, currentUserType } = usePermission();
  const router = useRouter();

  // 检查页面权限
  const hasPagePermission = computed(() => {
    if (!requiredPermissions) return true;
    return hasPermission(requiredPermissions);
  });

  // 权限不足时的处理
  const handlePermissionDenied = () => {
    if (currentUserType.value) {
      const homePath = UserTypeUtils.getDefaultHomePath(currentUserType.value);
      router.push(homePath);
    } else {
      router.push('/login');
    }
  };

  return {
    hasPagePermission,
    handlePermissionDenied,
    requiredPermissions
  };
}