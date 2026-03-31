<template>
  <div class="lease-management">
    <!-- 导航栏 -->
    <van-nav-bar 
      title="租约管理" 
      left-arrow 
      @click-left="handleGoBack"
      fixed
      placeholder
    />

    <!-- 页面内容 -->
    <div class="content">
      <!-- 统计卡片区域 -->
      <div class="stats-section">
        <!-- 第一行：总租约、已签约、签约待确认、续约待确认 -->
        <div class="stats-grid">
          <div class="stat-card" @click="goToLeaseList()">
            <div class="stat-number">{{ statistics.total }}</div>
            <div class="stat-label">总租约</div>
          </div>
          <div class="stat-card" @click="goToLeaseList({ status: LeaseStatus.SIGNED })">
            <div class="stat-number">{{ statistics.signed }}</div>
            <div class="stat-label">已签约</div>
          </div>
          <div class="stat-card" @click="goToLeaseList({ status: LeaseStatus.SIGNING })">
            <div class="stat-number">{{ statistics.signing }}</div>
            <div class="stat-label">签约待确认</div>
          </div>
          <div class="stat-card" @click="goToLeaseList({ status: LeaseStatus.RENEWING })">
            <div class="stat-number">{{ statistics.renewing }}</div>
            <div class="stat-label">续约待确认</div>
          </div>
        </div>

        <!-- 第二行：退租待确认、已退租、已取消、已到期 -->
        <div class="stats-grid">
          <div class="stat-card" @click="goToLeaseList({ status: LeaseStatus.WITHDRAWING })">
            <div class="stat-number">{{ statistics.withdrawing }}</div>
            <div class="stat-label">退租待确认</div>
          </div>
          <div class="stat-card" @click="goToLeaseList({ status: LeaseStatus.WITHDRAWN })">
            <div class="stat-number">{{ statistics.withdrawn }}</div>
            <div class="stat-label">已退租</div>
          </div>
          <div class="stat-card" @click="goToLeaseList({ status: LeaseStatus.CANCELED })">
            <div class="stat-number">{{ statistics.canceled }}</div>
            <div class="stat-label">已取消</div>
          </div>
          <div class="stat-card" @click="goToLeaseList({ status: LeaseStatus.EXPIRED })">
            <div class="stat-number">{{ statistics.expiring }}</div>
            <div class="stat-label">即将到期</div>
          </div>
        </div>

        <!-- 收入统计 -->
        <div class="income-stats">
          <div class="income-item">
            <div class="income-label">总租金收入</div>
            <div class="income-value">¥{{ formatMoney(statistics.totalRent) }}</div>
          </div>
          <div class="income-item">
            <div class="income-label">总押金收入</div>
            <div class="income-value">¥{{ formatMoney(statistics.totalDeposit) }}</div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-section">
        <!-- 状态分布图 -->
        <div class="chart-card">
          <div class="chart-title">租约状态分布</div>
          <div class="chart-container">
            <div ref="statusChartRef" class="chart"></div>
          </div>
        </div>

        <!-- 来源分布图 -->
        <div class="chart-card">
          <div class="chart-title">租约来源分布</div>
          <div class="chart-container">
            <div ref="sourceChartRef" class="chart"></div>
          </div>
        </div>
      </div>

      <!-- 快速操作区域 -->
      <div class="actions-section">
        <div class="action-title">快速操作</div>
        <div class="action-grid">
          <div class="action-item" @click="goToLeaseList()">
            <van-icon name="orders-o" size="24" />
            <div class="action-label">所有租约</div>
          </div>
          <div class="action-item" @click="goToCreateLease()">
            <van-icon name="add-o" size="24" />
            <div class="action-label">创建租约</div>
          </div>
          <div class="action-item" @click="goToExpiringLeases()">
            <van-icon name="clock-o" size="24" />
            <div class="action-label">即将到期</div>
          </div>
          <div class="action-item" @click="goToLeaseList({ status: LeaseStatus.RENEWING })">
            <van-icon name="replay" size="24" />
            <div class="action-label">续约待确认</div>
          </div>
          <div class="action-item" @click="goToLeaseList({ status: LeaseStatus.WITHDRAWING })">
            <van-icon name="warning-o" size="24" />
            <div class="action-label">退租待确认</div>
          </div>
        </div>
      </div>

      <!-- 最近租约 -->
      <div class="recent-section">
        <div class="section-header">
          <div class="section-title">最近租约</div>
          <div class="section-more" @click="goToLeaseList()">
            查看更多 <van-icon name="arrow" />
          </div>
        </div>
        <div class="recent-leases">
          <div 
            v-for="lease in recentLeases" 
            :key="lease.id"
            class="lease-item"
            @click="goToLeaseDetail(lease.id)"
          >
            <div class="lease-info">
              <div class="lease-title">
                {{ lease.apartmentInfo.name }} - {{ lease.roomInfo.roomNumber }}
              </div>
              <div class="lease-tenant">租客：{{ lease.name }}</div>
              <div class="lease-meta">
                <span class="lease-rent">¥{{ lease.rent }}/月</span>
                <span class="lease-date">{{ formatDate(lease.leaseStartDate) }} - {{ formatDate(lease.leaseEndDate) }}</span>
              </div>
            </div>
            <div class="lease-status">
              <van-tag 
                :color="getLeaseStatusColor(lease.status)"
                plain
                size="medium"
              >
                {{ getLeaseStatusName(lease.status) }}
              </van-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <van-loading v-if="loading" class="loading-overlay" vertical>
      加载中...
    </van-loading>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { NavigationHelper } from '@/utils/navigationHelper';
