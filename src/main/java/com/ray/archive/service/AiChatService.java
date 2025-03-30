package com.ray.archive.service;

import com.ray.archive.entity.Archive;
import java.util.List;
import java.util.Map;

/**
 * AI聊天服务接口
 */
public interface AiChatService {
    /**
     * 处理用户查询并返回相关响应
     * @param query 用户查询内容
     * @param userId 用户ID
     * @return 包含回答、相关档案、推荐等信息的Map
     */
    Map<String, Object> processQuery(String query, Long userId);

    /**
     * 搜索相关档案
     * @param query 查询内容
     * @return 相关档案列表
     */
    List<Archive> searchRelevantArchives(String query);

    /**
     * 分析用户问题并生成回答
     * @param question 用户问题
     * @param relevantArchives 相关档案列表
     * @return 生成的回答
     */
    String generateAnswer(String question, List<Archive> relevantArchives);

    /**
     * 获取用户最近的对话历史
     * @param userId 用户ID
     * @param limit 返回记录数量限制
     * @return 对话历史列表
     */
    List<Map<String, Object>> getChatHistory(Long userId, int limit);

    /**
     * 保存对话记录
     * @param userId 用户ID
     * @param query 用户查询
     * @param response AI回答
     */
    void saveChatHistory(Long userId, String query, String response);

    /**
     * 处理用户反馈
     * @param chatId 聊天记录ID
     * @param isHelpful 是否有帮助
     * @param relevanceScore 相关度评分
     * @param userAction 用户后续操作
     */
    void processFeedback(Long chatId, boolean isHelpful, Integer relevanceScore, String userAction);

    /**
     * 获取智能推荐
     * @param userId 用户ID
     * @param query 当前查询
     * @return 推荐列表
     */
    List<String> getRecommendations(Long userId, String query);

    /**
     * 更新学习模型
     * @param userId 用户ID
     * @param query 查询内容
     * @param isSuccessful 是否成功
     */
    void updateLearningModel(Long userId, String query, boolean isSuccessful);
} 