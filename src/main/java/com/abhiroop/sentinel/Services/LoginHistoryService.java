package com.abhiroop.sentinel.Services;

import com.abhiroop.sentinel.Repository.LoginHistoryRepository;
import com.abhiroop.sentinel.entity.LoginHistory;
import com.abhiroop.sentinel.entity.StressTestConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;

@Slf4j
@Service
@AllArgsConstructor
public class LoginHistoryService {
    private final LoginHistoryRepository loginHistoryRepository;
    private final StressTestConfigService stressTestConfigService;

    public Mono<LoginHistory> createSampleLoginHistory(LoginHistory loginHistory) {
        return loginHistoryRepository.save(loginHistory);
    }

    public Mono<LoginHistory> createSampleLoginHistory() {
        return this.createSampleLoginHistory(
                        LoginHistory.builder()
                                .email("abhiroop.m25902@gmail.com")
                                .timestamp(Instant.now())
                                .success(true)
                                .build()
                )
                .doOnSuccess(loginHistory ->
                        log.info("Successfully Created Login History: {}", loginHistory))
                .doOnError(throwable ->
                        log.error("Error Creating Login History: {}", throwable.getMessage()));
    }

    private void createMultipleSampleForTime(Duration duration) {
        //execute stress test
        final var endTime = Instant.now().plus(duration);

        try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) {

            while (Instant.now().isBefore(endTime)) {
                executorService.submit(() -> this.createSampleLoginHistory().block());
                Thread.sleep(100);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stressTestConfigService.setIsRunningFalse();
        }
    }

    public void createStressTest(Duration duration) {
        // get the config to check if running is possible
        final var config = stressTestConfigService.getConfig();

        if (config.isRunning()) return;

        if (Instant.now().isBefore(config.earliestNextRun())) return;

        // now save the updated config with new config
        final var updatedConfig = StressTestConfig
                .builder()
                .id(config.id())
                .isRunning(true)
                .earliestNextRun(Instant.now().plus(Duration.ofHours(1)))
                .build();

        stressTestConfigService.saveConfig(updatedConfig);

        //start stress test
        Thread.startVirtualThread(() -> createMultipleSampleForTime(duration));
    }

}
