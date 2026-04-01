CREATE TABLE `order_operate_log` (
                                     `id` BIGINT NOT NULL PRIMARY KEY,
                                     `order_id` BIGINT NOT NULL,
                                     `order_no` VARCHAR(64) NOT NULL,
                                     `before_status` TINYINT DEFAULT NULL,
                                     `after_status` TINYINT NOT NULL,
                                     `operate_type` VARCHAR(32) NOT NULL COMMENT 'CREATE/CANCEL/PAY/COMPLETE/EXPIRE',
                                     `operate_by` VARCHAR(64) DEFAULT NULL,
                                     `remark` VARCHAR(255) DEFAULT NULL,
                                     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
