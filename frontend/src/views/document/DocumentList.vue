<template>
  <div class="document-list-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>文档列表</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddDocument">
          <el-icon><Plus /></el-icon>新增文档
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>批量导入
        </el-button>
        <el-button type="warning" @click="handleExport">
          <el-icon><Download /></el-icon>导出文档
        </el-button>
      </div>
    </div>
    
    <!-- 搜索筛选区 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文档编号">
          <el-input v-model="searchForm.code" placeholder="请输入文档编号" clearable />
        </el-form-item>
        <el-form-item label="文档名称">
          <el-input v-model="searchForm.name" placeholder="请输入文档名称" clearable />
        </el-form-item>
        <el-form-item label="文档类型">
          <el-select v-model="searchForm.type" placeholder="请选择文档类型" clearable>
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
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
    
    <!-- 数据表格 -->
    <el-card class="table-container">
      <template #header>
        <div class="table-header">
          <span>文档列表 ({{ total }})</span>
          <div class="table-actions">
            <el-button-group>
              <el-tooltip content="刷新">
                <el-button @click="fetchDocumentList">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="密度">
                <el-dropdown @command="handleSizeChange">
                  <el-button>
                    <el-icon><GridKey /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="default">默认</el-dropdown-item>
                      <el-dropdown-item command="large">宽松</el-dropdown-item>
                      <el-dropdown-item command="small">紧凑</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </el-tooltip>
              <el-tooltip content="列设置">
                <el-button @click="columnsDialogVisible = true">
                  <el-icon><SetUp /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="documentList"
        :size="tableSize"
        border
        highlight-current-row
        @sort-change="handleSortChange"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column 
          v-for="col in visibleColumns" 
          :key="col.prop" 
          :prop="col.prop" 
          :label="col.label" 
          :width="col.width" 
          :sortable="col.sortable" 
          :show-overflow-tooltip="true"
        >
          <template #default="scope" v-if="col.prop === 'status'">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
          <template #default="scope" v-else-if="col.prop === 'createTime'">
            {{ formatDate(scope.row.createTime) }}
          </template>
          <template #default="scope" v-else-if="col.prop === 'fileSize'">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-tooltip content="查看详情">
                <el-button type="primary" link @click="handleView(scope.row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑文档">
                <el-button type="primary" link @click="handleEdit(scope.row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="分享文档">
                <el-button type="primary" link @click="handleShare(scope.row)">
                  <el-icon><Share /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="下载文档">
                <el-button type="primary" link @click="handleDownload(scope.row)">
                  <el-icon><Download /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除文档">
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
    
    <!-- 列设置对话框 -->
    <el-dialog
      v-model="columnsDialogVisible"
      title="列设置"
      width="500px"
    >
      <el-transfer
        v-model="visibleColumnProps"
        :data="allColumns"
        filterable
        :titles="['隐藏列', '显示列']"
        @change="updateVisibleColumns"
      >
        <template #default="{ option }">
          {{ option.label }}
        </template>
      </el-transfer>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="columnsDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveColumnsSettings">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Upload, Download, Search, Refresh,
  GridKey, SetUp, View, Edit, Share, Delete
} from '@element-plus/icons-vue'
// API 导入 - 实际应用中需要创建相应的API文件
// import { getDocumentList, createDocument, updateDocument, deleteDocument } from '@/api/document'

const router = useRouter()

// 文档类型选项
const typeOptions = [
  { value: '1', label: '文本文档' },
  { value: '2', label: 'PDF文档' },
  { value: '3', label: 'Excel表格' },
  { value: '4', label: 'Word文档' },
  { value: '5', label: '其他格式' }
]

// 文档分类选项 - 从分类管理获取
const categoryOptions = ref([
  { value: '1', label: '规章制度' },
  { value: '2', label: '技术文档' },
  { value: '3', label: '会议记录' },
  { value: '4', label: '项目资料' }
])

