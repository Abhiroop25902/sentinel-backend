package com.abhiroop.sentinel.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import lombok.Builder;

@Builder(toBuilder = true)
@Document(collectionName = "login_history")
public record LoginHistory(
        @DocumentId
        String id,
        String email,
        boolean success,
        long timestamp
) {
}
