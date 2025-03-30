<template>
  <div class="role-management-container">
    <div class="page-header">
      <h2>角色管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddRole" v-if="checkPermission(['admin', 'system:role:add'])">
          <el-icon><Plus /></el-icon> 添加角色
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card shadow="hover" class="filter-container">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="角色名称" prop="roleName">
          <el-input
            v-model="queryParams.roleName"
            placeholder="请输入角色名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="queryParams.roleCode"
            placeholder="请输入角色编码"
            clearable
            @keyup.enter="handleQuery"
          />
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

    <!-- 角色表格 -->
    <el-card shadow="hover" class="table-container">
      <el-table
        v-loading="loading"
        :data="roleList"
        border
        stripe
        row-key="id"
      >
        <el-table-column label="角色ID" prop="id" width="80" />
        <el-table-column label="角色名称" prop="name" width="150" />
        <el-table-column label="角色编码" prop="code" width="150" />
        <el-table-column label="显示顺序" prop="sort" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" width="180" :show-overflow-tooltip="true">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="handleEditRole(row)"
              v-if="checkPermission(['admin', 'system:role:edit'])"
            >
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handlePermission(row)"
              v-if="checkPermission(['admin', 'system:role:permission'])"
            >
              <el-icon><SetUp /></el-icon> 分配权限
            </el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
              v-if="checkPermission(['admin', 'system:role:edit'])"
            >
              <el-icon><SwitchButton /></el-icon> {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteRole(row)"
              v-if="row.code !== 'admin' && checkPermission(['admin', 'system:role:delete'])"
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

    <!-- 添加或修改角色对话框 -->
    <el-dialog
      :title="formTitle"
      v-model="roleDialogVisible"
      width="550px"
      append-to-body
      destroy-on-close
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleFormRules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input 
            v-model="roleForm.code" 
            placeholder="请输入角色编码"
            :disabled="roleForm.id !== undefined" 
          />
        </el-form-item>
        <el-form-item label="显示顺序" prop="sort">
          <el-input-number v-model="roleForm.sort" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="roleForm.remark"
            type="textarea"
            placeholder="请输入内容"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRoleForm" :loading="submitting">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog
      title="分配权限"
      v-model="permissionDialogVisible"
      width="600px"
      append-to-body
    >
      <el-form label-width="100px">
        <el-form-item label="角色名称">
          <span>{{ currentRole.name }}</span>
        </el-form-item>
      </el-form>
      <el-divider content-position="center">功能权限</el-divider>
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'name', children: 'children' }"
        :default-checked-keys="selectedPermissions"
        :check-strictly="false"
        :highlight-current="true"
        empty-text="暂无权限数据"
      />
      <el-divider content-position="center">数据权限</el-divider>
      <el-form label-width="120px">
        <el-form-item label="数据权限范围">
          <el-select v-model="dataScope" placeholder="请选择数据权限范围">
            <el-option
              v-for="item in dataScopeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="自定义部门" v-if="dataScope === 2">
          <el-tree
            ref="deptTreeRef"
            :data="deptTree"
            show-checkbox
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
            :default-checked-keys="selectedDepts"
            :check-strictly="true"
            :highlight-current="true"
            empty-text="暂无部门数据"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPermission" :loading="submitting">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Edit, Delete, Search, Refresh, SwitchButton, SetUp 
} from '@element-plus/icons-vue'

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const roleList = ref([])
const total = ref(0)
const roleDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const roleFormRef = ref(null)
const queryForm = ref(null)
const permissionTreeRef = ref(null)
const deptTreeRef = ref(null)

// 当前选中的角色
const currentRole = ref({})

// 权限树和选中的权限
const permissionTree = ref([])
const selectedPermissions = ref([])

// 部门树和选中的部门
const deptTree = ref([])
const selectedDepts = ref([])

// 数据权限范围
const dataScope = ref(1)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleName: '',
  roleCode: '',
  status: undefined,
  dateRange: []
})

// 表单数据
const roleForm = reactive({
  id: undefined,
  name: '',
  code: '',
  sort: 0,
  status: 1,
  remark: ''
})

// 表单验证规则
const roleFormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '角色编码只能包含字母、数字和下划线，且必须以字母开头', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' }
  ]
}

// 计算属性
const formTitle = computed(() => {
  return roleForm.id ? '编辑角色' : '添加角色'
})

// 下拉选项
const statusOptions = ref([
  { value: 1, label: '正常' },
  { value: 0, label: '停用' }
])

const dataScopeOptions = ref([
  { value: 1, label: '全部数据权限' },
  { value: 2, label: '自定义数据权限' },
  { value: 3, label: '本部门数据权限' },
  { value: 4, label: '本部门及以下数据权限' },
  { value: 5, label: '仅本人数据权限' }
])

// 生命周期钩子
onMounted(() => {
  // 获取角色列表
  fetchRoleList()
})

