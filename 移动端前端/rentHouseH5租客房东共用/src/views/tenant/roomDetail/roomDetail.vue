<template>
  <van-skeleton :row="20" :loading="!roomDetailInfo?.id">
    <div class="page-container">
      <!--  轮播图-->
      <van-swipe class="my-swipe" :autoplay="3000" indicator-color="white">
        <van-swipe-item
          v-for="item in roomDetailInfo.graphVoList"
          :key="item.url"
        >
          <van-image fit="fill" :src="item.url" width="100vw" height="35vh">
            <template v-slot:error>加载失败</template>
            <template v-slot:loading>
              <van-loading type="spinner" size="20" />
            </template>
          </van-image>
        </van-swipe-item>
      </van-swipe>

      <!--  房间的信息-->
      <div class="card">
        <div class="">
          <!--      标题-->
          <div class="font-bold text-lg">
            {{
              roomDetailInfo.apartmentItemVo ? `${roomDetailInfo.apartmentItemVo.name} ${roomDetailInfo.roomNumber}房间` : '房源详情'
            }}
          </div>
          
          <!-- 发布者信息 -->
          <div class="flex items-center gap-2 my-2">
            <PublisherBadge 
              :publisherType="roomDetailInfo.publisherType" 
              size="medium"
            />
            <span class="text-sm text-gray-600">
              {{ roomDetailInfo.publisherType === 1 ? '官方发布' : '房东发布' }}
            </span>
          </div>
          
          <!--      标签-->
          <div class="my-[7px]">
            <van-tag
              class="last:mr-0 mr-[5px]"
              plain
              v-for="item in roomDetailInfo.labelInfoList"
              :key="item.id"
              type="primary"
              >{{ item.name }}
            </van-tag>
          </div>
          
          <!--      价格和评分-->
          <div class="flex items-center justify-between">
            <div>
              <span class="text-red-500 text-[16px]">￥</span>
              <span class="text-red-500 text-[20px] font-bold"
                >{{ roomDetailInfo.rent }}</span>
              <span class="text-red-500 text-[14px]">/月</span>
            </div>
            
            <!-- 评分信息 -->
            <div class="flex flex-col items-end">
              <RatingDisplay 
                :complianceScore="roomDetailInfo.complianceScore"
                :overallRating="roomDetailInfo.reviewDetail?.overallRating"
                size="medium"
              />
              <span class="text-xs text-gray-500 mt-1">
                最后审核：{{ formatDate(roomDetailInfo.lastReviewedAt) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 房源状态信息 -->
      <div class="card">
        <div class="base-info-title py-[4px]">房源状态</div>
        <div class="my-[5px]">
          <van-row gutter="10">
            <van-col span="12" class="my-[3px]">
              <van-row>
                <van-col span="8">
                  <van-tag type="success">发布状态</van-tag>
                </van-col>
                <van-col span="16">
                  <span class="text-green-600">{{ roomDetailInfo.isRelease ? '已发布' : '未发布' }}</span>
                </van-col>
              </van-row>
            </van-col>
            <van-col span="12" class="my-[3px]">
              <van-row>
                <van-col span="8">
                  <van-tag type="primary">审核状态</van-tag>
                </van-col>
                <van-col span="16">
                  <span :class="getReviewStatusClass(roomDetailInfo.reviewStatus)">
                    {{ getReviewStatusText(roomDetailInfo.reviewStatus) }}
                  </span>
                </van-col>
              </van-row>
            </van-col>
            <van-col span="12" class="my-[3px]">
              <van-row>
                <van-col span="8">
                  <van-tag type="warning">合规性评分</van-tag>
                </van-col>
                <van-col span="16">
                  <span class="font-semibold" :class="getScoreClass(roomDetailInfo.complianceScore)">
                    {{ roomDetailInfo.complianceScore }}/10分
                  </span>
                </van-col>
              </van-row>
            </van-col>
            <van-col span="12" class="my-[3px]">
              <van-row>
                <van-col span="8">
                  <van-tag type="default">房源编号</van-tag>
                </van-col>
                <van-col span="16">
                  <span class="text-gray-600">#{{ roomDetailInfo.id }}</span>
                </van-col>
              </van-row>
            </van-col>
          </van-row>
        </div>
      </div>

      <!-- 专业评分详情 -->
      <div class="card" v-if="roomDetailInfo.reviewDetail">
        <div class="base-info-title py-[4px] flex items-center gap-2">
          <span>专业评分</span>
          <van-tag type="primary" size="small">官方审核</van-tag>
        </div>
        <div class="my-[5px]">
          <!-- 总体评分 -->
          <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-4 mb-4">
            <div class="flex items-center justify-between">
              <div>
                <div class="text-lg font-bold text-gray-800">合规性评分</div>
                <div class="text-sm text-gray-600 mt-1">
                  审核员：{{ roomDetailInfo.reviewDetail.reviewerName || '系统审核' }}
                </div>
                <div class="text-xs text-gray-500">
                  {{ formatDate(roomDetailInfo.reviewDetail.reviewedAt) }}
                </div>
              </div>
              <div class="text-right">
                <div class="text-3xl font-bold" :class="getScoreClass(roomDetailInfo.complianceScore)">
                  {{ roomDetailInfo.complianceScore }}
                </div>
                <div class="text-sm text-gray-500">/ 10分</div>
                <div class="flex items-center mt-1">
                  <span class="text-sm font-semibold" :class="getOverallRatingClass(roomDetailInfo.reviewDetail.overallRating)">
                    综合评级：{{ getOverallRatingText(roomDetailInfo.reviewDetail.overallRating) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 详细评分项 -->
          <div class="space-y-3">
            <!-- 图片质量 -->
            <div class="score-item">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <van-icon name="photo" color="#1989fa" size="16" />
                  <span class="text-sm font-medium text-gray-700">图片质量</span>
                </div>
                <span class="text-sm font-bold" :class="getScoreClass(roomDetailInfo.reviewDetail.photoQualityScore)">
                  {{ roomDetailInfo.reviewDetail.photoQualityScore }}/10
                </span>
              </div>
              <van-progress 
                :percentage="roomDetailInfo.reviewDetail.photoQualityScore * 10" 
                :color="getProgressColor(roomDetailInfo.reviewDetail.photoQualityScore)"
                stroke-width="6"
                :show-pivot="false"
              />
            </div>

            <!-- 描述准确性 -->
            <div class="score-item">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <van-icon name="edit" color="#1989fa" size="16" />
                  <span class="text-sm font-medium text-gray-700">描述准确性</span>
                </div>
                <span class="text-sm font-bold" :class="getScoreClass(roomDetailInfo.reviewDetail.descriptionAccuracyScore)">
                  {{ roomDetailInfo.reviewDetail.descriptionAccuracyScore }}/10
                </span>
              </div>
              <van-progress 
                :percentage="roomDetailInfo.reviewDetail.descriptionAccuracyScore * 10" 
                :color="getProgressColor(roomDetailInfo.reviewDetail.descriptionAccuracyScore)"
                stroke-width="6"
                :show-pivot="false"
              />
            </div>

            <!-- 设施完整性 -->
            <div class="score-item">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <van-icon name="home-o" color="#1989fa" size="16" />
                  <span class="text-sm font-medium text-gray-700">设施完整性</span>
                </div>
                <span class="text-sm font-bold" :class="getScoreClass(roomDetailInfo.reviewDetail.facilityCompletenessScore)">
                  {{ roomDetailInfo.reviewDetail.facilityCompletenessScore }}/10
                </span>
              </div>
              <van-progress 
                :percentage="roomDetailInfo.reviewDetail.facilityCompletenessScore * 10" 
                :color="getProgressColor(roomDetailInfo.reviewDetail.facilityCompletenessScore)"
                stroke-width="6"
                :show-pivot="false"
              />
            </div>

            <!-- 位置准确性 -->
            <div class="score-item">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <van-icon name="location" color="#1989fa" size="16" />
                  <span class="text-sm font-medium text-gray-700">位置准确性</span>
                </div>
                <span class="text-sm font-bold" :class="getScoreClass(roomDetailInfo.reviewDetail.locationAccuracyScore)">
                  {{ roomDetailInfo.reviewDetail.locationAccuracyScore }}/10
                </span>
              </div>
              <van-progress 
                :percentage="roomDetailInfo.reviewDetail.locationAccuracyScore * 10" 
                :color="getProgressColor(roomDetailInfo.reviewDetail.locationAccuracyScore)"
                stroke-width="6"
                :show-pivot="false"
              />
            </div>
          </div>

          <!-- 审核评语 -->
          <div v-if="roomDetailInfo.reviewDetail.reviewComments" class="mt-4 p-3 bg-gray-50 rounded-lg">
            <div class="text-sm font-medium text-gray-700 mb-2 flex items-center gap-1">
              <van-icon name="chat-o" size="14" />
              审核评语
            </div>
            <div class="text-sm text-gray-600 leading-relaxed">
              {{ roomDetailInfo.reviewDetail.reviewComments }}
            </div>
          </div>
        </div>
      </div>

      <!--    基本信息-->
      <div class="card">
        <div class="base-info-title py-[4px]">基本信息</div>
        <div class="my-[5px]">
          <van-row gutter="10">
            <van-col
              span="12"
              class="my-[3px]"
              v-for="item in roomDetailInfo.attrValueVoList"
              :key="item.id"
            >
              <van-row>
                <van-col span="6">
                  <van-tag type="primary">{{ item.attrKeyName }}</van-tag>
                </van-col>
                <van-col span="18">
                  {{ item.name }}
                </van-col>
              </van-row>
            </van-col>
            <!--          占位修饰-->
            <van-col
              class="my-[3px]"
              v-if="roomDetailInfo.attrValueVoList?.length % 2 !== 0"
              span="12"
            >
            </van-col>
          </van-row>
        </div>
      </div>
      <!--    配套信息-->
      <div class="card">
        <div class="base-info-title py-[4px]">配套信息</div>
        <div class="my-[5px]">
          <van-row>
            <van-col
              span="4"
              class="my-[3px]"
              v-for="item in roomDetailInfo.facilityInfoList"
              :key="item.id"
            >
              <div class="flex flex-col justify-center items-center">
                <SvgIcon :name="item.icon" size="25" />
                <span class="text-center">
                  {{ item.name }}
                </span>
              </div>
            </van-col>
          </van-row>
        </div>
      </div>
      <!--    位置详情-->
      <div class="card">
        <div class="base-info-title py-[4px]">位置详情</div>
        <div class="my-[5px]">
          <div class="text-xs mb-[5px] w-[300px]">
            {{ roomDetailInfo.apartmentItemVo?.addressDetail || '地址加载中...' }}
          </div>
        </div>
        <!--        地图容器-->
        <div v-if="!mapLoadError" id="container" class="w-[85vw] h-[30vh]"></div>
        <!--        地图加载失败时的备用显示-->
        <div v-else class="map-fallback">
          <div class="fallback-content">
            <van-icon name="location-o" size="24" color="#999" />
            <div class="fallback-text">地图暂时无法显示</div>
            <div class="fallback-address">{{ roomDetailInfo.apartmentItemVo?.addressDetail || '地址加载中...' }}</div>
          </div>
        </div>
      </div>
      <!--    费用明细-->
      <div class="card" v-if="roomDetailInfo.apartmentItemVo?.feeValueVoList?.length">
        <div class="base-info-title py-[4px]">费用明细</div>
        <div class="my-[5px]">
          <van-row gutter="10">
            <van-col span="12">
              <van-tag type="primary" size="medium">费用科目</van-tag>
            </van-col>
            <van-col span="12">
              <van-tag type="primary" size="medium">收费标准</van-tag>
            </van-col>
            <template
              v-for="item in roomDetailInfo.apartmentItemVo.feeValueVoList"
              :key="item.id"
            >
              <van-col span="12" class="my-[5px]">
                <span>{{ item.feeKeyName }}</span>
              </van-col>
              <van-col span="12" class="my-[5px]">
                <span>{{ `￥${item.name}${item.unit}` }}</span>
              </van-col>
            </template>
          </van-row>
        </div>
      </div>
      <!--    付款说明-->
      <div class="card">
        <div class="base-info-title py-[4px]">可选付款方式</div>
        <div class="my-[5px]">
          <van-row gutter="10">
            <van-col span="12">
              <van-tag type="primary" size="medium">可选付款方式</van-tag>
            </van-col>
            <van-col span="12">
              <van-tag type="primary" size="medium">说明</van-tag>
            </van-col>
            <template
              v-for="item in roomDetailInfo.paymentTypeList"
              :key="item.id"
            >
              <van-col span="12" class="my-[5px]">
                <span>{{ item.name }}</span>
              </van-col>
              <van-col span="12" class="my-[5px]">
                <span>{{ item.additionalInfo }}</span>
              </van-col>
            </template>
          </van-row>
        </div>
      </div>
      <div class="card">
        <div class="base-info-title py-[4px]">可选租期</div>
        <div class="my-[5px]">
          <van-row gutter="10">
            <van-col span="12">
              <van-tag type="primary" size="medium">可选租期</van-tag>
            </van-col>
            <van-col span="12">
              <van-tag type="primary" size="medium">说明</van-tag>
            </van-col>
            <template
              v-for="item in roomDetailInfo.leaseTermList"
              :key="item.id"
            >
              <van-col span="12" class="my-[5px]">
                <span>{{ item.monthCount + item.unit }}</span>
              </van-col>
              <van-col span="12" class="my-[5px]">
                <span> 到期可续 </span>
              </van-col>
            </template>
          </van-row>
        </div>
      </div>
      <!--    所属公寓-->
      <div class="card">
        <div class="base-info-title py-[4px]">所属公寓</div>
        <div class="my-[5px] pb-[50px] px-[10px]">
          <ApartmentCard :data="roomDetailInfo.apartmentItemVo"></ApartmentCard>
        </div>
      </div>
      <!--    预约看房和直接入住-->
      <van-sticky :offset-bottom="0" position="bottom">
        <div class="action-buttons-container">
          <van-row gutter="8">
            <van-col span="12">
              <van-button 
                type="default" 
                block 
                size="large"
                @click="appointmentToViewHandle"
                class="appointment-button"
              >预约看房</van-button>
            </van-col>
            <van-col span="12">
              <van-button 
                type="primary" 
                block 
                size="large"
                @click="directMoveInHandle"
                class="direct-move-in-button"
              >直接入住</van-button>
            </van-col>
          </van-row>
        </div>
      </van-sticky>
    </div>
  </van-skeleton>
</template>
<script setup lang="ts" name="TenantRoomDetail">
import { getRoomDetailById } from "@/api/tenant/search";
import { onMounted, ref, watch, onBeforeUnmount } from "vue";
import type { RoomDetailInterface } from "@/api/tenant/search/types";
import { useMap } from "@/hooks/useMap";
import poiMarkerRed from "@/assets/poi-marker-red.png";
import ApartmentCard from "@/components/common/ApartmentCard/ApartmentCard.vue";
import PublisherBadge from "@/components/common/PublisherBadge/PublisherBadge.vue";
import RatingDisplay from "@/components/common/RatingDisplay/RatingDisplay.vue";
import { useRouter, useRoute } from "vue-router";
import { showToast } from "vant";
import { checkDirectMoveInAvailability, checkAppointmentAvailability } from "@/utils/roomAvailabilityHelper";
const router = useRouter();
const route = useRoute();
// 房间的详情信息
const roomDetailInfo = ref<RoomDetailInterface>({} as RoomDetailInterface);

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '暂无';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

// 获取审核状态文本
const getReviewStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    1: '待审核',
    2: '审核通过',
    3: '审核拒绝'
  };
  return statusMap[status] || '未知状态';
};

// 获取审核状态样式类
const getReviewStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    1: 'text-orange-500',  // 待审核
    2: 'text-green-600',   // 审核通过
    3: 'text-red-500'      // 审核拒绝
  };
  return classMap[status] || 'text-gray-500';
};

