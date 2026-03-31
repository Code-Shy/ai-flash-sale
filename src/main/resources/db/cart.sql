CREATE TABLE `cart` (
                        `id` BIGINT NOT NULL PRIMARY KEY,
                        `user_id` BIGINT NOT NULL,
                        `store_id` BIGINT NOT NULL,
                        `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1-有效 0-失效',
                        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;