-- 2026-09-07: 계정 프로비저닝 Outbox 패턴 도입
-- account_sync_outbox 테이블(ERD 확정, DDL 미구현이었음)을 추가한다.
-- 계정 생성 트랜잭션에서 Outbox 이벤트를 함께 기록하고, Auth Pod 내부 Worker가
-- lease + SKIP LOCKED로 중복 선점 없이 Academic 프로필 생성을 재시도한다.

CREATE TABLE IF NOT EXISTS account_sync_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id CHAR(36) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSON NOT NULL,
    source_version BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME NOT NULL,
    locked_by VARCHAR(100) NULL,
    locked_until DATETIME NULL,
    last_error_code VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_account_sync_outbox_event_id UNIQUE (event_id),
    INDEX idx_account_sync_outbox_status_next_attempt (status, next_attempt_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
