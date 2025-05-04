<template>
  <div class="archive-list-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>档案列表</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddArchive">
          <el-icon><Plus /></el-icon>新增档案
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>批量导入
        </el-button>
        <el-button type="warning" @click="handleExport">
          <el-icon><Download /></el-icon>导出档案
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
              <span>档案总数</span>
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
              <span>可借阅</span>
              <el-tag type="success">在库</el-tag>
            </div>
          </template>
          <div class="stat-value">
            <span class="number">{{ statistics.available || 0 }}</span>
            <span class="unit">份</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <span>借出档案</span>
              <el-tag type="warning">外借</el-tag>
            </div>
          </template>
          <div class="stat-value">
            <span class="number">{{ statistics.borrowed || 0 }}</span>
            <span class="unit">份</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <span>本月新增</span>
              <el-tag type="info">统计</el-tag>
            </div>
          </template>
          <div class="stat-value">
            <span class="number">{{ statistics.newThisMonth || 0 }}</span>
            <span class="unit">份</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 搜索筛选区 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="档案编号">
          <el-input v-model="searchForm.code" placeholder="请输入档案编号" clearable />
        </el-form-item>
        <el-form-item label="档案名称">
          <el-input v-model="searchForm.name" placeholder="请输入档案名称" clearable />
        </el-form-item>
        <el-form-item label="档案类型">
          <el-select v-model="searchForm.type" placeholder="请选择档案类型" clearable>
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
          <span>档案列表 ({{ total }})</span>
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
                  <el-button @click="fetchArchiveList">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
              <el-tooltip content="密度">
                <div>
                  <el-dropdown @command="handleSizeChange">
                    <el-button>
                      <el-icon><SetUp /></el-icon>
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
          :sub-title="errorMessage || '获取档案列表失败，请稍后重试'"
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
              <p><strong>API路径：</strong> {{ apiConfig?.baseURL || '/api' }}/archives</p>
            </el-collapse-item>
            <el-collapse-item title="可能的解决方案">
              <ol>
                <li>确认后端服务是否启动，并且可以访问 http://localhost:8000/api/archives</li>
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
        :data="archiveList"
        :size="tableSize"
        border
        highlight-current-row
        @sort-change="handleSortChange"
        @selection-change="handleSelectionChange"
        :empty-text="fetchError ? errorMessage : '暂无档案数据'"
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
              <el-tooltip content="编辑档案">
                <div>
                  <el-button type="primary" link @click="handleEdit(scope.row)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
              <el-tooltip content="申请借阅">
                <div>
                  <el-button type="primary" link :disabled="scope.row.status === 'BORROWED'" @click="handleBorrow(scope.row)">
                    <el-icon><TopRight /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
              <el-tooltip content="下载档案">
                <div>
                  <el-button type="primary" link @click="handleDownload(scope.row)">
                    <el-icon><Download /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
              <el-tooltip content="删除档案">
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
        v-if="!loading && (!archiveList || archiveList.length === 0) && !fetchError" 
        description="暂无档案数据"
      >
        <template #extra>
          <el-button type="primary" @click="handleAddArchive">创建档案</el-button>
        </template>
      </el-empty>
      
      <!-- 错误提示 -->
      <el-alert
        v-if="fetchError"
        :title="errorMessage || '加载数据时发生错误'"
        type="error"
        :closable="false"
        show-icon
        style="margin: 20px 0;"
      >
        <template #default>
          <div class="error-actions" style="margin-top: 10px;">
            <el-button size="small" @click="fetchArchiveList">重试</el-button>
            <el-button size="small" @click="loadMockData">加载模拟数据</el-button>
          </div>
        </template>
      </el-alert>
      
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
              <li>刷新页面，查找archives相关请求</li>
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
   SetUp, View, Edit, TopRight, Delete, ArrowDown, Service, Connection, RefreshRight
} from '@element-plus/icons-vue'
import { 
  getArchiveList, 
  createArchive, 
  updateArchive, 
  deleteArchive, 
  getArchiveStatistics,
  importArchives,
  exportArchives,
  batchExportArchives,
  batchDeleteArchives,
  downloadArchive,
  checkApiConnection,
  getEnvironmentConfig,
  testArchiveListRequest
} from '@/api/archive'
import { apiConfig } from '@/api/config'

const router = useRouter()

