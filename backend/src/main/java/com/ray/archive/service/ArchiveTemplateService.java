package com.ray.archive.service;

import com.ray.archive.dto.ArchiveTemplateDTO;
import com.ray.archive.entity.ArchiveTemplate;
import com.ray.archive.dto.PageResult;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 档案模板服务接口
 */
public interface ArchiveTemplateService {
    /**
     * 保存档案模板
     * @param template 档案模板
     * @return 保存后的档案模板
     */
    ArchiveTemplate save(ArchiveTemplate template);
    
    /**
     * 根据ID查询档案模板
     * @param id 档案模板ID
     * @return 档案模板
     */
    Optional<ArchiveTemplate> findById(Long id);
    
    /**
     * 根据模板名称查询档案模板
     * @param templateName 模板名称
     * @return 档案模板
     */
    Optional<ArchiveTemplate> findByTemplateName(String templateName);
    
    /**
     * 分页查询所有档案模板
     * @param pageable 分页参数
     * @return 档案模板分页结果
     */
    Page<ArchiveTemplate> findAll(Pageable pageable);
    
    /**
     * 根据分类查询档案模板
     * @param category 分类
     * @return 档案模板列表
     */
    List<ArchiveTemplate> findByCategory(String category);
    
    /**
     * 根据名称模糊查询档案模板
     * @param name 名称
     * @param pageable 分页参数
     * @return 档案模板分页结果
     */
    Page<ArchiveTemplate> findByTemplateNameContaining(String name, Pageable pageable);
    
    /**
     * 根据ID删除档案模板
     * @param id 档案模板ID
     */
    void deleteById(Long id);
    
    /**
     * 分页查询档案模板列表
     * @param page 页码
     * @param pageSize 每页条数
     * @param templateName 模板名称
     * @param category 分类
     * @return 分页结果
     */
    PageResult<ArchiveTemplate> getTemplates(int page, int pageSize, String templateName, String category);
    
    /**
     * 判断模板名称是否存在
     * @param templateName 模板名称
     * @return 是否存在
     */
    boolean existsByTemplateName(String templateName);
    
    /**
     * 获取模板总数
     * @return 模板总数
     */
    long count();

    /**
     * 创建档案模板
     * @param templateDTO 模板数据
     * @param file 模板文件
     * @return 创建的模板DTO
     */
    ArchiveTemplateDTO createTemplate(ArchiveTemplateDTO templateDTO, MultipartFile file);

    /**
     * 更新档案模板
     * @param id 模板ID
     * @param templateDTO 模板数据
     * @param file 模板文件
     * @return 更新后的模板DTO
     */
    ArchiveTemplateDTO updateTemplate(Long id, ArchiveTemplateDTO templateDTO, MultipartFile file);

    /**
     * 根据ID获取模板
     * @param id 模板ID
     * @return 模板DTO
     */
    ArchiveTemplateDTO getTemplateById(Long id);

    /**
     * 分页获取模板列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param name 模板名称
     * @param type 模板类型
     * @param category 适用档案类别
     * @param isActive 是否启用
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页结果
     */
    PageResult<ArchiveTemplateDTO> getTemplates(int page, int pageSize, String name,
                                                String type, String category, Boolean isActive,
                                                String sortBy, String sortDirection);

    /**
     * 删除模板
     * @param id 模板ID
     * @return 是否删除成功
     */
    boolean deleteTemplate(Long id);

    /**
     * 激活/禁用模板
     * @param id 模板ID
     * @param isActive 是否激活
     * @return 更新后的模板DTO
     */
    ArchiveTemplateDTO toggleTemplateStatus(Long id, boolean isActive);

    /**
     * 获取模板预览
     * @param id 模板ID
     * @return 预览资源
     */
    Resource previewTemplate(Long id);

    /**
     * 下载模板
     * @param id 模板ID
     * @return 模板资源
     */
    Resource downloadTemplate(Long id);

    /**
     * 通过模板生成档案
     * @param templateId 模板ID
     * @param data 档案数据
     * @return 生成的文件资源
     */
    Resource generateArchiveFile(Long templateId, Map<String, Object> data);

    /**
     * 获取指定类型的所有可用模板
     * @param type 模板类型
     * @return 模板列表
     */
    List<ArchiveTemplateDTO> getActiveTemplatesByType(String type);

    /**
     * 获取指定档案类别的所有可用模板
     * @param category 档案类别
     * @return 模板列表
     */
    List<ArchiveTemplateDTO> getActiveTemplatesByCategory(String category);

    /**
     * 获取最新版本的模板
     * @param name 模板名称
     * @return 模板DTO
     */
    ArchiveTemplateDTO getLatestVersionByName(String name);

    /**
     * 获取特定模板的所有版本
     * @param name 模板名称
     * @return 模板列表
     */
    List<ArchiveTemplateDTO> getAllVersionsByName(String name);

    /**
     * 创建新版本的模板
     * @param id 当前模板ID
     * @param templateDTO 新版本模板数据
     * @param file 新版本模板文件
     * @return 创建的新版本模板DTO
     */
    ArchiveTemplateDTO createNewVersion(Long id, ArchiveTemplateDTO templateDTO, MultipartFile file);
} 