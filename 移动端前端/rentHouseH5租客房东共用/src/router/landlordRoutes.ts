import type { RouteRecordRaw } from "vue-router";

/**
 * 房东路由配置
 */
const landlordRoutes: Array<RouteRecordRaw> = [
  {
    name: "LandlordDashboard",
    path: "/landlord/dashboard",
    component: () => import("@/views/landlord/Dashboard.vue"),
    meta: {
      title: "房东仪表板",
      userType: "LANDLORD",
      requiresAuth: true,
      showTabbar: true
    }
  },
  {
    name: "LandlordApartments",
    path: "/landlord/apartments",
    component: () => import("@/views/landlord/ApartmentList.vue"),
    meta: {
      title: "公寓管理",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordApartmentDetail",
    path: "/landlord/apartments/:id",
    component: () => import("@/views/landlord/ApartmentDetail.vue"),
    meta: {
      title: "公寓详情",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordApartmentForm",
    path: "/landlord/apartments/form/:id?",
    component: () => import("@/views/landlord/ApartmentForm.vue"),
    meta: {
      title: "编辑公寓",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordRooms",
    path: "/landlord/rooms",
    component: () => import("@/views/landlord/RoomList.vue"),
    meta: {
      title: "房源管理",
      userType: "LANDLORD",
      requiresAuth: true,
      showTabbar: true
    }
  },
  {
    name: "LandlordRoomDetail",
    path: "/landlord/rooms/:id",
    component: () => import("@/views/landlord/RoomDetail.vue"),
    meta: {
      title: "房源详情",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordRoomCreate",
    path: "/landlord/rooms/create",
    component: () => import("@/views/landlord/RoomForm.vue"),
    meta: {
      title: "发布房源",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordRoomEdit",
    path: "/landlord/rooms/edit/:id",
    component: () => import("@/views/landlord/RoomForm.vue"),
    meta: {
      title: "编辑房源",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordRoomForm",
    path: "/landlord/rooms/form/:id?",
    component: () => import("@/views/landlord/RoomForm.vue"),
    meta: {
      title: "房源表单",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordApplications",
    path: "/landlord/applications",
    component: () => import("@/views/landlord/ApplicationList.vue"),
    meta: {
      title: "申请管理",
      userType: "LANDLORD",
      requiresAuth: true,
      showTabbar: true
    }
  },
  {
    name: "LandlordApplicationDetail",
    path: "/landlord/applications/:id",
    component: () => import("@/views/landlord/ApplicationDetail.vue"),
    meta: {
      title: "申请详情",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordApplicationHistory",
    path: "/landlord/applications/history",
    component: () => import("@/views/landlord/ApplicationHistory.vue"),
    meta: {
      title: "申请历史",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordAppointments",
    path: "/landlord/appointments",
    component: () => import("@/views/landlord/AppointmentList.vue"),
    meta: {
      title: "预约管理",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordAppointmentDetail",
    path: "/landlord/appointments/:id",
    component: () => import("@/views/landlord/AppointmentDetail.vue"),
    meta: {
      title: "预约详情",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordReviews",
    path: "/landlord/reviews",
    component: () => import("@/views/landlord/ReviewStatus.vue"),
    meta: {
      title: "审核管理",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordReviewDetail",
    path: "/landlord/reviews/:roomId",
    component: () => import("@/views/landlord/ReviewDetail.vue"),
    meta: {
      title: "审核详情",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordAttrManage",
    path: "/landlord/attr-manage",
    component: () => import("@/views/landlord/AttributeManage.vue"),
    meta: {
      title: "属性管理",
      userType: "LANDLORD",
      requiresAuth: true,
      isShowNavBar: false // 不显示全局导航栏，使用页面自己的导航栏
    }
  },
  {
    name: "LandlordFacilityManage",
    path: "/landlord/facility-manage",
    component: () => import("@/views/landlord/FacilityManage.vue"),
    meta: {
      title: "配套管理",
      userType: "LANDLORD",
      requiresAuth: true,
      isShowNavBar: false // 不显示全局导航栏，使用页面自己的导航栏
    }
  },
  {
    name: "LandlordFeeManage",
    path: "/landlord/fee-manage",
    component: () => import("@/views/landlord/FeeManage.vue"),
    meta: {
      title: "杂费管理",
      userType: "LANDLORD",
      requiresAuth: true,
      isShowNavBar: false // 不显示全局导航栏，使用页面自己的导航栏
    }
  },
  {
    name: "LandlordLabelManage",
    path: "/landlord/label-manage",
    component: () => import("@/views/landlord/LabelManage.vue"),
    meta: {
      title: "标签管理",
      userType: "LANDLORD",
      requiresAuth: true,
      isShowNavBar: false // 不显示全局导航栏，使用页面自己的导航栏
    }
  },
  {
    name: "LandlordPaymentTypeManage",
    path: "/landlord/payment-type-manage",
    component: () => import("@/views/landlord/PaymentTypeManage.vue"),
    meta: {
      title: "支付方式管理",
      userType: "LANDLORD",
      requiresAuth: true,
      isShowNavBar: false // 不显示全局导航栏，使用页面自己的导航栏
    }
  },
  {
    name: "LandlordLeaseTermManage",
    path: "/landlord/lease-term-manage",
    component: () => import("@/views/landlord/LeaseTermManage.vue"),
    meta: {
      title: "租期管理",
      userType: "LANDLORD",
      requiresAuth: true,
      isShowNavBar: false // 不显示全局导航栏，使用页面自己的导航栏
    }
  },
  {
    name: "LandlordProfile",
    path: "/landlord/profile",
    component: () => import("@/views/landlord/LandlordProfile.vue"),
    meta: {
      title: "个人中心",
      userType: "LANDLORD",
      requiresAuth: true,
      showTabbar: true
    }
  },
  // 租约管理路由
  {
    name: "LandlordLeaseManagement",
    path: "/landlord/leases",
    component: () => import("@/views/landlord/LeaseManagement.vue"),
    meta: {
      title: "租约管理",
      userType: "LANDLORD",
      requiresAuth: true,
      showTabbar: true
    }
  },
  {
    name: "LandlordLeaseList",
    path: "/landlord/leases/list",
    component: () => import("@/views/landlord/LeaseList.vue"),
    meta: {
      title: "租约列表",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordLeaseDetail",
    path: "/landlord/leases/detail/:id",
    component: () => import("@/views/landlord/LeaseDetail.vue"),
    meta: {
      title: "租约详情",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordLeaseForm",
    path: "/landlord/leases/form/:id?",
    component: () => import("@/views/landlord/LeaseForm.vue"),
    meta: {
      title: "租约表单",
      userType: "LANDLORD",
      requiresAuth: true
    }
  },
  {
    name: "LandlordExpiringLeases",
    path: "/landlord/leases/expiring",
    component: () => import("@/views/landlord/ExpiringLeases.vue"),
    meta: {
      title: "即将到期",
      userType: "LANDLORD",
      requiresAuth: true
    }
  }
];

export default landlordRoutes;