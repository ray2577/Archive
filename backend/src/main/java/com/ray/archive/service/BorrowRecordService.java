package com.ray.archive.service;

import com.ray.archive.entity.BorrowRecord;
import com.ray.archive.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BorrowRecordService {
    BorrowRecord save(BorrowRecord borrowRecord);
    Optional<BorrowRecord> findById(Long id);
    Page<BorrowRecord> findAll(Pageable pageable);
    List<BorrowRecord> findByUser(User user);
    List<BorrowRecord> findByStatus(String status);
    List<BorrowRecord> findOverdueRecords();
    List<BorrowRecord> findByUserAndStatus(User user, String status);
    void deleteById(Long id);
    BorrowRecord borrowArchive(Long archiveId, Long userId, String purpose);
    BorrowRecord returnArchive(Long borrowRecordId);
    void checkAndUpdateOverdueStatus();
} 