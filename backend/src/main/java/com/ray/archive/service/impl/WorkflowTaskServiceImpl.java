package com.ray.archive.service.impl;

import com.ray.archive.dto.PageResult;
import com.ray.archive.entity.WorkflowTask;
import com.ray.archive.repository.WorkflowTaskRepository;
import com.ray.archive.service.WorkflowTaskService;
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
import java.util.List;
import java.util.Optional;

/**
 * 工作流任务服务实现类
 */
@Service
@Transactional
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowTaskServiceImpl.class);
    
    private final WorkflowTaskRepository workflowTaskRepository;

    @Autowired
    public WorkflowTaskServiceImpl(WorkflowTaskRepository workflowTaskRepository) {
        this.workflowTaskRepository = workflowTaskRepository;
    }

    @Override
    public WorkflowTask save(WorkflowTask workflowTask) {
        Assert.notNull(workflowTask, "WorkflowTask must not be null");
        logger.debug("Saving workflow task: {}", workflowTask);
        return workflowTaskRepository.save(workflowTask);
    }

    @Override
    public Optional<WorkflowTask> findById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Finding workflow task by ID: {}", id);
        return workflowTaskRepository.findById(id);
    }

    @Override
    public Page<WorkflowTask> findAll(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");
        logger.debug("Finding all workflow tasks with pageable: {}", pageable);
        return workflowTaskRepository.findAll(pageable);
    }

    @Override
    public List<WorkflowTask> findByWorkflowInstanceId(Long workflowInstanceId) {
        Assert.notNull(workflowInstanceId, "Workflow instance ID must not be null");
        logger.debug("Finding workflow tasks by workflow instance ID: {}", workflowInstanceId);
        return workflowTaskRepository.findByWorkflowInstanceId(workflowInstanceId);
    }

    @Override
    public List<WorkflowTask> findByAssignee(String assignee) {
        Assert.hasText(assignee, "Assignee must not be empty");
        logger.debug("Finding workflow tasks by assignee: {}", assignee);
        return workflowTaskRepository.findByAssignee(assignee);
    }

    @Override
    public List<WorkflowTask> findByStatus(String status) {
        Assert.hasText(status, "Status must not be empty");
        logger.debug("Finding workflow tasks by status: {}", status);
        return workflowTaskRepository.findByStatus(status);
    }

    @Override
    public Page<WorkflowTask> findByAssigneeAndStatus(String assignee, String status, Pageable pageable) {
        Assert.hasText(assignee, "Assignee must not be empty");
        Assert.hasText(status, "Status must not be empty");
        Assert.notNull(pageable, "Pageable must not be null");
        logger.debug("Finding workflow tasks by assignee: {} and status: {} with pageable: {}", assignee, status, pageable);
        return workflowTaskRepository.findByAssigneeAndStatus(assignee, status, pageable);
    }

    @Override
    public List<WorkflowTask> findByWorkflowInstanceIdAndStatus(Long workflowInstanceId, String status) {
        Assert.notNull(workflowInstanceId, "Workflow instance ID must not be null");
        Assert.hasText(status, "Status must not be empty");
        logger.debug("Finding workflow tasks by workflow instance ID: {} and status: {}", workflowInstanceId, status);
        return workflowTaskRepository.findByWorkflowInstanceIdAndStatus(workflowInstanceId, status);
    }

    @Override
    public void deleteById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Deleting workflow task by ID: {}", id);
        workflowTaskRepository.deleteById(id);
    }

    @Override
    public PageResult<WorkflowTask> getTasks(int page, int pageSize, String assignee, String status) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<WorkflowTask> tasksPage;
//        if (assignee != null && !assignee.isEmpty() && status != null && !status.isEmpty()) {
//            tasksPage = workflowTaskRepository.findByAssigneeAndStatus(assignee, status, pageable);
//        } else if (assignee != null && !assignee.isEmpty()) {
//            tasksPage = workflowTaskRepository.findByAssignee(assignee, pageable);
//        } else if (status != null && !status.isEmpty()) {
//            tasksPage = workflowTaskRepository.findByStatus(status, pageable);
//        } else {
//            tasksPage = workflowTaskRepository.findAll(pageable);
//        }
        
        return new PageResult<>(
        );
    }

    @Override
    public WorkflowTask completeTask(Long id, String remarks) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Completing workflow task with ID: {} and remarks: {}", id, remarks);
        
        WorkflowTask task = workflowTaskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));
        
        task.setStatus("COMPLETED");
        //task.setRemarks(remarks);
        //task.setCompletionTime(LocalDateTime.now());
        
        return workflowTaskRepository.save(task);
    }

    @Override
    public WorkflowTask assignTask(Long id, String assignee) {
        Assert.notNull(id, "ID must not be null");
        Assert.hasText(assignee, "Assignee must not be empty");
        logger.debug("Assigning workflow task with ID: {} to assignee: {}", id, assignee);
        
        WorkflowTask task = workflowTaskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));
        
        task.setAssignee(assignee);
        task.setStatus("ASSIGNED");
        
        return workflowTaskRepository.save(task);
    }

    @Override
    public List<WorkflowTask> findOverdueTasks() {
        logger.debug("Finding overdue workflow tasks");
        return workflowTaskRepository.findOverdueTasks(LocalDateTime.now());
    }

    @Override
    public List<WorkflowTask> findByDueDateBefore(LocalDateTime dueDate) {
        Assert.notNull(dueDate, "Due date must not be null");
        logger.debug("Finding workflow tasks with due date before: {}", dueDate);
        return workflowTaskRepository.findByDueDateBefore(dueDate);
    }

    @Override
    public long countByStatus(String status) {
        Assert.hasText(status, "Status must not be empty");
        logger.debug("Counting workflow tasks by status: {}", status);
        return workflowTaskRepository.countByStatus(status);
    }

    @Override
    public long countByAssigneeAndStatus(String assignee, String status) {
        Assert.hasText(assignee, "Assignee must not be empty");
        Assert.hasText(status, "Status must not be empty");
        logger.debug("Counting workflow tasks by assignee: {} and status: {}", assignee, status);
        return workflowTaskRepository.countByAssigneeAndStatus(assignee, status);
    }
} 