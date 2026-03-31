<template>
  <div class="dashboard-container">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <div class="user-info">
        <van-image
          class="avatar"
          round
          width="50"
          height="50"
          src="https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg"
        />
        <div class="user-details">
          <div class="greeting">{{ greeting }}</div>
          <div class="user-name">{{ landlordName }}</div>
        </div>
      </div>
      <div class="header-actions">
        <div class="notification-icon" @click="showNotifications">
          <van-icon name="bell" size="24" />
          <van-badge content="3" />
        </div>
        <div class="logout-icon" @click="handleLogout" title="退出登录">
          <van-icon name="cross" size="24" />
        </div>
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-number">12</div>
          <div class="stat-label">公寓总数</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">48</div>
          <div class="stat-label">房间总数</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">36</div>
          <div class="stat-label">已发布</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">15</div>
          <div class="stat-label">可租房间</div>
        </div>
      </div>
    </div>

    <!-- 待处理事项 -->
    <div class="pending-section">
      <div class="section-title">待处理事项</div>
      <div class="pending-grid">
        <div class="pending-item" @click="goToApplications">
          <div class="pending-icon">
            <van-icon name="orders-o" size="20" />
          </div>
          <div class="pending-info">
            <div class="pending-count">5</div>
            <div class="pending-label">待处理直接入住申请</div>
          </div>
        </div>
        <div class="pending-item" @click="goToAppointments">
          <div class="pending-icon">
            <van-icon name="calendar-o" size="20" />
          </div>
          <div class="pending-info">
            <div class="pending-count">3</div>
            <div class="pending-label">待确认预约</div>
          </div>
        </div>
        <div class="pending-item" @click="goToReviews">
          <div class="pending-icon">
            <van-icon name="eye-o" size="20" />
          </div>
          <div class="pending-info">
            <div class="pending-count">2</div>
            <div class="pending-label">审核中房源</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions-section">
      <div class="section-title">快速操作</div>
      <div class="actions-grid">
        <div class="action-item" @click="goToApartmentManage">
          <div class="action-icon">
            <van-icon name="home-o" size="24" />
          </div>
          <div class="action-label">公寓管理</div>
        </div>
        <div class="action-item" @click="goToRoomManage">
          <div class="action-icon">
            <van-icon name="shop-o" size="24" />
          </div>
          <div class="action-label">房源管理</div>
        </div>
        <div class="action-item" @click="goToApplicationManage">
          <div class="action-icon">
            <van-icon name="orders-o" size="24" />
          </div>
          <div class="action-label">直接入住申请管理</div>
        </div>
        <div class="action-item" @click="goToAppointmentManage">
          <div class="action-icon">
            <van-icon name="calendar-o" size="24" />
          </div>
          <div class="action-label">预约管理</div>
        </div>
        <div class="action-item" @click="goToLeaseManage">
          <div class="action-icon">
            <van-icon name="description" size="24" />
          </div>
          <div class="action-label">租约管理</div>
        </div>
        <div class="action-item" @click="goToReviewManage">
          <div class="action-icon">
            <van-icon name="eye-o" size="24" />
          </div>
          <div class="action-label">审核管理</div>
        </div>
      </div>
    </div>

    <!-- 房源数据管理 -->
    <div class="data-manage-section">
      <div class="section-title">房源数据管理</div>
      <div class="data-manage-grid">
        <div class="data-manage-item" @click="goToAttrManage">
          <div class="data-manage-icon">
            <van-icon name="label-o" size="20" />
          </div>
          <div class="data-manage-label">房间属性</div>
        </div>
        <div class="data-manage-item" @click="goToFacilityManage">
          <div class="data-manage-icon">
            <van-icon name="shop-o" size="20" />
          </div>
          <div class="data-manage-label">配套设施</div>
        </div>
        <div class="data-manage-item" @click="goToFeeManage">
          <div class="data-manage-icon">
            <van-icon name="gold-coin-o" size="20" />
          </div>
          <div class="data-manage-label">杂费管理</div>
        </div>
        <div class="data-manage-item" @click="goToLabelManage">
          <div class="data-manage-icon">
            <van-icon name="bookmark-o" size="20" />
          </div>
          <div class="data-manage-label">标签管理</div>
        </div>
        <div class="data-manage-item" @click="goToPaymentTypeManage">
          <div class="data-manage-icon">
            <van-icon name="credit-pay" size="20" />
          </div>
          <div class="data-manage-label">支付方式</div>
        </div>
        <div class="data-manage-item" @click="goToLeaseTermManage">
          <div class="data-manage-icon">
            <van-icon name="calendar-o" size="20" />
          </div>
          <div class="data-manage-label">租期管理</div>
        </div>
      </div>
    </div>

    <!-- 经营数据 -->
    <div class="business-section">
      <div class="section-title">经营数据</div>
      <div class="business-cards">
        <div class="business-card">
          <div class="business-label">出租率</div>
          <div class="business-value">75%</div>
          <div class="business-trend up">↑ 5%</div>
        </div>
        <div class="business-card">
          <div class="business-label">本月收入</div>
          <div class="business-value">¥28,500</div>
          <div class="business-trend up">↑ 12%</div>
        </div>
        <div class="business-card">
          <div class="business-label">本月成交</div>
          <div class="business-value">8套</div>
          <div class="business-trend down">↓ 2套</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { TokenChecker } from '@/utils/tokenChecker';
