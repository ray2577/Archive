package com.ray.archive.repository;

import com.ray.archive.entity.ArchiveNumberRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArchiveNumberRuleRepository extends JpaRepository<ArchiveNumberRule, Long> {
    
    // 基本查询方法
    Optional<ArchiveNumberRule> findByCategory(String category);
    List<ArchiveNumberRule> findByIsActiveTrue();
    boolean existsByCategory(String category);
    
    // 前缀相关查询
    List<ArchiveNumberRule> findByPrefix(String prefix);
    boolean existsByPrefix(String prefix);
    
    // 活跃规则查询
    @Query("SELECT r FROM ArchiveNumberRule r WHERE r.category = :category AND r.isActive = true")
    Optional<ArchiveNumberRule> findActiveByCategoryAndIsActive(@Param("category") String category);
    
    // 序号更新（使用悲观锁确保并发安全）
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ArchiveNumberRule r WHERE r.id = :id")
    Optional<ArchiveNumberRule> findByIdWithLock(@Param("id") Long id);
    
    // 按创建时间排序查询
    List<ArchiveNumberRule> findByCategoryOrderByCreateTimeDesc(String category);
    
    // 查询特定类别的所有规则（包括历史规则）
    @Query("SELECT r FROM ArchiveNumberRule r WHERE r.category = :category ORDER BY r.isActive DESC, r.createTime DESC")
    List<ArchiveNumberRule> findAllByCategory(@Param("category") String category);
    
    // 统计每个类别的规则数量
    @Query("SELECT r.category, COUNT(r) FROM ArchiveNumberRule r GROUP BY r.category")
    List<Object[]> countRulesByCategory();
    
    // 查找重复的前缀规则
    @Query("SELECT r FROM ArchiveNumberRule r WHERE r.prefix = :prefix AND r.category != :category AND r.isActive = true")
    List<ArchiveNumberRule> findConflictingRules(@Param("prefix") String prefix, @Param("category") String category);

    Optional<Object> findByCategoryAndIsActiveTrue(String category);
} 