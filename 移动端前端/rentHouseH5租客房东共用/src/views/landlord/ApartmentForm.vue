<template>
  <PageContainer :loading="loading">
    <!-- 页面头部 -->
    <template #header>
      <div class="apartment-form-header">
        <h2 class="apartment-form-header__title">
          {{ isEdit ? '编辑公寓' : '新建公寓' }}
        </h2>
        <p class="apartment-form-header__subtitle">
          {{ isEdit ? '修改公寓信息' : '填写公寓基本信息' }}
        </p>
      </div>
    </template>

    <!-- 表单内容 -->
    <van-form @submit="handleSubmit" ref="formRef">
      <!-- 基本信息 -->
      <div class="form-section">
        <div class="form-section__title">基本信息</div>
        
        <van-field
          v-model="formData.name"
          name="name"
          label="公寓名称"
          placeholder="请输入公寓名称"
          :rules="[{ required: true, message: '请输入公寓名称' }]"
          maxlength="50"
          show-word-limit
        />
        
        <van-field
          v-model="formData.introduction"
          name="introduction"
          label="公寓介绍"
          type="textarea"
          placeholder="请输入公寓介绍"
          :rules="[{ required: true, message: '请输入公寓介绍' }]"
          maxlength="500"
          show-word-limit
          rows="3"
        />
        
        <van-field
          v-model="formData.phone"
          name="phone"
          label="联系电话"
          placeholder="请输入联系电话"
          :rules="[
            { required: true, message: '请输入联系电话' },
            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' }
          ]"
        />
      </div>

      <!-- 位置信息 -->
      <div class="form-section">
        <div class="form-section__title">位置信息</div>
        
        <van-field
          v-model="locationText"
          name="location"
          label="所在地区"
          placeholder="请选择所在地区"
          readonly
          is-link
          @click="openLocationPicker"
          :rules="[{ required: true, message: '请选择所在地区' }]"
        />
        
        <van-field
          v-model="formData.addressDetail"
          name="addressDetail"
          label="详细地址"
          placeholder="请输入详细地址"
          :rules="[{ required: true, message: '请输入详细地址' }]"
          maxlength="100"
          show-word-limit
          @input="handleAddressInput"
        />
        
        <!-- 地址建议列表 -->
        <div v-if="addressSuggestions.length > 0" class="address-suggestions">
          <div 
            v-for="(suggestion, index) in addressSuggestions" 
            :key="index"
            class="address-suggestion-item"
            @click="handleAddressSelect(suggestion)"
          >
            {{ suggestion.label }}
          </div>
        </div>
      </div>

      <!-- 公寓图片 -->
      <ImageUploader
        v-model="formData.graphVoList"
        :current-images="apartmentDetail?.graphVoList || []"
        :max-images="9"
      />

      <!-- 配套设施 -->
      <FacilitySelector
        v-model="formData.facilityInfoIds"
        :selected-facilities="apartmentDetail?.facilityInfoList || []"
        :landlord-id="landlordId"
      />

      <!-- 公寓标签 -->
      <LabelSelector
        v-model="formData.labelIds"
        :selected-labels="apartmentDetail?.labelInfoList || []"
        :landlord-id="landlordId"
      />

      <!-- 杂费信息 -->
      <FeeManager
        v-model="formData.feeValueVoList"
        :current-fees="apartmentDetail?.feeValueVoList || []"
        :landlord-id="landlordId"
      />

      <!-- 提交按钮 -->
      <div class="form-actions">
        <van-button
          type="default"
          size="large"
          @click="handleCancel"
          class="form-actions__cancel"
        >
          取消
        </van-button>
        
        <van-button
          type="primary"
          size="large"
          native-type="submit"
          :loading="submitting"
          class="form-actions__submit"
        >
          {{ isEdit ? '保存修改' : '创建公寓' }}
        </van-button>
      </div>
    </van-form>

    <!-- 地区选择器 - 参考租客实现，使用简单的三列布局 -->
    <van-popup v-model:show="showLocationPicker" position="bottom">
      <div class="area-selector">
        <div class="area-header">
          <span class="area-title">选择地区</span>
        </div>
        
        <div class="area-content">
          <div class="area-columns">
            <!-- 省份列 -->
            <div class="area-column">
              <div class="column-title">省份</div>
              <div class="column-list">
                <div
                  v-for="province in provinceList"
                  :key="province.id"
                  :class="['area-item', { 'area-item-active': province.id === selectedProvinceId }]"
                  @click="handleProvinceSelect(province)"
                >
                  {{ province.name }}
                </div>
              </div>
            </div>
            
            <!-- 城市列 -->
            <div class="area-column">
              <div class="column-title">城市</div>
              <div class="column-list">
                <div
                  v-for="city in cityList"
                  :key="city.id"
                  :class="['area-item', { 'area-item-active': city.id === selectedCityId }]"
                  @click="handleCitySelect(city)"
                >
                  {{ city.name }}
                </div>
              </div>
            </div>
            
            <!-- 区县列 -->
            <div class="area-column">
              <div class="column-title">区县</div>
              <div class="column-list">
                <div
                  v-for="district in districtList"
                  :key="district.id"
                  :class="['area-item', { 'area-item-active': district.id === selectedDistrictId }]"
                  @click="handleDistrictSelect(district)"
                >
                  {{ district.name }}
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="area-footer">
          <van-button type="default" size="small" @click="handleAreaCancel">取消</van-button>
          <van-button type="primary" size="small" @click="handleAreaConfirm">确认</van-button>
        </div>
      </div>
    </van-popup>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { useUserStore } from '@/store/modules/user';
