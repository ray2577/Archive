package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private String fileNumber;  // 档案编号

    @Column(nullable = false)
    private String category;    // 档案类别

    private String location;    // 存放位置

    private String keywords;    // 关键词

    @Column(nullable = false)
    private String status;      // 档案状态

    private String creator;     // 创建人

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    private String attachmentPath;  // 附件路径

    @Column(length = 2000)
    private String remarks;     // 备注
} 