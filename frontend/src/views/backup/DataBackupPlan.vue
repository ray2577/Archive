<template>
  <div class="backup-plan-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>备份计划</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddPlan">
          <el-icon><Plus /></el-icon>新建计划
        </el-button>
      </div>
    </div>
    
    <!-- 备份计划列表 -->
    <el-card class="plan-list-card">
      <template #header>
        <div class="card-header">
          <span>备份计划列表</span>
          <el-switch
            v-model="enableAutoBackup"
            active-text="启用自动备份"
            inactive-text="停用自动备份"
            @change="handleAutoBackupChange"
          />
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="planList"
        border
      >
        <el-table-column prop="name" label="计划名称" min-width="150" />
        <el-table-column prop="type" label="备份类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'full' ? 'primary' : 'success'">
              {{ scope.row.type === 'full' ? '完整备份' : '增量备份' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="schedule" label="执行计划" min-width="150" />
        <el-table-column prop="lastExecuteTime" label="上次执行" width="160" />
        <el-table-column prop="nextExecuteTime" label="下次执行" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status ? 'success' : 'info'">
              {{ scope.row.status ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-tooltip :content="scope.row.status ? '禁用' : '启用'">
                <el-button 
                  :type="scope.row.status ? 'warning' : 'success'" 
                  link 
                  @click="handleToggleStatus(scope.row)"
                >
                  <el-icon>
                    <component :is="scope.row.status ? 'Minus' : 'CircleCheck'" />
                  </el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="立即执行">
                <el-button type="primary" link @click="handleExecuteNow(scope.row)">
                  <el-icon><VideoPlay /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑">
                <el-button type="primary" link @click="handleEditPlan(scope.row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="查看日志">
                <el-button type="info" link @click="handleViewLogs(scope.row)">
                  <el-icon><Document /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除">
                <el-button type="danger" link @click="handleDeletePlan(scope.row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 系统备份配置 -->
    <el-card class="backup-settings-card">
      <template #header>
        <div class="card-header">
          <span>备份配置</span>
        </div>
      </template>
      
      <el-form :model="backupSettings" label-width="160px">
        <el-form-item label="默认备份存储路径">
          <el-input v-model="backupSettings.storagePath" placeholder="/data/backup">
            <template #append>
              <el-button @click="handleSelectPath">选择路径</el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="备份文件命名格式">
          <el-select v-model="backupSettings.fileNameFormat" style="width: 100%">
            <el-option label="日期_类型_名称 (20230510_full_backup)" value="date_type_name" />
            <el-option label="名称_日期_类型 (backup_20230510_full)" value="name_date_type" />
            <el-option label="类型_日期_名称 (full_20230510_backup)" value="type_date_name" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="备份文件保留策略">
          <el-radio-group v-model="backupSettings.retentionPolicy" class="retention-policy">
            <el-radio label="count">按数量保留</el-radio>
            <el-radio label="time">按时间保留</el-radio>
            <el-radio label="size">按大小保留</el-radio>
            <el-radio label="unlimited">无限制</el-radio>
          </el-radio-group>
          
          <div class="retention-settings" v-if="backupSettings.retentionPolicy !== 'unlimited'">
            <template v-if="backupSettings.retentionPolicy === 'count'">
              <span>保留最近</span>
              <el-input-number v-model="backupSettings.retentionCount" :min="1" :max="100" />
              <span>个备份</span>
            </template>
            <template v-else-if="backupSettings.retentionPolicy === 'time'">
              <span>保留</span>
              <el-input-number v-model="backupSettings.retentionDays" :min="1" :max="365" />
              <span>天内备份</span>
            </template>
            <template v-else-if="backupSettings.retentionPolicy === 'size'">
              <span>总大小限制为</span>
              <el-input-number v-model="backupSettings.retentionSize" :min="1" :max="10000" />
              <span>GB</span>
            </template>
          </div>
        </el-form-item>
        
        <el-form-item label="备份完成通知">
          <el-switch v-model="backupSettings.notification" />
          <span class="settings-desc">启用后，备份完成会发送通知</span>
        </el-form-item>
        
        <el-form-item label="自动压缩备份">
          <el-switch v-model="backupSettings.compression" />
          <span class="settings-desc">启用后，备份文件自动压缩</span>
        </el-form-item>
        
        <el-form-item label="备份加密">
          <el-switch v-model="backupSettings.encryption" />
          <span class="settings-desc">启用后，备份文件将被加密</span>
          
          <div v-if="backupSettings.encryption" class="encryption-settings">
            <el-input v-model="backupSettings.encryptionPassword" placeholder="请输入加密密码" show-password />
            <div class="encryption-warning">
              <el-alert
                title="请妥善保管加密密码，丢失密码将无法恢复备份数据"
                type="warning"
                show-icon
                :closable="false"
              />
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveSettings">保存配置</el-button>
          <el-button @click="resetSettings">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 添加/编辑备份计划对话框 -->
    <el-dialog
      v-model="planDialogVisible"
      :title="isEditMode ? '编辑备份计划' : '新建备份计划'"
      width="600px"
    >
      <el-form :model="planForm" :rules="planRules" ref="planFormRef" label-width="120px">
        <el-form-item label="计划名称" prop="name">
          <el-input v-model="planForm.name" placeholder="请输入计划名称" />
        </el-form-item>
        
        <el-form-item label="备份类型" prop="type">
          <el-radio-group v-model="planForm.type">
            <el-radio label="full">完整备份</el-radio>
            <el-radio label="incremental">增量备份</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="备份范围" prop="scope">
          <el-checkbox-group v-model="planForm.scope">
            <el-checkbox label="documents">文档数据</el-checkbox>
            <el-checkbox label="categories">分类数据</el-checkbox>
            <el-checkbox label="workflows">流程数据</el-checkbox>
            <el-checkbox label="users">用户数据</el-checkbox>
            <el-checkbox label="settings">系统设置</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="执行计划" prop="scheduleType">
          <el-radio-group v-model="planForm.scheduleType">
            <el-radio label="daily">每天</el-radio>
            <el-radio label="weekly">每周</el-radio>
            <el-radio label="monthly">每月</el-radio>
            <el-radio label="custom">自定义</el-radio>
          </el-radio-group>
          
          <div class="schedule-settings">
            <template v-if="planForm.scheduleType === 'daily'">
              <el-time-picker v-model="planForm.dailyTime" format="HH:mm" placeholder="执行时间" />
            </template>
            
            <template v-else-if="planForm.scheduleType === 'weekly'">
              <el-select v-model="planForm.weekDay" placeholder="星期几">
                <el-option label="星期一" value="1" />
                <el-option label="星期二" value="2" />
                <el-option label="星期三" value="3" />
                <el-option label="星期四" value="4" />
                <el-option label="星期五" value="5" />
                <el-option label="星期六" value="6" />
                <el-option label="星期日" value="0" />
              </el-select>
              <el-time-picker v-model="planForm.weeklyTime" format="HH:mm" placeholder="执行时间" />
            </template>
            
            <template v-else-if="planForm.scheduleType === 'monthly'">
              <el-input-number v-model="planForm.monthDay" :min="1" :max="31" placeholder="日期" />
              <el-time-picker v-model="planForm.monthlyTime" format="HH:mm" placeholder="执行时间" />
            </template>
            
            <template v-else-if="planForm.scheduleType === 'custom'">
              <el-input v-model="planForm.cronExpression" placeholder="Cron 表达式，例如: 0 0 2 * * ?" />
            </template>
          </div>
        </el-form-item>
        
        <el-form-item label="计划状态" prop="status">
          <el-switch v-model="planForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
        
        <el-form-item label="备份描述" prop="description">
          <el-input 
            v-model="planForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备份计划描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="planDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPlanForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 备份日志对话框 -->
    <el-dialog
      v-model="logsDialogVisible"
      title="备份执行日志"
      width="700px"
    >
      <div class="plan-info" v-if="currentPlan.id">
        <p><strong>计划名称：</strong>{{ currentPlan.name }}</p>
        <p><strong>备份类型：</strong>{{ currentPlan.type === 'full' ? '完整备份' : '增量备份' }}</p>
        <p><strong>执行计划：</strong>{{ currentPlan.schedule }}</p>
      </div>
      
      <el-table :data="backupLogs" stripe style="width: 100%">
        <el-table-column prop="executeTime" label="执行时间" width="180" />
        <el-table-column prop="duration" label="耗时" width="100" />
        <el-table-column prop="size" label="备份大小" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="消息" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Document, VideoPlay, 
  CircleCheck, Minus
} from '@element-plus/icons-vue'

// 自动备份开关
const enableAutoBackup = ref(true)

// 备份计划列表
const loading = ref(false)
const planList = ref([])

// 当前计划
const currentPlan = ref({})

// 添加/编辑对话框
const planDialogVisible = ref(false)
const isEditMode = ref(false)
const planFormRef = ref(null)

// 日志对话框
const logsDialogVisible = ref(false)
const backupLogs = ref([])

// 备份计划表单
const planForm = reactive({
  name: '',
  type: 'full',
  scope: ['documents', 'categories', 'workflows', 'users', 'settings'],
  scheduleType: 'daily',
  dailyTime: new Date(2023, 0, 1, 2, 0),
  weekDay: '1',
  weeklyTime: new Date(2023, 0, 1, 2, 0),
  monthDay: 1,
  monthlyTime: new Date(2023, 0, 1, 2, 0),
  cronExpression: '0 0 2 * * ?',
  status: true,
  description: ''
})

// 表单校验规则
const planRules = {
  name: [
    { required: true, message: '请输入计划名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  scope: [
    { type: 'array', required: true, message: '请至少选择一个备份范围', trigger: 'change' }
  ]
}

// 备份设置
const backupSettings = reactive({
  storagePath: '/data/backup',
  fileNameFormat: 'date_type_name',
  retentionPolicy: 'count',
  retentionCount: 10,
  retentionDays: 30,
  retentionSize: 100,
  notification: true,
  compression: true,
  encryption: false,
  encryptionPassword: ''
})

// 获取备份计划列表
const fetchPlanList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      planList.value = [
        {
          id: 1,
          name: '每日完整备份',
          type: 'full',
          schedule: '每天 02:00',
          lastExecuteTime: '2023-03-10 02:00:00',
          nextExecuteTime: '2023-03-11 02:00:00',
          status: true
        },
        {
          id: 2,
          name: '周末增量备份',
          type: 'incremental',
          schedule: '每周六 23:00',
          lastExecuteTime: '2023-03-04 23:00:00',
          nextExecuteTime: '2023-03-11 23:00:00',
          status: true
        },
        {
          id: 3,
          name: '月度归档备份',
          type: 'full',
          schedule: '每月1日 04:00',
          lastExecuteTime: '2023-03-01 04:00:00',
          nextExecuteTime: '2023-04-01 04:00:00',
          status: false
        }
      ]
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取备份计划列表失败')
    loading.value = false
  }
}

// 自动备份开关变化
const handleAutoBackupChange = (val) => {
  ElMessage.success(`${val ? '启用' : '禁用'}自动备份成功`)
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

// 处理新建计划
const handleAddPlan = () => {
  isEditMode.value = false
  planForm.name = ''
  planForm.type = 'full'
  planForm.scope = ['documents', 'categories', 'workflows', 'users', 'settings']
  planForm.scheduleType = 'daily'
  planForm.dailyTime = new Date(2023, 0, 1, 2, 0)
  planForm.status = true
  planForm.description = ''
  
  planDialogVisible.value = true
}

// 处理编辑计划
const handleEditPlan = (row) => {
  isEditMode.value = true
  currentPlan.value = row
  
  // 填充表单数据
  planForm.name = row.name
  planForm.type = row.type
  planForm.scope = ['documents', 'categories'] // 模拟数据
  planForm.scheduleType = 'daily' // 模拟数据，实际应根据schedule解析
  planForm.dailyTime = new Date(2023, 0, 1, 2, 0)
  planForm.status = row.status
  planForm.description = '备份计划描述' // 模拟数据
  
  planDialogVisible.value = true
}

// 提交备份计划表单
const submitPlanForm = async () => {
  if (!planFormRef.value) return
  
  await planFormRef.value.validate((valid) => {
    if (valid) {
      // 构建schedule字符串
      let scheduleStr = ''
      if (planForm.scheduleType === 'daily') {
        const time = planForm.dailyTime
        scheduleStr = `每天 ${time.getHours().toString().padStart(2, '0')}:${time.getMinutes().toString().padStart(2, '0')}`
      } else if (planForm.scheduleType === 'weekly') {
        const weekMap = { '0': '日', '1': '一', '2': '二', '3': '三', '4': '四', '5': '五', '6': '六' }
        const time = planForm.weeklyTime
        scheduleStr = `每周${weekMap[planForm.weekDay]} ${time.getHours().toString().padStart(2, '0')}:${time.getMinutes().toString().padStart(2, '0')}`
      } else if (planForm.scheduleType === 'monthly') {
        const time = planForm.monthlyTime
        scheduleStr = `每月${planForm.monthDay}日 ${time.getHours().toString().padStart(2, '0')}:${time.getMinutes().toString().padStart(2, '0')}`
      } else {
        scheduleStr = `自定义: ${planForm.cronExpression}`
      }
      
      if (isEditMode.value) {
        // 更新计划
        const index = planList.value.findIndex(p => p.id === currentPlan.value.id)
        if (index !== -1) {
          planList.value[index] = {
            ...planList.value[index],
            name: planForm.name,
            type: planForm.type,
            schedule: scheduleStr,
            status: planForm.status
          }
        }
        ElMessage.success('备份计划更新成功')
      } else {
        // 添加计划
        planList.value.push({
          id: Date.now(),
          name: planForm.name,
          type: planForm.type,
          schedule: scheduleStr,
          lastExecuteTime: '-',
          nextExecuteTime: '2023-03-12 02:00:00', // 模拟数据
          status: planForm.status
        })
        ElMessage.success('备份计划创建成功')
      }
      
      planDialogVisible.value = false
    }
  })
}

// 切换计划状态
const handleToggleStatus = (row) => {
  const newStatus = !row.status
  const index = planList.value.findIndex(p => p.id === row.id)
  if (index !== -1) {
    planList.value[index].status = newStatus
  }
  ElMessage.success(`备份计划${newStatus ? '启用' : '禁用'}成功`)
}

// 立即执行
const handleExecuteNow = (row) => {
  ElMessageBox.confirm(
    `确定要立即执行 ${row.name} 备份计划吗？`,
    '确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    ElMessage.success('备份任务已添加到队列，正在执行中')
  }).catch(() => {})
}

// 查看日志
const handleViewLogs = (row) => {
  currentPlan.value = row
  
  // 模拟获取日志
  backupLogs.value = [
    {
      id: 1,
      executeTime: '2023-03-10 02:00:00',
      duration: '5分钟',
      size: 256000000,
      status: 'success',
      message: '备份成功'
    },
    {
      id: 2,
      executeTime: '2023-03-09 02:00:00',
      duration: '4分30秒',
      size: 240000000,
      status: 'success',
      message: '备份成功'
    },
    {
      id: 3,
      executeTime: '2023-03-08 02:00:00',
      duration: '失败',
      size: 0,
      status: 'failed',
      message: '磁盘空间不足'
    }
  ]
  
  logsDialogVisible.value = true
}

// 删除计划
const handleDeletePlan = (row) => {
  ElMessageBox.confirm(
    `确定要删除备份计划 ${row.name} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = planList.value.findIndex(p => p.id === row.id)
    if (index !== -1) {
      planList.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 选择路径
const handleSelectPath = () => {
  ElMessage.info('路径选择功能开发中')
}

// 保存设置
const saveSettings = () => {
  // 实际应用应调用API保存
  ElMessage.success('备份配置保存成功')
}

// 重置设置
const resetSettings = () => {
  backupSettings.storagePath = '/data/backup'
  backupSettings.fileNameFormat = 'date_type_name'
  backupSettings.retentionPolicy = 'count'
  backupSettings.retentionCount = 10
  backupSettings.retentionDays = 30
  backupSettings.retentionSize = 100
  backupSettings.notification = true
  backupSettings.compression = true
  backupSettings.encryption = false
  backupSettings.encryptionPassword = ''
  
  ElMessage.success('备份配置已重置')
}

// 生命周期钩子
onMounted(() => {
  fetchPlanList()
})
</script>

<style scoped>
.backup-plan-container {
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

.plan-list-card {
  margin-bottom: 20px;
}

.backup-settings-card {
  margin-bottom: 20px;
}

.retention-policy {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.retention-settings {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.retention-settings span {
  margin: 0 10px;
}

.settings-desc {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.encryption-settings {
  margin-top: 10px;
}

.encryption-warning {
  margin-top: 10px;
}

.schedule-settings {
  margin-top: 10px;
  display: flex;
  align-items: center;
}

.schedule-settings .el-select,
.schedule-settings .el-input-number {
  margin-right: 10px;
}

.plan-info {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.plan-info p {
  margin: 5px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 