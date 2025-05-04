package com.ray.archive.controller;

import com.ray.archive.entity.ChatHistory;
import com.ray.archive.entity.ChatSession;
import com.ray.archive.entity.User;
import com.ray.archive.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @GetMapping("/history")
    public ResponseEntity<Page<ChatHistory>> getChatHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(chatHistoryService.findByUser_IdOrderByCreateTimeDesc(user.getId(), pageable));
    }

    @GetMapping("/history/recent")
    public ResponseEntity<List<ChatHistory>> getRecentHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatHistoryService.findRecentByUserId(
            user.getId(), PageRequest.of(0, limit)));
    }

    @GetMapping("/history/type/{queryType}")
    public ResponseEntity<List<ChatHistory>> getHistoryByType(
            Authentication authentication,
            @PathVariable String queryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(chatHistoryService.findByUserIdAndQueryTypeWithPaging(
            user.getId(), queryType, pageable));
    }

    @GetMapping("/history/search")
    public ResponseEntity<List<ChatHistory>> searchHistory(
            Authentication authentication,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(chatHistoryService.searchByKeyword(
            user.getId(), keyword, pageable));
    }

    @GetMapping("/history/date-range")
    public ResponseEntity<List<ChatHistory>> getHistoryByDateRange(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(chatHistoryService.findByCreateTimeBetween(startTime, endTime));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ChatHistory>> getSessionHistory(
            @PathVariable String sessionId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        // Implement security check to ensure user can access this session
        return ResponseEntity.ok(chatHistoryService.findBySessionId(sessionId));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getUserSessions(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatHistoryService.findUserSessions(user.getId()));
    }

    @PostMapping("/session")
    public ResponseEntity<ChatSession> createSession(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        User user = (User) authentication.getPrincipal();
        String title = request.getOrDefault("title", "New Conversation");
        return ResponseEntity.ok(chatHistoryService.createNewSession(user.getId(), title));
    }

    @PutMapping("/session/{sessionId}")
    public ResponseEntity<ChatSession> updateSession(
            @PathVariable String sessionId,
            @RequestBody ChatSession session,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        // Ensure session belongs to user and sessionId matches
        if (!session.getId().equals(sessionId) || !session.getUserId().equals(user.getId())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(chatHistoryService.updateSession(session));
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        // Security check to ensure user owns this session
        chatHistoryService.deleteSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/feedback/{chatId}")
    public ResponseEntity<Void> provideFeedback(
            @PathVariable Long chatId,
            @RequestBody Map<String, Object> feedback,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        boolean isHelpful = (boolean) feedback.getOrDefault("isHelpful", false);
        Integer relevanceScore = feedback.containsKey("relevanceScore") ? 
                (Integer) feedback.get("relevanceScore") : null;
        String feedbackText = (String) feedback.getOrDefault("feedbackText", "");
        
        chatHistoryService.saveUserFeedback(chatId, user.getId(), isHelpful, relevanceScore, feedbackText);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getChatStatistics(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatHistoryService.getChatStatistics(user.getId()));
    }

    @GetMapping("/history/{chatId}")
    public ResponseEntity<ChatHistory> getChatHistoryById(
            @PathVariable Long chatId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<ChatHistory> chatHistory = chatHistoryService.findById(chatId);
        
        if (chatHistory.isPresent() && chatHistory.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.ok(chatHistory.get());
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/hot-queries")
    public ResponseEntity<List<String>> getHotQueries(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(chatHistoryService.findHotQueries(PageRequest.of(0, limit)));
    }

    @GetMapping("/statistics/processing-time")
    public ResponseEntity<Double> getAverageProcessingTime(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatHistoryService.getAverageProcessingTime(user.getId()));
    }

    @GetMapping("/statistics/queries-by-date")
    public ResponseEntity<List<Object[]>> getQueriesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(chatHistoryService.countQueriesByDate(startTime, endTime));
    }

    @GetMapping("/statistics/helpful-responses")
    public ResponseEntity<Long> getHelpfulResponsesCount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatHistoryService.countHelpfulResponses(user.getId()));
    }
} 