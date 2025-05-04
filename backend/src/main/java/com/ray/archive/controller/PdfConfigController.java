package com.ray.archive.controller;

import com.ray.archive.entity.PdfConfig;
import com.ray.archive.service.PdfConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf-configs")
@PreAuthorize("hasRole('ADMIN')")
public class PdfConfigController {

    @Autowired
    private PdfConfigService pdfConfigService;

    @PostMapping
    public ResponseEntity<PdfConfig> createConfig(@RequestBody PdfConfig config) {
        if (pdfConfigService.existsByName(config.getName())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(pdfConfigService.save(config));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PdfConfig> getConfig(@PathVariable Long id) {
        return pdfConfigService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<PdfConfig>> getAllConfigs(Pageable pageable) {
        return ResponseEntity.ok(pdfConfigService.findAll(pageable));
    }

    @GetMapping("/default")
    public ResponseEntity<PdfConfig> getDefaultConfig() {
        return ResponseEntity.ok(pdfConfigService.getDefaultConfig());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PdfConfig> updateConfig(@PathVariable Long id, @RequestBody PdfConfig config) {
        return pdfConfigService.findById(id)
            .map(existingConfig -> {
                config.setId(id);
                return ResponseEntity.ok(pdfConfigService.save(config));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        if (!pdfConfigService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        pdfConfigService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/set-default")
    public ResponseEntity<Void> setDefaultConfig(@PathVariable Long id) {
        if (!pdfConfigService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        pdfConfigService.setDefaultConfig(id);
        return ResponseEntity.ok().build();
    }
} 