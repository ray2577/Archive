package com.ray.archive.service;

import com.ray.archive.entity.ArchiveAccessRequest;
import com.ray.archive.entity.ArchiveAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArchiveAccessService {
    // 查阅申请相关方法
    ArchiveAccessRequest createRequest(ArchiveAccessRequest request);
    ArchiveAccessRequest approveRequest(Long requestId, Long approverId, boolean approved, String rejectReason);
    Optional<ArchiveAccessRequest> findRequestById(Long id);
    Page<ArchiveAccessRequest> findAllRequests(Pageable pageable);
    List<ArchiveAccessRequest> findRequestsByArchiveId(Long archiveId);
    List<ArchiveAccessRequest> findRequestsByRequesterId(Long requesterId);
    List<ArchiveAccessRequest> findRequestsByStatus(String status);

    // 查阅记录相关方法
    ArchiveAccessLog createAccessLog(ArchiveAccessLog log);
    List<ArchiveAccessLog> findLogsByArchiveId(Long archiveId);
    List<ArchiveAccessLog> findLogsByUserId(Long userId);
    List<ArchiveAccessLog> findLogsByRequestId(Long requestId);
    Page<ArchiveAccessLog> findAllLogs(Pageable pageable);

    // 权限检查方法
    boolean canAccessArchive(Long userId, Long archiveId);
    boolean hasValidRequest(Long userId, Long archiveId);
} 