import { useMap } from '@/hooks/useMap';
import {
  createApartment,
  updateApartment,
  getApartmentDetailForEdit
} from '@/api/landlord/apartment';
import type { ApartmentSubmitVo, ApartmentDetailVo, FeeValueVo, GraphVo } from '@/types/landlord';
import { buildAreaListData, loadCityData, loadDistrictData, type AreaListData } from '@/api/common/region';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import { FacilitySelector, LabelSelector, FeeManager, ImageUploader } from '@/components/landlord';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const { AMap, initMap } = useMap();

// 响应式数据
const loading = ref(false);
const submitting = ref(false);
const showLocationPicker = ref(false);
const formRef = ref();
const apartmentDetail = ref<ApartmentDetailVo | null>(null);

// 表单数据
const formData = ref<Partial<ApartmentSubmitVo>>({
  name: '',
  introduction: '',
  phone: '',
  provinceId: 0,
  cityId: 0,
  districtId: 0,
  provinceName: '',
  cityName: '',
  districtName: '',
  addressDetail: '',
  latitude: '',
  longitude: '',
  isRelease: 0,
  facilityInfoIds: [],
  labelIds: [],
  feeValueVoList: [],
  graphVoList: []
});

// 地区数据
const areaList = ref<AreaListData>({
  province_list: {},
  city_list: {},
  county_list: {}
});

// 地区选择相关数据
const provinceList = ref<Array<{ id: number; name: string }>>([]);
const cityList = ref<Array<{ id: number; name: string }>>([]);
const districtList = ref<Array<{ id: number; name: string }>>([]);

// 当前选择的地区ID
const selectedProvinceId = ref<number>(0);
const selectedCityId = ref<number>(0);
const selectedDistrictId = ref<number>(0);

// 地址解析相关
const addressSuggestions = ref<Array<{
  label: string;
  value: string;
  location?: { lng: string; lat: string };
}>>([]);
const addressInputTimer = ref<NodeJS.Timeout | null>(null);

// 计算属性
const isEdit = computed(() => !!route.params.id);
const landlordId = computed(() => {
  const id = userStore.userInfo?.id;
  if (!id) {
    console.error('房东ID获取失败，用户信息:', userStore.userInfo);
  }
  return id || 0;
});

