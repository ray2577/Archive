package com.ray.archive.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作流实例实体类
 * 用于存储工作流执行的实例记录
 */
@Data
@Entity
@Table(name = "workflow_instances")
@EntityListeners(AuditingEntityListener.class)
public class WorkflowInstance {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstance.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;  // 所属流程定义

    @Column(nullable = false)
    private String businessKey;  // 业务标识

    @Column(nullable = false)
    private String processInstanceId;  // 流程实例ID

    @Column(nullable = false)
    private String processType;  // 流程类型

    @Column(nullable = false)
    private String initiator;  // 发起人

    @Column(nullable = false)
    private String status;  // 状态：RUNNING, COMPLETED, CANCELED, TERMINATED

    private String result;  // 结果：approved(通过), rejected(拒绝)

    @Column(columnDefinition = "TEXT")
    private String businessData;  // 业务数据JSON

    @Column(columnDefinition = "TEXT")
    private String variables;  // 流程变量JSON

    @CreatedDate
    private LocalDateTime startTime;  // 开始时间

    private LocalDateTime endTime;  // 结束时间
    
    // 计算耗时（毫秒）
    private Long duration;  // 耗时（毫秒）

    @LastModifiedDate
    private LocalDateTime updateTime;  // 更新时间

    private String currentTask;  // 当前任务
    
    private String currentAssignee;  // 当前处理人
    
    @Column(length = 2000)
    private String comment;  // 流程备注
    
    private Integer priority = 0;  // 优先级: 0=低, 1=中, 2=高, 3=紧急

    // Convert the Map field to proper serialized string format
    @Transient // This will exclude the field from being persisted directly
    private Map<String, Object> content = new HashMap<>();
    
    // JSON serialized content field for database storage
    @Column(columnDefinition = "TEXT", name = "content_json")
    private String contentJson;
    
    // Getter that deserializes JSON to Map
    public Map<String, Object> getContent() {
        if (content.isEmpty() && contentJson != null && !contentJson.isEmpty()) {
            try {
                content = objectMapper.readValue(contentJson, new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                logger.error("Error deserializing content JSON: {}", e.getMessage());
            }
        }
        return content;
    }
    
    // Setter that also updates the JSON representation
    public void setContent(Map<String, Object> content) {
        this.content = content;
        try {
            this.contentJson = objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing content to JSON: {}", e.getMessage());
        }
    }
    
    // Helper method to add a single key-value to the content map
    public void addContentItem(String key, Object value) {
        getContent().put(key, value);
        try {
            this.contentJson = objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing content to JSON: {}", e.getMessage());
        }
    }
} 