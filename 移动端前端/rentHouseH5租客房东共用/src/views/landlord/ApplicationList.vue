<template>
  <div class="application-page">
    <!-- 导航栏 -->
    <van-nav-bar 
      title="直接入住申请管理" 
      left-arrow 
      @click-left="handleGoBack"
      fixed
      placeholder
    />

    <!-- 页面头部统计 -->
    <div class="application-header" v-if="!loading">
      <!-- 主要统计卡片 -->
      <div class="stats-main">
        <div class="stat-item">
          <div class="stat-number">{{ statistics?.totalApplications || 0 }}</div>
          <div class="stat-label">总申请数</div>
          <van-icon name="contact" class="stat-icon" />
        </div>
        
        <div class="stat-item clickable" @click="handleFilterPending">
          <div class="stat-number">{{ statistics?.pendingApplications || 0 }}</div>
          <div class="stat-label">待处理</div>
          <van-icon name="clock-o" class="stat-icon" />
        </div>
        
        <div class="stat-item">
          <div class="stat-number">{{ formatPercentage(statistics?.approvalRate || 0) }}</div>
          <div class="stat-label">通过率</div>
          <van-icon name="chart-trending-o" class="stat-icon" />
        </div>
      </div>
      
      <!-- 次要统计信息 -->
      <div class="stats-secondary">
        <div class="secondary-item">
          <span class="secondary-label">今日申请</span>
          <span class="secondary-value">{{ statistics?.todayApplications || 0 }}条</span>
        </div>
        <div class="secondary-item">
          <span class="secondary-label">本月申请</span>
          <span class="secondary-value">{{ statistics?.monthApplications || 0 }}条</span>
        </div>
        <div class="secondary-item">
          <van-button 
            type="primary" 
            size="mini" 
            icon="clock-o"
            @click="handleViewHistory"
          >
            查看历史
          </van-button>
        </div>
      </div>
    </div>

    <!-- 筛选标签 - 始终显示 -->
    <div class="application-filters">
      <van-tabs v-model:active="activeTab" @change="onTabChange">
        <van-tab title="全部" name="all" />
        <van-tab title="待处理" name="pending" />
        <van-tab title="已通过" name="approved" />
        <van-tab title="已拒绝" name="rejected" />
      </van-tabs>
    </div>

    <!-- 申请列表 -->
    <div class="application-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <van-loading vertical>加载中...</van-loading>
      </div>
      
      <!-- 列表内容 -->
      <van-pull-refresh v-else v-model="refreshing" @refresh="handleRefresh">
        <van-list
          v-model:loading="listLoading"
          :finished="finished"
          finished-text="没有更多了"
          @load="handleLoadMore"
        >
          <ApplicationCard
            v-for="application in applicationList"
            :key="application.id"
            :application="application"
            :show-actions="true"
            @click="handleViewApplication"
            @approve="handleApproveApplication"
            @reject="handleRejectApplication"
          />
          
          <!-- 空状态 -->
          <EmptyState
            v-if="showEmpty"
            :type="emptyStateType"
            @action="handleEmptyAction"
          />
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 批量操作 -->
    <div class="application-batch" v-if="selectedApplications.length > 0">
      <div class="batch-info">
        <span>已选择 {{ selectedApplications.length }} 条申请</span>
        <van-button type="default" size="mini" @click="handleClearSelection">
          取消选择
        </van-button>
      </div>
      
      <div class="batch-actions">
        <van-button 
          type="success" 
          size="small"
          @click="handleBatchApprove"
          :loading="batchProcessing"
        >
          批量通过
        </van-button>
        
        <van-button 
          type="danger" 
          size="small"
          @click="handleBatchReject"
          :loading="batchProcessing"
        >
          批量拒绝
        </van-button>
      </div>
    </div>

    <!-- 快速筛选浮动按钮 -->
    <van-floating-bubble
      v-if="statistics && statistics.pendingApplications > 0"
      axis="xy"
      icon="clock-o"
      @click="handleFilterPending"
    >
      <template #default>
        <div class="floating-badge">{{ statistics.pendingApplications }}</div>
      </template>
    </van-floating-bubble>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showConfirmDialog, showDialog } from 'vant';
