package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 档案实体类
 * 用于存储系统中的物理和电子档案信息
 */
@Data
@Entity
@Table(name = "archives")
@EntityListeners(AuditingEntityListener.class)
public class Archive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;  // 档案标题

    @Column(length = 2000)
    private String description;  // 档案描述

    private String type;  // 档案类型

    @Column(nullable = false)
    private String category;  // 档案类别

    @Column(nullable = false, unique = true)
    private String fileNumber;  // 档案编号

    @Column(nullable = false)
    private String status;  // 档案状态 (AVAILABLE, BORROWED, PROCESSING, ARCHIVED, SHARED, DRAFT, DELETED)

    private String location;  // 存放位置

    @Column(columnDefinition = "TEXT")
    private String keywords;  // 关键词，存储为JSON字符串

    private String responsible;  // 负责人

    @Column(length = 2000)
    private String remarks;  // 备注

    // 档案文件相关属性
    private String fileType;  // 文件类型 (pdf, word, excel, image等)
    private String fileUrl;  // 文件访问路径
    private String filePath;  // 文件存储路径
    private Long fileSize;  // 文件大小(字节)
    private Integer downloadCount = 0;  // 下载次数

    // 分享相关属性
    private Boolean shared = false;  // 是否已分享
    private String shareToken;  // 分享令牌
    private LocalDateTime shareExpireTime;  // 分享过期时间
    
    // 借阅相关
    private Integer borrowCount = 0;  // 借阅次数
    
    // 档案日期相关
    private LocalDateTime archiveDate;  // 归档日期
    private LocalDateTime expirationDate;  // 过期日期
    private LocalDateTime lastDownloadTime;  // 最后下载时间

    // 审计字段
    @Column(nullable = false)
    private String creator;  // 创建人
    
    private String updater;  // 更新人
    
    @CreatedDate
    private LocalDateTime createTime;  // 创建时间

    @LastModifiedDate
    private LocalDateTime updateTime;  // 更新时间
    
    private Boolean deleted = false;  // 是否删除
} 