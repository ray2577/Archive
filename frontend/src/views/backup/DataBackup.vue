<template>
  <div class="data-backup-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>数据备份</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleManualBackup">
          <el-icon><Plus /></el-icon>新增备份
        </el-button>
      </div>
    </div>
    
    <!-- 备份列表 -->
    <el-card class="backup-card">
      <template #header>
        <div class="card-header">
          <span>备份列表</span>
          <div class="header-actions">
            <el-button-group>
              <el-button type="primary" @click="refreshBackupList">
                <el-icon><Refresh /></el-icon>刷新
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="backupList"
        border
        style="width: 100%"
      >
        <el-table-column prop="name" label="备份名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="type" label="备份类型" width="120">
          <template #default="scope">
            <el-tag :type="getBackupTypeTag(scope.row.type)">
              {{ getBackupTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="备份大小" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" sortable />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleDownload(scope.row)" :disabled="scope.row.status !== 'completed'">
              <el-icon><Download /></el-icon>
            </el-button>
            <el-button type="success" link @click="handleRestore(scope.row)" :disabled="scope.row.status !== 'completed'">
              <el-icon><RefreshRight /></el-icon>
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
            </el-button>
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
    
    <!-- 新增备份对话框 -->
    <el-dialog
      v-model="backupDialogVisible"
      title="新增备份"
      width="500px"
    >
      <el-form
        ref="backupFormRef"
        :model="backupForm"
        :rules="backupRules"
        label-width="100px"
      >
        <el-form-item label="备份名称" prop="name">
          <el-input v-model="backupForm.name" placeholder="请输入备份名称" />
        </el-form-item>
        <el-form-item label="备份类型" prop="type">
          <el-select v-model="backupForm.type" placeholder="请选择备份类型" style="width: 100%">
            <el-option v-for="item in backupTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input
            v-model="backupForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="backupDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBackupForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 恢复确认对话框 -->
    <el-dialog
      v-model="restoreDialogVisible"
      title="恢复数据"
      width="500px"
    >
      <div class="restore-warning">
        <el-alert
          title="警告：数据恢复将覆盖当前系统数据，请谨慎操作！"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>
      <div class="restore-info">
        <p><strong>备份名称：</strong>{{ currentBackup.name }}</p>
        <p><strong>备份类型：</strong>{{ getBackupTypeName(currentBackup.type) }}</p>
        <p><strong>创建时间：</strong>{{ currentBackup.createTime }}</p>
        <p><strong>备份大小：</strong>{{ formatFileSize(currentBackup.size) }}</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="restoreDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmRestore">确定恢复</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Refresh, Download, RefreshRight, Delete
} from '@element-plus/icons-vue'

const backupFormRef = ref(null)

// 备份类型选项
const backupTypeOptions = [
  { value: 'full', label: '全量备份' },
  { value: 'archive', label: '档案数据' },
  { value: 'system', label: '系统配置' },
  { value: 'user', label: '用户数据' }
]

// 加载状态
const loading = ref(false)
const backupList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  sortBy: 'createTime',
  sortOrder: 'desc'
})

// 对话框状态
const backupDialogVisible = ref(false)
const restoreDialogVisible = ref(false)
const currentBackup = ref({})

// 备份表单
const backupForm = reactive({
  name: '',
  type: 'full',
  description: ''
})

// 表单验证规则
const backupRules = {
  name: [
    { required: true, message: '请输入备份名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择备份类型', trigger: 'change' }
  ]
}

