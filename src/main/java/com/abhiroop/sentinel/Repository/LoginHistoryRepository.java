package com.abhiroop.sentinel.Repository;

import com.abhiroop.sentinel.entity.LoginHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class LoginHistoryRepository extends PubSubRepository<LoginHistory> {
    @Autowired
    public LoginHistoryRepository(ObjectMapper objectMapper, @Qualifier("LoginHistoryPubSubPublisher") Publisher publisher) {
        super(objectMapper, publisher);
    }

    public Mono<LoginHistory> save(LoginHistory loginHistory) {
        return super.saveToPubSub(loginHistory)
                .map(messageId -> loginHistory.toBuilder().id(messageId).build());
    }
}
