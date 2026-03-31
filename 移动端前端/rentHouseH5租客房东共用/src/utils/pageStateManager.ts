/**
 * 页面状态管理工具类
 * 用于保存和恢复用户的页面状态，提升登录后的用户体验
 */
export class PageStateManager {
  private static readonly REDIRECT_PATH_KEY = 'REDIRECT_PATH';
  private static readonly PAGE_STATE_KEY = 'PAGE_STATE';
  private static readonly STATE_TIMESTAMP_KEY = 'STATE_TIMESTAMP';
  
  // 状态保存的有效期（毫秒），默认30分钟
  private static readonly STATE_EXPIRY_TIME = 30 * 60 * 1000;

  /**
   * 保存当前页面路径
   * @param path 要保存的路径，如果不提供则使用当前路径
   */
  static saveCurrentPath(path?: string): void {
    try {
      const currentPath = path || this.getCurrentPath();
      
      // 不保存登录页和一些特殊页面
      if (this.shouldSkipPath(currentPath)) {
        console.log('🔍 跳过保存路径:', currentPath);
        return;
      }

      localStorage.setItem(this.REDIRECT_PATH_KEY, currentPath);
      localStorage.setItem(this.STATE_TIMESTAMP_KEY, Date.now().toString());
      
      console.log('🔍 已保存页面路径:', currentPath);
    } catch (error) {
      console.error('🔍 保存页面路径失败:', error);
    }
  }

  /**
   * 获取保存的重定向路径
   * @returns 保存的路径或null
   */
  static getSavedPath(): string | null {
    try {
      const savedPath = localStorage.getItem(this.REDIRECT_PATH_KEY);
      const timestamp = localStorage.getItem(this.STATE_TIMESTAMP_KEY);
      
      if (!savedPath || !timestamp) {
        return null;
      }

      // 检查是否过期
      const saveTime = parseInt(timestamp);
      const now = Date.now();
      
      if (now - saveTime > this.STATE_EXPIRY_TIME) {
        console.log('🔍 保存的路径已过期，清理数据');
        this.clearSavedState();
        return null;
      }

      console.log('🔍 获取保存的路径:', savedPath);
      return savedPath;
    } catch (error) {
      console.error('🔍 获取保存路径失败:', error);
      return null;
    }
  }

  /**
   * 保存页面状态数据
   * @param stateData 要保存的状态数据
   */
  static savePageState(stateData: any): void {
    try {
      const stateJson = JSON.stringify(stateData);
      localStorage.setItem(this.PAGE_STATE_KEY, stateJson);
      localStorage.setItem(this.STATE_TIMESTAMP_KEY, Date.now().toString());
      
      console.log('🔍 已保存页面状态:', stateData);
    } catch (error) {
      console.error('🔍 保存页面状态失败:', error);
    }
  }

  /**
   * 获取保存的页面状态
   * @returns 保存的状态数据或null
   */
  static getSavedPageState(): any | null {
    try {
      const stateJson = localStorage.getItem(this.PAGE_STATE_KEY);
      const timestamp = localStorage.getItem(this.STATE_TIMESTAMP_KEY);
      
      if (!stateJson || !timestamp) {
        return null;
      }

      // 检查是否过期
      const saveTime = parseInt(timestamp);
      const now = Date.now();
      
      if (now - saveTime > this.STATE_EXPIRY_TIME) {
        console.log('🔍 保存的页面状态已过期，清理数据');
        this.clearSavedState();
        return null;
      }

      const stateData = JSON.parse(stateJson);
      console.log('🔍 获取保存的页面状态:', stateData);
      return stateData;
    } catch (error) {
      console.error('🔍 获取页面状态失败:', error);
      return null;
    }
  }

  /**
   * 清理保存的状态数据
   */
  static clearSavedState(): void {
    try {
      localStorage.removeItem(this.REDIRECT_PATH_KEY);
      localStorage.removeItem(this.PAGE_STATE_KEY);
      localStorage.removeItem(this.STATE_TIMESTAMP_KEY);
      
      console.log('🔍 已清理保存的状态数据');
    } catch (error) {
      console.error('🔍 清理状态数据失败:', error);
    }
  }

  /**
   * 执行页面重定向
   * @param router Vue Router实例
   * @param defaultPath 默认跳转路径
   */
  static async restorePageState(router: any, defaultPath: string = '/landlord/dashboard'): Promise<void> {
    try {
      const savedPath = this.getSavedPath();
      
      if (savedPath && savedPath !== '/login') {
        console.log('🔍 恢复到保存的页面:', savedPath);
        
        // 清理已使用的状态
        this.clearSavedState();
        
        // 延迟跳转，确保登录状态已更新
        setTimeout(() => {
          router.push(savedPath);
        }, 100);
      } else {
        console.log('🔍 跳转到默认页面:', defaultPath);
        router.push(defaultPath);
      }
    } catch (error) {
      console.error('🔍 恢复页面状态失败:', error);
      // 出错时跳转到默认页面
      router.push(defaultPath);
    }
  }

  /**
   * 获取当前页面路径
   * @returns 当前路径
   */
  private static getCurrentPath(): string {
    return window.location.pathname + window.location.search + window.location.hash;
  }

  /**
   * 判断是否应该跳过保存的路径
   * @param path 路径
   * @returns 是否跳过
   */
  private static shouldSkipPath(path: string): boolean {
    const skipPaths = [
      '/login',
      '/register',
      '/forgot-password',
      '/404',
      '/error'
    ];

    return skipPaths.some(skipPath => path.startsWith(skipPath));
  }

  /**
   * 保存表单数据
   * @param formId 表单标识
   * @param formData 表单数据
   */
  static saveFormData(formId: string, formData: any): void {
    try {
      const key = `FORM_DATA_${formId}`;
      const dataWithTimestamp = {
        data: formData,
        timestamp: Date.now()
      };
      
      localStorage.setItem(key, JSON.stringify(dataWithTimestamp));
      console.log('🔍 已保存表单数据:', formId);
    } catch (error) {
      console.error('🔍 保存表单数据失败:', error);
    }
  }

  /**
   * 获取保存的表单数据
   * @param formId 表单标识
   * @returns 表单数据或null
   */
  static getSavedFormData(formId: string): any | null {
    try {
      const key = `FORM_DATA_${formId}`;
      const savedData = localStorage.getItem(key);
      
      if (!savedData) {
        return null;
      }

      const { data, timestamp } = JSON.parse(savedData);
      const now = Date.now();
      
      // 表单数据保存时间较短，只保存10分钟
      if (now - timestamp > 10 * 60 * 1000) {
        localStorage.removeItem(key);
        return null;
      }

      console.log('🔍 获取保存的表单数据:', formId);
      return data;
    } catch (error) {
      console.error('🔍 获取表单数据失败:', error);
      return null;
    }
  }

  /**
   * 清理过期的表单数据
   */
  static cleanupExpiredFormData(): void {
    try {
      const keys = Object.keys(localStorage);
      const formDataKeys = keys.filter(key => key.startsWith('FORM_DATA_'));
      
      formDataKeys.forEach(key => {
        try {
          const savedData = localStorage.getItem(key);
          if (savedData) {
            const { timestamp } = JSON.parse(savedData);
            const now = Date.now();
            
            if (now - timestamp > 10 * 60 * 1000) {
              localStorage.removeItem(key);
              console.log('🔍 清理过期表单数据:', key);
            }
          }
        } catch (error) {
          // 数据格式错误，直接删除
          localStorage.removeItem(key);
        }
      });
    } catch (error) {
      console.error('🔍 清理表单数据失败:', error);
    }
  }
}