<template>
  <div class="permission-denied">
    <div class="permission-denied__icon">
      <van-icon name="warning-o" size="48" color="#ee0a24" />
    </div>
    
    <div class="permission-denied__content">
      <h3 class="permission-denied__title">{{ title }}</h3>
      <p class="permission-denied__message">{{ message }}</p>
      
      <div class="permission-denied__details" v-if="showDetails">
        <div class="details-item">
          <span class="details-label">当前用户类型：</span>
          <span class="details-value">{{ currentUserTypeText }}</span>
        </div>
        
        <div class="details-item" v-if="requiredPermissions.length > 0">
          <span class="details-label">所需权限：</span>
          <div class="details-permissions">
            <van-tag 
              v-for="permission in requiredPermissions"
              :key="permission"
              type="danger"
              size="small"
            >
              {{ getPermissionText(permission) }}
            </van-tag>
          </div>
        </div>
      </div>
    </div>
    
    <div class="permission-denied__actions">
      <van-button 
        v-if="showContactButton"
        type="primary" 
        size="large"
        @click="handleContact"
      >
        联系管理员
      </van-button>
      
      <van-button 
        v-if="showBackButton"
        type="default" 
        size="large"
        @click="handleGoBack"
      >
        返回上页
      </van-button>
      
      <van-button 
        v-if="showHomeButton"
        type="default" 
        size="large"
        @click="handleGoHome"
      >
        返回首页
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { UserTypeUtils, PermissionUtils } from '@/utils/userType';

interface Props {
  title?: string;
  message?: string;
  requiredPermissions?: string[];
  showDetails?: boolean;
  showContactButton?: boolean;
  showBackButton?: boolean;
  showHomeButton?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  title: '权限不足',
  message: '您没有访问此功能的权限，请联系管理员申请相关权限。',
  requiredPermissions: () => [],
  showDetails: true,
  showContactButton: true,
  showBackButton: true,
  showHomeButton: true
});

const router = useRouter();

// 计算属性
const currentUserTypeText = computed(() => {
  const userType = UserTypeUtils.getCurrentUserType();
  return userType ? UserTypeUtils.getUserTypeDisplayName(userType) : '未登录';
});

// 权限文本映射
const permissionTextMap: Record<string, string> = {
  [PermissionUtils.LANDLORD_PERMISSIONS.APARTMENT_VIEW]: '公寓查看',
  [PermissionUtils.LANDLORD_PERMISSIONS.APARTMENT_CREATE]: '公寓创建',
  [PermissionUtils.LANDLORD_PERMISSIONS.APARTMENT_EDIT]: '公寓编辑',
  [PermissionUtils.LANDLORD_PERMISSIONS.APARTMENT_DELETE]: '公寓删除',
  [PermissionUtils.LANDLORD_PERMISSIONS.ROOM_VIEW]: '房源查看',
  [PermissionUtils.LANDLORD_PERMISSIONS.ROOM_CREATE]: '房源创建',
  [PermissionUtils.LANDLORD_PERMISSIONS.ROOM_EDIT]: '房源编辑',
  [PermissionUtils.LANDLORD_PERMISSIONS.ROOM_DELETE]: '房源删除',
  [PermissionUtils.LANDLORD_PERMISSIONS.ROOM_PUBLISH]: '房源发布',
  [PermissionUtils.LANDLORD_PERMISSIONS.APPLICATION_VIEW]: '申请查看',
  [PermissionUtils.LANDLORD_PERMISSIONS.APPLICATION_PROCESS]: '申请处理',
  [PermissionUtils.LANDLORD_PERMISSIONS.APPOINTMENT_VIEW]: '预约查看',
  [PermissionUtils.LANDLORD_PERMISSIONS.APPOINTMENT_PROCESS]: '预约处理',
  [PermissionUtils.LANDLORD_PERMISSIONS.REVIEW_VIEW]: '审核查看',
  [PermissionUtils.LANDLORD_PERMISSIONS.ATTR_MANAGE]: '属性管理',
  [PermissionUtils.LANDLORD_PERMISSIONS.FACILITY_MANAGE]: '配套管理',
  [PermissionUtils.LANDLORD_PERMISSIONS.FEE_MANAGE]: '杂费管理'
};

const getPermissionText = (permission: string) => {
  return permissionTextMap[permission] || permission;
};

// 事件处理
const handleContact = () => {
  // TODO: 实现联系管理员功能
  showToast('联系管理员功能开发中');
};

const handleGoBack = () => {
  router.go(-1);
};

const handleGoHome = () => {
  const userType = UserTypeUtils.getCurrentUserType();
  if (userType) {
    const homePath = UserTypeUtils.getDefaultHomePath(userType);
    router.push(homePath);
  } else {
    router.push('/');
  }
};
</script>

<style scoped lang="less">
.permission-denied {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 32px 20px;
  text-align: center;
  
  &__icon {
    margin-bottom: 24px;
  }
  
  &__content {
    margin-bottom: 32px;
    max-width: 320px;
  }
  
  &__title {
    font-size: 18px;
    font-weight: 600;
    color: #323233;
    margin: 0 0 12px 0;
  }
  
  &__message {
    font-size: 14px;
    color: #646566;
    line-height: 1.6;
    margin: 0 0 20px 0;
  }
  
  &__details {
    text-align: left;
    background: #f7f8fa;
    border-radius: 8px;
    padding: 16px;
    
    .details-item {
      display: flex;
      align-items: flex-start;
      margin-bottom: 8px;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    .details-label {
      font-size: 13px;
      color: #646566;
      min-width: 80px;
      flex-shrink: 0;
    }
    
    .details-value {
      font-size: 13px;
      color: #323233;
      font-weight: 500;
    }
    
    .details-permissions {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
    }
  }
  
  &__actions {
    display: flex;
    flex-direction: column;
    gap: 12px;
    width: 100%;
    max-width: 280px;
  }
}

// 响应式设计
@media (max-width: 375px) {
  .permission-denied {
    padding: 24px 16px;
    
    &__content {
      max-width: 100%;
    }
    
    &__actions {
      max-width: 100%;
    }
  }
}
</style>