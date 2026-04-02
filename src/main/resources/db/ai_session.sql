CREATE TABLE `ai_session` (
                              `id` BIGINT NOT NULL PRIMARY KEY,
                              `user_id` BIGINT NOT NULL,
                              `store_id` BIGINT NOT NULL,
                              `title` VARCHAR(128) NOT NULL,
                              `last_query` VARCHAR(255) DEFAULT NULL,
                              `last_intent_json` TEXT DEFAULT NULL,
                              `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1-活跃 0-归档',
                              `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              KEY `idx_user_store_status` (`user_id`, `store_id`, `status`),
                              KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