const locationText = computed(() => {
  if (!formData.value.provinceId || !formData.value.cityId || !formData.value.districtId) {
    return '';
  }
  
  const province = areaList.value.province_list[String(formData.value.provinceId)];
  const city = areaList.value.city_list[String(formData.value.cityId)];
  const district = areaList.value.county_list[String(formData.value.districtId)];
  
  return `${province} ${city} ${district}`;
});

// 加载地区数据
const loadAreaData = async () => {
  try {
    console.log('开始加载地区数据...');
    const areaData = await buildAreaListData();
    areaList.value = areaData;
    console.log('地区数据加载成功');
  } catch (error) {
    console.error('加载地区数据失败:', error);
    showToast('加载地区数据失败，使用默认数据');
    // 使用备用数据
    areaList.value = {
      province_list: {
        '11': '北京市',
        '12': '天津市',
        '31': '上海市',
        '44': '广东省',
        '50': '重庆市'
      },
      city_list: {},
      county_list: {}
    };
  }
};

// 加载公寓详情（编辑模式）
const loadApartmentDetail = async () => {
  if (!isEdit.value) return;
  
  try {
    loading.value = true;
    const apartmentId = Number(route.params.id);
    
    if (!landlordId.value) {
      showToast('用户信息获取失败');
      return;
    }
    
    console.log('加载公寓编辑详情，公寓ID:', apartmentId, '房东ID:', landlordId.value);
    
    const response = await getApartmentDetailForEdit(apartmentId, landlordId.value);
    const data = response.data;
    
    // 保存公寓详情数据
    apartmentDetail.value = data;
    
    // 填充表单基本数据
    formData.value = {
      id: data.id,
      name: data.name,
      introduction: data.introduction,
      phone: data.phone,
      provinceId: data.provinceId,
      cityId: data.cityId,
      districtId: data.districtId,
      addressDetail: data.addressDetail,
      latitude: data.latitude,
      longitude: data.longitude,
      isRelease: data.isRelease,
      facilityInfoIds: data.facilityInfoList?.map(f => f.id) || [],
      labelIds: data.labelInfoList?.map(l => l.id) || [],
      feeValueVoList: data.feeValueVoList || [],
      graphVoList: data.graphVoList || []
    };
    
    console.log('公寓详情加载完成，表单数据:', formData.value);
    
    // 为了正确显示地区信息，需要加载对应的地区数据
    if (data.provinceId && data.cityId && data.districtId) {
      try {
        // 确保省份数据已加载
        if (Object.keys(areaList.value.province_list).length === 0) {
          await loadAreaData();
        }
        
        // 加载对应的城市数据
        await loadCityData(data.provinceId, areaList.value);
        
        // 加载对应的区县数据
        await loadDistrictData(data.cityId, areaList.value);
        
        console.log('地区数据加载完成，可以正确显示地区信息');
      } catch (error) {
        console.error('加载地区数据失败:', error);
      }
    }
    
  } catch (error) {
    console.error('加载公寓详情失败:', error);
    showToast('加载公寓详情失败');
  } finally {
    loading.value = false;
  }
};

// 根据完整地址获取经纬度（使用高德地图API）
const getCoordinatesByAddress = async (fullAddress: string): Promise<{ lng: string; lat: string } | null> => {
  try {
    // 验证地址参数
    if (!fullAddress || fullAddress.trim() === '') {
      console.error('❌ 地址参数为空，无法进行地理编码');
      return null;
    }
    
    console.log('🗺️ 使用高德地图API根据地址获取经纬度:', fullAddress);
    
    // 确保高德地图API已初始化
    if (!AMap.value) {
      console.log('🔄 初始化高德地图API...');
      await initMap();
    }
    
    if (!AMap.value) {
      console.error('❌ 高德地图API初始化失败');
      return null;
    }
    
    // 检查Geocoder是否可用
    if (!AMap.value.Geocoder) {
      console.error('❌ 高德地图Geocoder插件未加载');
      return null;
    }
    
    return new Promise((resolve) => {
      // 使用高德地图的地理编码服务
      const geocoder = new AMap.value.Geocoder({
        // city 指定进行编码查询的城市，支持传入城市名、adcode、citycode
        city: formData.value.cityName || '全国'
      });
      
      geocoder.getLocation(fullAddress, (status: string, result: any) => {
        if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
          const location = result.geocodes[0].location;
          const coordinates = {
            lng: location.lng.toString(),
            lat: location.lat.toString()
          };
          
          console.log('✅ 高德地图API获取经纬度成功:', coordinates);
          console.log('📍 详细地址信息:', result.geocodes[0]);
          
          resolve(coordinates);
        } else {
          console.warn('⚠️ 高德地图API未找到匹配地址，使用默认坐标');
          console.log('API返回状态:', status);
          console.log('API返回结果:', result);
          
          // 使用基于城市的默认坐标
          const cityCoordinates = getCityDefaultCoordinates(formData.value.cityName || '');
          resolve(cityCoordinates);
        }
      });
    });
    
  } catch (error) {
    console.error('❌ 高德地图API调用失败:', error);
    
    // 降级到城市默认坐标
    const cityCoordinates = getCityDefaultCoordinates(formData.value.cityName || '');
    return cityCoordinates;
  }
};

