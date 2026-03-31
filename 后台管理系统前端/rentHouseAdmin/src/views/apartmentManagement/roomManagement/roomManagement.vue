<template>
  <el-card>
    <ProTable
      ref="proTable"
      :dataCallback="dataCallback"
      :columns="columns"
      :requestApi="getRoomList"
      :initParam="initParam"
      :stripe="true"
    >
      <template #tableHeader>
        <el-button type="primary" icon="Plus" @click="addHandle">
          新增房间
        </el-button>
      </template>
      <!-- 表格操作 -->
      <template #operation="scope">
        <el-button type="success" icon="Edit" @click="editHandle(scope.row)">
          修改
        </el-button>
        <!-- 只有房东发布且审核通过的房间才显示打回按钮 -->
        <el-button 
          v-if="shouldShowRejectButton(scope.row)"
          type="warning" 
          icon="RefreshLeft" 
          @click="rejectHandle(scope.row)">
          打回
        </el-button>
        <el-button type="danger" icon="Delete" @click="deleteHandle(scope.row)">
          删除
        </el-button>
      </template>
    </ProTable>
  </el-card>
</template>

<script setup lang="tsx">
import { onMounted, reactive, ref } from 'vue'
import { ColumnProps } from '@/components/ProTable/src/types'
import ProTable from '@/components/ProTable/src/ProTable.vue'
import { useRouter } from 'vue-router'
import { useHandleData } from '@/hooks/useHandleData'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  deleteRoomById,
  getApartmentListByDistrictId,
  getCityList,
  getDistrictList,
  getProvinceList,
  getRoomList,
  updateRoomReleaseStatus,
  rejectRoomReview,
} from '@/api/apartmentManagement'
import {
  ApartmentInterface,
  RegionInterface,
  RoomInterface,
} from '@/api/apartmentManagement/types'
import {
  getLabelByValue,
  PublisherType,
  PublisherTypeMap,
  ReviewStatus,
  ReviewStatusMap,
  RoomCheckInStatus,
  RoomCheckInStatusMap,
  RoomReleaseStatus,
} from '@/enums/constEnums'

const router = useRouter()

