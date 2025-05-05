package com.ray.archive.repository;

import com.ray.archive.entity.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 档案数据访问层
 */
@Repository
public interface ArchiveRepository extends JpaRepository<Archive, Long>, JpaSpecificationExecutor<Archive> {
    
    /**
     * 根据档案编号查找档案
     */
    Optional<Archive> findByFileNumber(String fileNumber);
    
    /**
     * 根据标题或描述查找档案（模糊查询）
     */
    List<Archive> findByTitleContainingOrDescriptionContaining(String title, String description);
    
    /**
     * 根据状态查找档案
     */
    List<Archive> findByStatus(String status);
    
    /**
     * 根据类别查找档案
     */
    List<Archive> findByCategory(String category);
    
    /**
     * 根据状态统计档案数量
     */
    long countByStatus(String status);
    
    /**
     * 统计指定日期之后创建的档案数量
     */
    long countByCreateTimeAfter(LocalDateTime startDate);
    
    /**
     * 获取最近创建的5个档案
     */
    List<Archive> findTop5ByOrderByCreateTimeDesc();
    
    /**
     * 获取借阅次数最多的5个档案
     */
    List<Archive> findTop5ByOrderByBorrowCountDesc();
    
    /**
     * 统计各类型档案数量
     */
    @Query("SELECT a.type as type, COUNT(a) as count FROM Archive a GROUP BY a.type")
    List<Map<String, Object>> countGroupByType();
    
    /**
     * 获取类型统计
     */
    @Query("SELECT a.type as type, COUNT(a) as count FROM Archive a GROUP BY a.type")
    List<Map<String, Object>> countByType();

    List<Archive> findByCreator(String creator);
    Page<Archive> findByDeleted(Boolean deleted, Pageable pageable);

    Collection<? extends Archive> findByLocation(String location);

    Collection<? extends Archive> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);

    Page<Archive> findByTitleContainingAndDeletedFalse(String title, Pageable pageable);
    Page<Archive> findByDescriptionContainingAndDeletedFalse(String description, Pageable pageable);

    Page<Archive> findAll(Pageable pageable);
    Page<Archive> findByDeletedFalse(Pageable pageable);

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
    long countByType(String type);
    long countBySharedTrue();

    @Query("SELECT a.category, COUNT(a) FROM Archive a WHERE a.deleted = false GROUP BY a.category")
    List<Object[]> countByCategories();

    @Query("SELECT a.status, COUNT(a) FROM Archive a WHERE a.deleted = false GROUP BY a.status")
    List<Object[]> countByStatuses();

    @Query("SELECT a.type, COUNT(a) FROM Archive a WHERE a.deleted = false GROUP BY a.type")
    List<Object[]> countByTypes();

    @Query("SELECT FUNCTION('FORMAT', a.createTime, 'yyyy-MM') as month, COUNT(a) FROM Archive a WHERE a.deleted = false GROUP BY month ORDER BY month DESC")
    List<Object[]> countByMonths();

    @Query(value = "SELECT * FROM archives a WHERE MATCH(title, description, keywords) AGAINST(:text IN NATURAL LANGUAGE MODE) AND a.deleted = false", 
           nativeQuery = true)
    List<Archive> fullTextSearch(@Param("text") String text);

    @Query("SELECT a FROM Archive a WHERE " +
           "(:fileNumber IS NULL OR a.fileNumber LIKE %:fileNumber%) AND " +
           "(:title IS NULL OR a.title LIKE %:title%) AND " +
           "(:type IS NULL OR a.type = :type) AND " +
           "(:category IS NULL OR a.category = :category) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "a.deleted = false")
    Page<Archive> findByFilters(@Param("fileNumber") String fileNumber, 
                              @Param("title") String title, 
                              @Param("type") String type,
                              @Param("category") String category,
                              @Param("status") String status,
                              Pageable pageable);

    List<Archive> findTop10ByDeletedFalseOrderByDownloadCountDesc();

    List<Archive> findTop10ByDeletedFalseOrderByUpdateTimeDesc();
} 