// 获取评分样式类
const getScoreClass = (score: number) => {
  if (score >= 9) return 'text-green-600';
  if (score >= 7) return 'text-orange-500';
  if (score >= 5) return 'text-yellow-600';
  return 'text-red-500';
};

// 获取进度条颜色
const getProgressColor = (score: number) => {
  if (score >= 9) return '#10b981'; // 绿色
  if (score >= 7) return '#f59e0b'; // 橙色
  if (score >= 5) return '#eab308'; // 黄色
  return '#ef4444'; // 红色
};

// 获取综合评级文字
const getOverallRatingText = (rating: number) => {
  const ratingMap: Record<number, string> = {
    1: '优秀',
    2: '良好', 
    3: '一般',
    4: '较差'
  };
  return ratingMap[rating] || '暂无评级';
};

// 获取综合评级颜色类
const getOverallRatingClass = (rating: number) => {
  const classMap: Record<number, string> = {
    1: 'text-green-600',
    2: 'text-blue-600',
    3: 'text-orange-500',
    4: 'text-red-500'
  };
  return classMap[rating] || 'text-gray-500';
};
// 获取房间的详情信息
const getRoomDetailHandle = async () => {
  const roomId = route.params.id as string;
  if (!roomId) {
    showToast('房源ID不能为空');
    return;
  }
  
  try {
    const { data } = await getRoomDetailById(roomId);
    
    // 检查数据是否有效
    if (!data || !data.id) {
      console.error('获取的房源数据无效:', data);
      return;
    }
    
    roomDetailInfo.value = data;
    
    // 数据加载完成后初始化地图
    if (data.apartmentItemVo?.longitude && data.apartmentItemVo?.latitude) {
      const lng = parseFloat(data.apartmentItemVo.longitude as string);
      const lat = parseFloat(data.apartmentItemVo.latitude as string);
      
      // 验证坐标是否有效
      if (!isNaN(lng) && !isNaN(lat) && lng !== 0 && lat !== 0) {
        console.log('房源坐标数据:', { lng, lat, addressDetail: data.apartmentItemVo.addressDetail });
        await initMapPage({
          lng,
          lat
        });
      } else {
        console.warn('坐标数据无效或为零:', { lng, lat });
        mapLoadError.value = true;
      }
    } else {
      console.warn('缺少坐标数据:', { 
        longitude: data.apartmentItemVo?.longitude, 
        latitude: data.apartmentItemVo?.latitude 
      });
      mapLoadError.value = true;
    }
  } catch (error) {
    console.error('获取房源详情失败:', error);
    showToast('获取房源详情失败');
  }
};
//#region <高德地图相关>
// 地图实例
const { AMap, initMap } = useMap();
// 地图加载错误状态
const mapLoadError = ref(false);

