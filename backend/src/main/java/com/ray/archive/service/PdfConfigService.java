package com.ray.archive.service;

import com.ray.archive.entity.PdfConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PdfConfigService {
    PdfConfig save(PdfConfig config);
    Optional<PdfConfig> findById(Long id);
    Page<PdfConfig> findAll(Pageable pageable);
    void deleteById(Long id);
    PdfConfig getDefaultConfig();
    void setDefaultConfig(Long id);
    boolean existsByName(String name);
} 