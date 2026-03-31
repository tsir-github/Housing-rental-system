<template>
  <div class="attr-manage">
    <van-nav-bar title="属性管理" left-arrow @click-left="handleGoBack">
      <template #right>
        <van-button type="primary" size="small" @click="showAddKeyDialog">
          添加属性
        </van-button>
      </template>
    </van-nav-bar>

    <div class="content">
      <van-loading v-if="loading" vertical>加载中...</van-loading>
      
      <div v-else>
        <div v-if="attrList.length > 0" class="attr-list">
          <div v-for="attrKey in attrList" :key="attrKey.id" class="attr-item">
            <div class="attr-header">
              <h3 class="attr-name">{{ attrKey.name }}</h3>
              <div class="attr-actions">
                <van-button type="primary" size="mini" @click="showAddValueDialog(attrKey)">
                  添加值
                </van-button>
                <van-button type="warning" size="mini" @click="showEditKeyDialog(attrKey)">
                  编辑
                </van-button>
                <van-button type="danger" size="mini" @click="confirmDeleteKey(attrKey)">
                  删除
                </van-button>
              </div>
            </div>
            
            <div v-if="attrKey.attrValueList && attrKey.attrValueList.length > 0" class="attr-values">
              <div v-for="value in attrKey.attrValueList" :key="value.id" class="value-item">
                <span>{{ value.name }}</span>
                <div class="value-actions">
                  <van-button type="warning" size="mini" @click="showEditValueDialog(value)">编辑</van-button>
                  <van-button type="danger" size="mini" @click="confirmDeleteValue(value)">删除</van-button>
                </div>
              </div>
            </div>
            
            <div v-else class="no-values">暂无属性值</div>
          </div>
        </div>
        
        <van-empty v-else description="暂无属性数据" />
      </div>
    </div>

    <!-- 添加/编辑属性名称对话框 -->
    <van-dialog
      v-model:show="showKeyDialog"
      :title="keyDialogTitle"
      show-cancel-button
      @confirm="handleKeySave"
      @cancel="resetKeyForm"
    >
      <div class="dialog-content">
        <van-field
          v-model="keyForm.name"
          label="属性名称"
          placeholder="请输入属性名称"
          required
        />
      </div>
    </van-dialog>

    <!-- 添加/编辑属性值对话框 -->
    <van-dialog
      v-model:show="showValueDialog"
      :title="valueDialogTitle"
      show-cancel-button
      @confirm="handleValueSave"
      @cancel="resetValueForm"
    >
      <div class="dialog-content">
        <van-field
          v-model="valueForm.name"
          label="属性值"
          placeholder="请输入属性值"
          required
        />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { 
  getAttrKeys, 
  createAttrKey, 
  updateAttrKey, 
  deleteAttrKey,
  createAttrValue,
  updateAttrValue,
  deleteAttrValue
} from '@/api/landlord/management'
import { hasToken } from '@/utils/token'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const attrList = ref([])

// 处理返回按钮点击
const handleGoBack = () => {
  // 返回到房东仪表板
  router.push('/landlord/dashboard')
}

// 获取房东ID
const landlordId = computed(() => {
  return userStore.userInfo?.id || 0
})

// 属性名称对话框相关
const showKeyDialog = ref(false)
const keyForm = ref({
  id: null,
  name: ''
})
const keyDialogTitle = computed(() => keyForm.value.id ? '编辑属性' : '添加属性')

// 属性值对话框相关
const showValueDialog = ref(false)
const valueForm = ref({
  id: null,
  name: '',
  attrKeyId: null
})
const valueDialogTitle = computed(() => valueForm.value.id ? '编辑属性值' : '添加属性值')

const loadData = async () => {
  try {
    loading.value = true
    
    if (!hasToken()) {
      showToast('请先登录')
      return
    }

    if (!landlordId.value) {
      showToast('获取用户信息失败')
      return
    }

    console.log('🏠 开始加载属性数据...')
    const response = await getAttrKeys()
    console.log('🏠 API响应:', response)
    
    attrList.value = response.data || []
    console.log('🏠 属性列表:', attrList.value)
  } catch (error) {
    console.error('🏠 加载失败:', error)
    showToast('加载失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 显示添加属性对话框
const showAddKeyDialog = () => {
  resetKeyForm()
  showKeyDialog.value = true
}

// 显示编辑属性对话框
const showEditKeyDialog = (attrKey) => {
  keyForm.value = {
    id: attrKey.id,
    name: attrKey.name
  }
  showKeyDialog.value = true
}

// 显示添加属性值对话框
const showAddValueDialog = (attrKey) => {
  resetValueForm()
  valueForm.value.attrKeyId = attrKey.id
  showValueDialog.value = true
}

// 显示编辑属性值对话框
const showEditValueDialog = (value) => {
  valueForm.value = {
    id: value.id,
    name: value.name,
    attrKeyId: value.attrKeyId
  }
  showValueDialog.value = true
}

// 重置属性表单
const resetKeyForm = () => {
  keyForm.value = {
    id: null,
    name: ''
  }
}

// 重置属性值表单
const resetValueForm = () => {
  valueForm.value = {
    id: null,
    name: '',
    attrKeyId: null
  }
}

// 保存属性
const handleKeySave = async () => {
  if (!keyForm.value.name.trim()) {
    showToast('请输入属性名称')
    return
  }

  try {
    if (keyForm.value.id) {
      // 更新
      await updateAttrKey(landlordId.value, keyForm.value)
      showToast('更新成功')
    } else {
      // 创建
      await createAttrKey(landlordId.value, keyForm.value)
      showToast('添加成功')
    }
    
    showKeyDialog.value = false
    await loadData()
  } catch (error) {
    console.error('保存属性失败:', error)
    showToast('保存失败: ' + (error.message || '未知错误'))
  }
}

// 保存属性值
const handleValueSave = async () => {
  if (!valueForm.value.name.trim()) {
    showToast('请输入属性值')
    return
  }

  try {
    if (valueForm.value.id) {
      // 更新
      await updateAttrValue(landlordId.value, valueForm.value)
      showToast('更新成功')
    } else {
      // 创建
      await createAttrValue(landlordId.value, valueForm.value)
      showToast('添加成功')
    }
    
    showValueDialog.value = false
    await loadData()
  } catch (error) {
    console.error('保存属性值失败:', error)
    showToast('保存失败: ' + (error.message || '未知错误'))
  }
}

// 确认删除属性
const confirmDeleteKey = async (attrKey) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除属性"${attrKey.name}"吗？删除后该属性下的所有属性值也会被删除。`,
    })
    
    await deleteAttrKey(landlordId.value, attrKey.id)
    showToast('删除成功')
    await loadData()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('删除属性失败:', error)
    showToast('删除失败: ' + (error.message || '未知错误'))
  }
}

// 确认删除属性值
const confirmDeleteValue = async (value) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除属性值"${value.name}"吗？`,
    })
    
    await deleteAttrValue(landlordId.value, value.id)
    showToast('删除成功')
    await loadData()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('删除属性值失败:', error)
    showToast('删除失败: ' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  console.log('🏠 属性管理组件已挂载')
  loadData()
})
</script>

<style scoped>
.attr-manage {
  min-height: 100vh;
  background: #f5f5f5;
}

.content {
  padding: 16px;
}

.attr-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.attr-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.attr-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.attr-actions {
  display: flex;
  gap: 8px;
}

.attr-values {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.value-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.value-actions {
  display: flex;
  gap: 6px;
}

.no-values {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 20px;
}

.dialog-content {
  padding: 16px;
}

.dialog-content .van-field {
  margin-bottom: 16px;
}
</style>