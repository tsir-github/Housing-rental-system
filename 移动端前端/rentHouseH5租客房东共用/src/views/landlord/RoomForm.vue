<template>
  <PageContainer :loading="loading">
    <!-- 页面头部 -->
    <template #header>
      <div class="room-form-header">
        <h2 class="room-form-header__title">
          {{ isEdit ? '编辑房源' : '发布房源' }}
        </h2>
        <p class="room-form-header__subtitle">
          {{ isEdit ? '修改房源信息' : '填写房源详细信息' }}
        </p>
      </div>
    </template>

    <!-- 表单内容 -->
    <van-form @submit="handleSubmit" ref="formRef">
      <!-- 基本信息 -->
      <div class="form-section">
        <div class="form-section__title">基本信息</div>
        
        <van-field
          v-model="apartmentText"
          name="apartment"
          label="所属公寓"
          placeholder="请选择所属公寓"
          readonly
          is-link
          @click="showApartmentPicker = true"
          :rules="getApartmentRules()"
        >
          <template #right-icon>
            <van-icon 
              v-if="formData.apartmentId" 
              name="info-o" 
              size="16" 
              color="#1989fa"
              @click.stop="showApartmentInfo"
            />
          </template>
        </van-field>
        
        <van-field
          v-model="formData.roomNumber"
          name="roomNumber"
          label="房间编号"
          placeholder="请输入房间编号"
          :rules="[{ required: true, message: '请输入房间编号' }]"
          maxlength="20"
        />
        
        <van-field
          v-model="formData.rent"
          name="rent"
          label="月租金"
          type="number"
          placeholder="请输入月租金"
          :rules="[
            { required: true, message: '请输入月租金' },
            { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的金额' }
          ]"
        >
          <template #right-icon>
            <span class="rent-unit">元/月</span>
          </template>
        </van-field>
      </div>

      <!-- 房源图片 -->
      <div class="form-section">
        <div class="form-section__title">房源图片</div>
        
        <van-uploader
          v-model="uploaderFileList"
          multiple
          :max-count="12"
          :after-read="handleImageUpload"
          :before-delete="handleImageDelete"
          upload-text="上传图片"
          :preview-image="true"
          :deletable="true"
        />
        
        <div class="form-section__tip">
          最多上传12张图片，建议尺寸750x500像素，第一张为封面图
        </div>
      </div>

      <!-- 房源属性 -->
      <div class="form-section">
        <div class="form-section__title">
          <span>房源属性</span>
          <div class="section-actions">
            <van-button 
              type="primary" 
              size="mini" 
              @click="openAttributeSelector"
              class="add-btn"
            >
              选择属性
            </van-button>
            <van-button 
              type="success" 
              size="mini" 
              @click="addCustomAttribute"
              class="add-btn"
            >
              自定义属性
            </van-button>
          </div>
        </div>
        
        <div v-if="formData.attributes && formData.attributes.length > 0" class="attribute-list">
          <div 
            v-for="(attr, index) in formData.attributes" 
            :key="index" 
            class="attribute-item"
          >
            <div class="attribute-header">
              <span class="attribute-source">{{ getAttributeSource(attr) }}</span>
              <van-button 
                type="danger" 
                size="mini" 
                @click="removeAttribute(index)"
                class="remove-btn"
              >
                删除
              </van-button>
            </div>
            
            <van-field
              v-model="attr.attrKeyName"
              label="属性名"
              placeholder="请输入属性名"
              :readonly="!attr.isCustom"
              :rules="[{ required: true, message: '请输入属性名' }]"
            />
            
            <van-field
              v-if="attr.isCustom"
              v-model="attr.attrValueName"
              label="属性值"
              placeholder="请输入属性值"
              :rules="[{ required: true, message: '请输入属性值' }]"
            />
            
            <van-field
              v-else
              v-model="attr.selectedValueName"
              label="属性值"
              placeholder="请选择属性值"
              readonly
              is-link
              @click="selectAttributeValue(attr, index)"
              :rules="[{ required: true, message: '请选择属性值' }]"
            />
          </div>
        </div>
        
        <van-empty v-else description="暂无属性信息" />
      </div>
      
      <!-- 属性选择弹窗 -->
      <van-popup v-model:show="showAttributeSelector" position="bottom" style="height: 60%">
        <div class="selector-header">
          <van-button type="default" size="small" @click="showAttributeSelector = false">取消</van-button>
          <span class="selector-title">选择属性</span>
          <van-button type="primary" size="small" @click="confirmAttributeSelection">确定</van-button>
        </div>
        
        <div class="attribute-selector-content">
          <van-checkbox-group v-model="selectedAttributeIds">
            <div v-for="attr in attributeOptions" :key="attr.id" class="attribute-option">
              <van-checkbox :name="attr.id" class="attribute-checkbox">
                <div class="attribute-option-content">
                  <div class="attribute-name-row">
                    <span class="attribute-name">{{ attr.name.replace(/^\[(官方|我的)\]\s*/, '') }}</span>
                    <PublisherBadge :publisher-type="attr.creatorType" />
                  </div>
                  <div class="attribute-values">
                    <van-tag 
                      v-for="value in attr.attrValueList" 
                      :key="value.id" 
                      size="small" 
                      type="primary"
                    >
                      {{ value.name }}
                    </van-tag>
                  </div>
                </div>
              </van-checkbox>
            </div>
          </van-checkbox-group>
        </div>
      </van-popup>
      
      <!-- 属性值选择弹窗 -->
      <van-popup v-model:show="showAttributeValueSelector" position="bottom">
        <van-picker
          :columns="currentAttributeValueColumns"
          @confirm="onAttributeValueConfirm"
          @cancel="showAttributeValueSelector = false"
        />
      </van-popup>

      <!-- 配套设施 -->
      <div class="form-section">
        <div class="form-section__title">配套设施</div>
        
        <div class="facility-grid">
          <van-checkbox-group v-model="selectedFacilities">
            <van-checkbox
              v-for="facility in facilityList"
              :key="facility.id"
              :name="facility.id"
              class="facility-item"
            >
              <div class="facility-content">
                <span class="facility-name">{{ facility.name.replace(/^\[(官方|我的)\]\s*/, '') }}</span>
                <PublisherBadge :publisher-type="getPublisherType(facility)" />
              </div>
            </van-checkbox>
          </van-checkbox-group>
        </div>
      </div>

      <!-- 房源标签 -->
      <div class="form-section">
        <div class="form-section__title">房源标签</div>
        
        <div class="label-grid">
          <van-checkbox-group v-model="selectedLabels">
            <van-checkbox
              v-for="label in labelList"
              :key="label.id"
              :name="label.id"
              class="label-item"
            >
              <div class="label-content">
                <span class="label-name">{{ label.name.replace(/^\[(官方|我的)\]\s*/, '') }}</span>
                <PublisherBadge :publisher-type="getLabelPublisherType(label)" />
              </div>
            </van-checkbox>
          </van-checkbox-group>
        </div>
      </div>

      <!-- 支付方式 -->
      <div class="form-section">
        <div class="form-section__title">支付方式</div>
        
        <div class="payment-grid">
          <van-checkbox-group v-model="selectedPaymentTypes">
            <van-checkbox
              v-for="payment in paymentTypeList"
              :key="payment.id"
              :name="payment.id"
              class="payment-item"
            >
              <div class="payment-content">
                <div class="payment-main">
                  <span class="payment-name">{{ payment.name.replace(/^\[(官方|我的)\]\s*/, '') }}</span>
                  <PublisherBadge :publisher-type="getPaymentPublisherType(payment)" />
                </div>
                <div class="payment-desc">{{ payment.additionalInfo }}</div>
              </div>
            </van-checkbox>
          </van-checkbox-group>
        </div>
      </div>

      <!-- 租期选择 -->
      <div class="form-section">
        <div class="form-section__title">可选租期</div>
        
        <div class="lease-grid">
          <van-checkbox-group v-model="selectedLeaseTerms">
            <van-checkbox
              v-for="term in leaseTermList"
              :key="term.id"
              :name="term.id"
              class="lease-item"
            >
              {{ term.monthCount }}{{ term.unit }}
            </van-checkbox>
          </van-checkbox-group>
        </div>
      </div>

      <!-- 杂费信息 -->
      <div class="form-section">
        <div class="form-section__title">
          杂费信息
          <span class="form-section__subtitle">（来自所属公寓）</span>
        </div>
        
        <div class="apartment-fees" v-if="apartmentFees.length > 0">
          <div 
            v-for="fee in apartmentFees" 
            :key="fee.fee_value_id"
            class="fee-display-item"
          >
            <div class="fee-info">
              <div class="fee-name">{{ fee.fee_key_name }}</div>
              <div class="fee-detail">
                <span class="fee-value">{{ fee.fee_value_name }}</span>
                <span class="fee-unit">{{ fee.fee_unit }}</span>
              </div>
            </div>
            <van-icon name="info-o" class="fee-icon" />
          </div>
        </div>
        
        <div v-else class="no-fees">
          <van-empty 
            image="search" 
            description="该公寓暂无杂费信息"
            :image-size="60"
          />
        </div>
      </div>

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
          {{ isEdit ? '保存修改' : '发布房源' }}
        </van-button>
      </div>
    </van-form>

    <!-- 公寓选择器 -->
    <van-popup v-model:show="showApartmentPicker" position="bottom">
      <van-picker
        :columns="apartmentOptions"
        @confirm="handleApartmentConfirm"
        @cancel="showApartmentPicker = false"
      />
    </van-popup>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showConfirmDialog } from 'vant';
