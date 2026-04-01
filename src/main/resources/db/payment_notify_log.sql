CREATE TABLE `payment_notify_log` (
                                      `id` BIGINT NOT NULL PRIMARY KEY,
                                      `payment_channel` VARCHAR(32) NOT NULL,
                                      `out_trade_no` VARCHAR(64) DEFAULT NULL,
                                      `provider_trade_no` VARCHAR(64) DEFAULT NULL,
                                      `notify_type` VARCHAR(32) NOT NULL COMMENT 'TRANSACTION',
                                      `raw_body` LONGTEXT NOT NULL,
                                      `process_status` TINYINT NOT NULL DEFAULT 10 COMMENT '10-收到 20-处理成功 30-忽略 40-处理失败 50-异常单',
                                      `error_message` VARCHAR(255) DEFAULT NULL,
                                      `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      KEY `idx_out_trade_no` (`out_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
