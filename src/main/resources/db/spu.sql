CREATE TABLE `spu` (
                       `id` BIGINT NOT NULL PRIMARY KEY,
                       `spu_name` VARCHAR(128) NOT NULL,
                       `category_name` VARCHAR(64) DEFAULT NULL,
                       `brand_name` VARCHAR(64) DEFAULT NULL,
                       `description` VARCHAR(512) DEFAULT NULL,
                       `status` TINYINT NOT NULL DEFAULT 1,
                       `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;