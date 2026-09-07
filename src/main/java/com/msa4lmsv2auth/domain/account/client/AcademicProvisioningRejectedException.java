package com.msa4lmsv2auth.domain.account.client;

// Academic이 4xx로 프로비저닝 요청을 거부한 경우(중복 이메일, 검증 실패 등) 던진다.
// 같은 요청을 반복해도 결과가 바뀌지 않는 영구 실패이므로, Outbox Worker는 이 예외를 재시도 대상에서 제외하고
// 즉시 MANUAL_REVIEW_REQUIRED로 종결한다.
public class AcademicProvisioningRejectedException extends RuntimeException {
    public AcademicProvisioningRejectedException(String message) {
        super(message);
    }
}
