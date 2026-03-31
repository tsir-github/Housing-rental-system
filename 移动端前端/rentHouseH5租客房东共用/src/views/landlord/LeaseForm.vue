<template>
  <div class="lease-form">
    <!-- 导航栏 -->
    <van-nav-bar 
      :title="getPageTitle()" 
      left-arrow 
      @click-left="handleBack"
      fixed
      placeholder
    />

    <!-- 表单内容 -->
    <van-form @submit="onSubmit" ref="formRef">
      <div class="form-content">
        <!-- 租客信息 -->
        <div class="form-section">
          <div class="section-title">
            <van-icon name="contact" />
            租客信息
          </div>
          
          <van-field
            v-model="formData.name"
            name="name"
            label="租客姓名"
            placeholder="请输入租客姓名"
            :rules="[{ required: true, message: '请输入租客姓名' }]"
          />
          
          <van-field
            v-model="formData.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            type="tel"
            :rules="[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
            ]"
          />
          
          <van-field
            v-model="formData.identificationNumber"
            name="identificationNumber"
            label="身份证号"
            placeholder="请输入身份证号"
            :rules="[
              { required: true, message: '请输入身份证号' },
              { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号' }
            ]"
          />
        </div>

        <!-- 房源信息 -->
        <div class="form-section">
          <div class="section-title">
            <van-icon name="home-o" />
            房源信息
          </div>
          
          <van-field
            v-model="selectedProvinceName"
            name="province"
            label="省份"
            placeholder="请选择省份"
            readonly
            is-link
            @click="showProvincePicker = true"
            :rules="[{ required: true, message: '请选择省份' }]"
          />
          
          <van-field
            v-model="selectedCityName"
            name="city"
            label="城市"
            placeholder="请选择城市"
            readonly
            is-link
            @click="showCityPicker = true"
            :rules="[{ required: true, message: '请选择城市' }]"
            :disabled="!regionData.provinceId"
          />
          
          <van-field
            v-model="selectedDistrictName"
            name="district"
            label="区县"
            placeholder="请选择区县"
            readonly
            is-link
            @click="showDistrictPicker = true"
            :rules="[{ required: true, message: '请选择区县' }]"
            :disabled="!regionData.cityId"
          />
          
          <van-field
            v-model="selectedApartmentName"
            name="apartment"
            label="所属公寓"
            placeholder="请选择公寓"
            readonly
            is-link
            @click="showApartmentPicker = true"
            :rules="[{ required: true, message: '请选择公寓' }]"
            :disabled="!regionData.districtId"
          />
          
          <van-field
            v-model="selectedRoomName"
            name="room"
            label="房间"
            placeholder="请选择房间"
            readonly
            is-link
            @click="showRoomPicker = true"
            :rules="[{ required: true, message: '请选择房间' }]"
            :disabled="!formData.apartmentId"
          />
        </div>

        <!-- 租赁条款 -->
        <div class="form-section">
          <div class="section-title">
            <van-icon name="description" />
            租赁条款
          </div>
          
          <van-field
            v-model="formData.rent"
            name="rent"
            label="月租金"
            placeholder="请输入月租金"
            type="number"
            :rules="[
              { required: true, message: '请输入月租金' },
              { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的金额' }
            ]"
          >
            <template #right-icon>
              <span class="unit">元/月</span>
            </template>
          </van-field>
          
          <van-field
            v-model="formData.deposit"
            name="deposit"
            label="押金"
            placeholder="请输入押金"
            type="number"
            :rules="[
              { required: true, message: '请输入押金' },
              { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的金额' }
            ]"
          >
            <template #right-icon>
              <span class="unit">元</span>
            </template>
          </van-field>
          
          <van-field
            v-model="selectedLeaseTermName"
            name="leaseTerm"
            label="租期"
            placeholder="请选择租期"
            readonly
            is-link
            @click="showLeaseTermPicker = true"
            :rules="[{ required: true, message: '请选择租期' }]"
          />
          
          <van-field
            v-model="selectedPaymentTypeName"
            name="paymentType"
            label="支付方式"
            placeholder="请选择支付方式"
            readonly
            is-link
            @click="showPaymentTypePicker = true"
            :rules="[{ required: true, message: '请选择支付方式' }]"
          />
        </div>

        <!-- 租期时间 -->
        <div class="form-section">
          <div class="section-title">
            <van-icon name="calendar-o" />
            租期时间
          </div>
          
          <van-field
            v-model="formData.leaseStartDate"
            name="leaseStartDate"
            label="开始时间"
            placeholder="请选择开始时间"
            readonly
            is-link
            @click="showStartDatePicker = true"
            :rules="[{ required: true, message: '请选择开始时间' }]"
          />
          
          <van-field
            v-model="formData.leaseEndDate"
            name="leaseEndDate"
            label="结束时间"
            placeholder="自动计算"
            readonly
            disabled
          />
        </div>

        <!-- 附加信息 -->
        <div class="form-section">
          <div class="section-title">
            <van-icon name="notes-o" />
            附加信息
          </div>
          
          <van-field
            v-model="formData.additionalInfo"
            name="additionalInfo"
            label="备注"
            type="textarea"
            :placeholder="isResendMode ? '系统将自动记录修改内容' : '请输入备注信息（选填）'"
            rows="3"
            autosize
            maxlength="500"
            show-word-limit
            :disabled="isResendMode"
            :readonly="isResendMode"
          />
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-section">
        <van-button 
          type="primary" 
          size="large" 
          native-type="submit"
          :loading="submitting"
          block
        >
          {{ getSubmitButtonText() }}
        </van-button>
      </div>
    </van-form>

    <!-- 选择器弹窗 -->
    <!-- 省份选择器 -->
    <van-popup v-model:show="showProvincePicker" position="bottom">
      <van-picker
        :columns="provinceOptions"
        @confirm="onProvinceConfirm"
        @cancel="showProvincePicker = false"
      />
    </van-popup>

    <!-- 城市选择器 -->
    <van-popup v-model:show="showCityPicker" position="bottom">
      <van-picker
        :columns="cityOptions"
        @confirm="onCityConfirm"
        @cancel="showCityPicker = false"
      />
    </van-popup>

    <!-- 区县选择器 -->
    <van-popup v-model:show="showDistrictPicker" position="bottom">
      <van-picker
        :columns="districtOptions"
        @confirm="onDistrictConfirm"
        @cancel="showDistrictPicker = false"
      />
    </van-popup>

    <!-- 公寓选择器 -->
    <van-popup v-model:show="showApartmentPicker" position="bottom">
      <van-picker
        :columns="apartmentOptions"
        @confirm="onApartmentConfirm"
        @cancel="showApartmentPicker = false"
      />
    </van-popup>

    <!-- 房间选择器 -->
    <van-popup v-model:show="showRoomPicker" position="bottom">
      <van-picker
        :columns="roomOptions"
        @confirm="onRoomConfirm"
        @cancel="showRoomPicker = false"
      />
    </van-popup>

    <!-- 租期选择器 -->
    <van-popup v-model:show="showLeaseTermPicker" position="bottom">
      <van-picker
        :columns="leaseTermOptions"
        @confirm="onLeaseTermConfirm"
        @cancel="showLeaseTermPicker = false"
      />
    </van-popup>

    <!-- 支付方式选择器 -->
    <van-popup v-model:show="showPaymentTypePicker" position="bottom">
      <van-picker
        :columns="paymentTypeOptions"
        @confirm="onPaymentTypeConfirm"
        @cancel="showPaymentTypePicker = false"
      />
    </van-popup>

    <!-- 日期选择器 -->
    <van-popup v-model:show="showStartDatePicker" position="bottom">
      <van-date-picker
        v-model="startDateArray"
        @confirm="onStartDateConfirm"
        @cancel="showStartDatePicker = false"
        title="选择开始时间"
        :min-date="new Date()"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { NavigationHelper } from '@/utils/navigationHelper';