async function initMapPage(opts?: { lng: number; lat: number }) {
  console.log("开始初始化地图，坐标:", opts);
  
  // 检查坐标是否有效
  if (!opts || typeof opts.lng !== 'number' || typeof opts.lat !== 'number') {
    console.error("地图坐标无效或类型错误:", opts);
    mapLoadError.value = true;
    return;
  }
  
  // 检查坐标范围（中国坐标范围）
  if (opts.lng < 73 || opts.lng > 135 || opts.lat < 18 || opts.lat > 53) {
    console.error("坐标超出中国范围:", opts);
    mapLoadError.value = true;
    return;
  }
  
  try {
    // 检查网络连接
    if (!navigator.onLine) {
      console.error("网络连接不可用");
      mapLoadError.value = true;
      return;
    }
    
    // 先初始化地图API
    console.log("正在加载高德地图API...");
    await initMap();
    
    // 等待一小段时间确保DOM元素已渲染
    await new Promise(resolve => setTimeout(resolve, 200));
    
    // 检查DOM元素是否存在
    const container = document.getElementById("container");
    if (!container) {
      console.error("地图容器元素不存在");
      mapLoadError.value = true;
      return;
    }
    
    // 检查AMap是否已正确加载
    if (!AMap.value || !AMap.value.Map) {
      console.error("高德地图API未正确加载");
      mapLoadError.value = true;
      return;
    }
    
    console.log("开始创建地图实例，中心坐标:", [opts.lng, opts.lat]);
    const map = new AMap.value.Map("container", {
      zoom: 16, // 提高缩放级别以更清楚地显示位置
      center: [opts.lng, opts.lat], // 初始地图中心点
      showIndoorMap: false, // 关闭室内地图
      mapStyle: 'amap://styles/normal', // 使用标准地图样式
      resizeEnable: true, // 允许地图自适应容器大小
      rotateEnable: false, // 禁用旋转
      pitchEnable: false, // 禁用倾斜
      zoomEnable: true, // 允许缩放
      dragEnable: true // 允许拖拽
    });
    
    console.log("地图实例创建成功，添加标记");
    
    // 创建自定义图标
    const icon = new AMap.value.Icon({
      size: new AMap.value.Size(25, 34), // 图标尺寸
      image: poiMarkerRed, // Icon的图像
      imageSize: new AMap.value.Size(25, 34)
    });
    
    // 创建标记点 - 使用相同的坐标
    const marker = new AMap.value.Marker({
      icon: icon,
      position: [opts.lng, opts.lat],
      anchor: "bottom-center",
      title: "房源位置"
    });
    
    map.add(marker);
    console.log("标记点已添加，位置:", [opts.lng, opts.lat]);
    
    // 等待地图加载完成
    map.on('complete', () => {
      console.log("地图加载完成");
      try {
        // 调整地图视野以显示标记
        map.setFitView([marker], false, [50, 50, 50, 50]);
      } catch (error) {
        console.warn("设置地图视野失败:", error);
      }
    });
    
    // 地图加载错误处理
    map.on('error', (error: any) => {
      console.error("地图运行时错误:", error);
      mapLoadError.value = true;
    });
    
    console.log("地图初始化成功");
    mapLoadError.value = false; // 重置错误状态
    
  } catch (error) {
    console.error("地图初始化失败:", error);
    mapLoadError.value = true;
    
    // 根据错误类型给出具体提示
    if (error instanceof Error) {
      if (error.message.includes('网络') || error.message.includes('NETWORK')) {
        console.error("网络连接问题，请检查网络设置");
      } else if (error.message.includes('key') || error.message.includes('KEY')) {
        console.error("地图API密钥问题");
      } else {
        console.error("地图服务暂时不可用:", error.message);
      }
    }
  }
}
// 预约看房
const appointmentToViewHandle = async () => {
  console.log("appointmentToViewHandle");
  
  const isAvailable = await checkAppointmentAvailability(roomDetailInfo.value.id);
  
  if (isAvailable) {
    router.push({
      path: "/tenant/appointment",
      query: { 
        roomId: roomDetailInfo.value.id,
        apartmentId: roomDetailInfo.value.apartmentId 
      }
    });
  }
};

