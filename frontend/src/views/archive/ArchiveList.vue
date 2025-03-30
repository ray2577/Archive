<template>
  <div class="archive-list-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>档案列表</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddArchive">
          <el-icon><Plus /></el-icon>新增档案
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>批量导入
        </el-button>
        <el-button type="warning" @click="handleExport">
          <el-icon><Download /></el-icon>导出档案
        </el-button>
      </div>
    </div>
    
    <!-- 搜索筛选区 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="档案编号">
          <el-input v-model="searchForm.code" placeholder="请输入档案编号" clearable />
        </el-form-item>
        <el-form-item label="档案名称">
          <el-input v-model="searchForm.name" placeholder="请输入档案名称" clearable />
        </el-form-item>
        <el-form-item label="档案类型">
          <el-select v-model="searchForm.type" placeholder="请选择档案类型" clearable>
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
          <span>档案列表 ({{ total }})</span>
          <div class="table-actions">
            <el-button-group>
              <el-tooltip content="刷新">
                <el-button @click="fetchArchiveList">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="密度">
                <el-dropdown @command="handleSizeChange">
                  <!-- <el-button>
                    <el-icon><GridKey /></el-icon>
                  </el-button> -->
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
        :data="archiveList"
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
              <el-tooltip content="编辑档案">
                <el-button type="primary" link @click="handleEdit(scope.row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="申请借阅">
                <el-button type="primary" link :disabled="scope.row.status === 2" @click="handleBorrow(scope.row)">
                  <el-icon><TopRight /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="下载档案">
                <el-button type="primary" link @click="handleDownload(scope.row)">
                  <el-icon><Download /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除档案">
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
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
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
   SetUp, View, Edit, TopRight, Delete
} from '@element-plus/icons-vue'
import { getArchiveList, createArchive, updateArchive, deleteArchive } from '@/api/archive'

const router = useRouter()

// 档案类型选项
const typeOptions = [
  { value: '1', label: '财务档案' },
  { value: '2', label: '人事档案' },
  { value: '3', label: '项目档案' },
  { value: '4', label: '合同档案' },
  { value: '5', label: '其他档案' }
]

// 档案状态选项
const statusOptions = [
  { value: '1', label: '在库' },
  { value: '2', label: '借出' }
]

// 所有列定义
const allColumns = [
  { key: 'id', label: 'ID', prop: 'id', width: '80', sortable: true },
  { key: 'code', label: '档案编号', prop: 'code', width: '150', sortable: true },
  { key: 'name', label: '档案名称', prop: 'name', width: '200', sortable: true },
  { key: 'type', label: '档案类型', prop: 'type', width: '120', sortable: true },
  { key: 'location', label: '存放位置', prop: 'location', width: '120', sortable: true },
  { key: 'responsible', label: '负责人', prop: 'responsible', width: '120', sortable: true },
  { key: 'createTime', label: '创建时间', prop: 'createTime', width: '150', sortable: true },
  { key: 'updateTime', label: '更新时间', prop: 'updateTime', width: '150', sortable: true },
  { key: 'fileSize', label: '文件大小', prop: 'fileSize', width: '120', sortable: true },
  { key: 'borrowCount', label: '借阅次数', prop: 'borrowCount', width: '100', sortable: true },
  { key: 'status', label: '状态', prop: 'status', width: '100', sortable: true }
]

// 搜索表单
const searchForm = reactive({
  code: '',
  name: '',
  type: '',
  status: ''
})

// 表格状态
const loading = ref(false)
const archiveList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableSize = ref('default')
const selectedRows = ref([])
const visibleColumnProps = ref(['code', 'name', 'type', 'location', 'createTime', 'status'])
const columnsDialogVisible = ref(false)

// 计算属性：可见列
const visibleColumns = computed(() => {
  return allColumns.filter(col => visibleColumnProps.value.includes(col.prop))
})

