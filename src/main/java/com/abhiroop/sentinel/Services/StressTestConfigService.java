package com.abhiroop.sentinel.Services;

import com.abhiroop.sentinel.Repository.StressTestConfigRepository;
import com.abhiroop.sentinel.entity.StressTestConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
@AllArgsConstructor
public class StressTestConfigService {
    private final StressTestConfigRepository stressTestConfigRepository;

    public StressTestConfig getConfig() {
        String configId = "global_config";
        return stressTestConfigRepository
                .findById(configId)
                .switchIfEmpty(
                        Mono.just(
                                StressTestConfig
                                        .builder()
                                        .id(configId)
                                        .earliestNextRun(Instant.now().minus(Duration.ofDays(1)))
                                        .isRunning(false)
                                        .build()
                        )
                )
                .block();
    }

    public StressTestConfig saveConfig(StressTestConfig config) {
        return stressTestConfigRepository.save(config).block();
    }

    public void setIsRunningFalse() {
        final var config = getConfig();

        if (!config.isRunning()) return;

        final var updatedConfig = config.toBuilder().isRunning(false).build();

        saveConfig(updatedConfig);
    }

}
