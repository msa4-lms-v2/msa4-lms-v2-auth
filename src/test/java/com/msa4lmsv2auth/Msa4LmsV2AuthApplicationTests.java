package com.msa4lmsv2auth;

import com.msa4lmsv2auth.support.MySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "GATEWAY_URI=http://localhost:8080")
class Msa4LmsV2AuthApplicationTests extends MySqlIntegrationTest {

    @Test
    void contextLoads() {
    }

}
