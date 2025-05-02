<template>
  <div class="data-restore-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>数据恢复</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshBackupList">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>
    
    <!-- 备份搜索区 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="备份名称">
          <el-input v-model="searchForm.name" placeholder="请输入备份名称" clearable />
        </el-form-item>
        <el-form-item label="备份类型">
          <el-select v-model="searchForm.type" placeholder="请选择备份类型" clearable>
            <el-option v-for="item in backupTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备份时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 备份列表 -->
    <el-card class="backup-list-card">
      <template #header>
        <div class="card-header">
          <span>备份列表</span>
          <el-button-group>
            <el-button type="primary" @click="handleUploadBackup">
              <el-icon><Upload /></el-icon>上传备份
            </el-button>
          </el-button-group>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="backupList"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="备份名称" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <div class="backup-name">
              <el-icon><Document /></el-icon>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="备份类型" width="120">
          <template #default="scope">
            {{ getBackupTypeName(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="backupTime" label="备份时间" width="160" sortable />
        <el-table-column prop="creator" label="创建人" width="100" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-tooltip content="恢复数据">
                <el-button type="success" link @click="handleRestore(scope.row)">
                  <el-icon><RefreshRight /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="下载备份">
                <el-button type="primary" link @click="handleDownload(scope.row)">
                  <el-icon><Download /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除备份">
                <el-button type="danger" link @click="handleDelete(scope.row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
    
    <!-- 上传备份对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传备份文件"
      width="500px"
    >
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="备份文件">
          <el-upload
            class="backup-upload"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将备份文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 .zip, .tar.gz 格式备份文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="备份名称">
          <el-input v-model="uploadForm.name" placeholder="请输入备份名称" />
        </el-form-item>
        <el-form-item label="备份类型">
          <el-select v-model="uploadForm.type" placeholder="请选择备份类型">
            <el-option v-for="item in backupTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备份描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpload">上传</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 恢复确认对话框 -->
    <el-dialog
      v-model="restoreDialogVisible"
      title="恢复确认"
      width="500px"
    >
      <div class="restore-confirm">
        <el-alert
          title="数据恢复将覆盖当前系统数据，请谨慎操作！"
          type="warning"
          :closable="false"
          show-icon
        />
        <div class="restore-info">
          <p><strong>备份名称：</strong>{{ currentBackup.name }}</p>
          <p><strong>备份类型：</strong>{{ getBackupTypeName(currentBackup.type) }}</p>
          <p><strong>备份时间：</strong>{{ currentBackup.backupTime }}</p>
          <p><strong>备份大小：</strong>{{ formatFileSize(currentBackup.size) }}</p>
        </div>
        <el-form :model="restoreForm" label-width="100px">
          <el-form-item label="恢复选项">
            <el-radio-group v-model="restoreForm.option">
              <el-radio label="all">恢复所有数据</el-radio>
              <el-radio label="selective">选择性恢复</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="恢复项" v-if="restoreForm.option === 'selective'">
            <el-checkbox-group v-model="restoreForm.items">
              <el-checkbox label="documents">文档数据</el-checkbox>
              <el-checkbox label="categories">分类数据</el-checkbox>
              <el-checkbox label="workflows">流程数据</el-checkbox>
              <el-checkbox label="users">用户数据</el-checkbox>
              <el-checkbox label="settings">系统设置</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="确认操作">
            <el-input
              v-model="restoreForm.confirmText"
              placeholder="请输入 'RESTORE' 确认恢复操作"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="restoreDialogVisible = false">取消</el-button>
          <el-button 
            type="danger" 
            @click="confirmRestore" 
            :disabled="restoreForm.confirmText !== 'RESTORE'"
          >
            确认恢复
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, Search, Upload, Download, Delete, Document,
  RefreshRight, UploadFilled
} from '@element-plus/icons-vue'

// 备份类型选项
const backupTypeOptions = [
  { value: '1', label: '自动备份' },
  { value: '2', label: '手动备份' },
  { value: '3', label: '升级备份' },
  { value: '4', label: '外部导入' }
]

// 搜索表单
const searchForm = reactive({
  name: '',
  type: '',
  dateRange: []
})

// 备份列表状态
const loading = ref(false)
const backupList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  sortBy: 'backupTime',
  sortOrder: 'desc'
})

// 上传对话框
const uploadDialogVisible = ref(false)
const uploadForm = reactive({
  name: '',
  type: '2',
  description: '',
  file: null
})

