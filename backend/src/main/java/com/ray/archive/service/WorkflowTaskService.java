package com.ray.archive.service;

import com.ray.archive.dto.PageResult;
import com.ray.archive.entity.WorkflowTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 工作流任务服务接口
 */
public interface WorkflowTaskService {
    /**
     * 保存工作流任务
     * @param workflowTask 工作流任务
     * @return 保存后的工作流任务
     */
    WorkflowTask save(WorkflowTask workflowTask);
    
    /**
     * 根据ID查询工作流任务
     * @param id 工作流任务ID
     * @return 工作流任务
     */
    Optional<WorkflowTask> findById(Long id);
    
    /**
     * 查询所有工作流任务
     * @param pageable 分页参数
     * @return 工作流任务分页结果
     */
    Page<WorkflowTask> findAll(Pageable pageable);
    
    /**
     * 根据工作流实例ID查询任务
     * @param workflowInstanceId 工作流实例ID
     * @return 工作流任务列表
     */
    List<WorkflowTask> findByWorkflowInstanceId(Long workflowInstanceId);
    
    /**
     * 根据处理人查询任务
     * @param assignee 处理人
     * @return 工作流任务列表
     */
    List<WorkflowTask> findByAssignee(String assignee);
    
    /**
     * 根据状态查询任务
     * @param status 状态
     * @return 工作流任务列表
     */
    List<WorkflowTask> findByStatus(String status);
    
    /**
     * 根据处理人和状态查询任务
     * @param assignee 处理人
     * @param status 状态
     * @param pageable 分页参数
     * @return 工作流任务分页结果
     */
    Page<WorkflowTask> findByAssigneeAndStatus(String assignee, String status, Pageable pageable);
    
    /**
     * 根据工作流实例ID和状态查询任务
     * @param workflowInstanceId 工作流实例ID
     * @param status 状态
     * @return 工作流任务列表
     */
    List<WorkflowTask> findByWorkflowInstanceIdAndStatus(Long workflowInstanceId, String status);
    
    /**
     * 删除工作流任务
     * @param id 工作流任务ID
     */
    void deleteById(Long id);
    
    /**
     * 分页查询工作流任务
     * @param page 页码
     * @param pageSize 每页条数
     * @param assignee 处理人
     * @param status 状态
     * @return 分页结果
     */
    PageResult<WorkflowTask> getTasks(int page, int pageSize, String assignee, String status);
    
    /**
     * 完成工作流任务
     * @param id 工作流任务ID
     * @param remarks 备注
     * @return 完成后的工作流任务
     */
    WorkflowTask completeTask(Long id, String remarks);
    
    /**
     * 分配工作流任务
     * @param id 工作流任务ID
     * @param assignee 处理人
     * @return 分配后的工作流任务
     */
    WorkflowTask assignTask(Long id, String assignee);
    
    /**
     * 查询逾期任务
     * @return 逾期任务列表
     */
    List<WorkflowTask> findOverdueTasks();
    
    /**
     * 查询即将到期的任务
     * @param dueDate 到期时间
     * @return 即将到期的任务列表
     */
    List<WorkflowTask> findByDueDateBefore(LocalDateTime dueDate);
    
    /**
     * 查询状态数量
     * @param status 状态
     * @return 状态数量
     */
    long countByStatus(String status);
    
    /**
     * 查询处理人和状态的数量
     * @param assignee 处理人
     * @param status 状态
     * @return 数量
     */
    long countByAssigneeAndStatus(String assignee, String status);
} 