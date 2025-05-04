<template>
  <div class="workflow-history-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>审批历史</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshList">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
        <el-button type="success" @click="exportHistory">
          <el-icon><Download /></el-icon>导出
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
    
    <!-- 搜索筛选区 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程名称">
          <el-input v-model="searchForm.processName" placeholder="请输入流程名称" clearable />
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="searchForm.initiator" placeholder="请输入申请人" clearable />
        </el-form-item>
        <el-form-item label="流程类型">
          <el-select v-model="searchForm.processType" placeholder="请选择流程类型" clearable>
            <el-option v-for="item in processTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
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
        <el-form-item label="结果">
          <el-select v-model="searchForm.result" placeholder="请选择结果" clearable>
            <el-option label="通过" value="approved" />
            <el-option label="拒绝" value="rejected" />
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
    
    <!-- 历史记录表格 -->
    <el-card class="table-container">
      <template #header>
        <div class="table-header">
          <span>审批历史记录 ({{ total }})</span>
          <div class="header-actions">
            <el-button-group>
              <el-tooltip content="刷新">
                <div>
                  <el-button @click="refreshList">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </div>
              </el-tooltip>
              <el-tooltip content="导出">
                <div>
                  <el-button @click="exportHistory">
                    <el-icon><Download /></el-icon>
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
          :sub-title="errorMessage || '获取审批历史记录失败，请稍后重试'"
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
              <p><strong>API路径：</strong> {{ apiConfig?.baseURL || '/api' }}/workflows/history</p>
            </el-collapse-item>
            <el-collapse-item title="可能的解决方案">
              <ol>
                <li>确认后端服务是否启动，并且可以访问 http://localhost:8000/api/workflows/history</li>
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
        :data="historyList"
        border
        style="width: 100%"
        :empty-text="fetchError ? errorMessage : '暂无审批历史记录'"
      >
        <el-table-column prop="processName" label="流程名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="businessKey" label="业务标识" width="120" show-overflow-tooltip />
        <el-table-column prop="processType" label="流程类型" width="120">
          <template #default="scope">
            {{ getProcessTypeName(scope.row.processType) }}
          </template>
        </el-table-column>
        <el-table-column prop="initiator" label="申请人" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="160" sortable />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="duration" label="耗时" width="120">
          <template #default="scope">
            {{ formatDuration(scope.row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.result === 'approved' ? 'success' : 'danger'">
              {{ scope.row.result === 'approved' ? '通过' : '拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空数据提示 -->
      <el-empty 
        v-if="!loading && (!historyList || historyList.length === 0) && !fetchError" 
        description="暂无审批历史记录"
      />
      
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
              <li>刷新页面，查找workflow相关请求</li>
              <li>点击请求查看详情，包括Headers和Response</li>
            </ol>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download, Connection, RefreshRight, Service } from '@element-plus/icons-vue'
import { 
  getWorkflowHistory, 
  exportWorkflowHistory,
  testWorkflowHistoryRequest 
} from '@/api/workflow'
import { apiConfig } from '@/api/config'

const router = useRouter()

// 环境信息
const envMode = computed(() => import.meta.env.MODE)
const currentUrl = computed(() => typeof window !== 'undefined' ? window.location.href : '')

// 流程类型选项
const processTypeOptions = [
  { value: '1', label: '档案借阅' },
  { value: '2', label: '档案归还' },
  { value: '3', label: '档案销毁' },
  { value: '4', label: '文档审批' }
]

// 搜索表单
const searchForm = reactive({
  processName: '',
  initiator: '',
  processType: '',
  dateRange: [],
  result: ''
})

// 表格数据
const loading = ref(false)
const historyList = ref([])
const total = ref(0)
const fetchError = ref(false)
const errorMessage = ref('')

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  sortBy: 'startTime',
  sortOrder: 'desc'
})

// 获取历史记录列表
const fetchHistoryList = async () => {
  loading.value = true
  fetchError.value = false
  errorMessage.value = ''
  
  try {
    // 整合搜索参数
    const params = {
      page: queryParams.page,
      pageSize: queryParams.pageSize,
      sortBy: queryParams.sortBy,
      sortOrder: queryParams.sortOrder
    }
    
    // 添加搜索条件
    if (searchForm.processName) params.processName = searchForm.processName
    if (searchForm.initiator) params.initiator = searchForm.initiator
    if (searchForm.processType) params.processType = searchForm.processType
    if (searchForm.result) params.result = searchForm.result
    
    // 处理日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    console.log('请求参数:', params)
    
    try {
      console.log('开始调用getWorkflowHistory API...')
      
      const result = await getWorkflowHistory(params)
      console.log('工作流历史API响应:', result)
      
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
          historyList.value = dataArray;
          total.value = totalCount;
          fetchError.value = false;
        } else {
          console.warn('未从响应中找到有效的数据数组');
          
          // 检查是否是空数据（这可能是合法的，例如没有记录）
          if (result.code === 200) {
            historyList.value = [];
            total.value = 0;
            fetchError.value = false;
          } else {
            historyList.value = [];
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
        const errorMsg = result?.message || '获取工作流历史记录失败';
        console.error('API返回错误:', errorMsg, result)
        ElMessage.error(errorMsg)
        historyList.value = []
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
    console.error('获取工作流历史记录错误:', error)
    
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
        message: `获取工作流历史记录失败: ${error.message}`,
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
    historyList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchHistoryList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.processName = ''
  searchForm.initiator = ''
  searchForm.processType = ''
  searchForm.dateRange = []
  searchForm.result = ''
  queryParams.page = 1
  fetchHistoryList()
}

// 刷新列表
const refreshList = () => {
  fetchHistoryList()
  ElMessage.success('刷新成功')
}

// 导出历史记录
const exportHistory = async () => {
  try {
    loading.value = true
    ElMessage.info('正在导出审批历史记录，请稍候...')
    
    // 整合参数
    const params = {
      processName: searchForm.processName,
      initiator: searchForm.initiator,
      processType: searchForm.processType,
      result: searchForm.result
    }
    
    // 处理日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    // 清除空参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    const result = await exportWorkflowHistory(params)
    
    // 处理blob响应并下载
    const blob = new Blob([result], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `审批历史记录_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出错误:', error)
    ElMessage.error(`导出失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 页面大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.page = 1 // 切换页大小时重置页码
  fetchHistoryList()
}

// 页码变化
const handlePageChange = (page) => {
  queryParams.page = page
  fetchHistoryList()
}

// 获取流程类型名称
const getProcessTypeName = (type) => {
  const option = processTypeOptions.find(opt => opt.value === type)
  return option ? option.label : '未知类型'
}

// 格式化耗时
const formatDuration = (duration) => {
  if (!duration && duration !== 0) return '未知'
  
  // 将毫秒转换为更友好的格式
  const seconds = Math.floor(duration / 1000)
  if (seconds < 60) return `${seconds}秒`
  
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}分${seconds % 60}秒`
  
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时${minutes % 60}分`
  
  const days = Math.floor(hours / 24)
  return `${days}天${hours % 24}小时`
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/workflow/detail/${row.id}`)
}

// API诊断相关
const diagnosisVisible = ref(false)
const apiTestResult = ref(null)

// 测试API连接
const testApiConnection = async () => {
  try {
    console.log('正在测试API连接...');
    const result = await testWorkflowHistoryRequest({
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
        url: result.config?.url || apiConfig.baseURL + '/workflows/history'
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
  const mockData = [
    {
      id: '1',
      processName: '档案借阅申请',
      businessKey: 'BORROW-20230310-001',
      processType: '1',
      initiator: '张三',
      startTime: '2023-03-10 09:30:00',
      endTime: '2023-03-10 14:15:00',
      duration: 17100000, // 毫秒
      result: 'approved'
    },
    {
      id: '2',
      processName: '技术文档审批',
      businessKey: 'DOC-20230305-002',
      processType: '4',
      initiator: '李四',
      startTime: '2023-03-05 10:20:00',
      endTime: '2023-03-06 11:30:00',
      duration: 90600000, // 毫秒
      result: 'approved'
    },
    {
      id: '3',
      processName: '档案销毁申请',
      businessKey: 'DESTROY-20230301-001',
      processType: '3',
      initiator: '王五',
      startTime: '2023-03-01 14:00:00',
      endTime: '2023-03-03 16:45:00',
      duration: 194700000, // 毫秒
      result: 'rejected'
    },
    {
      id: '4',
      processName: '档案归还流程',
      businessKey: 'RETURN-20230225-001',
      processType: '2',
      initiator: '张三',
      startTime: '2023-02-25 09:15:00',
      endTime: '2023-02-25 11:20:00',
      duration: 7500000, // 毫秒
      result: 'approved'
    }
  ];
  
  // 添加更多模拟数据
  for (let i = 5; i <= 25; i++) {
    const processTypes = ['1', '2', '3', '4'];
    const processType = processTypes[Math.floor(Math.random() * processTypes.length)];
    const initiators = ['张三', '李四', '王五', '赵六', '钱七'];
    const initiator = initiators[Math.floor(Math.random() * initiators.length)];
    const result = Math.random() > 0.3 ? 'approved' : 'rejected';
    
    // 计算日期，确保按时间排序
    const days = Math.floor(Math.random() * 30);
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - days);
    
    const duration = Math.floor(Math.random() * 259200000); // 0-3天的毫秒数
    const endDate = new Date(startDate.getTime() + duration);
    
    let processName = '';
    let businessKey = '';
    
    switch (processType) {
      case '1':
        processName = '档案借阅申请';
        businessKey = `BORROW-${startDate.getFullYear()}${String(startDate.getMonth() + 1).padStart(2, '0')}${String(startDate.getDate()).padStart(2, '0')}-${String(i).padStart(3, '0')}`;
        break;
      case '2':
        processName = '档案归还流程';
        businessKey = `RETURN-${startDate.getFullYear()}${String(startDate.getMonth() + 1).padStart(2, '0')}${String(startDate.getDate()).padStart(2, '0')}-${String(i).padStart(3, '0')}`;
        break;
      case '3':
        processName = '档案销毁申请';
        businessKey = `DESTROY-${startDate.getFullYear()}${String(startDate.getMonth() + 1).padStart(2, '0')}${String(startDate.getDate()).padStart(2, '0')}-${String(i).padStart(3, '0')}`;
        break;
      case '4':
        processName = '文档审批流程';
        businessKey = `DOC-${startDate.getFullYear()}${String(startDate.getMonth() + 1).padStart(2, '0')}${String(startDate.getDate()).padStart(2, '0')}-${String(i).padStart(3, '0')}`;
        break;
    }
    
    mockData.push({
      id: String(i),
      processName,
      businessKey,
      processType,
      initiator,
      startTime: startDate.toLocaleString(),
      endTime: endDate.toLocaleString(),
      duration,
      result
    });
  }
  
  // 按开始时间倒序排序
  mockData.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
  
  // 更新视图数据
  historyList.value = mockData.slice(0, 10); // 只显示前10条
  total.value = mockData.length;
  
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

// 重试加载
const retryFetch = () => {
  fetchHistoryList()
}

// 组件初始化
onMounted(async () => {
  try {
    await fetchHistoryList();
  } catch (error) {
    console.error('初始化加载失败:', error);
    // 错误已经在fetchHistoryList中处理
  }
});
</script>

<style scoped>
.workflow-history-container {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.filter-container {
  margin-bottom: 24px;
}

.table-container {
  margin-bottom: 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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

.diagnosis-content {
  padding: 20px;
}

.diagnosis-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.test-result {
  margin-bottom: 20px;
}

.result-details {
  margin-top: 10px;
}

.response-data,
.response-headers,
.error-details {
  max-height: 300px;
  overflow: auto;
  white-space: pre-wrap;
  font-family: monospace;
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.no-test {
  text-align: center;
  padding: 40px;
  color: #909399;
}
</style> 