import { Router } from 'vue-router';

/**
 * 导航返回工具类
 * 提供智能的返回逻辑，避免返回到登录页面
 */
export class NavigationHelper {
  /**
   * 根据当前路径获取默认回退路径
   * @param currentPath 当前路径
   * @returns 默认回退路径
   */
  static getDefaultFallbackPath(currentPath: string): string {
    console.log('🧭 获取默认回退路径，当前路径:', currentPath);

    // 房东页面的智能回退逻辑
    if (currentPath.startsWith('/landlord/')) {
      // 详情页面返回到对应的列表页面
      if (currentPath.includes('/apartments/') && currentPath !== '/landlord/apartments') {
        return '/landlord/apartments';
      }
      if (currentPath.includes('/rooms/') && currentPath !== '/landlord/rooms') {
        return '/landlord/rooms';
      }
      if (currentPath.includes('/applications/') && currentPath !== '/landlord/applications') {
        return '/landlord/applications';
      }
      if (currentPath.includes('/appointments/') && currentPath !== '/landlord/appointments') {
        return '/landlord/appointments';
      }
      if (currentPath.includes('/reviews/') && currentPath !== '/landlord/reviews') {
        return '/landlord/reviews';
      }

      // 租约管理：和其他管理模块保持一致的返回逻辑
      if (currentPath.includes('/leases/') && currentPath !== '/landlord/leases') {
        return '/landlord/leases';
      }

      // 列表页面和其他房东页面返回到仪表板
      return '/landlord/dashboard';
    } else if (currentPath.startsWith('/tenant/')) {
      // 租客页面的智能回退逻辑
      
      // 从个人中心进入的页面，返回个人中心
      if (currentPath === '/tenant/myAgreement' || 
          currentPath === '/tenant/myAppointment' || 
          currentPath === '/tenant/myDirectMoveIn' || 
          currentPath === '/tenant/browsingHistory') {
        return '/tenant/userCenter';
      }
      
      // 租约详情页面返回到我的租约列表
      if (currentPath.startsWith('/tenant/agreement')) {
        return '/tenant/myAgreement';
      }
      
      // 预约详情页面返回到我的预约列表
      if (currentPath === '/tenant/appointment') {
        return '/tenant/myAppointment';
      }
      
      // 直接入住详情页面返回到入住申请列表
      if (currentPath === '/tenant/directMoveInDetail') {
        return '/tenant/myDirectMoveIn';
      }
      
      // 房源详情页面返回到搜索页面
      if (currentPath.startsWith('/tenant/roomDetail') || 
          currentPath.startsWith('/tenant/apartmentDetail')) {
        return '/tenant/search';
      }
      
      // 直接入住申请页面返回到房源详情
      if (currentPath === '/tenant/directMoveIn') {
        // 需要从 query 参数中获取 roomId
        const roomId = new URLSearchParams(window.location.search).get('roomId');
        return roomId ? `/tenant/roomDetail/${roomId}` : '/tenant/search';
      }
      
      // 其他租客页面默认回到搜索首页
      return '/tenant/search';
    }

    // 其他情况回到登录页
    return '/login';
  }

  /**
   * 统一的智能返回方法
   * @param router Vue Router实例
   * @param customFallback 自定义回退路径
   */
  static smartGoBack(router: Router, customFallback?: string): void {
    console.log('🧭 智能返回开始');
    console.log('🧭 当前URL:', window.location.href);
    console.log('🧭 当前hash:', window.location.hash);

    try {
      const currentRoute = router.currentRoute.value;
      const currentPath = currentRoute.fullPath;
      
      // 检查是否有来源信息（from参数）
      const fromPath = currentRoute.query.from as string;
      
      console.log('🧭 当前路径:', currentPath);
      console.log('🧭 来源路径:', fromPath);
      console.log('🧭 历史记录长度:', window.history.length);
      console.log('🧭 referrer:', document.referrer);

      // 优先使用自定义回退路径
      if (customFallback) {
        console.log('🧭 使用自定义回退路径:', customFallback);
        this.safeNavigate(router, customFallback);
        return;
      }

      // 如果有明确的来源路径且安全，优先返回来源
      if (fromPath && this.isValidReturnPath(fromPath)) {
        console.log('🧭 返回到来源路径:', fromPath);
        this.safeNavigate(router, fromPath);
        return;
      }

      // 检查是否有安全的历史记录可以返回
      if (this.canSafelyGoBack(currentPath)) {
        console.log('🧭 安全返回上一页');
        router.back();
      } else {
        const fallbackPath = this.getDefaultFallbackPath(currentPath);
        console.log('🧭 跳转到回退路径:', fallbackPath);
        this.safeNavigate(router, fallbackPath);
      }
    } catch (error) {
      console.error('🧭 智能返回失败:', error);
      // 降级处理：根据当前路径类型选择合适的默认页面
      const currentPath = router.currentRoute.value.path;
      const fallbackPath = currentPath.startsWith('/landlord/') 
        ? '/landlord/dashboard' 
        : '/tenant/search';
      console.log('🧭 降级处理：跳转到:', fallbackPath);
      this.safeNavigate(router, fallbackPath);
    }
  }