import { NavigationHelper } from '@/utils/navigationHelper';
import { useLandlordStore } from '@/store/modules/landlord';
import { useUserStore } from '@/store/modules/user';
import { 
  getPendingApplications, 
  getApplicationHistory,
  getApplicationStatistics,
  processApplication,
  approveApplication,
  rejectApplication
} from '@/api/landlord';
import type { DirectMoveInApplicationDetailVO, ApplicationStatistics } from '@/api/landlord';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import StatCard from '@/components/landlord/StatCard/StatCard.vue';
import ApplicationCard from '@/components/landlord/ApplicationCard/ApplicationCard.vue';
import EmptyState from '@/components/common/EmptyState/EmptyState.vue';

const router = useRouter();
const landlordStore = useLandlordStore();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const refreshing = ref(false);
const listLoading = ref(false);
const batchProcessing = ref(false);
const finished = ref(false);
const activeTab = ref('all');
const applicationList = ref<DirectMoveInApplicationDetailVO[]>([]);
const statistics = ref<ApplicationStatistics | null>(null);
const selectedApplications = ref<number[]>([]);

// 计算属性
const showEmpty = computed(() => 
  !loading.value && !listLoading.value && applicationList.value.length === 0
);

const emptyType = computed(() => {
  return 'no-applications';
});

const emptyStateType = computed(() => {
  return 'no-applications';
});

// 格式化函数
const formatPercentage = (value: number | string) => {
  const num = typeof value === 'string' ? parseFloat(value) : value;
  return num.toFixed(1);
};

// 加载申请列表
const loadApplicationList = async (isRefresh = false) => {
  try {
    if (isRefresh) {
      finished.value = false;
    }
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    let data: DirectMoveInApplicationDetailVO[] = [];
    
    if (activeTab.value === 'pending') {
      const response = await getPendingApplications(landlordId);
      data = response.data;
    } else {
      const response = await getApplicationHistory(landlordId);
      data = response.data;
      
      // 根据标签页筛选
      if (activeTab.value === 'approved') {
        data = data.filter(app => app.status === 2); // 假设2为已通过
      } else if (activeTab.value === 'rejected') {
        data = data.filter(app => app.status === 3); // 假设3为已拒绝
      }
    }
    
    applicationList.value = data;
    finished.value = true; // 由于API不支持分页，直接设为完成
    
  } catch (error) {
    console.error('加载申请列表失败:', error);
    showToast('加载申请列表失败');
  }
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) return;
    
    // 尝试调用后端统计接口
    try {
      const { data } = await getApplicationStatistics(landlordId);
      statistics.value = data;
      return;
    } catch (error) {
      console.log('后端统计接口暂未实现，使用前端计算统计数据');
    }
    
    // 后端接口不可用时，从申请列表数据计算统计
    await calculateStatisticsFromData(landlordId);
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

// 从申请数据计算统计信息
const calculateStatisticsFromData = async (landlordId: number) => {
  try {
    // 获取所有申请数据
    const [pendingResponse, historyResponse] = await Promise.all([
      getPendingApplications(landlordId).catch(() => ({ data: [] })),
      getApplicationHistory(landlordId).catch(() => ({ data: [] }))
    ]);
    
    const pendingApps = pendingResponse.data || [];
    const historyApps = historyResponse.data || [];
    const allApps = [...pendingApps, ...historyApps];
    
    // 计算统计数据
    const totalApplications = allApps.length;
    const pendingApplications = pendingApps.length;
    const approvedApplications = historyApps.filter(app => app.status === 2).length;
    const rejectedApplications = historyApps.filter(app => app.status === 3).length;
    
    // 计算今日申请数
    const today = new Date().toDateString();
    const todayApplications = allApps.filter(app => {
      const appDate = new Date(app.createTime || app.applicationDate || '').toDateString();
      return appDate === today;
    }).length;
    
    // 计算本月申请数
    const thisMonth = new Date().getMonth();
    const thisYear = new Date().getFullYear();
    const thisMonthApplications = allApps.filter(app => {
      const appDate = new Date(app.createTime || app.applicationDate || '');
      return appDate.getMonth() === thisMonth && appDate.getFullYear() === thisYear;
    }).length;
    
    // 计算通过率
    const processedApps = approvedApplications + rejectedApplications;
    const approvalRate = processedApps > 0 ? (approvedApplications / processedApps) * 100 : 0;
    
    // 设置统计数据
    statistics.value = {
      totalApplications,
      pendingApplications,
      approvedApplications,
      rejectedApplications,
      todayApplications,
      monthApplications: thisMonthApplications, // 修改字段名以匹配后端
      approvalRate: Math.round(approvalRate * 10) / 10 // 保留一位小数
    };
    
    console.log('📊 计算得到的统计数据:', statistics.value);
  } catch (error) {
    console.error('计算统计数据失败:', error);
    // 设置默认值
    statistics.value = {
      totalApplications: 0,
      pendingApplications: 0,
      approvedApplications: 0,
      rejectedApplications: 0,
      todayApplications: 0,
      monthApplications: 0, // 修改字段名以匹配后端
      approvalRate: 0
    };
  }
};

