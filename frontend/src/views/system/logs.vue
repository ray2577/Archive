<template>
  <div class="log-management-container">
    <div class="page-header">
      <h2>日志管理</h2>
      <div class="header-actions">
        <el-button type="danger" @click="handleClearLogs" v-if="checkPermission(['admin', 'system:log:clear'])">
          <el-icon><Delete /></el-icon> 清空日志
        </el-button>
        <el-button type="primary" @click="handleExportLogs">
          <el-icon><Download /></el-icon> 导出日志
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card shadow="hover" class="filter-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="操作人员" prop="username">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入操作人员"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="操作类型" prop="operationType">
          <el-select v-model="queryParams.operationType" placeholder="请选择操作类型" clearable>
            <el-option
              v-for="item in operationTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择操作状态" clearable>
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块" prop="module">
          <el-input
            v-model="queryParams.module"
            placeholder="请输入操作模块"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="操作时间" prop="dateRange">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            :shortcuts="dateRangeShortcuts"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志表格 -->
    <el-card shadow="hover" class="table-container">
      <el-table
        v-loading="loading"
        :data="logList"
        border
        stripe
        row-key="id"
      >
        <el-table-column label="序号" type="index" width="50" align="center" />
        <el-table-column label="操作模块" prop="module" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)">
              {{ formatOperationType(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作描述" prop="description" :show-overflow-tooltip="true" />
        <el-table-column label="请求方法" prop="method" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="操作人员" prop="username" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="操作地址" prop="ip" width="130" :show-overflow-tooltip="true" />
        <el-table-column label="操作地点" prop="location" width="150" :show-overflow-tooltip="true" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.operationTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon> 详细
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      title="日志详情"
      v-model="detailDialogVisible"
      width="700px"
      append-to-body
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTag(currentLog.operationType)">
            {{ formatOperationType(currentLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog.status === 0 ? 'success' : 'danger'">
            {{ currentLog.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作地点">{{ currentLog.location }}</el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ currentLog.url }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ formatDate(currentLog.operationTime) }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="center">请求参数</el-divider>
      <el-input
        type="textarea"
        :rows="5"
        
        readonly
      />

      <el-divider content-position="center" v-if="currentLog.status === 1">异常信息</el-divider>
      <el-input
        v-if="currentLog.status === 1"
        type="textarea"
        :rows="5"
        v-model="currentLog.errorMessage"
        readonly
        class="error-message"
      />

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Delete, Download, View 
} from '@element-plus/icons-vue'

// 状态变量
const loading = ref(false)
const logList = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const queryForm = ref(null)
const currentLog = ref({})

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  operationType: undefined,
  status: undefined,
  module: '',
  dateRange: []
})

// 下拉选项
const operationTypeOptions = ref([
  { value: 0, label: '其他' },
  { value: 1, label: '新增' },
  { value: 2, label: '修改' },
  { value: 3, label: '删除' },
  { value: 4, label: '授权' },
  { value: 5, label: '导出' },
  { value: 6, label: '导入' },
  { value: 7, label: '强退' },
  { value: 8, label: '生成代码' },
  { value: 9, label: '清空数据' },
  { value: 10, label: '登录' },
  { value: 11, label: '登出' }
])

const statusOptions = ref([
  { value: 0, label: '成功' },
  { value: 1, label: '失败' }
])

// 日期范围快捷选项
const dateRangeShortcuts = [
  {
    text: '最近一小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000)
      return [start, end]
    }
  },
  {
    text: '今天',
    value: () => {
      const end = new Date()
      const start = new Date(new Date().toDateString())
      return [start, end]
    }
  },
  {
    text: '最近一天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24)
      return [start, end]
    }
  },
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    }
  }
]

// 生命周期钩子
onMounted(() => {
  // 获取日志列表
  fetchLogList()
})

// 方法定义
// 获取日志列表
const fetchLogList = () => {
  loading.value = true
  
  // 模拟API请求
  setTimeout(() => {
    // 模拟数据
    logList.value = [
      {
        id: 1,
        module: '用户管理',
        operationType: 1,
        description: '新增用户: admin001',
        method: 'com.example.controller.UserController.add()',
        url: '/system/user',
        username: 'admin',
        ip: '192.168.1.1',
        location: '内网IP',
        status: 0,
        operationTime: '2023-08-15T10:30:45',
        requestParams: JSON.stringify({
          username: 'admin001',
          name: '测试用户',
          email: 'test@example.com',
          roleId: 3
        }),
        errorMessage: ''
      },
      {
        id: 2,
        module: '角色管理',
        operationType: 2,
        description: '修改角色: 普通管理员',
        method: 'com.example.controller.RoleController.update()',
        url: '/system/role',
        username: 'admin',
        ip: '192.168.1.1',
        location: '内网IP',
        status: 0,
        operationTime: '2023-08-15T11:20:15',
        requestParams: JSON.stringify({
          id: 2,
          name: '普通管理员',
          code: 'manager',
          status: 1,
          remark: '普通管理员拥有除用户管理外的所有权限'
        }),
        errorMessage: ''
      },
      {
        id: 3,
        module: '登录日志',
        operationType: 10,
        description: '用户登录',
        method: 'com.example.controller.AuthController.login()',
        url: '/auth/login',
        username: 'manager',
        ip: '10.0.0.1',
        location: '内网IP',
        status: 0,
        operationTime: '2023-08-15T09:05:30',
        requestParams: JSON.stringify({
          username: 'manager',
          password: '******'
        }),
        errorMessage: ''
      },
      {
        id: 4,
        module: '档案管理',
        operationType: 3,
        description: '删除档案: AR2023-0568',
        method: 'com.example.controller.ArchiveController.delete()',
        url: '/archive/delete',
        username: 'admin',
        ip: '192.168.1.1',
        location: '内网IP',
        status: 1,
        operationTime: '2023-08-15T14:45:22',
        requestParams: JSON.stringify({
          id: 345,
          fileNo: 'AR2023-0568'
        }),
        errorMessage: '档案正在借阅中，不能删除'
      },
      {
        id: 5,
        module: '菜单管理',
        operationType: 2,
        description: '修改菜单: 系统设置',
        method: 'com.example.controller.MenuController.update()',
        url: '/system/menu',
        username: 'admin',
        ip: '192.168.1.1',
        location: '内网IP',
        status: 0,
        operationTime: '2023-08-15T16:10:05',
        requestParams: JSON.stringify({
          id: 10,
          name: '系统设置',
          path: '/system',
          icon: 'setting',
          sort: 9
        }),
        errorMessage: ''
      },
      {
        id: 6,
        module: '借阅管理',
        operationType: 1,
        description: '新增借阅申请: BR2023-0125',
        method: 'com.example.controller.BorrowController.add()',
        url: '/borrow/add',
        username: 'user1',
        ip: '192.168.1.100',
        location: '内网IP',
        status: 0,
        operationTime: '2023-08-15T13:30:40',
        requestParams: JSON.stringify({
          archiveId: 124,
          borrowDays: 7,
          reason: '项目研究需要'
        }),
        errorMessage: ''
      },
      {
        id: 7,
        module: '登录日志',
        operationType: 10,
        description: '用户登录',
        method: 'com.example.controller.AuthController.login()',
        url: '/auth/login',
        username: 'user2',
        ip: '203.0.113.1',
        location: '北京市 联通',
        status: 1,
        operationTime: '2023-08-15T08:25:15',
        requestParams: JSON.stringify({
          username: 'user2',
          password: '******'
        }),
        errorMessage: '密码错误'
      },
      {
        id: 8,
        module: '系统管理',
        operationType: 9,
        description: '清空操作日志',
        method: 'com.example.controller.LogController.clean()',
        url: '/system/log/clean',
        username: 'admin',
        ip: '192.168.1.1',
        location: '内网IP',
        status: 0,
        operationTime: '2023-08-14T17:50:30',
        requestParams: '{}',
        errorMessage: ''
      }
    ]
    total.value = logList.value.length
    loading.value = false
  }, 500)
}

// 搜索查询
const handleQuery = () => {
  queryParams.pageNum = 1
  fetchLogList()
}

// 重置查询
const resetQuery = () => {
  queryForm.value.resetFields()
  queryParams.dateRange = []
  handleQuery()
}

// 分页大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchLogList()
}

