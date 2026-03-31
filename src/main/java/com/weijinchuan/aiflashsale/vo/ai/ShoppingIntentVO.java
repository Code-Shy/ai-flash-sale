package com.weijinchuan.aiflashsale.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 购物意图解析结果
 *
 * 说明：
 * 这是大模型输出给业务层的结构化对象。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShoppingIntentVO {

    /**
     * 原始输入
     */
    private String rawQuery;

    /**
     * 品类关键词，例如：咖啡、轻食、热食
     */
    private String categoryKeyword;

    /**
     * 商品关键词，例如：美式、三明治、关东煮
     */
    private String productKeyword;

    /**
     * 场景关键词，例如：早餐、夜宵、加班
     */
    private String sceneKeyword;

    /**
     * 口味偏好，例如：不辣、清淡
     */
    private String tastePreference;

    /**
     * 预算上限
     */
    private Integer budget;
}