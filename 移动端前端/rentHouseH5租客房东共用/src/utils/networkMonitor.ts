import { ref, computed } from 'vue';
import { showToast, showNotify } from 'vant';

/**
 * 网络状态枚举
 */
export enum NetworkStatus {
  ONLINE = 'online',
  OFFLINE = 'offline',
  SLOW = 'slow'
}

/**
 * 网络类型枚举
 */
export enum NetworkType {
  WIFI = 'wifi',
  CELLULAR = 'cellular',
  ETHERNET = 'ethernet',
  UNKNOWN = 'unknown'
}

/**
 * 网络信息接口
 */
export interface NetworkInfo {
  status: NetworkStatus;
  type: NetworkType;
  effectiveType?: string;
  downlink?: number;
  rtt?: number;
  saveData?: boolean;
}

/**
 * 网络监控类
 */
export class NetworkMonitor {
  private static instance: NetworkMonitor;
  private listeners: Array<(info: NetworkInfo) => void> = [];
  private checkInterval: number | null = null;
  private lastCheckTime = 0;
  private checkFrequency = 30000; // 30秒检查一次

  // 响应式状态
  public isOnline = ref(navigator.onLine);
  public networkType = ref<NetworkType>(NetworkType.UNKNOWN);
  public connectionSpeed = ref<'fast' | 'slow' | 'unknown'>('unknown');
  public isSlowConnection = ref(false);

  private constructor() {
    this.setupEventListeners();
    this.detectNetworkType();
    this.startPeriodicCheck();
  }

  static getInstance(): NetworkMonitor {
    if (!NetworkMonitor.instance) {
      NetworkMonitor.instance = new NetworkMonitor();
    }
    return NetworkMonitor.instance;
  }

  /**
   * 设置事件监听器
   */
  private setupEventListeners() {
    // 监听在线/离线状态变化
    window.addEventListener('online', this.handleOnline.bind(this));
    window.addEventListener('offline', this.handleOffline.bind(this));

    // 监听网络信息变化（如果支持）
    if ('connection' in navigator) {
      const connection = (navigator as any).connection;
      connection.addEventListener('change', this.handleConnectionChange.bind(this));
    }

    // 监听页面可见性变化
    document.addEventListener('visibilitychange', this.handleVisibilityChange.bind(this));
  }

  /**
   * 处理在线状态
   */
  private handleOnline() {
    this.isOnline.value = true;
    this.detectNetworkType();
    this.notifyListeners();
    
    showNotify({
      type: 'success',
      message: '网络连接已恢复',
      duration: 2000
    });
  }

  /**
   * 处理离线状态
   */
  private handleOffline() {
    this.isOnline.value = false;
    this.networkType.value = NetworkType.UNKNOWN;
    this.connectionSpeed.value = 'unknown';
    this.isSlowConnection.value = false;
    this.notifyListeners();
    
    showNotify({
      type: 'danger',
      message: '网络连接已断开',
      duration: 3000
    });
  }

  /**
   * 处理网络连接变化
   */
  private handleConnectionChange() {
    this.detectNetworkType();
    this.checkConnectionSpeed();
    this.notifyListeners();
  }

  /**
   * 处理页面可见性变化
   */
  private handleVisibilityChange() {
    if (!document.hidden) {
      // 页面变为可见时检查网络状态
      this.checkNetworkStatus();
    }
  }

  /**
   * 检测网络类型
   */
  private detectNetworkType() {
    if (!this.isOnline.value) {
      this.networkType.value = NetworkType.UNKNOWN;
      return;
    }

    if ('connection' in navigator) {
      const connection = (navigator as any).connection;
      
      switch (connection.type) {
        case 'wifi':
          this.networkType.value = NetworkType.WIFI;
          break;
        case 'cellular':
          this.networkType.value = NetworkType.CELLULAR;
          break;
        case 'ethernet':
          this.networkType.value = NetworkType.ETHERNET;
          break;
        default:
          this.networkType.value = NetworkType.UNKNOWN;
      }
    } else {
      // 简单的网络类型检测
      this.networkType.value = NetworkType.UNKNOWN;
    }
  }

  /**
   * 检查连接速度
   */
  private async checkConnectionSpeed() {
    if (!this.isOnline.value) return;

    try {
      const startTime = performance.now();
      
      // 使用小图片测试连接速度
      const testImage = new Image();
      const imageUrl = `data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7?t=${Date.now()}`;
      
      await new Promise((resolve, reject) => {
        testImage.onload = resolve;
        testImage.onerror = reject;
        testImage.src = imageUrl;
      });

      const endTime = performance.now();
      const duration = endTime - startTime;

      // 根据响应时间判断连接速度
      if (duration < 100) {
        this.connectionSpeed.value = 'fast';
        this.isSlowConnection.value = false;
      } else if (duration < 500) {
        this.connectionSpeed.value = 'slow';
        this.isSlowConnection.value = true;
      } else {
        this.connectionSpeed.value = 'slow';
        this.isSlowConnection.value = true;
      }

      // 使用Network Information API（如果支持）
      if ('connection' in navigator) {
        const connection = (navigator as any).connection;
        if (connection.effectiveType) {
          const effectiveType = connection.effectiveType;
          this.isSlowConnection.value = effectiveType === 'slow-2g' || effectiveType === '2g';
        }
      }
    } catch (error) {
      console.warn('Failed to check connection speed:', error);
      this.connectionSpeed.value = 'unknown';
    }
  }

