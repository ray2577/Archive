<template>
  <div class="document-list-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>文档列表</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddDocument">
          <el-icon><Plus /></el-icon>新增文档
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>批量导入
        </el-button>
        <el-button type="warning" @click="handleExport">
          <el-icon><Download /></el-icon>导出文档
        </el-button>
        <el-tooltip content="API诊断" effect="dark" placement="top">
          <div>
            <el-button type="info" @click="diagnosisVisible = true">
              <el-icon><Service /></el-icon>
            </el-button>
          </div>
        </el-tooltip>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <span>文档总数</span>
              <el-tag type="info">全部</el-tag>
            </div>
          </template>
          <div class="stat-value">
            <span class="number">{{ statistics.total || 0 }}</span>
            <span class="unit">份</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <span>最近更新</span>
              <el-tag type="success">活跃</el-tag>
            </div>
          </template>
          <div class="stat-value">
            <span class="number">{{ statistics.recent || 0 }}</span>
            <span class="unit">份</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <span>已分享</span>
              <el-tag type="warning">共享</el-tag>
            </div>
          </template>
          <div class="stat-value">
            <span class="number">{{ statistics.shared || 0 }}</span>
            <span class="unit">份</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <span>总下载量</span>
              <el-tag type="info">统计</el-tag>
            </div>
          </template>
          <div class="stat-value">
            <span class="number">{{ statistics.downloads || 0 }}</span>
            <span class="unit">次</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 搜索筛选区 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文档编号">
          <el-input v-model="searchForm.code" placeholder="请输入文档编号" clearable />
        </el-form-item>
        <el-form-item label="文档名称">
          <el-input v-model="searchForm.name" placeholder="请输入文档名称" clearable />
        </el-form-item>
        <el-form-item label="文档类型">
          <el-select v-model="searchForm.type" placeholder="请选择文档类型" clearable>
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
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
    
    <!-- 数据表格 -->
    <el-card class="table-container">
      <template #header>
        <div class="table-header">
          <span>文档列表 ({{ total }})</span>
          <div class="table-actions">
            <!-- 批量操作按钮 -->
            <div>
              <el-dropdown :disabled="selectedRows.length === 0" @command="handleBatchOperation" style="margin-right: 10px;">
                <el-button type="primary" :disabled="selectedRows.length === 0">
                  批量操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="batchExport">批量导出</el-dropdown-item>
                    <el-dropdown-item command="batchDelete" divided>批量删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            
            <el-button-group>
              <el-tooltip content="刷新">
                <div>
                  <el-button @click="fetchDocumentList">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
              <el-tooltip content="密度">
                <div>
                  <el-dropdown @command="handleSizeChange">
                    <el-button>
                      <el-icon><GridKey /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="default">默认</el-dropdown-item>
                        <el-dropdown-item command="large">宽松</el-dropdown-item>
                        <el-dropdown-item command="small">紧凑</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </el-tooltip>
              <el-tooltip content="列设置">
                <div>
                  <el-button @click="columnsDialogVisible = true">
                    <el-icon><SetUp /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
            </el-button-group>
          </div>
        </div>
      </template>
      
      <div v-if="fetchError" class="error-container">
        <el-result
          icon="error"
          title="加载失败"
          :sub-title="errorMessage || '获取文档列表失败，请稍后重试'"
        >
          <template #extra>
            <el-button type="primary" @click="retryFetch">重新加载</el-button>
            <el-button @click="diagnosisVisible = true">API诊断</el-button>
            <el-button type="warning" @click="loadMockData">使用模拟数据</el-button>
          </template>
          <p class="error-tips">
            提示：您可以点击"API诊断"按钮检查连接问题，或者点击"使用模拟数据"查看界面效果。
          </p>
          <el-collapse class="error-details-collapse">
            <el-collapse-item title="查看错误详情">
              <p><strong>错误时间：</strong> {{ new Date().toLocaleString() }}</p>
              <p><strong>错误信息：</strong> {{ errorMessage }}</p>
              <p><strong>请求参数：</strong> {{ JSON.stringify({...searchForm, page: queryParams.page, pageSize: queryParams.pageSize}) }}</p>
              <p><strong>API路径：</strong> {{ apiConfig?.baseURL || '/api' }}/documents</p>
            </el-collapse-item>
            <el-collapse-item title="可能的解决方案">
              <ol>
                <li>确认后端服务是否启动，并且可以访问 http://localhost:8000/api/documents</li>
                <li>检查Network面板中是否有CORS跨域错误</li>
                <li>确认后端返回的数据格式与前端预期一致</li>
                <li>尝试使用API诊断工具进行更详细的调试</li>
              </ol>
            </el-collapse-item>
          </el-collapse>
        </el-result>
      </div>
      
      <el-table
        v-else
        v-loading="loading"
        :data="documentList"
        :size="tableSize"
        border
        highlight-current-row
        @sort-change="handleSortChange"
        @selection-change="handleSelectionChange"
        :empty-text="fetchError ? errorMessage : '暂无文档数据'"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column 
          v-for="col in visibleColumns" 
          :key="col.prop" 
          :prop="col.prop" 
          :label="col.label" 
          :width="col.width" 
          :sortable="col.sortable" 
          :show-overflow-tooltip="true"
        >
          <template #default="scope">
            <template v-if="col.prop === 'status'">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ formatStatus(scope.row.status) }}
              </el-tag>
            </template>
            <template v-else-if="col.prop === 'createTime'">
              {{ formatDate(scope.row.createTime) }}
            </template>
            <template v-else-if="col.prop === 'fileSize'">
              {{ formatFileSize(scope.row.fileSize) }}
            </template>
            <template v-else>
              {{ scope.row[col.prop] }}
            </template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-tooltip content="查看详情">
                <div>
                  <el-button type="primary" link @click="handleView(scope.row)">
                    <el-icon><View /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
              <el-tooltip content="编辑文档">
                <div>
                  <el-button type="primary" link @click="handleEdit(scope.row)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
              <el-tooltip content="分享文档">
                <div>
                  <el-button type="primary" link @click="handleShare(scope.row)">
                    <el-icon><Share /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
              <el-tooltip content="下载文档">
                <div>
                  <el-button type="primary" link @click="handleDownload(scope.row)">
                    <el-icon><Download /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
              <el-tooltip content="删除文档">
                <div>
                  <el-button type="danger" link @click="handleDelete(scope.row)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空数据提示 -->
      <el-empty 
        v-if="!loading && (!documentList || documentList.length === 0) && !fetchError" 
        description="暂无文档数据"
      >
        <template #extra>
          <el-button type="primary" @click="handleAddDocument">创建文档</el-button>
        </template>
      </el-empty>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="queryParams.page"
          :page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          @update:current-page="val => queryParams.page = val"
          @update:page-size="val => queryParams.pageSize = val"
        />
      </div>
    </el-card>
    
    <!-- 列设置对话框 -->
    <el-dialog
      v-model="columnsDialogVisible"
      title="列设置"
      width="500px"
    >
      <el-transfer
        v-model="visibleColumnProps"
        :data="allColumns"
        filterable
        :titles="['隐藏列', '显示列']"
        @change="updateVisibleColumns"
      >
        <template #default="{ option }">
          {{ option.label }}
        </template>
      </el-transfer>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="columnsDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveColumnsSettings">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- API诊断对话框 -->
    <el-dialog
      title="API诊断"
      v-model="diagnosisVisible"
      width="80%"
      top="5vh"
      destroy-on-close
    >
      <div class="diagnosis-content">
        <div class="diagnosis-buttons">
          <el-button type="primary" @click="testApiConnection">
            <el-icon><Connection /></el-icon>测试API连接
          </el-button>
          <el-button @click="reloadPage">
            <el-icon><RefreshRight /></el-icon>刷新页面
          </el-button>
        </div>
        
        <el-divider content-position="center">环境信息</el-divider>
        
        <el-descriptions border>
          <el-descriptions-item label="前端URL">{{ currentUrl }}</el-descriptions-item>
          <el-descriptions-item label="API基础路径">{{ apiConfig.baseURL }}</el-descriptions-item>
          <el-descriptions-item label="环境模式">{{ envMode }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="center">API测试结果</el-divider>
        
        <div v-if="apiTestResult" class="test-result">
          <el-alert
            :title="apiTestResult.success ? 'API连接成功' : 'API连接失败'"
            :type="apiTestResult.success ? 'success' : 'error'"
            :description="apiTestResult.statusText"
            show-icon
          />
          
          <el-descriptions border class="result-details">
            <el-descriptions-item label="状态码">{{ apiTestResult.status }}</el-descriptions-item>
            <el-descriptions-item label="请求URL">{{ apiTestResult.url }}</el-descriptions-item>
            <el-descriptions-item label="响应数据" :span="2">
              <el-collapse>
                <el-collapse-item title="查看详细数据">
                  <pre class="response-data">{{ apiTestResult.data }}</pre>
                </el-collapse-item>
              </el-collapse>
            </el-descriptions-item>
            <el-descriptions-item v-if="apiTestResult.headers" label="响应头" :span="2">
              <el-collapse>
                <el-collapse-item title="查看响应头">
                  <pre class="response-headers">{{ apiTestResult.headers }}</pre>
                </el-collapse-item>
              </el-collapse>
            </el-descriptions-item>
            <el-descriptions-item v-if="apiTestResult.error" label="错误详情" :span="2">
              <pre class="error-details">{{ JSON.stringify(apiTestResult.error, null, 2) }}</pre>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div v-else class="no-test">
          <el-empty description="尚未进行API测试" />
        </div>
        
        <el-divider content-position="center">常见问题</el-divider>
        
        <el-collapse>
          <el-collapse-item title="无法连接到API" name="1">
            <ol>
              <li>确认后端服务是否启动（http://localhost:8000）</li>
              <li>检查浏览器控制台是否有CORS跨域错误</li>
              <li>确认网络环境是否正常</li>
            </ol>
          </el-collapse-item>
          <el-collapse-item title="请求成功但无数据" name="2">
            <ol>
              <li>确认后端返回的数据格式是否符合前端预期</li>
              <li>检查数据库中是否有符合条件的记录</li>
              <li>在浏览器Network面板中查看实际响应内容</li>
            </ol>
          </el-collapse-item>
          <el-collapse-item title="如何手动测试API" name="3">
            <ol>
              <li>打开浏览器开发者工具 (F12)</li>
              <li>切换到Network选项卡</li>
              <li>刷新页面，查找documents相关请求</li>
              <li>点击请求查看详情，包括Headers和Response</li>
            </ol>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Upload, Download, Search, Refresh,
  GridKey, SetUp, View, Edit, Share, Delete, Service, Connection, RefreshRight, ArrowDown
} from '@element-plus/icons-vue'
import { 
  getDocumentList, 
  createDocument, 
  updateDocument, 
  deleteDocument, 
  getDocumentStatistics,
  importDocuments,
  exportDocuments,
  batchExportDocuments,
  batchDeleteDocuments,
  downloadDocument,
  testDocumentListRequest
} from '@/api/document'
import { apiConfig } from '@/api/config'

