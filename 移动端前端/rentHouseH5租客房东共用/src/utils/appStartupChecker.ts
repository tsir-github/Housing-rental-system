import { TokenChecker } from './tokenChecker';
import { AuthHandler } from './authHandler';
import { PageStateManager } from './pageStateManager';
import { showToast, showDialog } from 'vant';

/**
 * 应用启动检查器
 * 在应用启动时执行各种检查和初始化操作
 */
export class AppStartupChecker {
  private static isInitialized = false;
  private static initPromise: Promise<void> | null = null;

  /**
   * 执行应用启动检查
   * @returns Promise<void>
   */
  static async performStartupChecks(): Promise<void> {
    // 防止重复初始化
    if (this.isInitialized) {
      console.log('🚀 应用启动检查已完成，跳过重复执行');
      return;
    }

    // 如果正在初始化，返回现有的Promise
    if (this.initPromise) {
      console.log('🚀 应用启动检查正在进行中，等待完成');
      return this.initPromise;
    }

    console.log('🚀 开始执行应用启动检查');

    this.initPromise = this.executeChecks();

    try {
      await this.initPromise;
      this.isInitialized = true;
      console.log('🚀 应用启动检查完成');
    } catch (error) {
      console.error('🚀 应用启动检查失败:', error);
      // 即使检查失败，也标记为已初始化，避免无限重试
      this.isInitialized = true;
    } finally {
      this.initPromise = null;
    }
  }

  /**
   * 执行具体的检查逻辑
   */
  private static async executeChecks(): Promise<void> {
    try {
      // 1. 清理过期的页面状态和表单数据
      console.log('🚀 步骤1: 清理过期数据');
      PageStateManager.cleanupExpiredFormData();

      // 2. 检查Token状态
      console.log('🚀 步骤2: 检查Token状态');
      await this.checkTokenStatus();

      // 3. 初始化用户状态
      console.log('🚀 步骤3: 初始化用户状态');
      await this.initializeUserState();

      // 4. 设置定期检查
      console.log('🚀 步骤4: 设置定期检查');
      this.setupPeriodicChecks();

      console.log('🚀 所有启动检查步骤完成');

    } catch (error) {
      console.error('🚀 执行启动检查时发生错误:', error);
      throw error;
    }
  }

  /**
   * 检查Token状态
   */
  private static async checkTokenStatus(): Promise<void> {
    try {
      // 检查是否有Token
      if (!TokenChecker.hasToken()) {
        console.log('🚀 未发现Token，用户未登录');
        return;
      }

      const token = TokenChecker.getToken();
      if (!token) {
        console.log('🚀 Token为空，清理相关数据');
        AuthHandler.clearAuthData();
        return;
      }

      // 检查Token格式
      if (!TokenChecker.isTokenFormatValid(token)) {
        console.log('🚀 Token格式无效，清理数据');
        AuthHandler.clearAuthData();

        // 显示简单提示
        showToast('登录信息格式异常，请重新登录');
        return;
      }

      // 检查Token是否过期
      if (TokenChecker.isTokenExpired(token)) {
        console.log('🚀 Token已过期，处理过期逻辑');

        // 清理数据
        AuthHandler.clearAuthData();

        // 显示简单的过期提示
        showToast('登录已过期，请重新登录');
        return;
      }

      // Token有效，检查是否即将过期
      const remainingMinutes = TokenChecker.getRemainingMinutes(token);
      console.log(`🚀 Token有效，剩余时间: ${remainingMinutes} 分钟`);
      
      if (remainingMinutes <= 10 && remainingMinutes > 0) {
        console.log('🚀 Token即将过期，显示提醒');
        // 显示轻量级提醒，不阻塞应用启动
        setTimeout(() => {
          showToast(`登录将在 ${remainingMinutes} 分钟后过期`);
        }, 2000);
      }

    } catch (error) {
      console.error('🚀 检查Token状态时发生错误:', error);
      // 出错时为了安全起见，清理Token
      AuthHandler.clearAuthData();
    }
  }

