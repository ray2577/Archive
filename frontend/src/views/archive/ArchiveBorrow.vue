<template>
  <div class="archive-borrow-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>借阅管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreateBorrow">
          <el-icon><Plus /></el-icon>新增借阅
        </el-button>
        <el-button type="success" @click="handleExport">
          <el-icon><Download /></el-icon>导出记录
        </el-button>
      </div>
    </div>
    
    <!-- 筛选区域 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="档案名称">
          <el-input 
            v-model="filterForm.title" 
            placeholder="请输入档案名称" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="借阅状态">
          <el-select 
            v-model="filterForm.status" 
            placeholder="全部状态" 
            clearable
          >
            <el-option 
              v-for="item in borrowStatusOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="借阅人">
          <el-input 
            v-model="filterForm.borrower" 
            placeholder="请输入借阅人" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="借阅时间">
          <el-date-picker
            v-model="filterForm.dateRange"
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
          <el-button @click="resetFilter">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 借阅记录表格 -->
    <el-card class="borrow-table-container">
      <div class="table-header">
        <div class="tabs">
          <el-radio-group v-model="activeTab" @change="handleTabChange">
            <el-radio-button label="all">全部记录</el-radio-button>
            <el-radio-button label="borrowed">借阅中</el-radio-button>
            <el-radio-button label="returned">已归还</el-radio-button>
            <el-radio-button label="overdue">已逾期</el-radio-button>
          </el-radio-group>
        </div>
        <div class="actions">
          <el-tooltip content="刷新">
            <el-button @click="fetchData">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="borrowList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="expand">
          <template #default="props">
            <div class="expanded-row">
              <el-descriptions :column="3" border>
                <el-descriptions-item label="档案编号">{{ props.row.fileNumber }}</el-descriptions-item>
                <el-descriptions-item label="借阅编号">{{ props.row.borrowNumber }}</el-descriptions-item>
                <el-descriptions-item label="应还日期">{{ formatDate(props.row.expectedReturnDate) }}</el-descriptions-item>
                <el-descriptions-item label="借阅原因" :span="3">{{ props.row.reason }}</el-descriptions-item>
                <el-descriptions-item label="审批人">{{ props.row.approver }}</el-descriptions-item>
                <el-descriptions-item label="审批时间">{{ formatDate(props.row.approveTime) }}</el-descriptions-item>
                <el-descriptions-item label="审批备注">{{ props.row.approveRemark || '无' }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ formatDate(props.row.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ formatDate(props.row.updateTime) }}</el-descriptions-item>
                <el-descriptions-item label="备注">{{ props.row.remark || '无' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="档案名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="borrower" label="借阅人" width="120" />
        <el-table-column prop="borrowDate" label="借阅日期" width="180" :formatter="formatTableDate" sortable />
        <el-table-column prop="returnDate" label="归还日期" width="180" :formatter="formatTableDate" sortable />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getBorrowStatusType(scope.row.status)">
              {{ formatBorrowStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-tooltip content="查看档案">
                <el-button type="primary" link @click="viewArchiveDetail(scope.row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑">
                <el-button type="primary" link @click="handleEdit(scope.row)" :disabled="scope.row.status !== 'BORROWED'">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="归还">
                <el-button type="success" link @click="handleReturn(scope.row)" :disabled="scope.row.status !== 'BORROWED' && scope.row.status !== 'OVERDUE'">
                  <el-icon><Select /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="延期">
                <el-button type="warning" link @click="handleExtend(scope.row)" :disabled="scope.row.status !== 'BORROWED'">
                  <el-icon><Timer /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除">
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
    
    <!-- 借阅对话框 -->
    <el-dialog
      v-model="borrowDialogVisible"
      :title="isEdit ? '编辑借阅信息' : '新增借阅'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="borrowFormRef"
        :model="borrowForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="档案" prop="archiveId" v-if="!isEdit">
          <el-select
            v-model="borrowForm.archiveId"
            placeholder="请选择档案"
            filterable
            remote
            :remote-method="searchArchives"
            :loading="archiveSearching"
            style="width: 100%"
            @change="handleArchiveChange"
          >
            <el-option
              v-for="item in archiveOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            >
              <div class="archive-option">
                <span>{{ item.title }}</span>
                <span class="archive-number">{{ item.fileNumber }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="档案名称" v-else>
          <el-input v-model="borrowForm.title" disabled />
        </el-form-item>
        <el-form-item label="借阅人" prop="borrower">
          <el-input v-model="borrowForm.borrower" placeholder="请输入借阅人姓名" />
        </el-form-item>
        <el-form-item label="借阅日期" prop="borrowDate">
          <el-date-picker
            v-model="borrowForm.borrowDate"
            type="datetime"
            placeholder="请选择借阅日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预计归还" prop="expectedReturnDate">
          <el-date-picker
            v-model="borrowForm.expectedReturnDate"
            type="date"
            placeholder="请选择预计归还日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="借阅原因" prop="reason">
          <el-input
            v-model="borrowForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入借阅原因"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="isEdit">
          <el-select v-model="borrowForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option
              v-for="item in borrowStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="borrowForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="borrowDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBorrowForm" :loading="submitting">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 归还对话框 -->
    <el-dialog
      v-model="returnDialogVisible"
      title="归还档案"
      width="500px"
    >
      <el-form
        ref="returnFormRef"
        :model="returnForm"
        label-width="100px"
      >
        <el-form-item label="档案">
          <span>{{ currentBorrow.title }}</span>
        </el-form-item>
        <el-form-item label="借阅人">
          <span>{{ currentBorrow.borrower }}</span>
        </el-form-item>
        <el-form-item label="借阅日期">
          <span>{{ formatDate(currentBorrow.borrowDate) }}</span>
        </el-form-item>
        <el-form-item label="应还日期">
          <span>{{ formatDate(currentBorrow.expectedReturnDate) }}</span>
        </el-form-item>
        <el-form-item label="归还日期" prop="returnDate">
          <el-date-picker
            v-model="returnForm.returnDate"
            type="datetime"
            placeholder="请选择归还日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="档案状态" prop="archiveStatus">
          <el-select v-model="returnForm.archiveStatus" style="width: 100%">
            <el-option label="完好" value="GOOD" />
            <el-option label="轻微损坏" value="SLIGHT_DAMAGE" />
            <el-option label="严重损坏" value="SEVERE_DAMAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="returnForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入归还备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="returnDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReturn" :loading="submitting">
            确认归还
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 延期对话框 -->
    <el-dialog
      v-model="extendDialogVisible"
      title="延长借阅期限"
      width="500px"
    >
      <el-form
        ref="extendFormRef"
        :model="extendForm"
        label-width="100px"
      >
        <el-form-item label="档案">
          <span>{{ currentBorrow.title }}</span>
        </el-form-item>
        <el-form-item label="借阅人">
          <span>{{ currentBorrow.borrower }}</span>
        </el-form-item>
        <el-form-item label="当前到期">
          <span>{{ formatDate(currentBorrow.expectedReturnDate) }}</span>
        </el-form-item>
        <el-form-item label="新到期日" prop="newDate">
          <el-date-picker
            v-model="extendForm.newDate"
            type="date"
            placeholder="请选择新的到期日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="(time) => time.getTime() < new Date(currentBorrow.expectedReturnDate).getTime()"
          />
        </el-form-item>
        <el-form-item label="延期原因" prop="reason">
          <el-input
            v-model="extendForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入延期原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="extendDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitExtend" :loading="submitting">
            确认延期
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Download, Search, RefreshRight, Refresh,
  View, Edit, Select, Timer, Delete
} from '@element-plus/icons-vue'

const router = useRouter()

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const borrowList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const activeTab = ref('all')
const selectedRows = ref([])
const archiveSearching = ref(false)
const archiveOptions = ref([])

// 弹窗控制
const borrowDialogVisible = ref(false)
const returnDialogVisible = ref(false)
const extendDialogVisible = ref(false)
const isEdit = ref(false)
const currentBorrow = ref({})

// 表单引用
const borrowFormRef = ref(null)
const returnFormRef = ref(null)
const extendFormRef = ref(null)

// 借阅状态选项
const borrowStatusOptions = [
  { label: '借阅中', value: 'BORROWED' },
  { label: '已归还', value: 'RETURNED' },
  { label: '已逾期', value: 'OVERDUE' },
  { label: '待审批', value: 'PENDING' },
  { label: '已拒绝', value: 'REJECTED' }
]

// 筛选表单
const filterForm = reactive({
  title: '',
  status: '',
  borrower: '',
  dateRange: []
})

// 借阅表单
const borrowForm = reactive({
  id: '',
  archiveId: '',
  fileNumber: '',
  title: '',
  borrower: '',
  borrowDate: '',
  expectedReturnDate: '',
  reason: '',
  status: 'BORROWED',
  remark: ''
})

// 归还表单
const returnForm = reactive({
  id: '',
  returnDate: new Date().toISOString().slice(0, 19).replace('T', ' '),
  archiveStatus: 'GOOD',
  remark: ''
})

// 延期表单
const extendForm = reactive({
  id: '',
  newDate: '',
  reason: ''
})

// 借阅表单验证规则
const rules = {
  archiveId: [
    { required: true, message: '请选择档案', trigger: 'change' }
  ],
  borrower: [
    { required: true, message: '请输入借阅人姓名', trigger: 'blur' }
  ],
  borrowDate: [
    { required: true, message: '请选择借阅日期', trigger: 'change' }
  ],
  expectedReturnDate: [
    { required: true, message: '请选择预计归还日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入借阅原因', trigger: 'blur' }
  ]
}

// 获取借阅记录列表
const fetchData = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      title: filterForm.title,
      status: filterForm.status,
      borrower: filterForm.borrower,
      startDate: filterForm.dateRange && filterForm.dateRange[0],
      endDate: filterForm.dateRange && filterForm.dateRange[1],
      tab: activeTab.value
    }
    
    // 模拟API请求
    setTimeout(() => {
      // 模拟数据
      const mockData = Array.from({ length: pageSize.value }, (_, i) => ({
        id: (currentPage.value - 1) * pageSize.value + i + 1,
        borrowNumber: `BR${new Date().getFullYear()}${String(i + 1).padStart(6, '0')}`,
        archiveId: i + 1,
        fileNumber: `AR${new Date().getFullYear()}${String(i + 1).padStart(4, '0')}`,
        title: ['财务报表', '人事档案', '会议纪要', '合同文本', '技术文档'][Math.floor(Math.random() * 5)] + ` ${i + 1}`,
        borrower: ['张三', '李四', '王五', '赵六'][Math.floor(Math.random() * 4)],
        borrowDate: new Date(new Date().getTime() - Math.floor(Math.random() * 1000 * 3600 * 24 * 30)).toISOString(),
        returnDate: Math.random() > 0.5 ? new Date().toISOString() : null,
        expectedReturnDate: new Date(new Date().getTime() + Math.floor(Math.random() * 1000 * 3600 * 24 * 30)).toISOString(),
        status: ['BORROWED', 'RETURNED', 'OVERDUE'][Math.floor(Math.random() * 3)],
        reason: '需要参考该档案进行工作',
        approver: '管理员',
        approveTime: new Date(new Date().getTime() - Math.floor(Math.random() * 1000 * 3600 * 24 * 30)).toISOString(),
        approveRemark: '批准借阅',
        createTime: new Date(new Date().getTime() - Math.floor(Math.random() * 1000 * 3600 * 24 * 30)).toISOString(),
        updateTime: new Date().toISOString(),
        remark: Math.random() > 0.7 ? '借阅备注信息' : ''
      }))
      
      // 根据tab过滤数据
      const filteredData = mockData.filter(item => {
        if (activeTab.value === 'all') return true
        if (activeTab.value === 'borrowed') return item.status === 'BORROWED'
        if (activeTab.value === 'returned') return item.status === 'RETURNED'
        if (activeTab.value === 'overdue') return item.status === 'OVERDUE'
        return true
      })
      
      borrowList.value = filteredData
      total.value = 186
      loading.value = false
    }, 500)
  } catch (error) {
    console.error('Error fetching borrow records:', error)
    ElMessage.error('获取借阅记录失败，请稍后重试')
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

// 重置筛选
const resetFilter = () => {
  filterForm.title = ''
  filterForm.status = ''
  filterForm.borrower = ''
  filterForm.dateRange = []
  
  handleSearch()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchData()
}

// 标签切换
const handleTabChange = (tab) => {
  activeTab.value = tab
  fetchData()
}

// 选择行变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 搜索档案
const searchArchives = async (query) => {
  if (query === '') {
    archiveOptions.value = []
    return
  }
  
  archiveSearching.value = true
  
  // 模拟搜索
  setTimeout(() => {
    archiveOptions.value = Array.from({ length: 10 }, (_, i) => ({
      id: i + 1,
      fileNumber: `AR${new Date().getFullYear()}${String(i + 1).padStart(4, '0')}`,
      title: query + ['财务报表', '人事档案', '会议纪要', '合同文本', '技术文档'][Math.floor(Math.random() * 5)] + ` ${i + 1}`
    }))
    archiveSearching.value = false
  }, 500)
}

// 档案选择变化
const handleArchiveChange = (id) => {
  const selected = archiveOptions.value.find(item => item.id === id)
  if (selected) {
    borrowForm.fileNumber = selected.fileNumber
    borrowForm.title = selected.title
  }
}

// 创建借阅
const handleCreateBorrow = () => {
  isEdit.value = false
  borrowForm.id = ''
  borrowForm.archiveId = ''
  borrowForm.fileNumber = ''
  borrowForm.title = ''
  borrowForm.borrower = ''
  borrowForm.borrowDate = new Date().toISOString().slice(0, 19).replace('T', ' ')
  borrowForm.expectedReturnDate = new Date(new Date().getTime() + 7 * 24 * 3600 * 1000).toISOString().slice(0, 10)
  borrowForm.reason = ''
  borrowForm.status = 'BORROWED'
  borrowForm.remark = ''
  
  borrowDialogVisible.value = true
}

// 编辑借阅
const handleEdit = (row) => {
  isEdit.value = true
  currentBorrow.value = row
  
  borrowForm.id = row.id
  borrowForm.archiveId = row.archiveId
  borrowForm.fileNumber = row.fileNumber
  borrowForm.title = row.title
  borrowForm.borrower = row.borrower
  borrowForm.borrowDate = row.borrowDate.replace('T', ' ').slice(0, 19)
  borrowForm.expectedReturnDate = row.expectedReturnDate.slice(0, 10)
  borrowForm.reason = row.reason
  borrowForm.status = row.status
  borrowForm.remark = row.remark || ''
  
  borrowDialogVisible.value = true
}

// 提交借阅表单
const submitBorrowForm = async () => {
  if (!borrowFormRef.value) return
  
  await borrowFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 模拟API请求
      setTimeout(() => {
        ElMessage.success(isEdit.value ? '借阅信息更新成功' : '借阅创建成功')
        borrowDialogVisible.value = false
        fetchData()
        submitting.value = false
      }, 1000)
    } catch (error) {
      console.error('Error submitting borrow form:', error)
      ElMessage.error(isEdit.value ? '更新借阅信息失败' : '创建借阅失败')
      submitting.value = false
    }
  })
}

