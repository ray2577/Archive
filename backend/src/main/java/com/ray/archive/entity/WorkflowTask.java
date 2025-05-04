package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 工作流任务实体类
 * 用于存储工作流任务信息
 */
@Data
@Entity
@Table(name = "workflow_tasks")
@EntityListeners(AuditingEntityListener.class)
public class WorkflowTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workflow_instance_id", nullable = false)
    private WorkflowInstance workflowInstance;  // 所属流程实例

    @Column(nullable = false)
    private String taskId;  // 任务ID

    @Column(nullable = false)
    private String taskName;  // 任务名称

    @Column(nullable = false)
    private String taskDefinitionKey;  // 任务定义Key

    @Column(nullable = false)
    private String assignee;  // 处理人

    private String owner;  // 任务所有者

    @Column(nullable = false)
    private String status;  // 状态：PENDING, COMPLETED, CANCELED

    @CreatedDate
    private LocalDateTime createTime;  // 创建时间

    private LocalDateTime completeTime;  // 完成时间

    private Long duration;  // 处理时长（毫秒）

    @Column(length = 2000)
    private String comment;  // 处理意见

    @Column(columnDefinition = "TEXT")
    private String formData;  // 表单数据JSON

    private String outcome;  // 处理结果：approve, reject, transfer

    private String delegatedTo;  // 委派给谁

    private String transferredTo;  // 转交给谁

    private String transferReason;  // 转交原因

    @LastModifiedDate
    private LocalDateTime updateTime;  // 更新时间
    
    private Integer priority = 0;  // 优先级: 0=低, 1=中, 2=高, 3=紧急
    
    private LocalDateTime dueDate;  // 截止日期
} 