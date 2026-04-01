CREATE TABLE `outbox_event` (
                                 `id` BIGINT NOT NULL PRIMARY KEY,
                                 `event_type` VARCHAR(64) NOT NULL,
                                 `topic` VARCHAR(128) NOT NULL,
                                 `event_key` VARCHAR(128) DEFAULT NULL,
                                 `payload` TEXT NOT NULL,
                                 `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-待发送 1-已发送 2-发送失败 3-终态失败',
                                 `retry_count` INT NOT NULL DEFAULT 0,
                                 `next_retry_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `error_message` VARCHAR(255) DEFAULT NULL,
                                 `published_time` DATETIME DEFAULT NULL,
                                 `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 KEY `idx_status_retry_time` (`status`, `next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