  /**
   * 检查网络状态
   */
  private async checkNetworkStatus() {
    const now = Date.now();
    if (now - this.lastCheckTime < 5000) return; // 5秒内不重复检查
    
    this.lastCheckTime = now;
    
    try {
      // 尝试发送一个小的网络请求来验证连接
      const response = await fetch('/api/health', {
        method: 'HEAD',
        cache: 'no-cache',
        signal: AbortSignal.timeout(5000)
      });
      
      if (response.ok) {
        if (!this.isOnline.value) {
          this.handleOnline();
        }
      } else {
        throw new Error('Network check failed');
      }
    } catch (error) {
      if (this.isOnline.value) {
        this.handleOffline();
      }
    }
  }

  /**
   * 开始定期检查
   */
  private startPeriodicCheck() {
    this.checkInterval = window.setInterval(() => {
      this.checkNetworkStatus();
      this.checkConnectionSpeed();
    }, this.checkFrequency);
  }

  /**
   * 停止定期检查
   */
  private stopPeriodicCheck() {
    if (this.checkInterval) {
      clearInterval(this.checkInterval);
      this.checkInterval = null;
    }
  }

  /**
   * 通知监听器
   */
  private notifyListeners() {
    const networkInfo: NetworkInfo = {
      status: this.isOnline.value ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE,
      type: this.networkType.value
    };

    if ('connection' in navigator) {
      const connection = (navigator as any).connection;
      networkInfo.effectiveType = connection.effectiveType;
      networkInfo.downlink = connection.downlink;
      networkInfo.rtt = connection.rtt;
      networkInfo.saveData = connection.saveData;
    }

    this.listeners.forEach(listener => {
      try {
        listener(networkInfo);
      } catch (error) {
        console.error('Network listener error:', error);
      }
    });
  }

  /**
   * 添加网络状态监听器
   */
  addListener(listener: (info: NetworkInfo) => void) {
    this.listeners.push(listener);
  }

  /**
   * 移除网络状态监听器
   */
  removeListener(listener: (info: NetworkInfo) => void) {
    const index = this.listeners.indexOf(listener);
    if (index > -1) {
      this.listeners.splice(index, 1);
    }
  }

  /**
   * 获取当前网络信息
   */
  getCurrentNetworkInfo(): NetworkInfo {
    const info: NetworkInfo = {
      status: this.isOnline.value ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE,
      type: this.networkType.value
    };

    if ('connection' in navigator) {
      const connection = (navigator as any).connection;
      info.effectiveType = connection.effectiveType;
      info.downlink = connection.downlink;
      info.rtt = connection.rtt;
      info.saveData = connection.saveData;
    }

    return info;
  }

  /**
   * 销毁监控器
   */
  destroy() {
    window.removeEventListener('online', this.handleOnline.bind(this));
    window.removeEventListener('offline', this.handleOffline.bind(this));
    
    if ('connection' in navigator) {
      const connection = (navigator as any).connection;
      connection.removeEventListener('change', this.handleConnectionChange.bind(this));
    }

    document.removeEventListener('visibilitychange', this.handleVisibilityChange.bind(this));
    
    this.stopPeriodicCheck();
    this.listeners = [];
  }
}

/**
 * 创建网络监控实例
 */
export const networkMonitor = NetworkMonitor.getInstance();

/**
 * 网络状态组合式函数
 */
export function useNetworkStatus() {
  const monitor = NetworkMonitor.getInstance();

  const isOnline = computed(() => monitor.isOnline.value);
  const networkType = computed(() => monitor.networkType.value);
  const isSlowConnection = computed(() => monitor.isSlowConnection.value);
  const connectionSpeed = computed(() => monitor.connectionSpeed.value);

  const addNetworkListener = (listener: (info: NetworkInfo) => void) => {
    monitor.addListener(listener);
  };

  const removeNetworkListener = (listener: (info: NetworkInfo) => void) => {
    monitor.removeListener(listener);
  };

  const getCurrentNetworkInfo = () => monitor.getCurrentNetworkInfo();

  return {
    isOnline,
    networkType,
    isSlowConnection,
    connectionSpeed,
    addNetworkListener,
    removeNetworkListener,
    getCurrentNetworkInfo
  };
}

/**
 * 离线提示组合式函数
 */
export function useOfflineNotification() {
  const { isOnline, isSlowConnection } = useNetworkStatus();

  const showOfflineToast = () => {
    showToast({
      message: '当前网络不可用，请检查网络连接',
      type: 'fail',
      duration: 3000
    });
  };

  const showSlowConnectionToast = () => {
    showToast({
      message: '网络连接较慢，请耐心等待',
      type: 'loading',
      duration: 2000
    });
  };

  return {
    isOnline,
    isSlowConnection,
    showOfflineToast,
    showSlowConnectionToast
  };
}