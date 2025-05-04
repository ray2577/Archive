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
//            if (workflow.getWorkflowKey() == null) {
//                workflow.setWorkflowKey(UUID.randomUUID().toString());
//            }
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
        
        return new PageResult<>(
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
        
//        List<WorkflowInstanceDTO> dtos = result.getContent().stream()
//                .map(this::convertToInstanceDTO)
//                .collect(Collectors.toList());
        
        return new PageResult<>(
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
                page,
                pageSize,
                instancesPage.getTotalPages()
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
        );
    }
    
    @Override
    public PageResult<WorkflowTaskDTO> getTodoTasks(int page, int pageSize, String assignee) {
        // 实现待办任务查询
        // 这部分需要与具体的工作流引擎集成，这里只提供框架
        return new PageResult<>();
    }
    
    @Override
    public PageResult getDoneTasks(int page, int pageSize, String assignee) {
        // 实现已办任务查询
        // 这部分需要与具体的工作流引擎集成，这里只提供框架
        return new PageResult<>();
    }
    
    @Override
    public WorkflowTaskDTO completeTask(String taskId, String outcome, Map<String, Object> formData, String comment) {
        // 实现完成任务
        // 这部分需要与具体的工作流引擎集成，这里只提供框架
        return new WorkflowTaskDTO();
    }
    
    @Override
    public WorkflowTaskDTO rejectTask(String taskId, String reason) {
        // 实现拒绝任务
        // 这部分需要与具体的工作流引擎集成，这里只提供框架
        return new WorkflowTaskDTO();
    }
    
    @Override
    public WorkflowTaskDTO transferTask(String taskId, String assignee, String reason) {
        // 实现转交任务
        // 这部分需要与具体的工作流引擎集成，这里只提供框架
        return new WorkflowTaskDTO();
    }
    
    @Override
    public WorkflowTaskDTO delegateTask(String taskId, String assignee, String reason) {
        // 实现委派任务
        // 这部分需要与具体的工作流引擎集成，这里只提供框架
        return new WorkflowTaskDTO();
    }
    
    @Override
    public Resource getProcessDiagram(String processInstanceId) {
        // 实现获取流程图
        // 这部分需要与具体的工作流引擎集成，这里只提供框架
        try {
            Path placeholderPath = Paths.get(System.getProperty("java.io.tmpdir"), "placeholder.png");
            if (!Files.exists(placeholderPath)) {
                Files.createFile(placeholderPath);
            }
            return new UrlResource(placeholderPath.toUri());
        } catch (IOException e) {
            throw new IllegalStateException("Error creating placeholder process diagram", e);
        }
    }
    
    @Override
    public Resource exportWorkflowHistory(Map<String, Object> filters) {
        // 实现导出流程历史
        // 这部分需要与具体的工作流引擎集成，这里只提供框架
        try {
            Path placeholderPath = Paths.get(System.getProperty("java.io.tmpdir"), "workflow_history.xlsx");
            if (!Files.exists(placeholderPath)) {
                Files.createFile(placeholderPath);
            }
            return new UrlResource(placeholderPath.toUri());
        } catch (IOException e) {
            throw new IllegalStateException("Error creating placeholder export file", e);
        }
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
        //dto.setInstanceId(instance.getInstanceId());
        //dto.setWorkflowId(instance.getWorkflowId());
        dto.setBusinessKey(instance.getBusinessKey());
        dto.setProcessType(instance.getProcessType());
        dto.setInitiator(instance.getInitiator());
        dto.setStatus(instance.getStatus());
        dto.setResult(instance.getResult());
        dto.setStartTime(instance.getStartTime());
        dto.setEndTime(instance.getEndTime());
        dto.setDuration(instance.getDuration());
        //dto.setRemarks(instance.getRemarks());
        
        // 获取关联的流程定义信息
        try {
//            Workflow workflow = workflowRepository.findById(instance.getWorkflowId()).orElse(null);
//            if (workflow != null) {
//                dto.setWorkflowName(workflow.getName());
//                dto.setWorkflowCategory(workflow.getCategory());
//            }
        } catch (Exception e) {
            logger.warn("Error fetching workflow details for instance: {}", instance.getId(), e);
        }
        
        return dto;
    }
} 