import { showToast } from 'vant';
import { errorHandler, ErrorType } from './errorHandler';

/**
 * 重试配置接口
 */
export interface RetryConfig {
  maxAttempts?: number;
  delay?: number;
  backoff?: boolean;
  retryCondition?: (error: any) => boolean;
  onRetry?: (attempt: number, error: any) => void;
}

/**
 * 默认重试配置
 */
const DEFAULT_RETRY_CONFIG: Required<RetryConfig> = {
  maxAttempts: 3,
  delay: 1000,
  backoff: true,
  retryCondition: (error: any) => {
    // 默认重试条件：网络错误或5xx服务器错误
    if (!error.response) return true; // 网络错误
    const status = error.response?.status;
    return status >= 500 && status < 600; // 5xx错误
  },
  onRetry: () => {}
};

/**
 * 重试机制类
 */
export class RetryMechanism {
  private static instance: RetryMechanism;
  private retryMap = new Map<string, number>();

  private constructor() {}

  static getInstance(): RetryMechanism {
    if (!RetryMechanism.instance) {
      RetryMechanism.instance = new RetryMechanism();
    }
    return RetryMechanism.instance;
  }

  /**
   * 执行带重试的异步操作
   */
  async executeWithRetry<T>(
    operation: () => Promise<T>,
    config: RetryConfig = {},
    operationKey?: string
  ): Promise<T> {
    const finalConfig = { ...DEFAULT_RETRY_CONFIG, ...config };
    let lastError: any;
    
    for (let attempt = 1; attempt <= finalConfig.maxAttempts; attempt++) {
      try {
        const result = await operation();
        
        // 成功后清除重试记录
        if (operationKey) {
          this.retryMap.delete(operationKey);
        }
        
        return result;
      } catch (error) {
        lastError = error;
        
        // 记录重试次数
        if (operationKey) {
          this.retryMap.set(operationKey, attempt);
        }

        // 检查是否应该重试
        if (attempt === finalConfig.maxAttempts || !finalConfig.retryCondition(error)) {
          break;
        }

        // 调用重试回调
        finalConfig.onRetry(attempt, error);

        // 计算延迟时间
        const delay = finalConfig.backoff 
          ? finalConfig.delay * Math.pow(2, attempt - 1)
          : finalConfig.delay;

        // 等待后重试
        await this.sleep(delay);
      }
    }

    // 所有重试都失败，抛出最后一个错误
    throw lastError;
  }

  /**
   * 获取操作的重试次数
   */
  getRetryCount(operationKey: string): number {
    return this.retryMap.get(operationKey) || 0;
  }

  /**
   * 清除重试记录
   */
  clearRetryRecord(operationKey: string) {
    this.retryMap.delete(operationKey);
  }

  /**
   * 清除所有重试记录
   */
  clearAllRetryRecords() {
    this.retryMap.clear();
  }

  /**
   * 延迟函数
   */
  private sleep(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

/**
 * 创建重试机制实例
 */
export const retryMechanism = RetryMechanism.getInstance();

/**
 * API重试装饰器
 */
export function withRetry(config: RetryConfig = {}) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const originalMethod = descriptor.value;

    descriptor.value = async function (...args: any[]) {
      const operationKey = `${target.constructor.name}.${propertyKey}`;
      
      return retryMechanism.executeWithRetry(
        () => originalMethod.apply(this, args),
        {
          ...config,
          onRetry: (attempt, error) => {
            console.warn(`重试 ${operationKey} (第${attempt}次):`, error);
            
            // 显示重试提示
            if (attempt === 1) {
              showToast({
                message: '请求失败，正在重试...',
                type: 'loading',
                duration: 1000
              });
            }
            
            // 调用自定义重试回调
            config.onRetry?.(attempt, error);
          }
        },
        operationKey
      );
    };

    return descriptor;
  };
}

/**
 * HTTP请求重试配置
 */
export const HTTP_RETRY_CONFIG: RetryConfig = {
  maxAttempts: 3,
  delay: 1000,
  backoff: true,
  retryCondition: (error: any) => {
    // 网络错误
    if (!error.response) {
      return true;
    }
    
    const status = error.response.status;
    
    // 重试条件：5xx服务器错误、408超时、429限流
    return status >= 500 || status === 408 || status === 429;
  },
  onRetry: (attempt, error) => {
    errorHandler.handleError({
      type: ErrorType.API_ERROR,
      message: `API请求重试 (第${attempt}次)`,
      details: error,
      timestamp: Date.now()
    }, false); // 不显示给用户
  }
};

/**
 * 文件上传重试配置
 */
export const UPLOAD_RETRY_CONFIG: RetryConfig = {
  maxAttempts: 5,
  delay: 2000,
  backoff: true,
  retryCondition: (error: any) => {
    // 上传失败的重试条件更宽松
    if (!error.response) return true;
    
    const status = error.response.status;
    return status >= 500 || status === 408 || status === 413 || status === 429;
  },
  onRetry: (attempt, error) => {
    showToast({
      message: `上传失败，正在重试 (${attempt}/5)`,
      type: 'loading',
      duration: 1500
    });
  }
};

/**
 * 数据同步重试配置
 */
export const SYNC_RETRY_CONFIG: RetryConfig = {
  maxAttempts: 10,
  delay: 5000,
  backoff: true,
  retryCondition: () => true, // 数据同步总是重试
  onRetry: (attempt, error) => {
    console.log(`数据同步重试 (第${attempt}次):`, error);
  }
};

/**
 * 创建自定义重试函数
 */
export function createRetryFunction<T>(
  operation: () => Promise<T>,
  config: RetryConfig = {}
) {
  return () => retryMechanism.executeWithRetry(operation, config);
}

/**
 * 批量重试操作
 */
export async function retryBatch<T>(
  operations: Array<() => Promise<T>>,
  config: RetryConfig = {}
): Promise<Array<T | Error>> {
  const results = await Promise.allSettled(
    operations.map(op => retryMechanism.executeWithRetry(op, config))
  );

  return results.map(result => 
    result.status === 'fulfilled' ? result.value : result.reason
  );
}