  /**
   * 全局组件专用的智能返回方法（保持向后兼容）
   * @param router Vue Router实例
   * @param customFallback 自定义回退路径
   */
  static globalSmartGoBack(router: Router, customFallback?: string): void {
    this.smartGoBack(router, customFallback);
  }

  /**
   * 检查当前页面是否可以安全返回
   * @param currentPath 当前路径
   * @returns 是否可以安全返回
   */
  static canSafelyGoBack(currentPath: string): boolean {
    console.log('🧭 检查是否可以安全返回，当前路径:', currentPath);
    console.log('🧭 历史记录长度:', window.history.length);
    console.log('🧭 当前referrer:', document.referrer);

    // 如果没有历史记录，不能返回
    if (window.history.length <= 1) {
      console.log('🧭 没有历史记录，不能返回');
      return false;
    }

    // 检查referrer是否安全
    const referrer = document.referrer;
    if (this.isUnsafeReferrer(referrer)) {
      console.log('🧭 不安全的referrer，不能返回');
      return false;
    }

    // 对于租客的二级、三级页面，总是允许返回
    // 这样可以正确返回到上级页面而不是总是回到搜索页面
    if (currentPath.startsWith('/tenant/roomDetail') ||
        currentPath.startsWith('/tenant/apartmentDetail') ||
        currentPath.startsWith('/tenant/agreement') ||
        currentPath.startsWith('/tenant/appointment') ||
        currentPath.startsWith('/tenant/directMoveIn') ||
        currentPath.startsWith('/tenant/directMoveInDetail')) {
      console.log('🧭 租客详情页面，允许返回');
      return true;
    }

    console.log('🧭 可以安全返回');
    return true;
  }



  /**
   * 检查referrer是否不安全（不应该返回的页面）
   * @param referrer 上一页的URL
   * @returns 是否不安全
   */
  private static isUnsafeReferrer(referrer: string): boolean {
    if (!referrer) {
      console.log('🧭 没有referrer');
      return false;
    }

    const unsafePatterns = [
      '/login',
      '#/login',
      'login.html',
    ];

    const isUnsafe = unsafePatterns.some(pattern => referrer.includes(pattern));
    console.log('🧭 referrer安全检查:', referrer, '是否不安全:', isUnsafe);
    return isUnsafe;
  }

  /**
   * 安全导航方法，处理导航失败情况
   * @param router Vue Router实例
   * @param path 目标路径
   */
  static safeNavigate(router: Router, path: string): void {
    console.log('🧭 安全导航到:', path);

    try {
      router.push(path);
    } catch (error) {
      console.error('🧭 路由导航失败，尝试强制跳转:', error);
      try {
        window.location.href = path;
      } catch (locationError) {
        console.error('🧭 强制跳转也失败:', locationError);
        // 最后的降级：刷新页面
        window.location.reload();
      }
    }
  }

  /**
   * 验证回退路径的有效性
   * @param path 路径
   * @returns 是否有效
   */
  static validateFallbackPath(path: string): boolean {
    const validPaths = [
      '/landlord/dashboard',
      '/tenant/search',
      '/tenant/userCenter',
      '/login'
    ];

    return validPaths.includes(path) ||
      path.startsWith('/landlord/') ||
      path.startsWith('/tenant/');
  }

  /**
   * 检查路径是否为有效的返回路径
   * @param path 路径
   * @returns 是否有效
   */
  static isValidReturnPath(path: string): boolean {
    if (!path) return false;
    
    // 排除不安全的路径
    const unsafePaths = ['/login', '/register'];
    if (unsafePaths.some(unsafePath => path.includes(unsafePath))) {
      return false;
    }
    
    // 只允许应用内的路径
    return path.startsWith('/tenant/') || path.startsWith('/landlord/') || path.startsWith('/common/');
  }

  /**
   * 获取安全的返回路径
   * @param currentPath 当前路径
   * @returns 安全的返回路径
   */
  static getSafeBackPath(currentPath: string): string {
    return this.getDefaultFallbackPath(currentPath);
  }
}