package com.abhiroop.sentinel.Repository;

import com.abhiroop.sentinel.entity.LoginHistory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Repository
public class LoginHistoryRepository {
    private final static String projectId = "sentinel-25902";
    private final static String pubSubTopicName = "login-history";

    private final ObjectMapper objectMapper;
    private Publisher publisher = null;

    @Autowired
    public LoginHistoryRepository(ObjectMapper objectMapper) throws IOException {
        this.objectMapper = objectMapper;

        this.publisher = Publisher.newBuilder(TopicName.of(projectId, pubSubTopicName)).build();
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        if (publisher != null) {
            publisher.shutdown();
            publisher.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    public Mono<LoginHistory> save(LoginHistory loginHistory) {
        return Mono.fromCallable(() -> {
                    //create the data
                    final String json = objectMapper.writeValueAsString(loginHistory);
                    final ByteString data = ByteString.copyFromUtf8(json);
                    return PubsubMessage.newBuilder().setData(data).build();
                }).flatMap(message ->
                        Mono.fromFuture(() -> {
                            final ApiFuture<String> apiFuture = publisher.publish(message);
                            final CompletableFuture<String> completableFuture = new CompletableFuture<>();
                            ApiFutures.addCallback(apiFuture, new ApiFutureCallback<>() {

                                        @Override
                                        public void onFailure(Throwable t) {
                                            completableFuture.completeExceptionally(t);
                                        }

                                        @Override
                                        public void onSuccess(String result) {
                                            completableFuture.complete(result);
                                        }
                                    },
                                    MoreExecutors.directExecutor()
                            );
                            return completableFuture;
                        })
                )
                .map(messageId -> loginHistory.toBuilder().id(messageId).build())
                // 3. Centralized Exception Handling
                .onErrorMap(JsonProcessingException.class, ex -> new RuntimeException("Mapping error", ex))
                .onErrorMap(IOException.class, ex -> new RuntimeException("Publisher error", ex));

    }
}
