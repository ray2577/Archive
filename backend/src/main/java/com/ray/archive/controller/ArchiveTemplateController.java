package com.ray.archive.controller;

import com.ray.archive.entity.ArchiveTemplate;
import com.ray.archive.service.ArchiveTemplateService;
import com.ray.archive.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/archive-templates")
public class ArchiveTemplateController {

    @Autowired
    private ArchiveTemplateService archiveTemplateService;

    @Autowired
    private PdfService pdfService;

    // ... 其他现有的端点 ...

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable Long id,
            @RequestParam(required = false) String watermark,
            @RequestParam(required = false) String watermarkColor,
            @RequestParam(required = false) Float watermarkOpacity,
            @RequestParam(required = false) Double watermarkRotation,
            @RequestParam(required = false) String userPassword,
            @RequestParam(required = false) String ownerPassword) {
        
        ArchiveTemplate template = archiveTemplateService.getTemplateById(id);
        byte[] pdfContent = pdfService.generateTemplatePdf(
            template, 
            watermark,
            watermarkColor,
            watermarkOpacity,
            watermarkRotation,
            userPassword, 
            ownerPassword
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", template.getName() + "-模板.pdf");

        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfContent);
    }
} 