// *获取 ProTable 元素，调用其获取刷新数据方法
const proTable = ref<InstanceType<typeof ProTable>>()
// 地区数据
const areaInfo = reactive({
  // 省份数据
  provinceList: [] as RegionInterface[],
  provinceId: '',
  // 市区数据
  cityList: [] as RegionInterface[],
  cityId: '',
  // 区县数据
  districtList: [] as RegionInterface[],
  districtId: '',
  // 公寓数据
  apartmentList: [] as ApartmentInterface[],
  apartmentId: '',
})
// *表格配置项
const columns: ColumnProps[] = [
  {
    prop: 'provinceId',
    label: '省份',
    isShow: false,
    fieldNames: { label: 'name', value: 'id' },
    search: {
      el: 'select',
      render: () => {
        return (
          <el-select
            v-model={areaInfo.provinceId}
            placeholder="请选择省份"
            clearable
            key="provinceId"
            onChange={provinceChangeCallback}
            onClear={provinceClearCallback}
          >
            {areaInfo.provinceList.map((item) => (
              <el-option
                key={item.id}
                label={item.name}
                value={item.id}
              ></el-option>
            ))}
          </el-select>
        )
      },
    },
  },
  {
    prop: 'cityId',
    label: '城市',
    isShow: false,
    fieldNames: { label: 'name', value: 'id' },
    search: {
      el: 'select',
      key: 'cityId',
      render: () => {
        return (
          <el-select
            v-model={areaInfo.cityId}
            placeholder="请选择城市"
            clearable
            key="cityId"
            onChange={cityChangeCallback}
            onClear={cityClearCallback}
          >
            {areaInfo.cityList.map((item) => (
              <el-option
                key={item.id}
                label={item.name}
                value={item.id}
              ></el-option>
            ))}
          </el-select>
        )
      },
    },
  },
  {
    prop: 'districtId',
    label: '区域',
    isShow: false,
    fieldNames: { label: 'name', value: 'id' },
    search: {
      el: 'select',
      key: 'districtId',
      render: () => {
        return (
          <el-select
            v-model={areaInfo.districtId}
            placeholder="请选择区域"
            clearable
            onChange={districtChangeCallback}
            onClear={districtClearCallback}
          >
            {areaInfo.districtList.map((item) => (
              <el-option
                key={item.id}
                label={item.name}
                value={item.id}
              ></el-option>
            ))}
          </el-select>
        )
      },
    },
  },
  {
    prop: 'apartmentId',
    label: '公寓',
    isShow: false,
    fieldNames: { label: 'name', value: 'id' },
    search: {
      el: 'select',
      key: 'apartmentId',
      render: () => {
        return (
          <el-select
            v-model={areaInfo.apartmentId}
            placeholder="请选择公寓"
            clearable
            onChange={apartmentChangeCallback}
            onClear={apartmentClearCallback}
          >
            {areaInfo.apartmentList.map((item) => (
              <el-option
                key={item.id}
                label={item.name}
                value={item.id}
              ></el-option>
            ))}
          </el-select>
        )
      },
    },
  },
  {
    prop: 'publisherType',
    label: '发布者类型',
    isShow: false,
    enum: PublisherTypeMap,
    fieldNames: { label: 'label', value: 'value' },
    search: {
      el: 'select',
      key: 'publisherType',
    },
  },
  { prop: 'roomNumber', label: '房间号' },
  { prop: 'rent', label: '租金（元/月）' },
  {
    prop: 'publisherType',
    label: '发布者类型',
    render: ({ row }: { row: RoomInterface }) => {
      if (row.publisherType === PublisherType.OFFICIAL) {
        return (
          <el-tag type="primary">
            官方发布
          </el-tag>
        )
      } else if (row.publisherType === PublisherType.LANDLORD) {
        return (
          <el-tag type="warning">
            房东{row.publisherId}号发布
          </el-tag>
        )
      }
      return <span style="color: #999;">-</span>
    },
  },
  {
    prop: 'reviewInfo',
    label: '审核信息',
    render: ({ row }: { row: RoomInterface }) => {
      // 只有房东发布的房间才显示审核信息
      if (row.publisherType === PublisherType.LANDLORD && row.reviewInfo) {
        const reviewStatus = row.reviewInfo.reviewStatus
        const complianceScore = row.reviewInfo.complianceScore
        return (
          <div>
            <div>
              {reviewStatus === ReviewStatus.PENDING && (
                <el-tag type="info">待审核</el-tag>
              )}
              {reviewStatus === ReviewStatus.APPROVED && (
                <el-tag type="success">审核通过</el-tag>
              )}
              {reviewStatus === ReviewStatus.REJECTED && (
                <el-tag type="danger">审核拒绝</el-tag>
              )}
            </div>
            {complianceScore && (
              <div style="margin-top: 4px; font-size: 12px; color: #666;">
                合规评分: {complianceScore}分
              </div>
            )}
          </div>
        )
      }
      return <span style="color: #999;">-</span>
    },
  },
  { prop: 'apartmentInfo.provinceName', label: '所处省份' },
  { prop: 'apartmentInfo.cityName', label: '所处城市' },
  { prop: 'apartmentInfo.districtName', label: '所处区域' },
  { prop: 'apartmentInfo.name', label: '所处公寓' },
  {
    prop: 'isCheckIn',
    label: '入住状态',
    render: ({ row }: { row: RoomInterface }) => {
      return row.isCheckIn === RoomCheckInStatus.NOT_CHECK_IN ? (
        <el-tag type="info">
          {getLabelByValue(RoomCheckInStatusMap, row.isCheckIn)}
        </el-tag>
      ) : (
        <el-tag type="success">
          {getLabelByValue(RoomCheckInStatusMap, row.isCheckIn)}
        </el-tag>
      )
    },
  },
  {
    prop: 'isRelease',
    label: '发布状态',
    render: ({ row }: { row: RoomInterface }) => {
      return (
        <el-switch
          active-value={RoomReleaseStatus.RELEASED}
          inactive-value={RoomReleaseStatus.NOT_RELEASED}
          v-model={row.isRelease}
          onChange={() =>
            updateRoomReleaseStatus(row.id, row.isRelease as RoomReleaseStatus)
          }
        ></el-switch>
      )
    },
  },
  { prop: 'operation', label: '操作', fixed: 'right', width: 280 },
]

// *查询参数
const initParam = reactive({})

