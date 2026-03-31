import { showToast, showDialog } from 'vant';
import { removeToken } from '@/utils/token';
import { PageStateManager } from '@/utils/pageStateManager';
import { TokenChecker } from '@/utils/tokenChecker';

/**
 * 认证处理工具类
 * 专门处理token过期、登录失效等认证相关问题
 */
export class AuthHandler {
  private static isHandling = false; // 防止重复处理
  private static lastHandleTime = 0; // 记录上次处理时间
  private static readonly HANDLE_INTERVAL = 3000; // 3秒内不重复处理

  /**
   * 处理token过期（增强版）
   * @param showDialog 是否显示友好对话框，默认true
   * @param reason 过期原因，用于显示不同的提示
   */
  static async handleTokenExpired(showDialog: boolean = true, reason?: string) {
    const now = Date.now();
    
    // 防止短时间内重复处理
    if (this.isHandling || (now - this.lastHandleTime) < this.HANDLE_INTERVAL) {
      console.log('🔍 token过期处理已在进行中或间隔时间未到，跳过重复处理');
      return;
    }

    this.isHandling = true;
    this.lastHandleTime = now;
    console.log('🔍 开始处理token过期，原因:', reason || '未知');

    try {
      // 1. 保存当前页面状态
      PageStateManager.saveCurrentPath();
      console.log('🔍 已保存当前页面状态');

      // 2. 清理所有认证相关数据
      this.clearAuthData();

      // 3. 根据配置显示友好提示或简单提示
      if (showDialog) {
        const shouldRelogin = await this.showFriendlyExpiredDialog(reason);
        if (shouldRelogin) {
          console.log('🔍 用户选择立即重新登录');
          this.redirectToLogin();
        } else {
          console.log('🔍 用户选择稍后处理');
          // 用户选择稍后处理，但仍需要清理状态
        }
      } else {
        // 显示简单提示并自动跳转
        showToast({
          message: '登录已过期，正在跳转到登录页...',
          duration: 2000
        });
        
        setTimeout(() => {
          this.redirectToLogin();
        }, 2000);
      }

    } catch (error) {
      console.error('🔍 处理token过期失败:', error);
      // 即使出错也要强制跳转
      this.redirectToLogin();
    } finally {
      this.isHandling = false;
    }
  }

  /**
   * 显示友好的过期对话框
   * @param reason 过期原因
   * @returns 用户是否选择重新登录
   */
  private static async showFriendlyExpiredDialog(reason?: string): Promise<boolean> {
    try {
      let message = '您的登录已过期，需要重新登录才能继续使用。是否立即跳转到登录页面？';
      
      // 根据不同原因显示不同消息
      switch (reason) {
        case 'network_error':
          message = '网络连接异常导致登录状态失效，建议重新登录以确保正常使用。';
          break;
        case 'server_error':
          message = '服务器认证异常，您的登录状态可能已失效，建议重新登录。';
          break;
        case 'token_invalid':
          message = '登录信息格式异常，为了账户安全，建议重新登录。';
          break;
        case 'permission_denied':
          message = '权限验证失败，可能需要重新登录获取最新权限。';
          break;
      }

      await showDialog({
        title: '登录已过期',
        message,
        confirmButtonText: '立即登录',
        cancelButtonText: '稍后处理',
        showCancelButton: true
      });
      return true; // 用户点击确认
    } catch (error) {
      console.error('🔍 显示友好对话框失败:', error);
      // 用户点击取消或关闭对话框
      showToast('登录已过期，请重新登录');
      return false;
    }
  }

