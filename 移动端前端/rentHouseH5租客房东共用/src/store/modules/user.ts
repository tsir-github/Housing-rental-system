import { defineStore } from "pinia";
import type {
  loginQueryInterface,
  UserInfoInterface,
  UserStateInterface
} from "@/api/common/user/types";
import { UserType } from "@/api/common/user/types";
import { getUserInfo, login } from "@/api/common/user";
import { removeToken, setToken } from "@/utils/token";

export const useUserStore = defineStore({
  id: "app-user",
  state: (): UserStateInterface => ({
    token: null,
    userInfo: null,
    currentUserType: null
  }),
  getters: {
    // 是否为租客
    isTenant: (state) => state.currentUserType === 'TENANT',
    // 是否为房东
    isLandlord: (state) => state.currentUserType === 'LANDLORD',
    // 获取用户权限
    userPermissions: (state) => state.userInfo?.permissions || [],
    // 检查是否有特定权限
    hasPermission: (state) => (permission: string) => {
      return state.userInfo?.permissions?.includes(permission) || false;
    }
  },
  actions: {
    // setToken
    setToken(token: string) {
      this.token = token;
    },
    // 设置用户类型
    setUserType(userType: UserType) {
      // 将数字类型转换为字符串类型，保持与路由配置一致
      this.currentUserType = userType === UserType.LANDLORD ? 'LANDLORD' : 'TENANT';
      console.log('🔍 设置用户类型:', userType, '-> 转换后:', this.currentUserType);
    },
    
    // login
    async LoginAction(params: loginQueryInterface) {
      try {
        console.log('开始登录流程，参数:', params);
        
        // 1. 先设置用户类型，确保在获取用户信息前就已经设置好
        this.setUserType(params.userType);
        console.log('用户类型已预设置:', params.userType, '转换为:', this.currentUserType);
        
        // 2. 调用登录API
        const { data } = await login(params);
        console.log('登录API返回的token:', data);
        
        if (!data) {
          throw new Error('登录失败：未获取到有效的token');
        }
        
        // 3. 设置token到store和localStorage
        this.setToken(data);
        setToken(data);
        console.log('Token已设置到store和localStorage');
        
        // 4. 验证token是否正确设置
        const savedToken = localStorage.getItem('ACCESS_TOKEN');
        if (savedToken !== data) {
          console.error('Token设置验证失败');
          throw new Error('Token设置失败');
        }
        
        // 5. 获取用户信息（不会覆盖已设置的用户类型）
        console.log('开始获取用户信息');
        await this.GetInfoAction();
        
        // 6. 确保用户类型没有被覆盖
        const expectedUserType = params.userType === UserType.LANDLORD ? 'LANDLORD' : 'TENANT';
        if (this.currentUserType !== expectedUserType) {
          console.log('用户类型被覆盖，重新设置:', expectedUserType);
          this.currentUserType = expectedUserType;
        }
        
        console.log('登录流程完成，最终用户类型:', this.currentUserType);
        
      } catch (error) {
        console.error('登录流程失败:', error);
        
        // 清理可能的部分状态
        this.resetUserStore();
        removeToken();
        throw error;
      }
    },
    
    // setUserInfo - 修复：不要覆盖已设置的用户类型
    setUserInfo(userInfo: UserInfoInterface) {
      this.userInfo = userInfo;
      
      // 只有在用户类型为空时才设置，避免覆盖登录时设置的正确类型
      if (!this.currentUserType) {
        this.currentUserType = userInfo.userType === UserType.LANDLORD ? 'LANDLORD' : 'TENANT';
        console.log('🔍 从用户信息设置类型:', this.currentUserType);
      } else {
        console.log('🔍 保持已设置的用户类型:', this.currentUserType);
      }
    },
    // 获取用户信息
    async GetInfoAction() {
      try {
        const { data } = await getUserInfo();
        console.log('🔍 获取用户信息成功:', data);
        this.setUserInfo(data);
      } catch (error) {
        console.error('🔍 获取用户信息失败:', error);
        this.resetUserStore();
        removeToken();
        throw error;
      }
    },
    // 更新用户信息
    updateUserInfo(userInfo: Partial<UserInfoInterface>) {
      if (this.userInfo) {
        this.userInfo = { ...this.userInfo, ...userInfo };
      }
    },
    // 退出登录
    async Logout() {
      this.resetUserStore();
      removeToken();
    },
    // 退出登录
    async logout() {
      try {
        // TODO: 调用退出登录API
        this.resetUserStore();
        removeToken();
      } catch (error) {
        console.error('退出登录失败:', error);
        this.resetUserStore();
        removeToken();
      }
    },
    // 重置用户状态
    resetUserStore() {
      this.token = null;
      this.userInfo = null;
      this.currentUserType = null;
    }
  },
  // 设置为true，缓存state
  persist: true
});