const router = useRouter()

// 环境信息
const envMode = computed(() => import.meta.env.MODE)
const currentUrl = computed(() => typeof window !== 'undefined' ? window.location.href : '')

// 文档类型选项
const typeOptions = [
  { value: '1', label: '文本文档' },
  { value: '2', label: 'PDF文档' },
  { value: '3', label: 'Excel表格' },
  { value: '4', label: 'Word文档' },
  { value: '5', label: '其他格式' }
]

// 文档分类选项 - 从分类管理获取
const categoryOptions = ref([
  { value: '1', label: '规章制度' },
  { value: '2', label: '技术文档' },
  { value: '3', label: '会议记录' },
  { value: '4', label: '项目资料' }
])

// 所有列定义
const allColumns = [
  { key: 'id', label: 'ID', prop: 'id', width: '80', sortable: true },
  { key: 'code', label: '文档编号', prop: 'code', width: '150', sortable: true },
  { key: 'name', label: '文档名称', prop: 'name', width: '200', sortable: true },
  { key: 'type', label: '文档类型', prop: 'type', width: '120', sortable: true },
  { key: 'category', label: '所属分类', prop: 'category', width: '120', sortable: true },
  { key: 'creator', label: '创建人', prop: 'creator', width: '120', sortable: true },
  { key: 'createTime', label: '创建时间', prop: 'createTime', width: '150', sortable: true },
  { key: 'updateTime', label: '更新时间', prop: 'updateTime', width: '150', sortable: true },
  { key: 'fileSize', label: '文件大小', prop: 'fileSize', width: '120', sortable: true },
  { key: 'downloadCount', label: '下载次数', prop: 'downloadCount', width: '100', sortable: true },
  { key: 'status', label: '状态', prop: 'status', width: '100', sortable: true }
]

