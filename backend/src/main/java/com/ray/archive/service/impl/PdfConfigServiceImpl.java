package com.ray.archive.service.impl;

import com.ray.archive.entity.PdfConfig;
import com.ray.archive.repository.PdfConfigRepository;
import com.ray.archive.service.PdfConfigService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PdfConfigServiceImpl implements PdfConfigService {

    @Autowired
    private PdfConfigRepository pdfConfigRepository;

    @Override
    public PdfConfig save(PdfConfig config) {
        if (config.getIsDefault()) {
            // 如果当前配置设置为默认，则将其他默认配置取消
            pdfConfigRepository.findByIsDefaultTrue()
                .ifPresent(oldDefault -> {
                    oldDefault.setIsDefault(false);
                    pdfConfigRepository.save(oldDefault);
                });
        }
        return pdfConfigRepository.save(config);
    }

    @Override
    public Optional<PdfConfig> findById(Long id) {
        return pdfConfigRepository.findById(id);
    }

    @Override
    public Page<PdfConfig> findAll(Pageable pageable) {
        return pdfConfigRepository.findAll(pageable);
    }

    @Override
    public void deleteById(Long id) {
        pdfConfigRepository.deleteById(id);
    }

    @Override
    public PdfConfig getDefaultConfig() {
        return pdfConfigRepository.findByIsDefaultTrue()
            .orElseGet(() -> {
                // 如果没有默认配置，创建一个新的默认配置
                PdfConfig defaultConfig = new PdfConfig();
                defaultConfig.setName("默认配置");
                defaultConfig.setWatermarkText("档案管理系统");
                defaultConfig.setIsDefault(true);
                return pdfConfigRepository.save(defaultConfig);
            });
    }

    @Override
    @Transactional
    public void setDefaultConfig(Long id) {
        // 先取消当前默认配置
        pdfConfigRepository.findByIsDefaultTrue()
            .ifPresent(oldDefault -> {
                oldDefault.setIsDefault(false);
                pdfConfigRepository.save(oldDefault);
            });

        // 设置新的默认配置
        pdfConfigRepository.findById(id)
            .ifPresent(newDefault -> {
                newDefault.setIsDefault(true);
                pdfConfigRepository.save(newDefault);
            });
    }

    @Override
    public boolean existsByName(String name) {
        return pdfConfigRepository.existsByName(name);
    }
} 