// 获取档案列表数据
const fetchArchiveList = async () => {
  loading.value = true
  
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    
    // 模拟数据，实际项目中应该调用API
    // const { data } = await getArchiveList(params)
    // archiveList.value = data.list
    // total.value = data.total
    
    // 模拟数据
    setTimeout(() => {
      const mockData = Array.from({ length: pageSize.value }, (_, i) => ({
        id: (currentPage.value - 1) * pageSize.value + i + 1,
        code: `ARC${String(i + 1).padStart(4, '0')}`,
        name: ['财务报表', '人事档案', '会议纪要', '合同文本', '技术文档'][Math.floor(Math.random() * 5)] + ` ${i + 1}`,
        type: String((Math.floor(Math.random() * 5) + 1)),
        location: `${String.fromCharCode(65 + Math.floor(Math.random() * 3))}${Math.floor(Math.random() * 3) + 1}`,
        responsible: ['张三', '李四', '王五', '赵六'][Math.floor(Math.random() * 4)],
        createTime: new Date(new Date().getTime() - Math.floor(Math.random() * 1000 * 3600 * 24 * 30)).toISOString(),
        updateTime: new Date().toISOString(),
        fileSize: Math.floor(Math.random() * 10000000),
        borrowCount: Math.floor(Math.random() * 20),
        status: Math.floor(Math.random() * 2) + 1
      }))
      
      archiveList.value = mockData
      total.value = 1286
      loading.value = false
    }, 500)
  } catch (error) {
    console.error('Error fetching archive list:', error)
    ElMessage.error('获取档案列表失败，请稍后重试')
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchArchiveList()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchArchiveList()
}

// 分页处理
const handleSizeChange = (val) => {
  if (typeof val === 'string') {
    // 处理表格密度变化
    tableSize.value = val
    return
  }
  
  pageSize.value = val
  fetchArchiveList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchArchiveList()
}

// 排序处理
const handleSortChange = ({ prop, order }) => {
  // 实现排序逻辑
  console.log('Sort changed:', prop, order)
  fetchArchiveList()
}

// 选择行变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 更新可见列
const updateVisibleColumns = (value) => {
  visibleColumnProps.value = value
}

// 保存列设置
const saveColumnsSettings = () => {
  // 保存到本地存储
  localStorage.setItem('archiveListColumns', JSON.stringify(visibleColumnProps.value))
  columnsDialogVisible.value = false
}

// 档案操作
const handleAddArchive = () => {
  router.push('/archive/add')
}

const handleImport = () => {
  // 实现批量导入逻辑
}

const handleExport = () => {
  // 实现导出逻辑
}

const handleView = (row) => {
  router.push(`/archive/detail/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/archive/edit/${row.id}`)
}

const handleBorrow = (row) => {
  router.push({
    path: '/archive/borrow',
    query: { archiveId: row.id }
  })
}

const handleDownload = (row) => {
  // 实现下载逻辑
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除档案 "${row.name}" 吗？此操作不可恢复。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      // 模拟删除成功
      ElMessage.success('删除成功')
      fetchArchiveList()
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 工具函数
const getStatusType = (status) => {
  const typeMap = {
    '1': 'success',
    '2': 'warning',
    '3': 'info',
    '4': 'danger',
    '5': ''
  }
  return typeMap[status] || 'info'
}

const formatStatus = (status) => {
  const statusMap = {
    '1': '在库',
    '2': '借出'
  }
  return statusMap[status] || status
}

const formatDate = (date) => {
  if (!date) return '未知'
  return new Date(date).toLocaleString()
}

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

// 组件初始化
onMounted(() => {
  // 从本地存储恢复列设置
  const savedColumns = localStorage.getItem('archiveListColumns')
  if (savedColumns) {
    try {
      visibleColumnProps.value = JSON.parse(savedColumns)
    } catch (e) {
      console.error('Error parsing saved columns:', e)
    }
  }
  
  // 加载数据
  fetchArchiveList()
})
</script>

<style scoped>
.archive-list-container {
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
  font-size: 20px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-container {
  margin-bottom: 20px;
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
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 