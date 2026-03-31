CREATE TABLE `store` (
                         `id` BIGINT NOT NULL PRIMARY KEY,
                         `store_name` VARCHAR(128) NOT NULL,
                         `store_type` VARCHAR(32) DEFAULT 'convenience',
                         `address` VARCHAR(255) DEFAULT NULL,
                         `latitude` DECIMAL(10,6) DEFAULT NULL,
                         `longitude` DECIMAL(10,6) DEFAULT NULL,
                         `delivery_radius_km` DECIMAL(5,2) NOT NULL DEFAULT 3.00,
                         `business_status` TINYINT NOT NULL DEFAULT 1,
                         `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;