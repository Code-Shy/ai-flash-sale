package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.domain.UserPreferenceProfile;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;

import java.util.List;

/**
 * 用户偏好画像服务
 */
public interface UserPreferenceService {

    /**
     * 获取或初始化用户偏好画像。
     */
    UserPreferenceProfile loadOrCreateProfile(Long userId);

    /**
     * 从偏好画像中恢复默认意图。
     */
    ShoppingIntentVO buildPreferenceIntent(UserPreferenceProfile profile);

    /**
     * 合并本轮意图并落库。
     */
    UserPreferenceProfile mergeAndSaveProfile(Long userId, ShoppingIntentVO intent);

    /**
     * 输出可读的偏好摘要。
     */
    List<String> buildPreferenceHints(UserPreferenceProfile profile);
}