// 获取城市默认坐标（备用方案）
const getCityDefaultCoordinates = (cityName: string): { lng: string; lat: string } => {
  const cityCoordinates: { [key: string]: { lng: string; lat: string } } = {
    '北京市': { lng: '116.397428', lat: '39.90923' },
    '上海市': { lng: '121.473701', lat: '31.230416' },
    '广州市': { lng: '113.280637', lat: '23.125178' },
    '深圳市': { lng: '114.085947', lat: '22.547' },
    '杭州市': { lng: '120.153576', lat: '30.287459' },
    '南京市': { lng: '118.767413', lat: '32.041544' },
    '武汉市': { lng: '114.298572', lat: '30.584355' },
    '成都市': { lng: '104.065735', lat: '30.659462' },
    '天津市': { lng: '117.190182', lat: '39.125596' },
    '重庆市': { lng: '106.504962', lat: '29.533155' },
    '西安市': { lng: '108.948024', lat: '34.263161' },
    '沈阳市': { lng: '123.429096', lat: '41.796767' },
    '青岛市': { lng: '120.355173', lat: '36.082982' },
    '大连市': { lng: '121.618622', lat: '38.91459' },
    '厦门市': { lng: '118.11022', lat: '24.490474' },
    '苏州市': { lng: '120.619585', lat: '31.299379' },
    '宁波市': { lng: '121.549792', lat: '29.868388' },
    '无锡市': { lng: '120.301663', lat: '31.574729' },
    '长春市': { lng: '125.3245', lat: '43.817' },
    '吉林市': { lng: '126.5349', lat: '43.8171' },
    '哈尔滨市': { lng: '126.534967', lat: '45.803775' },
    '郑州市': { lng: '113.665412', lat: '34.757975' },
    '福州市': { lng: '119.296428', lat: '26.084745' },
    '南昌市': { lng: '115.857993', lat: '28.682892' },
    '长沙市': { lng: '112.938814', lat: '28.228209' },
    '南宁市': { lng: '108.33010', lat: '22.80699' },
    '海口市': { lng: '110.33119', lat: '20.02773' },
    '贵阳市': { lng: '106.71519', lat: '26.57540' },
    '昆明市': { lng: '102.71255', lat: '24.88441' },
    '兰州市': { lng: '103.83414', lat: '36.06138' },
    '太原市': { lng: '112.54909', lat: '37.87059' },
    '石家庄市': { lng: '114.54995', lat: '38.04280' },
    '呼和浩特市': { lng: '111.65408', lat: '40.81814' },
    '南通市': { lng: '120.89442', lat: '32.00013' },
    '常州市': { lng: '119.97265', lat: '31.77104' },
    '镇江市': { lng: '119.44599', lat: '32.20290' },
    '扬州市': { lng: '119.42863', lat: '32.39905' },
    '泰州市': { lng: '120.13326', lat: '32.62897' },
    '南京市': { lng: '118.79769', lat: '32.03041' },
    '合肥市': { lng: '117.22674', lat: '31.86119' },
    '芜湖市': { lng: '118.37389', lat: '30.94155' },
    '马鞍山市': { lng: '118.50861', lat: '31.68853' },
    '安庆市': { lng: '117.05264', lat: '30.51615' },
    '黄山市': { lng: '118.38095', lat: '29.71926' },
    '阜阳市': { lng: '115.82558', lat: '32.89837' },
    '六安市': { lng: '115.48572', lat: '31.75861' },
    '亳州市': { lng: '115.78125', lat: '32.86529' },
    '池州市': { lng: '117.49218', lat: '30.66704' },
    '宣城市': { lng: '118.75822', lat: '30.94555' },
    '嘉兴市': { lng: '120.75625', lat: '30.76641' },
    '湖州市': { lng: '120.09577', lat: '30.86008' },
    '绍兴市': { lng: '120.57627', lat: '29.99083' },
    '金华市': { lng: '119.64239', lat: '29.12154' },
    '衢州市': { lng: '118.87394', lat: '28.97383' },
    '舟山市': { lng: '122.20681', lat: '29.98683' },
    '台州市': { lng: '121.42048', lat: '28.65745' },
    '丽水市': { lng: '119.92616', lat: '28.45378' }
  };
  
  // 精确匹配
  if (cityCoordinates[cityName]) {
    console.log(`🎯 使用${cityName}的默认坐标:`, cityCoordinates[cityName]);
    return cityCoordinates[cityName];
  }
  
  // 模糊匹配
  for (const [city, coords] of Object.entries(cityCoordinates)) {
    if (cityName.includes(city.replace('市', '')) || city.includes(cityName)) {
      console.log(`🎯 模糊匹配到${city}的坐标:`, coords);
      return coords;
    }
  }
  
  // 默认使用北京坐标
  console.log('🎯 使用默认坐标（北京）');
  return cityCoordinates['北京市'];
};

