package com.ray.archive.service.impl;

import com.ray.archive.dto.PageResult;
import com.ray.archive.dto.WorkflowDTO;
import com.ray.archive.dto.WorkflowInstanceDTO;
import com.ray.archive.dto.WorkflowTaskDTO;
import com.ray.archive.entity.Workflow;
import com.ray.archive.entity.WorkflowInstance;
import com.ray.archive.repository.WorkflowRepository;
import com.ray.archive.service.WorkflowInstanceService;
import com.ray.archive.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 工作流服务实现类
 */
@Service
@Transactional
public class WorkflowServiceImpl implements WorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);
    
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceService workflowInstanceService;

    @Autowired
    public WorkflowServiceImpl(WorkflowRepository workflowRepository, 
                              WorkflowInstanceService workflowInstanceService) {
        this.workflowRepository = workflowRepository;
        this.workflowInstanceService = workflowInstanceService;
    }

    @Override
    public Workflow save(Workflow workflow) {
        Assert.notNull(workflow, "Workflow must not be null");
        logger.debug("Saving workflow: {}", workflow);
        
        if (workflow.getId() == null) {
            if (workflow.getWorkflowKey() == null) {
                workflow.setWorkflowKey(UUID.randomUUID().toString());
            }
            workflow.setCreateTime(LocalDateTime.now());
            workflow.setIsActive(true);
        }
        workflow.setUpdateTime(LocalDateTime.now());
        
        return workflowRepository.save(workflow);
    }

    @Override
    public Optional<Workflow> findById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Finding workflow by ID: {}", id);
        return workflowRepository.findById(id);
    }

    @Override
    public Optional<Workflow> findByWorkflowKey(String workflowKey) {
        Assert.hasText(workflowKey, "Workflow key must not be empty");
        logger.debug("Finding workflow by workflow key: {}", workflowKey);
        return workflowRepository.findByWorkflowKey(workflowKey);
    }

    @Override
    public Page<Workflow> findAll(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");
        logger.debug("Finding all workflows with pageable: {}", pageable);
        return workflowRepository.findAll(pageable);
    }

    @Override
    public List<Workflow> findByCategory(String category) {
        Assert.hasText(category, "Category must not be empty");
        logger.debug("Finding workflows by category: {}", category);
        return workflowRepository.findByCategory(category);
    }

    @Override
    public List<Workflow> findByProcessType(String processType) {
        Assert.hasText(processType, "Process type must not be empty");
        logger.debug("Finding workflows by process type: {}", processType);
        return workflowRepository.findByProcessType(processType);
    }

    @Override
    public Page<Workflow> findByNameContaining(String name, Pageable pageable) {
        Assert.hasText(name, "Name must not be empty");
        Assert.notNull(pageable, "Pageable must not be null");
        logger.debug("Finding workflows containing name: {} with pageable: {}", name, pageable);
        return workflowRepository.findByNameContaining(name, pageable);
    }

    @Override
    public List<Workflow> findByIsActiveTrue() {
        logger.debug("Finding all active workflows");
        return workflowRepository.findByIsActiveTrue();
    }

    @Override
    public void deleteById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Deleting workflow by ID: {}", id);
        workflowRepository.deleteById(id);
    }

    @Override
    public Workflow updateActiveStatus(Long id, boolean isActive) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Updating workflow active status: id={}, isActive={}", id, isActive);
        
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id: " + id));
        
        workflow.setIsActive(isActive);
        workflow.setUpdateTime(LocalDateTime.now());
        
        return workflowRepository.save(workflow);
    }

    @Override
    public boolean existsByWorkflowKey(String workflowKey) {
        Assert.hasText(workflowKey, "Workflow key must not be empty");
        logger.debug("Checking if workflow exists with workflow key: {}", workflowKey);
        return workflowRepository.existsByWorkflowKey(workflowKey);
    }

    @Override
    public PageResult<Workflow> getWorkflows(int page, int pageSize, String name, String category,
            String processType, Boolean isActive) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
        
        Page<Workflow> workflowsPage = workflowRepository.findByFilters(name, category, processType, isActive, pageable);
        
        return new PageResult<Workflow>(
                workflowsPage.getContent(),
                workflowsPage.getTotalElements(),
                workflowsPage.getTotalPages(),
                page
        );
    }

    @Override
    public Workflow deployWorkflow(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Deploying workflow with ID: {}", id);
        
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id: " + id));
        
        workflow.setDeployTime(LocalDateTime.now());
        workflow.setIsDeployed(true);
        workflow.setUpdateTime(LocalDateTime.now());
        
        return workflowRepository.save(workflow);
    }

    @Override
    public Map<String, Long> getProcessTypeDistribution() {
        logger.debug("Getting process type distribution");
        List<Object[]> processTypeStats = workflowRepository.countByProcessTypes();
        
        Map<String, Long> distribution = new HashMap<>();
        if (processTypeStats != null) {
            for (Object[] stat : processTypeStats) {
                if (stat[0] != null) {
                    distribution.put(stat[0].toString(), (Long) stat[1]);
                }
            }
        }
        
        return distribution;
    }

    @Override
    public Map<String, Long> getCategoryDistribution() {
        logger.debug("Getting category distribution");
        List<Object[]> categoryStats = workflowRepository.countByCategories();
        
        Map<String, Long> distribution = new HashMap<>();
        if (categoryStats != null) {
            for (Object[] stat : categoryStats) {
                if (stat[0] != null) {
                    distribution.put(stat[0].toString(), (Long) stat[1]);
                }
            }
        }
        
        return distribution;
    }
    
    // 以下是实现WorkflowService接口中的高级方法
    
    @Override
    public WorkflowDTO createWorkflow(WorkflowDTO workflowDTO) {
        Assert.notNull(workflowDTO, "WorkflowDTO must not be null");
        logger.debug("Creating workflow: {}", workflowDTO.getName());
        
        Workflow workflow = convertToEntity(workflowDTO);
        workflow.setCreateTime(LocalDateTime.now());
        workflow.setUpdateTime(LocalDateTime.now());
        workflow.setIsActive(true);
        
        Workflow savedWorkflow = workflowRepository.save(workflow);
        return convertToDTO(savedWorkflow);
    }
    
    @Override
    public WorkflowDTO updateWorkflow(Long id, WorkflowDTO workflowDTO) {
        Assert.notNull(id, "ID must not be null");
        Assert.notNull(workflowDTO, "WorkflowDTO must not be null");
        logger.debug("Updating workflow with ID: {}", id);
        
        Workflow existingWorkflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id: " + id));
        
        // 更新字段
        existingWorkflow.setName(workflowDTO.getName());
        existingWorkflow.setDescription(workflowDTO.getDescription());
        existingWorkflow.setProcessType(workflowDTO.getProcessType());
        existingWorkflow.setCategory(workflowDTO.getCategory());
        existingWorkflow.setDefinition(workflowDTO.getDefinition());
        existingWorkflow.setUpdateTime(LocalDateTime.now());
        
        Workflow updatedWorkflow = workflowRepository.save(existingWorkflow);
        return convertToDTO(updatedWorkflow);
    }
    
    @Override
    public WorkflowDTO getWorkflowById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Getting workflow DTO by ID: {}", id);
        
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id: " + id));
        
        return convertToDTO(workflow);
    }
    
    @Override
    public boolean deleteWorkflow(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Deleting workflow with ID: {}", id);
        
        try {
            if (!workflowRepository.existsById(id)) {
                return false;
            }
            workflowRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting workflow with ID: {}", id, e);
            return false;
        }
    }
    
    @Override
    public WorkflowInstanceDTO startWorkflow(Long workflowId, String businessKey, Map<String, Object> variables) {
        Assert.notNull(workflowId, "Workflow ID must not be null");
        Assert.hasText(businessKey, "Business key must not be empty");
        logger.debug("Starting workflow with ID: {}, business key: {}", workflowId, businessKey);
        
        // 这里使用当前登录用户作为发起人，实际应用中需要从安全上下文获取
        String initiator = "system";
        
        WorkflowInstance instance = workflowInstanceService.startWorkflow(workflowId, businessKey, initiator, variables);
        return convertToInstanceDTO(instance);
    }
    
    @Override
    public WorkflowInstanceDTO getWorkflowInstance(Long instanceId) {
        Assert.notNull(instanceId, "Instance ID must not be null");
        logger.debug("Getting workflow instance with ID: {}", instanceId);
        
        WorkflowInstance instance = workflowInstanceService.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow instance not found with id: " + instanceId));
        
        return convertToInstanceDTO(instance);
    }
    
    @Override
    public PageResult<WorkflowInstanceDTO> getWorkflowInstances(int page, int pageSize, String processName, 
                                                            String initiator, String processType, String status) {
        // 构建查询条件
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        
        PageResult<WorkflowInstance> result = workflowInstanceService.getInstances(
                page, pageSize, processType, null, initiator, status, startDate, endDate);
        
        List<WorkflowInstanceDTO> dtos = result.getRecords().stream()
                .map(this::convertToInstanceDTO)
                .collect(Collectors.toList());
        
        return new PageResult<WorkflowInstanceDTO>(
                dtos,
                result.getTotal(),
                result.getPages(),
                page
        );
    }
    
    @Override
    public PageResult<WorkflowInstanceDTO> getMyWorkflowInstances(int page, int pageSize, Long userId, String status) {
        Assert.notNull(userId, "User ID must not be null");
        logger.debug("Getting workflow instances for user ID: {}, status: {}", userId, status);
        
        // 从用户ID获取用户名
        String username = userId.toString(); // 实际应用中需要查询用户表
        
        List<String> statuses = new ArrayList<>();
        if (status != null && !status.isEmpty()) {
            statuses.add(status);
        } else {
            statuses.add("RUNNING");
            statuses.add("SUSPENDED");
        }
        
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "startTime"));
        
        Page<WorkflowInstance> instancesPage = workflowInstanceService.findByInitiatorAndStatusIn(username, statuses, pageable);
        
        List<WorkflowInstanceDTO> dtos = instancesPage.getContent().stream()
                .map(this::convertToInstanceDTO)
                .collect(Collectors.toList());

        return new PageResult<>(
                dtos,
                instancesPage.getTotalElements(),
                instancesPage.getTotalPages(),
                page
        );
    }
    
    @Override
    public PageResult<WorkflowInstanceDTO> getWorkflowHistory(int page, int pageSize, String processName, 
                                                         String initiator, String processType, String result) {
        // 查询已完成的流程实例
        List<String> statuses = new ArrayList<>();
        statuses.add("COMPLETED");
        statuses.add("CANCELLED");
        statuses.add("FAILED");
        
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "endTime"));
        
        Page<WorkflowInstance> instancesPage = workflowInstanceService.findByStatusIn(statuses, pageable);
        
        List<WorkflowInstanceDTO> dtos = instancesPage.getContent().stream()
                .map(this::convertToInstanceDTO)
                .collect(Collectors.toList());
        
        return new PageResult<>(
                dtos,
                instancesPage.getTotalElements(),
                instancesPage.getTotalPages(),
                page
        );
    }
    
    @Override
    public PageResult<WorkflowTaskDTO> getTodoTasks(int page, int pageSize, String assignee) {
        Assert.hasText(assignee, "Assignee must not be empty");
        logger.debug("Getting todo tasks for assignee: {}", assignee);
        
        // 在实际应用中，这里应该和工作流引擎集成
        // 例如，如果使用Flowable或Activiti:
        // List<Task> tasks = taskService.createTaskQuery()
        //                    .taskAssignee(assignee)
        //                    .active()
        //                    .orderByTaskCreateTime()
        //                    .desc()
        //                    .listPage((page-1)*pageSize, pageSize);
        //
        // long total = taskService.createTaskQuery()
        //              .taskAssignee(assignee)
        //              .active()
        //              .count();
        
        // 临时实现，返回空列表
        List<WorkflowTaskDTO> taskDTOs = new ArrayList<>();
        
        // 这里可以添加模拟数据用于测试
        if ("admin".equals(assignee)) {
            WorkflowTaskDTO task = new WorkflowTaskDTO();
            //task.setId("task-" + UUID.randomUUID().toString());
            //task.setName("审批文档");
            task.setAssignee(assignee);
            //task.setProcessInstanceId("inst-" + UUID.randomUUID().toString());
            task.setCreateTime(LocalDateTime.now().minusDays(1));
            task.setDueDate(LocalDateTime.now().plusDays(2));
            task.setPriority(50);
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("documentName", "重要文件.docx");
            variables.put("requester", "张三");
            //task.setVariables(variables);
            
            taskDTOs.add(task);
        }
        
        return new PageResult<>(
        );
    }
    
    @Override
    public PageResult<WorkflowTaskDTO> getDoneTasks(int page, int pageSize, String assignee) {
        Assert.hasText(assignee, "Assignee must not be empty");
        logger.debug("Getting done tasks for assignee: {}", assignee);
        
        // 在实际应用中，这里应该和工作流引擎集成
        // 例如，如果使用Flowable或Activiti:
        // List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
        //                    .taskAssignee(assignee)
        //                    .finished()
        //                    .orderByTaskCreateTime()
        //                    .desc()
        //                    .listPage((page-1)*pageSize, pageSize);
        //
        // long total = historyService.createHistoricTaskInstanceQuery()
        //              .taskAssignee(assignee)
        //              .finished()
        //              .count();
        
        // 临时实现，返回空列表
        List<WorkflowTaskDTO> taskDTOs = new ArrayList<>();
        
        // 这里可以添加模拟数据用于测试
        if ("admin".equals(assignee)) {
            WorkflowTaskDTO task = new WorkflowTaskDTO();
            //task.setId("task-" + UUID.randomUUID().toString());
            //task.setName("已审批文档");
            task.setAssignee(assignee);
            //task.setProcessInstanceId("inst-" + UUID.randomUUID().toString());
            task.setCreateTime(LocalDateTime.now().minusDays(5));
            //task.setEndTime(LocalDateTime.now().minusDays(4));
            task.setDuration(86400L);  // 1天
            task.setOutcome("通过");
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("documentName", "季度报告.docx");
            variables.put("requester", "李四");
            variables.put("approvalComment", "文档内容符合要求");
            //task.setVariables(variables);
            
            taskDTOs.add(task);
        }
        
        return new PageResult<>(
        );
    }
    
    @Override
    public WorkflowTaskDTO completeTask(String taskId, String outcome, Map<String, Object> formData, String comment) {
        Assert.hasText(taskId, "Task ID must not be empty");
        Assert.hasText(outcome, "Outcome must not be empty");
        logger.debug("Completing task with ID: {}, outcome: {}", taskId, outcome);
        
        // 在实际应用中，这里应该和工作流引擎集成
        // 例如，如果使用Flowable或Activiti:
        // 1. 添加评论（如果有）
        // if (StringUtils.hasText(comment)) {
        //     taskService.addComment(taskId, null, comment);
        // }
        //
        // 2. 设置流程变量
        // if (formData != null && !formData.isEmpty()) {
        //     taskService.setVariables(taskId, formData);
        // }
        //
        // 3. 完成任务，可能需要指定结果
        // Map<String, Object> outcomeMap = new HashMap<>();
        // if (StringUtils.hasText(outcome)) {
        //     outcomeMap.put("outcome", outcome);
        // }
        // taskService.complete(taskId, outcomeMap);
        
        // 临时模拟实现，返回一个假的任务DTO
        WorkflowTaskDTO taskDTO = new WorkflowTaskDTO();
        taskDTO.setTaskId(taskId);
        taskDTO.setTaskName("已完成的任务");
        //taskDTO.setProcessInstanceId("inst-" + UUID.randomUUID().toString());
        taskDTO.setCreateTime(LocalDateTime.now().minusDays(1));
        taskDTO.setCompleteTime(LocalDateTime.now());
        taskDTO.setDuration(3600L);  // 1小时
        taskDTO.setOutcome(outcome);
        taskDTO.setComment(comment);
        
        if (formData != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                taskDTO.setFormData(objectMapper.writeValueAsString(formData));
            } catch (Exception e) {
                logger.warn("Error converting form data to JSON", e);
            }
        }
        
        taskDTO.setStatus("COMPLETED");
        
        // 记录完成任务的操作
        logger.info("Task {} completed with outcome: {}, comment: {}", taskId, outcome, comment);
        
        return taskDTO;
    }
    
    @Override
    public WorkflowTaskDTO rejectTask(String taskId, String reason) {
        Assert.hasText(taskId, "Task ID must not be empty");
        Assert.hasText(reason, "Rejection reason must not be empty");
        logger.debug("Rejecting task with ID: {}, reason: {}", taskId, reason);
        
        // 在实际应用中，这里应该和工作流引擎集成
        // 例如，如果使用Flowable或Activiti:
        // 1. 添加拒绝原因作为评论
        // taskService.addComment(taskId, null, "拒绝原因: " + reason);
        //
        // 2. 设置拒绝变量
        // Map<String, Object> variables = new HashMap<>();
        // variables.put("rejected", true);
        // variables.put("rejectionReason", reason);
        // taskService.setVariables(taskId, variables);
        //
        // 3. 完成任务，通常会使用特定的拒绝流转路径
        // Map<String, Object> outcomeMap = new HashMap<>();
        // outcomeMap.put("outcome", "rejected");
        // taskService.complete(taskId, outcomeMap);
        
        // 临时模拟实现，返回一个假的任务DTO
        WorkflowTaskDTO taskDTO = new WorkflowTaskDTO();
        taskDTO.setTaskId(taskId);
        taskDTO.setTaskName("已拒绝的任务");
        //taskDTO.setProcessInstanceId("inst-" + UUID.randomUUID().toString());
        taskDTO.setCreateTime(LocalDateTime.now().minusDays(1));
        taskDTO.setCompleteTime(LocalDateTime.now());
        taskDTO.setDuration(3600L);  // 1小时
        taskDTO.setOutcome("rejected");
        taskDTO.setComment(reason);
        taskDTO.setStatus("COMPLETED");
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("rejected", true);
        variables.put("rejectionReason", reason);
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            taskDTO.setFormData(objectMapper.writeValueAsString(variables));
        } catch (Exception e) {
            logger.warn("Error converting variables to JSON", e);
        }
        
        // 记录拒绝任务的操作
        logger.info("Task {} rejected with reason: {}", taskId, reason);
        
        return taskDTO;
    }
    
    @Override
    public WorkflowTaskDTO transferTask(String taskId, String assignee, String reason) {
        Assert.hasText(taskId, "Task ID must not be empty");
        Assert.hasText(assignee, "Assignee must not be empty");
        logger.debug("Transferring task with ID: {} to assignee: {}, reason: {}", taskId, assignee, reason);
        
        // 在实际应用中，这里应该和工作流引擎集成
        // 例如，如果使用Flowable或Activiti:
        // 1. 记录转交原因（如果有）
        // if (StringUtils.hasText(reason)) {
        //     taskService.addComment(taskId, null, "转交原因: " + reason);
        // }
        //
        // 2. 设置新的处理人
        // taskService.setAssignee(taskId, assignee);
        
        // 临时模拟实现，返回一个假的任务DTO
        WorkflowTaskDTO taskDTO = new WorkflowTaskDTO();
        taskDTO.setTaskId(taskId);
        taskDTO.setTaskName("已转交的任务");
        taskDTO.setAssignee(assignee);
        //taskDTO.setProcessInstanceId("inst-" + UUID.randomUUID().toString());
        taskDTO.setCreateTime(LocalDateTime.now().minusDays(1));
        taskDTO.setStatus("PENDING");
        taskDTO.setTransferredTo(assignee);
        taskDTO.setTransferReason(reason);
        taskDTO.setUpdateTime(LocalDateTime.now());
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("previousAssignee", "currentUser");  // 实际应用中应该是当前用户ID
        variables.put("transferReason", reason);
        variables.put("transferTime", LocalDateTime.now().toString());
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            taskDTO.setFormData(objectMapper.writeValueAsString(variables));
        } catch (Exception e) {
            logger.warn("Error converting variables to JSON", e);
        }
        
        // 记录转交任务的操作
        logger.info("Task {} transferred to {} with reason: {}", taskId, assignee, reason);
        
        return taskDTO;
    }
    
    @Override
    public WorkflowTaskDTO delegateTask(String taskId, String assignee, String reason) {
        Assert.hasText(taskId, "Task ID must not be empty");
        Assert.hasText(assignee, "Assignee must not be empty");
        logger.debug("Delegating task with ID: {} to assignee: {}, reason: {}", taskId, assignee, reason);
        
        // 在实际应用中，这里应该和工作流引擎集成
        // 例如，如果使用Flowable或Activiti:
        // 1. 记录委派原因（如果有）
        // if (StringUtils.hasText(reason)) {
        //     taskService.addComment(taskId, null, "委派原因: " + reason);
        // }
        //
        // 2. 委派任务（与转交不同，委派会保留原处理人，任务完成后会返回给委派人）
        // taskService.delegateTask(taskId, assignee);
        
        // 临时模拟实现，返回一个假的任务DTO
        WorkflowTaskDTO taskDTO = new WorkflowTaskDTO();
        taskDTO.setTaskId(taskId);
        taskDTO.setTaskName("已委派的任务");
        taskDTO.setAssignee(assignee);
        taskDTO.setOwner("currentUser");  // 实际应用中应该是当前用户ID
        //taskDTO.setProcessInstanceId("inst-" + UUID.randomUUID().toString());
        taskDTO.setCreateTime(LocalDateTime.now().minusDays(1));
        taskDTO.setStatus("PENDING");
        taskDTO.setDelegatedTo(assignee);
        taskDTO.setComment("委派原因: " + reason);
        taskDTO.setUpdateTime(LocalDateTime.now());
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("delegateReason", reason);
        variables.put("delegateTime", LocalDateTime.now().toString());
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            taskDTO.setFormData(objectMapper.writeValueAsString(variables));
        } catch (Exception e) {
            logger.warn("Error converting variables to JSON", e);
        }
        
        // 记录委派任务的操作
        logger.info("Task {} delegated to {} with reason: {}", taskId, assignee, reason);
        
        return taskDTO;
    }
    
    @Override
    public Resource getProcessDiagram(String processInstanceId) {
        Assert.hasText(processInstanceId, "Process instance ID must not be empty");
        logger.debug("Getting process diagram for process instance ID: {}", processInstanceId);
        
        // 在实际应用中，这里应该和工作流引擎集成
        // 例如，如果使用Flowable:
        // try {
        //     // 获取流程实例
        //     ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
        //             .processInstanceId(processInstanceId)
        //             .singleResult();
        //     
        //     // 如果流程已结束，使用历史数据
        //     String processDefinitionId;
        //     if (processInstance == null) {
        //         HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
        //                 .processInstanceId(processInstanceId)
        //                 .singleResult();
        //         if (historicProcessInstance == null) {
        //             throw new IllegalArgumentException("Process instance not found: " + processInstanceId);
        //         }
        //         processDefinitionId = historicProcessInstance.getProcessDefinitionId();
        //     } else {
        //         processDefinitionId = processInstance.getProcessDefinitionId();
        //     }
        //     
        //     // 生成流程图
        //     BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        //     ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        //     
        //     // 获取当前活动的节点
        //     List<String> activeActivityIds = new ArrayList<>();
        //     if (processInstance != null) {
        //         activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
        //     }
        //     
        //     // 生成图像流
        //     InputStream diagramStream = diagramGenerator.generateDiagram(
        //             bpmnModel, "png", activeActivityIds, new ArrayList<>(),
        //             processEngineConfiguration.getActivityFontName(),
        //             processEngineConfiguration.getLabelFontName(),
        //             processEngineConfiguration.getAnnotationFontName(),
        //             processEngineConfiguration.getClassLoader(), 1.0, true);
        //     
        //     // 保存为临时文件
        //     Path tempFile = Files.createTempFile("process_diagram_" + processInstanceId, ".png");
        //     Files.copy(diagramStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        //     diagramStream.close();
        //     
        //     return new UrlResource(tempFile.toUri());
        // } catch (Exception e) {
        //     logger.error("Error generating process diagram for instance: " + processInstanceId, e);
        //     throw new RuntimeException("Failed to generate process diagram", e);
        // }

        // 临时实现，返回一个空的占位图片
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Path placeholderPath = tempDir.resolve("process_diagram_placeholder.png");
            
            // 如果占位图片不存在，创建一个简单的占位图片文件
            if (!Files.exists(placeholderPath)) {
                logger.info("Creating placeholder process diagram image at: {}", placeholderPath);
                
                // 可以使用更复杂的方法生成一个真实的图像，这里简化处理
                Files.createFile(placeholderPath);
            }
            
            return new UrlResource(placeholderPath.toUri());
        } catch (IOException e) {
            logger.error("Error creating placeholder process diagram", e);
            throw new IllegalStateException("Error creating placeholder process diagram", e);
        }
    }
    
    @Override
    public Resource exportWorkflowHistory(Map<String, Object> filters) {
        Assert.notNull(filters, "Filters must not be null");
        logger.debug("Exporting workflow history with filters: {}", filters);
        
        // 在实际应用中，这里应该和工作流引擎集成
        // 1. 根据筛选条件查询流程历史
        // List<WorkflowInstanceDTO> instances = getWorkflowHistoryForExport(filters);
        
        // 2. 创建Excel文件
        try {
            // 在实际应用中，这里应该使用POI或其他Excel处理库创建工作簿
            // Workbook workbook = new XSSFWorkbook();
            // Sheet sheet = workbook.createSheet("工作流历史");
            
            // 创建标题行
            // Row headerRow = sheet.createRow(0);
            // String[] columns = {"流程名称", "业务标识", "流程类型", "发起人", "开始时间", "结束时间", "耗时", "状态", "结果"};
            // for (int i = 0; i < columns.length; i++) {
            //     Cell cell = headerRow.createCell(i);
            //     cell.setCellValue(columns[i]);
            // }
            
            // 填充数据
            // int rowIndex = 1;
            // for (WorkflowInstanceDTO instance : instances) {
            //     Row row = sheet.createRow(rowIndex++);
            //     
            //     int cellIndex = 0;
            //     row.createCell(cellIndex++).setCellValue(instance.getWorkflowName() != null ? instance.getWorkflowName() : "");
            //     row.createCell(cellIndex++).setCellValue(instance.getBusinessKey() != null ? instance.getBusinessKey() : "");
            //     row.createCell(cellIndex++).setCellValue(instance.getProcessType() != null ? instance.getProcessType() : "");
            //     row.createCell(cellIndex++).setCellValue(instance.getInitiator() != null ? instance.getInitiator() : "");
            //     
            //     if (instance.getStartTime() != null) {
            //         row.createCell(cellIndex++).setCellValue(instance.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            //     } else {
            //         row.createCell(cellIndex++).setCellValue("");
            //     }
            //     
            //     if (instance.getEndTime() != null) {
            //         row.createCell(cellIndex++).setCellValue(instance.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            //     } else {
            //         row.createCell(cellIndex++).setCellValue("");
            //     }
            //     
            //     row.createCell(cellIndex++).setCellValue(instance.getDuration() != null ? formatDuration(instance.getDuration()) : "");
            //     row.createCell(cellIndex++).setCellValue(instance.getStatus() != null ? instance.getStatus() : "");
            //     row.createCell(cellIndex++).setCellValue(instance.getResult() != null ? instance.getResult() : "");
            // }
            
            // 调整列宽
            // for (int i = 0; i < columns.length; i++) {
            //     sheet.autoSizeColumn(i);
            // }
            
            // 将工作簿写入临时文件
            // Path tempFile = Files.createTempFile("workflow_history_", ".xlsx");
            // try (FileOutputStream fileOut = new FileOutputStream(tempFile.toFile())) {
            //     workbook.write(fileOut);
            // }
            // workbook.close();
            
            // 临时实现，创建一个空的Excel文件
            Path tempFile = Files.createTempFile("workflow_history_", ".xlsx");
            Files.createFile(tempFile); // 这会覆盖已存在的文件
            
            return new UrlResource(tempFile.toUri());
        } catch (IOException e) {
            logger.error("Error exporting workflow history", e);
            throw new IllegalStateException("Error exporting workflow history", e);
        }
    }
    
    /**
     * 格式化持续时间
     * @param durationInMillis 毫秒时长
     * @return 格式化后的持续时间字符串
     */
    private String formatDuration(long durationInMillis) {
        long seconds = durationInMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天 ");
        }
        if (hours % 24 > 0) {
            sb.append(hours % 24).append("小时 ");
        }
        if (minutes % 60 > 0) {
            sb.append(minutes % 60).append("分钟 ");
        }
        if (seconds % 60 > 0) {
            sb.append(seconds % 60).append("秒");
        }
        
        return sb.toString().trim();
    }
    
    // 辅助方法
    
    /**
     * 将实体转换为DTO
     * @param entity 实体
     * @return DTO
     */
    private WorkflowDTO convertToDTO(Workflow entity) {
        if (entity == null) {
            return null;
        }
        
        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        //dto.setWorkflowKey(entity.getWorkflowKey());
        dto.setDescription(entity.getDescription());
        dto.setProcessType(entity.getProcessType());
        dto.setCategory(entity.getCategory());
        dto.setDefinition(entity.getDefinition());
        //dto.setIsActive(entity.getIsActive());
        //dto.setIsDeployed(entity.getIsDeployed());
        dto.setVersion(entity.getVersion());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        //dto.setDeployTime(entity.getDeployTime());
        dto.setCreator(entity.getCreator());
        
        return dto;
    }
    
    /**
     * 将DTO转换为实体
     * @param dto DTO
     * @return 实体
     */
    private Workflow convertToEntity(WorkflowDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Workflow entity = new Workflow();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        //entity.setWorkflowKey(dto.getWorkflowKey());
        entity.setDescription(dto.getDescription());
        entity.setProcessType(dto.getProcessType());
        entity.setCategory(dto.getCategory());
        entity.setDefinition(dto.getDefinition());
        //entity.setIsActive(dto.getIsActive());
        //entity.setIsDeployed(dto.getIsDeployed());
        entity.setVersion(dto.getVersion());
        entity.setCreator(dto.getCreator());
        
        return entity;
    }
    
    /**
     * 将WorkflowInstance转换为DTO
     * @param instance 实体
     * @return DTO
     */
    private WorkflowInstanceDTO convertToInstanceDTO(WorkflowInstance instance) {
        if (instance == null) {
            return null;
        }
        
        WorkflowInstanceDTO dto = new WorkflowInstanceDTO();
        dto.setId(instance.getId());
        dto.setProcessInstanceId(instance.getProcessInstanceId());
        dto.setBusinessKey(instance.getBusinessKey());
        dto.setProcessType(instance.getProcessType());
        dto.setInitiator(instance.getInitiator());
        dto.setStatus(instance.getStatus());
        dto.setResult(instance.getResult());
        dto.setStartTime(instance.getStartTime());
        dto.setEndTime(instance.getEndTime());
        dto.setDuration(instance.getDuration());
        dto.setComment(instance.getComment());
        dto.setCurrentTask(instance.getCurrentTask());
        dto.setCurrentAssignee(instance.getCurrentAssignee());
        dto.setPriority(instance.getPriority());
        dto.setBusinessData(instance.getBusinessData());
        dto.setVariables(instance.getVariables());
        dto.setUpdateTime(instance.getUpdateTime());
        
        // 获取关联的流程定义信息
        try {
            if (instance.getWorkflow() != null) {
                WorkflowDTO workflow = convertToDTO(instance.getWorkflow());
                dto.setWorkflow(workflow);
            }
        } catch (Exception e) {
            logger.warn("Error fetching workflow details for instance: {}", instance.getId(), e);
        }
        
        return dto;
    }

    @Override
    public Object getWorkflows(int page, int pageSize, String name, String category, String status, String sortBy, String sortDirection) {
        logger.debug("Getting workflows with status: {}, sortBy: {}, sortDirection: {}", status, sortBy, sortDirection);
        
        Sort sort;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("asc")) {
            sort = Sort.by(Sort.Direction.ASC, sortBy != null ? sortBy : "updateTime");
        } else {
            sort = Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "updateTime");
        }
        
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        
        // 转换 status 到 isActive
        Boolean isActive = null;
        if (status != null) {
            if ("active".equalsIgnoreCase(status)) {
                isActive = true;
            } else if ("inactive".equalsIgnoreCase(status)) {
                isActive = false;
            }
        }
        
        Page<Workflow> workflowsPage = workflowRepository.findByFilters(name, category, null, isActive, pageable);
        
        return new PageResult<Workflow>(
                workflowsPage.getContent(),
                workflowsPage.getTotalElements(),
                workflowsPage.getTotalPages(),
                page
        );
    }
} 