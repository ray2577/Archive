package com.ray.archive.service.impl;

import com.ray.archive.entity.Archive;
import com.ray.archive.entity.ChatHistory;
import com.ray.archive.entity.User;
import com.ray.archive.repository.ArchiveRepository;
import com.ray.archive.repository.ChatHistoryRepository;
import com.ray.archive.repository.UserRepository;
import com.ray.archive.service.AiChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class AiChatServiceImpl implements AiChatService {
    private static final Logger logger = LoggerFactory.getLogger(AiChatServiceImpl.class);

    @Autowired
    private ArchiveRepository archiveRepository;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, AtomicInteger> queryFrequencyMap = new ConcurrentHashMap<>();
    private final Map<Long, String> userContext = new ConcurrentHashMap<>();
    private final Map<String, Double> keywordWeights = new ConcurrentHashMap<>();
    private final Map<Long, List<QueryPattern>> userQueryPatterns = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> successfulPatterns = new ConcurrentHashMap<>();
    private final Map<String, List<FailureReason>> failureReasons = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> processQuery(String query, Long userId) {
        Assert.hasText(query, "Query cannot be empty");
        Assert.notNull(userId, "User ID cannot be null");
        
        logger.info("Processing query for user {}: {}", userId, query);
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 分析查询意图
            QueryIntent intent = analyzeQueryIntent(query);
            
            // 2. 搜索相关档案
            List<Archive> relevantArchives = searchRelevantArchives(query);
            
            // 3. 生成回答
            String answer = generateAnswer(query, relevantArchives);
            
            // 4. 获取推荐
            List<String> recommendations = getRecommendations(userId, query);
            
            // 5. 保存对话历史
            saveChatHistory(userId, query, answer);
            
            // 6. 更新学习模型
            updateLearningModel(userId, query, !relevantArchives.isEmpty());
            
            // 7. 构建返回结果
            result.put("answer", answer);
            result.put("relevantArchives", relevantArchives);
            result.put("recommendations", recommendations);
            result.put("queryType", intent.getType());
            result.put("processingTime", System.currentTimeMillis() - startTime);
            
            logger.info("Query processed successfully in {}ms", System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            logger.error("Error processing query: {}", e.getMessage(), e);
            result.put("error", "处理查询时发生错误");
            return result;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getChatHistory(Long userId, int limit) {
        Assert.notNull(userId, "User ID cannot be null");
        Assert.isTrue(limit > 0, "Limit must be greater than 0");

        logger.debug("Fetching chat history for user {} with limit {}", userId, limit);
        
        try {
            List<ChatHistory> histories = chatHistoryRepository.findRecentByUserId(
                userId, 
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createTime"))
            );
            
            return histories.stream().map(history -> {
                Map<String, Object> historyMap = new HashMap<>();
                historyMap.put("id", history.getId());
                historyMap.put("query", history.getQuery());
                historyMap.put("response", history.getResponse());
                historyMap.put("createTime", history.getCreateTime());
                historyMap.put("queryType", history.getQueryType());
                historyMap.put("isHelpful", history.getIsHelpful());
                return historyMap;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching chat history for user {}: {}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public void saveChatHistory(Long userId, String query, String response) {
        Assert.notNull(userId, "User ID cannot be null");
        Assert.hasText(query, "Query cannot be empty");
        Assert.hasText(response, "Response cannot be empty");

        logger.debug("Saving chat history for user {}", userId);
        
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            ChatHistory history = new ChatHistory();
            history.setUser(user);
            history.setQuery(query);
            history.setResponse(response);
            history.setCreateTime(LocalDateTime.now());
            
            chatHistoryRepository.save(history);
            logger.info("Chat history saved successfully for user {}", userId);
        } catch (Exception e) {
            logger.error("Error saving chat history for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to save chat history", e);
        }
    }

    private ChatHistory saveChatHistory(Long userId, String query, String answer, 
            QueryIntent intent, List<Archive> relevantArchives, long processingTime) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ChatHistory history = new ChatHistory();
        history.setUser(user);
        history.setQuery(query);
        history.setResponse(answer);
        history.setQueryType(intent.getType());
        history.setProcessingTime((int) processingTime);
        history.setCreateTime(LocalDateTime.now());
        
        // 设置相关档案ID
        if (relevantArchives != null && !relevantArchives.isEmpty()) {
            history.setRelevantArchiveIds(
                relevantArchives.stream()
                    .map(Archive::getId)
                    .collect(Collectors.toList())
            );
            history.setArchiveCount(relevantArchives.size());
        }
        
        // 设置查询意图相关信息
        history.setHasTimeRange(!intent.getTimeRanges().isEmpty());
        history.setHasCategoryFilter(!intent.getCategories().isEmpty());
        history.setKeywords(String.join(",", intent.getKeywords()));
        
        return chatHistoryRepository.save(history);
    }

    @Override
    public void processFeedback(Long chatId, boolean isHelpful, Integer relevanceScore, String userAction) {
        Assert.notNull(chatId, "Chat ID cannot be null");
        
        logger.debug("Processing feedback for chat {}", chatId);
        
        try {
            ChatHistory chatHistory = chatHistoryRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat history not found"));
            
            chatHistory.setIsHelpful(isHelpful);
            chatHistory.setRelevanceScore(relevanceScore);
            chatHistory.setUserAction(userAction);
            
            chatHistoryRepository.save(chatHistory);
            
            // 根据反馈更新学习模型
            if (isHelpful) {
                reinforceSuccessfulPattern(String.valueOf(chatHistory));
            } else {
                adjustFailedPattern(String.valueOf(chatHistory));
            }
            
            logger.info("Feedback processed successfully for chat {}", chatId);
        } catch (Exception e) {
            logger.error("Error processing feedback for chat {}: {}", chatId, e.getMessage(), e);
            throw new RuntimeException("Failed to process feedback", e);
        }
    }

    private QueryIntent analyzeQueryIntent(String query) {
        QueryIntent intent = new QueryIntent();
        
        // 1. 分析查询类型
        intent.setType(determineQueryType(query));
        
        // 2. 提取时间范围
        List<TimeRange> timeRanges = extractTimeRanges(query);
        intent.setTimeRanges(timeRanges);
        
        // 3. 提取关键词
        List<String> keywords = extractKeywords(query);
        intent.setKeywords(keywords);
        
        // 4. 提取类别
        List<String> categories = extractCategories(query);
        intent.setCategories(categories);
        
        // 5. 提取位置
        List<String> locations = extractLocations(query);
        intent.setLocations(locations);
        
        // 6. 分析复杂度
        int complexity = analyzeComplexity(query);
        intent.setComplexity(complexity);
        
        return intent;
    }

    private String determineQueryType(String query) {
        // 1. 检查是否是统计类查询
        if (query.contains("统计") || query.contains("多少") || query.contains("数量")) {
            return "STATISTICS";
        }
        
        // 2. 检查是否是借阅相关
        if (query.contains("借阅") || query.contains("借出") || query.contains("归还")) {
            return "BORROW";
        }
        
        // 3. 检查是否是问答类型
        if (query.contains("什么") || query.contains("如何") || query.contains("为什么") ||
            query.contains("怎么") || query.contains("是否")) {
            return "QUESTION";
        }
        
        // 4. 默认为搜索类型
        return "SEARCH";
    }

    private List<String> extractKeywords(String query) {
        List<String> keywords = new ArrayList<>();
        
        // 1. 移除常见的停用词
        String[] stopWords = {"的", "了", "是", "在", "我", "要", "想", "请", "帮我", "查找", "查询"};
        String processedQuery = query;
        for (String stopWord : stopWords) {
            processedQuery = processedQuery.replace(stopWord, " ");
        }
        
        // 2. 分词处理
        String[] words = processedQuery.split("\\s+");
        for (String word : words) {
            if (isSignificantWord(word)) {
                keywords.add(word);
            }
        }
        
        // 3. 提取特定格式的关键词
        Pattern pattern = Pattern.compile("[a-zA-Z0-9-_]+");
        Matcher matcher = pattern.matcher(query);
        while (matcher.find()) {
            keywords.add(matcher.group());
        }
        
        return keywords;
    }

    private List<TimeRange> extractTimeRanges(String query) {
        List<TimeRange> timeRanges = new ArrayList<>();
        
        // 1. 提取具体时间范围
        String[] timeKeywords = query.split("[,，和与]");
        for (String timeKeyword : timeKeywords) {
            if (timeKeyword.matches(".*\\d+.*")) {
                TimeRange range = parseTimeRange(timeKeyword);
                if (range != null) {
                    timeRanges.add(range);
                }
            }
        }
        
        // 2. 如果没有找到具体时间，检查相对时间
        if (timeRanges.isEmpty()) {
            if (query.contains("最近") || query.contains("过去")) {
                LocalDateTime now = LocalDateTime.now();
                if (query.contains("一周") || query.contains("7天")) {
                    timeRanges.add(new TimeRange(now.minusDays(7), now));
                } else if (query.contains("一个月") || query.contains("30天")) {
                    timeRanges.add(new TimeRange(now.minusMonths(1), now));
                } else if (query.contains("一年")) {
                    timeRanges.add(new TimeRange(now.minusYears(1), now));
                }
            }
        }
        
        return timeRanges;
    }

    private double calculateTypeScore(QueryContext context, String... keywords) {
        double score = 0.0;
        String query = context.getQuery().toLowerCase();
        String previousQuery = context.getPreviousQuery();
        
        // 当前查询的权重
        for (String keyword : keywords) {
            if (query.contains(keyword)) {
                score += 1.0;
            }
        }
        
        // 上下文的权重
        if (previousQuery != null) {
            for (String keyword : keywords) {
                if (previousQuery.toLowerCase().contains(keyword)) {
                    score += 0.5;
                }
            }
        }
        
        return score;
    }

    private Map<String, List<String>> extractEntities(QueryContext context) {
        Map<String, List<String>> entities = new HashMap<>();
        
        // 1. 提取关键词
        List<String> keywords = new ArrayList<>();
        String[] words = context.getQuery().split("\\s+");
        for (String word : words) {
            if (isSignificantWord(word)) {
                keywords.add(word);
            }
        }
        entities.put("keywords", keywords);
        
        // 2. 提取类别
        List<String> categories = extractCategories(context.getQuery());
        entities.put("categories", categories);
        
        // 3. 提取位置信息
        List<String> locations = extractLocations(context.getQuery());
        entities.put("locations", locations);
        
        return entities;
    }

    private boolean isSignificantWord(String word) {
        // 停用词过滤
        Set<String> stopWords = Set.of("的", "了", "和", "与", "或", "在", "是", "有");
        return !stopWords.contains(word) && word.length() > 1;
    }

    private void updateStatistics(String query, Long userId) {
        // 更新查询频率统计
        queryFrequencyMap.computeIfAbsent(query.toLowerCase(), k -> new AtomicInteger(0))
            .incrementAndGet();
    }

    private void updateContext(Long userId, String query, String answer) {
        // 保存最近的查询上下文
        userContext.put(userId, String.format("%s|%s", query, answer));
    }

    // 内部类：查询上下文
    private static class QueryContext {
        private final String query;
        private final String previousQuery;
        
        public QueryContext(String query, String context) {
            this.query = query;
            this.previousQuery = context != null ? context.split("\\|")[0] : null;
        }
        
        public String getQuery() { return query; }
        public String getPreviousQuery() { return previousQuery; }
    }

    // 内部类：时间范围
    private static class TimeRange {
        private final LocalDateTime start;
        private final LocalDateTime end;
        
        public TimeRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
        
        public static TimeRange lastDays(int days) {
            return new TimeRange(
                LocalDateTime.now().minusDays(days),
                LocalDateTime.now()
            );
        }
        
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
    }

    // 内部类：查询意图增强
    private static class QueryIntent {
        private String type;
        private List<String> keywords = new ArrayList<>();
        private List<TimeRange> timeRanges = new ArrayList<>();
        private List<String> categories = new ArrayList<>();
        private List<String> locations = new ArrayList<>();
        private int complexity;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
        public List<TimeRange> getTimeRanges() { return timeRanges; }
        public void setTimeRanges(List<TimeRange> timeRanges) { this.timeRanges = timeRanges; }
        public List<String> getCategories() { return categories; }
        public void setCategories(List<String> categories) { this.categories = categories; }
        public List<String> getLocations() { return locations; }
        public void setLocations(List<String> locations) { this.locations = locations; }
        public int getComplexity() { return complexity; }
        public void setComplexity(int complexity) { this.complexity = complexity; }
    }

    @Override
    public List<Archive> searchRelevantArchives(String query) {
        Set<Archive> relevantArchives = new HashSet<>();
        
        // 1. 基于时间范围搜索
        List<TimeRange> timeRanges = extractTimeRanges(query);
        for (TimeRange range : timeRanges) {
            relevantArchives.addAll(archiveRepository.findByCreateTimeBetween(
                range.getStart(), range.getEnd()
            ));
        }
        
        // 2. 基于关键词搜索
        List<String> keywords = extractKeywords(query);
        for (String keyword : keywords) {
            relevantArchives.addAll(archiveRepository.findByTitleContainingOrDescriptionContaining(
                keyword, keyword
            ));
        }
        
        // 3. 基于类别搜索
        List<String> categories = extractCategories(query);
        for (String category : categories) {
            relevantArchives.addAll(archiveRepository.findByCategory(category));
        }
        
        // 4. 基于位置搜索
        List<String> locations = extractLocations(query);
        for (String location : locations) {
            relevantArchives.addAll(archiveRepository.findByLocation(location));
        }
        
        // 5. 计算相关度并排序
        List<Archive> sortedArchives = relevantArchives.stream()
            .map(archive -> new ArchiveRelevance(archive, calculateRelevance(archive, query)))
            .sorted(Comparator.comparingDouble(ArchiveRelevance::getRelevance).reversed())
            .map(ArchiveRelevance::getArchive)
            .collect(Collectors.toList());
        
        // 6. 限制返回数量
        return sortedArchives.stream().limit(5).collect(Collectors.toList());
    }

    private int calculateRelevance(Archive archive, String query) {
        int relevance = 0;
        
        // 1. 标题匹配权重
        for (String keyword : extractKeywords(query)) {
            if (archive.getTitle().contains(keyword)) {
                relevance += 3;
                // 完全匹配给予额外权重
                if (archive.getTitle().equals(keyword)) {
                    relevance += 2;
                }
            }
            if (archive.getDescription().contains(keyword)) {
                relevance += 2;
            }
            if (archive.getCategory().contains(keyword)) {
                relevance += 1;
            }
        }
        
        // 2. 时间匹配权重
        List<TimeRange> timeRanges = extractTimeRanges(query);
        for (TimeRange range : timeRanges) {
            if (isTimeMatched(archive.getCreateTime(), range)) {
                relevance += 2;
                // 如果是最近的文档，给予额外权重
                if (isRecentDocument(archive.getCreateTime())) {
                    relevance += 1;
                }
            }
        }
        
        // 3. 类别匹配权重
        for (String category : extractCategories(query)) {
            if (archive.getCategory().equals(category)) {
                relevance += 2;
            }
        }
        
        // 4. 位置匹配权重
        for (String location : extractLocations(query)) {
            if (archive.getLocation().equals(location)) {
                relevance += 2;
            }
        }
        
        // 5. 档案状态权重
        if ("AVAILABLE".equals(archive.getStatus())) {
            relevance += 1;
        }
        
        // 6. 访问频率权重
        relevance += getAccessFrequencyWeight(archive);
        
        return relevance;
    }

    private boolean isTimeMatched(LocalDateTime createTime, TimeRange range) {
        if (createTime == null || range == null || range.getStart() == null || range.getEnd() == null) {
            return false;
        }
        return !createTime.isBefore(range.getStart()) && !createTime.isAfter(range.getEnd());
    }

    private boolean isRecentDocument(LocalDateTime createTime) {
        if (createTime == null) {
            return false;
        }
        return createTime.isAfter(LocalDateTime.now().minusMonths(1));
    }

    private int getAccessFrequencyWeight(Archive archive) {
        // 获取档案的访问频率，可以从缓存或数据库中获取
        Integer frequency = queryFrequencyMap.get(archive.getFileNumber()) != null ?
            queryFrequencyMap.get(archive.getFileNumber()).get() : 0;
        
        // 根据访问频率返回权重
        if (frequency > 100) return 3;
        if (frequency > 50) return 2;
        if (frequency > 10) return 1;
        return 0;
    }

    // 内部类：用于档案相关度排序
    private static class ArchiveRelevance {
        private final Archive archive;
        private final int relevance;
        
        public ArchiveRelevance(Archive archive, int relevance) {
            this.archive = archive;
            this.relevance = relevance;
        }
        
        public Archive getArchive() { return archive; }
        public int getRelevance() { return relevance; }
    }

    @Override
    public String generateAnswer(String question, List<Archive> relevantArchives) {
        StringBuilder answer = new StringBuilder();
        
        switch (analyzeQueryIntent(question).getType()) {
            case "STATISTICS":
                answer.append(generateStatisticsAnswer(relevantArchives));
                break;
                
            case "BORROW":
                answer.append(generateBorrowAnswer(relevantArchives));
                break;
                
            case "QUESTION":
                answer.append(generateQuestionAnswer(question, relevantArchives));
                break;
                
            case "SEARCH":
            default:
                answer.append(generateSearchAnswer(relevantArchives));
                break;
        }
        
        // 添加推荐建议
        if (!relevantArchives.isEmpty()) {
            answer.append("\n\n您可能还想了解：\n");
            List<String> suggestions = generateSuggestions(relevantArchives);
            for (String suggestion : suggestions) {
                answer.append("- ").append(suggestion).append("\n");
            }
        }
        
        return answer.toString();
    }

    private String generateStatisticsAnswer(List<Archive> archives) {
        StringBuilder answer = new StringBuilder();
        
        // 1. 总数统计
        answer.append("找到相关档案 ").append(archives.size()).append(" 份。\n\n");
        
        // 2. 按类别统计
        Map<String, Long> categoryStats = archives.stream()
            .collect(Collectors.groupingBy(Archive::getCategory, Collectors.counting()));
        
        if (!categoryStats.isEmpty()) {
            answer.append("按类别统计：\n");
            categoryStats.forEach((category, count) -> 
                answer.append("- ").append(category).append(": ").append(count).append(" 份\n")
            );
        }
        
        // 3. 时间分布
        List<TimeRange> timeRanges = archives.stream()
            .map(Archive::getCreateTime)
            .filter(Objects::nonNull)
            .map(time -> new TimeRange(
                time.withHour(0).withMinute(0).withSecond(0),
                time.withHour(23).withMinute(59).withSecond(59)
            ))
            .distinct()
            .collect(Collectors.toList());
            
        if (!timeRanges.isEmpty()) {
            answer.append("\n时间分布：\n");
            for (TimeRange range : timeRanges) {
                long count = archives.stream()
                    .filter(a -> isTimeMatched(a.getCreateTime(), range))
                    .count();
                answer.append("- ")
                      .append(formatTimeRange(range))
                      .append(": ")
                      .append(count)
                      .append(" 份\n");
            }
        }
        
        return answer.toString();
    }

    private String generateBorrowAnswer(List<Archive> archives) {
        StringBuilder answer = new StringBuilder();
        
        // 1. 检查是否有可借阅的档案
        List<Archive> availableArchives = archives.stream()
            .filter(a -> "AVAILABLE".equals(a.getStatus()))
            .collect(Collectors.toList());
        
        if (availableArchives.isEmpty()) {
            answer.append("抱歉，当前没有找到可借阅的相关档案。");
            if (!archives.isEmpty()) {
                answer.append("找到的档案可能已被借出或不可借阅。");
            }
        } else {
            answer.append("找到 ").append(availableArchives.size()).append(" 份可借阅的档案：\n\n");
            for (Archive archive : availableArchives) {
                answer.append("- 《").append(archive.getTitle()).append("》\n")
                      .append("  档案编号：").append(archive.getFileNumber()).append("\n")
                      .append("  存放位置：").append(archive.getLocation()).append("\n");
            }
            answer.append("\n您可以点击档案标题申请借阅。");
        }
        
        return answer.toString();
    }

    private String generateQuestionAnswer(String question, List<Archive> archives) {
        StringBuilder answer = new StringBuilder();
        
        if (archives.isEmpty()) {
            answer.append("抱歉，我没有找到相关的信息。您可以尝试：\n")
                  .append("1. 使用不同的关键词\n")
                  .append("2. 检查时间范围是否正确\n")
                  .append("3. 联系档案管理员获取帮助");
        } else {
            // 根据问题类型生成回答
            if (question.contains("在哪") || question.contains("位置")) {
                answer.append("相关档案存放在：\n");
                archives.stream()
                    .map(Archive::getLocation)
                    .distinct()
                    .forEach(location -> answer.append("- ").append(location).append("\n"));
                    
            } else if (question.contains("什么时候") || question.contains("时间")) {
                answer.append("相关档案的时间信息：\n");
                archives.forEach(archive -> 
                    answer.append("- 《").append(archive.getTitle()).append("》: ")
                          .append(formatDateTime(archive.getCreateTime())).append("\n")
                );
                
            } else {
                // 默认回答
                Archive mostRelevant = archives.get(0);
                answer.append("根据找到的信息，")
                      .append(mostRelevant.getDescription())
                      .append("\n\n详细信息请参考档案《")
                      .append(mostRelevant.getTitle())
                      .append("》（档案编号：")
                      .append(mostRelevant.getFileNumber())
                      .append("）");
            }
        }
        
        return answer.toString();
    }

    private String generateSearchAnswer(List<Archive> archives) {
        StringBuilder answer = new StringBuilder();
        
        if (archives.isEmpty()) {
            answer.append("抱歉，没有找到符合条件的档案。\n");
            // 从当前上下文中获取查询意图，避免空指针
            QueryIntent intent = new QueryIntent();
            intent.setType("SEARCH");
            answer.append("建议您：\n");
            answer.append("1. 尝试使用不同的关键词\n");
            answer.append("2. 调整搜索的时间范围\n");
            answer.append("3. 减少限制条件");
        } else {
            answer.append("为您找到以下相关档案：\n\n");
            
            for (Archive archive : archives) {
                answer.append("- 《").append(archive.getTitle()).append("》\n")
                      .append("  档案编号：").append(archive.getFileNumber()).append("\n")
                      .append("  创建时间：").append(formatDateTime(archive.getCreateTime())).append("\n")
                      .append("  存放位置：").append(archive.getLocation()).append("\n")
                      .append("  状态：").append(formatStatus(archive.getStatus())).append("\n\n");
            }
        }
        
        return answer.toString();
    }

    private List<String> generateSuggestions(List<Archive> archives) {
        List<String> suggestions = new ArrayList<>();
        
        // 1. 基于档案类别的建议
        Set<String> categories = archives.stream()
            .map(Archive::getCategory)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
            
        categories.forEach(category -> 
            suggestions.add("查看更多" + category + "相关档案"));
            
        // 2. 基于时间的建议
        LocalDateTime latestTime = archives.stream()
            .map(Archive::getCreateTime)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);
            
        if (latestTime != null) {
            suggestions.add("查看" + latestTime.getYear() + "年的其他档案");
        }
        
        // 3. 基于位置的建议
        Set<String> locations = archives.stream()
            .map(Archive::getLocation)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
            
        locations.forEach(location -> 
            suggestions.add("查看" + location + "的其他档案"));
            
        return suggestions.stream()
            .limit(3)
            .collect(Collectors.toList());
    }

    private String formatTimeRange(TimeRange range) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        return range.getStart().format(formatter) + " 至 " + range.getEnd().format(formatter);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "未知时间";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "未知状态";
        }
        switch (status.toUpperCase()) {
            case "AVAILABLE":
                return "可用";
            case "BORROWED":
                return "已借出";
            case "PROCESSING":
                return "处理中";
            case "ARCHIVED":
                return "已归档";
            default:
                return status;
        }
    }

    // 辅助方法

    private List<String> extractCategories(String query) {
        List<String> categories = new ArrayList<>();
        String[] commonCategories = {"财务", "人事", "技术", "法律", "行政", "项目"};
        
        for (String category : commonCategories) {
            if (query.contains(category)) {
                categories.add(category);
            }
        }
        
        return categories;
    }

    private List<String> extractLocations(String query) {
        List<String> locations = new ArrayList<>();
        String[] commonLocations = {"档案室", "资料库", "办公室", "仓库"};
        
        for (String location : commonLocations) {
            if (query.contains(location)) {
                locations.add(location);
            }
        }
        
        return locations;
    }

    private int analyzeComplexity(String query) {
        int complexity = 0;
        
        // 1. 基于查询长度
        complexity += query.length() / 10;
        
        // 2. 基于条件数量
        if (query.contains("并且") || query.contains("而且")) complexity += 2;
        if (query.contains("或者")) complexity += 2;
        if (query.contains("不") || query.contains("没有")) complexity += 1;
        
        // 3. 基于时间条件
        if (query.matches(".*\\d{4}年.*")) complexity += 1;
        if (query.matches(".*\\d{1,2}月.*")) complexity += 1;
        if (query.matches(".*\\d{1,2}日.*")) complexity += 1;
        
        // 4. 基于特定要求
        if (query.contains("排序")) complexity += 2;
        if (query.contains("分类")) complexity += 2;
        if (query.contains("统计")) complexity += 3;
        
        return complexity;
    }

    private String analyzeQuestionType(String question) {
        // 简单的问题类型判断，可以使用NLP技术优化
        if (question.contains("搜索") || question.contains("查找") || question.contains("找到")) {
            return "SEARCH";
        }
        return "QUESTION";
    }

    private String generateAnswerFromArchives(String question, List<Archive> archives) {
        // 根据档案内容生成答案，可以使用更复杂的算法
        StringBuilder answer = new StringBuilder();
        Archive mostRelevant = archives.get(0);
        
        answer.append("我找到了相关信息：\n");
        answer.append(mostRelevant.getDescription());
        answer.append("\n\n您可以在档案《").append(mostRelevant.getTitle())
              .append("》（档案编号：").append(mostRelevant.getFileNumber())
              .append("）中找到更详细的信息。");
        
        return answer.toString();
    }

    public List<String> getRecommendations(Long userId, String query) {
        List<String> recommendations = new ArrayList<>();
        
        // 1. 基于用户历史行为推荐
        List<ChatHistory> userHistory = chatHistoryRepository.findByUserIdOrderByCreateTimeDesc(
            userId, PageRequest.of(0, 5)
        );
        
        // 2. 基于当前查询意图推荐
        QueryIntent intent = analyzeQueryIntent(query);
        switch (intent.getType()) {
            case "SEARCH":
                recommendations.add("您也可以尝试搜索相关的档案：" + String.join("、", intent.getKeywords()));
                break;
            case "BORROW":
                recommendations.add("您可以查看借阅记录或申请新的借阅");
                break;
            case "STATISTICS":
                recommendations.add("您可以查看档案统计报表获取更详细的信息");
                break;
        }
        
        // 3. 基于热门查询推荐
        List<String> hotQueries = chatHistoryRepository.findHotQueries(PageRequest.of(0, 3));
        recommendations.addAll(hotQueries);
        
        return recommendations;
    }

    private TimeRange parseTimeRange(String timeStr) {
        try {
            // 1. 处理精确时间
            Pattern fullPattern = Pattern.compile("(\\d{4})年?(\\d{1,2})月?(\\d{1,2})日?");
            Matcher matcher = fullPattern.matcher(timeStr);
            if (matcher.find()) {
                int year = Integer.parseInt(matcher.group(1));
                int month = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;
                int day = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 1;
                
                LocalDateTime start = LocalDateTime.of(year, month, day, 0, 0);
                LocalDateTime end;
                
                if (matcher.group(2) == null) {
                    // 只有年份，范围为整年
                    end = start.plusYears(1).minusNanos(1);
                } else if (matcher.group(3) == null) {
                    // 有年月，范围为整月
                    end = start.plusMonths(1).minusNanos(1);
                } else {
                    // 完整日期，范围为整天
                    end = start.plusDays(1).minusNanos(1);
                }
                
                return new TimeRange(start, end);
            }
            
            // 2. 处理年份范围
            Pattern yearPattern = Pattern.compile("(\\d{4})年?");
            matcher = yearPattern.matcher(timeStr);
            if (matcher.find()) {
                int year = Integer.parseInt(matcher.group(1));
                LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
                LocalDateTime end = start.plusYears(1).minusNanos(1);
                return new TimeRange(start, end);
            }
            
            // 3. 处理相对时间
            if (timeStr.contains("最近") || timeStr.contains("过去")) {
                LocalDateTime now = LocalDateTime.now();
                if (timeStr.contains("一周") || timeStr.contains("7天")) {
                    return new TimeRange(now.minusDays(7), now);
                } else if (timeStr.contains("一个月") || timeStr.contains("30天")) {
                    return new TimeRange(now.minusMonths(1), now);
                } else if (timeStr.contains("一年")) {
                    return new TimeRange(now.minusYears(1), now);
                }
            }
            
            // 4. 默认返回最近一个月
            LocalDateTime now = LocalDateTime.now();
            return new TimeRange(now.minusMonths(1), now);
            
        } catch (Exception e) {
            // 解析失败时返回最近一个月
            LocalDateTime now = LocalDateTime.now();
            return new TimeRange(now.minusMonths(1), now);
        }
    }

    public void updateLearningModel(Long userId, String query, boolean isSuccessful) {
        Assert.notNull(userId, "User ID cannot be null");
        Assert.hasText(query, "Query cannot be empty");
        
        try {
            // 1. 更新查询模式
            List<QueryPattern> patterns = userQueryPatterns.computeIfAbsent(userId, k -> new ArrayList<>());
            QueryPattern newPattern = new QueryPattern(query, isSuccessful);
            patterns.add(newPattern);
            
            // 2. 更新成功/失败统计
            if (isSuccessful) {
                reinforceSuccessfulPattern(query);
            } else {
                adjustFailedPattern(query);
            }
            
            // 3. 更新用户偏好
            Map<String, Integer> preferences = analyzeUserPreferences(userId);
            saveUserPreferences(userId, preferences);
            
            logger.info("Learning model updated for user {}: query='{}', successful={}", 
                userId, query, isSuccessful);
        } catch (Exception e) {
            logger.error("Error updating learning model: {}", e.getMessage(), e);
        }
    }

    private void reinforceSuccessfulPattern(String pattern) {
        // 增加成功模式的权重
        successfulPatterns.computeIfAbsent(pattern, k -> new AtomicInteger(0))
            .incrementAndGet();
            
        // 更新关键词权重
        String[] keywords = pattern.split("\\s+");
        for (String keyword : keywords) {
            keywordWeights.compute(keyword, (k, v) -> v == null ? 1.0 : v + 0.1);
        }
    }

    private void adjustFailedPattern(String pattern) {
        // 记录失败原因
        FailureReason reason = new FailureReason();
        reason.setPattern(pattern);
        reason.setTimestamp(LocalDateTime.now());
        reason.setCount(1);
        
        failureReasons.computeIfAbsent(pattern, k -> new ArrayList<>())
            .add(reason);
            
        // 降低关键词权重
        String[] keywords = pattern.split("\\s+");
        for (String keyword : keywords) {
            keywordWeights.compute(keyword, (k, v) -> v == null ? -0.1 : v - 0.1);
        }
    }


    public void saveUserPreferences(Long userId, Map<String, Integer> preferences) {
        Assert.notNull(userId, "User ID cannot be null");
        Assert.notNull(preferences, "Preferences cannot be null");
        
        try {
            // 1. 序列化偏好数据
            String preferencesJson = objectMapper.writeValueAsString(preferences);
            
            // 2. 更新用户实体
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setPreferences(preferencesJson);
            
            // 3. 保存更新
            userRepository.save(user);
            
            logger.info("User preferences saved for user {}", userId);
        } catch (Exception e) {
            logger.error("Error saving user preferences: {}", e.getMessage(), e);
        }
    }

    // 内部类：查询模式
    private static class QueryPattern {
        private final String pattern;
        private final boolean successful;
        private final LocalDateTime timestamp;
        
        public QueryPattern(String pattern, boolean successful) {
            this.pattern = pattern;
            this.successful = successful;
            this.timestamp = LocalDateTime.now();
        }
    }

    // 内部类：失败原因
    private static class FailureReason {
        private String pattern;
        private LocalDateTime timestamp;
        private int count;
        
        public void setPattern(String pattern) { this.pattern = pattern; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public void setCount(int count) { this.count = count; }
    }

    private Map<String, Integer> analyzeUserPreferences(Long userId) {
        Map<String, Integer> preferences = new HashMap<>();
        
        // 1. 分析历史查询
        List<ChatHistory> history = chatHistoryRepository.findByUserIdOrderByCreateTimeDesc(
            userId, PageRequest.of(0, 100));
            
        // 2. 统计关键词频率
        for (ChatHistory chat : history) {
            String[] keywords = chat.getQuery().split("\\s+");
            for (String keyword : keywords) {
                preferences.merge(keyword, 1, Integer::sum);
            }
        }
        
        return preferences;
    }
} 