// 搜索表单
const searchForm = reactive({
  code: '',
  name: '',
  type: '',
  category: ''
})

// 表格状态
const loading = ref(false)
const documentList = ref([])
const total = ref(0)
const tableSize = ref('default')
const selectedRows = ref([])
const visibleColumnProps = ref(['code', 'name', 'type', 'category', 'createTime', 'fileSize'])
const columnsDialogVisible = ref(false)

// 整合查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 20,
  // 其他参数会在搜索时添加
})

// 计算属性：可见列
const visibleColumns = computed(() => {
  return allColumns.filter(col => visibleColumnProps.value.includes(col.prop))
})

// 统计数据
const statistics = reactive({
  total: 0,
  recent: 0,
  shared: 0,
  downloads: 0
})

// 错误状态
const fetchError = ref(false)
const errorMessage = ref('')

// 获取统计数据
const fetchStatistics = async () => {
  try {
    // 调用API获取统计数据
    const result = await getDocumentStatistics()
    
    if (result.code === 200 && result.data) {
      statistics.total = result.data.total || 0
      statistics.recent = result.data.recent || 0
      statistics.shared = result.data.shared || 0
      statistics.downloads = result.data.downloads || 0
    } else {
      console.error('获取统计数据失败:', result.message)
      // 初始化为0
      statistics.total = 0
      statistics.recent = 0
      statistics.shared = 0
      statistics.downloads = 0
    }
  } catch (error) {
    console.error('获取统计数据错误:', error)
    // 初始化为0
    statistics.total = 0
    statistics.recent = 0
    statistics.shared = 0
    statistics.downloads = 0
  }
}