import { useUserStore } from '@/store/modules/user';
import {
  publishRoom,
  updateRoom,
  getRoomDetail,
  getAvailableApartments,
  getAvailableAttributes,
  getAvailableFacilities,
  getAvailableLabels,
  getAvailablePaymentTypes,
  getAvailableLeaseTerms,
  createCustomAttribute,
  getApartmentFees
} from '@/api/landlord/room';
import {
  getAttrKeys,
  getFacilities,
  getLabels,
  getPaymentTypes,
  getLeaseTerms,
  getFeeKeys
} from '@/api/landlord/management';
import { uploadImage } from '@/api/common/upload';
import type { RoomSubmitVo, RoomDetailVo, ApartmentInfo, CustomAttributeRequest } from '@/api/landlord/room';
import type { 
  AttrKeyVo, 
  FacilityInfo, 
  LabelInfo, 
  PaymentType, 
  LeaseTerm, 
  FeeKeyVo 
} from '@/api/landlord/management';
import PageContainer from '@/components/common/PageContainer/PageContainer.vue';
import PublisherBadge from '@/components/common/PublisherBadge/PublisherBadge.vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const submitting = ref(false);
const showApartmentPicker = ref(false);
const showAttributeSelector = ref(false);
const showAttributeValueSelector = ref(false);

const formRef = ref();
const imageList = ref<any[]>([]);
const uploaderFileList = ref<any[]>([]); // 专门用于van-uploader的v-model
const selectedFacilities = ref<number[]>([]);
const selectedLabels = ref<number[]>([]);
const selectedPaymentTypes = ref<number[]>([]);
const selectedLeaseTerms = ref<number[]>([]);
const apartmentOptions = ref<any[]>([]);
const attributeOptions = ref<AttrKeyVo[]>([]);
const selectedAttributeIds = ref<number[]>([]);
const currentAttributeValueColumns = ref<any[]>([]);
const currentAttributeIndex = ref(-1);

