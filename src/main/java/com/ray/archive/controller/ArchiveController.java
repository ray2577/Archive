package com.ray.archive.controller;

import com.ray.archive.entity.Archive;
import com.ray.archive.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/archives")
public class ArchiveController {

    private final ArchiveService archiveService;

    @Autowired
    public ArchiveController(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @PostMapping
    public ResponseEntity<Archive> createArchive(@RequestBody Archive archive) {
        return ResponseEntity.ok(archiveService.save(archive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Archive> getArchive(@PathVariable Long id) {
        return archiveService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<Archive>> getAllArchives(Pageable pageable) {
        return ResponseEntity.ok(archiveService.findAll(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Archive>> searchArchives(@RequestParam String keyword) {
        return ResponseEntity.ok(archiveService.search(keyword));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Archive>> getArchivesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(archiveService.findByCategory(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Archive> updateArchive(@PathVariable Long id, @RequestBody Archive archive) {
        return archiveService.findById(id)
                .map(existingArchive -> {
                    archive.setId(id);
                    return ResponseEntity.ok(archiveService.save(archive));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArchive(@PathVariable Long id) {
        if (!archiveService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        archiveService.deleteById(id);
        return ResponseEntity.ok().build();
    }
} 