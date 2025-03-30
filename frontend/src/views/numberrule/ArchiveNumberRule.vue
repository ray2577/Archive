# 档案编号规则管理页面
<template>
  <div class="archive-number-rule-container">
    <el-card class="rule-card">
      <template #header>
        <div class="card-header">
          <h2>档案编号规则管理</h2>
          <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
      </template>

      <!-- 规则列表 -->
      <el-table :data="rules" style="width: 100%" v-loading="loading">
        <el-table-column prop="category" label="档案类别" />
        <el-table-column prop="prefix" label="前缀" />
        <el-table-column prop="dateFormat" label="日期格式" />
        <el-table-column prop="serialLength" label="序号长度" />
        <el-table-column prop="separator" label="分隔符" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'">
              {{ row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button 
                type="success" 
                v-if="!row.isActive"
                @click="handleToggleStatus(row)">
                启用
              </el-button>
              <el-button 
                type="warning" 
                v-else
                @click="handleToggleStatus(row)">
                禁用
              </el-button>
              <el-button 
                type="danger" 
                @click="handleDelete(row)"
                :disabled="row.isActive">
                删除
              </el-button>
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
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑规则' : '新增规则'"
      width="500px">
      <el-form
        ref="ruleForm"
        :model="ruleForm"
        :rules="rules"
        label-width="100px">
        <el-form-item label="档案类别" prop="category">
          <el-input v-model="ruleForm.category" placeholder="请输入档案类别" />
        </el-form-item>
        <el-form-item label="前缀" prop="prefix">
          <el-input v-model="ruleForm.prefix" placeholder="请输入前缀" />
        </el-form-item>
        <el-form-item label="日期格式" prop="dateFormat">
          <el-select v-model="ruleForm.dateFormat" placeholder="请选择日期格式" style="width: 100%">
            <el-option label="yyyyMMdd" value="yyyyMMdd" />
            <el-option label="yyMMdd" value="yyMMdd" />
            <el-option label="yyyy" value="yyyy" />
            <el-option label="yyyyMM" value="yyyyMM" />
          </el-select>
        </el-form-item>
        <el-form-item label="序号长度" prop="serialLength">
          <el-input-number 
            v-model="ruleForm.serialLength" 
            :min="1" 
            :max="10"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="分隔符" prop="separator">
          <el-input v-model="ruleForm.separator" placeholder="请输入分隔符" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="ruleForm.isActive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 数据
const rules = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单
const ruleForm = reactive({
  id: null,
  category: '',
  prefix: '',
  dateFormat: 'yyyyMMdd',
  serialLength: 4,
  separator: '-',
  isActive: true,
  currentSerial: 0
})

// 表单验证规则
const formRules = {
  category: [
    { required: true, message: '请输入档案类别', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  prefix: [
    { required: true, message: '请输入前缀', trigger: 'blur' },
    { min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur' }
  ],
  dateFormat: [
    { required: true, message: '请选择日期格式', trigger: 'change' }
  ],
  serialLength: [
    { required: true, message: '请输入序号长度', trigger: 'blur' }
  ]
}

const ruleFormRef = ref(null)

// 获取规则列表
const fetchRules = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/archive-numbers/rules', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    rules.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取规则列表失败')
  } finally {
    loading.value = false
  }
}

// 新增规则
const handleAdd = () => {
  isEdit.value = false
  Object.assign(ruleForm, {
    id: null,
    category: '',
    prefix: '',
    dateFormat: 'yyyyMMdd',
    serialLength: 4,
    separator: '-',
    isActive: true,
    currentSerial: 0
  })
  dialogVisible.value = true
}

// 编辑规则
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(ruleForm, row)
  dialogVisible.value = true
}

// 保存规则
const handleSave = async () => {
  if (!ruleFormRef.value) return
  
  await ruleFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        if (isEdit.value) {
          await axios.put(`/api/archive-numbers/rules/${ruleForm.id}`, ruleForm)
        } else {
          await axios.post('/api/archive-numbers/rules', ruleForm)
        }
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        fetchRules()
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      } finally {
        saving.value = false
      }
    }
  })
}

// 删除规则
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      type: 'warning'
    })
    await axios.delete(`/api/archive-numbers/rules/${row.id}`)
    ElMessage.success('删除成功')
    fetchRules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 切换规则状态
const handleToggleStatus = async (row) => {
  try {
    const newStatus = !row.isActive
    await axios.put(`/api/archive-numbers/rules/${row.id}`, {
      ...row,
      isActive: newStatus
    })
    ElMessage.success(newStatus ? '启用成功' : '禁用成功')
    fetchRules()
  } catch (error) {
    ElMessage.error(newStatus ? '启用失败' : '禁用失败')
  }
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRules()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRules()
}

// 初始化
onMounted(() => {
  fetchRules()
})
</script>

<style scoped>
.archive-number-rule-container {
  padding: 20px;
}

.rule-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 