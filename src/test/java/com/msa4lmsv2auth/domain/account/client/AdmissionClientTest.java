package com.msa4lmsv2auth.domain.account.client;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AdmissionClientTest {
    private HttpServer server;
    private AdmissionClient client;
    private final AtomicReference<String> receivedToken = new AtomicReference<>();
    private String responseBody;
    private int responseStatus;

    @BeforeEach
    void startAcademicStub() throws Exception {
        responseStatus = 200;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/academic/internal/admissions/7", exchange -> {
            receivedToken.set(exchange.getRequestHeaders().getFirst("X-Admission-Token"));
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, bytes.length);
            try (var output = exchange.getResponseBody()) { output.write(bytes); }
        });
        server.start();
        client = new AdmissionClient("http://127.0.0.1:" + server.getAddress().getPort());
    }

    @AfterEach
    void stopAcademicStub() { server.stop(0); }

    @Test
    void confirmedPaymentWorksWithoutServiceToken() {
        responseBody = "{\"data\":{\"tuitionPaid\":true,\"status\":\"PENDING\"}}";
        assertThatCode(() -> client.requirePaid(7L)).doesNotThrowAnyException();
        assertThat(receivedToken.get()).isNull();
    }

    @Test
    void unpaidCancelledOrMissingPaymentCannotPassTheGuard() {
        for (String data : new String[]{
                "{\"tuitionPaid\":false,\"status\":\"PENDING\"}",
                "{\"tuitionPaid\":true,\"status\":\"CANCELLED\"}",
                "{\"status\":\"PENDING\"}", "null"}) {
            responseBody = "{\"data\":" + data + "}";
            assertThatThrownBy(() -> client.requirePaid(7L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("등록금 완납이 확인되지 않았습니다.");
        }
    }

    @Test
    void failedPaymentLookupDoesNotAuthorizeProvisioning() {
        responseStatus = 503;
        responseBody = "{\"message\":\"temporarily unavailable\"}";
        assertThatThrownBy(() -> client.requirePaid(7L))
                .isInstanceOf(org.springframework.web.client.HttpServerErrorException.class);
    }
}
