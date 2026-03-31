import type { RouteRecordRaw } from "vue-router";

/**
 * 通用路由配置
 * 包含登录等租客和房东都可以访问的页面
 */
const commonRoutes: Array<RouteRecordRaw> = [
  {
    name: "Login",
    path: "/login",
    component: () => import("@views/common/login/login.vue"),
    meta: {
      title: "登录",
      noCache: true,
      requiresAuth: false
    }
  },
  {
    name: "RoomAvailabilityTest",
    path: "/test/room-availability",
    component: () => import("@views/common/test/RoomAvailabilityTest.vue"),
    meta: {
      title: "房源可用性测试",
      noCache: true,
      requiresAuth: false
    }
  }
];

export default commonRoutes;