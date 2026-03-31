<template>
  <div class="login-container">
    <!-- 头部Logo区域 -->
    <div class="header-section">
      <div class="logo-area">
        <div class="logo-icon">🏠</div>
        <h1 class="app-title">公寓找房</h1>
        <p class="app-subtitle">您的理想居住选择</p>
      </div>
    </div>

    <!-- 登录表单区域 -->
    <div class="form-section">
      <van-form @submit="onSubmit">
        <!-- 身份选择 -->
        <div class="user-type-section">
          <div class="section-title">选择身份</div>
          <div class="user-type-buttons">
            <div 
              class="user-type-btn" 
              :class="{ active: userType === UserType.TENANT }"
              @click="selectUserType('tenant')"
            >
              <van-icon name="user-o" size="24" />
              <span>我是租客</span>
            </div>
            <div 
              class="user-type-btn" 
              :class="{ active: userType === UserType.LANDLORD }"
              @click="selectUserType('landlord')"
            >
              <van-icon name="manager-o" size="24" />
              <span>我是房东</span>
            </div>
          </div>
        </div>

        <!-- 手机号输入 -->
        <van-cell-group inset>
          <van-field
            v-model="phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            :rules="phoneRules"
            type="tel"
            maxlength="11"
            clearable
          />
          
          <!-- 验证码输入 -->
          <van-field
            v-model="code"
            name="code"
            label="验证码"
            placeholder="请输入验证码"
            :rules="codeRules"
            type="digit"
            maxlength="6"
            clearable
          >
            <template #button>
              <van-button 
                size="small" 
                type="primary" 
                :disabled="!canSendCode || countdown > 0"
                @click="sendCode"
                :loading="sendingCode"
              >
                {{ countdown > 0 ? `${countdown}s后重发` : '发送验证码' }}
              </van-button>
            </template>
          </van-field>
        </van-cell-group>

        <!-- 登录按钮 -->
        <div class="login-btn-section">
          <van-button 
            type="primary" 
            size="large" 
            block 
            round
            native-type="submit"
            :loading="logging"
            :disabled="logging"
          >
            {{ logging ? '登录中...' : '立即登录' }}
          </van-button>
        </div>

        <!-- 协议条款 -->
        <div class="agreement-section">
          <van-checkbox v-model="agreeTerms" icon-size="14px">
            我已阅读并同意
            <span class="link-text">《用户协议》</span>
            和
            <span class="link-text">《隐私政策》</span>
          </van-checkbox>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast } from 'vant';
import { useUserStore } from '@/store/modules/user';
import { UserType } from '@/api/common/user/types';
import { PageStateManager } from '@/utils/pageStateManager';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 表单数据
const userType = ref<UserType>(UserType.TENANT);
const phone = ref('15345728495');
const code = ref('');
const agreeTerms = ref(false);

// 状态管理
const logging = ref(false);
const sendingCode = ref(false);
const countdown = ref(0);

// 表单验证规则
const phoneRules = [
  { required: true, message: '请输入手机号' },
  { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
];

const codeRules = [
  { required: true, message: '请输入验证码' },
  { pattern: /^\d{6}$/, message: '请输入6位数字验证码' }
];

// 计算属性
const canSendCode = computed(() => {
  return /^1[3-9]\d{9}$/.test(phone.value);
});

// 选择用户类型
const selectUserType = (type: 'tenant' | 'landlord') => {
  userType.value = type === 'tenant' ? UserType.TENANT : UserType.LANDLORD;
};

// 发送验证码
const sendCode = async () => {
  if (!canSendCode.value) {
    showToast('请输入正确的手机号');
    return;
  }

  if (countdown.value > 0) {
    showToast(`请等待 ${countdown.value} 秒后重试`);
    return;
  }

  try {
    sendingCode.value = true;
    
    console.log('准备发送验证码到:', phone.value);
    console.log('当前环境变量 VITE_APP_BASE_URL:', import.meta.env.VITE_APP_BASE_URL);
    
    // 调用后端API发送验证码
    const { getSmsCode } = await import('@/api/common/user');
    const response = await getSmsCode({ phone: phone.value });
    
    console.log('验证码发送响应:', response);
    showToast('验证码已发送');
    
    // 开始倒计时
    startCountdown();
    
  } catch (error: any) {
    console.error('发送验证码失败:', error);
    if (error.message?.includes('频繁')) {
      showToast('发送过于频繁，请稍后再试');
      // 即使失败也开始倒计时，防止用户继续点击
      startCountdown();
    } else {
      showToast('发送失败，请重试');
    }
  } finally {
    sendingCode.value = false;
  }
};

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60;
  const timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
    }
  }, 1000);
};

