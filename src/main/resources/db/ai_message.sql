CREATE TABLE `ai_message` (
                              `id` BIGINT NOT NULL PRIMARY KEY,
                              `session_id` BIGINT NOT NULL,
                              `role` VARCHAR(16) NOT NULL COMMENT 'user/assistant',
                              `message_type` VARCHAR(32) NOT NULL COMMENT 'ask/recommend/clarification',
                              `content` TEXT NOT NULL,
                              `intent_json` TEXT DEFAULT NULL,
                              `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              KEY `idx_session_time` (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