import {
  getLandlordLeaseDetail,
  createLease,
  updateLease,
  resendLease,
  type LandlordLeaseCreateVo,
  type LandlordLeaseDetailVo
} from '@/api/landlord/lease';
import {
  getAdminProvinceList,
  getAdminCityListByProvinceId,
  getAdminDistrictListByCityId,
  type AdminProvinceInfo,
  type AdminCityInfo,
  type AdminDistrictInfo
} from '@/api/admin/region';
import {
  getAdminApartmentsByDistrictId,
  type AdminApartmentInfo
} from '@/api/admin/apartment';
import {
  getAdminRoomsByApartmentId,
  type AdminRoomInfo
} from '@/api/admin/room';
import {
  getPaymentTypes,
  getLeaseTerms,
  type PaymentType,
  type LeaseTerm
} from '@/api/landlord/management';
import {
  getUserInfo,
  type UserInfoInterface
} from '@/api/common/user';

const router = useRouter();
const route = useRoute();
const formRef = ref();

// 响应式数据
const submitting = ref(false);
const showProvincePicker = ref(false);
const showCityPicker = ref(false);
const showDistrictPicker = ref(false);
const showApartmentPicker = ref(false);
const showRoomPicker = ref(false);
const showLeaseTermPicker = ref(false);
const showPaymentTypePicker = ref(false);
const showStartDatePicker = ref(false);
const startDateArray = ref([new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()]);