  /**
   * 清理认证数据（增强版）
   */
  static clearAuthData() {
    console.log('🔍 开始清理认证数据');
    
    try {
      // 清理token
      removeToken();
      console.log('🔍 已清理token');

      // 清理用户状态
      localStorage.removeItem('app-user');
      console.log('🔍 已清理用户状态');

      // 清理其他可能的认证相关数据
      const keysToRemove = [
        'userInfo', 
        'permissions', 
        'userType',
        'ACCESS_TOKEN',
        'REFRESH_TOKEN',
        'USER_PROFILE',
        'LAST_LOGIN_TIME',
        'SESSION_ID'
      ];
      
      keysToRemove.forEach(key => {
        localStorage.removeItem(key);
        sessionStorage.removeItem(key); // 同时清理sessionStorage
      });
      console.log('🔍 已清理其他认证数据');

      // 清理可能的cookie（如果有的话）
      this.clearAuthCookies();

      // 清理页面状态中的敏感数据
      PageStateManager.clearSavedState();
      console.log('🔍 已清理页面状态');

    } catch (error) {
      console.error('🔍 清理认证数据失败:', error);
    }
  }

  /**
   * 清理认证相关的cookies
   */
  private static clearAuthCookies() {
    try {
      const cookiesToClear = ['token', 'auth', 'session', 'user'];
      cookiesToClear.forEach(cookieName => {
        document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
      });
      console.log('🔍 已清理认证相关cookies');
    } catch (error) {
      console.error('🔍 清理cookies失败:', error);
    }
  }

  /**
   * 跳转到登录页（增强版）
   */
  static redirectToLogin() {
    console.log('🔍 准备跳转到登录页');
    
    try {
      // 获取当前路径
      const currentPath = window.location.pathname + window.location.search + window.location.hash;
      console.log('🔍 当前页面路径:', currentPath);
      
      // 检查当前路径是否包含用户类型信息
      const isTenantPath = currentPath.includes('/tenant/');
      const isLandlordPath = currentPath.includes('/landlord/');
      
      // 如果是特定用户类型的页面，不保存重定向路径
      // 这样可以避免房东登录后跳转到租客页面的问题
      let shouldSaveRedirect = true;
      
      if (isTenantPath || isLandlordPath) {
        console.log('🔍 检测到用户类型特定页面，不保存重定向路径');
        shouldSaveRedirect = false;
        // 清除之前可能保存的路径
        PageStateManager.clearSavedState();
      }
      
      // 只有在通用页面或公共页面时才保存重定向路径
      let redirect = '';
      if (shouldSaveRedirect) {
        const savedPath = PageStateManager.getSavedPath();
        const redirectPath = savedPath || currentPath;
        
        // 排除登录页和根路径
        if (redirectPath !== '/login' && redirectPath !== '/' && 
            !redirectPath.includes('#/login') && 
            !redirectPath.includes('/tenant/') && 
            !redirectPath.includes('/landlord/')) {
          redirect = `?redirect=${encodeURIComponent(redirectPath)}`;
          console.log('🔍 保存重定向路径:', redirectPath);
        }
      }

      // 构建登录URL
      const loginUrl = `#/login${redirect}`;
      console.log('🔍 跳转到:', loginUrl);
      
      // 使用location.href进行跳转，确保页面完全刷新
      window.location.href = loginUrl;
      
    } catch (error) {
      console.error('🔍 跳转到登录页失败:', error);
      // 如果跳转失败，尝试简单跳转（不带重定向参数）
      window.location.href = '#/login';
    } finally {
      this.isHandling = false;
    }
  }

  /**
   * 手动退出登录（增强版）
   */
  static async logout(showConfirm: boolean = true) {
    try {
      if (showConfirm) {
        await showDialog({
          title: '确认退出',
          message: '确定要退出登录吗？退出后需要重新登录才能使用相关功能。',
          confirmButtonText: '退出',
          cancelButtonText: '取消',
          confirmButtonColor: '#ee0a24'
        });
      }

      console.log('🔍 用户确认退出登录');
      
      // 清理数据
      this.clearAuthData();
      
      // 显示友好提示
      showToast('已安全退出登录');
      
      // 延迟跳转，让用户看到提示
      setTimeout(() => {
        window.location.href = '#/login';
      }, 1500);

    } catch (error) {
      console.log('🔍 用户取消退出登录');
    }
  }

