package com.ray.archive.repository;

import com.ray.archive.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    
    List<ChatSession> findByUserIdOrderByLastQueryTimeDesc(Long userId);
    
    List<ChatSession> findByStatus(String status);
    
    @Query("SELECT cs FROM ChatSession cs WHERE cs.status = 'ACTIVE' AND cs.lastQueryTime < :cutoffTime")
    List<ChatSession> findInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    int countByUserIdAndStatus(Long userId, String status);
} 