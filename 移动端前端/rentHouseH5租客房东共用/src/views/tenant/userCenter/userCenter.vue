<template>
  <div class="user-container">
    <div class="user h-[30vh] flex flex-col justify-center items-center">
      <van-image
        @click="
          showImagePreview([userStore.userInfo?.avatarUrl || defaultAvatarUrl])
        "
        round
        width="30vw"
        height="30vw"
        :src="userStore.userInfo?.avatarUrl || defaultAvatarUrl"
      >
        <template v-slot:error>加载失败</template>
      </van-image>
      <div class="mt-[8px] font-bold text-[16px]">
        {{ userStore.userInfo?.nickname || "测试" }}
      </div>
    </div>
    <div class="main-container flex justify-around mt-[30px]">
      <div
        v-for="item in navList"
        :key="item.path"
        class="flex flex-col justify-center items-center"
        @click="navigateToPage(item.path)"
      >
        <SvgIcon :name="item.icon" size="50" />
        <span>{{ item.name }}</span>
      </div>
    </div>
    <div class="main-container flex justify-center mt-[150px]">
      <!--      退出登录-->
      <van-button type="primary" class="w-[50vw]" @click="logoutHandle"
        >退出登录</van-button
      >
    </div>
  </div>
</template>
<script setup lang="ts" name="UserCenter">
import { useUserStore } from "@/store/modules/user";
import { showImagePreview, showConfirmDialog, showToast } from "vant";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
console.log("router.currentRoute.value.path", router);

// 默认头像URL
const defaultAvatarUrl = "/favicon.ico";

const navList = ref([
  {
    icon: "历史",
    name: "浏览历史",
    path: "/tenant/browsingHistory"
  },
  {
    icon: "预约",
    name: "我的预约",
    path: "/tenant/myAppointment"
  },
  {
    icon: "合同",
    name: "我的租约",
    path: "/tenant/myAgreement"
  },
  {
    icon: "申请",
    name: "入住申请",
    path: "/tenant/myDirectMoveIn"
  }
]);

const userStore = useUserStore();

// 导航到指定页面，携带来源信息
const navigateToPage = (path: string) => {
  router.push({
    path: path,
    query: {
      from: '/tenant/userCenter'
    }
  });
};

// 退出登陆
const logoutHandle = async () => {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确定要退出登录吗？',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    });
    
    console.log('🚪 开始退出登录流程');
    
    // 清除用户信息和token
    await userStore.logout();
    
    // 额外清理：确保localStorage完全清空
    try {
      localStorage.removeItem('app-user');
      localStorage.removeItem('ACCESS_TOKEN');
      console.log('🚪 已清理localStorage中的用户数据');
    } catch (error) {
      console.warn('🚪 清理localStorage失败:', error);
    }
    
    showToast({
      message: '已退出登录',
      type: 'success'
    });
    
    // 清除页面状态管理器中的保存状态，避免重定向问题
    const { PageStateManager } = await import('@/utils/pageStateManager');
    PageStateManager.clearSavedState();
    
    console.log('🚪 准备跳转到登录页面');
    
    // 跳转到纯净的登录页面，不携带任何重定向参数
    await router.replace('/login');
    
    // 延迟刷新页面，确保完全清除状态
    setTimeout(() => {
      window.location.href = '/login';
    }, 100);
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('🚪 退出登录失败:', error);
      showToast({
        message: '退出登录失败',
        type: 'fail'
      });
    }
  }
};

console.log(userStore);

onMounted(() => {
  userStore.GetInfoAction();
});
</script>

<style scoped lang="less">
.user {
  background: var(--van-primary-background-color);
}
</style>
