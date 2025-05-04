package com.ray.archive.repository;

import com.ray.archive.entity.PdfConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PdfConfigRepository extends JpaRepository<PdfConfig, Long> {
    Optional<PdfConfig> findByIsDefaultTrue();
    boolean existsByName(String name);
} 