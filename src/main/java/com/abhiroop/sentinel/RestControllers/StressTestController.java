package com.abhiroop.sentinel.RestControllers;

import com.abhiroop.sentinel.Services.LoginHistoryService;
import com.abhiroop.sentinel.entity.StressTestSummary;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/stress-test")
@AllArgsConstructor
public class StressTestController {
    final LoginHistoryService loginHistoryService;


    @GetMapping
    public Mono<ResponseEntity<StressTestSummary>> stressTest() {
        return loginHistoryService.createStressTest(Duration.ofSeconds(30))
                .map(stressTest -> ResponseEntity
                        .status(HttpStatus.OK)
                        .body(stressTest)
                )
                .subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor()));
    }

}