// 表单数据
const formData = ref<Partial<RoomSubmitVo>>({
  roomNumber: '',
  rent: 0,
  apartmentId: 0,
  isRelease: 0,
  publisherType: 1, // 假设1表示房东
  publisherId: 0,
  attributes: [] // 添加属性数组
});

// 属性列表
const attrList = ref<Array<{
  keyName: string;
  valueName: string;
}>>([
  { keyName: '面积', valueName: '' },
  { keyName: '朝向', valueName: '' }
]);

// 杂费列表
const feeList = ref<Array<{
  keyName: string;
  value: string;
  unit: string;
}>>([]);

// 公寓杂费信息（只读显示）
const apartmentFees = ref<Array<{
  fee_key_id: number;
  fee_key_name: string;
  fee_value_id: number;
  fee_value_name: string;
  fee_unit: string;
}>>([]);

// 模拟数据
const facilityList = ref([
  { id: 1, name: '空调', icon: 'fire' },
  { id: 2, name: '洗衣机', icon: 'logistics' },
  { id: 3, name: '冰箱', icon: 'shopping-cart-o' },
  { id: 4, name: '热水器', icon: 'fire-o' },
  { id: 5, name: '电视', icon: 'tv-o' },
  { id: 6, name: '网络', icon: 'wifi' },
  { id: 7, name: '独立卫生间', icon: 'home-o' },
  { id: 8, name: '阳台', icon: 'flower-o' }
]);

const labelList = ref([
  { id: 1, name: '精装修' },
  { id: 2, name: '拎包入住' },
  { id: 3, name: '采光好' },
  { id: 4, name: '交通便利' },
  { id: 5, name: '安静' },
  { id: 6, name: '宠物友好' }
]);

const paymentTypeList = ref([
  { id: 1, name: '押一付一', additionalInfo: '押金1个月，每月支付' },
  { id: 2, name: '押一付三', additionalInfo: '押金1个月，每季度支付' },
  { id: 3, name: '押二付一', additionalInfo: '押金2个月，每月支付' },
  { id: 4, name: '半年付', additionalInfo: '押金1个月，每半年支付' }
]);

const leaseTermList = ref([
  { id: 1, monthCount: 3, unit: '个月' },
  { id: 2, monthCount: 6, unit: '个月' },
  { id: 3, monthCount: 12, unit: '个月' },
  { id: 4, monthCount: 24, unit: '个月' }
]);

// 计算属性
const isEdit = computed(() => !!route.params.id);

const apartmentText = computed(() => {
  if (!formData.value.apartmentId) return '';
  const apartment = apartmentOptions.value.find(apt => apt.value === formData.value.apartmentId);
  return apartment?.text || '';
});

// 计算已选中的属性ID列表（用于属性选择器的勾选状态）
const selectedAttributeKeyIds = computed(() => {
  if (!formData.value.attributes) return [];
  return formData.value.attributes.map(attr => attr.attrKeyId).filter(id => id > 0);
});

// 配套设施相关计算属性
const selectedFacilityNames = computed(() => {
  const selectedFacilityList = facilityList.value.filter(facility => 
    selectedFacilities.value.includes(facility.id)
  );
  return selectedFacilityList.map(f => f.name).join(', ');
});

// 动态公寓验证规则
const getApartmentRules = () => {
  // 如果是编辑模式且房间已有归属公寓，则不是必填
  if (isEdit.value && formData.value.apartmentId) {
    return [];
  }
  // 新建房源或编辑时没有公寓，则必填
  return [{ required: true, message: '请选择所属公寓' }];
};

// 显示公寓信息
const showApartmentInfo = () => {
  const apartment = apartmentOptions.value.find(apt => apt.value === formData.value.apartmentId);
  if (apartment) {
    showToast({
      message: `公寓：${apartment.text}`,
      duration: 2000
    });
  }
};

