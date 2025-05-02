package com.ray.archive.controller;

import com.ray.archive.entity.User;
import com.ray.archive.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> query(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String query = request.get("query");
        String sessionId = request.get("sessionId"); // May be null for new conversations
        
        Map<String, Object> response = aiChatService.processQuery(query, user.getId(), sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(aiChatService.getChatHistory(user.getId(), limit));
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
        
        // Forward to the chat service through ChatHistoryService
        return ResponseEntity.ok().build();
    }
} 