import { useUserStore } from '@/store/modules/user';

const router = useRouter();
const userStore = useUserStore();

// 计算属性：动态获取房东昵称
const landlordName = computed(() => {
  const nickname = userStore.userInfo?.nickname;
  if (nickname) {
    return nickname;
  }
  // 如果没有昵称，使用手机号后4位作为fallback
  const phone = userStore.userInfo?.phone;
  if (phone && phone.length >= 4) {
    return `房东${phone.slice(-4)}`;
  }
  // 最后的fallback
  return '房东先生';
});

// 计算属性：动态获取问候语
const greeting = computed(() => {
  const hour = new Date().getHours();
  let timeGreeting = '';
  
  if (hour < 6) {
    timeGreeting = '夜深了';
  } else if (hour < 12) {
    timeGreeting = '早上好';
  } else if (hour < 14) {
    timeGreeting = '中午好';
  } else if (hour < 18) {
    timeGreeting = '下午好';
  } else {
    timeGreeting = '晚上好';
  }
  
  return `${timeGreeting}，${landlordName.value}！`;
});

// 初始化页面数据
const initPageData = async () => {
  try {
    // 确保用户信息已加载
    if (!userStore.userInfo) {
      await userStore.getUserInfo();
    }
  } catch (error) {
    console.error('初始化页面数据失败:', error);
  }
};

// 页面加载时检查token和初始化数据
onMounted(() => {
  console.log('🔍 Dashboard组件已挂载，开始检查token');
  
  // 初始化页面数据
  initPageData();
  
  // 延迟检查，确保页面完全加载
  setTimeout(() => {
    TokenChecker.forceCheckTokenStatus();
  }, 2000);
});

// 退出登录
const handleLogout = async () => {
  console.log('🔍 退出登录按钮被点击');
  
  try {
    // 直接使用简单的确认对话框
    const confirmed = confirm('确定要退出登录吗？');
    
    if (confirmed) {
      console.log('🔍 用户确认退出登录');
      
      // 清理所有数据
      localStorage.clear();
      console.log('🔍 已清理localStorage');
      
      // 显示提示
      alert('已退出登录');
      
      // 强制跳转到登录页
      window.location.href = '/login';
      
    } else {
      console.log('🔍 用户取消退出登录');
    }
    
  } catch (error) {
    console.error('🔍 退出登录失败:', error);
    // 即使出错也强制跳转
    localStorage.clear();
    window.location.href = '/login';
  }
};

// 显示通知
const showNotifications = () => {
  console.log('🔍 通知按钮被点击');
  showToast('通知功能开发中');
};

// 导航方法
const goToApplications = () => {
  console.log('点击申请管理');
  router.push('/landlord/applications');
};

const goToAppointments = () => {
  console.log('点击预约管理');
  router.push('/landlord/appointments');
};

const goToReviews = () => {
  console.log('点击审核状态');
  router.push('/landlord/reviews');
};

const goToApartmentManage = () => {
  console.log('点击公寓管理');
  router.push('/landlord/apartments');
};

const goToRoomManage = () => {
  console.log('点击房源管理');
  router.push('/landlord/rooms');
};

const goToApplicationManage = () => {
  console.log('点击申请管理（快速操作）');
  router.push('/landlord/applications');
};

const goToAppointmentManage = () => {
  console.log('点击预约管理（快速操作）');
  router.push('/landlord/appointments');
};

const goToReviewManage = () => {
  console.log('点击审核管理');
  router.push('/landlord/reviews');
};

