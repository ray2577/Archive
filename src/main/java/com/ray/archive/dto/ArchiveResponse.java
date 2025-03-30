package com.ray.archive.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArchiveResponse {
    private Long id;
    private String title;
    private String description;
    private String fileType;
    private String filePath;
    private LocalDateTime createTime;
    private String createdByUsername;
} 