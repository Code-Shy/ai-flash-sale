CREATE TABLE `cart_item` (
                             `id` BIGINT NOT NULL PRIMARY KEY,
                             `cart_id` BIGINT NOT NULL,
                             `user_id` BIGINT NOT NULL,
                             `store_id` BIGINT NOT NULL,
                             `sku_id` BIGINT NOT NULL,
                             `quantity` INT NOT NULL DEFAULT 1,
                             `checked` TINYINT NOT NULL DEFAULT 1 COMMENT '1-选中 0-未选中',
                             `price_snapshot` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '加入购物车时价格快照',
                             `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             UNIQUE KEY `uk_cart_sku` (`cart_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;