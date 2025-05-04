package com.ray.archive.service;

import com.ray.archive.dto.PageResult;
import com.ray.archive.entity.WorkflowInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 工作流实例服务接口
 */
public interface WorkflowInstanceService {
    /**
     * 保存工作流实例
     * @param workflowInstance 工作流实例
     * @return 保存后的工作流实例
     */
    WorkflowInstance save(WorkflowInstance workflowInstance);
    
    /**
     * 根据ID查询工作流实例
     * @param id 工作流实例ID
     * @return 工作流实例
     */
    Optional<WorkflowInstance> findById(Long id);
    
    /**
     * 根据实例ID查询工作流实例
     * @param instanceId 实例ID
     * @return 工作流实例
     */
    Optional<WorkflowInstance> findByInstanceId(String instanceId);
    
    /**
     * 查询所有工作流实例
     * @param pageable 分页参数
     * @return 工作流实例分页结果
     */
    Page<WorkflowInstance> findAll(Pageable pageable);
    
    /**
     * 根据工作流ID查询实例
     * @param workflowId 工作流ID
     * @return 工作流实例列表
     */
    List<WorkflowInstance> findByWorkflowId(Long workflowId);
    
    /**
     * 根据状态查询实例
     * @param status 状态
     * @return 工作流实例列表
     */
    List<WorkflowInstance> findByStatus(String status);
    
    /**
     * 根据发起人查询实例
     * @param initiator 发起人
     * @return 工作流实例列表
     */
    List<WorkflowInstance> findByInitiator(String initiator);
    
    /**
     * 根据多个状态查询实例
     * @param statuses 状态列表
     * @param pageable 分页参数
     * @return 工作流实例分页结果
     */
    Page<WorkflowInstance> findByStatusIn(List<String> statuses, Pageable pageable);
    
    /**
     * 根据发起人和多个状态查询实例
     * @param initiator 发起人
     * @param statuses 状态列表
     * @param pageable 分页参数
     * @return 工作流实例分页结果
     */
    Page<WorkflowInstance> findByInitiatorAndStatusIn(String initiator, List<String> statuses, Pageable pageable);
    
    /**
     * 根据业务键和流程类型查询实例
     * @param businessKey 业务键
     * @param processType 流程类型
     * @return 工作流实例
     */
    Optional<WorkflowInstance> findByBusinessKeyAndProcessType(String businessKey, String processType);
    
    /**
     * 删除工作流实例
     * @param id 工作流实例ID
     */
    void deleteById(Long id);
    
    /**
     * 启动工作流实例
     * @param workflowId 工作流ID
     * @param businessKey 业务键
     * @param initiator 发起人
     * @param variables 变量
     * @return 工作流实例
     */
    WorkflowInstance startWorkflow(Long workflowId, String businessKey, String initiator, Map<String, Object> variables);
    
    /**
     * 完成工作流实例
     * @param id 工作流实例ID
     * @param status 状态
     * @param result 结果
     * @return 完成后的工作流实例
     */
    WorkflowInstance completeWorkflow(Long id, String status, String result);
    
    /**
     * 取消工作流实例
     * @param id 工作流实例ID
     * @param reason 原因
     * @return 取消后的工作流实例
     */
    WorkflowInstance cancelWorkflow(Long id, String reason);
    
    /**
     * 暂停工作流实例
     * @param id 工作流实例ID
     * @return 暂停后的工作流实例
     */
    WorkflowInstance suspendWorkflow(Long id);
    
    /**
     * 恢复工作流实例
     * @param id 工作流实例ID
     * @return 恢复后的工作流实例
     */
    WorkflowInstance resumeWorkflow(Long id);
    
    /**
     * 分页查询工作流实例
     * @param page 页码
     * @param pageSize 每页条数
     * @param processType 流程类型
     * @param businessKey 业务键
     * @param initiator 发起人
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    PageResult<WorkflowInstance> getInstances(int page, int pageSize, String processType, String businessKey, 
            String initiator, String status, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 查询状态数量
     * @param status 状态
     * @return 状态数量
     */
    long countByStatus(String status);
    
    /**
     * 查询发起人和状态的数量
     * @param initiator 发起人
     * @param status 状态
     * @return 数量
     */
    long countByInitiatorAndStatus(String initiator, String status);
    
    /**
     * 获取流程类型分布
     * @return 流程类型分布
     */
    Map<String, Long> getProcessTypeDistribution();
    
    /**
     * 获取状态分布
     * @return 状态分布
     */
    Map<String, Long> getStatusDistribution();
    
    /**
     * 获取月度分布
     * @return 月度分布
     */
    Map<String, Long> getMonthlyDistribution();
} 