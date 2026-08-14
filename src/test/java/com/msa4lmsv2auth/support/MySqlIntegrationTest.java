package com.msa4lmsv2auth.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public abstract class MySqlIntegrationTest {

    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("lms_auth")
            .withUsername("auth")
            .withPassword("auth");
    private static final KeyPair JWT_KEY_PAIR = generateRsaKeyPair();

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        // 운영용 dummy SQL이 통합 테스트 컨테이너의 데이터를 변경하지 않도록 비활성화한다.
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        registry.add("server.port", () -> "0");
        registry.add("springdoc.api-docs.enabled", () -> "true");
        registry.add("springdoc.api-docs.path", () -> "/api-docs");
        registry.add("springdoc.swagger-ui.enabled", () -> "false");
        registry.add("springdoc.open-api.servers[0].url", () -> "http://localhost");
        registry.add("springdoc.open-api.servers[0].description", () -> "Auth integration test");

        registry.add("jwt.kid", () -> "auth-integration-test");
        registry.add("jwt.private-key-b64", () -> encodePem(
                "PRIVATE KEY",
                JWT_KEY_PAIR.getPrivate().getEncoded()
        ));
        registry.add("jwt.public-key-b64", () -> encodePem(
                "PUBLIC KEY",
                JWT_KEY_PAIR.getPublic().getEncoded()
        ));

        registry.add("file.server-uri", () -> "http://localhost/files");
        registry.add("file.storage-path", () -> "build/test-files");
    }

    private static KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("테스트용 RSA 키 생성에 실패했습니다.", e);
        }
    }

    private static String encodePem(String type, byte[] keyBytes) {
        String pem = "-----BEGIN " + type + "-----\n"
                + Base64.getEncoder().encodeToString(keyBytes)
                + "\n-----END " + type + "-----\n";
        return Base64.getEncoder().encodeToString(pem.getBytes(StandardCharsets.UTF_8));
    }
}
