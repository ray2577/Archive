<template>
  <div class="user-management-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddUser">
          <el-icon><Plus /></el-icon> 添加用户
        </el-button>
        <el-button type="success" @click="handleImportUsers">
          <el-icon><Upload /></el-icon> 批量导入
        </el-button>
        <el-button @click="handleExportUsers">
          <el-icon><Download /></el-icon> 导出用户
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card shadow="hover" class="filter-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="关键词" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="用户名/姓名/邮箱"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="queryParams.roleId" placeholder="请选择角色" clearable>
            <el-option
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="departmentId">
          <el-select v-model="queryParams.departmentId" placeholder="请选择部门" clearable>
            <el-option
              v-for="item in departmentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间" prop="dateRange">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
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

    <!-- 用户表格 -->
    <el-card shadow="hover" class="table-container">
      <el-table
        v-loading="loading"
        :data="userList"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="用户ID" prop="id" width="80" />
        <el-table-column label="用户名" prop="username" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="姓名" prop="name" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="30" :src="row.avatar || defaultAvatar" />
          </template>
        </el-table-column>
        <el-table-column label="角色" prop="roleName" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="部门" prop="departmentName" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="手机号" prop="phone" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="邮箱" prop="email" :show-overflow-tooltip="true" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="150" :show-overflow-tooltip="true">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="150" :show-overflow-tooltip="true">
          <template #default="{ row }">
            {{ formatDate(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEditUser(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handleAssignRole(row)"
              v-if="checkPermission(['admin', 'system:user:role'])"
            >
              <el-icon><SetUp /></el-icon> 分配角色
            </el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              <el-icon><SwitchButton /></el-icon> {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteUser(row)"
              v-if="checkPermission(['admin', 'system:user:delete'])"
            >
              <el-icon><Delete /></el-icon> 删除
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

    <!-- 用户表单对话框 -->
    <el-dialog
      :title="formTitle"
      v-model="userDialogVisible"
      width="600px"
      append-to-body
      destroy-on-close
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userForm.username" 
            placeholder="请输入用户名" 
            :disabled="userForm.id !== undefined"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!userForm.id">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!userForm.id">
          <el-input
            v-model="userForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="部门" prop="departmentId">
          <el-select v-model="userForm.departmentId" placeholder="请选择部门">
            <el-option
              v-for="item in departmentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userForm.roleId" placeholder="请选择角色">
            <el-option
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="userForm.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="userForm.remark"
            type="textarea"
            placeholder="请输入备注"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUserForm" :loading="submitting">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog
      title="分配角色"
      v-model="roleDialogVisible"
      width="500px"
      append-to-body
    >
      <el-form label-width="100px">
        <el-form-item label="用户名">
          <span>{{ currentUser.username }}</span>
        </el-form-item>
        <el-form-item label="姓名">
          <span>{{ currentUser.name }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="selectedRoles">
            <el-checkbox
              v-for="role in allRoles"
              :key="role.id"
              :label="role.id"
            >
              {{ role.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRoleAssignment" :loading="submitting">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog
      title="批量导入用户"
      v-model="importDialogVisible"
      width="500px"
      append-to-body
    >
      <el-upload
        class="upload-demo"
        drag
        action="/api/system/user/import"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :limit="1"
        :headers="uploadHeaders"
        accept=".xlsx, .xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传Excel文件(xlsx/xls), 文件大小不超过10MB
            <div class="download-template">
              <el-button type="text" @click="downloadTemplate">
                <el-icon><Download /></el-icon> 下载模板
              </el-button>
            </div>
          </div>
        </template>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Edit, Delete, Search, Refresh, Download, Upload, 
  SetUp, SwitchButton, UploadFilled 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

// 默认头像
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCIgd2lkdGg9IjQ4IiBoZWlnaHQ9IjQ4IiBmaWxsPSIjMDA3NEQ5Ij48cGF0aCBkPSJNMjQgOGM0LjQgMCA4IDMuNiA4IDhzLTMuNiA4LTggOC04LTMuNi04LTggMy42LTggOC04em0wIDIwYzguOCAwIDE2IDQgMTYgOHYzSDb2LTNjMC00IDcuMi04IDE2LTh6Ii8+PC9zdmc+'

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const userList = ref([])
const total = ref(0)
const selectedUsers = ref([])
const userDialogVisible = ref(false)
const roleDialogVisible = ref(false)
const importDialogVisible = ref(false)
const userFormRef = ref(null)
const queryForm = ref(null)

// 当前操作的用户
const currentUser = ref({})
const selectedRoles = ref([])
const allRoles = ref([])

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  roleId: undefined,
  departmentId: undefined,
  status: undefined,
  dateRange: []
})

// 表单数据
const userForm = reactive({
  id: undefined,
  username: '',
  name: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  departmentId: undefined,
  roleId: undefined,
  position: '',
  status: 1,
  remark: ''
})

// 表单验证规则
const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== userForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 计算属性
const formTitle = computed(() => {
  return userForm.id ? '编辑用户' : '添加用户'
})

// 上传请求头
const uploadHeaders = computed(() => {
  const userStore = useUserStore()
  return {
    Authorization: userStore.token
  }
})

// 下拉选项
const roleOptions = ref([
  { value: 1, label: '管理员' },
  { value: 2, label: '部门经理' },
  { value: 3, label: '普通用户' }
])

const departmentOptions = ref([
  { value: 1, label: '总部' },
  { value: 2, label: '技术部' },
  { value: 3, label: '市场部' },
  { value: 4, label: '财务部' },
  { value: 5, label: '人力资源部' }
])

const statusOptions = ref([
  { value: 1, label: '正常' },
  { value: 0, label: '禁用' }
])

// 生命周期钩子
onMounted(() => {
  // 获取用户列表
  fetchUserList()
  // 获取所有角色列表（用于分配角色）
  fetchAllRoles()
})

// 方法定义
// 获取用户列表
const fetchUserList = () => {
  loading.value = true
  
  // 模拟API请求
  setTimeout(() => {
    // 模拟数据
    userList.value = [
      {
        id: 1,
        username: 'admin',
        name: '管理员',
        avatar: '',
        email: 'admin@example.com',
        phone: '13800138000',
        departmentId: 1,
        departmentName: '总部',
        roleId: 1,
        roleName: '管理员',
        position: '系统管理员',
        status: 1,
        remark: '系统内置管理员账号',
        createTime: '2023-01-01T00:00:00',
        lastLoginTime: '2023-08-15T08:30:00'
      },
      {
        id: 2,
        username: 'manager',
        name: '部门经理',
        avatar: '',
        email: 'manager@example.com',
        phone: '13800138001',
        departmentId: 2,
        departmentName: '技术部',
        roleId: 2,
        roleName: '部门经理',
        position: '技术经理',
        status: 1,
        remark: '',
        createTime: '2023-01-02T00:00:00',
        lastLoginTime: '2023-08-14T10:20:00'
      },
      {
        id: 3,
        username: 'user1',
        name: '张三',
        avatar: '',
        email: 'user1@example.com',
        phone: '13800138002',
        departmentId: 2,
        departmentName: '技术部',
        roleId: 3,
        roleName: '普通用户',
        position: '前端开发',
        status: 1,
        remark: '',
        createTime: '2023-02-15T00:00:00',
        lastLoginTime: '2023-08-13T14:30:00'
      },
      {
        id: 4,
        username: 'user2',
        name: '李四',
        avatar: '',
        email: 'user2@example.com',
        phone: '13800138003',
        departmentId: 3,
        departmentName: '市场部',
        roleId: 3,
        roleName: '普通用户',
        position: '市场专员',
        status: 0,
        remark: '已离职',
        createTime: '2023-03-01T00:00:00',
        lastLoginTime: '2023-07-20T16:40:00'
      },
      {
        id: 5,
        username: 'user3',
        name: '王五',
        avatar: '',
        email: 'user3@example.com',
        phone: '13800138004',
        departmentId: 4,
        departmentName: '财务部',
        roleId: 3,
        roleName: '普通用户',
        position: '财务主管',
        status: 1,
        remark: '',
        createTime: '2023-04-10T00:00:00',
        lastLoginTime: '2023-08-12T09:15:00'
      }
    ]
    total.value = 5
    loading.value = false
  }, 500)
}

// 获取所有角色
const fetchAllRoles = () => {
  // 模拟API请求
  allRoles.value = [
    { id: 1, name: '管理员', code: 'admin' },
    { id: 2, name: '部门经理', code: 'manager' },
    { id: 3, name: '普通用户', code: 'user' }
  ]
}

// 搜索查询
const handleQuery = () => {
  queryParams.pageNum = 1
  fetchUserList()
}

// 重置查询
const resetQuery = () => {
  queryForm.value.resetFields()
  queryParams.dateRange = []
  handleQuery()
}

// 表格多选
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 分页大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchUserList()
}

// 分页页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchUserList()
}

// 添加用户
const handleAddUser = () => {
  resetUserForm()
  userDialogVisible.value = true
}

// 编辑用户
const handleEditUser = (row) => {
  resetUserForm()
  // 复制数据到表单
  Object.assign(userForm, row)
  userDialogVisible.value = true
}

// 删除用户
const handleDeleteUser = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户"${row.name}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟API请求
    setTimeout(() => {
      ElMessage.success('删除成功')
      fetchUserList()
    }, 500)
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 切换用户状态
const handleToggleStatus = (row) => {
  const status = row.status === 1 ? 0 : 1
  const statusText = status === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(
    `确定要${statusText}用户"${row.name}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟API请求
    setTimeout(() => {
      row.status = status
      ElMessage.success(`已${statusText}`)
    }, 500)
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 分配角色
const handleAssignRole = (row) => {
  currentUser.value = row
  // 查询用户当前角色
  selectedRoles.value = [row.roleId]
  roleDialogVisible.value = true
}

// 提交角色分配
const submitRoleAssignment = () => {
  submitting.value = true
  
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('角色分配成功')
    submitting.value = false
    roleDialogVisible.value = false
    fetchUserList()
  }, 500)
}

// 重置用户表单
const resetUserForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  Object.assign(userForm, {
    id: undefined,
    username: '',
    name: '',
    password: '',
    confirmPassword: '',
    phone: '',
    email: '',
    departmentId: undefined,
    roleId: undefined,
    position: '',
    status: 1,
    remark: ''
  })
}

// 提交用户表单
const submitUserForm = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      // 模拟API请求
      setTimeout(() => {
        const message = userForm.id ? '修改成功' : '添加成功'
        ElMessage.success(message)
        userDialogVisible.value = false
        fetchUserList()
        submitting.value = false
      }, 1000)
    } catch (error) {
      console.error(error)
      ElMessage.error('操作失败，请稍后重试')
      submitting.value = false
    }
  })
}

// 批量导入用户
const handleImportUsers = () => {
  importDialogVisible.value = true
}

// 导入成功回调
const handleImportSuccess = (response) => {
  if (response.code === 0) {
    ElMessage.success(`成功导入${response.data.successCount}条数据`)
    importDialogVisible.value = false
    fetchUserList()
  } else {
    ElMessage.error(response.message || '导入失败')
  }
}

// 导入失败回调
const handleImportError = () => {
  ElMessage.error('导入失败，请检查文件格式或网络连接')
}

// 下载导入模板
const downloadTemplate = () => {
  // 模拟下载模板
  ElMessage.success('模板下载中...')
  // 这里应该有实际的下载逻辑
}

// 导出用户
const handleExportUsers = () => {
  // 模拟导出功能
  ElMessage.success('用户数据导出中...')
  // 这里应该有实际的导出逻辑
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 检查权限
const checkPermission = (permissions) => {
  // 这里应该检查当前用户是否有指定权限
  // 模拟权限检查
  return true
}
</script>

<style scoped>
.user-management-container {
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

.download-template {
  margin-top: 10px;
}

.el-upload__tip {
  line-height: 1.5;
}
</style> 