import * as echarts from 'echarts/core';
import { PieChart } from 'echarts/charts';
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import {
  getLandlordLeaseStatistics,
  getLandlordLeaseStatusStats,
  getLandlordLeaseSourceStats,
  getLandlordLeaseList,
  getLeaseStatusName,
  getLeaseStatusColor,
  LeaseStatus,
  type LandlordLeaseStatisticsVo,
  type StatusStatVo,
  type SourceStatVo,
  type LandlordLeaseVo,
  type LandlordLeaseQueryVo
} from '@/api/landlord/lease';

// 注册ECharts组件
echarts.use([
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer
]);

const router = useRouter();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const statistics = ref<LandlordLeaseStatisticsVo>({
  total: 0,
  signing: 0,
  signed: 0,
  canceled: 0,
  expired: 0,
  withdrawing: 0,
  withdrawn: 0,
  renewing: 0,
  expiring: 0,
  totalRent: 0,
  totalDeposit: 0,
  newLease: 0,
  renewLease: 0,
  directApplication: 0,
  autoGenerated: 0,
  manualCreated: 0
});

const statusStats = ref<StatusStatVo[]>([]);
const sourceStats = ref<SourceStatVo[]>([]);
const recentLeases = ref<LandlordLeaseVo[]>([]);

// 图表引用
const statusChartRef = ref<HTMLElement>();
const sourceChartRef = ref<HTMLElement>();
let statusChart: echarts.ECharts | null = null;
let sourceChart: echarts.ECharts | null = null;

// 加载数据
const loadData = async () => {
  try {
    loading.value = true;
    
    // 获取房东ID
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      console.error('房东ID不存在');
      return;
    }
    
    // 加载统计数据（包含状态和来源统计）
    const [
      statisticsRes,
      recentLeasesRes
    ] = await Promise.all([
      getLandlordLeaseStatistics(landlordId),
      getLandlordLeaseList(landlordId, 1, 5) // 获取最近5条租约
    ]);

    statistics.value = statisticsRes.data;
    statusStats.value = statisticsRes.data.statusStats || [];
    sourceStats.value = statisticsRes.data.sourceStats || [];
    recentLeases.value = recentLeasesRes.data.records;

    console.log('租约统计数据:', statistics.value);
    console.log('状态统计数据:', statusStats.value);
    console.log('来源统计数据:', sourceStats.value);

    // 等待DOM更新后初始化图表
    await nextTick();
    initCharts();
  } catch (error) {
    console.error('加载数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 初始化图表
const initCharts = () => {
  initStatusChart();
  initSourceChart();
};

// 初始化状态分布图
const initStatusChart = () => {
  if (!statusChartRef.value) return;

  statusChart = echarts.init(statusChartRef.value);
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '租约状态',
        type: 'pie',
        radius: ['30%', '60%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 12,
            fontWeight: 'bold'
          }
        },
        data: statusStats.value.map(item => ({
          value: item.count,
          name: item.statusName,
          itemStyle: {
            color: getLeaseStatusColor(item.status)
          }
        }))
      }
    ]
  };

  statusChart.setOption(option);
};

