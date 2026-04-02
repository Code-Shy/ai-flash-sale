package com.weijinchuan.aiflashsale.vo.ai;

import lombok.Data;

/**
 * 知识库同步结果
 */
@Data
public class KnowledgeSyncVO {

    private String source;
    private Integer totalDocuments;
    private Integer syncedDocuments;
    private Integer generatedChunks;
    private String message;
}
