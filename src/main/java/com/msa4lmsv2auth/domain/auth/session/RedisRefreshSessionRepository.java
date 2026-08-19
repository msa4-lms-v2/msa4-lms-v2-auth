package com.msa4lmsv2auth.domain.auth.session;

import com.msa4lmsv2auth.global.security.constant.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisRefreshSessionRepository implements RefreshSessionRepository {

    private static final String KEY_PREFIX = "auth:refresh:";
    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('HGET', KEYS[1], 'jtiHash') ~= ARGV[1] then
                return 0
            end
            redis.call('HSET', KEYS[1],
                'jtiHash', ARGV[2],
                'loginId', ARGV[3],
                'role', ARGV[4],
                'requiresPasswordChange', ARGV[5],
                'accountCreatedAt', ARGV[6],
                'lastDbVerifiedAt', ARGV[7],
                'expiresAt', ARGV[8])
            redis.call('EXPIRE', KEYS[1], ARGV[9])
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    @Override
    public Optional<RefreshSession> findByUserId(long userId) {
        Map<Object, Object> values = redisTemplate.opsForHash().entries(key(userId));
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new RefreshSession(
                userId,
                required(values, "jtiHash"),
                required(values, "loginId"),
                Role.valueOf(required(values, "role")),
                Boolean.parseBoolean(required(values, "requiresPasswordChange")),
                LocalDateTime.parse(required(values, "accountCreatedAt")),
                Instant.parse(required(values, "lastDbVerifiedAt")),
                Instant.parse(required(values, "expiresAt"))
        ));
    }

    @Override
    public void save(RefreshSession session) {
        String key = key(session.userId());
        redisTemplate.opsForHash().putAll(key, values(session));
        redisTemplate.expire(key, ttl(session));
    }

    @Override
    public boolean rotate(String expectedJtiHash, RefreshSession session) {
        Long result = redisTemplate.execute(
                ROTATE_SCRIPT,
                List.of(key(session.userId())),
                expectedJtiHash,
                session.jtiHash(),
                session.loginId(),
                session.role().name(),
                Boolean.toString(session.requiresPasswordChange()),
                session.accountCreatedAt().toString(),
                session.lastDbVerifiedAt().toString(),
                session.expiresAt().toString(),
                Long.toString(ttl(session).toSeconds())
        );
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void deleteByUserId(long userId) {
        redisTemplate.delete(key(userId));
    }

    private Map<String, String> values(RefreshSession session) {
        return Map.of(
                "jtiHash", session.jtiHash(),
                "loginId", session.loginId(),
                "role", session.role().name(),
                "requiresPasswordChange", Boolean.toString(session.requiresPasswordChange()),
                "accountCreatedAt", session.accountCreatedAt().toString(),
                "lastDbVerifiedAt", session.lastDbVerifiedAt().toString(),
                "expiresAt", session.expiresAt().toString()
        );
    }

    private Duration ttl(RefreshSession session) {
        Duration ttl = Duration.between(Instant.now(), session.expiresAt());
        return ttl.isNegative() || ttl.isZero() ? Duration.ofSeconds(1) : ttl;
    }

    private String required(Map<Object, Object> values, String key) {
        Object value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("Refresh Session 메타데이터가 누락되었습니다: " + key);
        }
        return value.toString();
    }

    private String key(long userId) {
        return KEY_PREFIX + userId;
    }
}
