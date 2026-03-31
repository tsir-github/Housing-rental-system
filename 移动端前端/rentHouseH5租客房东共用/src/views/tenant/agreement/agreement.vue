<template>
  <van-skeleton :row="20" :loading="!agreementDetailInfo?.roomId">
    <div class="page-container min-h-[100vh] py-[15px]">
      <!--    租约到期倒计时提醒-->
      <div v-if="showExpirationCountdown" class="main-container mb-[15px]">
        <van-notice-bar
          :left-icon="expirationInfo.icon"
          :color="expirationInfo.color"
          :background="expirationInfo.background"
          :text="expirationInfo.text"
          :scrollable="false"
        />
      </div>
      <!--    续约提示横幅-->
      <div v-if="isRenew" class="main-container mb-[15px]">
        <van-notice-bar
          left-icon="info-o"
          color="#1989fa"
          background="#ecf9ff"
          :text="renewalNoticeText"
        />
      </div>
      <!--    签约公寓-->
      <div>
        <div class="base-info-title main-container py-[4px]">签约公寓</div>
        <div class="my-[5px] px-[20px]">
          <van-card @click="goApartmentDetail()" class="rounded-xl shadow">
            <!--      title-->
            <template #title>
              <h2 class="text-[16px] font-bold mt-[25px] ml-[25px]">
                {{ `${agreementDetailInfo.apartmentName}` }}
              </h2>
            </template>
            <!--    thumb-->
            <template #thumb>
              <van-image
                class="w-full h-full object-cover"
                :src="
                  agreementDetailInfo.apartmentGraphVoList?.[0]?.url || '失败'
                "
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
      <!--    签约房间-->
      <div>
        <div class="base-info-title main-container py-[4px]">签约房间</div>
        <div class="my-[5px] px-[20px]">
          <van-card @click="goRoomDetail()" class="rounded-xl shadow">
            <!--      title-->
            <template #title>
              <h2 class="text-[16px] font-bold mt-[25px] ml-[25px]">
                {{ `${agreementDetailInfo.roomNumber}房间` }}
              </h2>
            </template>
            <!--    thumb-->
            <template #thumb>
              <van-image
                class="w-full h-full object-cover"
                :src="agreementDetailInfo.roomGraphVoList?.[0]?.url || '失败'"
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
      <!--    承租人信息-->
      <div>
        <div class="base-info-title main-container py-[4px]">签约房间</div>
        <div class="main-container my-[5px]">
          <div class="rounded-xl shadow overflow-hidden">
            <van-row>
              <van-col span="24">
                <van-field
                  readonly
                  label-width="70px"
                  label="姓名"
                  v-model="agreementDetailInfo.name"
                ></van-field>
              </van-col>
              <van-col span="24">
                <van-field
                  readonly
                  label-width="70px"
                  label="手机号"
                  v-model="agreementDetailInfo.phone"
                ></van-field>
              </van-col>
              <van-col span="24">
                <van-field
                  readonly
                  label-width="70px"
                  label="身份证号"
                  v-model="agreementDetailInfo.identificationNumber"
                ></van-field>
              </van-col>
            </van-row>
          </div>
        </div>
      </div>
      <!--    租约详情-->
      <div>
        <div class="base-info-title main-container py-[4px]">租约详情</div>
        <div class="main-container my-[5px]">
          <div class="rounded-xl shadow overflow-hidden">
            <van-row>
              <!--          原始租期（续约状态或续约预览时显示）-->
              <van-col v-if="(agreementDetailInfo.sourceType === 2 || isRenew) && originalLeaseDate" span="24">
                <van-field
                  readonly
                  label-width="90px"
                  label="原始租期"
                  v-model="originalLeaseDate"
                  class="original-lease-info"
                ></van-field>
              </van-col>
              <!--          租期-->
              <van-col span="24">
                <van-field
                  v-model="leaseTermInfo.text"
                  is-link
                  readonly
                  name="picker"
                  label-width="70px"
                  :label="isRenew ? '续约期限' : '租期'"
                  placeholder="点击选择租期"
                  :class="{ 'renewal-highlight': isRenew && isAllowEdit }"
                  @click="isAllowEdit && (showPickerLeaseTerm = true)"
                />
                <van-popup v-model:show="showPickerLeaseTerm" position="bottom">
                  <van-picker
                    title="租期"
                    :columns="leaseTermColumns"
                    @confirm="onConfirmLeaseTerm"
                    @cancel="showPickerLeaseTerm = false"
                  />
                </van-popup>
              </van-col>
              <!--          合同租期-->
              <van-col span="24">
                <van-field
                  readonly
                  label-width="90px"
                  :label="(agreementDetailInfo.sourceType === 2 || isRenew) ? '续约后租期' : '合同租期'"
                  v-model="leaseDate"
                  :class="{ 'renewal-highlight': agreementDetailInfo.sourceType === 2 || isRenew }"
                ></van-field>
              </van-col>
              <!--          身份证号-->
              <van-col span="24">
                <van-field
                  readonly
                  label-width="70px"
                  label="身份证号"
                  v-model="agreementDetailInfo.identificationNumber"
                ></van-field>
              </van-col>
              <!--          租金-->
              <van-col span="24">
                <van-field
                  readonly
                  label-width="70px"
                  label="租金"
                  v-model="rentText"
                ></van-field>
              </van-col>
              <!--          押金-->
              <van-col span="24">
                <van-field
                  readonly
                  label-width="70px"
                  label="押金(元)"
                  v-model="depositText"
                ></van-field>
              </van-col>
              <!--          支付方式-->
              <van-col span="24">
                <van-field
                  v-model="paymentTypeInfo.text"
                  is-link
                  readonly
                  name="picker"
                  label-width="70px"
                  label="支付方式"
                  placeholder="点击选择支付方式"
                  :class="{ 'renewal-highlight': isRenew && isAllowEdit }"
                  @click="isAllowEdit && (showPickerPaymentType = true)"
                />
                <van-popup
                  v-model:show="showPickerPaymentType"
                  position="bottom"
                >
                  <van-picker
                    title="支付方式"
                    :columns="paymentTypeColumns"
                    @confirm="onConfirmPaymentType"
                    @cancel="showPickerPaymentType = false"
                  />
                </van-popup>
              </van-col>
            </van-row>
          </div>
        </div>
      </div>
      <!--    按钮-->
      <div class="main-container py-[20px]">
        <van-button
          v-if="isAllowEdit && isRenew"
          type="primary"
          block
          round
          class="m-t-20"
          @click="submitHandle"
        >
          确认续约
        </van-button>
        <!--      确认签约-->
        <van-button
          v-if="isConfirmAgreement"
          type="primary"
          block
          round
          class="m-t-20"
          @click="submitHandle"
        >
          确认签约
        </van-button>
      </div>
    </div>
  </van-skeleton>
