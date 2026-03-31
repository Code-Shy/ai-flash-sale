CREATE TABLE `store_sku` (
                             `id` BIGINT NOT NULL PRIMARY KEY,
                             `store_id` BIGINT NOT NULL,
                             `sku_id` BIGINT NOT NULL,
                             `origin_price` DECIMAL(10,2) NOT NULL,
                             `sale_price` DECIMAL(10,2) NOT NULL,
                             `sale_status` TINYINT NOT NULL DEFAULT 1,
                             `sort` INT NOT NULL DEFAULT 0,
                             `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             UNIQUE KEY `uk_store_sku` (`store_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;