package com.ray.archive.service;

import com.ray.archive.entity.Archive;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 档案导出服务接口
 */
public interface ArchiveExportService {
    
    /**
     * 生成导出文件
     *
     * @param archives 要导出的档案列表
     * @return 导出的资源文件
     */
    Resource generateExportFile(List<Archive> archives);
    
    /**
     * 生成导出文件（指定格式）
     *
     * @param archives 要导出的档案列表
     * @param format 导出格式，如 "excel", "csv", "pdf" 等
     * @return 导出的资源文件
     */
    Resource generateExportFile(List<Archive> archives, String format);
} 