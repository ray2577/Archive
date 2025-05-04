package com.ray.archive.service.impl;

import com.ray.archive.entity.ArchiveTemplate;
import com.ray.archive.repository.ArchiveTemplateRepository;
import com.ray.archive.service.ArchiveTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArchiveTemplateServiceImpl implements ArchiveTemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArchiveTemplateServiceImpl.class);
    
    private final ArchiveTemplateRepository archiveTemplateRepository;
    
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
    public Page<ArchiveTemplate> findAll(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");
        logger.debug("Finding all archive templates with pageable: {}", pageable);
        return archiveTemplateRepository.findAll(pageable);
    }
    
    @Override
    public void deleteById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Deleting archive template with id: {}", id);
        archiveTemplateRepository.deleteById(id);
    }
    
    @Override
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
    
    @Override
    public Optional<ArchiveTemplate> findByName(String name) {
        Assert.hasText(name, "Name must not be empty");
        logger.debug("Finding archive template by name: {}", name);
        return archiveTemplateRepository.findByName(name);
    }
    
    @Override
    public List<ArchiveTemplate> findAllActive() {
        logger.debug("Finding all active archive templates");
        return archiveTemplateRepository.findByIsActiveTrue();
    }
    
    @Override
    public String getTemplateContent(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Getting template content for id: {}", id);
        
        return findById(id)
            .map(ArchiveTemplate::getContent)
            .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
    }
    
    @Override
    public boolean existsByName(String name) {
        Assert.hasText(name, "Name must not be empty");
        logger.debug("Checking if template exists with name: {}", name);
        return archiveTemplateRepository.existsByName(name);
    }
    
    @Override
    public ArchiveTemplate getTemplateById(Long id) {
        Assert.notNull(id, "ID must not be null");
        logger.debug("Getting template by id: {}", id);
        
        return findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
    }
} 