// 初始化数据
const initData = async () => {
  try {
    loading.value = true;
    await Promise.all([
      loadApplicationList(true),
      loadStatistics()
    ]);
  } catch (error) {
    console.error('初始化数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 事件处理
const handleRefresh = async () => {
  try {
    refreshing.value = true;
    await initData();
    showToast('刷新成功');
  } catch (error) {
    showToast('刷新失败');
  } finally {
    refreshing.value = false;
  }
};

const handleLoadMore = async () => {
  if (finished.value) return;
  
  try {
    listLoading.value = true;
    await loadApplicationList();
  } finally {
    listLoading.value = false;
  }
};

const onTabChange = (name: string | number) => {
  console.log('🔄 切换标签页:', name)
  
  try {
    // 清除选择并重新加载数据
    selectedApplications.value = []
    handleRefresh()
  } catch (error) {
    console.error('标签切换失败:', error)
    showToast('切换失败，请重试')
  }
}

const handleFilterPending = () => {
  activeTab.value = 'pending';
};

const handleViewApplication = (application: DirectMoveInApplicationDetailVO) => {
  router.push(`/landlord/applications/${application.id}`);
};

const handleApproveApplication = async (application: DirectMoveInApplicationDetailVO) => {
  try {
    await showConfirmDialog({
      title: '确认通过申请',
      message: `确定要通过租客"${application.userName}"的申请吗？通过后将生成租约。`,
      confirmButtonText: '通过',
      cancelButtonText: '取消'
    });
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await approveApplication(application.id, landlordId);
    showToast('申请已通过');
    
    // 更新列表中的状态
    const index = applicationList.value.findIndex(item => item.id === application.id);
    if (index !== -1) {
      applicationList.value[index].status = 2; // 设为已通过状态
      applicationList.value[index].processedAt = new Date().toISOString();
    }
    
    // 刷新统计数据
    await loadStatistics();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('通过申请失败:', error);
      showToast('操作失败');
    }
  }
};

const handleRejectApplication = async (application: DirectMoveInApplicationDetailVO) => {
  try {
    // 先显示确认对话框
    await showConfirmDialog({
      title: '拒绝申请',
      message: `确定要拒绝租客"${application.name}"的申请吗？`,
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    });
    
    // 确认后，使用简单的 prompt 获取拒绝原因
    const rejectionReason = prompt('请输入拒绝原因：');
    
    // 验证输入
    if (!rejectionReason || !rejectionReason.trim()) {
      showToast('拒绝原因不能为空');
      return;
    }
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    await rejectApplication({
      id: application.id,
      landlordId,
      rejectionReason: rejectionReason.trim()
    });
    
    showToast('申请已拒绝');
    
    // 更新列表中的状态
    const index = applicationList.value.findIndex(item => item.id === application.id);
    if (index !== -1) {
      applicationList.value[index].status = 3; // 设为已拒绝状态
      applicationList.value[index].processedAt = new Date().toISOString();
      applicationList.value[index].rejectionReason = rejectionReason.trim();
    }
    
    // 刷新统计数据
    await loadStatistics();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('拒绝申请失败:', error);
      showToast('操作失败，请重试');
    }
  }
};

const handleClearSelection = () => {
  selectedApplications.value = [];
};

const handleBatchApprove = async () => {
  try {
    await showConfirmDialog({
      title: '批量通过申请',
      message: `确定要通过选中的 ${selectedApplications.value.length} 条申请吗？`,
      confirmButtonText: '通过',
      cancelButtonText: '取消'
    });
    
    batchProcessing.value = true;
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    // 批量处理申请
    const promises = selectedApplications.value.map(id => 
      approveApplication(id, landlordId)
    );
    
    await Promise.all(promises);
    showToast('批量操作成功');
    
    // 清除选择并刷新数据
    selectedApplications.value = [];
    await initData();
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量通过失败:', error);
      showToast('批量操作失败');
    }
  } finally {
    batchProcessing.value = false;
  }
};

