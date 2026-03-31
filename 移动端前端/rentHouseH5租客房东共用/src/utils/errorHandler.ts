import { showToast, showDialog } from 'vant';

/**
 * 错误类型枚举
 */
export enum ErrorType {
  NETWORK_ERROR = 'NETWORK_ERROR',
  API_ERROR = 'API_ERROR',
  PERMISSION_ERROR = 'PERMISSION_ERROR',
  VALIDATION_ERROR = 'VALIDATION_ERROR',
  UNKNOWN_ERROR = 'UNKNOWN_ERROR'
}

/**
 * 错误信息接口
 */
export interface ErrorInfo {
  type: ErrorType;
  code?: string | number;
  message: string;
  details?: any;
  timestamp: number;
  url?: string;
  userAgent?: string;
}

/**
 * 全局错误处理器
 */
export class GlobalErrorHandler {
  private static instance: GlobalErrorHandler;
  private errorQueue: ErrorInfo[] = [];
  private maxErrorCount = 50; // 最大错误记录数
  private retryAttempts = new Map<string, number>();
  private maxRetryAttempts = 3;

  private constructor() {
    this.setupGlobalErrorHandlers();
  }

  static getInstance(): GlobalErrorHandler {
    if (!GlobalErrorHandler.instance) {
      GlobalErrorHandler.instance = new GlobalErrorHandler();
    }
    return GlobalErrorHandler.instance;
  }

  /**
   * 设置全局错误处理器
   */
  private setupGlobalErrorHandlers() {
    // 捕获未处理的Promise错误
    window.addEventListener('unhandledrejection', (event) => {
      console.error('Unhandled promise rejection:', event.reason);
      this.handleError({
        type: ErrorType.UNKNOWN_ERROR,
        message: '系统发生未知错误',
        details: event.reason,
        timestamp: Date.now(),
        url: window.location.href,
        userAgent: navigator.userAgent
      });
      event.preventDefault();
    });

    // 捕获JavaScript运行时错误
    window.addEventListener('error', (event) => {
      console.error('JavaScript error:', event.error);
      this.handleError({
        type: ErrorType.UNKNOWN_ERROR,
        message: event.message || '脚本执行错误',
        details: {
          filename: event.filename,
          lineno: event.lineno,
          colno: event.colno,
          error: event.error
        },
        timestamp: Date.now(),
        url: window.location.href,
        userAgent: navigator.userAgent
      });
    });

    // 捕获资源加载错误
    window.addEventListener('error', (event) => {
      if (event.target !== window) {
        console.error('Resource loading error:', event.target);
        this.handleError({
          type: ErrorType.NETWORK_ERROR,
          message: '资源加载失败',
          details: {
            tagName: (event.target as any)?.tagName,
            src: (event.target as any)?.src || (event.target as any)?.href
          },
          timestamp: Date.now(),
          url: window.location.href,
          userAgent: navigator.userAgent
        });
      }
    }, true);
  }

  /**
   * 处理错误
   */
  handleError(errorInfo: ErrorInfo, showToUser = true) {
    // 记录错误
    this.logError(errorInfo);

    // 根据错误类型决定是否显示给用户
    if (showToUser) {
      this.showErrorToUser(errorInfo);
    }

    // 上报错误（可选）
    this.reportError(errorInfo);
  }

  /**
   * 记录错误到本地队列
   */
  private logError(errorInfo: ErrorInfo) {
    this.errorQueue.push(errorInfo);
    
    // 保持错误队列大小
    if (this.errorQueue.length > this.maxErrorCount) {
      this.errorQueue.shift();
    }

    // 存储到localStorage（可选）
    try {
      const storedErrors = JSON.parse(localStorage.getItem('app_errors') || '[]');
      storedErrors.push(errorInfo);
      if (storedErrors.length > this.maxErrorCount) {
        storedErrors.shift();
      }
      localStorage.setItem('app_errors', JSON.stringify(storedErrors));
    } catch (e) {
      console.warn('Failed to store error to localStorage:', e);
    }
  }