// 获取文档列表数据
const fetchDocumentList = async () => {
  loading.value = true
  fetchError.value = false
  errorMessage.value = ''
  
  try {
    // 整合搜索参数
    const params = {
      page: queryParams.page,
      pageSize: queryParams.pageSize
    }
    
    // 添加搜索条件
    if (searchForm.code) params.code = searchForm.code
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.type) params.type = searchForm.type
    if (searchForm.category) params.category = searchForm.category
    
    console.log('请求参数:', params)
    
    try {
      console.log('开始调用getDocumentList API...')
      
      const result = await getDocumentList(params)
      console.log('文档列表API响应:', result)
      
      if (result && result.code === 200) {
        // 处理多种可能的响应格式
        let dataArray = [];
        let totalCount = 0;
        
        // 1. 如果result.data直接是数组
        if (Array.isArray(result.data)) {
          dataArray = result.data;
          totalCount = result.total || result.data.length || 0;
        } 
        // 2. 如果result.data包含records数组（常见分页格式）
        else if (result.data && result.data.records && Array.isArray(result.data.records)) {
          dataArray = result.data.records;
          totalCount = result.data.total || 0;
        } 
        // 3. 如果result.data包含content数组（Spring分页格式）
        else if (result.data && result.data.content && Array.isArray(result.data.content)) {
          dataArray = result.data.content;
          totalCount = result.data.totalElements || 0;
        }
        // 4. 如果result.data是对象，尝试寻找其中的数组属性
        else if (result.data && typeof result.data === 'object') {
          const arrayProps = Object.keys(result.data).filter(key => Array.isArray(result.data[key]));
          if (arrayProps.length > 0) {
            console.log('找到数据中的数组属性:', arrayProps);
            dataArray = result.data[arrayProps[0]];
            totalCount = dataArray.length;
          }
        }
        
        // 如果找到了数据，则更新视图
        if (dataArray.length > 0) {
          documentList.value = dataArray;
          total.value = totalCount;
          fetchError.value = false;
        } else {
          console.warn('未从响应中找到有效的数据数组');
          
          // 检查是否是空数据（这可能是合法的，例如没有记录）
          if (result.code === 200) {
            documentList.value = [];
            total.value = 0;
            fetchError.value = false;
          } else {
            documentList.value = [];
            total.value = 0;
            fetchError.value = true;
            errorMessage.value = '没有找到有效的数据格式';
          }
        }
        
        // 处理total字段可能在不同位置的情况
        if (typeof result.total === 'number') {
          total.value = result.total;
        } else if (result.data && typeof result.data.total === 'number') {
          total.value = result.data.total;
        }
        
      } else {
        const errorMsg = result?.message || '获取文档列表失败';
        console.error('API返回错误:', errorMsg, result)
        ElMessage.error(errorMsg)
        documentList.value = []
        total.value = 0
        
        // 设置错误状态
        fetchError.value = true
        errorMessage.value = errorMsg
      }
    } catch (apiError) {
      console.error('API调用过程中发生错误:', apiError)
      fetchError.value = true
      errorMessage.value = apiError.message
      throw apiError
    }
  } catch (error) {
    console.error('获取文档列表错误:', error)
    
    // 设置错误状态
    fetchError.value = true
    errorMessage.value = error.message
    
    // 检查是否是网络错误
    if (error.name === 'TypeError' && error.message.includes('Network')) {
      ElMessage.error({
        message: '网络连接错误，请检查您的网络连接',
        duration: 5000
      })
      
      // 提示使用模拟数据
      ElMessageBox.confirm(
        '无法连接到后端服务，是否加载模拟数据以测试界面？',
        '连接错误',
        {
          confirmButtonText: '加载模拟数据',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        loadMockData();
      }).catch(() => {
        // 用户取消，保持错误状态
      });
    } else if (error.code === 401 || (error.message && error.message.includes('401'))) {
      ElMessage.error({
        message: '身份验证失败，请重新登录',
        duration: 5000
      });
    } else {
      ElMessage.error({
        message: `获取文档列表失败: ${error.message}`,
        duration: 5000
      });
      
      // 也提示使用模拟数据选项
      ElMessageBox.confirm(
        '获取数据失败，是否加载模拟数据以测试界面？',
        '数据错误',
        {
          confirmButtonText: '加载模拟数据',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        loadMockData();
      }).catch(() => {
        // 用户取消，保持错误状态
      });
    }
    
    // 错误处理，确保清空数据避免使用旧数据
    documentList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索方法
const handleSearch = () => {
  queryParams.page = 1 // 重置页码
  fetchDocumentList()
}

const resetSearch = () => {
  // 重置搜索表单
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  queryParams.page = 1
  fetchDocumentList()
}

// 翻页方法
const handlePageChange = (page) => {
  queryParams.page = page
  fetchDocumentList()
}

// 每页数量变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.page = 1 // 切换页大小时重置页码
  fetchDocumentList()
}