// 重发租约相关数据
const isResendMode = computed(() => route.query.mode === 'resend');
const originalLeaseId = computed(() => route.query.resendFrom ? Number(route.query.resendFrom) : null);
const originalLeaseData = ref<LandlordLeaseDetailVo | null>(null);

// 表单数据
const formData = reactive<LandlordLeaseCreateVo>({
  name: '',
  phone: '',
  identificationNumber: '',
  apartmentId: 0,
  roomId: 0,
  rent: 0,
  deposit: 0,
  leaseTermId: 0,
  paymentTypeId: 0,
  leaseStartDate: '',
  leaseEndDate: '',
  additionalInfo: ''
});

// 地区选择辅助数据（用于UI显示，不提交到后端）
const regionData = reactive({
  provinceId: 0,
  cityId: 0,
  districtId: 0
});

// 选择器显示名称
const selectedProvinceName = ref('');
const selectedCityName = ref('');
const selectedDistrictName = ref('');
const selectedApartmentName = ref('');
const selectedRoomName = ref('');
const selectedLeaseTermName = ref('');
const selectedPaymentTypeName = ref('');

// 选择器选项
const provinceOptions = ref<Array<{ text: string; value: number }>>([]);
const cityOptions = ref<Array<{ text: string; value: number }>>([]);
const districtOptions = ref<Array<{ text: string; value: number }>>([]);
const apartmentOptions = ref<Array<{ text: string; value: number }>>([]);
const roomOptions = ref<Array<{ text: string; value: number }>>([]);
const leaseTermOptions = ref<Array<{ text: string; value: number; monthCount: number }>>([]);
const paymentTypeOptions = ref<Array<{ text: string; value: number }>>([]);

// 计算属性
const isEdit = computed(() => !!route.params.id && route.params.id !== 'new');
const leaseId = computed(() => Number(route.params.id));

// 获取页面标题
const getPageTitle = () => {
  if (isResendMode.value) {
    return '重发租约';
  } else if (isEdit.value) {
    return '编辑租约';
  } else {
    return '创建租约';
  }
};

// 监听租期变化，自动计算结束时间
watch([() => formData.leaseStartDate, () => formData.leaseTermId], () => {
  calculateEndDate();
});

// 计算结束时间
const calculateEndDate = () => {
  if (!formData.leaseStartDate || !formData.leaseTermId) return;
  
  const selectedTerm = leaseTermOptions.value.find(term => term.value === formData.leaseTermId);
  if (!selectedTerm) return;
  
  const startDate = new Date(formData.leaseStartDate);
  const endDate = new Date(startDate);
  endDate.setMonth(endDate.getMonth() + selectedTerm.monthCount);
  
  formData.leaseEndDate = endDate.toISOString().split('T')[0];
};

