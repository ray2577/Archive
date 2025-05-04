package com.ray.archive.service.impl;

import com.ray.archive.entity.Archive;
import com.ray.archive.service.ArchiveExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 档案导出服务实现类
 */
@Service
public class ArchiveExportServiceImpl implements ArchiveExportService {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveExportServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Value("${file.export-dir:./exports}")
    private String exportDir;
    
    @Override
    public Resource generateExportFile(List<Archive> archives) {
        return generateExportFile(archives, "excel");
    }
    
    @Override
    public Resource generateExportFile(List<Archive> archives, String format) {
        try {
            // 创建导出目录
            Path exportPath = Paths.get(exportDir).toAbsolutePath().normalize();
            Files.createDirectories(exportPath);
            
            // 生成唯一文件名
            String fileName = "archive_export_" + UUID.randomUUID().toString();
            
            if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
                fileName += ".xlsx";
                return exportToExcel(archives, exportPath.resolve(fileName));
            } else if ("csv".equalsIgnoreCase(format)) {
                fileName += ".csv";
                return exportToCsv(archives, exportPath.resolve(fileName));
            } else {
                // 默认为Excel
                fileName += ".xlsx";
                return exportToExcel(archives, exportPath.resolve(fileName));
            }
        } catch (Exception e) {
            logger.error("导出档案失败", e);
            throw new RuntimeException("导出档案失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 导出为Excel格式
     */
    private Resource exportToExcel(List<Archive> archives, Path filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("档案列表");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                "ID", "档案编号", "标题", "描述", "类型", "类别", 
                "状态", "位置", "负责人", "文件类型", "文件大小", 
                "创建时间", "更新时间", "借阅次数"
            };
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 1;
            for (Archive archive : archives) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(archive.getId());
                row.createCell(1).setCellValue(archive.getFileNumber());
                row.createCell(2).setCellValue(archive.getTitle());
                row.createCell(3).setCellValue(archive.getDescription() != null ? archive.getDescription() : "");
                row.createCell(4).setCellValue(archive.getType() != null ? archive.getType() : "");
                row.createCell(5).setCellValue(archive.getCategory());
                row.createCell(6).setCellValue(archive.getStatus());
                row.createCell(7).setCellValue(archive.getLocation() != null ? archive.getLocation() : "");
                row.createCell(8).setCellValue(archive.getResponsible() != null ? archive.getResponsible() : "");
                row.createCell(9).setCellValue(archive.getFileType() != null ? archive.getFileType() : "");
                row.createCell(10).setCellValue(archive.getFileSize() != null ? archive.getFileSize() : 0);
                row.createCell(11).setCellValue(formatDateTime(archive.getCreateTime()));
                row.createCell(12).setCellValue(formatDateTime(archive.getUpdateTime()));
                row.createCell(13).setCellValue(archive.getBorrowCount() != null ? archive.getBorrowCount() : 0);
            }
            
            // 自动调整列宽
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 保存工作簿到文件
            try (FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {
                workbook.write(fileOut);
            }
            
            return new FileSystemResource(filePath.toFile());
        } catch (Exception e) {
            logger.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 导出为CSV格式
     */
    private Resource exportToCsv(List<Archive> archives, Path filePath) {
        try {
            StringBuilder csv = new StringBuilder();
            
            // 添加标题行
            csv.append("ID,档案编号,标题,描述,类型,类别,状态,位置,负责人,文件类型,文件大小,创建时间,更新时间,借阅次数\n");
            
            // 添加数据行
            for (Archive archive : archives) {
                csv.append(archive.getId()).append(",");
                csv.append(escapeCsvField(archive.getFileNumber())).append(",");
                csv.append(escapeCsvField(archive.getTitle())).append(",");
                csv.append(escapeCsvField(archive.getDescription())).append(",");
                csv.append(escapeCsvField(archive.getType())).append(",");
                csv.append(escapeCsvField(archive.getCategory())).append(",");
                csv.append(escapeCsvField(archive.getStatus())).append(",");
                csv.append(escapeCsvField(archive.getLocation())).append(",");
                csv.append(escapeCsvField(archive.getResponsible())).append(",");
                csv.append(escapeCsvField(archive.getFileType())).append(",");
                csv.append(archive.getFileSize() != null ? archive.getFileSize() : 0).append(",");
                csv.append(escapeCsvField(formatDateTime(archive.getCreateTime()))).append(",");
                csv.append(escapeCsvField(formatDateTime(archive.getUpdateTime()))).append(",");
                csv.append(archive.getBorrowCount() != null ? archive.getBorrowCount() : 0).append("\n");
            }
            
            // 写入CSV文件
            Files.write(filePath, csv.toString().getBytes());
            
            return new FileSystemResource(filePath.toFile());
        } catch (Exception e) {
            logger.error("导出CSV失败", e);
            throw new RuntimeException("导出CSV失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "";
    }
    
    /**
     * 转义CSV字段中的特殊字符
     */
    private String escapeCsvField(String field) {
        if (field == null) return "";
        
        // 如果字段包含逗号、引号或换行符，需要用引号包围
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            // 将字段中的引号替换为两个引号
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
} 