package com.ray.archive.controller;

import com.ray.archive.common.ApiResponse;
import com.ray.archive.dto.ArchiveTemplateDTO;
import com.ray.archive.dto.PageResult;
import com.ray.archive.service.TemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 档案模板管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/templates")
@Api(tags = "档案模板管理", description = "提供档案模板的CRUD操作和预览生成功能")
public class TemplateController {

    private final TemplateService templateService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("创建档案模板")
    public ApiResponse<ArchiveTemplateDTO> createTemplate(
            @Valid @RequestPart("template") ArchiveTemplateDTO templateDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.success(templateService.createTemplate(templateDTO, file));
    }
    
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("更新档案模板")
    public ApiResponse<ArchiveTemplateDTO> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestPart("template") ArchiveTemplateDTO templateDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.success(templateService.updateTemplate(id, templateDTO, file));
    }
    
    @GetMapping("/{id}")
    @ApiOperation("获取档案模板详情")
    public ApiResponse<ArchiveTemplateDTO> getTemplate(@PathVariable Long id) {
        return ApiResponse.success(templateService.getTemplateById(id));
    }
    
    @GetMapping
    @ApiOperation("分页获取档案模板列表")
    public ApiResponse<PageResult<ArchiveTemplateDTO>> getTemplates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.success(templateService.getTemplates(
                page, pageSize, name, type, category, isActive, sortBy, sortDirection));
    }
    
    @DeleteMapping("/{id}")
    @ApiOperation("删除档案模板")
    public ApiResponse<Boolean> deleteTemplate(@PathVariable Long id) {
        return ApiResponse.success(templateService.deleteTemplate(id));
    }
    
    @PostMapping("/{id}/toggle-status")
    @ApiOperation("激活/禁用档案模板")
    public ApiResponse<ArchiveTemplateDTO> toggleTemplateStatus(
            @PathVariable Long id,
            @RequestParam boolean isActive) {
        return ApiResponse.success(templateService.toggleTemplateStatus(id, isActive));
    }
    
    @GetMapping("/{id}/preview")
    @ApiOperation("获取档案模板预览")
    public ResponseEntity<Resource> previewTemplate(@PathVariable Long id) {
        Resource resource = templateService.previewTemplate(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    @GetMapping("/{id}/download")
    @ApiOperation("下载档案模板")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable Long id) {
        Resource resource = templateService.downloadTemplate(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    @PostMapping("/{templateId}/generate")
    @ApiOperation("通过模板生成档案文件")
    public ResponseEntity<Resource> generateArchiveFile(
            @PathVariable Long templateId,
            @RequestBody Map<String, Object> data) {
        Resource resource = templateService.generateArchiveFile(templateId, data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"generated-archive.docx\"")
                .body(resource);
    }
    
    @GetMapping("/active/type/{type}")
    @ApiOperation("获取指定类型的所有可用模板")
    public ApiResponse<List<ArchiveTemplateDTO>> getActiveTemplatesByType(@PathVariable String type) {
        return ApiResponse.success(templateService.getActiveTemplatesByType(type));
    }
    
    @GetMapping("/active/category/{category}")
    @ApiOperation("获取指定档案类别的所有可用模板")
    public ApiResponse<List<ArchiveTemplateDTO>> getActiveTemplatesByCategory(@PathVariable String category) {
        return ApiResponse.success(templateService.getActiveTemplatesByCategory(category));
    }
    
    @GetMapping("/latest/{name}")
    @ApiOperation("获取最新版本的模板")
    public ApiResponse<ArchiveTemplateDTO> getLatestVersionByName(@PathVariable String name) {
        return ApiResponse.success(templateService.getLatestVersionByName(name));
    }
    
    @GetMapping("/versions/{name}")
    @ApiOperation("获取特定模板的所有版本")
    public ApiResponse<List<ArchiveTemplateDTO>> getAllVersionsByName(@PathVariable String name) {
        return ApiResponse.success(templateService.getAllVersionsByName(name));
    }
    
    @PostMapping("/{id}/new-version")
    @ApiOperation("创建新版本的模板")
    public ApiResponse<ArchiveTemplateDTO> createNewVersion(
            @PathVariable Long id,
            @Valid @RequestPart("template") ArchiveTemplateDTO templateDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.success(templateService.createNewVersion(id, templateDTO, file));
    }
} 