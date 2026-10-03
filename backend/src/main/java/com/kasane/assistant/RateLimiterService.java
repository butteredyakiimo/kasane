package com.kasane.assistant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory per-key token bucket. Fine for a single-instance deployment (no shared
 * cache/Redis needed); would need externalizing if the backend ever scales horizontally.
 */
@Service
public class RateLimiterService {

    private static final double CAPACITY = 10.0;
    private static final double REFILL_TOKENS_PER_SECOND = 10.0 / 60.0; // 10 requests/minute

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket());
        return bucket.tryConsume();
    }

    /**
     * Drops buckets that have been idle long enough to refill completely - they're
     * indistinguishable from a fresh bucket, so this changes no behaviour, it just
     * keeps the map from growing with every IP ever seen.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void evictIdleBuckets() {
        long now = System.nanoTime();
        buckets.values().removeIf(bucket -> bucket.isFullyRefilled(now));
    }

    private static class Bucket {
        private double tokens = CAPACITY;
        private long lastRefillNanos = System.nanoTime();

        synchronized boolean tryConsume() {
            long now = System.nanoTime();
            double elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000.0;
            tokens = Math.min(CAPACITY, tokens + elapsedSeconds * REFILL_TOKENS_PER_SECOND);
            lastRefillNanos = now;

            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        synchronized boolean isFullyRefilled(long now) {
            double elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000.0;
            return tokens + elapsedSeconds * REFILL_TOKENS_PER_SECOND >= CAPACITY;
        }
    }
}