// 直接入住
const directMoveInHandle = async () => {
  console.log("directMoveInHandle");
  
  const isAvailable = await checkDirectMoveInAvailability(roomDetailInfo.value.id);
  
  if (isAvailable) {
    router.push({
      path: "/tenant/directMoveIn",
      query: { 
        roomId: roomDetailInfo.value.id,
        apartmentId: roomDetailInfo.value.apartmentId 
      }
    });
  }
};
//#endregion

// 用于追踪当前是否在房源详情页面
let isRoomDetailPageActive = false;

// 监听路由参数变化，重新加载数据
// 只在同一页面内的ID变化时触发（例如从房源26切换到房源31）
watch(
  () => route.params.id,
  async (newId, oldId) => {
    // 只有在房源详情页面活跃时才加载数据
    // 这样可以避免跳转到其他页面时触发加载
    if (isRoomDetailPageActive && newId && newId !== oldId) {
      console.log('房源ID变化，重新加载数据:', { oldId, newId });
      // 重置数据
      roomDetailInfo.value = {} as RoomDetailInterface;
      mapLoadError.value = false;
      // 重新获取数据
      try {
        await getRoomDetailHandle();
      } catch (error) {
        console.error('监听器中获取房源详情失败:', error);
      }
    }
  },
  { immediate: false } // 不立即执行，避免与onMounted重复
);