// 获取备份列表
const fetchBackupList = async () => {
  loading.value = true
  try {
    // 模拟数据
    setTimeout(() => {
      backupList.value = [
        {
          id: '1',
          name: '每日全量备份-20230315',
          type: 'full',
          size: 1024000000, // 字节
          createTime: '2023-03-15 02:00:00',
          status: 'completed',
          description: '系统每日自动全量备份'
        },
        {
          id: '2',
          name: '档案数据备份-20230314',
          type: 'archive',
          size: 512000000,
          createTime: '2023-03-14 14:30:00',
          status: 'completed',
          description: '手动备份档案数据'
        },
        {
          id: '3',
          name: '系统配置备份-20230310',
          type: 'system',
          size: 10485760,
          createTime: '2023-03-10 10:15:00',
          status: 'completed',
          description: '系统更新前配置备份'
        },
        {
          id: '4',
          name: '每周全量备份-20230312',
          type: 'full',
          size: 1073741824,
          createTime: '2023-03-12 02:00:00',
          status: 'completed',
          description: '系统每周自动全量备份'
        }
      ]
      total.value = 4
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
  const typeMap = {
    'full': '全量备份',
    'archive': '档案数据',
    'system': '系统配置',
    'user': '用户数据'
  }
  return typeMap[type] || '未知类型'
}

// 获取备份类型标签样式
const getBackupTypeTag = (type) => {
  const typeMap = {
    'full': 'danger',
    'archive': 'warning',
    'system': 'info',
    'user': 'success'
  }
  return typeMap[type] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'completed': '已完成',
    'running': '进行中',
    'failed': '失败'
  }
  return statusMap[status] || '未知状态'
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'completed': 'success',
    'running': 'primary',
    'failed': 'danger'
  }
  return statusMap[status] || 'info'
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

// 手动备份
const handleManualBackup = () => {
  const now = new Date()
  const dateStr = now.toISOString().split('T')[0].replace(/-/g, '')
  const timeStr = now.toTimeString().split(' ')[0].replace(/:/g, '')
  
  backupForm.name = `手动备份-${dateStr}${timeStr}`
  backupForm.type = 'full'
  backupForm.description = '手动创建的备份'
  
  backupDialogVisible.value = true
}

// 提交备份表单
const submitBackupForm = async () => {
  if (!backupFormRef.value) return
  
  await backupFormRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success('备份任务已启动，请稍候...')
      backupDialogVisible.value = false
      
      // 模拟备份过程
      const newBackup = {
        id: Date.now().toString(),
        name: backupForm.name,
        type: backupForm.type,
        size: 0,
        createTime: new Date().toLocaleString(),
        status: 'running',
        description: backupForm.description
      }
      
      backupList.value.unshift(newBackup)
      total.value++
      
      // 模拟备份完成
      setTimeout(() => {
        const index = backupList.value.findIndex(item => item.id === newBackup.id)
        if (index !== -1) {
          backupList.value[index].status = 'completed'
          backupList.value[index].size = 512000000 + Math.floor(Math.random() * 512000000)
        }
        ElMessage.success('备份任务已完成')
      }, 5000)
    }
  })
}

// 下载备份
const handleDownload = (row) => {
  ElMessage.success(`正在下载备份：${row.name}`)
  // 实际应用中应调用下载API
}

// 恢复备份
const handleRestore = (row) => {
  currentBackup.value = row
  restoreDialogVisible.value = true
}

// 确认恢复
const confirmRestore = () => {
  ElMessageBox.confirm(
    '恢复数据将会覆盖当前系统数据且不可撤销，是否继续？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage({
      type: 'success',
      message: '数据恢复中，请勿关闭浏览器...',
      duration: 0
    })
    
    // 模拟恢复过程
    setTimeout(() => {
      ElMessage.closeAll()
      ElMessage.success('数据恢复成功，系统将在5秒后自动刷新')
      
      // 模拟页面刷新
      setTimeout(() => {
        refreshBackupList()
      }, 5000)
    }, 3000)
    
    restoreDialogVisible.value = false
  }).catch(() => {})
}

// 删除备份
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除备份 ${row.name} 吗？`,
    '删除备份',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟删除
    backupList.value = backupList.value.filter(item => item.id !== row.id)
    total.value--
    ElMessage.success('备份删除成功')
  }).catch(() => {})
}

// 生命周期钩子
onMounted(() => {
  fetchBackupList()
})
</script>

<style scoped>
.data-backup-container {
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

.backup-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  text-align: right;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.restore-warning {
  margin-bottom: 20px;
}

.restore-info {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.restore-info p {
  margin: 8px 0;
}
</style> 