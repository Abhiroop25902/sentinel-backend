package com.abhiroop.sentinel.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import lombok.Builder;

import java.time.Instant;

@Builder
@Document(collectionName = "login_history")
public record LoginHistory(
        @DocumentId
        String id,
        String email,
        boolean success,
        Instant timestamp
) {
}
