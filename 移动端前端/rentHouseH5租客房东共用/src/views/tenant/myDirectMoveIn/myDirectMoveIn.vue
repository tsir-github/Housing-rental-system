<template>
  <van-skeleton :row="20" :loading="loading">
    <div class="p-[10px]">
      <van-card
        class="rounded-xl shadow mb-[10px]"
        v-for="item in applicationList"
        :key="item.id"
        @click="goApplicationDetail(item)"
      >
        <!--      title-->
        <template #title>
          <h2 class="text-[15px] font-bold">{{ item.apartmentName }} {{ item.roomNumber }}房间</h2>
        </template>
        <!--    thumb-->
        <template #thumb>
          <van-image
            class="w-full h-full object-cover"
            :src="item.roomGraphUrl || ''"
          >
            <template v-slot:error>加载失败</template>
            <template v-slot:loading>
              <van-loading type="spinner" size="20" />
            </template>
          </van-image>
        </template>
        <!--      tags-->
        <template #tags>
          <van-tag
            class="mt-[10px]"
            :type="getStatusTagType(item.status)"
            size="medium"
          >
            {{ getStatusText(item.status) }}
          </van-tag>
        </template>
        <!--      price-->
        <template #price>
          <div class="flex justify-between">
            <div class="text-[12px] text-red-500">月租金</div>
            <div class="text-[12px] text-red-500">
              ￥{{ item.rent }}/月
            </div>
          </div>
        </template>
        <!--      desc-->
        <template #desc>
          <div class="text-[12px] text-gray-500 mt-[5px]">
            <div>申请时间：{{ formatDate(item.createTime) }}</div>
            <div v-if="item.leaseStartDate">入住日期：{{ item.leaseStartDate }}</div>
          </div>
        </template>
      </van-card>
    </div>

    <van-empty v-if="applicationList?.length <= 0" description="暂无申请记录" />
  </van-skeleton>
</template>

<script setup lang="ts" name="TenantMyDirectMoveIn">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/modules/user";
import { getMyApplications } from "@/api/tenant/directMoveIn";
import { showToast } from "vant";
import dayjs from "dayjs";

const router = useRouter();
const userStore = useUserStore();

// 申请列表
const applicationList = ref<any[]>([]);
const loading = ref(true);

// 获取申请列表
const getApplicationListHandle = async () => {
  try {
    loading.value = true;
    const userId = userStore.userInfo?.id;
    if (!userId) {
      showToast("请先登录");
      return;
    }
    
    const { data } = await getMyApplications(userId);
    applicationList.value = data || [];
  } catch (error) {
    console.error("获取申请列表失败:", error);
    showToast("获取申请列表失败");
  } finally {
    loading.value = false;
  }
};

// 跳转到申请详情页面
const goApplicationDetail = (item: any) => {
  router.push({
    path: "/tenant/directMoveInDetail",
    query: { applicationId: item.id }
  });
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
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm');
};

onMounted(async () => {
  await getApplicationListHandle();
});
</script>

<style scoped lang="less">
::v-deep .van-card {
  background: var(--van-background-2) !important;
}
</style>