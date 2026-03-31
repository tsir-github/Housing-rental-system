import { ref, nextTick } from 'vue';
import { showToast } from 'vant';

/**
 * 触摸反馈类型
 */
export enum FeedbackType {
  LIGHT = 'light',
  MEDIUM = 'medium',
  HEAVY = 'heavy',
  SUCCESS = 'success',
  WARNING = 'warning',
  ERROR = 'error'
}

/**
 * 移动端优化工具类
 */
export class MobileOptimization {
  private static instance: MobileOptimization;
  private touchStartTime = 0;
  private touchStartPosition = { x: 0, y: 0 };
  private isScrolling = false;
  private scrollTimeout: number | null = null;

  private constructor() {
    this.setupMobileOptimizations();
  }

  static getInstance(): MobileOptimization {
    if (!MobileOptimization.instance) {
      MobileOptimization.instance = new MobileOptimization();
    }
    return MobileOptimization.instance;
  }

  /**
   * 设置移动端优化
   */
  private setupMobileOptimizations() {
    // 禁用双击缩放
    this.disableDoubleTapZoom();
    
    // 优化滚动性能
    this.optimizeScrolling();
    
    // 设置视口
    this.setupViewport();
    
    // 优化触摸延迟
    this.optimizeTouchDelay();
  }

  /**
   * 禁用双击缩放
   */
  private disableDoubleTapZoom() {
    let lastTouchEnd = 0;
    
    document.addEventListener('touchend', (event) => {
      const now = Date.now();
      if (now - lastTouchEnd <= 300) {
        event.preventDefault();
      }
      lastTouchEnd = now;
    }, { passive: false });
  }

  /**
   * 优化滚动性能
   */
  private optimizeScrolling() {
    // 添加滚动监听
    document.addEventListener('scroll', () => {
      this.isScrolling = true;
      
      if (this.scrollTimeout) {
        clearTimeout(this.scrollTimeout);
      }
      
      this.scrollTimeout = window.setTimeout(() => {
        this.isScrolling = false;
      }, 150);
    }, { passive: true });

    // 添加CSS优化
    const style = document.createElement('style');
    style.textContent = `
      * {
        -webkit-overflow-scrolling: touch;
        -webkit-tap-highlight-color: transparent;
      }
      
      body {
        -webkit-user-select: none;
        -webkit-touch-callout: none;
        -webkit-text-size-adjust: none;
      }
      
      input, textarea {
        -webkit-user-select: auto;
      }
    `;
    document.head.appendChild(style);
  }

  /**
   * 设置视口
   */
  private setupViewport() {
    let viewport = document.querySelector('meta[name="viewport"]');
    
    if (!viewport) {
      viewport = document.createElement('meta');
      viewport.setAttribute('name', 'viewport');
      document.head.appendChild(viewport);
    }
    
    viewport.setAttribute('content', 
      'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover'
    );
  }

  /**
   * 优化触摸延迟
   */
  private optimizeTouchDelay() {
    // 添加FastClick样式
    const style = document.createElement('style');
    style.textContent = `
      .no-touch-delay {
        touch-action: manipulation;
      }
    `;
    document.head.appendChild(style);
  }

  /**
   * 触觉反馈
   */
  vibrate(type: FeedbackType = FeedbackType.LIGHT) {
    if (!navigator.vibrate) return;

    const patterns: Record<FeedbackType, number | number[]> = {
      [FeedbackType.LIGHT]: 10,
      [FeedbackType.MEDIUM]: 20,
      [FeedbackType.HEAVY]: 50,
      [FeedbackType.SUCCESS]: [10, 50, 10],
      [FeedbackType.WARNING]: [20, 100, 20],
      [FeedbackType.ERROR]: [50, 100, 50, 100, 50]
    };

    navigator.vibrate(patterns[type]);
  }

  /**
   * 防抖点击
   */
  debounceClick(callback: () => void, delay = 300) {
    let timeoutId: number | null = null;
    
    return () => {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
      
      timeoutId = window.setTimeout(() => {
        callback();
        timeoutId = null;
      }, delay);
    };
  }

