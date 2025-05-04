<template>
  <div class="archive-edit-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <h2>{{ isEdit ? '编辑档案' : '新增档案' }}</h2>
      </div>
      <div class="header-actions">
        <el-button-group>
          <el-button @click="resetForm">
            <el-icon><RefreshLeft /></el-icon>重置
          </el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            <el-icon><Check /></el-icon>保存
          </el-button>
        </el-button-group>
      </div>
    </div>
    
    <!-- 加载中状态 -->
    <el-skeleton :loading="loading" animated :rows="10" v-if="loading">
      <template #template>
        <div style="padding: 20px">
          <el-skeleton-item variant="text" style="width: 30%" />
          <div style="display: flex; margin-top: 20px">
            <div style="width: 48%">
              <el-skeleton-item variant="text" style="width: 100%; margin-top: 10px" />
              <el-skeleton-item variant="text" style="width: 100%; margin-top: 10px" />
              <el-skeleton-item variant="text" style="width: 100%; margin-top: 10px" />
            </div>
            <div style="width: 48%; margin-left: 4%">
              <el-skeleton-item variant="text" style="width: 100%; margin-top: 10px" />
              <el-skeleton-item variant="text" style="width: 100%; margin-top: 10px" />
              <el-skeleton-item variant="text" style="width: 100%; margin-top: 10px" />
            </div>
          </div>
        </div>
      </template>
    </el-skeleton>
    
    <!-- 编辑表单 -->
    <div class="form-container" v-else>
      <el-form 
        ref="formRef" 
        :model="archiveForm" 
        :rules="rules" 
        label-position="top"
        class="archive-form"
      >
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-tag v-if="isEdit" :type="getStatusType(archiveForm.status)">
                {{ formatStatus(archiveForm.status) }}
              </el-tag>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="档案编号" prop="fileNumber">
                <el-input 
                  v-model="archiveForm.fileNumber" 
                  placeholder="请输入档案编号" 
                  :disabled="isEdit"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="档案名称" prop="title">
                <el-input 
                  v-model="archiveForm.title" 
                  placeholder="请输入档案名称" 
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="档案类型" prop="type">
                <el-select 
                  v-model="archiveForm.type" 
                  placeholder="请选择档案类型" 
                  style="width: 100%"
                >
                  <el-option 
                    v-for="item in typeOptions" 
                    :key="item.value" 
                    :label="item.label" 
                    :value="item.value" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="档案类别" prop="category">
                <el-input 
                  v-model="archiveForm.category" 
                  placeholder="请输入档案类别" 
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="存放位置" prop="location">
                <el-cascader
                  v-model="archiveForm.location"
                  :options="locationOptions"
                  placeholder="请选择存放位置"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="负责人" prop="responsible">
                <el-input 
                  v-model="archiveForm.responsible" 
                  placeholder="请输入负责人姓名" 
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="状态" prop="status" v-if="isEdit">
                <el-select 
                  v-model="archiveForm.status" 
                  placeholder="请选择档案状态" 
                  style="width: 100%"
                >
                  <el-option 
                    v-for="item in statusOptions" 
                    :key="item.value" 
                    :label="item.label" 
                    :value="item.value" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="关键词" prop="keywords">
            <el-tag
              v-for="tag in archiveForm.keywords"
              :key="tag"
              closable
              @close="removeKeyword(tag)"
              class="keyword-tag"
            >
              {{ tag }}
            </el-tag>
            <el-input
              v-if="keywordInputVisible"
              ref="keywordInputRef"
              v-model="keywordValue"
              class="keyword-input"
              size="small"
              @keyup.enter="addKeyword"
              @blur="addKeyword"
            />
            <el-button v-else class="keyword-button" @click="showKeywordInput">
              <el-icon><Plus /></el-icon> 添加关键词
            </el-button>
          </el-form-item>
          
          <el-form-item label="档案描述" prop="description">
            <el-input 
              v-model="archiveForm.description" 
              type="textarea" 
              :rows="4"
              placeholder="请输入档案描述" 
            />
          </el-form-item>
        </el-card>
        
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>档案文件</span>
            </div>
          </template>
          
          <el-form-item label="上传文件" prop="file">
            <el-upload
              class="archive-upload"
              drag
              action="/api/archives/upload"
              :auto-upload="false"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :limit="1"
              :file-list="fileList"
            >
              <el-icon class="el-icon--upload"><Upload /></el-icon>
              <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
              <template #tip>
                <div class="el-upload__tip">
                  支持上传PDF、Word、Excel、图片等类型文件，单个文件不超过20MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
          
          <div class="file-preview" v-if="isEdit && archiveForm.fileUrl">
            <h4>当前档案文件</h4>
            <div class="current-file">
              <el-icon :size="32"><Document /></el-icon>
              <div class="file-info">
                <div class="file-name">{{ archiveForm.title }}.{{ archiveForm.fileType }}</div>
                <div class="file-meta">{{ formatFileSize(archiveForm.fileSize) }}</div>
              </div>
              <el-button type="primary" link @click="handlePreview">
                预览
              </el-button>
            </div>
          </div>
        </el-card>
      </el-form>
    </div>
    
    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="文件预览"
      width="90%"
      fullscreen
    >
      <div class="preview-container">
        <iframe 
          v-if="archiveForm.fileUrl" 
          :src="getPreviewUrl(archiveForm.fileUrl)" 
          class="preview-iframe"
        ></iframe>
        <div v-else class="preview-placeholder">
          <el-empty description="暂无可预览的文件" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  RefreshLeft, Check, Plus, 
  Upload, Document 
} from '@element-plus/icons-vue'
import { 
  getArchiveDetail, 
  createArchive, 
  updateArchive,
  getArchivePreviewUrl,
  getArchiveDownloadUrl,
  borrowArchive as requestBorrowArchive
} from '@/api/archive'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const keywordInputRef = ref(null)

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const isEdit = computed(() => !!route.params.id)
const archiveId = computed(() => route.params.id)
const keywordInputVisible = ref(false)
const keywordValue = ref('')
const fileList = ref([])
const previewDialogVisible = ref(false)

