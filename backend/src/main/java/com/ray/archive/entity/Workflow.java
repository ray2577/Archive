package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 工作流实体类
 * 用于存储系统中的各类审批流程定义
 */
@Data
@Entity
@Table(name = "workflows")
@EntityListeners(AuditingEntityListener.class)
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;  // 流程编码

    @Column(nullable = false)
    private String name;  // 流程名称

    @Column(nullable = false)
    private String category;  // 流程分类：档案借阅、档案归还、档案销毁、文档审批等

    @Column(length = 2000)
    private String description;  // 流程描述

    @Column(nullable = false)
    private String status;  // 状态：ACTIVE, INACTIVE, DEPRECATED

    @Column(columnDefinition = "TEXT")
    private String definitionJson;  // 流程定义JSON，包含节点和连线

    private String processDefinitionId;  // 工作流引擎中的流程定义ID

    private String processDefinitionKey;  // 工作流引擎中的流程定义Key

    private Integer version = 1;  // 版本号

    private Boolean isLatestVersion = true;  // 是否最新版本

    @CreatedDate
    private LocalDateTime createTime;  // 创建时间

    @LastModifiedDate
    private LocalDateTime updateTime;  // 更新时间

    @Column(nullable = false)
    private String creator;  // 创建人

    private String updater;  // 更新人

    private Boolean deleted = false;  // 是否删除

    private LocalDateTime deployTime;
    private boolean IsDeployed;
    private boolean IsActive;

    private String processType;

    private String definition;
} 