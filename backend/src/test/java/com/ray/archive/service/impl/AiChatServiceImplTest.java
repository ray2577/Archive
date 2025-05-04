package com.ray.archive.service.impl;

import com.ray.archive.entity.Archive;
import com.ray.archive.entity.ChatHistory;
import com.ray.archive.entity.User;
import com.ray.archive.repository.ArchiveRepository;
import com.ray.archive.repository.ChatHistoryRepository;
import com.ray.archive.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiChatServiceImplTest {

    @InjectMocks
    private AiChatServiceImpl aiChatService;

    @Mock
    private ArchiveRepository archiveRepository;

    @Mock
    private ChatHistoryRepository chatHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processQuery_ValidInput_ReturnsResult() {
        // Arrange
        String query = "查找2023年的财务档案";
        Long userId = 1L;
        String sessionId = "1";
        Archive archive = new Archive();
        archive.setTitle("2023年财务报告");
        archive.setFileNumber("FIN-2023-001");
        archive.setCreateTime(LocalDateTime.now());
        
        when(archiveRepository.findByTitleContainingOrDescriptionContaining(any(), any()))
            .thenReturn(Arrays.asList(archive));
        
        // Act
        Map<String, Object> result = aiChatService.processQuery(query, userId,sessionId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("answer"));
        assertTrue(result.containsKey("relevantArchives"));
        verify(archiveRepository, times(1))
            .findByTitleContainingOrDescriptionContaining(any(), any());
    }

    @Test
    void processQuery_EmptyQuery_ThrowsException() {
        // Arrange
        String query = "";
        Long userId = 1L;
        String sessionId = "1";
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            aiChatService.processQuery(query, userId,sessionId);
        });
    }

    @Test
    void processFeedback_ValidInput_SavesFeedback() {
        // Arrange
        Long chatId = 1L;
        boolean isHelpful = true;
        Integer relevanceScore = 5;
        String userAction = "FEEDBACK";
        
        ChatHistory chatHistory = new ChatHistory();
        when(chatHistoryRepository.findById(chatId)).thenReturn(Optional.of(chatHistory));
        
        // Act
        aiChatService.processFeedback(chatId, isHelpful, relevanceScore, userAction);
        
        // Assert
        verify(chatHistoryRepository, times(1)).save(any(ChatHistory.class));
    }

    @Test
    void getChatHistory_ValidInput_ReturnsHistory() {
        // Arrange
        Long userId = 1L;
        int limit = 10;
        ChatHistory history = new ChatHistory();
        history.setQuery("测试查询");
        history.setResponse("测试回答");
        
        when(chatHistoryRepository.findByUser_IdAndQueryTypeOrderByCreateTimeDesc(eq(userId),"1", any(PageRequest.class)))
            .thenReturn(Arrays.asList(history));
        
        // Act
        List<Map<String, Object>> result = aiChatService.getChatHistory(userId, limit);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("测试查询", result.get(0).get("query"));
        assertEquals("测试回答", result.get(0).get("response"));
    }

    @Test
    void saveChatHistory_ValidInput_SavesHistory() {
        // Arrange
        Long userId = 1L;
        String query = "测试查询";
        String response = "测试回答";
        
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // Act
        aiChatService.saveChatHistory(userId, query, response);
        
        // Assert
        verify(chatHistoryRepository, times(1)).save(any(ChatHistory.class));
    }
} 