package com.ray.archive.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ArchiveRequest {
    private String title;
    private String description;
    private MultipartFile file;
} 