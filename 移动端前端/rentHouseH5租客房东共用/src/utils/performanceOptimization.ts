/**
 * 性能优化工具类
 */
export class PerformanceOptimization {
  private static instance: PerformanceOptimization;
  private resourceObserver: PerformanceObserver | null = null;
  private navigationObserver: PerformanceObserver | null = null;
  private metrics = new Map<string, number>();

  private constructor() {
    this.setupPerformanceObservers();
    this.optimizeResourceLoading();
  }

  static getInstance(): PerformanceOptimization {
    if (!PerformanceOptimization.instance) {
      PerformanceOptimization.instance = new PerformanceOptimization();
    }
    return PerformanceOptimization.instance;
  }

  /**
   * 设置性能观察器
   */
  private setupPerformanceObservers() {
    if ('PerformanceObserver' in window) {
      // 观察资源加载性能
      this.resourceObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.analyzeResourcePerformance(entry as PerformanceResourceTiming);
        }
      });

      this.resourceObserver.observe({ entryTypes: ['resource'] });

      // 观察导航性能
      this.navigationObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.analyzeNavigationPerformance(entry as PerformanceNavigationTiming);
        }
      });

      this.navigationObserver.observe({ entryTypes: ['navigation'] });
    }
  }

  /**
   * 分析资源加载性能
   */
  private analyzeResourcePerformance(entry: PerformanceResourceTiming) {
    const { name, duration, transferSize, encodedBodySize } = entry;
    
    // 记录慢资源
    if (duration > 1000) {
      console.warn(`Slow resource detected: ${name} (${duration.toFixed(2)}ms)`);
    }

    // 记录大资源
    if (transferSize > 500 * 1024) {
      console.warn(`Large resource detected: ${name} (${(transferSize / 1024).toFixed(2)}KB)`);
    }

    // 计算压缩率
    if (encodedBodySize && transferSize) {
      const compressionRatio = (1 - transferSize / encodedBodySize) * 100;
      if (compressionRatio < 50) {
        console.warn(`Poor compression for: ${name} (${compressionRatio.toFixed(2)}%)`);
      }
    }
  }

  /**
   * 分析导航性能
   */
  private analyzeNavigationPerformance(entry: PerformanceNavigationTiming) {
    const metrics = {
      dns: entry.domainLookupEnd - entry.domainLookupStart,
      tcp: entry.connectEnd - entry.connectStart,
      ssl: entry.connectEnd - entry.secureConnectionStart,
      ttfb: entry.responseStart - entry.requestStart,
      download: entry.responseEnd - entry.responseStart,
      domParse: entry.domContentLoadedEventStart - entry.responseEnd,
      domReady: entry.domContentLoadedEventEnd - entry.domContentLoadedEventStart,
      load: entry.loadEventEnd - entry.loadEventStart
    };

    // 存储指标
    Object.entries(metrics).forEach(([key, value]) => {
      this.metrics.set(key, value);
    });

    // 输出性能报告
    console.log('Navigation Performance Metrics:', metrics);
  }

  /**
   * 优化资源加载
   */
  private optimizeResourceLoading() {
    // 预连接到关键域名
    this.preconnectToDomains([
      'https://api.example.com',
      'https://cdn.example.com'
    ]);

    // 预加载关键资源
    this.preloadCriticalResources();

    // 延迟加载非关键资源
    this.deferNonCriticalResources();
  }

  /**
   * 预连接到域名
   */
  private preconnectToDomains(domains: string[]) {
    domains.forEach(domain => {
      const link = document.createElement('link');
      link.rel = 'preconnect';
      link.href = domain;
      document.head.appendChild(link);
    });
  }

  /**
   * 预加载关键资源
   */
  private preloadCriticalResources() {
    const criticalResources = [
      { href: '/fonts/main.woff2', as: 'font', type: 'font/woff2' },
      { href: '/css/critical.css', as: 'style' }
    ];

    criticalResources.forEach(resource => {
      const link = document.createElement('link');
      link.rel = 'preload';
      link.href = resource.href;
      link.as = resource.as;
      if (resource.type) {
        link.type = resource.type;
      }
      if (resource.as === 'font') {
        link.crossOrigin = 'anonymous';
      }
      document.head.appendChild(link);
    });
  }

  /**
   * 延迟加载非关键资源
   */
  private deferNonCriticalResources() {
    // 延迟加载非关键CSS
    const deferCSS = (href: string) => {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = href;
      link.media = 'print';
      link.onload = () => {
        link.media = 'all';
      };
      document.head.appendChild(link);
    };

    // 延迟加载非关键JavaScript
    const deferJS = (src: string) => {
      const script = document.createElement('script');
      script.src = src;
      script.defer = true;
      document.head.appendChild(script);
    };

    // 在页面加载完成后加载非关键资源
    window.addEventListener('load', () => {
      // 这里可以添加非关键资源的加载
    });
  }

  /**
   * 资源提示优化
   */
  optimizeResourceHints() {
    // DNS预解析
    const dnsPrefetchDomains = [
      '//fonts.googleapis.com',
      '//fonts.gstatic.com',
      '//api.example.com'
    ];

    dnsPrefetchDomains.forEach(domain => {
      const link = document.createElement('link');
      link.rel = 'dns-prefetch';
      link.href = domain;
      document.head.appendChild(link);
    });

    // 预获取下一页可能需要的资源
    const prefetchResources = [
      '/js/next-page.js',
      '/css/next-page.css'
    ];

    prefetchResources.forEach(resource => {
      const link = document.createElement('link');
      link.rel = 'prefetch';
      link.href = resource;
      document.head.appendChild(link);
    });
  }

  /**
   * 代码分割优化
   */
  optimizeCodeSplitting() {
    // 动态导入优化
    const importWithRetry = async (importFn: () => Promise<any>, retries = 3): Promise<any> => {
      try {
        return await importFn();
      } catch (error) {
        if (retries > 0) {
          console.warn(`Import failed, retrying... (${retries} attempts left)`);
          await new Promise(resolve => setTimeout(resolve, 1000));
          return importWithRetry(importFn, retries - 1);
        }
        throw error;
      }
    };

    return { importWithRetry };
  }

  /**
   * 内存优化
   */
  optimizeMemoryUsage() {
    // 监控内存使用
    const monitorMemory = () => {
      if ('memory' in performance) {
        const memory = (performance as any).memory;
        const memoryInfo = {
          used: Math.round(memory.usedJSHeapSize / 1048576),
          total: Math.round(memory.totalJSHeapSize / 1048576),
          limit: Math.round(memory.jsHeapSizeLimit / 1048576)
        };

        console.log('Memory Usage:', memoryInfo);

        // 内存使用过高时的警告
        if (memoryInfo.used / memoryInfo.limit > 0.8) {
          console.warn('High memory usage detected!');
        }

        return memoryInfo;
      }
      return null;
    };

    // 定期监控内存
    setInterval(monitorMemory, 30000);

    return { monitorMemory };
  }

  /**
   * 网络优化
   */
  optimizeNetworkRequests() {
    // HTTP/2 服务器推送优化
    const optimizeHTTP2Push = () => {
      // 检测HTTP/2支持
      if ('serviceWorker' in navigator) {
        // 可以通过Service Worker实现推送优化
      }
    };

    // 请求合并优化
    const batchRequests = (requests: Array<() => Promise<any>>, batchSize = 5) => {
      const batches = [];
      for (let i = 0; i < requests.length; i += batchSize) {
        batches.push(requests.slice(i, i + batchSize));
      }

      return batches.reduce(async (prev, batch) => {
        await prev;
        return Promise.all(batch.map(request => request()));
      }, Promise.resolve());
    };

    return { optimizeHTTP2Push, batchRequests };
  }

  /**
   * 渲染优化
   */
  optimizeRendering() {
    // 防抖滚动事件
    const debounceScroll = (callback: () => void, delay = 16) => {
      let timeoutId: number;
      return () => {
        clearTimeout(timeoutId);
        timeoutId = window.setTimeout(callback, delay);
      };
    };

    // 节流resize事件
    const throttleResize = (callback: () => void, delay = 100) => {
      let lastCall = 0;
      return () => {
        const now = Date.now();
        if (now - lastCall >= delay) {
          callback();
          lastCall = now;
        }
      };
    };

    // 使用requestAnimationFrame优化动画
    const optimizeAnimation = (callback: () => void) => {
      let animationId: number;
      const animate = () => {
        callback();
        animationId = requestAnimationFrame(animate);
      };
      
      animate();
      
      return () => cancelAnimationFrame(animationId);
    };

    return { debounceScroll, throttleResize, optimizeAnimation };
  }

  /**
   * 获取性能指标
   */
  getPerformanceMetrics() {
    const metrics = Object.fromEntries(this.metrics);
    
    // 添加Web Vitals指标
    if ('PerformanceObserver' in window) {
      try {
        // LCP (Largest Contentful Paint)
        new PerformanceObserver((list) => {
          const entries = list.getEntries();
          const lastEntry = entries[entries.length - 1];
          metrics.lcp = lastEntry.startTime;
        }).observe({ entryTypes: ['largest-contentful-paint'] });

        // FID (First Input Delay)
        new PerformanceObserver((list) => {
          const entries = list.getEntries();
          entries.forEach(entry => {
            metrics.fid = entry.processingStart - entry.startTime;
          });
        }).observe({ entryTypes: ['first-input'] });

        // CLS (Cumulative Layout Shift)
        new PerformanceObserver((list) => {
          let clsValue = 0;
          const entries = list.getEntries();
          entries.forEach(entry => {
            if (!(entry as any).hadRecentInput) {
              clsValue += (entry as any).value;
            }
          });
          metrics.cls = clsValue;
        }).observe({ entryTypes: ['layout-shift'] });
      } catch (error) {
        console.warn('Failed to observe Web Vitals:', error);
      }
    }

    return metrics;
  }

  /**
   * 销毁性能观察器
   */
  destroy() {
    if (this.resourceObserver) {
      this.resourceObserver.disconnect();
      this.resourceObserver = null;
    }
    
    if (this.navigationObserver) {
      this.navigationObserver.disconnect();
      this.navigationObserver = null;
    }
  }
}

