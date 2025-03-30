package com.ray.archive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "chat_histories")
@EntityListeners(AuditingEntityListener.class)
public class ChatHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String query;

    @Column(nullable = false, length = 2000)
    private String response;

    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "relevant_archive_id_list", length = 1000)
    private String relevantArchiveIdList; // 相关档案ID，用逗号分隔

    @Column(name = "query_type", length = 50)
    private String queryType; // SEARCH, QUESTION, BORROW, STATISTICS

    @Column(name = "processing_time")
    private Integer processingTime; // 处理时间（毫秒）

    @Column(name = "is_helpful")
    private Boolean isHelpful; // 用户反馈是否有帮助

    @Column(name = "relevance_score")
    private Integer relevanceScore; // 相关度评分（1-5）

    @Column(name = "user_action", length = 50)
    private String userAction; // 用户后续操作：BORROW（借阅）, DOWNLOAD（下载）, NONE（无操作）

    @Column(name = "archive_count")
    private Integer archiveCount; // 相关档案数量

    @Column(name = "has_time_range")
    private Boolean hasTimeRange; // 是否包含时间范围查询

    @Column(name = "has_category_filter")
    private Boolean hasCategoryFilter; // 是否包含类别筛选

    @Column(length = 500)
    private String keywords; // 提取的关键词，用逗号分隔

    @Column(name = "sentiment_score", columnDefinition = "DOUBLE")
    private Double sentimentScore;

    @Column(name = "intent_confidence", columnDefinition = "DOUBLE")
    private Double intentConfidence;

    @Column(name = "session_id", length = 50)
    private String sessionId;

    @Column(name = "parent_query_id")
    private Long parentQueryId;

    @Column(name = "embedding_vector", columnDefinition = "TEXT")
    private String embeddingVector;

    @Column(columnDefinition = "JSON")
    private String metadata;

    // 辅助方法
    public List<Long> getRelevantArchiveIds() {
        if (relevantArchiveIdList == null || relevantArchiveIdList.isEmpty()) {
            return List.of();
        }
        return List.of(relevantArchiveIdList.split(",")).stream()
                .map(Long::parseLong)
                .toList();
    }

    public void setRelevantArchiveIds(List<Long> ids) {
        this.relevantArchiveIdList = ids == null ? "" :
            String.join(",", ids.stream().map(String::valueOf).toList());
    }
} 