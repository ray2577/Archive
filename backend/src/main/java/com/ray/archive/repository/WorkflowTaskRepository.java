package com.ray.archive.repository;

import com.ray.archive.entity.WorkflowTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 工作流任务仓库接口
 */
@Repository
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long>, JpaSpecificationExecutor<WorkflowTask> {
    
    // 基本查询方法
    Optional<WorkflowTask> findByTaskId(String taskId);
    List<WorkflowTask> findByAssignee(String assignee);
    List<WorkflowTask> findByStatus(String status);
    
    // 获取指定流程实例的任务
    @Query("SELECT wt FROM WorkflowTask wt WHERE wt.workflowInstance.id = :instanceId ORDER BY wt.createTime ASC")
    List<WorkflowTask> findByWorkflowInstanceId(@Param("instanceId") Long instanceId);
    
    // 获取用户的待办任务
    @Query("SELECT wt FROM WorkflowTask wt WHERE wt.assignee = :assignee AND wt.status = 'PENDING'")
    Page<WorkflowTask> findPendingTasksByAssignee(@Param("assignee") String assignee, Pageable pageable);
    
    // 获取用户的已办任务
    @Query("SELECT wt FROM WorkflowTask wt WHERE wt.assignee = :assignee AND wt.status = 'COMPLETED'")
    Page<WorkflowTask> findCompletedTasksByAssignee(@Param("assignee") String assignee, Pageable pageable);
    
    // 组合查询
    @Query("SELECT wt FROM WorkflowTask wt JOIN wt.workflowInstance wi WHERE " +
           "(:taskName IS NULL OR wt.taskName LIKE %:taskName%) AND " +
           "(:assignee IS NULL OR wt.assignee = :assignee) AND " +
           "(:status IS NULL OR wt.status = :status) AND " +
           "(:processType IS NULL OR wi.processType = :processType) AND " +
           "(:startDate IS NULL OR wt.createTime >= :startDate) AND " +
           "(:endDate IS NULL OR wt.createTime <= :endDate)")
    Page<WorkflowTask> findByFilters(
            @Param("taskName") String taskName,
            @Param("assignee") String assignee,
            @Param("status") String status,
            @Param("processType") String processType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    // 统计用户任务数量
    @Query("SELECT wt.assignee, COUNT(wt) FROM WorkflowTask wt WHERE wt.status = 'PENDING' GROUP BY wt.assignee")
    List<Object[]> countPendingTasksByAssignee();
    
    // 统计任务平均处理时长
    @Query("SELECT AVG(wt.duration) FROM WorkflowTask wt WHERE wt.status = 'COMPLETED'")
    Double getAverageDuration();
    
    // 统计用户任务平均处理时长
    @Query("SELECT AVG(wt.duration) FROM WorkflowTask wt WHERE wt.status = 'COMPLETED' AND wt.assignee = :assignee")
    Double getAverageDurationByAssignee(@Param("assignee") String assignee);
    
    // 查找逾期任务
    @Query("SELECT wt FROM WorkflowTask wt WHERE wt.status = 'PENDING' AND wt.dueDate < :now")
    List<WorkflowTask> findOverdueTasks(@Param("now") LocalDateTime now);

    Page<WorkflowTask> findByAssigneeAndStatus(String assignee, String status, Pageable pageable);
    
    List<WorkflowTask> findByWorkflowInstanceIdAndStatus(Long workflowInstanceId, String status);
    
    long countByStatus(String status);
    
    long countByAssigneeAndStatus(String assignee, String status);
    
    List<WorkflowTask> findByDueDateBefore(LocalDateTime dueDate);
} 