  /**
   * 检查token是否过期（增强版）
   */
  static checkTokenExpired(): boolean {
    try {
      // 使用TokenChecker进行更准确的检查
      if (!TokenChecker.hasToken()) {
        console.log('🔍 未找到token，视为已过期');
        return true;
      }

      const token = TokenChecker.getToken();
      if (!token) {
        return true;
      }

      // 检查token格式是否有效
      if (!TokenChecker.isTokenFormatValid(token)) {
        console.log('🔍 token格式无效，视为已过期');
        return true;
      }

      // 检查token是否已过期
      if (TokenChecker.isTokenExpired(token)) {
        console.log('🔍 token已过期');
        return true;
      }

      console.log('🔍 token有效');
      return false;
    } catch (error) {
      console.error('🔍 检查token过期状态失败:', error);
      // 出错时视为过期，安全起见
      return true;
    }
  }

  /**
   * 获取token剩余有效时间（分钟）
   */
  static getTokenRemainingTime(): number {
    try {
      const token = TokenChecker.getToken();
      if (!token || !TokenChecker.isTokenFormatValid(token)) {
        return 0;
      }
      return TokenChecker.getRemainingMinutes(token);
    } catch (error) {
      console.error('🔍 获取token剩余时间失败:', error);
      return 0;
    }
  }

  /**
   * 检查是否需要提醒用户token即将过期
   * @param warningMinutes 提前多少分钟提醒，默认10分钟
   */
  static shouldShowExpiringWarning(warningMinutes: number = 10): boolean {
    const remainingMinutes = this.getTokenRemainingTime();
    return remainingMinutes > 0 && remainingMinutes <= warningMinutes;
  }

  /**
   * 显示token即将过期的警告（简化版）
   */
  static async showExpiringWarning(): Promise<boolean> {
    const remainingMinutes = this.getTokenRemainingTime();
    if (remainingMinutes <= 0) {
      return false;
    }

    try {
      // 显示简单的过期警告
      await showDialog({
        title: '登录即将过期',
        message: `您的登录将在 ${remainingMinutes} 分钟后过期，是否现在重新登录？`,
        confirmButtonText: '立即登录',
        cancelButtonText: '稍后处理',
        showCancelButton: true
      });
      
      console.log('🔍 用户选择重新登录');
      await this.handleTokenExpired(true, 'user_choice');
      return false;
    } catch (error) {
      console.log('🔍 用户选择稍后处理');
      return false;
    }
  }

  /**
   * 处理权限不足的情况
   */
  static async handlePermissionDenied(): Promise<void> {
    try {
      await showDialog({
        title: '权限不足',
        message: '您没有访问此功能的权限，可能需要重新登录获取最新权限。',
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        showCancelButton: true
      });
      
      PageStateManager.saveCurrentPath();
      this.clearAuthData();
      this.redirectToLogin();
    } catch (error) {
      console.error('🔍 处理权限不足失败:', error);
    }
  }

  /**
   * 处理网络错误导致的认证问题
   */
  static async handleNetworkAuthError(errorMessage?: string): Promise<void> {
    try {
      await showDialog({
        title: '网络错误',
        message: errorMessage || '网络连接异常，可能影响登录状态。是否重新登录？',
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        showCancelButton: true
      });
      
      PageStateManager.saveCurrentPath();
      this.clearAuthData();
      this.redirectToLogin();
    } catch (error) {
      console.error('🔍 处理网络认证错误失败:', error);
    }
  }

  /**
   * 重置处理状态（紧急情况使用）
   */
  static resetHandlingState(): void {
    console.log('🔍 重置AuthHandler处理状态');
    this.isHandling = false;
    this.lastHandleTime = 0;
  }

  /**
   * 获取当前处理状态
   */
  static getHandlingStatus(): { isHandling: boolean; lastHandleTime: number } {
    return {
      isHandling: this.isHandling,
      lastHandleTime: this.lastHandleTime
    };
  }
}