// 档案表单数据
const archiveForm = reactive({
  fileNumber: '',
  title: '',
  type: '',
  category: '',
  location: [],
  responsible: '',
  status: 'AVAILABLE',
  keywords: [],
  description: '',
  file: null,
  fileUrl: '',
  fileType: '',
  fileSize: 0
})

// 表单验证规则
const rules = {
  fileNumber: [
    { required: true, message: '请输入档案编号', trigger: 'blur' },
    { pattern: /^[A-Z]{2}\d{8}$/, message: '档案编号格式错误，应为2个大写字母+8位数字', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入档案名称', trigger: 'blur' },
    { min: 2, max: 100, message: '档案名称长度应为2-100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择档案类型', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请输入档案类别', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请选择存放位置', trigger: 'change' }
  ],
  responsible: [
    { required: true, message: '请输入负责人姓名', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '档案描述不能超过500个字符', trigger: 'blur' }
  ]
}

// 档案类型选项
const typeOptions = [
  { label: '财务类', value: 'FINANCIAL' },
  { label: '人事类', value: 'PERSONNEL' },
  { label: '行政类', value: 'ADMINISTRATIVE' },
  { label: '技术类', value: 'TECHNICAL' },
  { label: '其他', value: 'OTHER' }
]

// 档案状态选项
const statusOptions = [
  { label: '可借阅', value: 'AVAILABLE' },
  { label: '已借出', value: 'BORROWED' },
  { label: '处理中', value: 'PROCESSING' }
]

// 存储位置选项
const locationOptions = [
  {
    value: 'A',
    label: 'A区',
    children: [
      { value: 'A1', label: 'A1架' },
      { value: 'A2', label: 'A2架' },
      { value: 'A3', label: 'A3架' }
    ]
  },
  {
    value: 'B',
    label: 'B区',
    children: [
      { value: 'B1', label: 'B1架' },
      { value: 'B2', label: 'B2架' },
      { value: 'B3', label: 'B3架' }
    ]
  },
  {
    value: 'C',
    label: 'C区',
    children: [
      { value: 'C1', label: 'C1架' },
      { value: 'C2', label: 'C2架' },
      { value: 'C3', label: 'C3架' }
    ]
  }
]

// 加载档案详情（编辑模式）
const loadArchiveDetail = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    // 调用API服务获取档案详情
    const res = await getArchiveDetail(archiveId.value)
    const data = res.data
    
    // 更新表单数据
    Object.keys(archiveForm).forEach(key => {
      if (data[key] !== undefined) {
        archiveForm[key] = data[key]
      }
    })
  } catch (error) {
    console.error('Error loading archive detail:', error)
    ElMessage.error('加载档案详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 初始化表单（新增模式）
const initForm = () => {
  if (isEdit.value) return
  
  // 生成档案编号
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  archiveForm.fileNumber = `AR${year}${month}${day}`
  archiveForm.status = 'AVAILABLE'
  archiveForm.category = '一般文档'
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 重置表单
const resetForm = () => {
  ElMessageBox.confirm(
    '确定要重置表单吗？所有未保存的修改将丢失。',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      if (isEdit.value) {
        // 重新加载档案详情
        loadArchiveDetail()
      } else {
        // 清空表单
        formRef.value.resetFields()
        archiveForm.keywords = []
        fileList.value = []
        initForm()
      }
    })
    .catch(() => {})
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请完善表单信息')
      return
    }
    
    submitting.value = true
    try {
      // 构建提交数据
      const formData = new FormData()
      Object.keys(archiveForm).forEach(key => {
        if (key === 'location') {
          formData.append(key, archiveForm[key].join(','))
        } else if (key === 'keywords') {
          formData.append(key, JSON.stringify(archiveForm[key]))
        } else if (key === 'file' && archiveForm[key]) {
          formData.append('file', archiveForm[key])
        } else if (key !== 'file') {
          formData.append(key, archiveForm[key])
        }
      })
      
      // 打印提交数据用于调试
      console.log('提交的表单数据:', Object.fromEntries(formData.entries()))
      
      // 检查文件上传
      if (!archiveForm.file && !isEdit.value) {
        ElMessage.warning('请上传档案文件')
        submitting.value = false
        return
      }
      
      // 使用API服务调用后端接口
      let result
      if (isEdit.value) {
        result = await updateArchive(archiveId.value, formData)
      } else {
        result = await createArchive(formData)
      }
      
      console.log('服务器响应成功:', result)
      
      ElMessage.success(isEdit.value ? '档案更新成功' : '档案创建成功')
      router.push('/archive/list')
    } catch (error) {
      console.error('提交表单错误:', error)
      console.error('错误详情:', error.response?.data || error.message)
      ElMessage.error({
        message: `${isEdit.value ? '更新' : '创建'}档案失败: ${error.message}`,
        duration: 5000
      })
    } finally {
      submitting.value = false
    }
  })
}

