package com.weijinchuan.aiflashsale.service.rag;

import com.weijinchuan.aiflashsale.vo.ai.KnowledgeSyncVO;

/**
 * 知识库导入服务
 */
public interface KnowledgeIngestionService {

    /**
     * 将 classpath 中的知识文档同步到数据库。
     */
    KnowledgeSyncVO syncFromClasspath();
}
