<template>
  <div class="workflow-process-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>流程管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddProcess">
          <el-icon><Plus /></el-icon>新增流程
        </el-button>
      </div>
    </div>
    
    <!-- 流程列表 -->
    <el-card class="process-card">
      <template #header>
        <div class="card-header">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索流程名称或描述"
            clearable
            prefix-icon="Search"
            @input="handleSearch"
          />
          <div class="header-actions">
            <el-button-group>
              <el-button type="primary" @click="refreshProcessList">
                <el-icon><Refresh /></el-icon>刷新
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="processList"
        border
        style="width: 100%"
      >
        <el-table-column prop="name" label="流程名称" min-width="150" />
        <el-table-column prop="code" label="流程编码" width="120" />
        <el-table-column prop="category" label="流程分类" width="120">
          <template #default="scope">
            <el-tag>{{ scope.row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-tooltip content="查看详情">
                <el-button type="primary" link @click="handleViewProcess(scope.row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑流程">
                <el-button type="primary" link @click="handleEditProcess(scope.row)" :disabled="scope.row.status === 1">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="流程设计">
                <el-button type="primary" link @click="handleDesignProcess(scope.row)" :disabled="scope.row.status === 1">
                  <el-icon><Operation /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="发布流程">
                <el-button type="success" link @click="handlePublishProcess(scope.row)" :disabled="scope.row.status === 1">
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="禁用流程">
                <el-button type="warning" link @click="handleToggleStatus(scope.row)" v-if="scope.row.status === 1">
                  <el-icon><CircleClose /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="启用流程">
                <el-button type="success" link @click="handleToggleStatus(scope.row)" v-if="scope.row.status !== 1">
                  <el-icon><CircleCheck /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除流程">
                <el-button type="danger" link @click="handleDeleteProcess(scope.row)" :disabled="scope.row.status === 1">
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
    
    <!-- 流程表单对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      :title="dialogType === 'add' ? '新增流程' : '编辑流程'"
      width="600px"
    >
      <el-form
        ref="processFormRef"
        :model="processForm"
        :rules="processRules"
        label-width="100px"
      >
        <el-form-item label="流程名称" prop="name">
          <el-input v-model="processForm.name" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="流程编码" prop="code">
          <el-input v-model="processForm.code" placeholder="请输入流程编码" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="流程分类" prop="category">
          <el-select v-model="processForm.category" placeholder="请选择流程分类">
            <el-option label="档案借阅" value="档案借阅" />
            <el-option label="档案归还" value="档案归还" />
            <el-option label="档案销毁" value="档案销毁" />
            <el-option label="文档审批" value="文档审批" />
          </el-select>
        </el-form-item>
        <el-form-item label="流程描述" prop="description">
          <el-input
            v-model="processForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入流程描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProcessForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Refresh, Search, View, Edit, Operation,
  Promotion, CircleClose, CircleCheck, Delete
} from '@element-plus/icons-vue'

const router = useRouter()
const processFormRef = ref(null)

// 搜索关键字
const searchKeyword = ref('')

// 加载状态
const loading = ref(false)
const processList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  keyword: ''
})

// 对话框状态
const processDialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'

// 流程表单
const processForm = reactive({
  id: '',
  name: '',
  code: '',
  category: '',
  description: ''
})

