/**
 * 缓存策略枚举
 */
export enum CacheStrategy {
  MEMORY_ONLY = 'memory_only',
  STORAGE_ONLY = 'storage_only',
  MEMORY_FIRST = 'memory_first',
  STORAGE_FIRST = 'storage_first'
}

/**
 * 缓存配置接口
 */
export interface CacheConfig {
  ttl?: number; // 生存时间（毫秒）
  maxSize?: number; // 最大缓存条目数
  strategy?: CacheStrategy;
  compress?: boolean; // 是否压缩
  encrypt?: boolean; // 是否加密
}

/**
 * 缓存项接口
 */
export interface CacheItem<T = any> {
  key: string;
  data: T;
  timestamp: number;
  ttl: number;
  accessCount: number;
  lastAccess: number;
  size: number;
}

/**
 * 缓存统计信息
 */
export interface CacheStats {
  totalItems: number;
  totalSize: number;
  hitCount: number;
  missCount: number;
  hitRate: number;
  memoryUsage: number;
  storageUsage: number;
}

/**
 * 缓存管理器
 */
export class CacheManager {
  private static instance: CacheManager;
  private memoryCache = new Map<string, CacheItem>();
  private config: Required<CacheConfig>;
  private stats = {
    hitCount: 0,
    missCount: 0,
    totalSize: 0
  };

  private constructor(config: CacheConfig = {}) {
    this.config = {
      ttl: 5 * 60 * 1000, // 默认5分钟
      maxSize: 100, // 默认最大100个条目
      strategy: CacheStrategy.MEMORY_FIRST,
      compress: false,
      encrypt: false,
      ...config
    };

    this.setupCleanupInterval();
  }

  static getInstance(config?: CacheConfig): CacheManager {
    if (!CacheManager.instance) {
      CacheManager.instance = new CacheManager(config);
    }
    return CacheManager.instance;
  }

  /**
   * 设置缓存
   */
  async set<T>(key: string, data: T, ttl?: number): Promise<void> {
    const item: CacheItem<T> = {
      key,
      data,
      timestamp: Date.now(),
      ttl: ttl || this.config.ttl,
      accessCount: 0,
      lastAccess: Date.now(),
      size: this.calculateSize(data)
    };

    // 根据策略选择存储位置
    switch (this.config.strategy) {
      case CacheStrategy.MEMORY_ONLY:
        this.setMemoryCache(key, item);
        break;
      case CacheStrategy.STORAGE_ONLY:
        await this.setStorageCache(key, item);
        break;
      case CacheStrategy.MEMORY_FIRST:
        this.setMemoryCache(key, item);
        await this.setStorageCache(key, item);
        break;
      case CacheStrategy.STORAGE_FIRST:
        await this.setStorageCache(key, item);
        this.setMemoryCache(key, item);
        break;
    }
  }

  /**
   * 获取缓存
   */
  async get<T>(key: string): Promise<T | null> {
    let item: CacheItem<T> | null = null;

    // 根据策略选择获取位置
    switch (this.config.strategy) {
      case CacheStrategy.MEMORY_ONLY:
        item = this.getMemoryCache<T>(key);
        break;
      case CacheStrategy.STORAGE_ONLY:
        item = await this.getStorageCache<T>(key);
        break;
      case CacheStrategy.MEMORY_FIRST:
        item = this.getMemoryCache<T>(key);
        if (!item) {
          item = await this.getStorageCache<T>(key);
          if (item) {
            this.setMemoryCache(key, item);
          }
        }
        break;
      case CacheStrategy.STORAGE_FIRST:
        item = await this.getStorageCache<T>(key);
        if (!item) {
          item = this.getMemoryCache<T>(key);
        }
        break;
    }

    if (item && this.isValid(item)) {
      item.accessCount++;
      item.lastAccess = Date.now();
      this.stats.hitCount++;
      return item.data;
    }

    if (item) {
      // 过期的缓存，删除它
      await this.delete(key);
    }

    this.stats.missCount++;
    return null;
  }

  /**
   * 删除缓存
   */
  async delete(key: string): Promise<void> {
    this.memoryCache.delete(key);

    try {
      localStorage.removeItem(`cache_${key}`);
    } catch (error) {
      console.warn('Failed to delete from localStorage:', error);
    }
  }

