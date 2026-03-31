<template>
  <div class="page-container min-h-[100vh] py-[15px]">
    <van-form @submit="onSubmit" ref="formRef">
      <!-- 房源信息 -->
      <div class="card mb-4">
        <div class="base-info-title py-[4px]">申请房源</div>
        <div class="my-[5px]">
          <van-card class="rounded-xl shadow">
            <template #title>
              <h2 class="text-[16px] font-bold mt-[25px] ml-[25px]">
                {{ `${roomInfo.apartmentName} ${roomInfo.roomNumber}房间` }}
              </h2>
            </template>
            <template #desc>
              <div class="ml-[25px] mb-[15px]">
                <div class="text-red-500 text-[18px] font-bold">
                  ￥{{ roomInfo.rent }}/月
                </div>
              </div>
            </template>
            <template #thumb>
              <van-image
                class="w-full h-full object-cover"
                :src="roomInfo.graphUrl || ''"
              >
                <template v-slot:error>加载失败</template>
                <template v-slot:loading>
                  <van-loading type="spinner" size="20" />
                </template>
              </van-image>
            </template>
          </van-card>
        </div>
      </div>

      <!-- 个人信息 -->
      <div class="card mb-4">
        <div class="base-info-title py-[4px]">个人信息</div>
        <div class="my-[5px]">
          <van-field
            v-model="formData.name"
            name="name"
            label="姓名"
            placeholder="请输入真实姓名"
            :rules="[{ required: true, message: '请输入姓名' }]"
          />
          <van-field
            v-model="formData.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
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
              { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入正确的身份证号' }
            ]"
          />
          <van-field
            v-model="formData.email"
            name="email"
            label="邮箱"
            placeholder="请输入邮箱地址（选填）"
            :rules="[
              { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '请输入正确的邮箱格式' }
            ]"
          />
        </div>
      </div>

      <!-- 紧急联系人 -->
      <div class="card mb-4">
        <div class="base-info-title py-[4px]">紧急联系人</div>
        <div class="my-[5px]">
          <van-field
            v-model="formData.emergencyContact"
            name="emergencyContact"
            label="联系人姓名"
            placeholder="请输入紧急联系人姓名"
            :rules="[{ required: true, message: '请输入紧急联系人姓名' }]"
          />
          <van-field
            v-model="formData.emergencyPhone"
            name="emergencyPhone"
            label="联系人电话"
            placeholder="请输入紧急联系人电话"
            :rules="[
              { required: true, message: '请输入紧急联系人电话' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
            ]"
          />
        </div>
      </div>

      <!-- 租期信息 -->
      <div class="card mb-4">
        <div class="base-info-title py-[4px]">租期信息</div>
        <div class="my-[5px]">
          <!-- 租期选择 -->
          <van-field
            v-model="leaseTermInfo.text"
            is-link
            readonly
            name="leaseTerm"
            label="租期"
            placeholder="请选择租期"
            @click="showPickerLeaseTerm = true"
            :rules="[{ required: true, message: '请选择租期' }]"
          />
          <van-popup v-model:show="showPickerLeaseTerm" position="bottom">
            <van-picker
              title="选择租期"
              :columns="leaseTermColumns"
              @confirm="onConfirmLeaseTerm"
              @cancel="showPickerLeaseTerm = false"
            />
          </van-popup>

          <!-- 入住日期 -->
          <van-field
            v-model="leaseStartDateText"
            is-link
            readonly
            name="leaseStartDate"
            label="入住日期"
            placeholder="请选择入住日期"
            @click="showDatePicker = true"
            :rules="[{ required: true, message: '请选择入住日期' }]"
          />
          <van-popup v-model:show="showDatePicker" position="bottom">
            <van-date-picker
              title="选择入住日期"
              :min-date="minDate"
              :max-date="maxDate"
              @confirm="onConfirmDate"
              @cancel="showDatePicker = false"
            />
          </van-popup>

          <!-- 租期结束日期（自动计算） -->
          <van-field
            v-model="leaseEndDateText"
            readonly
            label="租期结束"
            placeholder="根据租期自动计算"
          />

          <!-- 支付方式 -->
          <van-field
            v-model="paymentTypeInfo.text"
            is-link
            readonly
            name="paymentType"
            label="支付方式"
            placeholder="请选择支付方式"
            @click="showPickerPaymentType = true"
            :rules="[{ required: true, message: '请选择支付方式' }]"
          />
          <van-popup v-model:show="showPickerPaymentType" position="bottom">
            <van-picker
              title="选择支付方式"
              :columns="paymentTypeColumns"
              @confirm="onConfirmPaymentType"
              @cancel="showPickerPaymentType = false"
            />
          </van-popup>
        </div>
      </div>

      <!-- 费用信息 -->
      <div class="card mb-4">
        <div class="base-info-title py-[4px]">费用信息</div>
        <div class="my-[5px]">
          <van-field
            v-model="rentText"
            readonly
            label="月租金"
            placeholder="根据房源自动填充"
          />
          <van-field
            v-model="depositText"
            readonly
            label="押金"
            placeholder="根据房源自动填充"
          />
        </div>
      </div>

      <!-- 备注信息 -->
      <div class="card mb-4">
        <div class="base-info-title py-[4px]">备注信息</div>
        <div class="my-[5px]">
          <van-field
            v-model="formData.additionalInfo"
            type="textarea"
            label="备注"
            placeholder="请输入其他需要说明的信息（选填）"
            rows="3"
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="main-container py-[20px]">
        <van-button
          type="primary"
          block
          round
          size="large"
          native-type="submit"
          :loading="submitting"
          class="submit-button"
        >
          提交申请
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts" name="TenantDirectMoveIn">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { showToast, showConfirmDialog } from "vant";
import dayjs from "dayjs";
import { getRoomDetailById, getTermListByRoomId, getPaymentListByRoomId } from "@/api/tenant/search";
import { submitDirectMoveInApplication, validateRoomAvailability } from "@/api/tenant/directMoveIn";
import type { RoomDetailInterface } from "@/api/tenant/search/types";
import { useUserStore } from "@/store/modules/user";

