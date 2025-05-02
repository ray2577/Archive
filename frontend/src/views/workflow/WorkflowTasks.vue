<template>
  <div class="workflow-tasks-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>待办任务</h2>
      <div class="header-actions">
        <el-button-group>
          <el-button type="primary" @click="refreshTasks">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
          <el-button type="success" @click="handleBatchApprove" :disabled="selectedTasks.length === 0">
            <el-icon><Check /></el-icon>批量通过
          </el-button>
        </el-button-group>
      </div>
    </div>
    
    <!-- 任务筛选区 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="任务名称">
          <el-input v-model="searchForm.name" placeholder="请输入任务名称" clearable />
        </el-form-item>
        <el-form-item label="流程类型">
          <el-select v-model="searchForm.type" placeholder="请选择流程类型" clearable>
            <el-option v-for="item in processTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-select v-model="searchForm.priority" placeholder="请选择紧急程度" clearable>
            <el-option v-for="item in priorityOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收时间">
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
    
    <!-- 任务选项卡 -->
    <el-tabs v-model="activeTab" class="task-tabs" @tab-click="handleTabChange">
      <el-tab-pane label="待处理" name="pending">
        <el-table
          v-loading="loading"
          :data="tasksList"
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="name" label="任务名称" min-width="180" show-overflow-tooltip>
            <template #default="scope">
              <div class="task-name-column">
                <el-button type="primary" link @click="handleViewTask(scope.row)">
                  {{ scope.row.name }}
                </el-button>
                <el-tag v-if="isNew(scope.row.receiveTime)" size="small" type="danger" effect="dark">新</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="processType" label="流程类型" width="120">
            <template #default="scope">
              {{ getProcessTypeName(scope.row.processType) }}
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="紧急程度" width="100">
            <template #default="scope">
              <el-tag :type="getPriorityType(scope.row.priority)">
                {{ getPriorityName(scope.row.priority) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="initiator" label="发起人" width="100" />
          <el-table-column prop="receiveTime" label="接收时间" width="160" sortable />
          <el-table-column prop="dueTime" label="截止时间" width="160">
            <template #default="scope">
              <div :class="isOverdue(scope.row.dueTime) ? 'overdue-time' : ''">
                {{ scope.row.dueTime }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button-group>
                <el-tooltip content="查看详情">
                  <el-button type="primary" link @click="handleViewTask(scope.row)">
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="审批通过">
                  <el-button type="success" link @click="handleApprove(scope.row)">
                    <el-icon><Check /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="审批拒绝">
                  <el-button type="danger" link @click="handleReject(scope.row)">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="转交他人">
                  <el-button type="info" link @click="handleTransfer(scope.row)">
                    <el-icon><Position /></el-icon>
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
      </el-tab-pane>
      
      <el-tab-pane label="已处理" name="processed">
        <el-table
          v-loading="loading"
          :data="processedList"
          border
        >
          <el-table-column prop="name" label="任务名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="processType" label="流程类型" width="120">
            <template #default="scope">
              {{ getProcessTypeName(scope.row.processType) }}
            </template>
          </el-table-column>
          <el-table-column prop="initiator" label="发起人" width="100" />
          <el-table-column prop="receiveTime" label="接收时间" width="160" />
          <el-table-column prop="handleTime" label="处理时间" width="160" />
          <el-table-column prop="handleResult" label="处理结果" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.handleResult === 'approved' ? 'success' : 'danger'">
                {{ scope.row.handleResult === 'approved' ? '通过' : '拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="处理意见" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button type="primary" link @click="handleViewProcessed(scope.row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="processedQueryParams.page"
            v-model:page-size="processedQueryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="processedTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleProcessedSizeChange"
            @current-change="handleProcessedPageChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 审批对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      :title="approveDialogTitle"
      width="500px"
    >
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="任务名称">
          <span>{{ currentTask.name }}</span>
        </el-form-item>
        <el-form-item label="流程类型">
          <span>{{ getProcessTypeName(currentTask.processType) }}</span>
        </el-form-item>
        <el-form-item label="发起人">
          <span>{{ currentTask.initiator }}</span>
        </el-form-item>
        <el-form-item label="审批意见" prop="comment">
          <el-input
            v-model="approveForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApproval">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 转交对话框 -->
    <el-dialog
      v-model="transferDialogVisible"
      title="转交任务"
      width="500px"
    >
      <el-form :model="transferForm" label-width="100px">
        <el-form-item label="任务名称">
          <span>{{ currentTask.name }}</span>
        </el-form-item>
        <el-form-item label="转交给" prop="userId">
          <el-select v-model="transferForm.userId" placeholder="请选择转交人" filterable>
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转交原因" prop="reason">
          <el-input
            v-model="transferForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入转交原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="transferDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTransfer">确定</el-button>
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
  Refresh, Check, Search, View, Close, Position
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()

// 流程类型选项
const processTypeOptions = [
  { value: '1', label: '档案借阅' },
  { value: '2', label: '档案归还' },
  { value: '3', label: '档案销毁' },
  { value: '4', label: '文档审批' }
]

// 优先级选项
const priorityOptions = [
  { value: '1', label: '低' },
  { value: '2', label: '中' },
  { value: '3', label: '高' },
  { value: '4', label: '紧急' }
]

// 用户列表（用于转交）
const userList = [
  { id: '1', name: '张三' },
  { id: '2', name: '李四' },
  { id: '3', name: '王五' },
  { id: '4', name: '赵六' }
]

// 搜索表单
const searchForm = reactive({
  name: '',
  type: '',
  priority: '',
  dateRange: []
})

// 当前激活的标签
const activeTab = ref('pending')

// 任务状态
const loading = ref(false)
const tasksList = ref([])
const total = ref(0)
const selectedTasks = ref([])

// 已处理任务列表
const processedList = ref([])
const processedTotal = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  sortBy: 'receiveTime',
  sortOrder: 'desc'
})

// 已处理查询参数
const processedQueryParams = reactive({
  page: 1,
  pageSize: 10,
  sortBy: 'handleTime',
  sortOrder: 'desc'
})

// 当前任务
const currentTask = ref({})

// 审批表单
const approveDialogVisible = ref(false)
const approveDialogTitle = ref('审批任务')
const approveForm = reactive({
  taskId: '',
  result: 'approved', // 'approved' 或 'rejected'
  comment: ''
})

// 转交表单
const transferDialogVisible = ref(false)
const transferForm = reactive({
  taskId: '',
  userId: '',
  reason: ''
})

// 获取任务列表
const fetchTasksList = async () => {
  loading.value = true
  try {
    // 模拟数据
    setTimeout(() => {
      tasksList.value = [
        {
          id: '1',
          name: '档案借阅申请 - DOC-2023-001',
          processType: '1',
          priority: '3',
          initiator: '张三',
          receiveTime: '2023-03-10 09:30:00',
          dueTime: '2023-03-12 09:30:00'
        },
        {
          id: '2',
          name: '技术文档审批 - PRJ-2023-005',
          processType: '4',
          priority: '4',
          initiator: '李四',
          receiveTime: dayjs().subtract(2, 'hour').format('YYYY-MM-DD HH:mm:ss'),
          dueTime: dayjs().add(1, 'day').format('YYYY-MM-DD HH:mm:ss')
        },
        {
          id: '3',
          name: '档案销毁申请 - DOC-2022-120',
          processType: '3',
          priority: '2',
          initiator: '王五',
          receiveTime: '2023-03-08 14:25:00',
          dueTime: '2023-03-15 14:25:00'
        }
      ]
      total.value = 3
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取任务列表失败')
    loading.value = false
  }
}

// 获取已处理任务列表
const fetchProcessedList = async () => {
  loading.value = true
  try {
    // 模拟数据
    setTimeout(() => {
      processedList.value = [
        {
          id: '10',
          name: '档案借阅申请 - DOC-2023-010',
          processType: '1',
          initiator: '张三',
          receiveTime: '2023-03-05 10:30:00',
          handleTime: '2023-03-05 15:45:00',
          handleResult: 'approved',
          comment: '同意借阅，注意按时归还'
        },
        {
          id: '11',
          name: '技术文档审批 - PRJ-2023-003',
          processType: '4',
          initiator: '李四',
          receiveTime: '2023-03-03 14:20:00',
          handleTime: '2023-03-04 09:15:00',
          handleResult: 'rejected',
          comment: '文档内容有误，需要修改后重新提交'
        }
      ]
      processedTotal.value = 2
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取已处理任务列表失败')
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  if (activeTab.value === 'pending') {
    fetchTasksList()
  } else {
    fetchProcessedList()
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.type = ''
  searchForm.priority = ''
  searchForm.dateRange = []
  handleSearch()
}

// 刷新任务
const refreshTasks = () => {
  if (activeTab.value === 'pending') {
    fetchTasksList()
  } else {
    fetchProcessedList()
  }
  ElMessage.success('刷新成功')
}

// 页面大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  fetchTasksList()
}

// 页码变化
const handlePageChange = (page) => {
  queryParams.page = page
  fetchTasksList()
}

// 已处理页面大小变化
const handleProcessedSizeChange = (size) => {
  processedQueryParams.pageSize = size
  fetchProcessedList()
}

// 已处理页码变化
const handleProcessedPageChange = (page) => {
  processedQueryParams.page = page
  fetchProcessedList()
}

// 标签页变化
const handleTabChange = () => {
  if (activeTab.value === 'pending') {
    fetchTasksList()
  } else {
    fetchProcessedList()
  }
}

// 选中行变化
const handleSelectionChange = (rows) => {
  selectedTasks.value = rows
}

// 获取流程类型名称
const getProcessTypeName = (type) => {
  const found = processTypeOptions.find(item => item.value === type)
  return found ? found.label : '未知类型'
}

// 获取优先级名称
const getPriorityName = (priority) => {
  const found = priorityOptions.find(item => item.value === priority)
  return found ? found.label : '未知'
}

// 获取优先级类型
const getPriorityType = (priority) => {
  const typeMap = {
    '1': 'info',
    '2': 'success',
    '3': 'warning',
    '4': 'danger'
  }
  return typeMap[priority] || ''
}

// 判断是否为新任务（24小时内）
const isNew = (receiveTime) => {
  if (!receiveTime) return false
  const now = dayjs()
  const receiveDate = dayjs(receiveTime)
  return now.diff(receiveDate, 'hour') < 24
}

// 判断是否已逾期
const isOverdue = (dueTime) => {
  if (!dueTime) return false
  const now = dayjs()
  const dueDate = dayjs(dueTime)
  return now.isAfter(dueDate)
}

// 查看任务详情
const handleViewTask = (row) => {
  router.push(`/workflow/detail/${row.id}`)
}

// 查看已处理任务详情
const handleViewProcessed = (row) => {
  router.push(`/workflow/detail/${row.id}?type=processed`)
}

// 审批通过
const handleApprove = (row) => {
  currentTask.value = row
  approveDialogTitle.value = '审批通过'
  approveForm.taskId = row.id
  approveForm.result = 'approved'
  approveForm.comment = ''
  approveDialogVisible.value = true
}

// 审批拒绝
const handleReject = (row) => {
  currentTask.value = row
  approveDialogTitle.value = '审批拒绝'
  approveForm.taskId = row.id
  approveForm.result = 'rejected'
  approveForm.comment = ''
  approveDialogVisible.value = true
}

// 批量审批通过
const handleBatchApprove = () => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning('请选择要审批的任务')
    return
  }
  
  ElMessageBox.confirm(
    `确定要批量通过所选择的 ${selectedTasks.value.length} 个任务吗？`,
    '批量审批',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    // 模拟批量审批
    const taskIds = selectedTasks.value.map(task => task.id)
    tasksList.value = tasksList.value.filter(task => !taskIds.includes(task.id))
    total.value -= selectedTasks.value.length
    selectedTasks.value = []
    
    ElMessage.success('批量审批成功')
  }).catch(() => {})
}

// 提交审批
const submitApproval = async () => {
  try {
    // 模拟审批成功
    const taskIndex = tasksList.value.findIndex(task => task.id === approveForm.taskId)
    if (taskIndex !== -1) {
      tasksList.value.splice(taskIndex, 1)
      total.value--
    }
    
    ElMessage.success(`审批${approveForm.result === 'approved' ? '通过' : '拒绝'}成功`)
    approveDialogVisible.value = false
  } catch (error) {
    ElMessage.error('审批提交失败')
  }
}

// 转交任务
const handleTransfer = (row) => {
  currentTask.value = row
  transferForm.taskId = row.id
  transferForm.userId = ''
  transferForm.reason = ''
  transferDialogVisible.value = true
}

// 提交转交
const submitTransfer = async () => {
  if (!transferForm.userId) {
    ElMessage.warning('请选择转交人')
    return
  }
  
  try {
    // 模拟转交成功
    const taskIndex = tasksList.value.findIndex(task => task.id === transferForm.taskId)
    if (taskIndex !== -1) {
      tasksList.value.splice(taskIndex, 1)
      total.value--
    }
    
    ElMessage.success('转交成功')
    transferDialogVisible.value = false
  } catch (error) {
    ElMessage.error('转交提交失败')
  }
}

// 生命周期钩子
onMounted(() => {
  fetchTasksList()
})
</script>

<style scoped>
.workflow-tasks-container {
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

.task-tabs {
  margin-bottom: 20px;
}

.task-name-column {
  display: flex;
  align-items: center;
}

.task-name-column .el-tag {
  margin-left: 5px;
}

.overdue-time {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  text-align: right;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 