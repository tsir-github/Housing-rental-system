import { UserType } from '@/api/common/user/types';

/**
 * 权限工具类
 */
export class PermissionUtils {
  // 房东权限常量
  static readonly LANDLORD_PERMISSIONS = {
    APARTMENT_MANAGE: 'landlord:apartment:manage',
    ROOM_MANAGE: 'landlord:room:manage',
    APPLICATION_VIEW: 'landlord:application:view',
    APPOINTMENT_VIEW: 'landlord:appointment:view',
    REVIEW_VIEW: 'landlord:review:view',
    ATTR_MANAGE: 'landlord:attr:manage',
    FACILITY_MANAGE: 'landlord:facility:manage',
    FEE_MANAGE: 'landlord:fee:manage',
    LABEL_MANAGE: 'landlord:label:manage',
    PAYMENT_TYPE_MANAGE: 'landlord:payment:manage',
    LEASE_TERM_MANAGE: 'landlord:lease:manage'
  };

  /**
   * 检查是否可以管理公寓
   */
  static canManageApartment(): boolean {
    return UserTypeUtils.hasPermission(this.LANDLORD_PERMISSIONS.APARTMENT_MANAGE);
  }

  /**
   * 检查是否可以管理房源
   */
  static canManageRoom(): boolean {
    return UserTypeUtils.hasPermission(this.LANDLORD_PERMISSIONS.ROOM_MANAGE);
  }

  /**
   * 检查是否可以查看申请
   */
  static canViewApplication(): boolean {
    return UserTypeUtils.hasPermission(this.LANDLORD_PERMISSIONS.APPLICATION_VIEW);
  }

  /**
   * 检查是否可以查看预约
   */
  static canViewAppointment(): boolean {
    return UserTypeUtils.hasPermission(this.LANDLORD_PERMISSIONS.APPOINTMENT_VIEW);
  }

  /**
   * 检查是否可以查看审核
   */
  static canViewReview(): boolean {
    return UserTypeUtils.hasPermission(this.LANDLORD_PERMISSIONS.REVIEW_VIEW);
  }
}

/**
 * 用户类型工具类
 */
export class UserTypeUtils {
  /**
   * 获取用户类型显示名称
   */
  static getUserTypeDisplayName(userType: UserType): string {
    switch (userType) {
      case UserType.TENANT:
        return '租客';
      case UserType.LANDLORD:
        return '房东';
      default:
        return '未知';
    }
  }

  /**
   * 获取用户类型对应的默认首页路径
   */
  static getDefaultHomePath(userType: string | UserType): string {
    // 兼容处理数字和字符串类型
    const normalizedType = typeof userType === 'number' 
      ? (userType === 2 ? 'LANDLORD' : 'TENANT')
      : userType;
      
    switch (normalizedType) {
      case 'TENANT':
        return '/tenant/search';
      case 'LANDLORD':
        return '/landlord/dashboard';
      default:
        // 如果是枚举类型，转换为字符串比较
        if (userType === UserType.TENANT) {
          return '/tenant/search';
        } else if (userType === UserType.LANDLORD) {
          return '/landlord/dashboard';
        }
        return '/login';
    }
  }

  /**
   * 检查是否为租客
   */
  static isTenant(userType: string | UserType): boolean {
    const normalizedType = typeof userType === 'number' 
      ? (userType === 1 ? 'TENANT' : 'LANDLORD')
      : userType;
    return normalizedType === 'TENANT' || normalizedType === UserType.TENANT.toString();
  }

  /**
   * 检查是否为房东
   */
  static isLandlord(userType: string | UserType): boolean {
    const normalizedType = typeof userType === 'number' 
      ? (userType === 2 ? 'LANDLORD' : 'TENANT')
      : userType;
    return normalizedType === 'LANDLORD' || normalizedType === UserType.LANDLORD.toString();
  }

  /**
   * 获取当前用户类型（从localStorage或store中获取）
   */
  static getCurrentUserType(): string | null {
    try {
      // 从localStorage中获取用户信息
      const userStore = localStorage.getItem('app-user');
      if (userStore) {
        const parsed = JSON.parse(userStore);
        let userType = parsed.currentUserType;
        
        // 兼容处理：如果是数字类型，转换为字符串
        if (typeof userType === 'number') {
          userType = userType === 2 ? 'LANDLORD' : 'TENANT';
        }
        
        // 如果currentUserType为空，尝试从userInfo中获取
        if (!userType && parsed.userInfo?.userType) {
          const numericType = parsed.userInfo.userType;
          userType = numericType === 2 ? 'LANDLORD' : 'TENANT';
        }
        
        return userType || null;
      }
      return null;
    } catch (error) {
      console.error('获取当前用户类型失败:', error);
      return null;
    }
  }

  /**
   * 检查用户是否有特定权限
   */
  static hasPermission(permission: string): boolean {
    try {
      // 从localStorage中获取用户信息
      const userStore = localStorage.getItem('app-user');
      if (userStore) {
        const parsed = JSON.parse(userStore);
        const permissions = parsed.userInfo?.permissions || [];
        return permissions.includes(permission);
      }
      return false;
    } catch (error) {
      console.error('检查权限失败:', error);
      return false;
    }
  }
}