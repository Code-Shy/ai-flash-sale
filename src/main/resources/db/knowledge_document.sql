CREATE TABLE `knowledge_document` (
                                      `id` BIGINT NOT NULL PRIMARY KEY,
                                      `doc_code` VARCHAR(64) NOT NULL,
                                      `title` VARCHAR(255) NOT NULL,
                                      `content` TEXT NOT NULL,
                                      `doc_type` VARCHAR(32) NOT NULL,
                                      `store_id` BIGINT DEFAULT NULL,
                                      `sku_id` BIGINT DEFAULT NULL,
                                      `category` VARCHAR(64) DEFAULT NULL,
                                      `tags_json` TEXT DEFAULT NULL,
                                      `source_type` VARCHAR(32) NOT NULL DEFAULT 'classpath-json',
                                      `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1-有效 0-禁用',
                                      `version` INT NOT NULL DEFAULT 1,
                                      `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      UNIQUE KEY `uk_doc_code` (`doc_code`),
                                      KEY `idx_store_sku_status` (`store_id`, `sku_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
