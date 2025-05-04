package com.ray.archive.repository;

import com.ray.archive.entity.BorrowRecord;
import com.ray.archive.entity.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 借阅记录数据访问层
 */
@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {
    
    /**
     * 根据档案ID查找借阅记录
     */
    List<BorrowRecord> findByArchiveId(Long archiveId);
    
    /**
     * 根据借阅人查找借阅记录
     */
    List<BorrowRecord> findByBorrower(String borrower);
    
    /**
     * 根据状态查找借阅记录
     */
    List<BorrowRecord> findByStatus(BorrowStatus status);
    
    /**
     * 根据档案ID和状态查找借阅记录
     */
    Optional<BorrowRecord> findByArchiveIdAndStatus(Long archiveId, BorrowStatus status);
    
    /**
     * 获取档案的借阅次数
     */
    long countByArchiveId(Long archiveId);
} 