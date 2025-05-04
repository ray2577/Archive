package com.ray.archive.service.impl;

import com.ray.archive.entity.ArchiveAccessRequest;
import com.ray.archive.entity.ArchiveAccessLog;
import com.ray.archive.entity.User;
import com.ray.archive.repository.ArchiveAccessRequestRepository;
import com.ray.archive.repository.ArchiveAccessLogRepository;
import com.ray.archive.repository.UserRepository;
import com.ray.archive.service.ArchiveAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArchiveAccessServiceImpl implements ArchiveAccessService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArchiveAccessServiceImpl.class);

    @Autowired
    private ArchiveAccessRequestRepository requestRepository;

    @Autowired
    private ArchiveAccessLogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public ArchiveAccessRequest createRequest(ArchiveAccessRequest request) {
        Assert.notNull(request, "Request must not be null");
        Assert.notNull(request.getArchive(), "Archive must not be null");
        Assert.notNull(request.getRequester(), "Requester must not be null");
        Assert.hasText(request.getPurpose(), "Purpose must not be empty");

        logger.debug("Creating access request for archive {} by user {}", 
                    request.getArchive().getId(), request.getRequester().getId());

        request.setStatus("PENDING");
        request.setCreateTime(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public ArchiveAccessRequest approveRequest(Long requestId, Long approverId, boolean approved, String rejectReason) {
        Assert.notNull(requestId, "Request ID must not be null");
        Assert.notNull(approverId, "Approver ID must not be null");

        logger.debug("Processing approval for request {}, approved: {}", requestId, approved);

        ArchiveAccessRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));
        
        User approver = userRepository.findById(approverId)
            .orElseThrow(() -> new IllegalArgumentException("Approver not found: " + approverId));

        request.setApprover(approver);
        request.setApprovalTime(LocalDateTime.now());
        request.setStatus(approved ? "APPROVED" : "REJECTED");
        
        if (!approved) {
            Assert.hasText(rejectReason, "Reject reason must not be empty when rejecting request");
            request.setRejectReason(rejectReason);
        } else {
            request.setAccessStartTime(LocalDateTime.now());
            request.setAccessEndTime(LocalDateTime.now().plusDays(7)); // 默认7天有效期
        }

        logger.info("Request {} has been {}", requestId, approved ? "approved" : "rejected");
        return requestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArchiveAccessRequest> findRequestById(Long id) {
        Assert.notNull(id, "ID must not be null");
        return requestRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArchiveAccessRequest> findAllRequests(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");
        return requestRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveAccessRequest> findRequestsByArchiveId(Long archiveId) {
        Assert.notNull(archiveId, "Archive ID must not be null");
        return requestRepository.findByArchive_Id(archiveId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveAccessRequest> findRequestsByRequesterId(Long requesterId) {
        Assert.notNull(requesterId, "Requester ID must not be null");
        return requestRepository.findByRequester_Id(requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveAccessRequest> findRequestsByStatus(String status) {
        Assert.hasText(status, "Status must not be empty");
        return requestRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public ArchiveAccessLog createAccessLog(ArchiveAccessLog log) {
        Assert.notNull(log, "Log must not be null");
        Assert.notNull(log.getArchive(), "Archive must not be null");
        Assert.notNull(log.getUser(), "User must not be null");
        Assert.notNull(log.getRequest(), "Request must not be null");
        Assert.hasText(log.getAccessType(), "Access type must not be empty");

        logger.debug("Creating access log for archive {} by user {}", 
                    log.getArchive().getId(), log.getUser().getId());

        log.setAccessTime(LocalDateTime.now());
        return logRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveAccessLog> findLogsByArchiveId(Long archiveId) {
        Assert.notNull(archiveId, "Archive ID must not be null");
        return logRepository.findByArchive_Id(archiveId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveAccessLog> findLogsByUserId(Long userId) {
        Assert.notNull(userId, "User ID must not be null");
        return logRepository.findByUser_Id(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveAccessLog> findLogsByRequestId(Long requestId) {
        Assert.notNull(requestId, "Request ID must not be null");
        return logRepository.findByRequest_Id(requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArchiveAccessLog> findAllLogs(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");
        return logRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessArchive(Long userId, Long archiveId) {
        Assert.notNull(userId, "User ID must not be null");
        Assert.notNull(archiveId, "Archive ID must not be null");

        return requestRepository.hasActiveAccess(userId, archiveId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasValidRequest(Long userId, Long archiveId) {
        Assert.notNull(userId, "User ID must not be null");
        Assert.notNull(archiveId, "Archive ID must not be null");

        return requestRepository.existsByRequesterIdAndArchiveIdAndStatusIn(
            userId, archiveId, List.of("PENDING", "APPROVED"));
    }
} 