// 方法定义
// 获取角色列表
const fetchRoleList = () => {
  loading.value = true
  
  // 模拟API请求
  setTimeout(() => {
    // 模拟数据
    roleList.value = [
      {
        id: 1,
        name: '超级管理员',
        code: 'admin',
        sort: 0,
        status: 1,
        remark: '超级管理员拥有所有权限',
        createTime: '2023-01-01T00:00:00'
      },
      {
        id: 2,
        name: '普通管理员',
        code: 'manager',
        sort: 1,
        status: 1,
        remark: '普通管理员拥有除用户管理外的所有权限',
        createTime: '2023-01-01T00:00:00'
      },
      {
        id: 3,
        name: '档案管理员',
        code: 'archive_manager',
        sort: 2,
        status: 1,
        remark: '档案管理员只拥有档案管理相关权限',
        createTime: '2023-01-02T00:00:00'
      },
      {
        id: 4,
        name: '借阅专员',
        code: 'borrow_specialist',
        sort: 3,
        status: 1,
        remark: '借阅专员只拥有借阅管理相关权限',
        createTime: '2023-01-03T00:00:00'
      },
      {
        id: 5,
        name: '只读用户',
        code: 'reader',
        sort: 4,
        status: 0,
        remark: '只读用户只拥有查询权限',
        createTime: '2023-01-04T00:00:00'
      }
    ]
    total.value = roleList.value.length
    loading.value = false
  }, 500)
}

// 搜索查询
const handleQuery = () => {
  queryParams.pageNum = 1
  fetchRoleList()
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
  fetchRoleList()
}

// 分页页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchRoleList()
}

// 添加角色
const handleAddRole = () => {
  resetRoleForm()
  roleDialogVisible.value = true
}

// 编辑角色
const handleEditRole = (row) => {
  resetRoleForm()
  // 复制数据到表单
  Object.assign(roleForm, row)
  roleDialogVisible.value = true
}

// 删除角色
const handleDeleteRole = (row) => {
  ElMessageBox.confirm(
    `确定要删除角色"${row.name}"吗？`,
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
      fetchRoleList()
    }, 500)
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 切换角色状态
const handleToggleStatus = (row) => {
  const status = row.status === 1 ? 0 : 1
  const statusText = status === 1 ? '启用' : '停用'
  
  ElMessageBox.confirm(
    `确定要${statusText}角色"${row.name}"吗？`,
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

// 重置角色表单
const resetRoleForm = () => {
  if (roleFormRef.value) {
    roleFormRef.value.resetFields()
  }
  Object.assign(roleForm, {
    id: undefined,
    name: '',
    code: '',
    sort: 0,
    status: 1,
    remark: ''
  })
}

// 提交角色表单
const submitRoleForm = async () => {
  if (!roleFormRef.value) return
  
  await roleFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      // 模拟API请求
      setTimeout(() => {
        const message = roleForm.id ? '修改成功' : '添加成功'
        ElMessage.success(message)
        roleDialogVisible.value = false
        fetchRoleList()
        submitting.value = false
      }, 1000)
    } catch (error) {
      console.error(error)
      ElMessage.error('操作失败，请稍后重试')
      submitting.value = false
    }
  })
}

// 分配权限
const handlePermission = (row) => {
  currentRole.value = row
  
  // 获取权限树数据
  fetchPermissionTree()
  
  // 获取该角色已拥有的权限
  fetchRolePermissions(row.id)
  
  // 获取数据权限配置
  fetchDataScopeConfig(row.id)
  
  permissionDialogVisible.value = true
}

