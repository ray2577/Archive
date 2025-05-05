package com.ray.archive.repository;

import com.ray.archive.entity.BorrowRecord;
import com.ray.archive.entity.BorrowStatus;
import com.ray.archive.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 借阅记录数据访问层
 */
@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {
    
    /**
     * 根据用户查找借阅记录
     */
    List<BorrowRecord> findByUser(User user);
    
    /**
     * 根据状态查询借阅记录（字符串状态，用于兼容旧代码）
     */
    @Query("SELECT b FROM BorrowRecord b WHERE CAST(b.status AS string) = :status")
    List<BorrowRecord> findByStatus(@Param("status") String status);
    
    /**
     * 根据状态查询借阅记录（枚举状态）
     */
    List<BorrowRecord> findByStatus(BorrowStatus status);
    
    /**
     * 查找用户的指定状态借阅记录
     */
    @Query("SELECT b FROM BorrowRecord b WHERE b.user = :user AND CAST(b.status AS string) = :status")
    List<BorrowRecord> findByUserAndStatus(@Param("user") User user, @Param("status") String status);
    
    /**
     * 查找已过期未归还的记录
     */
    @Query("SELECT b FROM BorrowRecord b WHERE b.status = com.ray.archive.entity.BorrowStatus.BORROWED AND b.expectedReturnDate < :currentTime")
    List<BorrowRecord> findOverdueRecords(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 根据档案ID查找借阅记录
     */
    List<BorrowRecord> findByArchiveId(Long archiveId);
    
    /**
     * 根据借阅人查找借阅记录
     */
    List<BorrowRecord> findByBorrower(String borrower);
    
    /**
     * 根据档案ID和状态查找借阅记录
     */
    Optional<BorrowRecord> findByArchiveIdAndStatus(Long archiveId, BorrowStatus status);
    
    /**
     * 获取档案的借阅次数
     */
    long countByArchiveId(Long archiveId);
} 