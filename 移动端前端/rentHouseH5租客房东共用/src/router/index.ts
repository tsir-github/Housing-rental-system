import {
  createRouter,
  createWebHashHistory,
  type RouteLocationNormalized
} from "vue-router";
import routes from "./routes";
import NProgress from "@/utils/progress";
import setPageTitle from "@/utils/set-page-title";
import { TokenChecker } from "@/utils/tokenChecker";
import { PageStateManager } from "@/utils/pageStateManager";
import { getToken } from "@/utils/token";

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

export interface toRouteType extends RouteLocationNormalized {
  meta: {
    title?: string;
    noCache?: boolean;
    requiresAuth?: boolean;
    userType?: string;
    permissions?: string[];
    showTabbar?: boolean;
  };
}

router.beforeEach(async (to: toRouteType, _from, next) => {
  NProgress.start();
  
  try {
    // 动态导入避免循环依赖
    const [{ useCachedViewStore }, { useUserStore }] = await Promise.all([
      import("@/store/modules/cachedView"),
      import("@/store/modules/user")
    ]);
    
    // 清理过期的表单数据
    PageStateManager.cleanupExpiredFormData();
    
    // 清除登录页缓存
    if (to.name === "Login") {
      const cachedViewStore = useCachedViewStore();
      if (cachedViewStore?.delAllCachedViews) {
        cachedViewStore.delAllCachedViews();
      }
    }

    // 检查是否需要登录
    if (to.meta.requiresAuth !== false) {
      const userStore = useUserStore();
      
      console.log('🔍 路由守卫 - 检查认证状态');
      console.log('🔍 目标路径:', to.fullPath);
      
      // 使用TokenChecker进行更完整的Token检查
      const token = getToken();
      
      if (!token) {
        console.log('🔍 Token不存在，跳转登录');
        if (to.path !== '/login') {
          PageStateManager.saveCurrentPath(to.fullPath);
          next({
            path: '/login',
            query: { redirect: to.fullPath },
            replace: true
          });
          return;
        }
      } else {
        // 检查token格式和有效性
        if (!TokenChecker.isTokenFormatValid(token)) {
          console.log('🔍 Token格式无效，跳转登录');
          if (to.path !== '/login') {
            PageStateManager.saveCurrentPath(to.fullPath);
            next({
              path: '/login',
              query: { redirect: to.fullPath },
              replace: true
            });
            return;
          }
        }
        
        // 检查token是否过期
        if (TokenChecker.isTokenExpired(token)) {
          console.log('🔍 Token已过期，跳转登录');
          if (to.path !== '/login') {
            PageStateManager.saveCurrentPath(to.fullPath);
            next({
              path: '/login',
              query: { redirect: to.fullPath },
              replace: true
            });
            return;
          }
        }
        
        // 同步store中的token（如果不一致）
        if (userStore.token !== token) {
          console.log('🔍 同步store中的token');
          userStore.setToken(token);
        }
        
        // 检查用户类型权限
        const requiredUserType = to.meta.userType;
        const currentUserType = userStore.currentUserType;
        
        console.log('🔍 用户类型权限检查:', {
          需要的类型: requiredUserType,
          当前类型: currentUserType,
          目标路径: to.fullPath,
          用户信息: userStore.userInfo?.userType
        });
        
        // 如果store中没有用户类型但有用户信息，尝试从用户信息中获取
        if (!currentUserType && userStore.userInfo) {
          console.log('🔍 从用户信息中恢复用户类型');
          userStore.setUserType(userStore.userInfo.userType);
        }
        
        // 重新获取当前用户类型
        const finalUserType = userStore.currentUserType;
        
        if (requiredUserType && finalUserType && requiredUserType !== finalUserType) {
          console.log('🔍 用户类型不匹配，跳转到对应首页');
          const homePath = finalUserType === 'LANDLORD' ? '/landlord/dashboard' : '/tenant/search';
          next(homePath);
          return;
        }
        
        // 路径级别的用户类型检查
        if (to.path.startsWith('/landlord/') && finalUserType !== 'LANDLORD') {
          console.log('🔍 房东页面访问被拒绝，当前用户类型:', finalUserType);
          next('/tenant/search');
          return;
        }
        
        if (to.path.startsWith('/tenant/') && finalUserType !== 'TENANT') {
          console.log('🔍 租客页面访问被拒绝，当前用户类型:', finalUserType);
          next('/landlord/dashboard');
          return;
        }
      }
    }

    // 路由缓存处理
    const cachedViewStore = useCachedViewStore();
    if (to.name && typeof to.name === 'string' && cachedViewStore) {
      // 如果路由配置了 noCache: true，则从缓存中移除
      if (to.meta.noCache) {
        if (cachedViewStore.removeCachedView) {
          cachedViewStore.removeCachedView(to.name);
        }
      } else {
        // 否则添加到缓存
        if (cachedViewStore.addCachedView) {
          cachedViewStore.addCachedView(to.name);
        }
      }
    }
    
    // 页面 title
    setPageTitle(to.meta.title);
    
    next();
  } catch (error) {
    console.error('🔍 路由守卫执行失败:', error);
    // 出错时也要保存当前路径，以便恢复
    if (to.meta.requiresAuth !== false && to.path !== '/login') {
      PageStateManager.saveCurrentPath(to.fullPath);
    }
    next();
  }
});

router.afterEach(() => {
  NProgress.done();
});

export default router;