onMounted(async () => {
  isRoomDetailPageActive = true;
  await getRoomDetailHandle();
});

// 组件卸载时标记为不活跃
onBeforeUnmount(() => {
  isRoomDetailPageActive = false;
});
</script>

<style scoped lang="less">
.base-info-title {
  //background-color: var(--van-primary-background-color);
  font-weight: bold;
  //color: white;
}

.map-fallback {
  width: 85vw;
  height: 30vh;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fallback-content {
  text-align: center;
  padding: 20px;
}

.fallback-text {
  margin: 8px 0;
  color: #999;
  font-size: 14px;
}

.fallback-address {
  color: #666;
  font-size: 12px;
  line-height: 1.4;
  max-width: 200px;
}

.action-buttons-container {
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #ebedf0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.appointment-button {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background-color: #f7f8fa !important;
  color: #646566 !important;
  border: 1px solid #ebedf0 !important;
  
  &:active {
    transform: translateY(1px);
    background-color: #e8e9eb !important;
  }
  
  &:hover {
    background-color: #e8e9eb !important;
  }
}

.direct-move-in-button {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background-color: #1989fa !important;
  color: #fff !important;
  border: none !important;
  box-shadow: 0 2px 4px rgba(25, 137, 250, 0.3);
  
  &:active {
    transform: translateY(1px);
    background-color: #0570d9 !important;
  }
  
  &:hover {
    background-color: #0570d9 !important;
  }
}

// 友好提示对话框样式
:deep(.availability-dialog) {
  .van-dialog__header {
    padding: 20px 20px 10px;
    font-size: 18px;
    font-weight: 600;
  }
  
  .van-dialog__content {
    padding: 10px 20px 20px;
    font-size: 14px;
    line-height: 1.6;
    color: #646566;
  }
  
  .van-dialog__confirm {
    border-radius: 6px;
    font-weight: 500;
  }
}
</style>
