package com.abhiroop.sentinel.Services;

import com.abhiroop.sentinel.Repository.LoginHistoryRepository;
import com.abhiroop.sentinel.dto.LoginHistoryDto;
import com.abhiroop.sentinel.dto.StressTestSummaryDto;
import com.abhiroop.sentinel.entity.StressTestConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@AllArgsConstructor
public class LoginHistoryService {
    private final LoginHistoryRepository loginHistoryRepository;
    private final StressTestConfigService stressTestConfigService;

    public Mono<LoginHistoryDto> saveLoginHistory(LoginHistoryDto loginHistoryDto) {
        return loginHistoryRepository.save(loginHistoryDto);
    }


    private boolean getRandomBoolean() {
        return Math.random() < 0.9; //90% should be successful login
    }

    public Mono<LoginHistoryDto> createSampleLoginHistory() {
        return Mono.defer(() ->
                        this.saveLoginHistory(
                                LoginHistoryDto.builder()
                                        .id(UUID.randomUUID().toString())
                                        .email("abhiroop.m25902@gmail.com")
                                        .timestamp(Instant.now().toEpochMilli()) // Instant.now() will executed when Mono is subscribed, not when built
                                        .success(getRandomBoolean())// getRandomBoolean() will executed when Mono is subscribed, not when built
                                        .build()
                        )
                )
                .delayElement(Duration.ofMillis((long) (Math.random() * 500)))
                .doOnSuccess(loginHistoryDto ->
                        log.info("Successfully Created Login History: {}", loginHistoryDto))
                .doOnError(throwable ->
                        log.error("Error Creating Login History: {}", throwable.getMessage()));
    }

    private Mono<StressTestSummaryDto> createMultipleSampleForTime(Duration duration) {
        final var startTime = Instant.now();
        final var endTime = startTime.plus(duration);
        final AtomicLong counter = new AtomicLong(0);


        return Flux.interval(Duration.ofMillis(100))
                .onBackpressureDrop(tick -> log.warn("Dropped tick {} - System at capacity!", tick))
                .takeUntil(i -> Instant.now().isAfter(endTime))
                .flatMap(tick ->
                                this.createSampleLoginHistory()
                                        .doOnSuccess(res -> counter.incrementAndGet())
                                        .onErrorResume(e ->
                                                Mono.empty()
                                        ),
                        1024,
                        1024
                )
                .then(Mono.defer(stressTestConfigService::setIsRunningFalse))
                .map(config -> StressTestSummaryDto
                        .builder()
                        .startTime(startTime)
                        .endTime(Instant.now())
                        .recordsCreated(counter.get())
                        .build()
                )
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        //boundedElastic warning is fixed by virtual threads in Spring Boot
                        stressTestConfigService.setIsRunningFalse().subscribe();
                    }
                });
    }

    public Mono<StressTestSummaryDto> createStressTest(Duration duration) {

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
                            .then(createMultipleSampleForTime(duration));
                });

    }

}
