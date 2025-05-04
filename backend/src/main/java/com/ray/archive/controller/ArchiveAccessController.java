package com.ray.archive.controller;

import com.ray.archive.entity.ArchiveAccessLog;
import com.ray.archive.entity.ArchiveAccessRequest;
import com.ray.archive.entity.User;
import com.ray.archive.service.ArchiveAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/archive-access")
public class ArchiveAccessController {

    @Autowired
    private ArchiveAccessService archiveAccessService;

    // 查阅申请相关接口
    @PostMapping("/requests")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ArchiveAccessRequest> createRequest(
            @RequestBody ArchiveAccessRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        request.setRequester(user);
        return ResponseEntity.ok(archiveAccessService.createRequest(request));
    }

    @PutMapping("/requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArchiveAccessRequest> approveRequest(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectReason) {
        return ResponseEntity.ok(archiveAccessService.approveRequest(id, approverId, approved, rejectReason));
    }

    @GetMapping("/requests/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ArchiveAccessRequest> getRequest(@PathVariable Long id) {
        return archiveAccessService.findRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ArchiveAccessRequest>> getAllRequests(Pageable pageable) {
        return ResponseEntity.ok(archiveAccessService.findAllRequests(pageable));
    }

    @GetMapping("/requests/archive/{archiveId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ArchiveAccessRequest>> getRequestsByArchive(@PathVariable Long archiveId) {
        return ResponseEntity.ok(archiveAccessService.findRequestsByArchiveId(archiveId));
    }

    @GetMapping("/requests/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<ArchiveAccessRequest>> getRequestsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(archiveAccessService.findRequestsByRequesterId(userId));
    }

    @GetMapping("/requests/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ArchiveAccessRequest>> getRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(archiveAccessService.findRequestsByStatus(status));
    }

    // 查阅记录相关接口
    @PostMapping("/logs")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ArchiveAccessLog> createLog(
            @RequestBody ArchiveAccessLog log,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.setUser(user);
        return ResponseEntity.ok(archiveAccessService.createAccessLog(log));
    }

    @GetMapping("/logs/archive/{archiveId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ArchiveAccessLog>> getLogsByArchive(@PathVariable Long archiveId) {
        return ResponseEntity.ok(archiveAccessService.findLogsByArchiveId(archiveId));
    }

    @GetMapping("/logs/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<ArchiveAccessLog>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(archiveAccessService.findLogsByUserId(userId));
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ArchiveAccessLog>> getAllLogs(Pageable pageable) {
        return ResponseEntity.ok(archiveAccessService.findAllLogs(pageable));
    }

    // 权限检查接口
    @GetMapping("/check/{archiveId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Boolean> checkAccess(
            @PathVariable Long archiveId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(archiveAccessService.canAccessArchive(user.getId(), archiveId));
    }

    @GetMapping("/valid-request/{archiveId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Boolean> checkValidRequest(
            @PathVariable Long archiveId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(archiveAccessService.hasValidRequest(user.getId(), archiveId));
    }
} 