</template>
<script setup lang="ts" name="TenantAgreement">
import { computed, onMounted, ref, watch } from "vue";
import dayjs from "dayjs";
import type {
  AgreementDetailInterface,
  PaymentInfoInterface,
  TermInfoInterface
} from "@/api/tenant/search/types";
import { useRoute, useRouter } from "vue-router";
import { showToast } from "vant";
import {
  getAgreementDetailById,
  getPaymentListByRoomId,
  getTermListByRoomId,
  saveOrUpdateAgreement
} from "@/api/tenant/search";
import { AgreementSource, AgreementSourceMap, AgreementStatus } from "@/enums/constEnums";
const route = useRoute();
const router = useRouter();
// 是否允许修改
const isAllowEdit = ref(route.query.isEdit === "true");
// 是否是续约操作
const isRenew = ref(route.query.isRenew === "true");
// 是否确认签约
const isConfirmAgreement = ref(route.query.isConfirm === "true");

// 租约到期倒计时相关
const daysRemaining = ref(0);
const showExpirationCountdown = ref(false);

// 计算到期倒计时信息
const expirationInfo = computed(() => {
  if (daysRemaining.value < 0) {
    // 已过期
    return {
      icon: 'warning-o',
      color: '#ed6a0c',
      background: '#fff7e6',
      text: `⚠️ 您的租约已过期 ${Math.abs(daysRemaining.value)} 天，请尽快联系房东处理！`
    };
  } else if (daysRemaining.value === 0) {
    // 今天到期
    return {
      icon: 'warning-o',
      color: '#ee0a24',
      background: '#fff1f0',
      text: `🔴 您的租约今天到期，请及时续约或办理退租手续！`
    };
  } else if (daysRemaining.value <= 3) {
    // 3天内到期
    return {
      icon: 'warning-o',
      color: '#ee0a24',
      background: '#fff1f0',
      text: `🔴 紧急提醒：您的租约还有 ${daysRemaining.value} 天到期(${dayjs(agreementDetailInfo.value.leaseEndDate).format('YYYY-MM-DD')})，请尽快续约！`
    };
  } else if (daysRemaining.value <= 7) {
    // 7天内到期
    return {
      icon: 'warning-o',
      color: '#ff976a',
      background: '#fff7e6',
      text: `🟠 重要提醒：您的租约还有 ${daysRemaining.value} 天到期(${dayjs(agreementDetailInfo.value.leaseEndDate).format('YYYY-MM-DD')})，建议尽快续约！`
    };
  } else if (daysRemaining.value <= 30) {
    // 30天内到期
    return {
      icon: 'info-o',
      color: '#ed6a0c',
      background: '#fffbe8',
      text: `🟡 温馨提示：您的租约还有 ${daysRemaining.value} 天到期(${dayjs(agreementDetailInfo.value.leaseEndDate).format('YYYY-MM-DD')})，请提前规划续约事宜！`
    };
  } else {
    // 30天以上
    return {
      icon: 'info-o',
      color: '#1989fa',
      background: '#ecf9ff',
      text: `ℹ️ 您的租约还有 ${daysRemaining.value} 天到期(${dayjs(agreementDetailInfo.value.leaseEndDate).format('YYYY-MM-DD')})`
    };
  }
});

