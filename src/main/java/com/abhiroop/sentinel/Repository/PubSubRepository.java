package com.abhiroop.sentinel.Repository;

import com.abhiroop.sentinel.dto.PubSubMessageData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
abstract class PubSubRepository<T> {
    private final ObjectMapper objectMapper;
    private final Publisher publisher;

    /**
     * publisher instantiation happens via concrete class
     * cleanup happens here cause the code is same for all the different PubSub Repo
     */
    @PreDestroy
    public void destroy() throws InterruptedException {
        if (publisher != null) {
            publisher.shutdown();
            publisher.awaitTermination(1, TimeUnit.MINUTES);
        }
    }


    //will return the messageId for the pubsub message
    protected Mono<String> saveToPubSub(T obj) {
        return Mono.defer(() -> {
            try {
                //get data type -> syncing it with topic name for 1 to 1 mapping
                final String dataType = publisher.getTopicNameString();

                //create the data
                final PubSubMessageData<T> pubsubMessageData = PubSubMessageData.<T>builder()
                        .type(dataType)
                        .data(obj)
                        .build();
                final String json = objectMapper.writeValueAsString(pubsubMessageData);
                final ByteString data = ByteString.copyFromUtf8(json);
                final PubsubMessage message = PubsubMessage.newBuilder().setData(data).build();

                return Mono.create(sink -> {
                    ApiFuture<String> apiFuture = publisher.publish(message);
                    ApiFutures.addCallback(apiFuture, new ApiFutureCallback<>() {
                        @Override
                        public void onFailure(Throwable t) {
                            sink.error(t);
                        }

                        @Override
                        public void onSuccess(String messageId) {
                            sink.success(messageId);
                        }
                    }, MoreExecutors.directExecutor());

                    // Optional: Handle cancellation
                    sink.onCancel(() -> apiFuture.cancel(true));
                });
            } catch (JsonProcessingException e) {
                return Mono.error(new RuntimeException("Mapping error", e));
            }
        });
    }

}
