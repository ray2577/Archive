<template>
  <div class="archive-search-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>高级搜索</h2>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form 
        ref="searchFormRef" 
        :model="searchForm" 
        label-position="top"
        class="search-form"
      >
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="档案编号">
              <el-input 
                v-model="searchForm.code" 
                placeholder="请输入档案编号" 
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="档案名称">
              <el-input 
                v-model="searchForm.name" 
                placeholder="请输入档案名称" 
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="档案类型">
              <el-select 
                v-model="searchForm.type" 
                placeholder="请选择档案类型" 
                clearable
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
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="创建时间">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="状态">
              <el-select 
                v-model="searchForm.status" 
                placeholder="请选择状态" 
                clearable
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
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="关键词">
              <el-input 
                v-model="searchForm.keyword" 
                placeholder="请输入关键词" 
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="内容搜索">
              <el-input 
                v-model="searchForm.content" 
                type="textarea" 
                rows="3" 
                placeholder="请输入要搜索的内容" 
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <div class="search-actions">
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>搜索
          </el-button>
        </div>
      </el-form>
    </el-card>
    
    <!-- 搜索结果 -->
    <el-card class="result-card" v-loading="loading">
      <template #header>
        <div class="result-header">
          <span>搜索结果 ({{ total }})</span>
          <div class="result-actions">
            <el-dropdown @command="handleExport">
              <el-button type="primary">
                <el-icon><Download /></el-icon>导出结果
                <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="excel">导出Excel</el-dropdown-item>
                  <el-dropdown-item command="pdf">导出PDF</el-dropdown-item>
                  <el-dropdown-item command="csv">导出CSV</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>
      
      <div v-if="searched">
        <div v-if="searchResults.length > 0">
          <el-table
            :data="searchResults"
            style="width: 100%"
            border
            @row-click="handleRowClick"
          >
            <el-table-column type="expand">
              <template #default="props">
                <div class="expanded-row">
                  <el-descriptions :column="3" border size="small">
                    <el-descriptions-item label="创建时间">{{ formatDate(props.row.createTime) }}</el-descriptions-item>
                    <el-descriptions-item label="更新时间">{{ formatDate(props.row.updateTime) }}</el-descriptions-item>
                    <el-descriptions-item label="借阅次数">{{ props.row.borrowCount }}</el-descriptions-item>
                    <el-descriptions-item label="文件大小">{{ formatFileSize(props.row.fileSize) }}</el-descriptions-item>
                    <el-descriptions-item label="文件类型">{{ formatFileType(props.row.fileType) }}</el-descriptions-item>
                    <el-descriptions-item label="负责人">{{ props.row.responsible }}</el-descriptions-item>
                    <el-descriptions-item label="关键词" :span="3">
                      <el-tag 
                        v-for="(tag, index) in props.row.keywords" 
                        :key="index" 
                        size="small" 
                        class="keyword-tag"
                      >
                        {{ tag }}
                      </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="描述" :span="3">{{ props.row.description }}</el-descriptions-item>
                  </el-descriptions>
                  <div class="expanded-actions">
                    <el-button-group>
                      <el-button type="primary" @click.stop="viewDetail(props.row)">
                        <el-icon><View /></el-icon>查看详情
                      </el-button>
                      <el-button type="success" @click.stop="handleBorrow(props.row)">
                        <el-icon><PriceTag /></el-icon>申请借阅
                      </el-button>
                      <el-button type="warning" @click.stop="handleDownload(props.row)">
                        <el-icon><Download /></el-icon>下载档案
                      </el-button>
                    </el-button-group>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="code" label="档案编号" width="150" />
            <el-table-column prop="name" label="档案名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="120">
              <template #default="scope">
                {{ formatType(scope.row.type) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ formatStatus(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button-group>
                  <el-tooltip content="查看">
                    <el-button type="primary" link @click.stop="viewDetail(scope.row)">
                      <el-icon><View /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="借阅">
                    <el-button type="success" link @click.stop="handleBorrow(scope.row)">
                      <el-icon><PriceTag /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="下载">
                    <el-button type="warning" link @click.stop="handleDownload(scope.row)">
                      <el-icon><Download /></el-icon>
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
        </div>
        <el-empty description="未找到符合条件的档案" v-else />
      </div>
      <el-empty description="请输入搜索条件并点击搜索按钮" v-else />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search, Refresh, Download, View, PriceTag, ArrowDown
} from '@element-plus/icons-vue'
import { searchArchives } from '@/api/archive'

const router = useRouter()
const searchFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 是否已搜索
const searched = ref(false)

// 搜索结果
const searchResults = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 日期范围
const dateRange = ref([])

// 搜索表单
const searchForm = reactive({
  code: '',
  name: '',
  type: '',
  status: '',
  keyword: '',
  content: '',
  startDate: '',
  endDate: ''
})

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

// 监听日期范围变化
watch(dateRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    searchForm.startDate = newVal[0]
    searchForm.endDate = newVal[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
})

// 执行搜索
const handleSearch = async () => {
  loading.value = true
  
  try {
    // 构建查询参数
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      code: searchForm.code,
      name: searchForm.name,
      type: searchForm.type,
      status: searchForm.status,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate,
      keyword: searchForm.keyword,
      content: searchForm.content
    }
    
    // 实际项目中应该调用API
    // const { data } = await searchArchives(params)
    // searchResults.value = data.list
    // total.value = data.total
    
    // 模拟数据
    setTimeout(() => {
      const mockData = generateMockData()
      searchResults.value = mockData
      total.value = Math.floor(Math.random() * 500) + 20 // 随机生成总数
      
      searched.value = true
      loading.value = false
    }, 800)
  } catch (error) {
    console.error('Error performing search:', error)
    ElMessage.error('搜索失败，请稍后重试')
    loading.value = false
  }
}

// 重置搜索条件
const resetSearch = () => {
  if (searchFormRef.value) {
    searchFormRef.value.resetFields()
  }
  
  Object.keys(searchForm).forEach(key => {
    if (Array.isArray(searchForm[key])) {
      searchForm[key] = []
    } else {
      searchForm[key] = ''
    }
  })
  dateRange.value = []
  currentPage.value = 1
  searchResults.value = []
  searched.value = false
}

// 生成模拟数据
const generateMockData = () => {
  const result = []
  const count = pageSize.value
  
  for (let i = 0; i < count; i++) {
    const typeIndex = Math.floor(Math.random() * typeOptions.length)
    const statusIndex = Math.floor(Math.random() * statusOptions.length)
    const fileTypes = ['pdf', 'word', 'excel', 'image']
    const fileTypeIndex = Math.floor(Math.random() * fileTypes.length)
    
    result.push({
      id: i + 1,
      code: `AR${new Date().getFullYear()}${String(i + 1).padStart(4, '0')}`,
      name: ['财务报表', '人事档案', '会议纪要', '合同文本', '技术文档'][Math.floor(Math.random() * 5)] + ` ${i + 1}`,
      type: typeOptions[typeIndex].value,
      status: statusOptions[statusIndex].value,
      createTime: new Date(new Date().getTime() - Math.floor(Math.random() * 1000 * 3600 * 24 * 30)).toISOString(),
      updateTime: new Date().toISOString(),
      fileSize: Math.floor(Math.random() * 10000000),
      fileType: fileTypes[fileTypeIndex],
      borrowCount: Math.floor(Math.random() * 20),
      keywords: ['财务', '季度报表', '2023', '审计', '合同'].slice(0, Math.floor(Math.random() * 4) + 1),
      description: '这是一份关于' + ['财务', '人事', '行政', '技术'][Math.floor(Math.random() * 4)] + '方面的档案文件，包含了详细的信息和数据。'
    })
  }
  
  return result
}

// 处理排序或筛选变化
const handleSizeChange = (val) => {
  pageSize.value = val
  handleSearch()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  handleSearch()
}

// 处理行点击
const handleRowClick = (row) => {
  viewDetail(row)
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/archive/detail/${row.id}`)
}

// 申请借阅
const handleBorrow = (row) => {
  ElMessage.success(`已申请借阅《${row.name}》，待审核`)
}

// 下载档案
const handleDownload = (row) => {
  ElMessage.success(`《${row.name}》下载中...`)
}

// 导出结果
const handleExport = (command) => {
  if (searchResults.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  const exportTypeMap = {
    'excel': 'Excel',
    'pdf': 'PDF',
    'csv': 'CSV'
  }
  
  ElMessage.success(`正在导出${exportTypeMap[command]}文件，请稍候...`)
  
  // 模拟导出完成
  setTimeout(() => {
    ElMessage.success(`导出${exportTypeMap[command]}文件成功`)
  }, 1500)
}

// 格式化类型
const formatType = (type) => {
  const typeMap = {
    'FINANCIAL': '财务类',
    'PERSONNEL': '人事类',
    'ADMINISTRATIVE': '行政类',
    'TECHNICAL': '技术类',
    'OTHER': '其他'
  }
  return typeMap[type] || type
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

// 格式化日期
const formatDate = (date) => {
  if (!date) return '未知'
  return new Date(date).toLocaleString()
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

// 格式化文件类型
const formatFileType = (type) => {
  const typeMap = {
    'pdf': 'PDF文档',
    'word': 'Word文档',
    'excel': 'Excel表格',
    'image': '图片',
    'other': '其他类型'
  }
  return typeMap[type] || type
}
</script>

<style scoped>
.archive-search-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.search-card {
  margin-bottom: 20px;
}

.search-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  gap: 10px;
}

.result-card {
  margin-bottom: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.expanded-row {
  padding: 20px;
}

.keyword-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.expanded-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 