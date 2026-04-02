package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户偏好画像实体
 */
@Data
@TableName("user_preference_profile")
public class UserPreferenceProfile {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String preferredCategoryKeyword;
    private String preferredProductKeyword;
    private String preferredSceneKeyword;
    private String preferredTastePreference;
    private Integer preferredBudget;
    private String lastIntentJson;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
