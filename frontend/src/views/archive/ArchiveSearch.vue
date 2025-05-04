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
        :rules="rules"
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
            <el-form-item label="标签">
              <el-select
                v-model="searchForm.tags"
                multiple
                collapse-tags
                collapse-tags-tooltip
                placeholder="请选择标签"
                style="width: 100%"
              >
                <el-option
                  v-for="item in tagOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
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
                    <div>
                      <el-button type="primary" link @click.stop="viewDetail(scope.row)">
                        <el-icon><View /></el-icon>
                      </el-button>
                    </div>
                  </el-tooltip>
                  <el-tooltip content="借阅">
                    <div>
                      <el-button type="success" link @click.stop="handleBorrow(scope.row)">
                        <el-icon><PriceTag /></el-icon>
                      </el-button>
                    </div>
                  </el-tooltip>
                  <el-tooltip content="下载">
                    <div>
                      <el-button type="warning" link @click.stop="handleDownload(scope.row)">
                        <el-icon><Download /></el-icon>
                      </el-button>
                    </div>
                  </el-tooltip>
                </el-button-group>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              @update:current-page="val => currentPage = val"
              @update:page-size="val => pageSize = val"
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
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Download, View, PriceTag, ArrowDown
} from '@element-plus/icons-vue'
import { 
  searchArchives, 
  downloadArchive, 
  exportArchives, 
  getArchiveCategories, 
  getArchiveTags,
  getArchiveList
} from '@/api/archive'

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
  endDate: '',
  tags: []
})

// 档案类型选项
const typeOptions = [
  { value: 'FINANCIAL', label: '财务类' },
  { value: 'PERSONNEL', label: '人事类' },
  { value: 'PROJECT', label: '项目类' },
  { value: 'CONTRACT', label: '合同类' },
  { value: 'OTHER', label: '其他' }
]

// 档案状态选项
const statusOptions = [
  { value: 'AVAILABLE', label: '可借阅' },
  { value: 'BORROWED', label: '已借出' },
  { value: 'PROCESSING', label: '处理中' }
]

// 自定义标签
const tagOptions = ref([])

// 获取标签选项
const fetchTags = async () => {
  try {
    const result = await getArchiveTags()
    if (result.code === 200) {
      tagOptions.value = result.data || []
    }
  } catch (error) {
    console.error('获取标签失败:', error)
  }
}

// 搜索表单验证规则
const rules = {
  keyword: [
    { min: 2, message: '关键词至少需要2个字符', trigger: 'blur' }
  ],
  content: [
    { min: 4, message: '搜索内容至少需要4个字符', trigger: 'blur' }
  ],
  startDate: [
    { 
      validator: (rule, value, callback) => {
        if (value && searchForm.endDate && new Date(value) > new Date(searchForm.endDate)) {
          callback(new Error('开始时间不能晚于结束时间'));
        } else {
          callback();
        }
      }, 
      trigger: 'change' 
    }
  ],
  endDate: [
    { 
      validator: (rule, value, callback) => {
        if (value && searchForm.startDate && new Date(value) < new Date(searchForm.startDate)) {
          callback(new Error('结束时间不能早于开始时间'));
        } else {
          callback();
        }
      }, 
      trigger: 'change' 
    }
  ]
}

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

// 处理搜索
const handleSearch = async () => {
  // 验证日期
  if (dateRange.value && dateRange.value.length === 2) {
    searchForm.startDate = dateRange.value[0]
    searchForm.endDate = dateRange.value[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
  
  // 重置结果
  searchResults.value = []
  currentPage.value = 1
  
  loading.value = true
  
  try {
    // 构建查询参数
    const params = {
      code: searchForm.code,
      name: searchForm.name,
      type: searchForm.type,
      status: searchForm.status,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate,
      keyword: searchForm.keyword,
      content: searchForm.content,
      tags: searchForm.tags.join(','),
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 清除空参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined || 
          (Array.isArray(params[key]) && params[key].length === 0)) {
        delete params[key]
      }
    })
    
    // 调用API - 使用基本列表API替代搜索API
    const result = await getArchiveList(params)
    
    if (result.code === 200) {
      // 处理不同格式的返回结果
      if (Array.isArray(result.data)) {
        searchResults.value = result.data
        total.value = result.total || result.data.length || 0
      } else if (result.data && result.data.records && Array.isArray(result.data.records)) {
        searchResults.value = result.data.records
        total.value = result.data.total || 0
      } else if (result.data && result.data.content && Array.isArray(result.data.content)) {
        searchResults.value = result.data.content
        total.value = result.data.totalElements || 0
      } else {
        searchResults.value = []
        total.value = 0
        ElMessage.warning('返回数据格式无法解析')
      }
      
      searched.value = true
      
      if (searchResults.value.length === 0) {
        ElMessage.info('未找到符合条件的档案')
      }
    } else {
      ElMessage.error(result.message || '搜索失败')
      searchResults.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('搜索出错:', error)
    ElMessage.error('搜索失败，请稍后重试: ' + error.message)
    searchResults.value = []
    total.value = 0
  } finally {
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
const handleDownload = async (row) => {
  try {
    ElMessage({
      message: `正在准备下载档案：${row.name}`,
      type: 'info',
      duration: 2000
    })
    
    const result = await downloadArchive(row.id)
    
    // 处理blob响应并下载
    const contentDisposition = result.headers['content-disposition']
    let filename = `${row.code}_${row.name}.pdf`
    
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename="?(.+)"?/)
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1]
      }
    }
    
    const blob = new Blob([result.data], { type: result.headers['content-type'] })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(`档案《${row.name}》下载完成`)
  } catch (error) {
    console.error('下载错误:', error)
    ElMessage.error(`下载失败: ${error.message}`)
  }
}

// 导出结果
const handleExport = async (command) => {
  if (searchResults.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  try {
    loading.value = true
    
    // 构建查询参数，与搜索相同
    const params = {
      code: searchForm.code,
      name: searchForm.name,
      type: searchForm.type,
      status: searchForm.status,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate,
      keyword: searchForm.keyword,
      content: searchForm.content,
      format: command // 指定导出格式 excel, pdf, csv
    }
    
    // 清除空参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined || 
          (Array.isArray(params[key]) && params[key].length === 0)) {
        delete params[key]
      }
    })
    
    ElMessage.info(`正在导出${command.toUpperCase()}文件，请稍候...`)
    
    const result = await exportArchives(params)
    
    // 处理blob响应并下载
    const contentType = command === 'excel' ? 'application/vnd.ms-excel' : 
                        command === 'pdf' ? 'application/pdf' : 
                        'text/csv'
    
    const extension = command === 'excel' ? 'xlsx' : 
                     command === 'pdf' ? 'pdf' : 
                     'csv'
                     
    const blob = new Blob([result], { type: contentType })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `档案搜索结果_${new Date().toISOString().split('T')[0]}.${extension}`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(`导出${command.toUpperCase()}文件成功`)
  } catch (error) {
    console.error('导出错误:', error)
    ElMessage.error(`导出失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 格式化类型
const formatType = (type) => {
  const typeMap = {
    'FINANCIAL': '财务类',
    'PERSONNEL': '人事类',
    'PROJECT': '项目类',
    'CONTRACT': '合同类',
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

// 组件初始化
onMounted(() => {
  // 加载标签选项
  fetchTags()
})
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