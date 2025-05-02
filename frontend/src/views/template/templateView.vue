<template>
  <div class="template-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>档案模板管理</h2>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>新增模板
          </el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-input
          v-model="searchQuery"
          placeholder="输入模板名称搜索"
          class="search-input"
          clearable
          @clear="fetchTemplates"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="fetchTemplates">
          <el-icon><Search /></el-icon>搜索
        </el-button>
      </div>
      
      <!-- 表格区域 -->
      <el-table
        :data="templateList"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模板名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="fileType" label="文件类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getFileTypeTag(row.fileType)">{{ row.fileType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="previewTemplate(row)">
              <el-icon><View /></el-icon>预览
            </el-button>
            <el-button size="small" type="success" @click="handleDownload(row)">
              <el-icon><Download /></el-icon>下载
            </el-button>
            <el-button size="small" type="primary" @click="editTemplate(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button size="small" type="danger" @click="confirmDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑模板对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑模板' : '新增模板'"
      width="50%"
    >
      <el-form
        ref="templateFormRef"
        :model="templateForm"
        :rules="templateRules"
        label-width="100px"
      >
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="templateForm.description"
            type="textarea"
            placeholder="请输入模板描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="文件类型" prop="fileType">
          <el-select v-model="templateForm.fileType" placeholder="请选择文件类型">
            <el-option label="PDF" value="PDF" />
            <el-option label="Word" value="DOCX" />
            <el-option label="Excel" value="XLSX" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板文件" prop="file">
          <el-upload
            class="template-uploader"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            :limit="1"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传模板文件，支持PDF、Word、Excel格式
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTemplateForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="模板预览"
      width="70%"
    >
      <div class="preview-container" v-loading="previewLoading">
        <iframe
          v-if="previewUrl && !previewLoading"
          :src="previewUrl"
          frameborder="0"
          width="100%"
          height="500px"
        ></iframe>
        <div v-else-if="!previewLoading" class="preview-error">
          该文件类型不支持预览
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, View, Edit, Delete, Plus, Download } from '@element-plus/icons-vue'
import { 
  getTemplateList, 
  createTemplate, 
  updateTemplate, 
  deleteTemplate, 
  getTemplatePreviewUrl,
  downloadTemplate
} from '@/api/template'

// 状态变量
const loading = ref(false)
const templateList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')

// 对话框状态
const dialogVisible = ref(false)
const isEditing = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const previewLoading = ref(false)

// 表单相关
const templateFormRef = ref(null)
const templateForm = reactive({
  id: null,
  name: '',
  description: '',
  fileType: '',
  file: null
})
const fileList = ref([])

// 表单验证规则
const templateRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ],
  fileType: [
    { required: true, message: '请选择文件类型', trigger: 'change' }
  ],
  file: [
    { required: true, message: '请上传模板文件', trigger: 'change' }
  ]
}

// 方法
const fetchTemplates = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      name: searchQuery.value || undefined
    }
    
    const response = await getTemplateList(params)
    templateList.value = response.data.items || []
    total.value = response.data.total || 0
  } catch (error) {
    ElMessage.error('获取模板列表失败')
    console.error('获取模板列表失败:', error)
    // 如果API失败，使用模拟数据（开发阶段）
    useMockData()
  } finally {
    loading.value = false
  }
}

// 仅在开发阶段使用的模拟数据
const useMockData = () => {
  const mockTemplates = [
    {
      id: 1,
      name: '人事档案模板',
      description: '用于人事档案管理的标准模板',
      fileType: 'PDF',
      createdAt: '2023-05-10T08:30:00Z'
    },
    {
      id: 2,
      name: '财务档案模板',
      description: '财务部门使用的档案模板',
      fileType: 'DOCX',
      createdAt: '2023-06-15T13:45:00Z'
    },
    {
      id: 3,
      name: '项目档案模板',
      description: '项目文档归档使用的模板',
      fileType: 'XLSX',
      createdAt: '2023-07-22T09:20:00Z'
    }
  ]
  
  // 模拟搜索过滤
  let result = [...mockTemplates]
  if (searchQuery.value) {
    result = result.filter(item => 
      item.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }
  
  total.value = result.length
  
  // 模拟分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  templateList.value = result.slice(start, end)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchTemplates()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchTemplates()
}

const getFileTypeTag = (fileType) => {
  const typeMap = {
    'PDF': 'danger',
    'DOCX': 'primary',
    'XLSX': 'success'
  }
  return typeMap[fileType] || 'info'
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString()
}

const resetForm = () => {
  if (templateFormRef.value) {
    templateFormRef.value.resetFields()
  }
  templateForm.id = null
  templateForm.name = ''
  templateForm.description = ''
  templateForm.fileType = ''
  templateForm.file = null
  fileList.value = []
}

const showAddDialog = () => {
  resetForm()
  isEditing.value = false
  dialogVisible.value = true
}

const editTemplate = (row) => {
  resetForm()
  isEditing.value = true
  
  // 填充表单数据
  templateForm.id = row.id
  templateForm.name = row.name
  templateForm.description = row.description
  templateForm.fileType = row.fileType
  
  // 显示已有文件
  fileList.value = [{
    name: `${row.name}.${row.fileType.toLowerCase()}`,
    url: '#'
  }]
  
  dialogVisible.value = true
}

const confirmDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除模板"${row.name}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  .then(async () => {
    try {
      await deleteTemplate(row.id)
      ElMessage.success('删除成功')
      fetchTemplates()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error('删除模板失败:', error)
    }
  })
  .catch(() => {
    // 取消删除
  })
}

const handleFileChange = (file) => {
  templateForm.file = file.raw
}

const handleFileRemove = () => {
  templateForm.file = null
}

const submitTemplateForm = async () => {
  templateFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = new FormData()
        formData.append('name', templateForm.name)
        formData.append('description', templateForm.description)
        formData.append('fileType', templateForm.fileType)
        
        if (templateForm.file) {
          formData.append('file', templateForm.file)
        }
        
        if (isEditing.value) {
          await updateTemplate(templateForm.id, formData)
          ElMessage.success('更新成功')
        } else {
          await createTemplate(formData)
          ElMessage.success('添加成功')
        }
        
        dialogVisible.value = false
        fetchTemplates()
      } catch (error) {
        ElMessage.error(isEditing.value ? '更新失败' : '添加失败')
        console.error(isEditing.value ? '更新模板失败:' : '添加模板失败:', error)
      }
    }
  })
}

const previewTemplate = async (row) => {
  previewVisible.value = true
  previewLoading.value = true
  
  try {
    const response = await getTemplatePreviewUrl(row.id)
    previewUrl.value = response.data.url
  } catch (error) {
    ElMessage.error('获取预览地址失败')
    console.error('获取预览地址失败:', error)
    previewUrl.value = ''
  } finally {
    previewLoading.value = false
  }
}

const handleDownload = async (row) => {
  try {
    const response = await downloadTemplate(row.id)
    
    // 创建下载链接
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${row.name}.${row.fileType.toLowerCase()}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
    console.error('下载模板失败:', error)
  }
}

// 初始化
onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.template-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.search-area {
  display: flex;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
  margin-right: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.preview-container {
  display: flex;
  justify-content: center;
  min-height: 500px;
}

.preview-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 500px;
  color: #909399;
  font-size: 16px;
}

.template-uploader {
  width: 100%;
}
</style>
