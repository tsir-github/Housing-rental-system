<template>
  <van-field
    readonly
    clickable
    name="userType"
    :value="displayValue"
    label="用户类型"
    placeholder="请选择用户类型"
    @click="showPicker = true"
  />
  
  <van-popup v-model:show="showPicker" position="bottom">
    <van-picker
      :columns="userTypeOptions"
      @confirm="onConfirm"
      @cancel="showPicker = false"
    />
  </van-popup>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { UserType } from '@/api/common/user/types';
import { UserTypeUtils } from '@/utils/userType';

interface Props {
  modelValue: UserType;
}

interface Emits {
  (e: 'update:modelValue', value: UserType): void;
  (e: 'change', value: UserType): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const showPicker = ref(false);

// 用户类型选项
const userTypeOptions = [
  { text: '租客', value: UserType.TENANT },
  { text: '房东', value: UserType.LANDLORD }
];

// 显示值
const displayValue = computed(() => {
  return UserTypeUtils.getUserTypeDisplayName(props.modelValue);
});

// 确认选择
const onConfirm = ({ selectedValues }: { selectedValues: UserType[] }) => {
  const selectedValue = selectedValues[0];
  emit('update:modelValue', selectedValue);
  emit('change', selectedValue);
  showPicker.value = false;
};
</script>