// 获取权限树数据
const fetchPermissionTree = () => {
  // 模拟API请求
  permissionTree.value = [
    {
      id: 1,
      name: '系统管理',
      children: [
        {
          id: 11,
          name: '用户管理',
          children: [
            { id: 111, name: '用户查询' },
            { id: 112, name: '用户新增' },
            { id: 113, name: '用户修改' },
            { id: 114, name: '用户删除' },
            { id: 115, name: '用户导出' },
            { id: 116, name: '重置密码' }
          ]
        },
        {
          id: 12,
          name: '角色管理',
          children: [
            { id: 121, name: '角色查询' },
            { id: 122, name: '角色新增' },
            { id: 123, name: '角色修改' },
            { id: 124, name: '角色删除' },
            { id: 125, name: '角色导出' }
          ]
        },
        {
          id: 13,
          name: '菜单管理',
          children: [
            { id: 131, name: '菜单查询' },
            { id: 132, name: '菜单新增' },
            { id: 133, name: '菜单修改' },
            { id: 134, name: '菜单删除' }
          ]
        },
        {
          id: 14,
          name: '部门管理',
          children: [
            { id: 141, name: '部门查询' },
            { id: 142, name: '部门新增' },
            { id: 143, name: '部门修改' },
            { id: 144, name: '部门删除' }
          ]
        }
      ]
    },
    {
      id: 2,
      name: '档案管理',
      children: [
        {
          id: 21,
          name: '档案录入',
          children: [
            { id: 211, name: '档案新增' },
            { id: 212, name: '档案导入' }
          ]
        },
        {
          id: 22,
          name: '档案查询',
          children: [
            { id: 221, name: '档案列表查询' },
            { id: 222, name: '档案详情查看' },
            { id: 223, name: '档案导出' }
          ]
        },
        {
          id: 23,
          name: '档案管理',
          children: [
            { id: 231, name: '档案修改' },
            { id: 232, name: '档案删除' },
            { id: 233, name: '档案审批' }
          ]
        }
      ]
    },
    {
      id: 3,
      name: '借阅管理',
      children: [
        {
          id: 31,
          name: '借阅申请',
          children: [
            { id: 311, name: '申请提交' },
            { id: 312, name: '申请记录查询' }
          ]
        },
        {
          id: 32,
          name: '借阅审批',
          children: [
            { id: 321, name: '审批列表查询' },
            { id: 322, name: '审批处理' }
          ]
        },
        {
          id: 33,
          name: '借阅归还',
          children: [
            { id: 331, name: '归还登记' },
            { id: 332, name: '超期提醒' }
          ]
        }
      ]
    }
  ]
}

// 获取角色的权限
const fetchRolePermissions = (roleId) => {
  // 模拟API请求，根据角色获取已分配的权限
  if (roleId === 1) { // 超级管理员
    selectedPermissions.value = [1, 11, 12, 13, 14, 111, 112, 113, 114, 115, 116, 121, 122, 123, 124, 125, 131, 132, 133, 134, 141, 142, 143, 144, 2, 21, 22, 23, 211, 212, 221, 222, 223, 231, 232, 233, 3, 31, 32, 33, 311, 312, 321, 322, 331, 332]
  } else if (roleId === 2) { // 普通管理员
    selectedPermissions.value = [1, 12, 13, 14, 121, 122, 123, 124, 125, 131, 132, 133, 134, 141, 142, 143, 144, 2, 21, 22, 23, 211, 212, 221, 222, 223, 231, 232, 233, 3, 31, 32, 33, 311, 312, 321, 322, 331, 332]
  } else if (roleId === 3) { // 档案管理员
    selectedPermissions.value = [2, 21, 22, 23, 211, 212, 221, 222, 223, 231, 232, 233]
  } else if (roleId === 4) { // 借阅专员
    selectedPermissions.value = [3, 31, 32, 33, 311, 312, 321, 322, 331, 332]
  } else if (roleId === 5) { // 只读用户
    selectedPermissions.value = [2, 22, 221, 222]
  }
}

// 获取数据权限配置
const fetchDataScopeConfig = (roleId) => {
  // 模拟API请求，获取数据权限配置
  if (roleId === 1) {
    dataScope.value = 1 // 全部数据权限
    selectedDepts.value = []
  } else if (roleId === 2) {
    dataScope.value = 4 // 本部门及以下数据权限
    selectedDepts.value = []
  } else if (roleId === 3) {
    dataScope.value = 2 // 自定义数据权限
    selectedDepts.value = [2, 3] // 技术部和市场部
    fetchDeptTree()
  } else if (roleId === 4) {
    dataScope.value = 3 // 本部门数据权限
    selectedDepts.value = []
  } else if (roleId === 5) {
    dataScope.value = 5 // 仅本人数据权限
    selectedDepts.value = []
  }
}

// 获取部门树数据
const fetchDeptTree = () => {
  // 模拟API请求
  deptTree.value = [
    {
      id: 1,
      name: '总部',
      children: [
        {
          id: 2,
          name: '技术部',
          children: [
            { id: 6, name: '开发组' },
            { id: 7, name: '测试组' },
            { id: 8, name: '运维组' }
          ]
        },
        {
          id: 3,
          name: '市场部',
          children: [
            { id: 9, name: '国内市场组' },
            { id: 10, name: '海外市场组' }
          ]
        },
        {
          id: 4,
          name: '财务部'
        },
        {
          id: 5,
          name: '人力资源部'
        }
      ]
    }
  ]
}

// 提交权限分配
const submitPermission = () => {
  submitting.value = true
  
  // 获取选中的权限
  const checkedKeys = permissionTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
  const permissions = [...checkedKeys, ...halfCheckedKeys]
  
  // 获取选中的部门（如果是自定义数据权限）
  let departments = []
  if (dataScope.value === 2 && deptTreeRef.value) {
    departments = deptTreeRef.value.getCheckedKeys()
  }
  
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('权限分配成功')
    submitting.value = false
    permissionDialogVisible.value = false
  }, 1000)
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
.role-management-container {
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
</style> 