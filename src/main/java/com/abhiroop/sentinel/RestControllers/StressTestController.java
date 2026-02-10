package com.abhiroop.sentinel.RestControllers;

import com.abhiroop.sentinel.Services.LoginHistoryService;
import com.abhiroop.sentinel.entity.LoginHistory;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/stress-test")
@AllArgsConstructor
public class StressTestController {
    final LoginHistoryService loginHistoryService;


    @GetMapping
    public ResponseEntity<Mono<LoginHistory>> addSampleLoginHistory() {
        loginHistoryService.createStressTest(Duration.ofMinutes(1));
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }
}