const handleBatchReject = async () => {
  try {
    // 创建一个自定义的输入对话框用于批量拒绝
    let rejectionReason = '';
    
    const result = await new Promise((resolve) => {
      showDialog({
        title: '批量拒绝申请',
        message: `确定要拒绝选中的 ${selectedApplications.value.length} 条申请吗？请输入拒绝原因：`,
        showCancelButton: true,
        confirmButtonText: '确定拒绝',
        cancelButtonText: '取消',
        beforeClose: (action) => {
          if (action === 'confirm') {
            if (!rejectionReason.trim()) {
              showToast('请输入拒绝原因');
              return false;
            }
          }
          resolve(action);
          return true;
        }
      }).then(() => {
        // 创建输入框
        const input = document.createElement('input');
        input.type = 'text';
        input.placeholder = '请输入拒绝原因';
        input.style.cssText = `
          width: 100%;
          padding: 8px 12px;
          border: 1px solid #ddd;
          border-radius: 4px;
          margin-top: 12px;
          font-size: 14px;
        `;
        
        input.addEventListener('input', (e) => {
          rejectionReason = (e.target as HTMLInputElement).value;
        });
        
        const messageEl = document.querySelector('.van-dialog__message');
        if (messageEl) {
          messageEl.appendChild(input);
          input.focus();
        }
      });
    });
    
    // 如果用户取消了输入
    if (result !== 'confirm' || !rejectionReason.trim()) {
      return;
    }
    
    batchProcessing.value = true;
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    // 批量处理申请
    const promises = selectedApplications.value.map(id => 
      rejectApplication({
        id,
        landlordId,
        rejectionReason: rejectionReason.trim()
      })
    );
    
    await Promise.all(promises);
    showToast('批量操作成功');
    
    // 清除选择并刷新数据
    selectedApplications.value = [];
    await initData();
    
  } catch (error: any) {
    console.error('批量拒绝失败:', error);
    showToast('批量操作失败，请重试');
  } finally {
    batchProcessing.value = false;
  }
};

const handleEmptyAction = () => {
  // 空状态时的操作，可以跳转到房源管理
  router.push('/landlord/rooms');
};

const handleViewHistory = () => {
  // 跳转到申请历史页面
  router.push('/landlord/applications/history');
};

// 返回按钮处理
const handleGoBack = () => {
  console.log('🧭 申请管理页面返回按钮被点击');
  NavigationHelper.smartGoBack(router);
};

// 生命周期
onMounted(() => {
  initData();
});
</script>

<style scoped lang="less">
/* 固定头部区域，防止跳动 */
.application-list-header-fixed {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #f7f8fa;
  padding-bottom: 8px;
  /* 强制固定高度，防止任何布局变化 */
  min-height: 120px;
  max-height: 120px;
  overflow: hidden;
  /* 防止重排和重绘 */
  contain: layout style;
  will-change: auto;
}