// 加载重发租约的原始数据
const loadResendData = async () => {
  if (!isResendMode.value || !originalLeaseId.value) return;
  
  try {
    console.log('🔄 加载原租约数据用于重发 - originalLeaseId:', originalLeaseId.value);
    const response = await getLandlordLeaseDetail(originalLeaseId.value);
    const lease = response.data;
    
    // 保存原始数据用于比较
    originalLeaseData.value = lease;
    
    // 填充表单数据
    Object.assign(formData, {
      name: lease.name,
      phone: lease.phone,
      identificationNumber: lease.identificationNumber,
      apartmentId: lease.apartmentInfo.id,
      roomId: lease.roomInfo.id,
      rent: lease.rent,
      deposit: lease.deposit,
      leaseTermId: lease.leaseTerm.id,
      paymentTypeId: lease.paymentType.id,
      leaseStartDate: lease.leaseStartDate,
      leaseEndDate: lease.leaseEndDate,
      additionalInfo: lease.additionalInfo || ''
    });
    
    // 设置地区数据
    regionData.provinceId = lease.apartmentInfo.provinceId;
    regionData.cityId = lease.apartmentInfo.cityId;
    regionData.districtId = lease.apartmentInfo.districtId;
    
    // 设置显示名称
    selectedProvinceName.value = lease.apartmentInfo.provinceName;
    selectedCityName.value = lease.apartmentInfo.cityName;
    selectedDistrictName.value = lease.apartmentInfo.districtName;
    selectedApartmentName.value = lease.apartmentInfo.name;
    selectedRoomName.value = lease.roomInfo.roomNumber;
    selectedLeaseTermName.value = `${lease.leaseTerm.monthCount}${lease.leaseTerm.unit}`;
    selectedPaymentTypeName.value = lease.paymentType.name;
    
    // 直接设置公寓和房间选项，确保在重发模式下能正确显示
    apartmentOptions.value = [{
      text: lease.apartmentInfo.name,
      value: lease.apartmentInfo.id
    }];
    
    roomOptions.value = [{
      text: lease.roomInfo.roomNumber,
      value: lease.roomInfo.id
    }];
    
    // 同时设置省市区选项，确保联动选择器有数据
    provinceOptions.value = [{
      text: lease.apartmentInfo.provinceName,
      value: lease.apartmentInfo.provinceId
    }];
    
    cityOptions.value = [{
      text: lease.apartmentInfo.cityName,
      value: lease.apartmentInfo.cityId
    }];
    
    districtOptions.value = [{
      text: lease.apartmentInfo.districtName,
      value: lease.apartmentInfo.districtId
    }];
    
    console.log('✅ 直接设置所有选择器选项');
    console.log('省份选项:', provinceOptions.value);
    console.log('城市选项:', cityOptions.value);
    console.log('区县选项:', districtOptions.value);
    console.log('公寓选项:', apartmentOptions.value);
    console.log('房间选项:', roomOptions.value);
    
    // 根据地区数据加载对应的选择器选项
    await loadCitiesByProvince(regionData.provinceId);
    await loadDistrictsByCity(regionData.cityId);
    await loadApartmentsByDistrict(regionData.districtId);
    
    // 确保公寓选择器有正确的选项后，再加载房间
    // 等待一个tick确保选择器选项已经更新
    await new Promise(resolve => setTimeout(resolve, 100));
    
    // 如果公寓选择器中没有找到对应的公寓，手动添加
    const apartmentExists = apartmentOptions.value.some(apt => apt.value === formData.apartmentId);
    if (!apartmentExists && lease.apartmentInfo) {
      apartmentOptions.value.push({
        text: lease.apartmentInfo.name,
        value: lease.apartmentInfo.id
      });
      console.log('✅ 手动添加公寓选项:', lease.apartmentInfo.name);
    }
    
    // 加载房间数据
    await loadRoomsByApartment(formData.apartmentId);
    
    // 如果房间选择器中没有找到对应的房间，手动添加
    const roomExists = roomOptions.value.some(room => room.value === formData.roomId);
    if (!roomExists && lease.roomInfo) {
      roomOptions.value.push({
        text: lease.roomInfo.roomNumber,
        value: lease.roomInfo.id
      });
      console.log('✅ 手动添加房间选项:', lease.roomInfo.roomNumber);
    }
    
    // 强制更新显示名称（确保在选择器选项更新后）
    selectedApartmentName.value = lease.apartmentInfo.name;
    selectedRoomName.value = lease.roomInfo.roomNumber;
    
    console.log('✅ 原租约数据加载成功');
    console.log('公寓选项:', apartmentOptions.value);
    console.log('房间选项:', roomOptions.value);
    console.log('当前选中的公寓:', selectedApartmentName.value);
    console.log('当前选中的房间:', selectedRoomName.value);
  } catch (error) {
    console.error('加载原租约数据失败:', error);
    showToast('加载原租约数据失败');
  }
};

// 比较原数据和新数据，生成修改记录
const generateChangeLog = (): string => {
  if (!originalLeaseData.value) return '';
  
  const changes: string[] = [];
  const original = originalLeaseData.value;
  
  // 比较各个字段
  if (formData.name !== original.name) {
    changes.push(`租客姓名从"${original.name}"改为"${formData.name}"`);
  }
  
  if (formData.phone !== original.phone) {
    changes.push(`手机号从"${original.phone}"改为"${formData.phone}"`);
  }
  
  if (formData.identificationNumber !== original.identificationNumber) {
    changes.push(`身份证号从"${original.identificationNumber}"改为"${formData.identificationNumber}"`);
  }
  
  if (formData.apartmentId !== original.apartmentInfo.id) {
    changes.push(`公寓从"${original.apartmentInfo.name}"改为"${selectedApartmentName.value}"`);
  }
  
  if (formData.roomId !== original.roomInfo.id) {
    changes.push(`房间从"${original.roomInfo.roomNumber}"改为"${selectedRoomName.value}"`);
  }
  
  if (Number(formData.rent) !== Number(original.rent)) {
    changes.push(`月租金从"${original.rent}元"改为"${formData.rent}元"`);
  }
  
  if (Number(formData.deposit) !== Number(original.deposit)) {
    changes.push(`押金从"${original.deposit}元"改为"${formData.deposit}元"`);
  }
  
  if (formData.leaseTermId !== original.leaseTerm.id) {
    changes.push(`租期从"${original.leaseTerm.monthCount}${original.leaseTerm.unit}"改为"${selectedLeaseTermName.value}"`);
  }
  
  if (formData.paymentTypeId !== original.paymentType.id) {
    changes.push(`支付方式从"${original.paymentType.name}"改为"${selectedPaymentTypeName.value}"`);
  }
  
  if (formData.leaseStartDate !== original.leaseStartDate) {
    changes.push(`开始时间从"${original.leaseStartDate}"改为"${formData.leaseStartDate}"`);
  }
  
  if (formData.leaseEndDate !== original.leaseEndDate) {
    changes.push(`结束时间从"${original.leaseEndDate}"改为"${formData.leaseEndDate}"`);
  }
  
  if ((formData.additionalInfo || '') !== (original.additionalInfo || '')) {
    changes.push(`备注信息已修改`);
  }
  
  return changes.length > 0 ? `，房东修改了${changes.join('、')}` : '';
};