// 初始化来源分布图
const initSourceChart = () => {
  if (!sourceChartRef.value) return;

  sourceChart = echarts.init(sourceChartRef.value);
  
  // 定义来源类型颜色映射 - 使用区分度高的颜色
  const getSourceColor = (sourceType: number | null): string => {
    const colorMap = {
      1: '#1989fa',  // 蓝色 - 新签
      2: '#07c160',  // 绿色 - 续约  
      3: '#ff6b35'   // 橙红色 - 直接入住申请
    };
    return colorMap[sourceType as keyof typeof colorMap] || '#c8c9cc';
  };
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '租约来源',
        type: 'pie',
        radius: ['30%', '60%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 12,
            fontWeight: 'bold'
          }
        },
        data: sourceStats.value.map(item => ({
          value: item.count,
          name: item.sourceTypeName,
          itemStyle: {
            color: getSourceColor(item.sourceType)
          }
        }))
      }
    ]
  };

  sourceChart.setOption(option);
};

// 格式化金额
const formatMoney = (amount: number): string => {
  return amount.toLocaleString();
};

// 格式化日期
const formatDate = (dateStr: string): string => {
  return new Date(dateStr).toLocaleDateString();
};

// 导航方法
const goToLeaseList = (query?: LandlordLeaseQueryVo) => {
  router.push({
    name: 'LandlordLeaseList',
    query: query as any
  });
};

const goToLeaseDetail = (leaseId: number) => {
  router.push({
    name: 'LandlordLeaseDetail',
    params: { id: leaseId }
  });
};

const goToCreateLease = () => {
  router.push({ name: 'LandlordLeaseForm' });
};

const goToExpiringLeases = () => {
  router.push({ name: 'LandlordExpiringLeases' });
};

// 返回按钮处理
const handleGoBack = () => {
  console.log('🧭 租约管理页面返回按钮被点击');
  NavigationHelper.smartGoBack(router);
};

// 组件挂载时加载数据
onMounted(() => {
  loadData();
});

// 组件卸载时销毁图表
onUnmounted(() => {
  if (statusChart) {
    statusChart.dispose();
    statusChart = null;
  }
  if (sourceChart) {
    sourceChart.dispose();
    sourceChart = null;
  }
});
</script>

<style scoped>
.lease-management {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.content {
  padding: 16px;
}

/* 统计卡片区域 */
.stats-section {
  margin-bottom: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 16px 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:active {
  transform: scale(0.95);
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #1989fa;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #646566;
}

.income-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.income-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.income-label {
  font-size: 14px;
  color: #646566;
  margin-bottom: 8px;
}

.income-value {
  font-size: 20px;
  font-weight: bold;
  color: #07c160;
}

/* 图表区域 */
.charts-section {
  margin-bottom: 16px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 16px;
}

.chart-container {
  height: 200px;
}

.chart {
  width: 100%;
  height: 100%;
}

/* 快速操作区域 */
.actions-section {
  margin-bottom: 16px;
}

.action-title {
  font-size: 16px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 12px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.action-item {
  background: white;
  border-radius: 8px;
  padding: 16px 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s;
}

.action-item:active {
  transform: scale(0.95);
}

.action-item .van-icon {
  color: #1989fa;
  margin-bottom: 8px;
}

.action-label {
  font-size: 12px;
  color: #646566;
}

/* 最近租约区域 */
.recent-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #323233;
}

.section-more {
  font-size: 14px;
  color: #1989fa;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.recent-leases {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lease-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.lease-item:active {
  background-color: #ebedf0;
}

.lease-info {
  flex: 1;
}

.lease-title {
  font-size: 14px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 4px;
}

.lease-tenant {
  font-size: 12px;
  color: #646566;
  margin-bottom: 4px;
}

.lease-meta {
  font-size: 12px;
  color: #969799;
  display: flex;
  gap: 12px;
}

.lease-rent {
  color: #07c160;
  font-weight: bold;
}

.lease-status {
  margin-left: 12px;
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000;
}
</style>