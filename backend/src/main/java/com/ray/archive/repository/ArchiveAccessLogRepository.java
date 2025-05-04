package com.ray.archive.repository;

import com.ray.archive.entity.ArchiveAccessLog;
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
public interface ArchiveAccessLogRepository extends JpaRepository<ArchiveAccessLog, Long> {
    
    // 基本查询方法
    List<ArchiveAccessLog> findByArchive(Archive archive);
    List<ArchiveAccessLog> findByUser(User user);
    List<ArchiveAccessLog> findByAccessType(String accessType);
    
    // 关联ID查询方法
    List<ArchiveAccessLog> findByArchive_Id(Long archiveId);
    List<ArchiveAccessLog> findByUser_Id(Long userId);
    List<ArchiveAccessLog> findByRequest_Id(Long requestId);
    
    // 时间范围查询
    List<ArchiveAccessLog> findByAccessTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 分页查询
    Page<ArchiveAccessLog> findByArchiveId(Long archiveId, Pageable pageable);
    Page<ArchiveAccessLog> findByUserId(Long userId, Pageable pageable);
    
    // IP和设备查询
    List<ArchiveAccessLog> findByIpAddress(String ipAddress);
    List<ArchiveAccessLog> findByDeviceInfo(String deviceInfo);
    
    // 统计方法
    long countByArchiveId(Long archiveId);
    long countByUserId(Long userId);
    long countByAccessType(String accessType);
    
    // 最近访问记录
    @Query("SELECT l FROM ArchiveAccessLog l WHERE l.archive.id = :archiveId ORDER BY l.accessTime DESC")
    List<ArchiveAccessLog> findRecentAccessLogs(@Param("archiveId") Long archiveId, Pageable pageable);
    
    // 特定用户对特定档案的访问记录
    List<ArchiveAccessLog> findByUserIdAndArchiveId(Long userId, Long archiveId);
    
    // 时间范围内的访问统计
    @Query("SELECT COUNT(l) FROM ArchiveAccessLog l WHERE l.accessTime BETWEEN :startTime AND :endTime")
    long countAccessInTimeRange(@Param("startTime") LocalDateTime startTime, 
                              @Param("endTime") LocalDateTime endTime);
    
    // 访问频率最高的档案
    @Query("SELECT l.archive.id, COUNT(l) as accessCount FROM ArchiveAccessLog l " +
           "GROUP BY l.archive.id ORDER BY accessCount DESC")
    List<Object[]> findMostAccessedArchives(Pageable pageable);
    
    // 按日期统计访问量
    @Query("SELECT FUNCTION('DATE', l.accessTime) as accessDate, COUNT(l) " +
           "FROM ArchiveAccessLog l " +
           "WHERE l.accessTime BETWEEN :startTime AND :endTime " +
           "GROUP BY accessDate ORDER BY accessDate")
    List<Object[]> countAccessByDate(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
} 