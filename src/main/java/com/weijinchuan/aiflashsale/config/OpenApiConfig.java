package com.weijinchuan.aiflashsale.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 配置类
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mallOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Flash Sale API")
                        .description("AI 即时零售系统接口文档")
                        .version("1.0.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("project docs"));
    }
}