const route = useRoute();
const router = useRouter();
const formRef = ref();
const userStore = useUserStore();

// 房源信息
const roomInfo = ref({
  id: '',
  apartmentId: '',
  apartmentName: '',
  roomNumber: '',
  rent: 0,
  deposit: 0,
  graphUrl: ''
});

// 表单数据
const formData = ref({
  userId: null,
  roomId: '',
  apartmentId: '',
  name: '',
  phone: '',
  identificationNumber: '',
  email: '',
  emergencyContact: '',
  emergencyPhone: '',
  leaseStartDate: null,
  leaseEndDate: null,
  leaseTermId: '',
  rent: 0,
  deposit: 0,
  paymentTypeId: '',
  additionalInfo: ''
});

// 提交状态
const submitting = ref(false);

// 租期相关
const leaseTermInfo = ref({
  text: '',
  value: '',
  monthCount: 0
});
const leaseTermList = ref([]);
const showPickerLeaseTerm = ref(false);
const leaseTermColumns = computed(() => {
  return leaseTermList.value.map(item => ({
    text: item.monthCount + item.unit,
    value: item.id,
    monthCount: item.monthCount
  }));
});

// 支付方式相关
const paymentTypeInfo = ref({
  text: '',
  value: ''
});
const paymentTypeList = ref([]);
const showPickerPaymentType = ref(false);
const paymentTypeColumns = computed(() => {
  return paymentTypeList.value.map(item => ({
    text: item.name,
    value: item.id
  }));
});

// 日期相关
const showDatePicker = ref(false);
const minDate = new Date();
const maxDate = new Date(new Date().getFullYear() + 1, 11, 31);

// 计算属性
const leaseStartDateText = computed(() => {
  return formData.value.leaseStartDate 
    ? dayjs(formData.value.leaseStartDate).format('YYYY-MM-DD')
    : '';
});

const leaseEndDateText = computed(() => {
  return formData.value.leaseEndDate 
    ? dayjs(formData.value.leaseEndDate).format('YYYY-MM-DD')
    : '';
});

const rentText = computed(() => {
  return formData.value.rent ? `￥${formData.value.rent}/月` : '';
});

const depositText = computed(() => {
  return formData.value.deposit ? `￥${formData.value.deposit}` : '';
});

// 监听租期变化，自动计算结束日期
watch([() => formData.value.leaseStartDate, () => leaseTermInfo.value.monthCount], () => {
  if (formData.value.leaseStartDate && leaseTermInfo.value.monthCount) {
    formData.value.leaseEndDate = dayjs(formData.value.leaseStartDate)
      .add(leaseTermInfo.value.monthCount, 'month')
      .toDate();
  }
});