// 所有列定义
const allColumns = [
  { key: 'id', label: 'ID', prop: 'id', width: '80', sortable: true },
  { key: 'code', label: '文档编号', prop: 'code', width: '150', sortable: true },
  { key: 'name', label: '文档名称', prop: 'name', width: '200', sortable: true },
  { key: 'type', label: '文档类型', prop: 'type', width: '120', sortable: true },
  { key: 'category', label: '所属分类', prop: 'category', width: '120', sortable: true },
  { key: 'creator', label: '创建人', prop: 'creator', width: '120', sortable: true },
  { key: 'createTime', label: '创建时间', prop: 'createTime', width: '150', sortable: true },
  { key: 'updateTime', label: '更新时间', prop: 'updateTime', width: '150', sortable: true },
  { key: 'fileSize', label: '文件大小', prop: 'fileSize', width: '120', sortable: true },
  { key: 'downloadCount', label: '下载次数', prop: 'downloadCount', width: '100', sortable: true },
  { key: 'status', label: '状态', prop: 'status', width: '100', sortable: true }
]

// 搜索表单
const searchForm = reactive({
  code: '',
  name: '',
  type: '',
  category: ''
})

// 表格状态
const loading = ref(false)
const documentList = ref([])
const total = ref(0)
const tableSize = ref('default')
const selectedRows = ref([])
const visibleColumnProps = ref(['code', 'name', 'type', 'category', 'createTime', 'fileSize'])
const columnsDialogVisible = ref(false)

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  sortBy: '',
  sortOrder: ''
})

// 计算可见列
const visibleColumns = computed(() => {
  return allColumns.filter(col => visibleColumnProps.value.includes(col.key))
})

// 生命周期钩子
onMounted(() => {
  fetchDocumentList()
})

// 获取文档列表
const fetchDocumentList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    // const res = await getDocumentList(queryParams)
    // documentList.value = res.data.list
    // total.value = res.data.total
    
    // 模拟数据
    setTimeout(() => {
      documentList.value = [
        {
          id: 1,
          code: 'DOC-2023-001',
          name: '公司规章制度',
          type: '2',
          category: '1',
          creator: '张三',
          createTime: '2023-01-10 10:30:00',
          updateTime: '2023-02-15 14:25:00',
          fileSize: 1024000,
          downloadCount: 30,
          status: '1'
        },
        {
          id: 2,
          code: 'DOC-2023-002',
          name: '产品技术说明书',
          type: '4',
          category: '2',
          creator: '李四',
          createTime: '2023-02-18 09:15:00',
          updateTime: '2023-03-01 16:40:00',
          fileSize: 2048000,
          downloadCount: 45,
          status: '1'
        }
      ]
      total.value = 2
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取文档列表失败')
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchDocumentList()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 排序变化
const handleSortChange = ({ prop, order }) => {
  queryParams.sortBy = prop
  queryParams.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  fetchDocumentList()
}

// 页面大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  fetchDocumentList()
}

// 页码变化
const handlePageChange = (page) => {
  queryParams.page = page
  fetchDocumentList()
}

// 选中行变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 更新可见列
const updateVisibleColumns = () => {
  // 更新后的逻辑
}

// 保存列设置
const saveColumnsSettings = () => {
  // 保存逻辑
  columnsDialogVisible.value = false
  ElMessage.success('列设置已保存')
}

// 格式化状态
const formatStatus = (status) => {
  const map = { '1': '正常', '0': '禁用' }
  return map[status] || '未知'
}

// 状态类型
const getStatusType = (status) => {
  const map = { '1': 'success', '0': 'danger' }
  return map[status] || 'info'
}

// 格式化日期
const formatDate = (date) => {
  return date || '-'
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

// 添加文档
const handleAddDocument = () => {
  router.push('/document/add')
}

// 查看文档
const handleView = (row) => {
  router.push(`/document/detail/${row.id}`)
}

// 编辑文档
const handleEdit = (row) => {
  router.push(`/document/edit/${row.id}`)
}

// 分享文档
const handleShare = (row) => {
  ElMessage.success(`文档分享功能开发中：${row.name}`)
}

// 下载文档
const handleDownload = (row) => {
  ElMessage.success(`下载文档：${row.name}`)
}

// 删除文档
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除文档 ${row.name} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 模拟API调用
      // await deleteDocument(row.id)
      ElMessage.success('删除成功')
      fetchDocumentList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 导入文档
const handleImport = () => {
  ElMessage.info('文档导入功能开发中')
}

// 导出文档
const handleExport = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  ElMessage.success(`已选择${selectedRows.value.length}条记录，导出处理中...`)
}
</script>

<style scoped>
.document-list-container {
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

.table-container {
  margin-bottom: 20px;
}

.table-header {
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
  margin-top: 20px;
}
</style> 