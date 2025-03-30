package com.ray.archive.service;

import com.ray.archive.entity.ArchiveTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArchiveTemplateService {
    /**
     * 保存档案模板
     */
    ArchiveTemplate save(ArchiveTemplate template);

    /**
     * 根据ID查找模板
     */
    Optional<ArchiveTemplate> findById(Long id);

    /**
     * 分页查询所有模板
     */
    Page<ArchiveTemplate> findAll(Pageable pageable);

    /**
     * 删除模板
     */
    void deleteById(Long id);

    /**
     * 更新模板状态
     */
    void updateStatus(Long id, boolean isActive);

    /**
     * 根据名称查找模板
     */
    Optional<ArchiveTemplate> findByName(String name);

    /**
     * 获取所有活跃的模板
     */
    List<ArchiveTemplate> findAllActive();

    /**
     * 获取特定模板的内容
     */
    String getTemplateContent(Long id);

    /**
     * 验证模板名称是否已存在
     */
    boolean existsByName(String name);

    /**
     * 获取模板用于生成PDF
     */
    ArchiveTemplate getTemplateById(Long id);
} 