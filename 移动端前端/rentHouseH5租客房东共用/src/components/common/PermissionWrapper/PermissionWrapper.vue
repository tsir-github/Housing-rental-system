<template>
  <div v-if="hasAccess">
    <slot />
  </div>
  
  <PermissionDenied
    v-else-if="showDenied"
    :title="deniedTitle"
    :message="deniedMessage"
    :required-permissions="normalizedPermissions"
    :show-details="showDetails"
    :show-contact-button="showContactButton"
    :show-back-button="showBackButton"
    :show-home-button="showHomeButton"
  />
  
  <div v-else-if="showFallback">
    <slot name="fallback">
      <div class="permission-fallback">
        <van-empty 
          image="error" 
          description="权限不足"
          :image-size="60"
        />
      </div>
    </slot>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { usePermission } from '@/composables/usePermission';
import PermissionDenied from '../PermissionDenied/PermissionDenied.vue';

interface Props {
  // 权限配置
  permission?: string | string[];
  userType?: string | string[];
  landlordOnly?: boolean;
  tenantOnly?: boolean;
  
  // 显示配置
  mode?: 'hide' | 'show-denied' | 'show-fallback';
  deniedTitle?: string;
  deniedMessage?: string;
  showDetails?: boolean;
  showContactButton?: boolean;
  showBackButton?: boolean;
  showHomeButton?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'hide',
  showDetails: true,
  showContactButton: true,
  showBackButton: false,
  showHomeButton: false
});

const { hasPermission, isLandlord, isTenant, currentUserType } = usePermission();

// 计算权限检查结果
const hasAccess = computed(() => {
  // 检查用户类型权限
  if (props.landlordOnly && !isLandlord.value) {
    return false;
  }
  
  if (props.tenantOnly && !isTenant.value) {
    return false;
  }
  
  // 检查用户类型匹配
  if (props.userType) {
    const userTypes = Array.isArray(props.userType) ? props.userType : [props.userType];
    if (!userTypes.includes(currentUserType.value || '')) {
      return false;
    }
  }
  
  // 检查具体权限
  if (props.permission) {
    return hasPermission(props.permission);
  }
  
  return true;
});

// 显示模式控制
const showDenied = computed(() => props.mode === 'show-denied' && !hasAccess.value);
const showFallback = computed(() => props.mode === 'show-fallback' && !hasAccess.value);

// 标准化权限列表
const normalizedPermissions = computed(() => {
  if (!props.permission) return [];
  return Array.isArray(props.permission) ? props.permission : [props.permission];
});
</script>

<style scoped lang="less">
.permission-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  padding: 20px;
}
</style>