<template>
  <div class="category-container">
    <div class="page-header">
      <h2>文档分类管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddCategory">
          <el-icon><Plus /></el-icon>新增分类
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 分类树 -->
      <el-col :span="8">
        <el-card class="tree-card">
          <template #header>
            <div class="card-header">
              <span>分类结构</span>
              <div class="header-actions">
                <el-button type="text" @click="refreshCategoryTree">
                  <el-icon><Refresh /></el-icon>刷新
                </el-button>
                <el-button type="text" @click="expandAll">
                  <el-icon><Fold /></el-icon>展开/折叠
                </el-button>
              </div>
            </div>
          </template>
          <div class="filter-input">
            <el-input
              v-model="filterText"
              placeholder="输入关键字过滤"
              clearable
              prefix-icon="Search"
            />
          </div>
          <el-tree
            ref="categoryTree"
            :data="categoryData"
            node-key="id"
            :props="defaultProps"
            :filter-node-method="filterNode"
            highlight-current
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="custom-tree-node">
                <span>{{ node.label }}</span>
                <span class="node-actions">
                  <el-button type="primary" link @click.stop="handleEditCategory(data)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button type="danger" link @click.stop="handleDeleteCategory(node, data)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </span>
              </div>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 分类详情 -->
      <el-col :span="16">
        <el-card v-if="currentCategory.id" class="detail-card">
          <template #header>
            <div class="card-header">
              <span>分类详情</span>
            </div>
          </template>
          <el-form 
            ref="categoryForm" 
            :model="currentCategory" 
            :rules="categoryRules" 
            label-width="100px"
          >
            <el-form-item label="分类名称" prop="name">
              <el-input v-model="currentCategory.name" placeholder="请输入分类名称" />
            </el-form-item>
            <el-form-item label="上级分类" prop="parentId">
              <el-cascader
                v-model="currentCategory.parentId"
                :options="categoryOptions"
                :props="{ checkStrictly: true, value: 'id', label: 'name' }"
                clearable
                placeholder="请选择上级分类"
              />
            </el-form-item>
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="currentCategory.sort" :min="0" :max="999" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-switch
                v-model="currentCategory.status"
                :active-value="1"
                :inactive-value="0"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input
                v-model="currentCategory.description"
                type="textarea"
                :rows="4"
                placeholder="请输入分类描述"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveCategory">保存</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 文档统计信息 -->
          <div class="statistics-section" v-if="currentCategory.id">
            <h3>文档统计</h3>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-statistic title="文档总数" :value="currentCategory.documentCount || 0">
                  <template #suffix>
                    <span>个</span>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="8">
                <el-statistic title="文件大小" :value="formatFileSize(currentCategory.totalSize || 0)">
                </el-statistic>
              </el-col>
              <el-col :span="8">
                <el-statistic title="下载次数" :value="currentCategory.downloadCount || 0">
                  <template #suffix>
                    <span>次</span>
                  </template>
                </el-statistic>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <el-empty v-else description="请选择分类" />
      </el-col>
    </el-row>

    <!-- 新增/编辑分类对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="dialogType === 'add' ? '新增分类' : '编辑分类'"
      width="500px"
    >
      <el-form
        ref="dialogCategoryForm"
        :model="editingCategory"
        :rules="categoryRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="editingCategory.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类" prop="parentId">
          <el-cascader
            v-model="editingCategory.parentId"
            :options="categoryOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'name' }"
            clearable
            placeholder="请选择上级分类"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="editingCategory.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="editingCategory.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editingCategory.description"
            type="textarea"
            :rows="4"
            placeholder="请输入分类描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCategoryForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Refresh, Edit, Delete, Search, Fold
} from '@element-plus/icons-vue'

