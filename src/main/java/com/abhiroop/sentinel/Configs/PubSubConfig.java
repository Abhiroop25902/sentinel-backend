package com.abhiroop.sentinel.Configs;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.TopicName;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class PubSubConfig {
    private final static String projectId = "sentinel-25902";
    private final static String loginHistoryPubSubTopicName = "login-history";

    // destroyMethod = "" kept as this bean is tied to LoginHistoryRepo extends PubSubRepo
    // custom @PreDestroy for publisher defined there
    @Bean(name = "LoginHistoryPubSubPublisher", destroyMethod = "")
    public Publisher loginHistoryPublisher() throws IOException {
        return Publisher.newBuilder(TopicName.of(projectId, loginHistoryPubSubTopicName)).build();
    }
}
