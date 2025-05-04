package com.ray.archive.repository;

import com.ray.archive.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    
    // 基本查询方法
    Optional<Document> findByCode(String code);
    List<Document> findByCreator(String creator);
    Page<Document> findByDeleted(Boolean deleted, Pageable pageable);
    
    // 名称和描述模糊查询
    Page<Document> findByNameContainingAndDeletedFalse(String name, Pageable pageable);
    Page<Document> findByDescriptionContainingAndDeletedFalse(String description, Pageable pageable);
    
    // 组合查询
    @Query("SELECT d FROM Document d WHERE " +
           "(:code IS NULL OR d.code LIKE %:code%) AND " +
           "(:name IS NULL OR d.name LIKE %:name%) AND " +
           "(:type IS NULL OR d.type = :type) AND " +
           "(:category IS NULL OR d.category = :category) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "d.deleted = false")
    Page<Document> findByFilters(@Param("code") String code, 
                              @Param("name") String name, 
                              @Param("type") String type,
                              @Param("category") String category,
                              @Param("status") String status,
                              Pageable pageable);
    
    // 统计相关查询
    long countByType(String type);
    long countByStatus(String status);
    long countByCategory(String category);
    long countByCreateTimeAfter(LocalDateTime startTime);
    long countBySharedTrue();
    
    // 分组统计
    @Query("SELECT d.type, COUNT(d) FROM Document d WHERE d.deleted = false GROUP BY d.type")
    List<Object[]> countByTypes();
    
    @Query("SELECT d.category, COUNT(d) FROM Document d WHERE d.deleted = false GROUP BY d.category")
    List<Object[]> countByCategories();
    
    @Query("SELECT d.status, COUNT(d) FROM Document d WHERE d.deleted = false GROUP BY d.status")
    List<Object[]> countByStatuses();
    
    // 找出最近更新的文档
    List<Document> findTop10ByDeletedFalseOrderByUpdateTimeDesc();
    
    // 找出热门下载文档
    List<Document> findTop10ByDeletedFalseOrderByDownloadCountDesc();
} 