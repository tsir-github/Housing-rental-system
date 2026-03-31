<template>
  <div class="image-uploader">
    <div class="section-header">
      <h3 class="section-title">公寓图片</h3>
      <span class="image-count">{{ currentImages.length }}/{{ maxImages }}</span>
    </div>
    
    <div class="upload-tip">
      <van-icon name="info-o" />
      <span>最多上传{{ maxImages }}张图片，建议尺寸750x500像素</span>
    </div>
    
    <!-- 图片列表 -->
    <div class="image-list">
      <!-- 现有图片 -->
      <div 
        v-for="(image, index) in currentImages" 
        :key="image.id || index"
        class="image-item"
      >
        <div class="image-container">
          <img 
            :src="image.url" 
            :alt="image.name"
            class="image-preview"
            @click="previewImage(image.url)"
          />
          <div class="image-overlay">
            <van-icon 
              name="eye-o" 
              class="overlay-icon"
              @click="previewImage(image.url)"
            />
            <van-icon 
              name="delete-o" 
              class="overlay-icon delete-icon"
              @click="deleteImage(index)"
            />
          </div>
        </div>
        <div class="image-info">
          <van-field
            v-model="image.name"
            placeholder="请输入图片描述"
            class="image-name-input"
            @blur="updateImages"
          />
        </div>
      </div>
      
      <!-- 上传按钮 -->
      <div 
        v-if="currentImages.length < maxImages"
        class="upload-button"
        @click="triggerUpload"
      >
        <van-icon name="plus" class="upload-icon" />
        <span class="upload-text">添加图片</span>
      </div>
    </div>
    
    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      multiple
      style="display: none"
      @change="handleFileSelect"
    />
    
    <!-- 上传进度 -->
    <van-overlay :show="uploading" class="upload-overlay">
      <div class="upload-progress">
        <van-loading type="spinner" size="24px" />
        <div class="progress-text">上传中...</div>
        <van-progress 
          :percentage="uploadProgress" 
          stroke-width="4"
          color="#1989fa"
        />
      </div>
    </van-overlay>
    
    <!-- 删除确认弹窗 -->
    <van-dialog
      v-model:show="showDeleteConfirm"
      title="确认删除"
      message="确定要删除这张图片吗？"
      show-cancel-button
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
    
    <!-- 图片预览 -->
    <van-image-preview
      v-model:show="showPreview"
      :images="previewImages"
      :start-position="previewIndex"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { showToast } from 'vant';
import type { GraphVo } from '@/types/landlord';

// Props定义
interface Props {
  modelValue: GraphVo[];
  currentImages?: GraphVo[];
  maxImages?: number;
}

// Emits定义
interface Emits {
  (e: 'update:modelValue', value: GraphVo[]): void;
}

const props = withDefaults(defineProps<Props>(), {
  currentImages: () => [],
  maxImages: 9
});

const emit = defineEmits<Emits>();

// 响应式数据
const fileInput = ref<HTMLInputElement>();
const uploading = ref(false);
const uploadProgress = ref(0);
const showDeleteConfirm = ref(false);
const deleteIndex = ref(-1);
const showPreview = ref(false);
const previewIndex = ref(0);
const currentImages = ref<GraphVo[]>([...props.modelValue]);

// 计算属性
const previewImages = computed(() => {
  return currentImages.value.map(image => image.url);
});

// 方法
const triggerUpload = () => {
  if (currentImages.value.length >= props.maxImages) {
    showToast(`最多只能上传${props.maxImages}张图片`);
    return;
  }
  
  fileInput.value?.click();
};

const handleFileSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const files = target.files;
  
  if (!files || files.length === 0) {
    return;
  }
  
  // 检查文件数量限制
  const remainingSlots = props.maxImages - currentImages.value.length;
  if (files.length > remainingSlots) {
    showToast(`最多还能上传${remainingSlots}张图片`);
    return;
  }
  
  // 验证文件
  const validFiles: File[] = [];
  for (let i = 0; i < files.length; i++) {
    const file = files[i];
    
    // 检查文件类型
    if (!file.type.startsWith('image/')) {
      showToast(`${file.name} 不是有效的图片文件`);
      continue;
    }
    
    // 检查文件大小（10MB限制，与后端配置一致）
    if (file.size > 10 * 1024 * 1024) {
      showToast(`${file.name} 文件过大，请选择小于10MB的图片`);
      continue;
    }
    
    validFiles.push(file);
  }
  
  if (validFiles.length === 0) {
    return;
  }
  
  // 上传文件
  await uploadFiles(validFiles);
  
  // 清空input
  target.value = '';
};

