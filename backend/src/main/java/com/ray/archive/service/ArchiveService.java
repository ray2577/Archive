package com.ray.archive.service;

import com.ray.archive.dto.ArchiveDTO;
import com.ray.archive.dto.BorrowRecordDTO;
import com.ray.archive.dto.PageResult;
import com.ray.archive.dto.StatisticsDTO;
import com.ray.archive.entity.Archive;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 档案服务接口
 */
public interface ArchiveService {
    // 基础CRUD方法
    Archive save(Archive archive);
    Optional<Archive> findById(Long id);
    Optional<Archive> findByFileNumber(String fileNumber);
    Page<Archive> findAll(Pageable pageable);
    List<Archive> findByCategory(String category);
    List<Archive> search(String keyword);
    void deleteById(Long id);
    boolean existsByFileNumber(String fileNumber);
    List<Archive> findByStatus(String status);
    
    // 统计相关方法
    long count();
    long countByStatus(String status);
    
    // 文件操作相关方法
    Resource loadFileAsResource(Long id);
    List<Map<String, Object>> getBorrowRecords(Map<String, String> params);
    
    /**
     * 获取档案列表，支持分页、排序和过滤
     * @param page 页码
     * @param pageSize 每页条数
     * @param fileNumber 档案编号
     * @param title 档案标题
     * @param type 档案类型
     * @param status 档案状态
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页结果
     */
    PageResult<ArchiveDTO> getArchives(int page, int pageSize, String fileNumber, String title, 
                                      String type, String status, String sortBy, String sortDirection);
    
    /**
     * 根据ID获取档案详情
     * @param id 档案ID
     * @return 档案DTO
     */
    ArchiveDTO getArchiveById(Long id);
    
    /**
     * 根据档案编号获取档案详情
     * @param fileNumber 档案编号
     * @return 档案DTO
     */
    ArchiveDTO getArchiveByFileNumber(String fileNumber);
    
    /**
     * 创建新档案
     * @param archiveDTO 档案数据
     * @param file 档案文件
     * @return 创建后的档案DTO
     */
    ArchiveDTO createArchive(ArchiveDTO archiveDTO, MultipartFile file);
    
    /**
     * 更新档案
     * @param id 档案ID
     * @param archiveDTO 档案数据
     * @param file 档案文件
     * @return 更新后的档案DTO
     */
    ArchiveDTO updateArchive(Long id, ArchiveDTO archiveDTO, MultipartFile file);
    
    /**
     * 删除档案
     * @param id 档案ID
     * @return 是否删除成功
     */
    boolean deleteArchive(Long id);
    
    /**
     * 批量删除档案
     * @param ids 档案ID列表
     * @return 删除的数量
     */
    int batchDeleteArchives(List<Long> ids);
    
    /**
     * 借阅档案
     * @param borrowRecord 借阅信息
     * @return 借阅记录DTO
     */
    BorrowRecordDTO borrowArchive(BorrowRecordDTO borrowRecord);
    
    /**
     * 归还档案
     * @param id 档案ID
     * @return 借阅记录DTO
     */
    BorrowRecordDTO returnArchive(Long id);
    
    /**
     * 获取借阅记录
     * @param page 页码
     * @param pageSize 每页条数
     * @param archiveId 档案ID
     * @param borrower 借阅人
     * @param status 状态
     * @return 分页结果
     */
    PageResult<BorrowRecordDTO> getBorrowRecords(int page, int pageSize, Long archiveId, 
                                               String borrower, Integer status);
    
    /**
     * 获取档案统计数据
     * @return 统计数据DTO
     */
    StatisticsDTO getArchiveStatistics();
    
    /**
     * 分享档案
     * @param id 档案ID
     * @param expireDays 过期天数
     * @return 分享URL
     */
    String shareArchive(Long id, Integer expireDays);
    
    /**
     * 下载档案资源
     * @param id 档案ID
     * @return 档案资源
     */
    Resource downloadArchive(Long id);
    
    /**
     * 导出档案
     * @param filters 过滤条件
     * @return 导出资源
     */
    Resource exportArchives(Map<String, Object> filters);
    
    /**
     * 批量导出档案
     * @param ids 档案ID列表
     * @return 导出资源
     */
    Resource batchExportArchives(List<Long> ids);
    
    /**
     * 导入档案
     * @param file 导入文件
     * @return 导入数量
     */
    int importArchives(MultipartFile file);
} 