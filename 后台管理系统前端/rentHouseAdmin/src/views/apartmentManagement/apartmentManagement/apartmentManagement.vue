<template>
  <el-card>
    <ProTable
      ref="proTable"
      :dataCallback="dataCallback"
      :columns="columns"
      :requestApi="getApartmentList"
      :initParam="initParam"
      :stripe="true"
    >
      <template #tableHeader>
        <el-button type="primary" icon="Plus" @click="addHandle">
          新增公寓
        </el-button>
      </template>
      <!-- 表格操作 -->
      <template #operation="scope">
        <el-button type="success" icon="Edit" @click="editHandle(scope.row)">
          修改
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
import {
  deleteApartmentById,
  getApartmentList,
  getCityList,
  getDistrictList,
  getProvinceList,
  updateApartmentReleaseStatus,
} from '@/api/apartmentManagement'
import {
  ApartmentInterface,
  RegionInterface,
} from '@/api/apartmentManagement/types'
import { 
  ApartmentReleaseStatus,
  PublisherType,
  PublisherTypeMap,
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
  // 发布者类型
  publisherType: '',
})
// *表格配置项
const columns: ColumnProps[] = [
  {
    prop: 'publisherType',
    label: '发布者类型',
    isShow: false,
    search: {
      el: 'select',
      render: () => {
        return (
          <el-select
            v-model={areaInfo.publisherType}
            placeholder="请选择发布者类型"
            clearable
            onChange={publisherTypeChangeCallback}
            onClear={publisherTypeClearCallback}
          >
            <el-option label="官方发布" value={PublisherType.OFFICIAL}></el-option>
            <el-option label="房东发布" value={PublisherType.LANDLORD}></el-option>
          </el-select>
        )
      },
    },
  },
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
  { prop: 'name', label: '名称' },
  {
    prop: 'publisherType',
    label: '发布者类型',
    render: ({ row }: { row: ApartmentInterface }) => {
      if (row.landlordId) {
        return <el-tag type="warning">房东{row.landlordId}号发布</el-tag>
      } else {
        return <el-tag type="primary">官方发布</el-tag>
      }
    },
  },
  { prop: 'addressDetail', label: '详细地址' },
  { prop: 'freeRoomCount', label: '空闲房间数' },
  { prop: 'totalRoomCount', label: '房间总数' },
  { prop: 'provinceName', label: '所处省份' },
  { prop: 'cityName', label: '所处城市' },
  { prop: 'districtName', label: '所处区域' },
  { prop: 'phone', label: '联系方式' },
  {
    prop: '入住状态',
    label: '入住状态',
    render: ({ row }: { row: ApartmentInterface }) => {
      return row.freeRoomCount === 0 ? (
        <el-tag type="info">已满房</el-tag>
      ) : (
        <el-tag type="success">可入住</el-tag>
      )
    },
  },
  {
    prop: 'isRelease',
    label: '发布状态',
    render: ({ row }: { row: ApartmentInterface }) => {
      return (
        <el-switch
          active-value={ApartmentReleaseStatus.RELEASED}
          inactive-value={ApartmentReleaseStatus.NOT_RELEASED}
          v-model={row.isRelease}
          onChange={() =>
            updateApartmentReleaseStatus(
              row.id,
              row.isRelease as ApartmentReleaseStatus,
            )
          }
        ></el-switch>
      )
    },
  },
  { prop: 'operation', label: '操作', fixed: 'right', width: 230 },
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
// 省份改变回调
const provinceChangeCallback = async () => {
  let provinceId = proTable.value!.searchParam.provinceId
  if (provinceId) {
    resetCity()
    resetDistrict()
    await getCityListHandle(provinceId)
  }
}
// 省份清除回调
const provinceClearCallback = () => {
  areaInfo.provinceId = ''
  proTable.value!.searchParam.provinceId = ''
  resetCity()
  resetDistrict()
}
// 城市改变回调
const cityChangeCallback = async () => {
  let cityId = proTable.value!.searchParam.cityId
  if (cityId) {
    resetDistrict()
    await getDistrictListHandle(cityId)
  }
}
// 城市清除回调
const cityClearCallback = () => {
  console.log('清空城市')
  areaInfo.cityId = ''
  proTable.value!.searchParam.cityId = ''
  resetDistrict()
}
// 区域改变回调
const districtChangeCallback = async () => {
  console.log('区域改变')
}
// 区域清除回调
const districtClearCallback = () => {
  console.log('清空区域')
  areaInfo.districtId = ''
  proTable.value!.searchParam.districtId = ''
}
// 发布者类型改变回调
const publisherTypeChangeCallback = () => {
  if (proTable.value) {
    proTable.value.searchParam.publisherType = areaInfo.publisherType
  }
}
// 发布者类型清除回调
const publisherTypeClearCallback = () => {
  areaInfo.publisherType = ''
  if (proTable.value) {
    proTable.value.searchParam.publisherType = ''
  }
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
const editHandle = (row: ApartmentInterface) => {
  console.log('跳转')
  router.push({
    path: '/apartmentManagement/apartmentManagement/addOrEditApartment',
    query: {
      id: row.id,
    },
  })
}
// 新增
const addHandle = () => {
  router.push({
    path: '/apartmentManagement/apartmentManagement/addOrEditApartment',
  })
}
// 通过，不通过
const deleteHandle = async (row: ApartmentInterface) => {
  // status	0301-审核通过 0302-审核不通过
  await useHandleData(deleteApartmentById, row.id, `删除${row.name}`)
  // 修改row
  proTable.value?.getTableList()
}
onMounted(() => {
  // 获取省份
  getProvinceListHandle()
})
</script>