// 表单验证规则
const processRules = {
  name: [
    { required: true, message: '请输入流程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入流程编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: '编码只能包含字母、数字、下划线和连字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择流程分类', trigger: 'change' }
  ]
}

// 获取流程列表
const fetchProcessList = async () => {
  loading.value = true
  try {
    // 模拟数据
    setTimeout(() => {
      processList.value = [
        {
          id: '1',
          name: '档案借阅审批流程',
          code: 'FLOW_BORROW',
          category: '档案借阅',
          version: 'v1.0',
          status: 1, // 1-已发布，0-草稿
          createTime: '2023-02-10 10:30:00',
          updateTime: '2023-03-05 14:20:00',
          description: '档案借阅审批流程，包括申请、部门审批、档案管理员审批等环节'
        },
        {
          id: '2',
          name: '档案归还流程',
          code: 'FLOW_RETURN',
          category: '档案归还',
          version: 'v1.0',
          status: 1,
          createTime: '2023-02-12 09:15:00',
          updateTime: '2023-03-01 15:30:00',
          description: '档案归还流程，包括归还、档案管理员确认等环节'
        },
        {
          id: '3',
          name: '档案销毁审批流程',
          code: 'FLOW_DESTROY',
          category: '档案销毁',
          version: 'v1.0',
          status: 0,
          createTime: '2023-03-05 11:20:00',
          updateTime: '2023-03-05 11:20:00',
          description: '档案销毁审批流程，包括销毁申请、部门审批、分管领导审批、档案管理员确认等环节'
        }
      ]
      total.value = 3
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取流程列表失败')
    loading.value = false
  }
}

// 刷新流程列表
const refreshProcessList = () => {
  fetchProcessList()
  ElMessage.success('刷新成功')
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  queryParams.keyword = searchKeyword.value
  fetchProcessList()
}

// 页面大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  fetchProcessList()
}

// 页码变化
const handlePageChange = (page) => {
  queryParams.page = page
  fetchProcessList()
}

// 获取状态文本
const getStatusText = (status) => {
  return status === 1 ? '已发布' : '草稿'
}

// 获取状态类型
const getStatusType = (status) => {
  return status === 1 ? 'success' : 'info'
}

// 添加流程
const handleAddProcess = () => {
  dialogType.value = 'add'
  processForm.id = ''
  processForm.name = ''
  processForm.code = ''
  processForm.category = ''
  processForm.description = ''
  processDialogVisible.value = true
}

// 编辑流程
const handleEditProcess = (row) => {
  dialogType.value = 'edit'
  processForm.id = row.id
  processForm.name = row.name
  processForm.code = row.code
  processForm.category = row.category
  processForm.description = row.description
  processDialogVisible.value = true
}

// 查看流程
const handleViewProcess = (row) => {
  router.push(`/workflow/process-detail/${row.id}`)
}

// 流程设计
const handleDesignProcess = (row) => {
  router.push(`/workflow/process-design/${row.id}`)
}

// 发布流程
const handlePublishProcess = (row) => {
  ElMessageBox.confirm(
    `确定要发布流程 ${row.name} 吗？发布后将不能修改流程定义。`,
    '发布流程',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟发布
    const index = processList.value.findIndex(p => p.id === row.id)
    if (index !== -1) {
      processList.value[index].status = 1
      processList.value[index].updateTime = new Date().toLocaleString()
    }
    ElMessage.success('流程发布成功')
  }).catch(() => {})
}

// 切换状态
const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(
    `确定要${action}流程 ${row.name} 吗？`,
    `${action}流程`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟状态切换
    const index = processList.value.findIndex(p => p.id === row.id)
    if (index !== -1) {
      processList.value[index].status = row.status === 1 ? 0 : 1
      processList.value[index].updateTime = new Date().toLocaleString()
    }
    ElMessage.success(`流程${action}成功`)
  }).catch(() => {})
}

// 删除流程
const handleDeleteProcess = (row) => {
  ElMessageBox.confirm(
    `确定要删除流程 ${row.name} 吗？`,
    '删除流程',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟删除
    processList.value = processList.value.filter(p => p.id !== row.id)
    total.value--
    ElMessage.success('流程删除成功')
  }).catch(() => {})
}

// 提交流程表单
const submitProcessForm = async () => {
  if (!processFormRef.value) return
  
  await processFormRef.value.validate((valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        // 模拟添加
        const newProcess = {
          id: Date.now().toString(),
          ...processForm,
          version: 'v1.0',
          status: 0,
          createTime: new Date().toLocaleString(),
          updateTime: new Date().toLocaleString()
        }
        processList.value.unshift(newProcess)
        total.value++
        ElMessage.success('流程添加成功')
      } else {
        // 模拟编辑
        const index = processList.value.findIndex(p => p.id === processForm.id)
        if (index !== -1) {
          processList.value[index] = {
            ...processList.value[index],
            name: processForm.name,
            category: processForm.category,
            description: processForm.description,
            updateTime: new Date().toLocaleString()
          }
          ElMessage.success('流程更新成功')
        }
      }
      processDialogVisible.value = false
    }
  })
}

// 生命周期钩子
onMounted(() => {
  fetchProcessList()
})
</script>

<style scoped>
.workflow-process-container {
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

.process-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .el-input {
  width: 300px;
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