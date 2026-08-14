package com.msa4lmsv2auth.global.error;

import com.msa4lmsv2auth.global.error.custom.business.RefreshSessionUnavailableException;
import com.msa4lmsv2auth.global.response.GlobalResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RefreshSessionUnavailableExceptionHandler {

    @ExceptionHandler(RefreshSessionUnavailableException.class)
    public ResponseEntity<GlobalResponseDTO<Void>> handle(RefreshSessionUnavailableException exception) {
        log.error("Refresh Session 저장소를 사용할 수 없습니다.", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new GlobalResponseDTO<Void>(
                        "E90",
                        "Refresh Session 저장소를 사용할 수 없습니다.",
                        null
                ));
    }
}
