package com.weijinchuan.aiflashsale.config;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付配置
 */
@Configuration
@EnableConfigurationProperties(WechatPayProperties.class)
public class WechatPayConfig {

    @Bean
    @ConditionalOnProperty(prefix = "wechat.pay", name = "enabled", havingValue = "true")
    public RSAAutoCertificateConfig wechatPayAutoCertificateConfig(WechatPayProperties properties) {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(properties.getMerchantId())
                .privateKeyFromPath(properties.getPrivateKeyPath())
                .merchantSerialNumber(properties.getMerchantSerialNumber())
                .apiV3Key(properties.getApiV3Key())
                .build();
    }

    @Bean
    @ConditionalOnBean(RSAAutoCertificateConfig.class)
    public NativePayService nativePayService(RSAAutoCertificateConfig config) {
        return new NativePayService.Builder()
                .config(config)
                .build();
    }

    @Bean
    @ConditionalOnBean(RSAAutoCertificateConfig.class)
    public NotificationParser notificationParser(RSAAutoCertificateConfig config) {
        return new NotificationParser(config);
    }
}
