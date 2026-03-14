package com.abhiroop.sentinel.Configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FirebaseConfig {
    private final GcpProjectIdProvider projectIdProvider;

    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setProjectId(projectIdProvider.getProjectId())
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase App initialized for project: {}", projectIdProvider.getProjectId());
        }
    }
}
