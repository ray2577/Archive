package com.ray.archive.repository;

import com.ray.archive.entity.Archive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ArchiveRepository extends JpaRepository<Archive, Long>, JpaSpecificationExecutor<Archive> {
    Optional<Archive> findByFileNumber(String fileNumber);
    List<Archive> findByCategory(String category);
    List<Archive> findByTitleContainingOrDescriptionContaining(String titleKeyword, String descriptionKeyword);
    List<Archive> findByStatus(String status);

    Collection<? extends Archive> findByLocation(String location);

    Collection<? extends Archive> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
} 