// 计算剩余天数
const calculateDaysRemaining = () => {
  if (!agreementDetailInfo.value.leaseEndDate) {
    showExpirationCountdown.value = false;
    return;
  }
  
  const endDate = dayjs(agreementDetailInfo.value.leaseEndDate);
  const now = dayjs();
  const days = endDate.diff(now, 'day');
  
  daysRemaining.value = days;
  
  // 只有已签约状态才显示倒计时
  // AgreementStatus.SIGNED = 2
  showExpirationCountdown.value = agreementDetailInfo.value.status === 2;
};

// 原始租约信息（用于续约对比）
const originalLeaseInfo = ref({
  startDate: "",
  endDate: "",
  termId: "",
  paymentTypeId: ""
});

// 续约预览的结束日期
const previewEndDate = ref("");

// 续约提示横幅的文本
const renewalNoticeText = computed(() => {
  if (!isRenew.value) return '';
  
  const changes = [];
  
  // 检查日期变化
  if (originalLeaseInfo.value.endDate && previewEndDate.value) {
    const originalEnd = dayjs(originalLeaseInfo.value.endDate).format('YYYY-MM-DD');
    const newEnd = dayjs(previewEndDate.value).format('YYYY-MM-DD');
    changes.push(`租期延长至: ${newEnd} (原到期日: ${originalEnd})`);
  }
  
  // 检查租期变化
  if (leaseTermInfo.value.text) {
    changes.push(`续约期限: ${leaseTermInfo.value.text}`);
  }
  
  // 检查支付方式变化
  if (paymentTypeInfo.value.text) {
    changes.push(`支付方式: ${paymentTypeInfo.value.text}`);
  }
  
  if (changes.length > 0) {
    return `🔄 续约申请预览 - ${changes.join(' | ')} - 提交后需等待房东审批`;
  }
  
  return '🔄 正在办理续约申请，请仔细核对信息后提交申请';
});
// 公寓的详情信息
const agreementDetailInfo = ref<Partial<AgreementDetailInterface>>({
  id: "",
  // 	承租人姓名
  name: "",
  // phone	承租人手机号码
  phone: "",
  // identificationNumber	承租人身份证号码
  identificationNumber: "",
  // apartmentId	签约公寓id
  apartmentId: "",
  // roomId	签约房间id
  roomId: "",
  // leaseStartDate	租约开始日期
  leaseStartDate: "",
  // leaseEndDate	租约结束日期
  leaseEndDate: "",
  // leaseTermId	租期id
  leaseTermId: "",
  // rent	租金（元/月）
  rent: "",
  // deposit	押金（元）
  deposit: "",
  // paymentTypeId	支付类型id
  paymentTypeId: "",
  // status	租约状态,可用值:1,2,3,4,5,6
  status: AgreementStatus.WAITING,
  // sourceType	租约来源,可用值:1,2
  sourceType: AgreementSource.NEW,
  // additionalInfo	备注信息
  additionalInfo: ""
});
//#region <租期相关>
// 原始租期（续约时显示）
const originalLeaseDate = computed(() => {
  // 如果是续约状态且有原始租期信息，显示原始租期
  if (agreementDetailInfo.value.sourceType === AgreementSource.RENEW && 
      originalLeaseInfo.value.startDate && 
      originalLeaseInfo.value.endDate) {
    return `${dayjs(originalLeaseInfo.value.startDate).format("YYYY-MM-DD")} 至 ${dayjs(originalLeaseInfo.value.endDate).format("YYYY-MM-DD")}`;
  }
  // 如果是续约预览模式
  if (isRenew.value && originalLeaseInfo.value.startDate && originalLeaseInfo.value.endDate) {
    return `${dayjs(originalLeaseInfo.value.startDate).format("YYYY-MM-DD")} 至 ${dayjs(originalLeaseInfo.value.endDate).format("YYYY-MM-DD")}`;
  }
  return "";
});

