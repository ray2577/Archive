package com.ray.archive.service;

import com.ray.archive.dto.ArchiveDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 档案导入服务接口
 */
public interface ArchiveImportService {
    
    /**
     * 解析导入文件
     *
     * @param file 导入的文件
     * @return 解析出的档案DTO列表
     */
    List<ArchiveDTO> parseImportFile(MultipartFile file);
    
    /**
     * 验证导入的档案数据
     *
     * @param archives 档案DTO列表
     * @return 验证结果，key为索引，value为错误消息
     */
    Map<Integer, String> validateImportData(List<ArchiveDTO> archives);
} 