  /**
   * 向用户显示错误
   */
  private showErrorToUser(errorInfo: ErrorInfo) {
    const userMessage = this.getUserFriendlyMessage(errorInfo);
    
    switch (errorInfo.type) {
      case ErrorType.NETWORK_ERROR:
        showToast({
          message: userMessage,
          type: 'fail',
          duration: 3000
        });
        break;
      
      case ErrorType.PERMISSION_ERROR:
        showDialog({
          title: '权限不足',
          message: userMessage,
          confirmButtonText: '我知道了'
        });
        break;
      
      case ErrorType.VALIDATION_ERROR:
        showToast({
          message: userMessage,
          type: 'fail',
          duration: 2000
        });
        break;
      
      default:
        showToast({
          message: userMessage,
          type: 'fail',
          duration: 2000
        });
    }
  }

  /**
   * 获取用户友好的错误消息
   */
  private getUserFriendlyMessage(errorInfo: ErrorInfo): string {
    switch (errorInfo.type) {
      case ErrorType.NETWORK_ERROR:
        return '网络连接异常，请检查网络设置';
      case ErrorType.API_ERROR:
        return this.getApiErrorMessage(errorInfo.code, errorInfo.message);
      case ErrorType.PERMISSION_ERROR:
        return '您没有执行此操作的权限';
      case ErrorType.VALIDATION_ERROR:
        return errorInfo.message || '输入信息有误，请检查后重试';
      default:
        return '系统繁忙，请稍后重试';
    }
  }

  /**
   * 获取API错误消息
   */
  private getApiErrorMessage(code?: string | number, message?: string): string {
    const errorMessages: Record<string, string> = {
      '400': '请求参数错误',
      '401': '登录已过期，请重新登录',
      '403': '权限不足',
      '404': '请求的资源不存在',
      '500': '服务器内部错误',
      '502': '网关错误',
      '503': '服务暂时不可用',
      '504': '请求超时'
    };

    if (code && errorMessages[code.toString()]) {
      return errorMessages[code.toString()];
    }

    return message || '服务器响应异常';
  }

  /**
   * 上报错误到服务器
   */
  private async reportError(errorInfo: ErrorInfo) {
    try {
      // TODO: 实现错误上报API
      // await reportErrorToServer(errorInfo);
      console.log('Error reported:', errorInfo);
    } catch (e) {
      console.warn('Failed to report error:', e);
    }
  }

  /**
   * 获取错误历史
   */
  getErrorHistory(): ErrorInfo[] {
    return [...this.errorQueue];
  }

  /**
   * 清除错误历史
   */
  clearErrorHistory() {
    this.errorQueue = [];
    localStorage.removeItem('app_errors');
  }

  /**
   * 检查是否需要重试
   */
  shouldRetry(key: string): boolean {
    const attempts = this.retryAttempts.get(key) || 0;
    return attempts < this.maxRetryAttempts;
  }

  /**
   * 记录重试次数
   */
  recordRetryAttempt(key: string) {
    const attempts = this.retryAttempts.get(key) || 0;
    this.retryAttempts.set(key, attempts + 1);
  }

  /**
   * 重置重试次数
   */
  resetRetryAttempts(key: string) {
    this.retryAttempts.delete(key);
  }
}

/**
 * 创建错误处理器实例
 */
export const errorHandler = GlobalErrorHandler.getInstance();

/**
 * 错误处理装饰器
 */
export function handleError(target: any, propertyKey: string, descriptor: PropertyDescriptor) {
  const originalMethod = descriptor.value;

  descriptor.value = async function (...args: any[]) {
    try {
      return await originalMethod.apply(this, args);
    } catch (error) {
      errorHandler.handleError({
        type: ErrorType.UNKNOWN_ERROR,
        message: `方法 ${propertyKey} 执行失败`,
        details: error,
        timestamp: Date.now()
      });
      throw error;
    }
  };

  return descriptor;
}

/**
 * 异步错误处理包装器
 */
export async function withErrorHandling<T>(
  operation: () => Promise<T>,
  errorType: ErrorType = ErrorType.UNKNOWN_ERROR,
  customMessage?: string
): Promise<T | null> {
  try {
    return await operation();
  } catch (error) {
    errorHandler.handleError({
      type: errorType,
      message: customMessage || '操作执行失败',
      details: error,
      timestamp: Date.now()
    });
    return null;
  }
}