package com.ridehailing.auth.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenDenylistService {

    private static final Logger log = LoggerFactory.getLogger(TokenDenylistService.class);
    private static final String PREFIX = "denylist:jti:";

    private final RedisTemplate<String, String> redisTemplate;

    public TokenDenylistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void revoke(String jti, Duration ttl) {
        if (jti == null || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redisTemplate.opsForValue().set(PREFIX + jti, "1", ttl);
    }

    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
        } catch (Exception e) {
            log.warn("Token denylist check failed, treating token as not revoked: {}", e.getMessage());
            return false;
        }
    }
}