// 确认选择租期
const onConfirmLeaseTerm = (data) => {
  const selected = data.selectedOptions[0];
  leaseTermInfo.value = {
    text: selected.text,
    value: selected.value,
    monthCount: selected.monthCount
  };
  formData.value.leaseTermId = selected.value;
  showPickerLeaseTerm.value = false;
};

// 确认选择支付方式
const onConfirmPaymentType = (data) => {
  const selected = data.selectedOptions[0];
  paymentTypeInfo.value = {
    text: selected.text,
    value: selected.value
  };
  formData.value.paymentTypeId = selected.value;
  showPickerPaymentType.value = false;
};

// 确认选择日期
const onConfirmDate = (data) => {
  formData.value.leaseStartDate = data.selectedValues.join('-');
  showDatePicker.value = false;
};

// 获取房源信息
const getRoomInfo = async () => {
  try {
    const { data } = await getRoomDetailById(route.query.roomId as string);
    roomInfo.value = {
      id: data.id,
      apartmentId: data.apartmentId,
      apartmentName: data.apartmentItemVo?.name || '',
      roomNumber: data.roomNumber,
      rent: data.rent,
      deposit: data.deposit || data.rent, // 如果没有押金信息，默认为一个月租金
      graphUrl: data.graphVoList?.[0]?.url || ''
    };
    
    // 设置表单数据
    formData.value.roomId = roomInfo.value.id;
    formData.value.apartmentId = roomInfo.value.apartmentId;
    formData.value.rent = roomInfo.value.rent;
    formData.value.deposit = roomInfo.value.deposit;
  } catch (error) {
    console.error('获取房源信息失败:', error);
    showToast('获取房源信息失败');
  }
};

// 获取租期列表
const getLeaseTermList = async () => {
  try {
    const { data } = await getTermListByRoomId(formData.value.roomId);
    leaseTermList.value = data;
  } catch (error) {
    console.error('获取租期列表失败:', error);
    // 使用默认租期列表
    leaseTermList.value = [
      { id: '1', monthCount: 1, unit: '月' },
      { id: '2', monthCount: 3, unit: '月' },
      { id: '3', monthCount: 6, unit: '月' },
      { id: '4', monthCount: 12, unit: '月' }
    ];
  }
};

// 获取支付方式列表
const getPaymentTypeList = async () => {
  try {
    const { data } = await getPaymentListByRoomId(formData.value.roomId);
    paymentTypeList.value = data;
  } catch (error) {
    console.error('获取支付方式列表失败:', error);
    // 使用默认支付方式列表
    paymentTypeList.value = [
      { id: '1', name: '月付' },
      { id: '2', name: '季付' },
      { id: '3', name: '半年付' },
      { id: '4', name: '年付' }
    ];
  }
};

// 提交申请
const onSubmit = async () => {
  try {
    await showConfirmDialog({
      title: '确认提交',
      message: '请确认申请信息无误，提交后将等待审核。'
    });

    submitting.value = true;

    // 准备提交数据
    const submitData = {
      ...formData.value,
      leaseStartDate: formData.value.leaseStartDate ? dayjs(formData.value.leaseStartDate).format('YYYY-MM-DD') : null,
      leaseEndDate: formData.value.leaseEndDate ? dayjs(formData.value.leaseEndDate).format('YYYY-MM-DD') : null
    };

    // 调用API提交申请
    await submitDirectMoveInApplication(submitData);
    
    showToast({
      type: 'success',
      message: '申请提交成功，请等待审核'
    });

    // 返回上一页或跳转到申请列表
    router.back();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交申请失败:', error);
      showToast('提交申请失败，请重试');
    }
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  // 设置用户ID
  if (userStore.userInfo?.id) {
    formData.value.userId = userStore.userInfo.id;
  }
  
  // 自动填充用户信息（如果已有）
  if (userStore.userInfo) {
    formData.value.name = userStore.userInfo.nickname || '';
    formData.value.phone = userStore.userInfo.phone || '';
  }
  
  await getRoomInfo();
  await getLeaseTermList();
  await getPaymentTypeList();
});
</script>

<style scoped lang="less">
.base-info-title {
  font-weight: bold;
  color: #323233;
}

.submit-button {
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

::v-deep .van-card {
  background: var(--van-background-2) !important;
}
</style>