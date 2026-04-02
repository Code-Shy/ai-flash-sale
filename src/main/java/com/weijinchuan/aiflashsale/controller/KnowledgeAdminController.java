package com.weijinchuan.aiflashsale.controller;

import com.weijinchuan.aiflashsale.common.api.Result;
import com.weijinchuan.aiflashsale.service.rag.KnowledgeIngestionService;
import com.weijinchuan.aiflashsale.vo.ai.KnowledgeSyncVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库管理接口
 */
@RestController
@RequestMapping("/ai/knowledge")
@RequiredArgsConstructor
public class KnowledgeAdminController {

    private final KnowledgeIngestionService knowledgeIngestionService;

    @PostMapping("/sync")
    @Operation(summary = "将 classpath 知识库同步到数据库")
    public Result<KnowledgeSyncVO> syncFromClasspath() {
        return Result.success(knowledgeIngestionService.syncFromClasspath());
    }
}
