package com.weijinchuan.aiflashsale.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信支付配置
 */
@Data
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayProperties {

    /**
     * 是否启用微信支付
     */
    private boolean enabled;

    private String appId;
    private String merchantId;
    private String merchantSerialNumber;
    private String apiV3Key;
    private String privateKeyPath;
    private String notifyUrl;
    private String descriptionPrefix;
}
