package com.shankar.book_builder.auth.security.ratelimiter.engine;

public sealed interface RateLimitingDecision
        permits RateLimitingDecision.Allowed, RateLimitingDecision.Rejected {
    record Allowed(long remainingTokens) implements RateLimitingDecision {}

    record Rejected(long retryAfterSeconds) implements RateLimitingDecision {}
}