// 环境信息
const envMode = computed(() => import.meta.env.MODE)
const currentUrl = computed(() => typeof window !== 'undefined' ? window.location.href : '')

// 档案类型选项
const typeOptions = [
  { value: 'FINANCIAL', label: '财务档案' },
  { value: 'PERSONNEL', label: '人事档案' },
  { value: 'PROJECT', label: '项目档案' },
  { value: 'CONTRACT', label: '合同档案' },
  { value: 'OTHER', label: '其他档案' }
]

// 档案状态选项
const statusOptions = [
  { value: 'AVAILABLE', label: '在库' },
  { value: 'BORROWED', label: '借出' },
  { value: 'PROCESSING', label: '处理中' }
]

// 所有列定义
const allColumns = [
  { key: 'id', label: 'ID', prop: 'id', width: '80', sortable: true },
  { key: 'code', label: '档案编号', prop: 'code', width: '150', sortable: true },
  { key: 'name', label: '档案名称', prop: 'name', width: '200', sortable: true },
  { key: 'type', label: '档案类型', prop: 'type', width: '120', sortable: true },
  { key: 'location', label: '存放位置', prop: 'location', width: '120', sortable: true },
  { key: 'responsible', label: '负责人', prop: 'responsible', width: '120', sortable: true },
  { key: 'createTime', label: '创建时间', prop: 'createTime', width: '150', sortable: true },
  { key: 'updateTime', label: '更新时间', prop: 'updateTime', width: '150', sortable: true },
  { key: 'fileSize', label: '文件大小', prop: 'fileSize', width: '120', sortable: true },
  { key: 'borrowCount', label: '借阅次数', prop: 'borrowCount', width: '100', sortable: true },
  { key: 'status', label: '状态', prop: 'status', width: '100', sortable: true }
]

// 搜索表单
const searchForm = reactive({
  code: '',
  name: '',
  type: '',
  status: ''
})

// 表格状态
const loading = ref(false)
const archiveList = ref([])
const total = ref(0)
const tableSize = ref('default')
const selectedRows = ref([])
const visibleColumnProps = ref(['code', 'name', 'type', 'location', 'createTime', 'status'])
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
  available: 0,
  borrowed: 0,
  newThisMonth: 0
})

// 错误状态
const fetchError = ref(false)
const errorMessage = ref('')

// 获取统计数据
const fetchStatistics = async () => {
  try {
    // 调用API获取统计数据
    const result = await getArchiveStatistics()
    
    if (result.code === 200 && result.data) {
      statistics.total = result.data.total || 0
      statistics.available = result.data.available || 0
      statistics.borrowed = result.data.borrowed || 0
      statistics.newThisMonth = result.data.newThisMonth || 0
    } else {
      console.error('获取统计数据失败:', result.message)
      // 初始化为0
      statistics.total = 0
      statistics.available = 0
      statistics.borrowed = 0
      statistics.newThisMonth = 0
    }
  } catch (error) {
    console.error('获取统计数据错误:', error)
    // 初始化为0
    statistics.total = 0
    statistics.available = 0
    statistics.borrowed = 0
    statistics.newThisMonth = 0
  }
}

