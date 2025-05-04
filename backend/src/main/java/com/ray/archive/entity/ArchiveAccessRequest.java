package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "archive_access_requests")
@EntityListeners(AuditingEntityListener.class)
public class ArchiveAccessRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "archive_id", nullable = false)
    private Archive archive;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(nullable = false)
    private String purpose;  // 查阅目的

    @Column(nullable = false)
    private String status;  // PENDING, APPROVED, REJECTED

    private String rejectReason;  // 拒绝原因

    private LocalDateTime approvalTime;  // 审批时间

    private LocalDateTime accessStartTime;  // 查阅开始时间

    private LocalDateTime accessEndTime;    // 查阅结束时间

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @Column(length = 1000)
    private String remarks;  // 备注
} 