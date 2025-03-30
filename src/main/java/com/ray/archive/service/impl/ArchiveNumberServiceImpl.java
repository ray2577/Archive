package com.ray.archive.service.impl;

import com.ray.archive.entity.ArchiveNumberRule;
import com.ray.archive.repository.ArchiveNumberRuleRepository;
import com.ray.archive.service.ArchiveNumberService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ArchiveNumberServiceImpl implements ArchiveNumberService {

    @Autowired
    private ArchiveNumberRuleRepository archiveNumberRuleRepository;

    @Override
    @Transactional
    public String generateArchiveNumber(String category) {
        ArchiveNumberRule rule = getActiveRuleForCategory(category);
        if (rule == null) {
            throw new RuntimeException("No active rule found for category: " + category);
        }

        // 生成日期部分
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern(rule.getDateFormat()));

        // 生成序号部分
        Long serial = rule.getCurrentSerial() + 1;
        rule.setCurrentSerial(serial);
        archiveNumberRuleRepository.save(rule);

        // 格式化序号
        String serialPart = String.format("%0" + rule.getSerialLength() + "d", serial);

        // 组合档案编号
        String separator = rule.getSeparator() != null ? rule.getSeparator() : "-";
        return rule.getPrefix() + separator + datePart + separator + serialPart;
    }

    @Override
    public ArchiveNumberRule saveRule(ArchiveNumberRule rule) {
        return archiveNumberRuleRepository.save(rule);
    }

    @Override
    public Optional<ArchiveNumberRule> findRuleById(Long id) {
        return archiveNumberRuleRepository.findById(id);
    }

    @Override
    public Page<ArchiveNumberRule> findAllRules(Pageable pageable) {
        return archiveNumberRuleRepository.findAll(pageable);
    }

    @Override
    public void deleteRule(Long id) {
        archiveNumberRuleRepository.deleteById(id);
    }

    @Override
    public boolean existsByCategory(String category) {
        return archiveNumberRuleRepository.existsByCategory(category);
    }

    @Override
    public ArchiveNumberRule getActiveRuleForCategory(String category) {
        return (ArchiveNumberRule) archiveNumberRuleRepository.findByCategoryAndIsActiveTrue(category)
            .orElseThrow(() -> new RuntimeException("No active rule found for category: " + category));
    }
} 