import type { RouteRecordRaw } from "vue-router";

/**
 * @description 📚 路由参数配置简介
 * @param path ==> 菜单路径
 * @param name ==> 菜单别名
 * @param redirect ==> 重定向地址
 * @param component ==> 视图文件路径
 * @param meta ==> 菜单信息
 * @param meta.icon ==> 菜单图标，只有tabBar菜单才需要配置
 * @param meta.title ==> 菜单标题
 * @param meta.noCache ==> 是否不缓存，默认开启组件缓存，需要vue组件中配置name属性和路由菜单别名一致
 * @param meta.isShowNavBar ==> 是否展示顶部导航栏，默认不展示
 * */
const routes: Array<RouteRecordRaw> = [
  {
    name: "Search",
    path: "/tenant/search",
    component: () => import("@/views/tenant/search/search.vue"),
    meta: {
      title: "找房",
      icon: "search",
      showTabbar: true
    }
  },
  {
    path: "/tenant/aiChat",
    name: "AiChat",
    component: () => import("@/views/tenant/aiChat/index.vue"),
    meta: {
      title: "AI助手",
      icon: "service-o",
      showTabbar: true,
      requiresAuth: true,
      userType: "TENANT"
    }
  },
  {
    path: "/tenant/myRoom",
    name: "MyRoom",
    component: () => import("@/views/tenant/myRoom/myRoom.vue"),
    meta: {
      title: "我的房间",
      icon: "home-o",
      showTabbar: true
    }
  },
  {
    path: "/tenant/message",
    name: "Message",
    component: () => import("@/views/common/message/message.vue"),
    meta: {
      title: "消息",
      icon: "comment-o",
      showTabbar: true
    }
  },
  {
    path: "/tenant/userCenter",
    name: "UserCenter",
    component: () => import("@/views/tenant/userCenter/userCenter.vue"),
    meta: {
      title: "个人中心",
      icon: "user-o",
      isShowNavBar: true,
      showTabbar: true
    }
  },
  // 租客的二级页面路由
  {
    path: "/tenant/roomDetail/:id",
    name: "TenantRoomDetail",
    component: () => import("@/views/tenant/roomDetail/roomDetail.vue"),
    meta: {
      title: "房源详情",
      requiresAuth: true,
      userType: "TENANT",
      noCache: true // 禁用缓存，确保每次访问都重新加载数据
    }
  },
  {
    path: "/tenant/apartmentDetail/:id",
    name: "TenantApartmentDetail",
    component: () => import("@/views/tenant/apartmentDetail/apartmentDetail.vue"),
    meta: {
      title: "公寓详情",
      requiresAuth: true,
      userType: "TENANT"
    }
  },
  {
    path: "/tenant/appointment",
    name: "TenantAppointment",
    component: () => import("@/views/tenant/appointment/appointment.vue"),
    meta: {
      title: "预约看房",
      requiresAuth: true,
      userType: "TENANT"
    }
  },
  {
    path: "/tenant/agreement/:id",
    name: "TenantAgreement",
    component: () => import("@/views/tenant/agreement/agreement.vue"),
    meta: {
      title: "租约详情",
      requiresAuth: true,
      userType: "TENANT"
    }
  },
  {
    path: "/tenant/directMoveIn",
    name: "TenantDirectMoveIn",
    component: () => import("@/views/tenant/directMoveIn/directMoveIn.vue"),
    meta: {
      title: "直接入住申请",
      requiresAuth: true,
      userType: "TENANT"
    }
  },
  {
    path: "/tenant/browsingHistory",
    name: "TenantBrowsingHistory",
    component: () => import("@/views/tenant/browsingHistory/browsingHistory.vue"),
    meta: {
      title: "浏览历史",
      requiresAuth: true,
      userType: "TENANT"
    }
  },
  {
    path: "/tenant/myAppointment",
    name: "TenantMyAppointment",
    component: () => import("@/views/tenant/myAppointment/myAppointment.vue"),
    meta: {
      title: "我的预约",
      requiresAuth: true,
      userType: "TENANT"
    }
  },
  {
    path: "/tenant/myAgreement",
    name: "TenantMyAgreement",
    component: () => import("@/views/tenant/myAgreement/myAgreement.vue"),
    meta: {
      title: "我的合同",
      requiresAuth: true,
      userType: "TENANT"
    }
  },
  {
    path: "/tenant/myDirectMoveIn",
    name: "TenantMyDirectMoveIn",
    component: () => import("@/views/tenant/myDirectMoveIn/myDirectMoveIn.vue"),
    meta: {
      title: "我的入住申请",
      requiresAuth: true,
      userType: "TENANT"
    }
  },
  {
    path: "/tenant/directMoveInDetail",
    name: "TenantDirectMoveInDetail",
    component: () => import("@/views/tenant/directMoveInDetail/directMoveInDetail.vue"),
    meta: {
      title: "申请详情",
      requiresAuth: true,
      userType: "TENANT"
    }
  }
];

export default routes;
