<template>
  <div class="avatar-upload">
    <el-upload
      class="avatar-uploader"
      :http-request="handleCustomUpload"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :disabled="uploading"
      accept="image/*"
    >
      <div v-if="avatarUrl" class="avatar-wrapper" :style="{ width: size+'px', height: size+'px' }">
        <el-avatar :size="size" :src="avatarUrl" />
        <div class="upload-overlay">
          <el-icon :size="size * 0.3"><Camera /></el-icon>
        </div>
      </div>
      <div v-else class="upload-placeholder" :style="{ width: size+'px', height: size+'px' }">
        <el-icon :size="size * 0.4"><Plus /></el-icon>
        <span style="font-size:12px;margin-top:4px">点击上传</span>
      </div>
    </el-upload>
    <div v-if="pendingUrl" class="confirm-area">
      <el-button text type="primary" size="small" :loading="confirming" @click="handleConfirm">确认使用此头像</el-button>
      <el-button text size="small" @click="handleCancel">取消</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Camera } from '@element-plus/icons-vue'
import { uploadAvatar } from '@/api/user'

const props = defineProps({
  modelValue: { type: String, default: '' },
  size: { type: Number, default: 100 }
})
const emit = defineEmits(['update:modelValue', 'uploaded'])

const uploading = ref(false)
const confirming = ref(false)
const pendingUrl = ref('')

const avatarUrl = computed(() => {
  if (pendingUrl.value && typeof pendingUrl.value === 'string') return pendingUrl.value
  if (props.modelValue && typeof props.modelValue === 'string') return props.modelValue
  return ''
})

watch(() => props.modelValue, (newVal) => {
  if (newVal && typeof newVal === 'string') {
    if (!pendingUrl.value) pendingUrl.value = ''
  }
}, { immediate: true })

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件（支持 JPG、PNG、GIF、WebP 格式）')
    return false
  }
  
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('图片格式不支持，请上传 JPG、PNG、GIF 或 WebP 格式')
    return false
  }
  
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB！')
    return false
  }
  
  return true
}

async function handleCustomUpload(options) {
  const { file, onSuccess, onError } = options
  
  uploading.value = true
  
  try {
    const res = await uploadAvatar(file)
    const url = res?.data
    
    if (!url || typeof url !== 'string') {
      throw new Error('上传返回数据格式异常')
    }
    
    pendingUrl.value = url
    onSuccess({ url })
    ElMessage.success('头像上传成功，请点击确认保存')
  } catch (error) {
    const errorMsg = error?.response?.data?.message || 
                     error?.message || 
                     '上传失败，请检查网络或联系管理员'
    ElMessage.error(errorMsg)
    onError(error)
  } finally {
    uploading.value = false
  }
}

function handleConfirm() {
  if (!pendingUrl.value || typeof pendingUrl.value !== 'string') return
  
  confirming.value = true
  
  setTimeout(() => {
    const finalUrl = pendingUrl.value
    emit('update:modelValue', finalUrl)
    emit('uploaded', finalUrl)
    confirming.value = false
    pendingUrl.value = ''
    ElMessage.success('头像已更新')
  }, 300)
}

function handleCancel() {
  pendingUrl.value = ''
}
</script>

<style scoped>
.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-wrapper {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: #fff;
}

.avatar-wrapper:hover .upload-overlay {
  opacity: 1;
}

.upload-placeholder {
  border: 2px dashed #d9d9d9;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s, color 0.3s, background-color 0.3s;
  color: #999;
  background: #fafafa;
}

.upload-placeholder:hover {
  border-color: #409EFF;
  color: #409EFF;
  background: #f0f5ff;
}

.confirm-area {
  margin-top: 12px;
  display: flex;
  gap: 12px;
}
</style>