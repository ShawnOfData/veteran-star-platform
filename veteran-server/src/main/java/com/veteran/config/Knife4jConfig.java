package com.veteran.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("退役大学生信息档案与积分管理系统 API")
                        .version("1.0.0")
                        .description("戎归·星辉 后端接口文档")
                        .contact(new Contact()
                                .name("开发团队")));
    }
}