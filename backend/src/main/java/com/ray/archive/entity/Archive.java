package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "archives")
@EntityListeners(AuditingEntityListener.class)
public class Archive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String type;

    @Column(nullable = false)
    private String category;    // 档案类别

    @Column(nullable = false)
    private String fileNumber;  // 档案编号

    @Column(nullable = false)
    private String status;      // 档案状态 (AVAILABLE, BORROWED, PROCESSING)

    private String location;    // 存放位置

    @Column(columnDefinition = "TEXT")
    private String keywords;    // 关键词，存储为JSON字符串

    private String responsible; // 负责人

    @Column(length = 2000)
    private String remarks;     // 备注

    // 档案文件相关属性
    private String fileType;    // 文件类型 (pdf, word, excel, image等)
    private String fileUrl;     // 文件访问路径
    private Long fileSize;      // 文件大小(字节)

    // 审计字段
    private String creator;     // 创建人
    
    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;
} 