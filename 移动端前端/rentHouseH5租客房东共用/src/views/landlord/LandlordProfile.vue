<template>
  <PageContainer :loading="loading" :show-empty="showEmpty" empty-type="error">
    <!-- 个人信息卡片 -->
    <div class="profile-header">
      <div class="profile-avatar" @click="handleAvatarClick">
        <van-image
          :src="userInfo?.avatar || defaultAvatar"
          fit="cover"
          round
          width="80px"
          height="80px"
        >
          <template #error>
            <van-icon name="user-circle-o" size="80" color="#c8c9cc" />
          </template>
        </van-image>
        
        <div class="avatar-upload-btn">
          <van-icon name="camera-o" size="16" color="white" />
        </div>
      </div>
      
      <div class="profile-info">
        <h3 class="profile-name">{{ userInfo?.name || '未设置姓名' }}</h3>
        <p class="profile-phone">{{ userInfo?.phone || '未绑定手机' }}</p>
        <div class="profile-status">
          <van-tag 
            :type="getStatusType(userInfo?.status)"
            size="small"
          >
            {{ getStatusText(userInfo?.status) }}
          </van-tag>
        </div>
      </div>
      
      <div class="profile-edit">
        <van-button 
          type="primary" 
          size="small"
          @click="handleEditProfile"
        >
          编辑资料
        </van-button>
      </div>
    </div>

    <!-- 权限状态卡片 -->
    <div class="permission-status">
      <div class="section-title">
        <van-icon name="shield-o" size="16" color="#1989fa" />
        <span>权限状态</span>
      </div>
      
      <div class="permission-grid">
        <div
          v-for="permission in permissionList"
          :key="permission.key"
          class="permission-item"
          :class="{ active: permission.granted }"
        >
          <van-icon 
            :name="permission.icon" 
            size="20" 
            :color="permission.granted ? '#07c160' : '#c8c9cc'"
          />
          <span class="permission-name">{{ permission.name }}</span>
          <van-icon 
            :name="permission.granted ? 'success' : 'cross'" 
            size="14" 
            :color="permission.granted ? '#07c160' : '#ee0a24'"
          />
        </div>
      </div>
      
      <div class="permission-note">
        <van-icon name="info-o" size="14" color="#969799" />
        <span>权限由系统管理员分配，如需申请权限请联系客服</span>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="profile-menu">
      <van-cell-group>
        <van-cell 
          title="个人信息" 
          is-link 
          @click="handleEditProfile"
        >
          <template #icon>
            <van-icon name="user-o" size="16" color="#1989fa" />
          </template>
        </van-cell>
        
        <van-cell 
          title="账户安全" 
          is-link 
          @click="handleAccountSecurity"
        >
          <template #icon>
            <van-icon name="shield-o" size="16" color="#07c160" />
          </template>
        </van-cell>
        
        <van-cell 
          title="消息通知" 
          is-link 
          @click="handleNotificationSettings"
        >
          <template #icon>
            <van-icon name="bell-o" size="16" color="#ff976a" />
          </template>
        </van-cell>
        
        <van-cell 
          title="帮助中心" 
          is-link 
          @click="handleHelpCenter"
        >
          <template #icon>
            <van-icon name="question-o" size="16" color="#969799" />
          </template>
        </van-cell>
        
        <van-cell 
          title="关于我们" 
          is-link 
          @click="handleAbout"
        >
          <template #icon>
            <van-icon name="info-o" size="16" color="#969799" />
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <!-- 退出登录 -->
    <div class="profile-logout">
      <van-button 
        type="danger" 
        size="large" 
        block
        @click="handleLogout"
      >
        退出登录
      </van-button>
    </div> 
   <!-- 个人信息编辑弹窗 -->
    <van-popup 
      v-model:show="showProfileForm" 
      position="bottom" 
      :style="{ height: '70%' }"
      round
    >
      <div class="form-popup">
        <div class="form-popup__header">
          <h3>编辑个人信息</h3>
          <van-icon name="cross" size="18" @click="closeProfileForm" />
        </div>
        
        <div class="form-popup__content">
          <van-form @submit="handleSubmitProfile">
            <van-field
              v-model="profileForm.name"
              name="name"
              label="姓名"
              placeholder="请输入真实姓名"
              :rules="[{ required: true, message: '请输入姓名' }]"
            />
            
            <van-field
              v-model="profileForm.phone"
              name="phone"
              label="手机号"
              placeholder="请输入手机号码"
              type="tel"
              :rules="[
                { required: true, message: '请输入手机号码' },
                { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' }
              ]"
            />
            
            <van-field
              v-model="profileForm.email"
              name="email"
              label="邮箱"
              placeholder="请输入邮箱地址"
              type="email"
            />
            
            <van-field
              v-model="profileForm.idCard"
              name="idCard"
              label="身份证号"
              placeholder="请输入身份证号码"
              :rules="[
                { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入正确的身份证号码' }
              ]"
            />
            
            <van-field name="gender" label="性别">
              <template #input>
                <van-radio-group v-model="profileForm.gender" direction="horizontal">
                  <van-radio name="1">男</van-radio>
                  <van-radio name="2">女</van-radio>
                </van-radio-group>
              </template>
            </van-field>
            
            <van-field
              v-model="profileForm.address"
              name="address"
              label="联系地址"
              placeholder="请输入详细地址"
              type="textarea"
              rows="2"
            />
            
            <div class="form-actions">
              <van-button 
                type="default" 
                size="large" 
                @click="closeProfileForm"
              >
                取消
              </van-button>
              
              <van-button 
                type="primary" 
                size="large" 
                native-type="submit"
                :loading="submitting"
              >
                保存
              </van-button>
            </div>
          </van-form>
        </div>
      </div>
    </van-popup>

    <!-- 账户安全弹窗 -->
    <van-popup 
      v-model:show="showSecurityForm" 
      position="bottom" 
      :style="{ height: '60%' }"
      round
    >
      <div class="form-popup">
        <div class="form-popup__header">
          <h3>账户安全</h3>
          <van-icon name="cross" size="18" @click="showSecurityForm = false" />
        </div>
        
        <div class="form-popup__content">
          <van-cell-group>
            <van-cell 
              title="修改密码" 
              is-link 
              @click="handleChangePassword"
            >
              <template #icon>
                <van-icon name="lock" size="16" color="#1989fa" />
              </template>
            </van-cell>
            
            <van-cell 
              title="绑定手机" 
              :value="userInfo?.phone ? '已绑定' : '未绑定'"
              is-link 
              @click="handleBindPhone"
            >
              <template #icon>
                <van-icon name="phone-o" size="16" color="#07c160" />
              </template>
            </van-cell>
            
            <van-cell 
              title="实名认证" 
              :value="userInfo?.verified ? '已认证' : '未认证'"
              is-link 
              @click="handleVerification"
            >
              <template #icon>
                <van-icon name="certificate" size="16" color="#ff976a" />
              </template>
            </van-cell>
            
            <van-cell 
              title="登录记录" 
              is-link 
              @click="handleLoginHistory"
            >
              <template #icon>
                <van-icon name="records" size="16" color="#969799" />
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </div>
    </van-popup>

    <!-- 头像上传 -->
    <van-uploader
      ref="uploaderRef"
      v-model="fileList"
      :after-read="handleAvatarUpload"
      :max-count="1"
      :preview-image="false"
      accept="image/*"
      style="display: none;"
    />
  </PageContainer>