// 加载预约数据（从预约页面跳转过来时）
const loadAppointmentData = async () => {
  try {
    console.log('🔄 开始加载预约数据...');
    console.log('路由查询参数:', route.query);
    
    // 从路由参数中获取预约信息
    const { name, phone, apartmentId, roomId, appointmentId } = route.query;
    
    if (name) {
      formData.name = String(name);
      console.log('✅ 设置租客姓名:', formData.name);
    }
    
    if (phone) {
      formData.phone = String(phone);
      console.log('✅ 设置租客手机号:', formData.phone);
    }
    
    if (apartmentId && roomId) {
      const apartmentIdNum = Number(apartmentId);
      const roomIdNum = Number(roomId);
      
      console.log('🔄 开始加载预约相关的房源信息...');
      console.log('公寓ID:', apartmentIdNum, '房间ID:', roomIdNum);
      
      // 设置房源信息
      formData.apartmentId = apartmentIdNum;
      formData.roomId = roomIdNum;
      
      // 获取房间信息来确定地区信息
      try {
        const roomResponse = await getAdminRoomsByApartmentId(apartmentIdNum);
        const targetRoom = roomResponse.data.find((room: AdminRoomInfo) => room.id === roomIdNum);
        
        if (targetRoom) {
          selectedRoomName.value = targetRoom.roomNumber;
          console.log('✅ 设置房间名称:', selectedRoomName.value);
          
          // 获取公寓信息 - 需要通过区县ID来获取公寓列表
          // 先尝试通过现有的API获取公寓信息
          try {
            // 由于我们需要获取特定公寓的详细信息，这里需要优化
            // 暂时设置基本信息，后续可以通过专门的API获取完整信息
            selectedApartmentName.value = `公寓${apartmentIdNum}`;
            
            // 加载房间选项
            await loadRoomsByApartment(apartmentIdNum);
            
            // 确保房间选择器选项中包含当前选中的房间
            if (!roomOptions.value.some(room => room.value === roomIdNum)) {
              roomOptions.value.push({
                text: targetRoom.roomNumber,
                value: roomIdNum
              });
            }
          } catch (apartmentError) {
            console.error('获取公寓信息失败:', apartmentError);
            // 设置基本信息
            selectedApartmentName.value = `公寓${apartmentIdNum}`;
            selectedRoomName.value = targetRoom.roomNumber;
          }
        }
      } catch (error) {
        console.error('获取房源信息失败:', error);
        showToast('获取房源信息失败，请手动选择');
      }
    }
    
    // 设置默认的附加信息
    if (appointmentId) {
      formData.additionalInfo = `基于预约ID:${appointmentId} 创建的租约`;
      console.log('✅ 设置附加信息:', formData.additionalInfo);
    }
    
    // 显示成功提示
    showToast('已自动填充预约信息，请完善其他租约详情');
    console.log('✅ 预约数据加载完成');
    
  } catch (error) {
    console.error('加载预约数据失败:', error);
    showToast('加载预约数据失败，请手动填写');
  }
};

