<template>
  <div class="pie-chart">
    <div class="chart-header" v-if="title">
      <div class="chart-title">{{ title }}</div>
      <div v-if="subtitle" class="chart-subtitle">{{ subtitle }}</div>
    </div>
    
    <div class="chart-container">
      <div ref="chartRef" class="chart" :style="{ height: height + 'px' }"></div>
      
      <!-- 图例 -->
      <div v-if="showLegend && legendData.length > 0" class="chart-legend">
        <div 
          v-for="(item, index) in legendData" 
          :key="index"
          class="legend-item"
          @click="toggleSeries(index)"
        >
          <div 
            class="legend-color" 
            :style="{ backgroundColor: item.color }"
          ></div>
          <div class="legend-content">
            <div class="legend-name">{{ item.name }}</div>
            <div class="legend-value">
              {{ formatValue(item.value) }}
              <span class="legend-percent">({{ item.percent }}%)</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-if="isEmpty" class="empty-state">
      <van-empty description="暂无数据" image="search" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts/core';
import { PieChart } from 'echarts/charts';
import { 
  TitleComponent, 
  TooltipComponent, 
  LegendComponent 
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// 注册ECharts组件
echarts.use([
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer
]);

// Props
interface ChartDataItem {
  name: string;
  value: number;
  color?: string;
}

interface Props {
  data: ChartDataItem[];
  title?: string;
  subtitle?: string;
  height?: number;
  showLegend?: boolean;
  colors?: string[];
  formatType?: 'default' | 'currency' | 'percentage';
  innerRadius?: string;
  outerRadius?: string;
  center?: [string, string];
}

const props = withDefaults(defineProps<Props>(), {
  height: 200,
  showLegend: true,
  colors: () => ['#1989fa', '#07c160', '#ff9500', '#fa5151', '#9c26b0', '#00bcd4'],
  formatType: 'default',
  innerRadius: '30%',
  outerRadius: '60%',
  center: () => ['50%', '50%']
});

// 响应式数据
const chartRef = ref<HTMLElement>();
let chart: echarts.ECharts | null = null;

// 计算属性
const isEmpty = computed(() => {
  return !props.data || props.data.length === 0 || props.data.every(item => item.value === 0);
});

const legendData = computed(() => {
  if (isEmpty.value) return [];
  
  const total = props.data.reduce((sum, item) => sum + item.value, 0);
  
  return props.data.map((item, index) => ({
    name: item.name,
    value: item.value,
    color: item.color || props.colors[index % props.colors.length],
    percent: total > 0 ? Math.round((item.value / total) * 100) : 0
  }));
});

// 格式化数值
const formatValue = (value: number): string => {
  switch (props.formatType) {
    case 'currency':
      return `¥${value.toLocaleString()}`;
    case 'percentage':
      return `${value}%`;
    default:
      return value.toLocaleString();
  }
};

// 初始化图表
const initChart = async () => {
  if (!chartRef.value || isEmpty.value) return;
  
  await nextTick();
  
  if (chart) {
    chart.dispose();
  }
  
  chart = echarts.init(chartRef.value);
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        const { name, value, percent } = params;
        return `${name}<br/>${formatValue(value)} (${percent}%)`;
      }
    },
    series: [
      {
        type: 'pie',
        radius: [props.innerRadius, props.outerRadius],
        center: props.center,
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
        data: props.data.map((item, index) => ({
          value: item.value,
          name: item.name,
          itemStyle: {
            color: item.color || props.colors[index % props.colors.length]
          }
        }))
      }
    ]
  };
  
  chart.setOption(option);
  
  // 监听窗口大小变化
  const resizeHandler = () => {
    if (chart) {
      chart.resize();
    }
  };
  
  window.addEventListener('resize', resizeHandler);
  
  // 组件卸载时移除监听器
  onUnmounted(() => {
    window.removeEventListener('resize', resizeHandler);
  });
};

// 切换系列显示/隐藏
const toggleSeries = (index: number) => {
  if (!chart) return;
  
  chart.dispatchAction({
    type: 'legendToggleSelect',
    name: props.data[index]?.name
  });
};

// 监听数据变化
watch(() => props.data, () => {
  initChart();
}, { deep: true });

// 组件挂载时初始化图表
onMounted(() => {
  initChart();
});

// 组件卸载时销毁图表
onUnmounted(() => {
  if (chart) {
    chart.dispose();
    chart = null;
  }
});
</script>

<style scoped>
.pie-chart {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 图表头部 */
.chart-header {
  margin-bottom: 16px;
  text-align: center;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 4px;
}

.chart-subtitle {
  font-size: 12px;
  color: #969799;
}

/* 图表容器 */
.chart-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart {
  width: 100%;
}

/* 图例 */
.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.legend-item:hover {
  background-color: #f7f8fa;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  flex-shrink: 0;
}

.legend-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.legend-name {
  font-size: 14px;
  color: #323233;
}

.legend-value {
  font-size: 14px;
  color: #646566;
  font-weight: bold;
}

.legend-percent {
  font-size: 12px;
  color: #969799;
  font-weight: normal;
}

/* 空状态 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

/* 响应式适配 */
@media (max-width: 375px) {
  .pie-chart {
    padding: 12px;
  }
  
  .chart-title {
    font-size: 15px;
  }
  
  .legend-name,
  .legend-value {
    font-size: 13px;
  }
  
  .legend-percent {
    font-size: 11px;
  }
}

/* 水平图例布局 */
.pie-chart.horizontal-legend .chart-legend {
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.pie-chart.horizontal-legend .legend-item {
  flex-direction: column;
  text-align: center;
  min-width: 60px;
}

.pie-chart.horizontal-legend .legend-content {
  flex-direction: column;
  gap: 2px;
}
</style>