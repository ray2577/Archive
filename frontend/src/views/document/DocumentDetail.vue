<template>
  <div class="document-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" type="default">
          <el-icon><Back /></el-icon>返回
        </el-button>
        <h2>{{ documentData.name || '文档详情' }}</h2>
      </div>
      <div class="header-actions">
        <el-button-group>
          <el-button type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <el-button type="success" @click="handleDownload">
            <el-icon><Download /></el-icon>下载
          </el-button>
          <el-button type="info" @click="handleShare">
            <el-icon><Share /></el-icon>分享
          </el-button>
          <el-button type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon>删除
          </el-button>
        </el-button-group>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 文档基本信息 -->
      <el-col :span="16">
        <el-card class="document-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-tag :type="statusType" effect="dark">{{ statusText }}</el-tag>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="文档编号">{{ documentData.code }}</el-descriptions-item>
            <el-descriptions-item label="文档类型">{{ formatType(documentData.type) }}</el-descriptions-item>
            <el-descriptions-item label="创建人">{{ documentData.creator }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ documentData.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ documentData.updateTime }}</el-descriptions-item>
            <el-descriptions-item label="文件大小">{{ formatFileSize(documentData.fileSize) }}</el-descriptions-item>
            <el-descriptions-item label="所属分类">{{ formatCategory(documentData.category) }}</el-descriptions-item>
            <el-descriptions-item label="下载次数">{{ documentData.downloadCount }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ documentData.description || '暂无描述' }}
            </el-descriptions-item>
            <el-descriptions-item label="标签" :span="2">
              <el-tag
                v-for="tag in documentData.tags"
                :key="tag"
                class="document-tag"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
              <span v-if="!documentData.tags || documentData.tags.length === 0">暂无标签</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 文档预览 -->
        <el-card class="document-preview-card">
          <template #header>
            <div class="card-header">
              <span>文档预览</span>
              <el-button-group>
                <el-button type="primary" link @click="handleFullscreen">
                  <el-icon><FullScreen /></el-icon>全屏预览
                </el-button>
                <el-button type="primary" link @click="handlePrint">
                  <el-icon><Printer /></el-icon>打印
                </el-button>
              </el-button-group>
            </div>
          </template>

          <div class="preview-container">
            <!-- 如果是PDF文档 -->
            <div v-if="documentData.type === '2'" class="pdf-container">
              <div class="pdf-placeholder">
                <el-icon class="pdf-icon"><Document /></el-icon>
                <p>PDF文档预览</p>
                <el-button type="primary" @click="handleDownload">打开预览</el-button>
              </div>
            </div>
            <!-- 如果是图片 -->
            <div v-else-if="documentData.type === '5'" class="image-container">
              <el-image
                style="width: 100%; max-height: 500px"
                src="https://via.placeholder.com/800x600"
                fit="contain"
              />
            </div>
            <!-- 其他类型文档 -->
            <div v-else class="generic-preview">
              <el-empty description="该文档类型暂不支持在线预览" />
              <div class="preview-actions">
                <el-button type="primary" @click="handleDownload">下载文档</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 侧边信息 -->
      <el-col :span="8">
        <!-- 版本信息 -->
        <el-card class="version-card">
          <template #header>
            <div class="card-header">
              <span>版本历史</span>
              <el-button type="primary" link @click="handleVersionHistory">
                查看全部
              </el-button>
            </div>
          </template>
          
          <el-timeline>
            <el-timeline-item
              v-for="(version, index) in versionHistory"
              :key="index"
              :timestamp="version.time"
              :type="index === 0 ? 'primary' : ''"
            >
              <div class="version-item">
                <h4>{{ version.version }}</h4>
                <p>{{ version.description }}</p>
                <p class="version-user">{{ version.user }}</p>
                <div class="version-actions">
                  <el-button type="primary" size="small" link @click="previewVersion(version)">
                    预览
                  </el-button>
                  <el-button type="primary" size="small" link @click="restoreVersion(version)">
                    恢复
                  </el-button>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <!-- 关联文档 -->
        <el-card class="related-card">
          <template #header>
            <div class="card-header">
              <span>关联文档</span>
              <el-button type="primary" link @click="handleAddRelated">
                添加关联
              </el-button>
            </div>
          </template>
          
          <el-empty v-if="relatedDocuments.length === 0" description="暂无关联文档" />
          
          <el-table
            v-else
            :data="relatedDocuments"
            style="width: 100%"
            size="small"
          >
            <el-table-column prop="name" label="文档名称" />
            <el-table-column prop="relation" label="关系" width="100">
              <template #default="scope">
                <el-tag size="small">{{ scope.row.relation }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button type="primary" link @click="viewRelatedDocument(scope.row)">
                  查看
                </el-button>
                <el-button type="danger" link @click="removeRelatedDocument(scope.row)">
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 操作记录 -->
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>操作记录</span>
            </div>
          </template>
          
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in activityLogs"
              :key="index"
              :timestamp="activity.time"
              :type="getActivityType(activity.action)"
              :hollow="true"
              size="small"
            >
              <span>{{ activity.user }}</span>
              <span class="activity-action">{{ formatAction(activity.action) }}</span>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分享对话框 -->
    <el-dialog
      v-model="shareDialogVisible"
      title="分享文档"
      width="500px"
    >
      <el-form :model="shareForm" label-width="80px">
        <el-form-item label="有效期">
          <el-select v-model="shareForm.expireTime" placeholder="请选择有效期">
            <el-option label="1天" value="1" />
            <el-option label="7天" value="7" />
            <el-option label="30天" value="30" />
            <el-option label="永久" value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item label="访问密码">
          <el-input v-model="shareForm.password" placeholder="为空则无需密码" show-password />
        </el-form-item>
        <el-form-item label="权限">
          <el-checkbox-group v-model="shareForm.permissions">
            <el-checkbox label="view">查看</el-checkbox>
            <el-checkbox label="download">下载</el-checkbox>
            <el-checkbox label="print">打印</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <div v-if="shareLink" class="share-link-box">
        <p>分享链接</p>
        <div class="share-link-input">
          <el-input v-model="shareLink" readonly />
          <el-button type="primary" @click="copyShareLink">复制</el-button>
        </div>
        <p v-if="shareForm.password" class="password-tip">
          密码: <span>{{ shareForm.password }}</span>
        </p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="shareDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="generateShareLink">生成链接</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Back, Edit, Download, Share, Delete, Document,
  FullScreen, Printer
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const documentId = computed(() => route.params.id)

// 文档数据
const documentData = ref({
  id: 1,
  code: 'DOC-2023-001',
  name: '公司规章制度',
  type: '2', // PDF文档
  category: '1',
  creator: '张三',
  createTime: '2023-01-10 10:30:00',
  updateTime: '2023-02-15 14:25:00',
  fileSize: 1024000,
  downloadCount: 30,
  status: '1',
  description: '公司规章制度文档，包含员工行为准则、办公室管理规定等内容。',
  tags: ['规章制度', '人事', '管理']
})

// 状态文本映射
const statusText = computed(() => {
  const map = { '1': '正常', '0': '禁用' }
  return map[documentData.value.status] || '未知'
})

// 状态类型映射
const statusType = computed(() => {
  const map = { '1': 'success', '0': 'danger' }
  return map[documentData.value.status] || 'info'
})

// 版本历史
const versionHistory = ref([
  {
    version: 'v2.0',
    time: '2023-02-15 14:25:00',
    description: '更新公司规章制度，新增办公用品管理规定',
    user: '张三'
  },
  {
    version: 'v1.1',
    time: '2023-01-20 16:45:00',
    description: '修正文字错误，更新部分内容',
    user: '李四'
  },
  {
    version: 'v1.0',
    time: '2023-01-10 10:30:00',
    description: '初始版本',
    user: '张三'
  }
])

// 关联文档
const relatedDocuments = ref([
  {
    id: 2,
    name: '员工手册',
    relation: '关联',
    code: 'DOC-2023-002'
  },
  {
    id: 3,
    name: '办公室管理制度',
    relation: '参考',
    code: 'DOC-2023-003'
  }
])

// 操作记录
const activityLogs = ref([
  {
    user: '张三',
    action: 'edit',
    time: '2023-02-15 14:25:00'
  },
  {
    user: '李四',
    action: 'download',
    time: '2023-02-10 11:20:00'
  },
  {
    user: '王五',
    action: 'view',
    time: '2023-02-05 09:15:00'
  },
  {
    user: '张三',
    action: 'create',
    time: '2023-01-10 10:30:00'
  }
])

// 分享对话框
const shareDialogVisible = ref(false)
const shareForm = ref({
  expireTime: '7',
  password: '',
  permissions: ['view', 'download']
})
const shareLink = ref('')

// 获取文档详情
const fetchDocumentDetail = async () => {
  try {
    // 实际应用中应调用API获取文档详情
    // const response = await getDocumentDetail(documentId.value)
    // documentData.value = response.data
    
    // 模拟数据已在上面定义
  } catch (error) {
    ElMessage.error('获取文档详情失败')
  }
}

// 获取操作记录
const fetchActivityLogs = async () => {
  try {
    // 实际应用中应调用API获取操作记录
    // const response = await getDocumentLogs(documentId.value)
    // activityLogs.value = response.data
    
    // 模拟数据已在上面定义
  } catch (error) {
    ElMessage.error('获取操作记录失败')
  }
}

// 获取版本历史
const fetchVersionHistory = async () => {
  try {
    // 实际应用中应调用API获取版本历史
    // const response = await getDocumentVersions(documentId.value)
    // versionHistory.value = response.data
    
    // 模拟数据已在上面定义
  } catch (error) {
    ElMessage.error('获取版本历史失败')
  }
}

// 获取关联文档
const fetchRelatedDocuments = async () => {
  try {
    // 实际应用中应调用API获取关联文档
    // const response = await getRelatedDocuments(documentId.value)
    // relatedDocuments.value = response.data
    
    // 模拟数据已在上面定义
  } catch (error) {
    ElMessage.error('获取关联文档失败')
  }
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

// 格式化文档类型
const formatType = (type) => {
  const typeMap = {
    '1': '文本文档',
    '2': 'PDF文档',
    '3': 'Excel表格',
    '4': 'Word文档',
    '5': '图片'
  }
  return typeMap[type] || '未知类型'
}

// 格式化分类
const formatCategory = (category) => {
  const categoryMap = {
    '1': '规章制度',
    '2': '技术文档',
    '3': '会议记录',
    '4': '项目资料'
  }
  return categoryMap[category] || '未分类'
}

// 格式化操作
const formatAction = (action) => {
  const actionMap = {
    'create': '创建文档',
    'edit': '编辑文档',
    'download': '下载文档',
    'view': '查看文档',
    'share': '分享文档',
    'delete': '删除文档'
  }
  return actionMap[action] || action
}

// 获取操作记录类型
const getActivityType = (action) => {
  const typeMap = {
    'create': 'success',
    'edit': 'warning',
    'delete': 'danger',
    'download': 'info',
    'view': '',
    'share': 'primary'
  }
  return typeMap[action] || ''
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理编辑
const handleEdit = () => {
  router.push(`/document/edit/${documentId.value}`)
}

// 处理下载
const handleDownload = () => {
  ElMessage.success('文档下载中...')
  // 实际应用中调用下载API
}

// 处理分享
const handleShare = () => {
  shareDialogVisible.value = true
  shareLink.value = ''
}

// 处理删除
const handleDelete = () => {
  ElMessageBox.confirm(
    `确定要删除文档 ${documentData.value.name} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 实际应用中应调用API删除文档
      // await deleteDocument(documentId.value)
      ElMessage.success('删除成功')
      router.push('/document/list')
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 处理全屏预览
const handleFullscreen = () => {
  ElMessage.info('全屏预览功能开发中')
}

// 处理打印
const handlePrint = () => {
  ElMessage.info('打印功能开发中')
}

// 查看版本历史
const handleVersionHistory = () => {
  ElMessage.info('版本历史查看功能开发中')
}

// 预览版本
const previewVersion = (version) => {
  ElMessage.info(`预览版本：${version.version}`)
}

// 恢复版本
const restoreVersion = (version) => {
  ElMessageBox.confirm(
    `确定要恢复到版本 ${version.version} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success(`已恢复到版本：${version.version}`)
  }).catch(() => {})
}

// 添加关联文档
const handleAddRelated = () => {
  ElMessage.info('添加关联文档功能开发中')
}

// 查看关联文档
const viewRelatedDocument = (document) => {
  router.push(`/document/detail/${document.id}`)
}

// 移除关联文档
const removeRelatedDocument = (document) => {
  ElMessageBox.confirm(
    `确定要移除关联文档 ${document.name} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = relatedDocuments.value.findIndex(d => d.id === document.id)
    if (index !== -1) {
      relatedDocuments.value.splice(index, 1)
    }
    ElMessage.success('移除成功')
  }).catch(() => {})
}

// 生成分享链接
const generateShareLink = () => {
  // 模拟生成分享链接
  const baseUrl = window.location.origin
  const token = Math.random().toString(36).substring(2, 15)
  shareLink.value = `${baseUrl}/share/${token}`
  ElMessage.success('分享链接已生成')
}

// 复制分享链接
const copyShareLink = () => {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 生命周期钩子
onMounted(() => {
  fetchDocumentDetail()
  fetchActivityLogs()
  fetchVersionHistory()
  fetchRelatedDocuments()
})
</script>

<style scoped>
.document-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left h2 {
  margin: 0 0 0 15px;
  font-size: 22px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.document-card,
.document-preview-card,
.version-card,
.related-card,
.activity-card {
  margin-bottom: 20px;
}

.document-tag {
  margin-right: 8px;
  margin-bottom: 5px;
}

.preview-container {
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.pdf-container {
  width: 100%;
  height: 100%;
}

.pdf-placeholder {
  text-align: center;
  padding: 50px 0;
}

.pdf-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 20px;
}

.image-container {
  width: 100%;
  text-align: center;
}

.generic-preview {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 0;
}

.preview-actions {
  margin-top: 20px;
}

.version-item {
  margin-bottom: 10px;
}

.version-item h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.version-item p {
  margin: 0 0 5px 0;
  color: #606266;
}

.version-user {
  font-size: 12px;
  color: #909399;
}

.version-actions {
  margin-top: 5px;
}

.activity-action {
  margin-left: 8px;
  color: #409eff;
}

.share-link-box {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.share-link-input {
  display: flex;
  margin-bottom: 10px;
}

.share-link-input .el-input {
  margin-right: 10px;
}

.password-tip {
  font-size: 14px;
  color: #909399;
}

.password-tip span {
  font-weight: bold;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 