// 选择行变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 更新可见列
const updateVisibleColumns = (value) => {
  // 确保至少有一列可见
  if (Array.isArray(value) && value.length > 0) {
    visibleColumnProps.value = value
  } else {
    // 如果没有选择列，设置一个默认列
    visibleColumnProps.value = ['code', 'name']
    ElMessage.warning('至少需要选择一列显示')
  }
}

// 保存列设置
const saveColumnsSettings = () => {
  // 保存到本地存储
  localStorage.setItem('documentListColumns', JSON.stringify(visibleColumnProps.value))
  columnsDialogVisible.value = false
}

// 文档操作
const handleAddDocument = () => {
  router.push('/document/add')
}

const handleImport = () => {
  // 实现批量导入逻辑
  ElMessageBox.prompt('请选择要导入的Excel文件', '批量导入', {
    confirmButtonText: '导入',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '文件不能为空',
    showCancelButton: true,
    showInput: false,
    beforeClose: async (action, instance, done) => {
      if (action === 'confirm') {
        loading.value = true
        try {
          ElMessage.warning('导入功能尚未完全实现，请选择Excel文件进行导入')
        } catch (error) {
          console.error('导入错误:', error)
          ElMessage.error(`导入失败: ${error.message}`)
        } finally {
          loading.value = false
          fetchDocumentList() // 刷新列表
          done()
        }
      } else {
        done()
      }
    }
  }).then(() => {
    // 用户确认导入
  }).catch(() => {
    // 用户取消导入
  })
}

const handleExport = () => {
  // 实现导出逻辑
  if (documentList.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  ElMessageBox.confirm('确定要导出当前文档数据吗？', '导出确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      loading.value = true
      
      // 调用导出API
      const params = {
        ...queryParams,
        code: searchForm.code,
        name: searchForm.name,
        type: searchForm.type,
        category: searchForm.category
      }
      
      // 清除空参数
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null || params[key] === undefined) {
          delete params[key]
        }
      })
      
      const result = await exportDocuments(params)
      
      // 处理blob响应并下载
      const blob = new Blob([result], { type: 'application/vnd.ms-excel' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `文档列表_${new Date().toISOString().split('T')[0]}.xlsx`
      link.click()
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('导出成功')
      loading.value = false
    } catch (error) {
      console.error('导出错误:', error)
      ElMessage.error(`导出失败: ${error.message}`)
      loading.value = false
    }
  }).catch(() => {
    // 用户取消导出
  })
}

