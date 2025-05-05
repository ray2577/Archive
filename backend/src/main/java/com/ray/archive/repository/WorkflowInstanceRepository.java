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

/**
 * 工作流实例仓库接口
 */
@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long>, JpaSpecificationExecutor<WorkflowInstance> {
    
    Optional<WorkflowInstance> findByProcessInstanceId(String processInstanceId);
    
    @Query("SELECT wi FROM WorkflowInstance wi WHERE wi.workflow.id = :workflowId")
    List<WorkflowInstance> findByWorkflowId(@Param("workflowId") Long workflowId);
    
    List<WorkflowInstance> findByStatus(String status);
    
    List<WorkflowInstance> findByInitiator(String initiator);
    
    Page<WorkflowInstance> findByStatusIn(List<String> statuses, Pageable pageable);
    
    Page<WorkflowInstance> findByInitiatorAndStatusIn(String initiator, List<String> statuses, Pageable pageable);
    
    @Query("SELECT wi FROM WorkflowInstance wi WHERE " +
           "(:processType IS NULL OR wi.processType = :processType) AND " +
           "(:businessKey IS NULL OR wi.businessKey = :businessKey) AND " +
           "(:initiator IS NULL OR wi.initiator = :initiator) AND " +
           "(:status IS NULL OR wi.status = :status) AND " +
           "(:startDate IS NULL OR wi.startTime >= :startDate) AND " +
           "(:endDate IS NULL OR wi.startTime <= :endDate)")
    Page<WorkflowInstance> findByFilters(
            @Param("processType") String processType,
            @Param("businessKey") String businessKey,
            @Param("initiator") String initiator,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    long countByStatus(String status);
    
    long countByInitiatorAndStatus(String initiator, String status);
    
    @Query("SELECT wi.processType, COUNT(wi) FROM WorkflowInstance wi GROUP BY wi.processType")
    List<Object[]> countByProcessTypes();
    
    @Query("SELECT wi.status, COUNT(wi) FROM WorkflowInstance wi GROUP BY wi.status")
    List<Object[]> countByStatuses();
    
    @Query("SELECT FUNCTION('FORMAT', wi.startTime, 'yyyy-MM') as month, COUNT(wi) FROM WorkflowInstance wi GROUP BY month ORDER BY month DESC")
    List<Object[]> countByMonths();
    
    @Query("SELECT wi FROM WorkflowInstance wi WHERE wi.businessKey = :businessKey AND wi.processType = :processType")
    Optional<WorkflowInstance> findByBusinessKeyAndProcessType(@Param("businessKey") String businessKey, @Param("processType") String processType);
} 