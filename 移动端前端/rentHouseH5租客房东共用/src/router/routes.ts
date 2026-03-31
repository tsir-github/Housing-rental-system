import Layout from "@/layout/index.vue";
import type { RouteRecordRaw } from "vue-router";
// 引入路由配置
import commonRoutes from "./commonRoutes";
import tabBarRoutes from "./tabBarRoutes"; // 租客的底部导航路由
import landlordRoutes from "./landlordRoutes";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "root",
    component: Layout,
    redirect: (to) => {
      // 根据用户类型智能重定向
      try {
        const userStore = localStorage.getItem('app-user');
        if (userStore) {
          const parsed = JSON.parse(userStore);
          const userType = parsed.currentUserType;
          if (userType === 'LANDLORD') {
            return '/landlord/dashboard';
          } else if (userType === 'TENANT') {
            return '/tenant/search'; // 租客默认跳转到找房页面
          }
        }
      } catch (error) {
        console.error('获取用户类型失败:', error);
      }
      // 默认跳转到登录页
      return '/login';
    },
    children: [
      // 通用路由
      ...commonRoutes,
      // 租客路由（使用 tabBarRoutes）
      ...tabBarRoutes,
      // 房东路由
      ...landlordRoutes
    ]
  },
  // 404页面
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@views/common/404/404.vue"),
    meta: {
      title: "页面不存在",
      noCache: true
    }
  }
];

export default routes;