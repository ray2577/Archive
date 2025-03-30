package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "archive_template")
public class ArchiveTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String content;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
} 