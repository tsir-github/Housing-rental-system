<template>
  <van-skeleton :row="20" :loading="loading">
    <div class="page-container min-h-[100vh] py-[15px]" v-if="applicationDetail">
      <!-- 申请状态 -->
      <div class="card mb-4">
        <div class="base-info-title py-[4px]">申请状态</div>
        <div class="my-[5px] p-[15px]">
          <div class="flex items-center justify-between">
            <span class="text-[16px] font-bold">当前状态</span>
            <van-tag
              :type="getStatusTagType(applicationDetail.status)"
              size="large"
            >
              {{ getStatusText(applicationDetail.status) }}
            </van-tag>
          </div>
          <div class="mt-[10px] text-[14px] text-gray-600">
            申请时间：{{ formatDate(applicationDetail.createTime) }}
          </div>
          <div v-if="applicationDetail.updateTime" class="text-[14px] text-gray-600">
            更新时间：{{ formatDate(applicationDetail.updateTime) }}
          </div>
        </div>
      </div>

      <!-- 房源信息 -->
      <div class="card mb-4">
        <div class="base-info-title py-[4px]">申请房源</div>
        <div class="my-[5px]">
          <van-card class="rounded-xl shadow">
            <template #title>
              <h2 class="text-[16px] font-bold mt-[25px] ml-[25px]">
                {{ applicationDetail.apartmentName }} {{ applicationDetail.roomNumber }}房间
              </h2>
            </template>
            <template #desc>
              <div class="ml-[25px] mb-[15px]">
                <div class="text-red-500 text-[18px] font-bold">
                  ￥{{ applicationDetail.rent }}/月
                </div>
                <div class="text-gray-500 text-[14px] mt-[5px]">
                  押金：￥{{ applicationDetail.deposit }}
                </div>
              </div>
            </template>
            <template #thumb>
              <van-image
                class="w-full h-full object-cover"
                :src="applicationDetail.roomGraphUrl || ''"
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
        <div class="base-info-title py-[4px]">申请人信息</div>
        <div class="my-[5px]">
          <van-cell-group inset>
            <van-cell title="姓名" :value="applicationDetail.name" />
            <van-cell title="手机号" :value="applicationDetail.phone" />
            <van-cell title="身份证号" :value="maskIdCard(applicationDetail.identificationNumber)" />
            <van-cell title="邮箱" :value="applicationDetail.email || '未填写'" />
          </van-cell-group>
        </div>
      </div>

      <!-- 紧急联系人 -->
      <div class="card mb-4">
        <div class="base-info-title py-[4px]">紧急联系人</div>
        <div class="my-[5px]">
          <van-cell-group inset>
            <van-cell title="联系人姓名" :value="applicationDetail.emergencyContact" />
            <van-cell title="联系人电话" :value="applicationDetail.emergencyPhone" />
          </van-cell-group>
        </div>
      </div>

      <!-- 租期信息 -->
      <div class="card mb-4">
        <div class="base-info-title py-[4px]">租期信息</div>
        <div class="my-[5px]">
          <van-cell-group inset>
            <van-cell title="入住日期" :value="applicationDetail.leaseStartDate" />
            <van-cell title="租期结束" :value="applicationDetail.leaseEndDate" />
            <van-cell title="租期时长" :value="calculateLeaseDuration()" />
            <van-cell title="支付方式" :value="applicationDetail.paymentTypeName || '未知'" />
          </van-cell-group>
        </div>
      </div>

      <!-- 备注信息 -->
      <div class="card mb-4" v-if="applicationDetail.additionalInfo">
        <div class="base-info-title py-[4px]">备注信息</div>
        <div class="my-[5px] p-[15px]">
          <div class="text-[14px] text-gray-700">
            {{ applicationDetail.additionalInfo }}
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="main-container py-[20px]" v-if="applicationDetail.status === 1">
        <van-button
          type="danger"
          block
          round
          size="large"
          @click="cancelApplication"
          class="cancel-button"
        >
          取消申请
        </van-button>
      </div>
    </div>
  </van-skeleton>
</template>

<script setup lang="ts" name="TenantDirectMoveInDetail">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getApplicationDetail, cancelDirectMoveInApplication } from "@/api/tenant/directMoveIn";
import { showToast, showConfirmDialog } from "vant";
import dayjs from "dayjs";

const route = useRoute();
const router = useRouter();

// 申请详情
const applicationDetail = ref<any>(null);
const loading = ref(true);

// 获取申请详情
const getApplicationDetailHandle = async () => {
  try {
    loading.value = true;
    const applicationId = route.query.applicationId as string;
    if (!applicationId) {
      showToast("缺少申请ID参数");
      router.back();
      return;
    }

    const { data } = await getApplicationDetail(Number(applicationId));
    applicationDetail.value = data;
  } catch (error) {
    console.error("获取申请详情失败:", error);
    showToast("获取申请详情失败");
  } finally {
    loading.value = false;
  }
};

// 获取状态标签类型
const getStatusTagType = (status: number | string) => {
  const statusCode = typeof status === 'string' ? parseInt(status) : status;
  switch (statusCode) {
    case 1: // PENDING
      return 'warning';
    case 2: // APPROVED
      return 'success';
    case 3: // REJECTED
      return 'danger';
    default:
      return 'default';
  }
};

// 获取状态文本
const getStatusText = (status: number | string) => {
  const statusCode = typeof status === 'string' ? parseInt(status) : status;
  switch (statusCode) {
    case 1: // PENDING
      return '待处理';
    case 2: // APPROVED
      return '已确认';
    case 3: // REJECTED
      return '已拒绝';
    default:
      return '未知状态';
  }
};

// 格式化日期
const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss');
};

// 脱敏身份证号
const maskIdCard = (idCard: string) => {
  if (!idCard) return '';
  return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2');
};

// 计算租期时长
const calculateLeaseDuration = () => {
  if (!applicationDetail.value?.leaseStartDate || !applicationDetail.value?.leaseEndDate) {
    return '未知';
  }
  
  const start = dayjs(applicationDetail.value.leaseStartDate);
  const end = dayjs(applicationDetail.value.leaseEndDate);
  const months = end.diff(start, 'month');
  
  return `${months}个月`;
};

// 取消申请
const cancelApplication = async () => {
  try {
    await showConfirmDialog({
      title: '取消申请',
      message: '确定要取消这个申请吗？取消后无法恢复。',
      confirmButtonText: '确定取消',
      cancelButtonText: '我再想想'
    });

    // 调用取消申请的API
    const applicationId = route.query.applicationId as string;
    await cancelDirectMoveInApplication(Number(applicationId));
    
    showToast({
      type: 'success',
      message: '申请已取消'
    });

    // 返回上一页
    router.back();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消申请失败:', error);
      showToast('取消申请失败');
    }
  }
};

onMounted(async () => {
  await getApplicationDetailHandle();
});
</script>

<style scoped lang="less">
.base-info-title {
  font-weight: bold;
  color: #323233;
}

.cancel-button {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background-color: #ee0a24 !important;
  color: #fff !important;
  border: none !important;
  box-shadow: 0 2px 4px rgba(238, 10, 36, 0.3);
  
  &:active {
    transform: translateY(1px);
    background-color: #c8102e !important;
  }
  
  &:hover {
    background-color: #c8102e !important;
  }
}

::v-deep .van-card {
  background: var(--van-background-2) !important;
}
</style>