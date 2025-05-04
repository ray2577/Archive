package com.ray.archive.service;

import com.ray.archive.dto.DocumentDTO;
import com.ray.archive.dto.DocumentStatisticsDTO;
import com.ray.archive.dto.PageResult;
import com.ray.archive.entity.Document;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文档服务接口
 */
public interface DocumentService {
    
    /**
     * 创建新文档
     * @param documentDTO 文档数据
     * @param file 文档文件
     * @return 创建的文档DTO
     */
    DocumentDTO createDocument(DocumentDTO documentDTO, MultipartFile file);
    
    /**
     * 更新文档
     * @param id 文档ID
     * @param documentDTO 文档数据
     * @param file 文档文件
     * @return 更新后的文档DTO
     */
    DocumentDTO updateDocument(Long id, DocumentDTO documentDTO, MultipartFile file);
    
    /**
     * 根据ID获取文档
     * @param id 文档ID
     * @return 文档DTO
     */
    DocumentDTO getDocumentById(Long id);
    
    /**
     * 根据编码获取文档
     * @param code 文档编码
     * @return 文档DTO
     */
    DocumentDTO getDocumentByCode(String code);
    
    /**
     * 分页获取文档列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param code 文档编码
     * @param name 文档名称
     * @param type 文档类型
     * @param category 文档分类
     * @param status 文档状态
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页结果
     */
    PageResult<DocumentDTO> getDocuments(int page, int pageSize, String code, String name, 
                                        String type, String category, String status,
                                        String sortBy, String sortDirection);
    
    /**
     * 删除文档
     * @param id 文档ID
     * @return 是否删除成功
     */
    boolean deleteDocument(Long id);
    
    /**
     * 批量删除文档
     * @param ids 文档ID列表
     * @return 删除数量
     */
    int batchDeleteDocuments(List<Long> ids);
    
    /**
     * 获取文档统计数据
     * @return 统计数据DTO
     */
    DocumentStatisticsDTO getDocumentStatistics();
    
    /**
     * 分享文档
     * @param id 文档ID
     * @param expireDays 过期天数
     * @return 分享URL
     */
    String shareDocument(Long id, Integer expireDays);
    
    /**
     * 下载文档资源
     * @param id 文档ID
     * @return 文档资源
     */
    Resource downloadDocument(Long id);
    
    /**
     * 导出文档
     * @param filters 过滤条件
     * @return 导出资源
     */
    Resource exportDocuments(Map<String, Object> filters);
    
    /**
     * 批量导出文档
     * @param ids 文档ID列表
     * @return 导出资源
     */
    Resource batchExportDocuments(List<Long> ids);
    
    /**
     * 导入文档
     * @param file 导入文件
     * @return 导入数量
     */
    int importDocuments(MultipartFile file);
} 