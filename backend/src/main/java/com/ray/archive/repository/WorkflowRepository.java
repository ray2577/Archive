package com.ray.archive.repository;

import com.ray.archive.entity.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long>, JpaSpecificationExecutor<Workflow> {
    
    // 基本查询方法
    Optional<Workflow> findByCode(String code);
    List<Workflow> findByCategory(String category);
    Page<Workflow> findByDeletedFalse(Pageable pageable);
    
    // 查询启用的流程
    List<Workflow> findByStatusAndDeletedFalse(String status, Pageable pageable);
    
    // 按名称模糊查询
    Page<Workflow> findByNameContainingAndDeletedFalse(String name, Pageable pageable);
    
    // 组合查询
    @Query("SELECT w FROM Workflow w WHERE " +
           "(:name IS NULL OR w.name LIKE %:name%) AND " +
           "(:category IS NULL OR w.category = :category) AND " +
           "(:status IS NULL OR w.status = :status) AND " +
           "w.deleted = false")
    Page<Workflow> findByFilters(@Param("name") String name, 
                              @Param("category") String category, 
                              @Param("status") String status,
                              Pageable pageable);
    
    // 获取最新版本的流程
    @Query("SELECT w FROM Workflow w WHERE w.processDefinitionKey = :key AND w.isLatestVersion = true")
    Optional<Workflow> findLatestVersionByKey(@Param("key") String key);
    
    // 获取所有版本的流程
    @Query("SELECT w FROM Workflow w WHERE w.processDefinitionKey = :key ORDER BY w.version DESC")
    List<Workflow> findAllVersionsByKey(@Param("key") String key);
    
    // 统计各分类的流程数量
    @Query("SELECT w.category, COUNT(w) FROM Workflow w WHERE w.deleted = false GROUP BY w.category")
    List<Object[]> countByCategories();
    
    // 检查流程编码是否存在
    boolean existsByCodeAndIdNot(String code, Long id);
} 