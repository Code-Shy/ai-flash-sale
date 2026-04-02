CREATE TABLE `user_preference_profile` (
                                           `id` BIGINT NOT NULL PRIMARY KEY,
                                           `user_id` BIGINT NOT NULL,
                                           `preferred_category_keyword` VARCHAR(64) DEFAULT NULL,
                                           `preferred_product_keyword` VARCHAR(64) DEFAULT NULL,
                                           `preferred_scene_keyword` VARCHAR(64) DEFAULT NULL,
                                           `preferred_taste_preference` VARCHAR(64) DEFAULT NULL,
                                           `preferred_budget` INT DEFAULT NULL,
                                           `last_intent_json` TEXT DEFAULT NULL,
                                           `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1-有效 0-禁用',
                                           `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                           UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
