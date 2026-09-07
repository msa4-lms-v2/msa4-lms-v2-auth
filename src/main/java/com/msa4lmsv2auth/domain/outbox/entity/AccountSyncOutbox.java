package com.msa4lmsv2auth.domain.outbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_sync_outbox")
public class AccountSyncOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false, length = 36)
    private String eventId;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    private Map<String, Object> payload;

    @Column(name = "source_version", nullable = false)
    private Long sourceVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountSyncOutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "locked_by", length = 100)
    private String lockedBy;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_error_code", length = 100)
    private String lastErrorCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private AccountSyncOutbox(String aggregateType, Long aggregateId, String eventType,
                              Map<String, Object> payload, Long sourceVersion) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.sourceVersion = sourceVersion;
        this.status = AccountSyncOutboxStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public static AccountSyncOutbox create(String aggregateType, Long aggregateId, String eventType,
                                           Map<String, Object> payload, Long sourceVersion) {
        return new AccountSyncOutbox(aggregateType, aggregateId, eventType, payload, sourceVersion);
    }

    public void lock(String workerId, LocalDateTime until) {
        this.status = AccountSyncOutboxStatus.PROCESSING;
        this.lockedBy = workerId;
        this.lockedUntil = until;
    }

    public void complete(LocalDateTime now) {
        this.status = AccountSyncOutboxStatus.COMPLETED;
        this.completedAt = now;
        this.lockedBy = null;
        this.lockedUntil = null;
    }

    public void retryLater(LocalDateTime nextAttemptAt, String errorCode) {
        this.status = AccountSyncOutboxStatus.PENDING;
        this.attempts++;
        this.nextAttemptAt = nextAttemptAt;
        this.lastErrorCode = errorCode;
        this.lockedBy = null;
        this.lockedUntil = null;
    }

    // ERD의 account_sync_outbox.status에는 FAILED 값이 없어(PENDING/PROCESSING/COMPLETED/MANUAL_REVIEW_REQUIRED 4개뿐),
    // 영구 실패·24시간 초과 등 더 이상 자동 재시도로 해결되지 않는 경우 전부 이 상태로 종결하고
    // last_error_code로 원인을 구분한다. 운영자는 resetForRetry()로 PENDING으로 되돌려 재실행할 수 있다.
    public void giveUp(String errorCode) {
        this.status = AccountSyncOutboxStatus.MANUAL_REVIEW_REQUIRED;
        this.lastErrorCode = errorCode;
        this.lockedBy = null;
        this.lockedUntil = null;
    }

    // 운영자가 원인을 해결한 뒤 수동으로 재실행할 때 사용한다.
    public void resetForRetry(LocalDateTime now) {
        this.status = AccountSyncOutboxStatus.PENDING;
        this.nextAttemptAt = now;
        this.lockedBy = null;
        this.lockedUntil = null;
    }
}