const uploadFiles = async (files: File[]) => {
  uploading.value = true;
  uploadProgress.value = 0;
  
  try {
    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      
      // 调用真实的上传API
      const imageUrl = await uploadToMinio(file);
      
      // 添加到图片列表
      const newImage: GraphVo = {
        id: Date.now() + i, // 临时ID
        name: file.name.replace(/\.[^/.]+$/, ''), // 移除文件扩展名
        url: imageUrl,
        itemType: 1, // 公寓类型
        itemId: 0 // 临时值，保存时设置
      };
      
      currentImages.value.push(newImage);
      
      // 更新进度
      uploadProgress.value = Math.round(((i + 1) / files.length) * 100);
    }
    
    updateImages();
    showToast('图片上传成功');
    
  } catch (error) {
    console.error('图片上传失败:', error);
    showToast('图片上传失败，请重试');
  } finally {
    uploading.value = false;
    uploadProgress.value = 0;
  }
};

const uploadToMinio = async (file: File): Promise<string> => {
  try {
    // 调用真实的上传API
    const { uploadImage } = await import('@/api/common/upload');
    const { data: imageUrl } = await uploadImage(file);
    return imageUrl;
  } catch (error) {
    console.error('图片上传到MinIO失败:', error);
    throw new Error('图片上传失败');
  }
};

const deleteImage = (index: number) => {
  deleteIndex.value = index;
  showDeleteConfirm.value = true;
};

const confirmDelete = () => {
  if (deleteIndex.value >= 0 && deleteIndex.value < currentImages.value.length) {
    const deletedImage = currentImages.value[deleteIndex.value];
    
    // 如果是本地预览URL，需要释放内存
    if (deletedImage.url.startsWith('blob:')) {
      URL.revokeObjectURL(deletedImage.url);
    }
    
    currentImages.value.splice(deleteIndex.value, 1);
    updateImages();
    showToast('图片删除成功');
  }
  
  showDeleteConfirm.value = false;
  deleteIndex.value = -1;
};

const cancelDelete = () => {
  showDeleteConfirm.value = false;
  deleteIndex.value = -1;
};

const previewImage = (url: string) => {
  const index = currentImages.value.findIndex(image => image.url === url);
  if (index >= 0) {
    previewIndex.value = index;
    showPreview.value = true;
  }
};

const updateImages = () => {
  emit('update:modelValue', [...currentImages.value]);
  console.log('图片信息更新:', currentImages.value);
};

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  currentImages.value = [...newValue];
}, { deep: true });

// 生命周期
onMounted(() => {
  console.log('图片管理器初始化，当前图片:', currentImages.value);
});
</script>

<style scoped>
.image-uploader {
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin: 0;
}

.image-count {
  font-size: 12px;
  color: #969799;
}

.upload-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 12px;
  color: #d46b08;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.image-item {
  display: flex;
  flex-direction: column;
}

.image-container {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: #f7f8fa;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-container:hover .image-overlay {
  opacity: 1;
}

.overlay-icon {
  font-size: 20px;
  color: white;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.3);
  transition: background 0.3s ease;
}

.overlay-icon:hover {
  background: rgba(0, 0, 0, 0.6);
}

.delete-icon:hover {
  background: rgba(238, 10, 36, 0.8);
}

.image-info {
  margin-top: 8px;
}

.image-name-input {
  font-size: 12px;
}

.upload-button {
  aspect-ratio: 1;
  border: 2px dashed #dcdee0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.upload-button:hover {
  border-color: #1989fa;
  background: #f0f9ff;
}

.upload-icon {
  font-size: 24px;
  color: #969799;
  margin-bottom: 4px;
}

.upload-button:hover .upload-icon {
  color: #1989fa;
}

.upload-text {
  font-size: 12px;
  color: #969799;
}

.upload-button:hover .upload-text {
  color: #1989fa;
}

.upload-overlay {
  display: flex;
  justify-content: center;
  align-items: center;
}

.upload-progress {
  background: white;
  padding: 24px;
  border-radius: 8px;
  text-align: center;
  min-width: 200px;
}

.progress-text {
  margin: 12px 0;
  font-size: 14px;
  color: #323233;
}
</style>