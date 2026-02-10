package com.abhiroop.sentinel.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import lombok.Builder;

import java.time.Instant;

@Document(collectionName = "stress_test_config")
@Builder(toBuilder = true)
public record StressTestConfig(
        @DocumentId
        String id,
        boolean isRunning,
        Instant earliestNextRun
) {
}
