<template>
  <div class="archive-detail-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="title-section">
        <el-button icon="Back" @click="goBack">返回</el-button>
        <h2>档案详情</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleEdit" v-if="canEdit">
          <el-icon><Edit /></el-icon>编辑
        </el-button>
        <el-button type="warning" @click="handleBorrow" v-if="canBorrow">
          <el-icon><TopRight /></el-icon>借阅
        </el-button>
        <el-button type="success" @click="handleReturn" v-if="canReturn">
          <el-icon><Bottom /></el-icon>归还
        </el-button>
      </div>
    </div>

    <!-- 档案信息卡片 -->
    <el-card class="info-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-tag :type="archiveInfo.status === 1 ? 'success' : 'warning'">
            {{ archiveInfo.status === 1 ? '在库' : '借出' }}
          </el-tag>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="档案编号">{{ archiveInfo.code }}</el-descriptions-item>
        <el-descriptions-item label="档案名称">{{ archiveInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="档案类型">
          <el-tag :type="getTypeTagType(archiveInfo.type)">
            {{ getTypeName(archiveInfo.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="存放位置">{{ archiveInfo.location }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ archiveInfo.responsible }}</el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ formatFileSize(archiveInfo.fileSize) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(archiveInfo.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(archiveInfo.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="借阅次数">{{ archiveInfo.borrowCount }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 档案内容 -->
    <el-card class="content-card">
      <template #header>
        <div class="card-header">
          <span>档案内容</span>
        </div>
      </template>
      <div class="content-container">
        <div v-if="archiveInfo.fileType === 'pdf'" class="pdf-viewer">
          <div class="pdf-placeholder">
            <el-icon class="pdf-icon"><Document /></el-icon>
            <span>PDF 文档预览</span>
            <el-button type="primary" size="small" class="view-button">
              <el-icon><View /></el-icon>查看文档
            </el-button>
          </div>
        </div>
        <div v-else-if="archiveInfo.fileType === 'image'" class="image-viewer">
          <el-image :src="archiveInfo.fileUrl || '/images/placeholder.png'" fit="contain" class="preview-image">
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
                <span>图片加载失败</span>
              </div>
            </template>
          </el-image>
        </div>
        <div v-else class="text-content">
          <p v-if="archiveInfo.content">{{ archiveInfo.content }}</p>
          <p v-else class="empty-content">暂无内容</p>
        </div>
      </div>
    </el-card>

    <!-- 借阅记录 -->
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span>借阅历史</span>
          <el-button type="link" icon="More" @click="showAllHistory">查看全部</el-button>
        </div>
      </template>
      <el-table :data="borrowHistory" stripe style="width: 100%">
        <el-table-column prop="borrower" label="借阅人" width="120" />
        <el-table-column prop="department" label="借阅部门" width="150" />
        <el-table-column prop="borrowDate" label="借阅日期" width="150" :formatter="formatTableDate" />
        <el-table-column prop="returnDate" label="预计归还" width="150" :formatter="formatTableDate" />
        <el-table-column prop="actualReturnDate" label="实际归还" width="150" :formatter="formatTableDate">
          <template #default="scope">
            <span v-if="scope.row.actualReturnDate">{{ formatDate(scope.row.actualReturnDate) }}</span>
            <el-tag v-else type="warning">未归还</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已归还' : '借阅中' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getArchiveDetail, borrowArchive, returnArchive } from '@/api/archive'
import { getBorrowRecords } from '@/api/archive'

const route = useRoute()
const router = useRouter()
const archiveId = ref(route.params.id)
const loading = ref(true)
const archiveInfo = ref({})
const borrowHistory = ref([])

// 权限控制计算属性
const canEdit = computed(() => true) // 实际项目中应该根据用户权限判断
const canBorrow = computed(() => archiveInfo.value.status === 1) // 在库才能借阅
const canReturn = computed(() => archiveInfo.value.status === 2) // 借出才能归还

// 获取档案详情
const fetchArchiveDetail = async () => {
  try {
    loading.value = true
    const res = await getArchiveDetail(archiveId.value)
    archiveInfo.value = res.data || {
      id: archiveId.value,
      code: `ARC${String(archiveId.value).padStart(4, '0')}`,
      name: '示例档案 ' + archiveId.value,
      type: String(Math.floor(Math.random() * 5) + 1),
      location: 'A1-01',
      responsible: '张三',
      fileSize: 1024 * 1024 * 2,
      borrowCount: 5,
      status: Math.random() > 0.3 ? 1 : 2,
      createTime: '2023-01-15T08:30:00',
      updateTime: '2023-04-20T14:22:00',
      fileType: ['pdf', 'image', 'text'][Math.floor(Math.random() * 3)],
      content: '这是档案的详细内容，包含了相关的关键信息、文档说明以及其他重要的档案资料。这些信息对于理解档案的背景和历史非常重要。',
      fileUrl: ''
    }
    
    // 获取借阅历史
    fetchBorrowHistory()
  } catch (error) {
    console.error('获取档案详情失败:', error)
    ElMessage.error('获取档案详情失败')
  } finally {
    loading.value = false
  }
}

// 获取借阅历史
const fetchBorrowHistory = async () => {
  try {
    const res = await getBorrowRecords({ archiveId: archiveId.value, page: 1, pageSize: 5 })
    borrowHistory.value = res.data?.list || []
  } catch (error) {
    console.error('获取借阅历史失败:', error)
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 编辑档案
const handleEdit = () => {
  router.push(`/archive/edit/${archiveId.value}`)
}

// 借阅档案
const handleBorrow = () => {
  router.push({
    path: '/archive/borrow',
    query: { archiveId: archiveId.value }
  })
}

// 归还档案
const handleReturn = async () => {
  try {
    await returnArchive(archiveId.value)
    ElMessage.success('归还成功')
    archiveInfo.value.status = 1
    fetchBorrowHistory()
  } catch (error) {
    console.error('归还失败:', error)
    ElMessage.error('归还失败')
  }
}

// 查看全部借阅历史
const showAllHistory = () => {
  router.push({
    path: '/archive/borrow',
    query: { archiveId: archiveId.value }
  })
}

// 获取档案类型名称
const getTypeName = (type) => {
  const typeMap = {
    '1': '财务档案',
    '2': '人事档案',
    '3': '项目档案',
    '4': '合同档案',
    '5': '其他档案'
  }
  return typeMap[type] || '未知类型'
}

// 获取档案类型标签样式
const getTypeTagType = (type) => {
  const typeTagMap = {
    '1': 'success',
    '2': 'warning',
    '3': 'danger',
    '4': 'info',
    '5': ''
  }
  return typeTagMap[type] || ''
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化表格日期
const formatTableDate = (row, column, cellValue) => {
  return formatDate(cellValue)
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
  fetchArchiveDetail()
})
</script>

<style scoped>
.archive-detail-container {
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

.title-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-section h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.info-card, 
.content-card, 
.history-card {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: none;
}

.info-card:hover, 
.content-card:hover, 
.history-card:hover {
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

/* Content container styling */
.content-container {
  min-height: 200px;
  padding: 20px;
}

/* PDF viewer styling */
.pdf-viewer {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 400px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border: 1px dashed #d9d9d9;
}

.pdf-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.pdf-icon {
  font-size: 64px;
  color: #909399;
}

.view-button {
  margin-top: 16px;
}

/* Image viewer styling */
.image-viewer {
  width: 100%;
  height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.preview-image {
  max-height: 380px;
  max-width: 100%;
  object-fit: contain;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  font-size: 16px;
  color: #909399;
}

.image-error .el-icon {
  font-size: 64px;
}

/* Text content styling */
.text-content {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
}

.empty-content {
  text-align: center;
  color: #909399;
  font-style: italic;
}

/* Table styling enhancements */
:deep(.el-table) {
  margin-top: 10px;
  border-radius: 4px;
  overflow: hidden;
}

:deep(.el-table__row:hover) {
  background-color: #ecf5ff !important;
}

:deep(.el-descriptions__body) {
  background-color: #fff;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}

:deep(.el-tag) {
  border-radius: 4px;
  padding: 0 8px;
  height: 24px;
  line-height: 22px;
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .archive-detail-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  :deep(.el-descriptions) {
    padding: 8px;
  }
}
</style> 