// 归还档案
const handleReturn = (row) => {
  currentBorrow.value = row
  returnForm.id = row.id
  returnForm.returnDate = new Date().toISOString().slice(0, 19).replace('T', ' ')
  returnForm.archiveStatus = 'GOOD'
  returnForm.remark = ''
  
  returnDialogVisible.value = true
}

// 提交归还
const submitReturn = async () => {
  submitting.value = true
  try {
    // 模拟API请求
    setTimeout(() => {
      ElMessage.success('档案归还成功')
      returnDialogVisible.value = false
      fetchData()
      submitting.value = false
    }, 1000)
  } catch (error) {
    console.error('Error returning archive:', error)
    ElMessage.error('档案归还失败')
    submitting.value = false
  }
}

// 延长借阅期限
const handleExtend = (row) => {
  currentBorrow.value = row
  extendForm.id = row.id
  extendForm.newDate = new Date(new Date(row.expectedReturnDate).getTime() + 7 * 24 * 3600 * 1000).toISOString().slice(0, 10)
  extendForm.reason = ''
  
  extendDialogVisible.value = true
}

// 提交延期
const submitExtend = async () => {
  submitting.value = true
  try {
    // 模拟API请求
    setTimeout(() => {
      ElMessage.success('借阅期限延长成功')
      extendDialogVisible.value = false
      fetchData()
      submitting.value = false
    }, 1000)
  } catch (error) {
    console.error('Error extending borrow period:', error)
    ElMessage.error('延长借阅期限失败')
    submitting.value = false
  }
}

