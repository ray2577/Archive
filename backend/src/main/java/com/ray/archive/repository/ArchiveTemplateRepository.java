package com.ray.archive.repository;

import com.ray.archive.entity.ArchiveTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 档案模板仓库接口
 */
@Repository
public interface ArchiveTemplateRepository extends JpaRepository<ArchiveTemplate, Long>, JpaSpecificationExecutor<ArchiveTemplate> {
    Optional<ArchiveTemplate> findByTemplateName(String templateName);
    
    List<ArchiveTemplate> findByCategory(String category);
    
    Page<ArchiveTemplate> findByTemplateNameContaining(String name, Pageable pageable);
    
    Page<ArchiveTemplate> findByCreator(String creator, Pageable pageable);
    
    boolean existsByTemplateName(String templateName);
    
    // 基本查询方法
    Optional<ArchiveTemplate> findByName(String name);
    boolean existsByName(String name);
    List<ArchiveTemplate> findByIsActiveTrue();
    Page<ArchiveTemplate> findByDeletedFalse(Pageable pageable);
    
    // 按类型查询
    List<ArchiveTemplate> findByTypeAndIsActiveTrue(String type);
    
    // 按类别查询
    List<ArchiveTemplate> findByCategoryAndIsActiveTrue(String category);
    
    // 组合查询
    @Query("SELECT t FROM ArchiveTemplate t WHERE " +
           "(:name IS NULL OR t.name LIKE %:name%) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:isActive IS NULL OR t.isActive = :isActive) AND " +
           "t.deleted = false")
    Page<ArchiveTemplate> findByFilters(
            @Param("name") String name,
            @Param("type") String type,
            @Param("category") String category,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
    
    // 获取最新版本的模板
    @Query("SELECT t FROM ArchiveTemplate t WHERE t.name = :name AND t.isLatestVersion = true")
    Optional<ArchiveTemplate> findLatestVersionByName(@Param("name") String name);
    
    // 获取所有版本的模板
    @Query("SELECT t FROM ArchiveTemplate t WHERE t.name = :name ORDER BY t.version DESC")
    List<ArchiveTemplate> findAllVersionsByName(@Param("name") String name);
    
    // 统计各类型模板的数量
    @Query("SELECT t.type, COUNT(t) FROM ArchiveTemplate t WHERE t.deleted = false GROUP BY t.type")
    List<Object[]> countByTypes();
    
    // 统计各类别模板的数量
    @Query("SELECT t.category, COUNT(t) FROM ArchiveTemplate t WHERE t.deleted = false GROUP BY t.category")
    List<Object[]> countByCategories();
} 