.application-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.application-header {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
  
  /* 主要统计卡片 */
  .stats-main {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .stat-item {
    background: #f8f9fa;
    border-radius: 12px;
    padding: 16px 12px;
    text-align: center;
    position: relative;
    transition: all 0.2s ease;
    
    &.clickable {
      cursor: pointer;
      
      &:hover {
        background: #e8f4ff;
        transform: translateY(-2px);
      }
      
      &:active {
        transform: scale(0.98);
      }
    }
    
    .stat-number {
      font-size: 24px;
      font-weight: bold;
      color: #323233;
      line-height: 1.2;
      margin-bottom: 4px;
    }
    
    .stat-label {
      font-size: 12px;
      color: #646566;
      line-height: 1.2;
    }
    
    .stat-icon {
      position: absolute;
      top: 8px;
      right: 8px;
      font-size: 16px;
      color: #969799;
      opacity: 0.6;
    }
  }
  
  /* 次要统计信息 */
  .stats-secondary {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 8px;
  }
  
  .secondary-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    flex: 1;
    
    &:not(:last-child) {
      border-right: 1px solid #ebedf0;
    }
    
    .secondary-label {
      font-size: 12px;
      color: #969799;
    }
    
    .secondary-value {
      font-size: 14px;
      font-weight: 600;
      color: #323233;
    }
  }
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  
  .summary-label {
    font-size: 12px;
    color: #969799;
  }
  
  .summary-value {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
  }
}

.application-filters {
  background: white;
  margin-bottom: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  /* 强制固定高度，绝对防止跳动 */
  height: 44px;
  min-height: 44px;
  max-height: 44px;
  display: flex;
  align-items: center;
  /* 强制布局稳定性 */
  position: relative;
  z-index: 1;
  overflow: hidden;
  /* 防止内容变化影响布局 */
  contain: layout style;
  /* 优化渲染性能 */
  transform: translateZ(0);
  
  :deep(.van-tabs__nav) {
    background: transparent;
    padding: 0 16px;
    min-height: 44px; /* 确保导航栏有固定高度 */
    width: 100%; /* 确保导航栏占满宽度 */
  }
  
  :deep(.van-tab) {
    color: #646566;
    font-size: 14px;
    flex: 1; /* 让每个标签平均分配宽度 */
    text-align: center;
    padding: 0 8px; /* 增加内边距 */
    white-space: nowrap; /* 防止文字换行 */
    overflow: visible; /* 确保文字可见 */
  }
  
  :deep(.van-tab--active) {
    color: #1989fa;
    font-weight: 500;
  }
  
  :deep(.van-tabs__content) {
    display: none; /* 隐藏内容区域，只显示标签 */
  }
}

.application-content {
  padding: 0 16px;
  /* 强制最小高度，防止布局塌陷 */
  min-height: 400px;
  max-height: none;
  padding-bottom: 20px;
  /* 防止内容变化影响整体布局 */
  contain: layout;
  /* 确保内容区域稳定 */
  position: relative;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0; /* 增加加载状态的高度 */
  color: #999;
}

.application-batch {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 16px;
  border-top: 1px solid #ebedf0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  
  .batch-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
    color: #646566;
  }
  
  .batch-actions {
    display: flex;
    gap: 12px;
    
    .van-button {
      flex: 1;
    }
  }
}

.floating-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  background: #ee0a24;
  color: white;
  font-size: 10px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

// 响应式设计
@media (max-width: 375px) {
  .application-header {
    padding: 12px;
    
    .stats-main {
      gap: 8px;
      margin-bottom: 12px;
    }
    
    .stat-item {
      padding: 12px 8px;
      
      .stat-number {
        font-size: 20px;
      }
      
      .stat-label {
        font-size: 11px;
      }
      
      .stat-icon {
        font-size: 14px;
        top: 6px;
        right: 6px;
      }
    }
    
    .stats-secondary {
      padding: 8px;
    }
    
    .secondary-item {
      .secondary-label {
        font-size: 11px;
      }
      
      .secondary-value {
        font-size: 13px;
      }
    }
  }
  
  .summary-item {
    .summary-value {
      font-size: 14px;
    }
  }
}

// 超小屏幕适配
@media (max-width: 320px) {
  .application-header {
    .stats-main {
      gap: 6px;
    }
    
    .stat-item {
      padding: 10px 6px;
      
      .stat-number {
        font-size: 18px;
      }
      
      .stat-label {
        font-size: 10px;
      }
    }
    
    .stats-secondary {
      padding: 6px;
      
      .secondary-item {
        .secondary-label {
          font-size: 10px;
        }
        
        .secondary-value {
          font-size: 12px;
        }
      }
    }
  }
}
</style>