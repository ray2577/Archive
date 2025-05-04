package com.ray.archive.controller;

import com.ray.archive.common.ApiResponse;
import com.ray.archive.dto.DocumentDTO;
import com.ray.archive.dto.DocumentStatisticsDTO;
import com.ray.archive.dto.PageResult;
import com.ray.archive.service.DocumentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
@Api(tags = "文档管理", description = "提供文档的CRUD操作和高级功能")
public class DocumentController {

    private final DocumentService documentService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("创建新文档")
    public ApiResponse<DocumentDTO> createDocument(
            @Valid @RequestPart("document") DocumentDTO documentDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.success(documentService.createDocument(documentDTO, file));
    }
    
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("更新文档")
    public ApiResponse<DocumentDTO> updateDocument(
            @PathVariable Long id,
            @Valid @RequestPart("document") DocumentDTO documentDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.success(documentService.updateDocument(id, documentDTO, file));
    }
    
    @GetMapping("/{id}")
    @ApiOperation("获取文档详情")
    public ApiResponse<DocumentDTO> getDocument(@PathVariable Long id) {
        return ApiResponse.success(documentService.getDocumentById(id));
    }
    
    @GetMapping("/code/{code}")
    @ApiOperation("通过编码获取文档")
    public ApiResponse<DocumentDTO> getDocumentByCode(@PathVariable String code) {
        return ApiResponse.success(documentService.getDocumentByCode(code));
    }
    
    @GetMapping
    @ApiOperation("分页获取文档列表")
    public ApiResponse<PageResult<DocumentDTO>> getDocuments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.success(documentService.getDocuments(
                page, pageSize, code, name, type, category, status, sortBy, sortDirection));
    }
    
    @DeleteMapping("/{id}")
    @ApiOperation("删除文档")
    public ApiResponse<Boolean> deleteDocument(@PathVariable Long id) {
        return ApiResponse.success(documentService.deleteDocument(id));
    }
    
    @DeleteMapping("/batch")
    @ApiOperation("批量删除文档")
    public ApiResponse<Integer> batchDeleteDocuments(@RequestBody List<Long> ids) {
        return ApiResponse.success(documentService.batchDeleteDocuments(ids));
    }
    
    @GetMapping("/statistics")
    @ApiOperation("获取文档统计数据")
    public ApiResponse<DocumentStatisticsDTO> getDocumentStatistics() {
        return ApiResponse.success(documentService.getDocumentStatistics());
    }
    
    @PostMapping("/{id}/share")
    @ApiOperation("分享文档")
    public ApiResponse<String> shareDocument(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") Integer expireDays) {
        return ApiResponse.success(documentService.shareDocument(id, expireDays));
    }
    
    @GetMapping("/{id}/download")
    @ApiOperation("下载文档")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Resource resource = documentService.downloadDocument(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    @PostMapping("/export")
    @ApiOperation("导出文档")
    public ResponseEntity<Resource> exportDocuments(@RequestBody(required = false) Map<String, Object> filters) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        Resource resource = documentService.exportDocuments(filters);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documents.xlsx\"")
                .body(resource);
    }
    
    @PostMapping("/batch-export")
    @ApiOperation("批量导出文档")
    public ResponseEntity<Resource> batchExportDocuments(@RequestBody List<Long> ids) {
        Resource resource = documentService.batchExportDocuments(ids);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documents.xlsx\"")
                .body(resource);
    }
    
    @PostMapping("/import")
    @ApiOperation("导入文档")
    public ApiResponse<Integer> importDocuments(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(documentService.importDocuments(file));
    }
} 