package com.shankar.book_builder.auth.security.ratelimiter.engine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * <h3>Distributed Rate Limiting Migration Guide (Local Redis)</h3>
 *
 * This class currently uses in-memory storage (ConcurrentHashMap), which is
 * volatile and local to this JVM instance. To support horizontal scaling (e.g., AWS EC2 ASG),
 * this must be migrated to a distributed state provider like Redis.
 *
 * <pre>
 * 1. INSTALLATION & SETUP
 * - The easiest way to run Redis locally is via Docker:
 * 'docker run -d --name redis-ratelimiter -p 6379:6379 redis'
 * - Alternatively, install Redis for Windows/Mac/Linux from redis.io.
 *
 * 2. DEPENDENCIES (pom.xml)
 * - Add 'com.bucket4j:bucket4j-redis'
 * - Add 'io.lettuce:lettuce-core'
 *
 * 3. CONFIGURATION UPDATES
 * - UPDATE AppSecurityProperties: Add a nested static class 'Redis' with fields
 * 'host', 'port', and 'password'.
 * - UPDATE .env: Add the following keys:
 * APP_SECURITY_REDIS_HOST=localhost
 * APP_SECURITY_REDIS_PORT=6379
 * APP_SECURITY_REDIS_PASSWORD= (leave blank for local)
 *
 * 4. REFACTORING STEPS
 * - Replace 'Map<String, Bucket>' with 'ProxyManager<String>'.
 * - Inject 'RedisClient' and initialize 'LettuceBasedProxyManager' in the constructor.
 * - Create a @Configuration class to provide the 'RedisClient' bean using values
 * from AppSecurityProperties.
 * - Use 'proxyManager.builder().build(key, config)' in the evaluate method.
 *
 * 5. LAPTOP QUICK START CHECKLIST (For First-Time Users)
 * - PORT: Redis defaults to 6379. Ensure no other service is using it.
 * - PERSISTENCE: Local Redis stores data in RAM; restarting the container
 * usually clears active rate-limit buckets.
 * - NO-AUTH: By default, local Redis has no password. Ensure your RedisConfig
 * handles null/blank passwords gracefully.
 * - SERVER STATUS: Run 'docker ps' to ensure the 'redis-ratelimiter' container is Up.
 *
 * 6. HOW TO VERIFY ON YOUR MACHINE
 * - Open a terminal and run: 'docker exec -it redis-ratelimiter redis-cli'
 * - Once in the CLI, type: 'KEYS *'
 * - After making an API request, you should see a key like "login:127.0.0.1".
 * - Type 'GET "your:key:name"' to see the binary bucket state.
 * </pre>
 *
 * @see <a href="https://bucket4j.com/docs/8.10.1/toc.html#distributed-environments">Bucket4j Distributed Docs</a>
 */

@Component
public class Bucket4jRateLimitingEngine implements RateLimitingEngine {

    private final ProxyManager<String> proxyManager;

    public Bucket4jRateLimitingEngine(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    public RateLimitingDecision evaluate(
            String policy,
            String key,
            Bandwidth bandwidth
    ) {
        String bucketKey = policy + ":" + key;

        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();

        ConsumptionProbe probe = proxyManager.builder()
                .build(bucketKey, configSupplier)
                .tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            return new RateLimitingDecision.Allowed(probe.getRemainingTokens());
        }

        long retryAfterSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000L);

        return new RateLimitingDecision.Rejected(retryAfterSeconds);
    }
}