//#region <省市区查询相关>
// 获取省份
async function getProvinceListHandle() {
  try {
    const { data } = await getProvinceList()
    areaInfo.provinceList = data
    proTable.value?.enumMap.set('provinceId', areaInfo.provinceList)
  } catch (error) {
    console.log(error)
  }
}
// 获取城市
async function getCityListHandle(provinceId: number) {
  try {
    const { data } = await getCityList(provinceId)
    areaInfo.cityList = data
    proTable.value?.enumMap.set('cityId', areaInfo.cityList)
  } catch (error) {
    console.log(error)
  }
}
// 获取区域
async function getDistrictListHandle(cityId: number) {
  try {
    const { data } = await getDistrictList(cityId)
    areaInfo.districtList = data
    proTable.value?.enumMap.set('districtId', areaInfo.districtList)
  } catch (error) {
    console.log(error)
  }
}
// 获取公寓
async function getApartmentListHandle(districtId: number) {
  try {
    const { data } = await getApartmentListByDistrictId(districtId)
    areaInfo.apartmentList = data
    proTable.value?.enumMap.set('apartmentId', areaInfo.apartmentList)
  } catch (error) {
    console.log(error)
  }
}
// 重置市数据
function resetCity() {
  areaInfo.cityId = ''
  areaInfo.cityList = []
  proTable.value?.enumMap.set('cityId', [])
  proTable.value!.searchParam.cityId = ''
}
// 重置区数据
function resetDistrict() {
  areaInfo.districtId = ''
  areaInfo.districtList = []
  proTable.value?.enumMap.set('districtId', [])
  proTable.value!.searchParam.districtId = ''
}
// 重置公寓数据
function resetApartment() {
  areaInfo.apartmentId = ''
  areaInfo.apartmentList = []
  proTable.value?.enumMap.set('apartmentId', [])
  proTable.value!.searchParam.apartmentId = ''
}
// 省份改变回调
const provinceChangeCallback = async () => {
  let provinceId = proTable.value!.searchParam.provinceId
  if (provinceId) {
    resetCity()
    resetDistrict()
    resetApartment()
    await getCityListHandle(provinceId)
  }
}
// 省份清除回调
const provinceClearCallback = () => {
  areaInfo.provinceId = ''
  proTable.value!.searchParam.provinceId = ''
  resetCity()
  resetDistrict()
  resetApartment()
}
// 城市改变回调
const cityChangeCallback = async () => {
  let cityId = proTable.value!.searchParam.cityId
  if (cityId) {
    resetDistrict()
    resetApartment()
    await getDistrictListHandle(cityId)
  }
}
// 城市清除回调
const cityClearCallback = () => {
  console.log('清空城市')
  areaInfo.cityId = ''
  proTable.value!.searchParam.cityId = ''
  resetDistrict()
  resetApartment()
}
// 区域改变回调
const districtChangeCallback = async () => {
  let districtId = proTable.value!.searchParam.districtId
  if (districtId) {
    resetApartment()
    await getApartmentListHandle(districtId)
  }
}
// 区域清除回调
const districtClearCallback = () => {
  console.log('清空区域')
  areaInfo.districtId = ''
  proTable.value!.searchParam.districtId = ''
  resetApartment()
}
// 公寓改变回调
const apartmentChangeCallback = async () => {
  console.log('公寓改变')
}
// 公寓清除回调
const apartmentClearCallback = () => {
  console.log('清空公寓')
  areaInfo.apartmentId = ''
  proTable.value!.searchParam.apartmentId = ''
}
//#endregion

// 处理返回的数据格式
const dataCallback = (data: any) => {
  return {
    list: data?.records,
    total: data?.total,
  }
}
// 查看
const editHandle = (row: RoomInterface) => {
  console.log(row)
  router.push({
    path: '/apartmentManagement/roomManagement/addOrEditRoom',
    query: {
      id: row.id,
    },
  })
}
// 新增
const addHandle = () => {
  router.push({
    path: '/apartmentManagement/roomManagement/addOrEditRoom',
  })
}
// 通过，不通过
const deleteHandle = async (row: RoomInterface) => {
  // status	0301-审核通过 0302-审核不通过
  await useHandleData(
    deleteRoomById,
    row.id,
    `删除${row.apartmentInfo.name + row.roomNumber}房间`,
  )
  // 修改row
  proTable.value?.getTableList()
}

// 打回房间审核
const rejectHandle = async (row: RoomInterface) => {
  try {
    const { value: rejectReason } = await ElMessageBox.prompt(
      '请输入打回原因（必填）',
      `打回房间：${row.apartmentInfo.name} - ${row.roomNumber}`,
      {
        confirmButtonText: '确认打回',
        cancelButtonText: '取消',
        inputPattern: /\S+/,
        inputErrorMessage: '打回原因不能为空',
        inputPlaceholder: '请详细说明打回的具体原因...',
        inputType: 'textarea',
        customClass: 'reject-room-dialog'
      }
    )

    if (rejectReason && rejectReason.trim()) {
      await useHandleData(
        () => rejectRoomReview(row.id, rejectReason.trim()),
        null,
        `打回房间：${row.apartmentInfo.name} - ${row.roomNumber}`
      )
      
      // 刷新表格数据
      proTable.value?.getTableList()
      ElMessage.success('房间已成功打回，房间发布状态已重置为未发布，房东将收到通知')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('打回房间失败:', error)
      ElMessage.error('打回操作失败，请重试')
    }
  }
}
onMounted(() => {
  // 获取省份
  getProvinceListHandle()
})

// 判断是否显示打回按钮
const shouldShowRejectButton = (row: RoomInterface) => {
  console.log('检查打回按钮显示条件:', {
    publisherType: row.publisherType,
    PublisherTypeLANDLORD: PublisherType.LANDLORD,
    reviewInfo: row.reviewInfo,
    reviewStatus: row.reviewInfo?.reviewStatus,
    ReviewStatusAPPROVED: ReviewStatus.APPROVED
  })
  
  return row.publisherType === PublisherType.LANDLORD && 
         row.reviewInfo && 
         row.reviewInfo.reviewStatus === ReviewStatus.APPROVED
}
</script>
