import type { RouteRecordRaw } from 'vue-router';

/**
 * 路由懒加载配置
 */
export interface LazyRouteConfig {
  webpackChunkName?: string;
  webpackPreload?: boolean;
  webpackPrefetch?: boolean;
}

/**
 * 创建懒加载路由
 */
export function createLazyRoute(
  importFn: () => Promise<any>,
  config: LazyRouteConfig = {}
): () => Promise<any> {
  const {
    webpackChunkName,
    webpackPreload = false,
    webpackPrefetch = false
  } = config;

  return () => {
    // 添加webpack魔法注释
    let comment = '';
    if (webpackChunkName) {
      comment += `webpackChunkName: "${webpackChunkName}"`;
    }
    if (webpackPreload) {
      comment += comment ? ', ' : '';
      comment += 'webpackPreload: true';
    }
    if (webpackPrefetch) {
      comment += comment ? ', ' : '';
      comment += 'webpackPrefetch: true';
    }

    return importFn();
  };
}

/**
 * 房东路由懒加载配置
 */
export const landlordRoutes = {
  // 仪表板
  dashboard: () => import(
    /* webpackChunkName: "landlord-dashboard" */
    /* webpackPreload: true */
    '@/views/landlord/Dashboard.vue'
  ),

  // 公寓管理
  apartmentList: () => import(
    /* webpackChunkName: "landlord-apartment" */
    '@/views/landlord/ApartmentList.vue'
  ),
  apartmentDetail: () => import(
    /* webpackChunkName: "landlord-apartment" */
    '@/views/landlord/ApartmentDetail.vue'
  ),
  apartmentForm: () => import(
    /* webpackChunkName: "landlord-apartment" */
    '@/views/landlord/ApartmentForm.vue'
  ),

  // 房源管理
  roomList: () => import(
    /* webpackChunkName: "landlord-room" */
    '@/views/landlord/RoomList.vue'
  ),
  roomDetail: () => import(
    /* webpackChunkName: "landlord-room" */
    '@/views/landlord/RoomDetail.vue'
  ),
  roomForm: () => import(
    /* webpackChunkName: "landlord-room" */
    '@/views/landlord/RoomForm.vue'
  ),

  // 申请管理
  applicationList: () => import(
    /* webpackChunkName: "landlord-application" */
    '@/views/landlord/ApplicationList.vue'
  ),
  applicationDetail: () => import(
    /* webpackChunkName: "landlord-application" */
    '@/views/landlord/ApplicationDetail.vue'
  ),

  // 预约管理
  appointmentList: () => import(
    /* webpackChunkName: "landlord-appointment" */
    '@/views/landlord/AppointmentList.vue'
  ),
  appointmentDetail: () => import(
    /* webpackChunkName: "landlord-appointment" */
    '@/views/landlord/AppointmentDetail.vue'
  ),

  // 审核管理
  reviewStatus: () => import(
    /* webpackChunkName: "landlord-review" */
    '@/views/landlord/ReviewStatus.vue'
  ),
  reviewDetail: () => import(
    /* webpackChunkName: "landlord-review" */
    '@/views/landlord/ReviewDetail.vue'
  ),
  reviewHistory: () => import(
    /* webpackChunkName: "landlord-review" */
    '@/views/landlord/ReviewHistory.vue'
  ),

  // 其他管理
  attrManage: () => import(
    /* webpackChunkName: "landlord-manage" */
    '@/views/landlord/AttrManage.vue'
  ),
  facilityManage: () => import(
    /* webpackChunkName: "landlord-manage" */
    '@/views/landlord/FacilityManage.vue'
  ),
  feeManage: () => import(
    /* webpackChunkName: "landlord-manage" */
    '@/views/landlord/FeeManage.vue'
  ),

  // 个人中心
  profile: () => import(
    /* webpackChunkName: "landlord-profile" */
    '@/views/landlord/LandlordProfile.vue'
  )
};

/**
 * 租客路由懒加载配置
 */
export const tenantRoutes = {
  // 搜索页面（租客首页）
  search: () => import(
    /* webpackChunkName: "tenant-search" */
    /* webpackPreload: true */
    '@/views/tenant/search/search.vue'
  ),

  roomDetail: () => import(
    /* webpackChunkName: "tenant-room" */
    '@/views/tenant/roomDetail/roomDetail.vue'
  ),

  // 我的房间
  myRoom: () => import(
    /* webpackChunkName: "tenant-room" */
    '@/views/tenant/myRoom/myRoom.vue'
  ),

  // 个人中心
  userCenter: () => import(
    /* webpackChunkName: "tenant-profile" */
    '@/views/tenant/userCenter/userCenter.vue'
  ),

  // AI助手功能
  aiChat: () => import(
    /* webpackChunkName: "tenant-ai-chat" */
    '@/views/tenant/aiChat/index.vue'
  )
};

/**
 * 通用路由懒加载配置
 */
export const commonRoutes = {
  // 登录注册
  login: () => import(
    /* webpackChunkName: "auth" */
    /* webpackPreload: true */
    '@/views/auth/Login.vue'
  ),
  register: () => import(
    /* webpackChunkName: "auth" */
    '@/views/auth/Register.vue'
  ),

  // 消息中心
  message: () => import(
    /* webpackChunkName: "message" */
    '@/views/common/Message.vue'
  ),

  // 错误页面
  notFound: () => import(
    /* webpackChunkName: "error" */
    '@/views/error/NotFound.vue'
  ),
  serverError: () => import(
    /* webpackChunkName: "error" */
    '@/views/error/ServerError.vue'
  )
};

/**
 * 路由预加载管理器
 */