</template><script 
setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showConfirmDialog, showDialog } from 'vant';
import { useUserStore } from '@/store/modules/user';
import { useLandlordStore } from '@/store/modules/landlord';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';

const router = useRouter();
const userStore = useUserStore();
const landlordStore = useLandlordStore();

// 响应式数据
const loading = ref(false);
const submitting = ref(false);
const showProfileForm = ref(false);
const showSecurityForm = ref(false);
const fileList = ref([]);
const uploaderRef = ref();

// 默认头像
const defaultAvatar = 'https://img.yzcdn.cn/vant/cat.jpeg';

// 个人信息表单
const profileForm = ref({
  name: '',
  phone: '',
  email: '',
  idCard: '',
  gender: '1',
  address: ''
});

// 计算属性
const userInfo = computed(() => userStore.userInfo);
const showEmpty = computed(() => !loading.value && !userInfo.value);

// 权限列表
const permissionList = ref([
  {
    key: 'apartment_manage',
    name: '公寓管理',
    icon: 'building-o',
    granted: true
  },
  {
    key: 'room_manage',
    name: '房源管理',
    icon: 'shop-o',
    granted: true
  },
  {
    key: 'application_manage',
    name: '申请管理',
    icon: 'contact',
    granted: true
  },
  {
    key: 'appointment_manage',
    name: '预约管理',
    icon: 'calendar-o',
    granted: true
  },
  {
    key: 'review_manage',
    name: '审核管理',
    icon: 'eye-o',
    granted: false
  },
  {
    key: 'data_export',
    name: '数据导出',
    icon: 'down',
    granted: false
  }
]);