// 分类树数据
const categoryData = ref([
  {
    id: 1,
    name: '规章制度',
    parentId: 0,
    sort: 1,
    status: 1,
    description: '企业规章制度文档',
    documentCount: 12,
    totalSize: 25600000,
    downloadCount: 120,
    children: [
      {
        id: 5,
        name: '人事制度',
        parentId: 1,
        sort: 1,
        status: 1,
        description: '人事相关规章制度',
        documentCount: 5,
        totalSize: 10240000,
        downloadCount: 50
      },
      {
        id: 6,
        name: '财务制度',
        parentId: 1,
        sort: 2,
        status: 1,
        description: '财务相关规章制度',
        documentCount: 7,
        totalSize: 15360000,
        downloadCount: 70
      }
    ]
  },
  {
    id: 2,
    name: '技术文档',
    parentId: 0,
    sort: 2,
    status: 1,
    description: '技术相关文档',
    documentCount: 25,
    totalSize: 51200000,
    downloadCount: 230,
    children: [
      {
        id: 7,
        name: '需求文档',
        parentId: 2,
        sort: 1,
        status: 1,
        description: '项目需求说明文档',
        documentCount: 10,
        totalSize: 20480000,
        downloadCount: 90
      },
      {
        id: 8,
        name: '设计文档',
        parentId: 2,
        sort: 2,
        status: 1,
        description: '项目设计说明文档',
        documentCount: 8,
        totalSize: 16384000,
        downloadCount: 80
      },
      {
        id: 9,
        name: '操作手册',
        parentId: 2,
        sort: 3,
        status: 1,
        description: '系统操作手册',
        documentCount: 7,
        totalSize: 14336000,
        downloadCount: 60
      }
    ]
  },
  {
    id: 3,
    name: '会议记录',
    parentId: 0,
    sort: 3,
    status: 1,
    description: '各类会议记录文档',
    documentCount: 18,
    totalSize: 30720000,
    downloadCount: 90
  },
  {
    id: 4,
    name: '项目资料',
    parentId: 0,
    sort: 4,
    status: 1,
    description: '项目相关资料',
    documentCount: 30,
    totalSize: 61440000,
    downloadCount: 150
  }
])

// 分类选项（用于级联选择器）
const categoryOptions = ref([])

// 当前选中的分类
const currentCategory = ref({})

// 编辑中的分类
const editingCategory = ref({
  name: '',
  parentId: null,
  sort: 0,
  status: 1,
  description: ''
})

// 树形控件配置
const defaultProps = {
  children: 'children',
  label: 'name'
}

// 对话框相关
const categoryDialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'

// 表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sort: [
    { type: 'number', message: '排序必须为数字' }
  ]
}

// 引用
const categoryTree = ref(null)
const categoryForm = ref(null)
const dialogCategoryForm = ref(null)

// 过滤文本
const filterText = ref('')

// 监听过滤文本变化
watch(filterText, (val) => {
  categoryTree.value.filter(val)
})

// 过滤节点方法
const filterNode = (value, data) => {
  if (!value) return true
  return data.name.includes(value)
}

// 构建分类选项
const buildCategoryOptions = () => {
  // 深拷贝避免修改原始数据
  categoryOptions.value = JSON.parse(JSON.stringify(categoryData.value))
}

// 刷新分类树
const refreshCategoryTree = async () => {
  // 实际应用中应该从API获取最新数据
  ElMessage.success('分类树已刷新')
  // 重新构建分类选项
  buildCategoryOptions()
}

// 展开/折叠所有节点
const expandAll = () => {
  const tree = categoryTree.value
  // 获取当前展开状态
  const expandedKeys = tree.getCheckedKeys()
  if (expandedKeys.length > 0) {
    // 如果有展开的节点，则全部折叠
    tree.store.nodesMap = {}
  } else {
    // 否则全部展开
    tree.expandAll()
  }
}

// 处理节点点击
const handleNodeClick = (data) => {
  currentCategory.value = { ...data }
}

// 处理添加分类
const handleAddCategory = () => {
  dialogType.value = 'add'
  editingCategory.value = {
    name: '',
    parentId: null,
    sort: 0,
    status: 1,
    description: ''
  }
  categoryDialogVisible.value = true
  nextTick(() => {
    dialogCategoryForm.value?.resetFields()
  })
}