const handleView = (row) => {
  router.push(`/document/detail/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/document/edit/${row.id}`)
}

const handleShare = (row) => {
  // 打开分享对话框或跳转到分享页面
  ElMessage.info(`准备分享文档: ${row.name}`)
  // 实际应用中可能是打开分享对话框或调用分享API
}

const handleDownload = async (row) => {
  try {
    ElMessage({
      message: `正在准备下载文档：${row.name}`,
      type: 'info',
      duration: 2000
    })
    
    const result = await downloadDocument(row.id)
    
    // 处理blob响应并下载
    const contentDisposition = result.headers['content-disposition']
    let filename = `${row.name}`
    
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename="?(.+)"?/)
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1]
      }
    }
    
    const blob = new Blob([result.data], { type: result.headers['content-type'] })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(`文档《${row.name}》下载完成`)
  } catch (error) {
    console.error('下载错误:', error)
    ElMessage.error(`下载失败: ${error.message}`)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除文档 "${row.name}" 吗？此操作不可恢复。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        loading.value = true
        
        // 调用真实API
        const result = await deleteDocument(row.id)
        
        if (result.code === 200) {
          ElMessage.success('删除成功')
          fetchDocumentList() // 刷新列表
        } else {
          ElMessage.error(result.message || '删除失败')
        }
      } catch (error) {
        console.error('删除文档错误:', error)
        ElMessage.error(`删除失败: ${error.message}`)
      } finally {
        loading.value = false
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 工具函数
const getStatusType = (status) => {
  const typeMap = {
    'ACTIVE': 'success',
    'ARCHIVED': 'info',
    'SHARED': 'warning',
    'DRAFT': 'info',
    'DELETED': 'danger'
  }
  return typeMap[status] || 'info'
}

const formatStatus = (status) => {
  const statusMap = {
    'ACTIVE': '活跃',
    'ARCHIVED': '归档',
    'SHARED': '已分享',
    'DRAFT': '草稿',
    'DELETED': '已删除'
  }
  return statusMap[status] || status
}

const formatDate = (date) => {
  if (!date) return '未知'
  return new Date(date).toLocaleString()
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  
  return `${size.toFixed(2)} ${units[i]}`
}

// 处理排序变化
const handleSortChange = (column) => {
  // 实现排序逻辑
  console.log('Sort changed:', column)
  // 可以添加排序逻辑
  fetchDocumentList()
}

// 重试加载
const retryFetch = () => {
  fetchDocumentList()
  fetchStatistics()
}

// 组件初始化
onMounted(async () => {
  // 从本地存储恢复列设置
  const savedColumns = localStorage.getItem('documentListColumns')
  if (savedColumns) {
    try {
      const parsedColumns = JSON.parse(savedColumns)
      if (Array.isArray(parsedColumns) && parsedColumns.length > 0) {
        visibleColumnProps.value = parsedColumns
      } else {
        console.warn('保存的列配置非有效数组，使用默认配置')
      }
    } catch (e) {
      console.error('解析保存的列配置错误:', e)
    }
  }
  
  // 加载数据
  await fetchDocumentList()
  
  // 加载统计数据
  await fetchStatistics()
})

// API诊断相关
const diagnosisVisible = ref(false)
const apiTestResult = ref(null)

// 测试API连接
const testApiConnection = async () => {
  try {
    console.log('正在测试API连接...');
    const result = await testDocumentListRequest({
      page: 1,
      pageSize: 5
    });
    
    console.log('直接API测试结果:', result);
    
    if (result.status && result.status >= 200 && result.status < 300) {
      ElMessage.success('API连接测试成功！');
      
      // 显示响应详情
      apiTestResult.value = {
        success: true,
        status: result.status,
        statusText: result.statusText || '成功',
        data: JSON.stringify(result.data || result, null, 2),
        headers: JSON.stringify(result.headers || {}, null, 2),
        url: result.config?.url || apiConfig.baseURL + '/documents'
      };
    } else {
      ElMessage.error(`API连接测试失败: ${result.message || '未知错误'}`);
      
      apiTestResult.value = {
        success: false,
        status: result.status || 0,
        statusText: result.statusText || result.message || '未知错误',
        data: JSON.stringify(result, null, 2),
        error: result.error || {}
      };
    }
  } catch (error) {
    console.error('API测试出错:', error);
    
    ElMessage.error(`API测试错误: ${error.message}`);
    
    apiTestResult.value = {
      success: false,
      status: 0,
      statusText: '请求异常',
      error: error,
      message: error.message
    };
  }
};

// 刷新页面
const reloadPage = () => {
  window.location.reload()
}

// 加载模拟数据
const loadMockData = () => {
  console.log('加载模拟数据以便测试界面功能');
  
  // 生成模拟数据
  const mockData = Array.from({ length: 25 }, (_, index) => ({
    id: index + 1,
    code: `DOC${String(2000 + index).padStart(4, '0')}`,
    name: `测试文档 ${index + 1}`,
    type: ['1', '2', '3', '4', '5'][Math.floor(Math.random() * 5)],
    category: ['1', '2', '3', '4'][Math.floor(Math.random() * 4)],
    status: ['ACTIVE', 'ARCHIVED', 'SHARED', 'DRAFT'][Math.floor(Math.random() * 4)],
    createTime: new Date(Date.now() - Math.random() * 10000000000).toISOString(),
    updateTime: new Date(Date.now() - Math.random() * 5000000000).toISOString(),
    creator: ['张三', '李四', '王五', '赵六'][Math.floor(Math.random() * 4)],
    fileSize: Math.floor(Math.random() * 10000000),
    downloadCount: Math.floor(Math.random() * 50),
    description: `这是一个模拟生成的测试文档，用于界面功能测试。ID: ${index + 1}`
  }));
  
  // 更新视图数据
  documentList.value = mockData;
  total.value = 100; // 模拟总数
  
  // 更新统计数据
  statistics.total = 100;
  statistics.recent = 25;
  statistics.shared = 30;
  statistics.downloads = 255;
  
  // 显示提示
  ElMessage.warning({
    message: '显示的是模拟数据，仅用于界面测试',
    duration: 5000
  });
  
  // 重置加载和错误状态
  loading.value = false;
  fetchError.value = false;
  
  return mockData;
};

// 批量操作
const handleBatchOperation = (command) => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要操作的文档')
    return
  }
  
  switch (command) {
    case 'batchExport':
      handleBatchExport()
      break
    case 'batchDelete':
      handleBatchDelete()
      break
    default:
      break
  }
}