// 删除借阅记录
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除此借阅记录吗？此操作不可恢复。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      // 模拟删除
      setTimeout(() => {
        ElMessage.success('删除成功')
        fetchData()
      }, 500)
    })
    .catch(() => {})
}

// 导出记录
const handleExport = () => {
  ElMessage.success('借阅记录导出中...')
  
  // 模拟导出
  setTimeout(() => {
    ElMessage.success('借阅记录导出成功')
  }, 1500)
}

// 查看档案详情
const viewArchiveDetail = (row) => {
  router.push(`/archive/detail/${row.archiveId}`)
}

// 格式化借阅状态
const formatBorrowStatus = (status) => {
  const statusMap = {
    'BORROWED': '借阅中',
    'RETURNED': '已归还',
    'OVERDUE': '已逾期',
    'PENDING': '待审批',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

// 获取借阅状态类型
const getBorrowStatusType = (status) => {
  const typeMap = {
    'BORROWED': 'primary',
    'RETURNED': 'success',
    'OVERDUE': 'danger',
    'PENDING': 'info',
    'REJECTED': 'warning'
  }
  return typeMap[status] || 'info'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '未设置'
  return new Date(date).toLocaleString()
}

// 格式化表格日期
const formatTableDate = (row, column, cellValue) => {
  return formatDate(cellValue)
}

// 组件挂载
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.archive-borrow-container {
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

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.borrow-table-container {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.expanded-row {
  padding: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.archive-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.archive-number {
  color: #909399;
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 