package com.abhiroop.sentinel.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record StressTestSummaryDto(
        Instant startTime,
        Instant endTime,
        long recordsCreated
) {
}
