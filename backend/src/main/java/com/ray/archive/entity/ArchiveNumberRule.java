package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "archive_number_rules")
@EntityListeners(AuditingEntityListener.class)
public class ArchiveNumberRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String prefix;  // 前缀

    @Column(nullable = false)
    private String dateFormat;  // 日期格式，如 yyyyMMdd

    @Column(nullable = false)
    private Integer serialLength;  // 序号长度

    @Column(nullable = false)
    private Long currentSerial;  // 当前序号

    @Column(nullable = false)
    private String category;  // 适用的档案类别

    @Column(name = "separator_char")
    private String separatorChar;  // 分隔符

    @Column(nullable = false)
    private Boolean isActive = true;  // 是否启用

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    private String creator;  // 创建人
} 