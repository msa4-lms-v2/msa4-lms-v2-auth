package com.msa4lmsv2auth.global.outbox;

import com.msa4lmsv2auth.domain.account.client.AcademicClient;
import com.msa4lmsv2auth.domain.account.client.AcademicProvisioningRejectedException;
import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.domain.account.repository.AccountRepository;
import com.msa4lmsv2auth.domain.account.request.ProfessorProvisioningRequestDTO;
import com.msa4lmsv2auth.domain.account.request.StudentProvisioningRequestDTO;
import com.msa4lmsv2auth.domain.account.response.ProfessorProvisioningResponseDTO;
import com.msa4lmsv2auth.domain.account.response.StudentProvisioningResponseDTO;
import com.msa4lmsv2auth.domain.outbox.constant.AccountSyncEventType;
import com.msa4lmsv2auth.domain.outbox.entity.AccountSyncOutbox;
import com.msa4lmsv2auth.domain.outbox.repository.AccountSyncOutboxRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountSyncOutboxBatchProcessor {

    private static final int BATCH_SIZE = 20;
    private static final int LEASE_SECONDS = 30;
    // 계정 프로비저닝은 Academic의 Kafka 발행보다 실패 영향이 크므로(로그인 자체가 막힘) 짧게 포기하지 않는다.
    // 지수 백오프로 늘어나다 15분 간격에서 유지하고, 그래도 안 끝나면 24시간 초과 정책(expireStaleEntries)이 종결한다.
    private static final long[] BACKOFF_SECONDS = {5, 15, 60, 300, 900};
    private static final String WORKER_ID = "auth-outbox-worker";

    private final AccountSyncOutboxRepository accountSyncOutboxRepository;
    private final AccountRepository accountRepository;
    private final AcademicClient academicClient;

    // PENDING/PROCESSING으로 남아 staleAfter(기본 24시간)를 넘긴 행을 MANUAL_REVIEW_REQUIRED로 종결한다.
    // ERD의 account_sync_outbox.status에는 FAILED가 없어(PENDING/PROCESSING/COMPLETED/MANUAL_REVIEW_REQUIRED 4개뿐),
    // "PENDING이 24시간 넘으면 FAILED로 전환"(MSA-LMS_ARCHITECTURE.md 175행) 정책은 이 상태로 종결하는 것으로 반영한다.
    @Transactional
    public void expireStaleEntries(Duration staleAfter) {
        LocalDateTime staleBefore = LocalDateTime.now().minus(staleAfter);
        List<AccountSyncOutbox> stale = accountSyncOutboxRepository.lockStaleBatch(staleBefore, BATCH_SIZE);
        for (AccountSyncOutbox event : stale) {
            event.giveUp("STALE_PENDING_OVER_24H");
            log.error("계정 프로비저닝 outbox가 {}을 넘겨도 완료되지 않아 수동 검토가 필요합니다 (id={}, aggregateId={})",
                    staleAfter, event.getId(), event.getAggregateId());
        }
    }

    @Transactional
    public void publishPendingBatch() {
        LocalDateTime now = LocalDateTime.now();
        List<AccountSyncOutbox> batch = accountSyncOutboxRepository.lockNextBatch(now, BATCH_SIZE);
        for (AccountSyncOutbox event : batch) {
            event.lock(WORKER_ID, now.plusSeconds(LEASE_SECONDS));
            process(event, now);
        }
    }

    private void process(AccountSyncOutbox event, LocalDateTime now) {
        Account account = accountRepository.findById(event.getAggregateId()).orElse(null);
        if (account == null) {
            event.giveUp("ACCOUNT_NOT_FOUND");
            log.error("outbox aggregate_id에 해당하는 계정을 찾을 수 없어 수동 검토가 필요합니다 (id={}, aggregateId={})",
                    event.getId(), event.getAggregateId());
            return;
        }

        try {
            String loginId = provision(event);
            account.activateWithLoginId(loginId);
            event.complete(now);
        } catch (AcademicProvisioningRejectedException exception) {
            // Academic이 4xx로 거부한 경우(중복 이메일 등)는 재시도해도 결과가 바뀌지 않는다.
            event.giveUp("ACADEMIC_REJECTED");
            log.error("Academic이 계정 프로비저닝을 거부해 수동 검토가 필요합니다 (id={}, aggregateId={})",
                    event.getId(), event.getAggregateId(), exception);
        } catch (IllegalArgumentException exception) {
            // 알 수 없는 event_type 등 payload 계약 위반 - 재시도로 해결되지 않는다.
            event.giveUp("INVALID_PAYLOAD");
            log.error("outbox payload가 올바르지 않아 수동 검토가 필요합니다 (id={}, aggregateId={})",
                    event.getId(), event.getAggregateId(), exception);
        } catch (RestClientException | IllegalStateException exception) {
            // 연결 실패, timeout, 5xx, 빈 응답 등 - 일시적 실패로 보고 재시도한다.
            retryLater(event, now, exception);
        }
    }

    private String provision(AccountSyncOutbox event) {
        Map<String, Object> payload = event.getPayload();
        Long userId = asLong(payload.get("userId"));
        String name = (String) payload.get("name");
        String email = (String) payload.get("email");
        String phoneNumber = (String) payload.get("phoneNumber");
        String address = (String) payload.get("address");
        Long departmentId = asLong(payload.get("departmentId"));

        return switch (event.getEventType()) {
            case AccountSyncEventType.STUDENT_PROVISIONING_REQUESTED -> {
                StudentProvisioningResponseDTO response = academicClient.createStudent(
                        new StudentProvisioningRequestDTO(
                                userId, name, email, phoneNumber, address,
                                departmentId, asShort(payload.get("admissionYear"))
                        )
                );
                yield response.loginId();
            }
            case AccountSyncEventType.PROFESSOR_PROVISIONING_REQUESTED -> {
                ProfessorProvisioningResponseDTO response = academicClient.createProfessor(
                        new ProfessorProvisioningRequestDTO(
                                userId, name, email, phoneNumber, address,
                                departmentId, asShort(payload.get("hireYear"))
                        )
                );
                yield response.loginId();
            }
            default -> throw new IllegalArgumentException("알 수 없는 outbox event_type: " + event.getEventType());
        };
    }

    private void retryLater(AccountSyncOutbox event, LocalDateTime now, Exception exception) {
        long backoff = BACKOFF_SECONDS[Math.min(event.getAttempts(), BACKOFF_SECONDS.length - 1)];
        event.retryLater(now.plusSeconds(backoff), "ACADEMIC_CALL_FAILED");
        log.warn("Academic 프로비저닝 호출 실패, {}초 후 재시도 (id={}, attempts={})",
                backoff, event.getId(), event.getAttempts(), exception);
    }

    private Long asLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private Short asShort(Object value) {
        return value == null ? null : ((Number) value).shortValue();
    }
}
