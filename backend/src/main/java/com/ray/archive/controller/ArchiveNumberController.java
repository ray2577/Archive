package com.ray.archive.controller;

import com.ray.archive.entity.ArchiveNumberRule;
import com.ray.archive.service.ArchiveNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/archive-numbers")
@PreAuthorize("hasRole('ADMIN')")
public class ArchiveNumberController {

    @Autowired
    private ArchiveNumberService archiveNumberService;

    @PostMapping("/rules")
    public ResponseEntity<ArchiveNumberRule> createRule(@RequestBody ArchiveNumberRule rule) {
        if (archiveNumberService.existsByCategory(rule.getCategory())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(archiveNumberService.saveRule(rule));
    }

    @GetMapping("/rules/{id}")
    public ResponseEntity<ArchiveNumberRule> getRule(@PathVariable Long id) {
        return archiveNumberService.findRuleById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rules")
    public ResponseEntity<Page<ArchiveNumberRule>> getAllRules(Pageable pageable) {
        return ResponseEntity.ok(archiveNumberService.findAllRules(pageable));
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<ArchiveNumberRule> updateRule(
            @PathVariable Long id,
            @RequestBody ArchiveNumberRule rule) {
        if (!archiveNumberService.findRuleById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        rule.setId(id);
        return ResponseEntity.ok(archiveNumberService.saveRule(rule));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        if (!archiveNumberService.findRuleById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        archiveNumberService.deleteRule(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<String> generateNumber(@RequestParam String category) {
        try {
            String number = archiveNumberService.generateArchiveNumber(category);
            return ResponseEntity.ok(number);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 