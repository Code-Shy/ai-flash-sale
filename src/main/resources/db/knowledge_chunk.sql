CREATE TABLE `knowledge_chunk` (
                                   `id` BIGINT NOT NULL PRIMARY KEY,
                                   `document_id` BIGINT NOT NULL,
                                   `chunk_index` INT NOT NULL,
                                   `store_id` BIGINT DEFAULT NULL,
                                   `sku_id` BIGINT DEFAULT NULL,
                                   `category` VARCHAR(64) DEFAULT NULL,
                                   `tags_json` TEXT DEFAULT NULL,
                                   `content` TEXT NOT NULL,
                                   `content_preview` VARCHAR(255) DEFAULT NULL,
                                   `normalized_text` TEXT NOT NULL,
                                   `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1-有效 0-禁用',
                                   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   KEY `idx_document_id` (`document_id`),
                                   KEY `idx_store_sku_status` (`store_id`, `sku_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
