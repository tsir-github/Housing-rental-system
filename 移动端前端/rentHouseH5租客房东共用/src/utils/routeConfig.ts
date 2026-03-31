import { UserType } from "@/api/common/user/types";
import { UserTypeUtils } from "@/utils/userType";

/**
 * 路由配置工具类
 */
export class RouteConfigUtils {
  /**
   * 获取租客底部导航配置
   */
  static getTenantTabbarConfig() {
    return [
      {
        path: "/tenant/search",
        name: "TenantSearch",
        title: "找房",
        icon: "search"
      },
      {
        path: "/tenant/aiChat",
        name: "AiChat", 
        title: "AI助手",
        icon: "service-o"
      },
      {
        path: "/tenant/myRoom",
        name: "TenantMyRoom",
        title: "我的房间",
        icon: "home-o"
      },
      {
        path: "/message",
        name: "Message",
        title: "消息",
        icon: "comment-o"
      },
      {
        path: "/tenant/userCenter",
        name: "TenantUserCenter",
        title: "个人中心",
        icon: "user-o"
      }
    ];
  }

  /**
   * 获取房东底部导航配置
   */
  static getLandlordTabbarConfig() {
    return [
      {
        path: "/landlord/dashboard",
        name: "LandlordDashboard",
        title: "控制台",
        icon: "home-o"
      },
      {
        path: "/landlord/apartment",
        name: "LandlordApartment",
        title: "公寓管理",
        icon: "building-o"
      },
      {
        path: "/landlord/room",
        name: "LandlordRoom",
        title: "房源管理",
        icon: "shop-o"
      },
      {
        path: "/message",
        name: "Message",
        title: "消息",
        icon: "comment-o"
      },
      {
        path: "/landlord/profile",
        name: "LandlordProfile",
        title: "个人中心",
        icon: "user-o"
      }
    ];
  }

  /**
   * 根据用户类型获取底部导航配置
   */
  static getTabbarConfig() {
    const currentUserType = UserTypeUtils.getCurrentUserType();
    
    switch (currentUserType) {
      case UserType.TENANT:
        return this.getTenantTabbarConfig();
      case UserType.LANDLORD:
        return this.getLandlordTabbarConfig();
      default:
        return [];
    }
  }

  /**
   * 检查当前路由是否应该显示底部导航
   */
  static shouldShowTabbar(currentPath: string): boolean {
    const tabbarConfig = this.getTabbarConfig();
    return tabbarConfig.some(item => {
      // 检查精确匹配
      if (item.path === currentPath) {
        return true;
      }
      
      // 检查别名匹配（为了向后兼容）
      const aliasMap: Record<string, string[]> = {
        "/tenant/search": ["/search", "/", "/tenant/home"],
        "/tenant/aiChat": ["/aiChat"],
        "/tenant/myRoom": ["/myRoom"],
        "/tenant/userCenter": ["/userCenter"]
      };
      
      const aliases = aliasMap[item.path];
      return aliases && aliases.includes(currentPath);
    });
  }

  /**
   * 获取路由的显示标题
   */
  static getRouteTitle(routeName: string): string {
    const allConfigs = [
      ...this.getTenantTabbarConfig(),
      ...this.getLandlordTabbarConfig()
    ];
    
    const config = allConfigs.find(item => item.name === routeName);
    return config?.title || '';
  }
}