// 批量导出
const handleBatchExport = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要导出的文档')
    return
  }
  
  try {
    loading.value = true
    // 创建导出参数，包含所选ID
    const ids = selectedRows.value.map(row => row.id)
    
    const result = await batchExportDocuments(ids)
    
    // 处理blob响应并下载
    const blob = new Blob([result], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `选中文档_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(`已成功导出${ids.length}个文档`)
    loading.value = false
  } catch (error) {
    console.error('批量导出错误:', error)
    ElMessage.error(`批量导出失败: ${error.message}`)
    loading.value = false
  }
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) return
  
  ElMessageBox.confirm(
    `确定要删除选中的${selectedRows.value.length}个文档吗？此操作不可恢复。`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      loading.value = true
      
      // 获取选中ID
      const ids = selectedRows.value.map(row => row.id)
      
      // 调用批量删除API
      const result = await batchDeleteDocuments(ids)
      
      if (result.code === 200) {
        ElMessage.success(`成功删除${result.data?.count || ids.length}个文档`)
        fetchDocumentList()
      } else {
        ElMessage.error(result.message || '批量删除失败')
      }
    } catch (error) {
      console.error('批量删除错误:', error)
      ElMessage.error(`批量删除失败: ${error.message}`)
    } finally {
      loading.value = false
    }
  }).catch(() => {
    // 用户取消操作
  })
}
</script>

<style scoped>
.document-list-container {
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.error-container {
  margin-bottom: 20px;
}

.error-tips {
  margin-top: 10px;
}

.error-details-collapse {
  margin-top: 10px;
}

.error-details {
  margin-top: 10px;
}

.diagnosis-content {
  padding: 20px;
}

.diagnosis-buttons {
  margin-bottom: 20px;
}

.test-result {
  margin-bottom: 20px;
}

.result-details {
  margin-top: 10px;
}

.response-data {
  margin-top: 10px;
}

.response-headers {
  margin-top: 10px;
}

.error-details {
  margin-top: 10px;
}

.no-test {
  margin-top: 20px;
}
</style> 