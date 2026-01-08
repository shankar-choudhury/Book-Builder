package com.shankar.book_builder.auth.security.ratelimiter.engine;

import io.github.bucket4j.Bandwidth;

public interface RateLimitingEngine {

    RateLimitingDecision evaluate(
            String policy,
            String key,
            Bandwidth bandwidth
    );
}