const goToLeaseManage = () => {
  console.log('点击租约管理');
  router.push('/landlord/leases');
};

// 数据管理导航方法
const goToDataManage = () => {
  console.log('点击数据管理');
  showToast('数据管理功能');
};

const goToAttrManage = () => {
  console.log('点击房间属性管理');
  router.push('/landlord/attr-manage');
};

const goToFacilityManage = () => {
  console.log('点击配套设施管理');
  router.push('/landlord/facility-manage');
};

const goToFeeManage = () => {
  console.log('点击杂费管理');
  router.push('/landlord/fee-manage');
};

const goToLabelManage = () => {
  console.log('点击标签管理');
  router.push('/landlord/label-manage');
};

const goToPaymentTypeManage = () => {
  console.log('点击支付方式管理');
  router.push('/landlord/payment-type-manage');
};

const goToLeaseTermManage = () => {
  console.log('点击租期管理');
  router.push('/landlord/lease-term-manage');
};
</script>

<style scoped lang="less">
.dashboard-container {
  min-height: 100vh;
  background: #f8f9fa;
  padding-bottom: 80px; // 为底部导航留空间
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .user-details {
      .greeting {
        font-size: 14px;
        opacity: 0.9;
        margin-bottom: 4px;
      }
      
      .user-name {
        font-size: 18px;
        font-weight: 600;
      }
    }
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .notification-icon,
    .logout-icon {
      position: relative;
      cursor: pointer;
      padding: 8px;
      border-radius: 50%;
      transition: background-color 0.2s ease;
      
      &:active {
        background-color: rgba(255, 255, 255, 0.2);
      }
      
      .van-badge {
        position: absolute;
        top: -8px;
        right: -8px;
      }
    }
    
    .logout-icon {
      opacity: 0.9;
      
      &:hover {
        opacity: 1;
      }
    }
  }
}

.stats-section {
  padding: 20px;
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    
    .stat-card {
      background: white;
      padding: 16px 8px;
      border-radius: 8px;
      text-align: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      
      .stat-number {
        font-size: 20px;
        font-weight: bold;
        color: #1989fa;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 12px;
        color: #666;
      }
    }
  }
}

.pending-section, .quick-actions-section, .data-manage-section, .business-section {
  padding: 0 20px 20px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }
}

.pending-grid {
  display: flex;
  gap: 12px;
  
  .pending-item {
    flex: 1;
    background: white;
    padding: 16px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    
    .pending-icon {
      width: 40px;
      height: 40px;
      background: #f0f8ff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #1989fa;
    }
    
    .pending-info {
      .pending-count {
        font-size: 18px;
        font-weight: bold;
        color: #333;
        margin-bottom: 2px;
      }
      
      .pending-label {
        font-size: 12px;
        color: #666;
      }
    }
  }
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  
  .action-item {
    background: white;
    padding: 20px 12px;
    border-radius: 8px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: transform 0.2s ease;
    
    &:active {
      transform: scale(0.95);
    }
    
    .action-icon {
      width: 48px;
      height: 48px;
      background: #f0f8ff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 8px;
      color: #1989fa;
    }
    
    .action-label {
      font-size: 12px;
      color: #333;
      font-weight: 500;
    }
  }
  
  // 第5个按钮单独占一行
  .action-item:nth-child(5) {
    grid-column: 1 / 3; // 占据前两列的位置，居中显示
    max-width: 200px;
    margin: 0 auto;
  }
}

.data-manage-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  
  .data-manage-item {
    background: white;
    padding: 16px 12px;
    border-radius: 8px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: transform 0.2s ease;
    
    &:active {
      transform: scale(0.95);
    }
    
    .data-manage-icon {
      width: 40px;
      height: 40px;
      background: #fff7e6;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 8px;
      color: #ff8c00;
    }
    
    .data-manage-label {
      font-size: 12px;
      color: #333;
      font-weight: 500;
    }
  }
}

.business-cards {
  display: flex;
  gap: 12px;
  
  .business-card {
    flex: 1;
    background: white;
    padding: 16px;
    border-radius: 8px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    .business-label {
      font-size: 12px;
      color: #666;
      margin-bottom: 8px;
    }
    
    .business-value {
      font-size: 18px;
      font-weight: bold;
      color: #333;
      margin-bottom: 4px;
    }
    
    .business-trend {
      font-size: 12px;
      
      &.up {
        color: #07c160;
      }
      
      &.down {
        color: #ee0a24;
      }
    }
  }
}
</style>