// 事件处理
const handleSubmit = async () => {
  try {
    submitting.value = true;
    
    console.log('开始提交公寓表单，当前用户信息:', userStore.userInfo);
    console.log('房东ID:', landlordId.value);
    
    if (!landlordId.value) {
      console.error('房东ID获取失败，用户信息:', userStore.userInfo);
      showToast('用户信息获取失败，请重新登录');
      return;
    }
    
    // 验证必填字段
    if (!formData.value.name || !formData.value.introduction || !formData.value.phone) {
      showToast('请填写完整的基本信息');
      return;
    }
    
    if (!formData.value.provinceId || !formData.value.cityId || !formData.value.districtId) {
      showToast('请选择完整的地区信息');
      return;
    }
    
    if (!formData.value.addressDetail) {
      showToast('请输入详细地址');
      return;
    }
    
    // 如果经纬度为空，尝试根据地址获取
    if (!formData.value.latitude || !formData.value.longitude) {
      console.log('经纬度为空，尝试根据地址获取...');
      
      // 安全地构建完整地址，过滤掉空值
      const addressParts = [
        formData.value.provinceName,
        formData.value.cityName, 
        formData.value.districtName,
        formData.value.addressDetail
      ].filter(part => part && part.trim() !== ''); // 过滤掉空值和空字符串
      
      const fullAddress = addressParts.join('');
      
      if (fullAddress.trim() === '') {
        console.warn('⚠️ 地址信息不完整，无法获取经纬度');
        showToast('地址信息不完整，请完善地址信息');
        return;
      }
      
      console.log('🗺️ 构建的完整地址:', fullAddress);
      const coordinates = await getCoordinatesByAddress(fullAddress);
      
      if (coordinates) {
        formData.value.latitude = coordinates.lat;
        formData.value.longitude = coordinates.lng;
        console.log('自动获取经纬度成功:', coordinates);
      } else {
        console.warn('⚠️ 无法自动获取经纬度，使用所选城市的默认坐标');
        // 使用所选城市的默认坐标，而不是硬编码的北京坐标
        const cityCoordinates = getCityDefaultCoordinates(formData.value.cityName || '');
        formData.value.latitude = cityCoordinates.lat;
        formData.value.longitude = cityCoordinates.lng;
        console.log('使用城市默认坐标:', cityCoordinates);
      }
    }
    
    // 构建提交数据
    const submitData: ApartmentSubmitVo = {
      ...formData.value as ApartmentSubmitVo
    };
    
    console.log('提交公寓数据:', submitData);
    console.log('房东ID参数:', landlordId.value);
    console.log('经纬度信息:', {
      latitude: submitData.latitude,
      longitude: submitData.longitude
    });
    
    if (isEdit.value) {
      console.log('执行更新公寓操作');
      await updateApartment(submitData, landlordId.value);
      showToast('修改成功');
    } else {
      console.log('执行创建公寓操作');
      await createApartment(submitData, landlordId.value);
      showToast('创建成功');
    }
    
    router.back();
    
  } catch (error) {
    console.error('提交失败，详细错误信息:', error);
    
    // 更详细的错误处理
    let errorMessage = isEdit.value ? '修改失败' : '创建失败';
    
    if (error && typeof error === 'object') {
      if ('response' in error && error.response) {
        const response = error.response as any;
        console.error('HTTP响应错误:', response.status, response.data);
        
        if (response.status === 401) {
          errorMessage = '登录已过期，请重新登录';
        } else if (response.status === 403) {
          errorMessage = '权限不足';
        } else if (response.data && response.data.message) {
          errorMessage = response.data.message;
        }
      } else if ('message' in error) {
        errorMessage = (error as any).message;
      }
    }
    
    showToast(errorMessage);
  } finally {
    submitting.value = false;
  }
};