// 处理编辑分类
const handleEditCategory = (data) => {
  dialogType.value = 'edit'
  editingCategory.value = { ...data }
  if (editingCategory.value.parentId === 0) {
    editingCategory.value.parentId = null
  }
  categoryDialogVisible.value = true
}

// 处理删除分类
const handleDeleteCategory = (node, data) => {
  ElMessageBox.confirm(
    `确定要删除分类 ${data.name} 吗？${data.children?.length ? '注意：将同时删除所有子分类！' : ''}`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 实际应用中应该调用API删除
    // 模拟删除
    const parent = node.parent
    const children = parent.data.children || parent.data
    const index = children.findIndex(d => d.id === data.id)
    if (index !== -1) {
      children.splice(index, 1)
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 提交分类表单
const submitCategoryForm = async () => {
  const formRef = dialogCategoryForm.value
  if (!formRef) return
  
  await formRef.validate((valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        // 模拟添加
        const newCategory = {
          ...editingCategory.value,
          id: Date.now(),
          documentCount: 0,
          totalSize: 0,
          downloadCount: 0
        }
        
        // 如果有父级，则添加到对应父级的children中
        if (newCategory.parentId) {
          const findAndAddChild = (categories, parentId) => {
            for (const category of categories) {
              if (category.id === parentId) {
                if (!category.children) category.children = []
                category.children.push(newCategory)
                return true
              }
              if (category.children) {
                if (findAndAddChild(category.children, parentId)) {
                  return true
                }
              }
            }
            return false
          }
          
          findAndAddChild(categoryData.value, newCategory.parentId)
        } else {
          // 否则添加到顶级
          newCategory.parentId = 0
          categoryData.value.push(newCategory)
        }
        
        ElMessage.success('添加成功')
      } else {
        // 模拟编辑
        const updateCategory = (categories, category) => {
          for (let i = 0; i < categories.length; i++) {
            if (categories[i].id === category.id) {
              categories[i] = { ...categories[i], ...category }
              return true
            }
            if (categories[i].children) {
              if (updateCategory(categories[i].children, category)) {
                return true
              }
            }
          }
          return false
        }
        
        updateCategory(categoryData.value, editingCategory.value)
        ElMessage.success('更新成功')
        
        // 如果当前选中的就是编辑的分类，更新当前分类信息
        if (currentCategory.value.id === editingCategory.value.id) {
          currentCategory.value = { ...editingCategory.value }
        }
      }
      
      categoryDialogVisible.value = false
      // 重新构建分类选项
      buildCategoryOptions()
    }
  })
}

// 保存分类（详情表单）
const saveCategory = async () => {
  const formRef = categoryForm.value
  if (!formRef) return
  
  await formRef.validate((valid) => {
    if (valid) {
      // 模拟保存
      const updateCategory = (categories, category) => {
        for (let i = 0; i < categories.length; i++) {
          if (categories[i].id === category.id) {
            categories[i] = { ...categories[i], ...category }
            return true
          }
          if (categories[i].children) {
            if (updateCategory(categories[i].children, category)) {
              return true
            }
          }
        }
        return false
      }
      
      updateCategory(categoryData.value, currentCategory.value)
      ElMessage.success('保存成功')
      
      // 重新构建分类选项
      buildCategoryOptions()
    }
  })
}

// 重置表单
const resetForm = () => {
  categoryForm.value?.resetFields()
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0
  let formattedSize = size
  
  while (formattedSize >= 1024 && index < units.length - 1) {
    formattedSize /= 1024
    index++
  }
  
  return `${formattedSize.toFixed(2)} ${units[index]}`
}

// 生命周期钩子
onMounted(() => {
  buildCategoryOptions()
})
</script>

<style scoped>
.category-container {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-card,
.detail-card {
  margin-bottom: 20px;
  height: calc(100vh - 180px);
  overflow-y: auto;
}

.filter-input {
  margin-bottom: 15px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
}

.node-actions {
  display: none;
}

.custom-tree-node:hover .node-actions {
  display: inline-block;
}

.statistics-section {
  margin-top: 30px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.statistics-section h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 