  /**
   * 节流滚动
   */
  throttleScroll(callback: () => void, delay = 100) {
    let lastCall = 0;
    
    return () => {
      const now = Date.now();
      if (now - lastCall >= delay) {
        callback();
        lastCall = now;
      }
    };
  }

  /**
   * 检测滑动手势
   */
  detectSwipe(
    element: HTMLElement,
    onSwipe: (direction: 'left' | 'right' | 'up' | 'down', distance: number) => void,
    threshold = 50
  ) {
    let startX = 0;
    let startY = 0;
    let startTime = 0;

    const handleTouchStart = (e: TouchEvent) => {
      const touch = e.touches[0];
      startX = touch.clientX;
      startY = touch.clientY;
      startTime = Date.now();
    };

    const handleTouchEnd = (e: TouchEvent) => {
      const touch = e.changedTouches[0];
      const endX = touch.clientX;
      const endY = touch.clientY;
      const endTime = Date.now();

      const deltaX = endX - startX;
      const deltaY = endY - startY;
      const deltaTime = endTime - startTime;

      // 检查是否是快速滑动
      if (deltaTime > 500) return;

      const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
      
      if (distance < threshold) return;

      // 判断滑动方向
      if (Math.abs(deltaX) > Math.abs(deltaY)) {
        // 水平滑动
        onSwipe(deltaX > 0 ? 'right' : 'left', distance);
      } else {
        // 垂直滑动
        onSwipe(deltaY > 0 ? 'down' : 'up', distance);
      }
    };

    element.addEventListener('touchstart', handleTouchStart, { passive: true });
    element.addEventListener('touchend', handleTouchEnd, { passive: true });

    // 返回清理函数
    return () => {
      element.removeEventListener('touchstart', handleTouchStart);
      element.removeEventListener('touchend', handleTouchEnd);
    };
  }

  /**
   * 长按检测
   */
  detectLongPress(
    element: HTMLElement,
    onLongPress: () => void,
    duration = 500
  ) {
    let pressTimer: number | null = null;

    const handleTouchStart = () => {
      pressTimer = window.setTimeout(() => {
        onLongPress();
        this.vibrate(FeedbackType.MEDIUM);
      }, duration);
    };

    const handleTouchEnd = () => {
      if (pressTimer) {
        clearTimeout(pressTimer);
        pressTimer = null;
      }
    };

    element.addEventListener('touchstart', handleTouchStart, { passive: true });
    element.addEventListener('touchend', handleTouchEnd, { passive: true });
    element.addEventListener('touchmove', handleTouchEnd, { passive: true });

    return () => {
      element.removeEventListener('touchstart', handleTouchStart);
      element.removeEventListener('touchend', handleTouchEnd);
      element.removeEventListener('touchmove', handleTouchEnd);
      if (pressTimer) {
        clearTimeout(pressTimer);
      }
    };
  }

  /**
   * 安全区域适配
   */
  setupSafeArea() {
    const style = document.createElement('style');
    style.textContent = `
      :root {
        --safe-area-inset-top: env(safe-area-inset-top);
        --safe-area-inset-right: env(safe-area-inset-right);
        --safe-area-inset-bottom: env(safe-area-inset-bottom);
        --safe-area-inset-left: env(safe-area-inset-left);
      }
      
      .safe-area-top {
        padding-top: var(--safe-area-inset-top);
      }
      
      .safe-area-bottom {
        padding-bottom: var(--safe-area-inset-bottom);
      }
      
      .safe-area-left {
        padding-left: var(--safe-area-inset-left);
      }
      
      .safe-area-right {
        padding-right: var(--safe-area-inset-right);
      }
    `;
    document.head.appendChild(style);
  }

