package com.ray.archive.repository;

import com.ray.archive.entity.WorkflowInstance;
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

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long>, JpaSpecificationExecutor<WorkflowInstance> {
    
    // 基本查询方法
    Optional<WorkflowInstance> findByProcessInstanceId(String processInstanceId);
    List<WorkflowInstance> findByInitiator(String initiator);
    List<WorkflowInstance> findByStatus(String status);
    List<WorkflowInstance> findByResult(String result);
    
    // 按业务键查询
    Optional<WorkflowInstance> findByBusinessKey(String businessKey);
    
    // 按流程类型查询
    Page<WorkflowInstance> findByProcessType(String processType, Pageable pageable);
    
    // 组合查询
    @Query("SELECT wi FROM WorkflowInstance wi WHERE " +
           "(:processName IS NULL OR wi.workflow.name LIKE %:processName%) AND " +
           "(:initiator IS NULL OR wi.initiator = :initiator) AND " +
           "(:processType IS NULL OR wi.processType = :processType) AND " +
           "(:status IS NULL OR wi.status = :status) AND " +
           "(:result IS NULL OR wi.result = :result) AND " +
           "(:startDate IS NULL OR wi.startTime >= :startDate) AND " +
           "(:endDate IS NULL OR wi.startTime <= :endDate)")
    Page<WorkflowInstance> findByFilters(
            @Param("processName") String processName,
            @Param("initiator") String initiator,
            @Param("processType") String processType,
            @Param("status") String status,
            @Param("result") String result,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    // 获取当前任务为特定任务的实例
    List<WorkflowInstance> findByCurrentTask(String taskName);
    
    // 获取当前处理人的实例
    List<WorkflowInstance> findByCurrentAssignee(String assignee);
    
    // 统计各类型流程的数量
    @Query("SELECT wi.processType, COUNT(wi) FROM WorkflowInstance wi GROUP BY wi.processType")
    List<Object[]> countByProcessTypes();
    
    // 统计每日流程启动数量
    @Query("SELECT FUNCTION('DATE', wi.startTime) as startDate, COUNT(wi) FROM WorkflowInstance wi " +
           "WHERE wi.startTime BETWEEN :startDate AND :endDate " +
           "GROUP BY startDate ORDER BY startDate")
    List<Object[]> countDailyStarted(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // 统计流程平均耗时
    @Query("SELECT AVG(wi.duration) FROM WorkflowInstance wi WHERE wi.status = 'COMPLETED'")
    Double getAverageDuration();
    
    // 统计特定流程类型的平均耗时
    @Query("SELECT AVG(wi.duration) FROM WorkflowInstance wi WHERE wi.status = 'COMPLETED' AND wi.processType = :processType")
    Double getAverageDurationByType(@Param("processType") String processType);
} 