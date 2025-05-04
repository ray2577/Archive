package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 文档实体类
 * 用于存储系统中的各类电子文档信息
 */
@Data
@Entity
@Table(name = "documents")
@EntityListeners(AuditingEntityListener.class)
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;  // 文档编号

    @Column(nullable = false)
    private String name;  // 文档名称

    @Column(length = 2000)
    private String description;  // 文档描述

    @Column(nullable = false)
    private String type;  // 文档类型，如：1=文本文档，2=PDF文档，3=Excel表格，4=Word文档

    private String category;  // 所属分类

    private String fileUrl;  // 文件访问路径
    
    private String filePath;  // 文件存储路径
    
    private String fileType;  // 文件扩展名
    
    private Long fileSize;  // 文件大小(字节)
    
    private Integer downloadCount = 0;  // 下载次数

    @Column(nullable = false)
    private String status;  // 状态：ACTIVE, ARCHIVED, SHARED, DRAFT, DELETED

    @Column(columnDefinition = "TEXT")
    private String keywords;  // 关键词，存储为JSON字符串

    private Boolean shared = false;  // 是否已分享

    private LocalDateTime shareExpireTime;  // 分享过期时间

    @CreatedDate
    private LocalDateTime createTime;  // 创建时间

    @LastModifiedDate
    private LocalDateTime updateTime;  // 更新时间

    @Column(nullable = false)
    private String creator;  // 创建人

    private String updater;  // 更新人

    private Boolean deleted = false;  // 是否删除
} 