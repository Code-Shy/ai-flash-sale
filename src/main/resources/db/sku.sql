CREATE TABLE `sku` (
                       `id` BIGINT NOT NULL PRIMARY KEY,
                       `spu_id` BIGINT NOT NULL,
                       `sku_name` VARCHAR(128) NOT NULL,
                       `specs` VARCHAR(255) DEFAULT NULL,
                       `unit` VARCHAR(32) DEFAULT NULL,
                       `image_url` VARCHAR(255) DEFAULT NULL,
                       `status` TINYINT NOT NULL DEFAULT 1,
                       `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;