// 关键词相关方法
const showKeywordInput = () => {
  keywordInputVisible.value = true
  nextTick(() => {
    keywordInputRef.value.focus()
  })
}

const addKeyword = () => {
  if (keywordValue.value) {
    if (archiveForm.keywords.indexOf(keywordValue.value) === -1) {
      archiveForm.keywords.push(keywordValue.value)
    }
  }
  keywordInputVisible.value = false
  keywordValue.value = ''
}

const removeKeyword = (tag) => {
  archiveForm.keywords.splice(archiveForm.keywords.indexOf(tag), 1)
}

// 文件上传相关方法
const handleFileChange = (file) => {
  archiveForm.file = file.raw
  const fileExtension = file.name.split('.').pop().toLowerCase()
  const fileTypeMap = {
    'pdf': 'pdf',
    'doc': 'word',
    'docx': 'word',
    'xls': 'excel',
    'xlsx': 'excel',
    'jpg': 'image',
    'jpeg': 'image',
    'png': 'image'
  }
  
  archiveForm.fileType = fileTypeMap[fileExtension] || fileExtension
  archiveForm.fileSize = file.size
}

const handleFileRemove = () => {
  archiveForm.file = null
}

// 预览文件
const handlePreview = () => {
  if (!archiveForm.fileUrl) {
    ElMessage.warning('文件链接不可用，无法预览')
    return
  }
  previewDialogVisible.value = true
}

