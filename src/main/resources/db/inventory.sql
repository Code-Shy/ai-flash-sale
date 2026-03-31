CREATE TABLE `inventory` (
                             `id` BIGINT NOT NULL PRIMARY KEY,
                             `store_id` BIGINT NOT NULL,
                             `sku_id` BIGINT NOT NULL,
                             `available_stock` INT NOT NULL DEFAULT 0,
                             `locked_stock` INT NOT NULL DEFAULT 0,
                             `version` INT NOT NULL DEFAULT 0,
                             `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             UNIQUE KEY `uk_store_sku_stock` (`store_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;