// 加载表单选项数据
const loadFormOptions = async () => {
  try {
    loading.value = true;
    
    const landlordId = userStore.userInfo?.id;
    console.log('🔍 loadFormOptions - 用户信息:', userStore.userInfo);
    console.log('🔍 loadFormOptions - 房东ID:', landlordId);
    
    if (!landlordId) {
      showToast({
        message: '用户信息获取失败，请重新登录',
        type: 'fail'
      });
      return;
    }
    
    console.log('🔍 开始并行获取表单选项数据...');
    
    // 并行获取所有选项数据（带权限控制）
    const [apartmentRes, attributeRes, facilityRes, labelRes, paymentRes, leaseTermRes] = await Promise.all([
      getAvailableApartments(landlordId).catch(error => {
        console.error('获取公寓列表失败:', error);
        showToast({
          message: '获取公寓列表失败，请检查网络连接',
          type: 'fail'
        });
        return { data: [] };
      }),
      getAvailableAttributes(landlordId).catch(error => {
        console.error('获取属性列表失败:', error);
        showToast({
          message: '获取属性列表失败，请检查网络连接',
          type: 'fail'
        });
        return { data: [] };
      }),
      getAvailableFacilities(landlordId).catch(error => {
        console.error('🔍 获取配套设施列表失败:', error);
        console.error('🔍 错误详情:', error.response || error.message);
        showToast({
          message: '获取配套设施列表失败，请检查网络连接',
          type: 'fail'
        });
        return { data: [] };
      }),
      getAvailableLabels(landlordId).catch(error => {
        console.error('获取标签列表失败:', error);
        showToast({
          message: '获取标签列表失败，请检查网络连接',
          type: 'fail'
        });
        return { data: [] };
      }),
      getAvailablePaymentTypes(landlordId).catch(error => {
        console.error('获取支付方式列表失败:', error);
        showToast({
          message: '获取支付方式列表失败，请检查网络连接',
          type: 'fail'
        });
        return { data: [] };
      }),
      getAvailableLeaseTerms(landlordId).catch(error => {
        console.error('获取租期列表失败:', error);
        showToast({
          message: '获取租期列表失败，请检查网络连接',
          type: 'fail'
        });
        return { data: [] };
      })
    ]);
    
    console.log('🔍 API调用结果:');
    console.log('🔍 - 公寓数据:', apartmentRes.data);
    console.log('🔍 - 属性数据:', attributeRes.data);
    console.log('🔍 - 配套设施数据:', facilityRes.data);
    console.log('🔍 - 标签数据:', labelRes.data);
    console.log('🔍 - 支付方式数据:', paymentRes.data);
    console.log('🔍 - 租期数据:', leaseTermRes.data);
    
    // 设置公寓选项 - 修复van-picker数据格式
    apartmentOptions.value = apartmentRes.data.map((apt: ApartmentInfo) => ({
      text: apt.name,
      value: apt.id,
      // 为了兼容van-picker，同时保存原始数据
      apartmentData: apt
    }));
    
    // 设置属性选项
    attributeOptions.value = attributeRes.data || [];
    
    // 设置配套设施选项（替换模拟数据）
    facilityList.value = facilityRes.data.map((facility: FacilityInfo) => ({
      id: facility.id,
      name: facility.name,
      icon: facility.icon || 'setting-o' // 默认图标
    }));
    
    console.log('🔍 设置后的配套设施列表:', facilityList.value);
    
    // 设置标签选项（替换模拟数据）
    labelList.value = labelRes.data.map((label: LabelInfo) => ({
      id: label.id,
      name: label.name
    }));
    
    // 设置支付方式选项（替换模拟数据）
    paymentTypeList.value = paymentRes.data.map((payment: PaymentType) => ({
      id: payment.id,
      name: payment.name,
      additionalInfo: payment.additionalInfo || `${payment.payMonthCount}个月`
    }));
    
    // 设置租期选项（替换模拟数据）
    leaseTermList.value = leaseTermRes.data.map((term: LeaseTerm) => ({
      id: term.id,
      monthCount: term.monthCount,
      unit: '个月'
    }));
    
    console.log('🔍 设置后的租期列表:', leaseTermList.value);
    
    // 如果URL中有apartmentId参数，自动选择
    if (route.query.apartmentId) {
      formData.value.apartmentId = Number(route.query.apartmentId);
    }
    
    // 显示加载成功提示
    if (apartmentOptions.value.length > 0 || attributeOptions.value.length > 0) {
      showToast({
        message: '数据加载完成',
        type: 'success',
        duration: 1500
      });
    }
    
  } catch (error) {
    console.error('加载表单选项失败:', error);
    showToast({
      message: '数据加载失败，请刷新页面重试',
      type: 'fail'
    });
  } finally {
    loading.value = false;
  }
};

// 加载公寓杂费信息
const loadApartmentFees = async (apartmentId: number) => {
  if (!apartmentId) {
    apartmentFees.value = [];
    return;
  }
  
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      console.error('用户信息获取失败');
      return;
    }
    
    console.log('🔍 获取公寓杂费信息:', { apartmentId, landlordId });
    
    const { data } = await getApartmentFees(apartmentId, landlordId);
    apartmentFees.value = data || [];
    
    console.log('🔍 公寓杂费信息获取成功:', apartmentFees.value);
    
  } catch (error) {
    console.error('获取公寓杂费信息失败:', error);
    apartmentFees.value = [];
    // 不显示错误提示，因为杂费信息不是必需的
  }
};
const loadRoomDetail = async () => {
  if (!isEdit.value) return;
  
  try {
    loading.value = true;
    const roomId = Number(route.params.id);
    const landlordId = userStore.userInfo?.id;
    
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    const { data } = await getRoomDetail(roomId, landlordId);
    
    // 填充表单数据
    formData.value = {
      id: data.id,
      roomNumber: data.roomNumber,
      rent: data.rent,
      apartmentId: data.apartmentId,
      isRelease: data.isRelease,
      publisherType: data.publisherType,
      publisherId: data.publisherId
    };
    
    // 填充图片列表
    if (data.graphVoList) {
      imageList.value = data.graphVoList.map(img => ({
        id: img.id,
        name: img.name,
        url: img.url,
        itemType: img.itemType,
        itemId: img.itemId
      }));
      
      // 同时填充van-uploader的文件列表（用于显示）
      uploaderFileList.value = data.graphVoList.map(img => ({
        url: img.url,
        name: img.name,
        status: 'done'
      }));
      
      console.log('🔍 图片数据回显成功:', imageList.value);
      console.log('🔍 上传器数据回显成功:', uploaderFileList.value);
    }
    
    // 填充属性列表 - 同时支持新旧两种格式
    if (data.attrValueVoList && data.attrValueVoList.length > 0) {
      console.log('🔍 后端返回的属性数据:', data.attrValueVoList);
      console.log('🔍 当前属性选项:', attributeOptions.value);
      
      // 填充旧格式的属性列表（保持兼容性）
      attrList.value = data.attrValueVoList.map(attr => ({
        keyName: attr.attrKeyName,
        valueName: attr.name
      }));
      
      // 填充新格式的属性列表（用于新的属性选择器）
      formData.value.attributes = data.attrValueVoList.map(attr => {
        // 从属性选项中找到对应的属性键，获取完整的属性值列表
        const attrKey = attributeOptions.value.find(option => option.id === attr.attrKeyId);
        
        console.log(`🔍 处理属性: ${attr.attrKeyName}, 属性键ID: ${attr.attrKeyId}, 找到的属性键:`, attrKey);
        
        return {
          attrKeyId: attr.attrKeyId,
          attrKeyName: attr.attrKeyName,
          attrValueId: attr.id,
          attrValueName: attr.name,
          selectedValueName: attr.name,
          isCustom: false, // 从数据库加载的都不是自定义的
          needsSync: false, // 已经存在的不需要同步
          attrValueList: attrKey?.attrValueList || [] // 添加属性值列表
        };
      });
      
      console.log('🔍 转换后的属性数据:', formData.value.attributes);
      console.log('🔍 formData.value.attributes 长度:', formData.value.attributes.length);
      
      // 检查每个属性是否有属性值列表
      formData.value.attributes.forEach((attr, index) => {
        console.log(`🔍 属性 ${index}: ${attr.attrKeyName}, 属性值列表长度: ${attr.attrValueList?.length || 0}`);
        if (!attr.attrValueList || attr.attrValueList.length === 0) {
          console.warn(`⚠️ 属性 ${attr.attrKeyName} 没有属性值列表`);
        }
      });
    } else {
      console.log('🔍 后端没有返回属性数据或属性数据为空');
      formData.value.attributes = [];
    }
    
    // 填充配套设施
    if (data.facilityInfoList) {
      selectedFacilities.value = data.facilityInfoList.map(f => f.id);
    }
    
    // 填充标签
    if (data.labelInfoList) {
      selectedLabels.value = data.labelInfoList.map(l => l.id);
    }
    
    // 填充支付方式
    if (data.paymentTypeList) {
      selectedPaymentTypes.value = data.paymentTypeList.map(p => p.id);
    }
    
    // 填充租期
    if (data.leaseTermList) {
      selectedLeaseTerms.value = data.leaseTermList.map(t => t.id);
    }
    
    // 填充杂费信息
    if (data.feeValueVoList) {
      feeList.value = data.feeValueVoList.map(fee => ({
        keyName: fee.feeKeyName,
        value: fee.value,
        unit: fee.unit
      }));
    }
    
  } catch (error) {
    console.error('加载房源详情失败:', error);
    showToast('加载房源详情失败');
  } finally {
    loading.value = false;
  }
};

