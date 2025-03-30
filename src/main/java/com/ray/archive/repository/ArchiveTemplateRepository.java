package com.ray.archive.repository;

import com.ray.archive.entity.ArchiveTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchiveTemplateRepository extends JpaRepository<ArchiveTemplate, Long> {
    Optional<ArchiveTemplate> findByName(String name);
    boolean existsByName(String name);
    List<ArchiveTemplate> findByIsActiveTrue();
} 