package com.abhiroop.sentinel.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Builder;

@Builder(toBuilder = true)
public record LoginHistory(
        @DocumentId
        String id,
        String email,
        boolean success,
        long timestamp
) {
}
