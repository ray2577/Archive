package com.ray.archive.service;

import com.ray.archive.entity.ArchiveNumberRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ArchiveNumberService {
    String generateArchiveNumber(String category);
    ArchiveNumberRule saveRule(ArchiveNumberRule rule);
    Optional<ArchiveNumberRule> findRuleById(Long id);
    Page<ArchiveNumberRule> findAllRules(Pageable pageable);
    void deleteRule(Long id);
    boolean existsByCategory(String category);
    ArchiveNumberRule getActiveRuleForCategory(String category);
} 