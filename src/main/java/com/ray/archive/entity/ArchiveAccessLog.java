package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "archive_access_logs")
@EntityListeners(AuditingEntityListener.class)
public class ArchiveAccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "archive_id", nullable = false)
    private Archive archive;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private ArchiveAccessRequest request;

    @Column(nullable = false)
    private String accessType;  // VIEW, DOWNLOAD, PRINT

    @CreatedDate
    private LocalDateTime accessTime;

    private String ipAddress;  // 访问IP

    private String deviceInfo;  // 设备信息

    @Column(length = 1000)
    private String remarks;  // 备注
} 