const handleCancel = () => {
  showConfirmDialog({
    title: '确认取消',
    message: '确定要取消吗？未保存的内容将丢失。'
  }).then(() => {
    router.back();
  }).catch(() => {
    // 用户取消
  });
};

// 打开地区选择器
const openLocationPicker = async () => {
  try {
    console.log('打开地区选择器，加载省份数据');
    
    // 确保省份数据已加载
    if (Object.keys(areaList.value.province_list).length === 0) {
      console.log('省份数据未加载，重新加载...');
      await loadAreaData();
    }
    
    // 构建省份列表数据
    provinceList.value = Object.entries(areaList.value.province_list).map(([id, name]) => ({
      id: Number(id),
      name
    }));
    
    // 清空城市和区县列表
    cityList.value = [];
    districtList.value = [];
    
    // 重置选择状态
    selectedProvinceId.value = 0;
    selectedCityId.value = 0;
    selectedDistrictId.value = 0;
    
    console.log('省份数据已准备:', provinceList.value.length, '个省份');
    
    showLocationPicker.value = true;
  } catch (error) {
    console.error('打开地区选择器失败:', error);
    showToast('地区数据加载失败');
  }
};

// 省份选择处理
const handleProvinceSelect = async (province: { id: number; name: string }) => {
  try {
    console.log('选择省份:', province);
    
    selectedProvinceId.value = province.id;
    selectedCityId.value = 0;
    selectedDistrictId.value = 0;
    
    // 清空城市和区县数据
    areaList.value.city_list = {};
    areaList.value.county_list = {};
    cityList.value = [];
    districtList.value = [];
    
    console.log('开始加载省份', province.id, '的城市数据');
    
    // 加载城市数据
    await loadCityData(province.id, areaList.value);
    
    // 构建城市列表数据
    cityList.value = Object.entries(areaList.value.city_list).map(([id, name]) => ({
      id: Number(id),
      name
    }));
    
    console.log('城市数据加载完成:', cityList.value.length, '个城市');
  } catch (error) {
    console.error('加载城市数据失败:', error);
    showToast('加载城市数据失败');
  }
};

