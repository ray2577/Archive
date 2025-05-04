package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 档案模板实体类
 * 用于存储系统中的各类档案模板信息
 */
@Data
@Entity
@Table(name = "archive_templates")
@EntityListeners(AuditingEntityListener.class)
public class ArchiveTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;  // 模板名称

    @Column(nullable = false)
    private String type;  // 模板类型：WORD, PDF, HTML, EXCEL

    @Column(length = 2000)
    private String description;  // 模板描述

    @Column(nullable = false)
    private String category;  // 适用档案类别

    private String fileUrl;  // 模板文件URL
    
    private String filePath;  // 模板文件存储路径
    
    private String thumbnail;  // 缩略图URL

    @Column(columnDefinition = "TEXT")
    private String content;  // 模板内容，HTML或JSON格式

    @Column(columnDefinition = "TEXT")
    private String schema;  // 模板字段定义，JSON格式

    @Column(nullable = false)
    private Boolean isActive = true;  // 是否启用

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
} 