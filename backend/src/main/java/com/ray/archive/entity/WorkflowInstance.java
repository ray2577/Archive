package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工作流实例实体类
 * 用于存储工作流执行的实例记录
 */
@Data
@Entity
@Table(name = "workflow_instances")
@EntityListeners(AuditingEntityListener.class)
public class WorkflowInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;  // 所属流程定义

    @Column(nullable = false)
    private String businessKey;  // 业务标识

    @Column(nullable = false)
    private String processInstanceId;  // 流程实例ID

    @Column(nullable = false)
    private String processType;  // 流程类型

    @Column(nullable = false)
    private String initiator;  // 发起人

    @Column(nullable = false)
    private String status;  // 状态：RUNNING, COMPLETED, CANCELED, TERMINATED

    private String result;  // 结果：approved(通过), rejected(拒绝)

    @Column(columnDefinition = "TEXT")
    private String businessData;  // 业务数据JSON

    @Column(columnDefinition = "TEXT")
    private String variables;  // 流程变量JSON

    @CreatedDate
    private LocalDateTime startTime;  // 开始时间

    private LocalDateTime endTime;  // 结束时间
    
    // 计算耗时（毫秒）
    private Long duration;  // 耗时（毫秒）

    @LastModifiedDate
    private LocalDateTime updateTime;  // 更新时间

    private String currentTask;  // 当前任务
    
    private String currentAssignee;  // 当前处理人
    
    @Column(length = 2000)
    private String comment;  // 流程备注
    
    private Integer priority = 0;  // 优先级: 0=低, 1=中, 2=高, 3=紧急

    private Map content;
} 