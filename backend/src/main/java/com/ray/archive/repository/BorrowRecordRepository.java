package com.ray.archive.repository;

import com.ray.archive.entity.BorrowRecord;
import com.ray.archive.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUser(User user);
    List<BorrowRecord> findByStatus(String status);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'BORROWED' AND br.plannedReturnTime < ?1")
    List<BorrowRecord> findOverdueRecords(LocalDateTime currentTime);
    
    List<BorrowRecord> findByUserAndStatus(User user, String status);
} 