import { showDialog } from "vant";
import router from "@/router";
import { useUserStore } from "@/store/modules/user";

export const useToLoginPage = () => {
  /**
   * @description: 跳转到登录页
   * @param path 跳转后的路径
   */
  const showToLoginPageDialog = (path: string = "/login") => {
    showDialog({
      title: "登录过期",
      message: "您的登录已过期，请重新登录",
      confirmButtonText: "重新登录",
      showCancelButton: false
    }).then(() => {
      // 清理用户状态
      const userStore = useUserStore();
      userStore.logout();
      
      // 获取当前路径作为重定向参数
      const currentPath = router.currentRoute.value.fullPath;
      const redirect = currentPath !== '/login' 
        ? `?redirect=${encodeURIComponent(currentPath)}` 
        : "";
      
      // 跳转到登录页
      router.replace(`${path}${redirect}`);
    }).catch(() => {
      // 用户取消也要强制跳转到登录页
      const userStore = useUserStore();
      userStore.logout();
      router.replace(path);
    });
  };
  
  return {
    showToLoginPageDialog
  };
};
