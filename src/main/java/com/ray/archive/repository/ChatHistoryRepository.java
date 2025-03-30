package com.ray.archive.repository;

import com.ray.archive.entity.ChatHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    
    // 基本查询方法
    List<ChatHistory> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
    List<ChatHistory> findByUserId(Long userId);
    List<ChatHistory> findByQueryType(String queryType);
    
    // 时间范围查询
    List<ChatHistory> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 组合查询
    List<ChatHistory> findByUserIdAndQueryType(Long userId, String queryType);
    List<ChatHistory> findByUserIdAndIsHelpful(Long userId, Boolean isHelpful);
    
    // 最近对话查询
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.user.id = :userId ORDER BY ch.createTime DESC")
    List<ChatHistory> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // 热门查询统计
    @Query("SELECT ch.query, COUNT(ch) as count FROM ChatHistory ch " +
           "GROUP BY ch.query ORDER BY count DESC")
    List<String> findHotQueries(Pageable pageable);
    
    // 用户反馈统计
    @Query("SELECT COUNT(ch) FROM ChatHistory ch WHERE ch.user.id = :userId AND ch.isHelpful = true")
    long countHelpfulResponses(@Param("userId") Long userId);
    
    // 相关档案查询
    List<ChatHistory> findByRelevantArchiveIdListContaining(Long archiveId);
    
    // 处理时间统计
    @Query("SELECT AVG(ch.processingTime) FROM ChatHistory ch WHERE ch.user.id = :userId")
    Double getAverageProcessingTime(@Param("userId") Long userId);
    
    // 按日期统计查询量
    @Query("SELECT FUNCTION('DATE', ch.createTime) as date, COUNT(ch) " +
           "FROM ChatHistory ch " +
           "WHERE ch.createTime BETWEEN :startTime AND :endTime " +
           "GROUP BY date ORDER BY date")
    List<Object[]> countQueriesByDate(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
} 