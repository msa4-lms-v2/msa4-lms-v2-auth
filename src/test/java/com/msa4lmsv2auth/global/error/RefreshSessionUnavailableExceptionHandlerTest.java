package com.msa4lmsv2auth.global.error;

import com.msa4lmsv2auth.global.error.custom.business.RefreshSessionUnavailableException;
import com.msa4lmsv2auth.global.response.GlobalResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshSessionUnavailableExceptionHandlerTest {

    @Test
    void returnsServiceUnavailableContract() {
        ResponseEntity<GlobalResponseDTO<Void>> response =
                new RefreshSessionUnavailableExceptionHandler().handle(
                        new RefreshSessionUnavailableException("redis down")
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("E90");
    }
}
