package com.msa4lmsv2auth.domain.account.entity;

import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.global.security.constant.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "accounts")
@SQLDelete(sql = "UPDATE accounts SET login_id = CONCAT(login_id, '#deleted#', id), deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT UNSIGNED")
    private long id;

    @Column(name = "login_id", unique = true, length = 150)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(Types.VARCHAR)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(Types.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.PENDING_PROVISIONING;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "requires_password_change", nullable = false)
    private boolean requiresPasswordChange;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    // 실패 횟수 증가, 잠금 처리
    public void increaseFailedLoginAttempts() {
        this.failedLoginAttempts++;

        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.status = AccountStatus.LOCKED;
            this.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
        }
    }

    // 잠긴 계정인지 확인
    public boolean isLocked() {
        return this.status == AccountStatus.LOCKED;
    }

    // 잠긴 시간이 끝났는지 확인 / 현재시간 >= lockedUntil
    public boolean isLockExpired() {
        return this.isLocked()
                && this.lockedUntil != null
                && !LocalDateTime.now().isBefore(this.lockedUntil);
    }

    // 잠금 해제
    public void unlock() {
        this.status = AccountStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    // 로그인 성공 시 실패 횟수 초기화
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }
}