// 合同租期（续约时显示续约后的日期）
const leaseDate = computed(() => {
  // 续约预览模式：显示预览的续约后日期
  if (isRenew.value && isAllowEdit.value && previewEndDate.value) {
    const renewalStartDate = dayjs(originalLeaseInfo.value.endDate).add(1, 'day').format("YYYY-MM-DD");
    return `${renewalStartDate} 至 ${dayjs(previewEndDate.value).format("YYYY-MM-DD")}`;
  }
  
  // 已续约状态：直接使用数据库中的日期
  if (agreementDetailInfo.value.sourceType === AgreementSource.RENEW && 
      originalLeaseInfo.value.endDate) {
    // 续约后的开始日期 = 原租约结束日期 + 1天
    const renewalStartDate = dayjs(originalLeaseInfo.value.endDate).add(1, 'day').format("YYYY-MM-DD");
    // 续约后的结束日期 = 直接使用数据库中的 leaseEndDate
    const renewalEndDate = dayjs(agreementDetailInfo.value.leaseEndDate).format("YYYY-MM-DD");
    
    return `${renewalStartDate} 至 ${renewalEndDate}`;
  }
  
  // 普通状态：显示正常的租期
  return `${dayjs(agreementDetailInfo.value.leaseStartDate).format(
    "YYYY-MM-DD"
  )} 至 ${dayjs(agreementDetailInfo.value.leaseEndDate).format("YYYY-MM-DD")}`;
});
// 租期
const leaseTermInfo = ref({
  text: "",
  value: ""
});
// 监视leaseTermInfo的变化
watch(
  leaseTermInfo,
  newValue => {
    if (newValue && newValue.text) {
      console.log("租期变化监听 - newValue:", newValue);
      // 重赋值租期
      agreementDetailInfo.value.leaseTermId = newValue.value;
      agreementDetailInfo.value.leaseTermMonthCount = +newValue.text.slice(
        0,
        -1
      );
      agreementDetailInfo.value.leaseTermUnit = newValue.text.slice(-1);
      
      console.log("租期变化监听 - 解析结果:", {
        租期ID: agreementDetailInfo.value.leaseTermId,
        租期月数: agreementDetailInfo.value.leaseTermMonthCount,
        租期单位: agreementDetailInfo.value.leaseTermUnit,
        是否续约: isRenew.value
      });
      
      // 如果是续约，重新计算预览的结束日期
      if (isRenew.value && originalLeaseInfo.value.endDate) {
        const newEndDate = dayjs(originalLeaseInfo.value.endDate)
          .add(agreementDetailInfo.value.leaseTermMonthCount as number, "month")
          .format("YYYY-MM-DD");
        
        console.log("续约日期计算:", {
          原结束日期: originalLeaseInfo.value.endDate,
          续约月数: agreementDetailInfo.value.leaseTermMonthCount,
          新结束日期: newEndDate
        });
        
        previewEndDate.value = newEndDate;
      } else if (!isRenew.value && agreementDetailInfo.value.leaseStartDate) {
        // 非续约情况，重新计算起止日期
        agreementDetailInfo.value.leaseEndDate = dayjs(
          agreementDetailInfo.value.leaseStartDate
        )
          .add(agreementDetailInfo.value.leaseTermMonthCount as number, "month")
          .format("YYYY-MM-DD");
      }
    }
  },
  { immediate: true, deep: true }
);
// 租期的列表
const leaseTermList = ref<TermInfoInterface[]>([]);
// 租期的列表的列
const showPickerLeaseTerm = ref(false);
const leaseTermColumns = computed(() => {
  return leaseTermList.value.map(item => ({
    text: item.monthCount + item.unit,
    value: item.id
  }));
});
// 获取租期列表
const getLeaseTermListHandle = async () => {
  const { data } = await getTermListByRoomId(
    agreementDetailInfo.value.roomId as string
  );
  leaseTermList.value = data;
};
// 确认选择租期
const onConfirmLeaseTerm = (data: {
  selectedOptions: (typeof leaseTermInfo.value)[];
}) => {
  console.log("onConfirmLeaseTerm", data);
  leaseTermInfo.value = data.selectedOptions[0];
  showPickerLeaseTerm.value = false;
};
//#endregion
//#region <支付方式相关>
// 支付方式的列表
const paymentTypeList = ref<PaymentInfoInterface[]>([]);
// 支付方式
const paymentTypeInfo = ref({
  text: "",
  value: ""
});
// 监视paymentTypeInfo的变化
watch(
  paymentTypeInfo,
  newValue => {
    if (newValue) {
      console.log("newValue", newValue);
      // 重赋值支付方式
      agreementDetailInfo.value.paymentTypeId = newValue.value;
      agreementDetailInfo.value.paymentTypeName = newValue.text;
    }
  },
  { immediate: true, deep: true }
);
// 支付方式的列表的列
const showPickerPaymentType = ref(false);
const paymentTypeColumns = computed(() => {
  return paymentTypeList.value.map(item => ({
    text: `${item.name}`,
    value: item.id
  }));
});
// 获取支付方式列表
const getPaymentTypeListHandle = async () => {
  const { data } = await getPaymentListByRoomId(
    agreementDetailInfo.value.roomId as string
  );
  paymentTypeList.value = data;
};
// 确认选择支付方式
const onConfirmPaymentType = (data: {
  selectedOptions: (typeof paymentTypeInfo.value)[];
}) => {
  console.log("onConfirmPaymentType", data);
  paymentTypeInfo.value = data.selectedOptions[0];
  showPickerPaymentType.value = false;
};
//#endregion
//#region <租金、押金相关>
// 租金
const rentText = computed(() => {
  return `${agreementDetailInfo.value.rent}元/月`;
});
// 押金
const depositText = computed(() => {
  return `${agreementDetailInfo.value.deposit}元`;
});
//#endregion
// 获取公寓的详情信息
const getAgreementDetailHandle = async () => {
  const { data } = await getAgreementDetailById(route.params.id as string);
  agreementDetailInfo.value = data;
  
  console.log("获取租约详情 - 原始数据:", {
    开始日期: data.leaseStartDate,
    结束日期: data.leaseEndDate,
    租期月数: data.leaseTermMonthCount,
    租期单位: data.leaseTermUnit,
    租期ID: data.leaseTermId,
    来源类型: data.sourceType,
    备注信息: data.additionalInfo
  });
  
  // 如果是续约状态（sourceType=2），从additional_info中提取原始租期信息
  if (data.sourceType === AgreementSource.RENEW && data.additionalInfo) {
    // 解析格式：续约申请|原始租期:2026-03-22至2026-04-22
    const match = data.additionalInfo.match(/原始租期:(\d{4}-\d{2}-\d{2})至(\d{4}-\d{2}-\d{2})/);
    if (match) {
      originalLeaseInfo.value = {
        startDate: match[1],
        endDate: match[2],
        termId: data.leaseTermId || "",
        paymentTypeId: data.paymentTypeId || ""
      };
      
      console.log("续约状态 - 从备注中提取原始租期:", originalLeaseInfo.value);
      console.log("续约状态 - 当前租期（续约后）:", {
        开始日期: data.leaseStartDate,
        结束日期: data.leaseEndDate
      });
    }
  }
  
  //   如果是续约预览（编辑模式），显示续约预览信息
  if (isAllowEdit.value && isRenew.value) {
    // 续约预览逻辑：显示续约后的效果，但不直接修改原租约
    // 1. 保存原始租约信息用于显示
    originalLeaseInfo.value = {
      startDate: data.leaseStartDate || "",
      endDate: data.leaseEndDate || "",
      termId: data.leaseTermId || "",
      paymentTypeId: data.paymentTypeId || ""
    };
    
    console.log("续约预览 - 保存原始租约信息:", originalLeaseInfo.value);
    
    // 2. 计算续约后的新结束日期（从原租约结束日期开始延长）
    if (data.leaseEndDate && data.leaseTermMonthCount) {
      const renewalEndDate = dayjs(data.leaseEndDate)
        .add(data.leaseTermMonthCount as number, "month")
        .format("YYYY-MM-DD");
      
      console.log("续约预览 - 初始计算结果:", {
        原结束日期: data.leaseEndDate,
        续约月数: data.leaseTermMonthCount,
        新结束日期: renewalEndDate
      });
      
      // 3. 更新显示的结束日期（仅用于预览）
      previewEndDate.value = renewalEndDate;
    }
  }
  
  // 单独租期相关（在续约逻辑之后设置，避免触发watch时originalLeaseInfo还是空的）
  leaseTermInfo.value.text = data.leaseTermMonthCount + data.leaseTermUnit;
  leaseTermInfo.value.value = data.leaseTermId as string;
  // 单独支付方式相关
  paymentTypeInfo.value.text = data.paymentTypeName as string;
  paymentTypeInfo.value.value = data.paymentTypeId as string;
  
  // 计算剩余天数
  calculateDaysRemaining();
  
  //   如果是确认签约，修改状态为已签约，等待确认
  isConfirmAgreement.value &&
    (agreementDetailInfo.value.status = AgreementStatus.SIGNED);
};
// 更正租期和支付方式
const correctLeaseTermAndPaymentType = () => {
  // 修正租期，如果不存在就改为数组第一项
  let targetTerm =
    leaseTermList.value.find(
      item => item.id === agreementDetailInfo.value.leaseTermId
    ) || leaseTermList.value[0];
  if (targetTerm) {
    console.log("targetTerm", targetTerm);
    leaseTermInfo.value.text = targetTerm.monthCount + targetTerm.unit;
    leaseTermInfo.value.value = targetTerm.id as unknown as string;
  }
  //   修正支付方式，如果不存在就改为数组第一项
  let targetPaymentType =
    paymentTypeList.value.find(
      item => item.id === agreementDetailInfo.value.paymentTypeId
    ) || paymentTypeList.value[0];
  if (targetPaymentType) {
    console.log("targetPaymentType", targetPaymentType);
    paymentTypeInfo.value.text = targetPaymentType.name as string;
    paymentTypeInfo.value.value = targetPaymentType.id as unknown as string;
  }
};
// 跳转到公寓的详情页面
const goApartmentDetail = () => {
  router.push({
    path: `/tenant/apartmentDetail/${agreementDetailInfo.value.apartmentId}`
  });
};
// 跳转到房间的详情页面
const goRoomDetail = () => {
  router.push({
    path: `/tenant/roomDetail/${agreementDetailInfo.value.roomId}`
  });
};
// 续约,修改
const submitHandle = async () => {
  console.log("确认签约");
  try {
    if (isRenew.value) {
      // 续约逻辑：修改sourceType为续约状态，设置状态为续约待确认
      // 在additional_info中保存原始租期信息，供前端显示使用
      const originalPeriod = `原始租期:${dayjs(originalLeaseInfo.value.startDate).format('YYYY-MM-DD')}至${dayjs(originalLeaseInfo.value.endDate).format('YYYY-MM-DD')}`;
      const additionalInfo = `续约申请|${originalPeriod}`;
      
      const renewalData = {
        ...agreementDetailInfo.value,
        // 保持原租约的开始和结束日期不变（房东审批时会更新）
        leaseStartDate: originalLeaseInfo.value.startDate,
        leaseEndDate: originalLeaseInfo.value.endDate,
        // 修改sourceType为续约状态（2）
        sourceType: AgreementSource.RENEW, // 续约
        // 设置状态为续约待确认
        status: 7, // LeaseStatus.RENEWING 续约待确认
        // 租客选择的续约租期和支付方式（房东审批时使用）
        leaseTermId: agreementDetailInfo.value.leaseTermId,
        paymentTypeId: agreementDetailInfo.value.paymentTypeId,
        // 在备注中保存原始租期信息
        additionalInfo: additionalInfo
      };

      console.log("续约提交数据:", {
        租约ID: renewalData.id,
        原开始日期: renewalData.leaseStartDate,
        原结束日期: renewalData.leaseEndDate,
        续约来源类型: renewalData.sourceType,
        续约来源类型名称: AgreementSourceMap.find(item => item.value === renewalData.sourceType)?.label,
        续约状态: renewalData.status,
        续约租期ID: renewalData.leaseTermId,
        续约支付方式ID: renewalData.paymentTypeId,
        备注信息: renewalData.additionalInfo
      });

      // 调用现有的保存或更新接口
      await saveOrUpdateAgreement(renewalData);
      
      showToast({ type: "success", message: "续约申请已提交，请等待房东审批" });
      
      // 返回到我的租约页面
      router.replace('/tenant/myAgreement');
      return;
    }

    // 原有的签约逻辑
    // 租期时长不能小于支付方式月份长度
    let leaseTermMonthCount =
      leaseTermList.value.find(
        item => item.id === agreementDetailInfo.value.leaseTermId
      )?.monthCount || 0;

    console.log(leaseTermList.value);
    console.log(agreementDetailInfo.value);
    console.log("=============================");
    console.log("租期长度：" + leaseTermMonthCount);

    let paymentTypeMonthCount =
      paymentTypeList.value.find(
        item => item.id === agreementDetailInfo.value.paymentTypeId
      )?.paymentTypeMonthCount || 0;

    console.log("支付月份长度：" + paymentTypeMonthCount);

    if (+leaseTermMonthCount < +paymentTypeMonthCount) {
      showToast({
        type: "fail",
        message: "租期时长不能小于支付方式月份长度"
      });
      return;
    }

    await saveOrUpdateAgreement(agreementDetailInfo.value);
    showToast({ type: "success", message: "操作成功" });
    
    // 智能返回：优先返回到我的租约页面
    const fromPath = route.query.from as string;
    if (fromPath && fromPath === '/tenant/userCenter') {
      // 如果是从个人中心来的，先回到我的租约，再让用户选择是否回到个人中心
      router.replace('/tenant/myAgreement');
    } else {
      // 其他情况使用智能返回
      router.back();
    }
  } catch (error) {
    console.log(error);
    showToast({ type: "fail", message: "操作失败，请重试" });
  }
};
onMounted(async () => {
  route.params.id && (await getAgreementDetailHandle());
  await getLeaseTermListHandle();
  await getPaymentTypeListHandle();
  //  如果允许修改，修正租期和支付方式
  isAllowEdit.value && correctLeaseTermAndPaymentType();
});
</script>

<style scoped lang="less">
.base-info-title {
  //background-color: var(--van-primary-background-color);
  font-weight: bold;
  //color: white;
}
::v-deep .van-card {
  background: var(--van-background-2) !important;
}

// 原始租约信息样式
.original-lease-info {
  ::v-deep .van-field__control {
    background-color: #f5f5f5;
    color: #666;
    font-size: 14px;
  }
  
  ::v-deep .van-field__label {
    color: #666;
    font-weight: 500;
  }
}

// 续约高亮样式
.renewal-highlight {
  ::v-deep .van-field__control {
    background-color: #fff7e6;
    border: 1px solid #ffa940;
    border-radius: 4px;
    padding: 4px 8px;
    color: #d46b08;
    font-weight: 500;
  }
  
  ::v-deep .van-field__label {
    color: #d46b08;
    font-weight: 600;
  }
  
  // 添加一个小图标提示
  position: relative;
  
  &::after {
    content: "✨";
    position: absolute;
    right: 8px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 12px;
    z-index: 1;
  }
}
</style>
