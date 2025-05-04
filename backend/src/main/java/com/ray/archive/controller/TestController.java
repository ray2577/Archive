package com.ray.archive.controller;

import com.ray.archive.entity.Archive;
import com.ray.archive.repository.ArchiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试路径映射的控制器
 */
@RestController
@RequestMapping("/debug")
public class TestController {

    private final ArchiveRepository archiveRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TestController(ArchiveRepository archiveRepository, JdbcTemplate jdbcTemplate) {
        this.archiveRepository = archiveRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db-status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> result = new HashMap<>();
        
        // 检查archives表是否存在
        List<String> tables = jdbcTemplate.queryForList("SHOW TABLES", String.class);
        result.put("tables", tables);
        
        // 检查archives表中的记录数
        if (tables.contains("archives")) {
            Long archiveCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM archives", Long.class);
            result.put("archiveCount", archiveCount);
            
            // 获取一些示例记录
            if (archiveCount > 0) {
                List<Map<String, Object>> sampleRecords = jdbcTemplate.queryForList("SELECT * FROM archives LIMIT 3");
                result.put("sampleRecords", sampleRecords);
            }
        }
        
        // 使用JPA仓库查询
        try {
            long jpaCount = archiveRepository.count();
            result.put("jpaArchiveCount", jpaCount);
            
            Page<Archive> firstPage = archiveRepository.findAll(PageRequest.of(0, 5));
            List<Object> simplifiedArchives = new ArrayList<>();
            
            for (Archive archive : firstPage.getContent()) {
                Map<String, Object> simpleArchive = new HashMap<>();
                simpleArchive.put("id", archive.getId());
                simpleArchive.put("fileNumber", archive.getFileNumber());
                simpleArchive.put("title", archive.getTitle());
                simpleArchive.put("status", archive.getStatus());
                simplifiedArchives.add(simpleArchive);
            }
            
            result.put("jpaArchives", simplifiedArchives);
            result.put("totalPages", firstPage.getTotalPages());
            result.put("totalElements", firstPage.getTotalElements());
        } catch (Exception e) {
            result.put("jpaError", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
} 