  /**
   * 键盘弹起处理
   */
  handleKeyboard() {
    let initialViewportHeight = window.innerHeight;

    const handleResize = () => {
      const currentHeight = window.innerHeight;
      const heightDiff = initialViewportHeight - currentHeight;
      
      if (heightDiff > 150) {
        // 键盘弹起
        document.body.classList.add('keyboard-open');
        document.body.style.setProperty('--keyboard-height', `${heightDiff}px`);
      } else {
        // 键盘收起
        document.body.classList.remove('keyboard-open');
        document.body.style.removeProperty('--keyboard-height');
      }
    };

    window.addEventListener('resize', handleResize);
    
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }

  /**
   * 获取设备信息
   */
  getDeviceInfo() {
    const userAgent = navigator.userAgent;
    
    return {
      isIOS: /iPad|iPhone|iPod/.test(userAgent),
      isAndroid: /Android/.test(userAgent),
      isWechat: /MicroMessenger/.test(userAgent),
      isAlipay: /AlipayClient/.test(userAgent),
      isMobile: /Mobi|Android/i.test(userAgent),
      isTablet: /iPad/.test(userAgent) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1),
      screenWidth: window.screen.width,
      screenHeight: window.screen.height,
      pixelRatio: window.devicePixelRatio || 1,
      orientation: window.orientation || 0
    };
  }

  /**
   * 性能监控
   */
  monitorPerformance() {
    if ('performance' in window) {
      // 监控页面加载性能
      window.addEventListener('load', () => {
        setTimeout(() => {
          const perfData = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
          
          console.log('Performance metrics:', {
            domContentLoaded: perfData.domContentLoadedEventEnd - perfData.domContentLoadedEventStart,
            loadComplete: perfData.loadEventEnd - perfData.loadEventStart,
            firstPaint: performance.getEntriesByType('paint').find(entry => entry.name === 'first-paint')?.startTime,
            firstContentfulPaint: performance.getEntriesByType('paint').find(entry => entry.name === 'first-contentful-paint')?.startTime
          });
        }, 0);
      });
    }
  }
}

/**
 * 创建移动端优化实例
 */
export const mobileOptimization = MobileOptimization.getInstance();

/**
 * 移动端优化组合式函数
 */
export function useMobileOptimization() {
  const optimization = MobileOptimization.getInstance();

  const vibrate = (type: FeedbackType = FeedbackType.LIGHT) => {
    optimization.vibrate(type);
  };

  const debounceClick = (callback: () => void, delay = 300) => {
    return optimization.debounceClick(callback, delay);
  };

  const throttleScroll = (callback: () => void, delay = 100) => {
    return optimization.throttleScroll(callback, delay);
  };

  const detectSwipe = (
    element: HTMLElement,
    onSwipe: (direction: 'left' | 'right' | 'up' | 'down', distance: number) => void,
    threshold = 50
  ) => {
    return optimization.detectSwipe(element, onSwipe, threshold);
  };

  const detectLongPress = (
    element: HTMLElement,
    onLongPress: () => void,
    duration = 500
  ) => {
    return optimization.detectLongPress(element, onLongPress, duration);
  };

  const getDeviceInfo = () => optimization.getDeviceInfo();

  return {
    vibrate,
    debounceClick,
    throttleScroll,
    detectSwipe,
    detectLongPress,
    getDeviceInfo
  };
}

/**
 * 触摸反馈指令
 */
export function createTouchFeedbackDirective() {
  return {
    mounted(el: HTMLElement, binding: any) {
      const feedbackType = binding.value || FeedbackType.LIGHT;
      
      const handleTouch = () => {
        mobileOptimization.vibrate(feedbackType);
      };

      el.addEventListener('touchstart', handleTouch, { passive: true });
      
      // 存储清理函数
      (el as any)._touchFeedbackCleanup = () => {
        el.removeEventListener('touchstart', handleTouch);
      };
    },
    
    unmounted(el: HTMLElement) {
      if ((el as any)._touchFeedbackCleanup) {
        (el as any)._touchFeedbackCleanup();
      }
    }
  };
}