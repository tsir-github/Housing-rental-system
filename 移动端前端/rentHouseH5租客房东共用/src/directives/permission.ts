import type { Directive, DirectiveBinding } from 'vue';
import { UserTypeUtils, PermissionUtils } from '@/utils/userType';

/**
 * 权限检查指令
 * 用法：v-permission="'permission:name'" 或 v-permission="['permission1', 'permission2']"
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding);
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding);
  }
};

/**
 * 用户类型检查指令
 * 用法：v-user-type="'TENANT'" 或 v-user-type="['TENANT', 'LANDLORD']"
 */
export const userType: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkUserType(el, binding);
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkUserType(el, binding);
  }
};

/**
 * 房东专用指令
 * 用法：v-landlord-only
 */
export const landlordOnly: Directive = {
  mounted(el: HTMLElement) {
    if (!UserTypeUtils.isLandlord()) {
      hideElement(el);
    }
  },
  updated(el: HTMLElement) {
    if (!UserTypeUtils.isLandlord()) {
      hideElement(el);
    }
  }
};

/**
 * 租客专用指令
 * 用法：v-tenant-only
 */
export const tenantOnly: Directive = {
  mounted(el: HTMLElement) {
    if (!UserTypeUtils.isTenant()) {
      hideElement(el);
    }
  },
  updated(el: HTMLElement) {
    if (!UserTypeUtils.isTenant()) {
      hideElement(el);
    }
  }
};

// 辅助函数
function checkPermission(el: HTMLElement, binding: DirectiveBinding) {
  const { value } = binding;
  
  if (!value) {
    return;
  }

  let hasPermission = false;

  if (Array.isArray(value)) {
    // 数组形式：需要满足其中任意一个权限
    hasPermission = value.some(permission => UserTypeUtils.hasPermission(permission));
  } else if (typeof value === 'string') {
    // 字符串形式：检查单个权限
    hasPermission = UserTypeUtils.hasPermission(value);
  }

  if (!hasPermission) {
    hideElement(el);
  } else {
    showElement(el);
  }
}

function checkUserType(el: HTMLElement, binding: DirectiveBinding) {
  const { value } = binding;
  const currentUserType = UserTypeUtils.getCurrentUserType();
  
  if (!value || !currentUserType) {
    hideElement(el);
    return;
  }

  let hasUserType = false;

  if (Array.isArray(value)) {
    // 数组形式：需要满足其中任意一个用户类型
    hasUserType = value.includes(currentUserType);
  } else if (typeof value === 'string') {
    // 字符串形式：检查单个用户类型
    hasUserType = value === currentUserType;
  }

  if (!hasUserType) {
    hideElement(el);
  } else {
    showElement(el);
  }
}

function hideElement(el: HTMLElement) {
  // 使用display: none隐藏元素
  el.style.display = 'none';
  // 添加标记，表示元素被权限控制隐藏
  el.setAttribute('data-permission-hidden', 'true');
}

function showElement(el: HTMLElement) {
  // 只有当元素是被权限控制隐藏时才显示
  if (el.getAttribute('data-permission-hidden') === 'true') {
    el.style.display = '';
    el.removeAttribute('data-permission-hidden');
  }
}

// 导出所有指令
export default {
  permission,
  userType,
  landlordOnly,
  tenantOnly
};