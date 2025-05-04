package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_sessions")
public class ChatSession {
    
    @Id
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "last_query_time")
    private LocalDateTime lastQueryTime;
    
    @Column(name = "query_count")
    private Integer queryCount;
    
    @Column(name = "context_data", columnDefinition = "json")
    private String contextData;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (queryCount == null) {
            queryCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
} 