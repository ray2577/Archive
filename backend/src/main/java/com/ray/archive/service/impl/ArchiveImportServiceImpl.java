package com.ray.archive.service.impl;

import com.ray.archive.dto.ArchiveDTO;
import com.ray.archive.service.ArchiveImportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 档案导入服务实现类
 */
@Service
public class ArchiveImportServiceImpl implements ArchiveImportService {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveImportServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public List<ArchiveDTO> parseImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Collections.emptyList();
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Collections.emptyList();
        }
        
        try {
            if (originalFilename.endsWith(".xlsx") || originalFilename.endsWith(".xls")) {
                return parseExcel(file);
            } else if (originalFilename.endsWith(".csv")) {
                return parseCsv(file);
            } else {
                logger.error("不支持的文件格式: {}", originalFilename);
                throw new RuntimeException("不支持的文件格式，请上传Excel或CSV文件");
            }
        } catch (Exception e) {
            logger.error("解析导入文件失败", e);
            throw new RuntimeException("解析导入文件失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<Integer, String> validateImportData(List<ArchiveDTO> archives) {
        Map<Integer, String> errors = new HashMap<>();
        
        for (int i = 0; i < archives.size(); i++) {
            ArchiveDTO archive = archives.get(i);
            if (archive.getFileNumber() == null || archive.getFileNumber().isEmpty()) {
                errors.put(i, "档案编号不能为空");
            } else if (archive.getTitle() == null || archive.getTitle().isEmpty()) {
                errors.put(i, "档案标题不能为空");
            } else if (archive.getCategory() == null || archive.getCategory().isEmpty()) {
                errors.put(i, "档案类别不能为空");
            }
        }
        
        return errors;
    }
    
    /**
     * 解析Excel文件
     */
    private List<ArchiveDTO> parseExcel(MultipartFile file) throws Exception {
        List<ArchiveDTO> archives = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 获取标题行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return Collections.emptyList();
            }
            
            // 解析标题行，获取列索引
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerValue = cell.getStringCellValue().trim();
                    columnIndexMap.put(headerValue, i);
                }
            }
            
            // 处理数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    ArchiveDTO archive = new ArchiveDTO();
                    
                    // 设置各个字段的值
                    setFieldFromExcelRow(archive, row, columnIndexMap);
                    
                    // 添加到列表
                    if (isValidArchive(archive)) {
                        archives.add(archive);
                    }
                }
            }
        }
        
        return archives;
    }
    
    /**
     * 从Excel行中设置档案字段
     */
    private void setFieldFromExcelRow(ArchiveDTO archive, Row row, Map<String, Integer> columnIndexMap) {
        setCellValue(archive, row, columnIndexMap, "档案编号", value -> archive.setFileNumber(value));
        setCellValue(archive, row, columnIndexMap, "标题", value -> archive.setTitle(value));
        setCellValue(archive, row, columnIndexMap, "描述", value -> archive.setDescription(value));
        setCellValue(archive, row, columnIndexMap, "类型", value -> archive.setType(value));
        setCellValue(archive, row, columnIndexMap, "类别", value -> archive.setCategory(value));
        setCellValue(archive, row, columnIndexMap, "状态", value -> archive.setStatus(value));
        setCellValue(archive, row, columnIndexMap, "位置", value -> archive.setLocation(value));
        setCellValue(archive, row, columnIndexMap, "负责人", value -> archive.setResponsible(value));
        
        // 如果有关键词列，解析为List
        if (columnIndexMap.containsKey("关键词")) {
            String keywordsStr = getCellValueAsString(row, columnIndexMap.get("关键词"));
            if (keywordsStr != null && !keywordsStr.isEmpty()) {
                archive.setKeywords(Arrays.asList(keywordsStr.split(",")));
            }
        }
        
        // 设置默认值
        if (archive.getStatus() == null) {
            archive.setStatus("AVAILABLE");
        }
    }
    
    /**
     * 设置单元格值到档案字段
     */
    private void setCellValue(ArchiveDTO archive, Row row, Map<String, Integer> columnIndexMap, String columnName, java.util.function.Consumer<String> setter) {
        if (columnIndexMap.containsKey(columnName)) {
            String value = getCellValueAsString(row, columnIndexMap.get(columnName));
            if (value != null && !value.isEmpty()) {
                setter.accept(value);
            }
        }
    }
    
    /**
     * 获取单元格的字符串值
     */
    private String getCellValueAsString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DATE_FORMATTER);
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    /**
     * 解析CSV文件
     */
    private List<ArchiveDTO> parseCsv(MultipartFile file) throws Exception {
        List<ArchiveDTO> archives = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // 读取标题行
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 解析标题行，获取列索引
            String[] headers = headerLine.split(",");
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim();
                columnIndexMap.put(header, i);
            }
            
            // 读取数据行
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = splitCsvLine(line);
                if (fields.length > 0) {
                    ArchiveDTO archive = new ArchiveDTO();
                    
                    // 设置各个字段的值
                    setFieldFromCsvFields(archive, fields, columnIndexMap);
                    
                    // 添加到列表
                    if (isValidArchive(archive)) {
                        archives.add(archive);
                    }
                }
            }
        }
        
        return archives;
    }
    
    /**
     * 拆分CSV行，处理带引号的字段
     */
    private String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // 遇到双引号，切换引号状态
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 处理转义的双引号 (两个连续的双引号)
                    currentField.append('"');
                    i++; // 跳过下一个双引号
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // 遇到逗号且不在引号内，添加字段并重置
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                // 普通字符，添加到当前字段
                currentField.append(c);
            }
        }
        
        // 添加最后一个字段
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }
    
    /**
     * 从CSV字段中设置档案字段
     */
    private void setFieldFromCsvFields(ArchiveDTO archive, String[] fields, Map<String, Integer> columnIndexMap) {
        setFieldValue(archive, fields, columnIndexMap, "档案编号", value -> archive.setFileNumber(value));
        setFieldValue(archive, fields, columnIndexMap, "标题", value -> archive.setTitle(value));
        setFieldValue(archive, fields, columnIndexMap, "描述", value -> archive.setDescription(value));
        setFieldValue(archive, fields, columnIndexMap, "类型", value -> archive.setType(value));
        setFieldValue(archive, fields, columnIndexMap, "类别", value -> archive.setCategory(value));
        setFieldValue(archive, fields, columnIndexMap, "状态", value -> archive.setStatus(value));
        setFieldValue(archive, fields, columnIndexMap, "位置", value -> archive.setLocation(value));
        setFieldValue(archive, fields, columnIndexMap, "负责人", value -> archive.setResponsible(value));
        
        // 如果有关键词列，解析为List
        if (columnIndexMap.containsKey("关键词")) {
            int index = columnIndexMap.get("关键词");
            if (index < fields.length) {
                String keywordsStr = fields[index];
                if (keywordsStr != null && !keywordsStr.isEmpty()) {
                    archive.setKeywords(Arrays.asList(keywordsStr.split(",")));
                }
            }
        }
        
        // 设置默认值
        if (archive.getStatus() == null) {
            archive.setStatus("AVAILABLE");
        }
    }
    
    /**
     * 设置CSV字段值到档案字段
     */
    private void setFieldValue(ArchiveDTO archive, String[] fields, Map<String, Integer> columnIndexMap, String columnName, java.util.function.Consumer<String> setter) {
        if (columnIndexMap.containsKey(columnName)) {
            int index = columnIndexMap.get(columnName);
            if (index < fields.length) {
                String value = fields[index];
                if (value != null && !value.isEmpty()) {
                    setter.accept(value);
                }
            }
        }
    }
    
    /**
     * 判断档案是否有效
     */
    private boolean isValidArchive(ArchiveDTO archive) {
        return archive.getFileNumber() != null && !archive.getFileNumber().isEmpty()
                && archive.getTitle() != null && !archive.getTitle().isEmpty();
    }
} 