const loadEditData = async () => {
  if (!isEdit.value) return;
  
  try {
    const response = await getLandlordLeaseDetail(leaseId.value);
    const lease = response.data;
    
    // 填充表单数据
    Object.assign(formData, {
      name: lease.name,
      phone: lease.phone,
      identificationNumber: lease.identificationNumber,
      apartmentId: lease.apartmentInfo.id,
      roomId: lease.roomInfo.id,
      rent: lease.rent,
      deposit: lease.deposit,
      leaseTermId: lease.leaseTerm.id,
      paymentTypeId: lease.paymentType.id,
      leaseStartDate: lease.leaseStartDate,
      leaseEndDate: lease.leaseEndDate,
      additionalInfo: lease.additionalInfo || ''
    });
    
    // 设置显示名称
    selectedApartmentName.value = lease.apartmentInfo.name;
    selectedRoomName.value = lease.roomInfo.roomNumber;
    selectedLeaseTermName.value = `${lease.leaseTerm.monthCount}${lease.leaseTerm.unit}`;
    selectedPaymentTypeName.value = lease.paymentType.name;
  } catch (error) {
    console.error('加载租约数据失败:', error);
    showToast('加载数据失败');
  }
};

// 加载选择器数据
const loadPickerData = async () => {
  try {
    // 加载租期选项
    console.log('🔄 开始加载租期数据...');
    const leaseTermResponse = await getLeaseTerms();
    leaseTermOptions.value = leaseTermResponse.data.map((term: LeaseTerm) => ({
      text: `${term.monthCount}${term.unit}`,
      value: term.id!,
      monthCount: term.monthCount
    }));
    console.log('✅ 租期数据加载完成 - 共', leaseTermOptions.value.length, '个租期');
    
    // 加载支付方式选项
    console.log('🔄 开始加载支付方式数据...');
    const paymentTypeResponse = await getPaymentTypes();
    paymentTypeOptions.value = paymentTypeResponse.data.map((payment: PaymentType) => ({
      text: payment.name,
      value: payment.id!
    }));
    console.log('✅ 支付方式数据加载完成 - 共', paymentTypeOptions.value.length, '个支付方式');
    
    console.log('✅ 选择器数据加载完成');
  } catch (error) {
    console.error('加载选择器数据失败:', error);
    showToast('加载数据失败');
    // 使用备用数据
    leaseTermOptions.value = [
      { text: '3个月', value: 1, monthCount: 3 },
      { text: '6个月', value: 2, monthCount: 6 },
      { text: '12个月', value: 3, monthCount: 12 },
      { text: '24个月', value: 4, monthCount: 24 }
    ];
    
    paymentTypeOptions.value = [
      { text: '月付', value: 1 },
      { text: '季付', value: 2 },
      { text: '半年付', value: 3 },
      { text: '年付', value: 4 }
    ];
  }
};

// 选择器确认事件
const onProvinceConfirm = ({ selectedValues, selectedOptions }: any) => {
  regionData.provinceId = selectedValues[0];
  selectedProvinceName.value = selectedOptions[0]?.text || '';
  showProvincePicker.value = false;
  
  // 清空下级选择
  regionData.cityId = 0;
  regionData.districtId = 0;
  formData.apartmentId = 0;
  formData.roomId = 0;
  selectedCityName.value = '';
  selectedDistrictName.value = '';
  selectedApartmentName.value = '';
  selectedRoomName.value = '';
  
  // 加载对应省份的城市
  loadCitiesByProvince(regionData.provinceId);
};

const onCityConfirm = ({ selectedValues, selectedOptions }: any) => {
  regionData.cityId = selectedValues[0];
  selectedCityName.value = selectedOptions[0]?.text || '';
  showCityPicker.value = false;
  
  // 清空下级选择
  regionData.districtId = 0;
  formData.apartmentId = 0;
  formData.roomId = 0;
  selectedDistrictName.value = '';
  selectedApartmentName.value = '';
  selectedRoomName.value = '';
  
  // 加载对应城市的区县
  loadDistrictsByCity(regionData.cityId);
};

const onDistrictConfirm = ({ selectedValues, selectedOptions }: any) => {
  regionData.districtId = selectedValues[0];
  selectedDistrictName.value = selectedOptions[0]?.text || '';
  showDistrictPicker.value = false;
  
  // 清空下级选择
  formData.apartmentId = 0;
  formData.roomId = 0;
  selectedApartmentName.value = '';
  selectedRoomName.value = '';
  
  // 加载对应区县的公寓
  loadApartmentsByDistrict(regionData.districtId);
};

const onApartmentConfirm = ({ selectedValues, selectedOptions }: any) => {
  formData.apartmentId = selectedValues[0];
  selectedApartmentName.value = selectedOptions[0]?.text || '';
  showApartmentPicker.value = false;
  
  // 清空房间选择
  formData.roomId = 0;
  selectedRoomName.value = '';
  
  // 加载对应公寓的房间
  loadRoomsByApartment(formData.apartmentId);
};

const onRoomConfirm = ({ selectedValues, selectedOptions }: any) => {
  formData.roomId = selectedValues[0];
  selectedRoomName.value = selectedOptions[0]?.text || '';
  showRoomPicker.value = false;
};

