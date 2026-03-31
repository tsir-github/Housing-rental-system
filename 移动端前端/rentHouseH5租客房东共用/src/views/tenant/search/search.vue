<template>
  <div class="search-page">
    <SearchBar :confirm-callback="confirmCallback"></SearchBar>

    <div class="main-container">
      <PullDownRefreshContainer
        :request="getRoomListHandler"
        ref="pullDownRefreshContainerRef"
        class="min-h-[70vh]"
      >
        <template v-if="roomList?.length">
          <RoomCard
            v-for="item in roomList"
            :key="item.id"
            :data="item"
          ></RoomCard>
        </template>
        <template v-else>
          <div class="empty-state">
            <van-empty description="暂无房源信息" />
          </div>
        </template>
      </PullDownRefreshContainer>
    </div>
  </div>
</template>

<script setup lang="ts" name="Search">
import SearchBar from "@/components/common/SearchBar/SearchBar.vue";
import type { RoomInterface, RoomListQueryInterface } from "@/api/tenant/search/types";
import { ref } from "vue";
import { getRoomList } from "@/api/tenant/search";
import PullDownRefreshContainer from "@/components/common/PullDownRefreshContainer/PullDownRefreshContainer.vue";
import type { ReqPage } from "@/api/types";
import RoomCard from "@/components/common/RoomCard/RoomCard.vue";
// 搜索参数
const searchParams = ref<Partial<RoomListQueryInterface>>({});
const pullDownRefreshContainerRef =
  ref<InstanceType<typeof PullDownRefreshContainer>>();
// 房间列表
const roomList = ref<RoomInterface[]>([]);
// 点击查找，搜索条件
function confirmCallback(params: RoomListQueryInterface) {
  //    收集参数，调用接口
  let targetObi = {};
  //   去除空值
  for (let key in params) {
    if ((params as any)[key] !== "") {
      (targetObi as any)[key] = (params as any)[key];
    }
  }
  searchParams.value = targetObi;
  
  // 强制清空房间列表，确保数据重新加载
  roomList.value = [];
  
  // 重置分页组件状态并刷新
  pullDownRefreshContainerRef.value?.onRefresh();
}
// 分页获取房间的信息
async function getRoomListHandler(pageInfo: ReqPage) {
  console.log("=== getRoomListHandler 开始 ===");
  console.log("接收到的 pageInfo:", pageInfo);
  
  // 只在第一页时请求数据，后续页面直接返回
  if (pageInfo.current > 1) {
    console.log("非第一页请求，直接返回");
    return;
  }
  
  // 设置大的页面大小，一次性获取所有数据
  const requestParams = { 
    ...searchParams.value, 
    current: 1, 
    size: 100  // 设置足够大的数量，确保能获取所有数据
  };
  
  console.log("即将发送的完整请求参数:", requestParams);
  
  //   调用接口
  const { data } = await getRoomList(requestParams);
  console.log("=== 房间列表调试信息 ===");
  console.log("后端返回总数:", data.total);
  console.log("后端返回当前页数据量:", data.records.length);
  console.log("搜索参数:", searchParams.value);
  
  // 直接设置所有数据，不需要分页逻辑
  roomList.value = [...data.records];
  console.log("一次性加载完成，当前显示数量:", roomList.value.length);
  
  // 标记为加载完成，不再触发后续分页请求
  pullDownRefreshContainerRef.value?.setFinished(true);
  
  console.log("当前显示房间ID列表:", roomList.value.map(room => room.id));
  console.log("=== 调试信息结束 ===");
}
</script>
