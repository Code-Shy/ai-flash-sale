package com.weijinchuan.aiflashsale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.domain.UserPreferenceProfile;
import com.weijinchuan.aiflashsale.mapper.UserPreferenceProfileMapper;
import com.weijinchuan.aiflashsale.service.UserPreferenceService;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户偏好画像服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private static final int STATUS_ACTIVE = 1;

    private final UserPreferenceProfileMapper userPreferenceProfileMapper;
    private final ObjectMapper objectMapper;

    @Override
    public UserPreferenceProfile loadOrCreateProfile(Long userId) {
        UserPreferenceProfile profile = userPreferenceProfileMapper.selectOne(
                new LambdaQueryWrapper<UserPreferenceProfile>()
                        .eq(UserPreferenceProfile::getUserId, userId)
                        .eq(UserPreferenceProfile::getStatus, STATUS_ACTIVE)
                        .last("LIMIT 1")
        );
        if (profile != null) {
            return profile;
        }

        UserPreferenceProfile created = new UserPreferenceProfile();
        created.setUserId(userId);
        created.setStatus(STATUS_ACTIVE);
        userPreferenceProfileMapper.insert(created);
        return created;
    }

    @Override
    public ShoppingIntentVO buildPreferenceIntent(UserPreferenceProfile profile) {
        ShoppingIntentVO intent = new ShoppingIntentVO();
        if (profile == null) {
            return intent;
        }
        intent.setCategoryKeyword(profile.getPreferredCategoryKeyword());
        intent.setProductKeyword(profile.getPreferredProductKeyword());
        intent.setSceneKeyword(profile.getPreferredSceneKeyword());
        intent.setTastePreference(profile.getPreferredTastePreference());
        intent.setBudget(profile.getPreferredBudget());
        return intent;
    }

    @Override
    public UserPreferenceProfile mergeAndSaveProfile(Long userId, ShoppingIntentVO intent) {
        UserPreferenceProfile profile = loadOrCreateProfile(userId);
        if (intent != null) {
            if (!isBlank(intent.getCategoryKeyword())) {
                profile.setPreferredCategoryKeyword(intent.getCategoryKeyword());
            }
            if (!isBlank(intent.getProductKeyword())) {
                profile.setPreferredProductKeyword(intent.getProductKeyword());
            }
            if (!isBlank(intent.getSceneKeyword())) {
                profile.setPreferredSceneKeyword(intent.getSceneKeyword());
            }
            if (!isBlank(intent.getTastePreference())) {
                profile.setPreferredTastePreference(intent.getTastePreference());
            }
            if (intent.getBudget() != null && intent.getBudget() > 0) {
                profile.setPreferredBudget(intent.getBudget());
            }
            profile.setLastIntentJson(writeIntent(intent));
        }
        userPreferenceProfileMapper.updateById(profile);
        return profile;
    }

    @Override
    public List<String> buildPreferenceHints(UserPreferenceProfile profile) {
        if (profile == null) {
            return List.of();
        }

        List<String> hints = new ArrayList<>();
        if (!isBlank(profile.getPreferredCategoryKeyword())) {
            hints.add("偏好品类：" + profile.getPreferredCategoryKeyword());
        }
        if (!isBlank(profile.getPreferredProductKeyword())) {
            hints.add("常选商品：" + profile.getPreferredProductKeyword());
        }
        if (!isBlank(profile.getPreferredSceneKeyword())) {
            hints.add("常用场景：" + profile.getPreferredSceneKeyword());
        }
        if (!isBlank(profile.getPreferredTastePreference())) {
            hints.add("口味偏好：" + profile.getPreferredTastePreference());
        }
        if (profile.getPreferredBudget() != null && profile.getPreferredBudget() > 0) {
            hints.add("预算参考：" + profile.getPreferredBudget() + " 元以内");
        }
        return hints;
    }

    private String writeIntent(ShoppingIntentVO intent) {
        if (intent == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(intent);
        } catch (Exception e) {
            log.warn("序列化用户偏好画像意图失败", e);
            return null;
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
