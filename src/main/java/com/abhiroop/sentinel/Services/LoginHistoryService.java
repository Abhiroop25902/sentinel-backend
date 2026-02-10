package com.abhiroop.sentinel.Services;

import com.abhiroop.sentinel.Repository.LoginHistoryRepository;
import com.abhiroop.sentinel.entity.LoginHistory;
import com.abhiroop.sentinel.entity.StressTestConfig;
import com.abhiroop.sentinel.entity.StressTestSummary;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

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

    private Mono<StressTestSummary> createMultipleSampleForTime(Duration duration) {
        final var startTime = Instant.now();
        final var endTime = startTime.plus(duration);
        final AtomicLong counter = new AtomicLong(0);

        final var scheduler = Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor());

        return Flux.interval(Duration.ofMillis(100))
                .takeUntil(i -> Instant.now().isAfter(endTime))
                .flatMap(tick ->
                        this.createSampleLoginHistory()
                                .subscribeOn(scheduler)
                                .doOnSuccess(res -> counter.incrementAndGet())
                                .onErrorResume(e ->
                                        Mono.empty()
                                )
                )
                .then(stressTestConfigService.setIsRunningFalse())
                .map(config -> StressTestSummary
                        .builder()
                        .startTime(startTime)
                        .endTime(Instant.now())
                        .recordsCreated(counter.get())
                        .build()
                )
                .publishOn(Schedulers.boundedElastic())
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        stressTestConfigService.setIsRunningFalse().subscribe();
                    }
                });
    }

    public Mono<StressTestSummary> createStressTest(Duration duration) {

        return stressTestConfigService.getConfig()
                .flatMap(config -> {
                    // 1. MUST use 'return' here to stop the chain
                    if (config.isRunning() || Instant.now().isBefore(config.earliestNextRun())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Test already running or cooling down"));
                    }

                    final var updatedConfig = StressTestConfig
                            .builder()
                            .id(config.id())
                            .isRunning(true)
                            .earliestNextRun(Instant.now().plus(Duration.ofHours(1)))
                            .build();

                    // now save the updated config with new config
                    return stressTestConfigService.saveConfig(updatedConfig)
                            .then(createMultipleSampleForTime(duration))
                            .subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor())); // Don't block the main Event Loop

                });

    }

}