  /**
   * 清空缓存
   */
  async clear(): Promise<void> {
    this.memoryCache.clear();

    try {
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith('cache_')) {
          localStorage.removeItem(key);
        }
      });
    } catch (error) {
      console.warn('Failed to clear localStorage:', error);
    }

    this.stats = { hitCount: 0, missCount: 0, totalSize: 0 };
  }

  /**
   * 检查缓存是否存在
   */
  async has(key: string): Promise<boolean> {
    const item = await this.get(key);
    return item !== null;
  }

  /**
   * 获取缓存统计信息
   */
  getStats(): CacheStats {
    const memoryUsage = Array.from(this.memoryCache.values())
      .reduce((total, item) => total + item.size, 0);

    let storageUsage = 0;
    try {
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith('cache_')) {
          storageUsage += localStorage.getItem(key)?.length || 0;
        }
      });
    } catch (error) {
      console.warn('Failed to calculate storage usage:', error);
    }

    const totalRequests = this.stats.hitCount + this.stats.missCount;
    const hitRate = totalRequests > 0 ? this.stats.hitCount / totalRequests : 0;

    return {
      totalItems: this.memoryCache.size,
      totalSize: this.stats.totalSize,
      hitCount: this.stats.hitCount,
      missCount: this.stats.missCount,
      hitRate,
      memoryUsage,
      storageUsage
    };
  }

  /**
   * 设置内存缓存
   */
  private setMemoryCache<T>(key: string, item: CacheItem<T>): void {
    // 检查是否需要清理空间
    if (this.memoryCache.size >= this.config.maxSize) {
      this.evictLRU();
    }

    this.memoryCache.set(key, item);
    this.stats.totalSize += item.size;
  }

  /**
   * 获取内存缓存
   */
  private getMemoryCache<T>(key: string): CacheItem<T> | null {
    return this.memoryCache.get(key) as CacheItem<T> || null;
  }

  /**
   * 设置存储缓存
   */
  private async setStorageCache<T>(key: string, item: CacheItem<T>): Promise<void> {
    try {
      let data = JSON.stringify(item);

      if (this.config.compress) {
        data = await this.compress(data);
      }

      if (this.config.encrypt) {
        data = await this.encrypt(data);
      }

      localStorage.setItem(`cache_${key}`, data);
    } catch (error) {
      console.warn('Failed to set storage cache:', error);
    }
  }

  /**
   * 获取存储缓存
   */
  private async getStorageCache<T>(key: string): Promise<CacheItem<T> | null> {
    try {
      let data = localStorage.getItem(`cache_${key}`);
      if (!data) return null;

      if (this.config.encrypt) {
        data = await this.decrypt(data);
      }

      if (this.config.compress) {
        data = await this.decompress(data);
      }

      return JSON.parse(data) as CacheItem<T>;
    } catch (error) {
      console.warn('Failed to get storage cache:', error);
      return null;
    }
  }

  /**
   * 检查缓存项是否有效
   */
  private isValid(item: CacheItem): boolean {
    const now = Date.now();
    return (now - item.timestamp) < item.ttl;
  }

  /**
   * LRU淘汰策略
   */
  private evictLRU(): void {
    let oldestKey = '';
    let oldestTime = Date.now();

    for (const [key, item] of this.memoryCache) {
      if (item.lastAccess < oldestTime) {
        oldestTime = item.lastAccess;
        oldestKey = key;
      }
    }

    if (oldestKey) {
      const item = this.memoryCache.get(oldestKey);
      if (item) {
        this.stats.totalSize -= item.size;
      }
      this.memoryCache.delete(oldestKey);
    }
  }

  /**
   * 计算数据大小
   */
  private calculateSize(data: any): number {
    return JSON.stringify(data).length * 2; // 粗略估算
  }

  /**
   * 压缩数据（简单实现）
   */
  private async compress(data: string): Promise<string> {
    // 这里可以使用更复杂的压缩算法
    return data;
  }

  /**
   * 解压数据
   */
  private async decompress(data: string): Promise<string> {
    return data;
  }

  /**
   * 加密数据（简单实现）
   */
  private async encrypt(data: string): Promise<string> {
    // 这里可以使用更安全的加密算法
    return btoa(data);
  }

  /**
   * 解密数据
   */
  private async decrypt(data: string): Promise<string> {
    return atob(data);
  }

  /**
   * 设置清理定时器
   */
  private setupCleanupInterval(): void {
    setInterval(() => {
      this.cleanup();
    }, 60000); // 每分钟清理一次
  }

  /**
   * 清理过期缓存
   */
  private cleanup(): void {
    const now = Date.now();
    const expiredKeys: string[] = [];

    for (const [key, item] of this.memoryCache) {
      if (!this.isValid(item)) {
        expiredKeys.push(key);
      }
    }

    expiredKeys.forEach(key => {
      const item = this.memoryCache.get(key);
      if (item) {
        this.stats.totalSize -= item.size;
      }
      this.memoryCache.delete(key);
    });

    // 清理localStorage中的过期缓存
    try {
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith('cache_')) {
          const data = localStorage.getItem(key);
          if (data) {
            try {
              const item = JSON.parse(data) as CacheItem;
              if (!this.isValid(item)) {
                localStorage.removeItem(key);
              }
            } catch (error) {
              // 无效的缓存数据，删除
              localStorage.removeItem(key);
            }
          }
        }
      });
    } catch (error) {
      console.warn('Failed to cleanup localStorage:', error);
    }
  }
}

/**
 * 创建缓存管理器实例
 */
export const cacheManager = CacheManager.getInstance();

/**
 * API缓存装饰器
 */
export function cached(ttl?: number, key?: string) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const originalMethod = descriptor.value;

    descriptor.value = async function (...args: any[]) {
      const cacheKey = key || `${target.constructor.name}.${propertyKey}.${JSON.stringify(args)}`;

      // 尝试从缓存获取
      const cachedResult = await cacheManager.get(cacheKey);
      if (cachedResult !== null) {
        return cachedResult;
      }

      // 执行原方法
      const result = await originalMethod.apply(this, args);

      // 缓存结果
      await cacheManager.set(cacheKey, result, ttl);

      return result;
    };

    return descriptor;
  };
}

/**
 * 缓存组合式函数
 */
export function useCache() {
  const cache = CacheManager.getInstance();

  const set = <T>(key: string, data: T, ttl?: number) => cache.set(key, data, ttl);
  const get = <T>(key: string) => cache.get<T>(key);
  const del = (key: string) => cache.delete(key);
  const clear = () => cache.clear();
  const has = (key: string) => cache.has(key);
  const stats = () => cache.getStats();

  return {
    set,
    get,
    delete: del,
    clear,
    has,
    stats
  };
}