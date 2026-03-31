package com.weijinchuan.aiflashsale;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.weijinchuan.aiflashsale.mapper")
@SpringBootApplication
public class AiFlashSaleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiFlashSaleApplication.class, args);
    }

}
