package com.msa4lmsv2auth.global.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_AUTH= "bearerAuth";


    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(
                    new Info()
                            .title("Lms Auth API") // 문서의 제목
                            .description("Lms Auth REST API Document") // 문서의 설명
                            .version("v1.0.0") // API문서의 버전
                ).components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("일반 API에는 Access Token을, 최초 비밀번호 변경 API에는 로그인 응답의 passwordChangeToken을 입력합니다.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