// 获取档案列表数据
const fetchArchiveList = async () => {
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
    if (searchForm.status) params.status = searchForm.status
    
    console.log('请求参数:', params)
    
    // 使用try-catch包裹API调用，确保错误被正确捕获
    try {
      console.log('开始调用getArchiveList API...')
      console.log('请求URL:', `/archives`)
      
      // 调试当前环境变量和API配置
      console.log('环境变量:', import.meta.env)
      console.log('API基础URL:', import.meta.env.VITE_BASE_API || '/api')
      
      const result = await getArchiveList(params)
      console.log('档案列表API响应 (完整):', JSON.stringify(result))
      console.log('响应状态码:', result.code)
      console.log('响应消息:', result.message)
      console.log('响应数据类型:', typeof result.data)
      console.log('是否为数组:', Array.isArray(result.data))
      
      if (result.data) {
        console.log('数据前5项:', JSON.stringify(result.data.slice?.(0, 5) || result.data))
        
        // 尝试识别响应中的有效数据结构
        const possibleArrayFields = Object.keys(result).filter(key => Array.isArray(result[key]))
        console.log('响应中的数组字段:', possibleArrayFields)
        
        if (typeof result.data === 'object' && result.data !== null) {
          console.log('data对象的属性:', Object.keys(result.data))
          Object.keys(result.data).forEach(key => {
            console.log(`data.${key}的类型:`, typeof result.data[key], 
                       `是否为数组:`, Array.isArray(result.data[key]))
            if (Array.isArray(result.data[key])) {
              console.log(`data.${key}数组长度:`, result.data[key].length)
            }
          })
        }
      }
      
      // 检查响应中是否包含错误信息
      if (result && result.error) {
        throw new Error(`API请求错误: ${result.message || '未知错误'}`)
      }
      
      // 确保result存在且有正确的结构
      if (result && result.code === 200) {
        // 记录数据类型
        if (result.data) {
          console.log('API返回数据类型:', typeof result.data, Array.isArray(result.data) ? '数组' : '非数组')
        }
        
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
        // 5. 如果result本身包含数组属性
        else {
          const resultArrayProps = Object.keys(result).filter(key => Array.isArray(result[key]));
          if (resultArrayProps.length > 0) {
            console.log('在响应根级别找到数组属性:', resultArrayProps);
            dataArray = result[resultArrayProps[0]];
            totalCount = dataArray.length;
          }
        }
        
        // 如果找到了数据，则更新视图
        if (dataArray.length > 0) {
          archiveList.value = dataArray;
          total.value = totalCount;
          fetchError.value = false;
          console.log('成功解析到数据数组，数量:', dataArray.length);
        } else {
          console.warn('未从响应中找到有效的数据数组');
          
          // 检查是否是空数据（这可能是合法的，例如没有记录）
          if (result.code === 200) {
            archiveList.value = [];
            total.value = 0;
            // 不设置错误，因为这可能只是没有数据
            fetchError.value = false;
            console.log('API返回成功，但没有数据记录');
          } else {
            archiveList.value = [];
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
        
        // 日志记录响应数据的结构
        console.log('数据处理结果:', { 
          archiveListLength: archiveList.value.length,
          totalValue: total.value,
          hasError: fetchError.value,
          errorMessage: errorMessage.value
        });
        
      } else {
        const errorMsg = result?.message || '获取档案列表失败';
        console.error('API返回错误:', errorMsg, result)
        ElMessage.error(errorMsg)
        archiveList.value = []
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
    console.error('获取档案列表错误:', error)
    
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
      // 可以添加重定向到登录页的逻辑
    } else {
      ElMessage.error({
        message: `获取档案列表失败: ${error.message}`,
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
    archiveList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索方法
const handleSearch = () => {
  queryParams.page = 1 // 重置页码
  fetchArchiveList()
}

const resetSearch = () => {
  // 重置搜索表单
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  queryParams.page = 1
  fetchArchiveList()
}

// 翻页方法
const handlePageChange = (page) => {
  queryParams.page = page
  fetchArchiveList()
}

// 每页数量变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.page = 1 // 切换页大小时重置页码
  fetchArchiveList()
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
  localStorage.setItem('archiveListColumns', JSON.stringify(visibleColumnProps.value))
  columnsDialogVisible.value = false
}

// 档案操作
const handleAddArchive = () => {
  router.push('/archive/add')
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
          // 这里应该实现文件选择逻辑
          // const fileInput = document.createElement('input')
          // fileInput.type = 'file'
          // fileInput.accept = '.xlsx,.xls,.csv'
          // fileInput.click()
          // fileInput.onchange = async (e) => {
          //   const file = e.target.files[0]
          //   if (!file) {
          //     ElMessage.warning('请选择文件')
          //     return
          //   }
          //   
          //   const formData = new FormData()
          //   formData.append('file', file)
          //   const result = await importArchives(formData)
          //   
          //   if (result.code === 200) {
          //     ElMessage.success(`导入成功，已添加${result.data.count}条记录`)
          //     fetchArchiveList() // 刷新列表
          //   } else {
          //     ElMessage.error(result.message || '导入失败')
          //   }
          // }
          
          // 暂时使用提示
          ElMessage.warning('导入功能尚未完全实现，请选择Excel文件进行导入')
        } catch (error) {
          console.error('导入错误:', error)
          ElMessage.error(`导入失败: ${error.message}`)
        } finally {
          loading.value = false
          fetchArchiveList() // 刷新列表
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
  if (archiveList.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  ElMessageBox.confirm('确定要导出当前档案数据吗？', '导出确认', {
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
        status: searchForm.status
      }
      
      // 清除空参数
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null || params[key] === undefined) {
          delete params[key]
        }
      })
      
      const result = await exportArchives(params)
      
      // 处理blob响应并下载
      const blob = new Blob([result], { type: 'application/vnd.ms-excel' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `档案列表_${new Date().toISOString().split('T')[0]}.xlsx`
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
  router.push(`/archive/detail/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/archive/edit/${row.id}`)
}

const handleBorrow = (row) => {
  router.push({
    path: '/archive/borrow',
    query: { archiveId: row.id }
  })
}

const handleDownload = async (row) => {
  try {
    ElMessage({
      message: `正在准备下载档案：${row.name}`,
      type: 'info',
      duration: 2000
    })
    
    const result = await downloadArchive(row.id)
    
    // 处理blob响应并下载
    const contentDisposition = result.headers['content-disposition']
    let filename = `${row.code}_${row.name}.pdf`
    
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
    
    ElMessage.success(`档案《${row.name}》下载完成`)
  } catch (error) {
    console.error('下载错误:', error)
    ElMessage.error(`下载失败: ${error.message}`)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除档案 "${row.name}" 吗？此操作不可恢复。`,
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
        const result = await deleteArchive(row.id)
        
        if (result.code === 200) {
          ElMessage.success('删除成功')
          fetchArchiveList() // 刷新列表
        } else {
          ElMessage.error(result.message || '删除失败')
        }
      } catch (error) {
        console.error('删除档案错误:', error)
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
    'AVAILABLE': 'success',
    'BORROWED': 'warning',
    'PROCESSING': 'info',
    'ARCHIVED': 'info',
    'DAMAGED': 'danger'
  }
  return typeMap[status] || 'info'
}

const formatStatus = (status) => {
  const statusMap = {
    'AVAILABLE': '在库',
    'BORROWED': '借出',
    'PROCESSING': '处理中',
    'ARCHIVED': '归档',
    'DAMAGED': '损坏'
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
  fetchArchiveList()
}

// 批量操作
const handleBatchOperation = (command) => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要操作的档案')
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
    ElMessage.warning('请先选择要导出的档案')
    return
  }
  
  try {
    loading.value = true
    // 创建导出参数，包含所选ID
    const ids = selectedRows.value.map(row => row.id)
    
    const result = await batchExportArchives(ids)
    
    // 处理blob响应并下载
    const blob = new Blob([result], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `选中档案_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(`已成功导出${ids.length}个档案`)
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
    `确定要删除选中的${selectedRows.value.length}个档案吗？此操作不可恢复。`,
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
      const result = await batchDeleteArchives(ids)
      
      if (result.code === 200) {
        ElMessage.success(`成功删除${result.data?.count || ids.length}个档案`)
        fetchArchiveList()
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

// 重试加载
const retryFetch = () => {
  fetchArchiveList()
  fetchStatistics()
}

// 组件初始化
onMounted(async () => {
  // 从本地存储恢复列设置
  const savedColumns = localStorage.getItem('archiveListColumns')
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
  
  // 设置一个错误处理包装函数
  const safeCall = async (fn, fallbackMsg) => {
    try {
      await fn()
    } catch (err) {
      console.error(fallbackMsg, err)
      ElMessage.error(fallbackMsg)
    }
  }
  
  // 加载数据
  await safeCall(fetchArchiveList, '加载档案列表失败')
  
  // 加载统计数据
  await safeCall(fetchStatistics, '加载统计数据失败')
})

// API诊断相关
const diagnosisVisible = ref(false)
const diagnosisLoading = ref(false)
const diagnosisError = ref(null)
const diagnosis = reactive({
  env: import.meta.env.MODE || process.env.NODE_ENV || '未知',
  baseUrl: import.meta.env.VITE_BASE_API || '未设置',
  apiVersion: import.meta.env.VITE_API_VERSION || '未知',
  connectionStatus: 'unknown',
  errorMessage: '',
  showResponse: false,
  responseText: '',
  tokenStatus: false,
  tokenExpiry: '',
  networkInfo: null
})

// 检查API连接状态
const checkApiStatus = async () => {
  diagnosisLoading.value = true
  diagnosisError.value = null
  diagnosis.errorMessage = ''
  
  try {
    // 先获取环境配置
    const config = await getEnvironmentConfig()
    if (config && config.data) {
      diagnosis.env = config.data.env || '未知'
      diagnosis.baseUrl = config.data.baseUrl || '未设置'
      diagnosis.apiVersion = config.data.apiVersion || '未知'
    }
    
    // 检查token状态
    const token = localStorage.getItem('token')
    diagnosis.tokenStatus = !!token
    
    if (token) {
      try {
        // 尝试解析JWT token以获取过期时间
        const tokenParts = token.split('.')
        if (tokenParts.length === 3) {
          const payload = JSON.parse(atob(tokenParts[1]))
          if (payload.exp) {
            const expDate = new Date(payload.exp * 1000)
            diagnosis.tokenExpiry = expDate.toLocaleString()
          }
        }
      } catch (e) {
        console.error('解析token出错:', e)
        diagnosis.tokenStatus = false
        diagnosis.tokenExpiry = '解析失败'
      }
    }
    
    // 检查API连接
    const result = await checkApiConnection()
    
    if (result && result.code === 200) {
      diagnosis.connectionStatus = 'success'
      diagnosis.errorMessage = ''
    } else {
      diagnosis.connectionStatus = 'error'
      diagnosis.errorMessage = result?.message || '未知错误'
    }
    
    // 显示完整响应
    diagnosis.showResponse = true
    diagnosis.responseText = JSON.stringify(result, null, 2)
    diagnosis.networkInfo = {
      status: result?.status || (result?.code === 200 ? 200 : 'unknown'),
      requestId: result?.requestId || 'unknown',
      time: new Date().toISOString()
    }
  } catch (error) {
    console.error('诊断API状态错误:', error)
    diagnosis.connectionStatus = 'error'
    diagnosis.errorMessage = error?.message || '检查过程中发生错误'
    diagnosis.showResponse = true
    diagnosis.responseText = JSON.stringify({error: error?.toString(), stack: error?.stack}, null, 2)
    diagnosisError.value = `API连接诊断失败: ${error?.message || '未知错误'}`
  } finally {
    diagnosisLoading.value = false
  }
}

// 测试API请求函数的增强版本
const testApiRequest = async () => {
  diagnosisLoading.value = true
  diagnosisError.value = null
  diagnosis.connectionStatus = 'unknown'
  diagnosis.errorMessage = ''
  
  try {
    // 使用简单参数测试
    const testParams = { page: 1, pageSize: 5 }
    console.log('测试API请求参数:', testParams)
    
    // 检查token并添加到日志
    const token = localStorage.getItem('token')
    console.log('当前token状态:', token ? '已设置' : '未设置')
    
    const result = await testArchiveListRequest(testParams)
    
    diagnosis.showResponse = true
    diagnosis.responseText = JSON.stringify(result, null, 2)
    diagnosis.networkInfo = {
      status: result?.status || (result?.code === 200 ? 200 : 'unknown'),
      requestId: result?.requestId || 'unknown',
      time: new Date().toISOString()
    }
    
    if (result && result.code === 200) {
      diagnosis.connectionStatus = 'success'
      diagnosis.errorMessage = ''
      // 检查数据结构
      const data = result.data
      if (data) {
        let structureAnalysis = '\n\n数据结构分析:\n'
        
        if (Array.isArray(data)) {
          structureAnalysis += '✅ 返回数据是数组格式，可以直接用于表格渲染\n'
          structureAnalysis += `- 数组长度: ${data.length}\n`
          if (data.length > 0) {
            structureAnalysis += `- 数组中第一个元素示例: ${JSON.stringify(data[0], null, 2)}`
          }
        } else if (data.records && Array.isArray(data.records)) {
          structureAnalysis += '✅ 返回数据包含records数组字段，可以用于表格渲染\n'
          structureAnalysis += `- 数组长度: ${data.records.length}\n`
          structureAnalysis += `- total: ${data.total || '未提供'}\n`
          if (data.records.length > 0) {
            structureAnalysis += `- 示例元素: ${JSON.stringify(data.records[0], null, 2)}`
          }
        } else if (data.content && Array.isArray(data.content)) {
          structureAnalysis += '✅ 返回数据包含content数组字段 (Spring Data分页格式)，可以用于表格渲染\n'
          structureAnalysis += `- 数组长度: ${data.content.length}\n`
          structureAnalysis += `- totalElements: ${data.totalElements || '未提供'}\n`
          if (data.content.length > 0) {
            structureAnalysis += `- 示例元素: ${JSON.stringify(data.content[0], null, 2)}`
          }
        } else {
          structureAnalysis += '⚠️ 警告：返回数据格式无法直接用于表格渲染，需要调整API响应格式或前端处理逻辑\n'
          structureAnalysis += `- 返回数据类型: ${typeof data}\n`
          structureAnalysis += `- 返回数据字段: ${Object.keys(data).join(', ')}\n`
          
          // 尝试查找对象中的数组字段
          const arrayFields = Object.keys(data).filter(key => Array.isArray(data[key]))
          if (arrayFields.length > 0) {
            structureAnalysis += `- 发现可能的数组字段: ${arrayFields.join(', ')}\n`
            structureAnalysis += `- 建议使用 data.${arrayFields[0]} 作为表格数据源`
          }
        }
        
        diagnosis.responseText += structureAnalysis
      }
    } else {
      diagnosis.connectionStatus = 'error'
      diagnosis.errorMessage = result?.message || '请求失败'
      
      // 尝试分析错误类型
      if (result.error && result.error.response && result.error.response.status) {
        const status = result.error.response.status
        if (status === 401) {
          diagnosis.responseText += '\n\n⚠️ 身份验证错误 (401)，请检查token是否有效，可能需要重新登录'
          diagnosisError.value = '身份验证失败，请重新登录'
        } else if (status === 403) {
          diagnosis.responseText += '\n\n⚠️ 权限错误 (403)，您没有访问此资源的权限'
          diagnosisError.value = '权限不足，无法访问此资源'
        } else if (status === 404) {
          diagnosis.responseText += '\n\n⚠️ 资源不存在 (404)，请检查API路径是否正确'
          diagnosisError.value = 'API端点不存在，请检查配置'
        } else if (status >= 500) {
          diagnosis.responseText += '\n\n⚠️ 服务器错误，请联系后端开发人员'
          diagnosisError.value = '服务器内部错误'
        }
      } else if (result.error && result.error.code === 'ECONNREFUSED') {
        diagnosis.responseText += '\n\n⚠️ 连接被拒绝，服务器可能未启动或不可达'
        diagnosisError.value = '无法连接到服务器，请确认后端服务是否运行'
      } else if (result.error && result.error.message && result.error.message.includes('timeout')) {
        diagnosis.responseText += '\n\n⚠️ 请求超时，服务器响应时间过长'
        diagnosisError.value = '请求超时，请检查网络连接和服务器状态'
      }
    }
  } catch (error) {
    console.error('测试API请求错误:', error)
    diagnosis.connectionStatus = 'error'
    diagnosis.errorMessage = error?.message || '请求过程中发生错误'
    diagnosis.showResponse = true
    diagnosis.responseText = JSON.stringify({
      error: error?.toString(),
      stack: error?.stack,
      name: error?.name
    }, null, 2)
    diagnosisError.value = `API测试失败: ${error?.message || '未知错误'}`
  } finally {
    diagnosisLoading.value = false
  }
}

// 清除本地存储
const clearLocalStorage = () => {
  localStorage.clear()
  ElMessage.success('本地存储已清除，请刷新页面重试')
  diagnosis.tokenStatus = false
  diagnosis.tokenExpiry = ''
}

// 刷新页面
const reloadPage = () => {
  window.location.reload()
}

// 新增 - 尝试自动修复常见问题
const fixCommonIssues = async () => {
  diagnosisLoading.value = true
  try {
    // 1. 清除token和用户数据（解决认证问题）
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    
    // 2. 清除缓存的列设置
    localStorage.removeItem('archiveListColumns')
    
    // 3. 重置组件状态
    fetchError.value = false
    errorMessage.value = ''
    archiveList.value = []
    total.value = 0
    queryParams.page = 1
    queryParams.pageSize = 20
    
    // 4. 重新获取令牌 (如果有自动登录功能)
    // 如果有自动登录API，可以在这里调用
    
    // 5. 等待一下并重新测试连接
    await new Promise(resolve => setTimeout(resolve, 1000))
    await checkApiStatus()
    
    ElMessage.success('修复操作已完成，请刷新页面并重试')
  } catch (error) {
    console.error('自动修复过程中出错:', error)
    ElMessage.error(`修复失败: ${error.message}`)
    diagnosisError.value = `自动修复失败: ${error.message}`
  } finally {
    diagnosisLoading.value = false
  }
}

// 测试API连接
const testApiConnection = async () => {
  try {
    console.log('正在测试API连接...');
    const result = await testArchiveListRequest({
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
        url: result.config?.url || apiConfig.baseURL + '/archives'
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

// 追加API诊断方法导入
// 用于存储API测试结果
const apiTestResult = ref(null);

// 加载模拟数据
const loadMockData = () => {
  console.log('加载模拟数据以便测试界面功能');
  
  // 生成模拟数据
  const mockData = Array.from({ length: 25 }, (_, index) => ({
    id: index + 1,
    code: `ARC${String(2000 + index).padStart(4, '0')}`,
    name: `测试档案 ${index + 1}`,
    type: ['FINANCIAL', 'PERSONNEL', 'PROJECT', 'CONTRACT', 'OTHER'][Math.floor(Math.random() * 5)],
    status: ['AVAILABLE', 'BORROWED', 'PROCESSING'][Math.floor(Math.random() * 3)],
    location: `${String.fromCharCode(65 + Math.floor(index / 5))}-${String(index % 5).padStart(2, '0')}`,
    createTime: new Date(Date.now() - Math.random() * 10000000000).toISOString(),
    updateTime: new Date(Date.now() - Math.random() * 5000000000).toISOString(),
    responsible: ['张三', '李四', '王五', '赵六'][Math.floor(Math.random() * 4)],
    fileSize: Math.floor(Math.random() * 10000000),
    borrowCount: Math.floor(Math.random() * 50),
    description: `这是一个模拟生成的测试档案，用于界面功能测试。ID: ${index + 1}`
  }));
  
  // 更新视图数据
  archiveList.value = mockData;
  total.value = 100; // 模拟总数
  
  // 更新统计数据
  statistics.total = 100;
  statistics.available = 70;
  statistics.borrowed = 20;
  statistics.newThisMonth = 15;
  
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
</script>

<style scoped>
.archive-list-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background-color: white;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* Stats cards styling */
.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  height: 100%;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
  border: none;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.stat-value {
  display: flex;
  justify-content: center;
  align-items: baseline;
  padding: 20px 0;
}

.stat-value .number {
  font-size: 32px;
  font-weight: bold;
  margin-right: 8px;
  color: #409EFF;
  line-height: 1;
}

.stat-value .unit {
  font-size: 14px;
  color: #909399;
}

/* Filter and table containers */
.filter-container {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: none;
}

.search-form {
  padding: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.table-container {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: none;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Pagination styling */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 8px;
}

/* Dialog styling */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  gap: 12px;
}

/* Diagnosis section */
.diagnosis-content {
  padding: 20px;
}

.diagnosis-buttons {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.test-result {
  margin-bottom: 24px;
}

.result-details {
  margin-top: 16px;
}

.response-data,
.response-headers,
.error-details {
  max-height: 300px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  border: 1px solid #ebeef5;
}

.error-details {
  background-color: #fef0f0;
  border-color: #fbc4c4;
}

.no-test {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

/* Error container styling */
.error-container {
  padding: 30px 20px;
  text-align: center;
  margin: 2rem 0;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
  border: 1px solid #fbc4c4;
}

.error-container .el-result {
  padding: 20px 0;
}

.error-tips {
  color: #909399;
  font-size: 14px;
  margin: 16px 0;
  text-align: center;
  line-height: 1.6;
}

.error-details-collapse {
  max-width: 600px;
  margin: 0 auto;
}

/* Empty state styling */
.el-empty {
  padding: 40px 0;
}

/* Table row hover effect */
:deep(.el-table__row:hover) {
  background-color: #ecf5ff !important;
}

/* Button group styling */
:deep(.el-button-group) {
  display: flex;
}

/* Add transitions for smooth hover effects */
:deep(.el-card),
:deep(.el-button),
:deep(.el-table) {
  transition: all 0.3s ease;
}
</style> 