// 工具函数
const getStatusType = (status?: number) => {
  switch (status) {
    case 1: return 'success'; // 正常
    case 2: return 'warning'; // 待审核
    case 3: return 'danger';  // 已禁用
    default: return 'default';
  }
};

const getStatusText = (status?: number) => {
  switch (status) {
    case 1: return '正常';
    case 2: return '待审核';
    case 3: return '已禁用';
    default: return '未知';
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    
    // 加载用户信息
    if (userInfo.value) {
      profileForm.value = {
        name: userInfo.value.name || '',
        phone: userInfo.value.phone || '',
        email: userInfo.value.email || '',
        idCard: userInfo.value.idCard || '',
        gender: userInfo.value.gender?.toString() || '1',
        address: userInfo.value.address || ''
      };
    }
    
    // 加载权限信息
    await loadPermissions();
    
  } catch (error) {
    console.error('初始化数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 加载权限信息
const loadPermissions = async () => {
  try {
    // TODO: 调用权限查询API
    // const permissions = await getUserPermissions();
    // 更新权限列表状态
  } catch (error) {
    console.error('加载权限信息失败:', error);
  }
};

// 事件处理
const handleAvatarClick = () => {
  uploaderRef.value?.chooseFile();
};

const handleAvatarUpload = async (file: any) => {
  try {
    showToast.loading('上传中...');
    
    // TODO: 实现MinIO头像上传
    // const formData = new FormData();
    // formData.append('file', file.file);
    // const response = await uploadAvatar(formData);
    
    // 模拟上传成功
    setTimeout(() => {
      showToast.success('头像上传成功');
      // 更新用户头像
      // userStore.updateUserInfo({ avatar: response.data.url });
    }, 2000);
    
  } catch (error) {
    console.error('头像上传失败:', error);
    showToast.fail('头像上传失败');
  }
};

const handleEditProfile = () => {
  showProfileForm.value = true;
};

const handleSubmitProfile = async () => {
  try {
    submitting.value = true;
    
    // TODO: 调用更新用户信息API
    // await updateUserProfile(profileForm.value);
    
    // 模拟更新成功
    setTimeout(() => {
      showToast.success('保存成功');
      // 更新本地用户信息
      userStore.updateUserInfo(profileForm.value);
      closeProfileForm();
    }, 1000);
    
  } catch (error) {
    console.error('保存个人信息失败:', error);
    showToast.fail('保存失败');
  } finally {
    submitting.value = false;
  }
};

const closeProfileForm = () => {
  showProfileForm.value = false;
};

const handleAccountSecurity = () => {
  showSecurityForm.value = true;
};

const handleChangePassword = async () => {
  try {
    const result = await showDialog({
      title: '修改密码',
      message: '请输入新密码：',
      showCancelButton: true,
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    });
    
    if (result === 'confirm') {
      // TODO: 实现修改密码功能
      showToast('密码修改功能开发中');
    }
  } catch (error) {
    // 用户取消
  }
};

const handleBindPhone = () => {
  // TODO: 实现手机绑定功能
  showToast('手机绑定功能开发中');
};

const handleVerification = () => {
  // TODO: 实现实名认证功能
  showToast('实名认证功能开发中');
};

const handleLoginHistory = () => {
  // TODO: 实现登录记录功能
  showToast('登录记录功能开发中');
};

const handleNotificationSettings = () => {
  // TODO: 实现消息通知设置
  showToast('消息通知设置功能开发中');
};

const handleHelpCenter = () => {
  // TODO: 跳转到帮助中心
  showToast('帮助中心功能开发中');
};

const handleAbout = () => {
  // TODO: 显示关于我们信息
  showToast('关于我们功能开发中');
};

const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确定要退出登录吗？',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    });
    
    // 清除用户信息和token
    await userStore.logout();
    
    showToast.success('已退出登录');
    
    // 跳转到登录页
    router.replace('/login');
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error);
      showToast.fail('退出登录失败');
    }
  }
};

