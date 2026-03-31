CREATE TABLE `order_item` (
                              `id` BIGINT NOT NULL PRIMARY KEY,
                              `order_id` BIGINT NOT NULL,
                              `order_no` VARCHAR(64) NOT NULL,
                              `store_id` BIGINT NOT NULL,
                              `sku_id` BIGINT NOT NULL,
                              `sku_name` VARCHAR(128) NOT NULL,
                              `sku_image` VARCHAR(255) DEFAULT NULL,
                              `sale_price` DECIMAL(10,2) NOT NULL,
                              `quantity` INT NOT NULL DEFAULT 1,
                              `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                              `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;