package com.ray.archive.controller;

import com.ray.archive.entity.BorrowRecord;
import com.ray.archive.entity.User;
import com.ray.archive.service.BorrowRecordService;
import com.ray.archive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow-records")
public class BorrowRecordController {

    private final BorrowRecordService borrowRecordService;
    private final UserService userService;

    @Autowired
    public BorrowRecordController(BorrowRecordService borrowRecordService, UserService userService) {
        this.borrowRecordService = borrowRecordService;
        this.userService = userService;
    }

    @PostMapping("/borrow")
    public ResponseEntity<BorrowRecord> borrowArchive(
            @RequestParam Long archiveId,
            @RequestParam String purpose,
            Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(borrowRecordService.borrowArchive(archiveId, user.getId(), purpose));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowRecord> returnArchive(@PathVariable Long id) {
        return ResponseEntity.ok(borrowRecordService.returnArchive(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowRecord> getBorrowRecord(@PathVariable Long id) {
        return borrowRecordService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BorrowRecord>> getAllBorrowRecords(Pageable pageable) {
        return ResponseEntity.ok(borrowRecordService.findAll(pageable));
    }

    @GetMapping("/my-records")
    public ResponseEntity<List<BorrowRecord>> getMyBorrowRecords(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(borrowRecordService.findByUser(user));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BorrowRecord>> getBorrowRecordsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(borrowRecordService.findByStatus(status));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BorrowRecord>> getOverdueRecords() {
        return ResponseEntity.ok(borrowRecordService.findOverdueRecords());
    }

    @PostMapping("/check-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> checkAndUpdateOverdueStatus() {
        borrowRecordService.checkAndUpdateOverdueStatus();
        return ResponseEntity.ok().build();
    }
} 