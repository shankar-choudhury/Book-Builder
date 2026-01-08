package com.shankar.book_builder.auth.security.ratelimiter;

import com.shankar.book_builder.auth.security.config.AppSecurityProperties;
import com.shankar.book_builder.auth.security.ratelimiter.engine.Bucket4jRateLimitingEngine;
import com.shankar.book_builder.auth.security.ratelimiter.engine.RateLimitingDecision;
import com.shankar.book_builder.auth.security.ratelimiter.strategy.RateLimitStrategy;
import io.github.bucket4j.Bandwidth;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final AppSecurityProperties props;
    private final ObjectMapper om;
    private final Bucket4jRateLimitingEngine engine;
    private final List<RateLimitStrategy> strategies;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        strategies.stream()
                .filter(strategy -> strategy.matches(request))
                .findFirst()
                .ifPresentOrElse(
                        strategy -> handleRateLimiting(request, response, filterChain, strategy),
                        () -> uncheckedFilter(request, response, filterChain)
                );
    }

    private void handleRateLimiting(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            RateLimitStrategy strategy
    ) {
        try {
            String key = strategy.resolveKey(request);
            Bandwidth limit = strategy.resolveBandwidth(request, props);

            RateLimitingDecision decision = engine.evaluate(strategy.getName(), key, limit);

            if (decision instanceof RateLimitingDecision.Allowed(long remainingTokens)) {
                response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
                filterChain.doFilter(request, response);
            } else if (decision instanceof RateLimitingDecision.Rejected(long retryAfterSeconds)) {
                writeProblemDetails(response, retryAfterSeconds, strategy.getName());
            }
        } catch (Exception e) {
            // Fallback: if rate limiting logic fails, allow the request but log the error
            uncheckedFilter(request, response, filterChain);
        }
    }

    private void uncheckedFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw new RuntimeException("Filter chain execution failed", e);
        }
    }

    @Override
    @NullMarked
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return strategies.stream().noneMatch(s -> s.matches(request));
    }

    private void writeProblemDetails(HttpServletResponse response, long retryAfterSeconds, String policy) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/problem+json");

        om.writeValue(response.getWriter(), Map.of(
                "type", "https://api.example.com/problems/rate-limit-exceeded",
                "title", "Too Many Requests",
                "status", 429,
                "detail", "Rate limit exceeded for policy '" + policy + "'",
                "instance", UUID.randomUUID().toString(),
                "retryAfterSeconds", retryAfterSeconds,
                "timestamp", Instant.now().toString()
        ));
    }
}