// 生命周期
onMounted(() => {
  initData();
});
</script>

<style scoped lang="less">
.profile-header {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  
  .profile-avatar {
    position: relative;
    margin-right: 16px;
    cursor: pointer;
    
    .avatar-upload-btn {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 24px;
      height: 24px;
      background: #1989fa;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid white;
    }
  }
  
  .profile-info {
    flex: 1;
    margin-right: 12px;
    
    .profile-name {
      font-size: 18px;
      font-weight: 600;
      color: #323233;
      margin: 0 0 4px 0;
    }
    
    .profile-phone {
      font-size: 14px;
      color: #646566;
      margin: 0 0 8px 0;
    }
    
    .profile-status {
      display: flex;
      align-items: center;
    }
  }
  
  .profile-edit {
    flex-shrink: 0;
  }
}

.permission-status {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 16px;
  }
  
  .permission-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-bottom: 12px;
  }
  
  .permission-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: #f7f8fa;
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &.active {
      background: #f0f9ff;
      border: 1px solid #e6f7ff;
    }
    
    .permission-name {
      flex: 1;
      font-size: 13px;
      color: #646566;
    }
  }
  
  .permission-note {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    font-size: 12px;
    color: #969799;
    line-height: 1.4;
    padding: 8px 12px;
    background: #f7f8fa;
    border-radius: 6px;
  }
}

.profile-menu {
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  
  :deep(.van-cell) {
    padding: 16px;
    
    .van-cell__left-icon {
      margin-right: 12px;
    }
  }
}

.profile-logout {
  padding: 0 16px;
  margin-bottom: 20px;
}

.form-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #ebedf0;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #323233;
      margin: 0;
    }
    
    .van-icon {
      cursor: pointer;
      color: #969799;
      
      &:hover {
        color: #646566;
      }
    }
  }
  
  &__content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
  }
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  
  .van-button {
    flex: 1;
  }
}

// 响应式设计
@media (max-width: 375px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    
    .profile-avatar {
      margin-right: 0;
    }
    
    .profile-info {
      margin-right: 0;
    }
  }
  
  .permission-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .permission-item {
    padding: 10px;
    
    .permission-name {
      font-size: 12px;
    }
  }
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .profile-header,
  .permission-status,
  .profile-menu {
    background: #1e1e1e;
    
    .profile-name,
    .section-title {
      color: #ffffff;
    }
    
    .profile-phone {
      color: #cccccc;
    }
  }
  
  .permission-item {
    background: #2a2a2a;
    
    &.active {
      background: #1a3a5c;
      border-color: #2563eb;
    }
    
    .permission-name {
      color: #cccccc;
    }
  }
  
  .permission-note {
    background: #2a2a2a;
    color: #999999;
  }
}
</style>