  /**
   * 初始化用户状态
   */
  private static async initializeUserState(): Promise<void> {
    try {
      // 动态导入用户store，避免循环依赖
      const { useUserStore } = await import('@/store/modules/user');
      const userStore = useUserStore();

      // 如果有有效的Token但用户状态为空，尝试恢复用户状态
      if (TokenChecker.hasToken() && !userStore.token) {
        console.log('🚀 检测到Token但用户状态为空，尝试恢复状态');

        // 这里可以添加从Token中恢复用户信息的逻辑
        // 或者调用API获取用户信息

        // 暂时只是记录日志
        console.log('🚀 用户状态恢复逻辑待实现');
      }

    } catch (error) {
      console.error('🚀 初始化用户状态时发生错误:', error);
    }
  }

  /**
   * 设置定期检查
   */
  private static setupPeriodicChecks(): void {
    try {
      // 每5分钟检查一次Token状态
      setInterval(() => {
        this.performPeriodicTokenCheck();
      }, 5 * 60 * 1000); // 5分钟

      // 每小时清理一次过期数据
      setInterval(() => {
        PageStateManager.cleanupExpiredFormData();
        console.log('🚀 定期清理过期数据完成');
      }, 60 * 60 * 1000); // 1小时

      console.log('🚀 定期检查任务已设置');

    } catch (error) {
      console.error('🚀 设置定期检查时发生错误:', error);
    }
  }

  /**
   * 执行定期Token检查（简化版）
   */
  private static performPeriodicTokenCheck(): void {
    try {
      if (!TokenChecker.hasToken()) {
        return;
      }

      const token = TokenChecker.getToken();
      if (!token || !TokenChecker.isTokenFormatValid(token)) {
        console.log('🚀 定期检查发现Token无效，清理数据');
        AuthHandler.clearAuthData();
        return;
      }

      if (TokenChecker.isTokenExpired(token)) {
        console.log('🚀 定期检查发现Token已过期');
        // 静默处理过期，不显示对话框（避免打断用户操作）
        AuthHandler.handleTokenExpired(false);
        return;
      }

      // 检查是否需要提醒即将过期
      const remainingMinutes = TokenChecker.getRemainingMinutes(token);
      if (remainingMinutes <= 10 && remainingMinutes > 0) {
        // 检查是否已经显示过提醒（避免重复提醒）
        const lastWarningTime = localStorage.getItem('LAST_EXPIRY_WARNING');
        const now = Date.now();

        if (!lastWarningTime || (now - parseInt(lastWarningTime)) > 10 * 60 * 1000) {
          console.log('🚀 定期检查发现Token即将过期，显示提醒');
          showToast(`登录将在 ${remainingMinutes} 分钟后过期`);
          localStorage.setItem('LAST_EXPIRY_WARNING', now.toString());
        }
      }

    } catch (error) {
      console.error('🚀 定期Token检查时发生错误:', error);
    }
  }

  /**
   * 重置初始化状态（用于测试或重新初始化）
   */
  static resetInitializationState(): void {
    console.log('🚀 重置应用启动检查状态');
    this.isInitialized = false;
    this.initPromise = null;
  }

  /**
   * 获取初始化状态
   */
  static getInitializationStatus(): { isInitialized: boolean; isInitializing: boolean } {
    return {
      isInitialized: this.isInitialized,
      isInitializing: this.initPromise !== null
    };
  }

  /**
   * 手动触发Token检查（用于特殊情况）
   */
  static async manualTokenCheck(): Promise<void> {
    console.log('🚀 手动触发Token检查');
    try {
      await this.checkTokenStatus();
    } catch (error) {
      console.error('🚀 手动Token检查失败:', error);
    }
  }

  /**
   * 清理定期任务（用于应用卸载时）
   */
  static cleanup(): void {
    console.log('🚀 清理应用启动检查器');
    // 这里可以添加清理定期任务的逻辑
    // 由于setInterval返回的ID没有保存，暂时只记录日志
    this.isInitialized = false;
    this.initPromise = null;
  }
}