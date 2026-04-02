package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文档实体
 */
@Data
@TableName("knowledge_document")
public class KnowledgeDoc {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 外部文档编码，便于和 classpath/运营后台同步。
     */
    private String docCode;

    private String title;
    private String content;
    private String docType;
    private Long storeId;
    private Long skuId;
    private String category;
    private String tagsJson;
    private String sourceType;
    private Integer status;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