// 登录提交
const onSubmit = async () => {
  console.log('登录提交开始');
  
  // 防止重复提交
  if (logging.value) {
    console.log('登录正在进行中，忽略重复提交');
    return;
  }
  
  console.log('协议条款状态:', agreeTerms.value);
  console.log('用户类型:', userType.value);
  console.log('手机号:', phone.value);
  console.log('验证码:', code.value);
  
  // 验证协议条款
  if (!agreeTerms.value) {
    showToast({
      message: '请先同意用户协议和隐私政策',
      position: 'middle'
    });
    return;
  }

  // 验证手机号
  if (!phone.value || !/^1[3-9]\d{9}$/.test(phone.value)) {
    showToast({
      message: '请输入正确的手机号',
      position: 'middle'
    });
    return;
  }

  // 验证验证码
  if (!code.value || !/^\d{6}$/.test(code.value)) {
    showToast({
      message: '请输入6位数字验证码',
      position: 'middle'
    });
    return;
  }

  try {
    logging.value = true;
    console.log('开始调用登录API');
    
    // 在登录前完全清理旧状态，避免缓存污染
    console.log('🔍 登录前清理所有状态');
    userStore.resetUserStore();
    localStorage.removeItem('app-user');
    localStorage.removeItem('ACCESS_TOKEN');
    PageStateManager.clearSavedState();
    
    // 调用用户store的登录方法
    await userStore.LoginAction({
      phone: phone.value,
      code: code.value,
      userType: userType.value
    });
    
    console.log('登录API调用成功');
    showToast({
      message: '登录成功',
      type: 'success'
    });
    
    // 等待一小段时间确保状态更新完成
    await new Promise(resolve => setTimeout(resolve, 300));
    
    // 验证登录后的用户类型状态
    const finalUserType = userStore.currentUserType;
    console.log('🔍 登录完成后的用户类型验证:', {
      选择的类型: userType.value === UserType.LANDLORD ? 'LANDLORD' : 'TENANT',
      store中的类型: finalUserType,
      用户信息: userStore.userInfo
    });
    
    // 强制确保用户类型正确设置
    const expectedUserType = userType.value === UserType.LANDLORD ? 'LANDLORD' : 'TENANT';
    if (finalUserType !== expectedUserType) {
      console.log('🔍 用户类型不匹配，强制重新设置');
      userStore.setUserType(userType.value);
      // 再次等待状态更新
      await new Promise(resolve => setTimeout(resolve, 100));
    }
    
    // 根据用户类型跳转到对应页面
    const targetPath = userType.value === UserType.LANDLORD 
      ? '/landlord/dashboard' 
      : '/tenant/search';
    
    console.log('🔍 准备跳转到:', targetPath);
    console.log('🔍 当前用户类型:', userStore.currentUserType);
    
    // 使用replace而不是push，避免返回到登录页
    await router.replace(targetPath);
    
    console.log('🔍 跳转完成');
    
  } catch (error: any) {
    console.error('登录失败:', error);
    
    // 根据错误类型显示不同的提示
    let errorMessage = '登录失败，请重试';
    if (error?.message) {
      if (error.message.includes('验证码')) {
        errorMessage = '验证码错误或已过期，请重新获取';
      } else if (error.message.includes('手机号')) {
        errorMessage = '手机号不存在，请检查后重试';
      } else if (error.message.includes('网络')) {
        errorMessage = '网络连接异常，请检查网络后重试';
      }
    }
    
    showToast({
      message: errorMessage,
      type: 'fail',
      duration: 3000
    });
  } finally {
    logging.value = false;
  }
};
</script>

<style scoped lang="less">
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
}

.header-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  
  .logo-area {
    text-align: center;
    color: white;
    
    .logo-icon {
      font-size: 60px;
      margin-bottom: 16px;
    }
    
    .app-title {
      font-size: 32px;
      font-weight: bold;
      margin: 0 0 8px 0;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    }
    
    .app-subtitle {
      font-size: 16px;
      margin: 0;
      opacity: 0.9;
    }
  }
}

.form-section {
  background: white;
  border-radius: 24px 24px 0 0;
  padding: 32px 20px 40px;
  min-height: 60vh;
}

.user-type-section {
  margin-bottom: 24px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 16px;
  }
  
  .user-type-buttons {
    display: flex;
    gap: 12px;
    
    .user-type-btn {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 16px;
      border: 2px solid #f0f0f0;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      
      .van-icon {
        margin-bottom: 8px;
        color: #999;
      }
      
      span {
        font-size: 14px;
        color: #666;
        font-weight: 500;
      }
      
      &.active {
        border-color: #1989fa;
        background: #f0f8ff;
        
        .van-icon {
          color: #1989fa;
        }
        
        span {
          color: #1989fa;
        }
      }
      
      &:hover {
        border-color: #1989fa;
      }
    }
  }
}

.van-cell-group {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
  
  // 修复验证码按钮样式
  .van-field__button {
    .van-button {
      background-color: #1989fa !important;
      color: #fff !important;
      border: none !important;
      font-size: 12px !important;
      
      &:disabled {
        background-color: #c8c9cc !important;
        color: #fff !important;
      }
      
      &.van-button--loading {
        background-color: #1989fa !important;
        color: #fff !important;
      }
    }
  }
}

.login-btn-section {
  margin-bottom: 24px;
  
  .van-button {
    height: 50px;
    font-size: 16px;
    font-weight: 600;
    background-color: #1989fa !important;
    color: #fff !important;
    border: none !important;
    
    &:disabled {
      background-color: #c8c9cc !important;
      color: #fff !important;
    }
    
    &.van-button--loading {
      background-color: #1989fa !important;
      color: #fff !important;
    }
  }
}

.agreement-section {
  text-align: center;
  
  .van-checkbox {
    font-size: 12px;
    color: #999;
    
    .link-text {
      color: #1989fa;
    }
  }
}
</style>