// 事件处理
const handleApartmentConfirm = (value: any) => {
  console.log('🔍 公寓选择确认，接收到的参数:', value);
  
  // van-picker 的 confirm 事件传递的参数格式
  let selectedApartmentId: number | undefined;
  
  if (value && typeof value === 'object') {
    // Vant 4.x 的 van-picker 返回格式：{ selectedValues: [17], selectedOptions: [...], selectedIndexes: [...] }
    if (value.selectedValues && Array.isArray(value.selectedValues) && value.selectedValues.length > 0) {
      selectedApartmentId = value.selectedValues[0];
    }
    // 兼容其他可能的格式
    else if (value.value !== undefined) {
      selectedApartmentId = value.value;
    }
    else if (value.id !== undefined) {
      selectedApartmentId = value.id;
    }
  } else if (typeof value === 'number') {
    // 如果直接是数字
    selectedApartmentId = value;
  } else if (Array.isArray(value) && value.length > 0) {
    // 如果是数组，取第一个元素
    const firstItem = value[0];
    selectedApartmentId = typeof firstItem === 'object' ? (firstItem.value || firstItem.id) : firstItem;
  }
  
  console.log('🔍 解析出的公寓ID:', selectedApartmentId);
  
  if (selectedApartmentId) {
    formData.value.apartmentId = selectedApartmentId;
    console.log('🔍 公寓ID设置成功:', formData.value.apartmentId);
    
    // 加载新公寓的杂费信息
    loadApartmentFees(selectedApartmentId);
  } else {
    console.error('🔍 无法解析公寓ID，参数格式:', value);
    showToast('公寓选择失败，请重试');
  }
  
  showApartmentPicker.value = false;
};

const handleImageUpload = async (file: any) => {
  try {
    console.log('🔍 开始上传图片:', file);
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    // 检查文件是否存在
    if (!file.file) {
      console.error('❌ 文件对象不存在');
      return;
    }
    
    // 检查文件类型
    if (!file.file.type.startsWith('image/')) {
      showToast('请选择图片文件');
      return;
    }
    
    // 检查文件大小（10MB限制，与后端配置一致）
    if (file.file.size > 10 * 1024 * 1024) {
      showToast('图片大小不能超过10MB');
      return;
    }
    
    try {
      // 上传到MinIO
      const { data: imageUrl } = await uploadImage(file.file);
      
      // 验证返回的URL
      if (!imageUrl || typeof imageUrl !== 'string' || imageUrl.trim() === '') {
        throw new Error('上传返回的URL为空');
      }
      
      // 创建图片对象并添加到imageList（用于表单提交）
      const newImage = {
        id: Date.now(),
        name: file.file.name.replace(/\.[^/.]+$/, ''),
        url: imageUrl,
        itemType: 2,
        itemId: formData.value.id || 0,
        status: 'done'
      };
      
      // 只添加到imageList，不操作uploaderFileList（让van-uploader自己管理）
      imageList.value.push(newImage);
      
      // 更新van-uploader中对应文件的URL
      file.url = imageUrl;
      file.status = 'done';
      
      console.log('✅ 图片上传成功:', newImage);
      console.log('🔍 当前图片列表长度:', imageList.value.length);
      console.log('🔍 当前上传器列表长度:', uploaderFileList.value.length);
      showToast('图片上传成功');
      
    } catch (uploadError) {
      console.error('❌ 上传失败:', uploadError);
      file.status = 'failed';
      throw uploadError;
    }
    
  } catch (error) {
    console.error('❌ 图片上传失败:', error);
    showToast(`图片上传失败：${error.message || '请重试'}`);
    
    if (file) {
      file.status = 'failed';
    }
  }
};

