<template>
  <div class="archive">
    <div class="toolbar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索档案..."
        class="search-input"
      >
        <template #prefix>
          <el-icon><search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleAdd">
        <el-icon><plus /></el-icon>新建档案
      </el-button>
    </div>

    <el-table :data="archiveList" style="width: 100%">
      <el-table-column prop="id" label="编号" width="100" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="category" label="类别" width="120" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '已归档' ? 'success' : 'warning'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新建/编辑档案对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新建档案' : '编辑档案'"
      width="50%"
    >
      <el-form :model="archiveForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="archiveForm.title" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="archiveForm.category" placeholder="请选择类别">
            <el-option label="财务" value="财务" />
            <el-option label="人事" value="人事" />
            <el-option label="项目" value="项目" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="archiveForm.description"
            type="textarea"
            rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)
const dialogVisible = ref(false)
const dialogType = ref('add')

const archiveForm = ref({
  title: '',
  category: '',
  description: ''
})

const archiveList = ref([
  {
    id: '001',
    title: '2024年第一季度财务报告',
    category: '财务',
    createTime: '2024-03-04 14:30',
    status: '已归档'
  },
  {
    id: '002',
    title: '人事档案更新',
    category: '人事',
    createTime: '2024-03-04 11:20',
    status: '处理中'
  }
])

const handleAdd = () => {
  dialogType.value = 'add'
  archiveForm.value = {
    title: '',
    category: '',
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  archiveForm.value = { ...row }
  dialogVisible.value = true
}

const handleView = (row) => {
  // 实现查看功能
  console.log('查看档案:', row)
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该档案吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 实现删除功能
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleSubmit = () => {
  // 实现提交功能
  ElMessage.success(dialogType.value === 'add' ? '新建成功' : '更新成功')
  dialogVisible.value = false
}

const handleSizeChange = (val) => {
  pageSize.value = val
  // 重新加载数据
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  // 重新加载数据
}
</script>

<style scoped>
.archive {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}

.search-input {
  width: 300px;
}

.pagination {
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