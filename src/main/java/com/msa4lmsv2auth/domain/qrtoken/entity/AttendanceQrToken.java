package com.msa4lmsv2auth.domain.qrtoken.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_qr_tokens")
@Getter
@Setter
public class AttendanceQrToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT UNSIGNED")
    private long id;

    // Academic 서비스의 attendance_sessions.id를 참조하며 DB FK는 사용하지 않는다.
    @Column(name = "session_id", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private long sessionId;

    @Column(name = "token", unique = true, nullable = false, length = 512)
    private String token;

    @Column(name = "nonce", nullable = false, length = 100)
    private String nonce;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
