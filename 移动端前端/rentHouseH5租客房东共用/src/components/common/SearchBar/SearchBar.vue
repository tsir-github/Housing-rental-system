<template>
  <div>
    <!-- 筛选按钮栏 -->
    <div v-show="!isMenuOpen" class="filter-bar">
      <div class="filter-buttons">
        <button @click="openFilter('area')">地区</button>
        <button @click="openFilter('price')">价格</button>
        <button @click="openFilter('payment')">付款方式</button>
        <button @click="openFilter('publisher')">发布者</button>
        <button @click="openFilter('sort')">排序</button>
      </div>
    </div>
    
    <!-- 筛选内容区域 -->
    <div v-show="isMenuOpen" class="filter-overlay">
      <!-- 自定义筛选按钮栏 -->
      <div class="custom-filter-bar">
        <div class="filter-buttons">
          <button 
            :class="{ active: currentFilterType === 'area' }"
            @click="switchFilter('area')"
          >
            地区
          </button>
          <button 
            :class="{ active: currentFilterType === 'price' }"
            @click="switchFilter('price')"
          >
            价格
          </button>
          <button 
            :class="{ active: currentFilterType === 'payment' }"
            @click="switchFilter('payment')"
          >
            付款方式
          </button>
          <button 
            :class="{ active: currentFilterType === 'publisher' }"
            @click="switchFilter('publisher')"
          >
            发布者
          </button>
          <button 
            :class="{ active: currentFilterType === 'sort' }"
            @click="switchFilter('sort')"
          >
            排序
          </button>
        </div>
      </div>
      
      <!-- 筛选内容 -->
      <div class="filter-content">
        <!-- 地区筛选 -->
        <div v-if="currentFilterType === 'area'" class="comment-padding">
          <div class="flex flex-row h-[50vh] mt-[5px]">
            <!--        省-->
            <div class="basis-1/3" style="overflow: scroll">
              <div
                class="mt-[5px] area-item"
                :class="{ checked: item.isCheck }"
                @click="clickAreaItemHandle(item, 0)"
                v-for="item in areaInfo.provinceList"
                :key="item.id"
              >
                {{ item.name }}
              </div>
            </div>
            <!--        市-->
            <div class="basis-1/3" style="overflow: scroll">
              <div
                class="mt-[5px] area-item"
                @click="clickAreaItemHandle(item, 1)"
                v-for="item in areaInfo.cityList"
                :class="{ checked: item.isCheck }"
                :key="item.id"
              >
                {{ item.name }}
              </div>
            </div>
            <!--        区-->
            <div class="basis-1/3" style="overflow: scroll">
              <div
                class="mt-[5px] area-item"
                @click="clickAreaItemHandle(item, 2)"
                v-for="item in areaInfo.districtList"
                :class="{ checked: item.isCheck }"
                :key="item.id"
              >
                {{ item.name }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- 价格筛选 -->
        <div v-if="currentFilterType === 'price'" class="comment-padding">
          <van-row gutter="10" justify="space-between" class="mt-[5px]">
            <van-col
              class="text-center border p-[4px] mt-[5px] transition-all"
              :class="{ 'other-checked': item.isCheck }"
              @click="rentClickHandle(item)"
              v-for="item in rentInfoList"
              :key="item.label"
              span="7"
              >{{ item.label }}
            </van-col>
          </van-row>
        </div>
        
        <!-- 付款方式筛选 -->
        <div v-if="currentFilterType === 'payment'" class="comment-padding">
          <van-row justify="space-between" class="mt-[5px]">
            <van-col
              class="text-center border p-[4px] mt-[5px] transition-all"
              :class="{ 'other-checked': item.isCheck }"
              @click="paymentClickHandle(item)"
              v-for="item in paymentInfoList"
              :key="item.label"
              span="7"
              >{{ item.label }}
            </van-col>
          </van-row>
        </div>
        
        <!-- 发布者筛选 -->
        <div v-if="currentFilterType === 'publisher'" class="comment-padding">
          <van-row justify="space-between" class="mt-[5px]">
            <van-col
              class="text-center border p-[4px] mt-[5px] transition-all"
              :class="{ 'other-checked': item.isCheck }"
              @click="publisherClickHandle(item)"
              v-for="item in publisherInfoList"
              :key="item.label"
              span="7"
              >{{ item.label }}
            </van-col>
          </van-row>
        </div>
        
        <!-- 排序筛选 -->
        <div v-if="currentFilterType === 'sort'" class="comment-padding">
          <van-row gutter="10" justify="space-around" class="mt-[5px]">
            <van-col
              class="text-center border p-[4px] mt-[5px] transition-all"
              :class="{ 'other-checked': item.isCheck }"
              @click="orderTypeClickHandle(item)"
              v-for="item in orderTypeInfoList"
              :key="item.label"
              span="7"
              >{{ item.label }}
            </van-col>
          </van-row>
        </div>
      </div>
      
      <!-- 底部按钮 -->
      <div class="filter-footer">
        <div class="flex justify-between px-4 py-3">
          <van-button
            type="default"
            size="small"
            class="basis-2/5"
            round
            @click="onCancelHandle"
          >
            取消
          </van-button>
          <van-button
            type="primary"
            size="small"
            class="basis-2/5"
            round
            @click="onConfirmHandle"
          >
            查找
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, ref } from "vue";
import type { DropdownMenuInstance } from "vant";
import type {
  LabelValueInterface,
  RegionInterface,
  RoomListQueryInterface,
  PublisherType
} from "@/api/tenant/search/types";
import {
  getCityList,
  getDistrictList,
  getPaymentTypeList,
  getProvinceList
} from "@/api/tenant/search";
import { SearchOrderType, SearchOrderTypeMap } from "@/enums/constEnums";
const props = defineProps({
  confirmCallback: {
    type: Function,
    default: () => ({})
  }
});
// 组件实例
const menuRef = ref<DropdownMenuInstance>();
// 菜单打开状态
const isMenuOpen = ref(false);
// 当前筛选类型
const currentFilterType = ref<'area' | 'price' | 'payment' | 'publisher' | 'sort'>('area');

