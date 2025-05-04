-- 创建 archive 表
CREATE TABLE IF NOT EXISTS archive (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    file_path VARCHAR(255),
    file_size BIGINT,
    upload_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- 创建 chat_history 表
CREATE TABLE IF NOT EXISTS chat_history (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            user_id BIGINT NOT NULL,
                                            query TEXT NOT NULL,
                                            response TEXT,
                                            query_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建 feedback 表
CREATE TABLE IF NOT EXISTS feedback (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        chat_history_id BIGINT NOT NULL,
                                        rating INT,
                                        comment TEXT,
                                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                        FOREIGN KEY (chat_history_id) REFERENCES chat_history(id)
    );