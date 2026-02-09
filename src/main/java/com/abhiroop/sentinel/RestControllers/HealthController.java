package com.abhiroop.sentinel.RestControllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    public record HealthResponse(String status, String timestamp){ }


    @GetMapping
    public ResponseEntity<HealthResponse> getHealth(){
        return ResponseEntity.ok().body(
                new HealthResponse("UP", java.time.Instant.now().toString())
        );
    }
}