const handleImageDelete = (file: any) => {
  return new Promise((resolve) => {
    showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这张图片吗？'
    }).then(() => {
      try {
        // 从imageList中移除对应的图片
        const index = imageList.value.findIndex(img => 
          img.url === file.url || 
          img.name === file.name ||
          (file.file && img.name === file.file.name.replace(/\.[^/.]+$/, ''))
        );
        
        if (index >= 0) {
          const removedImage = imageList.value.splice(index, 1)[0];
          console.log('🔍 从imageList删除图片:', removedImage);
          
          // 如果是blob URL，释放内存
          if (removedImage.url && removedImage.url.startsWith('blob:')) {
            URL.revokeObjectURL(removedImage.url);
          }
        }
        
        console.log('🔍 删除后的imageList长度:', imageList.value.length);
        console.log('🔍 删除后的uploaderFileList长度:', uploaderFileList.value.length);
        resolve(true);
      } catch (error) {
        console.error('❌ 删除图片失败:', error);
        resolve(false);
      }
    }).catch(() => {
      resolve(false);
    });
  });
};

const addAttr = () => {
  attrList.value.push({
    keyName: '',
    valueName: ''
  });
};

const removeAttr = (index: number) => {
  attrList.value.splice(index, 1);
};

const addFee = () => {
  feeList.value.push({
    keyName: '',
    value: '',
    unit: '元/月'
  });
};

const removeFee = (index: number) => {
  feeList.value.splice(index, 1);
};

