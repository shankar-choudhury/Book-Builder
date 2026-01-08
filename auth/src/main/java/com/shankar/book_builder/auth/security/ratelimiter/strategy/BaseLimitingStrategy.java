package com.shankar.book_builder.auth.security.ratelimiter.strategy;

import com.shankar.book_builder.auth.security.config.AppSecurityProperties;
import io.github.bucket4j.Bandwidth;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

public abstract class BaseLimitingStrategy implements RateLimitStrategy {
    protected Bandwidth createBandwidth(int tokens, AppSecurityProperties props) {
        return Bandwidth.builder()
                .capacity(tokens)
                .refillGreedy(tokens, Duration.ofSeconds(props.getRateLimiter().getWindowSeconds()))
                .build();
    }

    protected Optional<String> getBody(HttpServletRequest request) {
        return Optional.of(request)
                .filter(ContentCachingRequestWrapper.class::isInstance)
                .map(ContentCachingRequestWrapper.class::cast)
                .map(w -> new String(w.getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    protected String getIp(HttpServletRequest request) {
        return Optional.ofNullable(request.getRemoteAddr()).orElse("unknown-ip");
    }
}
