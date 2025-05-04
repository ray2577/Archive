package com.ray.archive.service;

import com.ray.archive.entity.ChatHistory;
import com.ray.archive.entity.ChatSession;
import com.ray.archive.repository.ChatHistoryRepository;
import com.ray.archive.repository.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatHistoryService {

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;

    public ChatHistory saveChatHistory(ChatHistory chatHistory) {
        if (chatHistory.getCreateTime() == null) {
            chatHistory.setCreateTime(LocalDateTime.now());
        }
        chatHistory.setUpdateTime(LocalDateTime.now());
        return chatHistoryRepository.save(chatHistory);
    }

    public Page<ChatHistory> findByUser_IdOrderByCreateTimeDesc(Long userId, Pageable pageable) {
        return chatHistoryRepository.findByUser_IdOrderByCreateTimeDesc(userId, pageable);
    }
    
    public List<ChatHistory> findByUser_Id(Long userId) {
        return chatHistoryRepository.findByUser_Id(userId);
    }
    
    public List<ChatHistory> findRecentByUserId(Long userId, Pageable pageable) {
        return chatHistoryRepository.findRecentByUserId(userId, pageable);
    }
    
    public List<ChatHistory> findByUserIdAndQueryType(Long userId, String queryType) {
        return chatHistoryRepository.findByUser_IdAndQueryType(userId, queryType);
    }
    
    public List<ChatHistory> findByUserIdAndQueryTypeWithPaging(Long userId, String queryType, Pageable pageable) {
        return chatHistoryRepository.findByUser_IdAndQueryTypeOrderByCreateTimeDesc(userId, queryType, pageable);
    }
    
    public List<ChatHistory> searchByKeyword(Long userId, String keyword, Pageable pageable) {
        return chatHistoryRepository.searchByKeyword(userId, keyword, pageable);
    }

    public List<ChatHistory> findBySessionId(String sessionId) {
        return chatHistoryRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
    }

    public Optional<ChatHistory> findById(Long id) {
        return chatHistoryRepository.findById(id);
    }

    @Transactional
    public ChatSession createNewSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTitle(title);
        session.setStatus("ACTIVE");
        session.setLastQueryTime(LocalDateTime.now());
        session.setQueryCount(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        return chatSessionRepository.save(session);
    }

    @Transactional
    public ChatSession updateSession(ChatSession session) {
        session.setUpdateTime(LocalDateTime.now());
        return chatSessionRepository.save(session);
    }

    public List<ChatSession> findUserSessions(Long userId) {
        return chatSessionRepository.findByUserIdOrderByLastQueryTimeDesc(userId);
    }

    @Transactional
    public void saveUserFeedback(Long chatHistoryId, Long userId, boolean isHelpful, Integer relevanceScore, String feedbackText) {
        // Update the is_helpful field in chat_histories
        Optional<ChatHistory> optionalChatHistory = chatHistoryRepository.findById(chatHistoryId);
        if (optionalChatHistory.isPresent()) {
            ChatHistory chatHistory = optionalChatHistory.get();
            chatHistory.setIsHelpful(isHelpful);
            if (relevanceScore != null) {
                chatHistory.setRelevanceScore(relevanceScore);
            }
            chatHistoryRepository.save(chatHistory);
        }
    }

    @Transactional
    public void deleteSession(String sessionId) {
        // Delete all messages in the session
        chatHistoryRepository.deleteBySessionId(sessionId);
        // Delete the session
        chatSessionRepository.deleteById(sessionId);
    }

    public Map<String, Object> getChatStatistics(Long userId) {
        // Implement statistics gathering logic here
        return Map.of(
            "totalQueries", chatHistoryRepository.countByUser_Id(userId),
            "helpfulQueries", chatHistoryRepository.countByUser_IdAndIsHelpful(userId, true),
            "averageRelevance", chatHistoryRepository.averageRelevanceScoreByUserId(userId)
        );
    }
    
    public Double getAverageProcessingTime(Long userId) {
        return chatHistoryRepository.getAverageProcessingTime(userId);
    }
    
    public long countHelpfulResponses(Long userId) {
        return chatHistoryRepository.countHelpfulResponses(userId);
    }
    
    public List<String> findHotQueries(Pageable pageable) {
        return chatHistoryRepository.findHotQueries(pageable);
    }
    
    public List<Object[]> countQueriesByDate(LocalDateTime startTime, LocalDateTime endTime) {
        return chatHistoryRepository.countQueriesByDate(startTime, endTime);
    }
    
    public List<ChatHistory> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return chatHistoryRepository.findByCreateTimeBetween(startTime, endTime);
    }

    // Add a method to get active sessions count
    public int countActiveSessionsByUserId(Long userId) {
        return chatSessionRepository.countByUserIdAndStatus(userId, "ACTIVE");
    }

    // Add a method to find inactive sessions
    public List<ChatSession> findInactiveSessions(LocalDateTime cutoffTime) {
        return chatSessionRepository.findInactiveSessions(cutoffTime);
    }
} 