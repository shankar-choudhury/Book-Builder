package com.shankar.book_builder.auth.security.ratelimiter.strategy;

import com.shankar.book_builder.auth.security.config.AppSecurityProperties;
import io.github.bucket4j.Bandwidth;
import jakarta.servlet.http.HttpServletRequest;

public interface RateLimitStrategy {
    String resolveKey(HttpServletRequest request);
    Bandwidth resolveBandwidth(HttpServletRequest request, AppSecurityProperties props);
    boolean matches(HttpServletRequest request);
    String getName();
}
