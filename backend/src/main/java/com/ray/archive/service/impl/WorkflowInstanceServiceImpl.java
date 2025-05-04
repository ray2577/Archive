package com.ray.archive.service.impl;

import com.ray.archive.dto.PageResult;
import com.ray.archive.entity.Workflow;
import com.ray.archive.entity.WorkflowInstance;
import com.ray.archive.repository.WorkflowInstanceRepository;
import com.ray.archive.repository.WorkflowRepository;
import com.ray.archive.service.WorkflowInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 工作流实例服务实现类
 */
@Service
@Transactional
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstanceServiceImpl.class);
    
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowRepository workflowRepository;

    @Autowired
    public WorkflowInstanceServiceImpl(WorkflowInstanceRepository workflowInstanceRepository, 
                                      WorkflowRepository workflowRepository) {
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowRepository = workflowRepository;
    }

    @Override
    public WorkflowInstance save(WorkflowInstance workflowInstance) {
        Assert.notNull(workflowInstance, "WorkflowInstance must not be null");
        logger.debug("Saving workflow instance: {}", workflowInstance);
        return workflowInstanceRepository.save(workflowInstance);
    }

    @Override
    public Optional<WorkflowInstance> findById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Finding workflow instance by ID: {}", id);
        return workflowInstanceRepository.findById(id);
    }

    @Override
    public Optional<WorkflowInstance> findByInstanceId(String instanceId) {
        Assert.hasText(instanceId, "Instance ID must not be empty");
        logger.debug("Finding workflow instance by instance ID: {}", instanceId);
        return workflowInstanceRepository.findByInstanceId(instanceId);
    }

    @Override
    public Page<WorkflowInstance> findAll(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");
        logger.debug("Finding all workflow instances with pageable: {}", pageable);
        return workflowInstanceRepository.findAll(pageable);
    }

    @Override
    public List<WorkflowInstance> findByWorkflowId(Long workflowId) {
        Assert.notNull(workflowId, "Workflow ID must not be null");
        logger.debug("Finding workflow instances by workflow ID: {}", workflowId);
        return workflowInstanceRepository.findByWorkflowId(workflowId);
    }

    @Override
    public List<WorkflowInstance> findByStatus(String status) {
        Assert.hasText(status, "Status must not be empty");
        logger.debug("Finding workflow instances by status: {}", status);
        return workflowInstanceRepository.findByStatus(status);
    }

    @Override
    public List<WorkflowInstance> findByInitiator(String initiator) {
        Assert.hasText(initiator, "Initiator must not be empty");
        logger.debug("Finding workflow instances by initiator: {}", initiator);
        return workflowInstanceRepository.findByInitiator(initiator);
    }

    @Override
    public Page<WorkflowInstance> findByStatusIn(List<String> statuses, Pageable pageable) {
        Assert.notEmpty(statuses, "Statuses must not be empty");
        Assert.notNull(pageable, "Pageable must not be null");
        logger.debug("Finding workflow instances by statuses: {} with pageable: {}", statuses, pageable);
        return workflowInstanceRepository.findByStatusIn(statuses, pageable);
    }

    @Override
    public Page<WorkflowInstance> findByInitiatorAndStatusIn(String initiator, List<String> statuses, Pageable pageable) {
        Assert.hasText(initiator, "Initiator must not be empty");
        Assert.notEmpty(statuses, "Statuses must not be empty");
        Assert.notNull(pageable, "Pageable must not be null");
        logger.debug("Finding workflow instances by initiator: {} and statuses: {} with pageable: {}", initiator, statuses, pageable);
        return workflowInstanceRepository.findByInitiatorAndStatusIn(initiator, statuses, pageable);
    }

    @Override
    public Optional<WorkflowInstance> findByBusinessKeyAndProcessType(String businessKey, String processType) {
        Assert.hasText(businessKey, "Business key must not be empty");
        Assert.hasText(processType, "Process type must not be empty");
        logger.debug("Finding workflow instance by business key: {} and process type: {}", businessKey, processType);
        return workflowInstanceRepository.findByBusinessKeyAndProcessType(businessKey, processType);
    }

    @Override
    public void deleteById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Deleting workflow instance by ID: {}", id);
        workflowInstanceRepository.deleteById(id);
    }

    @Override
    public WorkflowInstance startWorkflow(Long workflowId, String businessKey, String initiator, Map<String, Object> variables) {
        Assert.notNull(workflowId, "Workflow ID must not be null");
        Assert.hasText(businessKey, "Business key must not be empty");
        Assert.hasText(initiator, "Initiator must not be empty");
        logger.debug("Starting workflow instance for workflow ID: {}, business key: {}, initiator: {}", workflowId, businessKey, initiator);
        
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id: " + workflowId));
        
        WorkflowInstance instance = new WorkflowInstance();
        //instance.setInstanceId(UUID.randomUUID().toString());
        //instance.setWorkflowId(workflowId);
        instance.setBusinessKey(businessKey);
        instance.setProcessType(workflow.getProcessType());
        instance.setInitiator(initiator);
        instance.setStatus("RUNNING");
        instance.setStartTime(LocalDateTime.now());
        instance.setVariables(variables != null ? variables.toString() : null);
        
        return workflowInstanceRepository.save(instance);
    }

    @Override
    public WorkflowInstance completeWorkflow(Long id, String status, String result) {
        Assert.notNull(id, "ID must not be null");
        Assert.hasText(status, "Status must not be empty");
        logger.debug("Completing workflow instance with ID: {}, status: {}, result: {}", id, status, result);
        
        WorkflowInstance instance = workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkflowInstance not found with id: " + id));
        
        instance.setStatus(status);
        instance.setResult(result);
        instance.setEndTime(LocalDateTime.now());
        if (instance.getStartTime() != null) {
            // 计算持续时间（秒）
            long durationSeconds = java.time.Duration.between(instance.getStartTime(), instance.getEndTime()).getSeconds();
            instance.setDuration(durationSeconds);
        }
        
        return workflowInstanceRepository.save(instance);
    }

    @Override
    public WorkflowInstance cancelWorkflow(Long id, String reason) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Canceling workflow instance with ID: {}, reason: {}", id, reason);
        
        WorkflowInstance instance = workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkflowInstance not found with id: " + id));
        
        instance.setStatus("CANCELLED");
        //instance.setRemarks(reason);
        instance.setEndTime(LocalDateTime.now());
        if (instance.getStartTime() != null) {
            long durationSeconds = java.time.Duration.between(instance.getStartTime(), instance.getEndTime()).getSeconds();
            instance.setDuration(durationSeconds);
        }
        
        return workflowInstanceRepository.save(instance);
    }

    @Override
    public WorkflowInstance suspendWorkflow(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Suspending workflow instance with ID: {}", id);
        
        WorkflowInstance instance = workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkflowInstance not found with id: " + id));
        
        instance.setStatus("SUSPENDED");
        
        return workflowInstanceRepository.save(instance);
    }

    @Override
    public WorkflowInstance resumeWorkflow(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Resuming workflow instance with ID: {}", id);
        
        WorkflowInstance instance = workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkflowInstance not found with id: " + id));
        
        instance.setStatus("RUNNING");
        
        return workflowInstanceRepository.save(instance);
    }

    @Override
    public PageResult<WorkflowInstance> getInstances(int page, int pageSize, String processType, String businessKey, 
            String initiator, String status, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "startTime"));
        
        Page<WorkflowInstance> instancesPage = workflowInstanceRepository.findByFilters(
                processType, businessKey, initiator, status, startDate, endDate, pageable);
        
        return new PageResult<>(
                instancesPage.getContent(),
                instancesPage.getTotalElements(),
                page,
                pageSize,
                instancesPage.getTotalPages()
        );
    }

    @Override
    public long countByStatus(String status) {
        Assert.hasText(status, "Status must not be empty");
        logger.debug("Counting workflow instances by status: {}", status);
        return workflowInstanceRepository.countByStatus(status);
    }

    @Override
    public long countByInitiatorAndStatus(String initiator, String status) {
        Assert.hasText(initiator, "Initiator must not be empty");
        Assert.hasText(status, "Status must not be empty");
        logger.debug("Counting workflow instances by initiator: {} and status: {}", initiator, status);
        return workflowInstanceRepository.countByInitiatorAndStatus(initiator, status);
    }

    @Override
    public Map<String, Long> getProcessTypeDistribution() {
        logger.debug("Getting process type distribution");
        List<Object[]> processTypeStats = workflowInstanceRepository.countByProcessTypes();
        
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
    public Map<String, Long> getStatusDistribution() {
        logger.debug("Getting status distribution");
        List<Object[]> statusStats = workflowInstanceRepository.countByStatuses();
        
        Map<String, Long> distribution = new HashMap<>();
        if (statusStats != null) {
            for (Object[] stat : statusStats) {
                if (stat[0] != null) {
                    distribution.put(stat[0].toString(), (Long) stat[1]);
                }
            }
        }
        
        return distribution;
    }

    @Override
    public Map<String, Long> getMonthlyDistribution() {
        logger.debug("Getting monthly distribution");
        List<Object[]> monthlyStats = workflowInstanceRepository.countByMonths();
        
        Map<String, Long> distribution = new HashMap<>();
        if (monthlyStats != null) {
            for (Object[] stat : monthlyStats) {
                if (stat[0] != null) {
                    distribution.put(stat[0].toString(), (Long) stat[1]);
                }
            }
        }
        
        return distribution;
    }
} 