const handleSubmit = async () => {
  try {
    submitting.value = true;
    
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }
    
    // 1. 先同步自定义属性到后端
    if (formData.value.attributes) {
      for (const attr of formData.value.attributes) {
        if (attr.isCustom && attr.needsSync && attr.attrKeyName && attr.attrValueName) {
          try {
            const customAttrRequest: CustomAttributeRequest = {
              attrKeyName: attr.attrKeyName,
              attrValueName: attr.attrValueName,
              landlordId: landlordId,
              roomId: formData.value.id
            };
            
            const { data: createdAttr } = await createCustomAttribute(customAttrRequest);
            
            // 更新属性信息，使用后端返回的ID
            attr.attrKeyId = createdAttr.id;
            attr.attrValueId = createdAttr.attrValueList?.[0]?.id || 0;
            attr.needsSync = false; // 标记已同步
            
            console.log('自定义属性同步成功:', createdAttr);
          } catch (error) {
            console.error('同步自定义属性失败:', error);
            showToast(`同步自定义属性"${attr.attrKeyName}"失败`);
            return;
          }
        }
      }
    }
    
    console.log('🔍 开始提交房源表单');
    console.log('🔍 当前图片列表:', imageList.value);
    console.log('🔍 图片列表长度:', imageList.value.length);
    
    // 2. 构建提交数据
    const submitData: RoomSubmitVo = {
      ...formData.value as RoomSubmitVo,
      publisherId: landlordId,
      // 属性值ID数组 - 后端用于批量插入关联关系
      attrValueIds: formData.value.attributes?.map(attr => attr.attrValueId).filter(id => id > 0) || [],
      // 属性详细信息 - 用于显示和验证
      attrValueVoList: formData.value.attributes?.map(attr => ({
        attrKeyName: attr.attrKeyName,
        attrValueName: attr.attrValueName,
        attrKeyId: attr.attrKeyId,
        attrValueId: attr.attrValueId
      })) || [],
      facilityInfoIds: selectedFacilities.value,
      labelInfoIds: selectedLabels.value,
      paymentTypeIds: selectedPaymentTypes.value,
      leaseTermIds: selectedLeaseTerms.value,
      feeValueVoList: feeList.value.map(fee => ({
        feeKeyName: fee.keyName,
        value: fee.value,
        unit: fee.unit,
        feeKeyId: 0 // TODO: 需要从后端获取费用类型ID
      })),
      graphVoList: imageList.value
        .filter(img => {
          const isValid = img.url && img.url.trim() !== '' && !img.url.startsWith('blob:');
          console.log('🔍 图片过滤检查:', { name: img.name, url: img.url, isValid });
          return isValid;
        }) // 过滤掉空URL和blob URL的图片
        .map((img, index) => {
          const graphItem = {
            name: img.name || `image_${index}`,
            url: img.url,
            itemType: 2, // 房源图片类型
            itemId: formData.value.id || 0
          };
          console.log('🔍 构建图片数据:', graphItem);
          return graphItem;
        })
    };
    
    console.log('🔍 最终提交的数据:', submitData);
    console.log('🔍 最终提交的图片数据:', submitData.graphVoList);
    
    // 3. 提交房源信息
    if (isEdit.value) {
      await updateRoom(submitData, landlordId);
      showToast('修改成功');
    } else {
      await publishRoom(submitData, landlordId);
      showToast('发布成功');
    }
    
    router.back();
    
  } catch (error) {
    console.error('提交失败:', error);
    showToast(isEdit.value ? '修改失败' : '发布失败');
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

// 属性选择功能
const addCustomAttribute = async () => {
  try {
    const landlordId = userStore.userInfo?.id;
    if (!landlordId) {
      showToast('用户信息获取失败');
      return;
    }

    // 创建一个临时的自定义属性，让用户填写
    if (!formData.value.attributes) {
      formData.value.attributes = [];
    }
    
    formData.value.attributes.push({
      attrKeyName: '',
      attrValueName: '',
      isCustom: true,
      attrKeyId: 0,
      attrValueId: 0,
      needsSync: true // 标记需要同步到后端
    });
    
    showToast('请填写自定义属性信息');
  } catch (error) {
    console.error('添加自定义属性失败:', error);
    showToast('添加自定义属性失败');
  }
};

const removeAttribute = (index: number) => {
  if (formData.value.attributes) {
    formData.value.attributes.splice(index, 1);
  }
};

const getAttributeSource = (attr: any) => {
  return attr.isCustom ? '自定义' : '系统';
};

// 获取配套设施的发布者类型
const getPublisherType = (facility: any) => {
  // 根据名称前缀判断发布者类型
  if (facility.name?.startsWith('[官方]')) {
    return 1; // 官方
  } else if (facility.name?.startsWith('[我的]')) {
    return 2; // 房东
  }
  // 如果没有前缀，根据creatorType判断
  return facility.creatorType || 1;
};

// 获取标签的发布者类型
const getLabelPublisherType = (label: any) => {
  // 根据名称前缀判断发布者类型
  if (label.name?.startsWith('[官方]')) {
    return 1; // 官方
  } else if (label.name?.startsWith('[我的]')) {
    return 2; // 房东
  }
  // 如果没有前缀，根据creatorType判断
  return label.creatorType || 1;
};

// 获取支付方式的发布者类型
const getPaymentPublisherType = (payment: any) => {
  // 根据名称前缀判断发布者类型
  if (payment.name?.startsWith('[官方]')) {
    return 1; // 官方
  } else if (payment.name?.startsWith('[我的]')) {
    return 2; // 房东
  }
  // 如果没有前缀，根据creatorType判断
  return payment.creatorType || 1;
};

// 打开属性选择器并同步已选中状态
const openAttributeSelector = () => {
  // 同步已选中的属性ID到选择器
  selectedAttributeIds.value = selectedAttributeKeyIds.value;
  showAttributeSelector.value = true;
};

const confirmAttributeSelection = () => {
  if (!formData.value.attributes) {
    formData.value.attributes = [];
  }
  
  selectedAttributeIds.value.forEach(attrId => {
    const attr = attributeOptions.value.find(a => a.id === attrId);
    if (attr) {
      // 检查是否已经添加过这个属性
      const exists = formData.value.attributes?.some(a => a.attrKeyId === attr.id);
      if (!exists) {
        formData.value.attributes?.push({
          attrKeyId: attr.id,
          attrKeyName: attr.name,
          attrValueId: 0,
          attrValueName: '',
          selectedValueName: '',
          isCustom: false,
          attrValueList: attr.attrValueList
        });
      }
    }
  });
  
  selectedAttributeIds.value = [];
  showAttributeSelector.value = false;
};

const selectAttributeValue = async (attr: any, index: number) => {
  try {
    console.log('🔍 selectAttributeValue 被调用:', { attr, index });
    
    // 如果是自定义属性，不需要选择值
    if (attr.isCustom) {
      console.log('🔍 这是自定义属性，跳过选择');
      return;
    }
    
    // 检查属性是否有属性值列表
    if (!attr.attrValueList || attr.attrValueList.length === 0) {
      console.warn('⚠️ 属性没有属性值列表，尝试从attributeOptions中获取');
      
      // 从属性选项中找到对应的属性键
      const attrKey = attributeOptions.value.find(option => option.id === attr.attrKeyId);
      
      if (!attrKey || !attrKey.attrValueList || attrKey.attrValueList.length === 0) {
        console.error('❌ 无法找到属性值列表');
        showToast('该属性暂无可选值');
        return;
      }
      
      // 更新属性的属性值列表
      attr.attrValueList = attrKey.attrValueList;
      console.log('✅ 从attributeOptions中获取到属性值列表:', attr.attrValueList);
    }
    
    currentAttributeIndex.value = index;
    currentAttributeValueColumns.value = attr.attrValueList.map((value: any) => ({
      text: value.name,
      value: value.id
    }));
    
    console.log('🔍 设置属性值选择器:', {
      currentAttributeIndex: currentAttributeIndex.value,
      columns: currentAttributeValueColumns.value
    });
    
    showAttributeValueSelector.value = true;
  } catch (error) {
    console.error('❌ 选择属性值失败:', error);
    showToast('获取属性值失败');
  }
};

const onAttributeValueConfirm = (...args: any[]) => {
  console.log('🔍 onAttributeValueConfirm 被调用，所有参数:', args);
  console.log('🔍 参数数量:', args.length);
  
  // 详细输出第一个参数的结构
  if (args.length > 0) {
    console.log('🔍 第一个参数类型:', typeof args[0]);
    console.log('🔍 第一个参数内容:', args[0]);
    console.log('🔍 第一个参数的所有属性:', Object.keys(args[0] || {}));
    
    // 如果是对象，输出所有属性值
    if (args[0] && typeof args[0] === 'object') {
      for (const key in args[0]) {
        console.log(`🔍 ${key}:`, args[0][key]);
      }
    }
  }
  
  console.log('🔍 currentAttributeIndex:', currentAttributeIndex.value);
  console.log('🔍 currentAttributeValueColumns:', currentAttributeValueColumns.value);
  
  if (currentAttributeIndex.value >= 0 && formData.value.attributes) {
    const attr = formData.value.attributes[currentAttributeIndex.value];
    
    console.log('🔍 更新前的属性:', attr);
    
    // 尝试不同的参数解析方式
    let valueId, valueName;
    
    // 方式1: 第一个参数是选中的值对象
    if (args.length > 0 && args[0] && typeof args[0] === 'object') {
      const selectedValue = args[0];
      valueId = selectedValue.value;
      valueName = selectedValue.text;
      console.log('🔍 方式1 - 从第一个参数获取:', { valueId, valueName });
      
      // 尝试其他可能的属性名
      if (!valueId) {
        valueId = selectedValue.id || selectedValue.key || selectedValue.val;
        console.log('🔍 方式1.1 - 尝试其他ID属性:', valueId);
      }
      if (!valueName) {
        valueName = selectedValue.name || selectedValue.label || selectedValue.title || selectedValue.content;
        console.log('🔍 方式1.2 - 尝试其他名称属性:', valueName);
      }
    }
    
    // 方式2: 第一个参数是选中的值，第二个参数是索引
    if (!valueId && args.length > 1 && typeof args[1] === 'number') {
      const selectedIndex = args[1];
      if (currentAttributeValueColumns.value[selectedIndex]) {
        const selectedItem = currentAttributeValueColumns.value[selectedIndex];
        valueId = selectedItem.value;
        valueName = selectedItem.text;
        console.log('🔍 方式2 - 从索引获取:', { valueId, valueName, selectedIndex });
      }
    }
    
    // 方式3: 第一个参数是选中的值数组
    if (!valueId && args.length > 0 && Array.isArray(args[0]) && args[0].length > 0) {
      const selectedValue = args[0][0];
      valueId = selectedValue.value;
      valueName = selectedValue.text;
      console.log('🔍 方式3 - 从数组获取:', { valueId, valueName });
    }
    
    // 方式4: 直接从第一个参数获取字符串值，然后在columns中查找
    if (!valueId && args.length > 0) {
      const selectedText = args[0];
      const foundItem = currentAttributeValueColumns.value.find(item => item.text === selectedText);
      if (foundItem) {
        valueId = foundItem.value;
        valueName = foundItem.text;
        console.log('🔍 方式4 - 从文本查找:', { valueId, valueName, selectedText });
      }
    }
    
    // 方式5: 如果第一个参数包含selectedOptions属性
    if (!valueId && args[0] && args[0].selectedOptions && Array.isArray(args[0].selectedOptions)) {
      const selectedOption = args[0].selectedOptions[0];
      if (selectedOption) {
        valueId = selectedOption.value;
        valueName = selectedOption.text;
        console.log('🔍 方式5 - 从selectedOptions获取:', { valueId, valueName });
      }
    }
    
    console.log('🔍 最终解析出的值:', { valueId, valueName });
    
    if (valueId && valueName) {
      // 更新属性值
      attr.attrValueId = valueId;
      attr.selectedValueName = valueName;
      attr.attrValueName = valueName;
      
      console.log('🔍 更新后的属性:', attr);
      console.log('✅ 属性值选择成功:', {
        attrKeyName: attr.attrKeyName,
        selectedValue: valueName,
        attrValueId: valueId
      });
    } else {
      console.error('❌ 无法解析选中的属性值');
      console.error('❌ 所有参数:', args);
      console.error('❌ columns:', currentAttributeValueColumns.value);
      showToast('属性值选择失败，请重试');
    }
  } else {
    console.error('❌ 无法更新属性值:', {
      currentAttributeIndex: currentAttributeIndex.value,
      hasAttributes: !!formData.value.attributes
    });
  }
  
  showAttributeValueSelector.value = false;
  currentAttributeIndex.value = -1;
};

// 监听公寓ID变化，自动加载杂费信息
watch(() => formData.value.apartmentId, (newApartmentId) => {
  if (newApartmentId) {
    loadApartmentFees(newApartmentId);
  } else {
    apartmentFees.value = [];
  }
}, { immediate: true });

// 生命周期
onMounted(async () => {
  await loadFormOptions();
  await loadRoomDetail();
});
</script>

<style scoped lang="less">
.room-form-header {
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

.rent-unit {
  font-size: 14px;
  color: #969799;
}

.attr-list,
.fee-list {
  padding: 0 16px;
}

/* 公寓杂费显示样式 */
.apartment-fees {
  padding: 0 16px;
}

.fee-display-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #1989fa;
}

.fee-info {
  flex: 1;
}

.fee-name {
  font-size: 14px;
  font-weight: 500;
  color: #323233;
  margin-bottom: 4px;
}

.fee-detail {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fee-value {
  font-size: 13px;
  color: #1989fa;
  font-weight: 500;
}

.fee-unit {
  font-size: 12px;
  color: #969799;
  background: #ebedf0;
  padding: 2px 6px;
  border-radius: 4px;
}

.fee-icon {
  color: #c8c9cc;
  font-size: 16px;
}

.no-fees {
  padding: 20px 16px;
  text-align: center;
}

.form-section__subtitle {
  font-size: 12px;
  color: #969799;
  font-weight: normal;
  margin-left: 8px;
}

.attr-item,
.fee-item {
  position: relative;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.attr-remove,
.fee-remove {
  position: absolute;
  top: 8px;
  right: 8px;
}

.attr-add,
.fee-add {
  width: 100%;
  margin-top: 8px;
}

.facility-grid,
.label-grid,
.payment-grid,
.lease-grid {
  padding: 0 16px;
  
  :deep(.van-checkbox-group) {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

.facility-item,
.label-item,
.payment-item,
.lease-item {
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  border: 1px solid transparent;
  transition: all 0.3s ease;
  
  &.van-checkbox--checked {
    border-color: #1989fa;
    background-color: #f0f8ff;
  }
  
  :deep(.van-checkbox__label) {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;
  }
}

.facility-content,
.label-content,
.payment-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.facility-name,
.label-name,
.payment-name {
  font-size: 14px;
  color: #323233;
  font-weight: 500;
  flex: 1;
}

.payment-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 4px;
}

.payment-desc {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}

.fee-unit {
  font-size: 14px;
  color: #969799;
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

/* 响应式设计 */
@media (max-width: 375px) {
  .facility-grid,
  .label-grid,
  .payment-grid,
  .lease-grid {
    :deep(.van-checkbox-group) {
      grid-template-columns: 1fr;
    }
  }
}

/* 属性选择相关样式 */
.section-actions {
  display: flex;
  gap: 8px;
  
  .add-btn {
    margin-left: 8px;
  }
}

.attribute-list {
  padding: 0 16px;
}

.attribute-item {
  position: relative;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.attribute-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.attribute-source {
  font-size: 12px;
  color: #1989fa;
  background: #e8f4ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.remove-btn {
  margin-left: auto;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebedf0;
}

.selector-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}

.attribute-selector-content {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.attribute-option {
  margin-bottom: 16px;
  
  .attribute-checkbox {
    width: 100%;
    
    :deep(.van-checkbox__label) {
      width: 100%;
    }
  }
}

.attribute-option-content {
  width: 100%;
}

.attribute-name {
  font-size: 14px;
  font-weight: 500;
  color: #323233;
  margin-bottom: 8px;
}

.attribute-name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.attribute-values {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>