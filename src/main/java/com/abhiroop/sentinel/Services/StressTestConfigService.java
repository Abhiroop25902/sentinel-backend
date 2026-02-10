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

    public Mono<StressTestConfig> getConfig() {
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
                );
    }

    public Mono<StressTestConfig> saveConfig(StressTestConfig config) {
        return stressTestConfigRepository.save(config);
    }

    public Mono<StressTestConfig> setIsRunningFalse() {
        return getConfig().flatMap(config -> {
            if (!config.isRunning()) return Mono.just(config);

            final var updatedConfig = config.toBuilder().isRunning(false).build();

            return saveConfig(updatedConfig);
        });
    }

}
