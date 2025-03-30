package com.ray.archive.service;

import com.ray.archive.entity.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArchiveService {
    Archive save(Archive archive);
    Optional<Archive> findById(Long id);
    Optional<Archive> findByFileNumber(String fileNumber);
    Page<Archive> findAll(Pageable pageable);
    List<Archive> findByCategory(String category);
    List<Archive> search(String keyword);
    void deleteById(Long id);
    boolean existsByFileNumber(String fileNumber);
    List<Archive> findByStatus(String status);
} 