// 恢复对话框
const restoreDialogVisible = ref(false)
const currentBackup = ref({})
const restoreForm = reactive({
  option: 'all',
  items: ['documents', 'categories', 'workflows', 'users', 'settings'],
  confirmText: ''
})

// 获取备份列表
const fetchBackupList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      backupList.value = [
        {
          id: 1,
          name: '系统完整备份_20230310',
          type: '2',
          size: 256000000,
          backupTime: '2023-03-10 10:30:00',
          creator: '管理员',
          description: '系统完整备份，包含所有数据'
        },
        {
          id: 2,
          name: '自动备份_20230305',
          type: '1',
          size: 204800000,
          backupTime: '2023-03-05 00:00:00',
          creator: '系统',
          description: '系统自动备份'
        },
        {
          id: 3,
          name: '升级前备份_20230301',
          type: '3',
          size: 307200000,
          backupTime: '2023-03-01 08:15:00',
          creator: '系统',
          description: '系统升级前自动备份'
        }
      ]
      total.value = 3
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取备份列表失败')
    loading.value = false
  }
}

// 刷新备份列表
const refreshBackupList = () => {
  fetchBackupList()
  ElMessage.success('刷新成功')
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchBackupList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.type = ''
  searchForm.dateRange = []
  handleSearch()
}

// 页面大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  fetchBackupList()
}

// 页码变化
const handlePageChange = (page) => {
  queryParams.page = page
  fetchBackupList()
}

// 获取备份类型名称
const getBackupTypeName = (type) => {
  const found = backupTypeOptions.find(item => item.value === type)
  return found ? found.label : '未知类型'
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0
  let formattedSize = size
  
  while (formattedSize >= 1024 && index < units.length - 1) {
    formattedSize /= 1024
    index++
  }
  
  return `${formattedSize.toFixed(2)} ${units[index]}`
}

// 处理上传备份
const handleUploadBackup = () => {
  uploadForm.name = ''
  uploadForm.type = '2'
  uploadForm.description = ''
  uploadForm.file = null
  uploadDialogVisible.value = true
}

// 处理文件变化
const handleFileChange = (file) => {
  uploadForm.file = file
  if (!uploadForm.name && file.name) {
    uploadForm.name = file.name.split('.')[0]
  }
}

// 提交上传
const submitUpload = async () => {
  if (!uploadForm.file) {
    ElMessage.warning('请选择备份文件')
    return
  }
  
  if (!uploadForm.name) {
    ElMessage.warning('请输入备份名称')
    return
  }
  
  try {
    // 模拟上传
    ElMessage.success('备份文件上传成功')
    uploadDialogVisible.value = false
    // 刷新列表
    fetchBackupList()
  } catch (error) {
    ElMessage.error('备份文件上传失败')
  }
}

// 处理恢复
const handleRestore = (row) => {
  currentBackup.value = row
  restoreForm.option = 'all'
  restoreForm.items = ['documents', 'categories', 'workflows', 'users', 'settings']
  restoreForm.confirmText = ''
  restoreDialogVisible.value = true
}

// 确认恢复
const confirmRestore = async () => {
  if (restoreForm.confirmText !== 'RESTORE') {
    ElMessage.warning('请输入 RESTORE 确认操作')
    return
  }
  
  try {
    // 模拟恢复操作
    ElMessage.success('数据恢复成功，系统将在5秒后自动刷新')
    restoreDialogVisible.value = false
    
    // 模拟系统刷新
    setTimeout(() => {
      window.location.reload()
    }, 5000)
  } catch (error) {
    ElMessage.error('数据恢复失败')
  }
}

// 处理下载
const handleDownload = (row) => {
  ElMessage.success(`正在下载备份文件：${row.name}`)
  // 实际应用中应调用下载API
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除备份 ${row.name} 吗？删除后无法恢复！`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 模拟删除
      const index = backupList.value.findIndex(item => item.id === row.id)
      if (index !== -1) {
        backupList.value.splice(index, 1)
        total.value--
      }
      ElMessage.success('删除成功')
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 生命周期钩子
onMounted(() => {
  fetchBackupList()
})
</script>

<style scoped>
.data-restore-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.filter-container {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.backup-list-card {
  margin-bottom: 20px;
}

.backup-name {
  display: flex;
  align-items: center;
}

.backup-name .el-icon {
  margin-right: 8px;
  color: #909399;
}

.pagination-container {
  text-align: right;
  margin-top: 20px;
}

.backup-upload {
  width: 100%;
}

.restore-confirm {
  margin-bottom: 20px;
}

.restore-info {
  margin: 15px 0;
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.restore-info p {
  margin: 5px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 