const onLeaseTermConfirm = ({ selectedValues, selectedOptions }: any) => {
  formData.leaseTermId = selectedValues[0];
  selectedLeaseTermName.value = selectedOptions[0]?.text || '';
  showLeaseTermPicker.value = false;
};

const onPaymentTypeConfirm = ({ selectedValues, selectedOptions }: any) => {
  formData.paymentTypeId = selectedValues[0];
  selectedPaymentTypeName.value = selectedOptions[0]?.text || '';
  showPaymentTypePicker.value = false;
};

const onStartDateConfirm = ({ selectedValues }: any) => {
  // van-date-picker 的 confirm 事件返回 { selectedValues: [year, month, day] }
  if (!selectedValues || selectedValues.length < 3) {
    showToast('请选择有效日期');
    return;
  }
  
  const [year, month, day] = selectedValues;
  
  // 格式化日期为 YYYY-MM-DD 格式
  const formattedMonth = String(month).padStart(2, '0');
  const formattedDay = String(day).padStart(2, '0');
  formData.leaseStartDate = `${year}-${formattedMonth}-${formattedDay}`;
  
  showStartDatePicker.value = false;
  
  console.log('选择的开始日期:', formData.leaseStartDate);
};

// 加载房间选项
const loadRoomsByApartment = async (apartmentId: number) => {
  try {
    console.log('🔄 开始加载房间数据 - apartmentId:', apartmentId);
    const response = await getAdminRoomsByApartmentId(apartmentId);
    roomOptions.value = response.data.map((room: AdminRoomInfo) => ({
      text: room.roomNumber,
      value: room.id
    }));
    console.log('✅ 房间数据加载完成 - apartmentId:', apartmentId, '共', roomOptions.value.length, '个房间');
  } catch (error) {
    console.error('加载房间数据失败:', error);
    showToast('加载房间数据失败');
    // 使用备用数据
    roomOptions.value = [
      { text: '101', value: 1 },
      { text: '102', value: 2 },
      { text: '201', value: 3 },
      { text: '202', value: 4 }
    ];
  }
};

// 加载省份数据
const loadProvinces = async () => {
  try {
    console.log('🔄 开始加载省份数据...');
    const response = await getAdminProvinceList();
    provinceOptions.value = response.data.map((province: AdminProvinceInfo) => ({
      text: province.name,
      value: province.id
    }));
    console.log('✅ 省份数据加载完成 - 共', provinceOptions.value.length, '个省份');
  } catch (error) {
    console.error('加载省份数据失败:', error);
    showToast('加载省份数据失败');
    // 使用备用数据
    provinceOptions.value = [
      { text: '北京市', value: 11 },
      { text: '上海市', value: 31 },
      { text: '广东省', value: 44 },
      { text: '浙江省', value: 33 },
      { text: '江苏省', value: 32 }
    ];
  }
};

// 根据省份加载城市
const loadCitiesByProvince = async (provinceId: number) => {
  try {
    console.log('🔄 开始加载城市数据 - provinceId:', provinceId);
    const response = await getAdminCityListByProvinceId(provinceId);
    cityOptions.value = response.data.map((city: AdminCityInfo) => ({
      text: city.name,
      value: city.id
    }));
    console.log('✅ 城市数据加载完成 - provinceId:', provinceId, '共', cityOptions.value.length, '个城市');
  } catch (error) {
    console.error('加载城市数据失败:', error);
    showToast('加载城市数据失败');
    // 清空城市选项
    cityOptions.value = [];
  }
};

// 根据城市加载区县
const loadDistrictsByCity = async (cityId: number) => {
  try {
    console.log('🔄 开始加载区县数据 - cityId:', cityId);
    const response = await getAdminDistrictListByCityId(cityId);
    districtOptions.value = response.data.map((district: AdminDistrictInfo) => ({
      text: district.name,
      value: district.id
    }));
    console.log('✅ 区县数据加载完成 - cityId:', cityId, '共', districtOptions.value.length, '个区县');
  } catch (error) {
    console.error('加载区县数据失败:', error);
    showToast('加载区县数据失败');
    // 清空区县选项
    districtOptions.value = [];
  }
};

// 获取当前登录房东ID
const getCurrentLandlordId = async (): Promise<number> => {
  try {
    const userInfo = await getUserInfo();
    console.log('当前用户信息:', userInfo.data);
    // 由于UserInfoInterface中没有id字段，暂时使用固定值
    // 实际应用中需要后端在用户信息中返回用户ID
    return 1; // 临时使用固定值
  } catch (error) {
    console.error('获取用户信息失败:', error);
    return 1;
  }
};

