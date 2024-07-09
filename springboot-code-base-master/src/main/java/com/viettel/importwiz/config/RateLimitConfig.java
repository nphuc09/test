package com.viettel.importwiz.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Configuration
public class RateLimitConfig {

    @Bean
    public Map<String, Bucket> userBuckets() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Bucket createBucketForUser() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(2, Duration.ofSeconds(1))))
            .build();
    }
}
