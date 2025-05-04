package com.ray.archive.service.impl;

import com.ray.archive.dto.ArchiveDTO;
import com.ray.archive.dto.BorrowRecordDTO;
import com.ray.archive.dto.PageResult;
import com.ray.archive.dto.StatisticsDTO;
import com.ray.archive.entity.Archive;
import com.ray.archive.repository.ArchiveRepository;
import com.ray.archive.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

@Service
@Transactional
public class ArchiveServiceImpl implements ArchiveService {

    private final ArchiveRepository archiveRepository;
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Autowired
    public ArchiveServiceImpl(ArchiveRepository archiveRepository) {
        this.archiveRepository = archiveRepository;
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
    public PageResult<ArchiveDTO> getArchives(int page, int pageSize, String code, String name, String type, String status, String sortBy, String sortDirection) {
        return null;
    }

    @Override
    public ArchiveDTO getArchiveById(Long id) {
        return null;
    }

    @Override
    public ArchiveDTO createArchive(ArchiveDTO archiveDTO, MultipartFile file) {
        return null;
    }

    @Override
    public ArchiveDTO updateArchive(ArchiveDTO archiveDTO, MultipartFile file) {
        return null;
    }

    @Override
    public boolean deleteArchive(Long id) {
        return false;
    }

    @Override
    public int batchDeleteArchives(List<Long> ids) {
        return 0;
    }

    @Override
    public BorrowRecordDTO borrowArchive(BorrowRecordDTO borrowRecord) {
        return null;
    }

    @Override
    public BorrowRecordDTO returnArchive(Long id) {
        return null;
    }

    @Override
    public PageResult<BorrowRecordDTO> getBorrowRecords(int page, int pageSize, Long archiveId, String borrower, Integer status) {
        return null;
    }

    /**
     * 获取档案统计数据
     * @return 统计数据DTO
     */
    @Override
    public StatisticsDTO getArchiveStatistics() {
        long totalCount = count();
        long availableCount = countByStatus("AVAILABLE");
        long borrowedCount = countByStatus("BORROWED");
        
        // 计算本月新增档案数
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newThisMonth = archiveRepository.countByCreateTimeAfter(startOfMonth);
        
        // 构建类型分布统计
        Map<String, Long> typeDistribution = new HashMap<>();
        List<Object[]> typeStats = archiveRepository.countByTypes();
        if (typeStats != null) {
            for (Object[] stat : typeStats) {
                if (stat[0] != null) {
                    typeDistribution.put(stat[0].toString(), (Long) stat[1]);
                }
            }
        }
        
        // 构建状态分布统计
        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("AVAILABLE", availableCount);
        statusDistribution.put("BORROWED", borrowedCount);
        statusDistribution.put("PROCESSING", countByStatus("PROCESSING"));
        
        // 构建月度分布统计
        Map<String, Long> monthlyDistribution = new HashMap<>();
        List<Object[]> monthlyStats = archiveRepository.countByMonths();
        if (monthlyStats != null) {
            for (Object[] stat : monthlyStats) {
                if (stat[0] != null) {
                    monthlyDistribution.put(stat[0].toString(), (Long) stat[1]);
                }
            }
        }
        
        // 借阅相关统计数据暂时模拟
        // 实际应用中，这些数据应该从借阅记录表中获取
        Long borrowCount = 250L;
        Long borrowerCount = 120L;
        Map<Long, Long> highFrequencyArchives = Map.of(1L, 15L, 2L, 12L, 3L, 10L);
        Double averageBorrowDuration = 7.5;
        
        // 构建并返回统计DTO
        return StatisticsDTO.builder()
                .total(totalCount)
                .available(availableCount)
                .borrowed(borrowedCount)
                .newThisMonth(newThisMonth)
                .typeDistribution(typeDistribution)
                .statusDistribution(statusDistribution)
                .monthlyDistribution(monthlyDistribution)
                .borrowCount(borrowCount)
                .borrowerCount(borrowerCount)
                .highFrequencyArchives(highFrequencyArchives)
                .averageBorrowDuration(averageBorrowDuration)
                .build();
    }
} 