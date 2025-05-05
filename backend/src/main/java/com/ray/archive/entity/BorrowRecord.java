package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 借阅用户对象

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
    @Column(name = "status", nullable = false, length = 20)
    private BorrowStatus status = BorrowStatus.PENDING; // 借阅状态，设置默认值

    private String purpose; // 借阅目的

    private String approvedBy; // 审批人
    
    @CreatedDate
    private LocalDateTime createTime; // 创建时间

    @LastModifiedDate
    private LocalDateTime updateTime; // 更新时间

    private String remarks; // 备注
    
    // 兼容方法 - 获取状态的字符串表示
    @Transient 
    public String getStatusString() {
        return this.status != null ? this.status.name() : null;
    }
    
    // 从字符串设置状态
    public void setStatusString(String statusStr) {
        this.status = BorrowStatus.fromValue(statusStr);
    }
    
    // Convenience getter/setter for service compatibility
    public LocalDateTime getBorrowTime() {
        return this.borrowDate;
    }
    
    public void setBorrowTime(LocalDateTime borrowTime) {
        this.borrowDate = borrowTime;
    }
    
    public LocalDateTime getPlannedReturnTime() {
        return this.expectedReturnDate;
    }
    
    public void setPlannedReturnTime(LocalDateTime plannedReturnTime) {
        this.expectedReturnDate = plannedReturnTime;
    }
    
    public LocalDateTime getReturnTime() {
        return this.actualReturnDate;
    }
    
    public void setReturnTime(LocalDateTime returnTime) {
        this.actualReturnDate = returnTime;
    }
} 