export class RoutePreloader {
  private static instance: RoutePreloader;
  private preloadedRoutes = new Set<string>();
  private preloadPromises = new Map<string, Promise<any>>();

  private constructor() {}

  static getInstance(): RoutePreloader {
    if (!RoutePreloader.instance) {
      RoutePreloader.instance = new RoutePreloader();
    }
    return RoutePreloader.instance;
  }

  /**
   * 预加载路由组件
   */
  async preloadRoute(routeName: string, importFn: () => Promise<any>): Promise<void> {
    if (this.preloadedRoutes.has(routeName)) {
      return;
    }

    if (this.preloadPromises.has(routeName)) {
      return this.preloadPromises.get(routeName);
    }

    const promise = importFn().then(() => {
      this.preloadedRoutes.add(routeName);
      this.preloadPromises.delete(routeName);
    }).catch(error => {
      console.warn(`Failed to preload route ${routeName}:`, error);
      this.preloadPromises.delete(routeName);
    });

    this.preloadPromises.set(routeName, promise);
    return promise;
  }

  /**
   * 批量预加载路由
   */
  async preloadRoutes(routes: Record<string, () => Promise<any>>): Promise<void> {
    const promises = Object.entries(routes).map(([name, importFn]) => 
      this.preloadRoute(name, importFn)
    );

    await Promise.allSettled(promises);
  }

  /**
   * 预加载房东相关路由
   */
  async preloadLandlordRoutes(): Promise<void> {
    // 预加载核心房东路由
    const coreRoutes = {
      dashboard: landlordRoutes.dashboard,
      apartmentList: landlordRoutes.apartmentList,
      roomList: landlordRoutes.roomList,
      profile: landlordRoutes.profile
    };

    await this.preloadRoutes(coreRoutes);
  }

  /**
   * 预加载租客相关路由
   */
  async preloadTenantRoutes(): Promise<void> {
    // 预加载核心租客路由
    const coreRoutes = {
      search: tenantRoutes.search,
      myRoom: tenantRoutes.myRoom,
      userCenter: tenantRoutes.userCenter
    };

    await this.preloadRoutes(coreRoutes);
  }

  /**
   * 根据用户类型预加载路由
   */
  async preloadByUserType(userType: 'LANDLORD' | 'TENANT'): Promise<void> {
    if (userType === 'LANDLORD') {
      await this.preloadLandlordRoutes();
    } else if (userType === 'TENANT') {
      await this.preloadTenantRoutes();
    }
  }

  /**
   * 检查路由是否已预加载
   */
  isPreloaded(routeName: string): boolean {
    return this.preloadedRoutes.has(routeName);
  }

  /**
   * 清除预加载缓存
   */
  clearCache(): void {
    this.preloadedRoutes.clear();
    this.preloadPromises.clear();
  }
}

/**
 * 创建路由预加载器实例
 */
export const routePreloader = RoutePreloader.getInstance();

/**
 * 路由预加载组合式函数
 */
export function useRoutePreloader() {
  const preloader = RoutePreloader.getInstance();

  const preloadRoute = (routeName: string, importFn: () => Promise<any>) => 
    preloader.preloadRoute(routeName, importFn);

  const preloadRoutes = (routes: Record<string, () => Promise<any>>) => 
    preloader.preloadRoutes(routes);

  const preloadByUserType = (userType: 'LANDLORD' | 'TENANT') => 
    preloader.preloadByUserType(userType);

  const isPreloaded = (routeName: string) => 
    preloader.isPreloaded(routeName);

  return {
    preloadRoute,
    preloadRoutes,
    preloadByUserType,
    isPreloaded
  };
}

/**
 * 智能路由预加载策略
 */
export class SmartPreloadStrategy {
  private static instance: SmartPreloadStrategy;
  private userBehavior = new Map<string, number>();
  private routeFrequency = new Map<string, number>();

  private constructor() {
    this.setupBehaviorTracking();
  }

  static getInstance(): SmartPreloadStrategy {
    if (!SmartPreloadStrategy.instance) {
      SmartPreloadStrategy.instance = new SmartPreloadStrategy();
    }
    return SmartPreloadStrategy.instance;
  }

  /**
   * 设置行为追踪
   */
  private setupBehaviorTracking() {
    // 追踪路由访问频率
    window.addEventListener('popstate', () => {
      this.trackRouteAccess(window.location.pathname);
    });
  }

  /**
   * 追踪路由访问
   */
  private trackRouteAccess(route: string) {
    const count = this.routeFrequency.get(route) || 0;
    this.routeFrequency.set(route, count + 1);
  }

  /**
   * 获取推荐预加载的路由
   */
  getRecommendedRoutes(currentRoute: string, limit = 3): string[] {
    // 基于访问频率和用户行为推荐
    const recommendations: Array<{ route: string; score: number }> = [];

    for (const [route, frequency] of this.routeFrequency) {
      if (route !== currentRoute) {
        const behaviorScore = this.userBehavior.get(route) || 0;
        const score = frequency * 0.7 + behaviorScore * 0.3;
        recommendations.push({ route, score });
      }
    }

    return recommendations
      .sort((a, b) => b.score - a.score)
      .slice(0, limit)
      .map(item => item.route);
  }

  /**
   * 智能预加载
   */
  async smartPreload(currentRoute: string): Promise<void> {
    const recommendedRoutes = this.getRecommendedRoutes(currentRoute);
    
    // 根据推荐路由进行预加载
    for (const route of recommendedRoutes) {
      // 这里需要根据路由名称找到对应的导入函数
      // 实际实现中需要维护路由名称到导入函数的映射
    }
  }
}

/**
 * 创建智能预加载策略实例
 */
export const smartPreloadStrategy = SmartPreloadStrategy.getInstance();