// 获取预览URL
const getPreviewUrl = (url) => {
  // 如果没有文件URL但有ID，使用API服务生成预览URL
  if (!url && isEdit.value) {
    return getArchivePreviewUrl(archiveId.value);
  }
  
  // 如果是PDF，可以直接预览
  if (archiveForm.fileType === 'pdf' && url) {
    // 通常会使用Google Docs或其他PDF预览服务
    return `https://docs.google.com/viewer?url=${encodeURIComponent(url)}&embedded=true`
  }
  // 如果是图片，直接返回URL
  if (archiveForm.fileType === 'image' && url) {
    return url
  }
  // 其他类型文件可能需要特殊处理
  return url
}

// 下载档案
const downloadArchive = async (archive) => {
  if (isEdit.value) {
    // 使用下载URL进行下载
    const downloadUrl = getArchiveDownloadUrl(archiveId.value);
    window.open(downloadUrl, '_blank');
  }
}

// 申请借阅
const borrowArchive = async (archive) => {
  if (isEdit.value) {
    try {
      await requestBorrowArchive(archiveId.value, {
        userId: localStorage.getItem('userId'),
        reason: '档案查阅',
        borrowDate: new Date().toISOString()
      });
      ElMessage.success('借阅申请已提交');
    } catch (error) {
      console.error('借阅申请失败:', error);
      ElMessage.error('借阅申请失败, 请稍后重试');
    }
  }
}

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    'AVAILABLE': '可借阅',
    'BORROWED': '已借出',
    'PROCESSING': '处理中'
  }
  return statusMap[status] || status
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'AVAILABLE': 'success',
    'BORROWED': 'warning',
    'PROCESSING': 'info'
  }
  return typeMap[status] || 'info'
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  
  return `${size.toFixed(2)} ${units[i]}`
}

// 组件挂载
onMounted(() => {
  if (isEdit.value) {
    loadArchiveDetail()
  } else {
    initForm()
  }
})
</script>

<style scoped>
.archive-edit-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background-color: white;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.form-container {
  margin-top: 20px;
}

.form-card {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: none;
}

.form-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  font-weight: 500;
}

.archive-form {
  padding: 12px;
}

.keyword-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  border-radius: 4px;
}

.keyword-input {
  width: 120px;
  margin-right: 8px;
  vertical-align: top;
}

.keyword-button {
  margin-top: 8px;
}

/* Upload area styling */
.archive-upload {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.el-upload-dragger:hover) {
  border-color: #409EFF;
  background-color: #f0f7ff;
}

:deep(.el-icon--upload) {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

/* Enhanced form controls */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__wrapper),
:deep(.el-select__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 4px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px #a0cfff inset;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  font-size: 14px;
}

:deep(.el-upload-list__item) {
  border-radius: 4px;
  transition: all 0.3s ease;
}

:deep(.el-upload-list__item:hover) {
  background-color: #f0f7ff;
}

/* File preview styling */
.file-preview {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
  margin-top: 16px;
  background-color: #fafafa;
}

.file-preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.file-preview-content {
  min-height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background-color: white;
  border-radius: 4px;
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .archive-edit-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .archive-form {
    padding: 8px;
  }
}
</style> 