const formData = ref<Omit<RoomListQueryInterface, "current" | "size">>({
  // 省市区
  provinceId: "",
  cityId: "",
  districtId: "",
  minRent: "",
  //   最大租金
  maxRent: "",
  //   支付方式
  paymentTypeId: "",
  //   	价格排序方式,可用值:desc,asc
  orderType: "",
  // 发布者类型
  publisherType: ""
});

// 打开筛选
const openFilter = (type: 'area' | 'price' | 'payment' | 'publisher' | 'sort') => {
  isMenuOpen.value = true;
  currentFilterType.value = type;
};

// 菜单打开事件
const onMenuOpen = () => {
  isMenuOpen.value = true;
  currentFilterType.value = 'area'; // 默认显示地区筛选
};

// 菜单关闭事件
const onMenuClose = () => {
  isMenuOpen.value = false;
};

// 切换筛选类型
const switchFilter = (type: 'area' | 'price' | 'payment' | 'publisher' | 'sort') => {
  currentFilterType.value = type;
};

//#region <省市区查询相关>
const areaInfo = ref({
  // 省份数据
  provinceList: [] as RegionInterface[],
  // 市区数据
  cityList: [] as RegionInterface[],
  // 缓存旧数据
  oldCityListInfo: {
    provinceId: "",
    cityList: [] as RegionInterface[]
  },
  // 区县数据
  districtList: [] as RegionInterface[],
  //   缓存旧数据
  oldDistrictList: {
    cityId: "",
    districtList: [] as RegionInterface[]
  }
});
// 点击省市区
const clickAreaItemHandle = (item: RegionInterface, type: number) => {
  // type 0省 1市 2区
  if (type === 0) {
    if (item.isCheck) {
      item.isCheck = !item.isCheck;
      formData.value.provinceId = "";
      formData.value.cityId = "";
      formData.value.districtId = "";
      areaInfo.value.cityList.forEach(item2 => {
        item2.isCheck = false;
      });
      areaInfo.value.districtList.forEach(item2 => {
        item2.isCheck = false;
      });
      return;
    }
    areaInfo.value.provinceList.forEach(item2 => {
      item2.isCheck = false;
    });
    item.isCheck = !item.isCheck;
    formData.value.provinceId = item.id;
    provinceChangeCallback();
    //   设置市区列表
  } else if (type === 1) {
    if (item.isCheck) {
      item.isCheck = !item.isCheck;
      formData.value.cityId = "";
      formData.value.districtId = "";
      areaInfo.value.districtList.forEach(item2 => {
        item2.isCheck = false;
      });
      return;
    }
    areaInfo.value.cityList.forEach(item2 => {
      item2.isCheck = false;
    });
    item.isCheck = !item.isCheck;
    formData.value.cityId = item.id;
    cityChangeCallback();
  } else if (type === 2) {
    areaInfo.value.districtList.forEach(item2 => {
      if (item2.id !== item.id) {
        item2.isCheck = false;
      } else {
        item.isCheck = !item.isCheck;
      }
    });
    formData.value.districtId = item.isCheck ? item.id : "";
  }

  console.log(item);
};

