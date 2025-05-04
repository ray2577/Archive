package com.ray.archive.controller;

import com.itextpdf.kernel.geom.Rectangle;
import com.ray.archive.service.PdfMergeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pdf-merge")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class PdfMergeController {

    @Autowired
    private PdfMergeService pdfMergeService;

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfs(
            @RequestParam("files") List<MultipartFile> pdfFiles,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "imageX", required = false) List<Float> imageXPositions,
            @RequestParam(value = "imageY", required = false) List<Float> imageYPositions,
            @RequestParam(value = "imagePages", required = false) List<Integer> imagePages) {
        try {
            byte[] mergedPdf = pdfMergeService.mergePdfs(pdfFiles, images, imageXPositions, imageYPositions, imagePages);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "merged.pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(mergedPdf);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{pageNumber}/add-image")
    public ResponseEntity<byte[]> addImageToPdf(
            @RequestParam("pdf") MultipartFile pdfFile,
            @RequestParam("image") MultipartFile image,
            @RequestParam float x,
            @RequestParam float y,
            @PathVariable int pageNumber) {
        try {
            byte[] resultPdf = pdfMergeService.addImageToPdf(
                pdfFile.getBytes(), image, x, y, pageNumber);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "with-image.pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resultPdf);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/page-count")
    public ResponseEntity<Integer> getPdfPageCount(@RequestParam("pdf") MultipartFile pdfFile) {
        try {
            int pageCount = pdfMergeService.getPdfPageCount(pdfFile.getBytes());
            return ResponseEntity.ok(pageCount);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{pageNumber}/size")
    public ResponseEntity<Rectangle> getPageSize(
            @RequestParam("pdf") MultipartFile pdfFile,
            @PathVariable int pageNumber) {
        try {
            Rectangle pageSize = pdfMergeService.getPageSize(pdfFile.getBytes(), pageNumber);
            return ResponseEntity.ok(pageSize);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 