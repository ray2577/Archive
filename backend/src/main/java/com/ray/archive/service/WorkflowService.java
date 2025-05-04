package com.ray.archive.service;

import com.ray.archive.dto.PageResult;
import com.ray.archive.dto.WorkflowDTO;
import com.ray.archive.dto.WorkflowInstanceDTO;
import com.ray.archive.dto.WorkflowTaskDTO;
import com.ray.archive.entity.Workflow;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 工作流服务接口
 */
public interface WorkflowService {
    
    /**
     * 创建工作流定义
     * @param workflowDTO 工作流数据
     * @return 创建的工作流DTO
     */
    WorkflowDTO createWorkflow(WorkflowDTO workflowDTO);
    
    /**
     * 更新工作流定义
     * @param id 工作流ID
     * @param workflowDTO 工作流数据
     * @return 更新后的工作流DTO
     */
    WorkflowDTO updateWorkflow(Long id, WorkflowDTO workflowDTO);
    
    /**
     * 根据ID获取工作流
     * @param id 工作流ID
     * @return 工作流DTO
     */
    WorkflowDTO getWorkflowById(Long id);
    
    /**
     * 分页获取工作流列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param name 工作流名称
     * @param category 工作流分类
     * @param status 工作流状态
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页结果
     */
    PageResult<WorkflowDTO> getWorkflows(int page, int pageSize, String name, 
                                        String category, String status,
                                        String sortBy, String sortDirection);
    
    /**
     * 删除工作流
     * @param id 工作流ID
     * @return 是否删除成功
     */
    boolean deleteWorkflow(Long id);
    
    /**
     * 发布工作流
     * @param id 工作流ID
     * @return 发布后的工作流DTO
     */
    WorkflowDTO deployWorkflow(Long id);
    
    /**
     * 启动工作流
     * @param workflowId 工作流ID
     * @param businessKey 业务键
     * @param variables 流程变量
     * @return 流程实例DTO
     */
    WorkflowInstanceDTO startWorkflow(Long workflowId, String businessKey, Map<String, Object> variables);
    
    /**
     * 获取流程实例
     * @param instanceId 实例ID
     * @return 流程实例DTO
     */
    WorkflowInstanceDTO getWorkflowInstance(Long instanceId);
    
    /**
     * 获取流程实例列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param processName 流程名称
     * @param initiator 发起人
     * @param processType 流程类型
     * @param status 状态
     * @return 分页结果
     */
    PageResult<WorkflowInstanceDTO> getWorkflowInstances(int page, int pageSize, 
                                                      String processName, String initiator, 
                                                      String processType, String status);
    
    /**
     * 获取我的工作流实例
     * @param page 页码
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @param status 状态
     * @return 分页结果
     */
    PageResult<WorkflowInstanceDTO> getMyWorkflowInstances(int page, int pageSize, Long userId, String status);
    
    /**
     * 获取工作流历史记录
     * @param page 页码
     * @param pageSize 每页大小
     * @param processName 流程名称
     * @param initiator 发起人
     * @param processType 流程类型
     * @param result 结果
     * @return 分页结果
     */
    PageResult<WorkflowInstanceDTO> getWorkflowHistory(int page, int pageSize, 
                                                    String processName, String initiator, 
                                                    String processType, String result);
    
    /**
     * 获取用户待办任务
     * @param page 页码
     * @param pageSize 每页大小
     * @param assignee 任务处理人
     * @return 分页结果
     */
    PageResult<WorkflowTaskDTO> getTodoTasks(int page, int pageSize, String assignee);
    
    /**
     * 获取用户已办任务
     * @param page 页码
     * @param pageSize 每页大小
     * @param assignee 任务处理人
     * @return 分页结果
     */
    PageResult<WorkflowTaskDTO> getDoneTasks(int page, int pageSize, String assignee);
    
    /**
     * 完成任务
     * @param taskId 任务ID
     * @param outcome 处理结果
     * @param formData 表单数据
     * @param comment 处理意见
     * @return 任务DTO
     */
    WorkflowTaskDTO completeTask(String taskId, String outcome, Map<String, Object> formData, String comment);
    
    /**
     * 拒绝任务
     * @param taskId 任务ID
     * @param reason 拒绝原因
     * @return 任务DTO
     */
    WorkflowTaskDTO rejectTask(String taskId, String reason);
    
    /**
     * 转交任务
     * @param taskId 任务ID
     * @param assignee 新的处理人
     * @param reason 转交原因
     * @return 任务DTO
     */
    WorkflowTaskDTO transferTask(String taskId, String assignee, String reason);
    
    /**
     * 委派任务
     * @param taskId 任务ID
     * @param assignee 委派给谁
     * @param reason 委派原因
     * @return 任务DTO
     */
    WorkflowTaskDTO delegateTask(String taskId, String assignee, String reason);
    
    /**
     * 获取流程图
     * @param processInstanceId 流程实例ID
     * @return 流程图资源
     */
    Resource getProcessDiagram(String processInstanceId);
    
    /**
     * 导出工作流历史
     * @param filters 过滤条件
     * @return 导出资源
     */
    Resource exportWorkflowHistory(Map<String, Object> filters);
} 