// 获取省份
async function getProvinceListHandle() {
  try {
    const { data } = await getProvinceList();
    areaInfo.value.provinceList = data;
  } catch (error) {
    console.log(error);
  }
}

// 获取城市
async function getCityListHandle(
  provinceId: number | string = formData.value.provinceId
) {
  if (!provinceId) return;
  try {
    // 如果存在缓存的旧数据,使用旧数据
    if (
      areaInfo.value.oldCityListInfo.provinceId === provinceId &&
      areaInfo.value.oldCityListInfo.cityList.length
    ) {
      areaInfo.value.cityList = JSON.parse(
        JSON.stringify(areaInfo.value.oldCityListInfo.cityList)
      );
      return;
    }
    const { data } = await getCityList(provinceId);
    // 缓存旧数据
    areaInfo.value.oldCityListInfo.provinceId = provinceId as string;
    areaInfo.value.oldCityListInfo.cityList = JSON.parse(JSON.stringify(data));
    areaInfo.value.cityList = data;
  } catch (error) {
    console.log(error);
  }
}

// 获取区域
async function getDistrictListHandle(
  cityId: number | string = formData.value.cityId
) {
  if (!cityId) return;
  try {
    // 如果存在缓存的旧数据,使用旧数据
    if (
      areaInfo.value.oldDistrictList.cityId === cityId &&
      areaInfo.value.oldDistrictList.districtList.length
    ) {
      areaInfo.value.districtList = JSON.parse(
        JSON.stringify(areaInfo.value.oldDistrictList.districtList)
      );
      return;
    }
    const { data } = await getDistrictList(cityId);
    // 缓存旧数据
    areaInfo.value.oldDistrictList.cityId = cityId as string;
    areaInfo.value.oldDistrictList.districtList = JSON.parse(
      JSON.stringify(data)
    );
    areaInfo.value.districtList = data;
  } catch (error) {
    console.log(error);
  }
}

// 重置市数据
function resetCity() {
  formData.value.cityId = "";
  areaInfo.value.cityList = [];
}

// 重置区数据
function resetDistrict() {
  formData.value.districtId = "";
  areaInfo.value.districtList = [];
}

// 省份改变回调
const provinceChangeCallback = async () => {
  let provinceId = formData.value.provinceId;
  if (provinceId) {
    resetCity();
    resetDistrict();
    await getCityListHandle(provinceId);
  }
};
// 城市改变回调
const cityChangeCallback = async () => {
  let cityId = formData.value.cityId;
  if (cityId) {
    resetDistrict();
    await getDistrictListHandle(cityId);
  }
};

//#endregion
//#region <价格>
// 价格数据
const rentInfoList = ref<LabelValueInterface<(number | string)[]>[]>([
  {
    label: "不限",
    value: ["", ""],
    isCheck: true
  },
  {
    label: "0-1500元",
    value: [0, 1500],
    isCheck: false
  },
  {
    label: "1500-3000元",
    value: [1500, 3000],
    isCheck: false
  },
  {
    label: "3000-6000元",
    value: [3000, 6000],
    isCheck: false
  },
  {
    label: "6000-9000元",
    value: [6000, 9000],
    isCheck: false
  },

  {
    label: "9000-12000元",
    value: [9000, 12000],
    isCheck: false
  }
]);

// 价格点击
function rentClickHandle(item: LabelValueInterface<(number | string)[]>) {
  rentInfoList.value.forEach(item2 => {
    if (item.label === item2.label) {
      item.isCheck = !item.isCheck;
      formData.value.minRent = item.isCheck ? item.value[0] : "";
      formData.value.maxRent = item.isCheck ? item.value[1] : "";
    } else {
      item2.isCheck = false;
    }
  });
}

//#endregion
//#region <付款方式>
// 价格数据
const paymentInfoList = ref<LabelValueInterface[]>([]);

// 获取支付方式列表
async function getPaymentListHandle() {
  try {
    const { data } = await getPaymentTypeList();

    paymentInfoList.value =
      data?.map(item => ({
        label: item.name,
        value: item.id,
        isCheck: false
      })) || [];
  } catch (error) {
    console.log(error);
  }
}

// 价格点击
function paymentClickHandle(item: LabelValueInterface) {
  paymentInfoList.value.forEach(item2 => {
    if (item.label === item2.label) {
      item.isCheck = !item.isCheck;
      formData.value.paymentTypeId = item.isCheck ? item.value : "";
    } else {
      item2.isCheck = false;
    }
  });
}

