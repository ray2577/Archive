-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    department VARCHAR(50),
    position VARCHAR(50),
    preferences TEXT,
    last_login_time DATETIME,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户角色关联表
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 档案表
CREATE TABLE archives (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    location VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    create_time DATETIME,
    update_time DATETIME,
    creator_id BIGINT,
    last_modifier_id BIGINT,
    file_path VARCHAR(255),
    file_size BIGINT,
    file_type VARCHAR(50),
    keywords VARCHAR(255),
    FOREIGN KEY (creator_id) REFERENCES users(id),
    FOREIGN KEY (last_modifier_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 借阅记录表
CREATE TABLE borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    archive_id BIGINT NOT NULL,
    user_id BIGINT,
    borrower VARCHAR(50) NOT NULL,
    borrower_department VARCHAR(50),
    borrow_date DATETIME NOT NULL,
    expected_return_date DATETIME,
    actual_return_date DATETIME,
    status VARCHAR(20) NOT NULL,
    purpose VARCHAR(255),
    approved_by VARCHAR(50),
    create_time DATETIME,
    update_time DATETIME,
    remarks TEXT,
    FOREIGN KEY (archive_id) REFERENCES archives(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 聊天历史记录表
CREATE TABLE chat_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    query TEXT NOT NULL,
    response TEXT NOT NULL,
    query_type VARCHAR(50) NOT NULL,
    processing_time INT,
    is_helpful BOOLEAN DEFAULT NULL,
    relevance_score INT DEFAULT 0,
    user_action VARCHAR(50),
    archive_count INT DEFAULT 0,
    has_time_range BOOLEAN DEFAULT FALSE,
    has_category_filter BOOLEAN DEFAULT FALSE,
    keywords VARCHAR(255),
    relevant_archive_id_list TEXT,
    sentiment_score DECIMAL(3,2),
    intent_confidence DECIMAL(3,2),
    session_id VARCHAR(50),
    parent_query_id BIGINT,
    embedding_vector TEXT,
    metadata JSON,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_query_id) REFERENCES chat_histories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 聊天会话表
CREATE TABLE chat_sessions (
    id VARCHAR(50) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_query_time DATETIME,
    query_count INT DEFAULT 0,
    context_data JSON,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 聊天反馈表
CREATE TABLE chat_feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_history_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_helpful BOOLEAN NOT NULL,
    relevance_score INT,
    feedback_text TEXT,
    feedback_type VARCHAR(20),
    create_time DATETIME NOT NULL,
    FOREIGN KEY (chat_history_id) REFERENCES chat_histories(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 聊天统计表
CREATE TABLE chat_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    query_count INT DEFAULT 0,
    avg_processing_time INT DEFAULT 0,
    helpful_count INT DEFAULT 0,
    not_helpful_count INT DEFAULT 0,
    archive_hit_count INT DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    UNIQUE KEY uk_user_date (user_id, date),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 档案访问日志表
CREATE TABLE archive_access_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    archive_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    access_type VARCHAR(20) NOT NULL,
    access_time DATETIME NOT NULL,
    ip_address VARCHAR(50),
    device_info VARCHAR(255),
    request_id BIGINT,
    create_time DATETIME,
    FOREIGN KEY (archive_id) REFERENCES archives(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (request_id) REFERENCES archive_access_requests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 档案访问申请表
CREATE TABLE archive_access_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    archive_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    approver_id BIGINT,
    request_time DATETIME NOT NULL,
    approve_time DATETIME,
    status VARCHAR(20) NOT NULL,
    reason TEXT,
    reject_reason TEXT,
    valid_from DATETIME,
    valid_until DATETIME,
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (archive_id) REFERENCES archives(id),
    FOREIGN KEY (requester_id) REFERENCES users(id),
    FOREIGN KEY (approver_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 档案编号规则表
CREATE TABLE archive_number_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    prefix VARCHAR(20) NOT NULL,
    current_number BIGINT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    create_time DATETIME,
    update_time DATETIME,
    creator_id BIGINT,
    UNIQUE KEY uk_category_prefix (category, prefix),
    FOREIGN KEY (creator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 系统配置表
CREATE TABLE system_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description VARCHAR(255),
    create_time DATETIME,
    update_time DATETIME,
    KEY idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建索引
CREATE INDEX idx_archives_status ON archives(status);
CREATE INDEX idx_archives_category ON archives(category);
CREATE INDEX idx_archives_create_time ON archives(create_time);
CREATE INDEX idx_chat_histories_create_time ON chat_histories(create_time);
CREATE INDEX idx_access_logs_access_time ON archive_access_logs(access_time);
CREATE INDEX idx_access_requests_status ON archive_access_requests(status);
CREATE INDEX idx_access_requests_request_time ON archive_access_requests(request_time);
CREATE INDEX idx_borrow_records_status ON borrow_records(status);
CREATE INDEX idx_borrow_records_borrow_date ON borrow_records(borrow_date);
CREATE INDEX idx_borrow_records_archive_id ON borrow_records(archive_id);
CREATE INDEX idx_borrow_records_user_id ON borrow_records(user_id);

-- 创建聊天相关索引
CREATE INDEX idx_chat_histories_user_id ON chat_histories(user_id);
CREATE INDEX idx_chat_histories_query_type ON chat_histories(query_type);
CREATE INDEX idx_chat_histories_session_id ON chat_histories(session_id);
CREATE INDEX idx_chat_sessions_user_id ON chat_sessions(user_id);
CREATE INDEX idx_chat_sessions_status ON chat_sessions(status);
CREATE INDEX idx_chat_feedbacks_chat_history_id ON chat_feedbacks(chat_history_id);
CREATE INDEX idx_chat_statistics_user_date ON chat_statistics(user_id, date);

-- 触发器：更新聊天统计
DELIMITER //
CREATE TRIGGER after_chat_history_insert
AFTER INSERT ON chat_histories
FOR EACH ROW
BEGIN
    INSERT INTO chat_statistics (user_id, date, query_count, create_time, update_time)
    VALUES (NEW.user_id, DATE(NEW.create_time), 1, NOW(), NOW())
    ON DUPLICATE KEY UPDATE
        query_count = query_count + 1,
        update_time = NOW();
END //

CREATE TRIGGER after_chat_feedback_insert
AFTER INSERT ON chat_feedbacks
FOR EACH ROW
BEGIN
    UPDATE chat_statistics
    SET 
        helpful_count = helpful_count + IF(NEW.is_helpful = 1, 1, 0),
        not_helpful_count = not_helpful_count + IF(NEW.is_helpful = 0, 1, 0),
        update_time = NOW()
    WHERE user_id = NEW.user_id
    AND date = DATE(NEW.create_time);
END //
DELIMITER ;

-- 初始化基础数据
INSERT INTO users (username, password, real_name, email, enabled, create_time, update_time)
VALUES ('admin', '$2a$10$rTZ7CWtxnXL5f.GW7oxPy.QaY1B8v1YhB8P5U.XhF9yfGMT/uhFYi', '系统管理员', 'admin@example.com', true, NOW(), NOW());

INSERT INTO user_roles (user_id, role)
VALUES (1, 'ROLE_ADMIN');

-- 初始化档案编号规则
INSERT INTO archive_number_rules (category, prefix, current_number, is_active, create_time, update_time, creator_id)
VALUES 
('财务', 'FIN', 1, true, NOW(), NOW(), 1),
('人事', 'HR', 1, true, NOW(), NOW(), 1),
('技术', 'TECH', 1, true, NOW(), NOW(), 1),
('法律', 'LAW', 1, true, NOW(), NOW(), 1),
('行政', 'ADM', 1, true, NOW(), NOW(), 1),
('项目', 'PRJ', 1, true, NOW(), NOW(), 1);

-- 初始化聊天配置
INSERT INTO system_configs (config_key, config_value, description, create_time, update_time)
VALUES 
('chat.max_history_days', '30', '聊天历史保留天数', NOW(), NOW()),
('chat.max_session_idle_minutes', '30', '会话最大空闲时间(分钟)', NOW(), NOW()),
('chat.max_tokens_per_request', '2000', '单次请求最大Token数', NOW(), NOW()),
('chat.enable_sentiment_analysis', 'true', '是否启用情感分析', NOW(), NOW()),
('chat.enable_vector_search', 'true', '是否启用向量搜索', NOW(), NOW()); 