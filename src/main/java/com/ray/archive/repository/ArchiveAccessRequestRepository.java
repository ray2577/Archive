package com.ray.archive.repository;

import com.ray.archive.entity.ArchiveAccessRequest;
import com.ray.archive.entity.Archive;
import com.ray.archive.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArchiveAccessRequestRepository extends JpaRepository<ArchiveAccessRequest, Long> {
    
    // 基本查询方法
    List<ArchiveAccessRequest> findByArchive(Archive archive);
    List<ArchiveAccessRequest> findByRequester(User requester);
    List<ArchiveAccessRequest> findByApprover(User approver);
    List<ArchiveAccessRequest> findByStatus(String status);
    
    // 关联ID查询方法
    List<ArchiveAccessRequest> findByArchive_Id(Long archiveId);
    List<ArchiveAccessRequest> findByRequester_Id(Long requesterId);
    List<ArchiveAccessRequest> findByApprover_Id(Long approverId);
    
    // 组合查询方法
    List<ArchiveAccessRequest> findByRequesterAndStatus(User requester, String status);
    List<ArchiveAccessRequest> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 自定义查询方法
    @Query("SELECT r FROM ArchiveAccessRequest r WHERE r.status = 'PENDING' ORDER BY r.createTime ASC")
    List<ArchiveAccessRequest> findPendingRequests();
    
    @Query("SELECT r FROM ArchiveAccessRequest r WHERE r.status = 'APPROVED' AND r.accessEndTime < :currentTime")
    List<ArchiveAccessRequest> findExpiredRequests(@Param("currentTime") LocalDateTime currentTime);
    
    // 分页查询
    Page<ArchiveAccessRequest> findByRequesterId(Long requesterId, Pageable pageable);
    
    // 活跃申请查询
    @Query("SELECT r FROM ArchiveAccessRequest r WHERE r.archive.id = :archiveId AND r.status = 'APPROVED' AND r.accessEndTime > :currentTime")
    List<ArchiveAccessRequest> findActiveRequestsByArchiveId(@Param("archiveId") Long archiveId, @Param("currentTime") LocalDateTime currentTime);
    
    // 统计方法
    long countByRequesterId(Long requesterId);
    long countByStatus(String status);
    
    // 权限检查方法
    @Query("SELECT COUNT(r) > 0 FROM ArchiveAccessRequest r " +
           "WHERE r.requester.id = :userId AND r.archive.id = :archiveId " +
           "AND r.status = 'APPROVED' AND r.accessEndTime > :currentTime")
    boolean hasActiveAccess(@Param("userId") Long userId, 
                          @Param("archiveId") Long archiveId, 
                          @Param("currentTime") LocalDateTime currentTime);
    
    // 检查是否存在有效申请
    boolean existsByRequesterIdAndArchiveIdAndStatusIn(Long requesterId, 
                                                     Long archiveId, 
                                                     List<String> statuses);
} 