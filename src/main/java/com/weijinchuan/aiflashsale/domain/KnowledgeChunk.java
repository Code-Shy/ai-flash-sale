package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识分片实体
 */
@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long documentId;
    private Integer chunkIndex;
    private Long storeId;
    private Long skuId;
    private String category;
    private String tagsJson;
    private String content;
    private String contentPreview;
    private String normalizedText;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
