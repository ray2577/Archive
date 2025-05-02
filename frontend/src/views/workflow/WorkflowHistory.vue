<template>
  <div class="workflow-history-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>审批历史</h2>
    </div>
    
    <!-- 搜索筛选区 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程名称">
          <el-input v-model="searchForm.processName" placeholder="请输入流程名称" clearable />
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="searchForm.initiator" placeholder="请输入申请人" clearable />
        </el-form-item>
        <el-form-item label="流程类型">
          <el-select v-model="searchForm.processType" placeholder="请选择流程类型" clearable>
            <el-option v-for="item in processTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
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
        <el-form-item label="结果">
          <el-select v-model="searchForm.result" placeholder="请选择结果" clearable>
            <el-option label="通过" value="approved" />
            <el-option label="拒绝" value="rejected" />
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
    
    <!-- 历史记录表格 -->
    <el-card class="table-container">
      <template #header>
        <div class="table-header">
          <span>审批历史记录</span>
          <div class="header-actions">
            <el-button-group>
              <el-button type="primary" @click="refreshList">
                <el-icon><Refresh /></el-icon>刷新
              </el-button>
              <el-button type="success" @click="exportHistory">
                <el-icon><Download /></el-icon>导出
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="historyList"
        border
        style="width: 100%"
      >
        <el-table-column prop="processName" label="流程名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="businessKey" label="业务标识" width="120" show-overflow-tooltip />
        <el-table-column prop="processType" label="流程类型" width="120">
          <template #default="scope">
            {{ getProcessTypeName(scope.row.processType) }}
          </template>
        </el-table-column>
        <el-table-column prop="initiator" label="申请人" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="160" sortable />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="duration" label="耗时" width="120">
          <template #default="scope">
            {{ formatDuration(scope.row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.result === 'approved' ? 'success' : 'danger'">
              {{ scope.row.result === 'approved' ? '通过' : '拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">
              查看详情
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'

const router = useRouter()

// 流程类型选项
const processTypeOptions = [
  { value: '1', label: '档案借阅' },
  { value: '2', label: '档案归还' },
  { value: '3', label: '档案销毁' },
  { value: '4', label: '文档审批' }
]

// 搜索表单
const searchForm = reactive({
  processName: '',
  initiator: '',
  processType: '',
  dateRange: [],
  result: ''
})

// 表格数据
const loading = ref(false)
const historyList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  sortBy: 'startTime',
  sortOrder: 'desc'
})

// 获取历史记录列表
const fetchHistoryList = async () => {
  loading.value = true
  try {
    // 模拟数据
    setTimeout(() => {
      historyList.value = [
        {
          id: '1',
          processName: '档案借阅申请',
          businessKey: 'BORROW-20230310-001',
          processType: '1',
          initiator: '张三',
          startTime: '2023-03-10 09:30:00',
          endTime: '2023-03-10 14:15:00',
          duration: 17100000, // 毫秒
          result: 'approved'
        },
        {
          id: '2',
          processName: '技术文档审批',
          businessKey: 'DOC-20230305-002',
          processType: '4',
          initiator: '李四',
          startTime: '2023-03-05 10:20:00',
          endTime: '2023-03-06 11:30:00',
          duration: 90600000, // 毫秒
          result: 'approved'
        },
        {
          id: '3',
          processName: '档案销毁申请',
          businessKey: 'DESTROY-20230301-001',
          processType: '3',
          initiator: '王五',
          startTime: '2023-03-01 14:00:00',
          endTime: '2023-03-03 16:45:00',
          duration: 194700000, // 毫秒
          result: 'rejected'
        },
        {
          id: '4',
          processName: '档案归还流程',
          businessKey: 'RETURN-20230225-001',
          processType: '2',
          initiator: '张三',
          startTime: '2023-02-25 09:15:00',
          endTime: '2023-02-25 11:20:00',
          duration: 7500000, // 毫秒
          result: 'approved'
        }
      ]
      total.value = 4
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取审批历史记录失败')
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchHistoryList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.processName = ''
  searchForm.initiator = ''
  searchForm.processType = ''
  searchForm.dateRange = []
  searchForm.result = ''
  handleSearch()
}

// 刷新列表
const refreshList = () => {
  fetchHistoryList()
  ElMessage.success('刷新成功')
}

// 导出历史记录
const exportHistory = () => {
  ElMessage.success('正在导出审批历史记录，请稍候...')
  // 实际应用中应调用导出API
}

// 页面大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  fetchHistoryList()
}

// 页码变化
const handlePageChange = (page) => {
  queryParams.page = page
  fetchHistoryList()
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/workflow/history-detail/${row.id}`)
}

// 获取流程类型名称
const getProcessTypeName = (type) => {
  const found = processTypeOptions.find(item => item.value === type)
  return found ? found.label : '未知类型'
}

// 格式化耗时
const formatDuration = (duration) => {
  if (!duration) return '0秒'
  
  const seconds = Math.floor(duration / 1000)
  if (seconds < 60) {
    return `${seconds}秒`
  }
  
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) {
    return `${minutes}分钟${seconds % 60}秒`
  }
  
  const hours = Math.floor(minutes / 60)
  if (hours < 24) {
    return `${hours}小时${minutes % 60}分钟`
  }
  
  const days = Math.floor(hours / 24)
  return `${days}天${hours % 24}小时`
}

// 生命周期钩子
onMounted(() => {
  fetchHistoryList()
})
</script>

<style scoped>
.workflow-history-container {
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
</style> 