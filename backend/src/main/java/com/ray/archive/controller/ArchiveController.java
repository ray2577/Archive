package com.ray.archive.controller;

import com.ray.archive.dto.ApiResponse;
import com.ray.archive.dto.ArchiveDTO;
import com.ray.archive.dto.BorrowRecordDTO;
import com.ray.archive.dto.PageResult;
import com.ray.archive.dto.StatisticsDTO;
import com.ray.archive.service.ArchiveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 档案管理控制器
 * 提供档案相关的REST API接口
 */
@RestController
@RequestMapping("/api/archives")
@Api(tags = "档案管理", description = "档案CRUD、导入导出、借阅管理、分享管理等功能")
public class ArchiveController {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveController.class);

    private final ArchiveService archiveService;

    @Autowired
    public ArchiveController(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    /**
     * 获取档案列表（带分页、排序和过滤）
     */
    @GetMapping
    @ApiOperation("获取档案列表")
    public ResponseEntity<ApiResponse<PageResult<ArchiveDTO>>> getArchiveList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @ApiParam("档案编号") @RequestParam(required = false) String fileNumber,
            @ApiParam("档案标题") @RequestParam(required = false) String title,
            @ApiParam("档案类型") @RequestParam(required = false) String type,
            @ApiParam("档案状态") @RequestParam(required = false) String status,
            @ApiParam("排序字段") @RequestParam(required = false) String sortBy,
            @ApiParam("排序方向") @RequestParam(defaultValue = "asc") String sortDirection) {

        logger.info("获取档案列表请求 - 页码: {}, 每页条数: {}, 过滤条件: {}", page, pageSize, 
                Map.of("fileNumber", fileNumber, "title", title, "type", type, "status", status));
        
        try {
            PageResult<ArchiveDTO> result = archiveService.getArchives(
                    page, pageSize, fileNumber, title, type, status, sortBy, sortDirection);
            
            logger.info("获取档案列表成功，返回{}条记录", result.getTotal());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            logger.error("获取档案列表失败", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取档案列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 通过编号获取档案
     */
    @GetMapping("/number/{fileNumber}")
    @ApiOperation("通过编号获取档案")
    public ResponseEntity<ApiResponse<ArchiveDTO>> getArchiveByNumber(
            @ApiParam("档案编号") @PathVariable String fileNumber) {
        
        logger.info("通过编号获取档案请求 - 编号: {}", fileNumber);
        
        try {
            ArchiveDTO archive = archiveService.getArchiveByFileNumber(fileNumber);
            
            if (archive == null) {
                logger.warn("未找到编号为{}的档案", fileNumber);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "档案不存在"));
            }
            
            logger.info("通过编号获取档案成功 - 编号: {}", fileNumber);
            return ResponseEntity.ok(ApiResponse.success(archive));
        } catch (Exception e) {
            logger.error("通过编号获取档案失败 - 编号: " + fileNumber, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取档案失败: " + e.getMessage()));
        }
    }

    /**
     * 获取档案详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取档案详情")
    public ResponseEntity<ApiResponse<ArchiveDTO>> getArchiveDetail(
            @ApiParam("档案ID") @PathVariable Long id) {
        
        logger.info("获取档案详情请求 - ID: {}", id);
        
        try {
            ArchiveDTO archive = archiveService.getArchiveById(id);
            
            if (archive == null) {
                logger.warn("未找到ID为{}的档案", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "档案不存在"));
            }
            
            logger.info("获取档案详情成功 - ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(archive));
        } catch (Exception e) {
            logger.error("获取档案详情失败 - ID: " + id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取档案详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建档案
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("创建新档案")
    public ResponseEntity<ApiResponse<ArchiveDTO>> createArchive(
            @ApiParam("档案数据") @Valid @RequestPart("archive") ArchiveDTO archiveDTO,
            @ApiParam("档案文件") @RequestPart(value = "file", required = false) MultipartFile file) {
        
        logger.info("创建档案请求 - 档案标题: {}", archiveDTO.getTitle());
        
        try {
            ArchiveDTO createdArchive = archiveService.createArchive(archiveDTO, file);
            
            logger.info("创建档案成功 - ID: {}", createdArchive.getId());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdArchive));
        } catch (Exception e) {
            logger.error("创建档案失败", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "创建档案失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新档案
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("更新档案")
    public ResponseEntity<ApiResponse<ArchiveDTO>> updateArchive(
            @ApiParam("档案ID") @PathVariable Long id,
            @ApiParam("档案数据") @Valid @RequestPart("archive") ArchiveDTO archiveDTO,
            @ApiParam("档案文件") @RequestPart(value = "file", required = false) MultipartFile file) {
        
        logger.info("更新档案请求 - ID: {}", id);
        
        try {
            ArchiveDTO updatedArchive = archiveService.updateArchive(id, archiveDTO, file);
            
            if (updatedArchive == null) {
                logger.warn("未找到ID为{}的档案", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "档案不存在"));
            }
            
            logger.info("更新档案成功 - ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(updatedArchive));
        } catch (Exception e) {
            logger.error("更新档案失败 - ID: " + id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新档案失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除档案
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除档案")
    public ResponseEntity<ApiResponse<Boolean>> deleteArchive(
            @ApiParam("档案ID") @PathVariable Long id) {
        
        logger.info("删除档案请求 - ID: {}", id);
        
        try {
            boolean result = archiveService.deleteArchive(id);
            
            if (!result) {
                logger.warn("未找到ID为{}的档案", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "档案不存在"));
            }
            
            logger.info("删除档案成功 - ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            logger.error("删除档案失败 - ID: " + id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除档案失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量删除档案
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除档案")
    public ResponseEntity<ApiResponse<Integer>> batchDeleteArchives(
            @ApiParam("档案ID列表") @RequestBody List<Long> ids) {
        
        logger.info("批量删除档案请求 - IDs: {}", ids);
        
        try {
            int count = archiveService.batchDeleteArchives(ids);
            
            logger.info("批量删除档案成功 - 删除数量: {}", count);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            logger.error("批量删除档案失败", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "批量删除档案失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    @ApiOperation("获取档案统计数据")
    public ResponseEntity<ApiResponse<StatisticsDTO>> getStatistics() {
        logger.info("获取档案统计数据请求");
        
        try {
            StatisticsDTO statistics = archiveService.getArchiveStatistics();
            logger.info("获取档案统计数据成功");
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            logger.error("获取档案统计数据失败", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取档案统计数据失败: " + e.getMessage()));
        }
    }
    
    /**
     * 借阅档案
     */
    @PostMapping("/{id}/borrow")
    @ApiOperation("借阅档案")
    public ResponseEntity<ApiResponse<BorrowRecordDTO>> borrowArchive(
            @ApiParam("档案ID") @PathVariable Long id,
            @ApiParam("借阅信息") @Valid @RequestBody BorrowRecordDTO borrowRecord) {
        
        logger.info("借阅档案请求 - 档案ID: {}, 借阅人: {}", id, borrowRecord.getBorrower());
        
        try {
            // 设置档案ID
            borrowRecord.setArchiveId(id);
            
            BorrowRecordDTO record = archiveService.borrowArchive(borrowRecord);
            
            logger.info("借阅档案成功 - 档案ID: {}, 借阅记录ID: {}", id, record.getId());
            return ResponseEntity.ok(ApiResponse.success(record));
        } catch (Exception e) {
            logger.error("借阅档案失败 - 档案ID: " + id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "借阅档案失败: " + e.getMessage()));
        }
    }
    
    /**
     * 归还档案
     */
    @PostMapping("/{id}/return")
    @ApiOperation("归还档案")
    public ResponseEntity<ApiResponse<BorrowRecordDTO>> returnArchive(
            @ApiParam("档案ID") @PathVariable Long id) {
        
        logger.info("归还档案请求 - 档案ID: {}", id);
        
        try {
            BorrowRecordDTO record = archiveService.returnArchive(id);
            
            logger.info("归还档案成功 - 档案ID: {}, 借阅记录ID: {}", id, record.getId());
            return ResponseEntity.ok(ApiResponse.success(record));
        } catch (Exception e) {
            logger.error("归还档案失败 - 档案ID: " + id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "归还档案失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取借阅记录
     */
    @GetMapping("/borrow-records")
    @ApiOperation("获取借阅记录")
    public ResponseEntity<ApiResponse<PageResult<BorrowRecordDTO>>> getBorrowRecords(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @ApiParam("档案ID") @RequestParam(required = false) Long archiveId,
            @ApiParam("借阅人") @RequestParam(required = false) String borrower,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        
        logger.info("获取借阅记录请求 - 页码: {}, 每页条数: {}, 档案ID: {}, 借阅人: {}, 状态: {}", 
                page, pageSize, archiveId, borrower, status);
        
        try {
            PageResult<BorrowRecordDTO> result = archiveService.getBorrowRecords(
                    page, pageSize, archiveId, borrower, status);
            
            logger.info("获取借阅记录成功，返回{}条记录", result.getTotal());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            logger.error("获取借阅记录失败", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取借阅记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 分享档案
     */
    @PostMapping("/{id}/share")
    @ApiOperation("分享档案")
    public ResponseEntity<ApiResponse<String>> shareArchive(
            @ApiParam("档案ID") @PathVariable Long id,
            @ApiParam("过期天数") @RequestParam(defaultValue = "7") Integer expireDays) {
        
        logger.info("分享档案请求 - 档案ID: {}, 过期天数: {}", id, expireDays);
        
        try {
            String shareUrl = archiveService.shareArchive(id, expireDays);
            
            logger.info("分享档案成功 - 档案ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(shareUrl));
        } catch (Exception e) {
            logger.error("分享档案失败 - 档案ID: " + id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "分享档案失败: " + e.getMessage()));
        }
    }
    
    /**
     * 下载档案
     */
    @GetMapping("/{id}/download")
    @ApiOperation("下载档案")
    public ResponseEntity<Resource> downloadArchive(@PathVariable Long id) {
        logger.info("下载档案请求 - 档案ID: {}", id);
        
        try {
            Resource resource = archiveService.downloadArchive(id);
            
            logger.info("下载档案成功 - 档案ID: {}", id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("下载档案失败 - 档案ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 导出档案
     */
    @PostMapping("/export")
    @ApiOperation("导出档案")
    public ResponseEntity<Resource> exportArchives(@RequestBody(required = false) Map<String, Object> filters) {
        logger.info("导出档案请求 - 过滤条件: {}", filters);
        
        if (filters == null) {
            filters = new HashMap<>();
        }
        
        try {
            Resource resource = archiveService.exportArchives(filters);
            
            logger.info("导出档案成功");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archives.xlsx\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("导出档案失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 批量导出档案
     */
    @PostMapping("/batch-export")
    @ApiOperation("批量导出档案")
    public ResponseEntity<Resource> batchExportArchives(@RequestBody List<Long> ids) {
        logger.info("批量导出档案请求 - IDs: {}", ids);
        
        try {
            Resource resource = archiveService.batchExportArchives(ids);
            
            logger.info("批量导出档案成功");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archives.xlsx\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("批量导出档案失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 导入档案
     */
    @PostMapping("/import")
    @ApiOperation("导入档案")
    public ResponseEntity<ApiResponse<Integer>> importArchives(@RequestParam("file") MultipartFile file) {
        logger.info("导入档案请求 - 文件名: {}", file.getOriginalFilename());
        
        try {
            int count = archiveService.importArchives(file);
            
            logger.info("导入档案成功 - 导入数量: {}", count);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            logger.error("导入档案失败", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "导入档案失败: " + e.getMessage()));
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health-check")
    @ApiOperation("API健康检查")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        logger.info("API健康检查请求");
        
        Map<String, Object> healthData = Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "version", "1.0.0"
        );
        
        return ResponseEntity.ok(ApiResponse.success(healthData));
    }
} 