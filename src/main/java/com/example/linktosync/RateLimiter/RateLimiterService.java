package com.example.linktosync.RateLimiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    // Use a ConcurrentHashMap to store buckets for each user or IP address.
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Configure a bucket with desired limits.
    public Bucket createBucket() {
        // Define the limit: 100 requests per minute with instant refills.
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    // Get a bucket for the user/IP, creating one if it doesn't exist.
    public Bucket resolveBucket(String userIdentifier) {
        return buckets.computeIfAbsent(userIdentifier, key -> createBucket());
    }

    // Check if a user can proceed with their request.
    public boolean tryConsume(String userIdentifier) {
        Bucket bucket = resolveBucket(userIdentifier);
        return bucket.tryConsume(1);  // Consume 1 token per request.
    }
}
