package com.ray.archive.service.impl;

import com.ray.archive.dto.ArchiveDTO;
import com.ray.archive.dto.BorrowRecordDTO;
import com.ray.archive.dto.PageResult;
import com.ray.archive.dto.StatisticsDTO;
import com.ray.archive.entity.Archive;
import com.ray.archive.entity.BorrowRecord;
import com.ray.archive.entity.BorrowStatus;
import com.ray.archive.exception.DuplicateResourceException;
import com.ray.archive.exception.PartialProcessingException;
import com.ray.archive.exception.ResourceNotFoundException;
import com.ray.archive.exception.ServiceException;
import com.ray.archive.repository.ArchiveRepository;
import com.ray.archive.repository.BorrowRecordRepository;
import com.ray.archive.service.ArchiveExportService;
import com.ray.archive.service.ArchiveImportService;
import com.ray.archive.service.ArchiveService;
import com.ray.archive.service.FileStorageService;
import com.ray.archive.util.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@Transactional
public class ArchiveServiceImpl implements ArchiveService {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveServiceImpl.class);

    private final ArchiveRepository archiveRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final FileStorageService fileStorageService;
    private final ArchiveExportService archiveExportService;
    private final ArchiveImportService archiveImportService;
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    public ArchiveServiceImpl(ArchiveRepository archiveRepository, 
                             BorrowRecordRepository borrowRecordRepository,
                             FileStorageService fileStorageService,
                             ArchiveExportService archiveExportService,
                             ArchiveImportService archiveImportService) {
        this.archiveRepository = archiveRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.fileStorageService = fileStorageService;
        this.archiveExportService = archiveExportService;
        this.archiveImportService = archiveImportService;
    }

    @Override
    public Archive save(Archive archive) {
        return archiveRepository.save(archive);
    }

    @Override
    public Optional<Archive> findById(Long id) {
        return archiveRepository.findById(id);
    }

    @Override
    public Optional<Archive> findByFileNumber(String fileNumber) {
        return archiveRepository.findByFileNumber(fileNumber);
    }

    @Override
    public Page<Archive> findAll(Pageable pageable) {
        return archiveRepository.findAll(pageable);
    }

    @Override
    public List<Archive> findByCategory(String category) {
        return archiveRepository.findByCategory(category);
    }

    @Override
    public List<Archive> search(String keyword) {
        return archiveRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }

    @Override
    public void deleteById(Long id) {
        archiveRepository.deleteById(id);
    }

    @Override
    public boolean existsByFileNumber(String fileNumber) {
        return archiveRepository.findByFileNumber(fileNumber).isPresent();
    }

    @Override
    public List<Archive> findByStatus(String status) {
        return archiveRepository.findByStatus(status);
    }

    @Override
    public long count() {
        return archiveRepository.count();
    }
    
    @Override
    public long countByStatus(String status) {
        return archiveRepository.countByStatus(status);
    }
    
    @Override
    public Resource loadFileAsResource(Long id) {
        try {
            Optional<Archive> archiveOpt = findById(id);
            if (!archiveOpt.isPresent()) {
                throw new FileNotFoundException("File not found for archive with id: " + id);
            }
            
            Archive archive = archiveOpt.get();
            String filePath = archive.getFileUrl();
            
            // If the fileUrl is a relative path, resolve it against the upload directory
            if (filePath != null && !filePath.startsWith("http")) {
                Path path = Paths.get(uploadDir).resolve(filePath).normalize();
                Resource resource = new UrlResource(path.toUri());
                
                if (resource.exists()) {
                    return resource;
                } else {
                    throw new FileNotFoundException("File not found: " + filePath);
                }
            } else if (filePath != null) {
                // Handle external URLs
                return new UrlResource(filePath);
            } else {
                throw new FileNotFoundException("File URL is null for archive with id: " + id);
            }
        } catch (MalformedURLException | FileNotFoundException e) {
            throw new RuntimeException("Error loading file for archive with id: " + id, e);
        }
    }
    
    @Override
    public List<Map<String, Object>> getBorrowRecords(Map<String, String> params) {
        // This is a stub implementation - in a real application, you would query a BorrowRecord entity
        List<Map<String, Object>> records = new ArrayList<>();
        
        // Find archives that are currently borrowed
        List<Archive> borrowedArchives = findByStatus("BORROWED");
        
        // Convert to records with additional metadata
        for (Archive archive : borrowedArchives) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", archive.getId());
            record.put("archiveId", archive.getId());
            record.put("archiveTitle", archive.getTitle());
            record.put("borrowDate", archive.getUpdateTime());
            record.put("borrowerId", 1L); // This would come from actual borrow records
            record.put("borrowerName", "John Doe"); // This would come from actual user data
            record.put("dueDate", archive.getUpdateTime().plusDays(14)); // Example due date calculation
            record.put("status", "BORROWED");
            
            records.add(record);
        }
        
        return records;
    }

    @Override
    public PageResult<ArchiveDTO> getArchives(int page, int pageSize, String fileNumber, String title, String type, String status, String sortBy, String sortDirection) {
        try {
            // Create pageable object
            Pageable pageable = PageHelper.createPageable(page, pageSize, sortBy, sortDirection);
            
            // Create specification for dynamic filtering
            Specification<Archive> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (fileNumber != null && !fileNumber.isEmpty()) {
                    predicates.add(cb.like(root.get("fileNumber"), "%" + fileNumber + "%"));
                }
                
                if (title != null && !title.isEmpty()) {
                    predicates.add(cb.like(root.get("title"), "%" + title + "%"));
                }
                
                if (type != null && !type.isEmpty()) {
                    predicates.add(cb.equal(root.get("type"), type));
                }
                
                if (status != null && !status.isEmpty()) {
                    predicates.add(cb.equal(root.get("status"), status));
                }
                
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            
            // Execute query with specification and pageable
            Page<Archive> archivePage = archiveRepository.findAll(spec, pageable);
            
            // Convert to DTOs
            List<ArchiveDTO> archiveDTOs = archivePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            // Create and return page result
            return new PageResult<>(
                archiveDTOs,
                archivePage.getTotalElements(),
                archivePage.getTotalPages(),
                archivePage.getNumber() + 1
            );
        } catch (Exception e) {
            throw new ServiceException("获取档案列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ArchiveDTO getArchiveById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("档案ID不能为空");
        }
        
        return findById(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的档案"));
    }

    @Override
    public ArchiveDTO createArchive(ArchiveDTO archiveDTO, MultipartFile file) {
        if (archiveDTO == null) {
            throw new IllegalArgumentException("档案数据不能为空");
        }
        
        // Validate file number uniqueness
        if (archiveDTO.getFileNumber() != null && existsByFileNumber(archiveDTO.getFileNumber())) {
            throw new DuplicateResourceException("档案编号已存在: " + archiveDTO.getFileNumber());
        }
        
        try {
            // Convert DTO to entity
            Archive archive = convertToEntity(archiveDTO);
            
            // Set creation metadata
            LocalDateTime now = LocalDateTime.now();
            archive.setCreateTime(now);
            archive.setUpdateTime(now);
            archive.setStatus(archiveDTO.getStatus() != null ? archiveDTO.getStatus() : "AVAILABLE");
            
            // Handle file upload if provided
            if (file != null && !file.isEmpty()) {
                String fileUrl = fileStorageService.storeFile(file);
                archive.setFileUrl(fileUrl);
                //archive.setFileName(file.getOriginalFilename());
                archive.setFileSize(file.getSize());
                archive.setFileType(file.getContentType());
            }
            
            // Save the archive
            Archive savedArchive = save(archive);
            
            // Convert back to DTO and return
            return convertToDTO(savedArchive);
        } catch (Exception e) {
            throw new ServiceException("创建档案失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ArchiveDTO updateArchive(Long id, ArchiveDTO archiveDTO, MultipartFile file) {
        if (id == null || archiveDTO == null) {
            throw new IllegalArgumentException("档案ID和数据不能为空");
        }
        
        try {
            // Find existing archive
            Archive existingArchive = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的档案"));
            
            // Validate file number uniqueness if changed
            if (archiveDTO.getFileNumber() != null && 
                !archiveDTO.getFileNumber().equals(existingArchive.getFileNumber()) &&
                existsByFileNumber(archiveDTO.getFileNumber())) {
                throw new DuplicateResourceException("档案编号已存在: " + archiveDTO.getFileNumber());
            }
            
            // Update fields from DTO
            if (archiveDTO.getFileNumber() != null) existingArchive.setFileNumber(archiveDTO.getFileNumber());
            if (archiveDTO.getTitle() != null) existingArchive.setTitle(archiveDTO.getTitle());
            if (archiveDTO.getDescription() != null) existingArchive.setDescription(archiveDTO.getDescription());
            if (archiveDTO.getCategory() != null) existingArchive.setCategory(archiveDTO.getCategory());
            if (archiveDTO.getType() != null) existingArchive.setType(archiveDTO.getType());
            if (archiveDTO.getStatus() != null) existingArchive.setStatus(archiveDTO.getStatus());
            if (archiveDTO.getLocation() != null) existingArchive.setLocation(archiveDTO.getLocation());
            if (archiveDTO.getKeywords() != null) existingArchive.setKeywords(String.join(",", archiveDTO.getKeywords()));
            if (archiveDTO.getArchiveDate() != null) existingArchive.setArchiveDate(archiveDTO.getArchiveDate());
            if (archiveDTO.getExpirationDate() != null) existingArchive.setExpirationDate(archiveDTO.getExpirationDate());
            
            // Always update the update time
            existingArchive.setUpdateTime(LocalDateTime.now());
            
            // Handle file upload if provided
            if (file != null && !file.isEmpty()) {
                // If there's an existing file, delete it first
                if (existingArchive.getFileUrl() != null) {
                    fileStorageService.deleteFile(existingArchive.getFileUrl());
                }
                
                String fileUrl = fileStorageService.storeFile(file);
                existingArchive.setFileUrl(fileUrl);
                //existingArchive.setFileName(file.getOriginalFilename());
                existingArchive.setFileSize(file.getSize());
                existingArchive.setFileType(file.getContentType());
            }
            
            // Save the updated archive
            Archive updatedArchive = save(existingArchive);
            
            // Convert back to DTO and return
            return convertToDTO(updatedArchive);
        } catch (ResourceNotFoundException | DuplicateResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("更新档案失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteArchive(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("档案ID不能为空");
        }
        
        try {
            // Find existing archive
            Archive existingArchive = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的档案"));
            
            // Check if archive can be deleted (e.g., not currently borrowed)
            if ("BORROWED".equals(existingArchive.getStatus())) {
                throw new IllegalStateException("档案当前处于借出状态，无法删除");
            }
            
            // Delete associated file if exists
            if (existingArchive.getFileUrl() != null) {
                try {
                    fileStorageService.deleteFile(existingArchive.getFileUrl());
                } catch (Exception e) {
                    // Log but continue with deletion of database record
                    logger.warn("删除档案文件失败: {}", e.getMessage());
                }
            }
            
            // Delete the archive record
            deleteById(id);
            
            return true;
        } catch (ResourceNotFoundException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("删除档案失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int batchDeleteArchives(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        int deletedCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Long id : ids) {
            try {
                if (deleteArchive(id)) {
                    deletedCount++;
                }
            } catch (Exception e) {
                errors.add("档案ID " + id + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            throw new PartialProcessingException("部分档案删除失败", errors, deletedCount);
        }
        
        return deletedCount;
    }

    @Override
    public BorrowRecordDTO borrowArchive(BorrowRecordDTO borrowRecordDTO) {
        if (borrowRecordDTO == null || borrowRecordDTO.getArchiveId() == null) {
            throw new IllegalArgumentException("借阅信息或档案ID不能为空");
        }
        
        try {
            // Find the archive
            Long archiveId = borrowRecordDTO.getArchiveId();
            Archive archive = findById(archiveId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + archiveId + "的档案"));
            
            // Check if archive is available for borrowing
            if (!"AVAILABLE".equals(archive.getStatus())) {
                throw new IllegalStateException("档案当前不可借阅，状态为: " + archive.getStatus());
            }
            
            // Create borrow record
            BorrowRecord borrowRecord = new BorrowRecord();
            borrowRecord.setArchive(archive);
            borrowRecord.setBorrower(borrowRecordDTO.getBorrower());
            borrowRecord.setBorrowerDepartment(borrowRecordDTO.getBorrowerDepartment());
            borrowRecord.setBorrowDate(LocalDateTime.now());
            borrowRecord.setExpectedReturnDate(borrowRecordDTO.getExpectedReturnDate());
            borrowRecord.setPurpose(borrowRecordDTO.getPurpose());
            borrowRecord.setApprovedBy(borrowRecordDTO.getApprovedBy());
            
            // 设置状态为BORROWED枚举
            borrowRecord.setStatus(BorrowStatus.valueOf(BorrowStatus.BORROWED.name()));
            
            // Save borrow record
            BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
            
            // Update archive status
            archive.setStatus("BORROWED");
            archive.setBorrowCount(archive.getBorrowCount() + 1);
            archive.setUpdateTime(LocalDateTime.now());
            save(archive);
            
            // Return borrow record DTO
            return convertToBorrowRecordDTO(savedRecord);
        } catch (ResourceNotFoundException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("借阅档案失败: " + e.getMessage(), e);
        }
    }

    @Override
    public BorrowRecordDTO returnArchive(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("档案ID不能为空");
        }
        
        try {
            // Find the archive
            Archive archive = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的档案"));
            
            // Check if archive is borrowed
            if (!"BORROWED".equals(archive.getStatus())) {
                throw new IllegalStateException("档案当前不是借出状态，无法归还，当前状态为: " + archive.getStatus());
            }
            
            // Find active borrow record
            BorrowRecord activeBorrow = borrowRecordRepository.findByArchiveIdAndStatus(id, BorrowStatus.BORROWED)
                .orElseThrow(() -> new ResourceNotFoundException("未找到档案ID为" + id + "的有效借阅记录"));
            
            // Update borrow record
            activeBorrow.setStatus(BorrowStatus.valueOf(BorrowStatus.RETURNED.name()));
            activeBorrow.setActualReturnDate(LocalDateTime.now());
            BorrowRecord updatedRecord = borrowRecordRepository.save(activeBorrow);
            
            // Update archive status
            archive.setStatus("AVAILABLE");
            archive.setUpdateTime(LocalDateTime.now());
            save(archive);
            
            // Return updated borrow record DTO
            return convertToBorrowRecordDTO(updatedRecord);
        } catch (ResourceNotFoundException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("归还档案失败: " + e.getMessage(), e);
        }
    }

    @Override
    public PageResult<BorrowRecordDTO> getBorrowRecords(int page, int pageSize, Long archiveId, String borrower, Integer status) {
        try {
            // Create pageable
            Pageable pageable = PageHelper.createPageable(page, pageSize, "borrowDate", "desc");
            
            // Create specification for filtering
            Specification<BorrowRecord> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (archiveId != null) {
                    predicates.add(cb.equal(root.get("archive").get("id"), archiveId));
                }
                
                if (borrower != null && !borrower.isEmpty()) {
                    predicates.add(cb.like(root.get("borrower"), "%" + borrower + "%"));
                }
                
                if (status != null) {
                    // 使用字符串状态进行比较
                    BorrowStatus borrowStatus = BorrowStatus.fromValue(status);
                    if (borrowStatus != null) {
                        predicates.add(cb.equal(root.get("status"), borrowStatus));
                    }
                }
                
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            
            // Execute query
            Page<BorrowRecord> recordPage = borrowRecordRepository.findAll(spec, pageable);
            
            // Convert to DTOs
            List<BorrowRecordDTO> recordDTOs = recordPage.getContent().stream()
                .map(this::convertToBorrowRecordDTO)
                .collect(Collectors.toList());
            
            // Create and return page result
            return new PageResult<>(
                recordDTOs,
                recordPage.getTotalElements(),
                recordPage.getTotalPages(),
                recordPage.getNumber() + 1
            );
        } catch (Exception e) {
            throw new ServiceException("获取借阅记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取档案统计数据
     * @return 统计数据DTO
     */
    @Override
    public StatisticsDTO getArchiveStatistics() {
        try {
            // Get total count
            long total = count();
            
            // Get count by status
            long available = countByStatus("AVAILABLE");
            long borrowed = countByStatus("BORROWED");
            long processing = countByStatus("PROCESSING");
            
            // Get count of archives created this month
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            long newThisMonth = archiveRepository.countByCreateTimeAfter(startOfMonth);
            
            // Get count by type
            Map<String, Long> typeStats = archiveRepository.countByType();
            
            // Get recent archives
            List<ArchiveDTO> recentArchives = archiveRepository.findTop5ByOrderByCreateTimeDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            // Get most borrowed archives
            List<ArchiveDTO> mostBorrowed = archiveRepository.findTop5ByOrderByBorrowCountDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            // Build and return statistics
            return StatisticsDTO.builder()
                .total(total)
                .available(available)
                .borrowed(borrowed)
                .processing(processing)
                .newThisMonth(newThisMonth)
                .typeDistribution(typeStats)
                .recentArchives(recentArchives)
                .mostBorrowedArchives(mostBorrowed)
                .lastUpdated(LocalDateTime.now())
                .build();
        } catch (Exception e) {
            throw new ServiceException("获取统计数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String shareArchive(Long id, Integer expireDays) {
        if (id == null) {
            throw new IllegalArgumentException("档案ID不能为空");
        }
        
        if (expireDays == null || expireDays <= 0) {
            expireDays = 7; // 默认7天过期
        }
        
        try {
            // 查找档案
            Archive archive = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的档案"));
            
            // 生成分享token (可以使用UUID或其他安全的随机字符串)
            String shareToken = UUID.randomUUID().toString();
            
            // 设置分享过期时间
            LocalDateTime expireTime = LocalDateTime.now().plusDays(expireDays);
            
            // 更新档案分享信息
            archive.setShared(true);
            archive.setShareToken(shareToken);
            archive.setShareExpireTime(expireTime);
            archive.setUpdateTime(LocalDateTime.now());
            save(archive);
            
            // 构建分享URL
            String shareUrl = String.format("%s/api/archives/share/%s", baseUrl, shareToken);
            
            // 记录分享操作日志
            logger.info("档案ID为{}的档案被分享，过期时间为{}", id, expireTime);
            
            return shareUrl;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("分享档案失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource downloadArchive(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("档案ID不能为空");
        }
        
        try {
            // Find the archive
            Archive archive = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的档案"));
            
            // Check if archive has a file
            if (archive.getFileUrl() == null || archive.getFileUrl().isEmpty()) {
                throw new ResourceNotFoundException("档案没有关联文件");
            }
            
            // Record download activity
            archive.setDownloadCount(archive.getDownloadCount() + 1);
            archive.setLastDownloadTime(LocalDateTime.now());
            save(archive);
            
            // Return file resource
            return loadFileAsResource(id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("下载档案文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource exportArchives(Map<String, Object> filters) {
        try {
            // Build query specification based on filters
            Specification<Archive> spec = buildSpecification(filters);
            
            // Find archives matching the filters
            List<Archive> archives = archiveRepository.findAll(spec);
            
            if (archives.isEmpty()) {
                throw new ResourceNotFoundException("未找到符合条件的档案");
            }
            
            // Generate export file (e.g., Excel or CSV)
            Resource exportFile = archiveExportService.generateExportFile(archives);
            
            // Log export activity
            logger.info("导出了{}个档案记录", archives.size());
            
            return exportFile;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("导出档案失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource batchExportArchives(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("档案ID列表不能为空");
        }
        
        try {
            // Find archives by IDs
            List<Archive> archives = archiveRepository.findAllById(ids);
            
            if (archives.isEmpty()) {
                throw new ResourceNotFoundException("未找到指定ID的档案");
            }
            
            // Generate export file
            Resource exportFile = archiveExportService.generateExportFile(archives);
            
            // Log export activity
            logger.info("批量导出了{}个档案记录", archives.size());
            
            return exportFile;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("批量导出档案失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int importArchives(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("导入文件不能为空");
        }
        
        try {
            // Parse the import file
            List<ArchiveDTO> archiveDTOs = archiveImportService.parseImportFile(file);
            
            if (archiveDTOs.isEmpty()) {
                return 0;
            }
            
            int importedCount = 0;
            List<String> errors = new ArrayList<>();
            
            // Process each archive
            for (ArchiveDTO dto : archiveDTOs) {
                try {
                    // Check for duplicate file number
                    if (dto.getFileNumber() != null && existsByFileNumber(dto.getFileNumber())) {
                        errors.add("档案编号已存在: " + dto.getFileNumber());
                        continue;
                    }
                    
                    // Convert to entity and save
                    Archive archive = convertToEntity(dto);
                    
                    // Set default values
                    LocalDateTime now = LocalDateTime.now();
                    archive.setCreateTime(now);
                    archive.setUpdateTime(now);
                    archive.setStatus(dto.getStatus() != null ? dto.getStatus() : "AVAILABLE");
                    
                    Archive savedArchive = save(archive);
                    if (savedArchive.getId() != null) {
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.add("处理档案失败: " + dto.getFileNumber() + " - " + e.getMessage());
                }
            }
            
            // If there were errors, throw exception with details
            if (!errors.isEmpty()) {
                throw new PartialProcessingException("部分档案导入失败", errors, importedCount);
            }
            
            return importedCount;
        } catch (PartialProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("导入档案失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * Builds a specification based on filters for querying archives
     */
    private Specification<Archive> buildSpecification(Map<String, Object> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters != null) {
                if (filters.containsKey("fileNumber")) {
                    predicates.add(cb.like(root.get("fileNumber"), "%" + filters.get("fileNumber") + "%"));
                }
                
                if (filters.containsKey("title")) {
                    predicates.add(cb.like(root.get("title"), "%" + filters.get("title") + "%"));
                }
                
                if (filters.containsKey("type")) {
                    predicates.add(cb.equal(root.get("type"), filters.get("type")));
                }
                
                if (filters.containsKey("status")) {
                    predicates.add(cb.equal(root.get("status"), filters.get("status")));
                }
                
                if (filters.containsKey("category")) {
                    predicates.add(cb.equal(root.get("category"), filters.get("category")));
                }
                
                if (filters.containsKey("startDate") && filters.get("startDate") instanceof LocalDateTime) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), (LocalDateTime) filters.get("startDate")));
                }
                
                if (filters.containsKey("endDate") && filters.get("endDate") instanceof LocalDateTime) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), (LocalDateTime) filters.get("endDate")));
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 根据档案编号获取档案详情
     * @param fileNumber 档案编号
     * @return 档案DTO
     */
    @Override
    public ArchiveDTO getArchiveByFileNumber(String fileNumber) {
        if (fileNumber == null || fileNumber.isEmpty()) {
            throw new IllegalArgumentException("档案编号不能为空");
        }
        
        return findByFileNumber(fileNumber)
            .map(this::convertToDTO)
            .orElseThrow(() -> new ResourceNotFoundException("未找到编号为" + fileNumber + "的档案"));
    }
    
    /**
     * 将Archive实体转换为ArchiveDTO
     * @param archive Archive实体
     * @return ArchiveDTO
     */
    private ArchiveDTO convertToDTO(Archive archive) {
        if (archive == null) {
            return null;
        }
        
        // 将keywords字符串转换为List<String>
        List<String> keywordsList = new ArrayList<>();
        if (archive.getKeywords() != null && !archive.getKeywords().isEmpty()) {
            // 假设keywords以JSON格式存储，这里简化为逗号分隔的字符串
            String[] keywords = archive.getKeywords().split(",");
            for (String keyword : keywords) {
                keywordsList.add(keyword.trim());
            }
        }
        
        return ArchiveDTO.builder()
                .id(archive.getId())
                .fileNumber(archive.getFileNumber())
                .title(archive.getTitle())
                .description(archive.getDescription())
                .type(archive.getType())
                .category(archive.getCategory())
                .location(archive.getLocation())
                .responsible(archive.getResponsible())
                .status(archive.getStatus())
                .remarks(archive.getRemarks())
                .keywords(keywordsList)
                .fileUrl(archive.getFileUrl())
                .filePath(archive.getFilePath())
                .fileType(archive.getFileType())
                .fileSize(archive.getFileSize())
                .downloadCount(archive.getDownloadCount())
                .shared(archive.getShared())
                .shareExpireTime(archive.getShareExpireTime())
                .createTime(archive.getCreateTime())
                .updateTime(archive.getUpdateTime())
                .creator(archive.getCreator())
                .updater(archive.getUpdater())
                .deleted(archive.getDeleted())
                .build();
    }

    /**
     * Converts DTO to entity
     */
    private Archive convertToEntity(ArchiveDTO dto) {
        if (dto == null) return null;
        
        Archive entity = new Archive();
        
        entity.setId(dto.getId());
        entity.setFileNumber(dto.getFileNumber());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setType(dto.getType());
        entity.setStatus(dto.getStatus());
        entity.setLocation(dto.getLocation());
        
        // 处理关键词列表，转换为逗号分隔的字符串
        if (dto.getKeywords() != null && !dto.getKeywords().isEmpty()) {
            entity.setKeywords(String.join(",", dto.getKeywords()));
        } else {
            entity.setKeywords("");
        }
        
        entity.setArchiveDate(dto.getArchiveDate());
        entity.setExpirationDate(dto.getExpirationDate());
        entity.setCreator(dto.getCreator());
        
        // Don't overwrite these in conversion
        // entity.setCreateTime(dto.getCreateTime());
        // entity.setUpdateTime(dto.getUpdateTime());
        // entity.setFileUrl(dto.getFileUrl());
        
        return entity;
    }

    /**
     * Converts BorrowRecord entity to DTO
     */
    private BorrowRecordDTO convertToBorrowRecordDTO(BorrowRecord entity) {
        if (entity == null) return null;
        
        BorrowRecordDTO dto = new BorrowRecordDTO();
        dto.setId(entity.getId());
        dto.setArchiveId(entity.getArchive().getId());
        dto.setArchiveTitle(entity.getArchive().getTitle());
        dto.setArchiveFileNumber(entity.getArchive().getFileNumber());
        dto.setBorrower(entity.getBorrower());
        dto.setBorrowerDepartment(entity.getBorrowerDepartment());
        dto.setBorrowDate(entity.getBorrowDate());
        dto.setExpectedReturnDate(entity.getExpectedReturnDate());
        dto.setActualReturnDate(entity.getActualReturnDate());
        dto.setPurpose(entity.getPurpose());
        dto.setApprovedBy(entity.getApprovedBy());
        dto.setStatus(entity.getStatusString());
        
        return dto;
    }
} 