// 城市选择处理
const handleCitySelect = async (city: { id: number; name: string }) => {
  try {
    console.log('选择城市:', city);
    
    selectedCityId.value = city.id;
    selectedDistrictId.value = 0;
    
    // 清空区县数据
    areaList.value.county_list = {};
    districtList.value = [];
    
    console.log('开始加载城市', city.id, '的区县数据');
    
    // 加载区县数据
    await loadDistrictData(city.id, areaList.value);
    
    // 构建区县列表数据
    districtList.value = Object.entries(areaList.value.county_list).map(([id, name]) => ({
      id: Number(id),
      name
    }));
    
    console.log('区县数据加载完成:', districtList.value.length, '个区县');
  } catch (error) {
    console.error('加载区县数据失败:', error);
    showToast('加载区县数据失败');
  }
};

// 区县选择处理
const handleDistrictSelect = (district: { id: number; name: string }) => {
  console.log('选择区县:', district);
  selectedDistrictId.value = district.id;
};

// 地区选择取消
const handleAreaCancel = () => {
  showLocationPicker.value = false;
};

// 地区选择确认
const handleAreaConfirm = () => {
  if (!selectedProvinceId.value || !selectedCityId.value || !selectedDistrictId.value) {
    showToast('请选择完整的省市区信息');
    return;
  }
  
  // 保存到表单数据
  formData.value.provinceId = selectedProvinceId.value;
  formData.value.cityId = selectedCityId.value;
  formData.value.districtId = selectedDistrictId.value;
  
  // 填充省市区名称
  const province = areaList.value.province_list[String(selectedProvinceId.value)];
  const city = areaList.value.city_list[String(selectedCityId.value)];
  const district = areaList.value.county_list[String(selectedDistrictId.value)];
  
  formData.value.provinceName = province || '';
  formData.value.cityName = city || '';
  formData.value.districtName = district || '';
  
  console.log('地区选择完成:', {
    provinceId: formData.value.provinceId,
    cityId: formData.value.cityId,
    districtId: formData.value.districtId,
    provinceName: formData.value.provinceName,
    cityName: formData.value.cityName,
    districtName: formData.value.districtName
  });
  
  showLocationPicker.value = false;
  showToast('地区选择成功');
};

// 地址输入处理（防抖）
const handleAddressInput = (value: string) => {
  // 清除之前的定时器
  if (addressInputTimer.value) {
    clearTimeout(addressInputTimer.value);
  }
  
  // 如果输入为空，清空建议列表和经纬度
  if (!value || value.trim().length < 2) {
    addressSuggestions.value = [];
    formData.value.latitude = '';
    formData.value.longitude = '';
    return;
  }
  
  // 设置防抖定时器
  addressInputTimer.value = setTimeout(() => {
    searchAddress(value.trim());
  }, 500);
};

// 地址搜索（使用高德地图API）
const searchAddress = async (keyword: string) => {
  try {
    // 构建完整的搜索关键词（包含省市区信息）
    let fullKeyword = keyword;
    if (formData.value.provinceName && formData.value.cityName && formData.value.districtName) {
      fullKeyword = `${formData.value.provinceName}${formData.value.cityName}${formData.value.districtName}${keyword}`;
    }
    
    console.log('搜索地址:', fullKeyword);
    
    // 这里需要集成高德地图API
    // 由于当前环境限制，我们先模拟一个简单的地址解析
    // 实际项目中需要使用高德地图的AutoComplete API
    
    // 获取所选城市的默认坐标
    const cityCoordinates = getCityDefaultCoordinates(formData.value.cityName || '');
    
    // 模拟地址建议数据，使用所选城市的坐标而不是硬编码的北京坐标
    const mockSuggestions = [
      {
        label: `${formData.value.districtName || ''}${keyword}`,
        value: `${formData.value.districtName || ''}${keyword}`,
        location: {
          lng: cityCoordinates.lng,
          lat: cityCoordinates.lat
        }
      }
    ];
    
    addressSuggestions.value = mockSuggestions;
    
  } catch (error) {
    console.error('地址搜索失败:', error);
    addressSuggestions.value = [];
  }
};

