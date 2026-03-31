CREATE TABLE `orders` (
                          `id` BIGINT NOT NULL PRIMARY KEY,
                          `order_no` VARCHAR(64) NOT NULL,
                          `user_id` BIGINT NOT NULL,
                          `store_id` BIGINT NOT NULL,
                          `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                          `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                          `delivery_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                          `order_status` TINYINT NOT NULL DEFAULT 10 COMMENT '10-待支付 20-已支付 30-已取消 40-已完成',
                          `remark` VARCHAR(255) DEFAULT NULL,
                          `expire_time` DATETIME DEFAULT NULL COMMENT '订单过期时间',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          UNIQUE KEY `uk_order_no` (`order_no`),
                          KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;