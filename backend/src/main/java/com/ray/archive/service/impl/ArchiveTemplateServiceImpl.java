package com.ray.archive.service.impl;

import com.ray.archive.dto.ArchiveTemplateDTO;
import com.ray.archive.dto.PageResult;
import com.ray.archive.entity.ArchiveTemplate;
import com.ray.archive.repository.ArchiveTemplateRepository;
import com.ray.archive.service.ArchiveTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 档案模板服务实现类
 */
@Service
@Transactional
public class ArchiveTemplateServiceImpl implements ArchiveTemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArchiveTemplateServiceImpl.class);
    
    private final ArchiveTemplateRepository archiveTemplateRepository;
    
    @Value("${file.upload-dir:./uploads/templates}")
    private String templatesDir;
    
    @Autowired
    public ArchiveTemplateServiceImpl(ArchiveTemplateRepository archiveTemplateRepository) {
        this.archiveTemplateRepository = archiveTemplateRepository;
    }
    
    @Override
    public ArchiveTemplate save(ArchiveTemplate template) {
        Assert.notNull(template, "Template must not be null");
        Assert.hasText(template.getName(), "Template name must not be empty");
        Assert.hasText(template.getContent(), "Template content must not be empty");
        
        if (template.getId() == null) {
            template.setCreateTime(LocalDateTime.now());
            template.setIsActive(true);
        }
        template.setUpdateTime(LocalDateTime.now());
        
        logger.debug("Saving archive template: {}", template.getName());
        return archiveTemplateRepository.save(template);
    }
    
    @Override
    public Optional<ArchiveTemplate> findById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Finding archive template by id: {}", id);
        return archiveTemplateRepository.findById(id);
    }
    
    @Override
    public Optional<ArchiveTemplate> findByTemplateName(String templateName) {
        Assert.hasText(templateName, "Template name must not be empty");
        logger.debug("Finding archive template by template name: {}", templateName);
        return archiveTemplateRepository.findByTemplateName(templateName);
    }
    
    @Override
    public Page<ArchiveTemplate> findAll(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");
        logger.debug("Finding all archive templates with pageable: {}", pageable);
        return archiveTemplateRepository.findAll(pageable);
    }
    
    @Override
    public List<ArchiveTemplate> findByCategory(String category) {
        Assert.hasText(category, "Category must not be empty");
        logger.debug("Finding archive templates by category: {}", category);
        return archiveTemplateRepository.findByCategory(category);
    }
    
    @Override
    public Page<ArchiveTemplate> findByTemplateNameContaining(String name, Pageable pageable) {
        Assert.hasText(name, "Name must not be empty");
        Assert.notNull(pageable, "Pageable must not be null");
        logger.debug("Finding archive templates containing name: {} with pageable: {}", name, pageable);
        return archiveTemplateRepository.findByTemplateNameContaining(name, pageable);
    }
    
    @Override
    public void deleteById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Deleting archive template with id: {}", id);
        archiveTemplateRepository.deleteById(id);
    }
    

    @Transactional
    public void updateStatus(Long id, boolean isActive) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Updating archive template status: id={}, isActive={}", id, isActive);
        
        archiveTemplateRepository.findById(id).ifPresent(template -> {
            template.setIsActive(isActive);
            template.setUpdateTime(LocalDateTime.now());
            archiveTemplateRepository.save(template);
        });
    }
    

    public Optional<ArchiveTemplate> findByName(String name) {
        Assert.hasText(name, "Name must not be empty");
        logger.debug("Finding archive template by name: {}", name);
        return archiveTemplateRepository.findByName(name);
    }
    

    public List<ArchiveTemplate> findAllActive() {
        logger.debug("Finding all active archive templates");
        return archiveTemplateRepository.findByIsActiveTrue();
    }
    

    public String getTemplateContent(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Getting template content for id: {}", id);
        
        return findById(id)
            .map(ArchiveTemplate::getContent)
            .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
    }
    

    public boolean existsByName(String name) {
        Assert.hasText(name, "Name must not be empty");
        logger.debug("Checking if template exists with name: {}", name);
        return archiveTemplateRepository.existsByName(name);
    }

    @Override
    public PageResult<ArchiveTemplate> getTemplates(int page, int pageSize, String templateName, String category) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<ArchiveTemplate> templatesPage;
        if (templateName != null && !templateName.isEmpty()) {
            templatesPage = archiveTemplateRepository.findByTemplateNameContaining(templateName, pageable);
        } else if (category != null && !category.isEmpty()) {
            // 假设有一个根据分类查询的方法
            templatesPage = archiveTemplateRepository.findAll(pageable);
        } else {
            templatesPage = archiveTemplateRepository.findAll(pageable);
        }
        
        return new PageResult<>(
        );
    }
    
    @Override
    public boolean existsByTemplateName(String templateName) {
        Assert.hasText(templateName, "Template name must not be empty");
        logger.debug("Checking if template exists with template name: {}", templateName);
        return archiveTemplateRepository.existsByTemplateName(templateName);
    }
    
    @Override
    public long count() {
        logger.debug("Counting all archive templates");
        return archiveTemplateRepository.count();
    }

    @Override
    public ArchiveTemplateDTO createTemplate(ArchiveTemplateDTO templateDTO, MultipartFile file) {
        Assert.notNull(templateDTO, "Template DTO must not be null");
        logger.debug("Creating archive template: {}", templateDTO.getName());
        
        // 检查名称是否已存在
        if (existsByName(templateDTO.getName())) {
            throw new IllegalArgumentException("Template with name '" + templateDTO.getName() + "' already exists");
        }
        
        ArchiveTemplate template = convertToEntity(templateDTO);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        template.setIsActive(true);
        template.setIsLatestVersion(true);
        template.setVersion(1);
        
        // 处理模板文件
        if (file != null && !file.isEmpty()) {
            String fileName = saveTemplateFile(file);
            template.setFilePath(fileName);
        }
        
        ArchiveTemplate savedTemplate = archiveTemplateRepository.save(template);
        return convertToDTO(savedTemplate);
    }

    @Override
    public ArchiveTemplateDTO updateTemplate(Long id, ArchiveTemplateDTO templateDTO, MultipartFile file) {
        Assert.notNull(id, "ID must not be null");
        Assert.notNull(templateDTO, "Template DTO must not be null");
        logger.debug("Updating archive template with id: {}", id);
        
        ArchiveTemplate existingTemplate = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
        
        // 检查名称是否已被其他模板使用
        if (!existingTemplate.getName().equals(templateDTO.getName()) && existsByName(templateDTO.getName())) {
            throw new IllegalArgumentException("Template with name '" + templateDTO.getName() + "' already exists");
        }
        
        // 更新模板属性
        existingTemplate.setName(templateDTO.getName());
        existingTemplate.setType(templateDTO.getType());
        existingTemplate.setCategory(templateDTO.getCategory());
        existingTemplate.setDescription(templateDTO.getDescription());
        existingTemplate.setContent(templateDTO.getContent());
        existingTemplate.setUpdateTime(LocalDateTime.now());
        
        // 处理模板文件
        if (file != null && !file.isEmpty()) {
            // 删除旧文件
            if (existingTemplate.getFilePath() != null) {
                deleteTemplateFile(existingTemplate.getFilePath());
            }
            
            String fileName = saveTemplateFile(file);
            existingTemplate.setFilePath(fileName);
        }
        
        ArchiveTemplate updatedTemplate = archiveTemplateRepository.save(existingTemplate);
        return convertToDTO(updatedTemplate);
    }

    @Override
    public ArchiveTemplateDTO getTemplateById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Getting template DTO by id: {}", id);
        
        ArchiveTemplate template = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
        
        return convertToDTO(template);
    }

    @Override
    public PageResult<ArchiveTemplateDTO> getTemplates(int page, int pageSize, String name, String type, String category, 
                                                     Boolean isActive, String sortBy, String sortDirection) {
        // 构建排序
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        
        String sortField = "createTime";
        if (sortBy != null && !sortBy.isEmpty()) {
            sortField = sortBy;
        }
        
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(direction, sortField));
        
        // 构建查询条件
        Page<ArchiveTemplate> templatesPage = archiveTemplateRepository.findByFilters(name, type, category, isActive, pageable);
        
        // 转换为DTO
        List<ArchiveTemplateDTO> templateDTOs = templatesPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageResult<>(
        );
    }

    @Override
    public boolean deleteTemplate(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Deleting template with id: {}", id);
        
        try {
            ArchiveTemplate template = findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
            
            // 删除模板文件
            if (template.getFilePath() != null) {
                deleteTemplateFile(template.getFilePath());
            }
            
            archiveTemplateRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting template with id: {}", id, e);
            return false;
        }
    }

    @Override
    public ArchiveTemplateDTO toggleTemplateStatus(Long id, boolean isActive) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Toggling template status: id={}, isActive={}", id, isActive);
        
        ArchiveTemplate template = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
        
        template.setIsActive(isActive);
        template.setUpdateTime(LocalDateTime.now());
        
        ArchiveTemplate updatedTemplate = archiveTemplateRepository.save(template);
        return convertToDTO(updatedTemplate);
    }

    @Override
    public Resource previewTemplate(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Previewing template with id: {}", id);
        
        ArchiveTemplate template = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
        
        if (template.getFilePath() == null) {
            throw new IllegalStateException("Template has no file to preview");
        }
        
        try {
            Path filePath = Paths.get(templatesDir).resolve(template.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new IllegalStateException("Template file not found: " + template.getFilePath());
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Error previewing template file: " + template.getFilePath(), e);
        }
    }

    @Override
    public Resource downloadTemplate(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Downloading template with id: {}", id);
        
        ArchiveTemplate template = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
        
        if (template.getFilePath() == null) {
            throw new IllegalStateException("Template has no file to download");
        }
        
        try {
            Path filePath = Paths.get(templatesDir).resolve(template.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new IllegalStateException("Template file not found: " + template.getFilePath());
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Error downloading template file: " + template.getFilePath(), e);
        }
    }

    @Override
    public Resource generateArchiveFile(Long templateId, Map<String, Object> data) {
        Assert.notNull(templateId, "Template ID must not be null");
        Assert.notNull(data, "Data must not be null");
        logger.debug("Generating archive file from template id: {}", templateId);
        
        ArchiveTemplate template = findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + templateId));
        
        if (template.getFilePath() == null) {
            throw new IllegalStateException("Template has no file to generate archive from");
        }
        
        try {
            // 这里是模板渲染的逻辑，根据具体的模板引擎实现
            // 示例实现，实际应用中需要使用真正的模板引擎
            String outputFileName = "generated_" + System.currentTimeMillis() + "_" + 
                    template.getFilePath().substring(template.getFilePath().lastIndexOf("/") + 1);
            Path outputPath = Paths.get(templatesDir).resolve("generated").resolve(outputFileName);
            
            // 确保目录存在
            Files.createDirectories(outputPath.getParent());
            
            // 复制模板文件并替换变量（简化处理，实际应用中需要使用模板引擎）
            Path templatePath = Paths.get(templatesDir).resolve(template.getFilePath());
            Files.copy(templatePath, outputPath, StandardCopyOption.REPLACE_EXISTING);
            
            return new FileSystemResource(outputPath.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Error generating archive file from template: " + template.getFilePath(), e);
        }
    }

    @Override
    public List<ArchiveTemplateDTO> getActiveTemplatesByType(String type) {
        Assert.hasText(type, "Type must not be empty");
        logger.debug("Getting active templates by type: {}", type);
        
        List<ArchiveTemplate> templates = archiveTemplateRepository.findByTypeAndIsActiveTrue(type);
        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArchiveTemplateDTO> getActiveTemplatesByCategory(String category) {
        Assert.hasText(category, "Category must not be empty");
        logger.debug("Getting active templates by category: {}", category);
        
        List<ArchiveTemplate> templates = archiveTemplateRepository.findByCategoryAndIsActiveTrue(category);
        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ArchiveTemplateDTO getLatestVersionByName(String name) {
        Assert.hasText(name, "Name must not be empty");
        logger.debug("Getting latest version of template by name: {}", name);
        
        ArchiveTemplate template = archiveTemplateRepository.findLatestVersionByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with name: " + name));
        
        return convertToDTO(template);
    }

    @Override
    public List<ArchiveTemplateDTO> getAllVersionsByName(String name) {
        Assert.hasText(name, "Name must not be empty");
        logger.debug("Getting all versions of template by name: {}", name);
        
        List<ArchiveTemplate> templates = archiveTemplateRepository.findAllVersionsByName(name);
        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ArchiveTemplateDTO createNewVersion(Long id, ArchiveTemplateDTO templateDTO, MultipartFile file) {
        Assert.notNull(id, "ID must not be null");
        Assert.notNull(templateDTO, "Template DTO must not be null");
        logger.debug("Creating new version of template with id: {}", id);
        
        ArchiveTemplate oldTemplate = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
        
        // 将旧版本标记为非最新版本
        oldTemplate.setIsLatestVersion(false);
        archiveTemplateRepository.save(oldTemplate);
        
        // 创建新版本
        ArchiveTemplate newTemplate = convertToEntity(templateDTO);
        newTemplate.setId(null); // 确保创建新记录
        newTemplate.setName(oldTemplate.getName()); // 保持名称一致
        newTemplate.setCreateTime(LocalDateTime.now());
        newTemplate.setUpdateTime(LocalDateTime.now());
        newTemplate.setIsActive(true);
        newTemplate.setIsLatestVersion(true);
        newTemplate.setVersion(oldTemplate.getVersion() + 1);
        
        // 处理模板文件
        if (file != null && !file.isEmpty()) {
            String fileName = saveTemplateFile(file);
            newTemplate.setFilePath(fileName);
        } else if (oldTemplate.getFilePath() != null) {
            // 复制旧文件
            try {
                String newFileName = "v" + newTemplate.getVersion() + "_" + 
                        oldTemplate.getFilePath().substring(oldTemplate.getFilePath().lastIndexOf("/") + 1);
                Path oldPath = Paths.get(templatesDir).resolve(oldTemplate.getFilePath());
                Path newPath = Paths.get(templatesDir).resolve(newFileName);
                Files.copy(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                newTemplate.setFilePath(newFileName);
            } catch (IOException e) {
                logger.error("Error copying template file for new version", e);
            }
        }
        
        ArchiveTemplate savedTemplate = archiveTemplateRepository.save(newTemplate);
        return convertToDTO(savedTemplate);
    }
    
    // 辅助方法
    
    /**
     * 保存模板文件
     * @param file 模板文件
     * @return 文件路径
     */
    private String saveTemplateFile(MultipartFile file) {
        try {
            // 确保目录存在
            Files.createDirectories(Paths.get(templatesDir));
            
            // 生成唯一文件名
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String fileName = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path targetPath = Paths.get(templatesDir).resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return fileName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save template file", e);
        }
    }
    
    /**
     * 删除模板文件
     * @param filePath 文件路径
     */
    private void deleteTemplateFile(String filePath) {
        try {
            Path path = Paths.get(templatesDir).resolve(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.error("Error deleting template file: {}", filePath, e);
        }
    }
    
    /**
     * 将DTO转换为实体
     * @param dto DTO
     * @return 实体
     */
    private ArchiveTemplate convertToEntity(ArchiveTemplateDTO dto) {
        ArchiveTemplate entity = new ArchiveTemplate();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setCategory(dto.getCategory());
        entity.setDescription(dto.getDescription());
        entity.setContent(dto.getContent());
        entity.setFilePath(dto.getFilePath());
        entity.setIsActive(dto.getIsActive());
        entity.setIsLatestVersion(dto.getIsLatestVersion());
        entity.setVersion(dto.getVersion());
        entity.setCreator(dto.getCreator());
        
        return entity;
    }
    
    /**
     * 将实体转换为DTO
     * @param entity 实体
     * @return DTO
     */
    private ArchiveTemplateDTO convertToDTO(ArchiveTemplate entity) {
        ArchiveTemplateDTO dto = new ArchiveTemplateDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setCategory(entity.getCategory());
        dto.setDescription(entity.getDescription());
        dto.setContent(entity.getContent());
        dto.setFilePath(entity.getFilePath());
        dto.setIsActive(entity.getIsActive());
        dto.setIsLatestVersion(entity.getIsLatestVersion());
        dto.setVersion(entity.getVersion());
        dto.setCreator(entity.getCreator());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        
        return dto;
    }
} 