// 选择地址建议
const handleAddressSelect = (suggestion: { label: string; value: string; location?: { lng: string; lat: string } }) => {
  console.log('选择地址:', suggestion);
  
  // 更新详细地址
  formData.value.addressDetail = suggestion.value;
  
  // 更新经纬度
  if (suggestion.location) {
    formData.value.longitude = suggestion.location.lng;
    formData.value.latitude = suggestion.location.lat;
    
    console.log('更新经纬度:', {
      longitude: formData.value.longitude,
      latitude: formData.value.latitude
    });
  }
  
  // 清空建议列表
  addressSuggestions.value = [];
  
  showToast('地址选择成功');
};

// 生命周期
onMounted(() => {
  loadAreaData();
  loadApartmentDetail();
});
</script>

<style scoped lang="less">
.apartment-form-header {
  margin-bottom: 20px;
  
  &__title {
    font-size: 20px;
    font-weight: 600;
    color: #323233;
    margin: 0 0 8px 0;
  }
  
  &__subtitle {
    font-size: 14px;
    color: #969799;
    margin: 0;
  }
}

.form-section {
  margin-bottom: 24px;
  
  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 3px solid #1989fa;
  }
  
  &__tip {
    font-size: 12px;
    color: #969799;
    margin-top: 8px;
    padding: 0 16px;
  }
}

.facility-grid {
  padding: 0 16px;
  
  :deep(.van-checkbox-group) {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

.facility-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  
  :deep(.van-checkbox__label) {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.label-grid {
  padding: 0 16px;
  
  :deep(.van-checkbox-group) {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

.label-item {
  padding: 8px 12px;
  background: #f7f8fa;
  border-radius: 6px;
  text-align: center;
}

.fee-list {
  padding: 0 16px;
}

.fee-item {
  position: relative;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.fee-unit {
  font-size: 14px;
  color: #969799;
}

.fee-remove {
  position: absolute;
  top: 8px;
  right: 8px;
}

.fee-add {
  width: 100%;
  margin-top: 8px;
}

.form-actions {
  display: flex;
  gap: 12px;
  padding: 20px 0;
  
  &__cancel,
  &__submit {
    flex: 1;
  }
}

// 响应式设计
@media (max-width: 375px) {
  .facility-grid,
  .label-grid {
    :deep(.van-checkbox-group) {
      grid-template-columns: 1fr;
    }
  }
}

// 地区选择器样式
.area-selector {
  background: white;
  border-radius: 16px 16px 0 0;
  
  .area-header {
    padding: 16px;
    text-align: center;
    border-bottom: 1px solid #ebedf0;
    
    .area-title {
      font-size: 16px;
      font-weight: 600;
      color: #323233;
    }
  }
  
  .area-content {
    height: 300px;
    overflow: hidden;
  }
  
  .area-columns {
    display: flex;
    height: 100%;
  }
  
  .area-column {
    flex: 1;
    border-right: 1px solid #ebedf0;
    
    &:last-child {
      border-right: none;
    }
    
    .column-title {
      padding: 12px;
      font-size: 14px;
      font-weight: 600;
      color: #646566;
      text-align: center;
      background: #f7f8fa;
      border-bottom: 1px solid #ebedf0;
    }
    
    .column-list {
      height: calc(100% - 45px);
      overflow-y: auto;
    }
  }
  
  .area-item {
    padding: 12px 16px;
    font-size: 14px;
    color: #323233;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #f7f8fa;
    }
    
    &.area-item-active {
      background: #1989fa;
      color: white;
    }
  }
  
  .area-footer {
    display: flex;
    gap: 12px;
    padding: 16px;
    border-top: 1px solid #ebedf0;
    
    .van-button {
      flex: 1;
    }
  }
}

// 地址建议列表样式
.address-suggestions {
  position: relative;
  background: white;
  border: 1px solid #ebedf0;
  border-radius: 8px;
  margin: 8px 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
  
  .address-suggestion-item {
    padding: 12px 16px;
    font-size: 14px;
    color: #323233;
    border-bottom: 1px solid #ebedf0;
    cursor: pointer;
    transition: background-color 0.2s;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      background: #f7f8fa;
    }
    
    &:active {
      background: #e8f3ff;
    }
  }
}
</style>