// 根据区县加载公寓
const loadApartmentsByDistrict = async (districtId: number) => {
  try {
    console.log('🔄 开始加载公寓数据 - districtId:', districtId);
    
    const response = await getAdminApartmentsByDistrictId(districtId);
    console.log('公寓API响应:', response);
    
    if (response.data) {
      apartmentOptions.value = response.data.map((apartment: AdminApartmentInfo) => ({
        text: apartment.name,
        value: apartment.id
      }));
      console.log('✅ 公寓数据加载完成 - districtId:', districtId, '共', apartmentOptions.value.length, '个公寓');
      console.log('公寓选项:', apartmentOptions.value);
    } else {
      console.warn('公寓API返回数据格式异常:', response);
      apartmentOptions.value = [];
    }
    
  } catch (error) {
    console.error('加载公寓数据失败:', error);
    showToast('加载公寓数据失败');
    // 使用备用数据
    apartmentOptions.value = [
      { text: '阳光公寓', value: 1 },
      { text: '绿城花园', value: 2 },
      { text: '万科城', value: 3 }
    ];
  }
};

// 获取提交按钮文本
const getSubmitButtonText = () => {
  if (isResendMode.value) {
    return '重发租约';
  } else if (isEdit.value) {
    return '更新租约';
  } else {
    return '创建租约';
  }
};

// 表单提交
const onSubmit = async () => {
  try {
    submitting.value = true;
    
    if (isResendMode.value) {
      // 重发租约模式
      const changeLog = generateChangeLog();
      const additionalInfo = `基于租约ID:${originalLeaseId.value} 重新发送${changeLog}`;
      
      // 创建重发租约的数据
      const resendData = {
        ...formData,
        additionalInfo
      };
      
      console.log('🔄 重发租约数据:', resendData);
      console.log('📝 修改记录:', changeLog);
      
      // 调用重发租约接口
      const { data: newLeaseId } = await resendLease(originalLeaseId.value!, resendData);
      
      showToast('租约重发成功');
      console.log('✅ 租约重发成功，新租约ID:', newLeaseId);
      
      // 跳转到新租约详情页面
      router.push(`/landlord/leases/detail/${newLeaseId}`);
      
    } else if (isEdit.value) {
      await updateLease(leaseId.value, formData);
      showToast('租约更新成功');
      NavigationHelper.smartGoBack(router);
    } else {
      await createLease(formData);
      showToast('租约创建成功');
      NavigationHelper.smartGoBack(router);
    }
    
  } catch (error) {
    console.error('提交失败:', error);
    showToast('提交失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 返回处理
const handleBack = async () => {
  // 检查是否有未保存的更改
  const hasChanges = Object.values(formData).some(value => value !== '' && value !== 0);
  
  if (hasChanges) {
    try {
      await showConfirmDialog({
        title: '确认离开',
        message: '您有未保存的更改，确定要离开吗？',
      });
      NavigationHelper.smartGoBack(router);
    } catch {
      // 用户取消
    }
  } else {
    NavigationHelper.smartGoBack(router);
  }
};

// 组件挂载时初始化
onMounted(async () => {
  // 加载基础数据
  await loadPickerData();
  await loadProvinces(); // 初始化省份数据
  
  if (isEdit.value) {
    await loadEditData();
  } else if (isResendMode.value) {
    await loadResendData();
  } else if (route.query.source === 'appointment') {
    // 处理从预约页面跳转过来的情况
    await loadAppointmentData();
  }
});
</script>

<style scoped>
.lease-form {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.form-content {
  padding: 16px;
}

/* 表单区域 */
.form-section {
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #323233;
  padding: 16px;
  background: #f7f8fa;
  border-bottom: 1px solid #ebedf0;
}

.section-title .van-icon {
  color: #1989fa;
}

.unit {
  font-size: 14px;
  color: #969799;
}

/* 提交区域 */
.submit-section {
  padding: 16px;
  background: white;
  border-top: 1px solid #ebedf0;
}

/* 表单字段样式调整 */
:deep(.van-field__label) {
  width: 80px;
  color: #646566;
}

:deep(.van-field__control) {
  color: #323233;
}

:deep(.van-field--disabled .van-field__control) {
  color: #c8c9cc;
}

/* 响应式适配 */
@media (max-width: 375px) {
  .form-content {
    padding: 12px;
  }
  
  .section-title {
    font-size: 15px;
    padding: 12px;
  }
  
  :deep(.van-field__label) {
    width: 70px;
    font-size: 14px;
  }
}
</style>