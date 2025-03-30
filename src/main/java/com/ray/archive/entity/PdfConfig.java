package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pdf_configs")
@EntityListeners(AuditingEntityListener.class)
public class PdfConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;        // 配置名称

    private String description; // 配置描述

    @Column(nullable = false)
    private String watermarkText; // 水印文本

    @Column(nullable = false)
    private String watermarkColor = "#000000"; // 水印颜色

    @Column(nullable = false)
    private Float watermarkOpacity = 0.3f; // 水印透明度

    @Column(nullable = false)
    private Double watermarkRotation = 45.0; // 水印旋转角度

    private String userPassword;  // PDF用户密码

    private String ownerPassword; // PDF管理员密码

    private Boolean isDefault = false; // 是否为默认配置

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    private String creator;    // 创建人

    @Column(nullable = false)
    private Boolean isActive = true; // 是否启用
} 