/**
 * 创建性能优化实例
 */
export const performanceOptimization = PerformanceOptimization.getInstance();

/**
 * 性能优化组合式函数
 */
export function usePerformanceOptimization() {
  const optimization = PerformanceOptimization.getInstance();

  const getMetrics = () => optimization.getPerformanceMetrics();
  const optimizeResourceHints = () => optimization.optimizeResourceHints();
  const { importWithRetry } = optimization.optimizeCodeSplitting();
  const { monitorMemory } = optimization.optimizeMemoryUsage();
  const { batchRequests } = optimization.optimizeNetworkRequests();
  const { debounceScroll, throttleResize, optimizeAnimation } = optimization.optimizeRendering();

  return {
    getMetrics,
    optimizeResourceHints,
    importWithRetry,
    monitorMemory,
    batchRequests,
    debounceScroll,
    throttleResize,
    optimizeAnimation
  };
}

/**
 * 性能监控装饰器
 */
export function performanceMonitor(name: string) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const originalMethod = descriptor.value;

    descriptor.value = async function (...args: any[]) {
      const startTime = performance.now();
      
      try {
        const result = await originalMethod.apply(this, args);
        const endTime = performance.now();
        const duration = endTime - startTime;
        
        console.log(`Performance [${name}]: ${duration.toFixed(2)}ms`);
        
        return result;
      } catch (error) {
        const endTime = performance.now();
        const duration = endTime - startTime;
        
        console.error(`Performance [${name}] failed after ${duration.toFixed(2)}ms:`, error);
        throw error;
      }
    };

    return descriptor;
  };
}