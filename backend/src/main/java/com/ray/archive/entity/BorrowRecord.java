package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "borrow_records")
@EntityListeners(AuditingEntityListener.class)
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "archive_id", nullable = false)
    private Archive archive;

    @Column(nullable = false)
    private String borrower; // 借阅人姓名
    
    private String borrowerDepartment; // 借阅人部门

    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate; // 借阅日期

    @Column(name = "expected_return_date")
    private LocalDateTime expectedReturnDate; // 预计归还日期

    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate; // 实际归还日期

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status; // 借阅状态

    private String purpose; // 借阅目的

    private String approvedBy; // 审批人
    
    @CreatedDate
    private LocalDateTime createTime; // 创建时间

    @LastModifiedDate
    private LocalDateTime updateTime; // 更新时间

    private String remarks; // 备注
} 