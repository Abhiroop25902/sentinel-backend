package com.abhiroop.sentinel.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record StressTestSummary(
        Instant startTime,
        Instant endTime,
        long recordsCreated
) {
}
