package com.weijinchuan.aiflashsale.controller;

import com.weijinchuan.aiflashsale.common.api.Result;
import com.weijinchuan.aiflashsale.dto.ai.ShoppingAskDTO;
import com.weijinchuan.aiflashsale.dto.ai.ShoppingRecommendDTO;
import com.weijinchuan.aiflashsale.service.AiShoppingService;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingAnswerVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI 导购接口控制器
 */
@RestController
@RequestMapping("/ai/shopping")
@RequiredArgsConstructor
public class AiShoppingController {

    private final AiShoppingService aiShoppingService;

    /**
     * AI 导购推荐接口
     */
    @PostMapping("/recommend")
    @Operation(summary = "AI 导购推荐")
    public Result<ShoppingRecommendVO> recommend(@Valid @RequestBody ShoppingRecommendDTO dto) {
        return Result.success(aiShoppingService.recommend(dto));
    }

    /**
     * AI 导购问答接口
     */
    @PostMapping("/ask")
    @Operation(summary = "AI 导购问答")
    public Result<ShoppingAnswerVO> ask(@Valid @RequestBody ShoppingAskDTO dto) {
        return Result.success(aiShoppingService.ask(dto));
    }
}