//#endregion
//#region <发布者类型>
// 发布者类型数据
const publisherInfoList = ref<LabelValueInterface[]>([
  {
    label: "不限",
    value: "",
    isCheck: true
  },
  {
    label: "官方发布",
    value: "1",  // 修改为数字字符串，对应后端的数字类型
    isCheck: false
  },
  {
    label: "房东发布",
    value: "2",  // 修改为数字字符串，对应后端的数字类型
    isCheck: false
  }
]);

// 发布者类型点击
function publisherClickHandle(item: LabelValueInterface) {
  publisherInfoList.value.forEach(item2 => {
    if (item.label === item2.label) {
      item.isCheck = !item.isCheck;
      formData.value.publisherType = item.isCheck ? item.value : "";
    } else {
      item2.isCheck = false;
    }
  });
}

//#endregion
//#region <排序>
// 价格数据
const orderTypeInfoList = ref<LabelValueInterface[]>(
  SearchOrderTypeMap.map(item => ({
    ...item,
    isCheck: false
  }))
);
// 价格点击
function orderTypeClickHandle(item: LabelValueInterface) {
  orderTypeInfoList.value.forEach(item2 => {
    if (item.label === item2.label) {
      item.isCheck = !item.isCheck;
      formData.value.orderType = item.isCheck
        ? (item.value as SearchOrderType)
        : "";
    } else {
      item2.isCheck = false;
    }
  });
}

//#endregion
const onConfirmHandle = () => {
  // 关闭筛选界面
  isMenuOpen.value = false;
  // 请求接口
  console.log(formData.value);
  // getRoomList(formData.value);
  console.log("props.confirmCallback", !!props.confirmCallback);
  props.confirmCallback && props.confirmCallback(formData.value);
};

// 取消
const onCancelHandle = () => {
  // 关闭筛选界面
  isMenuOpen.value = false;
};
onMounted(() => {
  // 获取省份列表
  getProvinceListHandle();
  // 获取支付方式列表
  getPaymentListHandle();
});
</script>

<style scoped lang="less">
// 地区选择
.checked {
  color: var(--van-primary-color);
}

// 价格选择
.other-checked {
  background-color: var(--van-primary-color);
  color: var(--van-white);
}

// 简单修复下拉菜单层级问题，不改变默认定位
:deep(.van-dropdown-menu) {
  z-index: 1000;
}

// 当菜单打开时隐藏筛选按钮栏
:deep(.van-dropdown-menu__bar) {
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

// 菜单打开时隐藏按钮栏 - 使用更强的选择器优先级
.menu-open :deep(.van-dropdown-menu__bar) {
  opacity: 0 !important;
  visibility: hidden !important;
  pointer-events: none !important;
  transform: translateY(-100%) !important;
}

// 确保下拉内容有足够空间显示
.menu-open :deep(.van-dropdown-item) {
  margin-top: 0 !important;
}

// 优化下拉内容的显示
:deep(.van-dropdown-item__content) {
  max-height: 70vh !important;
  overflow-y: auto;
}

// 筛选按钮栏样式
.filter-bar {
  background: white;
  border-bottom: 1px solid #eee;
  
  .filter-buttons {
    display: flex;
    height: 44px;
    
    button {
      flex: 1;
      border: none;
      background: white;
      color: #666;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background: #f5f5f5;
      }
      
      &:not(:last-child) {
        border-right: 1px solid #eee;
      }
    }
  }
}

// 筛选覆盖层
.filter-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: white;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

// 自定义筛选按钮栏样式
.custom-filter-bar {
  background: white;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
  
  .filter-buttons {
    display: flex;
    height: 44px;
    
    button {
      flex: 1;
      border: none;
      background: white;
      color: #666;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s ease;
      position: relative;
      
      &.active {
        color: var(--van-primary-color);
        font-weight: 500;
        
        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 20px;
          height: 2px;
          background: var(--van-primary-color);
        }
      }
      
      &:hover {
        background: #f5f5f5;
      }
      
      &:not(:last-child) {
        border-right: 1px solid #eee;
      }
    }
  }
}

// 筛选内容区域样式
.filter-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 80px; // 为底部按钮留出空间
}

// 底部按钮区域
.filter-footer {
  position: absolute;
  bottom: 60px; // 调整到底部导航栏上方，通常底部导航栏高度是50-60px
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #eee;
  padding: 12px 16px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1001; // 确保在其他元素之上
}

// 地区选择项样式
.area-item {
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  border-radius: 4px;
  margin: 2px 4px;
  
  &:hover {
    background-color: #f5f5f5;
  }
  
  &.checked {
    background-color: #f0f8ff;
    color: var(--van-primary-color);
    font-weight: 500;
  }
}
</style>