// 分页页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchLogList()
}

// 查看日志详情
const handleViewDetail = (row) => {
  currentLog.value = { ...row }
  detailDialogVisible.value = true
}

// 清空日志
const handleClearLogs = () => {
  ElMessageBox.confirm(
    '是否确认清空所有操作日志？此操作不可恢复',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟API请求
    setTimeout(() => {
      ElMessage.success('清空成功')
      fetchLogList()
    }, 500)
  }).catch(() => {
    ElMessage.info('已取消清空操作')
  })
}

// 导出日志
const handleExportLogs = () => {
  // 模拟导出
  ElMessage.success('日志导出中...')
  // 实际应该调用API进行导出操作
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 格式化操作类型
const formatOperationType = (type) => {
  const option = operationTypeOptions.value.find(option => option.value === type)
  return option ? option.label : '未知'
}

// 获取操作类型对应的标签类型
const getOperationTypeTag = (type) => {
  const tagMap = {
    0: 'info',    // 其他
    1: 'success', // 新增
    2: 'warning', // 修改
    3: 'danger',  // 删除
    4: 'warning', // 授权
    5: 'info',    // 导出
    6: 'info',    // 导入
    7: 'danger',  // 强退
    8: 'success', // 生成代码
    9: 'danger',  // 清空数据
    10: 'primary', // 登录
    11: 'info'     // 登出
  }
  return tagMap[type] || 'info'
}

// 格式化JSON显示
const formatJson = (jsonString) => {
  if (!jsonString) return ''
  
  try {
    const jsonObj = JSON.parse(jsonString)
    return JSON.stringify(jsonObj, null, 4)
  } catch (e) {
    return jsonString
  }
}

// 检查权限
const checkPermission = (permissions) => {
  // 这里应该检查当前用户是否有指定权限
  // 模拟权限检查
  return true
}
</script>

<style scoped>
.log-management-container {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

.error-message {
  color: #F56C6C;
}
</style> 