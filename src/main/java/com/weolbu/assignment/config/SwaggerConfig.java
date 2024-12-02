package com.weolbu.assignment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("최유정 월급쟁이 부자들 과제 테스트")
                        .version("1.0.0")
                        .description("과제테스트를 위해 구현한 API 문서입니다."));
    }
}