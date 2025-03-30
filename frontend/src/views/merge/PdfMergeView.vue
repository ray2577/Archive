<!-- PDF合成页面 -->
<template>
  <div class="pdf-merge-container">
    <el-card class="pdf-merge-card">
      <template #header>
        <div class="card-header">
          <h2>PDF合成工具</h2>
        </div>
      </template>

      <!-- PDF文件上传 -->
      <el-form :model="mergeForm" label-width="120px">
        <el-form-item label="PDF文件">
          <el-upload
            ref="pdfUpload"
            action="#"
            :auto-upload="false"
            :on-change="handlePdfChange"
            :on-remove="handlePdfRemove"
            multiple
            accept=".pdf"
            :file-list="mergeForm.pdfFiles">
            <el-button type="primary">选择PDF文件</el-button>
            <template #tip>
              <div class="el-upload__tip">请选择需要合并的PDF文件，可以多选</div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 图片和签名上传 -->
        <el-form-item label="图片/签名">
          <el-upload
            ref="imageUpload"
            action="#"
            :auto-upload="false"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
            multiple
            accept="image/*"
            :file-list="mergeForm.images">
            <el-button type="primary">选择图片/签名</el-button>
            <template #tip>
              <div class="el-upload__tip">可以添加图片或签名到PDF中</div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 图片位置设置 -->
        <template v-if="mergeForm.images.length > 0">
          <el-divider>图片/签名位置设置</el-divider>
          <div v-for="(image, index) in mergeForm.images" :key="index" class="image-position-setting">
            <el-descriptions :title="'图片 ' + (index + 1)" :column="1" border>
              <el-descriptions-item label="文件名">
                {{ image.name }}
              </el-descriptions-item>
              <el-descriptions-item label="页码">
                <el-input-number
                  v-model="mergeForm.imagePages[index]"
                  :min="1"
                  :max="maxPageNumber"
                  placeholder="选择页码" />
              </el-descriptions-item>
              <el-descriptions-item label="X坐标">
                <el-input-number
                  v-model="mergeForm.imageX[index]"
                  :min="0"
                  :step="10"
                  placeholder="X坐标" />
              </el-descriptions-item>
              <el-descriptions-item label="Y坐标">
                <el-input-number
                  v-model="mergeForm.imageY[index]"
                  :min="0"
                  :step="10"
                  placeholder="Y坐标" />
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </template>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleMerge" :loading="merging">
            合并PDF
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 表单数据
const mergeForm = reactive({
  pdfFiles: [],
  images: [],
  imagePages: [],
  imageX: [],
  imageY: []
})

const maxPageNumber = ref(1)
const merging = ref(false)

// 处理PDF文件变化
const handlePdfChange = (file, fileList) => {
  mergeForm.pdfFiles = fileList
  // 更新最大页码
  // 实际应用中，这里需要调用后端API获取PDF的页数
  maxPageNumber.value = Math.max(maxPageNumber.value, 1)
}

// 处理PDF文件移除
const handlePdfRemove = (file, fileList) => {
  mergeForm.pdfFiles = fileList
}

// 处理图片文件变化
const handleImageChange = (file, fileList) => {
  mergeForm.images = fileList
  // 为新添加的图片初始化位置数据
  if (mergeForm.imagePages.length < fileList.length) {
    mergeForm.imagePages.push(1)
    mergeForm.imageX.push(0)
    mergeForm.imageY.push(0)
  }
}

// 处理图片文件移除
const handleImageRemove = (file, fileList) => {
  const index = mergeForm.images.findIndex(f => f.uid === file.uid)
  if (index !== -1) {
    mergeForm.images.splice(index, 1)
    mergeForm.imagePages.splice(index, 1)
    mergeForm.imageX.splice(index, 1)
    mergeForm.imageY.splice(index, 1)
  }
}

// 合并PDF
const handleMerge = async () => {
  if (mergeForm.pdfFiles.length === 0) {
    ElMessage.warning('请至少选择一个PDF文件')
    return
  }

  merging.value = true
  try {
    const formData = new FormData()
    
    // 添加PDF文件
    mergeForm.pdfFiles.forEach(file => {
      formData.append('files', file.raw)
    })

    // 添加图片文件和位置信息
    if (mergeForm.images.length > 0) {
      mergeForm.images.forEach((image, index) => {
        formData.append('images', image.raw)
        formData.append('imageX', mergeForm.imageX[index])
        formData.append('imageY', mergeForm.imageY[index])
        formData.append('imagePages', mergeForm.imagePages[index])
      })
    }

    const response = await axios.post('/api/pdf/merge', formData, {
      responseType: 'blob'
    })

    // 下载合并后的PDF
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = 'merged.pdf'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('PDF合并成功')
    resetForm()
  } catch (error) {
    ElMessage.error('PDF合并失败')
  } finally {
    merging.value = false
  }
}

// 重置表单
const resetForm = () => {
  mergeForm.pdfFiles = []
  mergeForm.images = []
  mergeForm.imagePages = []
  mergeForm.imageX = []
  mergeForm.imageY = []
  maxPageNumber.value = 1
}
</script>

<style scoped>
.pdf-merge-container {
  padding: 20px;
}

.pdf-merge-card {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-position-setting {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.el-descriptions {
  margin: 10px 0;
}
</style> 