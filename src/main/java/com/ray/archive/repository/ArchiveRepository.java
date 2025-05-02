package com.ray.archive.repository;

import com.ray.archive.entity.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    Page<Archive> findAll(Pageable pageable);

    @Query("SELECT a FROM Archive a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Archive> search(@Param("keyword") String keyword);

    @Query("SELECT a FROM Archive a WHERE (:category IS NULL OR a.category = :category) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:startDate IS NULL OR a.createTime >= :startDate) " +
           "AND (:endDate IS NULL OR a.createTime <= :endDate)")
    List<Archive> advancedSearch(
            @Param("category") String category,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    long countByCategory(String category);

    long countByStatus(String status);

    @Query("SELECT a.category, COUNT(a) FROM Archive a GROUP BY a.category")
    List<Object[]> countByCategories();

    @Query("SELECT a.status, COUNT(a) FROM Archive a GROUP BY a.status")
    List<Object[]> countByStatuses();

    @Query(value = "SELECT * FROM archives a WHERE MATCH(title, description, keywords) AGAINST(:text IN NATURAL LANGUAGE MODE)", 
           nativeQuery = true)
    List<Archive> fullTextSearch(@Param("text") String text);
} 