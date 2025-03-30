package com.ray.archive.service.impl;

import com.ray.archive.entity.Archive;
import com.ray.archive.entity.BorrowRecord;
import com.ray.archive.entity.User;
import com.ray.archive.repository.ArchiveRepository;
import com.ray.archive.repository.BorrowRecordRepository;
import com.ray.archive.repository.UserRepository;
import com.ray.archive.service.BorrowRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BorrowRecordServiceImpl implements BorrowRecordService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final ArchiveRepository archiveRepository;
    private final UserRepository userRepository;

    @Autowired
    public BorrowRecordServiceImpl(BorrowRecordRepository borrowRecordRepository,
                                 ArchiveRepository archiveRepository,
                                 UserRepository userRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.archiveRepository = archiveRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BorrowRecord save(BorrowRecord borrowRecord) {
        return borrowRecordRepository.save(borrowRecord);
    }

    @Override
    public Optional<BorrowRecord> findById(Long id) {
        return borrowRecordRepository.findById(id);
    }

    @Override
    public Page<BorrowRecord> findAll(Pageable pageable) {
        return borrowRecordRepository.findAll(pageable);
    }

    @Override
    public List<BorrowRecord> findByUser(User user) {
        return borrowRecordRepository.findByUser(user);
    }

    @Override
    public List<BorrowRecord> findByStatus(String status) {
        return borrowRecordRepository.findByStatus(status);
    }

    @Override
    public List<BorrowRecord> findOverdueRecords() {
        return borrowRecordRepository.findOverdueRecords(LocalDateTime.now());
    }

    @Override
    public List<BorrowRecord> findByUserAndStatus(User user, String status) {
        return borrowRecordRepository.findByUserAndStatus(user, status);
    }

    @Override
    public void deleteById(Long id) {
        borrowRecordRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BorrowRecord borrowArchive(Long archiveId, Long userId, String purpose) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new RuntimeException("Archive not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"AVAILABLE".equals(archive.getStatus())) {
            throw new RuntimeException("Archive is not available for borrowing");
        }

        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setArchive(archive);
        borrowRecord.setUser(user);
        borrowRecord.setBorrowTime(LocalDateTime.now());
        borrowRecord.setPlannedReturnTime(LocalDateTime.now().plusDays(14)); // 默认借阅期限14天
        borrowRecord.setStatus("BORROWED");
        borrowRecord.setPurpose(purpose);

        archive.setStatus("BORROWED");
        archiveRepository.save(archive);

        return borrowRecordRepository.save(borrowRecord);
    }

    @Override
    @Transactional
    public BorrowRecord returnArchive(Long borrowRecordId) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));

        if (!"BORROWED".equals(borrowRecord.getStatus()) && !"OVERDUE".equals(borrowRecord.getStatus())) {
            throw new RuntimeException("Invalid borrow record status");
        }

        borrowRecord.setReturnTime(LocalDateTime.now());
        borrowRecord.setStatus("RETURNED");

        Archive archive = borrowRecord.getArchive();
        archive.setStatus("AVAILABLE");
        archiveRepository.save(archive);

        return borrowRecordRepository.save(borrowRecord);
    }

    @Override
    @Transactional
    public void checkAndUpdateOverdueStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(now);
        
        for (BorrowRecord record : overdueRecords) {
            if ("BORROWED".equals(record.getStatus())) {
                record.setStatus("OVERDUE");
                borrowRecordRepository.save(record);
            }
        }
    }
} 