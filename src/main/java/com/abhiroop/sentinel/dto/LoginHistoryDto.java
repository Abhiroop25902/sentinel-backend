package com.abhiroop.sentinel.dto;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Builder;

@Builder(toBuilder = true)
public record LoginHistoryDto(
        @DocumentId
        String id,
        String email,
        boolean success,
        long timestamp
) {
}
