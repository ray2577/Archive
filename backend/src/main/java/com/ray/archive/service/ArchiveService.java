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

public interface ArchiveService {
    Archive save(Archive archive);
    Optional<Archive> findById(Long id);
    Optional<Archive> findByFileNumber(String fileNumber);
    Page<Archive> findAll(Pageable pageable);
    List<Archive> findByCategory(String category);
    List<Archive> search(String keyword);
    void deleteById(Long id);
    boolean existsByFileNumber(String fileNumber);
    List<Archive> findByStatus(String status);
    
    // New methods for statistics and file operations
    long count();
    long countByStatus(String status);
    Resource loadFileAsResource(Long id);
    List<Map<String, Object>> getBorrowRecords(Map<String, String> params);
    
    // 根据ArchiveController方法添加的新方法
    /**
     * 获取档案列表，支持分页、排序和过滤
     * @param page 页码
     * @param pageSize 每页条数
     * @param code 档案编号
     * @param name 档案名称
     * @param type 档案类型
     * @param status 档案状态
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页结果
     */
    PageResult<ArchiveDTO> getArchives(int page, int pageSize, String code, String name, 
                                      String type, String status, String sortBy, String sortDirection);
    
    /**
     * 根据ID获取档案详情
     * @param id 档案ID
     * @return 档案DTO
     */
    ArchiveDTO getArchiveById(Long id);
    
    /**
     * 创建新档案
     * @param archiveDTO 档案数据
     * @param file 档案文件
     * @return 创建后的档案DTO
     */
    ArchiveDTO createArchive(ArchiveDTO archiveDTO, MultipartFile file);
    
    /**
     * 更新档案
     * @param archiveDTO 档案数据
     * @param file 档案文件
     * @return 更新后的档案DTO
     */
    ArchiveDTO updateArchive(ArchiveDTO archiveDTO, MultipartFile file);
    
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
} 